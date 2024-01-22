package com.cbozan.DAO;

import com.cbozan.Entity.Worker;
import com.cbozan.Exception.EntityException;

import javax.swing.*;
import javax.swing.plaf.nimbus.State;
import javax.xml.transform.Result;
import java.sql.*;
import java.util.*;

public class WorkerDAO {
    private final HashMap<Integer, Worker> cache = new HashMap<>();
    private boolean usingCache = true;

    private WorkerDAO() {
        list();
    }

    ;

    public Worker findById(int id) {
        if (!usingCache)
            list();

        if (cache.containsKey(id))
            return cache.get(id);

        return null;
    }

    public void refresh() {
        setUsingCache(false);
        list();
        setUsingCache(true);
    }

    public List<Worker> list() {
        List<Worker> list = new ArrayList<>();

        if (!cache.isEmpty() && usingCache) {
            for (Map.Entry<Integer, Worker> worker : cache.entrySet())
                list.add(worker.getValue());
            return list;
        }

        cache.clear();


        try {
            Connection conn;
            Statement st;
            ResultSet rs;
            String query = "SELECT * from worker;";

            conn = DB.getConnection();
            st = conn.createStatement();
            rs = st.executeQuery(query);

            Worker worker;
            Worker.WorkerBuilder builder;

            while (rs.next()) {
                builder = new Worker.WorkerBuilder();
                builder.setId(rs.getInt("id"));
                builder.setFname(rs.getString("fname"));
                builder.setLname(rs.getString("lname"));
                builder.setTel(rs.getString("tel"));
                builder.setIban(rs.getString("iban"));
                builder.setDescription(rs.getString("description"));
                builder.setDate(rs.getTimestamp("date"));

                try {
                    worker = builder.build();
                    list.add(worker);
                    cache.put(worker.getId(), worker);
                } catch (EntityException e) {
                    showEntityException(e, rs.getString("fname" + " " + rs.getString("lname")));
                }
            }
        } catch (SQLException e) {
            showSQLException(e);
        }

        return list;
    }


    //CREATE-------------------------------------------------------------------------------------------
    public boolean create(Worker worker) {
        if (!createControl(worker))
            return false;

        Connection conn;
        PreparedStatement pst;
        int result = 0;
        String query = "INSERT INTO worker (fname,lname,tel,iban,description) VALUES(?,?,?,?,?);";
        String query1 = "select * from worker order by id desc limit 1;";

        try {
            conn = DB.getConnection();
            pst = conn.prepareStatement(query);
            pst.setString(1, worker.getFname());
            pst.setString(2, worker.getLname());
            pst.setString(3, worker.getTel());
            pst.setString(4, worker.getIban());
            pst.setString(5, worker.getDescription());
            result = pst.executeUpdate();

            if (result != 0) {
                ResultSet rs = conn.createStatement().executeQuery(query1);

                while (rs.next()) {
                    Worker.WorkerBuilder builder = new Worker.WorkerBuilder();
                    builder.setId(rs.getInt("id"));
                    builder.setFname(rs.getString("fname"));
                    builder.setLname(rs.getString("lname"));
                    builder.setTel(rs.getString("tel"));
                    builder.setDescription(rs.getString("description"));
                    builder.setIban(rs.getString("iban"));
                    builder.setDate(rs.getTimestamp("date"));

                    try {
                        Worker obj = builder.build();
                        cache.put(obj.getId(), obj);
                    } catch (EntityException e) {
                        showEntityException(e, rs.getString("fname" + rs.getString("lname")));
                    }
                }
            }

        } catch (SQLException e) {
            showSQLException(e);
        }

        return result != 0;
    }

    private boolean createControl(Worker worker) {
        for (Map.Entry<Integer, Worker> obj : cache.entrySet()) {
            if (obj.getValue().getFname().equals(worker.getFname())
                    && obj.getValue().getLname().equals(worker.getLname())) {
                DB.ERROR_MESSAGE = obj.getValue().getFname() + " " + obj.getValue().getLname() + " has already existed.";
                return false;
            }
        }
        return true;
    }


    //UPDATE-------------------------------------------------------------------------------------------
    public boolean update(Worker worker) {
        if (!updateControl(worker))
            return false;

        Connection conn;
        PreparedStatement pst;
        int result = 0;
        String query = "update worker"
                + " set fname = ?, lname=?, tel=?, iban=?, description=?"
                + " where id=?;";
        try {
            conn = DB.getConnection();
            pst = conn.prepareStatement(query);
            pst.setString(1, worker.getFname());
            pst.setString(2, worker.getLname());
            pst.setString(3, worker.getTel());
            pst.setString(4, worker.getIban());
            pst.setString(5, worker.getDescription());
            pst.setInt(6, worker.getId());

            result = pst.executeUpdate();

            if (result != 0) {
                cache.put(worker.getId(), worker);
            }

        } catch (SQLException e) {
            showSQLException(e);
        }

        return result != 0;
    }
    private boolean updateControl(Worker worker) {
        for(Map.Entry<Integer, Worker> obj : cache.entrySet()) {
            if(obj.getValue().getFname().equals(worker.getFname())
                    && obj.getValue().getLname().equals(worker.getLname())
                    && obj.getValue().getId() != worker.getId()) {
                DB.ERROR_MESSAGE = obj.getValue().getFname() + " " + obj.getValue().getLname() + " haven't exited yet.";
                return false;
            }
        }
        return true;
    }


    //DELETE-------------------------------------------------------------------------------------------
    public boolean delete(Worker worker) {
        Connection conn;
        PreparedStatement pst;
        int result = 0;
        String query = "delete from worker where id=?;";

        try{
            conn = DB.getConnection();
            pst = conn.prepareStatement(query);
            pst.setInt(1, worker.getId());
            result = pst.executeUpdate();
            if(result != 0)
                cache.remove(worker.getId(), worker);
        }catch(SQLException e){
            showSQLException(e);
        }

        return result!=0;
    }


    //INSTANCE FOR WORKER_DAO
    private static class WorkerDAOHelper{
        private static final WorkerDAO instance = new WorkerDAO();
    }
    public static WorkerDAO getInstance(){
        return WorkerDAOHelper.instance;
    }


    //USING CACHE-----------------------------------------------------------------------------------
    public boolean isUsingCache() {
        return usingCache;
    }
    public void setUsingCache(boolean usingCache) {
        this.usingCache = usingCache;
    }


    //EXCEPTION AREA-----------------------------------------------------------------------------------
    private void showSQLException(SQLException e) {
        String message = e.getErrorCode() + "\n" + e.getMessage()
                + "\n" + e.getLocalizedMessage() + "\n" + e.getCause();
        JOptionPane.showMessageDialog(null, message);
    }
    private void showEntityException(EntityException e, String msg) {
        String message = msg + "not add"
                + "\n" + e.getMessage() + "\n" + e.getLocalizedMessage() + "\n" + e.getCause();
        JOptionPane.showMessageDialog(null, message);
    }
}
