package dao;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 获取数据库连接
 */
public class DBUtil {
    private static final String URL="jdbc:mysql://127.0.0.1:3306/java_image_server?useSSL=true&characterEncoding=UTF8";
    private static String USER="root";
    private static String PASSWORD="";
    //DataSource是Java配置的一个类，帮助我们获取连接，通过这个类可以去操作我们的数据库
    private volatile static DataSource dataSource=null;

    //初始化dataSource
    //用单例模式的懒汉式设计一个数据库连接的工具类，提供一个Connection对象。
    private static DataSource getDataSource(){
        //通过这个方法来创建 DataSource的实例
        if(dataSource==null){
            synchronized (DBUtil.class){
                if(dataSource==null){
                    dataSource=new  MysqlDataSource();
                    ((MysqlDataSource)dataSource).setURL(URL);
                    ((MysqlDataSource)dataSource).setUser(USER);
                    ((MysqlDataSource)dataSource).setPassword(PASSWORD);
                }
            }
        }
        return dataSource;
    }
    //获取具体的连接（这里获取的是jdbc自带的connection）
    public static Connection getConnection() {
        try {
            return getDataSource().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //使用完连接后，需要关闭连接
    public static void close(Connection connection, PreparedStatement statement, ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
