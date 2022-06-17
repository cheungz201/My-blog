package com.my.blog.website.interceptor;

import com.my.blog.website.utils.BlacklistUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: Zhang Zhe
 * @CreateTime: 2022-06-17 13:57
 */

@Component
public class BlackInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String ip = httpServletRequest.getRemoteAddr();
        if (BlacklistUtil.isBlack(ip)) {
            return false;
        }
         return !BlacklistUtil.exceedThreshold(ip);
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
