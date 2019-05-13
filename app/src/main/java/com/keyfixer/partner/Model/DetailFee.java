package com.keyfixer.partner.Model;

import java.util.List;

public class DetailFee {
    private String startAddress;
    private String endAddress;
    private String distance;
    private double totalFee;
    private List<Service> services;

    public DetailFee() {
    }

    public DetailFee(String startAddress , String endString , String distance , double totalFee , List<Service> services) {
        this.startAddress = startAddress;
        this.endAddress = endString;
        this.distance = distance;
        this.totalFee = totalFee;
        this.services = services;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endString) {
        this.endAddress = endString;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public double getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(double totalFee) {
        this.totalFee = totalFee;
    }

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    @Override
    public String toString() {
        return "DetailFee{" +
                "startAddress='" + startAddress + '\'' +
                ", endString='" + endAddress + '\'' +
                ", distance='" + distance + '\'' +
                ", totalFee=" + totalFee +
                ", services=" + services +
                '}';
    }
}
