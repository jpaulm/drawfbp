package com.jpaulmorrison.graphics;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Image; 
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.*;

//import java.awt.geom.RoundRectangle2D;


import math.geom2d.line.DegeneratedLine2DException;
import math.geom2d.line.Line2D;
import math.geom2d.Point2D;
import math.geom2d.line.StraightLine2D;

import java.awt.image.*;

import javax.swing.*;
import javax.swing.event.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.help.JHelp;
import javax.imageio.ImageIO;

import java.lang.reflect.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FontUIResource;

public class DrawFBP extends JFrame
		implements
			ActionListener,
			ComponentListener, 
			ChangeListener 
			{

	static final long serialVersionUID = 111L;
	//private static final DrawFBP DrawFBP = null;
	DrawFBP driver = this;

	JLabel diagDesc;

	JTextField jfl = null;
	JTextField jfs = null;
	JTextField jfv = null;

	JLabel scaleLab;

	Diagram curDiag = null;
	
	File currentImageDir = null;

	//JFrame this;

	Block blockSelForDragging = null;

	BufferedImage buffer = null;

	// int maxX, maxY;

	Graphics2D osg;

	// SelectionArea area;
	int gFontWidth, gFontHeight;

	// Enclosure enclSelForDragging = null;
	Enclosure enclSelForArrow = null;

	File propertiesFile = null;
	HashMap<String, String> properties = null;
	HashMap<String, String> startProperties = null;
	HashMap<String, String> propertyDescriptions = null; // description is key

	JTabbedPaneWithCloseIcons jtp;

	String javaFBPJarFile = null;
	String jhallJarFile = null;

	Block selBlockM = null; // used only when mousing over
	Block selBlock = null; // permanent select
	Arrow selArrow = null; // permanent select
	String generalFont = null;
	String fixedFont = null;
	Font fontf = null;
	Font fontg = null;

	float defaultFontSize;
	GenLang currLang;

	static double scalingFactor;
	// int xTranslate = 0; // 400;
	// int yTranslate = 0; // 400;

	BasicStroke bs;

	// boolean propertiesChanged = false;

	ImageIcon favicon = null;
	// String zipFileName;
	int ox = 0; // enclosure being dragged - cx
	int oy = 0; // enclosure being dragged - cy
	int ow = 0; // enclosure being dragged - width
	int oh = 0; // enclosure being dragged - height

	String diagramName = null;

	Arrow arrowEndForDragging = null; /// only used for Drag Head and Drag Tail

	Bend bendForDragging = null;
	CloseTabAction closeTabAction = null;
	CloseAppAction closeAppAction = null;
	EscapeAction escapeAction = null;
	// UpAction upAction = null;

	KeyStroke escapeKS = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);

	String blockType = Block.Types.PROCESS_BLOCK;

	FoundPoint arrowRoot = null; // this is used to draw blue circle where
									// arrows can start
	FoundPoint arrowEnd = null; // this is used to draw black square where
								// arrows can end
	// Arrow foundArrow = null;
	Arrow currentArrow = null;
	Block foundBlock;

	int curx, cury;

	GenLang genLangs[];

	//FileChooserParm[] fCPArray = new FileChooserParm[10];

	JCheckBox grid;

	boolean leftButton;
	
	boolean sortByDate;  // remember across invocations of MyFileChooser

	static final int gridUnitSize = 4; // can be static - try for now

	static final double FORCE_VERTICAL = 20.0; // can be static as this is a
												// slope

	static final double FORCE_HORIZONTAL = 0.05; // can be static as this is a
													// slope

	static final int CREATE = 1;
	static final int MODIFY = 2;

	//public static final String Side = null;

	static enum Corner {
		TOPLEFT, BOTTOMLEFT, TOPRIGHT, BOTTOMRIGHT
	}

	// Side side;

	ImageIcon leafIcon = null;
	ImageIcon javaIcon = null;
	ImageIcon jarIcon = null;
	ImageIcon folderIcon = null;
	ImageIcon classIcon = null;

	Class<?> jHelpClass = null;
	Class<?> helpSetClass = null;

	boolean panSwitch = false;
	int panX, panY;
	Cursor openPawCursor = null;
	Cursor closedPawCursor = null;
	Cursor drag_icon = null;

	// "Subnet" is not a separate block type (it is a variant of "Process")

	String blockNames[] = {"Process", "Initial IP", "Enclosure", "Subnet",
			"ExtPorts: In", "... Out", "... Out/In", "Legend", "File", "Person",
			"Report"};

	String blockTypes[] = {Block.Types.PROCESS_BLOCK, Block.Types.IIP_BLOCK,
			Block.Types.ENCL_BLOCK, Block.Types.PROCESS_BLOCK,
			Block.Types.EXTPORT_IN_BLOCK, Block.Types.EXTPORT_OUT_BLOCK,
			Block.Types.EXTPORT_OUTIN_BLOCK, Block.Types.LEGEND_BLOCK,
			Block.Types.FILE_BLOCK, Block.Types.PERSON_BLOCK,
			Block.Types.REPORT_BLOCK};

	HashMap<String, String> jarFiles = new HashMap<String, String>();
	HashMap<String, String> dllFiles = new HashMap<String, String>();

	// JPopupMenu curPopup = null; // currently active popup menu

	// String scale;
	// boolean tryFindJarFile = true;
	boolean willBeSubnet = false;

	JMenuBar menuBar = null;
	JMenu fileMenu = null;
	JMenu editMenu = null;
	JMenu helpMenu = null;

	JMenuItem gNMenuItem = null;
	JMenuItem[] gMenu = null;
	JMenuItem menuItem1 = null;
	JMenuItem menuItem2j = null;
	JMenuItem menuItem2c = null;
	JMenuItem compMenu = null;
	JMenuItem runMenu = null;

	JTextField jtf = new JTextField();

	boolean allFiles = false;
	// int wDiff, hDiff;
	JComponent jHelpViewer = null;
	MyFontChooser fontChooser;
	boolean gFontChanged, fFontChanged;

	static Color lg = new Color(240, 240, 240); // very light gray
	static Color slateGray1 = new Color(198, 226, 255);
	static Color ly = new Color(255, 255, 200); // light yellow
	static Color lb = new Color(200, 255, 255); // light blue (turquoise
												// actually)
	static Color grey = new Color(170, 244, 255); // sort of bluish grey (?)
	// JDialog popup = null;
	JDialog popup2 = null;
	JDialog depDialog = null;

	static enum Side {
		LEFT, TOP, RIGHT, BOTTOM
	}
	// static boolean READFILE = true;

	Cursor defaultCursor = null;
	boolean use_drag_icon = false;

	JLabel zoom = new JLabel("Zoom");
	JCheckBox pan = new JCheckBox("Pan");
	JButton up = new JButton("Go to Directory");

	JRadioButton[] but = new JRadioButton[11];
	Box box21 = null;

	//Timer ttStartTimer = null;
	//Timer ttEndTimer = null;
	//boolean drawToolTip = false;
	boolean gotDllReminder = false;
	
	FileChooserParm diagFCParm = null;
	String[] filterOptions = {"", "All (*.*)"};
	//volatile boolean finished = false;
	//String clsDir = null;
	String progName = null;
	String output = "";
	String error = "";
	
	String[] pBCmdArray = null;
	String pBDir = null;

	// constructor
	DrawFBP(String[] args) {
		
		properties = new HashMap<String, String>();
		startProperties = new HashMap<String, String>();
		readPropertiesFile();
		if (args.length == 1) {
			diagramName = args[0];
			diagramName = diagramName.replace("\\", "/");
			if (diagramName.indexOf("/") == -1){
				final String dir = System.getProperty("user.dir");				
				diagramName = dir + "/" + diagramName;
			}
			//System.out.println("Diagram: " + diagramName );
			File f = new File(diagramName);
			if (!f.exists())
			//	System.out.println("Diagram: " + diagramName + "can't be found" );
				diagramName = null;
		}
		else {
			diagramName = properties.get("currentDiagram");
		}
		
		try {
		scalingFactor = 1.0d;
		driver = this;

		diagDesc = new JLabel("  ");
		grid = new JCheckBox("Grid");

		

		genLangs = new GenLang[]{
				new GenLang("Java", "java", new JavaFileFilter()),
				new GenLang("C#", "cs", new CsharpFileFilter()),
				new GenLang("JSON", "json", new JSONFilter()),
				new GenLang("FBP", "fbp", new FBPFilter())};

		Lang lang0[] = new Lang[]{new Lang("Java", "java")
				// , new Lang("Groovy", "groovy"), new Lang("Scala", "scala")
		};
		genLangs[0].langs = lang0;

		Lang lang1[] = new Lang[]{new Lang("C#", "cs")};
		genLangs[1].langs = lang1;

		Lang lang2[] = new Lang[]{new Lang("JSON", "json")};
		genLangs[2].langs = lang2;

		Lang lang3[] = new Lang[]{new Lang("FBP", "fbp")};
		genLangs[3].langs = lang3;

		
		

		createAndShowGUI();
		} catch (NullPointerException e)
		{   e.printStackTrace();
			saveProperties();
		}
		
		
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	private void createAndShowGUI() {

		// Create and set up the window.
		
		

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		// label = new JLabel(" ");

		//this = this;
		setTitle("DrawFBP Diagram Generator");
		// SwingUtilities.updateComponentTreeUI(this);
		// this = new JFrame("DrawFBP Diagram Generator");
		setUndecorated(false); // can't change size of JFrame title,
										// though!
		defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		setCursor(defaultCursor);
		
		applyOrientation(this);

		int w = (int) dim.getWidth();
		int h = (int) dim.getHeight();
		// maxX = (int) (w * .8);
		// maxY = (int) (h * .8);
		buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);  // xxxxxxxxxxxx
		// osg = buffer.createGraphics();
		osg = (Graphics2D) buffer.getGraphics();
		//osg = (Graphics2D) getGraphics();
		// setVisible(true);

		// osg = (Graphics2D) getGraphics();

		// http://www.oracle.com/technetwork/java/painting-140037.html

		bs = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, // width 1.5
				BasicStroke.JOIN_ROUND);
		osg.setStroke(bs);

		// UIDefaults def = UIManager.getLookAndFeelDefaults();
		// UIDefaults def = UIManager.getDefaults();

		// osg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		// RenderingHints.VALUE_ANTIALIAS_ON);
		
		diagFCParm = new FileChooserParm("Diagram", "currentDiagramDir",
				"Specify diagram name in diagram directory", ".drw",
				new DiagramFilter(), "Diagrams (*.drw)");	

		RenderingHints rh = new RenderingHints(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		rh.put(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		rh.put(RenderingHints.KEY_STROKE_CONTROL,
				RenderingHints.VALUE_STROKE_NORMALIZE);
		rh.put(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		rh.put(RenderingHints.KEY_DITHERING,
				RenderingHints.VALUE_DITHER_ENABLE);
		rh.put(RenderingHints.KEY_FRACTIONALMETRICS,
				RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
		osg.setRenderingHints(rh);

		//readPropertiesFile();

		saveProp("versionNo", "v" + VersionAndTimestamp.getVersion());
		//saveProp("date", VersionAndTimestamp.getDate());
		
		//LocalDateTime date = LocalDateTime.now();
		//DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

		//String formattedDate = formatter.format(date); 
		LocalDateTime a = LocalDateTime.from(ZonedDateTime.now());
		saveProp("date", a.toString());

		if (null == (generalFont = properties.get("generalFont"))){
			generalFont = "Arial";
			saveProp("generalFont", generalFont);
		}
		if (null == (fixedFont = properties.get("fixedFont"))){
			fixedFont = "Courier";
			saveProp("fixedFont", fixedFont); 
		}

		String dfs = properties.get("defaultFontSize");
		if (dfs == null) {
			defaultFontSize = 14.0f;
			dfs = "14.0";
		} else
			defaultFontSize = Float.parseFloat(dfs);

		saveProp("defaultFontSize", dfs);

		String dcl = properties.get("defaultCompLang");
		// if (dcl.equals("NoFlo")) // transitional!
		// dcl = "JSON";
		if (dcl == null) {
			currLang = findGLFromLabel("Java");
			saveProperties();
		} else {
			if (dcl.equals("NoFlo")) // transitional!
				dcl = "JSON";
			currLang = findGLFromLabel(dcl);
		}
		
		String sBD = properties.get("sortbydate");
		if (sBD == null) {
			sortByDate = false;
			saveProp("sortbydate", "false");
		} else 
			sortByDate = (new Boolean(sBD)).booleanValue();

		Iterator<Entry<String, String>> entries = jarFiles.entrySet()
				.iterator();
		String z = "";
		String cma = "";

		while (entries.hasNext()) {
			Entry<String, String> thisEntry = entries.next();
			z += cma + thisEntry.getKey() + ":" + thisEntry.getValue();
			cma = ";";
		}
		saveProp("additionalJarFiles", z);
		
		entries = dllFiles.entrySet().iterator();
		z = "";
		cma = "";

		while (entries.hasNext()) {
			Entry<String, String> thisEntry = entries.next();
			z += cma + thisEntry.getKey() + ":" + thisEntry.getValue();
			cma = ";";
		}
		saveProp("additionalDllFiles", z);

		startProperties = new HashMap<String, String>();
		for (String s : properties.keySet()) {
			startProperties.put(s, properties.get(s));
		}

		fontg = new Font(generalFont, Font.PLAIN, (int) defaultFontSize);

		// read/create time
		fontf = new Font(fixedFont, Font.PLAIN, (int) defaultFontSize);
		osg.setFont(fontg);

		FontMetrics metrics = null;

		metrics = osg.getFontMetrics(fontg);

		gFontWidth = metrics.charWidth('n'); // should be the average!
		gFontHeight = metrics.getAscent() + metrics.getLeading();

		jfl = new JTextField("");

		jfl.setText(
				"Fixed font: " + fixedFont + "; general font: " + generalFont);

		jfs = new JTextField("");

		jfs.setText("Font Size: " + defaultFontSize);

		jfv = new JTextField();

		jfv.setText("Ver: " + VersionAndTimestamp.getVersion());

		jtp = new JTabbedPaneWithCloseIcons(this);
		//int i = jtp.getTabCount(); 

		jtp.setForeground(Color.BLACK);
		jtp.setBackground(Color.WHITE);

		BufferedImage image = loadImage("DrawFBP-logo-small.png");

		if (image != null) {
			favicon = new ImageIcon(image);
			setIconImage(image);

		} else {
			MyOptionPane.showMessageDialog(this,
					"Couldn't find file: DrawFBP-logo-small.png",
					MyOptionPane.ERROR_MESSAGE);
			// return null;
		}

		// repaint();
		// update(getGraphics());
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		closeTabAction = new CloseTabAction();
		closeAppAction = new CloseAppAction();
		escapeAction = new EscapeAction();

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent ev) {
				closeAppAction.actionPerformed(new ActionEvent(ev, 0, "CLOSE"));
			}

		});

		jtp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(escapeKS, "CLOSE");

		jtp.getActionMap().put("CLOSE", escapeAction);

		Container cont = getContentPane();
		buildUI(cont);

		add(Box.createRigidArea(new Dimension(0, 10)));

		String t = properties.get("x");
		int x = 0, y = 0, w2 = 1200, h2 = 800;
		if (t != null)
			x = Integer.parseInt(t);
		t = properties.get("y");
		if (t != null)
			y = Integer.parseInt(t);
		Point p = new Point(x, y);
		setLocation(p);

		t = properties.get("width");
		if (t != null)
			w2 = Integer.parseInt(t);
		t = properties.get("height");
		if (t != null)
			h2 = Integer.parseInt(t);

		Dimension dim2 = new Dimension(w2, h2);
		setPreferredSize(dim2);
		// repaint();
		// Display the window.
		pack();

		setVisible(true);
		addComponentListener(this);
		
		repaint();

		// wDiff = getWidth() - curDiag.area.getWidth();
		// hDiff = getHeight() - curDiag.area.getHeight();

				
		//if (diagramName == null) {          // See if a parameter was passed to the jar file....
		//	diagramName = properties.get("currentDiagram");
		//	System.out.println(diagramName);
		//}

		boolean small = (diagramName) == null ? false : true;

		if (!small) // try suppressing this...
			new SplashWindow(3000, this, small); // display
		// for 3.0 secs, or until mouse is moved

		//if (diagramName != null) {
		//	actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
		//			"Open " + diagramName));
		//}
		//if (diagramName == null) {
			curDiag = getNewDiag();
			curDiag.desc = "Click anywhere on selection area";
		// }
		//else  
			if (diagramName != null)  
				actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
						"Open " + diagramName));

		repaint();

	}

	private void buildUI(Container container) {

		buildPropDescTable();

		diagramName = properties.get("currentDiagram");
			 

		MouseListener mouseListener = new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				int i = jtp.indexAtLocation(e.getX(), e.getY());
				if (i == -1)
					return;
				ButtonTabComponent b = (ButtonTabComponent) jtp
						.getTabComponentAt(i);
				if (b == null || b.diag == null)
					return;
				Diagram diag = b.diag;

				if (diag == null) {
					getNewDiag();
					// diag = new Diagram(driver);
					// b.diag = diag;
				}
				// curDiag = diag;

				repaint();

			}
		};

		jtp.addMouseListener(mouseListener);

		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		Box box1 = new Box(BoxLayout.Y_AXIS);
		container.add(box1);

		Box box4 = new Box(BoxLayout.X_AXIS);
		box1.add(box4);

		int sf = (int) Math.round(100.0 * scalingFactor);

		JSlider zoomControl = new JSlider(JSlider.VERTICAL, 60, 200, sf);
		zoomControl.setPreferredSize(new Dimension(40, 200));
		zoomControl.setMajorTickSpacing(20);
		// zoomControl.setMinorTickSpacing(10);
		zoomControl.setPaintTicks(true);
		zoomControl.setSnapToTicks(true);
		zoomControl.setPaintLabels(false);
		zoomControl.setPaintTrack(true);
		zoomControl.setVisible(true);
		zoomControl.addChangeListener(this);
		zoomControl.getInputMap().put(escapeKS, "CLOSE");
		zoomControl.getActionMap().put("CLOSE", escapeAction);

		// zoomControl.setBackground(Color.WHITE);
		Box box45 = new Box(BoxLayout.Y_AXIS);
		Box box46 = new Box(BoxLayout.X_AXIS);
		Box box5 = new Box(BoxLayout.X_AXIS);
		Box box61 = new Box(BoxLayout.X_AXIS);
		Box box62 = new Box(BoxLayout.X_AXIS);
		Box box6 = new Box(BoxLayout.Y_AXIS);

		box5.add(Box.createRigidArea(new Dimension(10, 0)));

		box6.add(Box.createRigidArea(new Dimension(0, 10)));

		scaleLab = new JLabel();
		box61.add(scaleLab);
		box61.add(Box.createRigidArea(new Dimension(5, 0)));

		box62.add(zoom);
		box62.add(Box.createRigidArea(new Dimension(5, 0)));

		box6.add(zoomControl);
		box6.add(Box.createRigidArea(new Dimension(0, 10)));

		scaleLab.setForeground(Color.BLUE);
		String scale = "100%";
		scaleLab.setText(scale);

		box6.add(Box.createRigidArea(new Dimension(0, 10)));
		box6.add(box61);
		box6.add(Box.createRigidArea(new Dimension(0, 10)));
		box6.add(box62);
		box5.add(box6);
		box5.add(Box.createRigidArea(new Dimension(10, 0)));
		// grid.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		grid.setFont(fontg);
		grid.setSelected(true);
		// box.add(grid);
		grid.setActionCommand("Toggle Click to Grid");
		grid.addActionListener(this);
		grid.setBackground(slateGray1);
		grid.setBorderPaintedFlat(false);
		box6.add(Box.createRigidArea(new Dimension(0, 10)));
		box45.add(box5);
		box45.add(Box.createRigidArea(new Dimension(0, 10)));

		box46.add(grid);
		box46.add(Box.createRigidArea(new Dimension(0, 10)));
		box45.add(box46);
		// scaleLab.setFont(fontg);

		box4.add(box45);
		Point p = jtp.getLocation();
		jtp.setLocation(p.x + 100, p.y);
		box4.add(jtp);
		box4.add(Box.createRigidArea(new Dimension(50, 0)));
		// jtp.setBackground(Color.WHITE);
		// Align the left edges of the components.
		//curDiag.area.setAlignmentX(Component.LEFT_ALIGNMENT);
		diagDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
		// label.setLabelFor(area);
		box1.add(diagDesc);
		Font ft = fontg.deriveFont(Font.BOLD);
		diagDesc.setFont(ft);
		diagDesc.setPreferredSize(new Dimension(0, gFontHeight * 2));
		diagDesc.setForeground(Color.BLUE);

		box1.add(Box.createRigidArea(new Dimension(0, 4)));
		Box box2 = new Box(BoxLayout.X_AXIS);
		// JScrollPane jsp = new JScrollPane();
		// box2.add(jsp);
		box2.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		// box1.add(box2);
		// box2.add(Box.createHorizontalGlue());
		// box2.add(Box.createHorizontalStrut(0));
		box2.add(pan);
		box2.add(Box.createRigidArea(new Dimension(10, 0)));
		pan.setSelected(false);
		pan.setFont(fontg);
		pan.setActionCommand("Toggle Pan Switch");
		pan.addActionListener(this);
		pan.setBackground(slateGray1);
		pan.setBorderPaintedFlat(false);

		up.setFont(fontg);
		up.setActionCommand("Go to Folder");
		up.addActionListener(this);
		up.setBackground(slateGray1);
		// up.setEnabled(false);

		// pan.setBorder(null);
		// pan.setPreferredSize(new Dimension(50, 20));
		ButtonGroup butGroup = new ButtonGroup();

		box21 = new Box(BoxLayout.X_AXIS);
		box2.add(box21);
		box2.add(Box.createRigidArea(new Dimension(10, 0)));
		box2.add(up);
		// box2.add(Box.createHorizontalStrut(0));
		pack();

		for (int j = 0; j < but.length; j++) {
			but[j] = new JRadioButton();
			but[j].addActionListener(this);
			butGroup.add(but[j]);
			box21.add(but[j]);
			// jsp.add(but[j]);
			// but[j].setFont(fontg);
			but[j].setText(blockNames[j]);
			but[j].setFocusable(true);
		}
		but[but.length - 1].setAlignmentX(Component.RIGHT_ALIGNMENT);

		box1.add(box2);

		// box21.add(Box.createRigidArea(new Dimension(10,0)));
		// box21.add(Box.createHorizontalStrut(10));
		adjustFonts();

		but[0].setSelected(true); // "Process"

		box2.add(Box.createRigidArea(new Dimension(10, 0)));
		// box2.add(Box.createHorizontalGlue());
		for (int j = 0; j < but.length; j++) {
			but[j].getInputMap().put(escapeKS, "CLOSE");
			but[j].getActionMap().put("CLOSE", escapeAction);
		}

		BufferedImage image = loadImage("DrawFBP-logo-small.jpg");
		// loadImage("javaIcon.jpg");
		setIconImage(image);
		leafIcon = new ImageIcon(image);

		image = loadImage("javaIcon.jpg");
		javaIcon = new ImageIcon(image);

		image = loadImage("jarIcon.jpg");
		jarIcon = new ImageIcon(image);

		image = loadImage("folderIcon.jpg");
		folderIcon = new ImageIcon(image);

		image = loadImage("classIcon.jpg");
		classIcon = new ImageIcon(image);

		Toolkit tk = Toolkit.getDefaultToolkit();
		image = null;
		openPawCursor = null;

		image = loadImage("open_paw.gif");
		openPawCursor = tk.createCustomCursor(image, new Point(15, 15), "Paw");

		closedPawCursor = null;

		image = loadImage("closed_paw.gif");
		closedPawCursor = tk.createCustomCursor(image, new Point(15, 15),
				"Paw");

		image = loadImage("drag_icon.gif");
		drag_icon = tk.createCustomCursor(image, new Point(1, 1), "Drag");
		
		
        }

	BufferedImage loadImage(String s) {

		InputStream is = this.getClass().getResourceAsStream("/" + s);

		BufferedImage image = null;
		if (is == null) {
			MyOptionPane.showMessageDialog(this, "Missing icon: " + s,
					MyOptionPane.ERROR_MESSAGE);
		} else {
			try {
				image = ImageIO.read(is);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return image;
	}

	public JMenuBar createMenuBar() {

		// JMenu editMenu;

		// Create the menu bar.
		JMenuBar menuBar = new JMenuBar();
		// menuBar.add(Box.createRigidArea(new Dimension(20, 0)));
		// menuBar.setColor(new Color(200, 255, 255));

		// Build the first menu.
		// fileMenu = new JMenu(" File ");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		// fileMenu.setSelected(true);
		fileMenu.setBorderPainted(true);
		// fileMenu.setFont(fontg);
		menuBar.add(fileMenu);

		// a group of JMenuItems
		JMenuItem menuItem = new JMenuItem("Open Diagram");
		// menu.setMnemonic(KeyEvent.VK_D);
		menuItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.ALT_MASK));

		fileMenu.add(menuItem);
		menuItem.addActionListener(this);
		menuItem = new JMenuItem("Save");
		menuItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);
		menuItem = new JMenuItem("Save as...");
		menuItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK));
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);
		menuItem = new JMenuItem("New Diagram");
		menuItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);
		fileMenu.addSeparator();
		JMenu gnMenu = new JMenu("Select Diagram Language...");
		fileMenu.add(gnMenu);
		int j = genLangs.length;
		gMenu = new JMenuItem[j];

		int k = 0;
		for (int i = 0; i < j; i++) {
			//if (!(genLangs[i].label.equals("FBP"))) {
				gMenu[k] = new JMenuItem(genLangs[i].label);
				gnMenu.add(gMenu[k]);
				gMenu[k].addActionListener(this);
				k++;
			//}
		}

		// menuItem = new JMenuItem("Clear Language Association");
		// fileMenu.add(menuItem);
		// menuItem.addActionListener(this);
		fileMenu.addSeparator();
		menuItem = new JMenuItem("Export Image");
		menuItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.ALT_MASK));
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);
		menuItem = new JMenuItem("Show Image");
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);
		fileMenu.addSeparator();

		String s = "Generate ";
		if (curDiag != null)
			s += curDiag.diagLang.label + " ";
		s += "Network";
		gNMenuItem = new JMenuItem(s);
		fileMenu.add(gNMenuItem);
		gNMenuItem.addActionListener(this);

		menuItem = new JMenuItem("Display Generated Code");
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);

		fileMenu.addSeparator();
		compMenu = new JMenuItem("Compile Code");
		// compMenu.setEnabled(currLang != null &&
		// currLang.label.equals("Java"));
		compMenu.setMnemonic(KeyEvent.VK_C);
		compMenu.setBorderPainted(true);
		fileMenu.add(compMenu);
		compMenu.addActionListener(this);

		runMenu = new JMenuItem("Run Code");
		// runMenu.setEnabled(currLang != null &&
		// currLang.label.equals("Java"));
		runMenu.setMnemonic(KeyEvent.VK_R);
		runMenu.setBorderPainted(true);
		fileMenu.add(runMenu);
		runMenu.addActionListener(this);

		fileMenu.addSeparator();
		menuItem = new JMenuItem("Compare Diagrams");
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);

		fileMenu.addSeparator();

		menuItem1 = new JMenuItem("Locate JavaFBP Jar File");

		menuItem1.setEnabled(currLang != null && currLang.label.equals("Java"));
		fileMenu.add(menuItem1);
		menuItem1.addActionListener(this);

		menuItem2j = new JMenuItem("Add Additional Jar File");
		menuItem2j.setEnabled(currLang != null && currLang.label.equals("Java"));
		fileMenu.add(menuItem2j);
		menuItem2j.addActionListener(this);
		
		menuItem2c = new JMenuItem("Add Additional Dll File");
		menuItem2c.setEnabled(currLang != null && currLang.label.equals("C#"));
		fileMenu.add(menuItem2c);
		menuItem2c.addActionListener(this);
		
		menuItem = new JMenuItem("Remove Additional Jar and Dll Files");
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);

		fileMenu.addSeparator();
		menuItem = new JMenuItem("Locate DrawFBP Help File");
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);
		// }
		fileMenu.addSeparator();

		menuItem = new JMenuItem("Change Fonts");
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);
		menuItem = new JMenuItem("Change Font Size");
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);

		fileMenu.addSeparator();
		menuItem = new JMenuItem("Print");
		menuItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.ALT_MASK));
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);
		fileMenu.addSeparator();
		menuItem = new JMenuItem("Display Properties");
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);

		fileMenu.addSeparator();
		menuItem = new JMenuItem("Close Diagram");
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);

		// editMenu = new JMenu(" Edit ");
		editMenu.setMnemonic(KeyEvent.VK_E);
		// editMenu.setFont(fontg);
		menuBar.add(editMenu);
		editMenu.setBorderPainted(true);

		menuItem = new JMenuItem("Edit Diagram Description");
		editMenu.add(menuItem);
		menuItem.addActionListener(this);
		menuItem = new JMenuItem("New Block");
		editMenu.add(menuItem);
		menuItem.addActionListener(this);
		menuItem = new JMenuItem("Block-related Actions");
		editMenu.add(menuItem);
		menuItem.addActionListener(this);
		menuItem = new JMenuItem("Arrow-related Actions");
		editMenu.add(menuItem);
		menuItem.addActionListener(this);

		// JMenu helpMenu = new JMenu(" Help ");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		// helpMenu.setFont(fontg);
		helpMenu.setBorderPainted(true);
		// helpMenu.setColor(new Color(121, 201, 201));
		menuBar.add(helpMenu);

		Box box0 = new Box(BoxLayout.X_AXIS);
		// JPanel jp1 = new JPanel();
		Dimension dim = jtf.getPreferredSize();
		jtf.setPreferredSize(new Dimension(gFontWidth * 20, dim.height));

		box0.add(Box.createRigidArea(new Dimension(20, 0)));
		box0.add(jtf); // languages

		box0.add(Box.createRigidArea(new Dimension(10, 0)));

		box0.add(jfl); // font list

		box0.add(Box.createRigidArea(new Dimension(10, 0)));

		box0.add(jfs); // font size

		box0.add(Box.createRigidArea(new Dimension(10, 0)));

		box0.add(jfv);
		menuBar.add(box0);

		jtf.setText("Diagram Language: " + currLang.showLangs());
		jtf.setFont(fontg);
		jtf.setEditable(false);

		// jtf.setBackground(slateGray1);
		// jfl.setBackground(slateGray1);

		jfl.setFont(fontg);
		jfl.setEditable(false);

		jfs.setFont(fontg);
		jfs.setEditable(false);

		JMenuItem menu_help = new JMenuItem("Launch Help");
		helpMenu.add(menu_help);
		menu_help.addActionListener(this);

		menuItem = new JMenuItem("About");
		helpMenu.add(menuItem);
		menuItem.addActionListener(this);
		menuBar.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(escapeKS, "CLOSE");

		menuBar.getActionMap().put("CLOSE", escapeAction);
		menuBar.setVisible(true);
		
		
			 	
		//repaint();

		return menuBar;
	}

	Diagram getNewDiag() {
		Diagram diag = new Diagram(this);
		SelectionArea sa = getNewArea();
		diag.area = sa;
		int i = jtp.getTabCount(); // get count *before* adding new sa & label
		jtp.add(sa, new JLabel());
		
		//int j = jtp.getTabCount();  // for debugging
		// System.out.println("new tab");
		ButtonTabComponent b = new ButtonTabComponent(jtp, this);
		jtp.setTabComponentAt(i, b);		
		jtp.setSelectedIndex(i);
		b.diag = diag;
		//diag.tabNum = i;
		curDiag = diag;
		
		diag.title = "(untitled)";
		diag.area.setAlignmentX(Component.LEFT_ALIGNMENT);
		diag.blocks = new ConcurrentHashMap<Integer, Block>();
		diag.arrows = new ConcurrentHashMap<Integer, Arrow>();	
		
		//repaint(); 
		
		//diag.fCParm[Diagram.DIAGRAM] = diag.new FileChooserParm("Diagram", "currentDiagramDir",
		//		"Specify diagram name in diagram directory", ".drw",
		//		new DiagramFilter(), "Diagrams (*.drw)");
		
		diag.fCParm[Diagram.DIAGRAM] = diagFCParm;

		diag.fCParm[Diagram.IMAGE] = new FileChooserParm("Image", "currentImageDir",
				"Image: ", ".png", new ImageFilter(), "Image");	
				
		diag.fCParm[Diagram.FBP] = new  FileChooserParm("Generated FBP code",
						"currentFBPNetworkDir", "Specify file name for generated FBP code",
						".fbp", new FBPFilter(), "fbp notation");
 
		diag.fCParm[Diagram.JHELP] = new  FileChooserParm("Java Help file", "jhallJarFile",
				"Choose a directory for the JavaHelp jar file", ".jar",
				new JarFileFilter(), "Help files");
				
			
		diag.fCParm[Diagram.JARFILE] = new  FileChooserParm("Jar file", "javaFBPJarFile",
				"Choose a jar file for JavaFBP", ".jar",
				new JarFileFilter(), "Jar files");

				
		diag.fCParm[Diagram.CLASS] = new  FileChooserParm("Class", "currentClassDir",
				"Select component from class directory", ".class",
				new JavaClassFilter(), "Class files");
		
		diag.fCParm[Diagram.PROCESS] = new  FileChooserParm("Process", diag.diagLang.srcDirProp, "Select "
				+ diag.diagLang.showLangs() + " component from directory",
				diag.diagLang.suggExtn, diag.diagLang.filter, "Components: "
						+ diag.diagLang.showLangs() + " " + diag.diagLang.showSuffixes());
		
		diag.fCParm[Diagram.NETWORK] = new  FileChooserParm("Code",
				diag.diagLang.netDirProp,
				"Specify file name for code",
				"." + diag.diagLang.suggExtn, diag.diagLang.filter,
				diag.diagLang.showLangs());	
		
		diag.fCParm[Diagram.DLL] = new  FileChooserParm("C# .dll file",
				"dllFileDir",
				"Specify file name for .dll file",
				".dll", new DllFilter(),
				".dll");	
		
		diag.fCParm[Diagram.EXE] = new FileChooserParm("C# Executable",
				"exeDir",
				"Specify file name for .exe file",
				".exe", new ExeFilter(),
				".exe");	
		 	
				
		repaint();

		return diag;
	}

	@SuppressWarnings("rawtypes")
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == jfl) {
			changeFonts();
			return;
		}

		if (e.getSource() == jfs) {
			changeFontSize();
			return;
		}

		String s = e.getActionCommand();

		if (s.equals("Open Diagram")) {
			openAction(null);
			return;
		}

		if (s.length() > 5 && s.substring(0, 5).equals("Open ")) {
			s = s.substring(5); // drop Open and blank
			openAction(s);
			return;
		}

		boolean SAVE_AS = true;
		if (s.equals("Save")) {
			saveAction(!SAVE_AS);
			return;

		}
		if (s.equals("Save as...")) {

			saveAction(SAVE_AS);
			return;

		}
		if (s.equals("New Diagram")) {

			//int i = jtp.getTabCount();
			//if (i > 1 || curDiag.diagFile != null || curDiag.changed)
			curDiag = getNewDiag();

			//jtp.setSelectedIndex(curDiag.tabNum);

			repaint();

			return;

		}

		// if (curDiag.compLang == null) {
		for (int j = 0; j < gMenu.length; j++) {
			if (e.getSource() == gMenu[j]) {
				GenLang gl = genLangs[j];

				currLang = gl;

				saveProp("defaultCompLang", currLang.label);
				saveProperties();
				if (curDiag != null && curDiag.diagLang != currLang) {
					curDiag.diagLang = currLang;
					curDiag.changed = true;
				}
				changeLanguage(gl);

				MyOptionPane.showMessageDialog(this,
						"Language group changed to " + currLang.showLangs()+ "\nNote: some File and Block-related options will have changed");
				repaint();

				return;
			}
		}

		// }
		
		

		if (s.startsWith("Generate ")) {
			if (curDiag == null || curDiag.blocks.isEmpty()) {
				MyOptionPane.showMessageDialog(this, "No components specified",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

			if (curDiag.title == null || curDiag.title.equals("(untitled)")) {

				MyOptionPane.showMessageDialog(this,
						"Untitled diagram - please do Save first",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

			//properties.get(gl.netDirProp);
			
			CodeManager mc = new CodeManager(curDiag);
			mc.genCode();

			return;

		}

		if (s.equals("Display Generated Code")) {

			File cFile = null;			
			GenLang gl = curDiag.diagLang;

			
			String ss = properties.get(gl.netDirProp);
			//File f = curDiag.diagFile;
			
			//String name = f.getName();

			if (ss == null)
				ss = System.getProperty("user.home");

			File file = new File(ss);
			MyFileChooser fc = new MyFileChooser(this, file, curDiag.fCParm[Diagram.NETWORK]);
			File f = curDiag.diagFile;	
			if (f != null) {
				String name = f.getName();
				int i = name.indexOf(".drw");
				ss += "/" + name.substring(0, i)
					+ curDiag.fCParm[Diagram.NETWORK].fileExt;
				fc.setSuggestedName(ss);
			}

			int returnVal = fc.showOpenDialog(true, true); // force saveAs

			cFile = null;
			if (returnVal == MyFileChooser.APPROVE_OPTION) {
				cFile = new File(getSelFile(fc));
			}
			// }
			if (cFile == null)
				return;

			if (!(cFile.exists()))
				return;

			
			CodeManager cm = new CodeManager(curDiag);
			cm.doc.changed = false;
			cm.displayDoc(cFile, gl, null);

			return;
		}

		if (s.equals("Compile Code")) {
			compileCode();
			return;
		}

		if (s.equals("Run Code")) {
			runCode();
			return;
		}

		if (s.equals("Compare Diagrams")) {
			compare();
			return;
		}

		if (s.equals("Clear Language Association")) {
			curDiag.diagLang = null;
			curDiag.changed = true;
			jtf.setText("");

			// curDiag.changeCompLang();
		}

		// }
		if (s.equals("Locate JavaFBP Jar File")) {

			locateJavaFBPJarFile(true);
			return;
		}

		if (s.equals("Add Additional Jar File")) {

			addAdditionalJarFile();
			return;
		}
		
		if (s.equals("Add Additional Dll File")) {

			addAdditionalDllFile();
			return;
		}
		
		if (s.equals("Remove Additional Jar and Dll Files")) {
			jarFiles.clear();
			dllFiles.clear();
			properties.remove("additionalJarFiles");
			properties.remove("additionalDllFiles");
			MyOptionPane.showMessageDialog(null,
					"References to additional jar and dll files removed (not deleted)",
					MyOptionPane.INFORMATION_MESSAGE);
		}
		
		if (s.equals("Locate DrawFBP Help File")) {

			locateJhallJarFile(true);
			return;
		}

		if (s.equals("Change Fonts")) {
			changeFonts();
		}

		if (s.equals("Change Font Size")) {
			changeFontSize();
		}

		if (s.equals("Print")) {
			int x1, w1, y1, h1;

			x1 = curDiag.area.getX();
			y1 = curDiag.area.getY();
			w1 = curDiag.area.getWidth();
			h1 = curDiag.area.getHeight();
			Rectangle rect = new Rectangle(x1, y1, w1, h1);
			PrintableDocument pd = new PrintableDocument(getContentPane(),
					this);

			// PrintableDocument.printComponent(getContentPane());
			pd.setRectangle(rect); // doesn't seem to make a difference!
			pd.print();
			return;
		}
		if (s.equals("Display Properties")) {
			displayProperties();
		}

		if (s.equals("Toggle Click to Grid")) {
			curDiag.clickToGrid = !curDiag.clickToGrid;
			grid.setSelected(curDiag.clickToGrid);
			driver.saveProp("clicktogrid",(new Boolean(curDiag.clickToGrid)).toString());
			return;

		}
		if (s.equals("Toggle Pan Switch")) {
			panSwitch = !panSwitch;
			// if (panSwitch)
			// setCursor(openPawCursor);
			// else
			// setCursor(defaultCursor);
			return;
		}

		if (s.equals("Go to Folder")) {
			String w = null;
			File f = curDiag.diagFile;
			if (f != null) {
				w = f.getParent();
			}
			// w = f.getAbsolutePath();
			openAction(w);

			return;
		}

		if (s.equals("Export Image")) {

			if (curDiag == null || curDiag.title == null
					|| curDiag.blocks.isEmpty()) {
				MyOptionPane.showMessageDialog(null,
						"Unable to export image for empty or unsaved diagram - please do save first",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

			File file = null;
			// curDiag.imageFile = null;

			// crop
			int min_x, w, min_y, h;
			min_x = Math.max(1, curDiag.minX);
			min_x = Math.min(min_x, curDiag.minX);
			w = curDiag.maxX - min_x;
			min_y = Math.max(1, curDiag.minY);
			min_y = Math.min(min_y, curDiag.minY);
			h = curDiag.maxY - min_y;  // ok so far!

			int aw = curDiag.area.getWidth();
			int ah = curDiag.area.getHeight();
			w = Math.min(aw, w);
			h = Math.min(ah, h) + 60;


			//BufferedImage buffer2 = buffer.getSubimage(min_x, min_y , max_w,
			//		max_h);	
			
			int y = Math.max(0, min_y - 40);
			int bottom_border_height = 60;
			BufferedImage buffer2 = buffer.getSubimage(min_x, y , w, h);	
			//BufferedImage buffer2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			
			//Font f = fontg.deriveFont(Font.ITALIC, 18.0f);  // description a bit large - try using fontg + 10
			Font f = fontg.deriveFont(Font.ITALIC, (float) (fontg.getSize() + 10));
			
			FontMetrics metrics = getFontMetrics(f);
			String t = curDiag.desc;
			byte[] str = t.getBytes();
			int width = metrics.bytesWidth(str, 0, t.length());
			width = Math.max(width + 40, buffer2.getWidth());
			
			BufferedImage combined = new BufferedImage(width, buffer2.getHeight() + bottom_border_height,
					BufferedImage.TYPE_INT_ARGB);
			
			Graphics g = combined.getGraphics();
			
			g.setColor(Color.WHITE);

			g.fillRect(0, 0, combined.getWidth(), combined.getHeight());
			int x = (combined.getWidth() - buffer2.getWidth()) / 2;
			g.drawImage(buffer2, x, 0, null);
			// g.drawImage(buffer, 0, 0, null);
			// g.setColor(Color.RED);

			//g.fillRect(0, max_h, max_w, 80);
			g.fillRect(0, combined.getHeight(), combined.getWidth(), bottom_border_height);

			if (!(curDiag.desc == null || curDiag.desc.trim().equals(""))) {
				Color col = g.getColor();
				g.setColor(Color.BLUE);
				
				//Font f = fontg.deriveFont(Font.ITALIC, (float) (fontg.getSize() + 10));

				g.setFont(f);
				x = combined.getWidth() / 2;
				// x = buffer2.getWidth() / 2;
				metrics = g.getFontMetrics(f);
				t = curDiag.desc;
				str = t.getBytes();
				width = metrics.bytesWidth(str, 0, t.length());

				g.drawString(t, x - width / 2, buffer2.getHeight() + 10);    

				g.setColor(col);
			}
		
			//curDiag.fCParm[Diagram.IMAGE].prompt = curDiag.fCParm[Diagram.IMAGE].prompt.substring(0, i) + ": " + fn;					
			
			file = curDiag.genSave(null, curDiag.fCParm[Diagram.IMAGE], combined);
			// file = curDiag.genSave(null, fCPArray[IMAGE], buffer2);
			if (file == null) {
				MyOptionPane.showMessageDialog(this, "File not saved");
				// curDiag.imageFile = null;
				g.dispose();
				return;
			}

			// ImageIcon image = new ImageIcon(combined);
			// curDiag.imageFile = file;
			Date date = new Date();
			//Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			file.setLastModified(date.getTime());
			return;
		}

		if (s.equals("Show Image")) {

			File fFile = null;

			// if (fFile == null || !fFile.exists()) {

			String ss = properties.get("currentImageDir");
			if (ss == null)
				currentImageDir = new File(System.getProperty("user.home"));
			else
				currentImageDir = new File(ss);

			MyFileChooser fc = new MyFileChooser(this,currentImageDir,
					curDiag.fCParm[Diagram.IMAGE]);

			File f = curDiag.diagFile;
			if (f != null) {
				int i = curDiag.diagFile.getName().indexOf(".drw");
				if (i > -1) {
					ss += "/"
							+ curDiag.diagFile.getName().substring(0, i)
							+ curDiag.fCParm[Diagram.IMAGE].fileExt;
					fc.setSuggestedName(ss);
				}
			}

			int returnVal = fc.showOpenDialog(true, true); // set to saveAs

			// fFile = null;
			if (returnVal == MyFileChooser.APPROVE_OPTION) {
				fFile = new File(getSelFile(fc));
			}
			// }
			if (fFile == null)
				return;

			if (!(fFile.exists()))
				return;
			// }

			
			Image image = null;
			try {
				image = ImageIO.read(fFile);
			} catch (IOException e1) {
				MyOptionPane.showMessageDialog(this, "Could not get image: " + fFile.getAbsolutePath(),
						MyOptionPane.ERROR_MESSAGE);
				return;
 
			}
			BufferedImage img = (BufferedImage) image;

			currentImageDir = new File(fFile.getParent());
			saveProp("currentImageDir", fFile.getParent());
			saveProperties();

			// curDiag.imageFile = fFile;

			JDialog popup = new JDialog();
			popup.setTitle(fFile.getName());

			//popup.setUndecorated(true);
			JLabel jLabel = new JLabel(new ImageIcon(image));
			//jLabel.addComponentListener(this);
			//jLabel.setOpaque(false);
			popup.add(jLabel);
			popup.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		    popup.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
		    popup.setBounds((int) (java.awt.Toolkit.getDefaultToolkit()
		            .getScreenSize().getWidth() / 2 - img.getWidth() / 2),
		            (int) (java.awt.Toolkit.getDefaultToolkit().getScreenSize()
		                    .getHeight() / 2 - img.getHeight() / 2),
		            img.getWidth(), img.getHeight());
		    //RoundRectangle2D r = new RoundRectangle2D.Double(0, 0, img.getWidth(), img.getHeight(), 25, 25);
		    //popup.setShape(r);
		    //popup.setOpacity(0f);
		    popup.setMinimumSize(popup.getPreferredSize());
		    //popup.setVisible(true);
			//jLabel.setBackground(Color.WHITE);
			//popup.add(jsp, BorderLayout.CENTER);
			popup.setLocation(new Point(200, 200));
			//popup.setBackground(Color.WHITE);

			// popup.addComponentListener(this);
			// popup.setPreferredSize(dim);
			popup.pack();
			popup.setVisible(true);
			popup.setAlwaysOnTop(true);
			popup.repaint();
			repaint();
			return;
		}

		if (s.equals("Close Diagram")) {
			closeTab();
			return;
		}
		if (s.equals("Launch Help")) {

			/*
			if (jHelpViewer == null) {

				 
				if (jhallJarFile == null) {

					jhallJarFile = properties.get("jhallJarFile");
					boolean res = true;

					if (jhallJarFile == null) {
						int response = MyOptionPane.showConfirmDialog(this,
								// "Locate it?",
								"Locate the standard JavaHelp jar file (javax.help) -\n"
										+ "and specify the folder where you will keep it -\n" 
								        + " it is in your 'lib' folder",
								"Locate it?", MyOptionPane.OK_CANCEL_OPTION);
						if (response == MyOptionPane.OK_OPTION)
							res = locateJhallJarFile(false);
						else {
							MyOptionPane.showMessageDialog(this,
									"No DrawFBP Help jar file located",
									MyOptionPane.ERROR_MESSAGE);
							res = false;
						}
					}
					if (!res)
						return;
				}
				 
				jHelpClass = null;
				helpSetClass = null;
				URLClassLoader cl = null;

				 
				File jFile = new File(jhallJarFile);
				if (!(jFile.exists())) {
					MyOptionPane.showMessageDialog(this,
							"DrawFBP Help jar file shown in properties does not exist\n"
									+ "Use File/Locate DrawFBP Help File, and try Help again",
							MyOptionPane.ERROR_MESSAGE);
					return;
				}
				 
							
								
				try {
					 URL[] urls = new URL[]{jFile.toURI().toURL()}; 

					// Create a new class loader with the directory
					 cl = new URLClassLoader(urls,
								this.getClass().getClassLoader());

					// Find the HelpSet file and create the HelpSet object
					helpSetClass =   cl.loadClass("javax.help.HelpSet");
				} catch (MalformedURLException e2) {
				} catch (ClassNotFoundException e2) {
				} catch (NoClassDefFoundError e2) {
				}

				if (helpSetClass == null) {
					MyOptionPane.showMessageDialog(this,
							"HelpSet class not found in jar file or invalid",
							MyOptionPane.ERROR_MESSAGE);
					return;
				}
 
				
				URL url2 = null;
				jHelpViewer = null;
				try {
					Method m = helpSetClass.getMethod("findHelpSet",
							ClassLoader.class, String.class);
					url2 = (URL) m.invoke(null, cl, "helpSet.hs");

					Constructor conhs = helpSetClass
							.getConstructor(ClassLoader.class, URL.class);

					Object hs = conhs.newInstance(cl, url2);

					jHelpClass = cl.loadClass("javax.help.JHelp");
					if (jHelpClass == null) {
						MyOptionPane.showMessageDialog(this,
								"JHelp class not found in jar file",
								MyOptionPane.ERROR_MESSAGE);
						return;
					}
					Constructor conjh = jHelpClass.getConstructor(helpSetClass);
					jHelpViewer = (JComponent) conjh.newInstance(hs);

				} catch (Exception e2) {
					MyOptionPane.showMessageDialog(this,
							"HelpSet could not be processed: " + e2,
							MyOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			*/
			HelpSet hs = null;
			URL url = HelpSet.findHelpSet(getClass().getClassLoader(), "helpSet.hs");
			try {
				hs = new HelpSet(null, url);
			} catch (HelpSetException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
	        jHelpViewer = new JHelp(hs);
			
			// Create a new 
			popup2 = new JDialog(this);
			popup2.setTitle("Help DrawFBP");
			popup2.setIconImage(favicon.getImage());
			applyOrientation(popup2);

			popup2.setFocusable(true);
			popup2.requestFocusInWindow();

			popup2.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent ev) {
					if (ev.getKeyCode() == KeyEvent.VK_ESCAPE) {
						// frame2.setVisible(false);
						popup2.dispose();
					}

				}
			});
			jHelpViewer
					.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
					.put(escapeKS, "CLOSE");

			jHelpViewer.getActionMap().put("CLOSE", escapeAction);

			// frame2.setPreferredSize(getPreferredSize());
			// Add the created helpViewer to it.
			popup2.getContentPane().add(jHelpViewer);
			// Set a default close operation.
			popup2.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			// Make the this visible.
			popup2.setVisible(true);
			popup2.pack();
			Dimension dim = getSize();
			Point p = getLocation();
			int x_off = 100;
			int y_off = 100;
			popup2.setPreferredSize(
					new Dimension(dim.width - x_off * 2, dim.height - y_off));
			popup2.pack();
			popup2.setLocation(p.x + x_off, p.y + y_off);
			return;
		}

		if (s.equals("About")) {
			JTextArea ta = new JTextArea();
			Font f = fontf;
			ta.setColumns(80);
			ta.setRows(7);
			ta.setEditable(false);
			ta.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			ta.setLineWrap(true);

			String v = VersionAndTimestamp.getVersion();
			String dt = VersionAndTimestamp.getDate();

			int i = v.length();
			String sp1 = "       ".substring(0, 7 - i);

			i = dt.length();
			String sp2 = "       ".substring(0, 14 - i);

			ta.setText("****************************************************\n"
					+ "*                                                  *\n"
					+ "*             DrawFBP v" + v + "      " + sp1
					+ "               *\n"
					+ "*                                                  *\n"
					+ "*    Authors: J.Paul Rodker Morrison, Canada,      *\n"
					+ "*             Bob Corrick, UK                      *\n"
					+ "*                                                  *\n"
					+ "*    Copyright 2009, ..., 2019                     *\n"
					+ "*                                                  *\n"
					+ "*    FBP web site: www.jpaulmorrison.com/fbp       *\n"
					+ "*                                                  *\n"
					+ "*               (" + dt + ")            " + sp2
					+ "       *\n"
					+ "*                                                  *\n"
					+ "****************************************************\n");

			ta.setFont(f);
			final JDialog popup = new JDialog(this);
			popup.add(ta, BorderLayout.CENTER);
			Point p = getLocation();
			// popup.setPreferredSize(new Dimension(60,20));
			popup.pack();
			popup.setLocation(p.x + 200, p.y + 100);
			popup.setVisible(true);

			popup.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent ev) {
					popup.dispose();
				}
			});

			popup.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent ev) {
					if (ev.getKeyCode() == KeyEvent.VK_ESCAPE) {
						popup.dispose();
					}
				}
			});
			popup.requestFocusInWindow();

			popup.repaint();
			repaint();
			return;

		}

		if (s.equals("Edit Diagram Description")) { // Title of diagram
			// as a whole

			String ans = (String) MyOptionPane.showInputDialog(this,
					"Enter or change text", "Modify diagram description",
					MyOptionPane.PLAIN_MESSAGE, null, null, curDiag.desc);

			if (ans != null/* && ans.length() > 0 */) {
				curDiag.desc = ans;
				curDiag.desc = curDiag.desc.replace('\n', ' ');
				curDiag.desc = curDiag.desc.trim();
				curDiag.changed = true;
				if (curDiag.parent != null)
					curDiag.parent.desc = ans;
			}
			repaint();
			return;

		}

		int x, y;

		x = 100 + (new Random()).nextInt(curDiag.area.getWidth() - 200);
		y = 100 + (new Random()).nextInt(curDiag.area.getHeight() - 200);

		if (s.equals("New Block")) {
			// if (newItemMenu == null) {
			// newItemMenu = buildNewItemMenu(driver);
			// }
			// newItemMenu.setVisible(true);

			Block blk = createBlock(blockType, x, y, curDiag, true); 
			if (null != blk) {
				//if (!blk.editDescription(CREATE))
				//	return;;
				curDiag.changed = true;
			}
			repaint();
			return;

		}
		if (s.equals("Block-related Actions")) {
			Block b = selBlock;

			if (b == null) {
				MyOptionPane.showMessageDialog(this, "Block not selected",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}
			curDiag = b.diag;
			b.buildBlockPopupMenu();
			use_drag_icon = false;
			curDiag.jpm.show(this, x + 100, y + 100);
			repaint();
			return;

		}
		if (s.equals("Arrow-related Actions")) {
			Arrow a = selArrow;
			if (a == null) {
				MyOptionPane.showMessageDialog(this, "Arrow not selected",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}
			a.buildArrowPopupMenu();
			curDiag = a.diag;
			curDiag.jpm.show(this, a.toX + 100, a.toY + 100);
			repaint();
			return;
		}

		setBlkType(s);

		repaint();
	}

	void setBlkType(String s) {

		if (s.equals("none")) {
			blockType = "";

		} else

		if (s.equals("Subnet")) {
			blockType = Block.Types.PROCESS_BLOCK;
			willBeSubnet = true;

		} else {
			willBeSubnet = false;
			if (s.equals("Process")) {
				blockType = Block.Types.PROCESS_BLOCK;

			} else if (s.equals("File")) {
				blockType = Block.Types.FILE_BLOCK;

			} else if (s.equals("Report")) {
				blockType = Block.Types.REPORT_BLOCK;

			} else if (s.equals("ExtPorts: In")) {
				blockType = Block.Types.EXTPORT_IN_BLOCK; // In

			} else if (s.equals("... Out")) {
				blockType = Block.Types.EXTPORT_OUT_BLOCK;// Out

			} else if (s.equals("... Out/In")) {
				blockType = Block.Types.EXTPORT_OUTIN_BLOCK; // Out/In

			} else if (s.equals("Initial IP")) {
				blockType = Block.Types.IIP_BLOCK;

			} else if (s.equals("Legend")) {
				blockType = Block.Types.LEGEND_BLOCK;

			} else if (s.equals("Person")) {
				blockType = Block.Types.PERSON_BLOCK;

			} else if (s.equals("Enclosure")) {
				blockType = Block.Types.ENCL_BLOCK;
			}
		}
	}

	void changeLanguage(GenLang gl) {

		curDiag.diagLang = gl;
		saveProp("defaultCompLang", gl.label);
		currLang = gl;
		jtf.setText("Diagram Language: " + gl.showLangs());

		jtf.repaint();

		menuItem1.setEnabled(currLang.label.equals("Java"));
		menuItem2j.setEnabled(currLang.label.equals("Java"));
		menuItem2c.setEnabled(currLang.label.equals("C#"));
		// compMenu.setEnabled(currLang.label.equals("Java"));
		// runMenu.setEnabled(currLang.label.equals("Java"));

		fileMenu.remove(gNMenuItem);

		String u = "Generate ";
		if (curDiag != null)
			u += curDiag.diagLang.label + " ";
		u += "Network";
		gNMenuItem = new JMenuItem(u);
		gNMenuItem.addActionListener(this);
		fileMenu.add(gNMenuItem, 10);
		filterOptions[0] = gl.showLangs();

		curDiag.fCParm[Diagram.PROCESS] = new  FileChooserParm("Process",
				gl.srcDirProp,
				"Select " + gl.showLangs() + " component from directory",
				gl.suggExtn, gl.filter,
				"Components: " + gl.showLangs() + " " + gl.showSuffixes());

		curDiag.fCParm[Diagram.NETWORK] = new  FileChooserParm("Code",
				gl.netDirProp, "Specify file name for code", "." + gl.suggExtn,
				gl.filter, gl.showLangs());

		repaint();

	}

	// editType is false if no edit; true if block type determines type
	
	Block createBlock(String blkType, int xa, int ya, Diagram diag,
			boolean editType) {
		Block block = null;
		boolean oneLine = false;
		if (blkType.equals(Block.Types.PROCESS_BLOCK)) {
			block = new ProcessBlock(diag);
			block.isSubnet = willBeSubnet;
		}

		else if (blkType.equals(Block.Types.EXTPORT_IN_BLOCK)
				|| blkType.equals(Block.Types.EXTPORT_OUT_BLOCK)
				|| blkType.equals(Block.Types.EXTPORT_OUTIN_BLOCK)) {
			oneLine = true;
			block = new ExtPortBlock(diag);
		}

		else if (blkType.equals(Block.Types.FILE_BLOCK))
			block = new FileBlock(diag);

		else if (blkType.equals(Block.Types.IIP_BLOCK)) {
			oneLine = true;
			block = new IIPBlock(diag);
		}

		else if (blkType.equals(Block.Types.LEGEND_BLOCK))
			block = new LegendBlock(diag);

		else if (blkType.equals(Block.Types.ENCL_BLOCK)) {
			oneLine = true;
			block = new Enclosure(diag);
			Point pt = diag.area.getLocation();
			int y = Math.max(ya - block.height / 2, pt.y + 6);
			block.cy = ((ya + block.height / 2) + y) / 2;
		}

		else if (blkType.equals(Block.Types.PERSON_BLOCK))
			block = new PersonBlock(diag);

		else if (blkType.equals(Block.Types.REPORT_BLOCK))
			block = new ReportBlock(diag);
		else
			return null;

		block.type = blkType;

		block.cx = xa;
		block.cy = ya;
		if (block.cx == 0 || block.cy == 0)
			return null; // fudge!

		if (editType) {
			if (oneLine) {
				if (blkType != Block.Types.ENCL_BLOCK) {
					String d = "Enter description";
					String ans = (String) MyOptionPane.showInputDialog(this,
							"Enter text", d, MyOptionPane.PLAIN_MESSAGE, null,
							null, block.desc);

					if (ans == null)
						return null;
					else
						block.desc = ans;
				}
			} else if (!block.editDescription(CREATE))
				return null;

			if (blkType.equals(Block.Types.IIP_BLOCK)) {
				IIPBlock ib = (IIPBlock) block;
				block.desc = ib.checkNestedChars(block.desc);
			}
		}
		block.calcEdges();
		// diag.maxBlockNo++;
		// block.id = diag.maxBlockNo;
		diag.blocks.put(new Integer(block.id), block);
		// diag.changed = true;
		selBlock = block;
		// selArrowP = null;
		return block;
	}

	void buildPropDescTable() {
		propertyDescriptions = new LinkedHashMap<String, String>();

		propertyDescriptions.put("Version #", "versionNo");
		propertyDescriptions.put("Date", "date");
		propertyDescriptions.put("Click To Grid", "clicktogrid");
		propertyDescriptions.put("Sort By Date", "sortbydate");
		propertyDescriptions.put("Current C# source code directory",
				"currentCsharpSourceDir");
		propertyDescriptions.put("Current C# network code directory",
				"currentCsharpNetworkDir");
		propertyDescriptions.put("Current component class directory",
				"currentClassDir");
		propertyDescriptions.put("Current diagram directory",
				"currentDiagramDir");
		propertyDescriptions.put("Current diagram", "currentDiagram");
		propertyDescriptions.put("Current image directory", "currentImageDir");
		propertyDescriptions.put("Current Java source code directory",
				"currentSourceDir");
		propertyDescriptions.put("Current Java source code directory",
				"currentJavaSourceDir");
		propertyDescriptions.put("Current Java network code directory",
				"currentJavaNetworkDir");
		propertyDescriptions.put("Current NoFlo source code directory",
				"currentNoFloSourceDir");
		propertyDescriptions.put("Current NoFlo network code directory",
				"currentNoFloNetworkDir");
		propertyDescriptions.put("Current .fbp notation directory",
				"currentFBPNetworkDir");
		propertyDescriptions.put("Current package name", "currentPackageName");
		propertyDescriptions.put("Font for code", "fixedFont");
		propertyDescriptions.put("Font for text", "generalFont");
		propertyDescriptions.put("Default font size", "defaultFontSize");
		propertyDescriptions.put("Default component language",
				"defaultCompLang");
		propertyDescriptions.put("JavaFBP jar file", "javaFBPJarFile");
		propertyDescriptions.put("DrawFBP Help jar file", "jhallJarFile");
		propertyDescriptions.put("Additional Jar Files",
				"additionalJarFiles");
		propertyDescriptions.put("Current folder for .exe files",
				"exeDir");
		propertyDescriptions.put("Current folder for .dll files",
				"dllDir");

	}

	void displayProperties() {
		final JFrame jf = new JFrame();
		jf.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				jf.dispose();
			}
		});

		jf.setTitle("List of DrawFBP Properties");
		JPanel panel = new JPanel(new GridBagLayout());
		JScrollPane jsp = new JScrollPane(panel,				
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		jf.setFocusable(true);
		jf.requestFocusInWindow();

		jf.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ev) {
				if (ev.getKeyCode() == KeyEvent.VK_ESCAPE) {
					jf.dispose();
				}
			}
		});

		//jf.setLocation(50, 50);
		panel.setBackground(Color.GRAY);
		//panel.setLocation(getX() + 50, getY() + 50);
		//panel.setLocation(50, 50);
		panel.setSize(1200, 800);
		//jsp.setLocation(50, 50);

		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		panel.setLayout(gbl);

		gbc.fill = GridBagConstraints.BOTH;

		gbc.weightx = 0.5;
		gbc.weighty = 0.5;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;

		JTextField[] tft = new JTextField[4];
		tft[0] = new JTextField(" Property Descriptions");
		tft[1] = new JTextField(" Property ");
		tft[2] = new JTextField(" Current Value ");
		tft[3] = new JTextField(" Starting Value (if different)");

		tft[0].setFont(fontg);
		tft[1].setFont(fontg);
		tft[2].setFont(fontg);
		tft[3].setFont(fontg);

		displayRow(gbc, gbl, tft, panel, Color.BLUE);

		for (String p : propertyDescriptions.keySet()) {
			tft[0] = new JTextField(p);
			String q = propertyDescriptions.get(p);
			tft[1] = new JTextField(q);
			String u = properties.get(q);
			String w = "";
			int i;
			if (u != null)
				w = u;
			boolean done = true;
			boolean first = true;

			while (true) {

				String v = "";
				if (u == null)
					u = "(null)";

				else {
					if (q.equals("additionalJarFiles") && first) {
						first = false;
						continue;
					}
					if ((i = w.indexOf(";")) > -1) {
						u = w.substring(0, i);
						w = w.substring(i + 1);
						done = false;
					} else {
						u = w;
						done = true;
					}

					v = startProperties.get(q);
					if (v == null)
						v = "(null)";
					else if (v.equals(properties.get(q)) || v == null)
						v = "";
				}
				if (q.equals("defaultCompLang")) {
					if (!(u.equals("(null)"))) {
						u = findGLFromLabel(u).showLangs();
						if (!(v.equals("")))
							v = findGLFromLabel(v).showLangs();
					}
				}
				tft[2] = new JTextField(u);
				tft[3] = new JTextField(v);

				tft[0].setFont(fontg);
				tft[1].setFont(fontg);
				tft[2].setFont(fontf);
				tft[3].setFont(fontf);

				displayRow(gbc, gbl, tft, panel, Color.BLACK);

				if (done)
					break;
				tft[0] = new JTextField("");
				tft[1] = new JTextField("");
			}
		}

		// jsp.add(panel);
		jf.add(jsp);
		jf.pack();
		Point p = getLocation();
		jf.setLocation(p.x + 50, p.y + 50);
		//jf.setLocation(100, 100);
		// int height = 200 + properties.size() * 40;
		jf.setSize(1200, 800);
		// jsp.setVisible(true);
		panel.setVisible(true);
		jf.setVisible(true);
		jf.toFront();
		// jdialog.validate();
		jsp.repaint();
		panel.repaint();
		jf.repaint();
		repaint();
	}

	SelectionArea getNewArea() {

		SelectionArea sa = new SelectionArea();
		sa.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		sa.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(escapeKS, "CLOSE");
		sa.getInputMap().put(escapeKS, "CLOSE");

		sa.getActionMap().put("CLOSE", escapeAction);

		return sa;
	}

	File openAction(String fn) {

		File file = null;
		if (fn != null)
			file = new File(fn);
		String fname = fn;

		// if (fn == null) {

		// }

		String fileString = null;

		if (file == null || file.isDirectory()) {
			String s = properties.get("currentDiagramDir");
			if (s == null)
				s = System.getProperty("user.home");
			File f2 = new File(s);
			if (!f2.exists()) {
				MyOptionPane.showMessageDialog(this,
						"Directory '" + s + "' does not exist - reselect",
						MyOptionPane.ERROR_MESSAGE);
				// return null;
				f2 = new File(".");
			}

			MyFileChooser fc = new MyFileChooser(this, f2, diagFCParm);

			int returnVal = fc.showOpenDialog();

			if (returnVal == MyFileChooser.APPROVE_OPTION)
				file = new File(getSelFile(fc));

			if (file == null)
				return file;
		}

		if (file.isDirectory()) {
			MyOptionPane.showMessageDialog(this,
					"File is directory: " + file.getAbsolutePath());
			return null;
		}

		if (!(hasSuffix(file.getName())) && !(file.isDirectory())) {
			String name = file.getAbsolutePath();
			name += ".drw";
			file = new File(name);
		}
		if (!(file.exists()))
			return file;

		if (null == (fileString = readFile(file /*, !SAVEAS */))) {
			MyOptionPane.showMessageDialog(this,
					"Unable to read file: " + file.getName(),
					MyOptionPane.ERROR_MESSAGE);
			return null;
		}

		File currentDiagramDir = file.getParentFile();
		saveProp("currentDiagramDir", currentDiagramDir.getAbsolutePath());
		// saveProperties();

		// int j = jtp.getTabCount();

		ButtonTabComponent b = null;

		int i = getFileTabNo(file.getAbsolutePath());
		if (-1 != i) {
			b = (ButtonTabComponent) jtp.getTabComponentAt(i);
			if (b == null || b.diag == null)
				return null;
			curDiag = b.diag;
			// curDiag.tabNum = i;
			jtp.setSelectedIndex(i);
			// diagFile = b.diag.diagFile;
			return file;
		}
 
		 
		// if last slot has title == (untitled) and is not changed, reuse it
		
		boolean found = false;   
		int j = jtp.getTabCount() - 1;
		if (j > -1) {
			b = (ButtonTabComponent) jtp.getTabComponentAt(j);
			if (b != null && b.diag != null) {
				Diagram d = b.diag;
				if (d.title.equals("(untitled)") && !d.changed) {
					curDiag = d;					
					jtp.setSelectedIndex(j);					
					found = true;					
				}
			}
		}
		 
		if (!found)
			curDiag = getNewDiag();
		curDiag.title = file.getName();
		if (curDiag.title.toLowerCase().endsWith(".drw"))
			curDiag.title = curDiag.title.substring(0,
					curDiag.title.length() - 4);
		
		curDiag.blocks.clear();
		curDiag.arrows.clear();
		curDiag.desc = " ";

		DiagramBuilder.buildDiag(fileString, this, curDiag);
		// driver.jtp.setRequestFocusEnabled(true);
		// driver.jtp.requestFocusInWindow();
		arrowEnd = null; // get rid of black square...
		arrowRoot = null;
		currentArrow = null;
		foundBlock = null;
		//drawToolTip = false;
		blockSelForDragging = null;
		if (curDiag.diagLang != null)
			changeLanguage(curDiag.diagLang);

		fname = file.getName();
		curDiag.diagFile = file;

		// jtp.setSelectedIndex(curDiag.tabNum);
		GenLang gl = null;

		String suff = curDiag.getSuffix(fname);

		if (suff.equals("fbp")) {
			gl = findGLFromLabel("FBP");
			CodeManager cm = new CodeManager(curDiag);
			cm.displayDoc(file, gl, null);
			return file;
		}
		if (!(suff.equals("drw"))) {
			gl = findGLFromLanguage(suff);
			CodeManager cm = new CodeManager(curDiag);
			cm.displayDoc(file, gl, null);
			return file;
		}

		curDiag.title = fname;
		

		setTitle("Diagram: " + curDiag.title);
		if (curDiag.title.toLowerCase().endsWith(".drw"))
			curDiag.title = curDiag.title.substring(0,
					curDiag.title.length() - 4);
		// curDiag.tabNum = i;
		// jtp.setSelectedIndex(curDiag.tabNum);
		repaint();
		return file;
	}
	
	public String readFile(File file /*, boolean saveAs */) {
		StringBuffer fileBuffer;
		String fileString = null;
		int i;
		try {
			FileInputStream in = new FileInputStream(file);
			InputStreamReader dis = new InputStreamReader(in, "UTF-8");
			fileBuffer = new StringBuffer();
			try {
				while ((i = dis.read()) != -1) {
					fileBuffer.append((char) i);
				}

				in.close();

				fileString = fileBuffer.toString();
			} catch (IOException e) {
				MyOptionPane.showMessageDialog(this, "I/O Exception: "
						+ file.getName(), MyOptionPane.ERROR_MESSAGE);
				fileString = "";
			}

		} catch (FileNotFoundException e) {
			//if (!saveAs)
			//	MyOptionPane.showMessageDialog(this, "File not found: "  
			//		+ file.getName(), MyOptionPane.ERROR_MESSAGE);
			return null;
		} catch (IOException e) {
			MyOptionPane.showMessageDialog(this, "I/O Exception 2: "
					+ file.getName(), MyOptionPane.ERROR_MESSAGE);
			return null;
		}
		return fileString;
	} // readFile
	
	// returns index of found tab; -1 if none
	
	int getFileTabNo(String fileName) {
		//int k = jtp.getSelectedIndex();
		
		int j = jtp.getTabCount();
		for (int i = 0; i < j; i++) {
			ButtonTabComponent b = (ButtonTabComponent) jtp
					.getTabComponentAt(i);
			if (b == null)
				continue;
			Diagram d = b.diag;
			if (d == null)
				continue;
			//if (i == k) 
			//	continue;
			File f = d.diagFile;
			if (f != null) {

				String t = f.getAbsolutePath();
				if (t.endsWith(fileName)) {
					return i;
				}
			}
			if (d.title != null && fileName.endsWith(d.title))
				return i;
		}
		return -1;
	}
	
	static boolean hasSuffix(String s) {
		int i = s.lastIndexOf("/");
		int j = s.substring(i + 1).lastIndexOf(".");
		return j > -1;
	}
	
	void saveAction(boolean saveAs) {

		//File file = null;
		//if (curDiag.diagFile == null)
		//	saveAs = true;
		//if (!saveAs)
		File file = (!saveAs) ? curDiag.diagFile : null;

		file = curDiag.genSave(file, curDiag.fCParm[Diagram.DIAGRAM], null);  

		int i = jtp.getSelectedIndex();
		if (file == null) {
			return;
		}

		jtp.setSelectedIndex(i);

		// curDiag.tabNum = i;

		curDiag.title = file.getName();
		curDiag.diagFile = file;
		

		File currentDiagramDir = file.getParentFile();
		setTitle("Diagram: " + curDiag.title);
		if (curDiag.title.toLowerCase().endsWith(".drw"))
			curDiag.title = curDiag.title.substring(0,
					curDiag.title.length() - 4);
		saveProp("currentDiagramDir",
				currentDiagramDir.getAbsolutePath());
		saveProperties();

		curDiag.changed = false;
		repaint();
	}

	/**
	 * Use a BufferedWriter, which is wrapped around an OutputStreamWriter,
	 * which in turn is wrapped around a FileOutputStream, to write the string
	 * data to the given file.
	 */

	public boolean writeFile(File file, String fileString) {
		if (file == null)
			return false;
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "UTF-8"));
			out.write(fileString);
			out.flush();
			out.close();
		} catch (IOException e) {
			System.err.println("File error writing " + file.getAbsolutePath());
			return false;
		}

		return true;
	} // writeFile
	
	
	static String makeAbsFileName(String current, String parent) {
		if (current.equals(""))
			return parent;
		if (current.startsWith("/"))
			return current;
		if (current.length() > 1 && current.substring(1, 2).equals(":"))
			return current;

		String cur = current.replace('\\', '/');
		
		if (parent == null)
			return current;
		
		parent = parent.replace('\\', '/');

		int k = 0;
		int m = 0;

		while (true) {
			if (cur.length() >= 3 && cur.substring(k, k + 3).equals("../")) {
				k += 3;
				m++;
				continue;
			}
			if (cur.length() >= 2 && cur.substring(k, k + 2).equals("./")) {
				k += 2;
				m++;
				continue;
			}
			break;
		}

		// int j = par.lastIndexOf("/");
		// par = par.substring(0, j);

		for (int n = 0; n < m; n++) {
			int j = parent.lastIndexOf("/");
			if (j == -1)
				return current;
			parent = parent.substring(0, j);
		}
		return parent + "/" + cur.substring(k);
	}
	public static BufferedImage readImageFromFile(File file)
			throws IOException {
		return ImageIO.read(file);
	}

	public void menuDeselected(MenuEvent e) {
	}

	public void menuCanceled(MenuEvent e) {
	}

	void changeFonts() {
		fontChooser = new MyFontChooser(this, this);
		chooseFonts(fontChooser);

		if (gFontChanged) {
			saveProp("generalFont", generalFont);
			saveProperties();

			jfl.setText("Fixed font: " + fixedFont + "; general font: "
					+ generalFont);
			fontg = new Font(generalFont, Font.PLAIN, (int) defaultFontSize);
			adjustFonts();
			repaint();
			// repaint();
		}

		if (fFontChanged) {
			saveProp("fixedFont", fixedFont);
			saveProperties();

			jfl.setText("Fixed font: " + fixedFont + "; general font: "
					+ generalFont);
			fontf = new Font(fixedFont, Font.PLAIN, (int) defaultFontSize);
			adjustFonts();
			repaint();
			// repaint();

		}

		return;
	}

	void chooseFonts(MyFontChooser fontChooser) {

		fontChooser.buildFontLists();
		String s = fontChooser.getFixedFont();
		if (s != null) {
			fixedFont = s;
			fFontChanged = true;
		}
		s = fontChooser.getGeneralFont();
		if (s != null) {
			generalFont = s;
			gFontChanged = true;
		}
		fontChooser.done();
	}

	private void changeFontSize() {

		Float[] selectionValues = {new Float(10), new Float(12), new Float(14),
				new Float(16), new Float(18), new Float(20), new Float(22)};
		int j = 0;
		for (int i = 0; i < selectionValues.length; i++) {
			if (Float.compare(selectionValues[i].floatValue(),
					defaultFontSize) == 0)
				j = i;
		}
		Float fs = (Float) MyOptionPane.showInputDialog(this,
				"Font size dialog", "Select a font size",
				MyOptionPane.PLAIN_MESSAGE, null, selectionValues,
				selectionValues[j]);
		if (fs == null)
			return;

		defaultFontSize = fs.floatValue();
		fontg = fontg.deriveFont(fs);
		fontf = fontf.deriveFont(fs);
		jfs.setText("Font Size: " + defaultFontSize);
		adjustFonts();
		// repaint();
		saveProp("defaultFontSize", Float.toString(defaultFontSize));
		saveProperties();
		MyOptionPane.showMessageDialog(this, "Font size changed");
		repaint();
		// repaint();
	}

	void adjustFonts() {
		fileMenu = new JMenu(" File ");
		editMenu = new JMenu(" Edit ");
		helpMenu = new JMenu(" Help ");
		// runMenu = new JMenu(" Run ");

		int j = jtp.getTabCount();
		for (int i = 0; i < j; i++) {
			ButtonTabComponent b = (ButtonTabComponent) jtp
					.getTabComponentAt(i);
			if (b == null || b.diag == null)
				return;
			b.label.setFont(fontf);
		}
		jtp.repaint();

		// osg.setFont(fontg);
		jfl.setFont(fontg);
		jfs.setFont(fontg);
		jfv.setFont(fontg);
		jtp.setFont(fontg);
		zoom.setFont(fontg);
		jtf.setFont(fontg);
		pan.setFont(fontg);
		up.setFont(fontg);
		grid.setFont(fontg);
		scaleLab.setFont(fontg);
		fileMenu.setFont(fontg);
		editMenu.setFont(fontg);
		helpMenu.setFont(fontg);
		// runMenu.setFont(fontg);
		diagDesc.setFont(fontg);

		for (int i = 0; i < but.length; i++) {
			but[i].setFont(fontg);
			but[i].setFocusable(true);
			but[i].addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent ev) {
					if (ev.getKeyCode() == KeyEvent.VK_ENTER) {
						JRadioButton rb = (JRadioButton) ev.getSource();
						rb.setSelected(true);
						setBlkType(rb.getText());
					}
				}
			});
		}

		UIDefaults def = UIManager.getLookAndFeelDefaults();
		 
		final FontUIResource res = new FontUIResource(fontg);
		for (Enumeration<Object> e = def.keys(); e.hasMoreElements();) {
			Object item = e.nextElement();
			if (item instanceof String) {
				String s = (String) item;
				// System.out.println(s + " - " + def.get(item));
				if (def.get(item) instanceof Font) {
					// System.out.println(s + " - " + def.get(item));
					def.put(s, res);
					// UIManager.put(item, fontg);
					// System.out.println(s + " - " + def.get(item));
				}
			}
		}
		UIManager.put("Menu.font", fontg);
		UIManager.put("MenuBar.font", fontg);
		UIManager.put("MenuItem.font", fontg);
		UIManager.put("Label.font", fontf);

		// for (Block block : curDiag.blocks.values()) {
		// block.draw(osg);
		// }

		// for (Arrow arrow : curDiag.arrows.values()) {
		// arrow.draw(osg);
		// }

		if (depDialog != null)
			depDialog.setFont(fontf);

		/*
		 * for (Object item : ht.keySet()) { UIManager.put(item, fontg); //
		 * System.out.println(item + " - " + fontg); }
		 */
		// UIManager.put("Button.select", slateGray1);

		menuBar = createMenuBar();

		setJMenuBar(menuBar);

		repaint();
	}

	final boolean SAVEAS = true;
	
	void compileCode() {

		File cFile = null;
		GenLang gl = curDiag.diagLang;
		Process proc = null;
		//String program = "";
		//interrupt = false;
		
		if (currLang.label.equals("Java")) {
			String ss = properties.get(gl.netDirProp);
			File genDir = null;
			if (ss == null)
				genDir = new File(System.getProperty("user.home"));
			else
				genDir = new File(ss);

			MyFileChooser fc = new MyFileChooser(this,genDir,
					curDiag.fCParm[Diagram.NETWORK]);

			int returnVal = fc.showOpenDialog();

			cFile = null;
			if (returnVal == MyFileChooser.APPROVE_OPTION) {
				cFile = new File(getSelFile(fc));
			}
			// }
			if (cFile == null || !(cFile.exists()))
				return;
			
			

			String srcDir = cFile.getAbsolutePath();
			srcDir = srcDir.replace('\\', '/');
			
			int j = srcDir.lastIndexOf("/");
			progName = srcDir.substring(j + 1);
			srcDir = srcDir.substring(0, j);
			//String clsDir = srcDir;
			//(new File(srcDir)).mkdirs();
			saveProp(gl.netDirProp, srcDir);
			
			String fNPkg = "";
			int k = srcDir.indexOf("/src");
			if (k == -1) {
				MyOptionPane.showMessageDialog(this,
						"File name '" + srcDir + "' - file name should contain 'src' - cannot compile",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}
			
			if (j >= k + 5){
				fNPkg = cFile.getAbsolutePath().substring(k + 5, j)/* + "/" */ ;
				fNPkg = fNPkg.replace("\\", "/");
			}
			String clsDir = srcDir.replace("/src/", "/bin/");
			srcDir = srcDir.substring(0, k + 4); // drop after src
			clsDir = clsDir.substring(0, k + 4); // drop after bin
			//(new File(clsDir)).mkdir();
			
			saveProp("currentClassDir", clsDir);
			//clsDir = clsDir.substring(0, k + 4); 

			File fd = new File(clsDir);

			// pkg = pkg.replace(".", "/");

			fd.mkdirs();
			if (fd == null || !fd.exists()) {				    
				MyOptionPane.showMessageDialog(this,
					"'bin' directory does not exist - " + clsDir,
					MyOptionPane.ERROR_MESSAGE);
				return;
			}
			saveProp("currentClassDir", clsDir);
			if (!(fNPkg.equals(""))) {
				fd = new File(clsDir + "/" + fNPkg);
				fd.mkdirs();
				if (fd == null || !fd.exists()) {
				MyOptionPane.showMessageDialog(this,
						"Directory '" + clsDir + "/" + fNPkg + "' does not exist",
						MyOptionPane.ERROR_MESSAGE);
				return;
				
			}
			}

			if (javaFBPJarFile == null)
				locateJavaFBPJarFile(false);

			//String clsName = progName.replace(".java", ".class");

			// (new File(clsDir + "/" + t + clsName)).delete(); // make sure old
			// class has been deleted

			//String v = "";
			//if (!fNPkg.equals(""))
			//	v = fNPkg + "/";
			//MyOptionPane.showMessageDialog(this,
			//		"Compiling program - " + srcDir + "/" + v + progName,
			//		MyOptionPane.INFORMATION_MESSAGE);	

			proc = null;
			
			String jf = "\"" + javaFBPJarFile; 
			for (String jfv : jarFiles.values()) {
				jf += ";" + jfv;
			}
			jf += ";.\"";			 
			
			srcDir = srcDir.replace("\\",  "/");
			// clsDir = clsDir.replace("\\",  "/");
			
			String jh = System.getenv("JAVA_HOME");
			if (jh == null) {
				MyOptionPane.showMessageDialog(this,
						"Missing JAVA_HOME environment variable",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}
			if (-1 == jh.indexOf("jdk")){
				MyOptionPane.showMessageDialog(this,
						"To do Java compiles, JAVA_HOME environment variable must point at JDK",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}
			String javac = jh + "/bin/javac.exe";
			
			srcDir += "/" + fNPkg;
			//clsDir += "/" + fNPkg;
			
			srcDir = srcDir.replace("\\",  "/");
			clsDir = srcDir.replace("/src/", "/bin/");
			clsDir = clsDir.substring(0, clsDir.indexOf("/bin/") + 4);
			(new File(clsDir)).mkdirs(); 
			
			String w = srcDir + File.separator + progName;
			List<String> params = Arrays.asList("\"" + javac + "\"", 
					// "-verbose",
					"-cp", jf, 
					"-d", "\"" + clsDir + "\"",					 
					"\"" + w + "\""); 
			
			ProcessBuilder pb = new ProcessBuilder(params);
						
			pb.directory(new File(clsDir));

			pb.redirectErrorStream(true);			
			
			output = "";
			
			//new WaitWindow(this); // display "Processing..." message

			// int i = 0;
			String err = "";
			
			try {
				proc = pb.start();

				BufferedReader br = new BufferedReader(
						new InputStreamReader(proc.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					//System.out.println(line);
					output += "<br>" + line;
					// System.out.flush();
				}
			} catch (Exception e) {
				err = analyzeCatch(e);
				if (!err.equals(""))
					proc = null;
			} 
			if (!(output.equals("")) || !(err.equals(""))) {
				MyOptionPane.showMessageDialog(this,
						"<html>Compile output for " + "\"" + srcDir + "/" + progName + "\"<br>" +
				err + "<br>" + output + "<br>" +
				"Jar files:" + jf + "<br>" +
				"Source dir: " + srcDir + "<br>" +
				"Class dir: " + clsDir + "<br>" +
				"File name: " + progName + "</html>",
						MyOptionPane.ERROR_MESSAGE);
				//return;
			} 
			
			if (proc == null) 
				return;
			int u = 0;
				try {
					proc.waitFor();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				//interrupt = true;
				
				
				
				proc.destroy();
				u = proc.exitValue();
			 
				clsDir += "/" + fNPkg;
				
				  
				if (u == 0)
					MyOptionPane.showMessageDialog(this,
							"Program compiled - " + srcDir + "/" + progName
									+ "\n" + "   into - " + clsDir + "/"  
									+ progName.substring(0,
											progName.length() - 5)
									+ ".class",
							MyOptionPane.INFORMATION_MESSAGE);
				else
					MyOptionPane.showMessageDialog(this,
							"<html>Program compile failed, rc: " + u + " - " + srcDir
									+ "/" + progName + "<br>" +
									output + "</html>",
							MyOptionPane.WARNING_MESSAGE);
			  
		}

		else {

			if (!(currLang.label.equals("C#"))) {

				MyOptionPane.showMessageDialog(this,
						"Language not supported: " + currLang.label,
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

			// Start of C# part...

			String srcDir = properties.get("currentCsharpNetworkDir");
			
			if (srcDir == null)
				srcDir = System.getProperty("user.home");	

			MyFileChooser fc = new MyFileChooser(this,new File(srcDir),
					curDiag.fCParm[Diagram.PROCESS]);

			int returnVal = fc.showOpenDialog();

			String ss = null;
			cFile = null;
			if (returnVal == MyFileChooser.APPROVE_OPTION) {
				ss = getSelFile(fc);
				cFile = new File(ss);
			}
			// }
			if (cFile == null || !(cFile.exists()))
				return;

			if (!(ss.endsWith(".cs"))) {
				MyOptionPane.showMessageDialog(this,
						"C# program " + ss + " must end in '.cs'",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

			ss = ss.replace('\\', '/');
			//int j = ss.lastIndexOf("/");

			//String progName = ss.substring(j + 1);

			// ss = ss.substring(0, ss.length() - 3); // drop .cs suffix

			String progString = readFile(new File(ss) /*, !SAVEAS */);
			if (progString == null) {
				MyOptionPane.showMessageDialog(this,
						"Program not found: " + ss, MyOptionPane.ERROR_MESSAGE);
				return;
			}
			String t = "";
			String v = "";
			srcDir = ss;
			int k = progString.indexOf("namespace ");
			if (k > -1) {				
				k += 10; // skip over "namespace"
				int ks = k;

				while (true) {
					if (progString.substring(k, k + 1).equals(" ")
							|| progString.substring(k, k + 1).equals("{")
							|| progString.substring(k, k + 1).equals("\r")
							|| progString.substring(k, k + 1).equals("\n"))
						break;
					k++;
				}

				v = progString.substring(ks, k); // get name of
																// namespace
				v = v.replace(".", "/");
				//t = cFile.getAbsolutePath();
				//t = t.replace("\\", "/");
				//k = t.indexOf(v);
				
				//srcDir = ss.substring(0, k); // drop before namespace
													// string
			}
			 
			//if (srcDir.endsWith("/"))
			//	srcDir = srcDir.substring(0, srcDir.length() - 1);
			
			String trunc = ss.substring(0, ss.lastIndexOf("/"));
			String progName = ss.substring(ss.lastIndexOf("/") + 1);
			
			
			File f = new File(trunc);
			f.mkdirs();
			if (f == null || !f.exists()) {				
				MyOptionPane.showMessageDialog(this,
						"'bin' directory does not exist - " + f.getAbsolutePath(),
						MyOptionPane.ERROR_MESSAGE);
				return;
		}
			
			saveProp("currentCsharpNetworkDir",
					trunc);
			
								
			ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "cd '" + trunc + "' && dir");
			try {
				proc = pb.start();			
				proc.waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String target = /*trunc + "/" + */ "bin/Debug";  //  we've done a cd, so we don't need trunc
			
			File f2 = new File(target);
			f2.mkdirs();
			if (f2 == null || !f2.exists()) {				
				MyOptionPane.showMessageDialog(this,
						"'bin' directory does not exist - " + f2.getAbsolutePath(),
						MyOptionPane.ERROR_MESSAGE);
				return;
		}
			
			//MyOptionPane.showMessageDialog(this,
			//		"Starting compile - " + ss,
			//      MyOptionPane.INFORMATION_MESSAGE);

			proc = null;
			progName = progName.substring(0, progName.length() - 3); // drop .cs
			
			String z = properties.get("additionalDllFiles");
			boolean gotDlls = -1 < z.indexOf("FBPLib.dll") && -1 < z.indexOf("FBPVerbs.dll");
					
			List<String> cmdList = new ArrayList<String>();
            cmdList.add("csc");
            cmdList.add("-t:exe");
            t = t.replace("\\", "/");
            t = t.replace("/", ".");
            //if (v.equals(""))
            //cmdList.add("-main:" + progName);
            //else
            //cmdList.add("-main:" + v + "." + progName);
            //progName = progName.substring(0, progName.length() - 3);  // drop the .cs
            cmdList.add("-out:" + target + "/" + v + ".exe");
            			
			if (!gotDlls  && !gotDllReminder) {
				MyOptionPane.showMessageDialog(this,
						"If you are using FBP, you will need a FBPLib dll and a FBPVerbs dll - use File/Add Additional Dll File for each one",
						MyOptionPane.WARNING_MESSAGE);
				gotDllReminder = true;
				return;
			}
			
			else {
				Iterator<Entry<String, String>> entries = dllFiles.entrySet().iterator();
				//z = "";
				//String cma = "";

				 
				//String w = "";
				String libs = "";
				String cma = "";
				while (entries.hasNext()) {
					Entry<String, String> thisEntry = entries.next();
					if (!(new File(thisEntry.getValue()).exists())) {
						MyOptionPane.showMessageDialog(this,
								"Dll file does not exist: " + thisEntry.getValue(),
								MyOptionPane.WARNING_MESSAGE);
						return;
					}
					//z += "\"/r:" + thisEntry.getValue() + "\" ";
					//cma = ";";
					String w = thisEntry.getValue();
					w = w.replace("\\", "/");
					int j = w.indexOf("bin/Debug");
					libs += cma + w.substring(0, j);
					cma = ",";
					cmdList.add("-lib:" + libs);
					
					
				}
				entries = dllFiles.entrySet().iterator();
				while (entries.hasNext()) {
					Entry<String, String> thisEntry = entries.next();
					String w = thisEntry.getValue();
					w = w.replace("\\", "/");
					int j = w.indexOf("bin/Debug");
					cmdList.add("-r:" + w.substring(j));
				}
				 
				//cmdList.add("\"/r:C:/Users/Paul/My Documents/GitHub/csharpfbp/FBPLib/bin/Debug/FBPLib.dll\"");
				//cmdList.add("\"/r:C:/Users/Paul/My Documents/GitHub/csharpfbp/FBPVerbs/bin/Debug/FBPVerbs.dll\"");
			}					
			//String w = "\"" + trunc + "/" + "*.cs\"";
			ss = ss.replace("\\",  "/");
			cmdList.add(/*trunc + "/" +   */ "*.cs");			
			
			/* ProcessBuilder*/ pb = new ProcessBuilder(cmdList);

			pb.directory(new File(trunc));
			
			pb.redirectErrorStream(true);
			//MyOptionPane.showMessageDialog(this,
			//		"Compiling program - " + srcDir + "/" + v + progName,
			//		MyOptionPane.INFORMATION_MESSAGE);
			
			//new WaitWindow(this); // display "Processing..." message
			 
			String err = "";
			output = "";
			try {
				proc = pb.start();

				BufferedReader br = new BufferedReader(
						new InputStreamReader(proc.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					output += line + "<br>";
				}
			} catch (Exception e) {
				err = analyzeCatch(e);
				if (!err.equals(""))
					proc = null;
			} 

			//interrupt = true;
			//program = v + "/" + progName + ".cs";
			int u = 0;
			//if (!(output.equals("")) || !(err.equals(""))) {
			//	MyOptionPane
			//			.showMessageDialog(this,
			//					"<html>Compile output for " + target + "/" + v + ".exe <br>" +
			//							err + "<br>" + output + "</html>",
			//					MyOptionPane.ERROR_MESSAGE);
				//return;
			//} 
			if (proc == null)
				return;
				
				try {
					proc.waitFor();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				proc.destroy();
			 

			u = proc.exitValue();
			//interrupt = true;
			 
			//setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			 
			if (u == 0) {

				MyOptionPane.showMessageDialog(this,
						"Programs compiled and linked - " + trunc + "/"
								+ "*.cs\n" + "   into - " +  trunc + "/bin/Debug/" + v + ".exe",
						MyOptionPane.INFORMATION_MESSAGE);
				saveProp("exeDir", trunc)  ;
			}
			else
				MyOptionPane.showMessageDialog(this,
						"<html>Program compile failed, rc: " + u + " - " + trunc + "/*.cs" + "<br>" +
						"errcode: " + err + "<br>"	+	
				         output + "</html>" ,
						MyOptionPane.WARNING_MESSAGE);
			
			 
		}
		
	}

	void runCode() {

		File cFile = null;
		//program = "";
		//Process proc = null;
		//interrupt = false;
		
		if (currLang.label.equals("Java")) {

			String ss = properties.get("currentClassDir");
			String clsDir = null;
			if (ss == null)
				clsDir = System.getProperty("user.home");
			else
				clsDir = ss;

			String savePrompt = curDiag.fCParm[Diagram.CLASS].prompt;
			curDiag.fCParm[Diagram.CLASS].prompt = "Select program to be run from class directory or jar file";
			MyFileChooser fc = new MyFileChooser(this, new File(clsDir), curDiag.fCParm[Diagram.CLASS]);

			int returnVal = fc.showOpenDialog();
			curDiag.fCParm[Diagram.CLASS].prompt = savePrompt;

			cFile = null;
			if (returnVal == MyFileChooser.APPROVE_OPTION) {
				ss = getSelFile(fc);
				cFile = new File(ss);
			}
			// }
			if (cFile == null || !(cFile.exists()))
				return;

			// if (currLang.label.equals("Java")) {
			if (!(ss.endsWith(".class"))) {
				MyOptionPane.showMessageDialog(this,
						"Executable " + ss + " must end in '.class'",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

			ss = ss.replace('\\', '/');
			int j = ss.lastIndexOf("/");
			
			saveProp("currentClassDir", ss.substring(0, j));

			progName = ss.substring(j + 1);

			// if (currLang.label.equals("Java"))
			ss = ss.substring(0, ss.length() - 6); // drop .class suffix

			int k = ss.indexOf("/bin");
			String t = "";
			if (k > -1) {
				if (j > k + 5) {
					t = cFile.getAbsolutePath().substring(k + 5, j);
					t = t.replace("\\", "/");
				}

				String u = ss.substring(0, k + 4);
				u = u.replace("\\",  "/");
				clsDir = u; // drop after bin
			}

			//clsDir.mkdirs(); 
			if (clsDir == null || !(new File(clsDir)).exists()) {				
			MyOptionPane.showMessageDialog(this,
							"'bin' directory does not exist - " + clsDir,
							MyOptionPane.ERROR_MESSAGE);
					return;
			}
			//saveProp("currentClassDir", clsDir);

			progName = progName.substring(0, progName.length() - 6);
			if (!(t.equals("")))
				progName = t.replace("\\", "/") + "/" + progName;
			progName = progName.replace("/", ".");

			URL[] urls = buildUrls(null);

			URLClassLoader loader = null;
			Class<?> cls = null;
			if (urls != null) {

				// Create a new class loader with the directory
				loader = new URLClassLoader(urls,
						this.getClass().getClassLoader());

				try {
					// cls = loader.loadClass(thisCls);
					cls = loader.loadClass(progName);
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			// Class<?> cls = null;
			// cls = loader.loaderClass(thisCls);

			if (cls == null) {
				MyOptionPane.showMessageDialog(this,
						"Class not generated for program " + progName ,
						MyOptionPane.ERROR_MESSAGE);
				return;
			}
			Method meth = null;
			try {
				meth = cls.getMethod("main", String[].class);
			} catch (NoSuchMethodException | SecurityException e2) {
				meth = null;
				MyOptionPane.showMessageDialog(this,
						"Program \"" + progName + "\" has no 'main' method",
						MyOptionPane.ERROR_MESSAGE);
			}
			if (meth == null) {

				return;
			}

			// if(javaFBPJarFile == null)
			// locateJavaFBPJarFile();
			
			String jh = System.getenv("JAVA_HOME");
			if (jh == null) {
				MyOptionPane.showMessageDialog(/*this */ driver,
						"Missing JAVA_HOME environment variable",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}
			if (-1 == jh.indexOf("jdk") && -1 == jh.indexOf("jre")){
				MyOptionPane.showMessageDialog(/*this */ driver,
						"To run Java commmand, JAVA_HOME environment variable must point at JDK or JRE",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}
			String java = jh + "/bin/java";
			
			pBCmdArray = new String[] {
					java, "-cp",
					"\"" + javaFBPJarFile + ";.\"", "\"" + progName + "\""	
			};
			
			int m = clsDir.indexOf("/bin");
			pBDir = clsDir.substring(0, m + 4) + "/";

			Thread runthr = new Thread(new RunTask());
			runthr.start();
			
					
			// program = clsDir + "/" + progName;  
					//+ ".class";
		}

		else {

			if (!(currLang.label.equals("C#"))) {

				MyOptionPane.showMessageDialog(this,
						"Language not supported: " + currLang.label,
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

				
			
			String exeDir = properties.get("exeDir");
			if (exeDir == null)
				exeDir = System.getProperty("user.home");

			//ProcessBuilder pb = null;
			MyFileChooser fc = new MyFileChooser(this,new File(exeDir),
					curDiag.fCParm[Diagram.EXE]);

			int returnVal = fc.showOpenDialog();

			cFile = null;
			String exeFile = "";
			if (returnVal == MyFileChooser.APPROVE_OPTION) {
				exeFile = getSelFile(fc);
			}

			if (!(exeFile.endsWith(".exe"))) {
				MyOptionPane.showMessageDialog(this,
						"Executable " + exeFile + " must end in '.exe'",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

			exeFile = exeFile.replace("\\",  "/");
			int k = exeFile.lastIndexOf("bin/Debug/");
			exeDir = exeFile.substring(0, k + 10);
			
			saveProp("exeDir", exeDir);

			//exeFile = exeFile.replace("\\",  "/");
			
			//List<String> cmdList = new ArrayList<String>();			
			
			//cmdList.add("\"" + exeFile + "\"");
			//cmdList.add(exeFile);
						
			progName = exeFile.substring(exeFile.lastIndexOf("/") + 1);
			
			///ProcessBuilder pb = new ProcessBuilder(pBCmdArray);

			///pb.directory(new File(pBDir));
			
			pBCmdArray = new String[1];
			pBCmdArray[0] = exeFile;
			
			pBDir = exeDir;
			
			/*
			
			pb = new ProcessBuilder(pBCmdArray);
			
			pb.directory(new File(pBDir));

			pb.redirectErrorStream(true);
			
			//new WaitWindow(this); // display "Processing..." message
			output = "";
			String err = "";
			try {
				proc = pb.start();

				BufferedReader br = new BufferedReader(
						new InputStreamReader(proc.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					output += line + "<br>";
				}
			} catch (NullPointerException npe) {
				err = "Null Pointer Exception"; 
				proc = null;
				//return;
			} catch (IOException ioe) {
				err = "I/O Exception"; 
				proc = null;
				//return;
			} catch (IndexOutOfBoundsException iobe) {
				err = "Index Out Of Bounds Exception"; 
				proc = null;
				//return;
			} catch (SecurityException se) {
				err = "Security Exception"; 
				proc = null;
				//return;
			}
			if (!(err.equals("")) || !(output.equals(""))) {
				MyOptionPane.showMessageDialog(this, "<html>Run output<br>" + err + "<br>" + output + "</html>",
						MyOptionPane.ERROR_MESSAGE);
				//return;
			}
			if (proc != null) {
				try {
					proc.waitFor();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				
				proc.destroy();
			}
		}
		//interrupt = true;
		 
		
		if (proc == null)
			return;
		int u = proc.exitValue(); 
		if (u == 0)
			MyOptionPane.showMessageDialog(this,
					"Program completed - " + progName,
					MyOptionPane.INFORMATION_MESSAGE);
		else
			MyOptionPane.showMessageDialog(this,
					"Program test failed, rc: " + u + " - " + progName,
					MyOptionPane.WARNING_MESSAGE);
		*/
			Thread runthr = new Thread(new RunTask());
			runthr.start();
		}
	}
	
	String analyzeCatch(Exception e) {
		String err = "";
		
		 if (e instanceof NullPointerException) 
			err = "Null Pointer Exception"; 
		
		 if (e instanceof IOException) 
			err = "I/O Exception"; 				
				
		if (e instanceof IndexOutOfBoundsException) 
			err = "Index Out Of Bounds Exception"; 				
				
		if (e instanceof SecurityException) 
			err = "Security Exception";
		
		err += ": " + e.getMessage();
		return err;
	}
	
	/**
	 * 
	 * In the following, the description will be considered to identify a block - 
	 * unless new and old blocks are at (approximately) the same place
	 * An output port can only be attached to one arrow, so the process description and port name  
	 * will be considered to uniquely identify an arrow 
	 * 
	 */
	
	void compare() {
		MyOptionPane.showMessageDialog(driver,
				"Select diagram to be compared against - OK if already open!",			
				MyOptionPane.INFORMATION_MESSAGE);
		
		Diagram newDiag = curDiag;
		
		HashMap<String, Block> newBMap = new HashMap<String, Block>();
		for (Block blk : newDiag.blocks.values()) {			
				newBMap.put(blk.desc, blk);			
		}
		

		HashMap<String, Arrow> newAMap = new HashMap<String, Arrow>();
		for (Arrow arr : newDiag.arrows.values()) {
			String key = newDiag.blocks.get(new Integer(arr.fromId)).desc + "~" + arr.upStreamPort;
			newAMap.put(key, arr);
		}
		
		Diagram oldDiag = null;

		String t = curDiag.diagFile.getParent();
		MyFileChooser fc = new MyFileChooser(driver, new File(t),
				curDiag.fCParm[Diagram.DIAGRAM]);

		int returnVal = fc.showOpenDialog();

		if (returnVal != MyFileChooser.APPROVE_OPTION)
			return;

		String dFN = getSelFile(fc);
		String suff = curDiag.fCParm[Diagram.DIAGRAM].fileExt;
		if (!(dFN.endsWith(suff)))
			dFN += suff;
		File cFile = new File(dFN);
		if (cFile == null || !(cFile.exists()))
			return;
		
		int k = getFileTabNo(dFN);
		if (k == -1)
			openAction(dFN);
		else
			jtp.setSelectedIndex(k);
		
		oldDiag = curDiag;
		
		MyOptionPane.showMessageDialog(driver,
				"Comparing " + newDiag.diagFile.getAbsolutePath() + " against " + oldDiag.diagFile.getAbsolutePath() ,			
				MyOptionPane.INFORMATION_MESSAGE);

		HashMap<String, Block> oldBMap = new HashMap<String, Block>();
		for (Block blk : oldDiag.blocks.values()) {			
				oldBMap.put(blk.desc, blk);			
		}
		
		HashMap<String, Arrow> oldAMap = new HashMap<String, Arrow>();
		for (Arrow arr : oldDiag.arrows.values()) {
			String key = oldDiag.blocks.get(new Integer(arr.fromId)).desc + "~" + arr.upStreamPort;
			oldAMap.put(key, arr);
		}
		
		curDiag = newDiag;

		for (Block blk : newDiag.blocks.values()) {
			
			for (Block b : oldDiag.blocks.values()) {
				if ((Math.abs(b.cx - blk.cx) <= 50)
						&& (Math.abs(b.cy - blk.cy) <= 30)) {
							if (blk.desc != null && b.desc == null ||
								blk.desc == null && b.desc != null)
									blk.compareFlag = "C";
								if (blk.desc == null || b.desc == null)
									continue;
								if (!(blk.type.equals(b.type)) || !(blk.desc.equals(b.desc))) {
									blk.compareFlag = "C";
									b.compareFlag = " ";  // to ensure doesn't look like a delete!
								}
							break;
						}	
				}
			 
			if (blk.compareFlag != null)
				continue;
			
			Block b2 = oldBMap.get(blk.desc);
			if (b2 == null)
				blk.compareFlag = "A";
			else {
				if ((Math.abs(b2.cx - blk.cx) > 50)
						|| (Math.abs(b2.cy - blk.cy) > 30))
					blk.compareFlag = "M";
			}
		}

		
		
		
		for (Block b : oldDiag.blocks.values()) {
			if (b.compareFlag != null)
				continue;
			Block blk = newBMap.get(b.desc);
			if (blk == null) {
				Block gBlk = createBlock(b.type, b.cx, b.cy, newDiag,
						false);
				if (gBlk != null) {
					gBlk.desc = b.desc;
					gBlk.compareFlag = "D";
					gBlk.codeFileName = "ghost";
				}
			}
		}
	
		 
		for (Arrow arr : newDiag.arrows.values()) {
			 
			for (Arrow a : oldDiag.arrows.values()) {
				if ((a.fromX == arr.fromX)
						|| (a.fromY == arr.fromY)
						|| (a.toX == arr.toX) 
						|| (a.toY == arr.toY)) 
					// probably same line - now check for diffs
					        if (a.fromId != arr.fromId ||
					            a.toId != arr.toId ||
					            !a.upStreamPort.equals(arr.upStreamPort) ||
					            !a.downStreamPort.equals(arr.downStreamPort) ) {
									arr.compareFlag = "C";								
									a.compareFlag = " ";  // to ensure doesn't look like a delete!
								
							break;
						}	
			}
			 
			if (arr.compareFlag != null)
				continue;
			
			String key = newDiag.blocks.get(new Integer(arr.fromId)).desc + "~" + arr.upStreamPort;
			Arrow a2 = oldAMap.get(key);
			if (a2 == null)
				arr.compareFlag = "A";			
		}
 

		 
		for (Arrow a : oldDiag.arrows.values()) {
			if (a.compareFlag != null)
				continue;
			Block from = oldDiag.blocks.get(new Integer(a.fromId));
			String key = from.desc + "~" + a.upStreamPort;
			Arrow aNew = newAMap.get(key);
			if (aNew == null) {				
				Arrow gArr = oldDiag.copyArrow(a, newDiag, from);
				if (gArr != null) {
					gArr.compareFlag = "D";
				}
			}
		}
	 
		
		int j = getFileTabNo(curDiag.diagFile.getAbsolutePath());
		if (-1 != j) {
			ButtonTabComponent b = (ButtonTabComponent) jtp.getTabComponentAt(j);
			if (b == null || b.diag == null)
				return;
			// curDiag = b.diag; (redundant)
			// curDiag.tabNum = i;
			jtp.setSelectedIndex(j);
		}

		for (Block blk : oldDiag.blocks.values()) {
			blk.compareFlag = null;
		}
		
		repaint();
	}

	// 'between' checks that the value val is >= lim1 and <= lim2 - or the
	// inverse

	static boolean between(int val, int lim1, int lim2) {
		return between((double) val, (double) lim1, (double) lim2);
	}

	static boolean between(double val, double lim1, double lim2) {
		boolean res;
		res = val >= lim1 && val <= lim2 && lim1 < lim2;
		res = res || val >= lim2 && val <= lim1 && lim2 < lim1;
		return res;
	}

	boolean readPropertiesFile() {

		if (propertiesFile == null) {
			String uh = System.getProperty("user.home");
			propertiesFile = new File(
					uh + "/" + "DrawFBPProperties.xml");

		}
		BufferedReader in = null;
		String s = null;
		try {
			in = new BufferedReader(new FileReader(propertiesFile));
		} catch (FileNotFoundException e) {
			return false;
		}

		while (true) {
			try {
				s = in.readLine();
			} catch (IOException e) {
			}
			if (s == null)
				break;
			s = s.trim();
			if (s.equals("<properties>") || s.equals("</properties>"))
				continue;
			if (s.startsWith("<?xml"))
				continue;

			int i = s.indexOf("<");
			int j = s.indexOf(">");
			if (i > -1 && j > -1 && j > i + 1) {
				String key = s.substring(i + 1, j);
				s = s.substring(j + 1);  // value
				int k = s.indexOf("<");
				String u = "";
				if (k > 0) {
					if (!(key.equals("additionalJarFiles") || key.equals("additionalDllFiles"))) {
						s = s.substring(0, k).trim();
						key = key.replace("\\", "/");
						if (-1 == key.indexOf("/")) // compensate for old bug (key and value were reversed)!
							saveProp(key, s);
					} else {
						// additionalJar/DllFiles
						HashMap<String, String> list = key.equals("additionalJarFiles")? jarFiles: dllFiles;
						s = s.substring(0, k).trim();
						while (true) {
							int m = s.indexOf(";");
							if (m == -1) {
								u = s;
								int n = u.indexOf(":");

								if (n == -1)
									break;

								//saveProp("addnl_jf_" + u.substring(0, n),  
								// 		u.substring(n + 1));
								list.put(u.substring(0, n),
										u.substring(n + 1));
								break;
							} else {
								u = s.substring(0, m);
								s = s.substring(m + 1);
								int n = u.indexOf(":");

								if (n == -1)
									break;

								//saveProp("addnl_jf_" + u.substring(0, n),
								// 		u.substring(n + 1));
								list.put(u.substring(0, n),
										u.substring(n + 1));
							}
						}
					}

				}
			}

		}

		try {
			
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;

	}

	void writePropertiesFile() {
		BufferedWriter out = null;

		try {
			out = new BufferedWriter(new FileWriter(propertiesFile));
			out.write("<?xml version=\"1.0\"?> \n");
			out.write("<properties> \n");
			for (String k : properties.keySet()) {
				if (k.startsWith("addnl_jf_") ||
						 k.startsWith("additionalJarFiles") ||
						 k.startsWith("additionalDllFiles"))
					continue;
				String s = "<" + k + ">" + properties.get(k) + "</" + k
						+ "> \n";
				out.write(s);
			}
			
			Iterator<Entry<String, String>> entries = jarFiles.entrySet()
					.iterator();
			String z = "";
			String cma = "";

			while (entries.hasNext()) {
				Entry<String, String> thisEntry = entries.next();

				z += cma + thisEntry.getKey() + ":" + thisEntry.getValue();
				cma = ";";

			}
			String s = "<additionalJarFiles> " + z + "</additionalJarFiles> \n";
			out.write(s);
			
			entries = dllFiles.entrySet()
					.iterator();
			z = "";
			cma = "";

			while (entries.hasNext()) {
				Entry<String, String> thisEntry = entries.next();

				z += cma + thisEntry.getKey() + ":" + thisEntry.getValue();
				cma = ";";

			}
			s = "<additionalDllFiles> " + z + "</additionalDllFiles> \n";
			
			out.write(s);
			out.write("</properties> \n");
			// Close the BufferedWriter
			out.flush();
			out.close();

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	
	String getSelFile(MyFileChooser fc) {
		String[] sa = new String[1];
		fc.getSelectedFile(sa); // getSelectedFile puts result in sa[0]
		return sa[0];
	}

	boolean locateJavaFBPJarFile(boolean checkLocation) {

		// setting of checkLocation doesn't matter if javaFBPJarFile is null!
		
		String s = properties.get("javaFBPJarFile");
		javaFBPJarFile = s;

		boolean findJar = false;
		if (checkLocation || s == null)
			findJar = true;
		
		if (findJar) {
			if (s == null) {
				MyOptionPane.showMessageDialog(this,
						"To access Java classes - continue to File Chooser to locate Java class jar file",
						MyOptionPane.WARNING_MESSAGE);	
			} else {
				MyOptionPane.showMessageDialog(this,
						"JavaFBP jar file location: " + s,
						MyOptionPane.INFORMATION_MESSAGE);			

			int res = MyOptionPane.showConfirmDialog(this,					
					"Change JavaFBP jar file location?",
					"Change JavaFBP jar file", MyOptionPane.YES_NO_OPTION);	
			if (res != MyOptionPane.YES_OPTION)
				return true;
			}

			//MyOptionPane.showMessageDialog(this,
			//		"Use File Chooser to locate JavaFBP jar file",
			//		MyOptionPane.WARNING_MESSAGE);

			File f = new File(System.getProperty("user.home"));

			// else
			// f = (new File(s)).getParentFile();

			MyFileChooser fc = new MyFileChooser(this,f, curDiag.fCParm[Diagram.JARFILE]);

			int returnVal = fc.showOpenDialog();

			File cFile = null;
			if (returnVal == MyFileChooser.APPROVE_OPTION) {
				cFile = new File(getSelFile(fc));
				if (cFile == null || !(cFile.exists())) {
					MyOptionPane.showMessageDialog(this,
							"Unable to read JavaFBP jar file "
									+ cFile.getName(),
							MyOptionPane.ERROR_MESSAGE);
					return false;
				}
				// diag.currentDir = new File(cFile.getParent());
				javaFBPJarFile = cFile.getAbsolutePath();
				saveProp("javaFBPJarFile", javaFBPJarFile);

				saveProperties();
				MyOptionPane.showMessageDialog(this,
						"JavaFBP jar file location: " + cFile.getAbsolutePath(),
						MyOptionPane.INFORMATION_MESSAGE);
				// jarFiles.put("JavaFBP Jar File", cFile.getAbsolutePath());
				for (int i = 0; i < jtp.getTabCount(); i++) {
					ButtonTabComponent b = (ButtonTabComponent) jtp
							.getTabComponentAt(i);
					if (b == null || b.diag == null)
						return false;

					Diagram d = b.diag;
					if (d == null)
						continue;

					for (Block bk : d.blocks.values()) {
						bk.getClassInfo(bk.fullClassName);
					}
				}
				
			}
			
		}
		
		return true;
	}

	
	
	boolean addAdditionalJarFile() {

		String ans = (String) MyOptionPane.showInputDialog(this,
				"Enter Description of jar file being added",
				"Enter Description", MyOptionPane.PLAIN_MESSAGE, null, null,
				null);
		if (ans == null || ans.equals("")) {
			MyOptionPane.showMessageDialog(this, "No description entered",
					MyOptionPane.ERROR_MESSAGE);
			return false;
		}

		String s = properties.get("javaFBPJarFile");
		File f = null;
		if (s == null)
			f = new File(System.getProperty("user.home"));
		else
			f = (new File(s)).getParentFile();
				
		curDiag.fCParm[Diagram.JARFILE].prompt = "Specify file name for " + ans + " jar file";
		MyFileChooser fc = new MyFileChooser(this,f, curDiag.fCParm[Diagram.JARFILE]);	
		int returnVal = fc.showOpenDialog();
		File cFile = null;
		if (returnVal == MyFileChooser.APPROVE_OPTION) {
			cFile = new File(getSelFile(fc));
			if (cFile == null || !(cFile.exists())) {
				MyOptionPane.showMessageDialog(this,
						"Unable to read additional jar file " + cFile.getName(),
						MyOptionPane.ERROR_MESSAGE);
				return false;
			}

			jarFiles.put(ans, cFile.getAbsolutePath());

			@SuppressWarnings("rawtypes")
			Iterator entries = jarFiles.entrySet().iterator();
			String t = "";
			String cma = "";

			while (entries.hasNext()) {
				@SuppressWarnings("unchecked")
				Entry<String, String> thisEntry = (Entry<String, String>) entries
						.next();

				t += cma + thisEntry.getKey() + ":" + thisEntry.getValue();
				cma = ";";

			}
			saveProp("additionalJarFiles", t);
			MyOptionPane.showMessageDialog(this,
					"Additional jar file added: " + cFile.getName(),
					MyOptionPane.INFORMATION_MESSAGE);

			saveProperties();

		}
		return true;
	}
	
	boolean addAdditionalDllFile() {

		String ans = (String) MyOptionPane.showInputDialog(this,
				"Enter Description of dll file being added",
				"Enter Description", MyOptionPane.PLAIN_MESSAGE, null, null,
				null);
		if (ans == null || ans.equals("")) {
			MyOptionPane.showMessageDialog(this, "No description entered",
					MyOptionPane.ERROR_MESSAGE);
			return false;
		}

		String s = properties.get("dllFileDir");
		File f = null;
		if (s == null)
			f = new File(System.getProperty("user.home"));
		else
			f = (new File(s)).getParentFile();
		MyFileChooser fc = new MyFileChooser(this,f, curDiag.fCParm[Diagram.DLL]);

		curDiag.fCParm[Diagram.DLL].prompt = "Specify file name for " + ans + " dll";
		int returnVal = fc.showOpenDialog();
		File cFile = null;
		if (returnVal == MyFileChooser.APPROVE_OPTION) {
			cFile = new File(getSelFile(fc));
			if (cFile == null || !(cFile.exists())) {
				MyOptionPane.showMessageDialog(this,
						"Unable to read additional dll file " + cFile.getName(),
						MyOptionPane.ERROR_MESSAGE);
				return false;
			}

			dllFiles.put(ans, cFile.getAbsolutePath());

			@SuppressWarnings("rawtypes")
			Iterator entries = dllFiles.entrySet().iterator();
			String t = "";
			String cma = "";

			while (entries.hasNext()) {
				@SuppressWarnings("unchecked")
				Entry<String, String> thisEntry = (Entry<String, String>) entries
						.next();

				t += cma + thisEntry.getKey() + ":" + thisEntry.getValue();
				cma = ";";

			}
			
			saveProp("additionalDllFiles", t);
			
			String u = cFile.getParent();
			saveProp("dllFileDir", u);
			MyOptionPane.showMessageDialog(this,
					"Additional dll file added: " + cFile.getName(),
					MyOptionPane.INFORMATION_MESSAGE);

			saveProperties();

		}
		return true;
	}

	
		
	boolean locateJhallJarFile(boolean checkLocation) {

		// setting of checkLocation doesn't matter if javaFBPJarFile is null!
		
		String s = properties.get("jhallJarFile");
		javaFBPJarFile = s;

		boolean findJar = false;
		if (checkLocation || s == null)
			findJar = true;
		
		if (findJar) {
			if (s != null) {
				MyOptionPane.showMessageDialog(this,
						"JavaHelp jar file location: " + s,
						MyOptionPane.INFORMATION_MESSAGE);			

			int res = MyOptionPane.showConfirmDialog(this,					
					"Change JavaHelp jar file location?",
					"Change JavaHelp jar file", MyOptionPane.YES_NO_OPTION);	
			if (res != MyOptionPane.YES_OPTION)
				return true;
			}

			//MyOptionPane.showMessageDialog(this,
			//		"Use File Chooser to locate JavaFBP jar file",
			//		MyOptionPane.WARNING_MESSAGE);

			File f = new File(System.getProperty("user.home"));

			// else
			// f = (new File(s)).getParentFile();

			MyFileChooser fc = new MyFileChooser(this,f, curDiag.fCParm[Diagram.JHELP]);

			int returnVal = fc.showOpenDialog();

			File cFile = null;
			if (returnVal == MyFileChooser.APPROVE_OPTION) {
				cFile = new File(getSelFile(fc));
				if (cFile == null || !(cFile.exists())) {
					MyOptionPane.showMessageDialog(this,
							"Unable to read JavaHelp jar file "
									+ cFile.getName(),
							MyOptionPane.ERROR_MESSAGE);
					return false;
				}
				// diag.currentDir = new File(cFile.getParent());
				jhallJarFile = cFile.getAbsolutePath();
				saveProp("jhallJarFile", jhallJarFile);

				saveProperties();
				MyOptionPane.showMessageDialog(this,
						"JavaHelp jar file location: " + cFile.getAbsolutePath(),
						MyOptionPane.INFORMATION_MESSAGE);
				// jarFiles.put("JavaFBP Jar File", cFile.getAbsolutePath());
				
				//return true;
			}
			// return false;
		}
		return true;
	}

	void saveProp(String s, String t){
		properties.put(s, t); 
		saveProperties();
	}
	
	void saveProperties() {		
		writePropertiesFile();
	}
	
	void closeTab() {
		closeTabAction.actionPerformed(new ActionEvent(jtp, 0, "CLOSE"));
		if (jtp.getTabCount() == 0) {
			getNewDiag();
			curDiag.desc = "Click anywhere on selection area";
		}
	}

	
	void displayRow(GridBagConstraints gbc, GridBagLayout gbl, JTextField[] tf,
			JPanel panel, Color col) {
		gbc.gridx = 0;
		gbc.weightx = 0.25;
		for (int i = 0; i < 4; i++) {
			if (i > 1)
				gbc.weightx = 0.5;

			gbl.setConstraints(tf[i], gbc);
			tf[i].setBackground(lg);
			tf[i].setForeground(col);
			gbc.gridx++;
			panel.add(tf[i]);
			tf[i].setEditable(false);
		}
		gbc.gridy++;
	}

	/*
	 * 
	 * public final void getSamples() { Enumeration<?> entries; ZipFile zipFile;
	 * MyOptionPane.showMessageDialog(this,
	 * "Select new project for sample networks");
	 * 
	 * // zip file must have "src", "test" and "diagrams" directories // and
	 * optionally components...
	 * 
	 * String zipname = "FBPSamples.zip"; InputStream is =
	 * this.getClass().getClassLoader() .getResourceAsStream(zipname);
	 * 
	 * String zfn = System.getProperty("user.home") + "/" + zipname;
	 * File f = new File(zfn); String s = f.getParent(); if (f.exists())
	 * f.delete(); if (s != null) (new File(s)).mkdirs(); try {
	 * copyInputStream(is, new FileOutputStream(f)); } catch (IOException e) {
	 * 
	 * }
	 * 
	 * try {
	 * 
	 * zipFile = new ZipFile(zfn);
	 * 
	 * entries = zipFile.entries();
	 * 
	 * while (entries.hasMoreElements()) { ZipEntry entry = (ZipEntry)
	 * entries.nextElement();
	 * 
	 * if (entry.isDirectory()) { // Assume directories are stored parents first
	 * then // children. System.out.println("Extracting directory: " +
	 * entry.getName()); // This is not robust, just for demonstration purposes.
	 * (new File(entry.getName())).mkdirs(); } else {
	 * System.out.println("Extracting file: " + entry.getName()); f = new
	 * File(entry.getName()); if (f.exists()) f.delete();
	 * copyInputStream(zipFile.getInputStream(entry), new FileOutputStream(f));
	 * } }
	 * 
	 * zipFile.close(); } catch (IOException ioe) {
	 * System.err.println("Unhandled exception:"); ioe.printStackTrace();
	 * return; } }
	 * 
	 */

	/*
	void checkCompatibility(Arrow a) {
		Arrow a2 = a.findLastArrowInChain();
		Block from = curDiag.blocks.get(new Integer(a.fromId));
		Block to = curDiag.blocks.get(new Integer(a2.toId));
		// String downPort = a2.downStreamPort;
		a.checkStatus = Status.UNCHECKED;
		if (!(from instanceof ProcessBlock) || !(to instanceof ProcessBlock))
			return;
		if (a.upStreamPort == null || a.upStreamPort.equals(""))
			return;
		if (a2.downStreamPort == null || a2.downStreamPort.equals(""))
			return;
		if (a.upStreamPort.equals("*") || a2.downStreamPort.equals("*")) {
			a.checkStatus = Status.COMPATIBLE;
			return;
		}
		if (from.outputPortAttrs == null || to.inputPortAttrs == null)
			return;
		AOutPort ao = from.outputPortAttrs.get(from.stem(a.upStreamPort));
		if (ao == null)
			return;
		AInPort ai = to.inputPortAttrs.get(to.stem(a2.downStreamPort));
		if (ai == null)
			return;
		if (ai.type.isAssignableFrom(ao.type) || ao.type == Object.class) // Object
			// class
			// is
			// default
			a.checkStatus = Status.COMPATIBLE;
		else
			a.checkStatus = Status.INCOMPATIBLE;
	}
	
	*/

	boolean pointInLine(Point2D p, int fx, int fy, int tx, int ty) {
		Line2D line = new Line2D((double) fx, (double) fy, (double) tx,
				(double) ty);
		double d = 0.0;
		try {
			d = line.distance(p);
		} catch (DegeneratedLine2DException e) {

		}

		return d < 4;
	}

	/**
	 * Test if point (xp, yp) is "near" line defined by (x1, y1) and (x2, y2)
	 */
	static boolean nearpln(int xp, int yp, int x1, int y1, int x2, int y2) {

		if (x1 == x2 && y1 == y2)  // fudge to get around odd bug when creating bends 
			return false;
		Line2D line = new Line2D(x1, y1, x2, y2);
		Point2D p = new Point2D(xp, yp);
		double d = 0.0;
		try {
			d = line.distance(p);
		} catch (DegeneratedLine2DException e) {

		}
		return d < 4.0;
	}

	
	void displayAlignmentLines(Block block) {

		if (!(block instanceof ProcessBlock))
			return;

		block.hNeighbour = null;
		for (Block b : curDiag.blocks.values()) {
			if (!(b instanceof ProcessBlock))
				continue;
			int y = b.cy + b.height / 2;
			if (b != block
					&& between(block.cy + block.height / 2, y - 6, y + 6)) {
				block.hNeighbour = b;
				break;
			}
		}
		block.vNeighbour = null;
		for (Block b : curDiag.blocks.values()) {
			if (!(b instanceof ProcessBlock))
				continue;
			int x = b.cx - b.width / 2;
			if (b != block
					&& between(block.cx - block.width / 2, x - 6, x + 6)) {
				block.vNeighbour = b;
				break;
			}
		}
	}

	Point2D gridAlign(Point2D p) {
		Point2D p2 = p;
		if (curDiag.clickToGrid) {
			int x = ((int) (p.x() + gridUnitSize / 2) / gridUnitSize)
					* gridUnitSize;
			int y = ((int) (p.y() + gridUnitSize / 2) / gridUnitSize)
					* gridUnitSize;
			p2 = new Point2D(x, y);
		}
		return p2;
	}

	GenLang findGLFromLabel(String s) {
		for (int i = 0; i < genLangs.length; i++)
			if (genLangs[i].label.equals(s))
				return genLangs[i];
		return null;
	}

	GenLang findGLFromLanguage(String s) {
		for (int i = 0; i < genLangs.length; i++)
			for (int j = 0; j < genLangs[i].langs.length; j++)
				if (genLangs[i].langs[j].extn.equals(s))
					return genLangs[i];
		return null;
	}

	URL[] buildUrls(File f) {
		LinkedList<URL> ll = new LinkedList<URL>();
		URL[] urls = null;
		
		if (javaFBPJarFile == null)
			locateJavaFBPJarFile(false);
		String sh = null;
		try {

			if (f != null && !(f.equals(new File(javaFBPJarFile))))
				ll.add(f.toURI().toURL());

			File f2 = new File(javaFBPJarFile);
			ll.add(f2.toURI().toURL());

			for (String jfv : jarFiles.values()) {
				f2 = new File(jfv);
				if (!(f2.equals(f)))
					ll.add(f2.toURI().toURL());
			}

			String clsDir = properties.get("currentClassDir")
					+ "/";
			int m = clsDir.indexOf("bin/"); 
			sh = clsDir.substring(0, m + 4);
			
			//if (null != sh) {				 
				f2 = new File(sh);
				if (!(f2.equals(f)))
					ll.add(f2.toURI().toURL());
			//}

			urls = ll.toArray(new URL[ll.size()]);

		} catch (MalformedURLException e) {
			MyOptionPane.showMessageDialog(this, "Malformed URL: " + f,
					MyOptionPane.ERROR_MESSAGE);
			// e.printStackTrace();
			// javaClass = null;
			urls = null;
			e.printStackTrace();
		}

		return urls;
	}
	static public void applyOrientation(Component c) {
		c.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

		if (c instanceof JMenu) {
			JMenu menu = (JMenu) c;
			int ncomponents = menu.getMenuComponentCount();
			for (int i = 0; i < ncomponents; ++i) {
				applyOrientation(menu.getMenuComponent(i));
			}
		} else if (c instanceof Container) {
			Container container = (Container) c;
			int ncomponents = container.getComponentCount();
			for (int i = 0; i < ncomponents; ++i) {
				applyOrientation(container.getComponent(i));
			}
		}
	}

	/*
	 * public static BufferedImage getScreenShot( Component component) {
	 * 
	 * BufferedImage image = new BufferedImage( component.getWidth(),
	 * component.getHeight(), BufferedImage.TYPE_INT_RGB ); // call the
	 * Component's paint method, using // the Graphics object of the image.
	 * component.paint( image.getGraphics() ); // alternately use .printAll(..)
	 * return image; }
	 */

	// gives result Side or null (touches - yes/no), if point (x, y) is within 2
	// pixels of a side;

	static Side touches(Block b, int x, int y) {
		Side side = null;
		if (nearpln(x, y, b.cx - b.width / 2, b.cy - b.height / 2,
				b.cx - b.width / 2, b.cy + b.height / 2)) {
			side = Side.LEFT;
		}
		if (nearpln(x, y, b.cx - b.width / 2, b.cy - b.height / 2,
				b.cx + b.width / 2, b.cy - b.height / 2)) {
			side = Side.TOP;
		}
		if (nearpln(x, y, b.cx + b.width / 2, b.cy - b.height / 2,
				b.cx + b.width / 2, b.cy + b.height / 2)) {
			side = Side.RIGHT;
		}
		if (nearpln(x, y, b.cx - b.width / 2, b.cy + b.height / 2,
				b.cx + b.width / 2, b.cy + b.height / 2)) {
			side = Side.BOTTOM;
		}

		return side;
	}

	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();
		if (source instanceof JSlider) {
			JSlider js = (JSlider) source;
			// oldW = getSize().width;
			// oldH = getSize().height;
			if (!(js).getValueIsAdjusting()) {
				scalingFactor = ((int) js.getValue()) / 100.0;
				String scale = (int) js.getValue() + "%";
				scaleLab.setText(scale);
				// pack();
				// setPreferredSize(new Dimension(1200, 800));
				// repaint();
			}
		}
	}

	void drawBlueCircle(Graphics g, int x, int y, int opt) {
		Color col = g.getColor();

		g.setColor(Color.BLUE);
		g.drawOval(x - 3, y - 3, 6, 6);

		g.setColor(col);
	}

	void drawBlackSquare(Graphics g, int x, int y) {
		Color col = g.getColor();
		g.setColor(Color.BLACK);
		g.drawRect(x - 2, y - 2, 4, 4);
		g.fillRect(x - 2, y - 2, 4, 4);		
		g.setColor(col);
	}

	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void componentResized(ComponentEvent e) {
		 
		Dimension dim = this.getSize();
		Dimension dim2 = new Dimension(dim.width / but.length, dim.height);
		int no = but.length;
		for (int j = 0; j < no; j++) {
			box21.remove(0);
			but[j].setMaximumSize(dim2);
			box21.add(but[j]);
		}
		//(getGraphics()).drawImage(buffer, 0, 0, null); 
		//System.out.println("Resized");
			 
	}

	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}
	
	//public void repaint() {
	//	super.repaint();
	//	System.out.println("repaint");
	//}

	public static void main(final String[] args) {

		
			SwingUtilities.invokeLater(new Runnable() {
			        public void run() {
			            
			//        }
			//    });
			String laf = UIManager.getSystemLookAndFeelClassName();
			

			System.setProperty("apple.laf.useScreenMenuBar", "true");

			try {
				UIManager.setLookAndFeel(laf);
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			} catch (InstantiationException e1) {
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			} catch (UnsupportedLookAndFeelException e1) {
				e1.printStackTrace();
			}

			setDefaultLookAndFeelDecorated(true);

			DrawFBP _mf= new DrawFBP(args);
			_mf.setVisible(true);
			
			        }
					   });
			
	}

	public class Lang {
		String language; // this is the language...
		String extn; // extension - excluding period
		Lang(String lan, String ex) {
			language = lan;
			extn = ex;
		}
	}

	public class GenLang {
		// this class really refers more to a VM than a language... (ex. that
		// fbp notation is treated as a language)
		String label;
		// String genCodeFileName;
		String suggExtn; // excluding period - suggested extension when
							// generating code...
		String srcDirProp; // DrawFBP property specifying source directory
		String netDirProp; // DrawFBP property specifying source directory for
							// net definition
		FileFilter filter;

		Lang[] langs; // each entry has a language name, and an extension -
						// excluding periods
		GenLang(String lan, String se, FileFilter f) {
			label = lan;
			suggExtn = se;
			srcDirProp = "current" + label + "SourceDir";
			netDirProp = "current" + label + "NetworkDir";
			if (label.equals("C#")) {
				srcDirProp = "currentCsharpSourceDir";
				netDirProp = "currentCsharpNetworkDir"; // xml does not seem to
														// like #'s
			}
			filter = f;

		}

		String showLangs() {
			String s = "";
			if (langs.length == 1)
				s = langs[0].language;
			else {

				s = "(";
				for (int i = 0; i < langs.length; i++) {
					if (i > 0)
						s += ", ";
					s += langs[i].language;
				}
				s += ")";
			}
			return s;
		}

		String showSuffixes() {
			String s = "(";
			for (int i = 0; i < langs.length; i++) {
				if (i > 0)
					s += ", ";
				s += "*." + langs[i].extn;
			}
			return s + ")";
		}
	}

	

	public class CloseAppAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {

			boolean close = true;

			for (int i = 0; i < jtp.getTabCount(); i++) {
				ButtonTabComponent b = (ButtonTabComponent) jtp
						.getTabComponentAt(i);
				jtp.setSelectedIndex(i);
				if (b == null || b.diag == null)
					return;
				Diagram diag = b.diag;

				if (diag != null) {
					if (diag.askAboutSaving() == MyOptionPane.CANCEL_OPTION) {
						close = false;
						//break;
					}
				}
			}
			// if (propertiesChanged) {
			//writePropertiesFile();
			saveProperties();
			// }

			if (close) {
				dispose();
				System.exit(0);
			}
		}
	}
	public class CloseTabAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {

			int j = jtp.getTabCount();
			if (j < 1)
				return;

			int i = jtp.getSelectedIndex();
			if (i == -1) // don't know which to delete...
				return;
			ButtonTabComponent b = (ButtonTabComponent) jtp
					.getTabComponentAt(i);
			if (b == null || b.diag == null)
				return;
			Diagram diag = b.diag;

			if (diag != null) {
				if (diag.askAboutSaving() == MyOptionPane.CANCEL_OPTION)
					return;
			}

			if (i < jtp.getTabCount())
				jtp.remove(i);
			
			//curDiag = null;  
			if (jtp.getTabCount() > 0) {
			b = (ButtonTabComponent) jtp
					.getTabComponentAt(0);
			if (b == null || b.diag == null)
				return;
			diag = b.diag;
			curDiag = diag;
			saveProp("currentDiagram", diag.diagFile.getAbsolutePath());
			//properties.remove("currentDiagram");
			}

			repaint();
		}
	}

	public class EscapeAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (currentArrow != null) {
				Integer aid = new Integer(currentArrow.id);
				curDiag.arrows.remove(aid);
				currentArrow = null; // terminate arrow drawing
				repaint();
				return;
			}

			if (e.getSource() == jHelpViewer) {
				popup2.dispose();
				repaint();
				return;
			}

			closeAppAction.actionPerformed(new ActionEvent(jtp, 0, "CLOSE"));
				}
			}
				

	public class JavaFileFilter extends FileFilter {
		@Override
		public boolean accept(File f) {

			String s = f.getName().toLowerCase();
			return s.toLowerCase().endsWith(".java")
					|| s.toLowerCase().endsWith(".scala")
					|| s.toLowerCase().endsWith(".groovy") || f.isDirectory();

		}

		@Override
		public String getDescription() {
			return "Java source files (*.java, *.scala, *.groovy)";
		}

	}
	public class JavaGenFilter extends FileFilter {
		@Override
		public boolean accept(File f) {

			String s = f.getName().toLowerCase();
			return s.toLowerCase().endsWith(".java") || f.isDirectory();

		}

		@Override
		public String getDescription() {
			return "Java source files (*.java)";
		}

	}

	public class CsharpFileFilter extends FileFilter {
		@Override
		public boolean accept(File f) {

			return f.getName().toLowerCase().endsWith(".cs") || f.isDirectory();

		}

		@Override
		public String getDescription() {
			return "C# source files (*.cs)";
		}
	}

	public class JSONFilter extends FileFilter {
		@Override
		public boolean accept(File f) {

			return f.getName().toLowerCase().endsWith(".json")
					|| f.isDirectory();

		}

		@Override
		public String getDescription() {
			return "JSON source files (*.json)";
		}
	}
	/*
	 * public class NoFloGenFilter extends FileFilter {
	 * 
	 * @Override public boolean accept(File f) {
	 * 
	 * return f.getName().toLowerCase().endsWith(".json") || f.isDirectory();
	 * 
	 * }
	 * 
	 * @Override public String getDescription() { return
	 * "NoFlo source files (*.json)"; } }
	 */
	public class FBPFilter extends FileFilter {
		@Override
		public boolean accept(File f) {

			return f.getName().toLowerCase().endsWith(".fbp")
					|| f.getName().toLowerCase().endsWith("package.json")
					|| f.isDirectory();

		}

		@Override
		public String getDescription() {
			return "FBP diagram files (*.fbp)";
		}

	}

	public class JavaClassFilter extends FileFilter {
		@Override
		public boolean accept(File f) {

			return f.getName().toLowerCase().endsWith(".class")
					|| f.isDirectory();

		}

		@Override
		public String getDescription() {
			return "Java class files (*." + "class" + ")";
		}

	}

	public class DiagramFilter extends FileFilter {
		@Override
		public boolean accept(File f) {

			return f.getName().toLowerCase().endsWith(".drw")
					|| f.getName().toLowerCase().endsWith(".dr~")
					|| f.getName().toLowerCase().endsWith(".fbp")
					|| f.getName().toLowerCase().endsWith("package.json")
					// package.json added to remind us that this is like a
					// directory
					|| f.getName().toLowerCase().endsWith(".json")
					|| f.isDirectory();

		}

		@Override
		public String getDescription() {
			return "Diagrams (*.drw)";
		}

	}

	public class JarFileFilter extends FileFilter {
		@Override
		public boolean accept(File f) {

			return f.getName().toLowerCase().endsWith(".jar")
					|| f.isDirectory();

		}

		@Override
		public String getDescription() {
			return "Jar files (*." + "jar" + ")";
		}

	}
	// Filter for images
	public class ImageFilter extends FileFilter {
		@Override
		public boolean accept(File f) {

			return f.getName().toLowerCase().endsWith(".png")
					|| f.getName().toLowerCase().endsWith(".jpg")
					|| f.getName().toLowerCase().endsWith(".bmp")
					|| f.isDirectory();
		}

		@Override
		public String getDescription() {
			return "Images (*.png, *.jpg, *.bmp)";
		}

	}
	
	// Filter for .dll files
			public class DllFilter extends FileFilter {
				@Override
				public boolean accept(File f) {

					return f.getName().toLowerCase().endsWith(".dll")
							|| f.isDirectory();
				}

				@Override
				public String getDescription() {
					return ".dll files (*.dll)";
				}

			}

	// Filter for .exe files
		public class ExeFilter extends FileFilter {
			@Override
			public boolean accept(File f) {

				return f.getName().toLowerCase().endsWith(".exe")
						|| f.isDirectory();
			}

			@Override
			public String getDescription() {
				return ".exe files (*.exe)";
			}

		}

	public class FileChooserParm {
		// int index;
		String name;
		String propertyName;
		String prompt;
		String fileExt;
		FileFilter filter;
		String title;

		FileChooserParm(/* int n, */ String x, String a, String b, String c,
				FileFilter d, String e) {
			// index = n;
			name = x;
			propertyName = a;
			prompt = b;
			fileExt = c;
			filter = d;
			title = e;
		}
	}

	public class RunTask extends Thread {
		public void run() {
			
				ProcessBuilder pb = new ProcessBuilder(pBCmdArray);

				pb.directory(new File(pBDir));

				output = "";
				pb.redirectErrorStream(true);
				
				error = ""; 
				
				Process proc = null;
				
				try {
					proc = pb.start();
					
					BufferedReader br = new BufferedReader(
							new InputStreamReader(proc.getInputStream()));
					String line;
					while ((line = br.readLine()) != null) {
						output += line + "<br>";
					}
				} catch (NullPointerException npe) {
					error = "Null Pointer Exception"; 
					proc = null;
					//return;
				} catch (IOException ioe) {
					error = "I/O Exception"; 
					proc = null;
					//return;
				} catch (IndexOutOfBoundsException iobe) {
					error = "Index Out Of Bounds Exception"; 
					proc = null;
					//return;
				} catch (SecurityException se) {
					error = "Security Exception"; 
					proc = null;
					//return;			 
				} 
				
				
				if (proc == null) 
					return;
				
				try {
					proc.waitFor();
				} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				int u = proc.exitValue(); 
				proc.destroy();
				
				//if (proc == null)
				//	return;
				
				//int u = proc.exitValue(); 
				if (u == 0)
					MyOptionPane.showMessageDialog(driver,
							"Program completed - " + progName,
							MyOptionPane.INFORMATION_MESSAGE);
				else
					MyOptionPane.showMessageDialog(driver,
							"Program test failed, rc: " + u + " - " + progName,
							MyOptionPane.WARNING_MESSAGE);

				if (!(error.equals("")))  
					MyOptionPane.showMessageDialog(driver,
							"<html>Program error - " + pBDir + progName + "<br>" +
							error + "</html>",
							MyOptionPane.ERROR_MESSAGE);
					 
				if (!(output.equals("")))
				MyOptionPane.showMessageDialog(driver,
						"<html>Program output - " + pBDir + progName + "<br>" +
						output +  "</html>",
						MyOptionPane.INFORMATION_MESSAGE);
		}
	}


	public class SelectionArea extends JPanel implements MouseInputListener {
		static final long serialVersionUID = 111L;
		int oldx, oldy, mousePressedX, mousePressedY;

		public SelectionArea() {

			setOpaque(true);

			addMouseListener(this);
			addMouseMotionListener(this);
			// setFont(fontg);

			setBackground(Color.WHITE);
			// setPreferredSize(new Dimension(4000, 3000)); // experimental
			// pack();

		}

		// a is "from" arrow; a2 may be same, or arrow that a joins to...
		void defaultPortNames(Arrow a) {
			Block from = curDiag.blocks.get(new Integer(a.fromId));
			Block to = curDiag.blocks.get(new Integer(a.toId));
			Arrow a2 = a.findLastArrowInChain();
			to = curDiag.blocks.get(new Integer(a2.toId));
			if (from != null
					&& (from instanceof ProcessBlock
							|| from instanceof ExtPortBlock)
					&& (a2.endsAtBlock && to != null
							&& (to instanceof ProcessBlock
									|| to instanceof ExtPortBlock))) {
				if (a.upStreamPort == null || a.upStreamPort.trim().equals(""))
					a.upStreamPort = "OUT";

				if (a2.downStreamPort == null
						|| a2.downStreamPort.trim().equals(""))
					a2.downStreamPort = "IN";
			}

			if (from instanceof IIPBlock && a2.endsAtBlock && to != null
					&& to instanceof ProcessBlock) {
				if (a2.downStreamPort == null
						|| a2.downStreamPort.trim().equals(""))
					a2.downStreamPort = "IN";
			}

		}
 
		public void paintComponent(Graphics g) {

			// Paint background if we're opaque.
			// super.paintComponent(g);

			if (this.isOpaque()) {
				// g.setColor(getBackground());
				osg.setColor(Color.WHITE);

				int w = getWidth();
				int h = getHeight();
				osg.fillRect(0, 0, (int) (w / scalingFactor),
						(int) (h / scalingFactor - 0));
			}

			int i = jtp.getSelectedIndex();

			ButtonTabComponent b = (ButtonTabComponent) jtp
					.getTabComponentAt(i);
			if (b == null || b.diag == null)
				return;
			
			Diagram diag = b.diag;

			// if (curDiag != diag) {
			// int x = 0; // problem!
			// }

			grid.setSelected(diag.clickToGrid);

			// repaint();
			// Graphics2D g2d = (Graphics2D) g;

			for (Block block : diag.blocks.values()) {
				if (!(block instanceof Enclosure))
					block.draw(osg);
			}

			for (Block block : diag.blocks.values()) {
				if (block instanceof Enclosure)
					block.draw(osg);
			}

			//if (diag.diagFile != null)
			//	System.out.println(diag.diagFile.getAbsolutePath() + " " + diag.arrows.size());
			for (Arrow arrow : diag.arrows.values()) {
			//	if (diag.diagFile != null)
			//		System.out.println(diag.diagFile.getAbsolutePath() + " " + arrow);
				arrow.draw(osg);
			}

			String s = diag.desc;
			if (s != null) {
				if (s.trim().equals(""))
					s = "(no description)";
				s = s.replace('\n', ' ');
			}
			// else
			// s = "(no description)";

			diagDesc.setText(s);    

			Graphics2D g2d = (Graphics2D) g;

			//g2d.scale(scalingFactor, scalingFactor);
			//osg.scale(scalingFactor, scalingFactor);

			// g2d.translate(xTranslate, yTranslate);

			// Now copy that off-screen image onto the screen
			//g2d.drawImage(buffer, 0, 0, null);   
			g2d.scale(scalingFactor, scalingFactor);
			g.drawImage(buffer, 0, 0, null);   
			
		}
 
		FoundPoint findBlockEdge(int xa, int ya) {

			FoundPoint fp = null;
			for (Block block : curDiag.blocks.values()) {

				if (!(between(xa, block.leftEdge - 4 * scalingFactor,
						block.rgtEdge + 4 * scalingFactor)))
					continue;

				if (!(between(ya, block.topEdge - 4 * scalingFactor,
						block.botEdge + 4 * scalingFactor)))
					continue;

				/* look for block edge touching xa and ya */

				Side side = touches(block, xa, ya);
				if (side == null)
					continue;

				fp = new FoundPoint(xa, ya, side, block);

				if (side == Side.LEFT)
					fp.x = block.leftEdge;
				else if (side == Side.RIGHT)
					fp.x = block.rgtEdge;
				else if (side == Side.TOP)
					fp.y = block.topEdge;
				else if (side == Side.BOTTOM)
					fp.y = block.botEdge;

				// fp = new FoundPoint(xa, ya, side, block);

				break;
			}

			return fp;
		}

		// see if x and y are "close" to any arrow - if so, return it, else null
		FoundPoint findArrow(int x, int y) {

			FoundPoint fp = null;

			for (Arrow arrow : curDiag.arrows.values()) {
				if (arrow.toId == -1)
					continue;

				int x1 = arrow.fromX;
				int y1 = arrow.fromY;
				int segNo = 0;
				int x2, y2;

				if (arrow.bends != null) {
					for (Bend bend : arrow.bends) {
						x2 = bend.x;
						y2 = bend.y;
						if (nearpln(x, y, x1, y1, x2, y2)) {
							fp = new FoundPoint(x, y, arrow, segNo);
							return fp;
						}

						x1 = x2;
						y1 = y2;
						segNo++;
					}
				}

				x2 = arrow.toX;
				y2 = arrow.toY;
				if (nearpln(x, y, x1, y1, x2, y2)) {
					fp = new FoundPoint(x, y, arrow, segNo);
					return fp;
				}
			}
			return null;
		}
		void adjustArrowsEndingAtLine(Arrow arrow) {
			for (Arrow arr : curDiag.arrows.values()) {
				if (!arr.endsAtLine)
					continue;
				if (arr.toId != arrow.id)
					continue;
				int x1 = arrow.fromX;
				int y1 = arrow.fromY;
				int x2 = arrow.toX;
				int y2 = arrow.toY;
				int i = arr.segNo;
				Bend b = null;
				if (arrow.bends != null) {
					if (i > 0) {
						b = arrow.bends.get(i - 1);
						x1 = b.x;
						y1 = b.y;
					}
					if (i < arrow.bends.size()) {
						b = arrow.bends.get(i);
						x2 = b.x;
						y2 = b.y;
					}
				}
				Point2D p1 = new Point2D((double) x1, (double) y1);
				Point2D p2 = new Point2D((double) x2, (double) y2);
				Line2D line = new Line2D(p1, p2); // correct segment of arow

				int xp = arr.fromX;
				int yp = arr.fromY;

				if (arr.bends != null) {
					i = arr.bends.size();
					b = arr.bends.get(i - 1);
					xp = b.x;
					yp = b.y;
				}

				Point2D point = new Point2D((double) xp, (double) yp);

				// StraightLine2D perp = line.perpendicular(point);
				StraightLine2D open = new StraightLine2D(xp, yp, arr.toX - xp,
						arr.toY - yp);
				point = line.intersection(open);
				if (point != null) {
					arr.toX = (int) point.x();
					arr.toY = (int) point.y();
				}
				arr.extraArrowhead = null;
				adjustArrowsEndingAtLine(arr); // call recursively
			}
		}

		public void mouseMoved(MouseEvent e) {
			int i = jtp.getSelectedIndex();
			if (i == -1)
				return;
			//drawToolTip = false;
			arrowRoot = null;
			arrowEnd = null;
			/*
			if (!ttEndTimer.isRunning())
				ttStartTimer.restart();
			else
				ttEndTimer.stop();
			*/
			repaint();
			ButtonTabComponent b = (ButtonTabComponent) jtp
					.getTabComponentAt(i);
			if (b == null || b.diag == null)
				return;
			
			curDiag = b.diag;

			int x = (int) Math.round(e.getX() / scalingFactor);
			int y = (int) Math.round(e.getY() / scalingFactor);
			int xa, ya;

			if (panSwitch) {
				Rectangle r = curDiag.area.getBounds();
				r = new Rectangle(r.x, r.y, r.width - 20, r.height - 40);
				if (r.contains(x, y))
					setCursor(openPawCursor);
				else
					setCursor(defaultCursor);
			}

			Point2D p = new Point2D(x, y);
			p = gridAlign(p);
			xa = (int) p.x();
			ya = (int) p.y();

			if (enclSelForArrow != null) {
				enclSelForArrow.corner = null;
				enclSelForArrow = null;
				repaint();
				return;
			}

			selBlockM = null;
			// look for corner of an enclosure - if corner not null, you will
			// see diagonal arrows at corners
			for (Block block : curDiag.blocks.values()) {
				// block.calcEdges();
				if (block instanceof Enclosure) {
					Enclosure enc = (Enclosure) block;

					if (between(xa, block.leftEdge - 6, block.leftEdge + 6)
							&& between(ya, block.topEdge - 6,
									block.topEdge + 6)) {
						enclSelForArrow = enc;
						enc.corner = Corner.TOPLEFT;
						break;
					}
					if (between(xa, block.leftEdge - 6, block.leftEdge + 6)
							&& between(ya, block.botEdge - 6,
									block.botEdge + 6)) {
						enclSelForArrow = enc;
						enc.corner = Corner.BOTTOMLEFT;
						break;
					}
					if (between(xa, block.rgtEdge - 6, block.rgtEdge + 6)
							&& between(ya, block.topEdge - 6,
									block.topEdge + 6)) {
						enclSelForArrow = enc;
						enc.corner = Corner.TOPRIGHT;
						break;
					}
					if (between(xa, block.rgtEdge - 6, block.rgtEdge + 6)
							&& between(ya, block.botEdge - 6,
									block.botEdge + 6)) {
						enclSelForArrow = enc;
						enc.corner = Corner.BOTTOMRIGHT;
						break;
					}
				}

				// logic to change cursor to drag_icon
				int hh = gFontHeight;
				boolean udi;
				if (block.type.equals(Block.Types.ENCL_BLOCK)) {
					udi = between(xa, block.leftEdge + block.width / 5,
							block.rgtEdge - block.width / 5)
							&& between(ya, block.topEdge - hh,
									block.topEdge + hh / 2);
				} else {
					udi = between(xa, block.leftEdge + block.width / 8,
							block.rgtEdge - block.width / 8)
							&& between(ya, block.topEdge + block.height / 8,
									block.botEdge - block.height / 8);
				}

				if (udi) {
					selBlockM = block; // mousing select
					if (!use_drag_icon) {
						if (curDiag.jpm == null && !panSwitch)
							setCursor(drag_icon);
						use_drag_icon = true;
					}

					break;
				}

			}
			if (selBlockM == null) {
				if (use_drag_icon)
					use_drag_icon = false;

				if (!panSwitch)
					setCursor(defaultCursor);
			}
			// curDiag.foundBlock = null;

			FoundPoint fp = findBlockEdge(xa, ya);
			if (fp != null) {
				if (currentArrow == null)
					arrowRoot = fp;
				else
					arrowEnd = fp;
			} else {
				arrowEnd = findArrow(xa, ya);
			}

			repaint();
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {

		}

		/*
		 * The following mouse actions are supported:
		 * 
		 * - click on block - highlights block - double-click on block - brings
		 * up popup menu if not subnet - brings up subnet if subnet - press on
		 * side of block starts arrow - release on side of block starts or ends
		 * arrow - click on arrow - brings up popup menu - press on block -
		 * starts drag
		 * 
		 */

		public void mousePressed(MouseEvent e) {
			// Block foundBlock = null;
			int i = jtp.getSelectedIndex();
			if (i == -1)
				return;
			ButtonTabComponent b = (ButtonTabComponent) jtp
					.getTabComponentAt(i);
			if (b == null || b.diag == null)
				return;
			curDiag = b.diag;

			Side side = null;
			leftButton = (e.getModifiers()
					& InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK;
			int x = e.getX();
			x = (int) Math.round(x / scalingFactor);
			int y = e.getY();
			y = (int) Math.round(y / scalingFactor);
			int xa, ya;

			xa = x;
			ya = y;
			curx = xa;
			cury = ya;

			if (curDiag.jpm != null) {
				curDiag.jpm.setVisible(false);
				curDiag.jpm = null;
				repaint();
				return;
			}

			if (panSwitch) {
				// Rectangle r = curDiag.area.getBounds();
				Dimension d = curDiag.area.getSize();
				// if (r.contains(x, y)) {
				if (x >= curDiag.area.getX()
						&& x <= curDiag.area.getX() + d.width
						&& y >= curDiag.area.getY()
						&& y <= curDiag.area.getY() + d.height) {
					setCursor(closedPawCursor);
					panX = xa;
					panY = ya;
					return;
				} else
					setCursor(defaultCursor);
			}

			// if (e.getClickCount() == 2)
			// return;

			foundBlock = null;
			selBlock = null;
			selArrow = null;
			blockSelForDragging = null;
			// enclSelForDragging = null;
			// arrowEndForDragging = null;
			// bendForDragging = null;

			repaint();

			// look for side or corner of an enclosure
			for (Block block : curDiag.blocks.values()) {
				block.calcEdges();
				if (block instanceof Enclosure) {
					Enclosure enc = (Enclosure) block;
					/* test for a hit within the rectangle at top */
					int hh = gFontHeight;
					if (between(xa, block.leftEdge + block.width / 5,
							block.rgtEdge - block.width / 5)
							&& between(ya, block.topEdge - hh,
									block.topEdge + hh / 2)) {
						mousePressedX = oldx = xa;
						mousePressedY = oldy = ya;
						blockSelForDragging = block;
						break;
					}

					/* now handle stretching at the corners */
					if (between(xa, block.leftEdge - 6, block.leftEdge + 6)
							&& between(ya, block.topEdge - 6,
									block.topEdge + 6)) {
						blockSelForDragging = enc;
						enc.corner = Corner.TOPLEFT;
						break;
					}
					if (between(xa, block.leftEdge - 6, block.leftEdge + 6)
							&& between(ya, block.botEdge - 6,
									block.botEdge + 6)) {
						blockSelForDragging = enc;
						enc.corner = Corner.BOTTOMLEFT;
						break;
					}
					if (between(xa, block.rgtEdge - 6, block.rgtEdge + 6)
							&& between(ya, block.topEdge - 6,
									block.topEdge + 6)) {
						blockSelForDragging = enc;
						enc.corner = Corner.TOPRIGHT;
						break;
					}
					if (between(xa, block.rgtEdge - 6, block.rgtEdge + 6)
							&& between(ya, block.botEdge - 6,
									block.botEdge + 6)) {
						blockSelForDragging = enc;
						enc.corner = Corner.BOTTOMRIGHT;
						break;
					}

				} else { // not enclosure
					/*
					 * the following leaves a strip around the outside of each
					 * block that cannot be used for dragging!
					 */
					if (between((double) xa, block.leftEdge + block.width / 8,
							block.rgtEdge - block.width / 8)
							&& between((double) ya,
									block.topEdge + block.height / 8,
									block.botEdge - block.height / 8)) {
						mousePressedX = oldx = xa;
						mousePressedY = oldy = ya;
						blockSelForDragging = block;
						break;
					}
				}

				/* check for possible starts of arrows */

				side = touches(block, xa, ya);
				if (side != null) {
					foundBlock = block;
					if (side == Side.LEFT)
						xa = block.leftEdge;
					else if (side == Side.RIGHT)
						xa = block.rgtEdge;
					else if (side == Side.TOP)
						ya = block.topEdge;
					else if (side == Side.BOTTOM)
						ya = block.botEdge;
					arrowRoot = new FoundPoint(xa, ya, side, block);
					break;
				}

			}

			if (blockSelForDragging != null
					&& blockSelForDragging instanceof Enclosure) {
				ox = blockSelForDragging.cx;
				oy = blockSelForDragging.cy;
				ow = blockSelForDragging.width;
				oh = blockSelForDragging.height;
				repaint();
				return;
			}

			
			// if no currentArrow, but there is a found block, start an arrow
			if (currentArrow == null && foundBlock != null
					&& arrowEndForDragging == null) {

				Arrow arrow = new Arrow(curDiag);
				curDiag.maxArrowNo++;
				arrow.id = curDiag.maxArrowNo;
				selArrow = arrow;
				// selBlockP = null;
				arrow.fromX = xa;
				arrow.fromY = ya;

				arrow.fromId = foundBlock.id;
				Block fromBlock = curDiag.blocks.get(new Integer(arrow.fromId));
				if (fromBlock.type.equals(Block.Types.EXTPORT_IN_BLOCK)
						|| fromBlock.type
								.equals(Block.Types.EXTPORT_OUTIN_BLOCK))
					arrow.upStreamPort = "OUT";
				// arrow.fromId = -1;
				currentArrow = arrow;
				arrow.lastX = xa; // save last x and y
				arrow.lastY = ya;
				Integer aid = new Integer(arrow.id);
				curDiag.arrows.put(aid, arrow);

				// foundBlock = null;

			}
			repaint();
		}

		public void mouseDragged(MouseEvent e) {

			int i = jtp.getSelectedIndex();
			if (i == -1)
				return;
			// arrowRoot = null;
			//if (!ttEndTimer.isRunning()) {
			//	drawToolTip = false;
			//	ttStartTimer.restart();
			//}

			repaint();
			ButtonTabComponent b = (ButtonTabComponent) jtp
					.getTabComponentAt(i);
			if (b == null || b.diag == null)
				return;
			curDiag = b.diag;

			int x = (int) Math.round(e.getX() / scalingFactor);
			int y = (int) Math.round(e.getY() / scalingFactor);
			int xa, ya;

			Point2D p = new Point2D(x, y);
			p = gridAlign(p);
			xa = (int) p.x();
			ya = (int) p.y();

			if (e.getClickCount() == 2) {

				// blockSelForDragging = null;
				// enclSelForDragging = null;
				arrowEndForDragging = null;
				bendForDragging = null;
			}

			if (panSwitch) {
				for (Block block : curDiag.blocks.values()) {
					block.cx = block.cx + xa - panX;
					block.cy = block.cy + ya - panY;
					block.calcEdges();
				}
				for (Arrow arrow : curDiag.arrows.values()) {
					arrow.fromX = arrow.fromX + xa - panX;
					arrow.fromY = arrow.fromY + ya - panY;
					arrow.toX = arrow.toX + xa - panX;
					arrow.toY = arrow.toY + ya - panY;
					if (arrow.bends != null) {
						for (Bend bend : arrow.bends) {
							bend.x = bend.x + xa - panX;
							bend.y = bend.y + ya - panY;
						}
					}
				}
				// repaint();
				curDiag.changed = true;
				panX = xa;
				panY = ya;
				repaint();
				return;
			}

			// if (!leftButton) // use left button for dragging
			// return;

			if (arrowEndForDragging != null) {
				Arrow arr = arrowEndForDragging;
				if (arr.tailMarked) {
					arr.fromId = -1;
					arr.fromX = xa;
					arr.fromY = ya;
				} else {
					arr.toId = -1;
					arr.toX = xa;
					arr.toY = ya;
				}
				curDiag.changed = true;
				repaint();
				return;
			}

			if (bendForDragging != null) {
				bendForDragging.x = xa;
				bendForDragging.y = ya;
				curDiag.changed = true;
				repaint();
				return;
			}

			// logic to drag one corner of enclosure

			if (blockSelForDragging != null
					&& blockSelForDragging instanceof Enclosure) {
				Enclosure enc = (Enclosure) blockSelForDragging;
				
				if (enc.corner == Corner.TOPLEFT) {
					enc.width = ox + ow / 2 - xa;  // ox is value of cx when dragging started
					enc.height = oy + oh / 2 - ya;  // oy is value of cy when dragging started
					enc.cx = xa + enc.width / 2;   // ow is value of width when dragging started
					enc.cy = ya + enc.height / 2;  // oh is value of height when dragging started
					enc.calcEdges();
					curDiag.changed = true;
					repaint();
					return;
				}
				if (enc.corner == Corner.BOTTOMLEFT) {
					enc.width = ox + ow / 2 - xa;
					enc.height = ya - (oy - oh / 2);
					enc.cx = xa + enc.width / 2;
					enc.cy = ya - enc.height / 2;
					enc.calcEdges();
					curDiag.changed = true;
					repaint();
					return;
				}
				if (enc.corner == Corner.TOPRIGHT) {
					enc.width = xa - (ox - ow / 2);
					enc.height = oy + oh / 2 - ya;
					enc.cx = xa - enc.width / 2;
					enc.cy = ya + enc.height / 2;
					enc.calcEdges();
					curDiag.changed = true;
					repaint();
					return;
				}
				if (enc.corner == Corner.BOTTOMRIGHT) {
					enc.width = xa - (ox - ow / 2);
					enc.height = ya - (oy - oh / 2);
					enc.cx = xa - enc.width / 2;
					enc.cy = ya - enc.height / 2;
					enc.calcEdges();
					curDiag.changed = true;
					repaint();
					return;
				}

				// curDiag.changed = true;
				// repaint();
				// return;
			}

			if (blockSelForDragging != null) { // set in mousePressed

				if (curDiag.clickToGrid && Math.abs(xa - oldx) < 6
						&& Math.abs(ya - oldy) < 6 || // do not respond
						Math.abs(xa - oldx) > 200 || // to small twitches
						Math.abs(ya - oldy) > 200) // or big twitches!
					return;
				Block block = blockSelForDragging;
				displayAlignmentLines(block);

				for (Arrow arrow : curDiag.arrows.values()) {

					if (arrow.fromId == block.id) {
						arrow.fromX += xa - oldx;
						arrow.fromY += ya - oldy;
						arrow.extraArrowhead = null;
						adjustArrowsEndingAtLine(arrow); // must be recursive
					}
					if (arrow.toId == block.id && arrow.endsAtBlock) {
						arrow.toX += xa - oldx;
						arrow.toY += ya - oldy;
						arrow.extraArrowhead = null;
						adjustArrowsEndingAtLine(arrow); // must be recursive
					}
				}

				block.cx += xa - oldx;
				block.cy += ya - oldy;

				block.calcEdges();

				if (arrowRoot != null && arrowRoot.block == block) {
					arrowRoot.x += xa - oldx;
					arrowRoot.y += ya - oldy;
				}

				if (block instanceof Enclosure) {
					Enclosure enc = (Enclosure) block;

					if (enc.llb != null) {
						for (Block bk : enc.llb) {
							bk.cx += xa - oldx;
							bk.cy += ya - oldy;
							bk.calcEdges();
						}
						repaint();
					}
					if (enc.lla != null) {
						for (Arrow a : enc.lla) {
							a.fromX += xa - oldx;
							a.fromY += ya - oldy;
							a.toX += xa - oldx;
							a.toY += ya - oldy;
							if (a.bends != null)
								for (Bend bd : a.bends) {
									bd.x += xa - oldx;
									bd.y += ya - oldy;
								}
						}
						repaint();
					}
				}

				oldx = xa;
				oldy = ya;

				curDiag.changed = true;
				repaint();

				// block.calcEdges();
			}

			if (currentArrow != null) { // this ensures the line
										// stays visible

				currentArrow.toX = xa;
				currentArrow.toY = ya;
				curDiag.changed = true;
				arrowEnd = null;
				
				FoundPoint fp = findBlockEdge(xa, ya);

				if (fp != null) {
					foundBlock = fp.block;
					// side = fp.side;
					arrowEnd = fp;
				} else {
					arrowEnd = findArrow(xa, ya);
				}

			}
			repaint();
		}

		public void mouseReleased(MouseEvent e) {

			// Arrow foundArrow = null;
			Block foundBlock = null;

			int i = jtp.getSelectedIndex();
			if (i == -1)
				return;
			ButtonTabComponent b = (ButtonTabComponent) jtp
					.getTabComponentAt(i);
			if (b == null || b.diag == null)
				return;
			curDiag = b.diag;

			if (curDiag.jpm != null) {
				curDiag.jpm.setVisible(false);
				curDiag.jpm = null;
				repaint();
				return;
			}

			
			int x = (int) e.getX();
			int y = (int) e.getY();
			x = (int) Math.round(x / scalingFactor);
			y = (int) Math.round(y / scalingFactor);
			int xa, ya;

			Side side = null;
			Point2D p2 = new Point2D(x, y);
			p2 = gridAlign(p2);
			xa = (int) p2.x();
			ya = (int) p2.y();

			if (curDiag.area.contains(x, y) && panSwitch) {
				setCursor(openPawCursor);
				repaint();
				return;
			}

			// if (currentArrow == null) {
			// xa = curx; // used for bend dragging
			// ya = cury;
			// }

			if (e.getClickCount() == 2 || !leftButton) {
				// enclSelForDragging = null;
				// arrowEndForDragging = null;
				// bendForDragging = null;

				if (blockSelForDragging != null) {
					selBlock = blockSelForDragging;
					// this tests if mouse has moved (approximately) - ignore
					// small twitches and also big jumps!
					if (between(mousePressedX, x - 6 * scalingFactor,
							x + 6 * scalingFactor)
							&& between(mousePressedY, y - 6 * scalingFactor,
									y + 6 * scalingFactor)
							|| Math.abs(mousePressedX - x) > 100
							|| Math.abs(mousePressedY - y) > 100) {

						// if it was a small move, or a big jump, just get
						// subnet, or display options

						if (leftButton && blockSelForDragging.isSubnet) {
							if (blockSelForDragging.subnetFileName == null) {
								MyOptionPane.showMessageDialog(null,
										"No subnet diagram assigned",
										MyOptionPane.INFORMATION_MESSAGE);
							} else {
								
								String name = blockSelForDragging.subnetFileName;
								//String dir = properties.get("currentDiagramDir");
								//MyOptionPane.showMessageDialog(null,
								//		"Subnet OK - subnet diagram assigned",
								//		MyOptionPane.INFORMATION_MESSAGE);
								
								int k = getFileTabNo(name);
								if (k != -1) {
									jtp.setSelectedIndex(k);
									return;
								}
								
								
								//Diagram	sbnDiag = getNewDiag();   
								File df = openAction(name);  
								if (df == null)
									return;

								
								
								curDiag.diagFile = df;
								curDiag.desc = df.getName();
								curDiag.title = df.getName();
								/*
								String subnet = null;
								if (null == (subnet = readFile(df, !SAVEAS))) {    
									MyOptionPane.showMessageDialog(this, "Unable to read file: "
											+ df.getName(), MyOptionPane.ERROR_MESSAGE);
									return;
								}
								DiagramBuilder.buildDiag(subnet, this, sbnDiag);
								//jtp.setSelectedIndex(sbnDiag.tabNum);
								*/
								//curDiag = sbnDiag;
								curDiag.changed = false;
								return;  
							}
						} else {
							blockSelForDragging.buildBlockPopupMenu();
							// if (this == null)
							// curDiag = new Diagram(driver);

							curDiag = blockSelForDragging.diag;

							curDiag.jpm.show(this, xa + 100, ya + 100);

						}
						// blockSelForDragging = null;
					} // else {
					curDiag.changed = true;
					// }
					repaint();
					// return;
				}

			}

			if (arrowEndForDragging != null) {
				currentArrow = null;
				foundBlock = null;
				// curDiag.changed = true;
				Arrow arr = arrowEndForDragging;

				for (Block block : curDiag.blocks.values()) {
					if (arr.tailMarked) {
						arr.fromId = -1;
						if (null != touches(block, arr.fromX, arr.fromY)) {
							arr.fromId = block.id;
							break;
						}
					}
					FoundPoint fp;
					if (arr.headMarked) {
						arr.toId = -1;
						if (null != touches(block, arrowEndForDragging.toX,
								arrowEndForDragging.toY)) {
							arr.toId = block.id;
							arr.endsAtBlock = true;
							arr.endsAtLine = false;
							arr.toX = arrowEndForDragging.toX;
							arr.toY = arrowEndForDragging.toY;
							break;
						} else if (null != (fp = findArrow(
								arrowEndForDragging.toX,
								arrowEndForDragging.toY))) {
							arr.toId = fp.arrow.id;
							arr.endsAtBlock = false;
							arr.endsAtLine = true;
							arr.toX = arrowEndForDragging.toX;
							arr.toY = arrowEndForDragging.toY;
							break;
						}
					}
				}
				if (arr.toId == -1 || arr.fromId == -1) {
					arr.toX = -1;
				}

				arr.tailMarked = false;
				arr.headMarked = false;

				arrowEndForDragging = null;
				curDiag.changed = true;
				repaint();
				return;
			}

			if (bendForDragging != null) {
				bendForDragging.marked = false;
				bendForDragging = null;
				curDiag.changed = true;
				repaint();
				return;
			}

			if (blockSelForDragging != null) {
				// if (Math.abs(xa - oldx) < 4 && Math.abs(ya - oldy) < 4) // do
				// not respond to small twitches
				// return;
				selBlock = blockSelForDragging;
				int savex = blockSelForDragging.cx;
				int savey = blockSelForDragging.cy;

				if (blockSelForDragging.hNeighbour != null) {
					if (curDiag.clickToGrid) {
						blockSelForDragging.cy = blockSelForDragging.hNeighbour.cy;
						blockSelForDragging.calcEdges();
					}
					blockSelForDragging.hNeighbour = null;
				}

				if (blockSelForDragging.vNeighbour != null) {
					if (curDiag.clickToGrid) {
						blockSelForDragging.cx = blockSelForDragging.vNeighbour.cx;
						blockSelForDragging.calcEdges();
					}
					blockSelForDragging.vNeighbour = null;
				}

				for (Arrow arrow : curDiag.arrows.values()) {
					if (arrow.fromId == blockSelForDragging.id) {
						arrow.fromX += blockSelForDragging.cx - savex;
						arrow.fromY += blockSelForDragging.cy - savey;
					}
					if (arrow.toId == blockSelForDragging.id
							&& !arrow.endsAtLine) {
						arrow.toX += blockSelForDragging.cx - savex;
						arrow.toY += blockSelForDragging.cy - savey;
					}
				}

				if (blockSelForDragging instanceof Enclosure) {
					Enclosure enc = (Enclosure) blockSelForDragging;
					if (enc.llb != null)
						enc.llb = null;
					if (enc.lla != null)
						enc.lla = null;
					enc.draggingContents = false;
				}

				// blockSelForDragging = null;

				repaint();
				return;
			}
			if (blockSelForDragging != null
					&& blockSelForDragging instanceof Enclosure) {
				((Enclosure) blockSelForDragging).corner = null;
				blockSelForDragging = null;
				curDiag.changed = true;
				repaint();
				return;
			}
			foundBlock = null;
			if (currentArrow == null) {

				// Look for a line to detect, for deletion, etc. - logic to end
				// arrow at a line comes in a later section...
				// currentArrow = null;
				// if (!leftButton) {

				FoundPoint fp = findArrow(xa, ya);
				if (fp != null && fp.arrow != null) {
					currentArrow = fp.arrow;
					arrowEnd = fp;
				}

				selArrow = currentArrow;

				if (currentArrow != null) {
					// Arrow arr = foundArrow;
					// arr.fromId = curDiag.foundBlock.id;
					if (currentArrow.endsAtLine || currentArrow.endsAtBlock) {
						curDiag = currentArrow.diag;
						currentArrow.buildArrowPopupMenu();

						// currentArrow.lastX = xa;
						// currentArrow.lastY = ya;
						curDiag.jpm.show(e.getComponent(), xa, ya);
						repaint();
						return;
					}
				}
				// else
				// if (curDiag.findArrowCrossing)
				// MyOptionPane.showMessageDialog(this,
				// "No arrow detected");
				// curDiag.findArrowCrossing = false;

				foundBlock = null;

				// Check if we are within a block

				for (Block block : curDiag.blocks.values()) {
					// block.calcEdges();
					if (!(block instanceof Enclosure)) {
						if (between(xa, block.cx - block.width / 4,
								block.cx + block.width / 4)
								&& between(ya, block.cy - block.height / 4,
										block.cy + block.height / 4)) {
							foundBlock = block;
							selBlock = block;
							selArrow = null;
							// block.x = xa;
							// block.y = ya;
							break;
						}
					} else { // if it is an enclosure block

						int hh = gFontHeight;
						if (between(xa,
								block.cx - block.width / 2 + block.width / 5,
								block.cx + block.width / 2 - block.width / 5)
								&& between(ya, block.cy - block.height / 2 - hh,
										block.cy - block.height / 2 + hh / 2)) {
							foundBlock = block;
							selBlock = block;
							selArrow = null;
							// block.x = xa;
							// block.y = ya;
							break;
						}
					}
				}

				// Didn't find a block, so we can create a new block -
				// block must not overlap x=0 or y=0
				// use right button
				// if (left)
				// return;

				// curDiag.xa = xa;
				// curDiag.ya = ya;
				if (!(blockType.equals(""))
						&& null != createBlock(blockType, xa, ya, curDiag, true))
					curDiag.changed = true;
				repaint();
				// repaint();

				// repaint();
				return;
			}

			// curDiag.currentArrow is not null....

			// check for end of arrow

			foundBlock = null;

			FoundPoint fp = findBlockEdge(xa, ya);
			if (fp != null) {
				foundBlock = fp.block;
				side = fp.side;
			}

			if (foundBlock != null // && leftButton
			) {
				if (between(currentArrow.fromX, x - 4 * scalingFactor,
						x + 4 * scalingFactor)
						&& between(currentArrow.fromY, y - 4 * scalingFactor,
								y + 4 * scalingFactor))
					return;

				/*
				 * if (foundBlock.id == currentArrow.fromId) {
				 * 
				 * if (MyOptionPane.NO_OPTION == MyOptionPane.showConfirmDialog(
				 * this,
				 * "Connecting arrow to originating block is deadlock-prone - do anyway?"
				 * , "Allow?", MyOptionPane.YES_NO_OPTION)) { Integer aid = new
				 * Integer(currentArrow.id); curDiag.arrows.remove(aid);
				 * foundBlock = null; currentArrow = null;
				 * 
				 * repaint(); return; } }
				 */
				boolean OK = true;
				Block from = curDiag.blocks
						.get(new Integer(currentArrow.fromId));
				if ((foundBlock instanceof ProcessBlock
						|| foundBlock instanceof ExtPortBlock)
						&& !(from instanceof IIPBlock)) {
					if (side == Side.BOTTOM) {
						int answer = MyOptionPane.showConfirmDialog(this,
								"Connect arrow to bottom of block?",
								"Please choose one",
								MyOptionPane.YES_NO_OPTION);
						if (answer != MyOptionPane.YES_OPTION)
							OK = false;
					}
					if (side == Side.RIGHT) {
						int answer = MyOptionPane.showConfirmDialog(this,
								"Connect arrow to righthand side?",
								"Please choose one",
								MyOptionPane.YES_NO_OPTION);
						if (answer != MyOptionPane.YES_OPTION)
							OK = false;
					}
				}
				if (!OK) {
					// MyOptionPane.showMessageDialog(this,
					// "Cannot end an arrow here");
					Integer aid = new Integer(currentArrow.id);
					curDiag.arrows.remove(aid);
					foundBlock = null;
					currentArrow = null;
					repaint();
					return;
				}

				Arrow a = currentArrow;
				a.endsAtBlock = true;
				a.toId = foundBlock.id;

				if (xa != curx) { // make sure t not
					// zero!
					double s = ya - a.lastY;
					double t = xa - a.lastX;
					s = s / t;
					if (side == Side.LEFT)
						xa = foundBlock.leftEdge;
					else if (side == Side.RIGHT)
						xa = foundBlock.rgtEdge;
					else if (side == Side.TOP)
						ya = foundBlock.topEdge;
					else
						ya = foundBlock.botEdge;

					if (Math.abs(s) < FORCE_HORIZONTAL) // force horizontal
						ya = a.lastY;

					if (Math.abs(s) > FORCE_VERTICAL) // force vertical
						xa = a.lastX;
				}

				a.toX = xa;
				a.toY = ya;

				// a.toSide = side;

				defaultPortNames(a);

				from = curDiag.blocks.get(new Integer(a.fromId));
				Block to = curDiag.blocks.get(new Integer(a.toId));
				Arrow a2 = a.findLastArrowInChain();
				to = curDiag.blocks.get(new Integer(a2.toId));

				boolean error = false;
				if (to instanceof IIPBlock && from instanceof ProcessBlock) {
					a2.reverseDirection();
					// MyOptionPane
					// .showMessageDialog(this,
					// "Direction of arrow has been reversed");
				}
				if (from instanceof ExtPortBlock && (from.type
						.equals(Block.Types.EXTPORT_OUT_BLOCK)
						|| from.type.equals(Block.Types.EXTPORT_OUTIN_BLOCK)
								&& a2.fromX < from.cx))
					error = true;
				else if (to instanceof ExtPortBlock && (to.type
						.equals(Block.Types.EXTPORT_IN_BLOCK)
						|| to.type.equals(Block.Types.EXTPORT_OUTIN_BLOCK)
								&& a2.toX > to.cx))
					error = true;

				if (!a2.checkSides())
					error = true;

				if (error) {
					MyOptionPane.showMessageDialog(this,
							"Arrow attached to one or both wrong side(s) of blocks",
							MyOptionPane.WARNING_MESSAGE);
					Integer aid = new Integer(a2.id);
					curDiag.arrows.remove(aid);
				} else {
					curDiag.changed = true;
					// checkCompatibility(a);
				}

				Block toBlock = curDiag.blocks.get(new Integer(a2.fromId));
				if (toBlock.type.equals(Block.Types.EXTPORT_OUT_BLOCK)
						|| toBlock.type.equals(Block.Types.EXTPORT_OUTIN_BLOCK))
					a2.downStreamPort = "IN";
				foundBlock = null;

				currentArrow = null;
				curDiag.changed = true;

				repaint();
				return;
			}
			// currentDiag.foundBlock must be null
			// see if we can end an arrow on a line or line segment
			if (currentArrow != null && foundBlock == null) {

				Arrow foundArrow = null;

				// see if xa and ya are "close" to specified arrow
				fp = findArrow(xa, ya);
				if (fp != null && fp.arrow != null) {
					foundArrow = fp.arrow;
					arrowEnd = fp;

					if (x != curx) {
						double s = ya - foundArrow.lastY;
						double t = xa - foundArrow.lastX;
						s = s / t;
						if (Math.abs(s) < FORCE_HORIZONTAL) // force horizontal
							ya = foundArrow.lastY;
						if (Math.abs(s) > FORCE_VERTICAL) // force vertical
							xa = foundArrow.lastX;
					}
					currentArrow.toX = xa;
					currentArrow.toY = ya;
					currentArrow.endsAtLine = true;
					currentArrow.segNo = fp.segNo;
					currentArrow.upStreamPort = "OUT";

					// use id of target line, not of target block
					currentArrow.toId = foundArrow.id;

					defaultPortNames(foundArrow);

					Block from = curDiag.blocks
							.get(new Integer(currentArrow.fromId));
					Block to = curDiag.blocks.get(new Integer(foundArrow.toId));
					Arrow a2 = foundArrow.findLastArrowInChain();
					to = curDiag.blocks.get(new Integer(a2.toId));

					if (to == from) {
						if (MyOptionPane.NO_OPTION == MyOptionPane
								.showConfirmDialog(this,
										"Connecting arrow to originating block is deadlock-prone - do anyway?",
										"Allow?", MyOptionPane.YES_NO_OPTION)) {
							Integer aid = new Integer(currentArrow.id);
							curDiag.arrows.remove(aid);
							foundBlock = null;
							currentArrow = null;

							repaint();
							return;
						}

					}

					boolean error = true;
					if (from instanceof ExtPortBlock
							&& from.type.equals(Block.Types.EXTPORT_OUT_BLOCK))
						MyOptionPane.showMessageDialog(this,
								"Arrow in wrong direction",
								MyOptionPane.ERROR_MESSAGE);
					else if (to instanceof ExtPortBlock
							&& to.type.equals(Block.Types.EXTPORT_IN_BLOCK))
						MyOptionPane.showMessageDialog(this,
								"Arrow in wrong direction",
								MyOptionPane.ERROR_MESSAGE);
					else
						error = false;
					if (error) {
						Integer aid = new Integer(currentArrow.id);
						curDiag.arrows.remove(aid);
					} else {
						curDiag.changed = true;

						// checkCompatibility(curDiag.currentArrow);

						/*
						 * if (to != null) { if (side == Side.TOP)
						 * curDiag.currentArrow.toY = to.cy - to.height / 2;
						 * else if (side == Side.BOTTOM)
						 * curDiag.currentArrow.toY = to.cy + to.height / 2;
						 * else if (side == Side.LEFT) curDiag.currentArrow.toX
						 * = to.cx - to.width / 2; else if (side == Side.RIGHT)
						 * curDiag.currentArrow.toX = to.cx + to.width / 2; }
						 */
					}

					currentArrow = null;

					// repaint();
					return;
				}

				// else if (leftButton) { // foundArrow is null, so
				// we may
				// have a
				// bend

				// appears to be a delete arrow???

				if (currentArrow != null) {
					if (!(between(xa, currentArrow.toX - 4 * scalingFactor,
							currentArrow.toX + 4 * scalingFactor)
							&& between(ya, currentArrow.toY - 4 * scalingFactor,
									currentArrow.toY + 4 * scalingFactor))) {
						// curDiag.currentArrow.toX = xa;
						// curDiag.currentArrow.toY = ya;
						// }
						// else {
						Integer aid = new Integer(currentArrow.id);
						curDiag.arrows.remove(aid);
						foundBlock = null;
						currentArrow = null;
						repaint();
						return;
					}
					// repaint();
				}

				if (currentArrow.bends == null) {
					currentArrow.bends = new LinkedList<Bend>();
				}
				x = xa;
				y = ya;
				currentArrow.endX2 = x;
				currentArrow.endY2 = y;

				if (xa != currentArrow.lastX) {
					double s = ya - currentArrow.lastY;
					double t = xa - currentArrow.lastX;
					s = s / t;
					if (Math.abs(s) < FORCE_HORIZONTAL) // force horizontal
						ya = currentArrow.lastY;
					if (Math.abs(s) > FORCE_VERTICAL) // force vertical
						xa = currentArrow.lastX;
				}
				//Bend bend = new Bend(xa, ya);
				//currentArrow.bends.add(bend);  // was resulting in duplicate bend objects
				currentArrow.createBend(xa, ya);  
				currentArrow.lastX = x;
				currentArrow.lastY = y;
				currentArrow.toX = x;
				currentArrow.toY = y;
				curDiag.changed = true;
				repaint();
				// return;
				// }
			}

		}

		public void mouseClicked(MouseEvent arg0) {
			// do nothing
		}

	}

}
