/**
 * jira-client - a simple JIRA REST client
 * Copyright (c) 2013 Bob Carroll (bob.carroll@alum.rit.edu)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

/* Update MMA 2025-05-19: use of httpclient5 */

package net.rcarz.jiraclient;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Basic HTTP authentication credentials.
 */
public class BasicCredentials implements ICredentials {

    private final String username;
    private final String password;

    /**
     * Creates new basic HTTP credentials.
     *
     * @param username
     * @param password
     */
    public BasicCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Sets the Authorization header for the given request.
     *
     * @param req HTTP request to authenticate
     */
    public void authenticate(ClassicHttpRequest req) {
        String creds = username + ":" + password;
        String encodedCredentials = Base64.getEncoder()
                .encodeToString(creds.getBytes(StandardCharsets.UTF_8));
        Header authHeader = new BasicHeader("Authorization", "Basic " + encodedCredentials);
        req.addHeader(authHeader);
    }

    /**
     * Gets the logon name representing these credentials.
     *
     * @return logon name as a string
     */
    public String getLogonName() {
        return username;
    }
    
    public void initialize(RestClient client) throws JiraException {
    }

    public void logout(RestClient client) throws JiraException {
    }
    
}

