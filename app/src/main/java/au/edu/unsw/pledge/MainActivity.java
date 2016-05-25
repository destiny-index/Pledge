package au.edu.unsw.pledge;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;


import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import com.firebase.client.Firebase;

import au.edu.unsw.pledge.fragments.FragmentHome;
import au.edu.unsw.pledge.fragments.FragmentPayment;
import au.edu.unsw.pledge.fragments.FragmentSettings;
import au.edu.unsw.pledge.loginsystem.Constants;
import au.edu.unsw.pledge.loginsystem.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private Firebase mRef;
    private int[] tabIcons = {
            R.drawable.icwifi,
            R.drawable.icpay,
            R.drawable.icsetting
    };
    private TabLayout tabLayout;
    //private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(this); // ensure defaults are set
        if (prefs.getString("pref_paymentAccount", "").equals("") && getIntent().hasExtra("email")) {
            System.out.println(getIntent().getStringExtra("email"));
            prefs.edit().putString("pref_paymentAccount", getIntent().getStringExtra("email")).commit();
        }

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        setupTabIcons();

        //firebase SDK
        Firebase.setAndroidContext(this);
        mRef = new Firebase(Constants.FIREBASE_URL);
        if (mRef.getAuth() == null) { //no user exists
            loadLoginView();
        }
//        //set intent for login page
//        Intent intent = new Intent(this, LoginActivity.class);
//        startActivity(intent);
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getFragmentManager());
        adapter.addFrag(new FragmentHome(), "Nearby");
        adapter.addFrag(new FragmentPayment(), "Payment");
        adapter.addFrag(new FragmentSettings(), "Settings");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/
        if (id == R.id.action_logout) {
            mRef.unauth();
            loadLoginView();
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadLoginView() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //prevents the user going to login screen if they press back on main_activity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}
