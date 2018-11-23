package domain;

import java.sql.Timestamp;

/** @author chang **/

public class Receipt {
    private String receiptID, image, payStatus, receiveStatus, reason, status, tenantID, rentalID;
    private double amount;
    private String dateTime;

    public Receipt() {
    }

    public Receipt(String receiptID, String image, String payStatus, String receiveStatus, String reason, String status, String tenantID, String rentalID, double amount, String dateTime) {
        this.receiptID = receiptID;
        this.image = image;
        this.payStatus = payStatus;
        this.receiveStatus = receiveStatus;
        this.reason = reason;
        this.status = status;
        this.tenantID = tenantID;
        this.rentalID = rentalID;
        this.amount = amount;
        this.dateTime = dateTime;
    }

    public String getReceiptID() {
        return receiptID;
    }

    public void setReceiptID(String receiptID) {
        this.receiptID = receiptID;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(String payStatus) {
        this.payStatus = payStatus;
    }

    public String getReceiveStatus() {
        return receiveStatus;
    }

    public void setReceiveStatus(String receiveStatus) {
        this.receiveStatus = receiveStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTenantID() {
        return tenantID;
    }

    public void setTenantID(String tenantID) {
        this.tenantID = tenantID;
    }

    public String getRentalID() {
        return rentalID;
    }

    public void setRentalID(String rentalID) {
        this.rentalID = rentalID;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }



}
