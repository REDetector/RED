/**
 * Copyright 2009-13 Simon Andrews
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
package com.xl.help;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * The Class HelpPageDisplay provides a panel which can display a single help page.
 */
public class HelpPageDisplay extends JPanel implements HyperlinkListener {

    /**
     * The html pane.
     */
    public JEditorPane htmlPane;

    /**
     * Instantiates a new help page display.
     *
     * @param page the page
     */
    public HelpPageDisplay(HelpPage page) {

        if (page != null) {
            try {
                htmlPane = new JEditorPane(page.getFile().toURI().toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            htmlPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            htmlPane.setEditable(false);
            htmlPane.addHyperlinkListener(this);

            setLayout(new BorderLayout());
            add(new JScrollPane(htmlPane), BorderLayout.CENTER);
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.event.HyperlinkListener#hyperlinkUpdate(javax.swing.event.HyperlinkEvent)
     */
    public void hyperlinkUpdate(HyperlinkEvent h) {
        if (h.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                if (h.getURL().getProtocol().startsWith("http")) {
                    Desktop.getDesktop().browse(h.getURL().toURI());
                } else {
                    htmlPane.setPage(h.getURL());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }
}
