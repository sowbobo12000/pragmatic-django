package com.fresconews.fresco.framework.persistence;

import com.fresconews.fresco.framework.persistence.models.Assignment;
import com.fresconews.fresco.framework.persistence.models.Gallery;
import com.fresconews.fresco.framework.persistence.models.Notification;
import com.fresconews.fresco.framework.persistence.models.Post;
import com.fresconews.fresco.framework.persistence.models.Story;
import com.fresconews.fresco.framework.persistence.models.User;
import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.SQLiteType;
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration;

/**
 * Created by Ryan on 5/31/2016.
 */

@Database(name = FrescoDatabase.NAME, version = FrescoDatabase.VERSION)
public class FrescoDatabase {
    public static final String NAME = "Fresco2";
    public static final int VERSION = 14; //CANNOT DOWNGRADE NUMBERS. MUST GO UP. 1 is build 304
    // if @Migration version < VERSION this update WILL propagate
    // UNDER the condition that the app already installed has VERSION < VERSION
    //                                          INSTALLED_VERSION < NEW_VERSION
    // IF INSTALLED_VERSION == NEW_VERSION not even the old changes will propogate. Because it believes the necessary changes have been propagated.
    // so if INSTALLED_VERSION = 5   and  NEW_VERSION = 5 and you added @Migration version = 4 changes won't propagate. Change everything to 6.
    // Version history
    // 305  Two changes: Story/address and Post/status
    // 318  Gallery/highlightedAt
    // 319  Assignment/accepted && Assignment/acceptable
    // 322  Assignment/startsAt
    // 322  New table: Purchase
    //    --------------------------------- VERSION 305 -----------------------------------    //

    @Migration(version = 2, database = FrescoDatabase.class)

    public static class Migration2x1 extends AlterTableMigration<Post> {

        public Migration2x1(Class<Post> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {
            //REAL is floating point
            //INTEGER is an int/boolean
            addColumn(SQLiteType.INTEGER, "status");
        }
    }

    @Migration(version = 2, database = FrescoDatabase.class)
    public static class Migration2 extends AlterTableMigration<Story> {

        public Migration2(Class<Story> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {
            //REAL is floating point
            //INTEGER is an int/boolean
            addColumn(SQLiteType.TEXT, "address");
        }
    }

    //    --------------------------------- VERSION 318 -----------------------------------    //

    @Migration(version = 3, database = FrescoDatabase.class)
    public static class Migration3x1 extends AlterTableMigration<Gallery> {

        public Migration3x1(Class<Gallery> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {
            //REAL is floating point
            //INTEGER is an int/boolean/date
            addColumn(SQLiteType.INTEGER, "highlightedAt");
            addColumn(SQLiteType.INTEGER, "startsAt");
        }
    }

    //    --------------------------------- VERSION 319 -----------------------------------    //

    @Migration(version = 4, database = FrescoDatabase.class)
    public static class Migration4x1 extends AlterTableMigration<Assignment> {

        public Migration4x1(Class<Assignment> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {
            //REAL is floating point
            //INTEGER is an int/boolean/date
            addColumn(SQLiteType.INTEGER, "accepted");
        }
    }

    @Migration(version = 4, database = FrescoDatabase.class)
    public static class Migration4x2 extends AlterTableMigration<Assignment> {

        public Migration4x2(Class<Assignment> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {
            //REAL is floating point
            //INTEGER is an int/boolean/date
            addColumn(SQLiteType.INTEGER, "acceptable");
        }
    }

    //    --------------------------------- VERSION 322 -----------------------------------    //

    @Migration(version = 5, database = FrescoDatabase.class)
    public static class Migration5x1 extends AlterTableMigration<Assignment> {

        public Migration5x1(Class<Assignment> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {
            //REAL is floating point
            //INTEGER is an int/boolean/date
            addColumn(SQLiteType.INTEGER, "startsAt");
        }
    }

    //    --------------------------------- VERSION 322 -----------------------------------    //

    @Migration(version = 6, database = FrescoDatabase.class)
    public static class Migration6x1 extends AlterTableMigration<Post> {

        public Migration6x1(Class<Post> table) {
            super(table);

        }

        @Override
        public void onPreMigrate() {
            //REAL is floating point
            //INTEGER is an int/boolean/date
            addColumn(SQLiteType.INTEGER, "index");

        }
    }

    //    --------------------------------- VERSION 328 -----------------------------------    //

    @Migration(version = 7, database = FrescoDatabase.class)
    public static class Migration7x1 extends AlterTableMigration<Gallery> {

        public Migration7x1(Class<Gallery> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {
            //REAL is floating point
            //INTEGER is an int/boolean/date
            addColumn(SQLiteType.TEXT, "originalOwnerId");

        }
    }

    //    --------------------------------- VERSION 331 -----------------------------------    //

    @Migration(version = 10, database = FrescoDatabase.class)
    public static class Migration10x1 extends AlterTableMigration<User> {

        public Migration10x1(Class<User> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {
            //REAL is floating point
            //INTEGER is an int/boolean/date
            addColumn(SQLiteType.TEXT, "suspendedUntil");
            addColumn(SQLiteType.INTEGER, "blocked");
            addColumn(SQLiteType.INTEGER, "blocking");
            addColumn(SQLiteType.INTEGER, "disabled");
        }
    }

    @Migration(version = 10, database = FrescoDatabase.class)
    public static class Migration10x2 extends AlterTableMigration<Gallery> {

        public Migration10x2(Class<Gallery> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {
            //REAL is floating point
            //INTEGER is an int/boolean/date
            addColumn(SQLiteType.TEXT, "curatorId");
        }
    }

    //    --------------------------------- VERSION 3.1.2 -----------------------------------    //
    @Migration(version = 11, database = FrescoDatabase.class)
    public static class Migration11x1 extends AlterTableMigration<Post> {

        public Migration11x1(Class<Post> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {
            //REAL is floating point
            //INTEGER is an int/boolean/date
            addColumn(SQLiteType.TEXT, "parentId");
        }
    }

    @Migration(version = 11, database = FrescoDatabase.class)
    public static class Migration11x2 extends AlterTableMigration<Post> {

        public Migration11x2(Class<Post> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {
            //REAL is floating point
            //INTEGER is an int/boolean/date
            addColumn(SQLiteType.INTEGER, "duration");
        }
    }

    //    --------------------------------- VERSION 3.1.3 -----------------------------------    //
    @Migration(version = 13, database = FrescoDatabase.class)
    public static class Migration13x1 extends AlterTableMigration<User> {

        public Migration13x1(Class<User> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {
            //REAL is floating point
            //INTEGER is an int/boolean/date
            addColumn(SQLiteType.TEXT, "googleSocialLink");
        }
    }

    //    --------------------------------- VERSION 3.1.4 -----------------------------------    //
    @Migration(version = 14, database = FrescoDatabase.class)
    public static class Migration14x1 extends AlterTableMigration<Notification> {

        public Migration14x1(Class<Notification> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {
            //REAL is floating point
            //INTEGER is an int/boolean/date
            addColumn(SQLiteType.INTEGER, "endsAt");
        }
    }

}

