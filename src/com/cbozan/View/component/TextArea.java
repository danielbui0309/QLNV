package com.cbozan.View.component;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class TextArea extends JScrollPane implements FocusListener{
    private static final long serialVersionUID = 8546529312909157181L;
    JTextArea text;

    public TextArea() {
        text = new JTextArea();
        setViewportView(text);
        text.addFocusListener(this);
        this.addFocusListener(this);
    }

    public String getText(){
        return text.getText();
    }

    public void setText(String strText) {
        // Sử dụng UTF-8 khi đặt văn bản
        text.setText(strText);
    }

    public boolean isEditable() {
        return text.isEditable();
    }

    public void setEditable(boolean bool) {
        text.setEditable(bool);
    }

    @Override
    public void focusGained(FocusEvent e) {
        text.setBorder(new LineBorder(Color.BLUE));
    }

    @Override
    public void focusLost(FocusEvent e) {
        text.setBorder(new LineBorder(Color.WHITE));
    }
}
