package com.keyfixer.partner.Model;

public class Statistical {
    private String completedHour;
    private String completedMinutes;
    private String completedSeconds;
    private String completedWeekDate;
    private String completedMonthDate;
    private String completedYear;
    private String completedMonth;
    private String serviceName;
    private double vatFee;
    private double totalFee;
    private String locationAtRequestedTime;
    private String fixLocation;
    private String customerName;
    private String customerPhone;
    public String serviceFee;

    public Statistical() {
    }

    public Statistical(String completedHour, String completedMinutes, String completedSeconds, String completedWeekDate, String completedMonthDate, String completedYear, String completedMonth, String serviceName, double vatFee, double totalFee, String locationAtRequestedTime, String fixLocation, String customerName, String customerPhone, String serviceFee) {
        this.completedHour = completedHour;
        this.completedMinutes = completedMinutes;
        this.completedSeconds = completedSeconds;
        this.completedWeekDate = completedWeekDate;
        this.completedMonthDate = completedMonthDate;
        this.completedYear = completedYear;
        this.completedMonth = completedMonth;
        this.serviceName = serviceName;
        this.vatFee = vatFee;
        this.totalFee = totalFee;
        this.locationAtRequestedTime = locationAtRequestedTime;
        this.fixLocation = fixLocation;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.serviceFee = serviceFee;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public double getVatFee() {
        return vatFee;
    }

    public void setVatFee(double vatFee) {
        this.vatFee = vatFee;
    }

    public double getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(double totalFee) {
        this.totalFee = totalFee;
    }

    public String getLocationAtRequestedTime() {
        return locationAtRequestedTime;
    }

    public void setLocationAtRequestedTime(String locationAtRequestedTime) {
        this.locationAtRequestedTime = locationAtRequestedTime;
    }

    public String getFixLocation() {
        return fixLocation;
    }

    public void setFixLocation(String fixLocation) {
        this.fixLocation = fixLocation;
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

    public String getCompletedHour() {
        return completedHour;
    }

    public void setCompletedHour(String completedHour) {
        this.completedHour = completedHour;
    }

    public String getCompletedMinutes() {
        return completedMinutes;
    }

    public void setCompletedMinutes(String completedMinutes) {
        this.completedMinutes = completedMinutes;
    }

    public String getCompletedSeconds() {
        return completedSeconds;
    }

    public void setCompletedSeconds(String completedSeconds) {
        this.completedSeconds = completedSeconds;
    }

    public String getCompletedWeekDate() {
        return completedWeekDate;
    }

    public void setCompletedWeekDate(String completedWeekDate) {
        this.completedWeekDate = completedWeekDate;
    }

    public String getCompletedMonthDate() {
        return completedMonthDate;
    }

    public void setCompletedMonthDate(String completedMonthDate) {
        this.completedMonthDate = completedMonthDate;
    }

    public String getCompletedYear() {
        return completedYear;
    }

    public void setCompletedYear(String completedYear) {
        this.completedYear = completedYear;
    }

    public String getCompletedMonth() {
        return completedMonth;
    }

    public void setCompletedMonth(String completedMonth) {
        this.completedMonth = completedMonth;
    }

    public String getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(String serviceFee) {
        this.serviceFee = serviceFee;
    }

    @Override
    public String toString() {
        return "Statistical{" +
                "completedHour='" + completedHour + '\'' +
                ", completedMinutes='" + completedMinutes + '\'' +
                ", completedSeconds='" + completedSeconds + '\'' +
                ", completedWeekDate='" + completedWeekDate + '\'' +
                ", completedMonthDate='" + completedMonthDate + '\'' +
                ", completedYear='" + completedYear + '\'' +
                ", completedMonth='" + completedMonth + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", vatFee=" + vatFee +
                ", totalFee=" + totalFee +
                ", locationAtRequestedTime='" + locationAtRequestedTime + '\'' +
                ", fixLocation='" + fixLocation + '\'' +
                ", customerName='" + customerName + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", serviceFee='" + serviceFee + '\'' +
                '}';
    }
}
