package com.keyfixer.partner.Model;

public class DailyStatistical {
    private String date;
    private String customerName;
    private String customerPhone;
    private String finishedTime;
    private String fixedLocation;
    private String serviceFee;
    private double serviceVAT;
    private String serviceName;
    private double fee;

    public DailyStatistical() {
    }

    public DailyStatistical(String date, String customerName, String customerPhone, String finishedTime, String fixedLocation, String serviceFee, double serviceVAT, String serviceName, double fee) {
        this.date = date;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.finishedTime = finishedTime;
        this.fixedLocation = fixedLocation;
        this.serviceFee = serviceFee;
        this.serviceVAT = serviceVAT;
        this.serviceName = serviceName;
        this.fee = fee;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public String getFinishedTime() {
        return finishedTime;
    }

    public void setFinishedTime(String finishedTime) {
        this.finishedTime = finishedTime;
    }

    public String getFixedLocation() {
        return fixedLocation;
    }

    public void setFixedLocation(String fixedLocation) {
        this.fixedLocation = fixedLocation;
    }

    public String getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(String serviceFee) {
        this.serviceFee = serviceFee;
    }

    public double getServiceVAT() {
        return serviceVAT;
    }

    public void setServiceVAT(double serviceVAT) {
        this.serviceVAT = serviceVAT;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        return "DailyStatistical{" +
                "date='" + date + '\'' +
                ", customerName='" + customerName + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", finishedTime='" + finishedTime + '\'' +
                ", fixedLocation='" + fixedLocation + '\'' +
                ", serviceFee=" + serviceFee +
                ", serviceVAT=" + serviceVAT +
                ", serviceName='" + serviceName + '\'' +
                ", fee=" + fee +
                '}';
    }
}
