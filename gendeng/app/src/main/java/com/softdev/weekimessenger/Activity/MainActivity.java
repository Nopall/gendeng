package com.softdev.weekimessenger.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.softdev.weekimessenger.Configuration.Config;
import com.softdev.weekimessenger.Configuration.NotificationHandler;
import com.softdev.weekimessenger.Handlers.AppHandler;
import com.softdev.weekimessenger.Layouts.Chatbox;
import com.softdev.weekimessenger.Layouts.Friends;
import com.softdev.weekimessenger.Layouts.Groups;
import com.softdev.weekimessenger.Layouts.Inbox;
import com.softdev.weekimessenger.R;
import com.softdev.weekimessenger.Services.GCMService;
import com.softdev.weekimessenger.Services.WebService;

public class MainActivity extends AppCompatActivity {

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    Menu menu;

    Groups groupsFragment;
    Inbox InboxFragment;
    Friends FriendsFragment;
    WebService webService;
    int isNotificationPaused;

    BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        webService = new WebService();
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // Checking if user is already logged in or not.
        // Redirect to sign in activity, if not.
        if (AppHandler.getInstance().getDataManager().getString("user", null) == null) {
            Intent intent = new Intent(MainActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        isNotificationPaused = AppHandler.getInstance().getDataManager().getInt("pause_notification", 0);

        groupsFragment = Groups.newInstance();
        InboxFragment = com.softdev.weekimessenger.Layouts.Inbox.newInstance();
        FriendsFragment = Friends.newInstance();
        mViewPager.setCurrentItem(1);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    if (menu != null) {
                        menu.getItem(0).setVisible(true);
                    }
                }
                else {
                    if (menu != null) {
                        menu.getItem(0).setVisible(false);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        Intent intent = new Intent(this, GCMService.class);
        Toast.makeText(this, "Hoi", Toast.LENGTH_SHORT).show();
        startService(intent);
        if (InboxFragment != null) {
                webService.RetrieveMessages();
        } else {
            InboxFragment = Inbox.newInstance();
        }

        PreferenceManager.setDefaultValues(this, R.xml.prefs_chat, false);
        PreferenceManager.setDefaultValues(this, R.xml.prefs_notifications, false);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.KEY_NOTIFICATIONS)) {
                    HandleNotification(intent);
                } else if (intent.getAction().equals(Config.GCM_UPDATED)) {
                    Log.d("MainActivity", "" + intent.getStringExtra("token"));
                    Toast.makeText(context, "OK"+intent.getStringExtra("token"), Toast.LENGTH_SHORT).show();
                } else if (intent.getAction().equals(Config.FRIENDS_UPDATED)) {
                    FriendsFragment.Refresh();
                } else if (intent.getAction().equals(Config.INBOX_UPDATE)) {
                    InboxFragment.Refresh();
                    groupsFragment.Refresh();
                }
            }
        };
        /*
        Uncomment these lines to enable mobile ads.
         */
//        MobileAds.initialize(getApplicationContext(), "ca-app-pub-your-app-id");
//
//        AdView mAdView = (AdView) findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);
    }

    private void HandleNotification(Intent intent) {
        int flag = intent.getIntExtra("flag", -1);
        if (flag == Config.PUSH_TYPE_GROUP) {
            groupsFragment.Refresh();
            InboxFragment.Refresh();
        } else if (flag == Config.PUSH_TYPE_USER) {
            final String sender = intent.getStringExtra("sender");
            if (sender != null) {
                String name = AppHandler.getInstance().getDBHandler().GetUserInfo(sender).getName();
                InboxFragment.Refresh();
                Snackbar snackbar = Snackbar
                        .make(findViewById(R.id.main_content), "You've got a new message from $sender".replace("$sender", name), Snackbar.LENGTH_LONG)
                        .setAction("OPEN", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(getApplicationContext(), Chatbox.class);
                                intent.putExtra("isGroup", "-1");
                                intent.putExtra("group_id", "-1");
                                intent.putExtra("username", sender);
                                startActivity(intent);
                            }
                        });

                snackbar.show();
            }
        }
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(Config.GCM_UPDATED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(Config.KEY_NOTIFICATIONS));
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(Config.FRIENDS_UPDATED));
        NotificationHandler.clearNotifications();

        isNotificationPaused = AppHandler.getInstance().getDataManager().getInt("pause_notification", 0);
    }

    public void Logout() {
        AppHandler.getInstance().getDataManager().setString("user", null);
        AppHandler.getInstance().getDataManager().setString("username", null);
        AppHandler.getInstance().getDataManager().setString("email", null);
        AppHandler.getInstance().getDataManager().setString("name", null);
        AppHandler.getInstance().getDataManager().setString("created_At", null);
        AppHandler.getInstance().getDBHandler().resetDatabase();
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        final MenuItem searchMenu = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenu);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                int currentTab = mViewPager.getCurrentItem();
                switch (currentTab) {
                    case 0:
                        break;
                    case 1:
                        InboxFragment.filter(newText);
                        break;
                    case 2:
                        break;
                }
                return true;
            }
        });
        if (isNotificationPaused == 1) {
            menu.getItem(1).setIcon(R.drawable.ic_notifications_off);
        } else {
            menu.getItem(1).setIcon(R.drawable.ic_notifications_on);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case (R.id.action_add_friend): {
                startActivity(new Intent(getApplicationContext(), AddFriend.class));
                break;
            }
            case (R.id.action_toggle_notification): {
                AppHandler.getInstance().getDataManager().setInt("pause_notification", isNotificationPaused == 1 ? 0 : 1);
                if (isNotificationPaused == 1) {
                    isNotificationPaused = 0;
                    menu.getItem(1).setIcon(R.drawable.ic_notifications_on);
                    Toast.makeText(MainActivity.this, "You'll now receive new notifications.", Toast.LENGTH_SHORT).show();
                } else {
                    isNotificationPaused = 1;
                    menu.getItem(1).setIcon(R.drawable.ic_notifications_off);
                    Toast.makeText(MainActivity.this, "You won't receive notifications anymore.", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case (R.id.action_refresh): {
                int currentTab = mViewPager.getCurrentItem();
                if (currentTab == 0) {
                    if (groupsFragment != null) {
                        groupsFragment.Refresh();
                    } else {
                        groupsFragment = Groups.newInstance();
                        groupsFragment.Refresh();
                    }
                } else if (currentTab == 1) {
                    if (InboxFragment != null) {
                        InboxFragment.Refresh();
                        webService.RetrieveMessages();
                    } else {
                        InboxFragment = Inbox.newInstance();
                        InboxFragment.Refresh();
                        webService.RetrieveMessages();
                    }
                } else if (currentTab == 2) {
                    if (FriendsFragment != null) {
                        FriendsFragment.Refresh();
                    } else {
                        FriendsFragment = Friends.newInstance();
                        FriendsFragment.Refresh();
                    }
                }
                break;
            }
            case (R.id.action_new_group): {
                startActivity(new Intent(getApplicationContext(), CreateGroup.class));
                break;
            }
            case (R.id.action_settings): {
                startActivity(new Intent(getApplicationContext(), Settings.class));
                break;
            }
            case (R.id.action_logout): {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Logout");
                builder.setMessage("Are you sure you want to logout?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        Logout();
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    groupsFragment = Groups.newInstance();
                    return groupsFragment;
                case 1:
                    InboxFragment = com.softdev.weekimessenger.Layouts.Inbox.newInstance();
                    return InboxFragment;
                case 2:
                    FriendsFragment = Friends.newInstance();
                    return FriendsFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Groups";
                case 1:
                    return "Chats";
                case 2:
                    return "Friends";
            }
            return null;
        }


    }
}
