package com.parkrun.main.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.tooltip.OnDismissListener;
import com.tooltip.Tooltip;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
    private int athleteId;
    private String passString;

    private Button btnLogin, btnTooltip;
    private EditText txtAthleteIdLogin, txtPasswordLogin;
    private FirebaseAuth authentication;
    private FirebaseUser databaseUser;
    private ProgressBar progressBarLogin;
    private TextView lblAthleteId, lblPassword;
    private final UtilAlertDialog utilAlertDialog = new UtilAlertDialog(this);
    private DatabaseReference databaseReference;

    private boolean isError = false; //class scope boolean for the handler of login thread to check if there was a login error
    private boolean tooltipShowing = false; //class scope boolean to see if tooltip is showing

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
                changeActivity();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = findViewById(R.id.btnLogin);
        btnTooltip = findViewById(R.id.btnTooltip);

        txtAthleteIdLogin = findViewById(R.id.txtAthleteIdLogin);
        txtPasswordLogin = findViewById(R.id.txtPasswordLogin);

        progressBarLogin = findViewById(R.id.progressBarLogin);

        lblAthleteId = findViewById(R.id.lblAthleteId);
        lblPassword = findViewById(R.id.lblPassword);

        btnLogin.setEnabled(false);

        progressBarLogin.setVisibility(View.INVISIBLE);

        authentication = FirebaseAuth.getInstance();
        databaseUser = authentication.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        btnLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                closeKeyboard();
                loginFormVisibility(false);

                athleteId = Integer.parseInt(txtAthleteIdLogin.getText().toString().trim());
                passString = txtPasswordLogin.getText().toString();

                loginThread();
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

        btnTooltip.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showToolTip(view, Gravity.BOTTOM);
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
            lblAthleteId.setVisibility(View.INVISIBLE);
            lblPassword.setVisibility(View.INVISIBLE);

            progressBarLogin.setVisibility(View.VISIBLE);
        }
        else if (visible)
        {
            txtAthleteIdLogin.setVisibility(View.VISIBLE);
            txtPasswordLogin.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.VISIBLE);
            lblAthleteId.setVisibility(View.VISIBLE);
            lblPassword.setVisibility(View.VISIBLE);

            progressBarLogin.setVisibility(View.INVISIBLE);
        }
    }

    private void loginThread()
    {
        final boolean[] correctLogin = {false};
        final boolean[] wasUserCreated = {false};

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

                        if(lineRead.contains("Incorrect username/password combination.")) //Login UNSUCCESSFUL
                        {
                            correctLogin[0] = false;
                            wasUserCreated[0] = false;

                            break;
                        }
                        else if(lineRead.contains("Profile for:")) //Login SUCCESSFUL
                        {
                            correctLogin[0] = true;

                            String details, firstName, lastName, gender, DOBMonth, runningClubName, email, parkrunName, postcode, ICEName, ICEContact, medicalInfo;
                            int athleteId, DOBDay, DOBYear, runningClubId;

                            String[] parts = lineRead.split("</p>", 2);
                            details = parts[0].substring(parts[0].lastIndexOf('>') + 1).trim();
                            parts = details.split(" ", 3);
                            firstName = parts[0].trim();
                            lastName = parts[1].trim();
                            athleteId = Integer.parseInt(parts[2].substring(parts[2].indexOf("A") + 1, parts[2].indexOf(")")).trim());

                            Document detailsDoc = Jsoup.connect("https://admin.parkrun.com/runnerSupport/UK/update.php?Ath="+athleteId+"&Conf=561d52342f816f65f39f").get();
                            // Retrieve parkrun profile details

                            Element formTable = detailsDoc.selectFirst("table");

                            //get profile details off form and initialise Strings

                            gender = formTable.selectFirst("input[name=Sex][checked]").attr("value"); //get gender
                            DOBDay = Integer.parseInt(formTable.selectFirst("select[name=DOBDay]").selectFirst("option[selected=selected]").text()); //get DOB Day
                            DOBMonth = formTable.selectFirst("select[name=DOBMonth]").selectFirst("option[selected=selected]").text(); //get DOB Month
                            DOBYear = Integer.parseInt(formTable.selectFirst("select[name=DOBYear]").selectFirst("option[selected=selected]").text()); //get DOB Year
                            runningClubId = Integer.parseInt(formTable.selectFirst("select[name=club]").selectFirst("option[selected=selected]").attr("value")); //get running club id;
                            runningClubName = formTable.selectFirst("select[name=club]").selectFirst("option[selected=selected]").text(); //get running club name
                            email = formTable.selectFirst("input[name=email]").attr("value"); //get Email
                            parkrunName = formTable.selectFirst("select[name=homerun]").selectFirst("option[selected=selected]").text().toLowerCase().replace(" ", ""); //get home parkrun name
                            postcode = formTable.selectFirst("input[name=postcode]").attr("value"); //get postcode
                            ICEName = formTable.selectFirst("input[name=ICEName]").attr("value"); //get ICE Name
                            ICEContact = formTable.selectFirst("input[name=ICEContact]").attr("value"); //get ICE Contact number
                            medicalInfo = formTable.selectFirst("textarea[name=medicalInfo]").text(); //get medical info

                            wasUserCreated[0] = createUser(athleteId, email, passString, firstName, lastName, gender, DOBDay, DOBMonth, DOBYear,
                                    runningClubId, runningClubName, parkrunName, postcode, ICEName, ICEContact, medicalInfo); //create a user for firebase features not currently existing in parkrun

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

                Log.d("Debug", "FINAL: Login was successful: "+correctLogin[0]);
                Log.d("Debug", "FINAL: A new user was created: "+wasUserCreated[0]);

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

    private boolean createUser(final int athleteId, final String email, String password, final String firstName, final String lastName, final String gender,
                               final int DOBDay, final String DOBMonth, final int DOBYear, final int runningClubId, final String runningClubName, final String parkrunName, final String postcode,
                               final String ICEName, final String ICEContact, final String medicalInfo)
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
                    User user = new User(athleteId, firstName, lastName, email, gender, DOBDay, DOBMonth, DOBYear, runningClubId,
                            runningClubName, parkrunName, postcode, ICEName, ICEContact, medicalInfo, false);

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
        databaseUser = authentication.getCurrentUser();

        final int athleteId = Integer.parseInt(txtAthleteIdLogin.getText().toString().trim());
        final String passString = txtPasswordLogin.getText().toString();

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

        twoSecondSleep();
    }

    private void twoSecondSleep()
    {
        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        // This sleep is to stop the home fragment displaying "Welcome null!" due to async login
        // having not finished yet
    }

    private void changeActivity()
    {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void closeKeyboard()
    {
        InputMethodManager inputManager = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void showToolTip(View v, int gravity)
    {
        Button button = (Button) v;
        Tooltip.Builder tooltip;

        if(!tooltipShowing)
        {
            tooltip = new Tooltip.Builder(button)
                    .setText("Firebase is used to store personal details of parkrun accounts")
                    .setTextColor(Color.WHITE)
                    .setGravity(gravity)
                    .setCornerRadius(8f)
                    .setDismissOnClick(true)
                    .setOnDismissListener(new OnDismissListener()
                    {
                        @Override
                        public void onDismiss()
                        {
                           tooltipShowing = false;
                       }
                   });

            tooltip.show();

            tooltipShowing = true;
        }
    }
}