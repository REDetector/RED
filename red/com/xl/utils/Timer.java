/*
 * RED: RNA Editing Detector Copyright (C) <2014> <Xing Li>
 *
 * RED is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * RED is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.xl.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Xing Li on 2014/11/13.
 * <p/>
 * The Class Timer provides the method to get current time.
 */
public class Timer {
    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static SimpleDateFormat df2 = new SimpleDateFormat("yyyyMMddHHmmss");

    public static String getCurrentTime() {
        return df.format(new Date());
    }

    public static String getExportTime() {
        return df2.format(new Date());
    }

    public static String calculateInterval(String beginTime, String endTime) {
        try {

            Date now = df.parse(endTime);
            Date date = df.parse(beginTime);
            long l = now.getTime() - date.getTime();
            long day = l / (24 * 60 * 60 * 1000);
            long hour = (l / (60 * 60 * 1000) - day * 24);
            long minute = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
            long second = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60);
            if (day == 0) {
                return hour + ":" + minute + ":" + second;
            } else {
                return day + " day and " + hour + ":" + minute + ":" + second;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return 0 + "";
        }

    }
}
