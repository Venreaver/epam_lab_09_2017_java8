package com.epam;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateExample {

    public static void main(String[] args) {
        Date theEnd = new Date(Long.MAX_VALUE);

        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.LONG, SimpleDateFormat.LONG);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String text = dateFormat.format(theEnd);

        System.out.println(text);
    }
}
