package com.xl.utils;

import com.xl.dialog.UserPasswordDialog;
import net.sf.samtools.seekablestream.UserPasswordInput;

public class UserPasswordInputImpl implements UserPasswordInput {
    String host;
    String password;
    String user;

    @Override
    public String getPassword() {
        // TODO Auto-generated method stub
        return password;
    }

    @Override
    public String getUser() {
        // TODO Auto-generated method stub
        return user;
    }

    @Override
    public void setHost(String arg0) {
        // TODO Auto-generated method stub
        this.host = arg0;
    }

    @Override
    public boolean showDialog() {
        // TODO Auto-generated method stub
        UserPasswordDialog dlg = new UserPasswordDialog(user, host);
        dlg.setVisible(true);

        if (dlg.isCanceled()) {
            dlg.dispose();
            return false;
        } else {
            user = dlg.getUser();
            password = dlg.getPassword();
            dlg.dispose();
            return true;
        }
    }

}
