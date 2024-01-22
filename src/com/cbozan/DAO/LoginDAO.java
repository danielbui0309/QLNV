package com.cbozan.DAO;

import java.sql.*;

public class LoginDAO {
    public boolean verifyLogin(String username, String password){
        Connection connection;
        PreparedStatement pst;
        ResultSet rs;
        String login = "select * from admin where username= ? and password=?;";

        try{
            connection = DB.getConnection();
            pst = connection.prepareStatement(login);
            pst.setString(1, username);
            pst.setString(2, password);
            rs= pst.executeQuery();

            if(rs.next())
                return true;
        }catch(SQLException e){
            //TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }
}
