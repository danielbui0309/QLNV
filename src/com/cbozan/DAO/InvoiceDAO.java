package com.cbozan.DAO;

import com.cbozan.Entity.Employer;
import com.cbozan.Entity.Invoice;
import com.cbozan.Exception.EntityException;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvoiceDAO {
    private final HashMap<Integer, Invoice> cache = new HashMap<>();
    private boolean usingCache = true;

    private InvoiceDAO() {
        list();
    }

    public Invoice findById(int id) {

        if(!usingCache)
            list();
        if(cache.containsKey(id))
            return cache.get(id);

        return null;
    }

    public void refresh() {
        setUsingCache(false);
        list();
        setUsingCache(true);
    }

    public List<Invoice> list() {
        List<Invoice> list = new ArrayList<>();

        if (!cache.isEmpty() && usingCache) {
            for (Map.Entry<Integer, Invoice> invoice : cache.entrySet()) {
                list.add(invoice.getValue());
            }
            return list;
        }

        cache.clear();

        Connection conn;
        Statement st;
        ResultSet rs;
        String query = "SELECT * FROM invoice;";

        try {
            conn = DB.getConnection();
            st = conn.createStatement();
            rs = st.executeQuery(query);

            Invoice.InvoiceBuilder builder;
            Invoice invoice;

            while (rs.next()) {
                builder = new Invoice.InvoiceBuilder();
                builder.setId(rs.getInt("id"));
                builder.setJob_id(rs.getInt("job_id"));
                builder.setAmount(rs.getBigDecimal("amount"));
                builder.setDate(rs.getTimestamp("date"));

                try {
                    invoice = builder.build();
                    list.add(invoice);
                    cache.put(invoice.getId(), invoice);
                } catch (EntityException e) {
                    showEntityException(e, "ID : " + rs.getInt("id"));
                }
            }
        } catch (SQLException e) {
            showSQLException(e);
        }

        return list;
    }

    public List<Invoice> list(Employer employer){
        List<Invoice> invoiceList = new ArrayList<>();
        Connection conn;
        Statement st;
        ResultSet rs;
            String query = "SELECT * FROM invoice WHERE job_id IN (SELECT id FROM job WHERE employer_id=" + employer.getId() + ")";

        try {
            conn = DB.getConnection();
            st = conn.createStatement();
            rs = st.executeQuery(query);

            Invoice.InvoiceBuilder builder = new Invoice.InvoiceBuilder();
            Invoice invoice;

            while(rs.next()) {

                invoice = findById(rs.getInt("id"));
                if(invoice != null) { // cache control
                    invoiceList.add(invoice);
                } else {
                    builder.setId(rs.getInt("id"));
                    builder.setJob_id(rs.getInt("employer_id"));
                    builder.setAmount(rs.getBigDecimal("amount"));
                    builder.setDate(rs.getTimestamp("date"));

                    try {
                        invoice = builder.build();
                        invoiceList.add(invoice);
                        cache.put(invoice.getId(), invoice);
                    } catch (EntityException e) {
                        showEntityException(e, "EMPLOYER PAYMENT ADD ERROR");
                    }
                }
            }
        } catch(SQLException sqle) {
            showSQLException(sqle);
        }

        return invoiceList;
    }


    public List<Invoice> list(String columnName, int id){
        List<Invoice> list = new ArrayList<>();
        Connection conn;
        Statement st;
        ResultSet rs;
        String query = "SELECT * FROM invoice WHERE " + columnName + "=" + id;

        try {
            conn = DB.getConnection();
            st = conn.createStatement();
            rs = st.executeQuery(query);

            Invoice.InvoiceBuilder builder;
            Invoice invoice;

            while(rs.next()) {
                builder = new Invoice.InvoiceBuilder();
                builder.setId(rs.getInt("id"));
                builder.setJob_id(rs.getInt("job_id"));
                builder.setAmount(rs.getBigDecimal("amount"));
                builder.setDate(rs.getTimestamp("date"));
                try {
                    invoice = builder.build();
                    list.add(invoice);
                } catch (EntityException e) {
                    System.err.println(e.getMessage());
                    System.out.println("!! Invoice not added list !!");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }


    //CREATE------------------------------------------------------------------------------------
    public boolean create(Invoice invoice) {
        Connection conn;
        PreparedStatement pst;
        int result = 0;
        String query = "INSERT INTO invoice (job_id,amount) VALUES (?,?);";
        String query2 = "SELECT * FROM invoice ORDER BY id DESC LIMIT 1;";

        try {
            conn = DB.getConnection();
            pst = conn.prepareStatement(query);
            pst.setInt(1, invoice.getJob().getId());
            pst.setBigDecimal(2, invoice.getAmount());

            result = pst.executeUpdate();

            if(result != 0) {

                ResultSet rs = conn.createStatement().executeQuery(query2);
                while(rs.next()) {
                    Invoice.InvoiceBuilder builder = new Invoice.InvoiceBuilder();
                    builder.setId(rs.getInt("id"));
                    builder.setJob_id(rs.getInt("job_id"));
                    builder.setAmount(rs.getBigDecimal("amount"));
                    builder.setDate(rs.getTimestamp("date"));

                    try {
                        Invoice inv = builder.build();
                        cache.put(inv.getId(), inv);
                    } catch (EntityException e) {
                        showEntityException(e, "ID : " + rs.getInt("id"));
                    }
                }
            }
        } catch (SQLException e) {
            showSQLException(e);
        }

        return result > 0;
    }


    //UPDATE------------------------------------------------------------------------------------
    public boolean update(Invoice invoice) {
        Connection conn;
        PreparedStatement pst;
        int result = 0;
        String query = "UPDATE invoice SET job_id=?,"
                + "amount=? WHERE id=?;";

        try {
            conn = DB.getConnection();
            pst = conn.prepareStatement(query);
            pst.setInt(1, invoice.getJob().getId());
            pst.setBigDecimal(2, invoice.getAmount());
            pst.setInt(3, invoice.getId());

            result = pst.executeUpdate();

            if(result != 0) {
                cache.put(invoice.getId(), invoice);
            }
        } catch (SQLException e) {
            showSQLException(e);
        }

        return result > 0;
    }


    //DELETE------------------------------------------------------------------------------------
    public boolean delete(Invoice invoice) {
        Connection conn;
        PreparedStatement ps;
        int result = 0;
        String query = "DELETE FROM invoice WHERE id=?;";

        try {
            conn = DB.getConnection();
            ps = conn.prepareStatement(query);
            ps.setInt(1, invoice.getId());

            result = ps.executeUpdate();

            if(result != 0) {
                cache.remove(invoice.getId());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return result > 0;
    }


    //SET USING CACHE------------------------------------------------------------------------------------
    public boolean isUsingCache() {
        return this.usingCache;
    }

    public void setUsingCache(boolean usingCache) {
        this.usingCache = usingCache;
    }


    //INSTANCE---------------------------------------------------------------------------------------------
    private static class InvoiceDAOHelper {
        private static final InvoiceDAO instance = new InvoiceDAO();
    }

    public static InvoiceDAO getInstance() {
        return InvoiceDAOHelper.instance;
    }


    //EXCEPTION-----------------------------------------------------------------------------------------
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
