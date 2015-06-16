/*
 * RED: RNA Editing Detector
 *     Copyright (C) <2014>  <Xing Li>
 *
 *     RED is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     RED is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xl.utils;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Administrator on 2015/6/16.
 * <p/>
 * A tool to detect whether the network is available.
 */
public class NetworkDetector {
    public static boolean isNetworkAvailable() {
        return isNetworkAvailable("http://www.github.com");
    }

    public static boolean isNetworkAvailable(String path) {
        URL url;
        try {
            url = new URL(path);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.getInputStream();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
