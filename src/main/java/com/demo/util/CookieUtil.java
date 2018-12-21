package com.demo.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @description: TODO
 * @author: xiaowen
 * @create: 2018-12-20 14:43
 **/
@Slf4j
public class CookieUtil {

    private final static String COOKIE_DOMIAN = ".xiaowen.com";
    private final static String COOKIE_NAME = "login_token";

    public static String readLoginToken(HttpServletRequest request){
        Cookie[] cks = request.getCookies();
        if(cks!=null){
            for(Cookie ck:cks){
                log.info("read cookieName:{} cookieValue:{}",ck.getName(),ck.getValue());
                if(StringUtils.equals(ck.getName(),COOKIE_NAME)){
                    log.info("return cookieName:{} cookieValue:{}",ck.getName(),ck.getValue());
                    return ck.getValue();
                }
            }
        }
        return null;
    }

    //X:domain=".happymmall.com"
    //a:A.xiaowen.com               cookie:domain=A.xiaowen.com;path="/"
    //b:B.xiaowen.com               cookie:domain=B.xiaowen.com;path="/"
    //c:A.xiaowen.com/test/cc       cookie:domain=A.xiaowen.com;path="/test/cc"
    //d:A.xiaowen.com/test/dd       cookie:domain=A.xiaowen.com;path="/test/aa"
    //e:A.xiaowen.com/test          cookie:domain=A.xiaowen.com;path="/test"

    //a b c d e都能拿到X下的cookie
    //a b 相互之间拿不到彼此的cookie
    //c d 相互之间拿不到彼此的cookie 但是能拿到a的cookie
    //c d 相互之间拿不到彼此的cookie 但是能拿到e的cookie

    public static void writeLoginToken(HttpServletResponse response,String token){
        Cookie ck=new Cookie(COOKIE_NAME,token);
        ck.setDomain(COOKIE_DOMIAN);
        ck.setPath("/");//代表设置在根目录 访问根目录下的页面 都可以获取到这个cookie
        ck.setHttpOnly(true);
        //单位是秒
        //如果这个maxage 不设置的话，cookie就不会写入硬盘，而是写在内存，只在当前页面有效
        ck.setMaxAge(60*60*24*365);//如果是-1 代表永久，这里设置成一年的有效期
        log.info("write cookiename:{},cookievalue:{}",ck.getName(),ck.getValue());
        response.addCookie(ck);
    }

    public static void delLoginToken(HttpServletRequest request,HttpServletResponse response){
        Cookie[] cks = request.getCookies();
        if(cks!=null){
            for(Cookie ck:cks){
                if(StringUtils.equals(ck.getName(),COOKIE_NAME)){
                    ck.setDomain(COOKIE_DOMIAN);
                    ck.setPath("/");
                    ck.setMaxAge(0);//设置成0，代表删除此cookie
                    log.info("del cookiename:{},cookievalue:{}",ck.getName(),ck.getValue());
                    response.addCookie(ck);
                    return;
                }
            }
        }
    }
}
