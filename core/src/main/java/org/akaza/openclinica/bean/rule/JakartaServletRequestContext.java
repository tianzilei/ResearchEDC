/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2010 Akaza Research
 */
package org.akaza.openclinica.bean.rule;

import org.apache.commons.fileupload.RequestContext;

import java.io.IOException;
import java.io.InputStream;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Adapter to wrap a Jakarta Servlet HttpServletRequest as a commons-fileupload RequestContext.
 * This avoids dependency on javax.servlet which is not available in Tomcat 10+ / Jakarta EE runtime.
 */
public class JakartaServletRequestContext implements RequestContext {

    private final HttpServletRequest request;

    public JakartaServletRequestContext(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public String getContentType() {
        return request.getContentType();
    }

    @Override
    public int getContentLength() {
        return request.getContentLength();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return request.getInputStream();
    }

    @Override
    public String getCharacterEncoding() {
        return request.getCharacterEncoding();
    }
}
