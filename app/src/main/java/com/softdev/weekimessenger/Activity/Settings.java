package com.softdev.weekimessenger.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.os.Bundle;
import android.preference.PreferenceScreen;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.softdev.weekimessenger.Configuration.Config;
import com.softdev.weekimessenger.Handlers.AppHandler;
import com.softdev.weekimessenger.Layouts.BlockedUsers;
import com.softdev.weekimessenger.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Settings extends PreferenceActivity {

    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepareLayout();
    }

    private void prepareLayout() {
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        View content = root.getChildAt(0);
        LinearLayout toolbarContainer = (LinearLayout) View.inflate(this, R.layout.activity_settings, null);

        root.removeAllViews();
        toolbarContainer.addView(content);
        root.addView(toolbarContainer);

        toolbar = (Toolbar) toolbarContainer.findViewById(R.id.toolbar);
        toolbar.setTitle(getTitle());
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.prefs_main, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return SettingsFragment.class.getName().equals(fragmentName);
    }

    public static class SettingsFragment extends PreferenceFragment {

        String SELF;
        ProgressDialog pDialog;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.SELF = AppHandler.getInstance().getDataManager().getString("username", null);
            this.pDialog = new ProgressDialog(getActivity());
            String link = getArguments().getString("link");
            if ("account".equals(link)) {
                addPreferencesFromResource(R.xml.prefs_account);
                final Preference prefProfileIcon = findPreference("prefs_account_profilePic");
                final EditTextPreference profileName = (EditTextPreference) findPreference("prefs_account_profileName");
                final EditTextPreference profileStatus = (EditTextPreference) findPreference("prefs_account_profileStatus");
                final Preference prefUsername = findPreference("pref_account_txtUsername");
                final Preference prefEmail = findPreference("pref_account_txtEmail");
                final EditTextPreference prefPassword = (EditTextPreference) findPreference("pref_account_txtPassword");
                Preference prefBlockList = findPreference("prefs_account_blocklist");
                profileStatus.setTitle("Status: "+ AppHandler.getInstance().getDataManager().getString("status", "Just another user."));
                profileStatus.setText(AppHandler.getInstance().getDataManager().getString("status", "null"));
                prefUsername.setTitle("Username: " + AppHandler.getInstance().getDataManager().getString("username", "null"));
                prefEmail.setTitle("Email: " + AppHandler.getInstance().getDataManager().getString("email", "null"));
                profileName.setTitle("Profile name: " + AppHandler.getInstance().getDataManager().getString("name", "null"));
                profileName.setText(AppHandler.getInstance().getDataManager().getString("name", "null"));
                prefProfileIcon.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = new Intent(getActivity(), ViewProfile.class);
                        intent.putExtra("isGroup", "0");
                        intent.putExtra("username", AppHandler.getInstance().getDataManager().getString("username", null));
                        intent.putExtra("group_id", "-1");
                        startActivity(intent);
                        return false;
                    }
                });
                profileStatus.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pDialog.setMessage("Updating your profile status...");
                        pDialog.setIndeterminate(true);
                        pDialog.setCanceledOnTouchOutside(false);
                        pDialog.setCancelable(false);
                        pDialog.show();
                        StringRequest request = new StringRequest(Request.Method.PUT, Config.USER_UPDATE + "user-status", new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject obj = new JSONObject(response);
                                    if (!obj.getBoolean("error")) {
                                        if (obj.getString("status").equals(profileStatus.getEditText().getText().toString())) {
                                            Toast.makeText(getActivity(), "Your status has been updated.", Toast.LENGTH_SHORT).show();
                                            profileStatus.setTitle("Status: " + obj.getString("status"));
                                            AppHandler.getInstance().getDataManager().setString("status", obj.getString("status"));
                                            profileStatus.setText(AppHandler.getInstance().getDataManager().getString("status", "null"));
                                            pDialog.dismiss();
                                        } else {
                                            Toast.makeText(getActivity(), "Unable to update your profile status.", Toast.LENGTH_SHORT).show();
                                            pDialog.dismiss();
                                        }
                                    }
                                } catch (JSONException ex) {
                                    pDialog.dismiss();
                                    Log.e("Settings", "error: " + ex.getMessage());
                                    Toast.makeText(getActivity(), "There was an error while updating your status. Please try again later.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                pDialog.dismiss();
                                Log.e("Settings", "error: " + error.getMessage());
                                Toast.makeText(getActivity(), "There was an error while updating your status. Please try again later.", Toast.LENGTH_SHORT).show();
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<>();
                                params.put("username", SELF);
                                params.put("userstatus", profileStatus.getEditText().getText().toString());
                                return params;
                            }

                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                return AppHandler.getInstance().getAuthorization();
                            }
                        };
                        AppHandler.getInstance().addToRequestQueue(request);
                        return false;
                    }
                });

                profileName.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pDialog.setMessage("Updating your profile name...");
                        pDialog.setIndeterminate(true);
                        pDialog.setCanceledOnTouchOutside(false);
                        pDialog.setCancelable(false);
                        pDialog.show();
                        StringRequest request = new StringRequest(Request.Method.PUT, Config.USER_UPDATE + "name", new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject obj = new JSONObject(response);
                                    if (!obj.getBoolean("error")) {
                                        if (obj.getString("name").equals(profileName.getEditText().getText().toString())) {
                                            Toast.makeText(getActivity(), "Your name has been updated.", Toast.LENGTH_SHORT).show();
                                            profileName.setTitle("Profile name: " + obj.getString("name"));
                                            AppHandler.getInstance().getDataManager().setString("name", obj.getString("name"));
                                            profileName.setText(AppHandler.getInstance().getDataManager().getString("name", "null"));
                                            pDialog.dismiss();
                                        }
                                        else {
                                            Toast.makeText(getActivity(), "Unable to update your profile name.", Toast.LENGTH_SHORT).show();
                                            pDialog.dismiss();
                                        }
                                    }
                                } catch (JSONException ex) {
                                    pDialog.dismiss();
                                    Log.e("Settings", "error: " + ex.getMessage());
                                    Toast.makeText(getActivity(), "There was an error while updating your profile name. Please try again later.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                pDialog.dismiss();
                                Log.e("Settings", "error: "+error.getMessage());
                                Toast.makeText(getActivity(), "There was an error while updating your profile name. Please try again later.", Toast.LENGTH_SHORT).show();
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<>();
                                params.put("username", SELF);
                                params.put("name", profileName.getEditText().getText().toString());
                                return params;
                            }

                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                return AppHandler.getInstance().getAuthorization();
                            }
                        };
                        AppHandler.getInstance().addToRequestQueue(request);
                        return false;
                    }
                });

                prefBlockList.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivity(new Intent(getActivity(), BlockedUsers.class));
                        return false;
                    }
                });

                prefPassword.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pDialog.setMessage("Updating your account password...");
                        pDialog.setIndeterminate(true);
                        pDialog.setCanceledOnTouchOutside(false);
                        pDialog.setCancelable(false);
                        pDialog.show();
                        StringRequest request = new StringRequest(Request.Method.PUT, Config.USER_UPDATE + "user-password", new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    Log.d("Settings", "" + response);
                                    JSONObject obj = new JSONObject(response);
                                    if (!obj.getBoolean("error")) {
                                        Toast.makeText(getActivity(), "Your account password is successfully changed.", Toast.LENGTH_SHORT).show();
                                        pDialog.dismiss();
                                    }
                                } catch (JSONException ex) {
                                    pDialog.dismiss();
                                    Log.e("Settings", "error: " + ex.getMessage());
                                    Toast.makeText(getActivity(), "There was an error while changing your account password. Please try again later.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                pDialog.dismiss();
                                Log.e("Settings", "error: " + error.getMessage());
                                Toast.makeText(getActivity(), "There was an error while changing your account password. Please try again later.", Toast.LENGTH_SHORT).show();
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<>();
                                params.put("username", SELF);
                                params.put("userpassword", prefPassword.getEditText().getText().toString());
                                return params;
                            }

                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                return AppHandler.getInstance().getAuthorization();
                            }
                        };
                        AppHandler.getInstance().addToRequestQueue(request);
                        return false;
                    }
                });
            }
            else if ("notifications".equals(link)) {
                addPreferencesFromResource(R.xml.prefs_notifications);
            }
            else if ("chat".equals(link)) {
                addPreferencesFromResource(R.xml.prefs_chat);
                final Preference prefDeleteConversation = findPreference("pref_chat_delConvo");
                prefDeleteConversation.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Delete all conversations");
                        builder.setMessage("Are you sure you want to delete all group and private messages? This action cannot be undone.");
                        builder.setPositiveButton("PROCEED", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                pDialog.setMessage("Delete all messages...");
                                pDialog.setIndeterminate(true);
                                pDialog.setCanceledOnTouchOutside(false);
                                pDialog.setCancelable(false);
                                pDialog.show();
                                if (AppHandler.getInstance().getDBHandler().DeleteAllMessages()) {
                                    pDialog.dismiss();
                                    Toast.makeText(getActivity(), "All messages has been deleted.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(Config.INBOX_UPDATE);
                                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                                }
                            }
                        });
                        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                        return false;
                    }
                });
            }
        }

        @Override
        public Context getContext() {
            return super.getContext();
        }
    }
}
