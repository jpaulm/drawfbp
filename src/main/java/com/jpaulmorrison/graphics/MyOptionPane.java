package com.jpaulmorrison.graphics;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class MyOptionPane {	
	
	static int YES_NO_OPTION = JOptionPane.YES_NO_OPTION;
	static int YES_NO_CANCEL_OPTION = JOptionPane.YES_NO_CANCEL_OPTION;
	static int CANCEL_OPTION = JOptionPane.CANCEL_OPTION;
	static int DEFAULT_OPTION = JOptionPane.DEFAULT_OPTION;
	static int OK_CANCEL_OPTION = JOptionPane.OK_CANCEL_OPTION;
	static int YES_OPTION = JOptionPane.YES_OPTION;
	static int NO_OPTION = JOptionPane.NO_OPTION;
	static int OK_OPTION = JOptionPane.OK_OPTION;
	static int PLAIN_MESSAGE = JOptionPane.PLAIN_MESSAGE;
	static int QUESTION_MESSAGE = JOptionPane.QUESTION_MESSAGE;	
	static int INFORMATION_MESSAGE = JOptionPane.INFORMATION_MESSAGE;
	static int WARNING_MESSAGE = JOptionPane.WARNING_MESSAGE;
	static int ERROR_MESSAGE = JOptionPane.ERROR_MESSAGE;
	
	static Object showInputDialog(Component f,  Object message, String title, int messageType, Icon ico,
			 Object[] options, Object initialValue) {
		 
		 JOptionPane pane = new JOptionPane(message, messageType, OK_CANCEL_OPTION, ico,
				  options, initialValue);
		
		//JScrollPane jsp = new JScrollPane();
		
		//pane.add(jsp);
					
	    JDialog dialog = pane.createDialog(f, title);
	    // JDialog dialog = new JDialog();
	     dialog.setTitle(title);
	     if (!dialog.isResizable()) 
             dialog.setResizable(true);
	     
	     //jsp.add(dialog);
          
	     dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	     DrawFBP.applyOrientation(dialog); 
	     pane.setInputValue("");
	     pane.setInitialSelectionValue(pane.getInitialValue());
	     pane.setWantsInput(true);
	     	     
	     dialog.pack();	     	     
	     dialog.setVisible(true);	
	     //dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
	     Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
	     
	     Object selValue = pane.getValue();
	     if (selValue == null)
	     	 return null;
	     if (selValue instanceof Integer && ((Integer) selValue).intValue() == CANCEL_OPTION)
	    	 return null;
	     Object o = pane.getInputValue();
	     if (selValue instanceof Float)
	    	 return selValue;
	     else
	    	 return o;
	     
	     
	}
	
	static Object showInputDialog(Component f,  Object message, String title) {
		return showInputDialog(f,  message,  title,  PLAIN_MESSAGE, null, null, null);
	}
	
	
	
	//-------------------------------------------
	
	
	static void showMessageDialog(Component f,  Object message, String s, int i, ImageIcon ico) {
		
		JOptionPane pane = new JOptionPane(message, i, DEFAULT_OPTION, ico);
		 
	     
	    // JScrollPane jsp = new JScrollPane();
	   //  pane.add(jsp);
	    JDialog dialog = pane.createDialog(f, s);
	    // JDialog dialog = new JDialog();
	   //  dialog.setTitle(s);
	     
	    // jsp.add(dialog);
	     if (!dialog.isResizable())  
             dialog.setResizable(true);
	     dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
	     //dialog.setPreferredSize(new Dimension(200, 200));  //fudge
	          	     
	     dialog.pack();	     	     
	     dialog.setVisible(true);
	     //dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
	     Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
	     DrawFBP.applyOrientation(dialog);
	}
	 
	
	
	
	static void showMessageDialog(Component f,  String message, int messageType) {
		showMessageDialog(f, message, null, messageType, null);
	}
	 
	static void showMessageDialog(Component f,  String message) {
		showMessageDialog(f, message, null, PLAIN_MESSAGE, null);
	}
	//------------------------------------------------------------
	
	static int showConfirmDialog(Component f,  Object message, String title, int optionType, int messageType) {
		
		JOptionPane pane = new JOptionPane(message, messageType, optionType);		 
	 	 
	     JDialog dialog = pane.createDialog((Component) f, title);
	     if (!dialog.isResizable())  
           dialog.setResizable(true);
        
	     dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	     DrawFBP.applyOrientation(dialog);  
	     //pane.setInitialSelectionValue(pane.getInitialValue());
	     //pane.setWantsInput(true);
	     	     
	     dialog.pack();	     	     
	     dialog.setVisible(true);	
	     //dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
	     Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
	     int i = -1;
	     if (pane.getValue() != null)  
	         i = ((Integer) pane.getValue()).intValue();
	     return i;
	     
	}
	static int showConfirmDialog(Component f,  Object message, String title, int optionType) {
		return showConfirmDialog(f, message, title, optionType, QUESTION_MESSAGE);
	}

	//-------------------------------------------
	
	
	public static int showOptionDialog(Component f, Object[] message,
			String title) {

		 
		JOptionPane pane = new JOptionPane(message, PLAIN_MESSAGE, OK_CANCEL_OPTION);		
		
		/*
		http://stackoverflow.com/questions/27404362/custom-dialog-using-joptionpane-api-wont-dispose
		*/
			
	     JDialog dialog = pane.createDialog(f, title);
	     if (!dialog.isResizable()) 
            dialog.setResizable(true);
         
	     dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	     DrawFBP.applyOrientation(dialog); 
	     	     	     
	     dialog.pack();	     	     
	     dialog.setVisible(true);	
	     //dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
	     Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
	     int i = -1;
	     if (pane.getValue() != null)  
	         i = ((Integer) pane.getValue()).intValue();
	     return i;
	     
	     
	}
	
	} 
	
	
	
	

