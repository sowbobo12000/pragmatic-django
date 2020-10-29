package com.fresconews.fresco.framework.persistence.managers;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.Pair;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.network.EndpointHelper;
import com.fresconews.fresco.framework.network.requests.NetworkGalleryCreateRequest;
import com.fresconews.fresco.framework.network.responses.NetworkGallery;
import com.fresconews.fresco.framework.network.responses.NetworkPost;
import com.fresconews.fresco.framework.network.responses.NetworkPostCreateResponse;
import com.fresconews.fresco.framework.network.services.GalleryService;
import com.fresconews.fresco.framework.network.services.PostService;
import com.fresconews.fresco.framework.persistence.models.Gallery;
import com.fresconews.fresco.framework.persistence.models.GalleryCreateRequest;
import com.fresconews.fresco.framework.persistence.models.PostCreateRequest;
import com.fresconews.fresco.framework.persistence.models.UploadStatusMessage;
import com.fresconews.fresco.framework.rx.RxBus;
import com.fresconews.fresco.framework.rx.RxMediaScannerConnection;
import com.fresconews.fresco.v2.profile.ProfileActivity;
import com.fresconews.fresco.v2.utils.ContentUtils;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.MediaHelper;
import com.fresconews.fresco.v2.utils.StringUtils;
import com.fresconews.fresco.v2.utils.TranscodingUtils;

import net.ypresto.androidtranscoder.MediaTranscoder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class FrescoUploadService2 extends IntentService {
    private static final String TAG = FrescoUploadService2.class.getSimpleName();

    private static final String ACTION_UPLOAD = "com.fresconews.fresco.framework.persistence.managers.action.UPLOAD";
    private static final String ACTION_RETRY = "com.fresconews.fresco.framework.persistence.managers.action.RETRY";
    private static final String ACTION_CANCEL = "com.fresconews.fresco.framework.persistence.managers.action.CANCEL";
    private static final String UPLOAD_GALLERY = "com.fresconews.fresco.framework.persistence.managers.extra.GALLERY";

    private static final int NOTIFICATION_ID = 0x1337;

    @Inject
    GalleryService galleryService;

    @Inject
    PostService postService;

    @Inject
    FeedManager feedManager;

    @Inject
    AnalyticsManager analyticsManager;

    private static GalleryCreateRequest currentRequest;
    private static NetworkGallery response;
    private static UploadStatusMessage currentUploadStatus;
    private static boolean transcodingRequired;
    private static Map<TranscodeMedia, Double> transcodeProgressMap;

    private NotificationCompat.Builder builder;
    private Map<String, Integer> progressPerFile;
    private NotificationManager notificationManager;
    private int prevProgress = 0;
    private ReactiveLocationProvider locationProvider;

    private TransferUtility transferUtility;
    int targetBitrate = 0;
    private long startTime;
    private long endTime;
    private long takenTime;
    private double kBps;
    private int postsReady = 0;
    private String uploadErrorMessage;

    private boolean continueUploadingPosts; //used for monitoring recursion
    //if error on some post, stop uploading remaining posts. upload the rest on retry.
    private static ArrayList<String> completedUploadPostIds = new ArrayList<>();

    public FrescoUploadService2() {
        super("FrescoUploadService2");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ((Fresco2) getApplication()).getFrescoComponent().inject(this);
        notificationManager = ((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE));

        locationProvider = new ReactiveLocationProvider(this);

        AWSCredentials cred = new BasicAWSCredentials(EndpointHelper.currentEndpoint.amazonS3AccessKey, EndpointHelper.currentEndpoint.amazonS3SecretKey); // Dev
        AmazonS3 s3Client = new AmazonS3Client(cred);
        transferUtility = new TransferUtility(s3Client, this);
    }

    public static void startUpload(Context context, GalleryCreateRequest galleryCreateRequest) {
        Intent intent = new Intent(context, FrescoUploadService2.class);
        intent.setAction(ACTION_UPLOAD);
        intent.putExtra(UPLOAD_GALLERY, galleryCreateRequest);
        context.startService(intent);
    }

    public static void retryUpload(Context context) {
        Intent intent = new Intent(context, FrescoUploadService2.class);
        intent.setAction(ACTION_RETRY);
        context.startService(intent);
    }

    public static void cancelUpload(Context context) {
        Intent intent = new Intent(context, FrescoUploadService2.class);
        intent.setAction(ACTION_CANCEL);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPLOAD.equals(action)) {
                GalleryCreateRequest galleryCreateRequest = intent.getParcelableExtra(UPLOAD_GALLERY);
                currentRequest = galleryCreateRequest;
                handleActionUpload(galleryCreateRequest);
            }
            else if (ACTION_RETRY.equals(action)) {
                if (response != null) {
                    LogUtils.i(TAG, "Handle intent response wasn't null");
                    handleRetryActionUpload(currentRequest);
                }
                else {
                    LogUtils.i(TAG, "Handle intent response was null, have to retry");
                    analyticsManager.trackUploadDebug("Upload retry");
                    handleActionUpload(currentRequest);
                }
            }
            else if (ACTION_CANCEL.equals(action)) {
                handleActionCancel();
            }
        }
    }

    private void handleActionCancel() {
        analyticsManager.trackUploadDebug("Upload cancel");
        UploadStatusMessage uploadStatus = new UploadStatusMessage(UploadStatusMessage.DONE, 0);
        setCurrentUploadStatus(uploadStatus);
        RxBus.getInstance().post(uploadStatus);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void transcodeVideos(GalleryCreateRequest galleryCreateRequest, List<PostCreateRequest> transcodeVideos, int totalDuration) {

        if (transcodeProgressMap == null) {
            transcodeProgressMap = new HashMap<>();
        }
        else {
            transcodeProgressMap.clear();
        }

        for (PostCreateRequest postCreateRequest : transcodeVideos) {
            String path = ContentUtils.getMediaPath(this, postCreateRequest.getUri());
            Uri inputUri = Uri.parse(path);
            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
            metadataRetriever.setDataSource(Fresco2.getContext(), inputUri);
            LogUtils.i(TAG, "transcoding...");
            targetBitrate = TranscodingUtils.getPreferredTranscodedBitrate(inputUri);
            analyticsManager.trackTranscodeDebug(MediaHelper.getVideoBitRate(inputUri), targetBitrate);
            String outPath = TranscodingUtils.newPath(inputUri);
            TranscodeMedia transcodeMedia = new TranscodeMedia(inputUri, MediaHelper.getDuration(inputUri));
            long startTime = System.currentTimeMillis();
            AsyncTask.execute(() -> TranscodingUtils.transcode(inputUri, Uri.parse(outPath), targetBitrate, new MediaTranscoder.Listener() {
                @Override
                public void onTranscodeProgress(double progress) {
                    transcodeProgressMap.put(transcodeMedia, progress);
                    calculateProgressTranscode(transcodeProgressMap, totalDuration);
                }

                @Override
                public void onTranscodeCompleted() {
                    LogUtils.d(TAG, "Transcoding took " + (System.currentTimeMillis() - startTime) + "ms");
                    RxMediaScannerConnection.scanFile(FrescoUploadService2.this, outPath)
                                            .onErrorReturn(throwable -> {
                                                analyticsManager.trackUploadDebug("Transcoded URI error - " + throwable.getMessage());
                                                return null;
                                            })
                                            .subscribe(uri -> {
                                                postCreateRequest.setUri(uri);
                                                postsReady++;
                                                if (postsReady == transcodeVideos.size()) {
                                                    postsReady = 0;
                                                    startGeoCoding(galleryCreateRequest);
                                                }
                                            });
                }

                @Override
                public void onTranscodeCanceled() {
                    LogUtils.i(TAG, "transcoding cancelled");
                    analyticsManager.trackTranscodeError(transcodeProgressMap, totalDuration);
                }

                @Override
                public void onTranscodeFailed(Exception exception) {
                    analyticsManager.trackUploadError("Error with transcoding - " + exception.getMessage());
                    LogUtils.i(TAG, "Error with transcoding - " + exception.getMessage());
//                    uploadError(); //does absolutely nothing for the external storage double gallery amanda nelson transcoding issue
                    startGeoCoding(galleryCreateRequest); //called for each video that requires transcoding
                    transcodingRequired = false;
                }
            }));
        }
    }

    private void handleActionUpload(GalleryCreateRequest galleryCreateRequest) {
        progressPerFile = new HashMap<>();

        if (galleryCreateRequest == null) {
            analyticsManager.trackUploadDebug("Gallery create request was null again!");
            uploadError();
            return;
        }

        prevProgress = 0;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, ProfileActivity.class), PendingIntent.FLAG_ONE_SHOT);

        builder = new NotificationCompat.Builder(this)
                .setContentText(getString(R.string.uploading_gallery))
                .setSmallIcon(R.drawable.ic_fresco_white)
                .setContentIntent(pendingIntent);
        builder.setProgress(100, 10, true);
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        UploadStatusMessage uploadStatus = new UploadStatusMessage(UploadStatusMessage.UPLOADING, 0);
        setCurrentUploadStatus(uploadStatus);
        RxBus.getInstance().post(uploadStatus);

        List<PostCreateRequest> transcodeVideos = new ArrayList<>();
        int totalDuration = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            for (PostCreateRequest postCreateRequest : galleryCreateRequest.getPosts()) {
                if (postCreateRequest.isVideoType(this)) {
                    String path;
                    if (postCreateRequest.getUri().toString().startsWith("file://")) {
                        path = postCreateRequest.getUri().getPath();
                    }
                    else {
                        path = ContentUtils.getMediaPath(this, postCreateRequest.getUri());
                    }
                    if (!TranscodingUtils.isAcceptableBitrate(Uri.parse(path))) {
                        transcodeVideos.add(postCreateRequest);
                        totalDuration += MediaHelper.getDuration(Uri.parse(path));
                    }
                }
            }
        }
        if (!transcodeVideos.isEmpty()) {
            transcodingRequired = true;
            transcodeVideos(galleryCreateRequest, transcodeVideos, totalDuration);
        }
        else {
            transcodingRequired = false;
            postsReady = 0;
            startGeoCoding(galleryCreateRequest);
        }
    }

    private void handleRetryActionUpload(GalleryCreateRequest galleryCreateRequest) {
        progressPerFile = new HashMap<>();
        for (NetworkPostCreateResponse postCreateRequest : response.getNewPosts()) {
            progressPerFile.put(postCreateRequest.getPostId(), 0);
        }

        prevProgress = transcodingRequired ? 50 : 0; //don't know how this entirely makes sense. it doesn't at all. who said that transcoding worked???

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, ProfileActivity.class), PendingIntent.FLAG_ONE_SHOT);

        builder = new NotificationCompat.Builder(this)
                .setContentText(getString(R.string.uploading_gallery))
                .setSmallIcon(R.drawable.ic_fresco_white)
                .setContentIntent(pendingIntent);
        builder.setProgress(100, 10, true);
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        UploadStatusMessage uploadStatus = new UploadStatusMessage(UploadStatusMessage.UPLOADING, transcodingRequired ? 50 : 0);
        setCurrentUploadStatus(uploadStatus);
        RxBus.getInstance().post(uploadStatus);

        LogUtils.i(TAG, "Expected number of Addresses: " + galleryCreateRequest.getPosts().size());
        LogUtils.i(TAG, "an address - " + response.getNewPosts()[0].getPostId());

        Observable.just(galleryCreateRequest.getPosts())
                  .flatMapIterable(x -> x)
                  .flatMap(post -> getAddress(post.getLat(), post.getLng()), Pair::new, 2)
                  .doOnCompleted(() -> retryUpload(galleryCreateRequest, response))
                  .subscribe(pair -> {
                      pair.first.setAddress(pair.second);
                  });

    }

    private void retryUpload(GalleryCreateRequest galleryCreateRequest, NetworkGallery networkGalleryCreateResponse) {
        continueUploadingPosts = true;
        analyticsManager.trackUploadDebug("Upload retry");
        Observable.just(networkGalleryCreateResponse.getNewPosts())
                  .flatMapIterable(Arrays::asList)
                  .zipWith(galleryCreateRequest.getPosts(), Pair::new) //idt i need to zip it
                  .flatMap(networkPostCreateResponsePostCreateRequestPair -> { //catchy
                      NetworkPostCreateResponse postResponse = networkPostCreateResponsePostCreateRequestPair.first;
                      PostCreateRequest postRequest = networkPostCreateResponsePostCreateRequestPair.second;
                      LogUtils.i(TAG, "RETRY About to call amazon upload.");
                      if (completedUploadPostIds.contains(postResponse.getPostId())) {
                          //mark a ++ in the progress bar.
                          updateProgress(100, postResponse.getPostId());
                          LogUtils.i(TAG, "Contained this post - " + postResponse.getPostId());
                          //go to the next upload and skip this ish
                          return Observable.just(false);
                      }

                      if (continueUploadingPosts) {
                          return observableAmazonUploadListenerWrapper(this, postResponse.getKey(), postRequest.getUri(), postResponse.getPostId());
                      }
                      else {
                          return Observable.just(false);
                      }

                  })
                  .doOnError(throwable -> {
                      LogUtils.e(TAG, "RETRY Error uploading post", throwable);
                      continueUploadingPosts = false;
                      //                    uploadError(); //ehhhhhhhhhhh don't do this yet. do this once we're done iterating.
                  })
                  .onErrorResumeNext(throwable -> Observable.empty())
                  .doOnCompleted(() -> {
                      if (!continueUploadingPosts) {
                          LogUtils.i(TAG, "RETRY don't continue uploading posts, don't create Gallery");

                          uploadError(uploadErrorMessage);
                          return;
                      }

                      //This just means it's done iterating
                      LogUtils.i(TAG, "FINALLY DONE ITERATING, CREATING A GALLERY IN THE DB - RETRY");
                      uploadComplete();
                      NetworkGallery networkGallery = response;
                      NetworkPost[] posts = new NetworkPost[networkGalleryCreateResponse.getPosts().length];
                      for (int i = 0; i < networkGalleryCreateResponse.getPosts().length; i++) {
                          PostCreateRequest postRequest = galleryCreateRequest.getPosts().get(i);
                          if (response == null) {
                              analyticsManager.trackUploadError("Post Request Network Gallery was null");
                              uploadError(uploadErrorMessage);
                              return;
                          }
                          NetworkPostCreateResponse postResponse = response.getNewPosts()[i];

                          NetworkPost post = new NetworkPost();
                          post.setId(postResponse.getPostId());
                          post.setAddress(postRequest.getAddress());

                          post.setImage(postRequest.getUri().toString());

                          if (postRequest.getMediaType(this).equals("video/mp4")) {
                              post.setStream(postRequest.getUri().toString());
                          }

                          post.setOwner(response.getOwner());

                          posts[i] = post;

                      }
                      networkGallery.setPosts(posts);
                      networkGallery.setCreatedAt(new Date());

                      Gallery gallery = Gallery.from(networkGallery);
                      gallery.save();
                  })
                  .subscribe(booleanObservable -> {
                      LogUtils.i(TAG, "POST COMPLETED");
                  });
    }

    //ATTEMPT AT ASYNC
    private void createGallery(GalleryCreateRequest galleryCreateRequest) {
        LogUtils.i(TAG, "Actual upload called.");
        NetworkGalleryCreateRequest networkGalleryCreateRequest = galleryCreateRequest.toNetwork(this);
        response = null;
        galleryService.create(networkGalleryCreateRequest)
                      .doOnError(throwable -> {
                          LogUtils.e(TAG, "Error creating gallery-- " + throwable.getMessage());
                          analyticsManager.trackUploadError("Error creating gallery -- " + throwable.getMessage());
                          uploadError(uploadErrorMessage);
                      })
                      .onErrorReturn(throwable -> null)
                      .map(networkGalleryCreateResponse -> {
                          //Throw the camera upload error thing up then
                          LogUtils.i(TAG, "Got networkGalleryCreateResponse.");

                          response = networkGalleryCreateResponse;
                          if (response == null) {
                              uploadError(uploadErrorMessage);
                              analyticsManager.trackUploadError("Network Gallery Create Response was null");
                              return new NetworkPostCreateResponse[0]; // that way we wont iterate over the posts
                          }
                          for (NetworkPostCreateResponse postCreateRequest : networkGalleryCreateResponse.getNewPosts()) {
                              progressPerFile.put(postCreateRequest.getPostId(), 0);
                          }
                          continueUploadingPosts = true;

                          return networkGalleryCreateResponse.getNewPosts();
                      })
                      .flatMapIterable(Arrays::asList) //we're going to iterate over the posts
                      .zipWith(galleryCreateRequest.getPosts(), Pair::new) //idt i need to zip it
                      .flatMap(networkPostCreateResponsePostCreateRequestPair -> { //catchy
                          NetworkPostCreateResponse postResponse = networkPostCreateResponsePostCreateRequestPair.first;
                          PostCreateRequest postRequest = networkPostCreateResponsePostCreateRequestPair.second;
                          LogUtils.i(TAG, "About to call amazon upload.");
                          if (continueUploadingPosts) {
                              return observableAmazonUploadListenerWrapper(this, postResponse.getKey(), postRequest.getUri(), postResponse.getPostId());
                          }
                          else {
                              return Observable.just(false);
                          }

                      })
                      .doOnError(throwable -> {
                          LogUtils.e(TAG, "Error uploading post", throwable);
                          continueUploadingPosts = false;
//                    uploadError(); //ehhhhhhhhhhh don't do this yet. do this once we're done iterating.
                      })
                      .onErrorResumeNext(throwable -> Observable.empty())
                      .doOnCompleted(() -> {

                          if (!continueUploadingPosts) {
                              uploadError(uploadErrorMessage);
                              return;
                          }

                          //This just means it's done iterating
                          LogUtils.i(TAG, "FINALLY DONE ITERATING, CREATING A GALLERY IN THE DB");
                          uploadComplete();
                          NetworkGallery networkGallery = response;
                          NetworkPost[] posts = new NetworkPost[networkGalleryCreateRequest.getPosts().length];
                          for (int i = 0; i < networkGalleryCreateRequest.getPosts().length; i++) {
                              PostCreateRequest postRequest = galleryCreateRequest.getPosts().get(i);
                              if (response == null) {
                                  analyticsManager.trackUploadError("Post Request Network Gallery was null");
                                  uploadError();
                                  return;
                              }
                              NetworkPostCreateResponse postResponse = response.getNewPosts()[i];

                              NetworkPost post = new NetworkPost();
                              post.setId(postResponse.getPostId());
                              post.setAddress(postRequest.getAddress());

                              post.setImage(postRequest.getUri().toString());

                              if (postRequest.getMediaType(this).equals("video/mp4")) {
                                  post.setStream(postRequest.getUri().toString());
                              }

                              post.setOwner(response.getOwner());
//                          LogUtils.i(TAG, "captured at - "+post.getCapturedAt().toString());

                              posts[i] = post;
                          }
                          networkGallery.setPosts(posts);
                          networkGallery.setCreatedAt(new Date());

                          Gallery gallery = Gallery.from(networkGallery);
                          gallery.save();
                      })
                      .subscribe(booleanObservable -> {
                          LogUtils.i(TAG, "POST COMPLETED");
                      });
    }

    private Observable<Boolean> observableAmazonUploadListenerWrapper(Context context, String objectKey, Uri uri, String postId) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {

//                File postFile = ContentUtils.uriToFile(this, uri);
//                File postFile = ContentUtils.getFileFromUri(uri, this);
//                File postFile = ContentUtils.uriToInputStreamToCachedFile(context, uri, objectKey);
                String realPath = ContentUtils.getMediaPath(context, uri);
                File postFile = new File(realPath);

                if (postFile == null || uri == null || StringUtils.toNullIfEmpty(uri.toString()) == null) {
                    LogUtils.e(TAG, "File was null");
                    analyticsManager.trackUploadDebug("Post file in observableAmazonWrapper was null ... uri - " + uri.getPath());
                    subscriber.onError(new Throwable("Post file was null"));
                }
                ObjectMetadata postMetaData = new ObjectMetadata();

                //create a map to store user metadata
                Map<String, String> userMetadata = new HashMap<String, String>();
                userMetadata.put("post_id", postId);

                //call setUserMetadata on our ObjectMetadata object, passing it our map
                postMetaData.setUserMetadata(userMetadata);

                startTime = System.currentTimeMillis();
                TransferObserver observer = transferUtility.upload(
                        EndpointHelper.currentEndpoint.amazonS3Bucket,     /* The bucket to upload to */
                        "raw/" + objectKey,    /* The key for the uploaded object */
                        postFile,        /* The file where the data to upload exists */
                        postMetaData  /* The ObjectMetadata associated with the object*/
                );

                observer.setTransferListener(new TransferListener() {
                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        // do something
                        if (state == TransferState.COMPLETED) {
                            LogUtils.i(TAG, "Transfer compelted" + " id - " + Integer.toString(id) + " postId - " + postId + " with speed - " + Double.toString(kBps));
                            analyticsManager.trackUploadDebug("Post completed uploading", kBps);
                            completedUploadPostIds.add(postId);
                            observer.cleanTransferListener();
                            subscriber.onCompleted();
                        }
                        else if (state == TransferState.FAILED) {
                            LogUtils.i(TAG, "Transfer State - Failed" + " id - " + Integer.toString(id));
                            transferUtility.cancel(id);
                            subscriber.onError(new Throwable("Transfer has finally failed for post - " + postId)); //doesnt' trigger for timeout?
                        }
                        else if (state == TransferState.WAITING_FOR_NETWORK) {
                            LogUtils.i(TAG, "Transfer State - Waiting for network" + " id - " + Integer.toString(id));
                            transferUtility.cancel(id);
                            subscriber.onError(new Throwable("Transfer is waiting for network for post - " + postId));
                        }
                        else if (state == TransferState.WAITING) {
                            LogUtils.i(TAG, "Transfer State - Waiting..." + " id - " + Integer.toString(id));
//                            transferUtility.cancel(id);
//                            subscriber.onError(new Throwable("Transfer has finally failed for post - " + postId));
                        }
                        else if (state == TransferState.CANCELED) {
                            LogUtils.i(TAG, "Transfer State - Cancelled" + " id - " + Integer.toString(id));
                            transferUtility.cancel(id);
                            subscriber.onError(new Throwable("Transfer has cancelled for post - " + postId));
                        }
                        else if (state == TransferState.PENDING_CANCEL) {
                            LogUtils.i(TAG, "Transfer State - Pending Cancel" + " id - " + Integer.toString(id));
                            transferUtility.cancel(id);
                            subscriber.onError(new Throwable("Transfer pending cancel for post - " + postId));
                        }
                        else if (state == TransferState.PENDING_NETWORK_DISCONNECT) {
                            LogUtils.i(TAG, "Transfer State - Pending Network Disconnect" + " id - " + Integer.toString(id));
                            transferUtility.cancel(id);
                            subscriber.onError(new Throwable("Transfer pending disconnect for post- " + postId));
                        }
                        else if (state == TransferState.UNKNOWN) {
                            LogUtils.i(TAG, "Transfer State - Unknown" + " id - " + Integer.toString(id));
                            transferUtility.cancel(id);
                            subscriber.onError(new Throwable("Transfer state unknown for post - " + postId));
                        }
                        else if (state == TransferState.PAUSED) {
                            LogUtils.i(TAG, "Transfer State - Paused" + " id - " + Integer.toString(id));
                            subscriber.onError(new Throwable("Transfer has paused for post - " + postId));
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                        if (bytesTotal != 0) {
                            int percentage = (int) ((double) bytesCurrent / (double) bytesTotal * 100);

                            //Display percentage transfered to user
                            LogUtils.i(TAG, objectKey + "Progress: " + Integer.toString(percentage));
                            updateProgress(percentage, postId);

                            long dataSize = bytesCurrent / 1024;
                            endTime = System.currentTimeMillis();
                            takenTime = endTime - startTime;
                            long s = takenTime / 1000;
                            if (s != 0) {
                                kBps = dataSize / s;
                            }
                        }
                    }

                    @Override
                    public void onError(int id, Exception ex) {
                        LogUtils.e(TAG, "Amazon upload listener error: " + ex.getMessage());
                        uploadErrorMessage = ex.getMessage();
                        analyticsManager.trackUploadError("Amazon upload listener error: " + ex.getMessage(), kBps);
                        subscriber.onError(new Throwable("Amazon upload listener error - " + ex.getMessage())); //should also trigger for timeout...which is good. Monitor that there is no Upload debug - Gallery upload complete
                    }
                });

            }
        });
    }

    private void startGeoCoding(GalleryCreateRequest galleryCreateRequest) {
        Observable.just(galleryCreateRequest.getPosts())
                  .flatMapIterable(postCreateRequests -> postCreateRequests)
                  .flatMap(post -> getAddress(post.getLat(), post.getLng()), Pair::new, 2)
                  .doOnCompleted(() -> createGallery(galleryCreateRequest))
                  .subscribe(pair -> {
                      pair.first.setAddress(pair.second);
                  });
    }

    public Observable<String> getAddress(double latitude, double longitude) {
        return locationProvider.getReverseGeocodeObservable(Locale.getDefault(), latitude, longitude, 1)
                               .subscribeOn(Schedulers.io())
                               .map(addresses -> {
                                   if (addresses.isEmpty()) {
                                       analyticsManager.trackGeocodingError("Failed to resolve address from Lat: " + Double.toString(latitude) + " and Long: " + Double.toString(longitude));
                                       return "No Address";
                                   }
                                   else {
                                       Address address = addresses.get(0);
                                       StringBuilder addressBuilder = new StringBuilder();
                                       for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                                           addressBuilder.append(address.getAddressLine(i));
                                           addressBuilder.append(" ");
                                       }
                                       return addressBuilder.toString();
                                   }
                               })
                               .onErrorReturn(throwable -> {
                                   analyticsManager.trackGeocodingError(throwable.getMessage() + " with Lat: " + Double.toString(latitude) + " and Long: " + Double.toString(longitude));
                                   return "No Address";
                               });
    }

    private void uploadError() {
        uploadError(null);
    }

    private void uploadError(String errorMessage) {
        feedManager.clearUploadingGalleries();

        if (builder == null) {
            builder = new NotificationCompat.Builder(this)
                    .setContentText(getString(R.string.upload_failed))
                    .setSmallIcon(R.drawable.ic_fresco_white);
        }

        builder.setProgress(0, 0, false);
        builder.setContentTitle("Upload Failed");

        Intent cancelIntent = new Intent(this, FrescoUploadService2.class);
        cancelIntent.setAction(ACTION_CANCEL);
        PendingIntent cancelPending = PendingIntent.getService(this, 1, cancelIntent, 0);

        Intent retryIntent = new Intent(this, FrescoUploadService2.class);
        retryIntent.setAction(ACTION_RETRY);
        PendingIntent retryPending = PendingIntent.getService(this, 1, retryIntent, 0);

        builder.mActions.clear();

        builder.addAction(R.drawable.ic_close, getString(R.string.cancel), cancelPending);
        builder.addAction(R.drawable.ic_refresh, getString(R.string.retry), retryPending);
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        UploadStatusMessage uploadStatus = null;
        if (StringUtils.toNullIfEmpty(errorMessage) == null) {
            uploadStatus = new UploadStatusMessage(UploadStatusMessage.ERROR, 0);
        }
        else if (!errorMessage.contains("timeout")) {
            String timeoutErrorMessage = getResources().getString(R.string.error_timeout);
            uploadStatus = new UploadStatusMessage(UploadStatusMessage.ERROR, 0, timeoutErrorMessage);
        }
        else {
            String defaultErrorMessage = getResources().getString(R.string.error_upload_default);
            uploadStatus = new UploadStatusMessage(UploadStatusMessage.ERROR, 0, defaultErrorMessage);
        }

        setCurrentUploadStatus(uploadStatus);
        RxBus.getInstance().post(uploadStatus);
        LogUtils.e(TAG, "Error uploading gallery");
        analyticsManager.trackUploadDebug("Upload error shown to user");
    }

    private void uploadComplete() {
        if (currentUploadStatus.getUploadStatus() == UploadStatusMessage.ERROR) {
            return;
        }

        if (builder == null) {
            builder = new NotificationCompat.Builder(this)
                    .setContentText(getString(R.string.uploading))
                    .setSmallIcon(R.drawable.ic_fresco_white);
        }

        builder.setProgress(0, 0, false);
        builder.setContentTitle("Upload Complete");
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        UploadStatusMessage uploadStatus = new UploadStatusMessage(UploadStatusMessage.DONE, 0);
        setCurrentUploadStatus(uploadStatus);
        RxBus.getInstance().post(uploadStatus);
        LogUtils.i(TAG, "Gallery Upload Complete");
        analyticsManager.trackUploadDebug("Gallery upload complete");

        if (transcodeProgressMap != null) {
            for (Map.Entry<TranscodeMedia, Double> entry : transcodeProgressMap.entrySet()) {
                TranscodeMedia transcodeMedia = entry.getKey();
                RxMediaScannerConnection.deleteAndScanFile(this, TranscodingUtils.newPath(transcodeMedia.uri));
            }
        }
    }

    private void updateProgress(int progress, String uri) {
        progressPerFile.put(uri, progress);

        int totalDone = 0;
        for (int fileProgress : progressPerFile.values()) {
            totalDone += fileProgress;
        }

        int totalProgress = 0;
        totalProgress += (int) ((totalDone / (float) (progressPerFile.size() * 100)) * 100);

        if (totalProgress - prevProgress == 0) {
            return;
        }

        if (transcodingRequired) {
            totalProgress = (totalProgress / 2) + 50;
        }

        prevProgress = totalProgress;

        if (totalProgress < 10) { //upload progress must be at least at 10%
            totalProgress = 10;
        }

        builder.setProgress(100, totalProgress, false); //push notification progress, you can set your own maximum
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        UploadStatusMessage uploadStatus = new UploadStatusMessage(UploadStatusMessage.UPLOADING, totalProgress); //totalProgress for toolbar must be a maximum of 100
        setCurrentUploadStatus(uploadStatus);
        RxBus.getInstance().post(uploadStatus);
    }

    private void calculateProgressTranscode(Map<TranscodeMedia, Double> progressMap, int totalDuration) {
        int transcodeProgress = 0;

        for (Map.Entry<TranscodeMedia, Double> entry : progressMap.entrySet()) {
            TranscodeMedia transcodeMedia = entry.getKey();
            double progress = entry.getValue();
            transcodeProgress += (int) Math.round((transcodeMedia.duration * progress / totalDuration) * 100);
        }
        if (transcodeProgress < 20) { //upload bar must be at least at 10%
            transcodeProgress = 20;
        }

        builder.setProgress(100, transcodeProgress / 2, false);
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        UploadStatusMessage uploadStatus = new UploadStatusMessage(UploadStatusMessage.UPLOADING, transcodeProgress / 2);
        setCurrentUploadStatus(uploadStatus);
        RxBus.getInstance().post(uploadStatus);
    }

    //This is where the toolbar in ActivityViewModel derives its upload percentage. Progress MUST BE 100 MAX.
    private static void setCurrentUploadStatus(UploadStatusMessage uploadStatus) {
        currentUploadStatus = uploadStatus;
    }

    public static UploadStatusMessage getCurrentUploadStatus() {
        if (currentUploadStatus == null) {
            currentUploadStatus = new UploadStatusMessage(UploadStatusMessage.DONE, 0);
        }
        return currentUploadStatus;
    }

    class TranscodeMedia {
        Uri uri;
        int duration;

        TranscodeMedia(Uri uri, int duration) {
            this.uri = uri;
            this.duration = duration;
        }
    }
}
