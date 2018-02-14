package com.parkrun.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity
{
    private Button btnLogin, btnRegister;
    private EditText txtAthleteIdLogin, txtPasswordLogin;
    private FirebaseAuth authentication;
    private FirebaseUser databaseUser;
    private ProgressBar progressBarLogin;
    private TextView lblAthleteId, lblPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        txtAthleteIdLogin = findViewById(R.id.txtAthleteIdLogin);
        txtPasswordLogin = findViewById(R.id.txtPasswordLogin);

        progressBarLogin = findViewById(R.id.progressBarLogin);

        lblAthleteId = findViewById(R.id.lblAthleteId);
        lblPassword = findViewById(R.id.lblPassword);

        btnLogin.setEnabled(false);

        progressBarLogin.setVisibility(View.INVISIBLE);

        authentication = FirebaseAuth.getInstance();

        databaseUser = authentication.getCurrentUser();

        SharedPreferences sharedPreferences = getSharedPreferences("authQuery", Context.MODE_PRIVATE);
        if (databaseUser != null && !databaseUser.isAnonymous() && sharedPreferences.getBoolean("newUser", false))
        {
            authentication.signOut();
            authentication.signInAnonymously();
        }
        // Firebase authentication requires that new users are signed in once created,
        // this was not suitable for the mobile application so this is needed so that
        // new users cannot close the app once an account has been created and access
        // their account on reopening without having to verify their email or login.

        btnLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                login();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        txtAthleteIdLogin.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count)
            {
                if(txtAthleteIdLogin.getText().toString().equals("") || txtPasswordLogin.getText().toString().equals(""))
                {
                    btnLogin.setEnabled(false);
                }
                else
                {
                    btnLogin.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable)
            {

            }
        });

        txtPasswordLogin.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count)
            {
                if(txtAthleteIdLogin.getText().toString().equals("") || txtPasswordLogin.getText().toString().equals(""))
                {
                    btnLogin.setEnabled(false);
                }
                else
                {
                    btnLogin.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable)
            {

            }
        });
    }

    private void login()
    {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");

        authentication = FirebaseAuth.getInstance();

        databaseUser = authentication.getCurrentUser();

        final int athleteId = Integer.parseInt(txtAthleteIdLogin.getText().toString().trim());
        final String passString = txtPasswordLogin.getText().toString();

        final Utilities utilities = new Utilities();

        loginFormVisibility(0);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                boolean userFound = false;

                for (DataSnapshot child : children)
                {
                    User user = child.getValue(User.class);

                    if (user != null && user.getAthleteId() == athleteId)
                    {
                        userFound = true;

                        if (databaseUser.isAnonymous())
                        {
                            databaseUser.delete(); //stop the database authentication filling up with anonymous users
                        }

                        authentication.signInWithEmailAndPassword(user.getEmail(), passString).addOnCompleteListener(new OnCompleteListener<AuthResult>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task)
                            {
                                databaseUser = authentication.getCurrentUser();

                                if (task.isSuccessful() && databaseUser.isEmailVerified())
                                {
                                    Log.d("Testing", "Login was successful");

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else
                                {
                                    if (!task.isSuccessful() && task.getException().getMessage().equals("The password is invalid or the user does not have a password."))
                                    {
                                        utilities.getAlertDialog("Password Invalid", "The password provided does not match the password for this ID.", LoginActivity.this);
                                    }
                                    else if (task.isSuccessful() && !databaseUser.isEmailVerified())
                                    {
                                        utilities.getAlertDialog("Email Not Verified", "This account has not yet been verified.", LoginActivity.this);
                                    }
                                    else
                                    {
                                        utilities.getAlertDialog("Error", task.getException().getMessage(), LoginActivity.this);
                                    }

                                    loginFormVisibility(1);

                                    authentication.signInAnonymously();
                                }
                            }
                        });
                    break;
                    }
                }

                if (!userFound)
                {
                    utilities.getAlertDialog("User Not Found", "No user was found matching the ID provided.", LoginActivity.this);

                    loginFormVisibility(1);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void loginFormVisibility(int choice)
    {
        switch (choice)
        {
            case 0:
                txtAthleteIdLogin.setVisibility(View.INVISIBLE);
                txtPasswordLogin.setVisibility(View.INVISIBLE);
                btnLogin.setVisibility(View.INVISIBLE);
                btnRegister.setVisibility(View.INVISIBLE);
                lblAthleteId.setVisibility(View.INVISIBLE);
                lblPassword.setVisibility(View.INVISIBLE);

                progressBarLogin.setVisibility(View.VISIBLE);
                break;
            case 1:
                txtAthleteIdLogin.setVisibility(View.VISIBLE);
                txtPasswordLogin.setVisibility(View.VISIBLE);
                btnLogin.setVisibility(View.VISIBLE);
                btnRegister.setVisibility(View.VISIBLE);
                lblAthleteId.setVisibility(View.VISIBLE);
                lblPassword.setVisibility(View.VISIBLE);

                progressBarLogin.setVisibility(View.INVISIBLE);
                break;
            default:
                break;
        }
    }
}