package com.demo.filter;

import com.demo.common.Const;
import com.demo.pojo.User;
import com.demo.util.CookieUtil;
import com.demo.util.JsonUtil;
import com.demo.util.RedisPoolUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @description: TODO
 * @author: xiaowen
 * @create: 2018-12-21 15:35
 **/
public class SessionExpireFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {

    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest= (HttpServletRequest) servletRequest;
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isNotBlank(loginToken)){
            //判断logintoken是否为空或者""；
            //如果不为空的话，符合条件，继续拿user信息
            String userJsonStr=RedisPoolUtil.get(loginToken);
            User user = JsonUtil.string2Obj(userJsonStr, User.class);
            if(user!=null){
                //如果user不为空，则重置session的时间，即调用expire命令
                RedisPoolUtil.expire(loginToken,Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
            }
        }
        filterChain.doFilter(servletRequest,servletResponse);
    }

    public void destroy() {

    }
}
