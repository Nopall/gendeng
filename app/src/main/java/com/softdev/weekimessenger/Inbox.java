package com.softdev.weekimessenger;
public class Inbox {
    String p_group_id = "-1", p_name, p_username = "", icon, p_lastMessage, p_timestamp;
    int messageCounts, messageType, p_isReceived;

    public Inbox (String name, String lastMessage, String timestamp)
    {
        p_name = name;
        p_lastMessage = lastMessage;
        p_timestamp = timestamp;
    }

    public Inbox (String group_id, String name, String lastMessage, String timestamp)
    {
        p_group_id = group_id;
        p_name = name;
        p_lastMessage = lastMessage;
        p_timestamp = timestamp;
    }

    public void setLastMessage(String message) { p_lastMessage = message; }
    public String getLastMessage() { return p_lastMessage; }
    public void setMessageCounts(int counts) { messageCounts = counts; }
    public int getMessageCounts() { return messageCounts; }
    public String getGroupID() { return  p_group_id; }
    public String getName() { return p_name; }
    public String getTimeStamp() { return p_timestamp; }
    public String GetName() { return p_username; }
    public void SetName(String name) { p_username = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public int getType() { return messageType; }
    public void setType(int type) { messageType = type; }
    public boolean isReceived() { return p_isReceived != 0; }
    public void isReceived(int isReceived) { p_isReceived = isReceived; }
}
