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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.*;

import math.geom2d.line.DegeneratedLine2DException;
import math.geom2d.line.Line2D;
import math.geom2d.Point2D;
import math.geom2d.line.StraightLine2D;

import java.awt.image.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;
import java.net.*;

import javax.imageio.ImageIO;

import com.jpaulmorrison.graphics.Arrow.Status;
import com.jpaulmorrison.graphics.DrawFBP.DllFilter;
import com.jpaulmorrison.graphics.DrawFBP.ExeFilter;
import com.jpaulmorrison.graphics.DrawFBP.FileChooserParm;
import com.jpaulmorrison.graphics.DrawFBP.JavaClassFilter;

import java.lang.reflect.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FontUIResource;

public class DrawFBP extends JFrame
		implements
			ActionListener,
			ChangeListener,
			ComponentListener {

	static final long serialVersionUID = 111L;
	static DrawFBP driver;

	JLabel diagDesc;

	JTextField jfl = null;
	JTextField jfs = null;
	JTextField jfv = null;

	JLabel scaleLab;

	Diagram curDiag = null;
	Diagram sbnDiag = null; // used for Enclosure excise
	Diagram origDiag = null; // do.

	File currentImageDir = null;

	JFrame frame;

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

	FileChooserParm[] fCPArray = new FileChooserParm[9];

	
	public static int DIAGRAM = 0;
	public static int IMAGE = 1;	
	public static int JHELP = 2;
	public static int JARFILE = 3;  // class-dependent
	public static int CLASS = 4;  // class-dependent
	public static int PROCESS = 5;  // class-dependent
	public static int NETWORK = 6;  // class-dependent   
	public static int DLL = 7; // class-dependent
	public static int EXE = 8;  // class-dependent

	JCheckBox grid;

	boolean leftButton;

	static final int gridUnitSize = 4; // can be static - try for now

	static final double FORCE_VERTICAL = 20.0; // can be static as this is a
												// slope

	static final double FORCE_HORIZONTAL = 0.05; // can be static as this is a
													// slope

	static final int REG_CREATE = 1;
	static final int MODIFY = 2;

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
	static Color grey = new Color(170, 244, 255); // sort of grey (?)
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

	Timer ttStartTimer = null;
	Timer ttEndTimer = null;
	boolean drawToolTip = false;
	boolean gotDllReminder = false;

	// constructor
	DrawFBP(String[] args) {
		if (args.length == 1)
			diagramName = args[0];
		// frame = new JFrame("DrawFBP");
		// int screenRes = Toolkit.getDefaultToolkit().getScreenResolution();
		try {
		scalingFactor = 1.0d;
		driver = this;

		diagDesc = new JLabel("  ");
		grid = new JCheckBox("Grid");

		properties = new HashMap<String, String>();

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

		// Following array entries are language-independent - they are copied
		// over to the array of the same name in the Diagram object during
		// Diagram initialization
		
		// The missing array entries are language-dependent, and are set during
		// diagram building or initialization; they may change if the current language is changed 

		fCPArray[DIAGRAM] = new FileChooserParm(DIAGRAM, "Diagram", "currentDiagramDir",
				"Specify diagram name in diagram directory", ".drw",
				driver.new DiagramFilter(), "Diagrams (*.drw)");

		fCPArray[IMAGE] = new FileChooserParm(IMAGE, "Image", "currentImageDir",
				"Image: ", ".png", driver.new ImageFilter(), "Image files");

		fCPArray[JHELP] = new FileChooserParm(JHELP, "Java Help file", "jhallJarFile",
				"Choose a directory for the JavaHelp jar file", ".jar",
				driver.new JarFileFilter(), "Help files");

		createAndShowGUI();
		} catch (NullPointerException e)
		{
			writePropertiesFile();
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

		frame = this;
		frame.setTitle("DrawFBP Diagram Generator");
		// SwingUtilities.updateComponentTreeUI(frame);
		// frame = new JFrame("DrawFBP Diagram Generator");
		frame.setUndecorated(false); // can't change size of JFrame title,
										// though!
		defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		frame.setCursor(defaultCursor);

		applyOrientation(frame);

		int w = (int) dim.getWidth();
		int h = (int) dim.getHeight();
		// maxX = (int) (w * .8);
		// maxY = (int) (h * .8);
		buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		// osg = buffer.createGraphics();
		osg = (Graphics2D) buffer.getGraphics();
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

		readPropertiesFile();

		properties.put("versionNo", "v" + VersionAndTimestamp.getVersion());
		properties.put("date", VersionAndTimestamp.getDate());

		if (null == (generalFont = properties.get("generalFont")))
			generalFont = "Arial";
		if (null == (fixedFont = properties.get("fixedFont")))
			fixedFont = "Courier";

		String dfs = properties.get("defaultFontSize");
		if (dfs == null) {
			defaultFontSize = 14.0f;
			dfs = "14.0";
		} else
			defaultFontSize = Float.parseFloat(dfs);

		properties.put("defaultFontSize", dfs);

		String dcl = properties.get("defaultCompLang");
		// if (dcl.equals("NoFlo")) // transitional!
		// dcl = "JSON";
		if (dcl == null) {
			currLang = findGLFromLabel("Java");
			// propertiesChanged = true;
		} else {
			if (dcl.equals("NoFlo")) // transitional!
				dcl = "JSON";
			currLang = findGLFromLabel(dcl);
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
		properties.put("additionalJarFiles", z);
		
		entries = dllFiles.entrySet().iterator();
		z = "";
		cma = "";

		while (entries.hasNext()) {
			Entry<String, String> thisEntry = entries.next();
			z += cma + thisEntry.getKey() + ":" + thisEntry.getValue();
			cma = ";";
		}
		properties.put("additionalDllFiles", z);

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

		jtp.setForeground(Color.BLACK);
		jtp.setBackground(Color.WHITE);

		BufferedImage image = loadImage("DrawFBP-logo-small.png");

		if (image != null) {
			favicon = new ImageIcon(image);
			frame.setIconImage(image);

		} else {
			MyOptionPane.showMessageDialog(frame,
					"Couldn't find file: DrawFBP-logo-small.png",
					MyOptionPane.ERROR_MESSAGE);
			// return null;
		}

		// frame.repaint();
		// frame.update(frame.getGraphics());
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		closeTabAction = new CloseTabAction();
		closeAppAction = new CloseAppAction();
		escapeAction = new EscapeAction();

		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent ev) {
				closeAppAction.actionPerformed(new ActionEvent(ev, 0, "CLOSE"));
			}

		});

		jtp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(escapeKS, "CLOSE");

		jtp.getActionMap().put("CLOSE", escapeAction);

		Container cont = frame.getContentPane();
		buildUI(cont);

		frame.add(Box.createRigidArea(new Dimension(0, 10)));

		String t = properties.get("x");
		int x = 0, y = 0, w2 = 1200, h2 = 800;
		if (t != null)
			x = Integer.parseInt(t);
		t = properties.get("y");
		if (t != null)
			y = Integer.parseInt(t);
		Point p = new Point(x, y);
		frame.setLocation(p);

		t = properties.get("width");
		if (t != null)
			w2 = Integer.parseInt(t);
		t = properties.get("height");
		if (t != null)
			h2 = Integer.parseInt(t);

		Dimension dim2 = new Dimension(w2, h2);
		frame.setPreferredSize(dim2);
		// frame.repaint();
		// Display the window.
		frame.pack();

		frame.setVisible(true);
		frame.addComponentListener(this);

		frame.repaint();

		// wDiff = frame.getWidth() - curDiag.area.getWidth();
		// hDiff = frame.getHeight() - curDiag.area.getHeight();

		diagramName = properties.get("currentDiagram");

		boolean small = (diagramName) == null ? false : true;

		if (!small) // try suppressing this...
			new SplashWindow(frame, 3000, this, small); // display
		// for 3.0 secs, or until mouse is moved

		if (diagramName != null) {
			actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
					"Open " + diagramName));
		}

		// frame.repaint();

	}

	private void buildUI(Container container) {

		buildPropDescTable();

		getNewDiag();

		MouseListener mouseListener = new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				int i = jtp.indexAtLocation(e.getX(), e.getY());
				if (i == -1)
					return;
				ButtonTabComponent b = (ButtonTabComponent) driver.jtp
						.getTabComponentAt(i);
				Diagram diag = b.diag;

				if (diag == null) {
					getNewDiag();
					// diag = new Diagram(driver);
					// b.diag = diag;
				}
				// curDiag = diag;

				frame.repaint();

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
		curDiag.area.setAlignmentX(Component.LEFT_ALIGNMENT);
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
		frame.pack();

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
		frame.setIconImage(image);
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

		ttStartTimer = new Timer(0, new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				drawToolTip = true;
				ttEndTimer.restart();
				repaint();
			}
		});
		ttEndTimer = new Timer(0, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				drawToolTip = false;
				ttStartTimer.stop();
				ttEndTimer.stop();
				repaint();
			}
		});
		// }

		ttStartTimer.setInitialDelay(2000); // 2 sec
		ttStartTimer.setDelay(600000);
		ttEndTimer.setInitialDelay(10000); // 10 secs
		ttEndTimer.setDelay(600000);
		ttStartTimer.start();

	}

	BufferedImage loadImage(String s) {

		InputStream is = this.getClass().getResourceAsStream("/" + s);

		BufferedImage image = null;
		if (is == null) {
			MyOptionPane.showMessageDialog(frame, "Missing icon: " + s,
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
			if (!(genLangs[i].label.equals("FBP"))) {
				gMenu[k] = new JMenuItem(genLangs[i].label);
				gnMenu.add(gMenu[k]);
				gMenu[k].addActionListener(this);
				k++;
			}
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
		menuItem = new JMenuItem("Generate .fbp code");
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
		repaint();

		return menuBar;
	}

	void getNewDiag() {
		Diagram diag = new Diagram(this);
		SelectionArea sa = getNewArea();
		diag.area = sa;
		int i = jtp.getTabCount();
		jtp.add(sa, new JLabel());
		ButtonTabComponent b = new ButtonTabComponent(jtp, this);
		jtp.setTabComponentAt(i, b);
		jtp.setSelectedIndex(i);
		b.diag = diag;
		diag.tabNum = i;

		curDiag = diag;
		diag.blocks = new ConcurrentHashMap<Integer, Block>();
		diag.arrows = new ConcurrentHashMap<Integer, Arrow>();	
		
		diag.fCParm[DIAGRAM] = fCPArray[DIAGRAM];	
		diag.fCParm[IMAGE] = fCPArray[IMAGE];	
		diag.fCParm[JHELP] = fCPArray[JHELP];	
		
		diag.fCParm[JARFILE] = new FileChooserParm(JARFILE, "Jar file", "javaFBPJarFile",
				"Choose a jar file for JavaFBP", ".jar",
				new JarFileFilter(), "Jar files");

				
		diag.fCParm[CLASS] = driver.new FileChooserParm(CLASS, "Class", "currentClassDir",
				"Select component from class directory", ".class",
				new JavaClassFilter(), "Class files");
		
		diag.fCParm[PROCESS] = driver.new FileChooserParm(PROCESS, "Process", diag.diagLang.srcDirProp, "Select "
				+ diag.diagLang.showLangs() + " component from directory",
				diag.diagLang.suggExtn, diag.diagLang.filter, "Components: "
						+ diag.diagLang.showLangs() + " " + diag.diagLang.showSuffixes());
		
		diag.fCParm[NETWORK] = driver.new FileChooserParm(NETWORK, "Code",
				diag.diagLang.netDirProp,
				"Specify file name for code",
				"." + diag.diagLang.suggExtn, diag.diagLang.filter,
				diag.diagLang.showLangs());	
		
		diag.fCParm[DLL] = driver.new FileChooserParm(DLL, "C# .dll file",
				"dllFileDir",
				"Specify file name for .dll file",
				".dll", new DllFilter(),
				".dll");	
		
		diag.fCParm[EXE] = driver.new FileChooserParm(EXE, "C# Executable",
				"exeDir",
				"Specify file name for .exe file",
				".exe", new ExeFilter(),
				".exe");	
				
				
		repaint();

		return;
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

			int i = jtp.getTabCount();
			if (i > 1 || curDiag.diagFile != null || curDiag.changed)
				getNewDiag();

			jtp.setSelectedIndex(curDiag.tabNum);

			frame.repaint();

			return;

		}

		// if (curDiag.compLang == null) {
		for (int j = 0; j < gMenu.length; j++) {
			if (e.getSource() == gMenu[j]) {
				GenLang gl = genLangs[j];

				currLang = gl;

				properties.put("defaultCompLang", currLang.label);
				// propertiesChanged = true;
				if (curDiag != null && curDiag.diagLang != currLang) {
					curDiag.diagLang = currLang;
					curDiag.changed = true;
				}
				changeLanguage(gl);

				MyOptionPane.showMessageDialog(frame,
						"Language group changed to " + currLang.showLangs());
				frame.repaint();

				return;
			}
		}

		// }

		if (s.equals("Generate .fbp code")) {

			if (curDiag == null || curDiag.blocks.isEmpty()) {
				MyOptionPane.showMessageDialog(frame, "No components specified",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

			if (curDiag.title == null || curDiag.title.equals("(untitled)")) {

				MyOptionPane.showMessageDialog(frame,
						"Untitled diagram - please do Save first",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

			CodeManager mc = new CodeManager(curDiag);
			if (!mc.genFbpCode())
				MyOptionPane.showMessageDialog(frame,
						"Error in code generation", MyOptionPane.ERROR_MESSAGE);

			return;
		}

		if (s.startsWith("Generate ")) {
			if (curDiag == null || curDiag.blocks.isEmpty()) {
				MyOptionPane.showMessageDialog(frame, "No components specified",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

			if (curDiag.title == null || curDiag.title.equals("(untitled)")) {

				MyOptionPane.showMessageDialog(frame,
						"Untitled diagram - please do Save first",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

			CodeManager mc = new CodeManager(curDiag);
			mc.genCode();

			return;

		}

		if (s.equals("Display Generated Code")) {

			File cFile = null;
			GenLang gl = curDiag.diagLang;

			// String ss = properties.get("currentImageDir");
			String ss = properties.get(gl.netDirProp);
			String name = curDiag.diagFile.getName();

			if (ss == null)
				ss = System.getProperty("user.home");

			File file = new File(ss);
			MyFileChooser fc = new MyFileChooser(file, curDiag.fCParm[NETWORK]);
			int i = name.indexOf(".drw");
			ss += File.separator + name.substring(0, i)
					+ curDiag.fCParm[NETWORK].fileExt;
			fc.setSuggestedName(ss);

			int returnVal = fc.showOpenDialog(true); // force saveAs

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

		if (s.equals("Clear Language Association")) {
			curDiag.diagLang = null;
			curDiag.changed = true;
			jtf.setText("");

			// curDiag.changeCompLang();
		}

		// }
		if (s.equals("Locate JavaFBP Jar File")) {

			locateJavaFBPJarFile();
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

		if (s.equals("Locate DrawFBP Help File")) {

			locateJhallJarFile();
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
			PrintableDocument pd = new PrintableDocument(frame.getContentPane(),
					this);

			// PrintableDocument.printComponent(frame.getContentPane());
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
			return;

		}
		if (s.equals("Toggle Pan Switch")) {
			panSwitch = !panSwitch;
			// if (panSwitch)
			// frame.setCursor(openPawCursor);
			// else
			// frame.setCursor(defaultCursor);
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
			int min_x, max_w, min_y, max_h;
			min_x = Math.max(1, curDiag.minX);
			max_w = curDiag.maxX - min_x;
			min_y = Math.max(1, curDiag.minY);
			max_h = curDiag.maxY - min_y;

			int w = curDiag.area.getWidth();
			int h = curDiag.area.getHeight();
			max_w = Math.min(max_w, w - min_x);
			max_h = Math.min(max_h, h - min_y) + 20;

			BufferedImage buffer2 = buffer.getSubimage(min_x, min_y, max_w,
					max_h);
			// BufferedImage buffer = (BufferedImage)createImage(w1, h1);

			// int w2 = buffer2.getWidth();
			// int h2 = buffer2.getHeight();

			BufferedImage combined = new BufferedImage(max_w, max_h + 100,
					BufferedImage.TYPE_INT_ARGB);
			Graphics g = combined.getGraphics();

			// Graphics g = buffer2.getGraphics();
			g.setColor(Color.WHITE);

			// g.fillRect(0, 0, w1, h1 + 100);
			g.drawImage(buffer2, 0, 0, null);
			// g.drawImage(buffer, 0, 0, null);
			// g.setColor(Color.RED);
			g.fillRect(0, max_h, max_w, 80);

			if (curDiag.desc != null) {
				Color col = g.getColor();
				g.setColor(Color.BLUE);
				Font f = fontg.deriveFont(Font.ITALIC, 18.0f);
				g.setFont(f);
				int x = combined.getWidth() / 2;
				// int x = buffer2.getWidth() / 2;
				FontMetrics metrics = g.getFontMetrics(f);
				String t = curDiag.desc;
				byte[] str = t.getBytes();
				int width = metrics.bytesWidth(str, 0, t.length());

				g.drawString(t, x - width / 2, buffer2.getHeight() + 40);
				g.setColor(col);
			}

			int i = curDiag.fCParm[IMAGE].prompt.indexOf(":");
			String fn;
			if (curDiag.diagFile == null)
				fn = "(null)";
			else
				fn = curDiag.diagFile.getName();

			curDiag.fCParm[IMAGE].prompt = curDiag.fCParm[IMAGE].prompt
					.substring(0, i) + ": " + fn;

			file = curDiag.genSave(null, fCPArray[IMAGE], combined);
			// file = curDiag.genSave(null, fCPArray[IMAGE], buffer2);
			if (file == null) {
				MyOptionPane.showMessageDialog(frame, "File not saved");
				// curDiag.imageFile = null;
				g.dispose();
				return;
			}

			// ImageIcon image = new ImageIcon(combined);
			// curDiag.imageFile = file;
			Date date = new Date();
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

			MyFileChooser fc = new MyFileChooser(currentImageDir,
					curDiag.fCParm[IMAGE]);

			int i = curDiag.diagFile.getName().indexOf(".drw");
			ss += File.separator + curDiag.diagFile.getName().substring(0, i)
					+ curDiag.fCParm[IMAGE].fileExt;
			fc.setSuggestedName(ss);

			int returnVal = fc.showOpenDialog(true); // set to saveAs

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

			BufferedImage buffer2 = new BufferedImage(1200, 2000,
					BufferedImage.TYPE_INT_RGB);
			try {
				buffer2 = ImageIO.read(fFile);
			} catch (IOException e2) {
				MyOptionPane.showMessageDialog(frame, "Could not get image",
						MyOptionPane.ERROR_MESSAGE);
			}
			ImageIcon image = new ImageIcon(buffer2);

			currentImageDir = new File(fFile.getParent());
			properties.put("currentImageDir", fFile.getParent());
			// propertiesChanged = true;

			// curDiag.imageFile = fFile;

			JDialog popup = new JDialog();
			popup.setTitle(fFile.getName());
			JLabel jLabel = new JLabel(image);
			jLabel.addComponentListener(this);
			JScrollPane jsp = new JScrollPane(
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

			jsp.getViewport().add(jLabel);
			Dimension dim = new Dimension(image.getIconWidth(),
					image.getIconHeight());
			jsp.getViewport().setPreferredSize(dim);
			jsp.getViewport().setBackground(Color.WHITE);
			jLabel.setBackground(Color.WHITE);
			popup.add(jsp, BorderLayout.CENTER);
			popup.setLocation(new Point(200, 200));
			popup.setBackground(Color.WHITE);
			// popup.addComponentListener(this);
			// popup.setPreferredSize(dim);
			popup.pack();
			popup.setVisible(true);
			popup.setAlwaysOnTop(true);
			popup.repaint();
			frame.repaint();
			return;
		}

		if (s.equals("Close Diagram")) {
			closeTab();
			return;
		}
		if (s.equals("Launch Help")) {

			// The following is based on
			// https://supportweb.cs.bham.ac.uk/documentation/tutorials/
			// docsystem/build/tutorials/javahelp/javahelp.html
			// plus reflection

			if (jHelpViewer == null) {

				if (jhallJarFile == null) {

					jhallJarFile = properties.get("jhallJarFile");
					boolean res = true;

					if (jhallJarFile == null) {
						int response = MyOptionPane.showConfirmDialog(frame,
								// "Locate it?",
								"Specify the location of the JavaHelp jar file -\n"
										+ "do a search on Maven Central for 'javahelp' - \n"
										+ "Group ID:javax.help Artifact ID:javahelp",
								"Locate it?", MyOptionPane.OK_CANCEL_OPTION);
						if (response == MyOptionPane.OK_OPTION)
							res = locateJhallJarFile();
						else {
							MyOptionPane.showMessageDialog(frame,
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
					MyOptionPane.showMessageDialog(frame,
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
					helpSetClass = cl.loadClass("javax.help.HelpSet");
				} catch (MalformedURLException e2) {
				} catch (ClassNotFoundException e2) {
				} catch (NoClassDefFoundError e2) {
				}

				if (helpSetClass == null) {
					MyOptionPane.showMessageDialog(frame,
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
						MyOptionPane.showMessageDialog(frame,
								"JHelp class not found in jar file",
								MyOptionPane.ERROR_MESSAGE);
						return;
					}
					Constructor conjh = jHelpClass.getConstructor(helpSetClass);
					jHelpViewer = (JComponent) conjh.newInstance(hs);

				} catch (Exception e2) {
					MyOptionPane.showMessageDialog(frame,
							"HelpSet could not be processed: " + e2,
							MyOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			// Create a new frame.
			popup2 = new JDialog(frame);
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

			// frame2.setPreferredSize(frame.getPreferredSize());
			// Add the created helpViewer to it.
			popup2.getContentPane().add(jHelpViewer);
			// Set a default close operation.
			popup2.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			// Make the frame visible.
			popup2.setVisible(true);
			popup2.pack();
			Dimension dim = frame.getSize();
			Point p = frame.getLocation();
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
					+ "*    Authors: J.Paul Rodker Morrison,              *\n"
					+ "*             Bob Corrick                          *\n"
					+ "*                                                  *\n"
					+ "*    Copyright 2009, ..., 2017                     *\n"
					+ "*                                                  *\n"
					+ "*    FBP web site: www.jpaulmorrison.com/fbp       *\n"
					+ "*                                                  *\n"
					+ "*               (" + dt + ")            " + sp2
					+ "       *\n"
					+ "*                                                  *\n"
					+ "****************************************************\n");

			ta.setFont(f);
			final JDialog popup = new JDialog(frame);
			popup.add(ta, BorderLayout.CENTER);
			Point p = frame.getLocation();
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
			frame.repaint();
			return;

		}

		if (s.equals("Edit Diagram Description")) { // Title of diagram
			// as a whole

			String ans = (String) MyOptionPane.showInputDialog(frame,
					"Enter or change text", "Modify diagram description",
					MyOptionPane.PLAIN_MESSAGE, null, null, curDiag.desc);

			if (ans != null/* && ans.length() > 0 */) {
				curDiag.desc = ans;
				curDiag.desc = curDiag.desc.replace('\n', ' ');
				curDiag.desc = curDiag.desc.trim();
				curDiag.changed = true;
				if (curDiag.parent != null)
					curDiag.parent.description = ans;
			}
			frame.repaint();
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

			if (null != createBlock(blockType, x, y))
				curDiag.changed = true;
			frame.repaint();
			return;

		}
		if (s.equals("Block-related Actions")) {
			Block b = selBlock;

			if (b == null) {
				MyOptionPane.showMessageDialog(frame, "Block not selected",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}
			curDiag = b.diag;
			b.buildBlockPopupMenu();
			use_drag_icon = false;
			curDiag.jpm.show(frame, x + 100, y + 100);
			frame.repaint();
			return;

		}
		if (s.equals("Arrow-related Actions")) {
			Arrow a = selArrow;
			if (a == null) {
				MyOptionPane.showMessageDialog(frame, "Arrow not selected",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}
			a.buildArrowPopupMenu();
			curDiag = a.diag;
			curDiag.jpm.show(frame, a.toX + 100, a.toY + 100);
			frame.repaint();
			return;
		}

		setBlkType(s);

		frame.repaint();
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
		properties.put("defaultCompLang", gl.label);
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
		curDiag.filterOptions[0] = gl.showLangs();

		curDiag.fCParm[DrawFBP.PROCESS] = driver.new FileChooserParm(PROCESS, "Process",
				gl.srcDirProp,
				"Select " + gl.showLangs() + " component from directory",
				gl.suggExtn, gl.filter,
				"Components: " + gl.showLangs() + " " + gl.showSuffixes());

		curDiag.fCParm[DrawFBP.NETWORK] = driver.new FileChooserParm(NETWORK, "Code",
				gl.netDirProp, "Specify file name for code", "." + gl.suggExtn,
				gl.filter, gl.showLangs());

		frame.repaint();

	}

	Block createBlock(String blkType, int xa, int ya) {
		Block block = null;
		boolean oneLine = false;
		if (blkType == Block.Types.PROCESS_BLOCK) {
			block = new ProcessBlock(curDiag);
			block.isSubnet = willBeSubnet;
		}

		else if (blkType == Block.Types.EXTPORT_IN_BLOCK
				|| blkType == Block.Types.EXTPORT_OUT_BLOCK
				|| blkType == Block.Types.EXTPORT_OUTIN_BLOCK) {
			oneLine = true;
			block = new ExtPortBlock(curDiag);
		}

		else if (blkType == Block.Types.FILE_BLOCK)
			block = new FileBlock(curDiag);

		else if (blkType == Block.Types.IIP_BLOCK) {
			oneLine = true;
			block = new IIPBlock(curDiag);
		}

		else if (blkType == Block.Types.LEGEND_BLOCK)
			block = new LegendBlock(curDiag);

		else if (blkType == Block.Types.ENCL_BLOCK) {
			oneLine = true;
			block = new Enclosure(curDiag);
			Point pt = curDiag.area.getLocation();
			int y = Math.max(ya - block.height / 2, pt.y + 6);
			block.cy = ((ya + block.height / 2) + y) / 2;
		}

		else if (blkType == Block.Types.PERSON_BLOCK)
			block = new PersonBlock(curDiag);

		else if (blkType == Block.Types.REPORT_BLOCK)
			block = new ReportBlock(curDiag);
		else
			return null;

		block.type = blkType;

		block.cx = xa;
		block.cy = ya;
		if (block.cx == 0 || block.cy == 0)
			return null; // fudge!

		// if (enterDesc) {
		if (oneLine) {
			if (blkType != Block.Types.ENCL_BLOCK) {
				String d = "Enter description";
				String ans = (String) MyOptionPane.showInputDialog(driver.frame,
						"Enter text", d, MyOptionPane.PLAIN_MESSAGE, null, null,
						block.description);

				if (ans == null)
					return null;
				else
					block.description = ans;
			}
		} else if (!block.editDescription(REG_CREATE))
			return null;

		if (blkType == Block.Types.IIP_BLOCK) {
			IIPBlock ib = (IIPBlock) block;
			block.description = ib.checkNestedChars(block.description);
		}
		// }
		block.calcEdges();
		// curDiag.maxBlockNo++;
		// block.id = curDiag.maxBlockNo;
		curDiag.blocks.put(new Integer(block.id), block);
		// curDiag.changed = true;
		selBlock = block;
		// selArrowP = null;
		return block;
	}

	void buildPropDescTable() {
		propertyDescriptions = new LinkedHashMap<String, String>();

		propertyDescriptions.put("Version #", "versionNo");
		propertyDescriptions.put("Date", "date");
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

		JPanel panel = new JPanel(new GridBagLayout());
		JScrollPane jsp = new JScrollPane(panel,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		jf.setFocusable(true);
		jf.requestFocusInWindow();

		jf.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ev) {
				if (ev.getKeyCode() == KeyEvent.VK_ESCAPE) {
					jf.dispose();
				}
			}
		});

		panel.setBackground(Color.GRAY);
		panel.setLocation(frame.getX() + 50, frame.getY() + 50);
		panel.setSize(1200, 800);

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
		Point p = frame.getLocation();
		jf.setLocation(p.x + 150, p.y + 50);
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
		frame.repaint();
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

		int i = jtp.getTabCount();
		// ButtonTabComponent b = (ButtonTabComponent) jtp.getTabComponentAt(i);
		// curDiag = b.diag;

		if (i > 1 || curDiag.diagFile != null || curDiag.changed)
			getNewDiag();
		jtp.setSelectedIndex(curDiag.tabNum);

		file = curDiag.open(file);
		if (file == null) {
			// CloseTabAction closeTabAction = new CloseTabAction();
			// closeTabAction.actionPerformed(new ActionEvent(jtp, 0, "CLOSE"));
			closeTab();
			// curDiag = null;
			return null;
		}

		fname = file.getName();
		curDiag.diagFile = file;

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
		if (curDiag.title.toLowerCase().endsWith(".drw"))
			curDiag.title = curDiag.title.substring(0,
					curDiag.title.length() - 4);

		frame.setTitle("Diagram: " + curDiag.title);
		// curDiag.tabNum = i;
		jtp.setSelectedIndex(curDiag.tabNum);
		frame.repaint();
		return file;
	}

	void saveAction(boolean saveAs) {

		File file = null;
		if (curDiag.diagFile == null)
			saveAs = true;
		if (!saveAs)
			file = curDiag.diagFile;

		file = curDiag.genSave(file, fCPArray[DIAGRAM], null);

		int i = jtp.getSelectedIndex();
		if (file == null) {
			return;
		}

		jtp.setSelectedIndex(i);

		// curDiag.tabNum = i;

		curDiag.title = file.getName();
		curDiag.diagFile = file;
		if (curDiag.title.toLowerCase().endsWith(".drw"))
			curDiag.title = curDiag.title.substring(0,
					curDiag.title.length() - 4);

		File currentDiagramDir = file.getParentFile();
		frame.setTitle("Diagram: " + curDiag.title);
		properties.put("currentDiagramDir",
				currentDiagramDir.getAbsolutePath());
		// propertiesChanged = true;

		curDiag.changed = false;
		frame.repaint();
	}

	/*
	 * static String makeRelFileName(String current, String parent) { if
	 * (!(current.startsWith("/") || current.substring(1, 2).equals(":"))) {
	 * return current; } String res = ""; String cur = current.replace('\\',
	 * '/'); String curLead; String par = parent.replace('\\', '/'); String
	 * parLead;
	 * 
	 * while (true) { int i = par.indexOf("/"); if (i == -1) break; par =
	 * par.substring(i + 1); res += "../"; } int is = 0, js = 0; int i = 0; par
	 * = parent.replace('\\', '/'); // restore full length par
	 * 
	 * while (true) { i = cur.indexOf("/", is); if (i == -1) break; curLead =
	 * cur.substring(0, i);
	 * 
	 * int j = par.indexOf("/", js); if (j == -1) break; parLead =
	 * par.substring(0, j);
	 * 
	 * if (!(parLead.equals(curLead))) break;
	 * 
	 * res = res.substring(3); is = i + 1; js = j + 1; } return res +
	 * cur.substring(is); }
	 */
	static String makeAbsFileName(String current, String parent) {
		if (current.equals(""))
			return parent;
		if (current.startsWith("/"))
			return current;
		if (current.length() > 1 && current.substring(1, 2).equals(":"))
			return current;

		String cur = current.replace('\\', '/');
		String par = parent.replace('\\', '/');

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
	public static BufferedImage readImageFromFile(File file)
			throws IOException {
		return ImageIO.read(file);
	}

	public void menuDeselected(MenuEvent e) {
	}

	public void menuCanceled(MenuEvent e) {
	}

	void changeFonts() {
		fontChooser = new MyFontChooser(frame, this);
		chooseFonts(fontChooser);

		if (gFontChanged) {
			properties.put("generalFont", generalFont);
			// propertiesChanged = true;

			jfl.setText("Fixed font: " + fixedFont + "; general font: "
					+ generalFont);
			fontg = new Font(generalFont, Font.PLAIN, (int) defaultFontSize);
			adjustFonts();
			frame.repaint();
			// repaint();
		}

		if (fFontChanged) {
			properties.put("fixedFont", fixedFont);
			// propertiesChanged = true;

			jfl.setText("Fixed font: " + fixedFont + "; general font: "
					+ generalFont);
			fontf = new Font(fixedFont, Font.PLAIN, (int) defaultFontSize);
			adjustFonts();
			frame.repaint();
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
		Float fs = (Float) MyOptionPane.showInputDialog(frame,
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
		// frame.repaint();
		properties.put("defaultFontSize", Float.toString(defaultFontSize));
		// propertiesChanged = true;
		MyOptionPane.showMessageDialog(frame, "Font size changed");
		frame.repaint();
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
		// Hashtable<String, Font> ht = new Hashtable<String, Font>();
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

		frame.setJMenuBar(menuBar);

		frame.repaint();
	}

	void compileCode() {

		File cFile = null;
		GenLang gl = curDiag.diagLang;
		Process proc = null;
		//String program = "";
		if (currLang.label.equals("Java")) {
			String ss = properties.get(gl.netDirProp);
			File genDir = null;
			if (ss == null)
				genDir = new File(System.getProperty("user.home"));
			else
				genDir = new File(ss);

			MyFileChooser fc = new MyFileChooser(genDir,
					curDiag.fCParm[NETWORK]);

			int returnVal = fc.showOpenDialog();

			cFile = null;
			if (returnVal == MyFileChooser.APPROVE_OPTION) {
				cFile = new File(getSelFile(fc));
			}
			// }
			if (cFile == null || !(cFile.exists()))
				return;

			String source = curDiag.readFile(cFile, false);
			int m = source.indexOf("package");
			String pkg = "(null)";
			if (m > -1) {
				int n = source.substring(m + 8).indexOf(";");
				pkg = source.substring(m + 8, n + m + 8);
				pkg = pkg.trim();
			}
			properties.put("currentPackageName", pkg);

			String srcDir = cFile.getAbsolutePath();
			srcDir = srcDir.replace('\\', '/');
			int j = srcDir.lastIndexOf("/");

			String progName = srcDir.substring(j + 1);
			srcDir = srcDir.substring(0, j);
			(new File(srcDir)).mkdirs();
			properties.put(gl.netDirProp, srcDir);

			String clsDir;
			String t;
			int k = srcDir.indexOf("/src");
			if (k > -1) {
				if (j < k + 5)
					t = "";
				else {
					t = cFile.getAbsolutePath().substring(k + 5, j) + "/";
					t = t.replace("\\", "/");
				}
				clsDir = srcDir.replace("src", "bin");
				srcDir = srcDir.substring(0, k + 4); // drop after src
			} else {
				srcDir = srcDir.substring(0, j);
				clsDir = srcDir;
				t = "";
			}
			properties.put("currentClassDir", clsDir);
			clsDir = clsDir.substring(0, k + 4); // drop after bin

			File fd = new File(clsDir);

			pkg = pkg.replace(".", "/");

			if (fd == null || !fd.exists()) {
				fd.mkdirs();
				// driver.properties.put("currentClassDir", clsDir);
				MyOptionPane.showMessageDialog(frame,
						"'bin' directory created - " + clsDir,
						MyOptionPane.INFORMATION_MESSAGE);
			}

			if (javaFBPJarFile == null)
				locateJavaFBPJarFile();

			// String clsName = progName.replace(".java", ".class");

			// (new File(clsDir + "/" + t + clsName)).delete(); // make sure old
			// class has been deleted

			MyOptionPane.showMessageDialog(frame,
					"Compiling program - " + srcDir + "/" + t + progName,
					MyOptionPane.INFORMATION_MESSAGE);

			proc = null;
			
			String jf = "\"" + javaFBPJarFile; 
			for (String jfv : jarFiles.values()) {
				jf += ";" + jfv;
			}
			jf += ";.\"";

			ProcessBuilder pb = new ProcessBuilder("javac", "-cp", jf,
					"-d", "\"" + clsDir + "\"",
					"-sourcepath", "\"" + srcDir + "\"", "-Xlint:unchecked",
					"\"" + t + progName + "\"");

			pb.directory(new File(srcDir));

			pb.redirectErrorStream(true);

			// int i = 0;
			try {
				proc = pb.start();

				BufferedReader br = new BufferedReader(
						new InputStreamReader(proc.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					System.out.println(line);
					// System.out.flush();
				}
			} catch (NullPointerException npe) {
				// i = 1;
			} catch (IOException ioe) {
				// i = 2;
			} catch (IndexOutOfBoundsException iobe) {
				// i = 3;
			} catch (SecurityException se) {
				// i = 4;
			}
			if (proc == null) {
				MyOptionPane.showMessageDialog(frame,
						"Compile error - " + clsDir + "/" + progName,
						MyOptionPane.ERROR_MESSAGE);
				return;
			} else {
				try {
					proc.waitFor();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				proc.destroy();
				int u = proc.exitValue();

				if (u == 0)
					MyOptionPane.showMessageDialog(frame,
							"Program compiled - " + srcDir + "/" + t + progName
									+ "\n" + "   into - " + clsDir + "/" + pkg
									+ "/"
									+ progName.substring(0,
											progName.length() - 5)
									+ ".class",
							MyOptionPane.INFORMATION_MESSAGE);
				else
					MyOptionPane.showMessageDialog(frame,
							"Program compile failed, rc: " + u + " - " + srcDir
									+ "/" + t + progName,
							MyOptionPane.WARNING_MESSAGE);
			}
		}

		else {

			if (!(currLang.label.equals("C#"))) {

				MyOptionPane.showMessageDialog(frame,
						"Language not supported: " + currLang.label,
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

			// Start of C# part...

			String srcDir = properties.get("currentCsharpNetworkDir");
			
			if (srcDir == null)
				srcDir = System.getProperty("user.home");	

			MyFileChooser fc = new MyFileChooser(new File(srcDir),
					curDiag.fCParm[PROCESS]);

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
				MyOptionPane.showMessageDialog(frame,
						"C# program " + ss + " must end in '.cs'",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

			ss = ss.replace('\\', '/');
			int j = ss.lastIndexOf("/");

			String progName = ss.substring(j + 1);

			// ss = ss.substring(0, ss.length() - 3); // drop .cs suffix

			String progString = curDiag.readFile(new File(ss), false);
			if (progString == null) {
				MyOptionPane.showMessageDialog(frame,
						"Program not found: " + ss, MyOptionPane.ERROR_MESSAGE);
				return;
			}

			int k = progString.indexOf("namespace ");

			j = progString.substring(k + 10).indexOf(" ");
			int j2 = progString.substring(k + 10).indexOf("\n");
			int j3 = progString.substring(k + 10).indexOf("\r");
			j = Math.min(j, j2);
			j = Math.min(j, j3);
			String t = "";
			String v = "";
			if (k > -1) {
				v = progString.substring(k + 10, j + k + 10); // get name of
																// namespace
				v = v.replace(".", "/");
				t = cFile.getAbsolutePath();
				t = t.replace("\\", "/");
				k = t.indexOf(v);
				srcDir = ss.substring(0, k); // drop before namespace
														// string
			}
			if (srcDir.endsWith("/"))
				srcDir = srcDir.substring(0, srcDir.length() - 1);
			
			(new File(srcDir)).mkdirs();
			driver.properties.put("currentCsharpNetworkDir",
					srcDir);

			File target = new File(srcDir + "/" + v + "/bin/Debug");
			target.mkdirs();
			
			MyOptionPane.showMessageDialog(frame,
					"Starting compile - " + srcDir + "/" + v + "/" + "*.cs",
					MyOptionPane.INFORMATION_MESSAGE);

			proc = null;
			progName = progName.substring(0, progName.length() - 3); // drop .cs
			
			String z = properties.get("additionalDllFiles");
			boolean gotDlls = -1 < z.indexOf("FBPLib") && -1 < z.indexOf("FBPVerbs");
			
			//z = "";
			
			//String w = "csc /t:exe \"/out:" + srcDir + "/" + v + "/bin/Debug/" + v + ".exe\"";
			List<String> cmdList = new ArrayList<String>();
            cmdList.add("csc");
            cmdList.add("/t:exe");
            //cmdList.add("/out:");
            cmdList.add("\"/out:" + srcDir + "/" + v + "/bin/Debug/" + v + ".exe\"");
            			
			if (!gotDlls  && !gotDllReminder) {
				MyOptionPane.showMessageDialog(frame,
						"If you are using FBP, you will need a FBPLib dll and a FBPVerbs dll - use File/Add Additional Dll File",
						MyOptionPane.WARNING_MESSAGE);
				gotDllReminder = true;
			}
			
			else {
				Iterator<Entry<String, String>> entries = dllFiles.entrySet().iterator();
				//z = "";
				//String cma = "";

				 
				String w = "";
				while (entries.hasNext()) {
					Entry<String, String> thisEntry = entries.next();
					if (!(new File(thisEntry.getValue()).exists()))
						MyOptionPane.showMessageDialog(frame,
								"Dll file does not exist: " + thisEntry.getValue(),
								MyOptionPane.WARNING_MESSAGE);
					//z += "\"/r:" + thisEntry.getValue() + "\" ";
					//cma = ";";
					w = thisEntry.getValue();
					w = w.replace("\\", "/");
					cmdList.add("\"/r:" + w + "\"");
				}
				 
				//cmdList.add("\"/r:C:/Users/Paul/My Documents/GitHub/csharpfbp/FBPLib/bin/Debug/FBPLib.dll\"");
				//cmdList.add("\"/r:C:/Users/Paul/My Documents/GitHub/csharpfbp/FBPVerbs/bin/Debug/FBPVerbs.dll\"");
			}					
			
			cmdList.add("\"" + srcDir + "/" + v + "/" + "*.cs\"");			
			
			ProcessBuilder pb = new ProcessBuilder(cmdList);

			pb.directory(new File(srcDir + "/" + v));
			
			pb.redirectErrorStream(true);
			int i = 0;
			try {
				proc = pb.start();

				BufferedReader br = new BufferedReader(
						new InputStreamReader(proc.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					System.out.println(line);
					// System.out.flush();
				}
			} catch (NullPointerException npe) {
				i = 1;
			} catch (IOException ioe) {
				i = 2;
			} catch (IndexOutOfBoundsException iobe) {
				i = 3;
			} catch (SecurityException se) {
				i = 4;
			}

			//program = v + "/" + progName + ".cs";
			if (proc == null) {
				MyOptionPane
						.showMessageDialog(frame,
								"Program compile and run error - " + srcDir
										+ "/" + v + "/" + "*.cs",
								MyOptionPane.ERROR_MESSAGE);
				return;
			} else {
				try {
					proc.waitFor();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				proc.destroy();
			}

			int u = proc.exitValue();

			if (u == 0) {

				MyOptionPane.showMessageDialog(frame,
						"Programs compiled and linked - " + srcDir + "/" + v + "/"
								+ "*.cs\n" + "   into - " +  srcDir + "/" + v + "/bin/Debug/" + v + ".exe",
						MyOptionPane.INFORMATION_MESSAGE);
				properties.put("exeDir", srcDir + "/" + v)  ;
			}
			else
				MyOptionPane.showMessageDialog(frame,
						"Program compile failed, rc: " + u + " - " + progName,
						MyOptionPane.WARNING_MESSAGE);
		}
	}

	void runCode() {

		File cFile = null;
		String program = "";
		Process proc = null;
		if (currLang.label.equals("Java")) {

			String ss = properties.get("currentClassDir");
			File clsDir = null;
			if (ss == null)
				clsDir = new File(System.getProperty("user.home"));
			else
				clsDir = new File(ss);

			MyFileChooser fc = new MyFileChooser(clsDir, curDiag.fCParm[CLASS]);

			int returnVal = fc.showOpenDialog();

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
				MyOptionPane.showMessageDialog(frame,
						"Executable " + ss + " must end in '.class'",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

			ss = ss.replace('\\', '/');
			int j = ss.lastIndexOf("/");

			String progName = ss.substring(j + 1);

			// if (currLang.label.equals("Java"))
			ss = ss.substring(0, ss.length() - 6); // drop .class suffix

			int k = ss.indexOf("/bin");
			String t = "";
			if (k > -1) {
				if (j > k + 5) {
					t = cFile.getAbsolutePath().substring(k + 5, j);
					t = t.replace("\\", "/");
				}

				clsDir = new File(ss.substring(0, k + 4)); // drop after bin
			}

			clsDir.mkdirs();
			driver.properties.put("currentClassDir", clsDir.getAbsolutePath());

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

			Method meth = null;
			try {
				meth = cls.getMethod("main", String[].class);
			} catch (NoSuchMethodException | SecurityException e2) {
				meth = null;
				MyOptionPane.showMessageDialog(frame,
						"Program " + progName + " has no 'main' method",
						MyOptionPane.ERROR_MESSAGE);
			}
			if (meth == null) {

				return;
			}

			// if(javaFBPJarFile == null)
			// locateJavaFBPJarFile();

			MyOptionPane.showMessageDialog(frame,
					"Starting program - " + clsDir + "/" + progName,
					MyOptionPane.INFORMATION_MESSAGE);

			proc = null;
			ProcessBuilder pb = new ProcessBuilder("java", "-cp",
					"\"" + javaFBPJarFile + ";.\"", "\"" + progName + "\"");

			pb.directory(clsDir);

			pb.redirectErrorStream(true);

			// int i = 0;
			try {
				proc = pb.start();

				BufferedReader br = new BufferedReader(
						new InputStreamReader(proc.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					System.out.println(line);
					// System.out.flush();
				}
			} catch (NullPointerException npe) {
				// i = 1;
			} catch (IOException ioe) {
				// i = 2;
			} catch (IndexOutOfBoundsException iobe) {
				// i = 3;
			} catch (SecurityException se) {
				// i = 4;
			}
			if (proc == null) {
				MyOptionPane.showMessageDialog(frame,
						"Program error - " + clsDir + "/" + progName,
						MyOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				proc.waitFor();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			proc.destroy();

			// int u = proc.exitValue();
			program = clsDir + "/" + progName + ".java";
		}

		else {

			if (!(currLang.label.equals("C#"))) {

				MyOptionPane.showMessageDialog(frame,
						"Language not supported: " + currLang.label,
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

			// ----------------
			String exeDir = properties.get("exeDir");
			if (exeDir == null)
				exeDir = System.getProperty("user.home");

			ProcessBuilder pb = null;
			MyFileChooser fc = new MyFileChooser(new File(exeDir),
					curDiag.fCParm[EXE]);

			int returnVal = fc.showOpenDialog();

			cFile = null;
			String exeFile = "";
			if (returnVal == MyFileChooser.APPROVE_OPTION) {
				exeFile = getSelFile(fc);
			}

			if (!(exeFile.endsWith(".exe"))) {
				MyOptionPane.showMessageDialog(frame,
						"Executable " + exeFile + " must end in '.exe'",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

			int k = exeFile.lastIndexOf("bin/Debug/");
			exeDir = exeFile.substring(0, k + 10);
			
			properties.put("exeDir", exeDir);

			exeFile = exeFile.replace("\\",  "/");
			
			List<String> cmdList = new ArrayList<String>();
			
			cmdList.add("\"" + exeFile + "\"");
			
			program = exeFile.substring(exeFile.lastIndexOf("/") + 1);
			
			pb = new ProcessBuilder(cmdList);
			
			pb.directory(new File(exeDir));

			pb.redirectErrorStream(true);
			int i = 0;
			try {
				proc = pb.start();

				BufferedReader br = new BufferedReader(
						new InputStreamReader(proc.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					System.out.println(line);
					// System.out.flush();
				}
			} catch (NullPointerException npe) {
				i = 1;
			} catch (IOException ioe) {
				i = 2;
			} catch (IndexOutOfBoundsException iobe) {
				i = 3;
			} catch (SecurityException se) {
				i = 4;
			}
			if (proc == null) {
				MyOptionPane.showMessageDialog(frame, "Run error",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				proc.waitFor();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			proc.destroy();

		}
		// ---------------
		int u = proc.exitValue();
		if (u == 0)
			MyOptionPane.showMessageDialog(frame,
					"Program completed - " + program,
					MyOptionPane.INFORMATION_MESSAGE);
		else
			MyOptionPane.showMessageDialog(frame,
					"Program test failed, rc: " + u + " - " + program,
					MyOptionPane.WARNING_MESSAGE);

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
					uh + File.separator + "DrawFBPProperties.xml");

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
							properties.put(key, s);
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

								//properties.put("addnl_jf_" + u.substring(0, n),  
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

								//properties.put("addnl_jf_" + u.substring(0, n),
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
				String s = "<" + k + "> " + properties.get(k) + "</" + k
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

	/*
	 * 
	 * boolean getJavaFBPJarFile() { if (javaFBPJarFile == null) {
	 * javaFBPJarFile = properties.get("javaFBPJarFile"); }
	 * 
	 * File jf = null; boolean res = true; if (javaFBPJarFile != null) { jf =
	 * new File(javaFBPJarFile); } if (jf != null && jf.exists()) {
	 * //jarFiles.put("JavaFBP Jar File", jf.getAbsolutePath()); return res; }
	 * 
	 * String msg = null; if (jf != null) { if (!jf.exists()) msg =
	 * "Unable to read JavaFBP jar file: " + javaFBPJarFile; } else msg =
	 * "JavaFBP jar file missing";
	 * 
	 * if (msg != null) MyOptionPane.showMessageDialog(frame, msg,
	 * MyOptionPane.ERROR_MESSAGE);
	 * 
	 * int response = MyOptionPane.showConfirmDialog(frame,
	 * "Specify a JavaFBP jar file", "Locate JavaFBP jar file",
	 * MyOptionPane.OK_CANCEL_OPTION ); if (response == MyOptionPane.OK_OPTION)
	 * res = locateJavaFBPJarFile(); else {
	 * MyOptionPane.showMessageDialog(frame, "No JavaFBP jar file located",
	 * MyOptionPane.ERROR_MESSAGE); res = false; }
	 * 
	 * return res; }
	 */
	String getSelFile(MyFileChooser fc) {
		String[] sa = new String[1];
		fc.getSelectedFile(sa); // getSelectedFile puts result in sa[0]
		return sa[0];
	}

	boolean locateJavaFBPJarFile() {

		String s = properties.get("javaFBPJarFile");

		if (s != null) {
			javaFBPJarFile = s;
			return true;
		}

		MyOptionPane.showMessageDialog(frame,
				"Use File Chooser to locate JavaFBP jar file",
				MyOptionPane.WARNING_MESSAGE);

		File f = new File(System.getProperty("user.home"));

		// else
		// f = (new File(s)).getParentFile();

		MyFileChooser fc = new MyFileChooser(f, fCPArray[JARFILE]);

		int returnVal = fc.showOpenDialog();

		File cFile = null;
		if (returnVal == MyFileChooser.APPROVE_OPTION) {
			cFile = new File(getSelFile(fc));
			if (cFile == null || !(cFile.exists())) {
				MyOptionPane.showMessageDialog(frame,
						"Unable to read JavaFBP jar file " + cFile.getName(),
						MyOptionPane.ERROR_MESSAGE);
				return false;
			}
			// diag.driver.currentDir = new File(cFile.getParent());
			javaFBPJarFile = cFile.getAbsolutePath();
			properties.put("javaFBPJarFile", javaFBPJarFile);

			// propertiesChanged = true;
			MyOptionPane.showMessageDialog(frame,
					"JavaFBP jar file location: " + cFile.getAbsolutePath(),
					MyOptionPane.INFORMATION_MESSAGE);
			// jarFiles.put("JavaFBP Jar File", cFile.getAbsolutePath());
			for (int i = 0; i < driver.jtp.getTabCount(); i++) {
				ButtonTabComponent b = (ButtonTabComponent) driver.jtp
						.getTabComponentAt(i);

				Diagram d = b.diag;
				if (d == null)
					continue;

				for (Block bk : d.blocks.values()) {
					bk.getClassInfo(bk.fullClassName);
				}
			}
			return true;
		}
		return false;
	}

	boolean addAdditionalJarFile() {

		String ans = (String) MyOptionPane.showInputDialog(frame,
				"Enter Description of jar file being added",
				"Enter Description", MyOptionPane.PLAIN_MESSAGE, null, null,
				null);
		if (ans == null || ans.equals("")) {
			MyOptionPane.showMessageDialog(frame, "No description entered",
					MyOptionPane.ERROR_MESSAGE);
			return false;
		}

		String s = properties.get("javaFBPJarFile");
		File f = null;
		if (s == null)
			f = new File(System.getProperty("user.home"));
		else
			f = (new File(s)).getParentFile();
		MyFileChooser fc = new MyFileChooser(f, fCPArray[JARFILE]);

		int returnVal = fc.showOpenDialog();
		File cFile = null;
		if (returnVal == MyFileChooser.APPROVE_OPTION) {
			cFile = new File(getSelFile(fc));
			if (cFile == null || !(cFile.exists())) {
				MyOptionPane.showMessageDialog(frame,
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
			properties.put("additionalJarFiles", t);
			MyOptionPane.showMessageDialog(frame,
					"Additional jar file added: " + cFile.getName(),
					MyOptionPane.INFORMATION_MESSAGE);

			// propertiesChanged = true;

		}
		return true;
	}
	
	boolean addAdditionalDllFile() {

		String ans = (String) MyOptionPane.showInputDialog(frame,
				"Enter Description of dll file being added",
				"Enter Description", MyOptionPane.PLAIN_MESSAGE, null, null,
				null);
		if (ans == null || ans.equals("")) {
			MyOptionPane.showMessageDialog(frame, "No description entered",
					MyOptionPane.ERROR_MESSAGE);
			return false;
		}

		String s = properties.get("dllFileDir");
		File f = null;
		if (s == null)
			f = new File(System.getProperty("user.home"));
		else
			f = (new File(s)).getParentFile();
		MyFileChooser fc = new MyFileChooser(f, curDiag.fCParm[DLL]);

		int returnVal = fc.showOpenDialog();
		File cFile = null;
		if (returnVal == MyFileChooser.APPROVE_OPTION) {
			cFile = new File(getSelFile(fc));
			if (cFile == null || !(cFile.exists())) {
				MyOptionPane.showMessageDialog(frame,
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
			
			properties.put("additionalDllFiles", t);
			
			String u = cFile.getParent();
			properties.put("dllFileDir", u);
			MyOptionPane.showMessageDialog(frame,
					"Additional dll file added: " + cFile.getName(),
					MyOptionPane.INFORMATION_MESSAGE);

			// propertiesChanged = true;

		}
		return true;
	}


	boolean locateJhallJarFile() {

		String s = properties.get("jhallJarFile");
		// File f = null;
		if (s != null) {
			jhallJarFile = s;
			return true;
		}

		MyOptionPane.showMessageDialog(frame,
				"Use File Chooser to locate JavaHelp jar file",
				MyOptionPane.WARNING_MESSAGE);

		File f = new File(System.getProperty("user.home"));
		// else
		// f = (new File(s)).getParentFile();

		MyFileChooser fc = new MyFileChooser(f, fCPArray[JHELP]);

		int returnVal = fc.showOpenDialog();

		File cFile = null;
		if (returnVal == MyFileChooser.APPROVE_OPTION) {
			cFile = new File(getSelFile(fc));
			if (cFile == null || !(cFile.exists())) {
				MyOptionPane.showMessageDialog(frame,
						"Unable to read DrawFBP Help jar file "
								+ cFile.getName());
				return false;
			}
			// diag.driver.currentDir = new File(cFile.getParent());
			jhallJarFile = cFile.getAbsolutePath();
			properties.put("jhallJarFile", jhallJarFile);
			// propertiesChanged = true;
			MyOptionPane.showMessageDialog(frame,
					"DrawFBP Help jar file location: "
							+ cFile.getAbsolutePath());
			return true;
		} else
			return false;
	}

	void closeTab() {
		closeTabAction.actionPerformed(new ActionEvent(jtp, 0, "CLOSE"));
	}

	public static final void copyInputStream(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[1024];
		int len;

		while ((len = in.read(buffer)) >= 0) {
			// System.out.println(new String(buffer));
			out.write(buffer, 0, len);
		}
		out.flush();
		in.close();
		out.close();
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
	 * MyOptionPane.showMessageDialog(frame,
	 * "Select new project for sample networks");
	 * 
	 * // zip file must have "src", "test" and "diagrams" directories // and
	 * optionally components...
	 * 
	 * String zipname = "FBPSamples.zip"; InputStream is =
	 * this.getClass().getClassLoader() .getResourceAsStream(zipname);
	 * 
	 * String zfn = System.getProperty("user.home") + File.separator + zipname;
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
			locateJavaFBPJarFile();
		try {

			if (f != null)
				ll.add(f.toURI().toURL());

			File f2 = new File(javaFBPJarFile);
			ll.add(f2.toURI().toURL());

			for (String jfv : jarFiles.values()) {
				f2 = new File(jfv);
				ll.add(f2.toURI().toURL());
			}

			String curClsDir = properties.get("currentClassDir")
					+ File.separator;

			if (null != curClsDir) {
				f2 = new File(curClsDir);
				ll.add(f2.toURI().toURL());
			}

			urls = ll.toArray(new URL[ll.size()]);

		} catch (MalformedURLException e) {
			MyOptionPane.showMessageDialog(driver.frame, "Malformed URL: " + f,
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
				// frame.pack();
				// frame.setPreferredSize(new Dimension(1200, 800));
				// frame.repaint();
			}
		}
	}

	void drawBlueCircle(Graphics g, int x, int y, int opt) {
		Color col = g.getColor();

		g.setColor(Color.BLUE);
		g.drawOval(x - 3, y - 3, 6, 6);

		if (drawToolTip) {

			String s;

			if (opt == 1)
				s = "Click here to start an arrow";
			else if (opt == 2)
				s = "Hold button down to connect arrow to another block";
			else
				s = "Arrow not complete - click on block or line, or hit ESC";
			FontMetrics metrics = g.getFontMetrics(driver.fontg);
			byte[] str = s.getBytes();
			int w = metrics.bytesWidth(str, 0, s.length());
			g.setColor(Color.black);
			g.drawRect(x + 12, y + 10, w + 13, 23);
			g.setColor(ly);
			g.fillRect(x + 13, y + 11, w + 11, 21);
			Font font = g.getFont();
			g.setColor(Color.black);
			g.setFont(driver.fontg);
			g.drawString(s, x + 15, y + 28);
			g.setColor(col);
			g.setFont(font);

		}
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
		// dim2.width = 50;
		// but[4].setMinimumSize(dim2);
		box21.repaint();
	}

	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) {

		// System.out.println("Locale: " + Locale.getDefault() + "\n");

		// String osName = System.getProperty("os.name");
		// System.out.print("OS name: " + osName + "\n");

		String laf = UIManager.getSystemLookAndFeelClassName();
		// String nimbusLaf = null;

		/*
		 * //if (!(osName.startsWith("Win"))) { for (LookAndFeelInfo info :
		 * UIManager.getInstalledLookAndFeels()) {
		 * //System.out.print("Look and feel: " + info.getName() + "\n"); if
		 * ("Nimbus".equals(info.getName())) nimbusLaf = info.getClassName();
		 * //else // laf = info.getClassName(); } //}
		 * 
		 * if (nimbusLaf != null) { laf = nimbusLaf; //
		 * System.out.print("Look and feel: Nimbus\n"); } else
		 */
		// System.out.print("Look and feel: System \n");

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

		JFrame.setDefaultLookAndFeelDecorated(true);

		new DrawFBP(args);

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

	public class FileChooserParm {
		int index;
		String name;
		String propertyName;
		String prompt;
		String fileExt;
		FileFilter filter;
		String title;

		FileChooserParm(int n, String x, String a, String b, String c, FileFilter d,
				String e) {
			index = n;
			name = x;
			propertyName = a;
			prompt = b;
			fileExt = c;
			filter = d;
			title = e;
		}
	}

	public class CloseAppAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {

			boolean close = true;

			for (int i = 0; i < jtp.getTabCount(); i++) {
				ButtonTabComponent b = (ButtonTabComponent) jtp
						.getTabComponentAt(i);
				Diagram diag = b.diag;

				if (diag != null) {
					if (!diag.askAboutSaving()) {
						close = false;
						break;
					}
				}
			}
			// if (propertiesChanged) {
			writePropertiesFile();
			// }

			if (close) {
				frame.dispose();
				System.exit(0);
			}
		}
	}
	public class CloseTabAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {

			int i = jtp.getSelectedIndex();
			if (i == -1)
				return;
			ButtonTabComponent b = (ButtonTabComponent) jtp
					.getTabComponentAt(i);
			Diagram diag = b.diag;

			if (diag != null) {
				if (!diag.askAboutSaving())
					return;
			}

			jtp.removeTabAt(i);

			properties.remove("currentDiagram");

			int j = jtp.getTabCount();
			if (j == 0) {
				// make one tab with "(untitled)"
				// Diagram curDiag = new Diagram(driver);
				getNewDiag();
				frame.setTitle("Diagram: (untitled)");
			} else {
				jtp.setSelectedIndex(j - 1);
				b = (ButtonTabComponent) jtp.getTabComponentAt(j - 1);
				curDiag = b.diag;

				for (int k = i; k < j; k++) {
					b = (ButtonTabComponent) jtp.getTabComponentAt(k);
					diag = b.diag;
					diag.tabNum = k;
				}
			}

			frame.repaint();
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

			if (-1 < jtp.getSelectedIndex()) {
				closeTab();
				// else {
				ButtonTabComponent b = (ButtonTabComponent) jtp
						.getTabComponentAt(0);

				JLabel j = (JLabel) b.getComponent(0);
				String s = j.getText();

				if (1 == jtp.getTabCount()
						&& (s.equals("(untitled)") || s.equals(""))
						&& MyOptionPane.YES_OPTION == MyOptionPane
								.showConfirmDialog(frame, "Choose one option",
										"Leave DrawFBP?",
										MyOptionPane.YES_NO_OPTION)) {
					closeAppAction
							.actionPerformed(new ActionEvent(jtp, 0, "CLOSE"));
				}
			}
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

				osg.fillRect(0, 0, (int) (getWidth() / scalingFactor),
						(int) (getHeight() / scalingFactor - 0));
			}

			int i = jtp.getSelectedIndex();

			ButtonTabComponent b = (ButtonTabComponent) jtp
					.getTabComponentAt(i);
			Diagram diag = b.diag;

			// if (curDiag != diag) {
			// int x = 0; // problem!
			// }

			grid.setSelected(diag.clickToGrid);

			// repaint();
			// Graphics2D g2d = (Graphics2D) g;

			for (Block block : diag.blocks.values()) {
				block.draw(osg);
			}

			for (Arrow arrow : diag.arrows.values()) {
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

			g2d.scale(scalingFactor, scalingFactor);

			// g2d.translate(xTranslate, yTranslate);

			// Now copy that off-screen image onto the screen
			g2d.drawImage(buffer, 0, 0, null);

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
			drawToolTip = false;
			arrowRoot = null;
			arrowEnd = null;
			if (!ttEndTimer.isRunning())
				ttStartTimer.restart();
			else
				ttEndTimer.stop();

			repaint();
			ButtonTabComponent b = (ButtonTabComponent) jtp
					.getTabComponentAt(i);
			curDiag = b.diag;

			int x = (int) Math.round(e.getX() / scalingFactor);
			int y = (int) Math.round(e.getY() / scalingFactor);
			int xa, ya;

			if (panSwitch) {
				Rectangle r = curDiag.area.getBounds();
				r = new Rectangle(r.x, r.y, r.width - 20, r.height - 40);
				if (r.contains(x, y))
					frame.setCursor(openPawCursor);
				else
					frame.setCursor(defaultCursor);
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
							frame.setCursor(drag_icon);
						use_drag_icon = true;
					}

					break;
				}

			}
			if (selBlockM == null) {
				if (use_drag_icon)
					use_drag_icon = false;

				if (!panSwitch)
					frame.setCursor(defaultCursor);
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
				frame.repaint();
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
					frame.setCursor(closedPawCursor);
					panX = xa;
					panY = ya;
					return;
				} else
					frame.setCursor(defaultCursor);
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

			frame.repaint();

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
			if (!ttEndTimer.isRunning()) {
				drawToolTip = false;
				ttStartTimer.restart();
			}

			repaint();
			ButtonTabComponent b = (ButtonTabComponent) jtp
					.getTabComponentAt(i);
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
				// frame.repaint();
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
					enc.width = ox + ow / 2 - xa;
					enc.height = oy + oh / 2 - ya;
					enc.cx = xa + enc.width / 2;
					enc.cy = ya + enc.height / 2;
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
			curDiag = b.diag;

			int x = (int) e.getX();
			int y = (int) e.getY();

			if (curDiag.jpm != null) {
				curDiag.jpm.setVisible(false);
				curDiag.jpm = null;
				frame.repaint();
				return;
			}

			x = (int) Math.round(x / scalingFactor);
			y = (int) Math.round(y / scalingFactor);
			int xa, ya;

			Side side = null;
			Point2D p2 = new Point2D(x, y);
			p2 = gridAlign(p2);
			xa = (int) p2.x();
			ya = (int) p2.y();

			if (curDiag.area.contains(x, y) && panSwitch) {
				frame.setCursor(openPawCursor);
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
							if (blockSelForDragging.diagramFileName == null) {
								MyOptionPane.showMessageDialog(null,
										"No subnet diagram assigned",
										MyOptionPane.INFORMATION_MESSAGE);
							} else {
								File f = new File(
										blockSelForDragging.diagramFileName);
								Diagram saveCurDiag = curDiag;
								int tabno2 = curDiag
										.diagramIsOpen(f.getAbsolutePath());
								if (tabno2 > -1) {
									ButtonTabComponent b2 = (ButtonTabComponent) jtp
											.getTabComponentAt(tabno2);
									curDiag = b2.diag;
									// curDiag.tabNum = i;
									jtp.setSelectedIndex(tabno2);

									repaint();
									return;
								}
								if (null == openAction(f.getAbsolutePath()))
									curDiag = saveCurDiag;
								curDiag.parent = blockSelForDragging;
							}
						} else {
							blockSelForDragging.buildBlockPopupMenu();
							// if (frame == null)
							// curDiag = new Diagram(driver);

							curDiag = blockSelForDragging.diag;

							curDiag.jpm.show(frame, xa + 100, ya + 100);

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
				// MyOptionPane.showMessageDialog(frame,
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
						&& null != createBlock(blockType, xa, ya))
					curDiag.changed = true;
				frame.repaint();
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
				 * frame,
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
						int answer = MyOptionPane.showConfirmDialog(frame,
								"Connect arrow to bottom of block?",
								"Please choose one",
								MyOptionPane.YES_NO_OPTION);
						if (answer != MyOptionPane.YES_OPTION)
							OK = false;
					}
					if (side == Side.RIGHT) {
						int answer = MyOptionPane.showConfirmDialog(frame,
								"Connect arrow to righthand side?",
								"Please choose one",
								MyOptionPane.YES_NO_OPTION);
						if (answer != MyOptionPane.YES_OPTION)
							OK = false;
					}
				}
				if (!OK) {
					// MyOptionPane.showMessageDialog(frame,
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

				Boolean error = false;
				if (to instanceof IIPBlock && from instanceof ProcessBlock) {
					a2.reverseDirection();
					// MyOptionPane
					// .showMessageDialog(frame,
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
					MyOptionPane.showMessageDialog(frame,
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
								.showConfirmDialog(frame,
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
						MyOptionPane.showMessageDialog(frame,
								"Arrow in wrong direction",
								MyOptionPane.ERROR_MESSAGE);
					else if (to instanceof ExtPortBlock
							&& to.type.equals(Block.Types.EXTPORT_IN_BLOCK))
						MyOptionPane.showMessageDialog(frame,
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
