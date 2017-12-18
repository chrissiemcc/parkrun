package com.parkrun.main;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity
{
    DatabaseReference databaseUsers;
    EditText firstName, lastName, email, password;
    FirebaseAuth authentication;
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        databaseUsers = database.getReference("users");

        authentication = FirebaseAuth.getInstance();

        firstName = findViewById(R.id.firstNameRegisterField);
        lastName = findViewById(R.id.lastNameRegisterField);
        email = findViewById(R.id.emailRegisterField);
        password = findViewById(R.id.passwordRegisterField);

        Button registerButton = findViewById(R.id.registerSubmit);

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
        final String firstNameString = firstName.getText().toString();
        final String lastNameString = lastName.getText().toString();
        final String emailString = email.getText().toString();
        final String passwordString = password.getText().toString();

        if (!emailString.equals("") && !passwordString.equals(""))
        {
            authentication.createUserWithEmailAndPassword(emailString, passwordString).addOnCompleteListener(new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if(task.isSuccessful())
                    {
                        User user = new User(firstNameString, lastNameString, emailString, passwordString);

                        FirebaseUser databaseUser = authentication.getCurrentUser();

                        int id = user.getAthleteId();

                        if (databaseUser != null)
                        {
                            databaseUsers.child(databaseUser.getUid()).setValue(user);
                            Toast.makeText(getApplicationContext(),"Registration Complete. Your ID is "+id, Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                    }
                    else if (task.getException() instanceof FirebaseAuthUserCollisionException)
                    {
                        Toast.makeText(getApplicationContext(), "You are already registered", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Fields empty", Toast.LENGTH_SHORT).show();
        }
    }
}
