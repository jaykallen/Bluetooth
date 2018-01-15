package com.procatdt.appbluetooth;

// Created by jay kallen on 7/11/2017:  These helpers perform useful functions used within random places in the program.

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Helper {
    public static String getCurrDateTime(){
        // Returns current time in format 07/17/2017 12:59 PM
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm aaa", Locale.US);
        return simpleDateFormat.format(new Date());
    }


    public static String getCurrDateTimeWest(){
        // Returns current time in format 07/17/2017 12:59:59 PM
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aaa", Locale.US);
        return simpleDateFormat.format(new Date());
    }

}
