package com.parkrun.main;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

public class Utilities
{
    private Context context;

    public Utilities(){}

    public Utilities(Context context)
    {
        this.context = context;
    }

    public void getAlertDialog(String title, String message, Activity currentActivity)
    {
        AlertDialog dialog;
        AlertDialog.Builder loginCorrection = new AlertDialog.Builder(currentActivity);

        loginCorrection.setMessage(message).setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.cancel();
            }
        }).setTitle(title);

        dialog = loginCorrection.create();
        dialog.show();
    }

    public void getAlertDialog(String title, String message, final Activity currentActivity, final Class targetClass)
    {
        AlertDialog dialog;
        AlertDialog.Builder loginCorrection = new AlertDialog.Builder(currentActivity);

        loginCorrection.setMessage(message).setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.cancel();

                Intent intent = new Intent(currentActivity, targetClass);
                context.startActivity(intent);
            }
        }).setTitle(title);

        dialog = loginCorrection.create();
        dialog.show();
    }
}