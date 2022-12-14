package com.hippo.reggietakeout.interceptor;

import com.alibaba.fastjson.JSON;
import com.hippo.reggietakeout.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class LoginCheckInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("拦截到资源 {}",request.getRequestURI());
        /**
         * 判断是否已登录(session的employee属性是否为空)
         * 否，将处理结果写入response，返回false
         */
        if(request.getSession().getAttribute("employee") == null){
            log.info("用户未登录");
            response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
            return false;
        }
        return true;
    }
}
