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
 * Created by IntelliJ IDEA. User: nazaire Date: Jul 13, 2009
 */
public class ParserException extends RuntimeException {
    private long lineNumber = -1;
    private String line;

    public ParserException(String message, long lineNumber) {
        super(message);

        setLineNumber(lineNumber);
    }

    public ParserException(String message, long lineNumber, String line) {
        super(message);

        setLineNumber(lineNumber);

        setLine(line);
    }

    public ParserException(String message, Throwable th, long lineNumber) {
        super(message, th);

        setLineNumber(lineNumber);
    }

    public ParserException(String message, Throwable th, long lineNumber,
                           String line) {
        super(message, th);

        setLineNumber(lineNumber);

        setLine(line);
    }

    public void setLineNumber(long lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void setLine(String line) {
        if (line != null) {
            this.line = line;
            if (line.length() > 500) {
                this.line = line.substring(0, 500);
            }
        }
    }

    public String getMessage() {
        String message = super.getMessage();
        if (message == null)
            message = "";

        if (getCause() != null) {
            if (line != null) {
                return "Failed to parse line " + lineNumber + ":\n"
                        + "Cause\n  " + getCause().getClass().getSimpleName()
                        + ": " + message;

            } else {
                return "Failed to parse line " + lineNumber + ":\n" + "\t"
                        + line + "\n" + "Cause\n  "
                        + getCause().getClass().getSimpleName() + ": "
                        + message;
            }
        }

        if (line != null) {
            return "Failed to parse line " + lineNumber + ":\n" + "\t" + line
                    + "\n" + "\n  " + message;
        }

        return "Failed to parse line " + lineNumber + ":\n" + "\n  " + message;
    }
}
