//unique Package
package obd.edu.pdx.mohak.ece558.object_detection;
//Built in library
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Animated splash screen
 */
public class SplashActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_splash);
        int secondsDelayed = 4;
        /**
         * Handler to handle the splash screen,
         *Start login activity after 4 seconds,
         *Finish the splash activity,
         */
        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivity(new Intent(SplashActivity.this, loginActivity.class));
                finish();
            }
        }, secondsDelayed * 1000);

    }
}
