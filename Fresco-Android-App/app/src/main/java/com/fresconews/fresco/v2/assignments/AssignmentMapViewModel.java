package com.fresconews.fresco.v2.assignments;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.Bindable;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.managers.AssignmentManager;
import com.fresconews.fresco.framework.persistence.managers.AuthManager;
import com.fresconews.fresco.framework.persistence.managers.FrescoLocationManager;
import com.fresconews.fresco.framework.persistence.managers.NotificationFeedManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.framework.persistence.models.Assignment;
import com.fresconews.fresco.framework.persistence.models.Outlet;
import com.fresconews.fresco.framework.persistence.models.Session;
import com.fresconews.fresco.framework.rx.RxGoogleMaps;
import com.fresconews.fresco.v2.notificationfeed.NotificationFeedActivity;
import com.fresconews.fresco.v2.notifications.INotificationActivityViewModel;
import com.fresconews.fresco.v2.utils.BitmapUtils;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.MathUtils;
import com.fresconews.fresco.v2.utils.SnackbarUtil;
import com.fresconews.fresco.v2.utils.StringUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class AssignmentMapViewModel extends ActivityViewModel<AssignmentMapActivity> implements INotificationActivityViewModel {
    private static final String TAG = AssignmentMapViewModel.class.getSimpleName();

    private static final float METERS_IN_MILE = 1609.34f;
    private static final int ANIM_DURATION = 150;
    private static final LatLng DEFAULT_COORD = new LatLng(40.7128, -74.0059);

    @Inject
    SessionManager sessionManager;

    @Inject
    UserManager userManager;

    @Inject
    NotificationFeedManager notificationFeedManager;

    @Inject
    AssignmentManager assignmentManager;

    @Inject
    FrescoLocationManager frescoLocationManager;

    @Inject
    AuthManager authManager;

    @Inject
    AnalyticsManager analyticsManager;

    public BindableView<ViewGroup> assignmentBottomSheet = new BindableView<>();
    public BindableView<ViewGroup> globalFooter = new BindableView<>();

    private GoogleMap map;
    public BottomSheetBehavior assignmentBottomSheetBehavior;

    private Map<String, AssignmentOnMap> assignmentsOnMap;
    private Map<String, String> markersToAssignments;
    private boolean green;
    private String acceptedAssignmentId;
    public BindableView<Toolbar> acceptedToolbar = new BindableView<>();
    public BindableView<Toolbar> inRangeToolbar = new BindableView<>();
    private boolean acceptedAnAssignment = false;
    private boolean triggernow = false;
    private boolean alreadyNotifiedOfExpiration = false;
    private float notificationRadiusMiles = -1;

    private boolean globalHidden;

    private Assignment selectedAssignment;
    private LatLng userLocation;

    private int globalAssignments;

    private List<Outlet> outlets;

    private String avatarUrl;
    private int unreadNotifications;

    private String assignmentId;

    public AssignmentMapViewModel(AssignmentMapActivity activity, String assignmentId) {
        super(activity);
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);

        setTitle(activity.getString(R.string.assignments));
        setNavIcon(R.drawable.ic_menu);

        this.assignmentId = assignmentId;

        selectedAssignment = null;
        assignmentsOnMap = new HashMap<>();
        markersToAssignments = new HashMap<>();

        if (sessionManager.isLoggedIn()) {
            String userId = sessionManager.getCurrentSession().getUserId();
            userManager.getUser(userId)
                       .onErrorReturn(throwable -> null)
                       .subscribe(user -> {
                           if (user != null && user.getRadius() != null) {
                               notificationRadiusMiles = Float.parseFloat(user.getRadius());
                               setAvatarUrl(user.getAvatar());
                           }
                           notifyPropertyChanged(BR.avatarUrl);
                       });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isLoggedIn()) {
            notificationSubscription = notificationFeedManager.pollUnseenNotificationsCount()
                                                              .observeOn(AndroidSchedulers.mainThread())
                                                              .onErrorReturn(throwable -> null)
                                                              .subscribe(integer -> {
                                                                  if (integer == null) {
                                                                      setUnreadNotifications(0);
                                                                  }
                                                                  else {
                                                                      setUnreadNotifications(integer);
                                                                  }
                                                                  notifyPropertyChanged(BR.unreadNotifications);
                                                              });
        }
    }

    @Override
    public void onBound() {
        if (hasBeenBound) {
            return;
        }
        super.onBound();

        assignmentBottomSheetBehavior = BottomSheetBehavior.from(assignmentBottomSheet.get());
        closeDrawer();

        assignmentBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    ObjectAnimator animator = ObjectAnimator.ofFloat(globalFooter, "y", globalFooter.get().getY(), globalFooter.get().getY() - globalFooter.get().getHeight()); //TODO is this the onbound error?
                    animator.setDuration(ANIM_DURATION);
                    animator.start();
                    globalFooter.get().setVisibility(View.VISIBLE);
                    globalHidden = false;
                }
                else if (!globalHidden) {
                    globalHidden = true;
                    ObjectAnimator animator = ObjectAnimator.ofFloat(globalFooter, "y", globalFooter.get().getY(), globalFooter.get().getY() + globalFooter.get().getHeight());
                    animator.setDuration(ANIM_DURATION);
                    animator.start();
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            globalFooter.get().setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        SupportMapFragment mapFragment = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map));
        RxGoogleMaps.getMap(mapFragment)
                    .map(googleMap -> map = googleMap)
                    .compose(new RxPermissions(getActivity()).ensure(Manifest.permission.ACCESS_FINE_LOCATION))
                    .onErrorReturn(throwable -> null)
                    .subscribe(granted -> {
                        if (map != null) {
                            //noinspection MissingPermission
                            map.setIndoorEnabled(false);
                            //noinspection MissingPermission
                            map.setMyLocationEnabled(granted);
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                            MapsInitializer.initialize(getActivity());
                        }
                        initMap(granted);
                    });

        loadGlobalAssignments();
    }

    @Override
    public void refreshHeader(Session session) {
        super.refreshHeader(session);
        refreshNotifications();
    }

    private void refreshNotifications() {
        if (sessionManager.isLoggedIn()) {
            notificationFeedManager.getUnseenNotificationsCount()
                                   .observeOn(AndroidSchedulers.mainThread())
                                   .onErrorReturn(throwable -> null)
                                   .subscribe(integer -> {
                                       if (integer == null) {
                                           setUnreadNotifications(0);
                                       }
                                       else {
                                           setUnreadNotifications(integer);
                                       }
                                       notifyPropertyChanged(BR.unreadNotifications);
                                   });
        }
    }

    private void initMap(boolean granted) {
        if (granted) {
            if (frescoLocationManager.getReactiveLocationProvider() == null) {
                frescoLocationManager.setReactiveLocationProvider(new ReactiveLocationProvider(getActivity()));
            }
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            frescoLocationManager.getReactiveLocationProvider()
                                 .getLastKnownLocation()
                                 .defaultIfEmpty(null)
                                 .onErrorReturn(throwable -> null)
                                 .subscribe(location -> {
                                     if (location != null) {
                                         frescoLocationManager.setUserLocation(new LatLng(location.getLatitude(), location.getLongitude()));
                                         if (!TextUtils.isEmpty(assignmentId)) {
                                             assignmentManager.downloadAssignment(assignmentId)
                                                              .observeOn(AndroidSchedulers.mainThread())
                                                              .onErrorReturn(throwable -> null)
                                                              .filter(assignment -> assignment != null)
                                                              .onErrorReturn(throwable -> null)
                                                              .subscribe(assignment -> {
                                                                  if (assignment == null) {
                                                                      return;
                                                                  }
                                                                  LatLng assignmentLoc = new LatLng(assignment.getLatitude(), assignment.getLongitude());
                                                                  float radiusMeters = assignment.getRadius() * METERS_IN_MILE;

                                                                  zoomToLocation(assignmentLoc, radiusMeters);
                                                                  setAssignment(assignment, frescoLocationManager.getUserLocation());
                                                              });
                                         }
                                         else {
                                             zoomToLocation(location);
                                         }
                                     }
                                 });
        }
        else {
            if (!TextUtils.isEmpty(assignmentId)) {
                assignmentManager.downloadAssignment(assignmentId)
                                 .observeOn(AndroidSchedulers.mainThread())
                                 .onErrorReturn(throwable -> null)
                                 .filter(assignment -> assignment != null)
                                 .onErrorReturn(throwable -> null)
                                 .subscribe(assignment -> {
                                     if (assignment != null) {
                                         LatLng assignmentLoc = new LatLng(assignment.getLatitude(), assignment.getLongitude());
                                         float radiusMeters = assignment.getRadius() * METERS_IN_MILE;
                                         zoomToLocation(assignmentLoc, radiusMeters);
                                         setAssignment(assignment, frescoLocationManager.getUserLocation());
                                         openDrawer();
                                     }
                                 });
            }
            else {
                zoomToLocation(DEFAULT_COORD);
            }
            SnackbarUtil.dismissableSnackbar(getRoot(), R.string.permission_location_denied);
        }

        RxGoogleMaps.onCameraChanged(map)
                    .map(cameraPosition -> {
                        loadGlobalAssignments();
                        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
                        float radiusInMiles = (float) SphericalUtil.computeDistanceBetween(bounds.northeast, bounds.southwest) * 0.000621371f;
                        return new Pair<>(bounds.getCenter(), radiusInMiles);
                    })
                    .flatMap(latLngRadius -> assignmentManager.find(latLngRadius.first, latLngRadius.second))
                    .flatMapIterable(x -> x)
                    .filter(assignment -> assignment != null && !assignment.isGlobal())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorReturn(throwable -> null)
                    .subscribe(assignment -> {
                        if (assignment == null || assignmentsOnMap.containsKey(assignment.getId())) {
                            return; //Assignment already exists, skip
                        }

                        LatLng assignmentLoc = new LatLng(assignment.getLatitude(), assignment.getLongitude());
                        float radiusMeters = assignment.getRadius() * METERS_IN_MILE;

                        Marker marker = null;
                        Circle circle = null;

                        marker = map.addMarker(new MarkerOptions()
                                .position(assignmentLoc)
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapUtils.getBitmapDescriptor(getActivity(), R.drawable.map_assignment_circle)));

                        circle = map.addCircle(new CircleOptions()
                                .center(assignmentLoc)
                                .radius(radiusMeters)
                                .strokeColor(Color.TRANSPARENT)
                                .fillColor(ContextCompat.getColor(getActivity(), R.color.fresco_assignment_radius)));

                        assignmentsOnMap.put(assignment.getId(), new AssignmentOnMap(assignment, marker, circle));
                        markersToAssignments.put(marker.getId(), assignment.getId());
                        if (userLocation != null) {
                            updateMap(userLocation);
                        }
                        if (assignmentId != null && assignmentId.equals(assignment.getId())) {
                            AssignmentOnMap data = assignmentsOnMap.get(markersToAssignments.get(marker.getId()));
                            zoomToLocation(circle.getCenter(), circle.getRadius());
                            setAssignment(data.assignment, frescoLocationManager.getUserLocation());
                            openDrawer();
                        }
                    });

        RxGoogleMaps.onMarkerClicked(map)
                    .filter(marker -> marker != null)
                    .onErrorReturn(throwable -> null)
                    .subscribe(marker -> {
                        if (marker != null) {
                            AssignmentOnMap data = assignmentsOnMap.get(markersToAssignments.get(marker.getId()));
                            Circle circle = data.circle;

                            zoomToLocation(circle.getCenter(), circle.getRadius());
                            setAssignment(data.assignment, frescoLocationManager.getUserLocation());
                            openDrawer();
                        }
                    });
    }

    private void loadGlobalAssignments() {
        assignmentManager.getGlobalAssignments()
                         .onErrorReturn(throwable -> null)
                         .subscribe(assignments -> {
                             if (assignmentBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                                 if (assignments == null) {
                                     setGlobalAssignments(0);
                                 }
                                 else {
                                     setGlobalAssignments(assignments.size());
                                 }
                             }
                         });
    }

    private void zoomToLocation(Location location) {
        if (location == null) {
            zoomToLocation(DEFAULT_COORD);
        }
        else {
            zoomToLocation(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }

    private void zoomToLocation(LatLng coords) {
        if (coords == null) {
            coords = DEFAULT_COORD; //Default to NYC
            SnackbarUtil.dismissableSnackbar(getRoot(), R.string.location_not_found);
        }
        try {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(coords, 12));
        }
        catch (Exception e) {
            //It's a null pointer on camera update factory not being initialized.
            //This only happens if Google Play Services isn't on the phone and you try to zoom to location
            LogUtils.e(TAG, e.getMessage());
        }
    }

    private void zoomToLocation(LatLng coords, double radiusMeters) {
        if (map != null) {
            LatLngBounds circleBounds = new LatLngBounds.Builder()
                    .include(SphericalUtil.computeOffset(coords, radiusMeters, 0))
                    .include(SphericalUtil.computeOffset(coords, radiusMeters, 90))
                    .include(SphericalUtil.computeOffset(coords, radiusMeters, 180))
                    .include(SphericalUtil.computeOffset(coords, radiusMeters, 270))
                    .build();

            map.animateCamera(CameraUpdateFactory.newLatLngBounds(circleBounds, 10));
        }
    }

    private void openDrawer() {
        LogUtils.i(TAG, "set A open drawer");

        assignmentBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public void closeDrawer() {
        LogUtils.i(TAG, "set A close drawer");
        if (selectedAssignment != null && userLocation != null) {
            LatLng assLatLng = new LatLng(selectedAssignment.getLatitude(), selectedAssignment.getLongitude());
            double distanceMeters = SphericalUtil.computeDistanceBetween(assLatLng, userLocation);
            double distanceMiles = distanceMeters / 1609.34;
            analyticsManager.assignmentDismissed(selectedAssignment.getId(), MathUtils.round(distanceMiles, 2));
        }
        assignmentBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void setAssignment(Assignment selectedAssignment, LatLng userLoc) {
        this.selectedAssignment = selectedAssignment;
        LogUtils.i(TAG, "set Assignment");
        setAcceptButtonText();
        //if you're looking at an assignment you're not in the radius off that isn't assceptable, makeCamera yello
        LatLng assLatLng = new LatLng(selectedAssignment.getLatitude(), selectedAssignment.getLongitude());
        float radius = selectedAssignment.getRadius() * METERS_IN_MILE;

        //Mark the assignment was clicked on
        if (userLoc != null) {
            double distanceMeters = SphericalUtil.computeDistanceBetween(assLatLng, userLoc);
            double distanceMiles = distanceMeters / 1609.34;
            analyticsManager.assignmentClicked(selectedAssignment.getId(), MathUtils.round(distanceMiles, 2));
        }

        if (withinRadius(userLoc, assLatLng, radius) && selectedAssignment.isAcceptable() && sessionManager.isLoggedIn()) {
            setCameraGreen(true);
        }
        else {
            setCameraGreen(false);
        }
        userLocation = userLoc;
        notifyPropertyChanged(BR.assignmentTitle);
        notifyPropertyChanged(BR.caption);
        notifyPropertyChanged(BR.expirationString);
        notifyPropertyChanged(BR.approvedTime);
        notifyPropertyChanged(BR.distanceString);
        notifyPropertyChanged(BR.haveUserLocation);

        assignmentManager.getOutletsForAssignment(selectedAssignment.getId())
                         .onErrorReturn(throwable -> null)
                         .subscribe(outlets -> {
                             if (outlets != null) {
                                 this.outlets = outlets;
                                 notifyPropertyChanged(BR.outletName);
                                 notifyPropertyChanged(BR.outlet1Avatar);
                                 notifyPropertyChanged(BR.outlet2Avatar);
                                 notifyPropertyChanged(BR.outlet3Avatar);
                             }
                         });
    }

    @Bindable
    public String getAssignmentTitle() {
        return selectedAssignment == null ? "" : selectedAssignment.getTitle();
    }

    @Bindable
    public String getCaption() {
        return selectedAssignment == null ? "" : selectedAssignment.getCaption();
    }

    @Bindable
    public String getExpirationString() {
        if (selectedAssignment == null) {
            for (String key : assignmentsOnMap.keySet()) {
                AssignmentOnMap assignmentOnMap = assignmentsOnMap.get(key);
                if (assignmentOnMap == null) {
                    continue;
                }
                Assignment ass = assignmentOnMap.assignment;
                if (ass == null || !ass.isAcceptable()) {
                    continue;
                }
                if (ass.isAccepted() || assignmentOnMap.accepted) {
                    selectedAssignment = ass;
                }
            }
        }
        if (selectedAssignment == null) {
            return "";
        }

        DateTime expirationDateTime = new DateTime(selectedAssignment.getEndsAt());
        int days = Days.daysBetween(new DateTime(), expirationDateTime).getDays();
        int hours = Hours.hoursBetween(new DateTime(), expirationDateTime).getHours();
        int minutes = Minutes.minutesBetween(new DateTime(), expirationDateTime).getMinutes();
        int seconds = Seconds.secondsBetween(new DateTime(), expirationDateTime).getSeconds();

        if (days > 0) {
            return getString(R.string.expires_one_param, getQuantityString(R.plurals.days, days, days));
        }
        if (hours > 0) {
            if (minutes > 0) {
                return getString(R.string.expires_one_param, getQuantityString(R.plurals.hours, hours, hours));
            }
            else {
                return getString(R.string.expires_two_param, getQuantityString(R.plurals.hours, hours, hours), getQuantityString(R.plurals.minutes, minutes, minutes));
            }
        }
        if (minutes > 0) {
            return getString(R.string.expires_one_param, getQuantityString(R.plurals.minutes, minutes, minutes));
        }
        if (seconds > 0) {
            return getString(R.string.expires_one_param, getQuantityString(R.plurals.seconds, seconds, seconds));
        }
        if (!alreadyNotifiedOfExpiration) {
            alreadyNotifiedOfExpiration = true;
            showDialog();
        }
        return getString(R.string.error_assignment_expired);
    }

    @Bindable
    public String getApprovedTime() {
        if (selectedAssignment == null || selectedAssignment.getStartsAt() == null) {
            return "";
        }

        DateTime approvalDateTime = new DateTime(selectedAssignment.getStartsAt());
        int days = Days.daysBetween(approvalDateTime, new DateTime()).getDays();
        DateTimeFormatter fmt;
        if (!DateFormat.is24HourFormat(getActivity())) {
            fmt = DateTimeFormat.forPattern("hh:mm aa");
        }
        else {
            fmt = DateTimeFormat.forPattern("HH:mm");
        }
        String time = fmt.print(approvalDateTime);

        if (days > 1) {
            return getString(R.string.posted_at, getQuantityString(R.plurals.days, days, days), time);
        }
        else if (days == 1) {
            return getString(R.string.posted_at_yesterday, time);
        }
        else {
            return getString(R.string.posted_at_today, time);
        }
    }

    @Bindable
    public String getDistanceString() {
        if (selectedAssignment == null || userLocation == null) {
            return "";
        }
        LatLng assignmentLoc = new LatLng(selectedAssignment.getLatitude(), selectedAssignment
                .getLongitude());
        double distanceMeters = SphericalUtil.computeDistanceBetween(assignmentLoc, userLocation);
        double distanceMiles = distanceMeters / 1609.34;
        return StringUtils.formatDistance(distanceMiles);
    }

    @Bindable
    public boolean getHaveUserLocation() {
        return userLocation != null;
    }

    @Bindable
    public int getGlobalAssignments() {
        return globalAssignments;
    }

    @Bindable
    public String getGlobalAssignmentsString() {
        return getActivity().getResources()
                            .getQuantityString(R.plurals.global_assignments, globalAssignments, globalAssignments);
    }

    public void setGlobalAssignments(int globalAssignments) {
        this.globalAssignments = globalAssignments;
        notifyPropertyChanged(BR.globalAssignments);
        notifyPropertyChanged(BR.globalAssignmentsString);
    }

    @Bindable
    public String getOutletName() {
        if (outlets == null || outlets.size() == 0) {
            return "";
        }
        else if (outlets.size() == 1) {
            return outlets.get(0).getTitle();
        }
        else {
            return outlets.size() + " active news outlets";
        }
    }

    @Bindable
    public String getOutlet1Avatar() {
        return getOutletAvatar(0);
    }

    @Bindable
    public String getOutlet2Avatar() {
        return getOutletAvatar(1);
    }

    @Bindable
    public String getOutlet3Avatar() {
        return getOutletAvatar(2);
    }

    private String getOutletAvatar(int index) {
        if (outlets == null || outlets.size() < index + 1) {
            return null;
        }
        return outlets.get(index).getAvatar();
    }

    @Bindable
    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String url) {
        avatarUrl = url;
        notifyPropertyChanged(BR.avatarUrl);
    }

    @Bindable
    public int getUnreadNotifications() {
        return unreadNotifications;
    }

    public void setUnreadNotifications(int unreadNotifications) {
        this.unreadNotifications = unreadNotifications;
    }

    @Bindable
    public boolean isLoggedIn() {
        return super.isLoggedIn();
    }

    public Action1<View> recenterToUserLocation = view -> {
        triggernow = true;
//        zoomToLocation(frescoLocationManager.getUserLocation());
        //First find 5x the user acceptance rating.
        if (userLocation == null || notificationRadiusMiles < 0 || userLocation != null) {
            zoomToLocation(frescoLocationManager.getUserLocation());
            LogUtils.i(TAG, "userloc null or notif radius not gotten");
            return;
        }

        ArrayList<Assignment> assignments = new ArrayList<>();

        assignmentManager.find(userLocation, notificationRadiusMiles * 5)
                         .flatMapIterable(x -> x)
                         .filter(assignment -> assignment != null && !assignment.isGlobal())
                         .observeOn(AndroidSchedulers.mainThread())
                         .doOnCompleted(() -> {
                             //Zoom out to this assignment location
                             if (assignments == null || assignments.size() == 0) {
                                 zoomToLocation(frescoLocationManager.getUserLocation());
                             }
                             else {
                                 Assignment assignment = assignments.get(assignments.size() - 1);
                                 updateMapZoom(new LatLng(assignment.getLatitude(), assignment.getLongitude()), userLocation);
                                 LogUtils.i(TAG, "updating map zoom to assignment notif");
                             }
                         })
                         .subscribe(assignment -> {
                             assignments.add(assignment);
                         });
    };

    public Action1<View> profilePictureClicked = view -> {
        NotificationFeedActivity.start(getActivity());
    };

    public Action1<View> openGlobals = view -> {
        GlobalAssignmentActivity.start(getActivity());
    };

    public Action1<View> openDirections = view -> {
        String url;
        LatLng userLocation = frescoLocationManager.getUserLocation();
        if (userLocation == null) {
            url = String.format(Locale.getDefault(), "https://www.google.com/maps/dir/%s/", selectedAssignment.getAddress());
        }
        else {
            url = String.format(Locale.getDefault(), "https://www.google.com/maps/dir/%f,+%f/%s/", userLocation.latitude, userLocation.longitude, selectedAssignment.getAddress());
        }

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        getActivity().startActivity(browserIntent);
    };

    @Override
    public void onNotificationReceived() {
        refreshNotifications();
    }

    public void updateCameraGreen() {
        for (String key : assignmentsOnMap.keySet()) {
            AssignmentOnMap assignmentOnMap = assignmentsOnMap.get(key);
            if (assignmentOnMap == null || !sessionManager.isLoggedIn()) {
                LogUtils.i(TAG, "assignment on map was null");
                continue;
            }
            Assignment ass = assignmentOnMap.assignment;
            if (ass == null || !ass.isAcceptable()) {
                LogUtils.i(TAG, "assignment wasn't acceptable");
                continue;
            }
            LatLng assLatLng = new LatLng(selectedAssignment.getLatitude(), selectedAssignment.getLongitude());
            float radius = selectedAssignment.getRadius() * METERS_IN_MILE;

            if (withinRadius(userLocation, assLatLng, radius) && selectedAssignment.isAcceptable()) {
                setCameraGreen(true);
            }
        }
    }

    public void setCameraGreen(boolean makeGreen) {
        if (sessionManager.isLoggedIn()) {
            this.green = makeGreen;
        }
        else {
            this.green = false;
        }

        notifyPropertyChanged(BR.green);
    }

    @Bindable
    public boolean isGreen() {
        return green;
    }

    @Bindable
    public boolean isAcceptable() {
        if (selectedAssignment == null || !sessionManager.isLoggedIn()) {
            return false;
        }
        return selectedAssignment.isAcceptable();
    }

    public void setAcceptButtonText() {
        notifyPropertyChanged(BR.acceptButtonText);
    }

    @Bindable
    public String getAcceptButtonText() {
        //Shows the selectedAssignment
        //Not logged in or not acceptable
        if (!isAcceptable() || !sessionManager.isLoggedIn()) {

            LogUtils.i(TAG, "not acceptable, jesus marie");
            return "Open Camera";
        }

        if (selectedAssignment.isAccepted()) {
            LogUtils.i(TAG, "already accepted, jesus marie"); //this should also be irrelevant cause we need to hide the drawer
            return "Open Camera";
        }
        if (assignmentsOnMap != null) {
            Assignment ass = null;
            if (acceptedAssignmentId == null) {
                ass = getAssignmentFromAssignmentsOnMap(selectedAssignment.getId());
            }
            else {
                ass = getAssignmentFromAssignmentsOnMap(acceptedAssignmentId);
            }
            if (ass != null && ass.isAccepted()) {
                LogUtils.i(TAG, "already accepted, jesus marie");
                return "Open Camera";
            }
        }
        return "Accept";
    }

    private void setFeetAway() {
        notifyPropertyChanged(BR.feetAway);
    }

    @Bindable
    public String getFeetAway() {
        if (userLocation == null) {
            return "";
        }

        if (selectedAssignment == null) {
            for (String key : assignmentsOnMap.keySet()) {
                AssignmentOnMap assignmentOnMap = assignmentsOnMap.get(key);
                if (assignmentOnMap == null) {
                    LogUtils.i(TAG, "assignment on map was null");
                    continue;
                }
                Assignment ass = assignmentOnMap.assignment;
                if (ass == null || !ass.isAcceptable()) {
                    LogUtils.i(TAG, "assignment wasn't acceptable");
                    continue;
                }
                if (ass.isAccepted() || assignmentOnMap.accepted) {
                    selectedAssignment = ass;
                }
            }
        }
        if (selectedAssignment == null) {
            return "";
        }

        LatLng assignmentLoc = new LatLng(selectedAssignment.getLatitude(), selectedAssignment
                .getLongitude());
        double distanceMeters = SphericalUtil.computeDistanceBetween(assignmentLoc, userLocation);
        int distanceFeet = (int) (distanceMeters * 3.28084);
        return StringUtils.formatDistanceAway(distanceFeet);
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View promptView = inflater.inflate(R.layout.view_assignment_expired, null);
        final AlertDialog alertD = builder.create();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        alertD.setView(promptView);
        alertD.show();
        Button btnAdd1 = (Button) promptView.findViewById(R.id.positive_button_dismiss);
        btnAdd1.setOnClickListener(view -> {
            alertD.dismiss();
        });
    }

    public Action1<View> acceptAssignment = view -> {
        //Shit this logic gonna get funky
        //Actually I'll just steal the logic from the get accept button text.
        globalHidden = true;
        globalFooter.get().setVisibility(View.GONE);
//        setTitle("");

        if (getAcceptButtonText().equals("Accept")) {

            acceptedAssignmentId = selectedAssignment.getId(); // because who cares about api respose either
            assignmentManager.accept(selectedAssignment.getId())
                             .observeOn(AndroidSchedulers.mainThread())
                             .onErrorReturn(throwable -> {
                                 return null;
                             })
                             .subscribe(networkAssignment -> {
                                 selectedAssignment.setAccepted(true);
                                 if (assignmentsOnMap != null) {
                                     Assignment acceptedAssignment = getAssignmentFromAssignmentsOnMap(acceptedAssignmentId);
                                     if (acceptedAssignment != null) {
                                         acceptedAssignment.setAccepted(true);
                                     }
                                 }
                                 setAcceptButtonText();
                                 if (userLocation != null) {
                                     updateMap(userLocation);
                                 }
                                 for (String key : assignmentsOnMap.keySet()) {
                                     AssignmentOnMap assignmentOnMap = assignmentsOnMap.get(key);
                                     if (assignmentOnMap != null) {
                                         if (key.equals(acceptedAssignmentId) || assignmentOnMap.accepted) {
                                             assignmentOnMap.setAccepted(true);
                                             assignmentOnMap.assignment.setAccepted(true);
                                             continue;
                                         }
                                         assignmentOnMap.setAccepted(false);
                                         assignmentOnMap.assignment.setAccepted(false);
                                         assignmentOnMap.marker.setVisible(false);
                                         assignmentOnMap.circle.setVisible(false);

                                     }
                                 }

                                 //track anayltics
                                 if (networkAssignment != null) {
                                     LatLng assignmentLoc = new LatLng(networkAssignment.getLocation().lat(), networkAssignment.getLocation().lon());
                                     double distanceMeters = SphericalUtil.computeDistanceBetween(assignmentLoc, userLocation);
                                     double distanceMiles = distanceMeters / 1609.34;
                                     analyticsManager.assignmentAccepted(networkAssignment.getId(), MathUtils.round(distanceMiles, 2));
                                 }
                                 //todo give directions
                             });
        }
        else {
            openCamera.call(null);
        }

        //open camera for sure at somepoint
    };

    public Action1<View> unacceptAssignment = view -> {
        globalFooter.get().setVisibility(View.VISIBLE);
        setTitle(getActivity().getString(R.string.assignments));

        for (String key : assignmentsOnMap.keySet()) {
            AssignmentOnMap assignmentOnMap = assignmentsOnMap.get(key);
            if (assignmentOnMap.accepted && StringUtils.toNullIfEmpty(acceptedAssignmentId) == null) {
                acceptedAssignmentId = assignmentOnMap.assignment.getId();
            }
            assignmentOnMap.setAccepted(false);
            assignmentOnMap.assignment.setAccepted(false);
            assignmentOnMap.marker.setVisible(true);
            assignmentOnMap.circle.setVisible(true);
            updateMap(userLocation);
        }
        setAcceptButtonText();

        assignmentManager.unaccept(acceptedAssignmentId)
                         .onErrorReturn(throwable -> null)
                         .subscribe(networkAssignment -> {
                             if (networkAssignment != null) {
                                 LatLng assignmentLoc = new LatLng(networkAssignment.getLocation().lat(), networkAssignment.getLocation().lon());
                                 double distanceMeters = SphericalUtil.computeDistanceBetween(assignmentLoc, userLocation);
                                 double distanceMiles = distanceMeters / 1609.34;
                                 analyticsManager.assignmentUnaccepted(networkAssignment.getId(), MathUtils.round(distanceMiles, 2));
                             }
                         });
    };

    private Assignment getAssignmentFromAssignmentsOnMap(String assignmentId) {
        if (assignmentsOnMap != null) {
            for (String key : assignmentsOnMap.keySet()) {

                AssignmentOnMap assignmentonMap = assignmentsOnMap.get(key);
                if (key.equals(assignmentId) && assignmentonMap != null) {
                    return assignmentonMap.assignment;
                }
            }
        }
        return null;
    }

    private boolean withinRadius(LatLng userLocation, LatLng assignmentLocation, float radiusMeters) {
        float[] distance = new float[2];

        if (userLocation == null || assignmentLocation == null || radiusMeters == 0 || !sessionManager.isLoggedIn()) {
            LogUtils.i(TAG, "was null returning false");
            return false;
        }

        double distanceMeters = SphericalUtil.computeDistanceBetween(assignmentLocation, userLocation);

//        Location.distanceBetween(userLocation.latitude, userLocation.longitude,
//                assignmentLocation.latitude, assignmentLocation.longitude, distance);

        if (distanceMeters > radiusMeters) {
            LogUtils.i(TAG, "outside of radius");
            return false;
        }
        else {
            LogUtils.i(TAG, "inside of radius");
            return true;
        }
    }

    public void updateAssignments() {
        assignmentManager.accepted()
                         .observeOn(AndroidSchedulers.mainThread())
                         .onErrorReturn(throwable -> null)
                         .subscribe(assignment -> {
                             if (assignment == null || assignment.getId() == null) {
                                 return;
                             }
                             if (assignmentsOnMap.containsKey(assignment.getId())) {
                                 if (assignment.isAccepted()) {
                                     for (String key : assignmentsOnMap.keySet()) {
                                         if (!sessionManager.isLoggedIn()) {
                                             continue;
                                         }
                                         AssignmentOnMap assignmentOnMap = assignmentsOnMap.get(key);
                                         if (assignmentOnMap == null) {
                                             continue;
                                         }
                                         Assignment ass = assignmentOnMap.assignment;
                                         if (ass == null || !ass.isAcceptable()) {
                                             continue;
                                         }
                                         if (ass.getId().equals(assignment.getId())) {
                                             ass.setAccepted(true);
                                             assignmentOnMap.accepted = true;
                                             LogUtils.i(TAG, "accepting that ish");
                                         }
                                     }
                                     updateMap(userLocation);
                                 }
                             }
                             else { //add assignment to map
                                 LatLng assignmentLoc = new LatLng(assignment.getLatitude(), assignment.getLongitude());
                                 float radiusMeters = assignment.getRadius() * METERS_IN_MILE;

                                 Marker marker = null;
                                 Circle circle = null;

                                 if (map == null) {
                                     return;
                                 }

                                 marker = map.addMarker(new MarkerOptions()
                                         .position(assignmentLoc)
                                         .anchor(0.5f, 0.5f)
                                         .icon(BitmapUtils.getBitmapDescriptor(getActivity(), R.drawable.map_assignment_circle_green)));

                                 circle = map.addCircle(new CircleOptions()
                                         .center(assignmentLoc)
                                         .radius(radiusMeters)
                                         .strokeColor(Color.TRANSPARENT)
                                         .fillColor(ContextCompat.getColor(getActivity(), R.color.fresco_assignment_radius_green)));

                                 assignmentsOnMap.put(assignment.getId(), new AssignmentOnMap(assignment, marker, circle));

                                 markersToAssignments.put(marker.getId(), assignment.getId());
                                 if (userLocation != null) {
                                     updateMap(userLocation);
                                     updateMapZoom(assignmentLoc, userLocation);
                                 }
                                 if (assignmentId != null && assignmentId.equals(assignment.getId())) {
                                     AssignmentOnMap data = assignmentsOnMap.get(markersToAssignments.get(marker.getId()));
                                     zoomToLocation(circle.getCenter(), circle.getRadius());
                                     setAssignment(data.assignment, frescoLocationManager.getUserLocation());
                                     openDrawer();
                                 }

                             }
                         });

        if (map != null && map.getProjection() != null && map.getProjection().getVisibleRegion() != null) {
            LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
            float radiusInMiles = (float) SphericalUtil.computeDistanceBetween(bounds.northeast, bounds.southwest) * 0.000621371f;

            assignmentManager.find(bounds.getCenter(), radiusInMiles)
                             .flatMapIterable(x -> x)
                             .filter(assignment -> assignment != null && !assignment.isGlobal())
                             .observeOn(AndroidSchedulers.mainThread())
                             .onErrorReturn(throwable -> null)
                             .subscribe(assignment -> {
                                 if (assignment != null && assignmentsOnMap.containsKey(assignment.getId())) {
                                     if (assignment.isAccepted()) {
                                         for (String key : assignmentsOnMap.keySet()) {
                                             if (!sessionManager.isLoggedIn()) {
                                                 continue;
                                             }
                                             AssignmentOnMap assignmentOnMap = assignmentsOnMap.get(key);
                                             if (assignmentOnMap == null) {
                                                 continue;
                                             }
                                             Assignment ass = assignmentOnMap.assignment;
                                             if (ass == null || !ass.isAcceptable()) {
                                                 continue;
                                             }
                                             if (ass.getId().equals(assignment.getId())) {
                                                 ass.setAccepted(true);
                                                 assignmentOnMap.accepted = true;
                                                 LogUtils.i(TAG, "accepting that ish");
                                             }
                                         }
                                         updateMap(userLocation);
                                         updateMapZoom(new LatLng(assignment.getLatitude(), assignment.getLongitude()), userLocation);
                                     }
                                 }
                             });
        }
    }

    private void updateMapZoom(LatLng assignmentLoc, LatLng userLocation) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(userLocation);
        builder.include(assignmentLoc);
        LatLngBounds bounds = builder.build();

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 50);
        map.animateCamera(cu, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                CameraUpdate zout = CameraUpdateFactory.zoomBy(-.5f);
                map.animateCamera(zout);
            }

            @Override
            public void onCancel() {

            }
        });
    }

    public void updateMap(LatLng userLoc) {
        if (userLoc == null) {
            return;
        }
        this.userLocation = userLoc;
        boolean oneWithinRadius = false;
        boolean oneAccepted = false;

        for (String key : assignmentsOnMap.keySet()) {
            if (!sessionManager.isLoggedIn()) {
                continue;
            }

            AssignmentOnMap assignmentOnMap = assignmentsOnMap.get(key);
            if (assignmentOnMap == null) {
                continue;
            }
            Assignment ass = assignmentOnMap.assignment;
            if (ass == null || !ass.isAcceptable()) {
                continue;
            }
            if (ass.isAccepted() || assignmentOnMap.accepted) {
                assignmentOnMap.accepted = true;
                ass.setAccepted(true);
                oneAccepted = true;
                acceptedAssignmentId = ass.getId();
                notifyPropertyChanged(BR.expirationString);

                if (getExpirationString().equals(getString(R.string.error_assignment_expired))) {
                    assignmentOnMap.accepted = false;
                    ass.setAccepted(false);
                    oneAccepted = false;
                    ass.setAcceptable(false);
                    assignmentOnMap.marker.setIcon(BitmapUtils.getBitmapDescriptor(getActivity(), R.drawable.map_assignment_circle));
                    assignmentOnMap.circle.setFillColor(ContextCompat.getColor(getActivity(), R.color.fresco_assignment_radius));
                    unacceptAssignment.call(null);
                    break;
                }
            }
            setFeetAway();
            //Check if within radius, if so make green.
            LatLng assignmentLatLng = new LatLng(ass.getLatitude(), ass.getLongitude());
            float radius = ass.getRadius() * METERS_IN_MILE;
            Marker marker = assignmentOnMap.marker;
            Circle circle = assignmentOnMap.circle;
            if (circle == null || marker == null) {
                continue;
            }
            if (withinRadius(userLoc, assignmentLatLng, radius)) { //&& ass.isAccepted() imogen
                //Make the marker green
                marker.setIcon(BitmapUtils.getBitmapDescriptor(getActivity(), R.drawable.map_assignment_circle_green));
                circle.setFillColor(ContextCompat.getColor(getActivity(), R.color.fresco_assignment_radius_green));
                oneWithinRadius = true;
                if (assignmentBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    setCameraGreen(true); //sets the camera green
                }

            }
            else {

                marker.setIcon(BitmapUtils.getBitmapDescriptor(getActivity(), R.drawable.map_assignment_circle));
                circle.setFillColor(ContextCompat.getColor(getActivity(), R.color.fresco_assignment_radius));
            }
        } //END for loop

        if (!oneWithinRadius) {

            setCameraGreen(false); //we're not in the radius for any assignent
            if (acceptedToolbar != null && acceptedToolbar.get() != null) {
                acceptedToolbar.get().setVisibility(View.INVISIBLE);
            }
            if (inRangeToolbar != null && inRangeToolbar.get() != null) {
                inRangeToolbar.get().setVisibility(View.INVISIBLE);
            }
            if (toolbar != null && toolbar.get() != null) {
                toolbar.get().setVisibility(View.VISIBLE);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.upload_progress));
            }
            if (oneAccepted) {
                setCameraGreen(false); //check to see if we're inside the radius of the one accepted
                //Make sure the remaining markers are all gone from the map
                for (String key : assignmentsOnMap.keySet()) {
                    AssignmentOnMap assignmentOnMap = assignmentsOnMap.get(key);
                    if (assignmentOnMap == null) {
                        continue;
                    }
                    Assignment ass = assignmentOnMap.assignment;
                    if (ass == null) {
                        continue;
                    }
                    //Check if some assignment is accepted
                    if (!assignmentOnMap.accepted) {
                        assignmentOnMap.marker.setVisible(false);
                        assignmentOnMap.circle.setVisible(false);
                    }
                    if (assignmentOnMap.accepted) {
                        assignmentOnMap.marker.setVisible(true);
                        assignmentOnMap.circle.setVisible(true);
                        acceptedToolbar.get().setVisibility(View.VISIBLE);
                        inRangeToolbar.get().setVisibility(View.INVISIBLE);
                        toolbar.get().setVisibility(View.INVISIBLE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.fresco_dark_green));
                        }                        //44C159
                        float radius = ass.getRadius() * METERS_IN_MILE;

                        if (withinRadius(userLoc, assignmentOnMap.latLng, radius)) {
                            setCameraGreen(true);
                        }
                    }
                }
            }
            else {
                for (String key : assignmentsOnMap.keySet()) {
                    AssignmentOnMap assignmentOnMap = assignmentsOnMap.get(key);
                    if (assignmentOnMap == null) {
                        continue;
                    }
                    Assignment ass = assignmentOnMap.assignment;
                    if (ass == null) {
                        continue;
                    }
                    //Check if some assignment is accepted

                    assignmentOnMap.marker.setVisible(true);
                    assignmentOnMap.circle.setVisible(true);
                    acceptedToolbar.get().setVisibility(View.INVISIBLE);
                    inRangeToolbar.get().setVisibility(View.INVISIBLE);

                    //44C159
                }

            }
        }
        else { //we're within the radius
            if (oneAccepted) {
                setCameraGreen(false); //check to see if we're inside the radius of the one accepted
                //Make sure the remaining markers are all gone from the map
                for (String key : assignmentsOnMap.keySet()) {
                    AssignmentOnMap assignmentOnMap = assignmentsOnMap.get(key);
                    if (assignmentOnMap == null) {
                        continue;
                    }
                    Assignment ass = assignmentOnMap.assignment;
                    if (ass == null) {
                        continue;
                    }
                    //Check if some assignment is accepted
                    if (!assignmentOnMap.accepted) {
                        assignmentOnMap.marker.setVisible(false);
                        assignmentOnMap.circle.setVisible(false);
                    }
                    if (assignmentOnMap.accepted) {
                        assignmentOnMap.marker.setVisible(true);
                        assignmentOnMap.circle.setVisible(true);
                        acceptedToolbar.get().setVisibility(View.VISIBLE);
                        inRangeToolbar.get().setVisibility(View.INVISIBLE);
                        toolbar.get().setVisibility(View.INVISIBLE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.fresco_dark_green));
                        }                        //44C159
                        float radius = ass.getRadius() * METERS_IN_MILE;

                        if (withinRadius(userLoc, assignmentOnMap.latLng, radius)) {
                            setCameraGreen(true);
                        }
                    }
                }
            }
            else {
                for (String key : assignmentsOnMap.keySet()) {
                    AssignmentOnMap assignmentOnMap = assignmentsOnMap.get(key);
                    if (assignmentOnMap == null) {
                        continue;
                    }
                    Assignment ass = assignmentOnMap.assignment;
                    if (ass == null) {
                        continue;
                    }
                    //Check if some assignment is accepted

                    assignmentOnMap.marker.setVisible(true);
                    assignmentOnMap.circle.setVisible(true);
                    acceptedToolbar.get().setVisibility(View.INVISIBLE);
                    inRangeToolbar.get().setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.fresco_dark_green));
                    }
                    //44C159
                }
            }
        }
    }

    private class AssignmentOnMap {
        public Assignment assignment;
        public Marker marker;
        public Circle circle;
        public boolean accepted;
        public LatLng latLng;

        public AssignmentOnMap(Assignment assignment, Marker marker, Circle circle) {
            this.assignment = assignment;
            this.marker = marker;
            this.circle = circle;
            this.accepted = assignment.isAccepted();
            this.latLng = new LatLng(assignment.getLatitude(), assignment.getLongitude());
        }

        public void setAccepted(boolean accepted) {
            this.accepted = accepted;
        }
    }

}
