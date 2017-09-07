package com.softdev.weekimessenger;

public class Message
{
    String p_message_id;
    String p_sender;
    String p_to;
    String p_group_id = "-1";
    String p_message;
    String p_creation;
    int p_isError = 0;
    int p_isSeen = 0;
    int p_isReceived = 0;
    int p_message_type = 0;
    public Message(String sender, String message, String creation)
    {
        p_sender = sender;
        p_message = message;
        p_creation = creation;
    }

    public Message(String gid, String sender, String message, String creation)
    {
        p_group_id = gid;
        p_sender = sender;
        p_message = message;
        p_creation = creation;
    }
    public String getSender() { return p_sender; }
    public String getTO() { return p_to; }
    public String getCreation() { return p_creation; }
    public String getGroupID() { return p_group_id; }

    public boolean getError() { return p_isError != 0; }
    public void setError(int isError) { p_isError = isError; }

    public String getMessageID() { return p_message_id; }
    public void setMessageID(String id) { p_message_id = id; }

    public boolean getStatus() { return p_isSeen != 0; }
    public void setStatus(int isSeen) { p_isSeen = isSeen; }

    public boolean isReceived() { return p_isReceived != 0; }
    public void isReceived(int isReceived) { p_isReceived = isReceived; }

    public int getMessageType() { return p_message_type; }
    public void setMessageType(int type) { p_message_type = type; }

    public String getMessage() { return p_message; }
    public void setMessage(String message) { p_message = message; }
}
