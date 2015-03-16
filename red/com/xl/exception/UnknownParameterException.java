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

import java.util.Arrays;

/**
 * Created by Administrator on 2015/3/13.
 */
public class UnknownParameterException extends RuntimeException {
    private String message;
    private String[] params;

    public UnknownParameterException() {
    }

    public UnknownParameterException(String message) {
        this.message = message;
    }

    public UnknownParameterException(String[] params) {
        this.params = params;
    }

    public UnknownParameterException(String message, String[] params) {
        this(message);
        this.params = params;
    }

    public String getMessage() {
        if (message == null && params == null) {
            return "Unknown parameters exception.";
        } else if (message != null && params == null) {
            return message;
        } else if (message == null) {
            return "Unknown parameters: " + Arrays.asList(params);
        } else {
            return "Unknown parameters: " + Arrays.asList(params) + "\t" + message;
        }
    }
}
