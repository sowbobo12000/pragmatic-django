package com.fresconews.fresco.v2.dev;

import android.app.AlertDialog;
import android.databinding.Bindable;
import android.support.v4.view.GravityCompat;
import android.view.View;
import android.widget.Switch;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.network.EndpointHelper;
import com.fresconews.fresco.framework.persistence.FrescoDatabase;
import com.fresconews.fresco.framework.persistence.managers.AuthManager;
import com.fresconews.fresco.framework.persistence.managers.GalleryManager;
import com.fresconews.fresco.framework.persistence.managers.LocalSettingsManager;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

import javax.inject.Inject;

import rx.functions.Action1;

/**
 * Created by mauricewu on 11/14/16.
 */
public class DevOptionsViewModel extends ActivityViewModel<DevOptionsActivity> {
    @Inject
    AuthManager authManager;

    @Inject
    GalleryManager galleryManager;

    @Inject
    LocalSettingsManager localSettingsManager;

    public BindableView<Switch> printAPILogsSwitch = new BindableView<>();

    private String currentEndpoint;
    private boolean needsRestart;

    DevOptionsViewModel(DevOptionsActivity activity) {
        super(activity);

        setTitle(getActivity().getString(R.string.dev_options));
        setNavIcon(R.drawable.ic_close_white);

        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);
        setCurrentEndpoint(EndpointHelper.currentEndpoint.endpointName);
    }

    @Override
    public void onBound() {
        if (hasBeenBound) {
            return;
        }
        super.onBound();

        printAPILogsSwitch.get().setChecked(localSettingsManager.printAPILogs());
        printAPILogsSwitch.get().setOnCheckedChangeListener((compoundButton, b) -> {
            localSettingsManager.setPrintAPILogs(b);
            needsRestart = true;
        });
    }

    @Bindable
    public String getCurrentEndpoint() {
        return currentEndpoint;
    }

    public void setCurrentEndpoint(String currentEndpoint) {
        this.currentEndpoint = currentEndpoint;
        notifyPropertyChanged(BR.currentEndpoint);
    }

    public boolean needsRestart() {
        return needsRestart;
    }

    // Literally no idea why I have to override this witht the same method that is in ActivityViewModel, if you remove this, it will bug
    public Action1<View> onNavIconClicked = view -> {
        drawerLayout.openDrawer(GravityCompat.START);
    };

    public Action1<View> changeEndpoint = view -> {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
        String[] endpoints = getActivity().getResources().getStringArray(R.array.arrays_endpoints);
        builderSingle.setItems(endpoints, (dialogInterface, i) -> {
            EndpointHelper.INSTANCE.saveEndpoint(getActivity(), endpoints[i]);
            setCurrentEndpoint(endpoints[i]);
            clearDatabase();
        });
        builderSingle.show();
    };

    public Action1<View> clearDatabase = view -> clearDatabase();

    public Action1<View> clearGalleries = view -> galleryManager.clearGalleries();

    private void clearDatabase() {
        List<Class<? extends Model>> tablesClasses = FlowManager.getDatabase(FrescoDatabase.class).getModelClasses();
        for (int i = 0; i < tablesClasses.size(); i++) {
            SQLite.delete(tablesClasses.get(i)).execute();
        }
        authManager.logout();
        needsRestart = true;
    }
}
