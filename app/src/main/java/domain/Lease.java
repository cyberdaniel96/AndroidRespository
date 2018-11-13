/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain;

import java.sql.Date;

/**
 *
 * @author Daniel
 */
public class Lease {
    private String leaseID, dueDay, status, lodgingID;
    private String issueDate;

    public Lease() {
    }

    public Lease(String leaseID, String dueDay, String status, String lodgingID, String issueDate) {
        this.leaseID = leaseID;
        this.dueDay = dueDay;
        this.status = status;
        this.lodgingID = lodgingID;
        this.issueDate = issueDate;
    }

    public String getLeaseID() {
        return leaseID;
    }

    public void setLeaseID(String leaseID) {
        this.leaseID = leaseID;
    }

    public String getDueDay() {
        return dueDay;
    }

    public void setDueDay(String dueDay) {
        this.dueDay = dueDay;
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

    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }
}