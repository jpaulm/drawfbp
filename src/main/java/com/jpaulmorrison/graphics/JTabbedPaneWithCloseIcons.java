package com.jpaulmorrison.graphics;

import javax.swing.*;

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
		if (i == -1 || i >= getTabCount())
			return;

		super.setSelectedIndex(i);

		ButtonTabComponent b = (ButtonTabComponent) getTabComponentAt(i);

		if (b != null) {
			//b.selected = true;
			if (b.diag != null) {
				driver.curDiag = b.diag;
				driver.setTitle("Diagram: " + driver.curDiag.title);
				if (driver.curDiag.title.toLowerCase().endsWith(".drw"))
					driver.curDiag.title = driver.curDiag.title.substring(0,
							driver.curDiag.title.length() - 4);

				File f = driver.curDiag.diagFile;
				if (f != null) {
					File currentDiagramDir = f.getParentFile();
					// if (driver.properties == null)
					// driver.properties = new HashMap <String, String>();
					if (currentDiagramDir != null)
						driver.saveProp("currentDiagramDir",
								currentDiagramDir.getAbsolutePath());
					// saveProperties();
					
					driver.curDiag.area.removeMouseListener(driver.curDiag.area);
					driver.curDiag.area.removeMouseMotionListener(driver.curDiag.area);
					String s = f.getAbsolutePath();
					if (s == null || s.endsWith(".drw")) {
						driver.curDiag.area.addMouseListener(driver.curDiag.area);
						driver.curDiag.area.addMouseMotionListener(driver.curDiag.area);
					}
					
				}

				//if (driver.curDiag != null && driver.currNotn != null
				//		&& driver.currNotn != driver.currNotn) {
				//	driver.setNotation(driver.currNotn);
				//}

			}
		}

	}
	
	
	
	/*
	int getSelected(){
		int j = getTabCount();
		// Iterate through the tabs
		
		for (int i = 0; i <  j; i++) {
			ButtonTabComponent b = (ButtonTabComponent) getTabComponentAt(i);	
			if (b.selected)
				return i;
		}
		return -1;
	}
*/
 
}
