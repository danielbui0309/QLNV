package com.cbozan.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {
    public static String ERROR_MESSAGE = "";

    private DB() {}
    private Connection conn = null;

    //Mục đích của nó là tạo một singleton giúp ta truy cập đối tượng này ở mọi nơi mà không cần phải tạo một đối tượng DB mới.
    private static class DBHelper{
        private static final DB CONNECTION = new DB();
    }

    public static Connection getConnection() {
        return DBHelper.CONNECTION.connect();
    }

    public static void destroyConnection() {
        DBHelper.CONNECTION.disconnect();
    }

    private Connection connect() {

        try {
            if(conn == null || conn.isClosed()) {
                try{
                    Class.forName("com.mysql.cj.jdbc.Driver");
                }catch (ClassNotFoundException e){
                    System.out.println("Error loading driver: " + e);
                }
                try {
                    String url = "jdbc:mysql://localhost:3306/QLNV";
                    conn = DriverManager.getConnection(url, "root", "DanielMySQL.030902");
                } catch (SQLException e) {
                    System.err.println(e.getMessage());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return conn;
    }

    private void disconnect() {
        if(conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
