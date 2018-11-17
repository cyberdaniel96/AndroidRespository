package com.example.johnn.lodgingservicesystemstudent;

import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import domain.Lease;
import domain.Tenant;
import service.Converter;

public class PopupLease extends PopupWindow {
    private Context context;
    private Lease lease;
    private Tenant tenant;

    private View view;

    private TextView txtStatus;
    private TextView txtIssueDate;
    private TextView txtDueDay;

    private TextView txtUserID;
    private TextView txtMyStatus;
    private TextView txtDeposit;
    private TextView txtRental;
    private TextView txtRole;
    private TextView txtRoomType;
    private TextView txtLeaseStart;
    private TextView txtLeaseEnd;

    private RadioGroup rdOptGroup;
    private RadioButton rdOptBtn;

    private EditText txtReason;

    private Button btnSubmit;
    private String text = "";

    private Converter c = new Converter();

    public PopupLease(Context context, Lease lease, Tenant tenant){
        super(context);
        this.context = context;
        this.lease = lease;
        this.tenant = tenant;
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

        txtUserID = view.findViewById(R.id.txtUserID);
        txtStatus = view.findViewById(R.id.txtStatus);
        txtDeposit = view.findViewById(R.id.txtDeposit);
        txtRental = view.findViewById(R.id.txtRental);
        txtRole = view.findViewById(R.id.txtRole);
        txtRoomType = view.findViewById(R.id.txtRoomType);
        txtLeaseStart = view.findViewById(R.id.txtLeaseStart);
        txtLeaseEnd = view.findViewById(R.id.txtLeaseEnd);
//-----------------------------
        txtStatus.setText(lease.getStatus());
        txtIssueDate.setText(lease.getIssueDate());
        txtDueDay.setText(lease.getDueDay());

        txtUserID.setText(tenant.getUserID());
        txtStatus.setText(tenant.getStatus());
        txtDeposit.setText(tenant.getDeposit() + "");
        txtRental.setText(tenant.getRent() + "");
        txtRole.setText(tenant.getRole());
        txtRoomType.setText(tenant.getRoomType());
        txtLeaseStart.setText(tenant.getLeaseStart());
        txtLeaseEnd.setText(tenant.getLeaseEnd());

        rdOptGroup = (RadioGroup)view.findViewById(R.id.rdOptionalGroup);
        rdOptGroup.setOnCheckedChangeListener(rdCheckChange);

        txtReason = (EditText)view.findViewById(R.id.txtReason);
        txtReason.setEnabled(false);
        txtReason.setText("");
        txtReason.setHintTextColor(Color.TRANSPARENT);

        btnSubmit = (Button)view.findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(onClickListenerSubmit);
    }

    private View.OnClickListener onClickListenerSubmit = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String reason = txtReason.getText().toString();

            if(text.equals("Reject")){
                if(reason.isEmpty()){
                    txtReason.setError("Reason is required");
                    return;
                }else{
                    tenant.setStatus("Rejected");
                    tenant.setReason(txtReason.getText().toString());
                    String serverData = c.convertToHex(new String[]{
                            "004851",
                            "000000000000000000000000",
                            tenant.getTenantID() + 9,
                            "serverLSSserver",
                            ""
                    });

                    String message = c.convertToHex(new String[]{
                            tenant.getStatus(),
                            tenant.getReason(),
                            tenant.getTenantID()
                    });

                    Services.publish(serverData +"$"+ message);
                    pushNotification(false);
                    Toast.makeText(context, "Information Submitted", Toast.LENGTH_LONG).show();
                }

            }

            RadioButton btn = (RadioButton)view.findViewById(R.id.rdAccept);

            if(text.equals("Accept") && btn.isChecked()){
                tenant.setStatus("Active");
                tenant.setReason("non");
                String serverData = c.convertToHex(new String[]{
                        "004851",
                        "000000000000000000000000",
                        tenant.getTenantID() + 9,
                        "serverLSSserver",
                        ""
                });

                String message = c.convertToHex(new String[]{
                        tenant.getStatus(),
                        tenant.getReason(),
                        tenant.getTenantID()
                });

                Services.publish(serverData +"$"+ message);
                pushNotification(true);
                Toast.makeText(context, "Information Submitted", Toast.LENGTH_LONG).show();
            }

        }
    };

    private RadioGroup.OnCheckedChangeListener rdCheckChange = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            rdOptBtn = (RadioButton) view.findViewById(checkedId);
            text = rdOptBtn.getText().toString();

            if(text.equals("Accept")){
                txtReason.setEnabled(false);
                txtReason.setText("");
                txtReason.setHintTextColor(Color.TRANSPARENT);
            }

            if(text.equals("Reject")){
                txtReason.setEnabled(true);
                txtReason.setHintTextColor(Color.parseColor("#3d3d3d"));
            }
        }
    };

    public void showWindows(View parent){
        Drawable dim = new ColorDrawable(parent.getDrawingCacheBackgroundColor());
        dim.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        dim.setAlpha((int) (255 * 1));
        setBackgroundDrawable(dim);
        showAtLocation(parent,Gravity.CENTER,0,0);
    }

    public void pushNotification(boolean optional){
        if(optional == true){
            String serverData = c.convertToHex(new String[]{
                    "004841",
                    "000000000000000000000000",
                    tenant.getUserID() + 1,
                    "serverLSSserver",
                    "",
            });

            String notificationData = c.convertToHex(new String[]{"Lodging Service System",
                    tenant.getUserID() + " accepted the lease",
                    "LEASE ACCEPTED",
                    tenant.getLeaseID()});

            String resourcesData =  "Hello" + "@" +"world";

            String servicePayload = serverData + "$" + notificationData + "$" + resourcesData;
            Services.publish(servicePayload);
        }

        if(optional == false){
            String serverData = c.convertToHex(new String[]{
                    "004841",
                    "000000000000000000000000",
                    tenant.getUserID() + 1,
                    "serverLSSserver",
                    "",
            });

            String notificationData = c.convertToHex(new String[]{"Lodging Service System",
                    tenant.getUserID() + " rejected the lease"+"REASON: " + tenant.getReason(),
                    "LEASE REJECTED",
                    tenant.getLeaseID()});

            String resourcesData =  "Hello" + "@" +"world";

            String servicePayload = serverData + "$" + notificationData + "$" + resourcesData;
            Services.publish(servicePayload);
        }
    }
}
