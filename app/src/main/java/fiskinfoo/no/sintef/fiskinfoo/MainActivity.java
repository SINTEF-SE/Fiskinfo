/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fiskinfoo.no.sintef.fiskinfoo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fiskinfoo.no.sintef.fiskinfoo.Fragments.EditToolFragment;
import fiskinfoo.no.sintef.fiskinfoo.Fragments.MapFragment;
import fiskinfoo.no.sintef.fiskinfoo.Fragments.MyPageFragment;
import fiskinfoo.no.sintef.fiskinfoo.Fragments.MyToolsFragment;
import fiskinfoo.no.sintef.fiskinfoo.Fragments.OfflineModeFragment;
import fiskinfoo.no.sintef.fiskinfoo.Fragments.SettingsFragment;
import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.SubscriptionEntry;
import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.ToolEntry;
import fiskinfoo.no.sintef.fiskinfoo.Http.BarentswatchApiRetrofit.BarentswatchApi;
import fiskinfoo.no.sintef.fiskinfoo.Http.BarentswatchApiRetrofit.IBarentswatchApi;
import fiskinfoo.no.sintef.fiskinfoo.Http.BarentswatchApiRetrofit.models.PropertyDescription;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.FileDialog;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.FiskInfoUtility;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.FiskinfoScheduledTaskExecutor;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.User;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.UserSettings;
import fiskinfoo.no.sintef.fiskinfoo.Interface.UserInterface;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        SettingsFragment.OnFragmentInteractionListener,
        EditToolFragment.OnFragmentInteractionListener,
        OfflineModeFragment.OnFragmentInteractionListener,
        UserInterface
        {

    public final static int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0x001;
    public final static int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 0x002;
    private final String TAG = MainActivity.this.getClass().getSimpleName();

    private FiskInfoUtility fiskInfoUtility;
    private User user;
    private ScheduledFuture offlineModeBackGroundThread;
    boolean offlineModeLooperPrepared = false;
    private TextView mNetworkErrorTextView;
    private TextView navigationHeaderUserNameTextView;
    private NavigationView navigationView;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;
    private Menu menu;
    private int currentMenuItemID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        user = getIntent().getParcelableExtra("user");
        if (user == null) {
            Log.d(TAG, "did not receive user");
        }

        fiskInfoUtility = new FiskInfoUtility();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        initializeNavigationView();

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.inflateMenu(R.menu.navigation_drawer_menu);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().performIdentifierAction(R.id.navigation_view_subscriptions, 0);
        navigationHeaderUserNameTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.navigation_header_user_name_text_view);

        if(user.getSettings() != null && user.getSettings().getContactPersonName() != null) {
            navigationHeaderUserNameTextView.setText(user.getSettings().getContactPersonName());
        }

        mNetworkErrorTextView = (TextView) findViewById(R.id.activity_main_network_error_text_view);

        if(!fiskInfoUtility.isNetworkAvailable(getBaseContext())) {
            toggleNetworkErrorTextView(false);
        }

        if(user.getOfflineMode()) {
            initAndStartOfflineModeBackgroundThread();
        }
    }

    public void refreshTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(title);
        }
    }

    private void initializeNavigationView() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    public void navigate(int menuItemID) {
        navigationView.getMenu().performIdentifierAction(menuItemID, 0);
    }

    private void stopOfflineModeBackgroundThread() {
        if (offlineModeBackGroundThread != null) {
            offlineModeBackGroundThread.cancel(true);
            offlineModeBackGroundThread = null;
            offlineModeLooperPrepared = false;
        }

        MenuItem offlineModeItem = menu.findItem(R.id.action_offline_mode);

        offlineModeItem.setVisible(false);
        invalidateOptionsMenu();
    }

    private void initAndStartOfflineModeBackgroundThread() {
        if(menu != null) {
            MenuInflater inflater = getMenuInflater();

            if(user.getOfflineMode()) {
                inflater.inflate(R.menu.menu_offline_mode, menu);
            }

            MenuItem offlineModeItem = menu.findItem(R.id.action_offline_mode);
            offlineModeItem.setVisible(true);
        }

        offlineModeBackGroundThread = new FiskinfoScheduledTaskExecutor(2).scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Handler handler = new Handler(getMainLooper());

                if(!fiskInfoUtility.isNetworkAvailable(getBaseContext())) {
                    Log.i(TAG, "Offline mode update skipped");
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            toggleNetworkErrorTextView(false);
                        }
                    }, 100);

                    return;
                } else {
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            toggleNetworkErrorTextView(true);
                        }
                    }, 100);
                }

                BarentswatchApi barentswatchApi = new BarentswatchApi();
                IBarentswatchApi api = barentswatchApi.getApi();
                List<PropertyDescription> subscribables;
                Response response;
                String downloadPath;
                String format = "JSON";
                Date lastUpdatedDateTime;
                Date lastUpdatedCacheDateTime;
                byte[] data = null;
                boolean success;

                subscribables = api.getSubscribable();
                downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/FiskInfo/Offline/";

                if(!offlineModeLooperPrepared) {
                    Looper.prepare();
                    offlineModeLooperPrepared = true;
                }

                for(PropertyDescription subscribable : subscribables) {
                    SubscriptionEntry cacheEntry = user.getSubscriptionCacheEntry(subscribable.ApiName);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

                    if(cacheEntry == null || !cacheEntry.mOfflineActive) {
                        continue;
                    }

                    try {
                        lastUpdatedDateTime = simpleDateFormat.parse(subscribable.LastUpdated);
                        lastUpdatedCacheDateTime = simpleDateFormat.parse(cacheEntry.mLastUpdated.equals(getBaseContext().getString(R.string.abbreviation_na)) ? "2000-00-00T00:00:00" : cacheEntry.mLastUpdated);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Invalid datetime provided");
                        continue;
                    }

                    if(lastUpdatedDateTime.getTime() <= lastUpdatedCacheDateTime.getTime()){
                        continue;
                    }

                    response = api.geoDataDownload(subscribable.ApiName, format);

                    if(response.getStatus() != 200) {
                        Log.i(TAG, "Download failed");
                        continue;
                    }

                    try {
                        data = FiskInfoUtility.toByteArray(response.getBody().in());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    success = new FiskInfoUtility().writeMapLayerToExternalStorage(MainActivity.this,
                            data,
                            subscribable.Name
                                    .replace(",", "")
                                    .replace(" ", "_"),
                            format, downloadPath, false);

                    if(success) {
                        cacheEntry.mLastUpdated = subscribable.LastUpdated;
                        cacheEntry.mSubscribable = subscribable;
                        user.setSubscriptionCacheEntry(subscribable.ApiName, cacheEntry);
                    }
                }

                user.writeToSharedPref(getBaseContext());
                Log.i(TAG, "Offline mode update");
                System.out.println("BEEP");
            }
        }, getResources().getInteger(R.integer.zero), getResources().getInteger(R.integer.offline_mode_interval_time_seconds), TimeUnit.SECONDS);
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(permsRequestCode, permissions, grantResults);

        // Most requests are made from fragments, so results are handled there. Request codes used are also different when received in activity as compared to the original value used and returned to fragment on result, see http://stackoverflow.com/a/30334435
        switch(permsRequestCode){
            case 200:
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION:
                break;
            default:
//                Toast.makeText(this, R.string.permission_denied_app_limited, Toast.LENGTH_LONG).show();
                break;
        }
    }

    public synchronized void onActivityResult(final int requestCode, int resultCode, final Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            String filePath = data.getStringExtra(FileDialog.RESULT_PATH) + "/";
            user.setFilePathForExternalStorage(filePath);
            Toast mToast = Toast.makeText(this, "SELECTED FILEPATH AT: " + filePath, Toast.LENGTH_LONG);
            user.writeToSharedPref(this);
            mToast.show();
        }
    }

    public void toggleNetworkErrorTextView(boolean networkAvailable) {
        if(!networkAvailable) {
            mNetworkErrorTextView.setText(R.string.no_internet_access);
            mNetworkErrorTextView.setTextColor(ContextCompat.getColor(this, R.color.error_red));
            mNetworkErrorTextView.setVisibility(View.VISIBLE);
        } else {
            mNetworkErrorTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        android.support.v4.app.Fragment fragment;
        String tag;
        switch (item.getItemId()) {
            case R.id.navigation_view_subscriptions:
                fragment = MyPageFragment.newInstance();
                tag = MyPageFragment.FRAGMENT_TAG;
                break;
            case R.id.navigation_view_map:
                fragment = MapFragment.newInstance();
                tag = MapFragment.FRAGMENT_TAG;
                break;
            case R.id.navigation_view_tools:
                fragment = MyToolsFragment.newInstance();
                tag = MyToolsFragment.FRAGMENT_TAG;
                break;
            case R.id.navigation_view_settings:
                fragment = SettingsFragment.newInstance();
                tag = SettingsFragment.FRAGMENT_TAG;
                break;
            default:
                return false;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentByTag(tag);

        if (currentFragment != null && currentFragment.isVisible()) {
            mDrawerLayout.closeDrawers();
            return true;
        }

        Bundle userBundle = new Bundle();
        userBundle.putParcelable("user", user);
        fragment.setArguments(userBundle);

        fragmentManager.popBackStack();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction()
                .replace(R.id.main_activity_fragment_container, fragment, tag)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();

        currentMenuItemID = item.getItemId();
        navigationView.setCheckedItem(item.getItemId());
        mDrawerLayout.closeDrawers();
        return true;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();

        if(user.getOfflineMode()) {
            inflater.inflate(R.menu.menu_offline_mode, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_offline_mode:
                FragmentManager fragmentManager = getSupportFragmentManager();
                OfflineModeFragment fragment = OfflineModeFragment.newInstance(user);
                Fragment currentFragment = fragmentManager.findFragmentByTag(getString(R.string.offline_mode));

                if (currentFragment != null && currentFragment.isVisible()) {
                    return true;
                }

                fragmentManager.beginTransaction()
                        .replace(R.id.main_activity_fragment_container, fragment, getString(R.string.offline_mode))
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(getString(R.string.offline_mode))
                        .commit();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void deleteToolLogEntry(ToolEntry tool) {
        user.getToolLog().removeTool(tool.getSetupDate(), tool.getToolLogId());
        user.writeToSharedPref(this);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.Fragment fragment = fragmentManager.findFragmentByTag(MyPageFragment.FRAGMENT_TAG);

        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if ((getSupportFragmentManager().getBackStackEntryCount() > 0) ||
                (fragment != null && fragment.isVisible())) {
            super.onBackPressed();
        }
        else {
            navigate(R.id.navigation_view_subscriptions);
        }
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setOfflineMode(boolean active) {
        user.setOfflineMode(active);
        user.writeToSharedPref(this);
    }

    @Override
    public void updateUserSettings(UserSettings userSettings) {
        user.setSettings(userSettings);
        user.writeToSharedPref(this);
    }

    @Override
    public void updateTool(ToolEntry tool) {
        if(tool != null && !tool.getSetupDate().isEmpty()) {
            user.getToolLog().addTool(tool, tool.getSetupDate());
            user.writeToSharedPref(this);
            Toast.makeText(this, R.string.tool_updated, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void toggleOfflineMode(boolean offline) {
        if(offline) {
            initAndStartOfflineModeBackgroundThread();
        } else {
            stopOfflineModeBackgroundThread();
        }
    }
}