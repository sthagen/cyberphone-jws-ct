/*
 *  Copyright 2006-2018 WebPKI.org (http://webpki.org).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.webpki.webapps.jws_jcs;

import java.io.IOException;

import java.util.logging.Logger;

import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.webpki.json.JSONObjectReader;
import org.webpki.json.JSONObjectWriter;
import org.webpki.json.JSONOutputFormats;
import org.webpki.json.JSONParser;

import org.webpki.util.Base64URL;

import org.webpki.webutil.ServletUtil;

public class RequestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    static Logger logger = Logger.getLogger(RequestServlet.class.getName());

    static final String JWS_ARGUMENT = "JWS";

    static void error(HttpServletResponse response, String error_message)
            throws IOException, ServletException {
        HTML.errorPage(response, error_message);
    }

    void verifySignature(HttpServletRequest request,
            HttpServletResponse response, byte[] signed_json)
            throws IOException, ServletException {
        logger.info("JSON Signature Verification Entered");
        ReadSignature doc = new ReadSignature();
        JSONObjectReader parsed_json = JSONParser.parse(signed_json);
        doc.recurseObject(parsed_json);
        HTML.printResultPage(
                response,
                "<table>"
        + "<tr><td align=\"center\" style=\"font-weight:bolder;font-size:10pt;font-family:arial,verdana\">Successful Verification!<br>&nbsp;</td></tr>"
        + "<tr><td align=\"left\">"
        + HTML.newLines2HTML(doc.getResult())
        + "</td></tr>"
        + "<tr><td align=\"left\">Received Message:</td></tr>"
        + "<tr><td align=\"left\">"
        + HTML.fancyBox(
                "verify",
                new String(
                        new JSONObjectWriter(parsed_json)
                                .serializeToBytes(JSONOutputFormats.PRETTY_HTML),
                        "UTF-8")) + "</td></tr>" + "</table>");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        byte[] data = null;
        if (request.getContentType().startsWith(
                "application/x-www-form-urlencoded")) {
            data = Base64URL.decode(request.getParameter(JWS_ARGUMENT));
        } else {
            if (!request.getContentType().startsWith("application/json")) {
                error(response, "Request didn't have the proper mime-type: "
                        + request.getContentType());
                return;
            }
            data = ServletUtil.getData(request);
        }
        try {
            verifySignature(request, response, data);
        } catch (Exception e) {
            HTML.errorPage(response, e.getMessage());
            return;
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String json = request.getParameter(JWS_ARGUMENT);
        if (json == null) {
            error(response, "Request didn't contain a \"" + JWS_ARGUMENT
                    + "\" argment");
            return;
        }
        try {
            verifySignature(request, response, Base64URL.decode(json));
        } catch (IOException e) {
            HTML.errorPage(response, e.getMessage());
            return;
        }
    }
}
