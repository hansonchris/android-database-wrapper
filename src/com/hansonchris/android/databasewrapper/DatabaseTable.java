package com.hansonchris.android.databasewrapper;

import android.content.Context;

import java.util.List;
import java.util.Vector;

abstract public class DatabaseTable 
{
    protected Context context;

    public DatabaseTable(Context context)
    {
        this.context = context;
    }

    abstract protected String getCreateStatement();

    public String[] getAllStatements()
    {
        Vector<String> statements = new Vector<String>();
        addStatements(statements, getExtraStatementsPreCreate());
        statements.add(getCreateStatement());
        addStatements(statements, getExtraStatementsPostCreate());

        return statements.toArray(new String[]{});
    }

    protected void addStatements(List<String> allStatements, String[] newStatements)
    {
        if (newStatements != null) {
            for (String currentStatement : newStatements) {
                allStatements.add(currentStatement);
            }
        }
    }

    protected String[] getExtraStatementsPreCreate()
    {
        return null;
    }

    protected String[] getExtraStatementsPostCreate()
    {
        return null;
    }
}