package com.softdev.weekimessenger;

import java.util.ArrayList;

public class Group {

    String p_group_id;
    String p_group_name;
    String p_group_icon;
    String p_group_description;
    boolean p_isMember;
    int messageCounts;
    ArrayList<User> p_group_members;

    public Group(String id, String g_name, String g_icon)
    {
        p_group_id = id;
        p_group_name = g_name;
        p_group_icon = g_icon;
    }

    public String getGroupId() { return p_group_id; }
    public String getGroupName() { return p_group_name; }
    public String getGroupIcon() { return p_group_icon; }
    public String getGroupDescription() { return p_group_description; }
    public boolean getStatus() { return p_isMember; }
    public int getMessageCounts() { return messageCounts; }
    public ArrayList<User> getGroupMembers() { return p_group_members; }

    public void setGroupDescription(String description) { p_group_description = description; }
    public void setGroupMembers(ArrayList<User> members) { p_group_members = members; }
    public void setStatus(boolean isMember) { p_isMember = isMember; }
    public void setMessageCounts(int counts) { messageCounts = counts; }
    public void setGroupName(String name) { p_group_name = name; }
    public void setGroupIcon(String icon) { p_group_icon = icon; }
}
