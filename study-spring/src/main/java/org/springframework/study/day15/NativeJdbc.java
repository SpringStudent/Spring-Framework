package org.springframework.study.day15;

import java.sql.*;

/**
 * @author 周宁
 * @Date 2019-07-27 10:51
 */
public class NativeJdbc {

    public static void main(String[] args) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/test","root","root");
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select * from tb_account");
            while (rs.next()){
                System.out.println("id:"+rs.getString(1));
                System.out.println("username:"+rs.getString(2));
                System.out.println("realName:"+rs.getString(3));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            if(connection!=null){
                try {
                    connection.close();
                } catch (SQLException e) {

                }
            }
        }
    }

}
