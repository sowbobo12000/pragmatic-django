package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by Blaze on 10/4/2016.
 */

public class NetworkIdentity {

    @SerializedName("address_city")
    private String addressCity;

    @SerializedName("address_line1")
    private String addressLine1;

    @SerializedName("address_line2")
    private String addressLine2;

    @SerializedName("address_state")
    private String addressState;

    @SerializedName("address_zip")
    private String addressZip; //why. this should be an int

    @SerializedName("dob_day")
    private Integer dobDay;

    @SerializedName("dob_month")
    private Integer dobMonth;

    @SerializedName("dob_year")
    private Integer dobYear;

    @SerializedName("document_provided")
    private Boolean documentProvided;

//    disabled_reason: null

    @SerializedName("due_by")
    private Date dueBy;

    @SerializedName("fields_needed")
    private String[] fieldsNeeded;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("pid_last4_provided")
    private Boolean pidLast4Provided;

    @SerializedName("pid_provided")
    private Boolean pidProvided;

//    @SerializedName("stripe_document_id")
//    private String stripeDocumentId;

//    stripe_pid_id: null
//    updated_at: null

    public String[] getFieldsNeeded() {
        return fieldsNeeded;
    }

    public void setFieldsNeeded(String[] fieldsNeeded) {
        this.fieldsNeeded = fieldsNeeded;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getAddressState() {
        return addressState;
    }

    public void setAddressState(String addressState) {
        this.addressState = addressState;
    }

    public Date getDueBy() {
        return dueBy;
    }

    public void setDueBy(Date dueBy) {
        this.dueBy = dueBy;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddressZip() {
        return addressZip;
    }

    public void setAddressZip(String addressZip) {
        this.addressZip = addressZip;
    }

    public Integer getDobDay() {
        return dobDay;
    }

    public void setDobDay(Integer dobDay) {
        this.dobDay = dobDay;
    }

    public Integer getDobMonth() {
        return dobMonth;
    }

    public void setDobMonth(Integer dobMonth) {
        this.dobMonth = dobMonth;
    }

    public Integer getDobYear() {
        return dobYear;
    }

    public void setDobYear(Integer dobYear) {
        this.dobYear = dobYear;
    }

    public Boolean getDocumentProvided() {
        return documentProvided;
    }

    public void setDocumentProvided(Boolean documentProvided) {
        this.documentProvided = documentProvided;
    }

    public Boolean getPidLast4Provided() {
        return pidLast4Provided;
    }

    public void setPidLast4Provided(Boolean pidLast4Provided) {
        this.pidLast4Provided = pidLast4Provided;
    }

    public Boolean getPidProvided() {
        return pidProvided;
    }

    public void setPidProvided(Boolean pidProvided) {
        this.pidProvided = pidProvided;
    }
}
