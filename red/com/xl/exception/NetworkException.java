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

package com.xl.exception;

/**
 * Created by Administrator on 2015/6/12.
 */
public class NetworkException extends Exception {
    private String message;
    private int responseCode;

    public NetworkException(String message) {
        this.message = message.replace("<html>", "");
    }

    public NetworkException(String message, int responseCode) {
        if (message != null) this.message = message.replace("<html>", "");
        this.responseCode = responseCode;
    }

    public NetworkException() {
    }

    @Override
    public String getMessage() {
        if (message == null && responseCode == 0) {
            return "An error occurred while accessing the internet";
        } else if (responseCode == 0) {
            return "An error occurred while accessing the internet<br>" + message;
        } else {
            return "An error occurred while accessing the internet, response code: " + responseCode + "<br>" + message;
        }
    }
}
