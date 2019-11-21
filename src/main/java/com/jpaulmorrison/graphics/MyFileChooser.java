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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;


public class MyFileChooser extends JFrame
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
	String listShowingJarFile = null;
	boolean inJarTree = false;
	JScrollPane listView = null;
	JPanel panel = null;
	int result = CANCEL_OPTION;

	DrawFBP driver = null;
	MyButton butParent = new MyButton();
	MyButton butOK = new MyButton();
	MyButton butCancel = new MyButton();
	MyButton butDel = new MyButton();
	MyButton butNF = new MyButton();
	JCheckBox butSortByDate = new JCheckBox("Sort ByDate");

	MyButton butCopy = new MyButton();

	MyTextField t_dirName = new MyTextField(100, "dir");
	MyTextField t_fileName = new MyTextField(100, "file");
	MyTextField t_suggName = new MyTextField(100, "sugg");

	JComponent selComp = null;
	// Component changedField = null;
	MyTraversalPolicy mtp;
	DefaultMutableTreeNode jarTree;
	DefaultMutableTreeNode currentNode;
	String folder;

	String[] nodeNames = null;
	String suggestedName = null;

	boolean clickState = true;
	// String fileExt = null;
	boolean shift = false;
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
	CopyAction copyAction;

	ParentAction parentAction;
	NewFolderAction newFolderAction;

	DrawFBP.FileChooserParm fCP;

	public ClickListener clickListener;

	public MyFileChooser(DrawFBP driver, File f, DrawFBP.FileChooserParm fcp) {

		this.fCP = fcp;
		clickListener = new ClickListener();

		if (f == null || !f.exists())
			listHead = System.getProperty("user.home");
		else
			listHead = f.getAbsolutePath();
		// fullNodeName = f.getAbsolutePath();
		this.driver = driver;		
				
		butSortByDate.setSelected(driver.sortByDate);
		
		Point p = driver.getLocation();
		setLocation(p.x + 50, p.y + 30);
		
	}

	int showOpenDialog(final boolean saveas, final boolean saving) {

		dialog = new JDialog(driver,
				JDialog.ModalityType.APPLICATION_MODAL);
		// dialog.setUndecorated(false);
		
		//Dimension dim = driver.getSize();
		//dialog.setMaximumSize(new Dimension(dim.width - 100, dim.height - 60));

		this.saveAs = saveas;
		this.saving = saving;

		dialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dialog.dispose();
			}
		});
		
		selComp = null;

		DrawFBP.applyOrientation(dialog);

		panel = new JPanel();
		panel.setLayout(new BorderLayout());

		driver.filterOptions[0] = fCP.title;
		cBox = new MyComboBox(driver.filterOptions);
		cBox.setMaximumRowCount(2);
		cBox.addMouseListener(this);
		cBox.setSelectedIndex(driver.allFiles ? 1 : 0);

		order = new Vector<Component>(10);
		order.add(t_dirName);
		order.add(butSortByDate);
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

		//t_dirName.setBackground(vLightBlue);
		t_dirName.setEditable(true);
		t_dirName.setEnabled(true);
		t_dirName.getCaret().setVisible(true);
		t_dirName.setRequestFocusEnabled(true);
		t_dirName.addActionListener(this);
		t_dirName.addMouseListener(this);

		// text.getDocument().addDocumentListener(this);

		t_fileName.setEditable(true);
		t_fileName.setEnabled(true);
		t_fileName.setRequestFocusEnabled(true);
		t_fileName.setBackground(vLightBlue);
		t_fileName.getCaret().setVisible(true);
		t_fileName.addActionListener(this);
		t_fileName.addMouseListener(this);

		t_fileName.setPreferredSize(new Dimension(100, driver.gFontHeight + 2));

		t_suggName.setEditable(false);
		t_suggName.setEnabled(true);
		// text3.setRequestFocusEnabled(true);
		t_suggName.setFont(driver.fontg.deriveFont(Font.ITALIC));
		// text3.setPreferredSize(new Dimension(100, driver.fontHeight + 2));

		String s = (saveAs) ? "Save or Save As" : "Open File";
		// comp = new MyFileCompare();
		renderer = new ListRenderer(driver);

		if (fCP == driver.diagFCParm)
			dialog.setTitle(s);
		else {
			if (fCP == driver.curDiag.fCParm[Diagram.NETWORK]) {
				//String w = driver.curDiag.diagFile.getAbsolutePath();
				fCP.prompt = "Specify file name";
			}

			dialog.setTitle(fCP.prompt);
			if (fCP == driver.curDiag.fCParm[Diagram.CLASS])
				listShowingJarFile = listHead;
		}

		enterAction = new EnterAction();
		copyAction = new CopyAction();
		cancelAction = new CancelAction();
		deleteAction = new DeleteAction();

		parentAction = new ParentAction();
		newFolderAction = new NewFolderAction();
		
		//butSortByDate = new JCheckBox("Sort By Date");
		//butSortByDate.setActionCommand("Toggle Click to Grid");
		butSortByDate.addActionListener(this);
		butSortByDate.setBackground(slateGray1);
		butSortByDate.setBorderPaintedFlat(false);
		butSortByDate.setActionCommand("Toggle Sort By Date");

		butParent.setAction(parentAction);
		butParent.setText("Parent Folder");
		butParent.setMnemonic(KeyEvent.VK_P);

		butNF.setAction(newFolderAction);
		butNF.setMnemonic(KeyEvent.VK_N);
		butNF.setText("New Folder");

		// butNF.setEnabled(false);

		// butOK.setAction(okAction);
		butOK.setAction(enterAction);
		butCopy.setAction(copyAction);
		butCancel.setAction(cancelAction);
		butDel.setAction(deleteAction);

		butParent.setRequestFocusEnabled(true);
		//if (saveAs)
			butNF.setRequestFocusEnabled(true);
		butCopy.setRequestFocusEnabled(true);

		t_dirName.addMouseListener(this);
		t_fileName.addMouseListener(this);

		panel.setPreferredSize(new Dimension(600, 600));

		t_dirName.setFocusTraversalKeysEnabled(false);
		butParent.setFocusTraversalKeysEnabled(false);
		butSortByDate.setFocusTraversalKeysEnabled(false);
		//if (saveAs)
			butNF.setFocusTraversalKeysEnabled(false);
		t_fileName.setFocusTraversalKeysEnabled(false);
		butOK.setFocusTraversalKeysEnabled(false);
		butDel.setFocusTraversalKeysEnabled(false);
		butCancel.setFocusTraversalKeysEnabled(false);
		butCopy.setFocusTraversalKeysEnabled(false);

		butParent.setFocusTraversalKeysEnabled(false);
		butSortByDate.setFocusTraversalKeysEnabled(false);
		// if (saveAs)
		butNF.setEnabled(true);
		butOK.setEnabled(true);
		// butCopy.setEnabled(saveAs);
		butCopy.setEnabled(true);
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

		//cBox.addActionListener(this);

		// cBox.setUI(new BasicComboBoxUI());
		cBox.setRenderer(new ComboBoxRenderer());

		// Dimension dim = new Dimension(1000, 800);
		// dialog.setPreferredSize(dim);

		dialog.setFocusTraversalKeysEnabled(false);
		
		t_dirName.addKeyListener(this);  // needed to service tab keys
		t_fileName.addKeyListener(this);  // needed to service tab keys
		t_suggName.addKeyListener(this);  // needed to service tab keys
		butSortByDate.addKeyListener(this); // needed to service tab keys
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

		//showList();
		if (saveAs) {

			if (suggestedName != null && !(suggestedName.equals(""))) {
				File h = new File(suggestedName);
				listHead = h.getParent();
				t_dirName.setText(listHead);
				//t_fileName.setText(h.getName());
				t_suggName.setText(h.getName());

				t_fileName.addAncestorListener(new RequestFocusListener(false));
				selComp = t_fileName;

			}

			if (driver.curDiag.title != null
					&& driver.curDiag.diagFile != null) {
				s += " (current file: "
						+ driver.curDiag.diagFile.getAbsolutePath() + ")";
			}
			showList();
		} else {
			t_dirName.setText(listHead);
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

	void getSelectedFile(String[] s) {

		s[0] = DrawFBP.makeAbsFileName(t_fileName.getText(),
				t_dirName.getText());
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
		
		//String x = t_dirName.getText();
		//t_dirName.setVisible(true);
		 
		String t = null;
		File f = new File(listHead);
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
				//if (!(f.exists()))
				//	return;
				//File f = new File(listHead);
				t = f.getAbsolutePath();
				if (t.endsWith("My Documents"))
					f = new File(t.replace("My Documents", "Documents"));
				t = t.replace("\\",  "/");
				if (!f.exists() || !f.isDirectory())
					ll.add("Folder does not exist or is not directory");
					//return;					
				}
			else 
				inJarTree = true;
				
			 
				//else {
				//	return;

				if (!inJarTree) {
					if (listHead.equals(listShowingJarFile)) {
						t = driver.javaFBPJarFile;
						String v = "";
						try {
							File f2 = new File(t);
							v = Files.getLastModifiedTime(f2.toPath()).toString();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}	
						int i = v.lastIndexOf(".");
						if (i > -1)
							v = v.substring(0, i);
						v = v.replace("T", " ");
						ll.add(t + "@" + v);
						for (String u : driver.jarFiles.values()) {
							if (new File(u).exists()) {
								try {
									File f2 = new File(u);
									v = Files.getLastModifiedTime(f2.toPath()).toString();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}	
								i = v.lastIndexOf(".");
								if (i > -1)
									v = v.substring(0, i);
								v = v.replace("T", " ");
								ll.add(u+ "@" + v);
							}
						}
					}
				//}

				//String[] fl = f.list();
				//if (!f.exists()) {
				//	ll.add("Folder does not exist");
				//}
				//else {
					
				Path p = f.toPath();
				if (!f.exists()){
					MyOptionPane.showMessageDialog(driver,	"File does not exist: " + f.getAbsolutePath(),
							MyOptionPane.ERROR_MESSAGE);
					
				}

				else {
					
				DirectoryStream<Path> ds = null;
				try {
					ds = Files.newDirectoryStream(p);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				

				ll2 = new LinkedList<String>();

				for (Path entry : ds) {	
					String fs = entry.toString();

					File fx = new File(fs);
					if (!fx.exists())
						continue;
					if (fx.isDirectory())
						ll2.add(fs); // directories go into ll first

				}
				ll.addAll(mySort(ll2)); // add elements of ll2 to ll in sorted
										// order

				ll2.clear();
				try {
					ds = Files.newDirectoryStream(p);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				for (Path entry : ds) {
					String fs = entry.toString();					

					File fx = new File(fs);					
					if (!fx.exists())
						continue;
					if (!fx.isDirectory() /* && (!(fn.startsWith("."))) */
							&& (fCP.filter.accept(fx) || driver.allFiles))
						ll2.add(entry.toString()); // non-directories go into ll2,
										// which is
										// then sorted into ll

				}

				
				ll.addAll(mySort(ll2)); // add elements of ll2 to end of ll  
									 
				}					 

			} else {
				//inJarTree = true;

				if (currentNode == null)
					return;

				ll = new LinkedList<String>();

				ll2 = new LinkedList<String>();
								
				if (currentNode.getChildCount()  > 0) {

					t_dirName.setText(listHead);
					Enumeration<DefaultMutableTreeNode> e = currentNode.children();
				
					while (e.hasMoreElements()) {
						DefaultMutableTreeNode node = (e.nextElement());
						t = (String) node.getUserObject();					
						ll2.add((String) t);
					}
					ll.addAll(mySort(ll2)); // add elements of ll2 to end of ll in
										// sorted order
					}
				else {
					
					selComp = t_fileName;
				}
			}
		//}
		if (ll.size() == 0)
		    ll.add("No files match criteria");

		Object[] oa = ll.toArray();

		int k = 0;

		nodeNames = new String[oa.length];
		for (int j = 0; j < oa.length; j++) {
			if (oa[j] == null) // not sure where null came from, but it crashed
								// one test!
				continue;
			nodeNames[j] = (String) oa[j];
			if (nodeNames[j].endsWith(".jar"))
				k = k + 1; // get rid of spurious "unused" message
		}

		
		list = new JList<String>(nodeNames);
		// list.setSelectedIndex(k);
		list.setSelectedIndex(-1);

		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		list.addKeyListener(this);
		//ClickListener cL = new ClickListener();
		list.addMouseListener(clickListener);
		//ListSelectionListener lsl = new ListSelectionListener();  
		//list.addListSelectionListener(this);
		//ListSelectionModel listSelectionModel = list.getSelectionModel();
	    //listSelectionModel.addListSelectionListener(
	    //                        new SharedListSelectionHandler());
		//ListSelectionModel sM = list.getSelectionModel();
		//sM.setValueIsAdjusting(true);
		list.setFocusTraversalKeysEnabled(false);

		//order.remove(3);
		//order.add(3, list);
		order.remove(4);   // check!
		order.add(4, list);
		// list.setFixedCellHeight(driver.fontg.getSize() + 2);

		// list.setFixedCellHeight(14);
		// list.setFixedCellWidth(60);

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
		
		if (selComp != t_dirName)
			selComp = list;
		// list.setSelectedIndex(0);
		list.setFocusable(true);

		list.setFixedCellHeight(22);

		list.setVisible(true);
		list.requestFocusInWindow();		
		
		}

		//dialog.pack();
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
	
	/*
	 * Build tree of nodes (DefaultMutableTreeNode) using contents of jar file
	 */

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
				// System.out.println(entry);

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

final boolean SAVEAS = true;

	@SuppressWarnings("unchecked")
	LinkedList<String> buildListFromJSON(String fileName) {
		int level = 0;
		File f = new File(fileName);
		String fileString;
		LinkedList<String> ll = new LinkedList<String>();
		if (null == (fileString = driver.readFile(f  /*, !SAVEAS */))) {
			MyOptionPane.showMessageDialog(driver,	"Unable to read file " + f.getName(),
					MyOptionPane.ERROR_MESSAGE);
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
					if (k2.equals("graphs")
							&& fCP == driver.curDiag.fCParm[Diagram.DIAGRAM]
							|| k2.equals("components")
									&& fCP == driver.curDiag.fCParm[Diagram.NETWORK]
							|| fCP == driver.curDiag.fCParm[Diagram.PROCESS]) {
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
			MyOptionPane.showMessageDialog(driver,
					"No components or graphs in file: " + f.getName(),
					MyOptionPane.ERROR_MESSAGE);
			// return null;
		}

		return ll;
	}

	/*
	LinkedList<String> sortByName(LinkedList<String> from) {
		if (from.isEmpty()) {
			return new LinkedList<String>(); // return empty list

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
				File f = new File(listHead + "/" + low); 				
				Path path = f.toPath();
				String curDate = Files.getLastModifiedTime(path).toString();
				low += "!" + curDate;
				lkl.add(low);

				ll.remove(low_i);
			}

			catch (NoSuchElementException e) {
				return lkl;

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	*/
	LinkedList<String> mySort(LinkedList<String> from) {
		
		// Collections.sort sorts in place - that's OK!		
		//int lhl = listHead.length();
		LinkedList<String> ll = new LinkedList<String>();
		for (String s : from) {
			//File f = new File(listHead + "/" + s); 
			
			if (!inJarTree) {
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
			
				
		if (!inJarTree && driver.sortByDate)
			Collections.sort(ll, compDate);
		else
			Collections.sort(ll, compName);
		
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

			JPanel jp = new JPanel();
			
			BorderLayout gb = new BorderLayout();
			jp.setLayout(gb);			

			jp.setBackground(Color.WHITE);
			JLabel name = new JLabel();
			JLabel date = new JLabel();
			
			//if (driver.sortByDate) {
			//	jp.add(Box.createHorizontalGlue());				
			//}
			if (!inJarTree) {
				jp.add(date, BorderLayout.WEST);
				jp.add(Box.createRigidArea(new Dimension(20, 0)));
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
			
			if (s == null)
				name.setBackground(vLightBlue);

			else if (s.indexOf(".jar@") > -1 || inJarTree)
				name.setBackground(goldenRod);
			else
				name.setBackground(vLightBlue);

			if (isSelected) {
				if (s.indexOf(".jar@") > -1 || inJarTree)
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
				
				if (!inJarTree)
					date.setIcon(icon);
				else
					name.setIcon(icon);
				int i = s.indexOf("@");
				
				if (inJarTree) {
					name.setText(s);
					date.setText(blanks);
				}
				else  if (i > -1){
					name.setText(s.substring(0, i));	
					String t = s.substring(i + 1);
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
			
		if (e.getSource() == t_dirName){
			selComp = t_dirName;  
		}
		
		if (e.getSource() == butSortByDate){
			String s = e.getActionCommand();
			if (s.equals("Toggle Sort By Date")){
				driver.sortByDate = !driver.sortByDate;
				butSortByDate.setSelected(driver.sortByDate);
				driver.saveProp("sortbydate",(new Boolean(driver.sortByDate)).toString());
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
		 
		if (selComp == t_dirName || selComp == t_fileName) {
			selComp.setBackground(Color.WHITE);
			((JTextField) selComp).setEditable(false);
			((JTextField) selComp).getCaret().setVisible(false);

		}
         
		//t_fileName.setBackground(Color.WHITE);
		cBox.repaint();

		if (selComp instanceof MyButton) {  // if previous selComp referred to button...
			((MyButton) selComp).setSelected(false);
			((MyButton) selComp).setFocusable(false);

		}

		selComp = (JComponent) e.getSource();
		repaint();

		if (selComp == t_dirName || selComp == t_fileName) {

			//selComp.setBackground(vLightBlue);
			selComp.setRequestFocusEnabled(true);
			((MyTextField) selComp).getCaret().setVisible(true);
			((MyTextField) selComp).setEditable(true);
			((MyTextField) selComp).requestFocusInWindow();

		} 
		//else {
			//selComp.setRequestFocusEnabled(true);

			//((MyTextField) selComp).setBackground(Color.WHITE);
			//((MyTextField) selComp).getCaret().setVisible(false);
			//((MyTextField) selComp).setEditable(false);
			//t_fileName.requestFocusInWindow();
		//
		//}
	
		if (selComp == t_dirName)
			t_fileName.setText("");
		
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
		//	if (selComp == t_dirName || selComp == t_fileName) {
		//		selComp.setBackground(Color.WHITE);
		//		((JTextField) selComp).setEditable(false);
		//		((JTextField) selComp).getCaret().setVisible(false);
		//	}

		//	t_fileName.setBackground(Color.WHITE);
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

			//if (selComp == t_dirName || selComp == t_fileName) {
			//	selComp.setBackground(vLightBlue);
			//	((JTextField) selComp).getCaret().setVisible(true);
			//	((JTextField) selComp).setEditable(true);

			//}

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
			return;

		} 
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			shift = true;
			return;
		} 
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			if (selComp instanceof JList /*|| selComp == t_dirName */
					|| selComp == t_fileName) {

				enterAction.actionPerformed(new ActionEvent(e, 0, ""));
			}
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
		
		repaint();
	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			shift = false;
		}
		
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

				int rowNo = list.getSelectedIndex();
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
				for (Entry<String, String> entry : driver.jarFiles.entrySet()) {
					// (entry.getValue().equals(s)) {
					if (entry.getValue().startsWith(s)) {
						t = entry.getKey();
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
			t_dirName.setText(listHead);
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

			butNF.setEnabled(!inJarTree /*&& saveAs */);
			butDel.setEnabled(!inJarTree);

			if (!((selComp instanceof JList) || selComp == t_fileName))
				return;

			String s = t_fileName.getText();

			// if (s == null || s.equals("")) {

			if (selComp instanceof JList) {
				t_fileName.setText("");	

				int rowNo = list.getSelectedIndex();
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

				
				 
				} else {
				if (selComp == t_fileName) {
					s = t_fileName.getText();
					String s2 = s;
					if (!s.endsWith(".jar"))
						s2 = t_dirName.getText() + "/" + s;
					File f = new File(s2);

					if (!f.exists() && !inJarTree) {
						if (-1 == s.indexOf(".")) {
							// must be a directory

							MyOptionPane.showMessageDialog(driver,
									"Folder " + f.getAbsolutePath()
											+ " doesn't exist - create using New Folder",
									MyOptionPane.ERROR_MESSAGE);
							return;

						} 
						/*
						else {
							// must be a file
							if (MyOptionPane.YES_OPTION == MyOptionPane
									.showConfirmDialog(driver,
											"Create new file: "
													+ f.getAbsolutePath() + "?",
											"Confirm create",
											MyOptionPane.YES_NO_OPTION))
								try {
									f.createNewFile();
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}

						}
						*/
					} else if (selComp == t_dirName) {
						listHead = t_dirName.getText();
						showList();
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

			
			File f = null;

			if (s.toLowerCase().endsWith(".jar")) {
				butNF.setEnabled(false);
				butDel.setEnabled(false);
				// if (filter instanceof DrawFBP.JarFileFilter)
				if (fCP == driver.curDiag.fCParm[Diagram.JARFILE]
						|| fCP == driver.curDiag.fCParm[Diagram.JHELP]) {
					processOK();
					return;
				}

				jarTree = buildJarFileTree(s);
				inJarTree = true;
				butNF.setEnabled(!inJarTree /*&& saveAs */);
				butDel.setEnabled(!inJarTree);
				currentNode = jarTree;
				t_fileName.setText("");

				if (0 >= currentNode.getChildCount()) {
					MyOptionPane.showMessageDialog(driver,
							"Error in jar file", MyOptionPane.ERROR_MESSAGE);
					return;
				}

				listHead = s + "!";   //?????????????
				t_dirName.setText(listHead);

				showList();

			} else if (!inJarTree) {

				if (s.equals(""))
					f = new File(listHead);
				else {
					// int i = listHead.lastIndexOf("package.json");
					// if (i > -1)
					// listHead = listHead.substring(0, i - 1);
					//f = new File(DrawFBP.makeAbsFileName(s, listHead));
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
						|| f.getName().toLowerCase().endsWith("package.json")) {

					listHead = f.getAbsolutePath();
					t_dirName.setText(listHead);
					
					showList();

				} else
					processOK();
				
			} else { // inJarTree
				
				String w = list.getSelectedValue();
				
				currentNode = findChild(currentNode, w);
				if (currentNode == null)
					return;
				if (currentNode.getChildCount() > 0) {
					listHead = listHead + "/" + s;
					//listHead = s;
					t_dirName.setText(listHead);
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
			// text2.setBackground(vLightBlue);
			panel.validate();
			repaint();

		}
	}

	class ParentAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e) {
			t_fileName.setText("");

			if (!inJarTree) {
				listHead = (new File(listHead)).getParent();
				if (listHead == null)
					listHead = System.getProperty("user.home");

				t_dirName.setText(listHead);
				

			} else {
				String u = (String) currentNode.getUserObject();
				if (u == null) {
					inJarTree = false;
					currentNode = null;
				} else {

					currentNode = (DefaultMutableTreeNode) currentNode
							.getParent();
					u = listHead;
					int k = u.lastIndexOf("/");
					u = u.substring(0, k);
					listHead = u;
					
				}
				if (!inJarTree) {
					listHead = listShowingJarFile;

				}
			}
			butNF.setEnabled(!inJarTree /* && saveAs */);
			butDel.setEnabled(!inJarTree);
			// if (selComp instanceof MyButton) {
			butParent.setSelected(false);
			
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
		String name = null;

		public MyTextField(int i, String fldName) {
			super(i);
			name = fldName;
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			// System.out.println("MTF");
			if (this == selComp) {
				setBackground(vLightBlue);
				setEditable(true);
				setRequestFocusEnabled(true);

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						setFocusable(true);
						requestFocusInWindow();
						setEnabled(true);
						getCaret().setVisible(true);
					}
				});

			} else {
				setBackground(Color.WHITE);
				setEditable(false);
				getCaret().setVisible(false);
			}

		}

	}

	class MyButton extends JButton {

		private static final long serialVersionUID = 1L;

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

		MouseEvent lastEvent;
		int rowNo;

		public void mouseClicked(MouseEvent e)

		{

			// System.out.println(e.getClickCount());
			if (e.getClickCount() > 2)
				return;

			lastEvent = e;

			//firstClick(lastEvent);

			if (e.getClickCount() == 2) {
				secondClick(lastEvent);
			}
			else firstClick(lastEvent);
			dispose();
		}

		//public void actionPerformed(ActionEvent e) {

		//	firstClick(lastEvent);
		//}

		public void firstClick(MouseEvent e) {

			selComp = list;
			rowNo = -1;
			
			rowNo = list.locationToIndex(e.getPoint());
			
			list.setRequestFocusEnabled(true);

			list.setSelectedIndex(rowNo);
			
			if (rowNo == -1 || nodeNames[rowNo].equals("(empty folder"))
				return;

			list.setSelectedIndex(rowNo);
			list.repaint();

			//selComp = t_fileName;
			String t = (String) list.getSelectedValue();
			int i = t.indexOf("@");
			if (i > -1)
				t = t.substring(0, i);
			
			//t_fileName.setText(t);
			if (!inJarTree) {
				String t2 = t;
				if (!t.equals("")) {
					if (!t.endsWith(".jar")) {
						t2 = t_dirName.getText() + "/" + t;
						File f = new File(t2);
						 
						if (!f.exists()) {
							if (-1 < t.lastIndexOf(".")) // if file
								MyOptionPane.showMessageDialog(driver,
										"File does not exist: "
												+ f.getAbsolutePath(),
										MyOptionPane.ERROR_MESSAGE);
							//else // if folder, OK to mkdir
							//	f.mkdir();
							// return;
						} else {
							if (!f.isDirectory()) {
								  t_fileName.setText(f.getName());
								  selComp = t_fileName;
							}
						}
					}
				}
			}
			repaint();
		}
		
		public void secondClick(MouseEvent e) {
			
			int n;
			for (n = list.getFirstVisibleIndex(); n <= list.getLastVisibleIndex(); n++) {
				Rectangle r = list.getCellBounds(n, n);
				if (r.contains(e.getPoint())) {
					if (n > -1)
						break;
				}
			}
			if (rowNo == n) {
				selComp = t_fileName;        
				String w = t_dirName.getText();

				String v = list.getSelectedValue();
				if (!inJarTree) {
					int j = v.indexOf("@");
					if (j > -1)
						v = v.substring(0, j);

				}
				
				// if selected name has a suffix or is in jar tree
				if (-1 < v.indexOf(".") || inJarTree) {
					t_fileName.setText(v);
					enterAction.actionPerformed(new ActionEvent(e, 0, ""));
				} else {  // folder name AND not injartree
					//if (inJarTree)
					//	enterAction.actionPerformed(new ActionEvent(e, 0, ""));
					//else {
						w += "/" + v;
						File f2 = new File(w);
						if (!f2.exists())
							MyOptionPane.showMessageDialog(driver,
								"File does not exist: "
										+ f2.getAbsolutePath(),
								MyOptionPane.ERROR_MESSAGE);
						t_dirName.setText(w);
						listHead = w;
						showList();
					//}
				}
				//} else
				//	showList();
				repaint();
				
			}
		}
	}
	
	/*
	//public class SharedListSelectionHandler() implements ListSelectionListener { 
	public void valueChanged(ListSelectionEvent e) {
		int row=e.getFirstIndex();
        //if (row==previousRow) {
        //    row=-1;
        //}
		if (!e.getValueIsAdjusting() && row > -1){
            @SuppressWarnings("unchecked")
			JList<String> source = (JList<String>)e.getSource();
            String v = source.getSelectedValue().toString();
            if (!inJarTree) {
				int j = v.indexOf("@");
				if (j > -1)
					v = v.substring(0, j);
			}
         // if selected name has a suffix or is in jar tree
			if (-1 < v.indexOf(".") || inJarTree) {
				t_fileName.setText(v);
				enterAction.actionPerformed(new ActionEvent(e, 0, ""));
			} else {  // folder name AND not injartree
				//if (inJarTree)
				//	enterAction.actionPerformed(new ActionEvent(e, 0, ""));
				//else {
				String w = t_dirName.getText();
					w += "/" + v;
					File f2 = new File(w);
					if (!f2.exists()){
						MyOptionPane.showMessageDialog(driver,
							"File does not exist: "
									+ f2.getAbsolutePath(),
							MyOptionPane.ERROR_MESSAGE);
					} else {
					t_dirName.setText(w);
					listHead = w;
					showList();
				 }
			}
			//} else
			//	showList();
			repaint();
			//previousRow = row;
        }
	}
	*/
	//}
  }