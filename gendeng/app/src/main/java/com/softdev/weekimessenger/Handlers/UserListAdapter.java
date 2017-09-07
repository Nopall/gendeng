package com.softdev.weekimessenger.Handlers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.softdev.weekimessenger.R;
import com.softdev.weekimessenger.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    ArrayList<User> usersList;
    Context mContext;

    public UserListAdapter(Context context, ArrayList<User> users, ClickListener listener)
    {
        mContext = context;
        usersList = users;
        onClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_list_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = usersList.get(position);
        holder.name.setText(user.getName());
        holder.status.setText(user.getStatus());
        if(user.getIcon().trim().isEmpty() || user.getIcon().equals("null")) { holder.icon.setImageResource(R.drawable.default_user); }
        else { Picasso.with(mContext).load(user.getIcon()).error(R.drawable.default_user).placeholder(R.drawable.default_user).into(holder.icon); }
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public ClickListener onClickListener;
    public interface ClickListener {
        void onAddButtonPress(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, status;
        public CircleImageView icon;
        public Button addButton;
        public ViewHolder(View itemView)
        {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.listLayoutName);
            status = (TextView) itemView.findViewById(R.id.listLayoutStatus);
            icon = (CircleImageView) itemView.findViewById(R.id.listLayoutIcon);
            addButton = (Button) itemView.findViewById(R.id.addBtn);

            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onAddButtonPress(v, getAdapterPosition());
                }
            });
        }
    }
}
