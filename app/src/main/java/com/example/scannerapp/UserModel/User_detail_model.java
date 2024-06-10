package com.example.scannerapp.UserModel;

public class User_detail_model {
    private String check_in_time , check_out_time , eggs_left , money_collected , phone_no , outlet;

    public User_detail_model(String check_in_time, String check_out_time, String eggs_left, String money_collected, String phone_no, String outlet) {
        this.check_in_time = check_in_time;
        this.check_out_time = check_out_time;
        this.eggs_left = eggs_left;
        this.money_collected = money_collected;
        this.phone_no = phone_no;
        this.outlet = outlet;
    }

    public String getCheck_in_time() {
        return check_in_time;
    }

    public void setCheck_in_time(String check_in_time) {
        this.check_in_time = check_in_time;
    }

    public String getCheck_out_time() {
        return check_out_time;
    }

    public void setCheck_out_time(String check_out_time) {
        this.check_out_time = check_out_time;
    }

    public String getEggs_left() {
        return eggs_left;
    }

    public void setEggs_left(String eggs_left) {
        this.eggs_left = eggs_left;
    }

    public String getMoney_collected() {
        return money_collected;
    }

    public void setMoney_collected(String money_collected) {
        this.money_collected = money_collected;
    }

    public String getPhone_no() {
        return phone_no;
    }

    public void setPhone_no(String phone_no) {
        this.phone_no = phone_no;
    }

    public String getOutlet() {
        return outlet;
    }

    public void setOutlet(String outlet) {
        this.outlet = outlet;
    }
}
