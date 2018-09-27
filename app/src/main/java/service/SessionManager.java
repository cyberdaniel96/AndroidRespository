package service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.example.johnn.lodgingservicesystemstudent.Login;

import java.util.HashMap;

public class SessionManager {
    SharedPreferences sp;
    Editor editor;
    Context context;
    int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "lalalaPref";
    private static final String IS_LOGIN = "IsLoggedIn";
    public static final String KEY_ID = "id";
    public static final String KEY_EMAIL = "email";


    @SuppressLint("CommitPrefEdits")
    public SessionManager(Context context) {
        this.context = context;
        sp = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = sp.edit();
    }


    public void createLoginSession(String id, String email) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_ID, id);
        editor.putString(KEY_EMAIL, email);
        editor.commit();
    }

    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        user.put(KEY_ID, sp.getString(KEY_ID, null));
        user.put(KEY_EMAIL, sp.getString(KEY_EMAIL, null));
        return user;
    }

    public void logoutUser() {
        editor.clear();
        editor.commit();

        Intent i = new Intent(context, Login.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public boolean isLoggedIn() {
        return sp.getBoolean(IS_LOGIN, false);
    }
}