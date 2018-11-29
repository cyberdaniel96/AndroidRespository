package com.example.johnn.lodgingservicesystemstudent;


import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import domain.Lodging;

public class LodgingAdapter extends RecyclerView.Adapter<LodgingAdapter.ViewHolder>{

    private List<Lodging> ll;
    private static ClickListener clickListener;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView title;
        public TextView price;
        public TextView type;
        public ImageView picture;
        public TextView status;

        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            title = v.findViewById(R.id.titleTv);
            price = v.findViewById(R.id.priceTv);
            type = v.findViewById(R.id.lodgingTypeTv);
            picture = v.findViewById(R.id.lodgingPicIv);
            status = v.findViewById(R.id.statusTv);
        }

        @Override
        public void onClick(View view) {
            clickListener.onItemClick(getAdapterPosition(), view);
        }
    }

    public LodgingAdapter(List<Lodging> LodgingList) {
        ll = LodgingList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lodging_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(LodgingAdapter.ViewHolder holder, int position) {
        Lodging l = ll.get(position);
        holder.title.setText(l.getTitle());
        holder.price.setText("RM" +String.format("%.2f",l.getPrice()));
        holder.type.setText(l.getLodgingType());
        Glide.with(holder.itemView.getContext())
                .load(l.getImage())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .centerCrop()
                .into(holder.picture);
        if(!l.getStatus().equals("")){
            holder.status.setText(l.getStatus());
            if(!l.getStatus().equals("Active"))
                holder.status.setTextColor(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        return ll.size();
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        LodgingAdapter.clickListener = clickListener;
    }


    public interface ClickListener {
        void onItemClick(int position, View v);
    }
}
