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

package com.xl.main;

/**
 * Created by Xing Li on 2014/11/15.
 * <p/>
 * The Class Global provides the constant information for RED.
 */
public class Global {
    /**
     * The version of RED
     */
    public static final String VERSION = "0.0.3";
    /**
     * RED data version.
     * <p/>
     * The Constant MAX_DATA_VERSION says what is the highest version of the RED file format this parser can understand.
     * If the file to be loaded has a version higher than this then the parser won't attempt to load it.
     */
    public static final int DATA_VERSION = 1;
    /**
     * Our home page of RED.
     */
    public static final String HOME_PAGE = "https://github.com/REDetector/RED";

    public static final String HELP_ONLINE = "http://redetector.github.io";
    /**
     * The issue page.
     */
    public static final String ISSUES_PAGE = HOME_PAGE + "/issues";
    /**
     * The new issue page.
     */
    public static final String NEW_ISSUE_PAGE = ISSUES_PAGE + "/new";

    /**
     * Check online version.
     */
    public static final String VERSION_PAGE = HOME_PAGE + "/Version.txt";

}
