/**
 * Copyright 2010-13 Simon Andrews
 *
 *    This file is part of SeqMonk.
 *
 *    SeqMonk is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    SeqMonk is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with SeqMonk; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.xl.panel;

import com.xl.main.REDApplication;

import javax.swing.*;
import java.awt.*;

/**
 * This is an information panel which takes up all of the main display when
 * SeqMonk is first launched. It is intended to be a more friendly introduction
 * than the current blank screen. As well as putting up an advert it will also
 * show some status information to show people what is and isn't set up and
 * working.
 */

public class WelcomePanel extends JPanel {

    private REDInfoPanel infoPanel;

    public WelcomePanel(REDApplication application) {
        setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.8;
        gridBagConstraints.fill = GridBagConstraints.NONE;

        add(new JPanel(), gridBagConstraints);
        gridBagConstraints.weighty = 0.2;
        gridBagConstraints.gridy++;

        add(new REDTitlePanel(), gridBagConstraints);
        gridBagConstraints.gridy++;
        infoPanel = new REDInfoPanel(application);
        add(infoPanel, gridBagConstraints);

        gridBagConstraints.weighty = 0.8;
        gridBagConstraints.gridy++;
        add(new JPanel(), gridBagConstraints);

    }

    public boolean cacheDirectoryValid() {
        if (infoPanel != null) {
            return infoPanel.cacheDirectoryValid();
        } else {
            return false;
        }
    }

}
