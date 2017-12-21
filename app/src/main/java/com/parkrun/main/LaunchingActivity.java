package com.parkrun.main;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LaunchingActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launching);

        FirebaseAuth authentication = FirebaseAuth.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && !user.isAnonymous())
        {
            Intent intent = new Intent(LaunchingActivity.this, MainActivity.class);
            startActivity(intent);
        }
        else
        {
            signInAnonymously(authentication);

            Intent intent = new Intent(LaunchingActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        finish();
    }

    private void signInAnonymously(FirebaseAuth authentication)
    {
        authentication.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if (!task.isSuccessful())
                {
                    Toast.makeText(getApplicationContext(),task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
