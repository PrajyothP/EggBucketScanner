package com.example.scannerapp.UserModel;

public class User_detail_model {
    private String morning_check_in_time , morning_check_out_time ,evening_check_in_time,evening_check_out_time, eggs_left , money_collected , morning_opening_stock , evening_opening_stock;

    public User_detail_model(String check_in_time, String check_out_time,String evening_check_in_time,String evening_check_out_time, String eggs_left, String money_collected,String morning_opening_stock , String evening_opening_stock) {
        this.morning_check_in_time = check_in_time;
        this.morning_check_out_time = check_out_time;
        this.eggs_left = eggs_left;
        this.money_collected = money_collected;
        this.evening_check_in_time = evening_check_in_time;
        this.evening_check_out_time = evening_check_out_time;
        this.morning_opening_stock = morning_opening_stock;
        this.evening_opening_stock = evening_opening_stock;
    }

}
