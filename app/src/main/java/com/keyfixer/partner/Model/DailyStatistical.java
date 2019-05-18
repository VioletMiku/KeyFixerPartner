package com.keyfixer.partner.Model;

public class DailyStatistical {
    private String date;
    private double fee;

    public DailyStatistical() {
    }

    public DailyStatistical(String date, double fee) {
        this.date = date;
        this.fee = fee;
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

    @Override
    public String toString() {
        return "DailyStatistical{" +
                "date='" + date + '\'' +
                ", fee=" + fee +
                '}';
    }
}
