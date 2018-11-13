package com.example.johnn.lodgingservicesystemstudent;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import domain.Lease;

public class PopupLease extends PopupWindow {
    private Context context;
    private Lease lease;

    private View view;

    private TextView txtStatus;
    private TextView txtIssueDate;
    private TextView txtDueDay;

    public PopupLease(Context context, Lease lease){
        super(context);
        this.context = context;
        this.lease = lease;
        setLayout();
    }

    private void setLayout(){
        setContentView(LayoutInflater.from(context).inflate(R.layout.popup_lease, null));
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        view = getContentView();

        txtStatus = view.findViewById(R.id.txtStatus);
        txtIssueDate = view.findViewById(R.id.txtIssueDate);
        txtDueDay = view.findViewById(R.id.txtDueDay);

        txtStatus.setText(lease.getStatus());
        txtIssueDate.setText(lease.getIssueDate());
        txtDueDay.setText(lease.getDueDay());
    }

    public void showWindows(View parent){
        Drawable dim = new ColorDrawable(parent.getDrawingCacheBackgroundColor());
        dim.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        dim.setAlpha((int) (255 * 1));
        setBackgroundDrawable(dim);
        showAtLocation(parent,Gravity.CENTER,0,0);
    }
}
