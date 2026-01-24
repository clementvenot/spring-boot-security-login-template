package com.template.front.web;

import com.template.front.config.WebConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        var token = request.getSession().getAttribute(WebConfig.SESSION_JWT);
        if (token == null) {
            response.sendRedirect("/login?required");
            return false;
        }
        return true;
    }
}