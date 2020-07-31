package api;

import dao.ImageDao;
import dao.User;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

/**
 *  浏览器页面用户注册
 **/

public class RegistServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //获取注册用户信息
        req.setCharacterEncoding("UTF-8");
        String name=req.getParameter("name");
        String password=req.getParameter("password");
        //构建User对象，将user对象写进数据库
        ImageDao imageDao=new ImageDao();
        User user=new User();
        user.setName(name);
        user.setPassword(password);
        if(imageDao.register(user)){
            resp.setContentType("text/html;charset=utf-8");
            Cookie cookie=new Cookie("name",URLEncoder.encode(user.getName(),"utf-8"));
            resp.addCookie(cookie);
            resp.sendRedirect("lw-img.html");
            return;
        }
        resp.setContentType("text/html;charset=utf-8");
        resp.getWriter().write("注册失败！");
    }
}

