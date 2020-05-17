package com.jpaulmorrison.graphics;

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

//import com.jpaulmorrison.graphics.DrawFBP.FontType;

public class MyFontChooser implements ListSelectionListener, WindowListener {

	//private static final long serialVersionUID = 1L;
	//JFrame frame;
	// Graphics2D osg;
	DrawFBP driver;
	//FontType ft = null;
	//String result;
	JDialog jDialog;
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
		//frame = frm;
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

		jDialog = new JDialog(driver, Dialog.ModalityType.APPLICATION_MODAL);
		jDialog.setFocusable(true);
		DrawFBP.applyOrientation(jDialog);
		jDialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				jDialog.dispose();
			}
			// }
		});
		//Dimension dim = driver.getSize();
		//jDialog.setPreferredSize(dim);
		jDialog.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ev) {
				if (ev.getKeyCode() == KeyEvent.VK_ESCAPE) {
					jDialog.dispose();
				}
			}
		});
		//fontDesc = (ft == FontType.GENERAL) ? "general text" : "fixed width";
		
		

		JPanel p = new JPanel();
		Box b1 = new Box(BoxLayout.Y_AXIS);
		Box b2 = new Box(BoxLayout.Y_AXIS);
				
		prompt = "Select fonts for fixed and/or general characters";
		jDialog.setTitle(prompt);
		DrawFBP.applyOrientation(jDialog);

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
		lsF.setPreferredSize(new Dimension(800, 800));
		lsG = new JScrollPane(jlG);
		lsG.setPreferredSize(new Dimension(800, 800));
		
		fixedFont = driver.fixedFont;
		lb1 = new JLabel("Fixed Fonts (current: " + fixedFont + ")");
		b1.add(lb1);
		b1.add(lsF);
		jlF.setSelectedValue(fixedFont, true); 
		
		generalFont = driver.generalFont;
		lb2 = new JLabel("General Fonts (current: " + generalFont + ")");
		b2.add(lb2);
		b2.add(lsG);
		jlG.setSelectedValue(generalFont, true); 		
		
		p.add(b1);
		p.add(Box.createRigidArea(new Dimension(10, 0)));
		p.add(b2);
				
		jDialog.add(p);

		//popup.setBounds(100, 100, 800, 800);

		jlF.addListSelectionListener(this);
		jlG.addListSelectionListener(this);

		lsF.revalidate();
		lsG.revalidate();
		jDialog.revalidate();
		lsF.repaint();
		lsG.repaint();

		//popup.pack();
		jDialog.setLocation(50, 100);
		jDialog.pack();
		//frame.pack();
		 
		//jl.setVisible(true);
		//ls.setVisible(true);
		jDialog.setVisible(true);
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
		jDialog.setVisible(false);
		driver.repaint();
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
					.showMessageDialog(driver, "Font '" + fixedFont 
							+ "' selected as font for fixed-size characters.");
					lb1.setText("Fixed Fonts (current: " + fixedFont + ")");
					lb1.repaint();
				}
				else {
					generalFont = generalFonts[i];
					gFChanged = true;
					MyOptionPane
						.showMessageDialog(driver, "Font '" + generalFont
								+ "' selected as font for general characters.");
					lb2.setText("General Fonts (current: " + generalFont + ")");
					lb2.repaint();
				}
								
				jDialog.repaint();
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

			//Dimension minSize2;
			//Dimension prefSize2;
			//Dimension maxSize2;
			Dimension minSize = new Dimension(120, 15);
			Dimension prefSize = new Dimension(200, 15);
			Dimension maxSize = new Dimension(Short.MAX_VALUE, 15);
			JLabel lab1 = new JLabel((String) value);
			lab1.setFont(new Font("Arial", Font.PLAIN, (int) driver.defaultFontSize));
			lab1.setMinimumSize(minSize);
			lab1.setMaximumSize(maxSize);
			lab1.setPreferredSize(prefSize);
			jp.add(lab1);
			
			Dimension minSize2 = new Dimension(10, 15);
			Dimension prefSize2 = new Dimension(10, 15);
			Dimension maxSize2 = new Dimension(10, 15);
			jp.add(new Box.Filler(minSize2, prefSize2, maxSize2));

			JLabel lab2 = new JLabel("Sample English Text \\");
			lab2.setFont(new Font((String) value, Font.PLAIN,
					(int) driver.defaultFontSize));
			minSize = new Dimension(150, 15);
			prefSize = new Dimension(150, 15);
			lab2.setMinimumSize(minSize);
			lab2.setMaximumSize(maxSize);
			lab2.setPreferredSize(prefSize);

			jp.add(lab2);
			jp.add(new Box.Filler(minSize2, prefSize2, maxSize2));
			
			
			JLabel lab3 = new JLabel("\u0420\u0443\u0441\u0441\u043a\u0438\u0439 " +
			"\u043e\u0431\u0440\u0430\u0437\u0435\u0446 " +
			"\u0442\u0435\u043a\u0441\u0442\u0430");
		    //  Russian for "Russian text sample"
			lab3.setFont(new Font((String) value, Font.PLAIN,
					(int) driver.defaultFontSize));
			minSize = new Dimension(150, 15);
			prefSize = new Dimension(150, 15);
			lab3.setMinimumSize(minSize);
			lab3.setMaximumSize(maxSize);
			lab3.setPreferredSize(prefSize);

			jp.add(lab3);
			jp.add(new Box.Filler(minSize2, prefSize2, maxSize2));
			
			JLabel lab4 = new JLabel("\u0939\u093f\u0902\u0926\u0940 \u092a\u093e\u0920 \u0928\u092e\u0942\u0928\u093e");
		    //  Hindi for "Hindi text sample"
			lab4.setFont(new Font((String) value, Font.PLAIN,
					(int) driver.defaultFontSize));
			minSize = new Dimension(100, 15);
			prefSize = new Dimension(100, 15);
			lab4.setMinimumSize(minSize);
			lab4.setMaximumSize(maxSize);
			lab4.setPreferredSize(prefSize);

			jp.add(lab4);
			jp.add(new Box.Filler(minSize2, prefSize2, maxSize2));
		
			JLabel lab5 = new JLabel("\u65e5\u672c\u8a9e\u306e\u30c6\u30ad\u30b9\u30c8\u4f8b");
		    //  Japanese for "japanese text sample"
			lab5.setFont(new Font((String) value, Font.PLAIN,
					(int) driver.defaultFontSize));
			minSize = new Dimension(150, 15);
			prefSize = new Dimension(150, 15);
			lab5.setMinimumSize(minSize);
			lab5.setMaximumSize(maxSize);
			lab5.setPreferredSize(prefSize);

			jp.add(lab5);
			/*
			jp.add(new Box.Filler(minSize2, prefSize2, maxSize2));

			JLabel lab5 = new JLabel("\u4e2d\u6587\u6587\u672c\u793a\u4f8b");  			
			//  Chinese for "Chinese text sample"
			lab5.setFont(new Font((String) value, Font.PLAIN,
					(int) driver.defaultFontSize));
			minSize = new Dimension(100, 15);
			prefSize = new Dimension(100, 15);
			lab5.setMinimumSize(minSize);
			lab5.setMaximumSize(maxSize);
			lab5.setPreferredSize(prefSize);

			jp.add(lab5);
			*/
			Color vLightBlue = new Color(220, 235, 255);			
			
			if (isSelected) { 
				jp.setBackground(vLightBlue);
			}
			return jp;
		}
	}

	
	public void windowClosed(WindowEvent arg0) {
		jDialog.setVisible(false);

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
		jDialog.setVisible(false);

	}

	
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	

}
