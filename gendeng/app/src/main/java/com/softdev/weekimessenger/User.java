package com.softdev.weekimessenger;
public class User {

    String p_username;
    String p_email;
    String p_name;
    String p_icon;
    String p_status;
    String p_creation;

    public User(String username)
    {
        p_username = username;
    }

    public User(String username, String email, String name, String icon)
    {
        p_username = username;
        p_name = name;
        p_icon = icon;
        p_email = email;
    }

    public User(String username, String email, String name, String icon, String status)
    {
        p_username = username;
        p_name = name;
        p_icon = icon;
        p_email = email;
        p_status = status;
    }

    public String getUsername() { return p_username; }
    public String getEmail() { return p_email; }
    public String getName() { return p_name; }
    public String getIcon() { return p_icon; }
    public String getStatus() { return p_status; }
    public String getCreation() { return p_creation; }

    public void setStatus(String status) { p_status = status; }
    public void setCreation(String created_At) { p_creation = created_At; }
}
