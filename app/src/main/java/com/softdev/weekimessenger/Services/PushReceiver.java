package com.softdev.weekimessenger.Services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.softdev.weekimessenger.Configuration.Config;
import com.softdev.weekimessenger.Configuration.NotificationHandler;
import com.softdev.weekimessenger.Group;
import com.softdev.weekimessenger.Handlers.AppHandler;
import com.softdev.weekimessenger.Handlers.DatabaseHandler;
import com.softdev.weekimessenger.Layouts.Chatbox;
import com.softdev.weekimessenger.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class PushReceiver extends FirebaseMessagingService {
    NotificationHandler nHandler;
    DatabaseHandler dbHandler;
    SharedPreferences sharedPref;
    WebService deliveryNotifier;
    Vibrator vibrator;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHandler = AppHandler.getInstance().getDBHandler();
        deliveryNotifier = new WebService();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map data = remoteMessage.getData();
        String from = remoteMessage.getFrom();
        String title = data.get("title").toString();
        String flag = data.get("flag").toString();
        String strData = data.get("data").toString();
        if (AppHandler.getInstance().getDataManager().getString("user", null) == null) {
            return;
        }
        if (from == AppHandler.getInstance().getDataManager().getString("user", null)) {
            return;
        }
        if (Integer.parseInt(flag) == (Config.PUSH_TYPE_USER)) {
            addMessage(title, strData);
        } else {
            addGroupMessage(title, strData);
        }
    }

    // Add message to database and show notification to the user (if app is running on background).
    void addMessage(String title, String Data) {
        try {
            JSONObject data = new JSONObject(Data);
            JSONObject senderData = data.getJSONObject("sender");
            Message m = new Message(senderData.getString("username"), data.getString("message"), data.getString("creation"));
            m.setMessageType(data.getInt("message_type"));
            m.setMessageID(data.getString("id"));
            m.isReceived(1);
            deliveryNotifier.updateMessageDelivery("-1", m.getMessageID());
            boolean isMuted = dbHandler.isMuted("-1", m.getSender());
            int isPaused = AppHandler.getInstance().getDataManager().getInt("pause_notification", 0);
            if (!dbHandler.isBlocked(m.getSender())) {
                dbHandler.AddMessage(m);
                if (sharedPref.getBoolean("n_vibrateMessage", true))
                    vibrator.vibrate(1000);
                if (!NotificationHandler.isAppIsInBackground(getApplicationContext()) || isPaused == 1 || isMuted) {
                    Intent notificationIntent = new Intent(Config.KEY_NOTIFICATIONS);
                    notificationIntent.putExtra("flag", 2);
                    notificationIntent.putExtra("sender", m.getSender());
                    notificationIntent.putExtra("message", m.getMessage());
                    notificationIntent.putExtra("creation", m.getCreation());
                    LocalBroadcastManager.getInstance(this).sendBroadcast(notificationIntent);

                    if (!isMuted || isPaused != 1) {
                        if (sharedPref.getBoolean("notification_playSound", true)) {
                            NotificationHandler.playMessageSound();
                        }
                    }
                } else {
                    if (sharedPref.getBoolean("n_showMessageNotification", true)) {
                        Intent intent = new Intent(getApplicationContext(), Chatbox.class);
                        intent.putExtra("isGroup", "0");
                        intent.putExtra("username", m.getSender());
                        if (m.getMessageType() == 1) {
                            showNotificationMessage(getApplicationContext(), title, AppHandler.getInstance().getDBHandler().GetUserInfo(m.getSender()).getName() + " sent you an image.", m.getCreation(), intent);
                            NotificationHandler.playMessageSound();
                        } else if (m.getMessageType() == 0) {
                            showNotificationMessage(getApplicationContext(), title, AppHandler.getInstance().getDBHandler().GetUserInfo(m.getSender()).getName() + ": " + m.getMessage(), m.getCreation(), intent);
                            NotificationHandler.playMessageSound();
                        }
                    }
                }
            }
        } catch (JSONException ex) {
            Log.d("PushReceiver", ex.getMessage());
        }
    }

    // Add group message to database and show notification to the user (if app is running on background).
    void addGroupMessage(String title, String Data) {
        try {
            JSONObject data = new JSONObject(Data);
            JSONObject groupData = data.getJSONObject("group");
            JSONObject senderData = data.getJSONObject("sender");
            JSONObject actionData = null;
            if (data.has("extras")) {
                actionData = data.getJSONObject("extras");
            }
            Message m = new Message(groupData.getString("group_id"), senderData.getString("username"), data.getString("message"), data.getString("creation"));
            Group g = new Group(groupData.getString("group_id"), groupData.getString("name"), groupData.getString("icon"));
            m.setMessageType(data.getInt("message_type"));
            m.setMessageID(data.getString("id"));
            m.isReceived(1);
            g.setGroupDescription(groupData.getString("description"));
            g.setStatus(data.getBoolean("isMember"));
            if (actionData != null && actionData.has("action")) {
                if (actionData.getString("action").equals("kick")) {
                    if (actionData.getString("whom").equals(AppHandler.getInstance().getDataManager().getString("username", ""))) {
                        m.setMessage("You are removed");
                        g.setStatus(false);
                        dbHandler.AddGroup(g);
                        dbHandler.AddMessage(m);
                        Intent notificationIntent = new Intent(Config.KEY_NOTIFICATIONS);
                        notificationIntent.putExtra("flag", 1);
                        notificationIntent.putExtra("group_id", g.getGroupId());
                        notificationIntent.putExtra("message_type", m.getMessageType());
                        LocalBroadcastManager.getInstance(this).sendBroadcast(notificationIntent);
                    }
                    else {
                        dbHandler.RemoveGroupMember(g.getGroupId(), actionData.getString("whom"));
                    }
                    deliveryNotifier.updateMessageDelivery(g.getGroupId(), m.getMessageID());
                    return;
                }
            }
            deliveryNotifier.updateMessageDelivery(g.getGroupId(), m.getMessageID());
            boolean isMuted = dbHandler.isMuted(g.getGroupId(), "-1");
            int isPaused = AppHandler.getInstance().getDataManager().getInt("pause_notification", 0);
            if (!dbHandler.isBlocked(m.getSender())) {
                dbHandler.AddGroup(g);
                dbHandler.AddMessage(m);
                if (sharedPref.getBoolean("n_vibrateGroup", true))
                    vibrator.vibrate(1000);
                if (!NotificationHandler.isAppIsInBackground(getApplicationContext()) || isMuted || isPaused == 1) {
                            Intent notificationIntent = new Intent(Config.KEY_NOTIFICATIONS);
                            notificationIntent.putExtra("flag", 1);
                            notificationIntent.putExtra("group_id", g.getGroupId());
                            notificationIntent.putExtra("name", g.getGroupName());
                            notificationIntent.putExtra("sender", m.getSender());
                            notificationIntent.putExtra("message_type", m.getMessageType());
                            notificationIntent.putExtra("message", m.getMessage());
                            notificationIntent.putExtra("creation", m.getCreation());
                            LocalBroadcastManager.getInstance(this).sendBroadcast(notificationIntent);
                            if (!isMuted || isPaused != 1) {
                                if (sharedPref.getBoolean("notification_playSound", true)) {
                                    NotificationHandler.playGroupSound();
                                }
                            }
                } else {
                    if (m.getMessageType() != 2) {
                        if (sharedPref.getBoolean("n_showGroupNotification", true)) {
                            Intent intent = new Intent(getApplicationContext(), Chatbox.class);
                            intent.putExtra("isGroup", "1");
                            intent.putExtra("group_id", g.getGroupId());
                            if (m.getMessageType() == 0) {
                                showNotificationMessage(getApplicationContext(), title, AppHandler.getInstance().getDBHandler().GetUserInfo(m.getSender()).getName() + " in " + g.getGroupName() + ": " + m.getMessage(), m.getCreation(), intent);
                                NotificationHandler.playGroupSound();
                            } else if (m.getMessageType() == 1) {
                                showNotificationMessage(getApplicationContext(), title, AppHandler.getInstance().getDBHandler().GetUserInfo(m.getSender()).getName() + " sent an image in " + g.getGroupName(), m.getCreation(), intent);
                                NotificationHandler.playGroupSound();
                            }
                        }
                    }
                }
            }
        } catch (JSONException ex) {
            Log.d("PushReceiver", ex.getMessage());
        }
    }

    // Show notification of a new message received.
    void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        nHandler = new NotificationHandler(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        nHandler.showNotification(title, message, timeStamp, intent);
    }
}
