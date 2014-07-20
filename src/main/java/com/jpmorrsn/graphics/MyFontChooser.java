package com.jpmorrsn.graphics;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.LinkedList;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

//import com.jpmorrsn.graphics.DrawFBP.FontType;

public class MyFontChooser implements ListSelectionListener, WindowListener {

	//private static final long serialVersionUID = 1L;
	JFrame frame;
	// Graphics2D osg;
	DrawFBP driver;
	//FontType ft = null;
	//String result;
	JDialog popup;
	//String[] selFonts;
	String[] fixedFonts, generalFonts;
	JList<String> jlF = null;
	JList<String> jlG = null;
	//String fontDesc;
	JScrollPane lsF, lsG;
	//CellRenderer cr;
	String fixedFont, generalFont;
	boolean fFChanged, gFChanged;
	JLabel lb1, lb2;
	

	MyFontChooser(JFrame frm, DrawFBP drawFBP) {

		// type = t; // true if general text; false if fixed-width
		frame = frm;
		driver = drawFBP;
		//ft = ftyp;
		//cr = new CellRenderer();
	}
	
	void buildFontLists() {
		Font[] allfonts = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getAllFonts();
		LinkedList<String> llF = new LinkedList<String>();
		LinkedList<String> llG = new LinkedList<String>();

		for (int j = 0; j < allfonts.length; j++) {
			llG.add(allfonts[j].getName());
			FontMetrics fontMetrics = driver.osg.getFontMetrics(allfonts[j]);
			if (fontMetrics.charWidth('i') == fontMetrics
							.charWidth('m'))
				llF.add(allfonts[j].getName());
		}
		fixedFonts = new String[llF.size()];
		llF.toArray(fixedFonts);
		generalFonts = new String[llG.size()];
		llG.toArray(generalFonts);
			

		String prompt = "";

		popup = new JDialog(frame, Dialog.ModalityType.APPLICATION_MODAL);
		popup.setFocusable(true);
		popup.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				popup.dispose();
			}
			// }
		});
		popup.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ev) {
				if (ev.getKeyCode() == KeyEvent.VK_ESCAPE) {
					popup.dispose();
				}
			}
		});
		//fontDesc = (ft == FontType.GENERAL) ? "general text" : "fixed width";
		
		

		JPanel p = new JPanel();
		Box b1 = new Box(BoxLayout.Y_AXIS);
		Box b2 = new Box(BoxLayout.Y_AXIS);
				
		prompt = "Select fonts for fixed and/or general characters";
		popup.setTitle(prompt);

		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		
		jlF = new JList<String>(fixedFonts);		
		jlF.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);		
		jlF.setLayoutOrientation(JList.VERTICAL);  		
		jlF.setCellRenderer(new CellRenderer());
		
		jlG = new JList<String>(generalFonts);		
		jlG.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);		
		jlG.setLayoutOrientation(JList.VERTICAL);  		
		jlG.setCellRenderer(new CellRenderer());

		lsF = new JScrollPane(jlF);
		lsF.setPreferredSize(new Dimension(400, 600));
		lsG = new JScrollPane(jlG);
		lsG.setPreferredSize(new Dimension(400, 600));
		
		fixedFont = driver.fixedFont;
		lb1 = new JLabel("Fixed Fonts (" + fixedFont + ")");
		b1.add(lb1);
		b1.add(lsF);
		jlF.setSelectedValue(fixedFont, true); 
		
		generalFont = driver.generalFont;
		lb2 = new JLabel("General Fonts (" + generalFont + ")");
		b2.add(lb2);
		b2.add(lsG);
		jlG.setSelectedValue(generalFont, true); 		
		
		p.add(b1);
		p.add(Box.createRigidArea(new Dimension(10, 0)));
		p.add(b2);
				
		popup.add(p);

		popup.setBounds(100, 100, 800, 800);

		jlF.addListSelectionListener(this);
		jlG.addListSelectionListener(this);

		lsF.revalidate();
		lsG.revalidate();
		popup.revalidate();
		lsF.repaint();
		lsG.repaint();

		//popup.pack();
		popup.setLocation(200, 100);
		popup.pack();
		//frame.pack();
		 
		//jl.setVisible(true);
		//ls.setVisible(true);
		popup.setVisible(true);
	}
	
	public String getFixedFont() {
		if (fFChanged)
			return fixedFont;
		else
			return null;
	}

	public String getGeneralFont() {
		if (gFChanged)
			return generalFont;
		else
			return null;
	}
	
	void done() {
		popup.setVisible(false);
		frame.repaint();
	}
	
	public void valueChanged(ListSelectionEvent e) {
				
		//if (e.getValueIsAdjusting() == false) {
			
			@SuppressWarnings("unchecked")
			JList<String> l = (JList<String>) e.getSource();
			
			int i = l.getSelectedIndex();
			if (i > -1) {

				if (l == jlF) { 
					fixedFont = fixedFonts[i];
					fFChanged = true;
					MyOptionPane
					.showMessageDialog(frame, "Font '" + fixedFont 
							+ "' selected as font for fixed-size characters.");
					lb1.setText("Fixed Fonts (" + fixedFont + ")");
					lb1.repaint();
				}
				else {
					generalFont = generalFonts[i];
					gFChanged = true;
					MyOptionPane
						.showMessageDialog(frame, "Font '" + generalFont
								+ "' selected as font for general characters.");
					lb2.setText("General Fonts (" + generalFont + ")");
					lb2.repaint();
				}
								
				popup.repaint();
				//popup.setVisible(false);
				
			}
		//}
	}

	
	

	class CellRenderer implements ListCellRenderer<Object> {

		//private static final long serialVersionUID = 1L;

		public Component getListCellRendererComponent(JList<?> list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			JPanel jp = new JPanel();
			BoxLayout gb = new BoxLayout(jp, BoxLayout.X_AXIS);
			jp.setLayout(gb);
			jp.setBackground(Color.WHITE);

			Dimension minSize2;
			Dimension prefSize2;
			Dimension maxSize2;
			Dimension minSize = new Dimension(150, 15);
			Dimension prefSize = new Dimension(150, 15);
			Dimension maxSize = new Dimension(Short.MAX_VALUE, 15);
			JLabel lab1 = new JLabel((String) value);
			lab1.setFont(new Font("Arial", Font.PLAIN, driver.defaultFontSize));
			lab1.setMinimumSize(minSize);
			lab1.setMaximumSize(maxSize);
			lab1.setPreferredSize(prefSize);

			jp.add(lab1);
			minSize2 = new Dimension(20, 15);
			prefSize2 = new Dimension(20, 15);
			maxSize2 = new Dimension(Short.MAX_VALUE, 15);
			jp.add(new Box.Filler(minSize2, prefSize2, maxSize2));

			JLabel lab2 = new JLabel("Sample English Text \\");
			lab2.setFont(new Font((String) value, Font.PLAIN,
					driver.defaultFontSize));
			minSize = new Dimension(150, 15);
			prefSize = new Dimension(150, 15);
			lab2.setMinimumSize(minSize);
			lab2.setMaximumSize(maxSize);
			lab2.setPreferredSize(prefSize);

			jp.add(lab2);
			jp.add(new Box.Filler(minSize2, prefSize2, maxSize2));

			JLabel lab3 = new JLabel("\u4e2d\u6587\u6587\u672c\u793a\u4f8b"); // Chinese
			// for
			// "Sample Chinese text"

			lab3.setFont(new Font((String) value, Font.PLAIN,
					driver.defaultFontSize));
			minSize = new Dimension(100, 15);
			prefSize = new Dimension(100, 15);
			lab3.setMinimumSize(minSize);
			lab3.setMaximumSize(maxSize);
			lab3.setPreferredSize(prefSize);

			jp.add(lab3);
			Color vLightBlue = new Color(220, 235, 255);			
			
			if (isSelected) { 
				jp.setBackground(vLightBlue);
			}
			return jp;
		}
	}

	
	public void windowClosed(WindowEvent arg0) {
		popup.setVisible(false);

	}

	
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		//jl.repaint();
		//ls.repaint();

	}

	
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	
	public void windowClosing(WindowEvent e) {
		popup.setVisible(false);

	}

	
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	

}
