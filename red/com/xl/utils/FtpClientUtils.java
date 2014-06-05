package com.xl.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sf.samtools.seekablestream.UserPasswordInput;
import net.sf.samtools.util.ftp.FTPClient;
import net.sf.samtools.util.ftp.FTPReply;

public class FtpClientUtils {

	static Map<String, String> userCredentials = new HashMap<String, String>();

	/**
	 * Connect to an FTP server
	 * 
	 * @param host
	 * @param userInfo
	 * @param userPasswordInput
	 *            Dialog with which a user can enter credentials, if login fails
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