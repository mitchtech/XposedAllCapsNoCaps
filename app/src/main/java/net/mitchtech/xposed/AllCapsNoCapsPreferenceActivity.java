
package net.mitchtech.xposed;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.cgollner.unclouded.preferences.SwitchPreferenceCompat;
import com.tsengvn.typekit.TypekitContextWrapper;

import net.mitchtech.xposed.allcapsnocaps.R;

public class AllCapsNoCapsPreferenceActivity extends AppCompatActivity {

    private static final String TAG = AllCapsNoCapsPreferenceActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
//        addPreferencesFromResource(R.xml.settings);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(android.R.id.content, new SettingsFragment()).commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragment implements
            SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
            addPreferencesFromResource(R.xml.settings);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            // Enforce mutual exclusion of all caps and all lower case preferences
            if (key.contentEquals("prefAllCaps") && sharedPreferences.getBoolean("prefAllCaps", false)) {
                if (sharedPreferences.getBoolean("prefNoCaps", false)) {
                    SwitchPreferenceCompat pref = (SwitchPreferenceCompat) findPreference("prefNoCaps");
                    pref.setChecked(false);
                }
            } else if (key.contentEquals("prefNoCaps")
                    && sharedPreferences.getBoolean("prefNoCaps", false)) {
                if (sharedPreferences.getBoolean("prefAllCaps", false)) {
                    SwitchPreferenceCompat pref = (SwitchPreferenceCompat) findPreference("prefAllCaps");
                    pref.setChecked(false);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_exit:
                this.finish();
                return true;
        }
        return false;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

    public static String getVersion(Context context) {
        String version = "1.0";
        try {
            PackageInfo pi = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            version = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package name not found", e);
        }
        return version;
    }
}
