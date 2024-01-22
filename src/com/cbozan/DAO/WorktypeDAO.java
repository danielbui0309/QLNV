package com.cbozan.DAO;

import com.cbozan.Entity.Work;
import com.cbozan.Entity.Worktype;
import com.cbozan.Exception.EntityException;

import javax.swing.*;
import javax.swing.text.html.parser.Entity;
import javax.xml.stream.events.StartElement;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorktypeDAO {
    private HashMap<Integer, Worktype> cache = new HashMap<>();
    private boolean usingCache = true;

    private WorktypeDAO() {
        list();
    }

    public Worktype findById(int id) {
        if (!usingCache)
            list();
        if (cache.containsKey(id))
            return cache.get(id);
        return null;
    }

    public List<Worktype> list() {
        List<Worktype> list = new ArrayList<>();

        if (!cache.isEmpty() && usingCache) {
            for (Map.Entry<Integer, Worktype> obj : cache.entrySet())
                list.add(obj.getValue());
            return list;
        }

        cache.clear();

        Connection connection;
        Statement st;
        ResultSet rs;
        String query = "select * from worktype";

        try {
            connection = DB.getConnection();
            st = connection.createStatement();
            rs = st.executeQuery(query);
            Worktype worktype;
            Worktype.WorktypeBuilder builder;

            while (rs.next()) {
                builder = new Worktype.WorktypeBuilder();
                builder.setId(rs.getInt("id"));
                builder.setTitle(rs.getString("title"));
                builder.setNo(rs.getInt("no"));
                builder.setDate(rs.getTimestamp("date"));

                try {
                    worktype = builder.build();
                    list.add(worktype);
                    cache.put(worktype.getId(), worktype);
                } catch (EntityException e) {
                    showEntityException(e, "ID : " + rs.getInt("id") + ", Title : " + rs.getString("title"));
                }
            }
        } catch (SQLException e) {
            showSQLException(e);
        }

        return list;
    }


    //CREATE--------------------------------------------------------------------------------------------
    private boolean create(Worktype worktype) {
        if (!createControl(worktype))
            return false;

        Connection connection;
        PreparedStatement pst;
        int result = 0;
        String query = "insert into worktype (title, no, date) values (?,?);";

        try {
            connection = DB.getConnection();
            pst = connection.prepareStatement(query);
            pst.setString(1, worktype.getTitle());
            pst.setInt(2, worktype.getNo());
            result = pst.executeUpdate();

            if (result != 0) {
                String query1 = "select * from worktype order by id desc limit 1;";
                ResultSet rs = connection.createStatement().executeQuery(query1);
                while (rs.next()) {
                    Worktype.WorktypeBuilder builder = new Worktype.WorktypeBuilder();
                    builder.setId(rs.getInt("id"));
                    builder.setTitle(rs.getString("title"));
                    builder.setNo(rs.getInt("no"));
                    builder.setDate(rs.getTimestamp("date"));

                    try {
                        Worktype obj = builder.build();
                        cache.put(obj.getId(), obj);
                    } catch (EntityException e) {
                        showEntityException(e, "ID : " + rs.getInt("id") + ", Title : " + rs.getString("title"));
                    }
                }
            }
        } catch (SQLException e) {
            showSQLException(e);
        }

        return result > 0;
    }

    private boolean createControl(Worktype worktype) {
        for (Map.Entry<Integer, Worktype> obj : cache.entrySet()) {
            if (obj.getValue().getTitle().equals(worktype.getTitle())) {
                return false;
            }
        }
        return true;
    }


    //UPDATE--------------------------------------------------------------------------------------------
    private boolean update(Worktype worktype) {
        if (!updateControl(worktype))
            return false;

        Connection connection;
        PreparedStatement pst;
        int result = 0;
        String query = "update worktype set title =?, no=? where id=?;";

        try {
            connection = DB.getConnection();
            pst = connection.prepareStatement(query);
            pst.setString(1, worktype.getTitle());
            pst.setInt(2, worktype.getNo());
            pst.setInt(1, worktype.getId());

            result = pst.executeUpdate();

            if (result != 0)
                cache.put(worktype.getId(), worktype);
        } catch (SQLException e) {
            showSQLException(e);
        }

        return result > 0;
    }

    private boolean updateControl(Worktype worktype) {
        for (Map.Entry<Integer, Worktype> obj : cache.entrySet()) {
            if (obj.getValue().getTitle().equals(worktype.getTitle()) && obj.getValue().getId() != worktype.getId()) {
                return false;
            }
        }
        return true;
    }


    //DELETE--------------------------------------------------------------------------------------------
    private boolean delete(Worktype worktype){
        Connection connection;
        Statement st;
        String query="delete from worktype where id=" + worktype.getId();
        int result =0;
        try{
            connection = DB.getConnection();
            st=connection.createStatement();
            result=st.executeUpdate(query);
            if(result!=0)
                cache.remove(worktype.getId());
        }catch (SQLException e) {showSQLException(e);}
        return result>0;
    }

    //INSTANCE--------------------------------------------------------------------------------------------
    private static class WorktypeDAOHelper {
        private static final WorktypeDAO instance = new WorktypeDAO();
    }

    public static WorktypeDAO getInstance() {
        return WorktypeDAOHelper.instance;
    }

    //USING CACHE--------------------------------------------------------------------------------------------
    public boolean isUsingCache() {
        return usingCache;
    }

    public void setUsingCache(boolean usingCache) {
        this.usingCache = usingCache;
    }


    //EXCEPTION--------------------------------------------------------------------------------------------
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
