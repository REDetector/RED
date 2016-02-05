package com.xl.macoxs;

import com.apple.eawt.*;
import com.xl.display.dialog.AboutDialog;
import com.xl.display.dialog.EditPreferencesDialog;

/**
 * Created by xingli on 2/5/16.
 */
public class MacMenuController implements AboutHandler, QuitHandler, PreferencesHandler {

    @Override
    public void handleAbout(AppEvent.AboutEvent aboutEvent) {
        new AboutDialog();
    }

    @Override
    public void handlePreferences(AppEvent.PreferencesEvent preferencesEvent) {
        new EditPreferencesDialog();
    }

    @Override
    public void handleQuitRequestWith(AppEvent.QuitEvent quitEvent, QuitResponse quitResponse) {
        System.exit(0);
    }
}