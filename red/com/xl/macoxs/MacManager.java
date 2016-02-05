package com.xl.macoxs;

import java.awt.*;

import javax.swing.*;

import com.apple.eawt.Application;
import com.apple.eawt.FullScreenUtilities;

/**
 * Created by xingli on 2/5/16.
 */
public class MacManager {
    public static final MacManager sInstance = new MacManager();

    private MacManager() {
        init();
    }

    public static MacManager getInstance() {
        return sInstance;
    }

    private void init() {
        setMacAppName("RED");
    }

    public void setFullScreenEnable(Window window, boolean enable) {
        FullScreenUtilities.setWindowCanFullScreen(window, enable);
    }

    public void toggleFullScreen(Window window) {
        Application.getApplication().requestToggleFullScreen(window);
    }

    public void setMacMenu(JMenuBar jMenuBar) {
        Application application = Application.getApplication();
        MacMenuController macController = new MacMenuController();
        application.setAboutHandler(macController);
        application.setPreferencesHandler(macController);
        application.setQuitHandler(macController);
        application.setDefaultMenuBar(jMenuBar);
    }

    /**
     * The method won't work after Mac OS X 10.10.x or later.
     * 
     * @param name the app name.
     */
    public void setMacAppName(String name) {
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", name);
    }
}
