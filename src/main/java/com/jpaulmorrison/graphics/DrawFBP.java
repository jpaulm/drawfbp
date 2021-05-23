package com.jpaulmorrison.graphics;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.*;

import math.geom2d.line.DegeneratedLine2DException;
import math.geom2d.line.Line2D;
import math.geom2d.Point2D;
import math.geom2d.line.StraightLine2D;

import java.awt.image.*;

import javax.swing.*;
import javax.swing.event.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.help.JHelp;
import javax.imageio.ImageIO;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.Sides;

import java.lang.reflect.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class DrawFBP extends JFrame implements ActionListener, ComponentListener, ChangeListener, 
    MouseMotionListener, MouseListener {

	static final long serialVersionUID = 111L;
	// private static final DrawFBP DrawFBP = null;
	DrawFBP driver = this;

	JLabel diagDesc;

	JFrame jf = null;

	JTextField jfl = null;
	JTextField jfs = null;
	JTextField jfv = null;

	JLabel scaleLab;

	Diagram curDiag = null;

	File currentImageDir = null;

	// JFrame this;

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
	// String jhallJarFile = null;

	Block selBlockM = null; // used when mousing over, and locating block for 
	                        //    drawing arrows
	Block selBlock = null; // permanent select
	Arrow selArrow = null; // permanent select
	String generalFont = null;
	String fixedFont = null;
	Font fontf = null;
	Font fontg = null;

	float defaultFontSize;
	Notation currNotn = null;

	double scalingFactor;
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

	//String blockType = Block.Types.PROCESS_BLOCK;

	FoundPointB edgePoint = null; // this controls display of detection areas while mouse moving

	FoundPointB fpArrowRoot = null; // this is used to draw blue circle where
									// arrows can start
	FoundPointB fpArrowEndB = null;

	FoundPointA fpArrowEndA = null;

	Arrow currentArrow = null;
	Block foundBlock;

	URLClassLoader myURLClassLoader = null;

	int curx, cury;

	Notation notations[];
	Lang langs[];

	// FileChooserParm[] fCPArray = new FileChooserParm[10];

	JCheckBox grid;

	boolean leftButton;

	boolean sortByDate; // remember across invocations of MyFileChooser

	int zWS; // zone width scaled

	static final int gridUnitSize = 4; // can be static - try for now

	static final double FORCE_VERTICAL = 20.0; // can be static as this is a
												// slope

	static final double FORCE_HORIZONTAL = 0.05; // can be static as this is a
													// slope
	static final int zoneWidth = 8;

	static final int CREATE = 1;
	static final int MODIFY = 2;

	// public static final String Side = null;

	static enum Corner {
		NONE, TOPLEFT, BOTTOMLEFT, TOPRIGHT, BOTTOMRIGHT
	}

	static final int top_border_height = 60;
	static final int bottom_border_height = 60;		

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

	String blockNames[] = { "Process", "Initial IP", "Enclosure", "Subnet", "ExtPort In", "ExtPort Out", "ExtPort O/I",
			"Legend", "File", "Person", "Report" };
	
	String shortNames[] = { "Proc", "IIP", "Encl", "Subn", "ExtPt I", "EP O", "EP O/I",
			"Legd", "File", "Pers", "Rept" };

		
	static int BUT_PROCESS = 0;
	static int BUT_IIP = 1;
	static int BUT_ENCL = 2;
	static int BUT_SUBNET = 3;
	static int BUT_EXTPORT_IN = 4;
	static int BUT_EXTPORT_OUT = 5;
	static int BUT_EXTPORT_OI = 6;
	static int BUT_LEGEND = 7;
	static int BUT_FILE = 8;
	static int BUT_PERSON = 9;
	static int BUT_REPORT = 10;
	
	
	
	// HashMap<String, String> jarFiles = new HashMap<String, String>(); // does not
	// contain JavaFBP jar file
	Set<String> jarFiles = new HashSet<String>(); // does not contain JavaFBP jar file
	// HashMap<String, String> dllFiles = new HashMap<String, String>();
	Set<String> dllFiles = new HashSet<String>();

	// JPopupMenu curPopup = null; // currently active popup menu

	// String scale;
	// boolean tryFindJarFile = true;
	
	
	
	boolean willBeSubnet = false;

	JMenuBar menuBar = null;
	JMenu fileMenu = null;
	JMenu diagMenu = null;
	JMenu helpMenu = null;

	JMenuItem gNMenuItem = null;
	JMenuItem[] gMenu = null;
	JMenuItem menuItem1 = new JMenuItem();
	JMenuItem menuItem2 = new JMenuItem();
	JMenuItem menuItem3 = new JMenuItem();
	JMenuItem compMenu = null;
	JMenuItem runMenu = null;

	String fbpJsonFile = null;

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
	// JFrame popup = null;
	// JFrame popup2 = null;
	JFrame popup = null;
	// JFrame depDialog = null;

	DefaultMutableTreeNode fbpJsonTree = new DefaultMutableTreeNode();

	static enum Side {
		LEFT, TOP, RIGHT, BOTTOM
	}
	// static boolean READFILE = true;

	Cursor defaultCursor = null;
	// boolean use_drag_icon = false;

	JLabel zoom = new JLabel("Zoom");
	JCheckBox pan = new JCheckBox("Pan");
	JButton up = new JButton();
	
	MyRadioButton[] but = null;
	Box box21 = null;

	// Timer ttStartTimer = null;
	// Timer ttEndTimer = null;
	// boolean drawToolTip = false;
	//boolean gotDllReminder = false;

	// FileChooserParm diagFCParm = null;
	String[] filterOptions = { "", "All (*.*)" };
	// volatile boolean finished = false;
	// String clsDir = null;
	String progName = null;
	String output = "";
	String error = "";
	String exeDir = null;

	String[] pBCmdArray = null;
	String pBDir = null;

	Point headMark; // used for arrows
	Point tailMark;

	int moveX;
	int moveY;

	// Arrow detArr = null;
	// int detArrSegNo;

	boolean tabCloseOK = true;

	// boolean comparing = false;
	JFrame mmFrame = new JFrame();

	JSlider zoomControl = null;

	boolean clickToGrid = true;

	// LinkedList<String> fbpJsonLl = null;
	
	MyRadioButton selRB = null;
	
	//String blkType = null;

	final boolean CODEMGRCREATE = true;

	
	DrawFBP(String[] args) {

		properties = new HashMap<String, String>();
		startProperties = new HashMap<String, String>();
		readPropertiesFile();
		if (args.length == 1) {
			diagramName = args[0];
			diagramName = diagramName.replace("\\", "/");
			if (diagramName.indexOf("/") == -1) {
				final String dir = System.getProperty("user.dir");
				diagramName = dir + "/" + diagramName;
			}
			// System.out.println("Diagram: " + diagramName );
			File f = new File(diagramName);
			if (!f.exists())
				// System.out.println("Diagram: " + diagramName + "can't be found" );
				diagramName = null;
		} else {
			diagramName = properties.get("currentDiagram");
		}

		frameInit();

		langs = new Lang[12];

		langs[Lang.JAVA] = new Lang("Java", "java", new JavaFileFilter(), "currentJavaFBPDir");
		langs[Lang.CSHARP] = new Lang("C#", "cs", new CsharpFileFilter(), "currentCsharpFBPDir");
		langs[Lang.JS] = new Lang("JS", "js", new JSFilter(), "currentJSDir");
		langs[Lang.FBP] = new Lang("FBP", "fbp", new FBPFilter(), "currentFBPNetworkDir");
		langs[Lang.DIAGRAM] = new Lang("Diagram", "drw", new DiagramFilter(), "currentDiagramDir"); // y
		langs[Lang.IMAGE] = new Lang("Image", "png", new ImageFilter(), "currentImageDir");
		langs[Lang.JARFILE] = new Lang("Jar File", "jar", new JarFileFilter(), "javaFBPJarFile");
		langs[Lang.CLASS] = new Lang("Class", "class", new JavaClassFilter(), "currentClassDir");
		langs[Lang.FBP_JSON] = new Lang("JSON", "json", new JSONFilter(), "currentJSDir");
		langs[Lang.DLL] = new Lang(null, "dll", new DllFilter(), "dllFileDir");
		langs[Lang.EXE] = new Lang(null, "exe", new ExeFilter(), "exeDir");
		langs[Lang.PRINT] = new Lang("Print", null, null, null);

		notations = new Notation[4];
		notations[Notation.JAVA_FBP] = new Notation("JavaFBP", langs[Lang.JAVA]);
		notations[Notation.CSHARP_FBP] = new Notation("C#FBP", langs[Lang.CSHARP]);
		notations[Notation.JSON] = new Notation("JSON", langs[Lang.JS]);
		notations[Notation.FBP] = new Notation("FBP", langs[Lang.FBP]);

		
		// saveProp("defaultNotation", currNotn.label);
		// langs[Lang.PROCESS] = new Lang(null, "proc", null, currNotn.srcDirProp);
		// langs[Lang.NETWORK] = new Lang(null, "network", null, currNotn.netDirProp);

		fileMenu = new JMenu(" File ");
		diagMenu = new JMenu(" Diagram ");
		helpMenu = new JMenu(" Help ");
		
		currNotn = findNotnFromLabel(properties.get("defaultNotation")); 
		if (currNotn == null)
			currNotn = notations[Notation.JAVA_FBP];
		//setNotation(currNotn);

		scalingFactor = 1.0d;

		String sF = properties.get("scalingfactor");
		if (sF != null)
			scalingFactor = Double.valueOf(sF);

		zoomControl = new JSlider(SwingConstants.VERTICAL, 60, 200, (int) (scalingFactor * 100));
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

		zoomControl.setValue((int) (scalingFactor * 100));
		// String scale = (int) js.getValue() + "%";
		String scale = (int) (scalingFactor * 100) + "%";
		scaleLab = new JLabel();
		scaleLab.setText(scale);

		zWS = (int) Math.round(zoneWidth * scalingFactor);
		
		//but[BUT_PROCESS].code = Block.Types.PROCESS_BLOCK;

		try {

			diagDesc = new JLabel("  ");
			grid = new JCheckBox("Grid");

			createAndShowGUI();
		} catch (NullPointerException e) {
			e.printStackTrace();
			saveProperties();
		}

	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be invoked
	 * from the event-dispatching thread.
	 */
	private void createAndShowGUI() {

		// Create and set up the window.

		// readPropertiesFile();

		addMouseMotionListener(this);
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		// label = new JLabel(" ");

		// this = this;
		setTitle("DrawFBP Diagram Generator");
		// SwingUtilities.updateComponentTreeUI(this);
		// this = new JFrame("DrawFBP Diagram Generator");
		setUndecorated(false); // can't change size of JFrame title,
								// though!
		defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		setCursor(defaultCursor);
		//JScrollPane jsp = new JScrollPane(null, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		//		JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		//getContentPane().add(jsp);
		applyOrientation(this);

		int w = (int) dim.getWidth();
		int h = (int) dim.getHeight();
		
		setPreferredSize(new Dimension(w, h));
		// maxX = (int) (w * .8);
		// maxY = (int) (h * .8);
		buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		// osg = buffer.createGraphics();
		osg = (Graphics2D) buffer.getGraphics();
		// osg = (Graphics2D) getGraphics();
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

		// diagFCParm = new FileChooserParm("Diagram", "currentDiagramDir",
		// langs[Lang.4], "Diagrams (*.drw)");

		RenderingHints rh = new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		rh.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		rh.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
		rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		rh.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		rh.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
		osg.setRenderingHints(rh);

		// readPropertiesFile();

		saveProp("versionNo", "v" + VersionAndTimestamp.getVersion());
		// saveProp("date", VersionAndTimestamp.getDate());

		// LocalDateTime date = LocalDateTime.now();
		// DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

		// String formattedDate = formatter.format(date);
		LocalDateTime a = LocalDateTime.from(ZonedDateTime.now());
		saveProp("date", a.toString());

		if (null == (generalFont = properties.get("generalFont"))) {
			generalFont = "Arial";
			saveProp("generalFont", generalFont);
		}
		if (null == (fixedFont = properties.get("fixedFont"))) {
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

		String dn = properties.get("defaultNotation");

		if (dn == null) {
			currNotn = notations[Notation.JAVA_FBP]; // JavaFBP
			// saveProperties();
		} else {
			if (dn.equals("NoFlo")) // transitional!
				dn = "JSON";
			currNotn = findNotnFromLabel(dn);
		}
		saveProp("defaultNotation", currNotn.label);

		String sBD = properties.get("sortbydate");
		if (sBD == null) {
			sortByDate = false;
			saveProp("sortbydate", "false");
		} else
			sortByDate = Boolean.getBoolean(sBD);

		// Iterator<String> entries = jarFiles.iterator();
		String z = "";
		String cma = "";

		// while (entries.hasNext()) {
		// String thisEntry = entries.next();
		for (String thisEntry : jarFiles) {
			z += cma + thisEntry;
			cma = ";";
		}
		saveProp("additionalJarFiles", z);

		// entries = dllFiles.iterator();
		z = "";
		cma = "";

		// while (entries.hasNext()) {
		// String thisEntry = entries.next();
		for (String thisEntry : dllFiles) {
			z += cma + thisEntry;
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

		jfl.setText("Fixed font: " + fixedFont + "; general font: " + generalFont);

		jfs = new JTextField("");

		jfs.setText("Font Size: " + defaultFontSize);

		jfv = new JTextField();

		jfv.setText("Ver: " + VersionAndTimestamp.getVersion());

		jtp = new JTabbedPaneWithCloseIcons(this);
		// int i = jtp.getTabCount();

		jtp.setForeground(Color.BLACK);
		jtp.setBackground(Color.WHITE);

		BufferedImage image = loadImage("DrawFBP-logo-small.png");

		if (image != null) {
			favicon = new ImageIcon(image);
			setIconImage(image);

		} else {
			MyOptionPane.showMessageDialog(this, "Couldn't find file: DrawFBP-logo-small.png",
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

		// jtp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
		// .put(escapeKS, "CLOSE");
		jtp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKS, "CLOSE");

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

		sortByDate = false;
		t = properties.get("sortbydate");
		if (t != null)
			sortByDate = Boolean.parseBoolean(t);

		t = properties.get("clicktogrid");
		if (t == null)
			clickToGrid = true;
		else
			clickToGrid = Boolean.valueOf(t);

		grid.setSelected(clickToGrid);
		
		fbpJsonFile = properties.get("fbpJsonFile");
		
		//String blockTypes[] = { Block.Types.PROCESS_BLOCK, Block.Types.IIP_BLOCK, Block.Types.ENCL_BLOCK,
		//		Block.Types.PROCESS_BLOCK, Block.Types.EXTPORT_IN_BLOCK, Block.Types.EXTPORT_OUT_BLOCK,
		//		Block.Types.EXTPORT_OUTIN_BLOCK, Block.Types.LEGEND_BLOCK, Block.Types.FILE_BLOCK, Block.Types.PERSON_BLOCK,
		//		Block.Types.REPORT_BLOCK };
		
		//MyRadioButton but[] = new MyRadioButton[11];
		
		
		//blkType = blockNames[BUT_PROCESS];

		setVisible(true);
		addComponentListener(this);

		repaint();

		// wDiff = getWidth() - curDiag.area.getWidth();
		// hDiff = getHeight() - curDiag.area.getHeight();

		// if (diagramName == null) { // See if a parameter was passed to the jar
		// file....
		// diagramName = properties.get("currentDiagram");
		// System.out.println(diagramName);
		// }

		boolean small = (diagramName) == null ? false : true;

		if (!small) // try suppressing this...
			new SplashWindow(3000, this, small); // display
		// for 3.0 secs, or until mouse is moved

		// if (diagramName != null) {
		// actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
		// "Open " + diagramName));
		// }
		// if (diagramName == null) {
		// getNewDiag();
		// curDiag.desc = "Click anywhere on selection area";
		// }
		// else
		if (diagramName != null)
			actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Open " + diagramName));
	
		
		repaint();

	}

	private void buildUI(Container container) {

		buildPropDescTable();

		if (diagramName == null)
			diagramName = properties.get("currentDiagram");

		/*
		 * 
		 * MouseListener mouseListener = new MouseAdapter() {
		 * 
		 * public void mouseClicked(MouseEvent e) {
		 * 
		 * int i = jtp.indexAtLocation(e.getX(), e.getY()); if (i == -1) return;
		 * ButtonTabComponent b = (ButtonTabComponent) jtp .getTabComponentAt(i); if (b
		 * == null || b.diag == null) return; Diagram diag = b.diag;
		 * 
		 * if (diag == null) { getNewDiag(); // diag = new Diagram(driver); // b.diag =
		 * diag; } // curDiag = diag; else { //if (i == -1 ) { //
		 * MyOptionPane.showMessageDialog(driver, // "No diagram selected", //
		 * MyOptionPane.WARNING_MESSAGE); //} //else if (comparing) { comparing = false;
		 * compare(i); return; } else jtp.setSelectedIndex(i); repaint(); }
		 * 
		 * repaint();
		 * 
		 * } };
		 */
		getNewDiag();
		curDiag.desc = "Click anywhere on selection area";

		// jtp.addMouseListener(mouseListener);
		jtp.addMouseListener(this);

		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		Box box1 = new Box(BoxLayout.Y_AXIS);
		container.add(box1);

		Box box4 = new Box(BoxLayout.X_AXIS);
		box1.add(box4);
		/*
		 * int sf = (int) Math.round(100.0 * scalingFactor); zoomControl = new
		 * JSlider(JSlider.VERTICAL, 60, 200, sf); zoomControl.setPreferredSize(new
		 * Dimension(40, 200)); zoomControl.setMajorTickSpacing(20); //
		 * zoomControl.setMinorTickSpacing(10); zoomControl.setPaintTicks(true);
		 * zoomControl.setSnapToTicks(true); zoomControl.setPaintLabels(false);
		 * zoomControl.setPaintTrack(true); zoomControl.setVisible(true);
		 * zoomControl.addChangeListener(this); zoomControl.getInputMap().put(escapeKS,
		 * "CLOSE"); zoomControl.getActionMap().put("CLOSE", escapeAction);
		 */
		// zoomControl.setBackground(Color.WHITE);
		Box box45 = new Box(BoxLayout.Y_AXIS);
		Box box46 = new Box(BoxLayout.X_AXIS);
		Box box5 = new Box(BoxLayout.X_AXIS);
		Box box61 = new Box(BoxLayout.X_AXIS);
		Box box62 = new Box(BoxLayout.X_AXIS);
		Box box6 = new Box(BoxLayout.Y_AXIS);

		box5.add(Box.createRigidArea(new Dimension(10, 0)));

		box6.add(Box.createRigidArea(new Dimension(0, 10)));

		// scaleLab = new JLabel();
		box61.add(scaleLab);
		box61.add(Box.createRigidArea(new Dimension(5, 0)));

		box62.add(zoom);
		box62.add(Box.createRigidArea(new Dimension(5, 0)));

		box6.add(zoomControl);
		box6.add(Box.createRigidArea(new Dimension(0, 10)));

		scaleLab.setForeground(Color.BLUE);
		// String scale = "100%";
		// scaleLab.setText(scale);

		saveProp("scalingfactor", Double.toString(scalingFactor));

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
		// curDiag.area.setAlignmentX(Component.LEFT_ALIGNMENT);
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
		up.setActionCommand("Go to Directory");
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

		up.setText("Go to Dir");
		
		but = new MyRadioButton[11];
		for (int j = 0; j < but.length; j++) {
			but[j] = new MyRadioButton();
			but[j].addActionListener(this);
			butGroup.add(but[j]);
			box21.add(but[j]);
			if (j < but.length - 1)
				box21.add(Box.createHorizontalGlue());
			but[j].setText(blockNames[j]);
			but[j].setFocusable(true);
		}
		
		but[but.length - 1].setAlignmentX(Component.RIGHT_ALIGNMENT);
		
		but[BUT_PROCESS].code = Block.Types.PROCESS_BLOCK;
		but[BUT_IIP].code = Block.Types.IIP_BLOCK;
		but[BUT_ENCL].code = Block.Types.ENCL_BLOCK;
		but[BUT_SUBNET].code = Block.Types.PROCESS_BLOCK;
		but[BUT_EXTPORT_IN].code = Block.Types.EXTPORT_IN_BLOCK;
		but[BUT_EXTPORT_OUT].code = Block.Types.EXTPORT_OUT_BLOCK;
		but[BUT_EXTPORT_OI].code = Block.Types.EXTPORT_OUTIN_BLOCK;
		but[BUT_LEGEND].code = Block.Types.LEGEND_BLOCK;
		but[BUT_FILE].code = Block.Types.FILE_BLOCK;
		but[BUT_PERSON].code = Block.Types.PERSON_BLOCK;
		but[BUT_REPORT].code = Block.Types.REPORT_BLOCK;
		
		but[BUT_PERSON].oneLine = true; 
		but[BUT_EXTPORT_IN].oneLine = true; 
		but[BUT_EXTPORT_OUT].oneLine = true; 
		but[BUT_EXTPORT_OI].oneLine = true; 
		but[BUT_IIP].oneLine = true; 
		but[BUT_ENCL].oneLine = true; 
		
		selRB = but[BUT_PROCESS];  // set selected RadioButton to "Process"	

		box1.add(box2);

		// box21.add(Box.createRigidArea(new Dimension(10,0)));
		// box21.add(Box.createHorizontalStrut(10));
		//adjustFonts();

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
		closedPawCursor = tk.createCustomCursor(image, new Point(15, 15), "Paw");

		image = loadImage("drag_icon.gif");
		drag_icon = tk.createCustomCursor(image, new Point(4, 5), "Drag");
		
		menuBar = createMenuBar();
		setJMenuBar(menuBar);
		
		setNotation(currNotn);
		adjustFonts();
		
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
		fileMenu.setFont(fontg);
		diagMenu.setFont(fontg);
		helpMenu.setFont(fontg);
		menuBar.add(fileMenu);

		// a group of JMenuItems
		JMenuItem menuItem = new JMenuItem("Open Diagram");
		// menu.setMnemonic(KeyEvent.VK_D);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.ALT_MASK));

		fileMenu.add(menuItem);
		menuItem.addActionListener(this);
		menuItem = new JMenuItem("Save");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);
		menuItem = new JMenuItem("Save as...");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK));
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);
		menuItem = new JMenuItem("New Diagram");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);
		fileMenu.addSeparator();
		JMenu gnMenu = new JMenu("Select Network Notation...");
		fileMenu.add(gnMenu);
		int j = notations.length;
		gMenu = new JMenuItem[j];

		int k = 0;
		for (int i = 0; i < j; i++) {
			// if (!(genlangs[Lang.i].label.equals("FBP"))) {
			gMenu[k] = new JMenuItem(notations[i].label);
			gnMenu.add(gMenu[k]);
			gMenu[k].addActionListener(this);
			k++;
			// }
		}

		fileMenu.addSeparator();

		String s = "Generate ";
		// if (curDiag != null)
		s += currNotn.label + " ";
		s += "Network";
		gNMenuItem = new JMenuItem(s);
		fileMenu.add(gNMenuItem);
		gNMenuItem.addActionListener(this);

		menuItem = new JMenuItem("Display Source Code");
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);

		fileMenu.addSeparator();

		if (currNotn.lang == langs[Lang.JAVA] || currNotn.lang == langs[Lang.CSHARP]) {
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

			// fileMenu.addSeparator();
			// menuItem = new JMenuItem("Compare Diagrams");
			// fileMenu.add(menuItem);
			// menuItem.addActionListener(this);

			fileMenu.addSeparator();
		}
		
		if (currNotn.lang == langs[Lang.JAVA] || currNotn == notations[Notation.JSON])  
			fileMenu.add(menuItem1);
		
		if (currNotn.lang == langs[Lang.JAVA] || currNotn.lang == langs[Lang.CSHARP])  {
			fileMenu.add(menuItem2);		
			fileMenu.add(menuItem3);
		}
		
		modMenuItems();
		
		
		fileMenu.addSeparator();
		// menuItem = new JMenuItem("Locate DrawFBP Help File");
		// fileMenu.add(menuItem);
		// menuItem.addActionListener(this);
		// }
		//fileMenu.addSeparator();

		menuItem = new JMenuItem("Change Fonts");
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);
		menuItem = new JMenuItem("Change Font Size");
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);

		fileMenu.addSeparator();
		menuItem = new JMenuItem("Show Image");
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);
		
		menuItem = new JMenuItem("Print Image");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.ALT_MASK));
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
		
		int ct = fileMenu.getItemCount(); 		
		for (int i = 0; i < ct; i++) {
			JMenuItem jmi = fileMenu.getItem(i);
			if (jmi instanceof JMenuItem)
				jmi.setFont(fontg);			
		}

		// String u = "Generate " + currNotn.label + " " + "Network";
		// gNMenuItem = new JMenuItem(u);
		// gNMenuItem.addActionListener(this);
		// fileMenu.add(gNMenuItem, 7);

		// editMenu = new JMenu(" Edit ");
		diagMenu.setMnemonic(KeyEvent.VK_E);
		// editMenu.setFont(fontg);
		menuBar.add(diagMenu);
		diagMenu.setBorderPainted(true);

		menuItem = new JMenuItem("Edit Description");
		diagMenu.add(menuItem);
		menuItem.addActionListener(this);
		menuItem = new JMenuItem("New Block");
		diagMenu.add(menuItem);
		menuItem.addActionListener(this);
		diagMenu.addSeparator();
		menuItem = new JMenuItem("Create Image");
		diagMenu.add(menuItem);
		menuItem.addActionListener(this);
		//menuItem = new JMenuItem("Show Image");
		//diagMenu.add(menuItem);
		//menuItem.addActionListener(this);
		//fileMenu.addSeparator();
		//menuItem = new JMenuItem("Print Image");
		//menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.ALT_MASK));
		//diagMenu.add(menuItem);
		//menuItem.addActionListener(this);
		diagMenu.addSeparator();
		menuItem = new JMenuItem("Compare Diagrams");
		diagMenu.add(menuItem);
		menuItem.addActionListener(this);
		menuItem = new JMenuItem("Clear Compare Indicators");
		diagMenu.add(menuItem);
		menuItem.addActionListener(this);
		diagMenu.addSeparator();
		menuItem = new JMenuItem("Block-related Actions");
		diagMenu.add(menuItem);
		menuItem.addActionListener(this);
		menuItem = new JMenuItem("Arrow-related Actions");
		diagMenu.add(menuItem);
		menuItem.addActionListener(this);
		
		ct = diagMenu.getItemCount();  
		for (int i = 0; i< ct; i++) {
			JMenuItem jmi = diagMenu.getItem(i);
			if (jmi instanceof JMenuItem)
				jmi.setFont(fontg);
		}

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

		jtf.setText("Diagram Notation: " + currNotn.label);
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
		menuBar.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(escapeKS, "CLOSE");

		menuBar.getActionMap().put("CLOSE", escapeAction);
		menuBar.setVisible(true);

		ct = helpMenu.getItemCount();  
		for (int i = 0; i< ct; i++) {
			JMenuItem jmi = helpMenu.getItem(i);
			if (jmi instanceof JMenuItem)
				jmi.setFont(fontg);
		}

		return menuBar;
	}
	
	void modMenuItems() {
	//fileMenu.remove(menuItem1);
	//fileMenu.remove(menuItem2);
	//fileMenu.remove(menuItem3);
		
	menuItem1.setEnabled(false);
	menuItem2.setEnabled(false);
	menuItem3.setEnabled(false);
	
	menuItem1.removeActionListener(this);
	menuItem2.removeActionListener(this);
	menuItem3.removeActionListener(this);
	
	if (currNotn.lang == langs[Lang.JAVA]) {
		menuItem1.setText("Locate JavaFBP jar file");
		//fileMenu.add(menuItem1);
		menuItem1.setEnabled(true);
		menuItem1.addActionListener(this);
	}
	
	if (currNotn == notations[Notation.JSON]) {
		menuItem1.setText("Locate fbp.json file");
		//fileMenu.add(menuItem1);
		menuItem1.setEnabled(true);
		menuItem1.addActionListener(this);
	}

	if (currNotn.lang == langs[Lang.JAVA]) {
		menuItem2.setText("Add Additional Jar File");
		//fileMenu.add(menuItem2);
		menuItem2.setEnabled(true);
		menuItem2.addActionListener(this);

		menuItem3.setText("Remove Additional " + "Jar" + " Files");
		//fileMenu.add(menuItem3);
		menuItem3.setEnabled(true);
		menuItem3.addActionListener(this);

	}  
	if (currNotn.lang == langs[Lang.CSHARP]) {
		menuItem2.setText("Add Additional Dll File");
		// menuItem2c.setEnabled(currNotn != null && currNotn.lang.equals("C#"));
		//fileMenu.add(menuItem2);
		menuItem2.setEnabled(true);
		menuItem2.addActionListener(this);

		menuItem3.setText("Remove Additional " + "Dll" + " Files");
		//fileMenu.add(menuItem3);
		menuItem3.setEnabled(true);
		menuItem3.addActionListener(this);
	}
	
}

	// sets curDiag

	Diagram getNewDiag() {
		Diagram diag = new Diagram(this);
		SelectionArea sa = getNewArea();
		diag.area = sa;
		int i = jtp.getTabCount(); // get count *before* adding new sa & label
		jtp.add(sa, new JLabel());

		String s = diagramName;
		if (s == null || s.endsWith(".drw")) {
			curDiag.area.addMouseListener(curDiag.area);
			curDiag.area.addMouseMotionListener(curDiag.area);
		}

		// int j = jtp.getTabCount(); // for debugging
		// System.out.println("new tab");
		ButtonTabComponent b = new ButtonTabComponent(jtp, this);
		jtp.setTabComponentAt(i, b);
		jtp.setSelectedIndex(i);
		b.diag = diag;
		// diag.tabNum = i;
		curDiag = diag;

		// diag.diagNotn = currNotn;

		diag.title = "(untitled)";
		diag.area.setAlignmentX(Component.LEFT_ALIGNMENT);
		diag.blocks = new ConcurrentHashMap<Integer, Block>();
		diag.arrows = new ConcurrentHashMap<Integer, Arrow>();

		repaint();

		return diag;
	}

	
	public void actionPerformed(ActionEvent e) {

		// comparing = false;

		if (e.getSource() == jfl) {
			changeFonts();
			return;
		}

		if (e.getSource() == jfs) {
			changeFontSize();
			return;
		}
	
		if (e.getSource() instanceof MyRadioButton) {
			MyRadioButton rb = (MyRadioButton) e.getSource();
			rb.setSelected(true);
			selRB = rb;
			//setBlkType(); 
			willBeSubnet = (selRB == but[BUT_SUBNET]);  
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

			// int i = jtp.getTabCount();
			// if (i > 1 || curDiag.diagFile != null || curDiag.changed)
			getNewDiag();

			// jtp.setSelectedIndex(curDiag.tabNum);

			repaint();

			return;

		}

		// Change Notation
		for (int j = 0; j < gMenu.length; j++) {
			if (e.getSource() == gMenu[j]) {
				Notation currNotn = notations[j];

				curDiag.changed = true;

				setNotation(currNotn);

				MyOptionPane.showMessageDialog(this, "Notation changed to " + currNotn.label
						+ "\nNote: some File and Block-related options will have changed");
				repaint();

				return;
			}
		}

		// }

		if (s.startsWith("Generate ")) {
			if (curDiag == null || curDiag.blocks.isEmpty()) {
				MyOptionPane.showMessageDialog(this, "No components specified", MyOptionPane.ERROR_MESSAGE);
				return;
			}

			if (curDiag.title == null || curDiag.title.equals("(untitled)")) {

				MyOptionPane.showMessageDialog(this, "Untitled diagram - please do Save first",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

			// properties.get(gl.netDirProp);

			CodeManager mc = new CodeManager(curDiag, CODEMGRCREATE);
			mc.genCode();

			//((java.awt.AWTEvent) e).consume();
			return;

		}

		if (s.equals("Display Source Code")) {

			File cFile = null;

			// MyOptionPane.showMessageDialog(this, "Select a source file",
			// MyOptionPane.INFORMATION_MESSAGE);

			String ss = properties.get(currNotn.netDirProp);
			// File f = curDiag.diagFile;

			// String name = f.getName();

			if (ss == null)
				ss = System.getProperty("user.home");

			File file = new File(ss);
			MyFileChooser fc = new MyFileChooser(this, file, currNotn.lang, "Display Source Code");

			int returnVal = fc.showOpenDialog(); // force NOT saveAs

			cFile = null;
			if (returnVal == MyFileChooser.APPROVE_OPTION) {
				cFile = new File(getSelFile(fc));
			}

			if (cFile == null || !(cFile.exists()))
				return;

			CodeManager cm = new CodeManager(curDiag, !CODEMGRCREATE);
			cm.doc.changed = false;

			cm.displayDoc(cFile, currNotn.lang, null);

			repaint();
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

			if (curDiag == null || curDiag.diagFile == null) {
				MyOptionPane.showMessageDialog(this, "No diagram selected", MyOptionPane.INFORMATION_MESSAGE);
				return;
			}

			int result = MyOptionPane.showConfirmDialog(this,
					"Select diagram to compare against current diagram: " + curDiag.diagFile.getName(),
					"Select diagram to compare against", MyOptionPane.OK_CANCEL_OPTION);

			if (result != MyOptionPane.OK_OPTION)
				return;

			File f = curDiag.diagFile;

			MyFileChooser fc = new MyFileChooser(this, f, langs[Lang.DIAGRAM], "Select compare diagram");

			int returnVal = fc.showOpenDialog(); // force NOT saveAs

			File cFile = null;
			if (returnVal == MyFileChooser.APPROVE_OPTION) {
				cFile = new File(getSelFile(fc));
			}

			if (cFile == null || !(cFile.exists()))
				return;

			Diagram sd = curDiag;

			openAction(cFile.getAbsolutePath());

			curDiag = sd;

			int i = getFileTabNo(cFile.getAbsolutePath());
			if (i == -1)
				return;
			compare(i);
			repaint();
			return;
		}

		if (s.equals("Clear Compare Indicators")) {

			for (Block bl : curDiag.blocks.values()) {
				bl.compareFlag = null;
			}

			// for (Arrow ar : curDiag.arrows.values()) {
			// ar.compareFlag = null;
			// }

			// if (mmFrame != null)
			// mmFrame.dispose();

			return;
		}

		// if (s.equals("Clear Language Association")) {
		// currNotn = null;
		// curDiag.changed = true;
		// jtf.setText("");

		// curDiag.changeCompLang();
		// }

		// }
		if (s.equals("Locate JavaFBP jar file")) {

			locateJavaFBPJarFile(true);
			return;
		}

		if (s.equals("Locate fbp.json file")) {

			//int res = MyOptionPane.showConfirmDialog(this, "Locate or change location of fbp.json file?",
			//		"Locate fbp.json file", MyOptionPane.YES_NO_OPTION);
			//if (res == MyOptionPane.YES_OPTION)			
				locateFbpJsonFile(true);
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

		if (s.startsWith("Remove Additional ")) {
			jarFiles.clear();
			dllFiles.clear();
			String lib;
			if (currNotn.lang == langs[Lang.JAVA]) {
				properties.remove("additionalJarFiles");
				lib = "Jar";
			} else {
				properties.remove("additionalDllFiles");
				lib = "Dll";
			}
			MyOptionPane.showMessageDialog(this, "References to additional " + lib + " files removed (not deleted)",
					MyOptionPane.INFORMATION_MESSAGE);
			return;
		}

		

		if (s.equals("Change Fonts")) {
			changeFonts();
			return;
		}

		if (s.equals("Change Font Size")) {
			changeFontSize();
			return;
		}
		

		if (s.equals("Print Image")) {

			File fFile = null;

			String ss = properties.get("currentImageDir");
			if (ss == null)
				currentImageDir = new File(System.getProperty("user.home"));
			else
				currentImageDir = new File(ss);

			MyFileChooser fc = new MyFileChooser(this, currentImageDir, langs[Lang.IMAGE], "Print Image");

			File f = curDiag.diagFile;
			if (f != null) {
				int i = curDiag.diagFile.getName().indexOf(".drw");
				if (i > -1) {
					ss += "/" + curDiag.diagFile.getName().substring(0, i) + "." + langs[Lang.IMAGE].ext;
					fc.setSuggestedName(ss);
				}
			}

			int returnVal = fc.showOpenDialog(true, true,null); // set to saveAs

			String fileStr = null;
			fFile = null;
			if (returnVal == MyFileChooser.APPROVE_OPTION) {
				fileStr = getSelFile(fc);
				fFile = new File(fileStr);
			}

			if (fFile == null)
				return;

			if (!(fFile.exists()))
				return;

			BufferedImage image = null;
			try {
				image = ImageIO.read(fFile);
			} catch (IOException e1) {
				MyOptionPane.showMessageDialog(this, "Could not get image: " + fFile.getAbsolutePath(),
						MyOptionPane.ERROR_MESSAGE);
				return;

			}

			boolean landscape = image.getWidth() > image.getHeight();
			
			currentImageDir = new File(fFile.getParent());
			saveProp("currentImageDir", fFile.getParent());
			saveProperties();

			
			FileInputStream imagestream = null;
			try {
				imagestream = new FileInputStream(fileStr);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			DocFlavor doc = null;
			if (fileStr.endsWith(".jpg") || fileStr.endsWith(".jpeg"))
				doc = DocFlavor.INPUT_STREAM.JPEG;
			else if (fileStr.endsWith(".png"))
				doc = DocFlavor.INPUT_STREAM.PNG;
			else {
				MyOptionPane.showMessageDialog(this, "Image format must be 'jpg' or 'png': " + fileStr,		 
					MyOptionPane.ERROR_MESSAGE);
				return;
			}

			PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
			aset.add(new Copies(1));
			aset.add(MediaSizeName.A);
			aset.add(Sides.ONE_SIDED);
			
			if (landscape) 
				aset.add(OrientationRequested.LANDSCAPE);
			else 
				aset.add(OrientationRequested.PORTRAIT);
			
			Doc document = new SimpleDoc(imagestream, doc, null);

			
			PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
			String[] sa = new String[services.length];
			for (int i = 0; i < services.length; i++) {
				//System.out.println("Service: " + services[i].getName());
				sa[i] = services[i].getName();
			}
						
			MyFileChooser fc2 = new MyFileChooser(driver, null, langs[Lang.PRINT], "Choose Print Service");
			
			int ret = fc2.showOpenDialog(sa);

			if (ret == MyFileChooser.CANCEL_OPTION)
				return;
			if (ret != MyFileChooser.APPROVE_OPTION) {
				return;
			}
			
			int svc =  fc2.getRowNo();
			
			DocPrintJob job = services[svc].createPrintJob();
			try {
				job.print(document, aset);
			} catch (PrintException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return;
		}
		
		if (s.equals("Display Properties")) {
			displayProperties();
			return; 
		}

		if (s.equals("Toggle Click to Grid")) {
			clickToGrid = !clickToGrid;
			grid.setSelected(clickToGrid);
			saveProp("clicktogrid", Boolean.toString(clickToGrid));
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

		if (s.equals("Go to Directory")) {
			String w = null;
			File f = curDiag.diagFile;
			if (f != null) {
				w = f.getParent();
			}
			// w = f.getAbsolutePath();

			openAction(w);

			return;
		}

		if (s.equals("Create Image")) {

			if (curDiag == null || curDiag.title == null || curDiag.title.trim().equals("") ||
					curDiag.title.trim().equals("(untitled)") || 
					curDiag.blocks.isEmpty()) {
				MyOptionPane.showMessageDialog(this,
						"Unable to export image for empty or unsaved diagram - please do save first",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}
			
			curDiag.createImage();

			
			
			return;
			
		}

		if (s.equals("Show Image")) {

			File fFile = null;

			String ss = properties.get("currentImageDir");
			if (ss == null)
				currentImageDir = new File(System.getProperty("user.home"));
			else
				currentImageDir = new File(ss);

			MyFileChooser fc = new MyFileChooser(this, currentImageDir, langs[Lang.IMAGE], "Show Image");

			File f = curDiag.diagFile;
			if (f != null) {
				int i = curDiag.diagFile.getName().indexOf(".drw");
				if (i > -1) {
					ss += "/" + curDiag.diagFile.getName().substring(0, i) + "." + langs[Lang.IMAGE].ext;
					fc.setSuggestedName(ss);
				}
			}

			int returnVal = fc.showOpenDialog(true, true, null); // set to saveAs

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

			BufferedImage image = null;
			try {
				image = ImageIO.read(fFile);
			} catch (IOException e1) {
				MyOptionPane.showMessageDialog(this, "Could not get image: " + fFile.getAbsolutePath(),
						MyOptionPane.ERROR_MESSAGE);
				return;

			}

			currentImageDir = new File(fFile.getParent());
			saveProp("currentImageDir", fFile.getParent());
			saveProperties();

		    final JFrame jf = new JFrame();
		    jf.setUndecorated(false);
		   
		    jf.add(new JLabel(new ImageIcon(image)));
		    jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		    
		    jf.setPreferredSize(new Dimension(image.getWidth() + 20, image.getHeight() + top_border_height));
		 
		    jf.setLocation(400, 400);
		    //jf.setMinimumSize(jf.getPreferredSize());
		    jf.pack();
		    jf.setVisible(true);

			return;
		}

		if (s.equals("Close Diagram")) {
			closeTab(false);
			return;
		}
		if (s.equals("Launch Help")) {

			HelpSet hs = null;
			URL url = HelpSet.findHelpSet(getClass().getClassLoader(), "helpSet.hs");
			try {
				hs = new HelpSet(null, url);
			} catch (HelpSetException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			jHelpViewer = new JHelp(hs);

			// Create a new dialog screen
			// popup2 = new JFrame(this);
			popup = new JFrame();
			popup.setTitle("DrawFBP Help");
			popup.setIconImage(favicon.getImage());
			applyOrientation(popup);

			popup.setFocusable(true);
			popup.requestFocusInWindow();

			popup.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent ev) {
					if (ev.getKeyCode() == KeyEvent.VK_ESCAPE) {
						// frame2.setVisible(false);
						popup.dispose();
					}

				}
			});
			jHelpViewer.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(escapeKS, "CLOSE");

			jHelpViewer.getActionMap().put("CLOSE", escapeAction);

			// frame2.setPreferredSize(getPreferredSize());
			// Add the created helpViewer to it.
			popup.getContentPane().add(jHelpViewer);
			// Set a default close operation.
			popup.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			// Make this visible.
			popup.setVisible(true);
			popup.pack();
			Dimension dim = getSize();
			Point p = getLocation();
			int x_off = 100;
			int y_off = 100;
			popup.setPreferredSize(new Dimension(dim.width, dim.height));

			popup.pack();

			// popup2.setPreferredSize(
			// new Dimension(dim.width, dim.height - y_off));

			popup.setLocation(p.x + x_off, p.y + y_off);
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
					+ "*                                                  *\n" + "*             DrawFBP v" + v
					+ "      " + sp1 + "               *\n" + "*                                                  *\n"
					+ "*    Authors: J.Paul Rodker Morrison, Canada,      *\n"
					+ "*             Bob Corrick, UK                      *\n"
					+ "*                                                  *\n"
					+ "*    Copyright 2009, ..., 2021                     *\n"
					+ "*                                                  *\n"
					+ "*    FBP web site:                                 *\n"
					+ "*      https://jpaulm.github.io/fbp/index.html     *\n"
					+ "*                                                  *\n" + "*               (" + dt
					+ ")            " + sp2 + "       *\n" + "*                                                  *\n"
					+ "****************************************************\n");

			ta.setFont(f);
			final JDialog popup = new JDialog(this, Dialog.ModalityType.APPLICATION_MODAL);
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

		if (s.equals("Edit Description")) { // Title of diagram
			// as a whole

			String ans = (String) MyOptionPane.showInputDialog(this, "Enter or change text",
					"Modify diagram description", MyOptionPane.PLAIN_MESSAGE, null, null, curDiag.desc);

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
			// newItemMenu = buildNewItemMenu(this);
			// }
			// newItemMenu.setVisible(true);

			//Block blk = createBlock(blockType, x, y, curDiag, true);
			Block blk = createBlock(x, y, curDiag, true);
			if (null != blk) {
				// if (!blk.editDescription(CREATE))
				// return;;
				curDiag.changed = true;
				blk.buildSideRects();
			}
			repaint();
			return;

		}
		if (s.equals("Block-related Actions")) {
			Block b = selBlock;

			if (b == null) {
				MyOptionPane.showMessageDialog(this, "Block not selected", MyOptionPane.ERROR_MESSAGE);
				return;
			}
			curDiag = b.diag;
			b.buildBlockPopupMenu();
			// use_drag_icon = false;
			curDiag.jpm.show(this, x + 100, y + 100);
			repaint();
			return;

		}
		if (s.equals("Arrow-related Actions")) {
			Arrow a = selArrow;
			if (a == null) {
				MyOptionPane.showMessageDialog(this, "Arrow not selected", MyOptionPane.ERROR_MESSAGE);
				return;
			}
			a.buildArrowPopupMenu();
			curDiag = a.diag;
			curDiag.jpm.show(this, a.toX + 100, a.toY + 100);
			repaint();
			return;
		}

		MyOptionPane.showMessageDialog(this, "Command not recognized: " + s, MyOptionPane.ERROR_MESSAGE);
		//setBlkType(s);

		repaint();
	}

		
	void showImage(final BufferedImage image, String title, final boolean save) {
		
	
		ImagePanel jPanel = new ImagePanel(image);	
		
		//final JDialog dialog = new JDialog();	
		final JDialog dialog = new JDialog(this, "", Dialog.ModalityType.APPLICATION_MODAL);
		//iFrame.setTitle(title);
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {		
				askAboutSavingImage(jPanel, dialog, save);
				//askAboutSavingImage(jPanel.stretched_image, dialog, save);
			}

		});
		
		ComponentListener cl = new ComponentListener() {

			@Override
			public void componentResized(ComponentEvent e) {
				dialog.repaint();
				
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				
				
			}

			@Override
			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		};
		
		dialog.addComponentListener(cl);

				
		dialog.setPreferredSize(new Dimension(image.getWidth(), image.getHeight() + top_border_height + 
				bottom_border_height));
				
		dialog.add(jPanel);

		dialog.setLocation(new Point(200, 200));	
				
		dialog.pack();
		dialog.setVisible(true);

		dialog.repaint();
		repaint();
	}

	// https://stackoverflow.com/questions/11272938/how-to-save-panel-as-image-in-swing
	
	void askAboutSavingImage(ImagePanel ip, JDialog jd, boolean save) {
		if (!save) {
			jd.dispose();
			return;
		}
		int answer = MyOptionPane.showConfirmDialog(this, "Save image?", "Save image",
				MyOptionPane.YES_NO_CANCEL_OPTION);

		// boolean b = false;
		// final boolean SAVE_AS = true;
		if (answer == MyOptionPane.YES_OPTION) {
			
			int w = curDiag.maxX - curDiag.minX;
			//int h = curDiag.maxY - curDiag.minY;
			
			int h = curDiag.maxY + top_border_height + bottom_border_height;
			
			ip.setSize(new Dimension(w, h));     
			//jd.setSize(new Dimension(w, h));     
			
			File f = curDiag.genSave(null, langs[Lang.IMAGE], ip, null);
			// diag.diagLang = gl;
			Date date = new Date();
			// Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			if (f != null)
				f.setLastModified(date.getTime());
			jd.dispose();
			return;
		}

		if (answer == MyOptionPane.NO_OPTION) {
			// diag.diagLang = gl;
			jd.dispose();
		}
		return;

	}

	/*
	void setBlkType() {

		blkType = null;
		willBeSubnet = false;
		for (int i = 0; i < but.length; i++) {
			if (but[i] == selRB) {
				blkType = blockNames[i];
				break;
			}
		}
		//if (s.equals("Subnet")) {
		if (selRB == but[BUT_SUBNET]) {
			//blockType = Block.Types.PROCESS_BLOCK;
			willBeSubnet = true;
		}
	}
	
	*/

	void setNotation(Notation notn) {

		// curDiag.diagNotn = notn;
		// saveProp("defaultNotation", notn.label);
		currNotn = notn;
		jtf.setText("Diagram Notation: " + notn.label);
		jtf.repaint();

		/*
		menuItem1.setEnabled(true); 
		if (currNotn.lang == langs[Lang.JAVA])
			menuItem1.setText("Locate JavaFBP Jar File");
		else if (currNotn == notations[Notation.JSON])
			menuItem1.setText("Locate fbp.json File");
		else 
			menuItem1.setEnabled(false);

		menuItem2.setEnabled(true);
		if (currNotn.lang == langs[Lang.JAVA])
			menuItem2.setText("Add Additional Jar File");
		else if (currNotn.lang == langs[Lang.CSHARP])
			menuItem2.setText("Add Additional Dll File");
		else
			menuItem2.setEnabled(false);
		
		*/
		modMenuItems();

		filterOptions[0] = currNotn.lang.filter.getDescription();

		saveProp("defaultNotation", currNotn.label);
		saveProperties();
	//menuBar = createMenuBar(); 

		//setJMenuBar(menuBar);
		
		String u = "Generate "; 
		// if (curDiag != null)
		u += currNotn.label + " ";
		u += "Network";
		gNMenuItem.setText(u);
		//gNMenuItem.addActionListener(this);
		//fileMenu.add(gNMenuItem, 7);

		for (Block bk : curDiag.blocks.values()) {
			bk.component = null;
			bk.fullClassName = null;
			bk.codeFileName = null;
			bk.subnetFileName = null;
			bk.compName = null;
		}

		repaint();
	}

	// editType is false if no edit; true if block type determines type

	//Block createBlock(String blkType, int xa, int ya, Diagram diag, boolean editType) {
	Block createBlock(int xa, int ya, Diagram diag, boolean editType) {
		Block block = null;
		//boolean oneLine = false;
		String title = "";
		//if (blkType.equals(Block.Types.PROCESS_BLOCK)) {
		if (selRB == but[BUT_PROCESS] || selRB == but[BUT_SUBNET]) {	
			block = new ProcessBlock(diag);
			block.isSubnet = willBeSubnet;
		}

		//else if (blkType.equals(Block.Types.EXTPORT_IN_BLOCK) || blkType.equals(Block.Types.EXTPORT_OUT_BLOCK)
		//		|| blkType.equals(Block.Types.EXTPORT_OUTIN_BLOCK)) {
		else if (selRB == but[BUT_EXTPORT_IN] || selRB == but[BUT_EXTPORT_OUT] || selRB == but[BUT_EXTPORT_OI]) {		
			//oneLine = true;
			title = "External Port";
			block = new ExtPortBlock(diag);
		}

		else if (selRB == but[BUT_FILE]) {
			title = "File";
			block = new FileBlock(diag);
		}

		else if (selRB == but[BUT_IIP]) {
			//oneLine = true;
			title = "IIP";
			block = new IIPBlock(diag);
			// IIPBlock ib = (IIPBlock) block;
			// block.width = ib.width;
			// block.width = 60; // default
		}

		else if (selRB == but[BUT_LEGEND]) {
			title = "Legend";
			block = new LegendBlock(diag);
		}

		else if (selRB == but[BUT_ENCL]) {
			//oneLine = true;
			title = "Enclosure";
			block = new Enclosure(diag);
			Point pt = diag.area.getLocation();
			int y = Math.max(ya - block.height / 2, pt.y + 6);
			block.cy = ((ya + block.height / 2) + y) / 2;
		}

		else if (selRB == but[BUT_PERSON]) {
			title = "Person";
			//oneLine = true;
			block = new PersonBlock(diag);
		}

		else if (selRB == but[BUT_REPORT]) {
			title = "Report";
			block = new ReportBlock(diag);
		} 
		
		else
			return null;

		block.typeCode = selRB.code;

		block.cx = xa;
		block.cy = ya;
		if (block.cx == 0 || block.cy == 0)
			return null; // fudge!

		if (editType) {
			if (selRB.oneLine) {
				//if (blkType != Block.Types.ENCL_BLOCK) {
				if (selRB != but[BUT_ENCL]) {	
					// String d = "Enter description";
					String d = "Enter " + title + " text";
					String ans = (String) MyOptionPane.showInputDialog(this, "Enter text", d,
							MyOptionPane.PLAIN_MESSAGE, null, null, block.desc);

					if (ans == null)
						return null;

					else
						block.desc = ans;
				}
				
			} else if (!block.editDescription(CREATE))
				return null;

			//if (blkType.equals(Block.Types.IIP_BLOCK)) {  
			if (selRB == but[BUT_IIP]) {		
				IIPBlock ib = (IIPBlock) block;
				ib.desc = ib.checkNestedChars(block.desc);
				ib.width = ib.calcIIPWidth(osg);
				if (ib.width < 15)
					ib.width = 15;
				ib.buildSideRects();
			}
		}
		block.calcEdges();
		// diag.maxBlockNo++;
		// block.id = diag.maxBlockNo;
		diag.blocks.put(Integer.valueOf(block.id), block);
		// diag.changed = true;
		selBlock = block;
		// selArrowP = null;
		block.buildSideRects();
		return block;
	}

	void buildPropDescTable() {
		propertyDescriptions = new LinkedHashMap<String, String>();

		propertyDescriptions.put("Version #", "versionNo");
		propertyDescriptions.put("Date", "date");
		propertyDescriptions.put("Click To Grid", "clicktogrid");
		propertyDescriptions.put("Scaling Factor", "scalingfactor");
		propertyDescriptions.put("Sort By Date", "sortbydate");
		propertyDescriptions.put("Current C# source code directory", "currentCsharpSourceDir");
		propertyDescriptions.put("Current C# network code directory", "currentCsharpNetworkDir");
		propertyDescriptions.put("Current component class directory", "currentClassDir");
		propertyDescriptions.put("Source file directory", "srcDirProp");
		propertyDescriptions.put("Source file directory", "srcDir");
		propertyDescriptions.put("Network directory", "netDirProp");
		propertyDescriptions.put("Current diagram directory", "currentDiagramDir");
		propertyDescriptions.put("Current diagram", "currentDiagram");
		propertyDescriptions.put("Current image directory", "currentImageDir");
		propertyDescriptions.put("Current Java source code directory", "currentSourceDir");
		propertyDescriptions.put("Current Java source code directory", "currentJavaSourceDir");
		propertyDescriptions.put("Current Java network code directory", "currentJavaNetworkDir");
		propertyDescriptions.put("Current NoFlo source code directory", "currentNoFloSourceDir");
		propertyDescriptions.put("Current NoFlo network code directory", "currentNoFloNetworkDir");
		propertyDescriptions.put("Current .fbp notation directory", "currentFBPNetworkDir");
		propertyDescriptions.put("Current package name", "currentPackageName");
		propertyDescriptions.put("Font for code", "fixedFont");
		propertyDescriptions.put("Font for text", "generalFont");
		propertyDescriptions.put("Default font size", "defaultFontSize");
		propertyDescriptions.put("Default notation", "defaultNotation");
		propertyDescriptions.put("JavaFBP jar file", "javaFBPJarFile");
		// propertyDescriptions.put("DrawFBP Help jar file", "jhallJarFile");
		propertyDescriptions.put("Additional Jar Files", "additionalJarFiles");
		propertyDescriptions.put("Additional Dll Files", "additionalDllFiles");
		propertyDescriptions.put("Current folder for .exe files", "exeDir");
		propertyDescriptions.put("Current folder for .dll files", "dllDir");

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
		JScrollPane jsp = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
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

		// jf.setLocation(50, 50);
		panel.setBackground(Color.GRAY);
		// panel.setLocation(getX() + 50, getY() + 50);
		// panel.setLocation(50, 50);
		panel.setSize(1200, 800);
		// jsp.setLocation(50, 50);

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

		tft[0].setFont(fontg);
		tft[1].setFont(fontg);
		tft[2].setFont(fontf);
		tft[3].setFont(fontf);

		for (String p : propertyDescriptions.keySet()) {
			tft[0] = new JTextField(p);
			String q = propertyDescriptions.get(p);
			tft[1] = new JTextField(q);
			String u = properties.get(q);
			String w = "";
			// System.out.println(p + " " + q);
			int i;
			if (u == null)
				u = "";
			w = u;
			String v = startProperties.get(q);
			// if (v == null)
			// v = "(null)";
			// else
			if (v == null || v.equals(properties.get(q)))
				v = "";
			/*
			 * if (q.equals("defaultNotation")) { if (!(u.equals("(null)"))) { u =
			 * findNotnFromLabel(u).label; if (!(v.equals(""))) v =
			 * findNotnFromLabel(v).label; } continue; }
			 */

			if (q.equals("additionalJarFiles") || q.equals("additionalDllFiles")) {

				boolean done = false;
				// boolean first = true;

				while (!done) {

					v = "";
					if (u == null)
						u = "(null)";

					else {

						// first = false;
						// done = false;

						if ((i = w.indexOf(";")) > -1) {
							u = w.substring(0, i);
							w = w.substring(i + 1); // remainder
							// if (!done) {
							tft[2] = new JTextField(u);
							tft[3] = new JTextField(v);

							// tft[0].setFont(fontg);
							// tft[1].setFont(fontg);
							// tft[2].setFont(fontf);
							// tft[3].setFont(fontf);

							displayRow(gbc, gbl, tft, panel, Color.BLACK);
							tft[0] = new JTextField("");
							tft[1] = new JTextField("");
							// }

							// done = false;
						} else {
							u = w;
							done = true;
						}

					}
					// if (done)
					// break;
				}
			}
			tft[2] = new JTextField(u);
			tft[3] = new JTextField(v);

			// tft[0].setFont(fontg);
			// tft[1].setFont(fontg);
			// tft[2].setFont(fontf);
			// tft[3].setFont(fontf);

			displayRow(gbc, gbl, tft, panel, Color.BLACK);

			// if (done)
			// break;
			tft[0] = new JTextField("");
			tft[1] = new JTextField("");

		}

		// jsp.add(panel);
		jf.add(jsp);
		jf.pack();
		Point p = getLocation();
		jf.setLocation(p.x + 50, p.y + 50);
		// jf.setLocation(100, 100);
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
		sa.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(escapeKS, "CLOSE");
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
				MyOptionPane.showMessageDialog(this, "Directory '" + s + "' does not exist - reselect",
						MyOptionPane.ERROR_MESSAGE);
				// return null;
				f2 = new File(".");
			}

			MyFileChooser fc = new MyFileChooser(this, f2, langs[Lang.DIAGRAM], "Open Diagram");

			int returnVal = fc.showOpenDialog();

			if (returnVal == MyFileChooser.APPROVE_OPTION)
				file = new File(getSelFile(fc)); // xxx

			if (file == null)
				return file;
		}

		if (file.isDirectory()) {
			MyOptionPane.showMessageDialog(this, "File is directory: " + file.getAbsolutePath());
			return null;
		}

		if (null == getSuffix(file.getName()) && !(file.isDirectory())) {
			String name = file.getAbsolutePath();
			name += ".drw";
			file = new File(name);
		}
		if (!(file.exists())) {
			MyOptionPane.showMessageDialog(this, "File does not exist: " + file.getName(), MyOptionPane.ERROR_MESSAGE);
			return file;
		}

		if (null == (fileString = readFile(file /* , !SAVEAS */))) {
			MyOptionPane.showMessageDialog(this, "Unable to read file: " + file.getName(), MyOptionPane.ERROR_MESSAGE);
			return null;
		}

		File currentDiagramDir = file.getParentFile();
		saveProp("currentDiagramDir", currentDiagramDir.getAbsolutePath());

		diagramName = file.getAbsolutePath();
		saveProp("currentDiagram", diagramName);

		String s = diagramName;
		curDiag.area.removeMouseListener(curDiag.area);
		curDiag.area.removeMouseMotionListener(curDiag.area);
		if (s == null || s.endsWith(".drw")) {
			curDiag.area.addMouseListener(curDiag.area);
			curDiag.area.addMouseMotionListener(curDiag.area);
		}

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
			getNewDiag();
		curDiag.title = file.getName();

		if (curDiag.title.toLowerCase().endsWith(".drw"))
			curDiag.title = curDiag.title.substring(0, curDiag.title.length() - 4);

		curDiag.blocks.clear();
		curDiag.arrows.clear();
		curDiag.desc = " ";

		DiagramBuilder.buildDiag(fileString, this, curDiag);
		// jtp.setRequestFocusEnabled(true);
		// jtp.requestFocusInWindow();
		fpArrowEndA = null;
		fpArrowEndB = null;
		fpArrowRoot = null;
		currentArrow = null;
		foundBlock = null;
		// drawToolTip = false;
		blockSelForDragging = null;
		// if (curDiag.diagNotn != null)
		// changeLanguage(curDiag.diagNotn);

		fname = file.getName();
		curDiag.diagFile = file;

		// jtp.setSelectedIndex(curDiag.tabNum);
		// Notation notn = null;

		String suff = getSuffix(fname);

		// Notation notn = null;
		if (!(suff.equals("drw") || suff.equals("dr~"))) {
			Lang lang = findLangFromSuff(suff);
			CodeManager cm = new CodeManager(curDiag, CODEMGRCREATE); // does fbp have a Display form?
			cm.displayDoc(file, lang, null);
			return file;
		}
		// if (!(suff.equals("drw")) && !(suff.equals("dr~"))) {
		// notn = findNotnFromLang(suff);
		// CodeManager cm = new CodeManager(curDiag, CODEMGRCREATE); // do.
		// cm.displayDoc(file, notn, null);
		// return file;
		// }

		curDiag.title = fname;

		setTitle("Diagram: " + curDiag.title);
		if (curDiag.title.toLowerCase().endsWith(".drw"))
			curDiag.title = curDiag.title.substring(0, curDiag.title.length() - 4);
		// curDiag.tabNum = i;
		// jtp.setSelectedIndex(curDiag.tabNum);
		repaint();
		return file;
	}

	public String readFile(File file /* , boolean saveAs */) {
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
				MyOptionPane.showMessageDialog(this, "I/O Exception: " + file.getName(), MyOptionPane.ERROR_MESSAGE);
				// fileString = "";
			}

		} catch (FileNotFoundException e) {
			// if (!saveAs)
			// MyOptionPane.showMessageDialog(this, "File not found: "
			// + file.getName(), MyOptionPane.ERROR_MESSAGE);
			return null;
		} catch (IOException e) {
			MyOptionPane.showMessageDialog(this, "I/O Exception 2: " + file.getName(), MyOptionPane.ERROR_MESSAGE);
			return null;
		}
		return fileString;
	} // readFile

	// returns index of found tab; -1 if none

	int getFileTabNo(String fileName) {
		// int k = jtp.getSelectedIndex();

		int j = jtp.getTabCount();
		for (int i = 0; i < j; i++) {
			ButtonTabComponent b = (ButtonTabComponent) jtp.getTabComponentAt(i);
			if (b == null)
				continue;
			Diagram d = b.diag;
			if (d == null)
				continue;
			// if (i == k)
			// continue;
			File f = d.diagFile;
			if (f != null) {

				String t = f.getAbsolutePath();
				String u = fileName.replace("\\", "/");
				t = t.replace("\\", "/");
				if (t.endsWith(u)) {
					return i;
				}
			}
			// if (d.title != null && fileName.endsWith(d.title))
			// return i;
		}
		return -1;
	}

	String getSuffix(String s) {
		String s2 = s.replace("\\", "/");
		int i = s.lastIndexOf("/");
		if (i > -1)
			s2 = s.substring(0, i + 1);
		int j = s2.lastIndexOf(".");
		if (j == -1)
			return null;
		else
			return s2.substring(j + 1);
	}

	void saveAction(boolean saveAs) {

		// File file = null;
		// if (curDiag.diagFile == null)
		// saveAs = true;
		// if (!saveAs)
		File file = (!saveAs) ? curDiag.diagFile : null;

		file = curDiag.genSave(file, langs[Lang.DIAGRAM], null, this);

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
			curDiag.title = curDiag.title.substring(0, curDiag.title.length() - 4);
		saveProp("currentDiagramDir", currentDiagramDir.getAbsolutePath());
		saveProperties();

		curDiag.changed = false;
		repaint();
	}

	/**
	 * Use a BufferedWriter, which is wrapped around an OutputStreamWriter, which in
	 * turn is wrapped around a FileOutputStream, to write the string data to the
	 * given file.
	 */

	public boolean writeFile(File file, String fileString) {
		if (file == null || fileString == null)
			return false;
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
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
		String par = parent.replace('\\', '/');
		// if (par.endsWith("/"))
		// par = par.substring(0, par.length() - 1);

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
			int j = par.lastIndexOf("/");
			if (j == -1)
				return current;
			par = par.substring(0, j);
		}
		return par + "/" + cur.substring(k);
	}

	public static BufferedImage readImageFromFile(File file) throws IOException {
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

			jfl.setText("Fixed font: " + fixedFont + "; general font: " + generalFont);
			fontg = new Font(generalFont, Font.PLAIN, (int) defaultFontSize);
			adjustFonts();
			repaint();
			// repaint();
			redrawVarBlocks();
		}

		if (fFontChanged) {
			saveProp("fixedFont", fixedFont);
			saveProperties();

			jfl.setText("Fixed font: " + fixedFont + "; general font: " + generalFont);
			fontf = new Font(fixedFont, Font.PLAIN, (int) defaultFontSize);
			adjustFonts();
			repaint();
			// repaint();
			redrawVarBlocks();
		}

		return;
	}

	void redrawVarBlocks() {
		for (Block b : curDiag.blocks.values()) {
			if (b.typeCode.equals(Block.Types.IIP_BLOCK) || b.typeCode.equals(Block.Types.LEGEND_BLOCK)) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						curDiag.area.repaint();
					}
				});
			}

		}
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

		// Float[] selectionValues = {new Float(10), new Float(12), new Float(14),
		// new Float(16), new Float(18), new Float(20), new Float(22)};
		Float[] selectionValues = new Float[7];
		for (int i = 0; i < selectionValues.length; i++) {
			selectionValues[i] = Float.valueOf(i * 2.0f + 10);
		}
		int j = 0;
		for (int i = 0; i < selectionValues.length; i++) {
			if (Float.compare(selectionValues[i].floatValue(), defaultFontSize) == 0)
				j = i;
		}
		Float fs = (Float) MyOptionPane.showInputDialog(this, "Font size dialog", "Select a font size",
				MyOptionPane.PLAIN_MESSAGE, null, selectionValues, selectionValues[j]);
		if (fs == null)
			return;

		defaultFontSize = fs.floatValue();
		fontg = fontg.deriveFont(fs);
		fontf = fontf.deriveFont(fs);
		jfs.setText("Font Size: " + defaultFontSize);
		saveProp("defaultFontSize", Float.toString(defaultFontSize));
		adjustFonts();
		// repaint();		
		saveProperties();
		MyOptionPane.showMessageDialog(this, "Font size changed");
		repaint();
		// repaint();
	}

	void adjustFonts() {
		//fileMenu = new JMenu(" File ");
		//diagMenu = new JMenu(" Diagram ");
		//helpMenu = new JMenu(" Help ");
		// runMenu = new JMenu(" Run ");

		int j = jtp.getTabCount();
		for (int i = 0; i < j; i++) {
			ButtonTabComponent b = (ButtonTabComponent) jtp.getTabComponentAt(i);
			if (b == null || b.diag == null)
				return;
			b.label.setFont(fontf);
		}
		jtp.repaint();
		String dfs = properties.get("defaultFontSize");
		if (dfs == null) {
			defaultFontSize = 14.0f;
			dfs = "14.0";
		} else
			defaultFontSize = Float.parseFloat(dfs);
		
		saveProp("defaultFontSize", dfs);
		
		fontg = new Font(generalFont, Font.PLAIN, (int) defaultFontSize);
		fontg = fontg.deriveFont(defaultFontSize);
		
		fontf = new Font(fixedFont, Font.PLAIN, (int) defaultFontSize);
		fontf = fontf.deriveFont(defaultFontSize);
		
		osg.setFont(fontg);
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
		diagMenu.setFont(fontg);
		helpMenu.setFont(fontg);
		// runMenu.setFont(fontg);
		diagDesc.setFont(fontg);
		
		int ct = fileMenu.getItemCount(); 		
		for (int i = 0; i < ct; i++) {
			JMenuItem jmi = fileMenu.getItem(i);
			if (jmi instanceof JMenuItem)
				jmi.setFont(fontg);			
		}
		
		ct = diagMenu.getItemCount(); 		
		for (int i = 0; i < ct; i++) {
			JMenuItem jmi = fileMenu.getItem(i);
			if (jmi instanceof JMenuItem)
				jmi.setFont(fontg);			
		}
		
		ct = helpMenu.getItemCount(); 		
		for (int i = 0; i < ct; i++) {
			JMenuItem jmi = fileMenu.getItem(i);
			if (jmi instanceof JMenuItem)
				jmi.setFont(fontg);			
		}
		
		for (int i = 0; i < gMenu.length; i++) {
			gMenu[i].setFont(fontg);
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
		
        SwingUtilities.updateComponentTreeUI(DrawFBP.this);
        
		/*
		 * for (Object item : ht.keySet()) { UIManager.put(item, fontg); //
		 * System.out.println(item + " - " + fontg); }
		 */
		// UIManager.put("Button.select", slateGray1);

		//menuBar = createMenuBar();

		//setJMenuBar(menuBar);

		repaint();
		validate();
	}

	final boolean SAVEAS = true;

	void compileCode() {

		File cFile = null;
		// Notation notn = curDiag.diagNotn;
		Process proc = null;
		// String program = "";
		// interrupt = false;
		// String cMsg = null;
		if (!(currNotn.lang == langs[Lang.JAVA]) && !(currNotn.lang == langs[Lang.CSHARP])) {
			MyOptionPane.showMessageDialog(this, "Language not supported: " + currNotn.lang,
					MyOptionPane.ERROR_MESSAGE);
			return;
		}

		// if (curDiag.changed) {
		// cMsg = "Select a " + currLang.label + " source file - if diagram has changed,
		// \n invoke 'File/Generate " + currLang.label + " Network' first";

		// }

		// cMsg = "Select a " + currLang.label + " source file";

		// MyOptionPane.showMessageDialog(this, cMsg, MyOptionPane.INFORMATION_MESSAGE);

		// if (currLang.label.equals("Java")) {
		String ss = properties.get(currNotn.netDirProp);
		String srcDir = null;
		if (ss == null)
			srcDir = System.getProperty("user.home");
		else {
			ss = ss.replace("\\", "/");
			String st = ss.substring(ss.lastIndexOf("/") + 1);
			if (0 < st.lastIndexOf("."))
				srcDir = System.getProperty("user.home");
			else
				srcDir = ss;
		}

		MyFileChooser fc = new MyFileChooser(this, new File(srcDir), currNotn.lang, "Compile Network");

		int returnVal = fc.showOpenDialog();

		cFile = null;
		if (returnVal == MyFileChooser.APPROVE_OPTION) {
			cFile = new File(getSelFile(fc));
		}
		// }
		if (cFile == null || !(cFile.exists())) {

			MyOptionPane.showMessageDialog(this, "No file selected or file does not exist", MyOptionPane.ERROR_MESSAGE);
			return;
		}

		srcDir = cFile.getAbsolutePath();
		srcDir = srcDir.replace('\\', '/');

		int j = srcDir.lastIndexOf("/");
		progName = srcDir.substring(j + 1);
		srcDir = srcDir.substring(0, j);
		// String clsDir = srcDir;
		// (new File(srcDir)).mkdirs();
		saveProp(currNotn.netDirProp, srcDir);

		if (currNotn.lang == langs[Lang.JAVA]) {
			String fNPkg = "";
			int k = srcDir.indexOf("/src/");
			if (k == -1) {
				k = srcDir.length() - 4;
				if (!(srcDir.substring(k).equals("/src"))) { // folder name starting with "src" would not work!
					MyOptionPane.showMessageDialog(this,
							"File name '" + srcDir + "' - file name should contain 'src' - cannot compile",
							MyOptionPane.ERROR_MESSAGE);
					return;
				}
			}

			if (j >= k + 5) {
				// fNPkg = cFile.getAbsolutePath().substring(k + 5, j)/* + "/" */ ;
				String fileString = readFile(cFile);
				fNPkg = getPackageFromCode(fileString);

				// if (null == packageName) {
				// }
				// }
				if (fNPkg != null)
					fNPkg = fNPkg.replace(".", "/");
			}
			String clsDir = srcDir.replace("/src", "/bin");
			// srcDir = srcDir.substring(0, k + 4); // drop after src
			// clsDir = clsDir.substring(0, k + 4); // drop after bin

			// (new File(clsDir)).mkdir();

			saveProp("currentClassDir", clsDir);
			// clsDir = clsDir.substring(0, k + 4);

			File fd = new File(clsDir);

			fd.mkdirs();

			if (fd == null || !fd.exists()) {
				MyOptionPane.showMessageDialog(this, "'bin' directory does not exist - " + clsDir,
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

			saveProp("currentClassDir", clsDir);
			/*
			 * if (!(fNPkg.equals(""))) { fd = new File(clsDir + "/" + fNPkg); fd.mkdirs();
			 * if (fd == null || !fd.exists()) { MyOptionPane.showMessageDialog(this,
			 * "Directory '" + clsDir + "/" + fNPkg + "' does not exist",
			 * MyOptionPane.ERROR_MESSAGE); return;
			 * 
			 * } }
			 * 
			 */

			if (javaFBPJarFile == null)
				locateJavaFBPJarFile(false);

			proc = null;

			String delim = null;
			if (System.getProperty("os.name").startsWith("Windows"))
				delim = ";";
			else
				delim = ":";

			srcDir = srcDir.replace("\\", "/");
			// clsDir = clsDir.replace("\\", "/");

			String jh = System.getenv("JAVA_HOME");
			if (jh == null) {
				MyOptionPane.showMessageDialog(this, "Missing JAVA_HOME environment variable",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}
			if (-1 == jh.indexOf("jdk")) {
				MyOptionPane.showMessageDialog(this,
						"To do Java compiles, JAVA_HOME environment variable must point at JDK",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

			String javac = null;
			if (System.getProperty("os.name").startsWith("Windows"))
				javac = jh + "/bin/javac.exe";
			else
				javac = jh + "/bin/javac";

			// Switch to JavaCompiler javac = ToolProvider.getSystemJavaCompiler();

			// if (fNPkg != null && !(fNPkg.trim().equals("")))
			// srcDir += "/" + fNPkg;

			srcDir = srcDir.replace("\\", "/");
			clsDir = srcDir.replace("/src", "/bin");

			int m = clsDir.indexOf(fNPkg);
			if (m == -1)
				m = clsDir.indexOf("bin/") + 4;
			String clsDirTr = clsDir.substring(0, m);

			(new File(clsDirTr)).mkdirs();

			String jf = "\"" + javaFBPJarFile;
			for (String jfv : jarFiles) {
				if (!(new File(jfv).exists())) {
					MyOptionPane.showMessageDialog(driver,
							"Jar file does not exist: " + jfv, MyOptionPane.ERROR_MESSAGE);
					return;
				}
				jf += delim + jfv;
			}
			jf += delim + clsDirTr;
			jf += delim + ".\"";

			String w = srcDir + "/" + progName;
			List<String> params = Arrays.asList("\"" + javac + "\"",
					// "-verbose",
					"-cp", jf, "-d", "\"" + clsDirTr + "\"", "\"" + w + "\"");

			ProcessBuilder pb = new ProcessBuilder(params);

			pb.directory(new File(clsDir));

			pb.redirectErrorStream(true);

			output = "";

			// new WaitWindow(this); // display "Processing..." message

			// int i = 0;
			String err = "";

			try {
				proc = pb.start();

				BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					// System.out.println(line);
					output += "<br>" + line;
					// System.out.flush();
				}
			} catch (Exception e) {
				err = analyzeCatch(e);
				if (!err.equals(""))
					proc = null;
			}

			if (proc == null) {
				MyOptionPane.showMessageDialog(this, "Compile process failed", MyOptionPane.WARNING_MESSAGE);
				return;
			}

			if (srcDir.endsWith("/"))
				srcDir = srcDir.substring(0, srcDir.length() - 1);

			String fontFamily = fontf.getFamily();
			String s = "<html><body style=\"font-family: " + fontFamily + "\"> <p>Compile output for " + "\"" + srcDir
					+ "/" + progName + "\"<br>" + err + /* output + */ "<br>" + "Jar files: <br> ";
			s += "&nbsp;&nbsp;" + javaFBPJarFile + "<br>";
			if (jarFiles != null) {
				for (String jfv : jarFiles) {
					s += "&nbsp;&nbsp;" + jfv + "<br>";
				}
			}

			s += "<br><br>";
			String cls = progName;
			if (progName.endsWith(".java"))
				cls = progName.substring(0, progName.length() - 5);

			s += "Source dir: " + srcDir + "<br>" + "Class dir: " + clsDir + "<br>" + "File name: " + srcDir + "/"
					+ progName + "<br>" + "Class file name: " + clsDir + "/" + cls + ".class";

			try {
				proc.waitFor();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			s += "<br><br>";

			int u = proc.exitValue();

			if (u == 0) {
				s += "Program compiled and linked - \"" + srcDir + "/" + progName + "\"<br>"
						+ "&nbsp;&nbsp;&nbsp;into - \"" + clsDir + "/" + progName.substring(0, progName.length() - 5)
						+ ".class\"";
				saveProp("currentClassDir", clsDir);
			} else {
				s += "Program compile failed, rc: " + u + " - \"" + srcDir + "/" + progName + "\"<br>"
						+ "&nbsp;&nbsp;&nbsp;errcode: " + err + "<br>" + output;
			}
			s += "</html>";

			// JFrame jf2 = new JFrame();
			JDialog jf2 = new JDialog(this, Dialog.ModalityType.APPLICATION_MODAL);
			// JDialog jf2 = new JDialog(this);
			// jf2.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
			JEditorPane jep = new JEditorPane(/* "text/plain", */ "text/html", " ");
			jep.setEditable(false);
			JScrollPane jsp = new JScrollPane(jep);
			jf2.add(jsp);
			jsp.setViewportView(jep);
			// jf2.setVisible(true);
			jf2.setTitle("Compile Output");
			jep.setText(s);

			jf2.setLocation(200, 200);
			jf2.setSize(800, 500);
			// jf2.setAlwaysOnTop(true);
			jep.setFont(fontf);
			jf2.pack();
			jf2.setVisible(true);

			// return;
			// }

			// int u = 0;

			// interrupt = true;

			proc.destroy();
			// u = proc.exitValue();

			// if (fNPkg != null && !(fNPkg.trim().equals("")))
			// clsDir += "/" + fNPkg;

			/*
			 * if (u == 0) MyOptionPane.showMessageDialog(this, "Program compiled - " +
			 * srcDir + "/" + progName + "\n" + "   into - " + clsDir + "/" +
			 * progName.substring(0, progName.length() - 5) + ".class",
			 * MyOptionPane.INFORMATION_MESSAGE); else MyOptionPane.showMessageDialog(this,
			 * "<html>Program compile failed, rc: " + u + " - " + srcDir + "/" + progName +
			 * "<br>" + output + "</html>", MyOptionPane.WARNING_MESSAGE);
			 * 
			 */

		}

		else {

			// Must be C#

			// if (!(currLang.label.equals("C#"))) {

			// Start of C# part...

			srcDir = properties.get("currentCsharpNetworkDir");

			ss = cFile.getAbsolutePath();
			if (!(ss.endsWith(".cs"))) {
				MyOptionPane.showMessageDialog(this, "C# program " + ss + " must end in '.cs'",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

			ss = ss.replace('\\', '/');
			ss = ss.replace("/", File.separator);

			String progString = readFile(cFile /* new File(ss), !SAVEAS */);
			if (progString == null) {
				MyOptionPane.showMessageDialog(this, "Program not found: " + ss, MyOptionPane.ERROR_MESSAGE);
				return;
			}
			//String t = "";
			String v = "";
			 
			int i = ss.lastIndexOf(File.separator);
			srcDir = ss.substring(0, i);
			j = ss.lastIndexOf(".cs");
			v = ss.substring(i + 1, j);
			
			
			
			int k = progString.indexOf("namespace ");
			if (k > -1) {
				k += 10; // skip over "namespace"
				int ks = k;

				while (true) {
					if (progString.substring(k, k + 1).equals(" ") || progString.substring(k, k + 1).equals("{")
							|| progString.substring(k, k + 1).equals("\r")
							|| progString.substring(k, k + 1).equals("\n"))
						break;
					k++;
				}

				v = progString.substring(ks, k); // get name of
													// namespace
				v = v.replace(".", "/");

			}

			// String trunc = ss.substring(0, ss.lastIndexOf("/"));
			String trunc = srcDir;
			String progName = ss.substring(ss.lastIndexOf(File.separator) + 1);

			File f = new File(trunc);
			f.mkdirs();
			// if (f == null || !f.exists()) {
			// MyOptionPane.showMessageDialog(this,
			// "'bin' directory does not exist - " + f.getAbsolutePath(),
			// MyOptionPane.ERROR_MESSAGE);
			// return;
			// }

			saveProp("currentCsharpNetworkDir", trunc);

			/*
			//ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "cd '" + trunc + "' && dir");
			ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "cd '" + trunc + "'");
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

			*/
			//System.out.println("cd : " + trunc);
			String target =  trunc + File.separator +  "bin\\Debug"; // we've done a cd, so we don't need trunc

			File f2 = new File(target);
			f2.mkdirs();
			if (f2 == null || !f2.exists()) {
				MyOptionPane.showMessageDialog(this, "'bin' directory does not exist - " + f2.getAbsolutePath(),
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

			// MyOptionPane.showMessageDialog(this,
			// "Starting compile - " + ss,
			// MyOptionPane.INFORMATION_MESSAGE);

			proc = null;
			progName = progName.substring(0, progName.length() - 3); // drop .cs

			String z = properties.get("additionalDllFiles");
			boolean gotDlls = -1 < z.indexOf("FBPLib.dll") && -1 < z.indexOf("FBPVerbs.dll");

			List<String> cmdList = new ArrayList<String>();
			cmdList.add("csc");
			cmdList.add("-t:exe");
			//t = t.replace("\\", "/");
			//t = t.replace("/", ".");
			target = target.replace("/", File.separator);
			cmdList.add("-out:\"" + target + File.separator + v + ".exe\"");
			//exeDir = trunc + File.separator + target;
			exeDir = target;

			if (!gotDlls /*&& !gotDllReminder */) {
				MyOptionPane.showMessageDialog(this,
						"If you are using FBP, you will need a FBPLib dll and a FBPVerbs dll - use File/Add Additional Dll File for each one",
						MyOptionPane.WARNING_MESSAGE);
				//gotDllReminder = true;
				return;
			}

					
			for (String thisEntry : dllFiles) {
				String w = thisEntry;
				if (!(new File(w).exists())) {
					MyOptionPane.showMessageDialog(driver, "Dll file does not exist: " + w, MyOptionPane.ERROR_MESSAGE);
					return;
				}
				w = w.replace("\\", "/");
				j = w.indexOf("bin/Debug");
				// cmdList.add("-r:\"" + w.substring(j) + "\"");
				cmdList.add("-r:\"" + thisEntry + "\"");
			}
					  
			//}
			// String w = "\"" + trunc + "/" + "*.cs\"";
			ss = ss.replace("\\", "/");
			cmdList.add(/* trunc + "/" + */ progName + ".cs");
			
			//for (String s: cmdList) 
			//	System.out.println(s);

			ProcessBuilder pb = new ProcessBuilder(cmdList);
			for (int m = 0; m < cmdList.size(); m++)
				System.out.println(cmdList.get(m));

			pb.directory(new File(trunc));

			pb.redirectErrorStream(true);
			// MyOptionPane.showMessageDialog(this,
			// "Compiling program - " + srcDir + "/" + v + progName,
			// MyOptionPane.INFORMATION_MESSAGE);

			// new WaitWindow(this); // display "Processing..." message

			String err = "";
			output = "";
			try {
				proc = pb.start();

				BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					output += line + "<br>";
				}
			} catch (Exception e) {
				err = analyzeCatch(e);
				if (!err.equals(""))
					proc = null;
			}

			if (proc == null) {
				MyOptionPane.showMessageDialog(this, "Compile process failed", MyOptionPane.WARNING_MESSAGE);
				return;
			}

			// interrupt = true;
			// program = v + "/" + progName + ".cs";
			int u = 0;
			// if (!(output.equals("")) || !(err.equals(""))) {
			// MyOptionPane
			// .showMessageDialog(this,
			// "<html>Compile output for " + target + "/" + v + ".exe <br>" +
			// err + "<br>" + output + "</html>",
			// MyOptionPane.ERROR_MESSAGE);
			// return;
			// }

			try {
				proc.waitFor();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String fontFamily = fontf.getFamily();
			String s = "<html><body style=\"font-family: " + fontFamily + "\"> <p> Compile output for " + "\"" + srcDir
					+ "/" + progName + ".cs\"<br>" + err + /* output + */ "<br>";

			if (dllFiles != null) {
				s += "Dll files: <br>";
				for (String dlv : dllFiles) {
					s += "&nbsp;&nbsp;&nbsp; \"" + dlv + "\"<br>";
				}
				s += "<br>";
			}
			exeDir = exeDir.replace("/", File.separator);
			s += "Source dir: \"" + srcDir + "\"<br>" + "Exe dir: \"" + exeDir + "\"<br>" + "File name: \"" + srcDir
					+ File.separator + progName + ".cs\"<br>" + "Output file: \"" + exeDir + File.separator + v + ".exe\"";

			s += "<br><br>";

			u = proc.exitValue();

			if (u == 0) {
				s += "Programs compiled and linked - \"" + trunc + File.separator +
						progName + ".cs\"<br>" + "&nbsp;&nbsp;&nbsp;into - \""
						+ trunc + "/bin/Debug/" + v + ".exe\"";
				saveProp("exeDir", trunc);
			} else {
				s += "Program compile failed, rc: " + u + " - \"" + trunc + File.separator +
						progName + ".cs\"<br>"
						+ "&nbsp;&nbsp;&nbsp;errcode: " + err + "<br>" + output;
			}
			s += "</html>";
			// JFrame jf2 = new JFrame();
			JDialog jf2 = new JDialog(this, Dialog.ModalityType.APPLICATION_MODAL);
			JEditorPane jep = new JEditorPane(/* "text/plain", */ "text/html", " ");
			jep.setEditable(false);
			JScrollPane jsp = new JScrollPane(jep);
			jf2.add(jsp);
			jsp.setViewportView(jep);

			jf2.setTitle("Compile Output");
			jep.setText(s);

			jf2.setLocation(200, 200);
			jf2.setSize(800, 500);
			// jf2.setAlwaysOnTop(true);
			jep.setFont(fontf);

			jf2.pack();
			jf2.setVisible(true);

			// proc.destroy();

			// u = proc.exitValue();

			// if (u == 0) {

			// MyOptionPane.showMessageDialog(this,
			// "Programs compiled and linked - " + trunc + "/"
			// + "*.cs\n" + " into - " + trunc + "/bin/Debug/" + v + ".exe",
			// MyOptionPane.INFORMATION_MESSAGE);
			// saveProp("exeDir", trunc) ;
			// }
			// else
			// MyOptionPane.showMessageDialog(this,
			// "<html>Program compile failed, rc: " + u + " - " + trunc + "/*.cs" + "<br>" +
			// "errcode: " + err + "<br>" +
			// output + "</html>" ,
			// MyOptionPane.WARNING_MESSAGE);
			proc.destroy();

		}

	}

	void runCode() {

		File cFile = null;
		// program = "";
		// Process proc = null;
		// interrupt = false;

		if (currNotn.lang == langs[Lang.JAVA]) {

			String ss = properties.get("currentClassDir");
			String clsDir = null;
			if (ss == null)
				clsDir = System.getProperty("user.home");
			else
				clsDir = ss;

			// String savePrompt = curDiag.fCParm[Diagram.CLASS].prompt;
			// curDiag.fCParm[Diagram.CLASS].prompt = "Select program to be run from class
			// directory or jar file";
			MyFileChooser fc = new MyFileChooser(this, new File(clsDir), langs[Lang.CLASS], "Run Java Network");

			int returnVal = fc.showOpenDialog();
			// curDiag.fCParm[Diagram.CLASS].prompt = savePrompt;

			cFile = null;
			if (returnVal == MyFileChooser.APPROVE_OPTION) {
				ss = getSelFile(fc);
				cFile = new File(ss);
			}
			// }
			if (cFile == null || !(cFile.exists())) {
				MyOptionPane.showMessageDialog(this, "No file selected or file does not exist",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

			// if (currLang.label.equals("Java")) {
			if (!(ss.endsWith(".class"))) {
				MyOptionPane.showMessageDialog(this, "Executable " + ss + " must end in '.class'",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

			ss = ss.replace('\\', '/');
			int j = ss.lastIndexOf("/");

			saveProp("currentClassDir", ss.substring(0, j));

			progName = ss.substring(j + 1);

			// if (currLang.label.equals("Java"))

			if (javaFBPJarFile == null)
				locateJavaFBPJarFile(false);

			Class<?> cls = null;

			/*
			 * URL[] urls = buildUrls(ssPlus);
			 * 
			 * URLClassLoader loader = null; Class<?> cls = null; if (urls != null) {
			 * 
			 * // Create a new class loader with the directory loader = new
			 * URLClassLoader(urls, this.getClass().getClassLoader());
			 * 
			 * //try { // cls = loader.loadClass(thisCls); // cls =
			 * loader.loadClass(progName); //} catch (ClassNotFoundException e1) { // TODO
			 * Auto-generated catch block // e1.printStackTrace(); //} }
			 */

			int k = ss.indexOf("/bin");
			String t = "";
			if (k > -1) {
				String st = ss.replace("/bin", "/src");
				st = st.replace(".class", ".java");
				File f = new File(st);
				String stc = readFile(f);

				String pkg = getPackageFromCode(stc);
				pkg = pkg.replace(".", "/");

				j = ss.indexOf(pkg);
				t = cFile.getAbsolutePath().substring(j);
				t = t.replace("\\", "/");
				int n = t.lastIndexOf("/");
				t = t.substring(0, n);
				String u = ss.substring(0, j);

				clsDir = u; // drop before package
			}

			ss = ss.substring(0, ss.length() - 6); // drop .class suffix
			// clsDir.mkdirs();
			if (clsDir == null || !(new File(clsDir)).exists()) {
				MyOptionPane.showMessageDialog(this, "Run class directory does not exist - " + clsDir,
						MyOptionPane.ERROR_MESSAGE);
				return;
			}
			saveProp("currentClassDir", clsDir);

			progName = progName.substring(0, progName.length() - 6);
			if (!(t.equals("")))
				progName = t.replace("\\", "/") + "/" + progName;
			progName = progName.replace("/", ".");

			if (javaFBPJarFile == null)
				locateJavaFBPJarFile(false);

			URL[] urls = buildUrls(new File(clsDir));

			URLClassLoader loader = null;
			// Class<?> cls = null;
			if (urls != null) {

				// Create a new class loader with the directory
				loader = new URLClassLoader(urls, getClass().getClassLoader());

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
				MyOptionPane.showMessageDialog(this, "Class not generated for program " + progName,
						MyOptionPane.ERROR_MESSAGE);
				return;
			}
			Method meth = null;
			try {
				meth = cls.getMethod("main", String[].class);
			} catch (NoSuchMethodException e2) {
				meth = null;
				MyOptionPane.showMessageDialog(this, "Program \"" + progName + "\" has no 'main' method",
						MyOptionPane.ERROR_MESSAGE);
			} catch (SecurityException e2) {
				meth = null;
				MyOptionPane.showMessageDialog(this, "Program \"" + progName + "\" has no 'main' method",
						MyOptionPane.ERROR_MESSAGE);
			}
			if (meth == null) {

				return;
			}

			// if(javaFBPJarFile == null)
			// locateJavaFBPJarFile();

			String jh = System.getenv("JAVA_HOME");
			if (jh == null) {
				MyOptionPane.showMessageDialog(/* this */ this, "Missing JAVA_HOME environment variable",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}
			if (-1 == jh.indexOf("jdk") && -1 == jh.indexOf("jre")) {
				MyOptionPane.showMessageDialog(/* this */ this,
						"To run Java commmand, JAVA_HOME environment variable must point at JDK or JRE",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}
			String java = jh + "/bin/java";
			String delim = "";
			if (System.getProperty("os.name").startsWith("Windows"))
				delim = ";";
			else
				delim = ":";

			String jf = "\"" + javaFBPJarFile;
			for (String jfv : jarFiles) {
				jfv = jfv.replace("\\", "/");
				jf += delim + jfv;
			}

			String progName2 = progName.substring(0, progName.lastIndexOf(".")); // drop program name
			jf += delim + clsDir + progName2.replace(".", "/");
			pBCmdArray = new String[] { java, "-cp", jf + ";.\"", "-Xdiag", "\"" + progName + "\"" };

			// int m = clsDir.indexOf("/bin");
			// pBDir = clsDir.substring(0, m + 4) + "/";
			pBDir = clsDir;

			Thread runthr = new Thread(new RunTask());
			runthr.start();
			//run();

			// program = clsDir + "/" + progName;
			// + ".class";
		}

		else {

			if (currNotn.lang != langs[Lang.CSHARP]) {

				MyOptionPane.showMessageDialog(this, "Language not supported: " + currNotn.lang,
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

			exeDir = properties.get("exeDir");
			if (exeDir == null)
				exeDir = System.getProperty("user.home");

			// ProcessBuilder pb = null;
			MyFileChooser fc = new MyFileChooser(this, new File(exeDir), langs[Lang.EXE], "Run EXE File");

			int returnVal = fc.showOpenDialog();

			cFile = null;
			String exeFile = "";
			if (returnVal == MyFileChooser.APPROVE_OPTION) {
				exeFile = getSelFile(fc);
			}

			if (!(exeFile.endsWith(".exe"))) {
				MyOptionPane.showMessageDialog(this, "Executable " + exeFile + " must end in '.exe'",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

			exeFile = exeFile.replace("\\", "/");
			int k = exeFile.lastIndexOf("bin/Debug/");
			exeDir = exeFile.substring(0, k + 10);

			saveProp("exeDir", exeDir);

			// exeFile = exeFile.replace("\\", "/");

			// List<String> cmdList = new ArrayList<String>();

			// cmdList.add("\"" + exeFile + "\"");
			// cmdList.add(exeFile);

			progName = exeFile.substring(exeFile.lastIndexOf("/") + 1);

			/// ProcessBuilder pb = new ProcessBuilder(pBCmdArray);

			/// pb.directory(new File(pBDir));

			pBCmdArray = new String[1];
			pBCmdArray[0] = exeFile;

			pBDir = exeDir;

			Thread runthr = new Thread(new RunTask());
			runthr.start();
			//run();
			
		}
	}

	
	
	
	String getPackageFromCode(String data) {
		String pkg = null;
		// int lineNo = 0;
		int errNo = 0;
		BabelParser2 bp = new BabelParser2(data, errNo);

		while (true) {
			while (true) {
				if (!bp.tb('o'))
					break;
			}

			if (bp.tc('/', 'o')) {
				if (bp.tc('*', 'o')) {
					while (true) {
						if (bp.tc('*', 'o') && bp.tc('/', 'o'))
							break;
						bp.tu('o');
					}
					continue;
				} else
					bp.bsp(); // back up one character bc one slash has been consumed
			}

			if (bp.tc('/', 'o') && bp.tc('/', 'o')) {
				while (true) {
					if (bp.tc('\r', 'o')) {
						bp.tc('\n', 'o');
						break;
					}
					if (bp.tc('\n', 'o')) {
						bp.tc('\r', 'o');
						break;
					}
					bp.tu('o');
				}
				continue;
			}
			// else

			if (bp.tc('\r', 'o'))
				continue;
			if (bp.tc('\n', 'o'))
				continue;
			break;
		}

		if (bp.tc('p', 'o') && bp.tc('a', 'o') && bp.tc('c', 'o') && bp.tc('k', 'o') && bp.tc('a', 'o')
				&& bp.tc('g', 'o') && bp.tc('e', 'o')) {

			while (true) {
				if (!bp.tb('o'))
					break;
			}
			while (true) {
				if (bp.tc(';', 'o') || bp.tb('o'))
					break;
				bp.tu();
			}
		}
		pkg = bp.getOutStr();
		if (pkg != null)
			pkg = pkg.trim();
		return pkg;
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

		e.printStackTrace();

		return err;
	}

	void compare(int tabNo) {

		// comparing = false;
		LinkedList<String> mismatches = new LinkedList<String>();

		int i = tabNo;
		if (i == -1) {
			// comparing = false;
			return;
		}

		ButtonTabComponent b = (ButtonTabComponent) jtp.getTabComponentAt(i);
		if (b == null || b.diag == null) {
			// comparing = false;
			return;
		}
		// curDiag = b.diag;
		Diagram oldDiag = b.diag;

		Diagram newDiag = curDiag;

		// Diagram oldDiag = null;

		File f = newDiag.diagFile;
		if (f == null) {
			// comparing = false;
			return;
		}

		int result = MyOptionPane.showConfirmDialog(this,
				"Comparing " + newDiag.diagFile.getAbsolutePath() + " against " + oldDiag.diagFile.getAbsolutePath(),
				"Comparing", MyOptionPane.OK_CANCEL_OPTION);

		if (result != MyOptionPane.OK_OPTION)
			return;

		if (!strEqu(oldDiag.desc, newDiag.desc)) {
			String cd = new String("Diagram descriptions different: \n" + "   This value:   " + newDiag.desc + "\n"
					+ "   Other value: " + oldDiag.desc + "\n\n");

			mismatches.add(cd);
		}

		for (Block nb : newDiag.blocks.values()) {
			nb.compareFlag = null;

			for (Block ob : oldDiag.blocks.values()) {
				if (strEqu(nb.desc, ob.desc)) {

					ob.compareFlag = "N";
					nb.compareFlag = "N";

					if ((Math.abs(ob.cx - nb.cx) > 10) || (Math.abs(ob.cy - nb.cy) > 10))
						nb.compareFlag = "M";

					if (!(strEqu(nb.typeCode, ob.typeCode))) {
						nb.compareFlag = "C";
						mismatches.add(new String("This Block Type for '" + cleanDesc(nb, false) + ": " + nb.typeCode + "\n"
								+ "   Other value: " + ob.typeCode + "\n\n"));
					}

					if (!(strEqu(nb.fullClassName, ob.fullClassName))) {
						nb.compareFlag = "C";
						mismatches.add(new String(
								"This Full Class Name for '" + cleanDesc(nb, false) + ": \n                      "
										+ nb.fullClassName + "\n" + "   Other value: " + ob.fullClassName + "\n\n"));

					}
					if (!(strEqu(nb.codeFileName, ob.codeFileName))) {
						nb.compareFlag = "C";
						mismatches.add(new String("This Code File Name for '" + cleanDesc(nb, false) + ": "
								+ nb.codeFileName + "\n" + "   Other value: " + ob.codeFileName + "\n\n"));
					}
					if (!(strEqu(nb.subnetFileName, ob.subnetFileName))) {
						nb.compareFlag = "C";
						mismatches.add(new String("This Subnet File Name for '" + cleanDesc(nb, false) + ": "
								+ nb.subnetFileName + "\n" + "   Other value: " + ob.subnetFileName + "\n\n"));
					}
					if (nb.isSubnet != ob.isSubnet) {
						nb.compareFlag = "C";
						mismatches.add(new String("This Subnet flag for '" + cleanDesc(nb, false) + ": " + nb.isSubnet
								+ "\n" + "   Other value: " + ob.isSubnet + "\n\n"));
					}

					break;
				}

			}

			if (nb.compareFlag == null) {
				nb.compareFlag = "A";
				String ce = new String(
						"Block with name '" + cleanDesc(nb, false) + "' added to this diagram\n\n");

				mismatches.add(ce);
			}

		}

		for (Block ob : oldDiag.blocks.values()) {
			if (ob.compareFlag == null) {
				ob.compareFlag = "O";
				String ce = new String(
						"Block with name '" + cleanDesc(ob, false) + "' omitted from this diagram\n\n");

				mismatches.add(ce);
			} else
				ob.compareFlag = null;
		}

		for (Block nb : newDiag.blocks.values()) {
			if (nb.compareFlag != null && nb.compareFlag.equals("N"))
				nb.compareFlag = null;
		}

		/*
		 * for (Arrow arr : newDiag.arrows.values()) {
		 * 
		 * for (Arrow a : oldDiag.arrows.values()) { //if ((a.fromX == arr.fromX) // &&
		 * (a.fromY == arr.fromY) // && (a.toX == arr.toX) // && (a.toY == arr.toY)) if
		 * (a.id == arr.id) { // probably same line - now check for diffs if (a.fromId
		 * != arr.fromId || a.toId != arr.toId || a.dropOldest != arr.dropOldest ||
		 * a.capacity != arr.capacity || !(strEqu(a.upStreamPort, arr.upStreamPort)) ||
		 * !(strEqu(a.downStreamPort, arr.downStreamPort))) arr.compareFlag = "C";
		 * break; } }
		 * 
		 * if (arr.compareFlag != null) continue;
		 * 
		 * 
		 * Arrow a2 = oldDiag.arrows.get(new Integer(arr.id)); if (a2 == null ||
		 * a2.fromX != arr.fromX || a2.toX != arr.toX || a2.fromY != arr.fromY || a2.toY
		 * != arr.toY) arr.compareFlag = "A"; }
		 */

		int j = getFileTabNo(newDiag.diagFile.getAbsolutePath());
		if (-1 != j) {
			b = (ButtonTabComponent) jtp.getTabComponentAt(j);
			if (b == null || b.diag == null) {
				// comparing = false;
				return;
			}
			// curDiag = b.diag; (redundant)
			// curDiag.tabNum = i;
			jtp.setSelectedIndex(j);

		}

		if (mismatches == null || mismatches.isEmpty()) {
			curDiag = newDiag;
			return;
		}
		if (mmFrame != null)
			mmFrame.dispose();

		mmFrame = new JFrame();

		mmFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				mmFrame = null;
			}
		});

		BufferedImage image = loadImage("DrawFBP-logo-small.png");
		mmFrame.setIconImage(image);

		mmFrame.setLocation(500, 500);
		mmFrame.setForeground(Color.WHITE);
		mmFrame.setTitle("Differences between " + newDiag.diagFile.getName() + " and " + oldDiag.diagFile.getName());
		JTextPane pane = new JTextPane();
		JScrollPane sp = new JScrollPane(pane);
		StyleContext sc = new StyleContext();
		Style defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
		// Style baseStyle = sc.addStyle(null, defaultStyle);
		Style hdgStyle = sc.addStyle(null, defaultStyle);
		// StyleConstants.setItalic(hdgStyle, true);
		// StyleConstants.setAlignment(hdgStyle, StyleConstants.ALIGN_CENTER);
		MyDocument doc = new MyDocument(sc);

		sp.setViewportView(pane);
		pane.setStyledDocument(doc);
		// pane.setPreferredSize(new Dimension(800, 300));
		// sp.add(pane);
		pane.setPreferredSize(new Dimension(800, 400));
		mmFrame.add(sp, BorderLayout.CENTER);

		try {
			doc.insertString(0, "This diagram: ", hdgStyle);
			doc.insertString(doc.getLength(), newDiag.diagFile.getAbsolutePath(), defaultStyle);
			doc.insertString(doc.getLength(), "\nOther diagram: ", hdgStyle);
			doc.insertString(doc.getLength(), oldDiag.diagFile.getAbsolutePath(), defaultStyle);
			doc.insertString(doc.getLength(), "\n\n", hdgStyle);
		} catch (BadLocationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for (String s : mismatches) {
			try {
				doc.insertString(doc.getLength(), s, defaultStyle);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		mmFrame.pack();
		mmFrame.setVisible(true);
		pane.repaint();
		sp.repaint();
		mmFrame.repaint();

		curDiag = newDiag;
		// oldDiag.changed = false;
		// newDiag.changed = false;
		// repaint();
		// comparing = false;
	}

	String cleanDesc(Block b, boolean fbpMode) {

		String t = b.desc;

		// if (!(b instanceof IIPBlock)) {

		if (t == null || t.equals(""))
			t = "_comp_";
		t = t.replace('"', '\u0007');
		Pattern p;
		Matcher ma;

		if (fbpMode) {
			t = t.replace('_', '\u0007');
			p = Pattern.compile("\\p{Punct}"); // punctuation chars
			ma = p.matcher(t);

			String u = "";
			int i = 0;
			while (ma.find(i)) {
				String s = "\\" + ma.group();
				u += t.substring(i, ma.start()) + s;
				i = ma.end();
			}
			u += t.substring(i);
			t = u;
		}

		p = Pattern.compile("\\s"); // whitespace chars
		ma = p.matcher(t);
		t = ma.replaceAll("_");

		t = t.replace('\u0007', '_');

		// return makeUniqueDesc(t); // and make it unique
		return t;

	}
	// Compare two strings, checking for null first

	boolean strEqu(String a, String b) {
		if (a == null)
			return b == null;
		if (b == null)
			return a == null;

		return (a.trim().equals(b.trim()));

	}

	// 'between' checks that the value val is >= lim1 and <= lim2 - or the
	// inverse

	static boolean between(int val, int lim1, int lim2) {
		boolean res;
		if (lim1 < lim2)
			res = val >= lim1 && val <= lim2;
		else
			res = val >= lim2 && val <= lim1;
		return res;
	}
	
	

	boolean readPropertiesFile() {

		if (propertiesFile == null) {
			String uh = System.getProperty("user.home");
			propertiesFile = new File(uh + "/" + "DrawFBPProperties.xml");

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
				s = s.substring(j + 1); // value
				int k = s.indexOf("<");
				String u = "";
				if (k > 0) {
					if (!(key.equals("additionalJarFiles") || key.equals("additionalDllFiles"))) {
						s = s.substring(0, k).trim();
						key = key.replace("\\", "/");
						if (-1 == key.indexOf("/")) // compensate for old bug (key and value were reversed)!
							// saveProp(key, s);
							//if (key.equals("currentDiagramDir"))
							//	properties.put(key, s);
							//else
								properties.put(key, s);
					} else {
						// additionalJar/DllFiles
						Set<String> set = key.equals("additionalJarFiles") ? jarFiles : dllFiles;
						s = s.substring(0, k).trim();
						while (true) {
							int m = s.indexOf(";");
							if (m == -1) {
								u = s;
								if (!u.equals(""))
									set.add(u);
								break;
							} else {
								u = s.substring(0, m);
								s = s.substring(m + 1);
								if (!u.equals(""))
									set.add(u);
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
				if (k.startsWith("addnl_jf_") || k.startsWith("additionalJarFiles")
						|| k.startsWith("additionalDllFiles"))
					continue;
				if (k.equals("currentPackageName")) {
					String t = properties.get(k);
					if (t == null || t.equals("null") || t.equals("(null)"))
						continue;
				}
				String t = properties.get(k);
				String s = "<" + k + ">" + t + "</" + k + "> \n";
				out.write(s);
			}

			// Iterator<String> entries = jarFiles.iterator();
			String z = "";
			String cma = "";

			// while (entries.hasNext()) {
			// String thisEntry = entries.next();
			for (String thisEntry : jarFiles) {
				if (!thisEntry.equals("")) {
					z += cma + thisEntry;
					cma = ";";
				}

			}
			String s = "<additionalJarFiles> " + z + "</additionalJarFiles> \n";
			out.write(s);

			// entries = dllFiles.iterator();
			z = "";
			cma = "";

			// while (entries.hasNext()) {
			// String thisEntry = entries.next();
			for (String thisEntry : dllFiles) {
				if (!thisEntry.equals("")) {
					z += cma + thisEntry;
					cma = ";";
				}

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
			if (s != null) {
				// MyOptionPane.showMessageDialog(this,
				// "To access Java classes - File Chooser will be presented to locate JavaFBP
				// jar file",
				// MyOptionPane.WARNING_MESSAGE);
				// } else {
				MyOptionPane.showMessageDialog(this, "JavaFBP jar file location: " + s,
						MyOptionPane.INFORMATION_MESSAGE);

				int res = MyOptionPane.showConfirmDialog(this, "Change JavaFBP jar file location?",
						"Change JavaFBP jar file", MyOptionPane.YES_NO_OPTION);
				if (res != MyOptionPane.YES_OPTION)
					return true;
			}

			// MyOptionPane.showMessageDialog(this,
			// "Use File Chooser to locate JavaFBP jar file",
			// MyOptionPane.WARNING_MESSAGE);

			File f = new File(System.getProperty("user.home"));

			// else
			// f = (new File(s)).getParentFile();

			MyFileChooser fc = new MyFileChooser(this, f, langs[Lang.JARFILE], "Locate JavaFBP jar file");

			int returnVal = fc.showOpenDialog();

			File cFile = null;
			if (returnVal == MyFileChooser.APPROVE_OPTION) {
				cFile = new File(getSelFile(fc));
				if (cFile == null || !(cFile.exists())) {
					MyOptionPane.showMessageDialog(this, "Unable to read JavaFBP jar file " + cFile.getName(),
							MyOptionPane.ERROR_MESSAGE);
					return false;
				}
				// diag.currentDir = new File(cFile.getParent());
				javaFBPJarFile = cFile.getAbsolutePath();
				saveProp("javaFBPJarFile", javaFBPJarFile);

				saveProperties();
				MyOptionPane.showMessageDialog(this, "JavaFBP jar file location: " + cFile.getAbsolutePath(),
						MyOptionPane.INFORMATION_MESSAGE);
				// jarFiles.put("JavaFBP Jar File", cFile.getAbsolutePath());
				for (int i = 0; i < jtp.getTabCount(); i++) {
					ButtonTabComponent b = (ButtonTabComponent) jtp.getTabComponentAt(i);
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

	void locateFbpJsonFile(boolean checkLocation) {

		// setting of checkLocation doesn't matter if javaFBPJarFile is null!

		String s = properties.get("fbpJsonFile");
		fbpJsonFile = s;

		boolean findFile = false;
		// if (s != null && s.equals("fbp.json not needed"))
		// return;
		if (checkLocation || s == null)
			findFile = true;

		if (findFile) {

			if (s == null) {
				//int res = MyOptionPane.showConfirmDialog(this, "Locate fbp.json file?", "Locate fbp.json file",
				//		MyOptionPane.YES_NO_OPTION);
				//if (res != MyOptionPane.YES_OPTION)
				//	return;

			} else {
			int res = MyOptionPane.showConfirmDialog(this, "Change fbp.json file location?",
						"Change fbp.json file location", MyOptionPane.YES_NO_OPTION);
				if (res != MyOptionPane.YES_OPTION)
					return;
			}

			File f = new File(System.getProperty("user.home"));

			MyFileChooser fc = new MyFileChooser(this, f, langs[Lang.FBP_JSON], "Locate fbp.json File");

			int returnVal = fc.showOpenDialog();

			File cFile = null;
			if (returnVal == MyFileChooser.APPROVE_OPTION) {
				cFile = new File(getSelFile(fc));
				if (cFile == null || !(cFile.exists())) {
					MyOptionPane.showMessageDialog(this, "Unable to read fbp.json file " + cFile.getName(),
							MyOptionPane.ERROR_MESSAGE);
					return;
				}
				// diag.currentDir = new File(cFile.getParent());
				fbpJsonFile = cFile.getAbsolutePath();
				saveProp("fbpJsonFile", fbpJsonFile);

				saveProperties();
				MyOptionPane.showMessageDialog(this, "fbp.json file location: " + cFile.getAbsolutePath(),
						MyOptionPane.INFORMATION_MESSAGE);

				/*
				 * for (int i = 0; i < jtp.getTabCount(); i++) { ButtonTabComponent b =
				 * (ButtonTabComponent) jtp .getTabComponentAt(i); if (b == null || b.diag ==
				 * null) return false;
				 * 
				 * Diagram d = b.diag; if (d == null) continue;
				 * 
				 * for (Block bk : d.blocks.values()) { bk.getClassInfo(bk.fullClassName); } }
				 */

			}

		}

		// buildFbpJsonTree(fbpJsonFile);

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

				String s = entry.getName();
				if (s.toLowerCase().endsWith(".class")) {

					next = top;
					DefaultMutableTreeNode child;
					while (true) {
						int i = s.indexOf("/");
						String t;
						if (i == -1) {
							child = new DefaultMutableTreeNode(s);
							// System.out.println(s);
							next.add(child);
							break;
						} else {
							t = s.substring(0, i);
							if (null == (child = findChild(next, t))) {
								child = new DefaultMutableTreeNode(t);
								// System.out.println(t);
								next.add(child);
							}
							s = s.substring(i + 1);
							next = child;
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

	/*
	 * Build tree of nodes (DefaultMutableTreeNode) using contents of fbp.json
	 */

	
	void buildFbpJsonTree(String fileName) {
		File f = new File(fileName);
		String fileString;
		
		if (null == (fileString = readFile(f))) {
			MyOptionPane.showMessageDialog(this, "Unable to read file " + f.getName(), MyOptionPane.ERROR_MESSAGE);
			return;
		}
		Integer errNo = Integer.valueOf(0);
		BabelParser2 bp = new BabelParser2(fileString, errNo);

		String label = null;
		String operand = null;
		// String data = null;
		String first = null;
		String second = null;
		//boolean compList = true;
		int levelNo = 0;
		
		//DefaultMutableTreeNode currentNode  = null;
		DefaultMutableTreeNode nodes[] = new DefaultMutableTreeNode[100];

		while (true) {
			if (!bp.tb('o'))
				break;
		}

		while (true) {
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

			if (bp.tc('[', 'o')) { // start of array	
				if (bp.tc(']', 'o'))  // empty array
					continue;
				
				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode();	
				//if (label == null) 
				//	label = "list";
				newNode.setUserObject(new String(first));
				//System.out.println("[" + levelNo + " " + label);
				nodes[levelNo] = newNode;
				DefaultMutableTreeNode node = null;
				if (levelNo > 0) {
					node = nodes[levelNo - 1]; 
					node.add(newNode);
				}
				//currentNode = newNode;
				levelNo++;	
				
				continue;
			}
			if (bp.tc('{', 'o')) { // start of object
				
				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode();
				newNode.setUserObject(new String("group"));
				//System.out.println("{" + levelNo + " " + label);
				if (levelNo == 0)
					fbpJsonTree = newNode;
				
				nodes[levelNo] = newNode;
				DefaultMutableTreeNode node = null;
				if (levelNo > 0) {
					node = nodes[levelNo - 1]; 
					node.add(newNode);
				}
				//currentNode = newNode;
				levelNo++;	
				continue;
			}
			if (bp.tc(']', 'o')) { // end of array	
				levelNo--;
				continue;
			}
			if (bp.tc('}', 'o')) { // end of object
				levelNo--;
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

				if (bp.tc(':', 'o')) {
					first = new String(operand);
				} else {
					second = new String(operand);
					if (first == null) {
						MyOptionPane.showMessageDialog(this,
								"Operand \"" + second + "\" not preceded by colon in fbp.json file\n" +
						        "    Incorrect file format?", MyOptionPane.ERROR_MESSAGE);
						return;
					}
					if (first.equals("path")) {
						if (levelNo > 0) {
							DefaultMutableTreeNode node = nodes[levelNo - 1];
							//if (label != null)
							node.setUserObject(new String(second));
							//System.out.println((levelNo - 1) + " " + second);
							//label = null;
						}
					}
					if (first.equals("description")) {						
						label = new String(second);
					}
					if (first.equals("runtime")) {						
						label = new String(label + ":" + second);
						if (levelNo > 0) {
							DefaultMutableTreeNode node = nodes[levelNo - 1]; 
							node.setUserObject(new String(label));
						}
					}
				}

				bp.eraseOutput();
				continue;
			}

			if (!(bp.tu('o'))) // tu only returns false at end of string
				break; // skip next character

		}

		// if (ll.isEmpty()) {
		// MyOptionPane.showMessageDialog(this,
		// "No components in file: " + f.getName(),
		// MyOptionPane.ERROR_MESSAGE);
		// return null;
		// }
		
		return;
	}

	@SuppressWarnings("unchecked")
	DefaultMutableTreeNode findChild(DefaultMutableTreeNode current, String t) {
		if (current == null)
			return null;
		Enumeration<TreeNode> e = current.children();
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
			Object obj = node.getUserObject();
			if (t.equals((String) obj))
				return node;
		}
		return null;
	}

	boolean addAdditionalJarFile() {

		/*
		 * String ans = (String) MyOptionPane.showInputDialog(this,
		 * "Enter Description of jar file being added", "Enter Description",
		 * MyOptionPane.PLAIN_MESSAGE, null, null, null); if (ans == null ||
		 * ans.equals("")) { MyOptionPane.showMessageDialog(this,
		 * "No description entered", MyOptionPane.ERROR_MESSAGE); return false; }
		 */

		File f = new File(System.getProperty("user.home"));

		// curDiag.fCParm[Diagram.JARFILE].prompt = "Select jar file";
		// MyFileChooser fc = new MyFileChooser(this,f,
		// curDiag.fCParm[Diagram.JARFILE]);
		MyFileChooser fc = new MyFileChooser(this, f, langs[Lang.JARFILE], "Add Jar File");
		int returnVal = fc.showOpenDialog();
		File cFile = null;
		if (returnVal == MyFileChooser.APPROVE_OPTION) {
			cFile = new File(getSelFile(fc));
			if (cFile == null || !(cFile.exists())) {
				MyOptionPane.showMessageDialog(this, "Unable to read additional jar file " + cFile.getName(),
						MyOptionPane.ERROR_MESSAGE);
				return false;
			}

			jarFiles.add(cFile.getAbsolutePath());

			// Iterator<String> entries = jarFiles.iterator();
			String t = "";
			String cma = "";

			// while (entries.hasNext()) {
			// String thisEntry = (String) entries.next();
			for (String thisEntry : jarFiles) {
				t += cma + thisEntry;
				cma = ";";

			}
			saveProp("additionalJarFiles", t);
			MyOptionPane.showMessageDialog(this, "Additional jar file added: " + cFile.getName(),
					MyOptionPane.INFORMATION_MESSAGE);

			saveProperties();

		}
		return true;
	}

	boolean addAdditionalDllFile() {

		/*
		 * String ans = (String) MyOptionPane.showInputDialog(this,
		 * "Enter Description of dll file being added", "Enter Description",
		 * MyOptionPane.PLAIN_MESSAGE, null, null, null); if (ans == null ||
		 * ans.equals("")) { MyOptionPane.showMessageDialog(this,
		 * "No description entered", MyOptionPane.ERROR_MESSAGE); return false; }
		 */
		String s = properties.get("dllFileDir");
		File f = null;
		if (s == null)
			f = new File(System.getProperty("user.home"));
		else
			f = (new File(s)).getParentFile();
		MyFileChooser fc = new MyFileChooser(this, f, langs[Lang.DLL], "Add DLL File");

		// curDiag.fCParm[Diagram.DLL].prompt = "Select dll name";
		int returnVal = fc.showOpenDialog();
		File cFile = null;
		if (returnVal == MyFileChooser.APPROVE_OPTION) {
			cFile = new File(getSelFile(fc));
			if (cFile == null || !(cFile.exists())) {
				MyOptionPane.showMessageDialog(this, "Unable to read additional dll file " + cFile.getName(),
						MyOptionPane.ERROR_MESSAGE);
				return false;
			}

			dllFiles.add(cFile.getAbsolutePath());

			// Iterator entries = dllFiles.iterator();
			String t = "";
			String cma = "";

			// while (entries.hasNext()) {

			// String thisEntry = (String) entries.next();
			for (String thisEntry : dllFiles) {

				t += cma + thisEntry;
				cma = ";";

			}

			saveProp("additionalDllFiles", t);

			String u = cFile.getParent();
			saveProp("dllFileDir", u);
			MyOptionPane.showMessageDialog(this, "Additional dll file added: " + cFile.getName(),
					MyOptionPane.INFORMATION_MESSAGE);

			saveProperties();

		}
		return true;
	}

	void saveProp(String s, String t) {
		properties.put(s, t);
		saveProperties();
	}

	void saveProperties() {
		writePropertiesFile();
	}

	boolean closeTab(boolean terminate) {

		closeTabAction.actionPerformed(new ActionEvent(jtp, 0, "CLOSE"));
		if (!tabCloseOK)
			return false;
		if (terminate)
			return true;
		if (jtp.getTabCount() == 0) {
			getNewDiag();
			curDiag.desc = "Click anywhere on selection area";
		}
		return true;
	}

	void displayRow(GridBagConstraints gbc, GridBagLayout gbl, JTextField[] tf, JPanel panel, Color col) {
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
	 * String zfn = System.getProperty("user.home") + "/" + zipname; File f = new
	 * File(zfn); String s = f.getParent(); if (f.exists()) f.delete(); if (s !=
	 * null) (new File(s)).mkdirs(); try { copyInputStream(is, new
	 * FileOutputStream(f)); } catch (IOException e) {
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
	 * copyInputStream(zipFile.getInputStream(entry), new FileOutputStream(f)); } }
	 * 
	 * zipFile.close(); } catch (IOException ioe) {
	 * System.err.println("Unhandled exception:"); ioe.printStackTrace(); return; }
	 * }
	 * 
	 */

	/*
	 * void checkCompatibility(Arrow a) { Arrow a2 = a.findLastArrowInChain(); Block
	 * from = curDiag.blocks.get(new Integer(a.fromId)); Block to =
	 * curDiag.blocks.get(new Integer(a2.toId)); // String downPort =
	 * a2.downStreamPort; a.checkStatus = Status.UNCHECKED; if (!(from instanceof
	 * ProcessBlock) || !(to instanceof ProcessBlock)) return; if (a.upStreamPort ==
	 * null || a.upStreamPort.equals("")) return; if (a2.downStreamPort == null ||
	 * a2.downStreamPort.equals("")) return; if (a.upStreamPort.equals("*") ||
	 * a2.downStreamPort.equals("*")) { a.checkStatus = Status.COMPATIBLE; return; }
	 * if (from.outputPortAttrs == null || to.inputPortAttrs == null) return;
	 * AOutPort ao = from.outputPortAttrs.get(from.stem(a.upStreamPort)); if (ao ==
	 * null) return; AInPort ai = to.inputPortAttrs.get(to.stem(a2.downStreamPort));
	 * if (ai == null) return; if (ai.type.isAssignableFrom(ao.type) || ao.type ==
	 * Object.class) // Object // class // is // default a.checkStatus =
	 * Status.COMPATIBLE; else a.checkStatus = Status.INCOMPATIBLE; }
	 * 
	 */

	boolean pointInLine(Point2D p, int fx, int fy, int tx, int ty) {
		Line2D line = new Line2D((double) fx, (double) fy, (double) tx, (double) ty);
		double d = 0.0;
		try {
			d = line.distance(p);
		} catch (DegeneratedLine2DException e) {

		}

		return d < 4;
	}

	/**
	 * Test if point (xp, yp) is "near" line defined by arrow
	 */

	boolean nearpln(int xp, int yp, Arrow arr) {

		int x1 = arr.fromX;
		int y1 = arr.fromY;
		int x2 = 0;
		int y2 = 0;
		int seg = 0;
		Point2D p = new Point2D(xp, yp);
		Line2D line;
		double d = 0.0;
		boolean res = false;
		arr.highlightedSeg = -1;

		if (arr.shapeList == null)
			return false;

		Block b = curDiag.blocks.get(Integer.valueOf(arr.fromId));
		if (b == null || b.contains(new Point(xp, yp)))
			return false;

		if (arr.bends != null) {
			for (Bend bend : arr.bends) {
				x2 = bend.x;
				y2 = bend.y;

				line = new Line2D(x1, y1, x2, y2);
				try {
					d = line.distance(p);
				} catch (DegeneratedLine2DException e) {

				}
				if (d < 40.0) {
					res = true;
					break;
				}
				x1 = x2;
				y1 = y2;

				seg++;
			}

		}

		x2 = arr.toX;
		y2 = arr.toY;

		if (!res) {

			line = new Line2D(x1, y1, x2, y2);
			try {
				d = line.distance(p);
			} catch (DegeneratedLine2DException e) {

			}
			if (d >= 40.0)
				return false;

		}
		// boolean res = false;

		// System.out.println(d);

		// if (d >= 40.0) {
		// detArr = null;
		// detArrSegNo = -1;
		// arr.highlightedSeg = -1;
		// }
		// else {
		// detArr = arr;
		// detArrSegNo = segNo;

		if (arr.shapeList == null || arr.shapeList.size() <= seg)
			return false;

		Shape sh = arr.shapeList.get(seg);

		arr.highlightedSeg = seg;

		res = sh.contains(xp, yp);
		// }

		return res;
	}

	void displayAlignmentLines(Block block) {

		if (!(block instanceof ProcessBlock))
			return;

		block.hNeighbour = null;
		for (Block b : curDiag.blocks.values()) {
			if (!(b instanceof ProcessBlock))
				continue;
			int y = b.cy + b.height / 2;
			if (b != block && between(block.cy + block.height / 2, y - 6, y + 6)) {
				block.hNeighbour = b;
				break;
			}
		}
		block.vNeighbour = null;
		for (Block b : curDiag.blocks.values()) {
			if (!(b instanceof ProcessBlock))
				continue;
			int x = b.cx - b.width / 2;
			if (b != block && between(block.cx - block.width / 2, x - 6, x + 6)) {
				block.vNeighbour = b;
				break;
			}
		}
	}

	Point2D gridAlign(Point2D p) {
		Point2D p2 = p;
		if (clickToGrid) {
			int x = ((int) (p.x() + gridUnitSize / 2) / gridUnitSize) * gridUnitSize;
			int y = ((int) (p.y() + gridUnitSize / 2) / gridUnitSize) * gridUnitSize;
			p2 = new Point2D(x, y);
		}
		return p2;
	}

	Notation findNotnFromLabel(String s) {
		for (int i = 0; i < notations.length; i++)
			if (notations[i].label.equals(s))
				return notations[i];
		return null;
	}

	// Notation findNotnFromLang(String suff) {
	// for (int i = 0; i < notations.length; i++)
	// if (notations[Notation.i].lang.ext.equals(suff))
	// return notations[Notation.i];
	// return null;
	// }

	Lang findLangFromSuff(String suff) {
		for (int i = 0; i < langs.length; i++)
			if (langs[i].ext.equals(suff))
				return langs[i];
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

			for (String jfv : jarFiles) {
				f2 = new File(jfv);
				if (!(f2.equals(f)))
					ll.add(f2.toURI().toURL());
			}

			String clsDir = properties.get("currentClassDir") + "/";
			int m = clsDir.indexOf("bin/");
			sh = clsDir.substring(0, m + 4);

			// if (null != sh) {
			f2 = new File(sh);
			if (!(f2.equals(f)))
				ll.add(f2.toURI().toURL());
			// }

			urls = ll.toArray(new URL[ll.size()]);

		} catch (MalformedURLException e) {
			MyOptionPane.showMessageDialog(this, "Malformed URL: " + f, MyOptionPane.ERROR_MESSAGE);
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
	
	

	// "touches" changed to test if point (x, y) is within one of the side
	// rectangles...

	// gives result Side or null (touches - yes/no)

	/* static */ Side touches(Block b, int x, int y) {
		Side side = null;

		// System.out.println("Trytouch " + b + " " + x + " " + y);

		if (b.leftRect != null && b.leftRect.contains(x, y))
			side = Side.LEFT;

		else if (b.topRect != null && b.topRect.contains(x, y))
			side = Side.TOP;

		else if (b.rightRect != null && b.rightRect.contains(x, y))
			side = Side.RIGHT;

		else if (b.botRect != null && b.botRect.contains(x, y))
			side = Side.BOTTOM;

//		if (side != null)
//			System.out.println("Touches " + b + " " + (side != null) + " " + x + " " + y); 
		// else
		// System.out.println("Touches " + b + "no side " + " " + x + " " + y);

		return side;
	}

	public void blueCircs(Graphics g) {
		if (fpArrowRoot != null) {
			drawBlueCircle(g, fpArrowRoot.x, fpArrowRoot.y);
			repaint();
		}

		if (fpArrowEndB != null && currentArrow != null /* && currentArrow.toX > -1 */) {
			drawBlueCircle(g, fpArrowEndB.x, fpArrowEndB.y);
			repaint();
		}

		if (fpArrowEndA != null && currentArrow != null) {
			drawBlueCircle(g, fpArrowEndA.x, fpArrowEndA.y);
			repaint();
		}
	}

	void drawBlueCircle(Graphics g, int x, int y) {

		int cSize = zWS - 2;

		Color col = g.getColor();

		g.setColor(Color.BLUE);
		g.drawOval(x - cSize / 2, y - cSize / 2, cSize, cSize);

		g.setColor(col);
	}

	void drawBlackSquare(Graphics g, int x, int y) {
		final int squSize = 8;
		Color col = g.getColor();
		g.setColor(Color.BLACK);
		g.drawRect(x - squSize / 2, y - squSize / 2, squSize, squSize);
		g.setColor(col);
	}

	public void drawRedCircle(Graphics g, int x, int y) {
		Color col = g.getColor();
		g.setColor(Color.RED);
		g.drawOval(x - 5, y - 5, 10, 10);
		g.setColor(col);
	}

	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void componentResized(ComponentEvent e) {
		
		Dimension dim = getSize();
		
	
		//if (dim.width > 1000)
		//	up.setText("Go to Directory");
		//else
			up.setText("Go to Dir");
		
		
		//Dimension dim2 = new Dimension(dim.width / but.length, dim.height);
		int no = but.length;
		for (int j = 0; j < no; j++) {
			//box21.remove(0);
			//but[j].setMaximumSize(dim2);
			if (dim.width > 1000)
				but[j].setText(blockNames[j]);
			else
				but[j].setText(shortNames[j]);
			//box21.add(but[j]);
		}
		// (getGraphics()).drawImage(buffer, 0, 0, null);
		// System.out.println("Resized");

	}

	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();
		if (source instanceof JSlider && scaleLab != null) {
			JSlider js = (JSlider) source;
			// oldW = getSize().width;
			// oldH = getSize().height;
			if (!(js).getValueIsAdjusting()) {
				scalingFactor = ((double) js.getValue()) / 100.0;
				saveProp("scalingfactor", Double.toString(scalingFactor));
				// zWS = (int) Math.round(zoneWidth * scalingFactor);
				String scale = (int) js.getValue() + "%";
				scaleLab.setText(scale);
				// pack();
				// setPreferredSize(new Dimension(1200, 800));
				// repaint();
			}
		}

	}

	/*
	 * @Override public void mouseClicked(MouseEvent e) { int i = -1; //if
	 * (comparing) { Object source = e.getSource(); if (source == jtp) { Point p =
	 * new Point(e.getX(), e.getY()); for (i = 0; i < jtp.getTabCount(); i++) { if
	 * (jtp.getTabComponentAt(i).getBounds().contains(p)) {
	 * //jtp.setSelectedIndex(i); break; } } } // comparing = false; //}
	 * //repaint(); if (i == -1 ) { MyOptionPane.showMessageDialog(this,
	 * "No diagram selected", MyOptionPane.WARNING_MESSAGE); } else if (comparing) {
	 * comparing = false; jtp.removeMouseListener(this); compare(i); } else
	 * jtp.setSelectedIndex(i); repaint(); }
	 * 
	 * 
	 * @Override public void mouseEntered(MouseEvent e) { // TODO Auto-generated
	 * method stub
	 * 
	 * }
	 * 
	 * @Override public void mouseExited(MouseEvent e) { // TODO Auto-generated
	 * method stub
	 * 
	 * }
	 * 
	 * @Override public void mousePressed(MouseEvent e) { // TODO Auto-generated
	 * method stub
	 * 
	 * }
	 */

	public BufferedImage loadImage(String fileName) {
		BufferedImage image = null;

		// see
		// https://stackoverflow.com/questions/14089146/file-loading-by-getclass-getresource
		URL url = null;
		try {
			url = getClass().getResource("/" + fileName);
			if (url != null)
				image = ImageIO.read(url);
			else {
				MyOptionPane.showMessageDialog(this, "Missing icon: " + fileName, MyOptionPane.ERROR_MESSAGE);
				image = new BufferedImage(6, 6, BufferedImage.TYPE_INT_RGB);
			}

			// image = ImageIO.read(DrawFBP.class.getResourceAsStream("/" + fileName));
		} catch (MalformedURLException mue) {
			System.err.println("url: " + mue.getMessage() + ": " + url);
		} catch (IllegalArgumentException iae) {
			System.err.println("arg: " + iae.getMessage() + ": " + url);
		} catch (IOException ioe) {
			System.err.println("read: " + ioe.getMessage() + ": " + url);
		}
		// if(image == null)
		// {
		// image = new BufferedImage(6, 6, BufferedImage.TYPE_INT_RGB);
		// }
		return image;
	}

	public static void main(final String[] args) {

		Runnable myRunnable = createRunnable(args);

		SwingUtilities.invokeLater(myRunnable);

		/*
		 * SwingUtilities.invokeLater(new Runnable(args) { public void run(args) {
		 * 
		 * String[] runArgs = args;
		 * 
		 * String laf = UIManager.getSystemLookAndFeelClassName();
		 * 
		 * 
		 * System.setProperty("apple.laf.useScreenMenuBar", "true");
		 * 
		 * try { UIManager.setLookAndFeel(laf); } catch (ClassNotFoundException e1) {
		 * e1.printStackTrace(); } catch (InstantiationException e1) {
		 * e1.printStackTrace(); } catch (IllegalAccessException e1) {
		 * e1.printStackTrace(); } catch (UnsupportedLookAndFeelException e1) {
		 * e1.printStackTrace(); }
		 * 
		 * setDefaultLookAndFeelDecorated(true);
		 * 
		 * DrawFBP _mf= new DrawFBP(runArgs); _mf.setVisible(true);
		 * System.out.println(runArgs); } });
		 */

	}

	private static Runnable createRunnable(final String[] paramStr) {

		Runnable aRunnable = new Runnable() {
			public void run() {
				String[] runArgs = paramStr;

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

				DrawFBP _mf = new DrawFBP(runArgs);
				_mf.setVisible(true);
				// System.out.println(runArgs);

			}
		};

		return aRunnable;

	}
/*
	class TimeOutTask extends TimerTask {
	    private Thread t;
	    private Timer timer;

	    TimeOutTask(Thread t, Timer timer){
	        this.t = t;
	        this.timer = timer;
	    }
	 
	    public void run() {
	        if (t != null && t.isAlive()) {
	            t.interrupt();
	            timer.cancel();
	        }
	    }
	}
	*/
	// @Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		public void mouseClicked(MouseEvent e) {
			int i = jtp.indexAtLocation(e.getX(), e.getY());
			if (i > -1) {

				ButtonTabComponent b = (ButtonTabComponent) jtp.getTabComponentAt(i);
				if (b != null && b.diag != null) {

					Diagram diag = b.diag;

					if (diag == null)
						getNewDiag();

					jtp.setSelectedIndex(i);
				}

			}

			repaint();

		}

		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseDragged(MouseEvent e) {
			//selBlockM = null;
			
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			//selBlockM = null;
			
		}

	public class Notation {
		// this class refers the network notation
		String label;

		String srcDirProp; // DrawFBP property specifying source directory
		String netDirProp; // DrawFBP property specifying source directory for
							// net definition
		// FileFilter filter; // moved to Lang

		Lang lang; // programming language used

		// list of notations
				public static final int JAVA_FBP = 0;
				public static final int CSHARP_FBP = 1;
				public static final int JSON = 2;
				public static final int FBP = 3;
				
		Notation(String vm, Lang lan) {
			label = vm;		
			lang = lan;
			srcDirProp = "current" + lang.label + "SourceDir";
			netDirProp = "current" + lang.label + "NetworkDir";
			if (lang.label.equals("C#")) {
				srcDirProp = "currentCsharpSourceDir";
				netDirProp = "currentCsharpNetworkDir"; // xml does not seem to
														// like #'s
			}
		}

	}

	public class Lang {
		// this class refers the language used for notation
		String label;
		String ext; // extension - without period
		FileFilter filter;
		String propertyName;
		
		// list of "languages"
		public static final int JAVA = 0;
		public static final int CSHARP = 1;
		public static final int JS = 2;
		public static final int FBP = 3;
		public static final int DIAGRAM = 4;
		public static final int IMAGE = 5;
		public static final int JARFILE = 6;
		public static final int FBP_JSON = 7;
		public static final int CLASS = 8;
		public static final int DLL = 9;
		public static final int EXE = 10;
		public static final int PRINT = 11;

		Lang(String lab, String extn, FileFilter filt, String dir) {
			label = lab;
			ext = extn;
			filter = filt;
			propertyName = dir;
		}
	}
	
	public class MyRadioButton extends JRadioButton {
		
		private static final long serialVersionUID = 1L;
		String code = null; 
		boolean oneLine = false;
	}

	public class CloseAppAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {

			int openDiags = 0;

			for (int i = jtp.getTabCount() - 1; i > -1; i--) {
				ButtonTabComponent b = (ButtonTabComponent) jtp.getTabComponentAt(i);
				jtp.setSelectedIndex(i);
				if (b == null || b.diag == null)
					return;
				Diagram diag = b.diag;

				if (diag != null && diag.changed) {
					if (!closeTab(true)) {
						openDiags++;
						break; // return true if tab closed
					}
				}
			}

			if (tabCloseOK) {

				for (int i = jtp.getTabCount() - 1; i > -1; i--) {
					ButtonTabComponent b = (ButtonTabComponent) jtp.getTabComponentAt(i);
					jtp.setSelectedIndex(i);
					if (b == null || b.diag == null)
						return;
					Diagram diag = b.diag;

					if (diag != null)
						if (diag.changed)
							openDiags++;
						else if (!closeTab(true)) {
							openDiags++;
							break; // return true if tab closed
						}
				}
			}

			if (curDiag != null && curDiag.diagFile != null) {
				saveProp("currentDiagram", curDiag.diagFile.getAbsolutePath());
				saveProp("currentDiagramDir", curDiag.diagFile.getParent());
			}
			saveProp("scalingfactor", scalingFactor + "");
			saveProp("x", getX() + "");
			saveProp("y", getY() + "");
			saveProp("width", getWidth() + "");
			saveProp("height", getHeight() + "");
			saveProp("sortbydate", sortByDate + "");

			saveProperties();

			// if (jtp.getTabCount() == 0) {
			if (openDiags == 0) {
				dispose();
				System.exit(0);
			}
		}
	}

	public class CloseTabAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {

			tabCloseOK = true;

			int j = jtp.getTabCount();
			if (j < 1)
				return;

			int i = jtp.getSelectedIndex();
			if (i == -1) // don't know which to delete...
				return;
			ButtonTabComponent b = (ButtonTabComponent) jtp.getTabComponentAt(i);
			if (b == null || b.diag == null)
				return;
			Diagram diag = b.diag;

			if (diag != null) {
				if (diag.changed && diag.askAboutSaving() == MyOptionPane.CANCEL_OPTION) {
					tabCloseOK = false;
					return;
				}
			}

			if (i < jtp.getTabCount())
				jtp.remove(i);

			// curDiag = null;
			if (jtp.getTabCount() > 0) {
				b = (ButtonTabComponent) jtp.getTabComponentAt(0);
				if (b == null || b.diag == null)
					return;
				diag = b.diag;
				curDiag = diag;
				if (diag.diagFile != null)
					saveProp("currentDiagram", diag.diagFile.getAbsolutePath());
				// properties.remove("currentDiagram");
			}

			for (Arrow ar : curDiag.arrows.values()) {
				ar.compareFlag = null;
			}

			if (mmFrame != null)
				mmFrame.dispose();

			repaint();
		}
	}

	public class EscapeAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (currentArrow != null) {
				Integer aid = Integer.valueOf(currentArrow.id);
				curDiag.arrows.remove(aid);
				currentArrow = null; // terminate arrow drawing
				repaint();
				return;
			}

			if (e.getSource() == jHelpViewer) {
				popup.dispose();
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
			return s.toLowerCase().endsWith(".java") || f.isDirectory();

		}

		@Override
		public String getDescription() {
			return "Java source files (*.java)";
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

	public class JSFilter extends FileFilter {
		@Override
		public boolean accept(File f) {

			return f.getName().toLowerCase().endsWith(".js") || 
					f.getName().toLowerCase().endsWith(".coffee") ||
					f.isDirectory();

		}

		@Override
		public String getDescription() {
			return "JS source files (*.js or *.coffee)";
		}
	}
	
	public class JSONFilter extends FileFilter {
		@Override
		public boolean accept(File f) {

			return f.getName().toLowerCase().endsWith(".json") || 
					f.isDirectory();

		}

		@Override
		public String getDescription() {
			return "JSON source files (*.json)";
		}
	}

	public class FBPFilter extends FileFilter {
		@Override
		public boolean accept(File f) {

			return f.getName().toLowerCase().endsWith(".fbp")
					// || f.getName().toLowerCase().endsWith("fbp.json")
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

			return f.getName().toLowerCase().endsWith(".class") || f.isDirectory();

		}

		@Override
		public String getDescription() {
			return "Java class files (*." + "class" + ")";
		}

	}

	public class DiagramFilter extends FileFilter {
		@Override
		public boolean accept(File f) {

			return f.getName().toLowerCase().endsWith(".drw") || f.getName().toLowerCase().endsWith(".dr~")
					|| f.getName().toLowerCase().endsWith(".fbp") || f.getName().toLowerCase().endsWith("fbp.json")
					// fbp.json added to remind us that this is like a
					// directory
					|| f.getName().toLowerCase().endsWith(".json") || f.isDirectory();

		}

		@Override
		public String getDescription() {
			return "Diagrams (*.drw)";
		}

	}

	public class JarFileFilter extends FileFilter {
		@Override
		public boolean accept(File f) {

			return f.getName().toLowerCase().endsWith(".jar") || f.isDirectory();

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

			return f.getName().toLowerCase().endsWith(".png") || f.getName().toLowerCase().endsWith(".jpg")
					/* || f.getName().toLowerCase().endsWith(".bmp") */ || f.isDirectory();
		}

		@Override
		public String getDescription() {
			return "Images (*.png, *.jpg)";
		}

	}

	// Filter for .dll files
	public class DllFilter extends FileFilter {
		@Override
		public boolean accept(File f) {

			return f.getName().toLowerCase().endsWith(".dll") || f.isDirectory();
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

			return f.getName().toLowerCase().endsWith(".exe") || f.isDirectory();
		}

		@Override
		public String getDescription() {
			return ".exe files (*.exe)";
		}

	}
	/*

	public class FileChooserParm {
		// int index;
		String name;
		String propertyName;
		Lang lang;
		// String prompt;
		// String fileExt;
		// FileFilter filter;
		String title;

		FileChooserParm( String x, String a, Lang lan, String e) {
			// index = n;

			name = x;
			propertyName = a;
			lang = lan;
			// prompt = b;
			// fileExt = c;
			// filter = d;
			title = e;
		}
	}
*/
	public class RunTask extends Thread {
		public void run() {
			
			//Thread thr = Thread.currentThread();
			
			//Timer timer = new Timer();
			//timer.schedule(new TimeOutTask(thr, timer), 2000);  // time out after 2 secs
			
			//Thread haltedHook = new Thread(() -> thr.interrupt());
			//Runtime.getRuntime().addShutdownHook(haltedHook);
			
			ProcessBuilder pb = new ProcessBuilder(pBCmdArray);
			
			pb.directory(new File(pBDir));

			output = "";
			pb.redirectErrorStream(true);

			error = "";

			Process proc = null;
			//File log = new File(progName + File.separator + "log.txt");
			//pb.redirectOutput(Redirect.appendTo(log));
						
			 

			int u = 0;
			try {
				proc = pb.start();				
				
				//proc.waitFor();
				
			} catch (NullPointerException npe) {
				error = "Null Pointer Exception";
				proc = null;
				// return;
			} catch (IOException ioe) {
				error = "I/O Exception";
				proc = null;
				// return;
			}  catch (IndexOutOfBoundsException iobe) {
				error = "Index Out Of Bounds Exception";
				proc = null;
				// return;
			} catch (SecurityException se) {
				error = "Security Exception";
				proc = null;
				// return;
			}
				
			if (proc == null)
				return;	

			/*
			OutputStream os = proc.getOutputStream();
			try {
				os.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			} finally {

			}
			*/
			
			InputStream is = proc.getInputStream(); 
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			    String line = null;

			    while ((line = reader.readLine()) != null) {
			         output += line + "\n";
			         //System.out.println(line);
			         // see https://stackoverflow.com/questions/4886293/socket-input-stream-hangs-on-final-read-best-way-to-handle-this/4886747
			         if (driver.currNotn.lang == langs[Lang.CSHARP] && line.startsWith("Counts: C:")  && -1 < line.indexOf(", DO: "))
			        	 break;
			    }
			    /*
			    try {
			    	is.close();
			    } catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    */
			    
			    } catch (IOException ex) {
			        // Process IOException
			    }  finally {
			    	proc.destroy();
			    	//u = proc.exitValue();
			    	u = 0;  // fudge!
			    	/*
					try {
						is.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}		
					*/
			        //proc.destroy();
			    }
			
		 	 
			try {
				proc.waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		 
		
			
			//String lp = log.getAbsolutePath();
			//System.out.println(lp);		
			
		
			if (u == 0)
				MyOptionPane.showMessageDialog(driver, "Program completed - " + progName,
						MyOptionPane.INFORMATION_MESSAGE);
			else
				MyOptionPane.showMessageDialog(driver, "Program test failed, rc: " + u + " - " + progName,
						MyOptionPane.WARNING_MESSAGE);

			if (!(error.equals("")))
				MyOptionPane.showMessageDialog(driver,
						"<html>Program error - " + pBDir + progName + "<br/>" + error + "</html>",
						MyOptionPane.ERROR_MESSAGE);

			if (!(output.equals(""))) {
				//String msg = "<html>Program output from: " + pBDir + progName + "<br/>\n" + output + "</html>";
				JTextArea jta = new JTextArea(output);
				
				Dimension dim = driver.getPreferredSize();
				//Dimension dim2 = jta.getPreferredScrollableViewportSize();
				JScrollPane jsp = new JScrollPane(jta);
				jsp.setPreferredSize(dim);
				MyOptionPane.showMessageDialog(driver, jsp, MyOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
	
	public class ImagePanel extends JPanel {

		private static final long serialVersionUID = 1L;
		BufferedImage image = null;
		Dimension dim = null;
		//int top_border_height = 60;
		//int bottom_border_height = 60;

	  		
		public ImagePanel(BufferedImage img) {
			image = img;
			dim = new Dimension(image.getWidth(null), image.getHeight(null));
			setPreferredSize(dim);	
		}
		
		public void paintComponent(Graphics g) {
			//g.drawImage(image, 0, 0, getWidth(), getHeight(), this); // draw the image
			super.paintComponent(g);
			
			// Fill area with white
			
			int width = getWidth();
			int height = getHeight();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, width, height);
			
			Font f = driver.fontf;

			// Graphics g = buffer2.getGraphics();
			//Color col = g.getColor();
			g.setColor(Color.BLACK);
			g.setFont(f);
			//FontMetrics metrics = null; // g.getFontMetrics(f);
			int y = /* buffer2.getMinY() + */ 20;

			String t = curDiag.diagFile.getAbsolutePath();
			int x = 0;
			g.drawString(" " + t, x, y);
			
			x = (width - dim.width) /2;  
			y = (height - dim.height) /2; 
			FontMetrics metrics = g.getFontMetrics(f);
			y = 20 + 2 * metrics.getHeight();
			
			g.drawImage(image, x, y, dim.width, dim.height, this); // draw the original image
			
			Color col = g.getColor();
			g.setColor(Color.BLACK);
			
			
			//x = width - 140;
			ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
			t = utc.toString();
			int i = t.indexOf(":");
			int j = t.substring(i + 1).indexOf(":");
			t = t.substring(0, i + j + 1);
			t = t.replace("T", " ");
			t += " (UTC) ";
			byte[] str = t.getBytes();
			
			//metrics = g.getFontMetrics(f);
			width = metrics.bytesWidth(str, 0, str.length);
			//x = w - width + 20;
			x = getWidth() - width;   
			g.drawString(t, x, getHeight() - metrics.getHeight());

			g.setColor(col);			
		}
		
	}

	public class SelectionArea extends JPanel implements MouseInputListener {
		static final long serialVersionUID = 111L;
		int oldx, oldy, mousePressedX, mousePressedY;

		public SelectionArea() {

			setOpaque(true);

			/*
			 * String s = diagramName; if (s == null || s.endsWith(".drw")) {
			 * addMouseListener(this); addMouseMotionListener(this); }
			 * 
			 */
			// setFont(fontg);

			setBackground(Color.WHITE);
			// setPreferredSize(new Dimension(4000, 3000)); // experimental
			// pack();

		}

		// a is "from" arrow; a2 may be same, or arrow that a joins to...
		void defaultPortNames(Arrow a) {
			Block from = curDiag.blocks.get(Integer.valueOf(a.fromId));
			Block to = curDiag.blocks.get(Integer.valueOf(a.toId));
			Arrow a2 = a.findLastArrowInChain();
			to = curDiag.blocks.get(Integer.valueOf(a2.toId));
			if (from != null && (from instanceof ProcessBlock || from instanceof ExtPortBlock)
					&& (a2.endsAtBlock && to != null && (to instanceof ProcessBlock || to instanceof ExtPortBlock))) {
				if (a.upStreamPort == null || a.upStreamPort.trim().equals(""))
					a.upStreamPort = "OUT";

				if (a2.downStreamPort == null || a2.downStreamPort.trim().equals(""))
					a2.downStreamPort = "IN";
			}

			if (from instanceof IIPBlock && a2.endsAtBlock && to != null && to instanceof ProcessBlock) {
				if (a2.downStreamPort == null || a2.downStreamPort.trim().equals(""))
					a2.downStreamPort = "IN";
			}

		}

		public void paintComponent(Graphics g) {

			// Paint background if we're opaque.
			// super.paintComponent(g);
			int w = getWidth();
			if (isOpaque()) {
				// g.setColor(getBackground());
				osg.setColor(Color.WHITE);
				int h = getHeight();
				osg.fillRect(0, 0, (int) (w / scalingFactor), (int) (h / scalingFactor - 0));
			}

			int i = jtp.getSelectedIndex();

			ButtonTabComponent b = (ButtonTabComponent) jtp.getTabComponentAt(i);
			if (b == null || b.diag == null)
				return;

			Diagram diag = b.diag;

			// if (curDiag != diag) {
			// int x = 0; // problem!
			// }

			grid.setSelected(clickToGrid);

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

			// if (diag.diagFile != null)
			// System.out.println(diag.diagFile.getAbsolutePath() + " " +
			// diag.arrows.size());
			for (Arrow arrow : diag.arrows.values()) {
				// if (diag.diagFile != null)
				// System.out.println(diag.diagFile.getAbsolutePath() + " " + arrow);
				arrow.draw(osg);
				// System.out.println("arrow-draw");
			}

			if (tailMark != null)
				drawRedCircle(osg, tailMark.x, tailMark.y);

			if (headMark != null)
				drawRedCircle(osg, headMark.x, headMark.y);

			blueCircs(osg);

			String s = diag.desc;
			if (s != null) {
				if (s.trim().equals(""))
					s = "(no description)";
				s = s.replace('\n', ' ');
			}
			// else
			// s = "(no description)";

			diagDesc.setText(s);

			/*
			 * 
			 * if (comparing) { Color col = osg.getColor(); osg.setColor(lb); int cSize =
			 * 80; int x = w - cSize / 2; int y = cSize / 2;
			 * 
			 * osg.drawOval(x - cSize / 2, y - cSize / 2, cSize, cSize); osg.fillOval(x -
			 * cSize / 2, y - cSize / 2, cSize, cSize); osg.setColor(col);
			 * osg.setFont(fontg); FontMetrics metrics =
			 * this.osg.getFontMetrics(this.fontg);
			 * 
			 * String[] s1 = new String[]{"waiting", "to", "compare"}; y -= 10;
			 * 
			 * for (int j = 0; j < s1.length; j++) { byte[] str2 = s1[j].getBytes(); int xx
			 * = 2 + metrics.bytesWidth(str2, 0, s1[j].length()); osg.drawString(s1[j], x -
			 * xx / 2, y); y += 15; } }
			 * 
			 */

			Graphics2D g2d = (Graphics2D) g;

			// g2d.scale(scalingFactor, scalingFactor);
			// osg.scale(scalingFactor, scalingFactor);

			// g2d.translate(xTranslate, yTranslate);

			// Now copy that off-screen image onto the screen
			// g2d.drawImage(buffer, 0, 0, null);
			g2d.scale(scalingFactor, scalingFactor);  
			// g2d.scale(.8, .8);
			g.drawImage(buffer, 0, 0, null);

		}

		FoundPointB findBlockEdge(int x, int y, String type) {

			FoundPointB fpB = null;
			for (Block block : curDiag.blocks.values()) {

				// test whether to even look at block...
				if (type.equals("D")) {
					// System.out.println("horiz " + x + " " + (block.leftEdge - zWS / 2) + "-" +
					// (block.rgtEdge + zWS / 2));
					// System.out.println("vert " + y + " " + (block.topEdge - zWS / 2) + "-" +
					// (block.botEdge + zWS / 2));
				}

				if (!(between(x, block.leftEdge - zWS / 2, block.rgtEdge + zWS / 2)))
					continue;

				if (!(between(y, block.topEdge - zWS / 2, block.botEdge + zWS / 2)))
					continue;

				/* look for block edge touching xa and ya */
				// if (type.equals("D"))
				// System.out.println("calltouch " + x + " " + y);
				Side side = touches(block, x, y);
				if (side == null)
					continue;

				fpB = new FoundPointB(x, y, side, block);

				if (side == Side.LEFT)
					fpB.x = block.leftEdge;
				else if (side == Side.RIGHT)
					fpB.x = block.rgtEdge;
				else if (side == Side.TOP)
					fpB.y = block.topEdge;
				else if (side == Side.BOTTOM)
					fpB.y = block.botEdge;

				// fpB.block = block; // set in FoundPointB constructor

				break;
			}

			return fpB;
		}

		@SuppressWarnings("unused")
		// see if x and y are "close" to any arrow - if so, return it, else null
		FoundPointA findArrow(int x, int y) {

			FoundPointA fpA = null;

			for (Arrow arrow : curDiag.arrows.values()) {
				if (arrow.toId == -1)
					continue;

				// int x1 = arrow.fromX;
				// int y1 = arrow.fromY;
				int segNo = 0;
				// int x2, y2;

				if (arrow.bends != null) {
					for (Bend bend : arrow.bends) {
						// x2 = bend.x;
						// y2 = bend.y;

						if (nearpln(x, y, arrow)) {
							segNo = arrow.highlightedSeg;
							fpA = new FoundPointA(x, y, arrow, segNo);
							return fpA;
						}

						// x1 = x2;
						// y1 = y2;
						segNo++;
					}
				}

				// x2 = arrow.toX;
				// y2 = arrow.toY;

				if (nearpln(x, y, arrow)) {
					segNo = arrow.highlightedSeg;
					fpA = new FoundPointA(x, y, arrow, segNo);
					return fpA;
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
				StraightLine2D open = new StraightLine2D(xp, yp, arr.toX - xp, arr.toY - yp);
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
			selBlockM = null;
			int i = jtp.getSelectedIndex();
			if (i == -1)
				return;
			// drawToolTip = false;
			// fpArrowRoot = null;
			fpArrowEndA = null;
			fpArrowEndB = null;
			edgePoint = null;

			// detArr = null;
			// detArrSegNo = -1;

			repaint();
			ButtonTabComponent b = (ButtonTabComponent) jtp.getTabComponentAt(i);
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

			moveX = xa;
			moveY = ya;

			// System.out.println("M: " + xa + "," + ya);
			if (enclSelForArrow != null) {
				enclSelForArrow.corner = Corner.NONE;
				enclSelForArrow = null;
				repaint();
				return;
			}

			//selBlockM = null;
			// look for corner of an enclosure - if corner not null, you will
			// see diagonal arrows at corners
			for (Block block : curDiag.blocks.values()) {
				// block.calcEdges();
				if (block instanceof Enclosure) {
					Enclosure enc = (Enclosure) block;

					if (between(xa, block.leftEdge - 6, block.leftEdge + 6)
							&& between(ya, block.topEdge - 6, block.topEdge + 6)) {
						enclSelForArrow = enc;
						enc.corner = Corner.TOPLEFT;
						break;
					}
					if (between(xa, block.leftEdge - 6, block.leftEdge + 6)
							&& between(ya, block.botEdge - 6, block.botEdge + 6)) {
						enclSelForArrow = enc;
						enc.corner = Corner.BOTTOMLEFT;
						break;
					}
					if (between(xa, block.rgtEdge - 6, block.rgtEdge + 6)
							&& between(ya, block.topEdge - 6, block.topEdge + 6)) {
						enclSelForArrow = enc;
						enc.corner = Corner.TOPRIGHT;
						break;
					}
					if (between(xa, block.rgtEdge - 6, block.rgtEdge + 6)
							&& between(ya, block.botEdge - 6, block.botEdge + 6)) {
						enclSelForArrow = enc;
						enc.corner = Corner.BOTTOMRIGHT;
						break;
					}
				}

				// logic to change cursor to drag_icon
				int hh = gFontHeight;
				boolean udi; // Use Drag Icon
				if (block.typeCode.equals(Block.Types.ENCL_BLOCK)) {
					udi = between(xa, block.leftEdge + block.width / 5, block.rgtEdge - block.width / 5)
							&& between(ya, block.topEdge - hh, block.topEdge + hh / 2);
				} else {

					udi = between(xa, block.leftEdge + zWS * 3 / 4, block.rgtEdge - zWS * 3 / 4)
							&& between(ya, block.topEdge + zWS * 3 / 2, block.botEdge - zWS * 3 / 2);
				}

				if (udi) {
					selBlockM = block; // mousing select
					// if (!use_drag_icon) {
					if (curDiag.jpm == null && !panSwitch)
						setCursor(drag_icon);
					// use_drag_icon = true;
					// }

					break;
				}

			}

			// setCursor(defaultCursor); // experimental!

			if (selBlockM == null) {
				// if (use_drag_icon)
				// use_drag_icon = false;

				if (!panSwitch)
					setCursor(defaultCursor);
			}
			// curDiag.foundBlock = null;

			if (currentArrow != null
					&& (Math.abs(currentArrow.fromX - xa) > 10 || Math.abs(currentArrow.fromY - ya) > 10)) {
				edgePoint = findBlockEdge(xa, ya, "M");
				if (edgePoint != null) {
					xa = edgePoint.x;
					ya = edgePoint.y;
					fpArrowEndB = edgePoint;
					repaint();
				}
			}

			if (currentArrow == null) {
				edgePoint = findBlockEdge(xa, ya, "M");
				if (edgePoint != null) {
					// fpArrowRoot = edgePoint;
					xa = edgePoint.x;
					ya = edgePoint.y;
					selBlockM = edgePoint.block;
				}

			}

			colourArrows(xa, ya);

			repaint();
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {

		}

		/***
		 * The following mouse actions are supported:
		 * 
		 * - click on block - highlights block - double-click on block - brings up popup
		 *    menu if not subnet - brings up subnet if subnet - press on side of block
		 *    starts arrow - release on side of block starts or ends arrow - click on arrow
		 * - brings up popup menu - press on block - starts drag
		 * 
		 */

		public void mousePressed(MouseEvent e) {
			// Block foundBlock = null;
			int i = jtp.getSelectedIndex();
			if (i == -1)
				return;
			ButtonTabComponent b = (ButtonTabComponent) jtp.getTabComponentAt(i);
			if (b == null || b.diag == null)
				return;
			curDiag = b.diag;

			// detArr = null;
			// detArrSegNo = 0;
			repaint();

			// Side side = null;
			leftButton = (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) == InputEvent.BUTTON1_DOWN_MASK;
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
				if (x >= curDiag.area.getX() && x <= curDiag.area.getX() + d.width && y >= curDiag.area.getY()
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
				block.buildSideRects();
				block.adjEdgeRects();
				block.calcEdges();
				if (block instanceof Enclosure) {
					Enclosure enc = (Enclosure) block;
					/* test for a hit within the rectangle at top */
					int hh = gFontHeight;
					if (between(xa, block.leftEdge + block.width / 5, block.rgtEdge - block.width / 5)
							&& between(ya, block.topEdge - hh, block.topEdge + hh / 2)) {
						mousePressedX = oldx = xa;
						mousePressedY = oldy = ya;
						blockSelForDragging = block;
						break;
					}

					/* now handle stretching at the corners */
					if (between(xa, block.leftEdge - 6, block.leftEdge + 6)
							&& between(ya, block.topEdge - 6, block.topEdge + 6)) {
						blockSelForDragging = enc;
						enc.corner = Corner.TOPLEFT;
						break;
					}
					if (between(xa, block.leftEdge - 6, block.leftEdge + 6)
							&& between(ya, block.botEdge - 6, block.botEdge + 6)) {
						blockSelForDragging = enc;
						enc.corner = Corner.BOTTOMLEFT;
						break;
					}
					if (between(xa, block.rgtEdge - 6, block.rgtEdge + 6)
							&& between(ya, block.topEdge - 6, block.topEdge + 6)) {
						blockSelForDragging = enc;
						enc.corner = Corner.TOPRIGHT;
						break;
					}
					if (between(xa, block.rgtEdge - 6, block.rgtEdge + 6)
							&& between(ya, block.botEdge - 6, block.botEdge + 6)) {
						blockSelForDragging = enc;
						enc.corner = Corner.BOTTOMRIGHT;
						break;
					}

				} else { // not enclosure
					/*
					 * the following leaves a strip around the outside of each block that cannot be
					 * used for dragging!
					 */
					Rectangle rect = new Rectangle(block.leftEdge + zWS * 3 / 4, block.topEdge + zWS * 3 / 4,
							block.width - zWS * 3 / 2, block.height - zWS * 3 / 2);
					if (rect.contains(xa, ya)) {

						mousePressedX = oldx = xa;
						mousePressedY = oldy = ya;
						blockSelForDragging = block;
						break;
					}
				}

				/* check for possible starts of arrows */

				if (headMark != null || tailMark != null) {
					repaint();
					return;
				}
				edgePoint = findBlockEdge(xa, ya, "P");
				if (edgePoint != null) {
					xa = edgePoint.x;
					ya = edgePoint.y;
					// fpArrowRoot = edgePoint;
					repaint();
				}

			}

			if (blockSelForDragging != null && blockSelForDragging instanceof Enclosure) {
				ox = blockSelForDragging.cx;
				oy = blockSelForDragging.cy;
				ow = blockSelForDragging.width;
				oh = blockSelForDragging.height;
				repaint();
				return;
			}

			// if no currentArrow, but there is a found block, start an arrow
			// if (currentArrow == null && foundBlock != null
			// && arrowEndForDragging == null) {
			if (currentArrow == null) {
				fpArrowRoot = edgePoint;
				if (fpArrowRoot != null) {
					Arrow arrow = new Arrow(curDiag);
					curDiag.maxArrowNo++;
					arrow.id = curDiag.maxArrowNo;
					selArrow = arrow;
					// selBlockP = null;
					arrow.fromX = fpArrowRoot.x; // xa;
					arrow.fromY = fpArrowRoot.y; // ya;

					// int id = fpArrowRoot.block.id;
					// arrow.fromId = foundBlock.id;
					arrow.fromId = fpArrowRoot.block.id;
					Block fromBlock = curDiag.blocks.get(Integer.valueOf(arrow.fromId));
					if (fromBlock.typeCode.equals(Block.Types.EXTPORT_IN_BLOCK)
							|| fromBlock.typeCode.equals(Block.Types.EXTPORT_OUTIN_BLOCK))
						arrow.upStreamPort = "OUT";
					// arrow.fromId = -1;
					currentArrow = arrow;
					arrow.lastX = xa; // save last x and y
					arrow.lastY = ya;
					Integer aid = Integer.valueOf(arrow.id);
					curDiag.arrows.put(aid, arrow);
					arrowEndForDragging = arrow;

					// foundBlock = null;

				}
			}

			else {
				Arrow arrow = currentArrow;
				if (tailMark != null || headMark != null) {
					arrowEndForDragging = arrow;
				}
			}

			repaint();
		}

		public void mouseDragged(MouseEvent e) {

			fpArrowRoot = null;
			fpArrowEndA = null;
			fpArrowEndB = null;
			// edgePoint = null;

			int i = jtp.getSelectedIndex();
			if (i == -1)
				return;

			repaint();
			ButtonTabComponent b = (ButtonTabComponent) jtp.getTabComponentAt(i);
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
			// System.out.println("D: " + xa + "," + ya);
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
					block.buildSideRects();
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
				if (tailMark != null) {
					arr.fromId = -1;
					arr.fromX = xa;
					arr.fromY = ya;
					tailMark.x = xa;
					tailMark.y = ya;
					curDiag.changed = true;
					currentArrow = arr;
					repaint();
					return;

				} else if (headMark != null) {
					arr.toId = -1;
					arr.toX = xa;
					arr.toY = ya;
					headMark.x = xa;
					headMark.y = ya;
					curDiag.changed = true;
					currentArrow = arr;
					repaint();
					return;
				}
				// curDiag.changed = true;
				// repaint();
				// return;
			}

			if (bendForDragging != null) {
				bendForDragging.x = xa;
				bendForDragging.y = ya;
				curDiag.changed = true;
				repaint();
				return;
			}

			// logic to drag one corner of enclosure

			if (blockSelForDragging != null) {
				if (blockSelForDragging instanceof Enclosure) {
					Enclosure enc = (Enclosure) blockSelForDragging;

					if (enc.corner == Corner.TOPLEFT) {
						enc.width = ox + ow / 2 - xa; // ox is value of cx when dragging started
						enc.height = oy + oh / 2 - ya; // oy is value of cy when dragging started
						enc.cx = xa + enc.width / 2; // ow is value of width when dragging started
						enc.cy = ya + enc.height / 2; // oh is value of height when dragging started

					}
					if (enc.corner == Corner.BOTTOMLEFT) {
						enc.width = ox + ow / 2 - xa;
						enc.height = ya - (oy - oh / 2);
						enc.cx = xa + enc.width / 2;
						enc.cy = ya - enc.height / 2;

					}
					if (enc.corner == Corner.TOPRIGHT) {
						enc.width = xa - (ox - ow / 2);
						enc.height = oy + oh / 2 - ya;
						enc.cx = xa - enc.width / 2;
						enc.cy = ya + enc.height / 2;

					}
					if (enc.corner == Corner.BOTTOMRIGHT) {
						enc.width = xa - (ox - ow / 2);
						enc.height = ya - (oy - oh / 2);
						enc.cx = xa - enc.width / 2;
						enc.cy = ya - enc.height / 2;

					}
					// enc.buildSides();
					// enc.adjEdgeRects();
					enc.calcEdges();
					if (enc.corner != Corner.NONE) {
						curDiag.changed = true;
						// enc.corner = Corner.NONE;
						repaint();
						return;
					}

				}

				if (clickToGrid && Math.abs(xa - oldx) < 6 && Math.abs(ya - oldy) < 6 || // do not respond
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

				int x_inc = xa - oldx;
				int y_inc = ya - oldy;
				block.cx += x_inc;
				block.cy += y_inc;
				block.topRect.x += x_inc;
				block.topRect.y += y_inc;
				if (block.botRect != null) {
					block.botRect.x += x_inc;
					block.botRect.y += y_inc;
				}
				block.leftRect.x += x_inc;
				block.leftRect.y += y_inc;
				block.rightRect.x += x_inc;
				block.rightRect.y += y_inc;

				block.buildSideRects();
				block.calcEdges();
				block.adjEdgeRects();

				if (fpArrowRoot != null && fpArrowRoot.block == block) {
					fpArrowRoot.x += xa - oldx;
					fpArrowRoot.y += ya - oldy;
				}

				if (block instanceof Enclosure) {
					Enclosure enc = (Enclosure) block;

					if (enc.llb != null) {
						for (Block bk : enc.llb) {
							bk.cx += xa - oldx;
							bk.cy += ya - oldy;
							bk.buildSideRects();
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

			if (currentArrow != null) { // this ensures the line stays visible
				// Math.abs(currentArrow.fromX - xa) > zWS && // pick arbitrary figure!
				// Math.abs(currentArrow.fromY - ya) > zWS) {

				currentArrow.toId = -1;
				currentArrow.toX = xa;
				currentArrow.toY = ya;
				curDiag.changed = true;
				fpArrowEndA = null;
				fpArrowEndB = null;
				currentArrow.endsAtBlock = false;
				currentArrow.endsAtLine = false;

				if (Math.abs(currentArrow.fromX - xa) > 10 || // pick arbitrary figure!
						Math.abs(currentArrow.fromY - ya) > 10) {
					// System.out.println("dragging " + xa + " " + ya);
					// FoundPointB fpB = findBlockEdge(xa, ya);
					edgePoint = findBlockEdge(xa, ya, "D");
					if (edgePoint != null) {
						xa = edgePoint.x;
						ya = edgePoint.y;
						currentArrow.toId = -2; 
						repaint();
					}

				}

			}
			colourArrows(xa, ya);

			repaint();
		}

		public void mouseReleased(MouseEvent e) {

			// comparing = false;
			// Arrow foundArrow = null;
			// Block foundBlock = null;
			edgePoint = null;
			fpArrowEndA = null;
			fpArrowEndB = null;

			int i = jtp.getSelectedIndex();
			if (i == -1)
				return;
			ButtonTabComponent b = (ButtonTabComponent) jtp.getTabComponentAt(i);
			if (b == null || b.diag == null)
				return;
			curDiag = b.diag;

			// detArr = null;
			// detArrSegNo = 0;
			repaint();

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

			// Side side = null;
			Point2D p2 = new Point2D(x, y);
			p2 = gridAlign(p2);
			xa = (int) p2.x();
			ya = (int) p2.y();

			// System.out.println("R: " + xa + "," + ya);

			if (curDiag.area.contains(xa, ya) && panSwitch) {
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
					if (between(mousePressedX, (int) (x - 6 * scalingFactor), (int) (x + 6 * scalingFactor))
							&& between(mousePressedY, (int) (y - 6 * scalingFactor), (int) (y + 6 * scalingFactor))
							|| Math.abs(mousePressedX - x) > 100 || Math.abs(mousePressedY - y) > 100) {

						// if it was a small move, or a big jump, just get
						// subnet, or display options

						if (leftButton && blockSelForDragging.isSubnet) {
							if (blockSelForDragging.subnetFileName == null) {
								MyOptionPane.showMessageDialog(this, "No subnet diagram assigned",
										MyOptionPane.INFORMATION_MESSAGE);
							} else {

								String name = blockSelForDragging.subnetFileName;
								// String dir = properties.get("currentDiagramDir");
								// MyOptionPane.showMessageDialog(null,
								// "Subnet OK - subnet diagram assigned",
								// MyOptionPane.INFORMATION_MESSAGE);

								int k = getFileTabNo(name);
								if (k != -1) {
									jtp.setSelectedIndex(k);
									return;
								}

								File df = openAction(name);
								if (df == null)
									return;

								curDiag.diagFile = df;
								curDiag.desc = df.getName();
								curDiag.title = df.getName();

								curDiag.changed = false;
								return;
							}
						} else {
							blockSelForDragging.buildBlockPopupMenu();
							// if (this == null)
							// curDiag = new Diagram(this);

							curDiag = blockSelForDragging.diag;

							curDiag.jpm.show(this, xa + 100, ya + 100);

						}

					}
					repaint();
					// return;
				}

			}

			if (arrowEndForDragging != null) {

				// currentArrow = null;

				foundBlock = null;
				// curDiag.changed = true;
				Arrow arr = arrowEndForDragging;

				// try to anchor arrow tail or head!

				for (Block block : curDiag.blocks.values()) {
					if (tailMark != null) {
						arr.fromId = -1;
						if (null != touches(block, tailMark.x, tailMark.y)) {
							arr.fromId = block.id;
							foundBlock = block;

							arrowEndForDragging = null;
							currentArrow = null;
							// currentArrow = null;
							// arr.tailMark = null;
							tailMark = null;
							// break;
							currentArrow = null;
							edgePoint = null;

							repaint();
							return;
						}

					}

					if (headMark != null) {
						arr.toId = -1;
						if (null != touches(block, headMark.x, headMark.y)) {
							arr.toId = block.id;
							foundBlock = block;
							arr.endsAtBlock = true;
							arr.endsAtLine = false;
							// break;
							headMark = null;
							currentArrow = null;
							edgePoint = null;
							repaint();
							return;
						}
					}
				}

				// if headmarked and no block found, try to detect an arrow...

				if (headMark != null && foundBlock == null) {
					FoundPointA fpA;
					if (null != (fpA = findArrow(arr.toX, arr.toY))) {
						arr.toId = fpA.arrow.id;
						arr.endsAtBlock = false;
						arr.endsAtLine = true;

						// currentArrow.toId = arr.id;

						arr.toX = fpA.x;
						arr.toY = fpA.y;
						arrowEndForDragging = null;
						currentArrow = null;
						headMark = null;
						edgePoint = null;
						repaint();
						return;
					}

					arr.toId = -1;
					// arr.toX = -1;
					// if (arr.bends == null)
					// arr.bends = new LinkedList<Bend>();
					// arr.bends.add(new Bend(arr.toX, arr.toY));
					arr.toX = xa;
					arr.toY = ya;
					Arrow arr2 = currentArrow;
					arrowEndForDragging = arr2;
					headMark = null;

				}

				curDiag.changed = true;

				/*
				 * if (tailMark != null || headMark != null ) { tailMark = null; //headMark =
				 * null; //currentArrow = null; edgePoint = null; repaint(); return; }
				 */
				repaint();
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
					if (clickToGrid) {
						blockSelForDragging.cy = blockSelForDragging.hNeighbour.cy;
						blockSelForDragging.adjEdgeRects();
						blockSelForDragging.calcEdges();
					}
					blockSelForDragging.hNeighbour = null;
				}

				if (blockSelForDragging.vNeighbour != null) {
					if (clickToGrid) {
						blockSelForDragging.cx = blockSelForDragging.vNeighbour.cx;
						blockSelForDragging.adjEdgeRects();
						blockSelForDragging.calcEdges();
					}
					blockSelForDragging.vNeighbour = null;
				}

				for (Arrow arrow : curDiag.arrows.values()) {
					if (arrow.fromId == blockSelForDragging.id) {
						arrow.fromX += blockSelForDragging.cx - savex;
						arrow.fromY += blockSelForDragging.cy - savey;
					}
					if (arrow.toId == blockSelForDragging.id && !arrow.endsAtLine) {
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

				setCursor(defaultCursor);
				repaint();
				return;
			}

			if (blockSelForDragging != null && blockSelForDragging instanceof Enclosure) {
				((Enclosure) blockSelForDragging).corner = Corner.NONE;
				blockSelForDragging = null;
				curDiag.changed = true;
				setCursor(defaultCursor);
				repaint();
				return;
			}

			foundBlock = null;
			if (currentArrow == null) {

				// Look for a line to detect, for deletion, etc. - logic to end
				// arrow at a line comes in a later section...
				// currentArrow = null;
				// if (!leftButton) {

				FoundPointA fpA = findArrow(xa, ya);
				if (fpA != null && fpA.arrow != null) {
					currentArrow = fpA.arrow;
					fpArrowEndA = fpA;
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
					block.calcEdges();
					if (!(block instanceof Enclosure)) {
						if (between(xa, block.cx - block.width / 4, block.cx + block.width / 4)
								&& between(ya, block.cy - block.height / 4, block.cy + block.height / 4)) {
							foundBlock = block;
							selBlock = block;
							selArrow = null;
							// block.x = xa;
							// block.y = ya;
							break;
						}
					} else { // if it is an enclosure block

						int hh = gFontHeight;
						if (between(xa, block.cx - block.width / 2 + block.width / 5,
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
				//if (!(blockType.equals("")) && foundBlock == null)
				//	if (null != createBlock(blockType, xa, ya, curDiag, true))
				if (foundBlock == null && null != createBlock(xa, ya, curDiag, true))
						curDiag.changed = true;
				repaint();

				return;
			}

			edgePoint = findBlockEdge(xa, ya, "R");
			// foundBlock = null;

			if (edgePoint != null) {
				foundBlock = edgePoint.block;
				// side = fpB.side;
				fpArrowEndB = edgePoint;
			}

			if (foundBlock != null // && leftButton
			) {

				if (between(currentArrow.fromX, x - zWS / 2, x + zWS / 2)
						&& between(currentArrow.fromY, y - zWS / 2, y + zWS / 2))
					return;

				if (foundBlock.id == currentArrow.fromId) {

					if (MyOptionPane.NO_OPTION == MyOptionPane.showConfirmDialog(this,
							"Connecting arrow to originating block is deadlock-prone - do anyway?", "Allow?",
							MyOptionPane.YES_NO_OPTION)) {
						Integer aid = Integer.valueOf(currentArrow.id);
						curDiag.arrows.remove(aid);
						foundBlock = null;
						currentArrow = null;
						repaint();
						return;
					}
				}

				boolean OK = true;
				Block from = curDiag.blocks.get(Integer.valueOf(currentArrow.fromId));
				if ((foundBlock instanceof ProcessBlock || foundBlock instanceof ExtPortBlock)
						&& !(from instanceof IIPBlock)) {
					if (edgePoint.side == Side.BOTTOM) {
						int answer = MyOptionPane.showConfirmDialog(this, "Connect arrow to bottom of block?",
								"Please choose one", MyOptionPane.YES_NO_OPTION);
						if (answer != MyOptionPane.YES_OPTION)
							OK = false;
					}
					if (edgePoint.side == Side.RIGHT) {
						int answer = MyOptionPane.showConfirmDialog(this, "Connect arrow to righthand side?",
								"Please choose one", MyOptionPane.YES_NO_OPTION);
						if (answer != MyOptionPane.YES_OPTION)
							OK = false;
					}
				}
				if (!OK) {
					// MyOptionPane.showMessageDialog(this,
					// "Cannot end an arrow here");
					Integer aid = Integer.valueOf(currentArrow.id);
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
					if (edgePoint.side == Side.LEFT)
						xa = foundBlock.leftEdge;
					else if (edgePoint.side == Side.RIGHT)
						xa = foundBlock.rgtEdge;
					else if (edgePoint.side == Side.TOP)
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
				
				if (curDiag.oldArrow != null) {
					a.upStreamPort = curDiag.oldArrow.upStreamPort;
					a.downStreamPort = curDiag.oldArrow.downStreamPort;
					a.capacity = curDiag.oldArrow.capacity;
					curDiag.oldArrow = null;
				}

				defaultPortNames(a);

				from = curDiag.blocks.get(Integer.valueOf(a.fromId));
				Block to = curDiag.blocks.get(Integer.valueOf(a.toId));
				Arrow a2 = a.findLastArrowInChain();
				to = curDiag.blocks.get(Integer.valueOf(a2.toId));

				boolean error = false;
				if (to instanceof IIPBlock && from instanceof ProcessBlock) {
					a2.reverseDirection();
					// MyOptionPane
					// .showMessageDialog(this,
					// "Direction of arrow has been reversed");
				}
				if (from instanceof ExtPortBlock && (from.typeCode.equals(Block.Types.EXTPORT_OUT_BLOCK)
						|| from.typeCode.equals(Block.Types.EXTPORT_OUTIN_BLOCK) && a2.fromX < from.cx))
					error = true;
				else if (to instanceof ExtPortBlock && (to.typeCode.equals(Block.Types.EXTPORT_IN_BLOCK)
						|| to.typeCode.equals(Block.Types.EXTPORT_OUTIN_BLOCK) && a2.toX > to.cx))
					error = true;

				if (!a2.checkSides())
					error = true;

				if (error) {
					MyOptionPane.showMessageDialog(this, "Arrow attached to one or both wrong side(s) of blocks",
							MyOptionPane.WARNING_MESSAGE);
					Integer aid = Integer.valueOf(a2.id);
					curDiag.arrows.remove(aid);
				} else {
					curDiag.changed = true;
					// checkCompatibility(a);
				}

				from.displayPortInfo();

				to.displayPortInfo();

				// Block toBlock = curDiag.blocks.get(new Integer(a2.toId));
				if (to.typeCode.equals(Block.Types.EXTPORT_OUT_BLOCK) || to.typeCode.equals(Block.Types.EXTPORT_OUTIN_BLOCK))
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
				FoundPointA fpA = findArrow(xa, ya);
				if (fpA != null && fpA.arrow != null) {
					foundArrow = fpA.arrow;
					fpArrowEndA = fpA;

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
					currentArrow.segNo = fpA.segNo;
					currentArrow.upStreamPort = "OUT";

					// use id of target line, not of target block
					currentArrow.toId = foundArrow.id;

					defaultPortNames(foundArrow);

					Block from = curDiag.blocks.get(Integer.valueOf(currentArrow.fromId));
					Block to = curDiag.blocks.get(Integer.valueOf(foundArrow.toId));
					Arrow a2 = foundArrow.findLastArrowInChain();
					to = curDiag.blocks.get(Integer.valueOf(a2.toId));

					if (to == from) {
						if (MyOptionPane.NO_OPTION == MyOptionPane.showConfirmDialog(this,
								"Connecting arrow to originating block is deadlock-prone - do anyway?", "Allow?",
								MyOptionPane.YES_NO_OPTION)) {
							Integer aid = Integer.valueOf(currentArrow.id);
							curDiag.arrows.remove(aid);
							foundBlock = null;
							currentArrow = null;

							repaint();
							return;
						}

					}

					boolean error = true;
					if (from instanceof ExtPortBlock && from.typeCode.equals(Block.Types.EXTPORT_OUT_BLOCK))
						MyOptionPane.showMessageDialog(this, "Arrow in wrong direction", MyOptionPane.ERROR_MESSAGE);
					else if (to instanceof ExtPortBlock && to.typeCode.equals(Block.Types.EXTPORT_IN_BLOCK))
						MyOptionPane.showMessageDialog(this, "Arrow in wrong direction", MyOptionPane.ERROR_MESSAGE);
					else
						error = false;
					if (error) {
						Integer aid = Integer.valueOf(currentArrow.id);
						curDiag.arrows.remove(aid);
					} else {
						curDiag.changed = true;

						// checkCompatibility(curDiag.currentArrow);

						/*
						 * if (to != null) { if (side == Side.TOP) curDiag.currentArrow.toY = to.cy -
						 * to.height / 2; else if (side == Side.BOTTOM) curDiag.currentArrow.toY = to.cy
						 * + to.height / 2; else if (side == Side.LEFT) curDiag.currentArrow.toX = to.cx
						 * - to.width / 2; else if (side == Side.RIGHT) curDiag.currentArrow.toX = to.cx
						 * + to.width / 2; }
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

				// if (currentArrow != null) {

				if (!(between(xa, currentArrow.toX - zWS / 2, // what is this?!
						currentArrow.toX + zWS / 2)
						&& between(ya, currentArrow.toY - zWS / 2, currentArrow.toY + zWS / 2))) {
					// curDiag.currentArrow.toX = xa;
					// curDiag.currentArrow.toY = ya;
					// }
					// else {
					Integer aid = Integer.valueOf(currentArrow.id);
					curDiag.arrows.remove(aid);
					foundBlock = null;
					currentArrow = null;
					repaint();
					return;
				}
				// repaint();
				// }

				if (currentArrow.bends == null) {
					currentArrow.bends = new LinkedList<Bend>();
				}
				x = xa;
				y = ya;
				//currentArrow.endX2 = x;
				//currentArrow.endY2 = y;

				if (xa != currentArrow.lastX) {
					double s = ya - currentArrow.lastY;
					double t = xa - currentArrow.lastX;
					s = s / t;
					if (Math.abs(s) < FORCE_HORIZONTAL) // force horizontal
						ya = currentArrow.lastY;
					if (Math.abs(s) > FORCE_VERTICAL) // force vertical
						xa = currentArrow.lastX;
				}

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

		}

		@SuppressWarnings("unused")
		void colourArrows(int x, int y) {

			// following loop is just to colour arrows - x and y are the position of the
			// cursor

			for (Arrow arr : curDiag.arrows.values()) {
				int seg = 0;
				if (arr.bends != null) {
					for (Bend bend : arr.bends) {
						if (nearpln(x, y, arr)) {
							seg = arr.highlightedSeg;
							break;
						}
						seg++;
					}
				}

				if (nearpln(x, y, arr)) {
					seg = arr.highlightedSeg;
					break;
				}
			}
		}

	}

	
}
