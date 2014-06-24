package com.hansonchris.android.databasewrapper;

import android.content.pm.PackageInfo;

public interface ApplicationWithDatabaseWrapperInterface
{
    public PackageInfo getPackageInfo();

    public int getApplicationVersionCode();
}
