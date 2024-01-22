package com.cbozan.DAO;

import com.cbozan.Entity.Employer;
import com.cbozan.Exception.EntityException;

import javax.swing.*;
import java.sql.*;
import java.util.*;

public class EmployerDAO {
    //Gán cho thằng Employer đó một Integer(id của nó). Lúc sau mình có thể dễ dàng truy cập và lấy Employer mà mình đã truy vấn sql trước đó mà không cần phải thực hiện truy vấn thêm lần nưữa
    private final HashMap<Integer, Employer> cache = new HashMap<>();
    private boolean usingCache = true;

    private EmployerDAO() {list();}

    public Employer findById(int id) {
        if(!usingCache) {
            list(); //Nếu cache đang không sử dụng thì gọi list()
        }
        if(cache.containsKey(id))
            return cache.get(id); //Kiểm tra xem id của Employer mình muốn tìm có không? Không thì trả về null
        return null;
    }


    public List<Employer> list(){
        List<Employer> list = new ArrayList<>();

        if(cache.size() != 0 && usingCache) {
            for(Map.Entry<Integer, Employer> employer : cache.entrySet()) {
                list.add(employer.getValue());
            }

            return list;
        }

        cache.clear();

        Connection conn;
        Statement st;
        ResultSet rs;
        String query = "SELECT * FROM employer;";

        try {
            conn = DB.getConnection();
            st = conn.createStatement();
            rs = st.executeQuery(query);

            Employer.EmployerBuilder builder;
            Employer employer;

            while(rs.next()) {

                builder = new Employer.EmployerBuilder();
                builder.setId(rs.getInt("id"));
                builder.setFname(rs.getString("fname"));
                builder.setLname(rs.getString("lname"));
                builder.setTel(rs.getString("tel"));
                builder.setDescription(rs.getString("description"));
                builder.setDate(rs.getTimestamp("date"));

                try {
                    employer = builder.build();
                    list.add(employer);
                    cache.put(employer.getId(), employer);

                } catch (EntityException e) {
                    showEntityException(e, rs.getString("fname") + " " + rs.getString("lname"));
                }
            }
        } catch (SQLException e) {
            showSQLException(e);
        }

        return list;
    }

    //CREATE---------------------------------------------------------------------------------------------
    public boolean create(Employer employer) {

        if(!createControl(employer))
            return false;

        Connection conn;
        PreparedStatement pst;
        int result = 0;
        String query = "INSERT INTO employer (fname,lname,tel,description) VALUES (?,?,?,?);";
        String query2 = "SELECT * FROM employer ORDER BY id DESC LIMIT 1;"; //Lấy nhân viên mới được thêm

        try {
            conn = DB.getConnection();
            pst = conn.prepareStatement(query);
            pst.setString(1, employer.getFname());
            pst.setString(2, employer.getLname());
            pst.setString(3, employer.getTel());
            pst.setString(4, employer.getDescription());
            result = pst.executeUpdate();

            // adding cache
            if(result != 0) {
                ResultSet rs = conn.createStatement().executeQuery(query2);
                while(rs.next()) {
                    Employer.EmployerBuilder builder = new Employer.EmployerBuilder();
                    builder.setId(rs.getInt("id"));
                    builder.setFname(rs.getString("fname"));
                    builder.setLname(rs.getString("lname"));
                    builder.setTel(rs.getString("tel"));
                    builder.setDescription(rs.getString("description"));
                    builder.setDate(rs.getTimestamp("date"));

                    try {
                        Employer emp = builder.build();
                        cache.put(emp.getId(), emp);
                    } catch (EntityException e) {
                        showEntityException(e, rs.getString("fname") + " " + rs.getString("lname"));
                    }
                }
            }
        } catch (SQLException e) {
            showSQLException(e);
        }

        return result != 0;
    }

    //Cùng tên cùng họ thì KHÔNG được thêm vào cache
    private boolean createControl(Employer employer) {
        for(Map.Entry<Integer, Employer> obj : cache.entrySet()) {
            if(obj.getValue().getFname().equals(employer.getFname())
                    && obj.getValue().getLname().equals(employer.getLname())) {
                DB.ERROR_MESSAGE = obj.getValue().getFname() + " " + obj.getValue().getLname() + " registration already exists.";
                return false;
            }
        }

        return true;
    }

    //UPDATE---------------------------------------------------------------------------------------------
    public boolean update(Employer employer) {
        if(!updateControl(employer))
            return false;

        Connection conn;
        PreparedStatement pst;
        int result = 0;
        String query = "UPDATE employer SET fname=?,"
                + "lname=?, tel=?, description=? WHERE id=?;";

        try {
            conn = DB.getConnection();
            pst = conn.prepareStatement(query);
            pst.setString(1, employer.getFname());
            pst.setString(2, employer.getLname());
            pst.setString(3, employer.getTel());
            pst.setString(4, employer.getDescription());
            pst.setInt(5, employer.getId());

            result = pst.executeUpdate();

            // update cache
            if(result != 0) {
                cache.put(employer.getId(), employer);
            }

        } catch (SQLException e) {
            showSQLException(e);
        }

        return result == 0 ? false : true;
    }

    //Phải nếu cùng tên cùng họ nhưng khác id thì KHÔNG dược cập nhật
    private boolean updateControl(Employer employer) {
        for(Map.Entry<Integer, Employer> obj : cache.entrySet()) {
            if(obj.getValue().getFname().equals(employer.getFname())
                    && obj.getValue().getLname().equals(employer.getLname())
                    && obj.getValue().getId() != employer.getId()) {
                DB.ERROR_MESSAGE = employer.getFname() + " " + employer.getLname() + " registration haven't exists.";
                return false;
            }
        }
        return true;
    }

    //DELETE---------------------------------------------------------------------------------------------
    public boolean delete(Employer employer) {
        Connection conn;
        PreparedStatement ps;
        int result = 0;
        String query = "DELETE FROM employer WHERE id=?;";

        try {

            conn = DB.getConnection();
            ps = conn.prepareStatement(query);
            ps.setInt(1, employer.getId());
            result = ps.executeUpdate();

            if(result != 0) {
                cache.remove(employer.getId());
            }
        } catch (SQLException e) {
            showSQLException(e);
        }

        return result == 0 ? false : true;
    }


    //USING CACHE---------------------------------------------------------------------------------------------
    public boolean isUsingCache() {
        return this.usingCache;
    }

    public void setUsingCache(boolean usingCache) {
        this.usingCache = usingCache;
    }

    //SINGLETON---------------------------------------------------------------------------------------------
    private static class EmployerDAOHelper{
        private static final EmployerDAO instance = new EmployerDAO();
    }

    public static EmployerDAO getInstance() {
        return EmployerDAOHelper.instance;
    }



    //EXCEPTION---------------------------------------------------------------------------------------------
    private void showEntityException(EntityException e, String msg) {
        String message = msg + " not added" +
                "\n" + e.getMessage() + "\n" + e.getLocalizedMessage() + e.getCause();
        JOptionPane.showMessageDialog(null, message);
    }
    private void showSQLException(SQLException e) {
        String message = e.getErrorCode() + "\n" + e.getMessage() + "\n" + e.getLocalizedMessage() + "\n" + e.getCause();
        JOptionPane.showMessageDialog(null, message);
    }
}
