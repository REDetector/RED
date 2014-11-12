package com.xl.net.crashreport;

import com.xl.main.REDApplication;

import javax.swing.*;

public class HTMLDisplayDialog extends JDialog {

    public HTMLDisplayDialog(String html) {

        super(REDApplication.getInstance(), "Crash Report Help");

        JEditorPane jep = new JEditorPane("text/html", html);

        setContentPane(new JScrollPane(jep, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

        setSize(700, 500);
        setLocationRelativeTo(REDApplication.getInstance());
        setVisible(true);

    }

}
