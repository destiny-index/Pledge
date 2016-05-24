package au.edu.unsw.pledge.loginsystem;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by youhancheery on 7/05/2016.
 */
public class TestLoginApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //before we can start using Firebase it must be initialised one time with Android context
        Firebase.setAndroidContext(this);
    }
}
