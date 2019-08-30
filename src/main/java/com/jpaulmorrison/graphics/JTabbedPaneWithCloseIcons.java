package com.jpaulmorrison.graphics;

import javax.swing.*;
import java.awt.*;
import java.io.File;

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
			
			if (b != null && b.diag != null) {
				driver.curDiag = b.diag;
				driver.frame.setTitle("Diagram: " + driver.curDiag.title);  
				File f = driver.curDiag.diagFile;
				if (f != null) {
				    File currentDiagramDir = f.getParentFile();
				    //if (driver.properties == null)
				    //	driver.properties = new HashMap <String, String>();
				    if (currentDiagramDir != null)	
				    	driver.properties
							.put("currentDiagramDir", currentDiagramDir.getAbsolutePath());  
				    //saveProperties();  
				}

				 
				if (driver.curDiag != null && driver.curDiag.diagLang != null && 
						driver.curDiag.diagLang != driver.currLang) {
					driver.changeLanguage(driver.curDiag.diagLang);
				}
				 
			}
		}
	}

 
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		int tabno =  getSelectedIndex();
		int j = getTabCount();
		// Iterate through the tabs
		
		for (int i = 0; i < /* driver.jtp.*/ j; i++) {
			ButtonTabComponent b = (ButtonTabComponent) getTabComponentAt(i);	
			
			if (i == tabno) {
				/*driver.jtp.*/setBackgroundAt(i, Color.WHITE);
				b.selected = true;
			}
			else {
				/*driver.jtp.*/setBackgroundAt(i, Color.lightGray); 
				b.selected = false;
			}
		}
	}
	 
}
