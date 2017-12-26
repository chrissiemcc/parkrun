package com.parkrun.main;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity
{
    final Utilities utilities = new Utilities(this);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button registerButton = findViewById(R.id.btnRegisterSubmit);

        registerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                createUser();
            }
        });
    }

    private void createUser()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = database.getReference("users");

        EditText firstName = findViewById(R.id.txtFirstNameRegister),
        lastName = findViewById(R.id.txtLastNameRegister),
        email = findViewById(R.id.txtEmailRegister),
        password = findViewById(R.id.txtPasswordRegister),
        passwordConfirm = findViewById(R.id.txtPasswordConfirmRegister);

        final FirebaseAuth authentication = FirebaseAuth.getInstance();

        final String firstNameString = firstName.getText().toString().trim(),
        lastNameString = lastName.getText().toString().trim(),
        emailString = email.getText().toString().trim(),
        passwordString = password.getText().toString().trim(),
        passwordConfirmString = passwordConfirm.getText().toString().trim();

        if (!emailString.equals("") || !passwordString.equals("") || !firstNameString.equals("") || !lastNameString.equals(""))
        {
            if (passwordString.equals(passwordConfirmString))
            {
                final FirebaseUser[] databaseUser = {authentication.getCurrentUser()};

                if (databaseUser[0].isAnonymous())
                {
                    databaseUser[0].delete();//stop the database authentication filling up with anonymous users
                }
                authentication.createUserWithEmailAndPassword(emailString, passwordString).addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if(task.isSuccessful())
                        {
                            User user = new User(firstNameString, lastNameString, emailString, passwordString);

                            databaseUser[0] = authentication.getCurrentUser();

                            if (databaseUser[0] != null)
                            {
                                databaseReference.child(databaseUser[0].getUid()).setValue(user);

                                UserProfileChangeRequest displayName = new UserProfileChangeRequest.Builder().setDisplayName(firstNameString).build();
                                databaseUser[0].updateProfile(displayName);

                                databaseUser[0].sendEmailVerification();

                                authentication.signOut();

                                utilities.getAlertDialog("Email Verification", "A verification email has been sent. Your ID is " + user.getAthleteId(), RegisterActivity.this, LoginActivity.class);
                            }
                        }
                        else if (task.getException() instanceof FirebaseAuthUserCollisionException)
                        {
                            utilities.getAlertDialog("User Exists", "This email address provided is already registered", RegisterActivity.this);
                            authentication.signInAnonymously();
                        }
                        else
                        {
                            utilities.getAlertDialog("Error", task.getException().getMessage(), RegisterActivity.this);
                            authentication.signInAnonymously();
                        }
                    }
                });
            }
            else
            {
                utilities.getAlertDialog("Password Error", "The two passwords provided do not match", RegisterActivity.this);
            }
        }
        else
        {
            utilities.getAlertDialog("Fields Empty", "There are fields that still require to be filled", RegisterActivity.this);
        }
    }
}