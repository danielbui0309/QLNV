package com.cbozan.Main;

import com.cbozan.View.add.JobPaymentPanel;
import com.cbozan.View.record.JobPanel;

import java.awt.*;
import java.util.Locale;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        Locale.setDefault(new Locale("vn", "VN"));

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Login();
            }
        });
    }
}