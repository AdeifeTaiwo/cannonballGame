package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.AttributeSet;

import androidx.appcompat.app.AlertDialog;


@SuppressLint("ValidFragment")
public class MyAlertDialogFragment extends DialogFragment {
    private static Context context;
    private static AttributeSet attr;
    private static int shotsFired;
    private static double totalElapsedTime;



    @SuppressLint({"ValidFragment", "ResourceType"})
    public MyAlertDialogFragment(int shotsFired, double totalElapsedTime, Context context) {
    this.shotsFired = shotsFired;
    this.totalElapsedTime = totalElapsedTime;
    this.context = context;


    }


    public static MyAlertDialogFragment newInstance(int title){
        MyAlertDialogFragment frag = new MyAlertDialogFragment(shotsFired, totalElapsedTime, context);
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }


    CannonView cannonView;




        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            //// *******************************************************8check the below well sir/ ma
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("hello");
            builder.setMessage(getResources().getString(R.string.results_format, shotsFired,totalElapsedTime));


            builder.setPositiveButton(R.string.reset_game, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    cannonView.dialogIsDisplayed = false;
                    cannonView.newGame();
                }
            });


            return builder.create();
        }
    };




