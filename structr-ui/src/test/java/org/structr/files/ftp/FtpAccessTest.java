/*
 *  Copyright (C) 2013 Axel Morgner
 * 
 *  This file is part of structr <http://structr.org>.
 * 
 *  structr is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 * 
 *  structr is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with structr.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.structr.files.ftp;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.structr.web.common.FtpTest;

/**
 * Tests for FTP service.
 * 
 * @author Axel Morgner
 */
public class FtpAccessTest extends FtpTest {
	
	private static final Logger logger = Logger.getLogger(FtpAccessTest.class.getName());
	
	public void test01LoginFailed() {
		
		try {
			FTPClient ftp = new FTPClient();

			ftp.connect("localhost", 8876);
			logger.log(Level.INFO, "Reply from FTP server:", ftp.getReplyString());
			
			int reply = ftp.getReplyCode();
			assertTrue(FTPReply.isPositiveCompletion(reply));
			
			boolean loginSuccess = ftp.login("jt978hasdl", "lj3498ha");
			logger.log(Level.INFO, "Try to login as jt978hasdl/lj3498ha:", loginSuccess);
			assertFalse(loginSuccess);
			
			ftp.disconnect();
			
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "Error in FTP test", ex);
			fail("Unexpected exception: " + ex.getMessage());
		}
		
	}
	
	public void test02LoginSuccess() {
		
		FTPClient ftp = setupFTPClient();
		disconnect(ftp);
		
	}
	
}
