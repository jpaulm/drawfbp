package com.jpaulmorrison.graphics;

import javax.swing.*;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;

/**
 * A JTabbedPane which has a close ('X') icon on each tab.
 * 
 */
public class JTabbedPaneWithCloseIcons extends JTabbedPane {
	static final long serialVersionUID = 111L;
	DrawFBP driver;
	
	public JTabbedPaneWithCloseIcons(DrawFBP d) {
		super();
		driver = d;

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
				//driver.setTitle("Diagram: " + driver.curDiag.title);
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
					
					driver.curDiag.area.removeMouseListener((MouseListener) driver.curDiag.area);
					driver.curDiag.area.removeMouseMotionListener((MouseMotionListener)driver.curDiag.area);
					String s = f.getAbsolutePath();
					if (s == null || s.endsWith(".drw")) {
						driver.curDiag.area.addMouseListener((MouseListener)driver.curDiag.area);
						driver.curDiag.area.addMouseMotionListener((MouseMotionListener)driver.curDiag.area);
					}
					
				}

				
			}
		}

	}
	
		
	
	
 
}
