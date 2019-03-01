package com.sym;

import java.net.Socket;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Jdbc {
    public static Map<String,Socket> map ;
    public  static Map<String,Socket> getJdbc(){
        //1.加载驱动程序  5.1后可以不明确加载驱动
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //2.连接数据库
        //关于数据库的URL格式规范
        //jdbc:database://host:port/databaseName?p1=v1&p2=v2
        //jdbc:mysql://localhost:3306/memo?user=root&password=980703sym.
        try {
            Connection connection = DriverManager.getConnection
                    ("jdbc:mysql://localhost:3306/chat?user=root&password=980703sym.&useUnicode=true&characterEncoding=UTF-8");
//            Connection connection = DriverManager.getConnection
//                    ("jdbc:mysql://localhost:3306/memo","root","980703sym.");
            connection.setAutoCommit(false);
            connection.commit();
            connection.rollback();
            //3.创建命令

            String sql = "select* from chat_table";//？问号占位
            PreparedStatement statement= connection.prepareStatement(sql);
//4.准备sql，并且执行
            ResultSet resultSet = statement.executeQuery();
            //5.返回结果集、处理结果
            map =new ConcurrentHashMap<>();
            while(resultSet.next()){
                String name = resultSet.getString("name");
                Socket socket = (Socket) resultSet.getObject("socket");
                map.put(name,socket);
            }
            //6.关闭结果集
            resultSet.close();
            //7.关闭命令
            statement.close();
            //8。关闭连接
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            return map;
        }
    }
    public  static void addData(String name,Socket socket){
        map.put(name,socket);
    }
    public static void deleteData(String name){
        map.remove(name);
    }
    public static Map<String,Socket> updataJdbc(){
        //1.加载驱动程序  5.1后可以不明确加载驱动
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //2.连接数据库
        //关于数据库的URL格式规范
        //jdbc:database://host:port/databaseName?p1=v1&p2=v2
        //jdbc:mysql://localhost:3306/memo?user=root&password=980703sym.
        try {
            Connection connection = DriverManager.getConnection
                    ("jdbc:mysql://localhost:3306/chat?user=root&password=980703sym.&useUnicode=true&characterEncoding=UTF-8");
//            Connection connection = DriverManager.getConnection
//                    ("jdbc:mysql://localhost:3306/memo","root","980703sym.");
            connection.setAutoCommit(false);
            connection.commit();
            connection.rollback();
            //3.创建命令

            String sql = "delete from chat_table";//？问号占位
            PreparedStatement statement= connection.prepareStatement(sql);
//4.准备sql，并且执行
            statement.execute();
            sql = "select * from chat_table";//？问号占位
            statement= connection.prepareStatement(sql);
//4.准备sql，并且执行
            ResultSet resultSet = statement.executeQuery();
            //5.返回结果集、处理结果
            map =new ConcurrentHashMap<>();
            while(resultSet.next()){
                String name = resultSet.getString("name");
                Socket socket = (Socket) resultSet.getObject("socket");
                map.put(name,socket);
            }
            //6.关闭结果集
            resultSet.close();
            //7.关闭命令
            statement.close();
            //8。关闭连接
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            return map;
        }
    }
}
