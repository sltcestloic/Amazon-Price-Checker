package me.loic.checker.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Loïc on 02/12/2019
 **/
public class Logger {

    public static void log(String message){
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        System.out.println('[' + simpleDateFormat.format(date) + "] " + message);
    }
}
