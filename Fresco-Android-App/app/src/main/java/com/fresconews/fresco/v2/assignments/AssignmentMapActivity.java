package com.fresconews.fresco.v2.assignments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseActivity;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;

public class AssignmentMapActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String TAG = AssignmentMapActivity.class.getSimpleName();

    public static final String EXTRA_ASSIGNMENT_ID = "assignment_id";

    private AssignmentMapViewModel viewModel;

    GoogleApiClient mGoogleApiClient;

    @Inject
    SessionManager sessionManager;

    @Inject
    AnalyticsManager analyticsManager;

    public static void start(Context context) {
        start(context, null);
    }

    public static void start(Context context, String assignmentId) {
        Intent starter = new Intent(context, AssignmentMapActivity.class);
        starter.putExtra(EXTRA_ASSIGNMENT_ID, assignmentId);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Fresco2) getApplication()).getFrescoComponent().inject(this);

        viewModel = new AssignmentMapViewModel(this, getIntent().getStringExtra(EXTRA_ASSIGNMENT_ID));
        setViewModel(R.layout.activity_assignment, viewModel);

        analyticsManager.trackScreen("Assignments");

        if (sessionManager.isLoggedIn()) {
            // Create an instance of GoogleAPIClient.
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
            }
            mGoogleApiClient.connect();
        }

    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && sessionManager.isLoggedIn()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (viewModel != null && viewModel.assignmentBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            viewModel.closeDrawer();
            if (sessionManager.isLoggedIn()) {
                viewModel.updateCameraGreen();
            }
            return;
        }
        super.onBackPressed();
    }

    protected void createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            LogUtils.i(TAG, "Lat - " + String.valueOf(mLastLocation.getLatitude()));
            LogUtils.i(TAG, "Lon - " + String.valueOf(mLastLocation.getLongitude()));
            viewModel.updateAssignments();
            onLocationChanged(mLastLocation);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        if (viewModel != null && viewModel.assignmentBottomSheetBehavior != null &&
                viewModel.assignmentBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            viewModel.closeDrawer();
        }
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && sessionManager.isLoggedIn()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            createLocationRequest();
            return;
        }
        if (sessionManager.isLoggedIn()) {
            // Create an instance of GoogleAPIClient.
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
            }
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LogUtils.i(TAG, "connected");
        if (mGoogleApiClient == null || !sessionManager.isLoggedIn()) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        createLocationRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {
        LogUtils.i(TAG, "connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        LogUtils.i(TAG, "connection failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        if (!sessionManager.isLoggedIn()) {
            return;
        }
        LatLng userLoc = new LatLng(location.getLatitude(), location.getLongitude());
        //Update Map
        viewModel.updateMap(userLoc);
    }
}
