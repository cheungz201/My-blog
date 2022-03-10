package com.my.blog.website.filter;

import com.my.blog.website.utils.TaleUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * @Program: my-blog
 * @Author: Zhang Zhe
 * @Create: 2022-03-08 17:37
 * @Version: 1.0.0
 * @Description:
 **/
public class XssAndSqlHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private HttpServletRequest request;

    public XssAndSqlHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
        this.request = request;
    }

    @Override
    public String getParameter(String name) {
        String value = request.getParameter(name);
        if (!StringUtils.isEmpty(value)) {
            value = TaleUtils.xssUtil(value);
        }
        return value;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] parameterValues = super.getParameterValues(name);
        if (parameterValues == null) {
            return null;
        }
        for (int i = 0; i < parameterValues.length; i++) {
            String value = parameterValues[i];
			parameterValues[i] = TaleUtils.xssUtil(value);
        }
        return parameterValues;
    }
}
