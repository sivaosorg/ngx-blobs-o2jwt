package com.phuocnguyen.app.ngxblobso2jwt.model.request;


import com.sivaos.Utility.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.*;

public class MutableHttpServletRequest extends HttpServletRequestWrapper {

    private static final Logger logger = LoggerFactory.getLogger(MutableHttpServletRequest.class);

    private final Map<String, String> headers;

    public MutableHttpServletRequest(HttpServletRequest request) {
        super(request);
        this.headers = new HashMap<>();
    }

    public void addHeader(String name, String value) {
        this.headers.put(name, value);
    }

    public String getHeader(String name) {
        String headerValue = headers.get(name);

        if (StringUtility.isNotEmpty(headerValue)) {
            return headerValue;
        }

        return ((HttpServletRequest) getRequest()).getHeader(name);
    }

    public Enumeration<String> getHeaderNames() {
        Set<String> set = new HashSet<>(headers.keySet());

        Enumeration<String> headers = ((HttpServletRequest) getRequest()).getHeaderNames();
        while (headers.hasMoreElements()) {
            String next = headers.nextElement();
            set.add(next);
        }

        return Collections.enumeration(set);
    }

}
