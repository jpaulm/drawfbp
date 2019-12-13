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

		/*
		for (int j = 0; j < getTabCount(); j++) {
			ButtonTabComponent b = (ButtonTabComponent) getTabComponentAt(j);
			if (b != null  )
				b.selected = false;
		}
		*/
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
				}

				if (driver.curDiag != null && driver.curDiag.diagLang != null
						&& driver.curDiag.diagLang != driver.currLang) {
					driver.changeLanguage(driver.curDiag.diagLang);
				}

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
