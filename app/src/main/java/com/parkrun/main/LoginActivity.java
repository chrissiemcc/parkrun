package com.parkrun.main;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

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
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final Button loginButton = findViewById(R.id.btnLogin);
        final Button registerButton = findViewById(R.id.btnRegister);
        loginButton.setEnabled(false);

        final EditText athleteNumber = findViewById(R.id.txtAthleteNumberLogin);
        final EditText password = findViewById(R.id.txtPasswordLogin);

        final ProgressBar progressBar = findViewById(R.id.progressBar_login);
        progressBar.setVisibility(View.INVISIBLE);

        loginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                login(athleteNumber, password, loginButton, registerButton, progressBar);
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        athleteNumber.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count)
            {
                if(athleteNumber.getText().toString().equals("") && password.getText().toString().equals(""))
                {
                    loginButton.setEnabled(false);
                }
                else
                {
                    loginButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable)
            {

            }
        });
    }

    private void login(EditText athleteNumber, EditText password, final Button loginButton, final Button registerButton, final ProgressBar progressBar)
    {
        final int athleteId;
        final String passString;
        final String[] correctPass = new String[1];

        DatabaseReference databaseUsers = FirebaseDatabase.getInstance().getReference("users");

        athleteId = Integer.parseInt(athleteNumber.getText().toString());
        passString = password.getText().toString();
        loginButton.setVisibility(View.INVISIBLE);
        registerButton.setVisibility(View.INVISIBLE);

        progressBar.setVisibility(View.VISIBLE);

        databaseUsers.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                for (DataSnapshot child : children)
                {
                    User user = child.getValue(User.class);

                    if (user != null && user.getAthleteId() == athleteId)
                    {
                        correctPass[0] = user.getPassword();

                        if(passString.equals(correctPass[0]))
                        {
                            FirebaseAuth authentication = FirebaseAuth.getInstance();

                            FirebaseUser databaseUser = authentication.getCurrentUser();

                            if (databaseUser.isAnonymous())
                            {
                                databaseUser.delete();//stop the database authentication filling up with anonymous users
                            }

                            authentication.signInWithEmailAndPassword(user.getEmail(), correctPass[0]).addOnCompleteListener(new OnCompleteListener<AuthResult>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }
                                    else
                                    {
                                        Toast.makeText(getApplicationContext(),task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            break;
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),"Wrong password", Toast.LENGTH_SHORT).show();

                            loginButton.setVisibility(View.VISIBLE);
                            registerButton.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);
                            break;
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }
}