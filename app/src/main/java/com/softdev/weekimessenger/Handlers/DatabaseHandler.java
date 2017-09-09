package com.softdev.weekimessenger.Handlers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.softdev.weekimessenger.Group;
import com.softdev.weekimessenger.Inbox;
import com.softdev.weekimessenger.Message;
import com.softdev.weekimessenger.User;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DBVersion = 1;
    private static final String DBName = "weekiDatabase";

    public DatabaseHandler(Context context)
    {
        super(context, DBName, null, DBVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Table Query.
        Log.d("SQLite", "Creating tables ...");
        String usersTable = "CREATE TABLE IF NOT EXISTS users (username VARCHAR(255), email VARCHAR(255), name VARCHAR(50), icon TEXT, status VARCHAR(130), creation DATETIME);";
        String messagesTable = "CREATE TABLE IF NOT EXISTS messages (message_id INTEGER PRIMARY KEY, group_id INTEGER DEFAULT -1, user VARCHAR(255), message_type INTEGER, message TEXT, creation DATETIME, isSeen INTEGER, isError INTEGER, isReceived INTEGER);";
        String groupsTable = "CREATE TABLE IF NOT EXISTS groups (group_id INTEGER PRIMARY KEY, group_name VARCHAR(50), icon TEXT, isMember INTEGER DEFAULT 1, description VARCHAR(130));";
        String groupMembersTable = "CREATE TABLE IF NOT EXISTS groupMembers (group_id INTEGER, username VARCHAR(255));";
        String blockTable = "CREATE TABLE IF NOT EXISTS blockedUsers (username VARCHAR(255), blockDate DATETIME);";
        String muteTable = "CREATE TABLE IF NOT EXISTS mutedUsers (username VARCHAR(255), group_id INTEGER);";

        // Executing Query.
        db.execSQL(usersTable);
        db.execSQL(messagesTable);
        db.execSQL(groupsTable);
        db.execSQL(groupMembersTable);
        db.execSQL(blockTable);
        db.execSQL(muteTable);

        Log.d("SQLite", "Query executed.");
    }

    public void resetDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM users");
        db.execSQL("DELETE FROM messages");
        db.execSQL("DELETE FROM groups");
        db.execSQL("DELETE FROM groupMembers");
        db.execSQL("DELETE FROM blockedUsers");
        db.execSQL("DELETE FROM mutedUsers");
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS messages");
        db.execSQL("DROP TABLE IF EXISTS groups");
        db.execSQL("DROP TABLE IF EXISTS groupMembersTable");
        db.execSQL("DROP TABLE IF EXISTS blockedUsers");
        db.execSQL("DROP TABLE IF EXISTS mutedUsers");
        onCreate(db);
    }

    // Adding a new message on database.
    public long AddMessage(Message message)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user", message.getSender());
        values.put("group_id", message.getGroupID());
        values.put("message_type", message.getMessageType());
        values.put("message", message.getMessage());
        values.put("creation", message.getCreation());
        values.put("isSeen", message.getStatus());
        values.put("isError", message.getError());
        values.put("isReceived", message.isReceived());
        return db.insert("messages", null, values);
    }

    // Retrieving all messages from database accordingly.
    public ArrayList<Inbox> getAllMessages()
    {
        ArrayList<Inbox> inboxArray = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select *, (SELECT COUNT(*) FROM messages m WHERE m.user = mm.user AND m.isSeen = 0 AND m.group_id = -1) as messagesCount, (SELECT COUNT(*) FROM messages m WHERE m.group_id = mm.group_id AND m.isSeen = 0) as gMessagesCount from messages mm where message_id in (select max(message_id) from messages where group_id = -1 group by user) or message_id in (select max(message_id) from messages group by group_id) ORDER BY message_id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(1).equals("-1")) {
                    Inbox inbox = new Inbox(cursor.getString(2), cursor.getString(4), cursor.getString(5));
                    inbox.setType(cursor.getInt(3));
                    inbox.setMessageCounts(cursor.getInt(9));
                    inbox.isReceived(cursor.getInt(8));
                    inboxArray.add(inbox);
                }
                else {
                    Inbox inbox = new Inbox(cursor.getString(1), cursor.getString(2), cursor.getString(4), cursor.getString(5));
                    inbox.setType(cursor.getInt(3));
                    inbox.setMessageCounts(cursor.getInt(10));
                    inbox.isReceived(cursor.getInt(8));
                    inboxArray.add(inbox);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return inboxArray;
    }

    public ArrayList<Message> getAllMessages(String group_id, String username)
    {
        ArrayList<Message> messagesArray = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        if (group_id.equals("-1")) {
            Cursor cursor = db.rawQuery("SELECT * FROM messages WHERE group_id = -1 AND user = '$sender'".replace("$sender", username), null);
            if (cursor.moveToFirst()) {
                do {
                    Message m = new Message(cursor.getString(1), cursor.getString(2), cursor.getString(4), cursor.getString(5));
                    m.setMessageType(cursor.getInt(3));
                    m.isReceived(cursor.getInt(8));
                    m.setMessageID(cursor.getString(0));
                    m.setStatus(cursor.getInt(6));
                    m.setError(cursor.getInt(7));
                    messagesArray.add(m);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        else
        {
            Cursor cursor = db.rawQuery("SELECT * FROM messages WHERE group_id = '$id'".replace("$id", group_id), null);
            if (cursor.moveToFirst()) {
                do {
                    Message m = new Message(cursor.getString(1), cursor.getString(2), cursor.getString(4), cursor.getString(5));
                    m.setMessageType(cursor.getInt(3));
                    m.isReceived(cursor.getInt(8));
                    m.setMessageID(cursor.getString(0));
                    m.setStatus(cursor.getInt(6));
                    m.setError(cursor.getInt(7));
                    messagesArray.add(m);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return messagesArray;
    }

    public void MarkMessagesAsRead(String group_id, String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (group_id != "-1") {
            String query = ("UPDATE messages SET isSeen = 1 WHERE group_id = '$id'".replace("$id", group_id));
            db.execSQL(query);
        }
        else {
            String query = ("UPDATE messages SET isSeen = 1 WHERE group_id = -1 AND user = '$username'".replace("$username", username));
            db.execSQL(query);
        }
        db.close();
    }

    public boolean DeleteAllMessages() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("messages", null, null);
        db.close();
        return true;
    }

    public ArrayList<Group> getAllGroups()
    {
        ArrayList<Group> group = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT *, (SELECT COUNT(*) FROM messages m WHERE m.group_id = g.group_id AND m.isSeen = 0) as messagesCount FROM groups g WHERE g.isMember = 1 ORDER BY messagesCount DESC", null);
        if (cursor.moveToFirst())
        {
            do {
                Cursor uCursor = db.rawQuery("SELECT * FROM groupMembers WHERE group_id = '$gid' LIMIT 4".replace("$gid", cursor.getString(0)), null);
                Group g = new Group(cursor.getString(0), cursor.getString(1), cursor.getString(2));
                ArrayList<User> membersList = new ArrayList<>();
                if (uCursor.moveToFirst()) {
                    do {
                        membersList.add(GetUserInfo(uCursor.getString(0)));
                    } while (uCursor.moveToNext());
                }
                g.setGroupMembers(membersList);
                g.setMessageCounts(cursor.getInt(5));
                g.setGroupDescription(cursor.getString(4));
                g.setStatus(cursor.getInt(3) == 1);
                group.add(g);
                uCursor.close();
            } while (cursor.moveToNext());
        }
        cursor.close();
        return group;
    }

    // Adding a new group on database.
    public void AddGroup(Group group)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = ("INSERT OR REPLACE INTO groups (group_id, group_name, icon, description, isMember) VALUES ($id, \"$name\", '$icon', \"$description\", $isMember)".replace("$id", group.getGroupId())
                .replace("$name", group.getGroupName()).replace("$icon", group.getGroupIcon()).replace("$description", group.getGroupDescription()).replace("$isMember", group.getStatus() ? "1" : "0"));
        db.execSQL(query);
        AddGroupMembers(group);
    }

    public void AddGroupMembers(Group group) {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<User> members = group.getGroupMembers();
        if (members != null) {
            for (User u : members) {
                if (!isGroupMember(group.getGroupId(), u.getUsername())) {
                    String addQuery = ("INSERT INTO groupMembers (group_id, username) VALUES ($id, '$username')".replace("$id", group.getGroupId()).replace("$username", u.getUsername()));
                    db.execSQL(addQuery);
                }
            }
        }
    }

    public void RemoveGroupMember(String group_id, String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("groupMembers", "group_id = '$id' AND username = '$user'".replace("$id", group_id).replace("$user", username), null);
    }

    public boolean isGroupMember(String group_id, String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM groupMembers WHERE group_id = '$id' AND username = '$user'".replace("$id", group_id).replace("$user", username), null);
        int i = cursor.getCount();
        cursor.close();
        return i >= 1;
    }

    public Group getGroupInfo(String group_id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM groups WHERE group_id = '$id'".replace("$id", group_id), null);
        Group g;
        if (cursor.moveToFirst())
        {
            Cursor uCursor = db.rawQuery("SELECT DISTINCT * FROM groupMembers WHERE group_id = '$gid'".replace("$gid", cursor.getString(0)), null);
            g = new Group(cursor.getString(0), cursor.getString(1), cursor.getString(2));
            ArrayList<User> membersList = new ArrayList<>();
            if (uCursor.moveToFirst()) {
                do {
                    membersList.add(GetUserInfo(uCursor.getString(1)));
                } while (uCursor.moveToNext());
            }
            g.setStatus(cursor.getInt(3) == 1);
            g.setGroupMembers(membersList);
            g.setGroupDescription(cursor.getString(4));
            uCursor.close();
        } else {
            g = null;
        }
        cursor.close();
        return g;
    }

    public ArrayList<User> getAllUsers()
    {
        ArrayList<User> usersArray = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users ORDER BY name", null);
        if (cursor.moveToFirst()) {
            do {
                User u = new User(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
                usersArray.add(u);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return usersArray;
    }

    public boolean AddUser(User user)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (isUserExists(user.getUsername())) {
            values.put("email", user.getEmail());
            values.put("name", user.getName());
            values.put("icon", user.getIcon());
            values.put("status", user.getStatus());
            values.put("creation", user.getCreation());
            db.update("users", values, ("username = '$user'").replace("$user", user.getUsername()), null);
        }
        else {
            values.put("username", user.getUsername());
            values.put("email", user.getEmail());
            values.put("name", user.getName());
            values.put("icon", user.getIcon());
            values.put("status", user.getStatus());
            values.put("creation", user.getCreation());
            db.insert("users", null, values);
        }
        db.close();
        return true;
    }

    // Check if there's already a user in database or not.
    public boolean isUserExists(String username)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username = '" + username + "'", null);
        int userCount = cursor.getCount();
        cursor.close();
        return userCount == 1;
    }

    // Delete whole conversation with specific username.
    public int DeleteConversation(String group_id, String username)
    {
        if (group_id.equals("-1")) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete("messages", "user = '$sender' AND group_id = -1".replace("$sender", username), null);
            db.close();
        }
        else
        {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete("messages", "group_id = '$id'".replace("$id", group_id), null);
            db.close();
        }
        return 1;
    }

    // Retrieve user information.
    public User GetUserInfo(String username)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username = '" + username + "'", null);
        User user;
        if(cursor.moveToFirst()) {
            user = new User(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
            user.setCreation(cursor.getString(5));
            cursor.close();
            return user;
        }
        user = new User(username, "", username, "");
        cursor.close();
        return user;
    }

    // Block user using its username.
    public void BlockUser(String username)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("blockDate", DateFormat.getDateTimeInstance().format(new Date()));
        db.insert("blockedUsers", null, values);
        db.close();
    }

    // Check whether the username is currently blocked or not.
    public boolean isBlocked(String username)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM blockedUsers WHERE username = '" + username + "'", null);
        int i = cursor.getCount();
        cursor.close();
        return i == 1;
    }

    // Unblock user using its username.
    public int unBlock(String username)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("blockedUsers", "username = '$user'".replace("$user", username), null);
        db.close();
        return 1;
    }

    // Retrieve a list of blocked users.
    public ArrayList<blockedUsers> RetrieveBlockedUsers()
    {
        ArrayList<blockedUsers> users = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM blockedUsers", null);
        if (cursor.moveToFirst()) {
            do {
                blockedUsers bUsers = new blockedUsers(cursor.getString(0), cursor.getString(1));
                users.add(bUsers);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return users;
    }

    // Class for holding blocked users details.
    public class blockedUsers
    {
        String p_username;
        String p_when;

        public blockedUsers(String username, String when) {
            p_username = username;
            p_when = when;
        }

        public String getUsername() { return p_username; }
        public String getBlockDate() { return p_when; }
    }

    // Mute user using its username.
    public void MuteUser(String username, String group_id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (group_id.equals("-1")) {
            values.put("username", username);
        }
        else { values.put("group_id", Integer.parseInt(group_id)); }
        db.insert("mutedUsers", null, values);
        db.close();
    }

    // Unmute user using its username.
    public int UnmuteUser(String username, String group_id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        if (group_id.equals("-1")) {
            db.delete("mutedUsers", "username = '$id'".replace("$id", username), null);
            db.close();
        }
        else {
            db.delete("mutedUsers", "group_id = '$id'".replace("$id", group_id), null);
            db.close();
        }
        return 1;
    }

    // Check whether the username or group is currently muted or not.
    public boolean isMuted(String group_id, String username)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        int i;
        if (group_id.equals("-1")) {

            Cursor cursor = db.rawQuery("SELECT * FROM mutedUsers WHERE username = '$username'".replace("$username", username), null);
            i = cursor.getCount();
            cursor.close();
        }
        else
        {
            Cursor cursor = db.rawQuery("SELECT * FROM mutedUsers WHERE group_id = '$id'".replace("$id", group_id), null);
            i = cursor.getCount();
            cursor.close();
        }
        return i == 1;
    }
}
