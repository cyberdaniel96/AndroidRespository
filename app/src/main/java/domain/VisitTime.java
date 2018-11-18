package domain;

import java.io.Serializable;

public class VisitTime implements Serializable {
    private String TimeID, VisitDateTime, Status, UserID, LodgingID;
    private int AppointmentQty;

    public VisitTime() {
    }

    public VisitTime(String timeID, String visitDateTime, String status, String userID, String lodgingID, int appointmentQty) {
        TimeID = timeID;
        VisitDateTime = visitDateTime;
        Status = status;
        UserID = userID;
        LodgingID = lodgingID;
        AppointmentQty = appointmentQty;
    }

    public String getTimeID() {
        return TimeID;
    }

    public void setTimeID(String timeID) {
        TimeID = timeID;
    }

    public String getVisitDateTime() {
        return VisitDateTime;
    }

    public void setVisitDateTime(String visitDateTime) {
        VisitDateTime = visitDateTime;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getLodgingID() {
        return LodgingID;
    }

    public void setLodgingID(String lodgingID) {
        LodgingID = lodgingID;
    }

    public int getAppointmentQty() {
        return AppointmentQty;
    }

    public void setAppointmentQty(int appointmentQty) {
        AppointmentQty = appointmentQty;
    }
}
