//unique package
package obd.edu.pdx.mohak.ece558.object_detection;

//Built in library
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

//library for firebase authentication
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Sign up page for new user to register
 */
public class signupActivity extends AppCompatActivity {

    //Different variables
    private static final String TAG = "Mark";
    private ProgressBar progressBar;
    private EditText editTextEmail, editTextPassword, confPassword;
    private FirebaseAuth mAuth;
    private Button SignUp;


    /**
     *  OnCreate is called when the quiz_activity
     *  is started.
     *  super.xxx method whenever we override a method
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // calls the super.onCreate() method and inflates
        // the layout with an ID of R.id.layout
        //Hide the system status bar
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_signup);

        //create the objects
        //wiring up the widgets
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        confPassword = (EditText) findViewById(R.id.editConfPassword);
        progressBar = (ProgressBar) findViewById(R.id.progressbar_1);
        SignUp=(Button)findViewById(R.id.register);
        mAuth = FirebaseAuth.getInstance();

        //Listner for signup button
        // Register the new user on firebase
        SignUp.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View view) {
               registerUser();

            }
        });

    }


    /**
     *Check entered email id and password is valid or not
    * if it is valid then register the user
    * and open the login page
     */
    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String Conf_password = confPassword.getText().toString().trim();

        //Check email field is empty or not
        if (email.isEmpty()) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }

        //check email id pattern is valid or not
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email");
            editTextEmail.requestFocus();
            return;
            }
        //check password field is empty or not
        if (password.isEmpty()) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
            }
        //check password length
        if (password.length() < 6) {
            editTextPassword.setError("Minimum lenght of password should be 6");
            editTextPassword.requestFocus();
            return;
        }
        //Compare confirm password and password
        if(!password.equals(Conf_password))
        {
            confPassword.setError("Password does not match");
            confPassword.requestFocus();
            return;
        }
        //set progressbar visible
        progressBar.setVisibility(View.VISIBLE);

        /**
         * Register the new user on firebase data
         */
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this,new OnCompleteListener<AuthResult>() {

            /**
             * if task is successful then register the user
             * and start login activity
             * else toast the error
             * @param task
             */

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    //  finish();
                    startActivity(new Intent(signupActivity.this, loginActivity.class));
                } else {
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getApplicationContext(), "You are already registered", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
