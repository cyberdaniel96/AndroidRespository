package com.example.johnn.lodgingservicesystemstudent;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;



public class CodeGenerate extends AppCompatActivity {

    TextView txtNotice;
    TextView txtCode;
    Button myButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_generate);

        txtNotice = (TextView)findViewById(R.id.txtNotice);
        String noticeMessage = "NOTED: Do not simply share to others." +
                "Because this is the code make used of involving in contact purpose.";
        txtNotice.setText(noticeMessage);
        txtCode = (TextView)findViewById(R.id.txtCode);
        myButton = (Button)findViewById(R.id.generateProcced);
        myButton.setText("Generate Code");

    }

    public void codeBtn(View v){
        String btnText = myButton.getText().toString();

        if(btnText.equals("Generate Code")){
            generateCode();
            myButton.setText("Send Code");

        }
        if(btnText.equals("Send Code")){
            Intent intent = new Intent(getApplicationContext(), CodeChoosePeople.class);
            intent.putExtra("myCode", txtCode.getText().toString());
            finish();
            startActivity(intent);
        }
    }

    public void generateCode(){
        int random = (int)(Math.random() * 888888 + 111111);
        txtCode.setText(random+"");
    }
}
