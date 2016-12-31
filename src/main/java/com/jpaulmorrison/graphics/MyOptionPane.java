package com.jpaulmorrison.graphics;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

public class MyOptionPane {
	
	static Object showInputDialog(Object f,  Object message, String title, int messageType, Icon ico,
			 Object[] options, Object initialValue) {
		 
		 JOptionPane pane = new JOptionPane(message, messageType, JOptionPane.OK_CANCEL_OPTION, ico,
				  options, initialValue);
		 
		if (options != null) {
			 new Throwable().printStackTrace();
			 return null;
		}
		 
	     JDialog dialog = pane.createDialog((Component) f, title);
	     if (!dialog.isResizable()) {
             dialog.setResizable(true);
         }
	     DrawFBP.applyOrientation((Component)f); 
	     pane.setInputValue("");
	     pane.setInitialSelectionValue(pane.getInitialValue());
	     pane.setWantsInput(true);
	     	     
	     dialog.pack();	     	     
	     dialog.setVisible(true);	
	     
	     Object selectedValue = pane.getValue();
	     Integer in = (Integer) selectedValue;
	     if (in == null || in.intValue() == JOptionPane.CANCEL_OPTION)
	       return null;
	     
	     return pane.getInputValue(); 
	}
	
	static Object showInputDialog(Object f,  Object message, String title, int messageType) {
		return showInputDialog(f,  message,  title,  messageType, null, null, null);
	}
	
	static Object showInputDialog(Object f,  Object message, String title) {
		return showInputDialog(f,  message,  title,  JOptionPane.PLAIN_MESSAGE);
	}
	
	//-------------------------------------------
	
	
	static void showMessageDialog(Object f,  Object message, String s, int i, ImageIcon ico) {
		JOptionPane pane = new JOptionPane(message, i, JOptionPane.DEFAULT_OPTION, ico);
		 
		 //if (options != null)
		//	 new Throwable().printStackTrace();
		 
	     JDialog dialog = pane.createDialog((Component) f, null);
	     if (!dialog.isResizable()) {
             dialog.setResizable(true);
         }
	          	     
	     dialog.pack();	     	     
	     dialog.setVisible(true);
	}
	 
	
	static void showMessageDialog(Object f,  Object message, String s, int i) {
		showMessageDialog(f, message, s, i, null);
	}
	
	static void showMessageDialog(Object f,  Object message, String s) {
		showMessageDialog(f, message, s, JOptionPane.PLAIN_MESSAGE);
	}
	
	static void showMessageDialog(Object f,  Object message) {
		showMessageDialog(f, message, null, JOptionPane.PLAIN_MESSAGE);
	}
	
	//static void showMessageDialog(Object f,  String s) {
	//	showMessageDialog(f, null, s, JOptionPane.PLAIN_MESSAGE);
	//}
	 
	
	//------------------------------------------------------------
	
	static int showConfirmDialog(Object f,  Object message, String title, int optionType, int messageType) {
		JOptionPane pane = new JOptionPane(message, messageType, optionType);		 
	 	 
	     JDialog dialog = pane.createDialog((Component) f, title);
	     if (!dialog.isResizable()) {
           dialog.setResizable(true);
       }
	     DrawFBP.applyOrientation((Component)f);  
	     //pane.setInitialSelectionValue(pane.getInitialValue());
	     //pane.setWantsInput(true);
	     	     
	     dialog.pack();	     	     
	     dialog.setVisible(true);	
	     int i = -1;
	     if (pane.getValue() != null)  
	         i = ((Integer) pane.getValue()).intValue();
	     return i;
	     
	}
	static int showConfirmDialog(Object f,  Object message, String title, int optionType) {
		return showConfirmDialog(f, message, title, optionType, JOptionPane.PLAIN_MESSAGE);
	} 
	
	static int showConfirmDialog(Object f,  Object message, String title) {
		return showConfirmDialog(f, message, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
	}
	
	
	
	}

