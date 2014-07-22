# android-database-wrapper

## This library serves a few key purposes

* Handles opening and closing database connection to (hopefully) avoid crashes due to the database not being available.
* Enables developer to automatically create tables upon app installation, and can insert data after tables are created.
* Enables developer to automatically handle database updates when the app version is updated.

##Implementation is simple

1. Import the android-database-wrapper library into your project.
1. Write a class that inherits from the `com.hansonchris.android.databasewrapper.Database` abstract class.
1. Write one or more classes that inherit from the `com.hansonchris.android.databasewrapper.DatabaseTable` class.
1. Write a class that inherits from `android.database.sqlite.SQLiteOpenHelper`.
1. If you haven't already, write a class that inherits from the `android.app.Application` class, and then implement `com.hansonchris.android.databasewrapper.ApplicationWithDatabaseWrapperInterface`.
1. Optionally, create an entity class to represent a single record from a database table.

### Example code
#### Subclass of `com.hansonchris.android.databasewrapper.Database`
```java
package com.hansonchris.android.databasewrappertest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.hansonchris.android.databasewrapper.Database;

public class TestDatabase extends Database
{
    private static TestDatabase instance;
    protected SQLiteDatabase db;

    private TestDatabase(Context context)
    {
        super(context);
    }

    /**
     * Most implementations should use the Singleton pattern.
     * This will result in less overhead and better performance.
     * However, in apps where multiple databases may be required,
     * the Registry pattern is recommended instead.
     *
     * @param context
     * @return TestDatabase
     */
    public static TestDatabase getInstance(Context context)
    {
        if (instance == null) {
            instance = new TestDatabase(context);
        }

        return instance;
    }

    protected String getDatabaseName()
    {
        return "test";
    }

    protected String getSqliteOpenHelperClassName()
    {
        return "com.hansonchris.android.databasewrappertest.TestSQLiteOpenHelper";
    }
}
```

#### Subclass of `com.hansonchris.android.databasewrapper.DatabaseTable`
```java
package com.hansonchris.android.databasewrappertest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.hansonchris.android.databasewrapper.DatabaseTable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Users extends DatabaseTable
{
    private static Users instance;
    public static final String TABLE_NAME = "users";
    public static final String COLUMN_NAME_USER_ID = "user_id";
    public static final String COLUMN_NAME_USERNAME = "username";
    public static final String COLUMN_NAME_DATE_ADDED = "date_added";

    private Users(Context context)
    {
        super(context);
    }

    public static Users getInstance(Context context)
    {
        if (instance == null) {
            instance = new Users(context);
        }

        return instance;
    }

    protected String getCreateStatement()
    {
        return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
COLUMN_NAME_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
COLUMN_NAME_USERNAME + " TEXT NOT NULL," +
COLUMN_NAME_DATE_ADDED + " LONG NOT NULL" +
")";
    }

    public List<User> getUsers()
    {
        List<User> users = getListUser();
        TestDatabase db = TestDatabase.getInstance(context);
        Cursor result = db.query(TABLE_NAME, null, null, null, null, null, null);
        result.moveToFirst();
        for (int i = 0; i < result.getCount(); ++i) {
            long id = result.getLong(result.getColumnIndex(COLUMN_NAME_USER_ID));
            String username = result.getString(result.getColumnIndex(COLUMN_NAME_USERNAME));
            long dateAdded = result.getLong(result.getColumnIndex(COLUMN_NAME_DATE_ADDED));
            users.add(getUser(id, username, dateAdded));
            result.moveToNext();
        }
        result.close();

        return users;
    }

    public long addUpdateUser(User user)
    {
        TestDatabase db = TestDatabase.getInstance(context);
        Cursor result = db.query(
            TABLE_NAME,
            null,
            COLUMN_NAME_USER_ID + " = ?",
            new String[] {String.valueOf(user.getId())},
            null,
            null,
            null
        );
        ContentValues values = _getContentValues();
        values.put(COLUMN_NAME_USER_ID, (user.getId() > 0) ? user.getId() : null);
        values.put(COLUMN_NAME_USERNAME, user.getUsername());
        values.put(COLUMN_NAME_DATE_ADDED, user.getDateAdded().getTimeInMillis());
        long id;
        if (result != null && result.getCount() > 0) {
            db.update(TABLE_NAME, values, COLUMN_NAME_USER_ID + " = ?", new String[] {String.valueOf(user.getId())});
            id = user.getId();
        } else {
            id = db.insert(TABLE_NAME, null, values);
        }
        result.close();

        return id;
    }

    protected List<User> getListUser()
    {
        return new ArrayList<User>();
    }

    protected User getUser(long id, String username, long dateAddedTimestamp)
    {
        Calendar dateAdded = Calendar.getInstance();
        dateAdded.setTimeInMillis(dateAddedTimestamp);

        return new User(id, username, dateAdded);
    }

    protected ContentValues _getContentValues()
    {
        return new ContentValues();
    }
}
```

#### Subclass of `android.database.sqlite.SQLiteOpenHelper`
```java
package com.hansonchris.android.databasewrappertest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class TestSQLiteOpenHelper extends SQLiteOpenHelper
{
    protected Context context;

    public TestSQLiteOpenHelper(
        Context context,
        String name,
        CursorFactory factory,
        Integer version
    ) {
        super(context, name, factory, version.intValue());
        this.context = context;
    }

    /**
     * This is where you pass in the initial database schema.
     * Statements can CREATE tables, and they can INSERT data too.
     */
    public void onCreate(SQLiteDatabase db)
    {
        String[][] statements = new String[][]
        {
            //do this for each model which represents a db table:
            Users.getInstance(context).getAllStatements(),
        };
        for (String[] statementGroup : statements) {
            for (String currentStatement : statementGroup) {
                db.execSQL(currentStatement);
            }
        }
    }

    /**
     * If a new version of you app requires schema or data changes,
     * handle them here, similar to onCreate().
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        onCreate(db);
    }
}
```

#### Subclass of `android.app.Application`
```java
package com.hansonchris.android.databasewrappertest;

import android.app.Application;
import android.content.ComponentName;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import com.hansonchris.android.databasewrapper.ApplicationWithDatabaseWrapperInterface;

public class TestApplication extends Application implements ApplicationWithDatabaseWrapperInterface
{
    public void onCreate()
    {
        super.onCreate();
        //calling getInstance() on your Database subclass is optional here
        //and will create the database if the app is a new installation
        //or will update the database if the version has changed
        TestDatabase.getInstance(this);
    }

    public PackageInfo getPackageInfo()
    {
        PackageInfo packageInfo = null;
        ComponentName comp = new ComponentName(this, "TestApplication");
        try  {
            packageInfo = this.getPackageManager().getPackageInfo(comp.getPackageName(), 0);
        } catch (NameNotFoundException e) {}

        return packageInfo;
    }

    public int getApplicationVersionCode()
    {
        int versionCode = getPackageInfo().versionCode;

        return versionCode;
    }
}
```

#### Entity class - represents a single record from a database table
```java
package com.hansonchris.android.databasewrappertest;

import java.util.Calendar;

public class User
{
    protected long id;
    protected String username;
    protected Calendar dateAdded;
    protected String previousUsername;

    public User(String username)
    {
        //no user ID, default date to now
        this(0, username, Calendar.getInstance());
    }

    public User(long id, String username, Calendar dateAdded)
    {
        this.id = id;
        setUsername(username);
        this.dateAdded = dateAdded;
    }

    public long getId()
    {
        return id;
    }

    public void setUsername(String username)
    {
        this.username = prepareUsername(username);
    }

    public String getUsername()
    {
        return username;
    }

    public Calendar getDateAdded()
    {
        return dateAdded;
    }

    protected String prepareUsername(String username)
    {
        return username.trim();
    }
}
```

#### Addition to manifest
##### Necessary to use your subclass of `android.app.Application`
Add this attribute to the `<application>` node, making sure that it's the full class name of your custom Application class:
```xml
android:name="com.hansonchris.android.databasewrappertest.TestApplication"
```

#### Simple implementation example of creating and reading records
```java
package com.hansonchris.android.databasewrappertest;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

public class MainActivity extends Activity
{
    public static final String TAG = "Test";

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addUserToDatabase();
    }

    protected void addUserToDatabase()
    {
        //Instantiate Users database table model
        Users usersModel = Users.getInstance(this);
        //Create User entity
        User user = new User("Chris");
        //Save entity data to database
        usersModel.addUpdateUser(user);
        //Get all rows from users table
        List<User> allUsers = usersModel.getUsers();
        if (allUsers.size() == 0) {
            Log.i(TAG, "No users in database");
        } else {
            for (User currentUser : allUsers) {
                Log.i(TAG, String.valueOf(currentUser.getId()) + ", " + currentUser.getUsername());
            }
        }
    }
}
```
