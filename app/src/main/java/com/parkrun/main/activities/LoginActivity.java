package com.parkrun.main.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parkrun.main.R;
import com.parkrun.main.objects.User;
import com.parkrun.main.util.UtilAlertDialog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends AppCompatActivity
{
    private Button btnLogin, btnRegister;
    private EditText txtAthleteIdLogin, txtPasswordLogin;
    private FirebaseAuth authentication;
    private FirebaseUser databaseUser;
    private ProgressBar progressBarLogin;
    private TextView lblAthleteId, lblPassword;
    private final UtilAlertDialog utilAlertDialog = new UtilAlertDialog(this);

    private boolean isError = false; //class scope boolean for the handler of login thread to check if there was a login error

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if(isError)
            {
                utilAlertDialog.getAlertDialog("Error", "Incorrect login details", LoginActivity.this);

                loginFormVisibility(true);
            }
            else
            {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    };

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

        btnLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                loginFormVisibility(false);

                websiteLoginThread();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // TODO
                //Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                //startActivity(intent);
                // TODO
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

    private void loginFormVisibility(boolean visible)
    {
        if (!visible)
        {
            txtAthleteIdLogin.setVisibility(View.INVISIBLE);
            txtPasswordLogin.setVisibility(View.INVISIBLE);
            btnLogin.setVisibility(View.INVISIBLE);
            btnRegister.setVisibility(View.INVISIBLE);
            lblAthleteId.setVisibility(View.INVISIBLE);
            lblPassword.setVisibility(View.INVISIBLE);

            progressBarLogin.setVisibility(View.VISIBLE);
        }
        else if (visible)
        {
            txtAthleteIdLogin.setVisibility(View.VISIBLE);
            txtPasswordLogin.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.VISIBLE);
            btnRegister.setVisibility(View.VISIBLE);
            lblAthleteId.setVisibility(View.VISIBLE);
            lblPassword.setVisibility(View.VISIBLE);

            progressBarLogin.setVisibility(View.INVISIBLE);
        }
    }

    private void websiteLoginThread()
    {
        final boolean[] correctLogin = {false};
        final boolean[] wasUserCreated = {false};

        final int athleteId = Integer.parseInt(txtAthleteIdLogin.getText().toString().trim());
        final String passString = txtPasswordLogin.getText().toString();

        Runnable loginRun = new Runnable()
        {
            @Override
            public void run()
            {
                HttpsURLConnection connection = null;
                CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL)); // set default cookie manager

                try
                {
                    URL signInUrl = new URL("https://www.parkrun.com/signin/"); //website login URL
                    connection = (HttpsURLConnection) signInUrl.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                    writer.write("athleteid="+athleteId+"&password="+passString+"&submit=OK"); //Login credentials
                    writer.close();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    String lineRead;

                    while ((lineRead = reader.readLine()) != null)
                    {
                        Log.d("Debug", lineRead);

                        if(lineRead.contains("Incorrect username/password combination."))
                        {
                            correctLogin[0] = false;
                            wasUserCreated[0] = false;

                            break;
                        }
                        else if(lineRead.contains("Profile for:"))
                        {
                            correctLogin[0] = true;

                            String[] parts = lineRead.split("</p>", 2);
                            String details = parts[0].substring(parts[0].lastIndexOf('>') + 1).trim();
                            parts = details.split(" ", 3);
                            String firstName = parts[0].trim();
                            String lastName = parts[1].trim();
                            int id = Integer.parseInt(parts[2].substring(parts[2].indexOf("A") + 1, parts[2].indexOf(")")).trim());
                            Log.d("Debug", "Firstname: " + firstName + ", Lastname: " + lastName + " and id: " + id);

                            wasUserCreated[0] = createUser(id, "chrissiemcc44@gmail.com", passString, firstName, lastName); //create a user for firebase features not currently existing in parkrun

                            break;
                        }
                    }

                    reader.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    if(connection != null)
                    {
                        connection.disconnect();
                    }
                }

                /*try
                {
                    URL detailsUrl = new URL("https://www.parkrun.com/profile/update/?Country=UK"); //profile details URL
                    connection = (HttpsURLConnection) detailsUrl.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                    writer.write("athleteid="+athleteId+"&password="+passString+"&submit=OK"); //Login credentials
                    writer.close();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    String lineRead;

                    while ((lineRead = reader.readLine()) != null)
                    {
                        Log.d("Debug", lineRead);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    if(connection != null)
                    {
                        connection.disconnect();
                    }
                }*/

                Log.d("Debug", "FINAL: "+correctLogin[0]);
                Log.d("Debug", "FINAL: "+wasUserCreated[0]);

                if(correctLogin[0] && !wasUserCreated[0])
                {
                    isError = false;

                    login();
                }
                else isError = !(correctLogin[0] && wasUserCreated[0]);

                handler.sendEmptyMessage(0);
            }
        };

        Thread loginThread = new Thread(loginRun);
        loginThread.start();
    }

    private boolean createUser(final int athleteId, final String email, String password, final String firstName, final String lastName)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = database.getReference("users");

        final boolean[] wasUserCreated = {false};

        authentication = FirebaseAuth.getInstance();

        databaseUser = authentication.getCurrentUser();

        if (databaseUser.isAnonymous())
        {
            databaseUser.delete(); //stop the database authentication filling up with anonymous users
        }

        authentication.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if (task.isSuccessful())
                {
                    User user = new User(athleteId, firstName, lastName, email);

                    databaseUser = authentication.getCurrentUser();

                    if (databaseUser != null)
                    {
                        databaseReference.child(databaseUser.getUid()).setValue(user);

                        UserProfileChangeRequest displayName = new UserProfileChangeRequest.Builder().setDisplayName(firstName).build();
                        databaseUser.updateProfile(displayName);
                    }

                    wasUserCreated[0] = true;
                }
                else if(task.getException() instanceof FirebaseAuthUserCollisionException)
                {
                    Log.d("Debug", "User already exists");

                    authentication.signInAnonymously();

                    wasUserCreated[0] = false;
                }
            }
        });

        try
        {
            Thread.sleep(2500);
            // This sleep is to allow the user to be created in time before a boolean value is
            // returned to specify whether a user was created or not (optimisation), this is due to
            // firebase tasks being async, however shouldn't take longer than this (3 seconds).
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        return wasUserCreated[0];
    }

    private void login()
    {
        authentication = FirebaseAuth.getInstance();

        databaseUser = authentication.getCurrentUser();

        final int athleteId = Integer.parseInt(txtAthleteIdLogin.getText().toString().trim());
        final String passString = txtPasswordLogin.getText().toString();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener()
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
                        if (databaseUser.isAnonymous())
                        {
                            databaseUser.delete(); //stop the database authentication filling up with anonymous users
                        }

                        authentication.signInWithEmailAndPassword(user.getEmail(), passString).addOnCompleteListener(new OnCompleteListener<AuthResult>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task)
                            {
                                if (task.isSuccessful())
                                {
                                    Log.d("Testing", "Login was successful");
                                }
                            }
                        });
                        break;
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