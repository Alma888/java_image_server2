package api;

import dao.ImageDao;
import dao.User;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;


public class LoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //1,获取登录信息。
        req.setCharacterEncoding("UTF-8");
        String name=req.getParameter("name");
        String password=req.getParameter("password");
        //2,构建User对象
        User user=new User();
        user.setName(name);
        user.setPassword(password);
        //查询数据库是否存在用户
        ImageDao imageDao=new ImageDao();
        if(imageDao.login(user)){
            resp.setContentType("text/html;charset=utf-8");
            Cookie cookie=new Cookie("name",URLEncoder.encode(user.getName(),"utf-8"));
            resp.addCookie(cookie);
            resp.sendRedirect("index.html");
            return;
        }

        resp.setContentType("text/html;charset=utf-8");
        resp.getWriter().write("登录失败，请检查用户名和密码是否正确"+name+"--"+password);
    }
}
