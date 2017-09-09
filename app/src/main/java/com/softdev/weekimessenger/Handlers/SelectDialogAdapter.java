package com.softdev.weekimessenger.Handlers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.softdev.weekimessenger.R;
import com.softdev.weekimessenger.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SelectDialogAdapter extends RecyclerView.Adapter<SelectDialogAdapter.ViewHolder> {
    ArrayList<User> friends;
    ArrayList<User> selectedUsers;
    Context mContext;

    public SelectDialogAdapter(Context context, ArrayList<User> friends, eventListener listener) {
        this.mContext = context;
        this.friends = friends;
        this.Listener = listener;
        this.selectedUsers = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_users_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final User user = friends.get(position);
        holder.txtName.setText(user.getName());
        holder.txtStatus.setText(user.getStatus());
        if(user.getIcon().trim().isEmpty() || user.getIcon().equals("null")) { holder.imageView.setImageResource(R.drawable.default_user); }
        else { Picasso.with(mContext).load(user.getIcon()).error(R.drawable.default_user).placeholder(R.drawable.default_user).into(holder.imageView); }
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) { selectedUsers.add(user); }
                else { selectedUsers.remove(user); }
                Listener.onCheckedChanged(isChecked);
            }
        });
    }

    public ArrayList<String> getSelectedUsers() {
        ArrayList<String> members = new ArrayList<>();
        for (User u: selectedUsers) {
            members.add(u.getUsername());
        }
        return members;
    }

    public int getSelectedItemsCount() {
        return selectedUsers.size();
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public eventListener Listener;
    public interface eventListener {
        void onCheckedChanged(boolean isChecked);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public CircleImageView imageView;
        public TextView txtName, txtStatus;
        public CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            this.imageView = (CircleImageView) itemView.findViewById(R.id.listLayoutIcon);
            this.txtName = (TextView) itemView.findViewById(R.id.listLayoutName);
            this.txtStatus = (TextView) itemView.findViewById(R.id.listLayoutStatus);
            this.checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            this.checkBox.setChecked(!this.checkBox.isChecked());
        }
    }
}
