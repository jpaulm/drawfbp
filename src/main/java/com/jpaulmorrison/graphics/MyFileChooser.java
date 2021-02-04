package com.jpaulmorrison.graphics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import com.jpaulmorrison.graphics.DrawFBP.Lang;


public class MyFileChooser extends JDialog 
		implements
			MouseListener,
			ActionListener,
			//ListSelectionListener,
			KeyListener
			 {

	private static final long serialVersionUID = 1L;
	// private static final DrawFBP DrawFBP = null;
	public static int APPROVE_OPTION = 0;
	public static int CANCEL_OPTION = 1;
	//boolean sortByDate = false;   // default is sort by name

	// FileFilter filter = null;
	JDialog dialog = null;
	// String prompt = "";
	// int type;

	// JFrame frame;
	JList<String> list = null;
	String listHead = null;
	String listShowingSpecHdr = null;
	boolean inTree = false;
	JScrollPane listView = null;
	JPanel panel = null;
	int result = CANCEL_OPTION;
	
	//DefaultMutableTreeNode fbpJsonTree = new DefaultMutableTreeNode();

	DrawFBP driver = null;
	MyButton butParent = new MyButton(null, "parent");
	MyButton butOK = new MyButton(null, "OK");
	MyButton butFind = new MyButton(null, "Find");
	MyButton butCancel = new MyButton(null, "cancel");
	MyButton butDel = new MyButton(null, "delete");
	MyButton butNF = new MyButton(null, "new folder");
	JCheckBox butSortByDate = new JCheckBox("Sort ByDate");

	MyButton butUseSugg = new MyButton(null, "useSugg");

	MyTextField t_dirName = new MyTextField(100, "dir");
	MyTextField t_fileName = new MyTextField(100, "file");
	MyTextField t_suggName = new MyTextField(100, "suggName");

	JComponent selComp = null;
	// Component changedField = null;
	MyTraversalPolicy mtp;
	DefaultMutableTreeNode jarTree;
	DefaultMutableTreeNode currentNode;
	String folder;

	String[] nodeNames = null;
	int rowNo = -1;
	String suggestedName = null;

	boolean clickState = true;
	// String fileExt = null;
	//boolean shift = false;
	Color slateGray1 = new Color(198, 226, 255);
	Color vLightBlue = new Color(220, 235, 255);
	// Color lightBlue = new Color(135, 206, 250);
	Color lightBlue = new Color(160, 220, 250);
	//Color paleGreen = new Color(209, 253, 209);

	// String title;

	MyComboBox cBox = null;

	boolean saveAs;
	boolean saving;
	Vector<Component> order = null;

	// Point mLoc = null;
	// MyComboBox cBox = null;
	ListRenderer renderer;

	CancelAction cancelAction;
	DeleteAction deleteAction;
	EnterAction enterAction;
	FindAction findAction;
	SuggAction suggAction;

	ParentAction parentAction;
	NewFolderAction newFolderAction;

	//DrawFBP.FileChooserParm fCP;
	Lang lang;
	
	String chooserTitle;

	public ClickListener clickListener;
	
	//DefaultMutableTreeNode fbpJsonTree = null;

	public MyFileChooser(DrawFBP driver, File f, Lang lang, String chooserTitle) {
		

		this.lang = lang;
		this.chooserTitle = chooserTitle; 
		clickListener = new ClickListener();

		if (f == null || !f.exists() /* || !f.isDirectory() */)
			listHead = System.getProperty("user.home");
		else if (!f.isDirectory())
			listHead = f.getParent();
		else
			listHead = f.getAbsolutePath();
	
		this.driver = driver;		
				
		butSortByDate.setSelected(driver.sortByDate);
		//Dimension dim = driver.getContentPane().getPreferredSize();
		//setPreferredSize(new Dimension(1400, 1000));
		Point p = driver.getLocation();
		setLocation(p.x + 50, p.y + 30);
		
		driver.fbpJsonTree = null;
		
	}

	int showOpenDialog(final boolean saveas, final boolean saving) {
		
		if (driver.currNotn == driver.notations[DrawFBP.Notation.JSON] &&
				lang != driver.langs[DrawFBP.Lang.FBPJSON] &&
				driver.fbpJsonFile == null)  {
			if (MyOptionPane.YES_OPTION == MyOptionPane.showConfirmDialog(driver,
					"Do you want to use fbp.json - if so, exit and execute File/Locate fbp.json File first", 
					"Use fbp.json?", MyOptionPane.YES_NO_OPTION))  
				return CANCEL_OPTION;
			 
		}

		dialog = new JDialog(driver, Dialog.ModalityType.APPLICATION_MODAL);
		
		this.saveAs = saveas;
		this.saving = saving;
		
		dialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dialog.dispose();
			}
		});
		
		//selComp = null;
		selComp = t_fileName;    //experimental

		DrawFBP.applyOrientation(dialog);

		panel = new JPanel();
		panel.setLayout(new BorderLayout());

		driver.filterOptions[0] = lang.filter.getDescription();
		
		cBox = new MyComboBox(driver.filterOptions);
		cBox.setMaximumRowCount(2);
		cBox.addMouseListener(this);
		cBox.setSelectedIndex(driver.allFiles ? 1 : 0);

		order = new Vector<Component>(10);
		//order.add(t_dirName);
		order.add(butSortByDate);
		order.add(butFind);
		order.add(butParent);
		order.add(butNF);
		order.add(panel); // just a place-holder - will be filled in by
							// buildList
		order.add(t_fileName);
		order.add(butUseSugg);
		order.add(butOK);
		
		order.add(cBox);
		order.add(butDel);
		order.add(butCancel);

		if (driver.currNotn == driver.notations[DrawFBP.Notation.JSON] &&
				driver.fbpJsonFile != null && driver.fbpJsonTree == null) 	
			driver.buildFbpJsonTree(driver.fbpJsonFile);	 
		
		t_fileName.setEditable(true);
		t_fileName.setEnabled(true);
		t_fileName.setRequestFocusEnabled(true);
		t_fileName.setBackground(vLightBlue);
		t_fileName.getCaret().setVisible(true);
		t_fileName.addActionListener(this);
		t_fileName.addMouseListener(this);
		t_fileName.setText(new File(listHead).getName() + "/");

		t_fileName.setPreferredSize(new Dimension(100, driver.gFontHeight + 2));

		t_suggName.setEditable(false);
		t_suggName.setEnabled(true);
		// text3.setRequestFocusEnabled(true);
		t_suggName.setFont(driver.fontg.deriveFont(Font.ITALIC));
		// text3.setPreferredSize(new Dimension(100, driver.fontHeight + 2));

		//String s = (saveAs) ? "Save or Save As" : "Open File";
		// comp = new MyFileCompare();
		renderer = new ListRenderer(driver);

		//if (fCP == driver.diagFCParm)
		//	dialog.setTitle(s);
		//else {
			//if (fCP == driver.curDiag.fCParm[Diagram.NETWORK]) {
				//String w = driver.curDiag.diagFile.getAbsolutePath();
				//fCP.prompt = "Specify file name";
			//}

			//dialog.setTitle(fCP.prompt);
			dialog.setTitle(chooserTitle);
			if (lang == driver.langs[DrawFBP.Lang.CLASS])
				listShowingSpecHdr = listHead;
			if (lang == driver.langs[DrawFBP.Lang.JS] || 
					driver.currNotn == driver.notations[DrawFBP.Notation.JSON])
				listShowingSpecHdr = listHead;
		//}

		enterAction = new EnterAction();
		suggAction = new SuggAction();
		cancelAction = new CancelAction();
		deleteAction = new DeleteAction();
		findAction = new FindAction();
		parentAction = new ParentAction();
		newFolderAction = new NewFolderAction();
		
		//butSortByDate = new JCheckBox("Sort By Date");
		//butSortByDate.setActionCommand("Toggle Click to Grid");
		butSortByDate.addActionListener(this);
		butSortByDate.setBackground(slateGray1);
		butSortByDate.setBorderPaintedFlat(false);
		butSortByDate.setActionCommand("Toggle Sort By Date");

		butParent.setAction(parentAction);
		butParent.setText("Parent");
		butParent.setMnemonic(KeyEvent.VK_P);

		butNF.setAction(newFolderAction);
		butNF.setMnemonic(KeyEvent.VK_N);
		butNF.setText("New Folder");

		// butNF.setEnabled(false);

		// butOK.setAction(okAction);
		butOK.setAction(enterAction);
		butFind.setAction(findAction);
		butUseSugg.setAction(suggAction);
		butCancel.setAction(cancelAction);
		butDel.setAction(deleteAction);

		butFind.setRequestFocusEnabled(true);
		butParent.setRequestFocusEnabled(true);
		//if (saveAs)
			butNF.setRequestFocusEnabled(true);
		butUseSugg.setRequestFocusEnabled(true);

		//t_dirName.addMouseListener(this);
		//t_fileName.addMouseListener(this);

		panel.setPreferredSize(new Dimension(600, 600));

		//t_dirName.setFocusTraversalKeysEnabled(false);
		butParent.setFocusTraversalKeysEnabled(false);
		butSortByDate.setFocusTraversalKeysEnabled(false);
		//if (saveAs)
			butNF.setFocusTraversalKeysEnabled(false);
		t_fileName.setFocusTraversalKeysEnabled(false);
		butOK.setFocusTraversalKeysEnabled(false);
		butFind.setFocusTraversalKeysEnabled(false);
		butDel.setFocusTraversalKeysEnabled(false);
		butCancel.setFocusTraversalKeysEnabled(false);
		butUseSugg.setFocusTraversalKeysEnabled(false);

		
		// if (saveAs)
		butNF.setEnabled(true);
		butOK.setEnabled(true);
		butFind.setEnabled(true);
		// butCopy.setEnabled(saveAs);
		butUseSugg.setEnabled(true);
		butCancel.setEnabled(true);
		butDel.setEnabled(true);

		KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(escape, "CLOSE");

		panel.getActionMap().put("CLOSE", cancelAction);

		JLabel label = new JLabel("Current folder: ");
		label.setFont(driver.fontg);

		Box box0 = new Box(BoxLayout.Y_AXIS);
		Box box1 = new Box(BoxLayout.X_AXIS);

		box1.add(label);

		box1.add(Box.createRigidArea(new Dimension(12, 0)));
		box1.add(t_dirName);
		box1.add(Box.createRigidArea(new Dimension(6, 0)));
		box1.add(butSortByDate);
		box1.add(Box.createRigidArea(new Dimension(6, 0)));
		box1.add(butFind);
		box1.add(Box.createRigidArea(new Dimension(6, 0)));

		box1.add(butParent);
		// butParent.addActionListener(this);
		box1.add(Box.createRigidArea(new Dimension(6, 0)));

		butNF.addActionListener(this);
		box1.add(butNF);
		// box1.add(butOK);

		box0.add(Box.createRigidArea(new Dimension(0, 20)));
		box0.add(box1);

		box0.add(Box.createRigidArea(new Dimension(0, 20)));
		panel.add(box0, BorderLayout.NORTH);

		t_dirName.setFont(label.getFont());
		//t_dirName.addActionListener(this);
		//t_fileName.addActionListener(this);
		//t_dirName.addKeyListener(this);

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;

		JPanel pan2 = new JPanel();

		pan2.setLayout(gridbag);
		// c.fill = GridBagConstraints.BOTH;

		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.0;

		JLabel lab1 = new JLabel("File name: ");
		lab1.setFont(driver.fontg);
		gridbag.setConstraints(lab1, c);
		pan2.add(lab1);

		c.gridx = 1;
		c.weightx = 0.0;
		JLabel lab5 = new JLabel("  ");
		gridbag.setConstraints(lab5, c);
		pan2.add(lab5);

		c.gridx = 2;

		c.weightx = saveAs ? 0.1 : 1.0;
		c.gridwidth = saveAs ? 1 : 3;
		// c.ipadx = saveAs ? -20: 0;
		gridbag.setConstraints(t_fileName, c);
		pan2.add(t_fileName);

		if (saveAs) {
			c.gridx = 3;
			c.weightx = 0.0;
			c.gridwidth = 1;
			JLabel lab6 = new JLabel("   Suggestion: ");
			lab6.setFont(driver.fontg);
			gridbag.setConstraints(lab6, c);
			pan2.add(lab6);

			c.gridx = 4;
			c.weightx = 0.9;
			// c.ipadx = 20;
			gridbag.setConstraints(t_suggName, c);
			pan2.add(t_suggName);
			t_suggName.setBackground(Color.WHITE);
			Dimension dim2 = t_suggName.getPreferredSize();
			t_suggName.setPreferredSize(
					new Dimension(driver.gFontWidth * 25, dim2.height));
		}

		c.gridx = 5;
		c.weightx = 0.0;
		JLabel lab7 = new JLabel("  ");
		gridbag.setConstraints(lab7, c);
		pan2.add(lab7);

		c.gridx = 6;
		c.weightx = 0.0;

		if (saveAs) {
			c.gridwidth = 1;
			gridbag.setConstraints(butUseSugg, c);
			pan2.add(butUseSugg);

			c.gridx = 7;
			c.weightx = 0.0;

			c.gridwidth = 1;
		} else
			c.gridwidth = 2;
		
		gridbag.setConstraints(butOK, c);
		pan2.add(butOK);

		c.weightx = 0.0;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;

		JLabel lab2 = new JLabel("Files of type:");
		lab2.setFont(driver.fontg);
		gridbag.setConstraints(lab2, c);
		pan2.add(lab2);

		c.gridx = 1;
		c.weightx = 0.0;
		JLabel lab8 = new JLabel("  ");
		gridbag.setConstraints(lab8, c);
		pan2.add(lab8);

		c.gridx = 2;
		c.weightx = 1.0;
		c.gridwidth = 3;
		gridbag.setConstraints(cBox, c);
		pan2.add(cBox);
		cBox.addActionListener(this);

		c.gridx = 5;
		c.weightx = 0.0;
		c.gridwidth = 1;
		JLabel lab9 = new JLabel("  ");
		gridbag.setConstraints(lab9, c);
		pan2.add(lab9);

		c.gridx = 6;
		c.weightx = 0.0;
		gridbag.setConstraints(butDel, c);
		pan2.add(butDel);

		c.gridx = 7;
		c.weightx = 0.0;
		gridbag.setConstraints(butCancel, c);
		pan2.add(butCancel);

		butOK.setText("OK");
		butOK.setFont(driver.fontg.deriveFont(Font.BOLD));
		butFind.setText("Find");
		butCancel.setText("Cancel");
		butDel.setText("Delete");
		butUseSugg.setText(saveAs ? "Use suggested name" : "");

		JLabel lab3 = new JLabel();
		lab3.setPreferredSize(new Dimension(500, 30));
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 4;
		c.weightx = 1.0;
		gridbag.setConstraints(lab3, c);
		pan2.add(lab3);

		//cBox.addActionListener(this);

		// cBox.setUI(new BasicComboBoxUI());
		cBox.setRenderer(new ComboBoxRenderer());

		// Dimension dim = new Dimension(1000, 800);
		// dialog.setPreferredSize(dim);

		dialog.setFocusTraversalKeysEnabled(false);
		
		// Following statements needed to service tab keys
		
		//t_dirName.addKeyListener(this);   
		t_fileName.addKeyListener(this);   
		t_suggName.addKeyListener(this);   
		butSortByDate.addKeyListener(this);  
		butParent.addKeyListener(this); 
		butNF.addKeyListener(this);  
		butOK.addKeyListener(this);  
		butFind.addKeyListener(this);  
		cBox.addKeyListener(this);  
		butDel.addKeyListener(this);  
		butCancel.addKeyListener(this);  
		butUseSugg.addKeyListener(this);  
		
		cBox.setFocusTraversalKeysEnabled(false);
		mtp = new MyTraversalPolicy();
		setFocusTraversalPolicy(mtp);
		setFocusCycleRoot(false);
		
		selComp = t_fileName;    // experimental

		if (saveAs) {
			
			if (suggestedName != null && !(suggestedName.equals(""))) {
				// check if valid file name 
				File h = new File(suggestedName);
				if (h.getParent() != null && h.getParentFile().exists())
					listHead = h.getParent();
				//t_dirName.setText(listHead);
				showListHead();
				//t_fileName.setText(h.getName());
				String w = h.getName();
				w = w.replace("\\",  File.separator);
				w = w.replace("/",  File.separator);
				t_suggName.setText(w);

				t_fileName.addAncestorListener(new RequestFocusListener(false));
				selComp = t_fileName;

			}

			showListHead();
			
			//if (driver.curDiag.title != null
			//		&& driver.curDiag.diagFile != null) {
			//	s += " (current file: "
			//			+ driver.curDiag.diagFile.getAbsolutePath() + ")";
			//}
			showList();
		} else {
			showListHead();
			showList();
			if (list != null) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						list.requestFocusInWindow();
						//selComp = list;
						// list.setBackground(vLightBlue);
						list.setBackground(Color.WHITE);
					}
				});

				list.addAncestorListener(new RequestFocusListener());
				selComp = list;
			}
		}

		panel.add(pan2, BorderLayout.SOUTH);
		Component vertStrip = Box.createRigidArea(new Dimension(20, 0));
		Component vertStrip2 = Box.createRigidArea(new Dimension(20, 0));
		//vertStrip.setMinimumSize(new Dimension(20,600));
		panel.add(vertStrip, BorderLayout.WEST);
		panel.add(vertStrip2, BorderLayout.EAST);
		dialog.add(panel);

		Point p = driver.getLocation();
		Dimension dim = driver.getSize();
		int x_off = 100;
		int y_off = 100;
		dialog.setPreferredSize(
				new Dimension(dim.width - x_off - 50, dim.height - y_off - 50));
		//dialog.setPreferredSize(new Dimension(1000, 700));
		dialog.pack();
		dialog.setLocation(p.x + x_off, p.y + y_off);
		// frame.pack();

		dialog.setVisible(true); 
		// if (!saveAs)
		// textBackground = Color.WHITE;

		repaint();

		return result;
	}
	int showOpenDialog() {
		return showOpenDialog(false, false);
	}

	void showListHead() {
		listHead = listHead.replace("\\",  File.separator);
		listHead = listHead.replace("/",  File.separator);		
		t_dirName.setText(listHead);
	}
	
	void getSelectedFile(String[] s) {
		
		String t = t_fileName.getText();
		String u = t_dirName.getText();
		
		if (driver.currNotn == driver.notations[DrawFBP.Notation.JSON]) {
			u = u.replace("\\",  "/");
			while (true) {	
				int i = t.indexOf("/modules");
				if (i > -1) {
					t = t.substring(0, i) + t.substring(i + 8);
					continue;
				}
				i = u.indexOf("/components");
				if (i > -1) {
					t = t.substring(0, i) + t.substring(i + 11);
					continue;
				}
				break;
			}
			
			while (true) {
				int i = u.indexOf("/modules");
				if (i > -1) {
					u = u.substring(0, i) + u.substring(i + 8);
					continue;
				}
				i = u.indexOf("/components");
				if (i > -1) {
					u = u.substring(0, i) + u.substring(i + 11);
					continue;
				}
				break;
			}
		}

		s[0] = DrawFBP.makeAbsFileName(t, u);
		dialog.dispose();
		return;
	}

	void setSuggestedName(String s) {
		suggestedName = s;
	}

	@SuppressWarnings("unchecked")
	private void showList() {

		LinkedList<String> ll = new LinkedList<String>();
		LinkedList<String> ll2 = null;
		inTree = false;
		String s = listHead;

		// String x = t_dirName.getText();
		// t_dirName.setVisible(true);

		String t = null;
		File f = new File(listHead);
		//if (s.toLowerCase().endsWith("fbp.json")) {
		if (0 == 1)	{
			//ll2 = driver.buildTreeFromJsonList(s);
			inTree = true;
			currentNode = driver.fbpJsonTree;
			

			// fullNodeName = s;
			// showFileNames();
		} else {
			if (-1 == s.indexOf("!")) { // if fullNodeName is NOT a
										// file
										// within a jar file or fbp.json...

				if (listHead == null)
					return;
				// if (!(f.exists()))
				// return;
				// File f = new File(listHead);
				t = f.getAbsolutePath();
				if (t.endsWith("My Documents"))
					f = new File(t.replace("My Documents", "Documents"));
				t = t.replace("\\", "/");
				if (!f.exists() || !f.isDirectory())
					ll.add("Folder does not exist or is not directory");
				// return;
			} else {
				inTree = true;
			}

			// else {
			// return;

			t = null;
			
			if (!inTree) {
				if (listHead.equals(listShowingSpecHdr)) {    
					String v = "";
					if (driver.currNotn == driver.notations[DrawFBP.Notation.JSON] && 
							driver.fbpJsonTree != null) {
						t = driver.fbpJsonFile;
						//inTree = true;
						//currentNode = driver.fbpJsonTree;
						//return;
					}
					else
						t = driver.javaFBPJarFile;
					
					if (t != null) {

						try {
							File f2 = new File(t);
							v = Files.getLastModifiedTime(f2.toPath())
									.toString();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						int i = v.lastIndexOf(".");
						if (i > -1)
							v = v.substring(0, i);
						v = v.replace("T", " ");
						ll.add(t + "@" + v);
					}					
				 
					if (driver.currNotn == driver.notations[DrawFBP.Notation.JAVA_FBP]) {
						for (String u : driver.jarFiles) {
							if (new File(u).exists()) {
								try {
									File f2 = new File(u);
									v = Files.getLastModifiedTime(f2.toPath()).toString();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								int i = v.lastIndexOf(".");
								if (i > -1)
									v = v.substring(0, i);
								v = v.replace("T", " ");
								ll.add(u + "@" + v);
							}
						}
					}
				}
				

				//Path p = f.toPath();
				if (!f.exists()) {
					MyOptionPane.showMessageDialog(driver,
							"File does not exist: " + f.getAbsolutePath(),
							MyOptionPane.ERROR_MESSAGE);

				}

				else {

					//DirectoryStream<Path> ds = null;
					//try {
					//	ds = Files.newDirectoryStream(p);
					//} catch (IOException e) {
						// TODO Auto-generated catch block
					//	e.printStackTrace();
					//}
					
					File[] ds = f.listFiles();
					ll2 = new LinkedList<String>();
					// if (ds != null) {
					 //  for (File child : ds) {
					//      // Do something with child
					//    }

					

					 if (ds != null) {
						    for (File entry : ds) {
						String fs = entry.toString();		
						//System.out.println(fs);


						File fx = new File(fs);
						if (!fx.exists())
							continue;
						if (fx.isDirectory())
							ll2.add(fs); // directories go into ll first

					}
					ll.addAll(mySort(ll2)); // add elements of ll2 to ll in
											// sorted
											// order
					 
					ll2.clear();
					//try (DirectoryStream<Path> ds2 = Files.newDirectoryStream(p))) 
					
					//try {
					//	ds = Files.newDirectoryStream(p);
					//} catch (IOException e) {
						// TODO Auto-generated catch block
					//	e.printStackTrace();
					//}
					//ds = f.listFiles();

					//if (ds != null) {
					    for (File entry : ds) {
				
						String fs = entry.toString();
						//System.out.println("-" + fs);

						File fx = new File(fs);
						if (!fx.exists())
							continue;
						if (fx.isDirectory())
							continue;
								
						if (lang.filter.accept(fx) || driver.allFiles) 
							ll2.add(entry.toString()); // non-directories go
														// into ll2,
						// which is
						// then sorted into ll

					}
 
					
				//} catch (IOException e) {
					// TODO Auto-generated catch block
				//	e.printStackTrace();
				//}
				
				ll.addAll(mySort(ll2)); // add elements of ll2 to end of ll
					    }
				}

			} else {
				
				// must be in tree
				
				
				if (currentNode == null)
					return;

				ll = new LinkedList<String>();

				ll2 = new LinkedList<String>();

				if (currentNode.getChildCount() > 0) {

					showListHead();
					Enumeration<TreeNode> e = currentNode
							.children();

					while (e.hasMoreElements()) {
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) (e.nextElement());
						t = (String) node.getUserObject();
						if (t != null)
							ll2.add((String) t);
					}
					ll.addAll(mySort(ll2)); // add elements of ll2 to end of ll - sorted
					//ll.addAll(ll2); // add elements of ll2 to end of ll - don't sort!
											
				} else {

					selComp = t_fileName;
				}
			}
			
			
			if (ll.size() == 0)  {
				//ll.add("No files match criteria");
				MyOptionPane.showMessageDialog(driver,
						"No files match criteria",
						MyOptionPane.INFORMATION_MESSAGE);
				//t_suggName.setEditable(true);
			}
			//else {

			Object[] oa = ll.toArray();

			//int k = 0;

			nodeNames = new String[oa.length];
			for (int j = 0; j < oa.length; j++) {
				if (oa[j] == null) // not sure where null came from, but it
									// crashed
									// one test!
					continue;
				nodeNames[j] = (String) oa[j];
				//if (nodeNames[j].endsWith(".jar"))
				//	k = k + 1; // get rid of spurious "unused" message
			}

			list = new JList<String>(nodeNames);
			// list.setSelectedIndex(k);
			//list.setSelectedIndex(-1);
			
			
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			list.addKeyListener(this);
			
			list.addMouseListener(clickListener); // note: list has its own
													// clicklistener
			
			ListSelectionModel listSelectionModel = list.getSelectionModel();
			listSelectionModel
					.addListSelectionListener(new SharedListSelectionHandler());

			list.setFocusTraversalKeysEnabled(false);

			
			order.remove(5);             //??????????????
			order.add(5, list);			

			FontMetrics metrics = driver.osg.getFontMetrics(driver.fontg);
			list.setFixedCellHeight(metrics.getHeight());

			list.setCellRenderer(renderer);
			list.setEnabled(true);

			if (!saveAs)
				list.addAncestorListener(new RequestFocusListener());
			
			if (listView != null)
				panel.remove(listView);
			
			listView = new JScrollPane(list);
			panel.add(listView, BorderLayout.CENTER);

			//if (selComp != t_dirName)
			//	selComp = list;
			
			list.setFocusable(true);

			list.setFixedCellHeight(22);

			list.setVisible(true);
			//list.requestFocusInWindow();
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					list.setSelectedIndex(0);    // select first row - will trigger list selection handler valueChanged
				}
			});
		//}
		}
		// dialog.pack();
		panel.validate();
		repaint();

		// frame.pack();
		listView.repaint();
		dialog.repaint();

		// panel.repaint();
		// frame.repaint();

	}

	void processOK() {

		result = APPROVE_OPTION;
		dialog.dispose();
		return;
	}
	
	
	final boolean SAVEAS = true;
	
	
	LinkedList<String> mySort(LinkedList<String> from) {
		
		// Collections.sort sorts in place - that's OK!		
		//int lhl = listHead.length();
		LinkedList<String> ll = new LinkedList<String>();
		for (String s : from) {
			//File f = new File(listHead + "/" + s); 
			
			if (!inTree) {
				String s2 = (new File(s)).getName();

				File f = new File(s);

				Path path = f.toPath();
				String t = "";
				try {
					t = Files.getLastModifiedTime(path).toString();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				int i = t.lastIndexOf(".");
				if (i > -1)
					t = t.substring(0, i);
				t = t.replace("T", " ");

				// if (!inJarTree)
				ll.add(s2 + "@" + t);
			} else
				ll.add(s);		 
		}
		
		if (driver.currNotn != driver.notations[DrawFBP.Notation.JSON]) {
			if (!inTree && driver.sortByDate)
				Collections.sort(ll, compDate);
			else
				Collections.sort(ll, compName);
		}
		
		return ll;
		
	}

	Comparator<String> compName = new Comparator<String>() {
		public int compare(String s1, String s2) {
			return s1.compareToIgnoreCase(s2);
			}
	};
	
	Comparator<String> compDate = new Comparator<String>() {
		public int compare(String s1, String s2) {
			String s1d = s1.substring(s1.indexOf("@") + 1);
			String s2d = s2.substring(s2.indexOf("@") + 1);
			return s2d.compareTo(s1d);   // sort in reverse sequence by date
			}
	};
	class ListRenderer extends JPanel implements ListCellRenderer<String> {
		static final long serialVersionUID = 111L;

		Dimension minSize;
		Dimension maxSize;
		Dimension prefSize;
		DrawFBP driver;
		JList<String> list; 

		public ListRenderer(DrawFBP driver) {
			this.driver = driver;
			//this.list= list;
			setOpaque(true);
		}

		public Component getListCellRendererComponent(
				JList<? extends String> list, String value, int index,
				boolean isSelected, boolean cellHasFocus) {
			Color goldenRod = new Color(255, 255, 224);
			Color bisque = new Color(255, 228, 196);
			String s = (String) value;
			Icon icon = driver.leafIcon;

			JPanel jp = new JPanel();
			
			BorderLayout gb = new BorderLayout();
			jp.setLayout(gb);			

			jp.setBackground(Color.WHITE);
			
			JLabel name = new JLabel();
			JLabel date = new JLabel();
			
			//if (driver.sortByDate) {
			//	jp.add(Box.createHorizontalGlue());				
			//}
			if (!inTree) {
				jp.add(date, BorderLayout.WEST);
				jp.add(Box.createHorizontalStrut(20));
				//jp.add(Box.createRigidArea(new Dimension(20, 0)));
				jp.add(name, BorderLayout.EAST);
			}
			else
				jp.add(name, BorderLayout.CENTER);
			//jp.add(Box.createHorizontalGlue());
			
			if (s == null || s.equals("(empty folder)"))
				icon = null;
			else if (s.toLowerCase().endsWith(".jar")) {
				icon = driver.jarIcon;
				File f = new File(s); 				
				Path path = f.toPath();
				String curDate = "";
				try {
					curDate = Files.getLastModifiedTime(path).toString();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
				s += "@" + curDate;	
			}
			else {
				if (currentNode == null) {
					File f = new File(listHead + "/" + s);
					if (f.isDirectory() || -1 == s.indexOf("."))
						icon = driver.folderIcon;
				} else {
					if (currentNode.getChildCount() > 0)
						icon = driver.folderIcon;
				}
				if (s.toLowerCase().endsWith(".java"))
					icon = driver.javaIcon;
				else if (s.toLowerCase().endsWith(".class"))
					icon = driver.classIcon;
				
			}
			name.setOpaque(true);
			date.setOpaque(true);
			
			Color ly2 = new Color(255, 255, 51);  // slightly more intense yellow
			if (isSelected) 
				date.setBackground(ly2);
			else
				date.setBackground(DrawFBP.ly);		
			
			if (selComp == list && isSelected){
				date.setBackground(Color.ORANGE);  // indicates when tab function points at list
			}
			
			if (s == null)
				name.setBackground(vLightBlue);

			else if (s.indexOf(".jar@") > -1 || inTree)
				name.setBackground(goldenRod);
			else
				name.setBackground(vLightBlue);

			if (isSelected) {
				if (s.indexOf(".jar@") > -1 || inTree)
					name.setBackground(bisque);
				else
					name.setBackground(lightBlue);
				// System.out.println("Selected " + index);
			}
			// }

			

			String blanks = "";
			for (int i = 0; i < 19; i++)
				blanks += " ";
			name.setText(s);
			date.setText(blanks);
			if (s != null && s.charAt(0) != ' ') {
				
				if (!inTree)
					date.setIcon(icon);
				else
					name.setIcon(icon);
				int i = s.indexOf("@");
				
				if (inTree) {
					name.setText(s);
					date.setText(blanks);
				}
				else  if (i > -1){
					name.setText(s.substring(0, i));	
					String t = s.substring(i + 1);  // drop the 'Z'
					//i = t.lastIndexOf(".");
					//if (i > -1)
					// = t.substring(0, i);
					//t = t.replace("T",  " ");
					date.setText(t);
				}
			}
			name.setFont(driver.fontg);
			
			minSize = new Dimension(400, 20);			
			
			int x = (int) (60 * driver.defaultFontSize);
			maxSize = new Dimension(x, 20);
			
			prefSize = maxSize;
			name.setPreferredSize(prefSize);
			
			name.setMinimumSize(minSize);
			
			
			return jp;
		}
	}

	class ComboBoxRenderer extends DefaultListCellRenderer {
		static final long serialVersionUID = 111L;

		@Override
		public Component getListCellRendererComponent(JList<?> list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			String s = (String) value;
			if (!s.startsWith("All"))
				value = driver.filterOptions[0];

			JLabel c = (JLabel) super.getListCellRendererComponent(list, value,
					index, isSelected, cellHasFocus);

			if (isSelected)
				c.setBackground(lightBlue);
			else
				c.setBackground(vLightBlue);

			return c;
		}
	}

	public void actionPerformed(ActionEvent e) {
		//t_dirName.setBackground(Color.WHITE);

		if (e.getSource() == t_fileName){
			selComp = t_fileName;  
		}
			
		//if (e.getSource() == t_dirName){
		//	selComp = t_dirName;  
		//}
		
		if (e.getSource() == butSortByDate){
			String s = e.getActionCommand();
			if (s.equals("Toggle Sort By Date")){
				driver.sortByDate = !driver.sortByDate;
				butSortByDate.setSelected(driver.sortByDate);
				driver.saveProp("sortbydate", Boolean.toString(driver.sortByDate));
				showList();
				repaint();
			}
		}
	
		if (e.getSource() == cBox) {

			int i = cBox.getSelectedIndex();
			driver.allFiles = (i == 1);			
			showList();
			selComp = cBox;
			cBox.requestFocusInWindow();
			cBox.setBackground(vLightBlue);
			// Component c = cBox.getComponent(1);
			// c.setBackground(vLightBlue);
			selComp.setFocusable(true);
			cBox.setEnabled(true);

		}

		repaint();
	}

	public void mouseClicked(MouseEvent e) {

		list.setSelectedIndex(-1);
		list.setRequestFocusEnabled(false);
		// changedField = null;
		// if previous value of selComp was a text field, make non-editable
		
		//if (selComp == t_dirName || selComp == t_fileName) {
		//	selComp.setBackground(Color.WHITE);
		//	((MyTextField) selComp).setEditable(false);
		//	((MyTextField) selComp).getCaret().setVisible(false);

		//}
         
		//t_fileName.setBackground(Color.WHITE);
		cBox.repaint();

		//if (selComp instanceof MyButton) {  // if previous selComp referred to button...
		//	((MyButton) selComp).setSelected(false);
		//	((MyButton) selComp).setFocusable(false);

		//}

		selComp = (JComponent) e.getSource();
		repaint();

		if (/* selComp == t_dirName || */ selComp == t_fileName) {

			//selComp.setBackground(vLightBlue);
			//selComp.setRequestFocusEnabled(true);
			//((MyTextField) selComp).getCaret().setVisible(true);
			//((MyTextField) selComp).setEditable(true);
			//((MyTextField) selComp).requestFocusInWindow();
			//((MyTextField) selComp).grabFocus();
			//String s = ((MyTextField) selComp).getText();
			//((MyTextField) selComp).setCaretPosition(s.length());

		} 
		//else {
			//selComp.setRequestFocusEnabled(true);

			//((MyTextField) selComp).setBackground(Color.WHITE);
			//((MyTextField) selComp).getCaret().setVisible(false);
			//((MyTextField) selComp).setEditable(false);
			//t_fileName.requestFocusInWindow();
		//
		//}
	
		//if (selComp == t_dirName)
		//	t_fileName.setText("");
		
		selComp.setRequestFocusEnabled(true);
		
		if (e.getSource() instanceof JList) {

			// shouldn't happen -- force a divide by zero!
			int div_by_0 = 0;
			div_by_0 /= div_by_0;

		}

		if (selComp == cBox) {
			selComp.setFocusable(true);
			cBox.requestFocusInWindow();
			cBox.setEnabled(true);
			cBox.setBackground(vLightBlue);
		}

		if (selComp instanceof MyButton) {
			((MyButton) selComp).setSelected(false);
		}
		//paintList();
		//selComp = t_fileName;
		//if (selComp == t_dirName) {
		//	showList();
			//list.repaint();
		//}
		repaint();
	}

	public void mouseEntered(MouseEvent e) {
		// selComp = (Component) e.getSource();

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {
		// selComp = (Component) e.getSource();

	}

	public void mouseReleased(MouseEvent e) {

	}

	public void keyPressed(KeyEvent e) {

				
		if (e.getKeyCode() == KeyEvent.VK_TAB) {
		
			cBox.repaint();
			

			if (selComp == cBox)
				cBox.setRequestFocusEnabled(false);
			
			if (selComp instanceof MyButton) 
				((MyButton) selComp).setSelected(false);
			 
			// selComp.setRequestFocusEnabled(false);
			selComp.setFocusable(false);

			// list.setSelectedIndex(-1);
			
			int onmask = InputEvent.SHIFT_DOWN_MASK;

			if ((e.getModifiersEx() & onmask) == onmask)
				selComp = (JComponent) mtp.getComponentBefore(dialog, selComp);
			else
				selComp = (JComponent) mtp.getComponentAfter(dialog, selComp);

			if (selComp == butUseSugg && !saveAs)  // skip butCopy if not saveAs
				if ((e.getModifiersEx() & onmask) == onmask)
					selComp = (JComponent) mtp.getComponentBefore(dialog,
							selComp);
				else
					selComp = (JComponent) mtp.getComponentAfter(dialog,
							selComp);

			if (/*selComp == t_dirName || */ selComp == t_fileName) {
				selComp.setBackground(vLightBlue);
				((MyTextField) selComp).getCaret().setVisible(true);
				((MyTextField) selComp).setEditable(true);
			}

			//if (selComp == null) {
			//	selComp = list;
			//}

			else if (selComp instanceof MyButton)
				((MyButton) selComp).setSelected(true);

			if (selComp instanceof MyComboBox) {

				cBox.setBackground(vLightBlue);
			}

			selComp.setFocusable(true);
			selComp.requestFocusInWindow();
			repaint();
			return;

		} 
		
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			//if (selComp instanceof JList /*|| selComp == t_dirName */
			//		|| selComp == t_fileName) {

				enterAction.actionPerformed(new ActionEvent(e, 0, ""));
			//}
			return;
		} 
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			// if (selComp instanceof JList) {

			cancelAction.actionPerformed(new ActionEvent(e, 0, ""));
			// }
			return;
		}

		if (e.getKeyCode() == KeyEvent.VK_DELETE) {
		 if (selComp instanceof JList) {
		// if (selComp == t_fileName) {

		 deleteAction.actionPerformed(new ActionEvent(e, 0, ""));
		 }
		 return;		 
		 }

		if (selComp == cBox && ((e.getKeyCode() == KeyEvent.VK_UP)
				&& driver.allFiles
				|| (e.getKeyCode() == KeyEvent.VK_DOWN) && !driver.allFiles)) {

			driver.allFiles = !driver.allFiles;
			cBox.setSelectedIndex(driver.allFiles ? 1 : 0);

			return;
		}
		
		if (selComp == t_fileName) {
			if (e.getKeyCode()  == KeyEvent.VK_UP)
				rowNo = Math.max(0, rowNo - 1);
			if (e.getKeyCode()  == KeyEvent.VK_DOWN)
				rowNo = Math.min(list.getModel().getSize() - 1, rowNo + 1);
			
			list.ensureIndexIsVisible(rowNo);
			
		}
		list.setSelectedIndex(rowNo);
		
		repaint();
	}

	public void keyReleased(KeyEvent e) {
		// (e.getKeyCode() == KeyEvent.VK_SHIFT) {
		//	shift = false;
		//}
		
	}

	
	public void keyTyped(KeyEvent e) {
		
	}
	
	
	class CancelAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			result = CANCEL_OPTION;
			dialog.dispose();
		}
	}

	class DeleteAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			result = CANCEL_OPTION;

			if (!(selComp instanceof JList) && selComp != t_fileName) {

				return;
			}
			String s = null;
			if (selComp instanceof JList) {
				// String s = t_dirName.getText();
				// String t = t_fileName.getText();
				// if (!(t.equals("")))
				// s += "/" + t;
				// File f = new File(s);

				rowNo = list.getSelectedIndex();
				if (nodeNames.length == 0 || rowNo == -1) {
					MyOptionPane.showMessageDialog(driver,
							"Empty directory or no entry selected",
							MyOptionPane.ERROR_MESSAGE);
					return;
				}

				s = nodeNames[rowNo];
			} else {
				s = t_fileName.getText();
			}
			
			int i = s.indexOf("@");
			if (i > -1)
				s = s.substring(0, i);
			
			if (s.endsWith(".jar")) {
				if (s.equals(driver.javaFBPJarFile)) {
					MyOptionPane.showMessageDialog(driver,
							"JavaFBP jar file cannot be deleted",
							MyOptionPane.ERROR_MESSAGE);
					return;
				}
			} else
				s = t_dirName.getText() + "/" + s;

			File f = new File(s);
			if (f.isDirectory()) {
				if (f.list().length > 0) {
					MyOptionPane.showMessageDialog(driver,
							"Folder '" + f.getName()
									+ "' not empty - cannot be deleted",
							MyOptionPane.ERROR_MESSAGE);

					return;
				}
			} else {
				if (-1 != driver.getFileTabNo(s)) {
					MyOptionPane.showMessageDialog(driver,
							"File '" + f.getName()
									+ "' cannot be deleted while open",
							MyOptionPane.ERROR_MESSAGE);

					return;
				}
			}
			
			String u = f.isDirectory() ? "folder" : "file";
			String v = "F" + u.substring(1);

			if (MyOptionPane.YES_OPTION != MyOptionPane.showConfirmDialog(
					dialog,
					"Do you want to delete this " + u + ": "
							+ f.getAbsolutePath() + "?",
					"File/folder delete", MyOptionPane.YES_NO_OPTION))
				return;

			if (s.endsWith(".jar")) {
				f = new File(s);
				String t = null;
				//for (Entry<String, String> entry : driver.jarFiles.entrySet()) {
				for (String entry : driver.jarFiles) {
					// (entry.getValue().equals(s)) {
					if (entry.startsWith(s)) {
						t = entry;
						break;
					}
				}
				if (t != null)
					driver.jarFiles.remove(t);
			} else
				listHead = f.getParent();

			if (!f.exists()) {
				MyOptionPane.showMessageDialog(driver,
						v + " " + f.getName() + " doesn't exist",
						MyOptionPane.ERROR_MESSAGE);
				// return;
			} else {
				f.delete();
				MyOptionPane.showMessageDialog(driver,
						v + " " + f.getName() + " deleted",
						MyOptionPane.INFORMATION_MESSAGE);
				if (s.endsWith(".jar"))
					driver.jarFiles.remove(s);
			}

			// fullNodeName = listHead.getAbsolutePath();
			// showFileNames();
			showListHead();
			t_fileName.setText("");

			panel.validate();
			// panel.remove(listView);
			showList();

			// }
		}

	}

	class EnterAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e) {

			//System.out.println(selComp);
			if (selComp instanceof MyButton) {
				// ((MyButton) selComp).getAction().actionPerformed(new
				// ActionEvent(e, 0, ""));
				if (selComp == butOK)  //check this out!
					return;
				((MyButton) selComp).doClick();
				return;
			}
			if (selComp == butSortByDate) {
				driver.sortByDate = !driver.sortByDate;
				butSortByDate.setSelected(driver.sortByDate);
				driver.saveProp("sortbydate",Boolean.toString(driver.sortByDate));
				showList();
				repaint();
				return;
			}
			
			/*		
			if (selComp == t_dirName) {           
				String u = t_dirName.getText();
				File h = new File(u);
				if (!h.exists() || !h.isDirectory()) {
					MyOptionPane.showMessageDialog(driver,
							"File " + u
									+ " either doesn't exist or is not a directory",
							MyOptionPane.ERROR_MESSAGE);

					return;
				}
				listHead = u; 
				// panel.remove(listView);
				//t_dirName.setBackground(vLightBlue);
				showList();
				return;
			}
			*/

			butNF.setEnabled(!inTree /*&& saveAs */);
			butDel.setEnabled(!inTree);

			if (!((selComp instanceof JList) || selComp == t_fileName))
				return;

			String s = t_fileName.getText();  

			// if (s == null || s.equals("")) {

			if (selComp instanceof JList) {
				t_fileName.setText("");	

				rowNo = list.getSelectedIndex();
				if (nodeNames.length == 0 || rowNo == -1) {
					if (!saving) {
						MyOptionPane.showMessageDialog(driver,
								"Empty directory or no entry selected",
								MyOptionPane.ERROR_MESSAGE);
					}
					return;
				}

				s = nodeNames[rowNo];
				int i = s.indexOf("@");
				if (i > -1)
					s = s.substring(0, i);  // drop date, if any
				File f = new File(t_dirName.getText() + "/" + s);
				if (f.exists())
					if (f.isDirectory()) {
						String w = f.getAbsolutePath();
						w = w.replace("\\",  File.separator);
						w = w.replace("/",  File.separator);
						t_dirName.setText(w);
						showList();
				}
					else {
						//s = s.replace("\\",  File.separator);
						//s = s.replace("/",  File.separator);
						t_fileName.setText(s);
						enterAction.actionPerformed(new ActionEvent(e, 0, ""));  // recursive!  probably doesn't get control!
					}
				 
				} else {
				if (selComp == t_fileName) {
					s = t_fileName.getText();
					if (s.endsWith("/"))
						s = s.substring(0, s.length() - 1);
					String s2 = s;
					//System.out.println("Show file name: " + s2);
					s = s.replace("\\", "/");
 					if (!s.endsWith(".jar") &&
 							!s.endsWith("fbp.json") ||
 							-1 == s.indexOf("/"))
						s2 = t_dirName.getText() + "/" + s;
					File f = new File(s2);

					if (!inTree) {
					if (!f.exists()) {
						//if (-1 == s.indexOf(".")) {
						if (null == driver.getSuffix(s)) {	
													
							// add appropriate extension							

							f = new File(f.getAbsolutePath() +  "." + lang.ext);  
							
							s = f.getName();

							//s = s.replace("\\",  File.separator);
							//s = s.replace("/",  File.separator);
							t_fileName.setText(s);
							repaint();
						} 
						 
				//		if (!f.exists()) {
					 /*
					 	if (MyOptionPane.YES_OPTION == MyOptionPane.showConfirmDialog(
					 
									driver,
									"Create new file: " + f.getAbsolutePath() + "?", 
									"Confirm create", MyOptionPane.YES_NO_OPTION)) {
								try {
									f.createNewFile();
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							} else {
							 
								MyOptionPane.showMessageDialog(driver, f.getAbsolutePath() + " does not exist - use New Diagram or Save As");
								return;
								*/
						//	}
						 
					//	}
						
						 
						} 
					/* else   
						if (selComp == t_dirName) {
						listHead = t_dirName.getText();
						showList();
					}
					*/
					}
				}
				}
				 
			repaint();
			// }

			if (s == null || s.equals("")) {
				MyOptionPane.showMessageDialog(driver,
						"No file specified", MyOptionPane.ERROR_MESSAGE);
				return;
			}

			
			//File f = null;

			if (s.toLowerCase().endsWith(".jar") ||
					s.toLowerCase().endsWith("fbp.json")) {
				butNF.setEnabled(false);
				butDel.setEnabled(false);
				
				if (lang == driver.langs[DrawFBP.Lang.JARFILE] || 
						lang == driver.langs[DrawFBP.Lang.FBPJSON]) {
					processOK();
					return;
				}

				if (s.toLowerCase().endsWith(".jar")) {
					jarTree = driver.buildJarFileTree(s);
					inTree = true;
				}
				
				//String s2 = "";
				//if (!s.endsWith(".jar") || -1 == s.indexOf("/"))
				//	s2 = t_dirName.getText() + "/" + s;
				//File f = new File(s2);
				inTree = true;
				//if (s.toLowerCase().endsWith("fbp.json")) {
					//processOK();
				//	buildListFromJSON(s2);				
				//}
				
				butNF.setEnabled(!inTree /*&& saveAs */);
				butDel.setEnabled(!inTree);
				if (driver.currNotn == driver.notations[DrawFBP.Notation.JSON])
					currentNode = driver.fbpJsonTree;
				else	
					currentNode = jarTree;
				t_fileName.setText("");

				//if (0 >= currentNode.getChildCount()) {
				//	MyOptionPane.showMessageDialog(driver,
				//			"Error in jar file", MyOptionPane.ERROR_MESSAGE);
				//	return;
				//}

				listHead = s + "!";   //?????????????
				showListHead();

				showList();

			} else if (!inTree) {

				File f = null;
				if (s.equals(""))
					f = new File(listHead);
				else {
					if (s.endsWith("/"))
							s = s.substring(0, s.length() - 1);
					f = new File(listHead + "/" + s);
				}

				if (!f.exists()) {

					 if (!saveAs)
					 	processOK();
					 else 
						if (selComp != t_fileName) {
						MyOptionPane.showMessageDialog(driver,
								"Folder does not exist: " + f.getAbsolutePath(),
								MyOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				if (f.isDirectory()
						|| f.getName().toLowerCase().endsWith("fbp.json")) {

					listHead = f.getAbsolutePath();
					showListHead();
					
					showList();

				} else
					processOK();
				
			} else { // inTree
				
				String w = list.getSelectedValue();
				//int k = w.lastIndexOf(".");
				//if (k > -1) {
				//	String suff = w.substring(k + 1);
				//	if (suff.equals(driver.currNotn.lang.ext)) 
				if (w != null) {
					File f = new File(w);
					if (f != null && driver.currNotn.lang.filter.accept(f))						
						processOK();	
				}
				//}
				//else {
				//String suff = w.substring(k + 1);
				currentNode = driver.findChild(currentNode, w);  
				if (currentNode == null)
					return;
				if (currentNode.getChildCount() > 0) {
					listHead = listHead + "/" + s;
					//listHead = s;
					showListHead();
					// panel.remove(listView);
					showList();
				} 
				else
					// if (!saveAs)
					processOK();
				//}
			}

			// }
			panel.validate();
			dialog.repaint();
			// frame.repaint();
		}

	}

	class SuggAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e) {
			
			// For now we will only shift from suggested file to t_fileName
			String s = t_suggName.getText();
			//s = s.replace("\\",  File.separator);
			//s = s.replace("/",  File.separator);
			t_fileName.setText(s);
			// text3.setText(s);
			t_fileName.requestFocusInWindow();
			selComp.setBackground(Color.WHITE);   
			selComp = t_fileName;
			// text2.setBackground(vLightBlue);
			panel.validate();
			repaint();

		}
	}

	class FindAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e) {

			String s = (String) MyOptionPane.showInputDialog(dialog,
					"Enter search characters", null);

			if (s == null || s.equals(""))
				return;
			
			int row = -1;

			//if (s != null) {
				s = s.toLowerCase();

				for (int i = 0; i < list.getModel().getSize(); i++) {
					String item = list.getModel().getElementAt(i);
					item = item.toLowerCase();
					if (item.startsWith(s)) {
						row = i;
						break;
					}
				}
			//}

			if (row == -1)
				MyOptionPane.showMessageDialog(driver, "String not found",
						MyOptionPane.WARNING_MESSAGE);
			else {
				rowNo = row;
				list.setSelectedIndex(rowNo);
				list.ensureIndexIsVisible(rowNo);
			}
			repaint();
		}

	}

	class ParentAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e) {
			t_fileName.setText("");

			if (!inTree) {
				listHead = (new File(listHead)).getParent();
				if (listHead == null)
					listHead = System.getProperty("user.home");

				showListHead();
				

			} else {
				String u = (String) currentNode.getUserObject();
				if (u == null) {
					inTree = false;
					currentNode = null;
				} else {

					currentNode = (DefaultMutableTreeNode) currentNode
							.getParent();
					u = listHead;
					//int k = u.lastIndexOf("/");
					int k = u.lastIndexOf(File.separator);
					u = u.substring(0, k);
					listHead = u;
					
				}
				if (!inTree) {
					listHead = listShowingSpecHdr;

				}
			}
			butNF.setEnabled(!inTree /* && saveAs */);
			butDel.setEnabled(!inTree);
			// if (selComp instanceof MyButton) {
			butParent.setSelected(false);
			t_fileName.setText((new File(listHead)).getName()+ "/");
			showList();
						
			//listView.repaint();
			dialog.repaint();
			panel.validate();
			panel.repaint();
			// frame.repaint();
			t_dirName.repaint();
			repaint();

		}
	}
	class NewFolderAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e) {
			String fileName = (String) MyOptionPane.showInputDialog(dialog,
					"Enter new folder name", null);

			if (fileName != null) {
				String s = listHead;
				// String t = s;
				s += "/" + fileName;
				File f = new File(s);
				if (f.exists()) {
					MyOptionPane.showMessageDialog(driver,
							"Folder already exists: " + f.getAbsolutePath(),
							MyOptionPane.WARNING_MESSAGE);
					return;
				}

				boolean b = f.mkdir();
				if (!b)
					MyOptionPane.showMessageDialog(driver,
							"Folder not created: " + f.getAbsolutePath(),
							MyOptionPane.ERROR_MESSAGE);
				// panel.remove(listView);
				// fullNodeName = s;
				// showFileNames();
				showList();
				// selComp = text2;

			}
			panel.validate();
			repaint();
		}
	}

	class MyComboBox extends JComboBox<String> {
		private static final long serialVersionUID = 1L;
		MyComboBox(String[] s) {
			super(s);
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			Color c = (this == selComp) ? vLightBlue : Color.WHITE;

			int i = driver.allFiles ? 1 : 0;
			String lt = driver.filterOptions[i];

			JLabel l = new JLabel(lt);
l.setFont(driver.fontg);
			Rectangle bounds = super.getBounds();
			g.setColor(c);
			g.fillRect(0, 0, bounds.width, bounds.height);
			l.setBounds(bounds);
			// setOpaque(true);
			l.paint(g); 

		}

	}

	class MyTraversalPolicy extends FocusTraversalPolicy {

		// Vector<Component> order;

		// public MyTraversalPolicy(Vector<Component> order2) {
		// this.order = new Vector<Component>(order2.size());
		// this.order.addAll(order2);
		// }

		public Component getFirstComponent(Container focusCycleRoot) {
			return (Component) order.get(0);
		}

		public Component getLastComponent(Container focusCycleRoot) {
			return (Component) order.lastElement();
		}

		public Component getDefaultComponent(Container focusCycleRoot) {
			return (Component) order.get(0);
		}

		public Component getComponentAfter(Container focusCycleRoot,
				Component aComponent) {
			
			int idx;
			if (aComponent == null)
				idx = 0;			
			else if (aComponent instanceof JList)
				idx = 4;
			else
				idx = order.indexOf(aComponent);

			idx = (idx + 1) % order.size();

			Component c = order.get(idx);
			return c;
		}
		public Component getComponentBefore(Container focusCycleRoot,
				Component aComponent) {

			int idx;
			if (aComponent == null)
				idx = 0;			
			else if (aComponent instanceof JList)
				idx = 4;
			else
				idx = order.indexOf(aComponent);
			
			idx--;

			if (idx < 0) {
				idx = order.size() - 1;
			}

			Component c = order.get(idx);
			return c;
		}

	}

	class MyTextField extends JTextField {

		private static final long serialVersionUID = 1L;
		String name = null;
		String lowLevel; 
		JTextField field;

		public MyTextField(int i, String fldName) {
			super(i);
			name = fldName;
		}

		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			//JComponent c = selComp;
			//if (c instanceof MyTextField)
			//	System.out.println(selComp == c);
			
			if (this == selComp) {
				setBackground(vLightBlue);
				//int i = getText().indexOf(".");
				//if (-1 < i){		// if has suffix			
				//getCaret().setVisible(true);
				//setEditable(true);
				//}
				 
				 	/*
				setFocusable(true);
				setRequestFocusEnabled(true);
				setEnabled(true);
				requestFocusInWindow();
				setVisible(true);
				//String s = getText();
				//setCaretPosition(s.length());
				validate();
				 */
				 
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						setFocusable(true);
						setRequestFocusEnabled(true);
						requestFocusInWindow();
						setEnabled(true);
						setVisible(true);
						getCaret().setVisible(true);
						setEditable(true);	
					}
				});
				 
			 
				

			} else {
				setBackground(Color.WHITE);
				setEditable(false);
				setEnabled(false);
				getCaret().setVisible(false);
			}
			repaint();
		}

	}

	class MyButton extends JButton {

		private static final long serialVersionUID = 1L;
		
		String name = null;
		
		public MyButton(String text, String name) {
			super(text);
			this.name = name;
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			// if (isSelected())
			if (this == selComp)
				g.setColor(vLightBlue);
			else
				g.setColor(Color.WHITE);
			setOpaque(false);
			setFocusPainted(false);

		}
	}
	
	// For list only!

	public class ClickListener extends MouseAdapter	{
		
		// Only applies to list selection

		MouseEvent lastEvent;
		
		String lowLevel;

		public void mouseClicked(MouseEvent e)

		{

			//System.out.println("Click count: " + e.getClickCount());
			if (e.getClickCount() > 2)
				return;

			lastEvent = e;

		
			if (e.getClickCount() == 2) 
				secondClick(lastEvent);
			//else
			//	firstClick(lastEvent);			 
		
			
			dispose();
		}

		//public void actionPerformed(ActionEvent e) {

		//	firstClick(lastEvent);
		//}
/*
		public void firstClick(MouseEvent e) {
			//System.out.println("First click");

			selComp = list;
			rowNo = -1;
			
			rowNo = list.locationToIndex(e.getPoint());
			
			list.setRequestFocusEnabled(true);

			list.setSelectedIndex(rowNo);
			
			if (rowNo == -1 || nodeNames[rowNo].equals("(empty folder"))
				return;

			//list.setSelectedIndex(rowNo);
			//list.repaint();                  // do we really need this?

			selComp = t_fileName;
			oneClick();
			
		}
		*/
		public void secondClick(MouseEvent e) {
			
			int n;
			//System.out.println("Second click");
			
			for (n = list.getFirstVisibleIndex(); n <= list.getLastVisibleIndex(); n++) {
				Rectangle r = list.getCellBounds(n, n);
				if (r.contains(e.getPoint())) {
					if (n > -1)
						break;
				}
			}
			
			String w = null;
			if (rowNo == n) {
				//selComp = t_fileName;        
				
				String v = list.getSelectedValue();
				if (!inTree) {
					selComp = t_fileName;        
					w = t_dirName.getText();
					int j = v.indexOf("@");
					if (j > -1)
						v = v.substring(0, j);

				}
				
				// if selected name has a suffix
				if (0 < v.indexOf(".") /*|| inJarTree */) {
					//v = v.replace("\\",  File.separator);
					//v = v.replace("/",  File.separator);
					t_fileName.setText(v);
					enterAction.actionPerformed(new ActionEvent(e, 0, ""));
					return;
					
				}
				if (inTree) {
					enterAction.actionPerformed(new ActionEvent(e, 0, ""));
					return;
				}
				else {  // folder name AND not injartree
						w += "/" + v;
						File f2 = new File(w);
						if (!f2.exists())
							MyOptionPane.showMessageDialog(driver,
								"File does not exist: "
										+ f2.getAbsolutePath(),
								MyOptionPane.ERROR_MESSAGE);
						w = w.replace("\\",  File.separator);
						w = w.replace("/",  File.separator);
						t_dirName.setText(w);
						listHead = w;
						showList();
					 
				}
			}
			repaint();
		}
	}

public void oneClick() {
		
		if (rowNo == -1 || nodeNames[rowNo].equals("(empty folder"))
			return;
		String t = (String) list.getSelectedValue();
		int i = t.indexOf("@");
		if (i > -1)
			t = t.substring(0, i);
		
		//t_fileName.setText(t);
		
		if (!inTree) {
			String t2 = t;
			if (!t.equals("")) {
				if (!t.endsWith(".jar") && !t.endsWith("fbp.json")) {
					t2 = t_dirName.getText() + "/" + t;
					File f = new File(t2);
					 
					if (!f.exists()) {
						if (0 < t.lastIndexOf(".")) { // if file
							MyOptionPane.showMessageDialog(driver,
									"File does not exist: "
											+ f.getAbsolutePath(),
									MyOptionPane.ERROR_MESSAGE);
							repaint();
							return;
						}
						
					} 
				}
			}
		}
		
				

		if (-1 < t.indexOf(".")) { // if has suffix
			//t = t.replace("\\",  File.separator);
			//t = t.replace("/",  File.separator);
			t_fileName.setText(t);
			//t_fileName.getCaret().setVisible(true);
			
			selComp = t_fileName;
			//((MyTextField) selComp).setEditable(true);

			//selComp.setFocusable(true);
			// selComp.requestFocusInWindow();
			//selComp.grabFocus();
			//selComp.setEnabled(true);
			// t_fileName.setText(lowLevel);

			selComp.requestFocus();
		}
		else
			t_fileName.setText(t + "/");

		selComp.setVisible(true);   
		selComp.validate();

		repaint();
	}

	
	public class SharedListSelectionHandler implements ListSelectionListener {

		public void valueChanged(ListSelectionEvent lSE) {
		        ListSelectionModel lsm = (ListSelectionModel)lSE.getSource();
		         if (!lSE.getValueIsAdjusting() && !lsm.isSelectionEmpty()) {
		            rowNo = lsm.getMinSelectionIndex();
		            list.setSelectedIndex(rowNo);
		            oneClick();
		        }
		}

	}
	

	 
  }