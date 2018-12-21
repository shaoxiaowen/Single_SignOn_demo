package com.demo.controller;

import com.demo.pojo.Order;
import com.demo.service.IOrderService;
import com.demo.service.IUserService;
import com.demo.common.Const;
import com.demo.common.ServerResponse;
import com.demo.pojo.User;
import com.demo.util.CookieUtil;
import com.demo.util.JsonUtil;
import com.demo.util.RedisPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @description: TODO
 * @author: xiaowen
 * @create: 2018-11-28 14:54
 **/
@Controller
@RequestMapping("/user/")
public class UserController {
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IOrderService iOrderService;
    /**
     * 用户登录
     *
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody   //通过SpringMVC的Jaskson插件(在dispatcher-servlet.xml中配置) ，将返回值转换为Json给前台
    //获取sessionId
    public ServerResponse<User> login(String username, String password, HttpSession session, HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest) {
        ServerResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()) {
//            session.setAttribute(Const.CURRENT_USER, response.getData());
            //将sessionId存入cookie
            CookieUtil.writeLoginToken(httpServletResponse,session.getId());
            //以sessionId作为键，将用户信息存入redis中
            RedisPoolUtil.setEx(session.getId(),JsonUtil.obj2String(response.getData()),Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
        }
        return response;
    }

    @RequestMapping(value = "getOrder_ByUserId.do")
    @ResponseBody
    public ServerResponse<Order> getOrderInfobyUserId(String userId,HttpServletRequest request){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户的信息");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if (user != null) {
            Order order = iOrderService.selectByUserId(user.getId());
            return ServerResponse.createBySuccess(order);
        }
        return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户的信息");
    }

    @RequestMapping(value = "logout.do")
    @ResponseBody
    public ServerResponse<String> logout(HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse) {
        String loginToken=CookieUtil.readLoginToken(httpServletRequest);
        CookieUtil.delLoginToken(httpServletRequest,httpServletResponse);
        RedisPoolUtil.del(loginToken);

        return ServerResponse.createBySuccess();
    }
}

