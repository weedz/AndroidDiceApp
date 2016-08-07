package com.weedz.dice;

/**
 * Created by WeeDz on 2016-08-07.
 */
public class Log {
    public static void d(String str, String tag) {
        android.util.Log.d(tag, str);
    }
    public static void d(String str) {
        android.util.Log.d("Dice.Log", str);
    }

    public static void print(Object str) {
        System.out.println(str);
    }

}
