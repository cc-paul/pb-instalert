package com.pegp.arwithmapping.EmergencyList;

public class emergencyListData {
    Integer id;
    String originLatLong,hospitalLatLong,requestor,dateCreated,details,status,mobileNumber;

    public emergencyListData(Integer id, String originLatLong, String hospitalLatLong, String requestor, String dateCreated, String details, String status,String mobileNumber) {
        this.id = id;
        this.originLatLong = originLatLong;
        this.hospitalLatLong = hospitalLatLong;
        this.requestor = requestor;
        this.dateCreated = dateCreated;
        this.details = details;
        this.status = status;
        this.mobileNumber = mobileNumber;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOriginLatLong() {
        return originLatLong;
    }

    public void setOriginLatLong(String originLatLong) {
        this.originLatLong = originLatLong;
    }

    public String getHospitalLatLong() {
        return hospitalLatLong;
    }

    public void setHospitalLatLong(String hospitalLatLong) {
        this.hospitalLatLong = hospitalLatLong;
    }

    public String getRequestor() {
        return requestor;
    }

    public void setRequestor(String requestor) {
        this.requestor = requestor;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
