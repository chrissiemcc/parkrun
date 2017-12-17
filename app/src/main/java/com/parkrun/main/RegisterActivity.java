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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity
{
    Button registerButton;
    DatabaseReference databaseUsers;
    EditText firstName, lastName, email, password;
    FirebaseAuth authentication;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    Intent intent;
    String firstNameString, lastNameString, emailString, passwordString;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        databaseUsers = database.getReference("Users");

        authentication = FirebaseAuth.getInstance();

        firstName = findViewById(R.id.firstNameRegisterField);
        lastName = findViewById(R.id.lastNameRegisterField);
        email = findViewById(R.id.emailRegisterField);
        password = findViewById(R.id.passwordRegisterField);

        registerButton = findViewById(R.id.registerSubmit);

        registerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                firstNameString = firstName.getText().toString();
                lastNameString = lastName.getText().toString();
                emailString = email.getText().toString();
                passwordString = password.getText().toString();

                user = new User(firstNameString, lastNameString, emailString, passwordString);

                databaseUsers.child("A"+user.getAthleteId()).setValue(user);

                authentication.createUserWithEmailAndPassword(emailString, passwordString).addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if(task.isSuccessful())
                        {
                            intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                            Toast.makeText(getApplicationContext(),"Registration Complete", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),"Registration Unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
