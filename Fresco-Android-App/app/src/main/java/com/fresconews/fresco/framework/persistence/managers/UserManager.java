package com.fresconews.fresco.framework.persistence.managers;

import android.content.Context;

import com.fresconews.fresco.framework.network.responses.NetworkAvatarResponse;
import com.fresconews.fresco.framework.network.responses.NetworkReport;
import com.fresconews.fresco.framework.network.responses.NetworkSettings;
import com.fresconews.fresco.framework.network.responses.NetworkSuccessResult;
import com.fresconews.fresco.framework.network.responses.NetworkUser;
import com.fresconews.fresco.framework.network.responses.NetworkUserCheck;
import com.fresconews.fresco.framework.network.responses.NetworkUserSettings;
import com.fresconews.fresco.framework.network.services.UserService;
import com.fresconews.fresco.framework.persistence.models.User;
import com.fresconews.fresco.framework.persistence.models.User_Table;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class UserManager {
    private static final String TAG = UserManager.class.getSimpleName();

    private UserService userService;

    private Context context;

    private NetworkUser networkUserMe;

    public UserManager(UserService userService, Context context) {
        this.userService = userService;
        this.context = context;
    }

    public Observable<User> downloadUser(String id) {
        return handleUser(userService.get(id));
    }

    public Observable<NetworkUserCheck> checkEmail(String email) {
        return userService.checkEmail(email);
    }

    public Observable<NetworkUserCheck> checkUsername(String username) {
        return userService.checkUsername(username);
    }

    public Observable<NetworkUserCheck> checkUsernameAndEmail(String username, String email) {
        return userService.checkUsernameAndEmail(username, email);
    }

    public Observable<User> getUser(String id) {
        return Observable.create(subscriber -> {
            SQLite.select()
                  .from(User.class)
                  .where(User_Table.id.eq(id))
                  .async()
                  .querySingleResultCallback((transaction, user) -> {
                      if (user != null) {
                          subscriber.onNext(user);
                      }
                      subscriber.onCompleted();
                  })
                  .execute();
        });
    }

    public Observable<User> getOrDownloadUser(String id) {
        return getUser(id).switchIfEmpty(downloadUser(id));
    }

    public Observable<NetworkUser> connectSocialTwitter(String token, String secret) {
        return userService.connectSocial("twitter", token, secret);
    }

    public Observable<NetworkUser> connectSocialFacebook(String token) {
        return userService.connectSocial("facebook", token);
    }

    public Observable<NetworkUser> connectSocialGoogle(String token) {
        return userService.connectSocialGoogle("google", token);
    }

    public Observable<NetworkUser> disconnectSocial(String platform) {
        return userService.disconnectSocial(platform);
    }

    public void disableUser(String password) {
        userService.disableUser(password);
    }

    public Observable<User> me() {
        return handleUserMe(userService.me());
    }

    public Observable<List<NetworkUserSettings>> updateAssignmentNotifications(
            boolean showFrescoNotifs, boolean showPushNotifs, boolean showSMSNotifs, boolean showEmailNotifs) {
        return userService.updateAssignmentNotifications(showFrescoNotifs, showPushNotifs, showSMSNotifs, showEmailNotifs);
    }

    public Observable<List<NetworkUserSettings>> getUserSettings() {
        return userService.getUserSettings();
    }

    public void setNotificationRadius(double radius) {
        userService.updateRadius(radius)
                   .onErrorReturn(throwable -> null)
                   .subscribe();
    }

    public Observable<NetworkUser> setUserProfile(String fullname, String bio, String location) {
        return userService.updateProfile(fullname, bio, location);
    }

    public Observable<NetworkSettings> setUsername(String username, String password) {
        return userService.updateUsername(username, password);
    }

    public Observable<NetworkSettings> setEmail(String email, String password) {
        return userService.updateEmail(email, password);
    }

    public Observable<NetworkSettings> setPassword(String newPassword, String oldPassword) {
        return userService.updatePassword(newPassword, oldPassword);
    }

    public Observable<NetworkSuccessResult> setUsernameEmail(String username, String email, String password) {
        return userService.updateUsernameEmail(username, email, password);
    }

    public Observable<NetworkSuccessResult> setSocialUsernameEmail(String username, String email, String password, String platform, String token, String secret) {
        return userService.updateSocialUsernameEmail(username, email, password, platform, token, secret);
    }

    public Observable<URI> setAvatar(File file) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("avatar", file.getName(), requestFile);
        return userService.updateAvatar(body).map(NetworkAvatarResponse::getAvatar);
    }

    public Observable<Boolean> follow(User user) {
        if (user == null) {
            return Observable.just(false);
        }
        if (user.isFollowing()) {
            return Observable.just(true);
        }

        followLocal(user);

        return userService.follow(user.getId())
                          .map(networkSuccessResult -> true)
                          .onErrorReturn(throwable -> {
                              LogUtils.e(TAG, "Error following user: " + user.getId(), throwable);
                              unfollowLocal(user);
                              return null;
                          });
    }

    public Observable<Boolean> unfollow(User user) {
        if (user == null) {
            return Observable.just(false);
        }
        if (!user.isFollowing()) {
            return Observable.just(true);
        }

        unfollowLocal(user);

        return userService.unfollow(user.getId())
                          .map(networkSuccessResult -> true)
                          .onErrorReturn(throwable -> {
                              LogUtils.e(TAG, "Error unfollowing user: " + user.getId(), throwable);
                              followLocal(user);
                              return null;
                          });
    }

    public Observable<List<User>> following(String id, int limit, String last) {
        return handleUsers(userService.following(id, limit, last));
    }

    public Observable<List<User>> following(String id, int limit) {
        return handleUsers(userService.following(id, limit));
    }

    public Observable<List<User>> followers(String id, int limit) {
        return handleUsers(userService.followers(id, limit));
    }

    public Observable<List<User>> followers(String id, int limit, String last) {
        return handleUsers(userService.followers(id, limit, last));
    }

    public Observable<List<User>> suggestions() {
        return handleUsers(userService.suggestions());
    }

    private void followLocal(User user) {
        if (user.isFollowing()) {
            return;
        }
        user.setFollowing(true);
        user.setFollowedCount(user.getFollowedCount() + 1);
        user.save();
    }

    private void unfollowLocal(User user) {
        if (!user.isFollowing()) {
            return;
        }
        user.setFollowing(false);
        user.setFollowedCount(user.getFollowedCount() - 1);
        user.save();
    }

    private Observable<User> handleUser(Observable<NetworkUser> response) {
        return response.map(networkUser -> {
            User user = User.from(networkUser);
            user.save();
            return user;
        }).onErrorReturn(throwable -> null);
    }

    private Observable<User> handleUserMe(Observable<NetworkUser> response) {
        return response.map(networkUser -> {
            User user = User.from(networkUser);
            user.save();
            setNetworkUserMe(networkUser);
            return user;
        }).onErrorReturn(throwable -> null);
    }

    public void setNetworkUserMe(NetworkUser networkUser) {
        networkUserMe = networkUser;
    }

    public NetworkUser getNetworkUserMe() {
        return networkUserMe;
    }

    private Observable<List<User>> handleUsers(Observable<List<NetworkUser>> response) {
        return response.map(networkUsers -> {
            List<User> result = new ArrayList<>(networkUsers.size());
            for (NetworkUser networkUser : networkUsers) {
                User user = User.from(networkUser);
                result.add(user);
            }
            return result;
        });
    }

    public Observable<NetworkSuccessResult> block(String id) {
        return userService.block(id);
    }

    public Observable<NetworkSuccessResult> unblock(String id) {
        return userService.unblock(id);
    }

    public Observable<NetworkReport> report(String id, String reason, String message) {
        return userService.report(id, reason, message);
    }

    public void setSmoochUser(User user) {
        Observable.just(1)
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribeOn(Schedulers.io())
                  .onErrorReturn(throwable -> null)
                  .subscribe(integer -> {
                      if (integer != null) {
                          io.smooch.core.Smooch.login(user.getId(), null);
                          io.smooch.core.User.getCurrentUser().setFirstName(user.getFullName());
                          io.smooch.core.User.getCurrentUser().setEmail(user.getEmail());
                          LogUtils.d(TAG, "Setting Smooch user - " + io.smooch.core.User.getCurrentUser().getFirstName());
                      }
                  });
    }

    public Observable<NetworkUser> acceptTerms() {
        return userService.acceptTOS();
    }

    public void updateLocation(float lng, float lat) {
        userService.location(lng, lat)
                   .onErrorReturn(throwable -> null)
                   .subscribe();
    }

    public boolean hasFieldsNeeded() {
        return getNetworkUserMe() != null && getNetworkUserMe().getNetworkIdentity() != null &&
                getNetworkUserMe().getNetworkIdentity().getFieldsNeeded() != null;
    }

    public String getUsername(String id) {
        User user = SQLite.select(User_Table.username)
                          .from(User.class)
                          .where(User_Table.id.eq(id))
                          .querySingle();
        if (user != null) {
            return user.getUsername();
        }
        return null;
    }
}
