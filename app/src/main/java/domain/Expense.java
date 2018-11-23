package domain;

import java.sql.Date;

/** @author chang **/

public class Expense {
    private String expenseID, category, description, status, rentalID;
    private Date payDate;
    private double amount;

    public Expense() {
    }

    public Expense(String expenseID, String category, String description, String status, String rentalID, Date payDate, double amount) {
        this.expenseID = expenseID;
        this.category = category;
        this.description = description;
        this.status = status;
        this.rentalID = rentalID;
        this.payDate = payDate;
        this.amount = amount;
    }

    public String getExpenseID() {
        return expenseID;
    }

    public void setExpenseID(String expenseID) {
        this.expenseID = expenseID;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRentalID() {
        return rentalID;
    }

    public void setRentalID(String rentalID) {
        this.rentalID = rentalID;
    }

    public Date getPayDate() {
        return payDate;
    }

    public void setPayDate(Date payDate) {
        this.payDate = payDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }


}
