package com.softdev.weekimessenger.Handlers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.softdev.weekimessenger.R;
import com.softdev.weekimessenger.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMemberListAdapter extends RecyclerView.Adapter<GroupMemberListAdapter.ViewHolder> {

    Context mContext;
    ArrayList<User> members;
    boolean showRemoveButton = true;
    public GroupMemberListAdapter(Context context, ArrayList<User> members, ClickListener onClickListener) {
        this.mContext = context;
        this.members = members;
        this.onClickListener = onClickListener;
    }

    @Override
    public GroupMemberListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_users_layout_2, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(GroupMemberListAdapter.ViewHolder holder, int position) {
        User user = members.get(position);
        holder.txtName.setText(user.getName());
        holder.txtStatus.setText(user.getStatus());
        if(user.getIcon().trim().isEmpty() || user.getIcon().equals("null")) { holder.imageView.setImageResource(R.drawable.default_user); }
        else { Picasso.with(mContext).load(user.getIcon()).error(R.drawable.default_user).placeholder(R.drawable.default_user).into(holder.imageView); }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }
    public void showRemoveButton(boolean showRemoveButton) {
        this.showRemoveButton = showRemoveButton;
    }
    public ClickListener onClickListener;
    public interface ClickListener {
        void onRemoveButtonClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView imageView;
        public TextView txtName, txtStatus;
        public ImageButton removeButton;

        public ViewHolder(View itemView) {
            super(itemView);
            this.imageView = (CircleImageView) itemView.findViewById(R.id.listLayoutIcon);
            this.txtName = (TextView) itemView.findViewById(R.id.listLayoutName);
            this.txtStatus = (TextView) itemView.findViewById(R.id.listLayoutStatus);
            this.removeButton = (ImageButton) itemView.findViewById(R.id.removeBtn);
            this.removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onRemoveButtonClick(v, getAdapterPosition());
                }
            });
            this.removeButton.setVisibility(showRemoveButton ? View.VISIBLE : View.GONE);
        }
    }
}
