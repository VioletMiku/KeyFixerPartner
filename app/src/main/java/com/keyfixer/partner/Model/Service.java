package com.keyfixer.partner.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Service implements Serializable, Parcelable {
    private int serviceImage;
    private String serviceName;
    private double servicePrice;

    public Service() {
    }

    public Service(int serviceImage , String serviceName , double servicePrice) {
        this.serviceImage = serviceImage;
        this.serviceName = serviceName;
        this.servicePrice = servicePrice;
    }

    protected Service(Parcel in) {
        serviceImage = in.readInt();
        serviceName = in.readString();
        servicePrice = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest , int flags) {
        dest.writeInt(serviceImage);
        dest.writeString(serviceName);
        dest.writeDouble(servicePrice);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Service> CREATOR = new Creator<Service>() {
        @Override
        public Service createFromParcel(Parcel in) {
            return new Service(in);
        }

        @Override
        public Service[] newArray(int size) {
            return new Service[size];
        }
    };

    public int getServiceImage() {
        return serviceImage;
    }

    public void setServiceImage(int serviceImage) {
        this.serviceImage = serviceImage;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public double getServicePrice() {
        return servicePrice;
    }

    public void setServicePrice(double servicePrice) {
        this.servicePrice = servicePrice;
    }

    @Override
    public String toString() {
        return "Service{" +
                "serviceImage=" + serviceImage +
                ", serviceName='" + serviceName + '\'' +
                ", servicePrice=" + servicePrice +
                '}';
    }
}
