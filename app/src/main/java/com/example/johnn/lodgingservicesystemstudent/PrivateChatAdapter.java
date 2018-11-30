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

import domain.Message;
import domain.PrivateChat;

public class PrivateChatAdapter extends RecyclerView.Adapter{
    private final int VIEW_TYPE_MESSAGE_SENT = 1;
    private final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private Context context;
    private List<Message> messageList;

    private String id;

    public PrivateChatAdapter(Context context, List<Message> messageList){
        this.context = context;
        this.messageList = messageList;
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder{
         TextView name, content, time;
         ImageView picture;

         ReceivedMessageHolder(View itemView){
             super(itemView);

             name = (TextView)itemView.findViewById(R.id.text_message_name);
             content = (TextView)itemView.findViewById(R.id.text_message_body);
             time = (TextView)itemView.findViewById(R.id.text_message_time);
             picture = (ImageView)itemView.findViewById(R.id.image_message_profile);
         }

         void bind(Message message){
             Message msg = (PrivateChat)message;

             name.setText(((PrivateChat) msg).getSenderID());
             content.setText(msg.getContent());
             time.setText(msg.getSentTime());
             String imageAddress = "http://"+Home.ip+"/img/User/"+name.getText().toString()+".jpg";

             Glide.with(itemView.getContext())
                     .load(imageAddress)
                     .transform(new CircleTransform(itemView.getContext()))
                     .override(50,50)
                     .into(picture);
         }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
         TextView content, time;

         SentMessageHolder(View itemView){
            super(itemView);

            content = (TextView)itemView.findViewById(R.id.text_message_body);
            time = (TextView)itemView.findViewById(R.id.text_message_time);
        }

        void bind(Message message) {
            Message msg = (PrivateChat)message;
            content.setText(msg.getContent());

            // Format the stored timestamp into a readable String using method.
            time.setText(msg.getSentTime());
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.private_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.private_message_receive, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = (PrivateChat) messageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = (PrivateChat) messageList.get(position);
        //System.out.println(id);
       if(id.equals(((PrivateChat) message).getSenderID())) {
           //System.out.println("Sender SET");
           return VIEW_TYPE_MESSAGE_SENT;
       }else{
           //System.out.println("Receiver SET");
           return VIEW_TYPE_MESSAGE_RECEIVED;
       }
    }

    public void setID(String id){
        String temp = id.substring(0,id.length()-1);
        this.id = temp;
        System.out.println(this.id);
    }

}
