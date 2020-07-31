package dao;

import common.JavaImageServerException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//imageDao 是 Image对象的管理器，借助这个类完成Image对象的增删改查操作
public class ImageDao {
    /**
     * 注册用户
     * @param user
     * @return
     */
    public boolean register(User user){
        //1，获取数据库连接
        Connection connection=DBUtil.getConnection();
        //构建SQL语句
        String sql="insert into user values(?,?,null)";
        PreparedStatement statement=null;
        try {
            statement=connection.prepareStatement(sql);
            //执行SQL查询
            statement.setString(1,user.getName());
            statement.setString(2,user.getPassword());
            int res=statement.executeUpdate();
            if(res!=1){
                throw new JavaImageServerException("用户注册失败,该用户已存在！");
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JavaImageServerException e) {
            e.printStackTrace();
        } finally {
            //关闭连接
            DBUtil.close(connection,statement,null);
        }
        return false;
    }

    /**
     * 登录用户
     * @param user
     * @return
     */
    public boolean login(User user){
        //1，获取数据库连接
        Connection connection=DBUtil.getConnection();
        //构建SQL语句
        String sql="select * from user where name =?and password=?";
        PreparedStatement statement=null;
        ResultSet set=null;
        try {
            statement=connection.prepareStatement(sql);
            //执行SQL查询
            statement.setString(1,user.getName());
            statement.setString(2,user.getPassword());
            set=statement.executeQuery();
            if(!set.next()) {
                throw new JavaImageServerException("登录失败，该用户不存在！");
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JavaImageServerException e) {
            e.printStackTrace();
        } finally {
            //关闭连接
            DBUtil.close(connection,statement,null);
        }
        return false;
    }
    /**
     * 根据 userName查询 userId
     */
    public int getUserId(String userName){
        //1，获取数据库连接
        Connection connection=DBUtil.getConnection();
        //构建SQL语句
        String sql="select * from user where name =?";
        PreparedStatement statement=null;
        ResultSet set=null;
        try {
            statement=connection.prepareStatement(sql);
            //执行SQL查询
            statement.setString(1,userName);
            set=statement.executeQuery();
            if(set.next()) {
                return set.getInt("userId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            //关闭连接
            DBUtil.close(connection,statement,null);
        }
        return 0;
    }

    /**
     * 把 image对象插入到数据库中
     * @param image
     */
    public void insert(Image image) {
        //1. 获取数据库连接
        Connection connection = DBUtil.getConnection();
        //2. 创建并拼装 SQL 语句
        String sql = "insert into image_table values(null,?,?,?,?,?,?,?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, image.getImageName());
            statement.setInt(2, image.getSize());
            statement.setString(3, image.getUploadTime());
            statement.setString(4, image.getContentType());
            statement.setString(5, image.getPath());
            statement.setString(6, image.getMd5());
            statement.setInt(7,image.getUserId());
            //3. 执行 SQL 语句
            int ret = statement.executeUpdate();
            if (ret != 1) {
                //程序出现问题，抛出一个异常
                throw new JavaImageServerException("图片插入失败");
            }
        } catch (SQLException | JavaImageServerException e) {
            e.printStackTrace();
        } finally {
            //4. 关闭连接 和 statement对象
            DBUtil.close(connection, statement, null);
        }
    }

    /**
     * 查找数据库中的所有图片的信息
     * @return
     */
    public List<Image> selectAll(int userId) {
        List<Image> images = new ArrayList<>();
        //1.获取数据库连接
        Connection connection = DBUtil.getConnection();
        //2.构造SQL语句
        String sql = "select * from image_table where userId=?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            //3.执行SQL语句
            statement = connection.prepareStatement(sql);
            statement.setInt(1,userId);
            resultSet = statement.executeQuery();
            //4.处理结果集
            while (resultSet.next()) {
                Image image = new Image();
                image.setImageId(resultSet.getInt("imageId"));
                image.setImageName(resultSet.getString("imageName"));
                image.setSize(resultSet.getInt("size"));
                image.setUploadTime(resultSet.getString("uploadTime"));
                image.setContentType(resultSet.getString("contentType"));
                image.setPath(resultSet.getString("path"));
                image.setMd5(resultSet.getString("md5"));
                image.setUserId(resultSet.getInt("userId"));
                images.add(image);
            }
            return images;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            //5.关闭连接
            DBUtil.close(connection, statement, resultSet);
        }
        return null;
    }

    /**
     * 根据 imageId 查找指定的图片信息
     * @param imageId
     * @return
     */
    public Image selectOne(int imageId) {
        //1.获取数据库连接
        Connection connection = DBUtil.getConnection();
        //2.构造 SQL 语句
        String sql = "select * from image_table where imageId=?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            //3.执行 SQL 语句
            statement = connection.prepareStatement(sql);
            statement.setInt(1, imageId);
            resultSet = statement.executeQuery();
            //4.处理结果集
            if (resultSet.next()) {
                Image image = new Image();
                image.setImageId(resultSet.getInt("imageId"));
                image.setImageName(resultSet.getString("imageName"));
                image.setSize(resultSet.getInt("size"));
                image.setUploadTime(resultSet.getString("uploadTime"));
                image.setContentType(resultSet.getString("contentType"));
                image.setPath(resultSet.getString("path"));
                image.setMd5(resultSet.getString("md5"));
                image.setUserId(resultSet.getInt("userId"));
                return image;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            //5.关闭连接
            DBUtil.close(connection, statement, resultSet);
        }
        return null;
    }

    /**
     * 根据 imageId 删除指定imageId图片属性
     *
     * @param imageId
     */
    public void delete(int imageId) {
        //1.获取数据库连接
        Connection connection = DBUtil.getConnection();
        //2.创建 SQL 语句
        String sql = "delete from image_table where imageId=?";
        PreparedStatement statement = null;
        try {
            //3.执行 SQL 语句
            statement = connection.prepareStatement(sql);
            statement.setInt(1, imageId);
            int ret = statement.executeUpdate();
            if (ret != 1) {
                throw new JavaImageServerException("图片删除失败");
            }
        } catch (SQLException | JavaImageServerException e) {
            e.printStackTrace();
        } finally {
            //4.关闭连接
            DBUtil.close(connection, statement, null);
        }
    }

    //通过md5来查找一个图片
    public Image selectByMd5(String md5,int userId){
        //1.获取数据库连接
        Connection connection = DBUtil.getConnection();
        //2.构造 SQL 语句
        String sql = "select * from image_table where md5=? and userId=?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            //3.执行 SQL 语句
            statement = connection.prepareStatement(sql);
            statement.setString(1, md5);
            statement.setInt(2,userId);
            resultSet = statement.executeQuery();
            //4.处理结果集
            if (resultSet.next()) {
                Image image = new Image();
                image.setImageId(resultSet.getInt("imageId"));
                image.setImageName(resultSet.getString("imageName"));
                image.setSize(resultSet.getInt("size"));
                image.setUploadTime(resultSet.getString("uploadTime"));
                image.setContentType(resultSet.getString("contentType"));
                image.setPath(resultSet.getString("path"));
                image.setMd5(resultSet.getString("md5"));
                return image;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            //5.关闭连接
            DBUtil.close(connection, statement, resultSet);
        }
        return null;
    }
    public static void main(String[] args) {
        // 用于进行简单的测试
        // 1. 测试插入数据
        /*
        Image image = new Image();
        image.setImageName("1.png");
        image.setSize(100);
        image.setUploadTime("20200216");
        image.setContentType("image/png");
        image.setPath("./data/1.png");
        image.setMd5("11223344");
        ImageDao imageDao = new ImageDao();
        imageDao.insert(image);

        // 2. 测试查找所有图片信息
        ImageDao imageDao = new ImageDao();
        List<Image> images = imageDao.selectAll();
        System.out.println(images);
        */

        // 3. 测试查找指定图片信息
//        ImageDao imageDao = new ImageDao();
//        Image image = imageDao.selectOne(1);
//        System.out.println(image);

        // 4. 测试删除图片
        //ImageDao imageDao = new ImageDao();
        //imageDao.delete(1);
    }
}