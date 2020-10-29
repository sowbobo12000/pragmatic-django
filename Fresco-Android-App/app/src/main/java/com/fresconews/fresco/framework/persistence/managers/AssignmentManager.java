package com.fresconews.fresco.framework.persistence.managers;

import com.fresconews.fresco.framework.network.responses.NetworkAssignment;
import com.fresconews.fresco.framework.network.responses.NetworkAssignmentFindResult;
import com.fresconews.fresco.framework.network.services.AssignmentService;
import com.fresconews.fresco.framework.persistence.models.Assignment;
import com.fresconews.fresco.framework.persistence.models.Assignment_Outlet;
import com.fresconews.fresco.framework.persistence.models.Assignment_Outlet_Table;
import com.fresconews.fresco.framework.persistence.models.Assignment_Table;
import com.fresconews.fresco.framework.persistence.models.Outlet;
import com.google.android.gms.maps.model.LatLng;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.Observable;

public class AssignmentManager {
    private static final String TAG = AssignmentManager.class.getSimpleName();

    private AssignmentService assignmentService;

    public AssignmentManager(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    public Observable<List<Assignment>> validAssignments(List<LatLng> submissionPoints) {
        HashMap<String, String> params = new HashMap<>();
        params.put("where", "intersects"); //logical OR including all assignments for any geo point
        params.put("geo[type]", "MultiPoint");

        for (int i = 0; i < submissionPoints.size(); i++) {
            LatLng coords = submissionPoints.get(i);
            params.put("geo[coordinates][" + i + "][0]", String.valueOf(coords.longitude));
            params.put("geo[coordinates][" + i + "][1]", String.valueOf(coords.latitude));
        }

        return assignmentService.find(params)
                                .onErrorReturn(throwable -> null)
                                .map(this::findAssignments);
    }

    public Observable<List<Assignment>> find(LatLng point, float radiusMiles) {
        HashMap<String, String> params = new HashMap<>();

        params.put("geo[type]", "Point");
        params.put("geo[coordinates][0]", String.valueOf(point.longitude));
        params.put("geo[coordinates][1]", String.valueOf(point.latitude));

        params.put("radius", String.valueOf(radiusMiles));

        return assignmentService.find(params)
                                .onErrorReturn(throwable -> null)
                                .map(this::findAssignments);
    }

    public Observable<Assignment> accepted(){
        return assignmentService.accepted()
                .onErrorReturn(throwable -> null)
                .map(Assignment::from);
    }

    public Observable<Assignment> downloadAssignment(String assignmentId) {
        return assignmentService.get(assignmentId)
                                .onErrorReturn(throwable -> null)
                                .map(Assignment::from);
    }

    private List<Assignment> findAssignments(NetworkAssignmentFindResult result) {
        clearAssignments();
        List<Assignment> assignments = new ArrayList<>();
        if (result != null) {
            for (NetworkAssignment nearbyAssignment : result.getNearby()) {
                if (nearbyAssignment != null) {
                    assignments.add(Assignment.from(nearbyAssignment));
                }
            }

            for (NetworkAssignment globalAssignment : result.getGlobal()) {
                if (globalAssignment != null) {
                    assignments.add(Assignment.from(globalAssignment));
                }
            }
        }
        return assignments;
    }

    public Observable<List<Outlet>> getOutletsForAssignment(String assignmentId) {
        List<Assignment_Outlet> assignmentOutlets = SQLite.select()
                                                          .from(Assignment_Outlet.class)
                                                          .where(Assignment_Outlet_Table.assignment_id.eq(assignmentId))
                                                          .orderBy(Assignment_Outlet_Table.outlet_id, true)
                                                          .queryList();

        List<Outlet> outlets = new ArrayList<>();

        for (Assignment_Outlet assignmentOutlet : assignmentOutlets) {
            outlets.add(assignmentOutlet.getOutlet());
        }

        return Observable.just(outlets);
    }

    public Observable<List<Assignment>> getGlobalAssignments() {
        return Observable.create(subscriber -> {
            SQLite.select()
                  .from(Assignment.class)
                  .where(Assignment_Table.isGlobal.eq(true))
                  .orderBy(Assignment_Table.id, false)
                  .async()
                  .queryListResultCallback((transaction, assignments) -> {
                      subscriber.onNext(assignments);
                      subscriber.onCompleted();
                  })
                  .execute();
            ;
        });
    }

    public void clearAssignments() {
        SQLite.delete(Assignment.class).execute();
        SQLite.delete(Assignment_Outlet.class).execute();
    }

    public Observable<NetworkAssignment> accept(String id){
        return assignmentService.accept(id);
    }

    public Observable<NetworkAssignment> unaccept(String id){
        return assignmentService.unaccept(id);
    }
}
