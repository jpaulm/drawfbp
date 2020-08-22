package com.jpaulmorrison.graphics;

import javax.swing.text.*;

public class MyDocument extends DefaultStyledDocument {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	boolean changed = false;
	public MyDocument(StyleContext sc) {
		super(sc);
	}
	
	public void insertString(int i, String s, AttributeSet a) throws BadLocationException {		
			super.insertString(i, s, a);	
			//System.out.println(i + ":" + s); 
	}
	
}
