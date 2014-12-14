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

import net.sf.samtools.seekablestream.UserPasswordInput;
import net.sf.samtools.util.ftp.FTPClient;
import net.sf.samtools.util.ftp.FTPReply;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FtpClientUtils {

    static Map<String, String> userCredentials = new HashMap<String, String>();

    /**
     * Connect to an FTP server
     *
     * @param host
     * @param userInfo
     * @param userPasswordInput Dialog with which a user can enter credentials, if login fails
     * @return
     * @throws IOException
     */
    public static synchronized FTPClient connect(String host, String userInfo,
                                                 UserPasswordInput userPasswordInput) throws IOException {

        FTPClient ftp = new FTPClient();
        FTPReply reply = ftp.connect(host);
        if (!reply.isSuccess()) {
            throw new RuntimeException("Could not connect to " + host);
        }

        String user = "anonymous";
        String password = "igv@broadinstitute.org";

        if (userInfo == null) {
            userInfo = userCredentials.get(host);
        }
        if (userInfo != null) {
            String[] tmp = userInfo.split(":");
            user = tmp[0];
            if (tmp.length > 1) {
                password = tmp[1];
            }
        }

        reply = ftp.login(user, password);
        if (!reply.isSuccess()) {
            if (userPasswordInput == null) {
                throw new RuntimeException("Login failure for host: " + host);
            } else {
                userPasswordInput.setHost(host);
                boolean success = false;
                while (!success) {
                    if (userPasswordInput.showDialog()) {
                        user = userPasswordInput.getUser();
                        password = userPasswordInput.getPassword();
                        reply = ftp.login(user, password);
                        success = reply.isSuccess();
                    } else {
                        // canceled
                        break;
                    }

                }
                if (success) {
                    userInfo = user + ":" + password;
                    userCredentials.put(host, userInfo);
                } else {
                    throw new RuntimeException("Login failure for host: "
                            + host);
                }
            }
        }

        reply = ftp.binary();
        if (!(reply.isSuccess())) {
            throw new RuntimeException("Could not set binary mode on host: "
                    + host);
        }

        return ftp;

    }

}