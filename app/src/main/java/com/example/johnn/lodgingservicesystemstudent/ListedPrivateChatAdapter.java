package com.example.johnn.lodgingservicesystemstudent;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.concurrent.RecursiveAction;

import domain.Message;
import domain.PrivateChat;

public class ListedPrivateChatAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<Message> list;

    public ListedPrivateChatAdapter(Context context, List<Message> list){
        this.context = context;
        this.list = list;
    }

    private class ViewHolder extends RecyclerView.ViewHolder{
        ImageView img;
        TextView name;

        ViewHolder(View itemView){
            super(itemView);

            img = (ImageView)itemView.findViewById(R.id.profileImg);
            name = (TextView)itemView.findViewById(R.id.nametxt);
        }

        void bind(Message message){
            Message msg = (PrivateChat)message;


            Glide.with(itemView.getContext())
                    .load(msg.getImage())
                    .transform(new CircleTransform(itemView.getContext()))
                    .override(50,50)
                    .into(img);
            name.setText(((PrivateChat) msg).getReceiverID());
        }

    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listed_privatechatrow, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = (PrivateChat) list.get(position);
        ((ViewHolder) holder).bind(message);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
