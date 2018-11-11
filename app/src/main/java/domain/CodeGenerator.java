/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain;

import java.util.Date;
import java.text.SimpleDateFormat;


/**
 *
 * @author Daniel
 */
public class CodeGenerator {
    private String verificationID;
    private Date issueDate;
    private Date issueTime;
    private int verifyCode;
    private String userID;

    private final String datePattern = "yyyy-MM-dd";
    private final String timePattern = "HH:mm:ss";

    public CodeGenerator(String verificationID, Date issueDate, Date issueTime, int verifyCode, String userID) {
        this.verificationID = verificationID;
        this.issueDate = issueDate;
        this.issueTime = issueTime;
        this.verifyCode = verifyCode;
        this.userID = userID;
    }

    public String getVerificationID() {
        return verificationID;
    }

    public void setVerificationID(String verificationID) {
        this.verificationID = verificationID;
    }

    public String getIssueDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
        String sqlDate = dateFormat.format(issueDate);
        return sqlDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;

    }

    public String getIssueTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(timePattern);
        String sqlDate = dateFormat.format(issueTime);
        return sqlDate;
    }

    public void setIssueTime(Date issueTime) {
        this.issueTime = issueTime;
    }

    public int getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(int verifyCode) {
        this.verifyCode = verifyCode;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }



}
