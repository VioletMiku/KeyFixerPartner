package com.keyfixer.partner.Model;

public class Fixer {
    private String strEmail, strPassword, strPhone, strName, avatarUrl, rates, serviceType;
    private boolean isAdmin, isActivated;
    private double jobFee;

    public Fixer() {
    }

    public Fixer(String strEmail , String strPassword , String strPhone , String strName , String avatarUrl , String rates , String serviceType , boolean isAdmin , boolean isActivated , double jobFee) {
        this.strEmail = strEmail;
        this.strPassword = strPassword;
        this.strPhone = strPhone;
        this.strName = strName;
        this.avatarUrl = avatarUrl;
        this.rates = rates;
        this.serviceType = serviceType;
        this.isAdmin = isAdmin;
        this.isActivated = isActivated;
        this.jobFee = jobFee;
    }

    public double getJobFee() {
        return jobFee;
    }

    public void setJobFee(double jobFee) {
        this.jobFee = jobFee;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public boolean isActivated() {
        return isActivated;
    }

    public void setActivated(boolean activated) {
        isActivated = activated;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getRates() {
        return rates;
    }

    public void setRates(String rates) {
        this.rates = rates;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getStrEmail() {
        return strEmail;
    }

    public void setStrEmail(String strEmail) {
        this.strEmail = strEmail;
    }

    public String getStrPassword() {
        return strPassword;
    }

    public void setStrPassword(String strPassword) {
        this.strPassword = strPassword;
    }

    public String getStrPhone() {
        return strPhone;
    }

    public void setStrPhone(String strPhone) {
        this.strPhone = strPhone;
    }

    public String getStrName() {
        return strName;
    }

    public void setStrName(String strName) {
        this.strName = strName;
    }

    @Override
    public String toString() {
        return "Fixer{" +
                "strEmail='" + strEmail + '\'' +
                ", strPassword='" + strPassword + '\'' +
                ", strPhone='" + strPhone + '\'' +
                ", strName='" + strName + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", rates='" + rates + '\'' +
                ", serviceType='" + serviceType + '\'' +
                ", isAdmin=" + isAdmin +
                ", isActivated=" + isActivated +
                '}';
    }
}
