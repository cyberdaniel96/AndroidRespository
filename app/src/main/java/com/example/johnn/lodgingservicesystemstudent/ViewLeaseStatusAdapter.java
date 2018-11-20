package com.example.johnn.lodgingservicesystemstudent;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.BitSet;
import java.util.List;

import domain.Lodging;


public class ViewLeaseStatusAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<Lodging> list;

    ImageView lodgingImage;
    TextView txtTitle;
    TextView txtAddress;

    private OnClickListener onClickListener;

    public ViewLeaseStatusAdapter(Context context, List<Lodging> list) {
        this.context = context;
        this.list = list;
    }

    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            lodgingImage =  (ImageView)itemView.findViewById(R.id.imgLodging);
            txtTitle = (TextView)itemView.findViewById(R.id.txtTitle);
            txtAddress = (TextView)itemView.findViewById(R.id.txtAddress);

        }

        void bind(Lodging lodging){
            txtTitle.setText(lodging.getTitle());
            txtAddress.setText(lodging.getAddress());
            Glide.with(itemView.getContext())
                    .load(lodging.getImage())
                    .transform(new CircleTransform(itemView.getContext()))
                    .into(lodgingImage);
        }

        @Override
        public void onClick(View v) {
            onClickListener.onClick(v, getAdapterPosition());
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cell_view_lease_status, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Lodging lodging= (Lodging) list.get(position);
        ((ViewHolder) holder).bind(lodging);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setOnClickListener(OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }

    interface OnClickListener{
        void onClick(View v, int position);
    }
}
