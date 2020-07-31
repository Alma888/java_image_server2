package api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dao.Image;
import dao.ImageDao;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class ImageServlet extends HttpServlet {
    private static HashSet<String> set=new HashSet<>();
    static {
        set.add("image/jpeg");
        set.add("image/gif");
        set.add("image/png");
        set.add("image/bmp");
    }
    /**
     * 查找图片
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    // req 对象中包含了请求中的所有信息。
    // resp 对象要生成的结果就放到里面去
    //当前这个 doGet 方法就是要根据请求，生成响应
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //考虑到 ：查看所有图片属性 和 查看指定图片属性
        //若存在 imageId 查看指定图片属性，否则就查看所有图片属性
        //如果 URL 中不存在imageId,那么返回null

        //通过请求 URL 中是否带有imageId参数来判断是查看指定图片或者查看全部图片。
        String imageId=req.getParameter("imageId");
        if(imageId==null||imageId.equals("")){
            //查看所有图片属性
            selectAll(req,resp);
        }else {
            //查看指定图片属性
            selectOne(req,imageId,resp);
        }
    }

    private void selectAll(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        //从数据库查询全部图片记录
        resp.setContentType("application/json; charset=utf-8");
        //1. 先创建一个ImageDao对象，并查找数据库
        ImageDao imageDao=new ImageDao();
        Cookie[] cookies=req.getCookies();
        String name=null;
        for(Cookie cookie:cookies){
            if(cookie.getName().equals("name")){
                name=cookie.getValue();
            }
        }
        if(name==null){
            resp.setContentType("text/html;charset=utf-8");
            resp.getWriter().write("{\"ok\":fasle,\"reason\":登录异常}");
            return;
        }
        //针对http get请求不能传输中文参数问题。http请求时不接受中文参数的
        //这时，就需要发送方将中文参数encode，而接收方将参数decode，这样接收方
        // 就能收到准确的原始字符串了。
        String userName=URLDecoder.decode(name,"UTF-8");
        int userId=imageDao.getUserId(userName);
        List<Image> images=imageDao.selectAll(userId);
        //2. 使用 gson 把查到的结果转换成 json格式的字符串，并写回给resp对象
        Gson gson=new GsonBuilder().create();
        //jsonData就是一个json格式的字符串，就和之前约定的格式一样
        //重点：gson自动完成了大量的格式转换，只要把之前的相关字段都约定成统一的命名，
        // 下面的操作一步到位完成整个转换。
        String jsonData=gson.toJson(images);
        resp.getWriter().write(jsonData);
    }

    private void selectOne(HttpServletRequest req,String imageId, HttpServletResponse resp) throws IOException {
        //从数据库查询指定图片记录
        resp.setContentType("application/json; charset=utf-8");
        //1. 创建一个 ImageDao 对象，查找数据库
        ImageDao imageDao=new ImageDao();
        Image image=imageDao.selectOne(Integer.parseInt(imageId));
        if(image==null){
            resp.getWriter().write("{\"ok\":false,\"reason\":指定图片不存在}");
            return;
        }
        //2. 使用 gson 把查到的数据转成 json 格式，并写回给resp 对象
        Gson gson=new GsonBuilder().create();
        String jsonData=gson.toJson(image);
        resp.getWriter().write(jsonData);
    }

    /**
     * 上传图片
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        // 1. 获取图片的属性信息，并且存入数据库
        //  a) 需要创建一个 factory 对象 和 upload 对象，这是为了获取到图片属性做的准备工作
        //     固定的逻辑
        FileItemFactory factory=new DiskFileItemFactory();
        ServletFileUpload upload=new ServletFileUpload(factory);
        //  b) 通过 upload 对象进一步解析请求（解析HTTP请求中奇怪的 body中的内容）
        //    FileItem 就代表一个上传的文件对象
        //    理论上来说，HTTP 支持一个请求中同时上传多个文件
        List<FileItem> items=null;
        try {
            items=upload.parseRequest(req);
        } catch (FileUploadException e) {
            // 出现异常说明解析出错！
            e.printStackTrace();
            //告诉客户端出现的具体的错误是啥
            resp.setContentType("application/json; charset=utf-8");
            resp.getWriter().write("{\"ok\":false,\"reason\":\"请求解析失败\"}");
            return;
        }
        //  c) 把 FileItem 中的属性提取出来，转换成 Image 对象，才能存入数据库中
        //     当前只考虑一张图片的情况
        FileItem fileItem=items.get(0);
        if(!set.contains(fileItem.getContentType())){
            resp.setContentType("text/html;charset=utf-8");
            resp.getWriter().write("{\"ok\":fasle,\"reason\":图片格式错误}");
            return;
        }
        Image image=new Image();
        image.setImageName(fileItem.getName());
        image.setSize((int)fileItem.getSize());
        // 手动获取一下当前日期，并转成格式化日期，yyyyMMdd—> 20200728
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMdd");
        image.setUploadTime(simpleDateFormat.format(new Date()));
        image.setContentType(fileItem.getContentType());
        //  MD5 暂时先不去计算
        // 这里用到第三方库 codec 自带的一个DigestUtils类去直接计算md5
        image.setMd5(DigestUtils.md5Hex(fileItem.get()));
        Cookie[] cookies=req.getCookies();
        String name=null;
        for(Cookie cookie:cookies){
            if(cookie.getName().equals("name")){
                name=cookie.getValue();
            }
        }
        if(name==null){
            resp.setContentType("text/html;charset=utf-8");
            resp.getWriter().write("{\"ok\":fasle,\"reason\":登录异常}");
            return;
        }
        String userName=URLDecoder.decode(name,"UTF-8");
        ImageDao imageDao=new ImageDao();
        int userId=imageDao.getUserId(userName);
        // 自己创造一个路径来保存图片，
        image.setPath("./image/"+userName+"的"+image.getMd5());
        image.setUserId(userId);
        // 将image存到数据库中
        //查看数据库中是否存在相同md5值的图片
        Image existImage = imageDao.selectByMd5(image.getMd5(),image.getUserId());
        imageDao.insert(image);

        //看看数据库中是否存在相同的 MD5 值的图片，不存在就写入磁盘，存在就只存一份
        // 2. 获取图片的内容信息，并且写入磁盘
        if (existImage==null) {
            File file=new File(image.getPath());
            try {
                fileItem.write(file);
            } catch (Exception e) {
                e.printStackTrace();
                //告诉客户端出现的具体的错误是啥
                resp.setContentType("application/json; charset=utf-8");
                resp.getWriter().write("{\"ok\":false,\"reason\":\"写磁盘失败\"}");
                return;
            }
        }
        // 3. 给客户端返回一个结果数据
        //resp.setContentType("application/json; charset=utf-8");
       // resp.getWriter().write("{\"ok\":true }");
        resp.sendRedirect("index.html");

    }

    /**
     * 删除指定图片
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //获取请求消息中要删除的图片中的ImageId
        resp.setContentType("application/json; charset=utf-8");
        //1. 先获取请求中的 imageId
        String imageId=req.getParameter("imageId");
        if(imageId==null||imageId.equals("")){
            resp.setStatus(200);
            resp.getWriter().write("{ \"ok\": false, \"reason\": \"请求解析失败\"}");
            return;
        }
        //2. 创建 ImageDao 对象，查看到该图片对象对应的相关属性
        ImageDao imageDao=new ImageDao();
        Image image=imageDao.selectOne(Integer.parseInt(imageId));

        //3.根据 imageId 删除数据库中的记录
        imageDao.delete(Integer.parseInt(imageId));
        //  若数据库中不存在相同图片的记录，则成功删除
        String md5=image.getMd5();
        int userId=image.getUserId();
        //  如果数据库中当前图片已经被删除，再去删除磁盘中保存的当前图片文件
        if (imageDao.selectByMd5(md5,userId)==null) {
            //4.将本地磁盘中保存的图片删除。
            File file=new File(image.getPath());
            file.delete();
        }
    }
}
