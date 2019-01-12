//unique package
package obd.edu.pdx.mohak.ece558.object_detection;
//built in library
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

//library for firebase
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import static android.widget.Toast.makeText;

/**
 * Secured login screen for user to
 * use the app
 */
public class loginActivity extends AppCompatActivity {

    //variables
    private RelativeLayout rellay2;
    private EditText Email_id;
    private EditText Password;
    private Button Login_now;
    private Button Sign_Up;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    /**
     *  OnCreate is called when the quiz_activity
     *  is started.
     *  super.xxx method whenever we override a method
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // calls the super.onCreate() method and inflates
        // the layout with an ID of R.id.layout
        //Hide the system status bar
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        //create objects and
        //wiring up the widget
        rellay2 = findViewById(R.id.rellay2);
        rellay2.setVisibility(View.VISIBLE);
        mAuth = FirebaseAuth.getInstance();
        Email_id = findViewById(R.id.email_id);
        Password = findViewById(R.id.Password);
        Login_now =findViewById(R.id.login_button);
        Sign_Up =findViewById(R.id.Signup);
        progressBar =  findViewById(R.id.progressbar);

        //listner for login button
        //if email id and password is correct then
        // open camera activity
        Login_now.setOnClickListener(new View.OnClickListener() {

            /**
             * if email id and password is correct then
             * open camera activity
             * @param view
             */
            @Override
            public void onClick(View view) {
                userLogin();
            }
        });

        //listner for sign up button
        Sign_Up.setOnClickListener(new View.OnClickListener() {

            /**
             * open sign up activity
             * for user to register
             * @param view
             */

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(loginActivity.this, signupActivity.class);
                startActivity(intent);

            }
        });
    }

    /**
     *Check entered email id and password is valid or not
     * if it is valid then register the user
     * and open the camera activity
     */
    private void userLogin() {
        String email = Email_id.getText().toString().trim();
        String password = Password.getText().toString().trim();

        //check email field is empty or not
        if (email.isEmpty()) {
            Email_id.setError("Email is required");
            Email_id.requestFocus();
            return;
        }

        //check email pattern is valid or not
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Email_id.setError("Please enter a valid email");
            Email_id.requestFocus();
            return;
        }

        //check password field is empty or not
        if (password.isEmpty()) {
            Password.setError("Password is required");
            Password.requestFocus();
            return;
        }
        //chec password length
        if (Password.length() < 6) {
            Password.setError("Minimum lenght of password should be 6");
            Password.requestFocus();
            return;
        }

        // set progressbar visible
        progressBar.setVisibility(View.VISIBLE);

        /**
         * check email id and password match with
         * register user
         */
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

            /**
             * if email id and password is valid
             * opn the camera activity
             * else toast the error
             * @param task
             */

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    finish();
                    Intent intent = new Intent(loginActivity.this, CameraActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /**
     * exit the app if user pressed bac button
     */
    @Override
    public void onBackPressed(){
        finishAffinity();    }

}
