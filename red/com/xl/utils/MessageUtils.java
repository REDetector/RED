package com.xl.utils;

/**
 * Created by Administrator on 2014/6/9.
 */
public class MessageUtils {
    private static final boolean D = true;

    public static void showInfo(Class t, String info) {
        if (D) System.out.println(t.getClass().getName() + ":" + info);
    }

    public static void showError(Class t, String error) {
        if (D) System.err.println(t.getClass().getName() + ":" + error);
    }
}
