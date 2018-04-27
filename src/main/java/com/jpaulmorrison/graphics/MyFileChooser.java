package com.jpaulmorrison.graphics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

public class MyFileChooser extends JFrame
		implements
			MouseListener,
			ActionListener,
			KeyListener,
			//DocumentListener,
			ListSelectionListener  {

	private static final long serialVersionUID = 1L;
	public static int APPROVE_OPTION = 0;
	public static int CANCEL_OPTION = 1;

	//FileFilter filter = null;
	JDialog dialog = null;
	//String prompt = "";
	//int type;

	//JFrame frame;
	JList<String> list = null;
	String listHead = null;
	String listShowingJarFile = null;
	boolean inJarTree = false;
	JScrollPane listView = null;
	JPanel panel = null;
	int result = CANCEL_OPTION;

	DrawFBP driver;
	MyButton butParent = new MyButton();
	MyButton butOK = new MyButton();
	MyButton butCancel = new MyButton();
	MyButton butDel = new MyButton();
	MyButton butNF = new MyButton();
	 
	MyButton butCopy = new MyButton();

	MyTextField t_dirName = new MyTextField(100);
	MyTextField t_fileName = new MyTextField(100);
	MyTextField t_suggName = new MyTextField(100);

	Component selComp = null;
	// Component changedField = null;
	MyTraversalPolicy mtp;
	DefaultMutableTreeNode jarTree;
	DefaultMutableTreeNode currentNode;
	String folder;

	String[] nodeNames = null;
	String suggestedName = null;

	boolean clickState = true;
	//String fileExt = null;
	boolean shift = false;
	// Color slateGray1 = new Color(198, 226, 255);
	Color vLightBlue = new Color(220, 235, 255);
	// Color lightBlue = new Color(135, 206, 250);
	Color lightBlue = new Color(160, 220, 250);
	//String title;

	MyComboBox cBox = null;
	
	boolean saveAs;
	Vector<Component> order = null;

	//Point mLoc = null;
	//MyComboBox cBox = null;
	ListRenderer renderer;

	CancelAction cancelAction;
	DeleteAction deleteAction;
	EnterAction enterAction;
	CopyAction copyAction;

	ParentAction parentAction;
	NewFolderAction newFolderAction;

	//MyFileCompare comp;
	//String[] filterOptions = {"", "All (*.*)"};

	// Color textBackground;

	// String fullNodeName;
	
	DrawFBP.FileChooserParms fCParms;
	
	Point mLoc = null;

	public MyFileChooser(File f, DrawFBP.FileChooserParms fCP) {
			
		if (!f.exists()) 
			listHead = System.getProperty("user.home");
		else 	
			listHead = f.getAbsolutePath();	
		// fullNodeName = f.getAbsolutePath();
		driver = DrawFBP.driver;

		fCParms = fCP;
		
		// text3.setEditable(false);
		//frame = driver.frame;
		//filter = driver.curDiag.saveInfoArray[type].filter;
		//fileExt = driver.curDiag.saveInfoArray[type].fileExt;
		//prompt = driver.curDiag.saveInfoArray[type].prompt;
		//title = driver.curDiag.saveInfoArray[type].title;
		//this.type = type;
	}

	int showOpenDialog(final boolean saveas) {

		dialog = new JDialog(driver.frame, JDialog.ModalityType.APPLICATION_MODAL);
		// dialog.setUndecorated(false);

		saveAs = saveas;

		dialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dialog.dispose();
			}
		});

		DrawFBP.applyOrientation(dialog);
		panel = new JPanel();
		panel.setLayout(new BorderLayout());		
		
		driver.curDiag.filterOptions[0] = fCParms.title;
		cBox = new MyComboBox(driver.curDiag.filterOptions);
		cBox.setMaximumRowCount(2);
		cBox.addMouseListener(this);
		cBox.setSelectedIndex(driver.allFiles ? 1 : 0);

		order = new Vector<Component>(9);
		order.add(t_dirName);
		order.add(butParent);
		order.add(butNF);
		order.add(panel); // just a place-holder - will be filled in by
							// buildList
		order.add(t_fileName);
		order.add(butCopy);
		order.add(butOK);
		order.add(cBox);
		order.add(butDel);
		order.add(butCancel);
		
		t_dirName.setEditable(true);
		t_dirName.setEnabled(true);

		//text.getDocument().addDocumentListener(this);

		t_fileName.setEditable(true);
		t_fileName.setEnabled(true);
		t_fileName.setRequestFocusEnabled(true);
		//text2.addKeyListener(this);
		//text2.getDocument().addDocumentListener(this);
		t_fileName.setPreferredSize(new Dimension(100, driver.gFontHeight + 2));
			
		t_suggName.setEditable(false);
		t_suggName.setEnabled(true);
		// text3.setRequestFocusEnabled(true);
		t_suggName.setFont(driver.fontg.deriveFont(Font.ITALIC));
		// text3.setPreferredSize(new Dimension(100, driver.fontHeight + 2));

		String s = (saveAs) ? "Save or Save As" : "Open File";
		//comp = new MyFileCompare();
		renderer = new ListRenderer(driver);

		if (fCParms == driver.curDiag.fCPArr[DrawFBP.DIAGRAM])
			dialog.setTitle(s);
		else {
			if (fCParms == driver.curDiag.fCPArr[DrawFBP.GENCODE])
				fCParms.prompt = "Specify file name for generated code - for diagram: " + driver.curDiag.title + ".drw";
			 
			dialog.setTitle(fCParms.prompt);
			if (fCParms == driver.curDiag.fCPArr[DrawFBP.CLASS])
				listShowingJarFile = listHead;
		}

		enterAction = new EnterAction();
		copyAction = new CopyAction();
		cancelAction = new CancelAction();
		deleteAction = new DeleteAction();

		parentAction = new ParentAction();
		newFolderAction = new NewFolderAction();

		butParent.setAction(parentAction);
		butParent.setText("Parent Folder");
		butParent.setMnemonic(KeyEvent.VK_P);

		butNF.setAction(newFolderAction);
		butNF.setMnemonic(KeyEvent.VK_N);
		butNF.setText("New Folder");
		
		butNF.setEnabled(false);

		// butOK.setAction(okAction);
		butOK.setAction(enterAction);
		butCopy.setAction(copyAction);
		butCancel.setAction(cancelAction);
		butDel.setAction(deleteAction);

		butParent.setRequestFocusEnabled(true);
		if (saveAs)
			butNF.setRequestFocusEnabled(true);
		butCopy.setRequestFocusEnabled(true);

		t_dirName.addMouseListener(this);
		t_fileName.addMouseListener(this);
		
		panel.setPreferredSize(new Dimension(600, 600));

		t_dirName.setFocusTraversalKeysEnabled(false);
		butParent.setFocusTraversalKeysEnabled(false);
		if (saveAs)
			butNF.setFocusTraversalKeysEnabled(false);
		t_fileName.setFocusTraversalKeysEnabled(false);
		butOK.setFocusTraversalKeysEnabled(false);
		butDel.setFocusTraversalKeysEnabled(false);
		butCancel.setFocusTraversalKeysEnabled(false);
		butCopy.setFocusTraversalKeysEnabled(false);

		butParent.setEnabled(true);
		if (saveAs)
			butNF.setEnabled(true);
		butOK.setEnabled(true);
		// butCopy.setEnabled(saveAs);
		butCopy.setEnabled(true);
		butCancel.setEnabled(true);
		butDel.setEnabled(true);

		KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				escape, "CLOSE");

		panel.getActionMap().put("CLOSE", cancelAction);

		JLabel label = new JLabel("Current folder: ");
		label.setFont(driver.fontg);

		Box box0 = new Box(BoxLayout.Y_AXIS);
		Box box1 = new Box(BoxLayout.X_AXIS);

		box1.add(label);

		box1.add(Box.createRigidArea(new Dimension(12, 0)));
		box1.add(t_dirName);
		box1.add(Box.createRigidArea(new Dimension(6, 0)));

		box1.add(butParent);
		// butParent.addActionListener(this);
		box1.add(Box.createRigidArea(new Dimension(6, 0)));

		// butNF.addActionListener(this);
		box1.add(butNF);
		//box1.add(butOK);

		box0.add(Box.createRigidArea(new Dimension(0, 20)));
		box0.add(box1);

		box0.add(Box.createRigidArea(new Dimension(0, 20)));
		panel.add(box0, BorderLayout.NORTH);

		t_dirName.setFont(label.getFont());
		t_dirName.addActionListener(this);
		t_fileName.addActionListener(this);
		
		
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;

		JPanel pan2 = new JPanel();

		pan2.setLayout(gridbag);
		//c.fill = GridBagConstraints.BOTH;

		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.0;

		JLabel lab1 = new JLabel("File name: ");
		gridbag.setConstraints(lab1, c);
		pan2.add(lab1);

		c.gridx = 1;
		c.weightx = 0.0;
		JLabel lab5 = new JLabel("  ");
		gridbag.setConstraints(lab5, c);
		pan2.add(lab5);

		c.gridx = 2;
		
		c.weightx = saveAs ? 0.1: 1.0;
		c.gridwidth = saveAs ? 1 : 3;
		//c.ipadx  = saveAs ? -20: 0;
		gridbag.setConstraints(t_fileName, c);
		pan2.add(t_fileName);

		if (saveAs) {
			c.gridx = 3;
			c.weightx = 0.0;
			c.gridwidth = 1;
			JLabel lab6 = new JLabel("   Suggestion: ");
			gridbag.setConstraints(lab6, c);
			pan2.add(lab6);

			c.gridx = 4;
			c.weightx = 0.9;
			//c.ipadx = 20;
			gridbag.setConstraints(t_suggName, c);
			pan2.add(t_suggName);
			t_suggName.setBackground(Color.WHITE);
			Dimension dim2 = t_suggName.getPreferredSize();
			t_suggName.setPreferredSize(new Dimension(driver.gFontWidth * 25, dim2.height));
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
			gridbag.setConstraints(butCopy, c);
			pan2.add(butCopy);

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
		butCancel.setText("Cancel");
		butDel.setText("Delete");
		butCopy.setText(saveAs ? "Use suggested name" : "");

		JLabel lab3 = new JLabel();
		lab3.setPreferredSize(new Dimension(500, 30));
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 4;
		c.weightx = 1.0;
		gridbag.setConstraints(lab3, c);
		pan2.add(lab3);

		cBox.addActionListener(this);

		// cBox.setUI(new BasicComboBoxUI());
		cBox.setRenderer(new ComboBoxRenderer());

		

		//Dimension dim = new Dimension(1000, 800);
		//dialog.setPreferredSize(dim);

		dialog.setFocusTraversalKeysEnabled(false);
		t_dirName.addKeyListener(this);
		t_fileName.addKeyListener(this);
		butParent.addKeyListener(this); // needed to service tab keys
		butNF.addKeyListener(this); // needed to service tab keys
		butOK.addKeyListener(this); // needed to service tab keys
		cBox.addKeyListener(this); // needed to service tab keys
		butDel.addKeyListener(this); // needed to service tab keys
		butCancel.addKeyListener(this); // needed to service tab keys
		butCopy.addKeyListener(this); // needed to service tab keys
		cBox.setFocusTraversalKeysEnabled(false);
		mtp = new MyTraversalPolicy();
		setFocusTraversalPolicy(mtp);
		setFocusCycleRoot(false);

		showList();
		if (saveAs) {

			if (suggestedName != null && !(suggestedName.equals(""))) {
				File h = new File(suggestedName);
				listHead = h.getParent();
				t_dirName.setText(listHead);
				//t_fileName.setText(h.getName());
				t_suggName.setText(h.getName());

				/*
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						text2.requestFocusInWindow();
						selComp = text2;
						text2.setBackground(vLightBlue);
					}
				});
				
				*/
				
				t_fileName.addAncestorListener( new RequestFocusListener(false) );
				//selComp = t_fileName;
				//text2.setBackground(vLightBlue);
				//text2.setEditable(true); 
			}

			if (driver.curDiag.title != null && driver.curDiag.diagFile != null) {
				s += " (current file: " + driver.curDiag.diagFile.getAbsolutePath()
						+ ")";
			}
		} else {
			t_dirName.setText(listHead);
			/*
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					list.requestFocusInWindow();
					selComp = list;
					// list.setBackground(vLightBlue);
				}
			});
			*/
			//list.addAncestorListener( new RequestFocusListener() );
			selComp = list;
		}

		panel.add(pan2, BorderLayout.SOUTH);
		dialog.add(panel);

		Point p = driver.frame.getLocation();
		Dimension dim = driver.frame.getSize();
		int x_off = 100;
		int y_off = 100;
		dialog.setPreferredSize(new Dimension(dim.width - x_off, dim.height - y_off));
		dialog.pack();
		dialog.setLocation(p.x + x_off, p.y + y_off);
		//frame.pack();

		dialog.setVisible(true);

		// if (!saveAs)
		// textBackground = Color.WHITE;

		//frame.repaint();

		return result;
	}
	int showOpenDialog() {
		return showOpenDialog(false);
	}

	void getSelectedFile(String[] s) {
		
		s[0] = DrawFBP.makeAbsFileName(t_fileName.getText(), t_dirName.getText());
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
		inJarTree = false;
		String s = listHead;
				
		
		if (s.toLowerCase().endsWith("package.json")) {
			ll2 = buildListFromJSON(s);
			
			// fullNodeName = s;
			// showFileNames();
		} else {
			if (-1 == s.indexOf("!")) { // if fullNodeName is NOT a
										// file
										// within a jar file ...

				if (listHead == null)
					return;
				File f = new File(listHead);
				String t = f.getAbsolutePath();
				if (t.endsWith("My Documents"))
					f = new File(t.replace("My Documents", "Documents"));
				if (!f.exists() || !f.isDirectory())
					return;
				
				if (!inJarTree) {
					if (listHead.equals(listShowingJarFile)) {
						t = driver.javaFBPJarFile;
						ll.add(t);
						for (String u : driver.jarFiles.values()) {
							ll.add(u);
						}
					}
				}

				String[] fl = f.list();
				
				ll2 = new LinkedList<String>();
				//if (fl == null || fl.length == 0) {
				//	ll2.add("(empty folder)");
				//} else {
					for (int j = 0; j < fl.length; j++) {
						String fn = s + File.separator + fl[j];
						File fx = new File(fn);
						if (!fx.exists())
							continue;
						if (fx.isDirectory())
							ll2.add(fl[j]); // directories go into ll first
						
					 }
					ll.addAll(mySort(ll2));  // add elements of ll2 to ll in sorted order
					
					ll2.clear(); 

					for (int j = 0; j < fl.length; j++) {
						String fn = s + File.separator + fl[j];
						File fx = new File(fn);
						if (!fx.exists())
							continue;
						if (!fx.isDirectory() /* && (!(fn.startsWith("."))) */
								&& (fCParms.filter.accept(fx) || driver.allFiles))
							ll2.add(fl[j]); // non-directories go into ll2,
											// which is
											// then sorted into ll
											
					}
					
		 			ll.addAll(mySort(ll2));   // add elements of ll2 to end of ll in sorted order
				 

			} else {
				inJarTree = true;

				if (currentNode == null)
					return;

				ll = new LinkedList<String>();

				ll2 = new LinkedList<String>();
				
				Enumeration<DefaultMutableTreeNode> e = currentNode.children();
				while (e.hasMoreElements()) {
					DefaultMutableTreeNode node = (e.nextElement());
					String t = (String) node.getUserObject();
					File f = new File(t);
					if (f.isDirectory())
					    ll2.add((String) t);
				}
				ll.addAll(mySort(ll2));
				
				ll2.clear();
				e = currentNode.children();
				while (e.hasMoreElements()) {
					DefaultMutableTreeNode node = (e.nextElement());
					String t = (String) node.getUserObject();
					File f = new File(t);
					if (!(f.isDirectory()))
					    ll2.add((String) t);
				}
				ll.addAll(mySort(ll2));   // add elements of ll2 to end of ll in sorted order
			}
		}
		//if (ll == null)
		//	return;


		

		Object[] oa = ll.toArray();
		
		int k = 0;

		nodeNames = new String[oa.length];
		for (int j = 0; j < oa.length; j++) {
			nodeNames[j] = (String) oa[j];
			if (nodeNames[j].endsWith(".jar"))
				k = k + 1;  //get rid of spurious "unused" message			
		}

		list = new JList<String>(nodeNames);
		//list.setSelectedIndex(k);
		list.setSelectedIndex(-1);

		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		list.addKeyListener(this);
		list.addMouseListener(this);
		list.addListSelectionListener(this);
		list.setFocusTraversalKeysEnabled(false);

		order.remove(3);
		order.add(3, list);
		//list.setFixedCellHeight(driver.fontg.getSize() + 2);
		
		//list.setFixedCellHeight(14);
		//list.setFixedCellWidth(60);
		
		
		FontMetrics metrics = driver.osg.getFontMetrics(driver.fontg);
		list.setFixedCellHeight(metrics.getHeight());

		list.setCellRenderer(renderer);
		list.setEnabled(true);

		if (!saveAs)
			list.addAncestorListener( new RequestFocusListener() );
		if (listView != null)
			panel.remove(listView);
		listView = new JScrollPane(list);		
		panel.add(listView, BorderLayout.CENTER);

		selComp = list;
		//list.setSelectedIndex(0);
		list.setFocusable(true);
		
		
		list.setVisible(true);
		// list.requestFocusInWindow();
		paintList();

		//panel.validate();
		
		//frame.pack();
		listView.repaint();
		dialog.repaint();
 		
		panel.repaint();
		//frame.repaint();

	}

	void processOK() {

		result = APPROVE_OPTION;
		dialog.dispose();
		return;
	}

	/* Build tree of nodes (DefaultMutableTreeNode) using contents of jar file */

	public final DefaultMutableTreeNode buildJarFileTree(String jarFileName) {
		Enumeration<?> entries;
		JarFile jarFile;
		DefaultMutableTreeNode top = new DefaultMutableTreeNode();
		DefaultMutableTreeNode next;

		try {
			File jFile = new File(jarFileName);
			jarFile = new JarFile(jFile);

			entries = jarFile.entries();

			while (entries.hasMoreElements()) {
				JarEntry entry = (JarEntry) entries.nextElement();
				//System.out.println(entry);

				if (!(entry.isDirectory())) {
					String s = entry.getName();
					if (s.toLowerCase().endsWith(".class")) {

						next = top;
						DefaultMutableTreeNode child;
						while (true) {
							int i = s.indexOf("/");
							String t;
							if (i == -1) {
								child = new DefaultMutableTreeNode(s);
								next.add(child);
								break;
							} else {
								t = s.substring(0, i);
								if (null == (child = findChild(next, t))) {
									child = new DefaultMutableTreeNode(t);
									next.add(child);
								}
								s = s.substring(i + 1);
								next = child;
							}
						}
					}
				}
			}
			jarFile.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {

		}
		return top;
	}

	@SuppressWarnings("unchecked")
	private DefaultMutableTreeNode findChild(DefaultMutableTreeNode current,
			String t) {
		if (current == null)
			return null;
		Enumeration<DefaultMutableTreeNode> e = current.children();
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode node = (e.nextElement());
			Object obj = node.getUserObject();
			if (t.equals((String) obj))
				return node;
		}
		return null;
	}

	public void paintList() {

		// selComp.setBackground(vLightBlue);
		if (selComp instanceof JList) {
			String s = list.getSelectedValue();
			if (s == null || s.equals("(empty folder)"))
				s = "";
			
			//String fn = DrawFBP.makeAbsFileName(s, listHead);
			if (currentNode == null) {
				
				//File h = new File(fn);
				//if (h.isDirectory())  
				//	t_fileName.setText("");	
				//else  
				//if (!h.isDirectory()) 	
				//	t_fileName.setText(s);
				t_dirName.setText(listHead);
				//selComp = t_fileName;
				
			} else {
				// String t = list.getSelectedValue();
				DefaultMutableTreeNode ch = currentNode; // findChild(currentNode,
															// t);
				if (ch.getChildCount() > 0) {
					// text.setText(fn);
					t_dirName.setText(listHead);
					//t_fileName.setText("");
					
				} else {
					t_fileName.setText(list.getSelectedValue());
					t_dirName.setText(listHead);
					selComp = t_fileName;
				}
			}

		}
		
		panel.validate();
		repaint();
	}

	@SuppressWarnings("unchecked")
	LinkedList<String> buildListFromJSON(String fileName) {
		int level = 0;
		File f = new File(fileName);
		String fileString;
		LinkedList<String> ll = new LinkedList<String>();
		if (null == (fileString = driver.curDiag.readFile(f))) {
			MyOptionPane.showMessageDialog(driver.frame, "Unable to read file "
					+ f.getName(), MyOptionPane.ERROR_MESSAGE);
			return null;
		}
		Integer errNo = new Integer(0);
		BabelParser2 bp = new BabelParser2(fileString, errNo);
		String label = null;
		String operand = null;
		HashMap<String, Object> hm = new HashMap<String, Object>();
		// Stack<String> lStack = new Stack<String>();
		Stack<HashMap<String, Object>> hmStack = new Stack<HashMap<String, Object>>();

		// we will ignore the array structure for now...

		while (true) {
			if (!bp.tb('o'))
				break;
		}

		
		do {
			if (bp.tc('#', 'o')) { // assuming #-sign only in col.1
				while (true) {
					if (bp.tc('\r', 'o'))
						break;
					if (bp.tc('\n', 'o'))
						break;
					bp.tu('o');
				}
				continue;
			}

			if (bp.tc('[', 'o')) {
				level++;
				continue;
			}
			if (bp.tc('{', 'o')) {
				level++;
				if (label != null) {
					HashMap<String, Object> hm2 = new HashMap<String, Object>();
					hm.put(label, hm2);
					hmStack.push(hm);
					hm = hm2;
					label = null;
				}
				continue;
			}
			if (bp.tc(']', 'o')) {
				level--;
				continue;
			}
			if (bp.tc('}', 'o')) {
				level--;
				if (level > 0)
					hm = hmStack.pop();
				continue;
			}

			if (bp.tc(':', 'o')) {
				label = operand;
				continue;
			}
			if (bp.tc('"', 'o')) {
				while (true) {
					if (bp.tc('"', 'o'))
						break;
					if (bp.tc('\\', 'o')) {
						if (!(bp.tc('"')))
							bp.w('\\');
						continue;
					}
					bp.tu();
				}
				operand = new String(bp.getOutStr());
				bp.eraseOutput();
				if (label != null) {
					hm.put(label, operand);
					label = null;
				}
				continue;
			}

			if (!(bp.tu('o'))) // tu only returns false at end of string
				break; // skip next character

		} while (level > 0);

		for (String k : hm.keySet()) {
			if (k.equals("noflo")) {
				HashMap<String, Object> m = (HashMap<String, Object>) hm.get(k);
				for (String k2 : m.keySet()) {
					if (k2.equals("graphs") && fCParms == driver.curDiag.fCPArr[DrawFBP.DIAGRAM]
							|| k2.equals("components")
							&& (fCParms == driver.curDiag.fCPArr[DrawFBP.GENCODE] ||
									fCParms == driver.curDiag.fCPArr[DrawFBP.PROCESS])) {
						HashMap<String, Object> m2 = (HashMap<String, Object>) m
								.get(k2);
						for (Object v : m2.values()) {
							ll.add((String) v);
						}
					}
				}
			}
		}

		if (ll.isEmpty()) {
			MyOptionPane.showMessageDialog(driver.frame,
					"No components or graphs in file: " + f.getName(), MyOptionPane.ERROR_MESSAGE);
			//return null;
		}

		return ll;
	}
	

	LinkedList<String> mySort(LinkedList<String> from) {
		if (from.isEmpty()) {
			return new LinkedList<String>();

		}
		 
		LinkedList<String> ll = from;
		LinkedList<String> lkl = new LinkedList<String>();
		while (true) {
			try {
				String low = ll.getFirst();
				int i = 0;
				int low_i = 0;
				for (String s : ll) {

					if (i > 0 && s.compareToIgnoreCase(low) < 0) {

						low = s;
						low_i = i;
					}
					
					i++;
				}
				lkl.add(low);

				ll.remove(low_i);				
			}

			catch (NoSuchElementException e) {
				return lkl;				

			}
		}
	}

	class ListRenderer extends JLabel implements ListCellRenderer<String>  {
		static final long serialVersionUID = 111L;

		Dimension minSize;
		Dimension maxSize;
		Dimension prefSize;
		DrawFBP driver;

		public ListRenderer(DrawFBP driver) {
			this.driver = driver;			
		    setOpaque(true);
		   	}

		public Component getListCellRendererComponent(
				JList<? extends String> list, String value, int index,
				boolean isSelected, boolean cellHasFocus) {
			Color goldenRod = new Color(255, 255, 224);
			Color bisque = new Color(255, 228, 196);
			String s = (String) value;
			Icon icon = driver.leafIcon;
			
			//JPanel jp = new JPanel();	
			//JLabel jp = this;
			//BoxLayout gb = new BoxLayout(jp, BoxLayout.X_AXIS);
			//jp.setLayout(gb);
			//jp.setPreferredSize(new Dimension(30,40));
			//setPreferredSize(new Dimension(150,20));

			setBackground(Color.WHITE);
			
			// System.out.println("|" + s + "|");
			// if (s.equals(""))
			// return jp;

			int j = 0;
			if (s == null || s.equals("(empty folder)"))
				icon = null;
			else if (s.toLowerCase().endsWith(".jar"))
				icon = driver.jarIcon;			
			else {
				if (currentNode == null) {
					File f = new File(listHead + File.separator + s);
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
				//else if (s.toLowerCase().endsWith(".drw")) //fudge
				//	setText("xxxx");
			}

			//if (selComp instanceof JList) {
			if (s == null)
				setBackground(vLightBlue);
			
			else if (/*listHead.equals(listShowingJarFile)
						&& */ s.toLowerCase().endsWith(".jar") || inJarTree)
					setBackground(goldenRod);
				else
					setBackground(vLightBlue);

				if (isSelected) {
					if (/*listHead.equals(listShowingJarFile)
							&& */ s.toLowerCase().endsWith(".jar") || inJarTree)
						setBackground(bisque);
					else
						setBackground(lightBlue);
					// System.out.println("Selected " + index);
				}
			//}

			minSize = new Dimension(400, 20);
			prefSize = new Dimension(400, 20);
			maxSize = new Dimension(Short.MAX_VALUE, 20);

			//JLabel lab1;
			if (s == null || s.charAt(0) == ' ') {				
				setText(s);
			}
			else {
				//lab1 = new JLabel(s, icon, JLabel.LEFT);
				setText(s);
				setIcon(icon);
			}
			setFont(driver.fontg);
			//lab1.setMinimumSize(minSize);
			//lab1.setMaximumSize(maxSize);
			setPreferredSize(prefSize); 
			setMaximumSize(maxSize);
			setMinimumSize(minSize);
			if (s.equals("a.drw"))
				return this;
			return this;
		}
	}

	class ComboBoxRenderer extends DefaultListCellRenderer {
		static final long serialVersionUID = 111L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			String s = (String) value;
			if (!s.startsWith("All"))
			 value = driver.curDiag.filterOptions[0];

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
		t_dirName.setBackground(Color.WHITE);

		// if (e.getSource() == butParent) {
		// ParentAction parentAction = new ParentAction();
		// parentAction.actionPerformed(new ActionEvent(e, 0, ""));
		// return;
		// }

		// if (e.getSource() == butNF) {
		// NewFolderAction nfAction = new NewFolderAction();
		// newFolderAction.actionPerformed(new ActionEvent(e, 0, ""));
		// return;
		// }

		if (e.getSource() == cBox) {

			int i = cBox.getSelectedIndex();
			driver.allFiles = (i == 1);
			// fullNodeName = (new File(fullNodeName)).getParent();
			// driver.properties
			// .put("allFiles", Boolean.toString(driver.allFiles));
			// driver.propertiesChanged = true;
			// panel.remove(listView);
			showList();
			// selComp = cBox;
			cBox.requestFocusInWindow();
			cBox.setBackground(vLightBlue);
			// Component c = cBox.getComponent(1);
			// c.setBackground(vLightBlue);
			selComp.setFocusable(true);
			cBox.setEnabled(true);

		}

		// repaint();
	}

	public void mouseClicked(MouseEvent e) {

		list.setSelectedIndex(-1);
		list.setRequestFocusEnabled(false);
		// changedField = null;

		if (selComp == t_dirName || selComp == t_fileName) {
			selComp.setBackground(Color.WHITE);
			((JTextField) selComp).setEditable(false);
			((JTextField) selComp).getCaret().setVisible(false);
			
		}

		t_fileName.setBackground(Color.WHITE);
		// cBox.repaint();

		if (selComp instanceof MyButton) {
			((MyButton) selComp).setSelected(false);
			((MyButton) selComp).setFocusable(false);

		}

		selComp = (Component) e.getSource();

		if (selComp == t_dirName || selComp == t_fileName) {

			((JTextField) selComp).setRequestFocusEnabled(true);

 			selComp.setBackground(vLightBlue);
			((JTextField) selComp).getCaret().setVisible(true);
			((JTextField) selComp).setEditable(true);
			//((JTextField) selComp).requestFocusInWindow();  

		}

		if (e.getSource() instanceof JList) {
			selComp = list;
			int rowNo = list.locationToIndex(e.getPoint());
			if (rowNo == -1)
				return;

			list.setRequestFocusEnabled(true);

			list.setSelectedIndex(rowNo);
			t_dirName.setBackground(Color.WHITE);
			// text2.setBackground(textBackground);
			
			// http://stackoverflow.com/questions/16392212/unable-to-type-or-delete-text-in-jtextfield
			//http://stackoverflow.com/questions/13415150/java-swing-form-and-cannot-type-text-in-newly-added-jtextfield
			//(this says don't use keylistener!)
			//		http://stackoverflow.com/questions/22642401/jtextfield-and-keylistener-java-swing?rq=1
			//		textField.getDocument().addDocumentListener(...); 
			//new code 
			
					
			//text2.requestFocusInWindow();
			//text2.setBackground(vLightBlue);
			//text2.getCaret().setVisible(true);

			// String fn = listHead + File.separator + nodeNames[rowNo];

			
			if (e.getClickCount() == 1) {
				mLoc = e.getLocationOnScreen();
				
				if (nodeNames[rowNo].equals("(empty folder"))
					return;			
				
				list.setSelectedIndex(rowNo);
				list.repaint();
				
				String t = (String)list.getSelectedValue();
				if (!t.equals("")) {
					File f = new File(t_dirName.getText() + File.separator + t);
					if (f.exists() && !f.isDirectory()) {
						t_fileName.setText(t);				
						t_fileName.repaint();
					}
				}

			} else if (e.getClickCount() == 2) {

				Point p = e.getLocationOnScreen();

				if (mLoc != null && Math.abs(p.x - mLoc.x) < 8  // allow for moved cursor...
						&& Math.abs(p.y - mLoc.y) < 8) {

					enterAction.actionPerformed(new ActionEvent(e, 0, ""));
				}

			}

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
		paintList();
		list.repaint();
	}

	
	public void mouseEntered(MouseEvent e) {
		// selComp = (Component) e.getSource();

	}

	
	public void mouseExited(MouseEvent e) {

	}

	
	public void mousePressed(MouseEvent e) {
		//selComp = (Component) e.getSource();

	}

	
	public void mouseReleased(MouseEvent e) {

	}

	
	public void keyPressed(KeyEvent e) {

   	if (e.getKeyCode() == KeyEvent.VK_TAB) {
			if (selComp == t_dirName || selComp == t_fileName) {
				selComp.setBackground(Color.WHITE);
				((JTextField) selComp).setEditable(false);
				((JTextField) selComp).getCaret().setVisible(false);
			}

			t_fileName.setBackground(Color.WHITE);
			// list.setSelectedIndex(-1);
			cBox.repaint();
			// if (saveAs)
			// text2.setBackground(Color.WHITE);

			if (selComp == cBox)
				cBox.setRequestFocusEnabled(false);
			if (selComp instanceof MyButton) {
				((MyButton) selComp).setSelected(false);
			}
			// selComp.setRequestFocusEnabled(false);
			selComp.setFocusable(false);

			// list.setSelectedIndex(-1);

			if (!shift)
				selComp = (JComponent) mtp.getComponentAfter(dialog, selComp);
			else
				selComp = (JComponent) mtp.getComponentBefore(dialog, selComp);

			if (selComp == butCopy && !saveAs)
				if (!shift)
					selComp = (JComponent) mtp.getComponentAfter(dialog,
							selComp);
				else
					selComp = (JComponent) mtp.getComponentBefore(dialog,
							selComp);

			if (selComp == t_dirName || selComp == t_fileName) {
				selComp.setBackground(vLightBlue);
				((JTextField) selComp).getCaret().setVisible(true);
				((JTextField) selComp).setEditable(true);

			}

			if (selComp == null) {
				selComp = list;
			}

			else if (selComp instanceof MyButton)
				((MyButton) selComp).setSelected(true);

			if (selComp instanceof MyComboBox) {

				cBox.setBackground(vLightBlue);
			}

			selComp.setFocusable(true);
			selComp.requestFocusInWindow();

		} else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			shift = true;
		} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			 if (selComp instanceof JList || selComp == t_dirName || selComp ==
			 t_fileName) {

			enterAction.actionPerformed(new ActionEvent(e, 0, ""));
			 }
		} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			// if (selComp instanceof JList) {

			cancelAction.actionPerformed(new ActionEvent(e, 0, ""));
			// }
		}

		//else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
			//if (selComp instanceof JList) {
		//	if (selComp == t_fileName) {

		//		deleteAction.actionPerformed(new ActionEvent(e, 0, ""));
		//	}
		//}

		else if (selComp == cBox
				&& ((e.getKeyCode() == KeyEvent.VK_UP) && driver.allFiles || (e
						.getKeyCode() == KeyEvent.VK_DOWN) && !driver.allFiles)) {

			driver.allFiles = !driver.allFiles;
			cBox.setSelectedIndex(driver.allFiles ? 1 : 0);

			return;
		}
		
		paintList();
		list.repaint();
		repaint();
	}

	
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			shift = false;
		}
		//selComp = (Component) e.getSource();
	}

	
	public void valueChanged(ListSelectionEvent e) {
		paintList();

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
			

			if (!(selComp instanceof JList) && selComp != t_fileName){
				
				return;
			}
			String s = null;
			if (selComp instanceof JList) {
				//String s = t_dirName.getText();
				//String t = t_fileName.getText();
				//if (!(t.equals("")))
				//	s += File.separator + t;
				//File f = new File(s);
				
				int rowNo = list.getSelectedIndex();
				if (nodeNames.length == 0 || rowNo == -1) {
					MyOptionPane.showMessageDialog(driver.frame,
							"Empty directory or no entry selected",
							MyOptionPane.ERROR_MESSAGE);
					return;
				}

				s = nodeNames[rowNo];
			} else {
				s = t_fileName.getText();
			}
				File f = new File(t_dirName.getText() + File.separator + s);
				if (f.isDirectory()) {
					if (f.list().length > 0) {
						MyOptionPane.showMessageDialog(driver.frame,
								"Folder '" + f.getName()
										+ "' not empty - cannot be deleted",
								MyOptionPane.ERROR_MESSAGE);

						return;
					}
				} else {
					if (-1 != driver.curDiag.diagramIsOpen(s)) {
						MyOptionPane.showMessageDialog(driver.frame,
								"File '" + f.getName()
										+ "' cannot be deleted while open",
								MyOptionPane.ERROR_MESSAGE);

						return;
					}
				}

				String u = f.isDirectory() ? "folder" : "file";

				if (MyOptionPane.YES_OPTION == MyOptionPane.showConfirmDialog(
						dialog,
						"Do you want to delete this " + u + ": "
								+ f.getAbsolutePath() + "?",
						"File/folder delete", MyOptionPane.YES_NO_OPTION)) {

					listHead = f.getParent();

					if (!f.exists()) {
						MyOptionPane.showMessageDialog(driver.frame,
								u + " " + f.getName() + " doesn't exist",
								MyOptionPane.ERROR_MESSAGE);
						// return;
					} else {
						f.delete();
						MyOptionPane.showMessageDialog(driver.frame,
								u + " " + f.getName() + " deleted",
								MyOptionPane.INFORMATION_MESSAGE);
					}

					// fullNodeName = listHead.getAbsolutePath();
					// showFileNames();
					t_dirName.setText(listHead);
					panel.validate();
					// panel.remove(listView);
					showList();

				}
			}
		 
	}

	class EnterAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e) {

			if (selComp instanceof MyButton) {
				// ((MyButton) selComp).getAction().actionPerformed(new
				// ActionEvent(e, 0, ""));
				if (selComp == butOK)
					return;
				((MyButton) selComp).doClick();
				return;
			}

			if (selComp == t_dirName) {
				String u = t_dirName.getText();
				File h = new File(u);
				if (!h.exists() || !h.isDirectory()) {
					MyOptionPane.showMessageDialog(driver.frame, "File " + u
							+ " either doesn't exist or is not a directory", MyOptionPane.ERROR_MESSAGE);

					return;
				}
				listHead = u;
				// panel.remove(listView);
				t_dirName.setBackground(vLightBlue);
				showList();
				return;
			}
			// if (selComp == text2) {
			// fullNodeName = text.getText() + File.separator
			// + text2.getText();
			// if (!saveAs)
			// processOK();
			// }

			butNF.setEnabled(!inJarTree && saveAs);
			butDel.setEnabled(!inJarTree);

			if (!((selComp instanceof JList) || selComp == t_fileName))
				return;

			String s = t_fileName.getText();

			//if (s == null || s.equals("")) {

				if (selComp instanceof JList) {

					int rowNo = list.getSelectedIndex();
					if (nodeNames.length == 0 || rowNo == -1) {
						MyOptionPane.showMessageDialog(driver.frame,
								"Empty directory or no entry selected",
								MyOptionPane.ERROR_MESSAGE);
						return;
					}

					s = nodeNames[rowNo];

					if (!s.equals("")) {
						File f = new File(
								t_dirName.getText() + File.separator + s);
						if (f.exists() && !f.isDirectory()) {
							t_fileName.setText(s);
							t_fileName.repaint();
						}
					}
					// t_fileName.setText(s);
				} else
					s = t_fileName.getText();
			//}

			if (s == null || s.equals("")) {
				MyOptionPane.showMessageDialog(driver.frame,
						"No file specified", MyOptionPane.ERROR_MESSAGE);
				return;
			}

			File f = null;

			if (/* s.startsWith("JavaFBP") && */ s.toLowerCase()
					.endsWith(".jar")) {
				butNF.setEnabled(false);
				butDel.setEnabled(false);
				// if (filter instanceof DrawFBP.JarFileFilter)
				if (fCParms == driver.curDiag.fCPArr[DrawFBP.JARFILE]
						|| fCParms == driver.curDiag.fCPArr[DrawFBP.JHALL]) {
					processOK();
					return;
				}

				jarTree = buildJarFileTree(s);
				inJarTree = true;
				butNF.setEnabled(!inJarTree && saveAs);
				butDel.setEnabled(!inJarTree);
				currentNode = jarTree;
				t_fileName.setText("");

				if (0 >= currentNode.getChildCount()) {
					MyOptionPane.showMessageDialog(driver.frame,
							"Error in jar file", MyOptionPane.ERROR_MESSAGE);
					return;
				}

				listHead = s + "!";
				t_dirName.setText(listHead);

				showList();

			} else if (!inJarTree) {

				if (s.equals(""))
					f = new File(listHead);
				else {
					// int i = listHead.lastIndexOf("package.json");
					// if (i > -1)
					// listHead = listHead.substring(0, i - 1);
					f = new File(DrawFBP.makeAbsFileName(s, listHead));
				}

				if (!f.exists()) {

					if (!saveAs)
						processOK();
					else 
						if (selComp != t_fileName) { 
							MyOptionPane.showMessageDialog(driver.frame,
								"Folder does not exist: "
										+ f.getAbsolutePath(), MyOptionPane.ERROR_MESSAGE);
							return;
						}
				}
				if (f.isDirectory()
						|| f.getName().toLowerCase().endsWith("package.json")) {

					listHead = f.getAbsolutePath();
					// showFileNames();

					// panel.remove(listView);
					showList();

				} else
					// if (!saveAs)
					processOK();
			} else { // inJarTree

				currentNode = findChild(currentNode, s);
				if (currentNode == null)
					return;
				if (currentNode.getChildCount() > 0)
					listHead = listHead + File.separator + s;
				if (0 < currentNode.getChildCount()) {

					// panel.remove(listView);
					showList();

				} else
					// if (!saveAs)
					processOK();

			}

			// }
			panel.validate();
			dialog.repaint();
			// frame.repaint();
		}

	}

	class CopyAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e) {
			// For now we will only shift from suggested file to text2

			t_fileName.setText(t_suggName.getText());
			// text3.setText(s);
			t_fileName.requestFocusInWindow();
			selComp.setBackground(Color.WHITE);
			selComp = t_fileName;
			//text2.setBackground(vLightBlue);
			panel.validate();
			list.repaint();

		}
	}

	class ParentAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e) {
			//t_fileName.setText("");

			if (!inJarTree) {
				listHead = (new File(listHead)).getParent();
				if (listHead == null)
					listHead = System.getProperty("user.home");

				t_dirName.setText(listHead);
				// text2.setText("");
				// fullNodeName = listHead;

			} else {
				String u = (String) currentNode.getUserObject();
				if (u == null) {
					inJarTree = false;
					currentNode = null;
				} else {

					currentNode = (DefaultMutableTreeNode) currentNode
							.getParent();
					u = listHead;
					int k = u.lastIndexOf(File.separator);
					u = u.substring(0, k);
					listHead = u;

				}
				if (!inJarTree) {
					listHead = listShowingJarFile;

				}
			}
			butNF.setEnabled(!inJarTree && saveAs);
			butDel.setEnabled(!inJarTree);
			// if (selComp instanceof MyButton) {
			butParent.setSelected(false);
			// }
			// if (listView != null)
			// panel.remove(listView);
			// dialog.repaint();
			showList();
			// showFileNames();
			// selComp = list;
			// rowNo = 0;
			// list.setSelectedIndex(0);
			listView.repaint();
			dialog.repaint();
			panel.validate();
			panel.repaint();
			//frame.repaint();
			//frame.repaint();
			t_dirName.repaint();
			repaint();

		}
	}
	class NewFolderAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e) {
			String fileName = (String) MyOptionPane
					.showInputDialog(dialog, "Enter new folder name", null);

			if (fileName != null) {
				String s = listHead;
				// String t = s;
				s += File.separator + fileName;
				File f = new File(s);

				boolean b = f.mkdirs();
				if (!b)
					MyOptionPane.showMessageDialog(driver.frame,
							"Folder not created: "
									+ f.getAbsolutePath(), MyOptionPane.ERROR_MESSAGE);
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

			Color c = (this == selComp) ? vLightBlue : Color.WHITE;
			
			int i = driver.allFiles ? 1 : 0;
			String lt = driver.curDiag.filterOptions[i];

			JLabel l = new JLabel(lt);
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
			if (aComponent == null || aComponent instanceof JList)
				idx = 3;
			else
				idx = order.indexOf(aComponent);

			idx = (idx + 1) % order.size();

			Component c = order.get(idx);
			return c;
		}
		public Component getComponentBefore(Container focusCycleRoot,
				Component aComponent) {

			int idx;
			if (aComponent == null || aComponent instanceof JList)
				idx = 3;
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
		
		public MyTextField(int i) {
			super(i);
		}

		public void paint(Graphics g) {
			super.paint(g);
			
			if (this == selComp) {
				setBackground(vLightBlue);
				setEditable(true);
				
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						requestFocusInWindow();	
						setEnabled(true);
						getCaret().setVisible(true);
					}
				});
				
			}
			else {
				setBackground(Color.WHITE);
				setEditable(false);
				getCaret().setVisible(false);
			}
			
		}
	}
	
	class MyButton extends JButton {

		private static final long serialVersionUID = 1L;

		public void paint(Graphics g) {
			super.paint(g);
			// if (isSelected())
			if (this == selComp)
				g.setColor(vLightBlue);
			else
				g.setColor(Color.WHITE);
			setOpaque(false);
			setFocusPainted(false);

		}
	}

	

	

}
