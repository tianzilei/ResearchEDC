/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2010 Akaza Research
 */
package org.akaza.openclinica.web;

import org.jmesa.web.WebContext;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Adapts Jakarta Servlet HttpServletRequest to JMesa's WebContext interface.
 * This avoids requiring javax.servlet types at compile time for JMesa integration.
 */
public class JakartaWebContext implements WebContext {

    private final HttpServletRequest request;
    private final ServletContext servletContext;
    private Map<String, String> parameterMap;

    public JakartaWebContext(HttpServletRequest request) {
        this.request = request;
        this.servletContext = request.getServletContext();
        this.parameterMap = new HashMap<>();
        for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements(); ) {
            String name = e.nextElement();
            parameterMap.put(name, request.getParameter(name));
        }
    }

    @Override
    public Object getApplicationInitParameter(String name) {
        return servletContext.getInitParameter(name);
    }

    @Override
    public Object getApplicationAttribute(String name) {
        return servletContext.getAttribute(name);
    }

    @Override
    public void setApplicationAttribute(String name, Object value) {
        servletContext.setAttribute(name, value);
    }

    @Override
    public void removeApplicationAttribute(String name) {
        servletContext.removeAttribute(name);
    }

    @Override
    public Object getPageAttribute(String name) {
        return request.getAttribute(name);
    }

    @Override
    public void setPageAttribute(String name, Object value) {
        request.setAttribute(name, value);
    }

    @Override
    public void removePageAttribute(String name) {
        request.removeAttribute(name);
    }

    @Override
    public String getParameter(String name) {
        return request.getParameter(name);
    }

    @Override
    public Map<?, ?> getParameterMap() {
        return parameterMap;
    }

    @Override
    public void setParameterMap(Map<?, ?> map) {
        this.parameterMap = (Map<String, String>) map;
    }

    @Override
    public Object getRequestAttribute(String name) {
        return request.getAttribute(name);
    }

    @Override
    public void setRequestAttribute(String name, Object value) {
        request.setAttribute(name, value);
    }

    @Override
    public void removeRequestAttribute(String name) {
        request.removeAttribute(name);
    }

    @Override
    public Object getSessionAttribute(String name) {
        HttpSession session = request.getSession(false);
        return session != null ? session.getAttribute(name) : null;
    }

    @Override
    public void setSessionAttribute(String name, Object value) {
        request.getSession().setAttribute(name, value);
    }

    @Override
    public void removeSessionAttribute(String name) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(name);
        }
    }

    @Override
    public Writer getWriter() {
        return new StringWriter();
    }

    @Override
    public Locale getLocale() {
        return request.getLocale();
    }

    @Override
    public void setLocale(Locale locale) {
        // not applicable for servlet request
    }

    @Override
    public String getContextPath() {
        return request.getContextPath();
    }

    @Override
    public String getRealPath(String path) {
        return servletContext.getRealPath(path);
    }

    @Override
    public Object getBackingObject() {
        return request;
    }
}
