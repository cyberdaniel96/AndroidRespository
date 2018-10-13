/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain;

import java.io.Serializable;

import javax.crypto.SecretKey;

/**
 *
 * @author Daniel
 */
public class Appointment implements Serializable {
    private String appointmentID;
    private String dateTime;
    private String reason;
    private String state;
    private String priority;
    private String comment;
    private String status;
    private String lodgingID;
    private String tenantID;
    private String ownerID;


    public Appointment(String appointmentID, String dateTime, String reason, String state, String priority, String comment, String status, String lodgingID, String tenantID, String ownerID) {
        this.appointmentID = appointmentID;
        this.dateTime = dateTime;
        this.reason = reason;
        this.state = state;
        this.priority = priority;
        this.comment = comment;
        this.status = status;
        this.lodgingID = lodgingID;
        this.tenantID = tenantID;
        this.ownerID = ownerID;
    }

    public String getAppointmentID() {
        return appointmentID;
    }

    public void setAppointmentID(String appointmentID) {
        this.appointmentID = appointmentID;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getReason() {
        return reason;
    }

    public void setState(String state){
        this.state = state;
    }

    public String getState(){
        return state;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLodgingID() {
        return lodgingID;
    }

    public void setLodgingID(String lodgingID) {
        this.lodgingID = lodgingID;
    }

    public String getTenantID() {
        return tenantID;
    }

    public void setTenantID(String tenantID) {
        this.tenantID = tenantID;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public String toString(){
        String message = "";
        message =
                "Appointment ID : " + appointmentID + "\n" +
                        "Date Time : " + dateTime + "\n" +
                        "Reason    : " + reason + "\n" +
                        "State     : " + state + "\n" +
                        "Priority  : " + priority + "\n" +
                        "Comment   : " + comment + "\n" +
                        "Status    : " + status + "\n" +
                        "Lodging ID: " + lodgingID + "\n" +
                        "Tenant ID : " + tenantID + "\n" +
                        "Owner ID  : " + ownerID + "\n\n";
        return message;
    }
}
