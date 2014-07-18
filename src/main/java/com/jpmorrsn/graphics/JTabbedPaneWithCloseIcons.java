package com.jpmorrsn.graphics;

import javax.swing.*;
import java.awt.*;
//import java.io.File;

/**
 * A JTabbedPane which has a close ('X') icon on each tab.
 * 
 */
public class JTabbedPaneWithCloseIcons extends JTabbedPane {
	static final long serialVersionUID = 111L;
	DrawFBP driver;
	
	public JTabbedPaneWithCloseIcons(DrawFBP drawFBP) {
		super();
		driver = drawFBP;

		setFont(driver.fontf); // because diagram names may have Unicode
								// characters...

	}

	
	public void setSelectedIndex(int i) {
		if (i > -1) {
			super.setSelectedIndex(i);
			ButtonTabComponent b = (ButtonTabComponent) getTabComponentAt(i);
			if (b != null) {
				driver.curDiag = b.diag;
				if (driver.curDiag != null && driver.curDiag.diagLang != null && 
						driver.curDiag.diagLang != driver.defaultCompLang) {
					driver.changeLanguage(driver.curDiag.diagLang);
				}
			}
		}
	}

	public void paint(Graphics g) {
		super.paint(g);
		int tabno = getSelectedIndex();
		for (int i = 0; i < driver.jtp.getTabCount(); i++) {
			String s = "";
			ButtonTabComponent b = (ButtonTabComponent) getTabComponentAt(i);
			Diagram d = b.diag;
			driver.jtp.setBackgroundAt(i, Color.LIGHT_GRAY);
			if (d == null)
				s = "(untitled)";
			else if (d.diagFile == null){
				if (d.title == null)
					s = "(untitled)";
				else
					s = d.title;
			}
			else {
				if (i == tabno) {
					s = d.diagFile.getAbsolutePath();
					
				} else {
					s = d.diagFile.getName();
				}
			}

			if (d != null && d.changed)
				s = "* " + s;
			if (i == tabno) {
				driver.jtp.setBackgroundAt(i, Color.WHITE);
				
			}

			// c contains JLabel followed by button
			JLabel j = (JLabel) b.getComponent(0);

			j.setText(s);
		}
	}
}
