package com.parkrun.main;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

public class Utilities
{
    public void getAlertDialog(String title, String message, Activity activity)
    {
        AlertDialog dialog;
        AlertDialog.Builder loginCorrection = new AlertDialog.Builder(activity);

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
}