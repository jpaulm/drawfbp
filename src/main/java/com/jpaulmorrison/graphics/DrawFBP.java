package com.jpaulmorrison.graphics;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.event.*;

import java.util.*;
import java.util.Map.Entry;
import java.io.*;
import java.net.*;

import javax.imageio.ImageIO;

import com.jpaulmorrison.graphics.Arrow.Status;

import java.lang.reflect.*;

import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
//import javax.help.*;

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

	File currentImageDir = null;

	JFrame frame;

	Block blockSelForDragging = null;

	BufferedImage buffer = null;

	//int maxX, maxY;

	Graphics2D osg;

	// SelectionArea area;
	int gFontWidth, gFontHeight;

	//Enclosure enclSelForDragging = null;
	Enclosure enclSelForArrow = null;

	File propertiesFile = null;
	HashMap<String, String> properties = null;
	HashMap<String, String> startProperties = null;
	HashMap<String, String> propertyDescriptions = null; // description is key

	JTabbedPaneWithCloseIcons jtp;

	String javaFBPJarFile = null;
	String jhallJarFile = null;

	Block selBlockM = null;  // used only when mousing over 
	Block selBlock = null; // permanent select
	Arrow selArrow = null; // permanent select
	String generalFont = null;
	String fixedFont = null;
	Font fontf = null;
	Font fontg = null;

	float defaultFontSize;
	GenLang defaultCompLang;

	double scalingFactor;
	//int xTranslate = 0; // 400;
	//int yTranslate = 0; // 400;

	BasicStroke bs;

	boolean propertiesChanged = false;

	ImageIcon favicon = null;
	// String zipFileName;
	int ox = 0; // enclosure being dragged - cx
	int oy = 0; // enclosure being dragged - cy
	int ow = 0; // enclosure being dragged - width
	int oh = 0; // enclosure being dragged - height

	String diagramName = null;

	Arrow arrowEndForDragging = null;

	Bend bendForDragging = null;
	CloseTabAction closeTabAction = null;
	CloseAppAction closeAppAction = null;
	EscapeAction escapeAction = null;

	KeyStroke escapeKS = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);

	String blockType = Block.Types.PROCESS_BLOCK;
	
	FoundPoint arrowRoot = null;  // used to draw blue circles where arrows can start

	int curx, cury;

	GenLang genLangs[];

	FileChooserParms[] fCPArray = new FileChooserParms[7];

	public static int CLASS = 0;
	public static int DIAGRAM = 1;
	public static int IMAGE = 2;
	public static int JARFILE = 3;
	public static int JHALL = 4;
	public static int PROCESS = 5;
	public static int GENCODE = 6;

	JCheckBox grid;

	boolean leftButton;

	static final int gridUnitSize = 4; // can be static - try for now

	static final double FORCE_VERTICAL = 10.0; // can be static as this is a
												// slope

	static final double FORCE_HORIZONTAL = 0.1; // can be static as this is a
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
	
	String blockNames[] = {"Process", "Initial IP",
					"Enclosure", "Subnet", "ExtPorts: In", "... Out",
					"... Out/In", "Legend", "File", "Person", "Report"};
	
	String blockTypes[] = {Block.Types.PROCESS_BLOCK, Block.Types.IIP_BLOCK, Block.Types.ENCL_BLOCK, Block.Types.PROCESS_BLOCK,
			 Block.Types.EXTPORT_IN_BLOCK, Block.Types.EXTPORT_OUT_BLOCK, Block.Types.EXTPORT_OUTIN_BLOCK,
			 Block.Types.LEGEND_BLOCK, Block.Types.FILE_BLOCK, Block.Types.PERSON_BLOCK, Block.Types.REPORT_BLOCK,};	
	
	//String buttonNames[] = {"Process", "Initial IP",
	//		"Enclosure", "Subnet", "ExtPorts: In", "... Out",
	//		"... Out/In", "Legend", "File", "Person", "Report"};	
	
	HashMap<String, String> jarFiles = new HashMap<String, String> ();

	// JPopupMenu curPopup = null; // currently active popup menu

	//String scale;
	boolean tryFindJarFile = true;
	boolean willBeSubnet = false;

	JMenuBar menuBar = null;
	JMenu fileMenu = null;
	JMenu editMenu = null;
	JMenu helpMenu = null;
	
	JMenuItem gNMenuItem = null;
	JMenuItem[] gMenu = null;
	JMenuItem menuItem1 = null;
	JMenuItem menuItem2 = null;
	JTextField jtf = new JTextField();
	
	boolean allFiles = false;
	//int wDiff, hDiff;
	JComponent jHelpViewer = null;
	MyFontChooser fontChooser;
	boolean gFontChanged, fFontChanged;

	static Color lg = new Color(240, 240, 240); // very light gray
	static Color slateGray1 = new Color(198, 226, 255);
	//JDialog popup = null;
	JDialog popup2 = null;
	JDialog depDialog = null;

	static enum Side {
		LEFT, TOP, RIGHT, BOTTOM
	}
	//static boolean READFILE = true;
	
	Cursor defaultCursor = null;
	boolean use_drag_icon = false;
	
	JLabel zoom = new JLabel("Zoom");
	JCheckBox pan = new JCheckBox("Pan");
	JRadioButton[] but = new JRadioButton[11];
	Box box21 = null;

	// constructor
	DrawFBP(String[] args) {
		if (args.length == 1)
			diagramName = args[0];
		// frame = new JFrame("DrawFBP");
		// int screenRes = Toolkit.getDefaultToolkit().getScreenResolution();
		scalingFactor = 1.0d;
		driver = this;
		
		diagDesc = new JLabel("  ");
		grid = new JCheckBox("Grid");

		properties = new HashMap<String, String>();		
			
		genLangs = new GenLang[]{
				new GenLang("Java", "java",  new JavaFileFilter()),
				new GenLang("C#", "cs",  new CsharpFileFilter()),
				new GenLang("JSON", "json", new JSONFilter()),
				new GenLang("FBP", "fbp", new FBPFilter())
				};

		Lang lang0[] = new Lang[]{new Lang("Java", "java")
				//, new Lang("Groovy", "groovy"), new Lang("Scala", "scala")
		};
		genLangs[0].langs = lang0;

		Lang lang1[] = new Lang[]{new Lang("C#", "cs")};
		genLangs[1].langs = lang1;

		Lang lang2[] = new Lang[]{
				new Lang("JSON", "json")};
		genLangs[2].langs = lang2;
		
		Lang lang3[] = new Lang[]{new Lang("FBP", "fbp")};
		genLangs[3].langs = lang3;

		// Following array entries are language-independent - they are copied over
		//  to the array of the same name in the Diagram object
		// The missing array entries are language-dependent, and are set during diagram building or initialization
		
		
		fCPArray[DIAGRAM] = new FileChooserParms("Diagram", "currentDiagramDir",
				"Specify diagram name in diagram directory", ".drw",
				driver.new DiagramFilter(), "Diagrams (*.drw, *.fbp)");

		fCPArray[IMAGE] = new FileChooserParms("Image", "currentImageDir",
				"Image: ", ".png", driver.new ImageFilter(),
				"Image files");

		fCPArray[JARFILE] = new FileChooserParms("Jar file", "javaFBPJarFile",
				"Choose a jar file for JavaFBP", ".jar",
				driver.new JarFileFilter(), "Jar files");

		fCPArray[JHALL] = new FileChooserParms("Java Help file", "jhallJarFile",
				"Choose a directory for the JavaHelp jar file", ".jar",
				driver.new JarFileFilter(), "Help files");

		
		createAndShowGUI();
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
		SwingUtilities.updateComponentTreeUI(frame);
		// frame = new JFrame("DrawFBP Diagram Generator");
		frame.setUndecorated(false); // can't change size of JFrame title,
										// though!
		defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		frame.setCursor(defaultCursor);

		applyOrientation(frame);

		int w = (int) dim.getWidth();
		int h = (int) dim.getHeight();
		//maxX = (int) (w * .8);
		//maxY = (int) (h * .8);
		buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB); 
		osg = buffer.createGraphics();

		// http://www.oracle.com/technetwork/java/painting-140037.html

		bs = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, // width 1.5
				BasicStroke.JOIN_ROUND);
		osg.setStroke(bs);
		osg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		// UIDefaults def = UIManager.getLookAndFeelDefaults();
		// UIDefaults def = UIManager.getDefaults();

		readPropertiesFile();				
		
		properties.put("versionNo", "v" + VersionAndTimestamp.getVersion());
		properties.put("date", VersionAndTimestamp.getDate());
		
		if (null == (generalFont = properties.get("generalFont")))
			generalFont = "Arial";
		if (null == (fixedFont = properties.get("fixedFont")))
			fixedFont = "Courier";
		String dfs = properties.get("defaultFontSize");
		if (dfs == null)
			defaultFontSize = 14.0f;
		else
			defaultFontSize = Float.parseFloat(dfs);

		String dcl = properties.get("defaultCompLang");
		//if (dcl.equals("NoFlo"))    // transitional!
		//	dcl = "JSON";
		if (dcl == null) {
			defaultCompLang = findGLFromLabel("Java");
			propertiesChanged = true;
		} else {
			if (dcl.equals("NoFlo"))    // transitional!
				dcl = "JSON";
			defaultCompLang = findGLFromLabel(dcl);			

		}
		
		
		Iterator<Entry<String, String>> entries = jarFiles.entrySet().iterator();
		String z = "";
		String cma = "";
		
		while (entries.hasNext()) {
			Entry<String, String> thisEntry = entries.next();
			
				z += cma + thisEntry.getKey() + ":" + thisEntry.getValue();
				cma = ";";
			
		}
		properties.put("additionalJarFiles", z);

		startProperties = new HashMap<String, String>();
		for (String s : properties.keySet()) {
			startProperties.put(s, properties.get(s));
		}

		fontg = new Font(generalFont, Font.PLAIN, (int) defaultFontSize);

		// read/create time
		fontf = new Font(fixedFont, Font.PLAIN, (int) defaultFontSize);
		osg.setFont(fontg);

		FontMetrics metrics = osg.getFontMetrics(fontg);
		gFontWidth = metrics.charWidth('n'); // should be the average!
		gFontHeight = metrics.getAscent() + metrics.getLeading();

		jfl = new JTextField("");		

		jfl.setText("Fixed font: " + fixedFont + "; general font: " + generalFont);
		
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
					"Couldn't find file: DrawFBP-logo-small.png", MyOptionPane.ERROR_MESSAGE);
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

		jtp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				escapeKS, "CLOSE");

		jtp.getActionMap().put("CLOSE", escapeAction);

		Container cont = frame.getContentPane();
		buildUI(cont);		
		
		frame.add(Box.createRigidArea(new Dimension(0,10)));
		
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
		frame.repaint();
		// Display the window.
		frame.pack();

		frame.setVisible(true);
		frame.addComponentListener(this);
		
		frame.repaint();

		//wDiff = frame.getWidth() - curDiag.area.getWidth();
		//hDiff = frame.getHeight() - curDiag.area.getHeight();
		
		diagramName = properties.get("currentDiagram");
		
		boolean small = (diagramName) == null ? false : true;	
		
		if (!small)  // try suppressing this...
		    new SplashWindow(frame, 3000, this, small); // display
		    // for 3.0 secs, or until mouse is moved
		
	    if (diagramName != null)  {	
		    actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
		       "Open " + diagramName));
		}
		frame.repaint();
		
	}

	private void buildUI(Container container) {
		
		buildPropDescTable();
		
		curDiag = getNewDiag();				

		MouseListener mouseListener = new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				int i = jtp.indexAtLocation(e.getX(), e.getY());
				if (i == -1)
					return;
				ButtonTabComponent b = (ButtonTabComponent) driver.jtp
						.getTabComponentAt(i);
				Diagram diag = b.diag;

				if (diag == null) {
					curDiag = getNewDiag();
					// diag = new Diagram(driver);
					// b.diag = diag;
				}
				curDiag = diag;

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
		//grid.setAlignmentX(JComponent.LEFT_ALIGNMENT);
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
		//JScrollPane jsp = new JScrollPane();
		//box2.add(jsp);
		box2.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));  
		box1.add(box2);
		//box2.add(Box.createHorizontalGlue());
		box2.add(pan);
		//.addbox2(Box.createHorizontalGlue());
		box2.add(Box.createRigidArea(new Dimension(10, 0))); 
		//box2.add(Box.createHorizontalGlue());
		pan.setSelected(false);
		pan.setFont(fontg);		
		pan.setActionCommand("Toggle Pan Switch");
		pan.addActionListener(this);
		pan.setBackground(slateGray1);
		pan.setBorderPaintedFlat(false);
		// pan.setBorder(null);
		// pan.setPreferredSize(new Dimension(50, 20));
		ButtonGroup butGroup = new ButtonGroup();

		box21 = new Box(BoxLayout.X_AXIS);
		box2.add(box21);
					
		for (int j = 0; j < but.length; j++) {
			but[j] = new JRadioButton();
			but[j].addActionListener(this);
			butGroup.add(but[j]);
			box21.add(but[j]);
			//jsp.add(but[j]);
			//but[j].setFont(fontg);
			but[j].setText(blockNames[j]);
			but[j].setFocusable(true);		 
		}
		but[but.length - 1].setAlignmentX(Component.RIGHT_ALIGNMENT);

		//box21.add(Box.createRigidArea(new Dimension(10,0)));
		//box21.add(Box.createHorizontalStrut(10));
		adjustFonts();

		but[0].setSelected(true); // "Process"     

		box2.add(Box.createRigidArea(new Dimension(10,0)));
		//box2.add(Box.createHorizontalGlue());		
		for (int j = 0; j < but.length; j++) {				
			but[j].getInputMap().put(escapeKS, "CLOSE");
			but[j].getActionMap().put("CLOSE", escapeAction);		
		}

		BufferedImage image = loadImage("DrawFBP-logo-small.jpg");
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
		closedPawCursor = tk
				.createCustomCursor(image, new Point(15, 15), "Paw");
		
		image = loadImage("drag_icon.gif");
		drag_icon = tk.createCustomCursor(image, new Point(1, 1), "Drag"); 
		
	}
	
	BufferedImage loadImage(String s) {
		
		InputStream is = this.getClass().getClassLoader() 
				.getResourceAsStream(s);
		BufferedImage image = null;
		if (is == null) {
			MyOptionPane.showMessageDialog(frame, "Missing icon: " + s, MyOptionPane.ERROR_MESSAGE);			
		}
		else {
			try {
				image = ImageIO.read(is);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return image;
	}

	public void createMenuBar() throws IOException {

		//JMenu editMenu;

		// Create the menu bar.
		menuBar = new JMenuBar();
		// menuBar.add(Box.createRigidArea(new Dimension(20, 0)));
		// menuBar.setColor(new Color(200, 255, 255));

		// Build the first menu.
		//fileMenu = new JMenu(" File ");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		// fileMenu.setSelected(true);
		fileMenu.setBorderPainted(true);
		//fileMenu.setFont(fontg);
		menuBar.add(fileMenu);

		// a group of JMenuItems
		JMenuItem menuItem = new JMenuItem("Open Diagram");
		// menu.setMnemonic(KeyEvent.VK_D);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
				ActionEvent.ALT_MASK));

		fileMenu.add(menuItem);
		menuItem.addActionListener(this);
		menuItem = new JMenuItem("Save");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.ALT_MASK));
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);
		menuItem = new JMenuItem("Save as...");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
				ActionEvent.ALT_MASK));
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);
		menuItem = new JMenuItem("New Diagram");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				ActionEvent.ALT_MASK));
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);
		fileMenu.addSeparator();
		JMenu gnMenu = new JMenu("Select Diagram Language...");
		fileMenu.add(gnMenu);
		int j = genLangs.length;
		gMenu = new JMenuItem[j];
		
		int k = 0;		
		for (int i = 0; i < j; i++) {
			if (!(genLangs[i].label.equals("FBP"))){
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
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
				ActionEvent.ALT_MASK));
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
		//JMenuItem runMenu = new JMenuItem("Run Command");
		
		//runMenu.setMnemonic(KeyEvent.VK_R);		
		//runMenu.setBorderPainted(true);		
		//fileMenu.add(runMenu);  		
		//runMenu.addActionListener(this);		
		
		//fileMenu.addSeparator();
		menuItem = new JMenuItem("Generate .fbp code");
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);

		fileMenu.addSeparator();
		//if (curDiag != null
		//		&&curDiag.diagLang != null
		//		&& curDiag.diagLang.label.equals("Java")){
		menuItem1 = new JMenuItem("Locate JavaFBP Jar File");
		if (defaultCompLang == null
				|| !( defaultCompLang.label.equals("Java")))  
			menuItem1.setEnabled(false);  
		else
			menuItem1.setEnabled(true); 
		fileMenu.add(menuItem1);
		menuItem1.addActionListener(this);

		menuItem2 = new JMenuItem("Add Additional Component Jar File");
		if (defaultCompLang == null
				|| !( defaultCompLang.label.equals("Java")))  
			menuItem2.setEnabled(false);  
		else
			menuItem2.setEnabled(true); 
		fileMenu.add(menuItem2);
		menuItem2.addActionListener(this);

		fileMenu.addSeparator();
		menuItem = new JMenuItem("Locate DrawFBP Help File");
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);
		//}
		fileMenu.addSeparator();

		menuItem = new JMenuItem("Change Fonts");
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);
		menuItem = new JMenuItem("Change Font Size");
		fileMenu.add(menuItem);
		menuItem.addActionListener(this);

		fileMenu.addSeparator();
		menuItem = new JMenuItem("Print");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
				ActionEvent.ALT_MASK));
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

		//editMenu = new JMenu(" Edit ");
		editMenu.setMnemonic(KeyEvent.VK_E);
		//editMenu.setFont(fontg);
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

		//JMenu helpMenu = new JMenu(" Help ");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		//helpMenu.setFont(fontg);
		helpMenu.setBorderPainted(true);
		// helpMenu.setColor(new Color(121, 201, 201));
		menuBar.add(helpMenu); 			

		Box box0 = new Box(BoxLayout.X_AXIS);
		//JPanel jp1 = new JPanel();
		Dimension dim = jtf.getPreferredSize();
		jtf.setPreferredSize(new Dimension(gFontWidth * 20, dim.height));
		
		box0.add(Box.createRigidArea(new Dimension(20,0)));
		box0.add(jtf); // languages
		
		box0.add(Box.createRigidArea(new Dimension(10,0)));
		
		box0.add(jfl); // font list
		
		box0.add(Box.createRigidArea(new Dimension(10,0)));
		
		box0.add(jfs); // font size
		
        box0.add(Box.createRigidArea(new Dimension(10,0)));
		
		box0.add(jfv);
		menuBar.add(box0);

		jtf.setText(defaultCompLang.showLangs());
		jtf.setFont(fontg);
		jtf.setEditable(false);
		
		//jtf.setBackground(slateGray1);
		//jfl.setBackground(slateGray1);
		 
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
		menuBar.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				escapeKS, "CLOSE");

		menuBar.getActionMap().put("CLOSE", escapeAction);
		menuBar.setVisible(true);
		repaint();

		return;
	}

	Diagram getNewDiag() {
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

			int i = jtp.getTabCount();
			if (i > 1 || curDiag.diagFile != null || curDiag.changed)
				curDiag = getNewDiag();

			jtp.setSelectedIndex(curDiag.tabNum);

			frame.repaint();

			return;

		}

		// if (curDiag.compLang == null) {
		for (int j = 0; j < gMenu.length; j++) {
			if (e.getSource() == gMenu[j]) {
				GenLang gl = genLangs[j];
								
				defaultCompLang = gl;

				properties.put("defaultCompLang", defaultCompLang.label);
				propertiesChanged = true;
				if (curDiag != null && curDiag.diagLang != defaultCompLang) {
					curDiag.diagLang = defaultCompLang;
					curDiag.changed = true;
				}
				changeLanguage(gl);

				MyOptionPane.showMessageDialog(
						frame,
						"Language group changed to "
								+ defaultCompLang.showLangs());
				frame.repaint();

				return;
			}
		}

		// }
		
		if (s.equals("Generate .fbp code")) {
			
			if (curDiag == null || curDiag.blocks.isEmpty()) {
				MyOptionPane.showMessageDialog(frame, "No components specified", MyOptionPane.ERROR_MESSAGE);
				return;
			}

			if (curDiag.title == null || curDiag.title.equals("(untitled)")) {

				MyOptionPane.showMessageDialog(frame,
						"Untitled diagram - please do Save first", MyOptionPane.ERROR_MESSAGE);
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
				MyOptionPane.showMessageDialog(frame, "No components specified", MyOptionPane.ERROR_MESSAGE);
				return;
			}

			if (curDiag.title == null || curDiag.title.equals("(untitled)")) {

				MyOptionPane.showMessageDialog(frame,
						"Untitled diagram - please do Save first", MyOptionPane.ERROR_MESSAGE);
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
			MyFileChooser fc = new MyFileChooser(file,
					curDiag.fCPArr[GENCODE]);
			int i = name.indexOf(".drw");
			ss += File.separator + name.substring(0, i) + curDiag.fCPArr[GENCODE].fileExt;
			fc.setSuggestedName(ss);

			int returnVal = fc.showOpenDialog(true);  // force saveAs

			cFile = null;
			if (returnVal == MyFileChooser.APPROVE_OPTION) {
				cFile = new File(getSelFile(fc));
			}
			// }
			if (cFile == null)
				return;

			if (!(cFile.exists()))
				return;

			CodeManager mc = new CodeManager(curDiag);
			mc.display(cFile, gl);

			return;
		}
		
		/*
		if (s.equals("Run Code")) {

			File cFile = null;
			GenLang gl = curDiag.diagLang;

			// String ss = properties.get("currentImageDir");
			String ss = properties.get(gl.netDirProp);
			File genDir = null;
			if (ss == null)
				genDir = new File(System.getProperty("user.home"));
			else
				genDir = new File(ss);

			MyFileChooser fc = new MyFileChooser(genDir,
					curDiag.fCPArr[GENCODE], frame);

			int returnVal = fc.showOpenDialog();

			cFile = null;
			if (returnVal == MyFileChooser.APPROVE_OPTION) {
				cFile = new File(getSelFile(fc));
			}
			// }
			if (cFile == null)
				return;

			if (!(cFile.exists()))
				return;

			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			int result = compiler.run(null, null, null,
			             cFile.getAbsolutePath());

			return;
		}
		*/		
			
		// Run Command temporarily disabled
		
		if (s.equals("Run Command")) {
			String command = "";
			readPropertiesFile();
			if (null == (command = properties.get("runCommand")))
				command = "echo Enter command";

			String ans = (String) MyOptionPane.showInputDialog(driver.frame,
					"Enter or change text", "Command with no diagram name",
					MyOptionPane.PLAIN_MESSAGE, null, null, command);

			if (ans != null && ans.length() > 0) {
				command = ans;

				properties.put("runCommand", command);
				propertiesChanged = true;
			}
			Process p = null;
			String realCommand = "";
			if ((System.getProperty("os.name")).startsWith("Windows"))
				realCommand += "cmd /c ";
			realCommand += command;
			String jSONNetworkDir = null;
			if (curDiag.title == null)
				MyOptionPane.showMessageDialog(frame,
						"No diagram selected: executing command with no diagram JSON", MyOptionPane.ERROR_MESSAGE);
			else {
				if (null == (jSONNetworkDir = properties
						.get("currentJSONNetworkDir"))) {
					MyOptionPane.showMessageDialog(frame,
							"Diagram selected but JSON directory missing: generate JSON from diagram \n"
							+ "will prompt for JSON directory", MyOptionPane.ERROR_MESSAGE);
					return;
				}
				
				String fileName = jSONNetworkDir + File.separator + curDiag.title + ".json";
				File file = new File(fileName);
				if (!file.isFile()){
					MyOptionPane.showMessageDialog(frame,
							"JSON file for diagram does not exist: generate JSON from diagram", MyOptionPane.ERROR_MESSAGE);
					return;
				}
				realCommand += " " + fileName;				
			}
			
			ans = (String) MyOptionPane.showInputDialog(driver.frame,
					"Enter or change text", "Actual command",
					MyOptionPane.PLAIN_MESSAGE, null, null, realCommand);

			if (ans != null && ans.length() > 0) {
				realCommand = ans;

				//properties.put("runCommand", realCommand);
				//propertiesChanged = true;
			}
			
			try {
				p = Runtime.getRuntime().exec(realCommand);

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// final JEditorPane pane = new JEditorPane();

			final JFrame jframe = new JFrame("Run Output");
			final JEditorPane pane = new JEditorPane("text/plain", " ");
			pane.setEditable(false);
			JScrollPane scrollPane = new JScrollPane(pane);
			jframe.add(scrollPane);
			jframe.setVisible(true);
			pane.setVisible(true);
			scrollPane.setVisible(true);
			pane.setFont(fontf);
			jframe.setSize(600, 400);
			jframe.setLocation(100, 50);
			jframe.addWindowListener(new WindowAdapter() {

				@Override
				public void windowClosing(final WindowEvent ev) {
					jframe.dispose();
				}
			});

			MyRunnable r = new MyRunnable(pane) {
				public void run() {
					BufferedReader input = new BufferedReader(
							new InputStreamReader(proc.getInputStream()));
					String line = null;

					try {
						while ((line = input.readLine()) != null) {
							try {
								Document doc = pane.getDocument();
								doc.insertString(doc.getLength(), line, null);
							} catch (BadLocationException exc) {
								exc.printStackTrace();
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};

			r.proc = p;
			new Thread(r).start();

			try {
				r.proc.waitFor();
			} catch (InterruptedException ev) {
				// TODO Auto-generated catch block
				ev.printStackTrace();
			}

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
		
		if (s.equals("Add Additional Component Jar File")) {

			addAdditionalJarFile();
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
			PrintableDocument pd = new PrintableDocument(
					frame.getContentPane(), this);

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
			//if (panSwitch)  
			//	frame.setCursor(openPawCursor);
			//else 
			//	frame.setCursor(defaultCursor);	
			return;
		}
		
		if (s.equals("Export Image")) {

			if (curDiag == null || curDiag.title == null || curDiag.blocks.isEmpty()) {
				MyOptionPane.showMessageDialog(null,
						"Unable to export image for empty or unsaved diagram - please do save first",
						MyOptionPane.ERROR_MESSAGE);
				return;
			}

			File file = null;
			//curDiag.imageFile = null;

			// crop
			int x1, w1, y1, h1;
			x1 = Math.max(1, curDiag.minX);
			w1 = curDiag.maxX - x1;
			y1 = Math.max(1, curDiag.minY);
			h1 = curDiag.maxY - y1;
			
			int w = curDiag.area.getWidth();
			int h = curDiag.area.getHeight();
			w1 = Math.min(w1, w - x1);
			h1 = Math.min(h1, h - y1);
			
			BufferedImage buffer2 = buffer.getSubimage(x1, y1, w1, h1);

			//int w2 = buffer2.getWidth();
			//int h2 = buffer2.getHeight();
			
			BufferedImage combined = new BufferedImage(w1, h1 + 100,
					BufferedImage.TYPE_INT_ARGB);
			Graphics g = combined.getGraphics();
			//Graphics g = buffer2.getGraphics();
			g.setColor(Color.WHITE);
			//g.fillRect(0, 0, w1, h1 + 100);
			g.drawImage(buffer2, 0, 0, null);
			//g.setColor(Color.RED);
			g.fillRect(0, h1, w1, 100);
			 
			if (curDiag.desc != null) {
				Color col = g.getColor();
				g.setColor(Color.BLUE);
				Font f = fontg.deriveFont(Font.ITALIC, 18.0f);
				g.setFont(f);
				int x = combined.getWidth() / 2;				
				//int x = buffer2.getWidth() / 2;
				FontMetrics metrics = g.getFontMetrics(f);
				String t = curDiag.desc;
				byte[] str = t.getBytes();
				int width = metrics.bytesWidth(str, 0, t.length());

				g.drawString(t, x - width / 2, buffer2.getHeight() + 40);
				g.setColor(col);
			}

			int i = curDiag.fCPArr[IMAGE].prompt.indexOf(":");
			String fn;
			if (curDiag.diagFile == null)
				fn = "(null)";
			else
				fn = curDiag.diagFile.getName();

			curDiag.fCPArr[IMAGE].prompt = curDiag.fCPArr[IMAGE].prompt
					.substring(0, i) + ": " + fn;

			file = curDiag.genSave(null, fCPArray[IMAGE], combined);
			//file = curDiag.genSave(null, fCPArray[IMAGE], buffer2);
			if (file == null) {
				MyOptionPane.showMessageDialog(frame, "File not saved");
				// curDiag.imageFile = null;
				g.dispose();
				return;
			}

			// ImageIcon image = new ImageIcon(combined);
			//curDiag.imageFile = file;
			Date date = new Date();
			file.setLastModified(date.getTime());
			return;
		}
		
		if (s.equals("Show Image")) {

			File fFile = null;

			//if (fFile == null || !fFile.exists()) {

				String ss = properties.get("currentImageDir");
				if (ss == null)
					currentImageDir = new File(System.getProperty("user.home"));
				else
					currentImageDir = new File(ss);

				MyFileChooser fc = new MyFileChooser(currentImageDir,
						curDiag.fCPArr[IMAGE]);

				int i = curDiag.diagFile.getName().indexOf(".drw");
				ss += File.separator
						+ curDiag.diagFile.getName().substring(0, i)
						+ curDiag.fCPArr[IMAGE].fileExt;
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
			//}

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
			propertiesChanged = true;

			//curDiag.imageFile = fFile;

			
			JDialog popup = new JDialog();
			popup.setTitle(fFile.getName());
			JLabel jLabel = new JLabel(image);	
			jLabel.addComponentListener(this);			
			JScrollPane jsp = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

			jsp.getViewport().add(jLabel);
			Dimension dim = new Dimension(image.getIconWidth(), image.getIconHeight());
			jsp.getViewport().setPreferredSize(dim);
			jsp.getViewport().setBackground(Color.WHITE);
			jLabel.setBackground(Color.WHITE);
			popup.add(jsp, BorderLayout.CENTER);
			popup.setLocation(new Point(200, 200));
			popup.setBackground(Color.WHITE);
			//popup.addComponentListener(this);
			//popup.setPreferredSize(dim);
			popup.pack();
			popup.setVisible(true);
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
									int response =  MyOptionPane
											.showConfirmDialog(
													frame,
													//"Locate it?",
													"Specify the location of the JavaHelp jar file -\n"
															+ "do a search on Maven Central for 'javahelp'\n"
													        + "Artifact ID: javahelp",
													"Locate it?",		
													MyOptionPane.OK_CANCEL_OPTION);
									if (response == MyOptionPane.OK_OPTION)
										res = locateJhallJarFile();
									else {
										MyOptionPane.showMessageDialog(frame,
												"No DrawFBP Help jar file located", MyOptionPane.ERROR_MESSAGE);
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
								MyOptionPane
										.showMessageDialog(
												frame,
												"DrawFBP Help jar file shown in properties does not exist\n"
														+ "Use File/Locate DrawFBP Help File, and try Help again", 
														MyOptionPane.ERROR_MESSAGE);
								return;
							}
							try {								
								URL[] urls = new URL[]{jFile.toURI().toURL()};

								// Create a new class loader with the directory
								cl = new URLClassLoader(urls, this.getClass()
										.getClassLoader());

								// Find the HelpSet file and create the HelpSet object
								helpSetClass = cl.loadClass("javax.help.HelpSet");
							} catch (MalformedURLException e2) {
							} catch (ClassNotFoundException e2) {
							} catch (NoClassDefFoundError e2) {
							}

							if (helpSetClass == null) {
								MyOptionPane.showMessageDialog(frame,
										"HelpSet class not found in jar file or invalid", MyOptionPane.ERROR_MESSAGE);
								return;
							}

							URL url2 = null;
							jHelpViewer = null;
							try {
								Method m = helpSetClass.getMethod("findHelpSet",
										ClassLoader.class, String.class);
								url2 = (URL) m.invoke(null, cl, "helpSet.hs");

								Constructor conhs = helpSetClass.getConstructor(
										ClassLoader.class, URL.class);

								Object hs = conhs.newInstance(cl, url2);

								jHelpClass = cl.loadClass("javax.help.JHelp");
								if (jHelpClass == null) {
									MyOptionPane.showMessageDialog(frame,
											"JHelp class not found in jar file", MyOptionPane.ERROR_MESSAGE);
									return;
								}
								Constructor conjh = jHelpClass.getConstructor(helpSetClass);
								jHelpViewer = (JComponent) conjh.newInstance(hs);

								
							} catch (Exception e2) {
								MyOptionPane.showMessageDialog(frame,
										"HelpSet could not be processed: " + e2, MyOptionPane.ERROR_MESSAGE);
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
						jHelpViewer.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
								escapeKS, "CLOSE");

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
						popup2.setPreferredSize(new Dimension(dim.width - x_off * 2, dim.height - y_off));
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
					+  "*                                                  *\n"
					+  "*             DrawFBP v" + v + "      "+ sp1 +"               *\n"
					+  "*                                                  *\n"
					+  "*    Authors: J.Paul Rodker Morrison,              *\n"
					+  "*             Bob Corrick                          *\n"
					+  "*                                                  *\n"
					+  "*    Copyright 2009, ..., 2017                     *\n"
					+  "*                                                  *\n"
					+  "*    FBP web site: www.jpaulmorrison.com/fbp       *\n"
					+  "*                                                  *\n"
					+  "*               (" + dt + ")            "+ sp2 +"       *\n"
					+  "*                                                  *\n"
					+  "****************************************************\n");

			ta.setFont(f);
			final JDialog popup = new JDialog(frame);
			popup.add(ta, BorderLayout.CENTER);			
			Point p = frame.getLocation();
			//popup.setPreferredSize(new Dimension(60,20));
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

			if (ans != null/* && ans.length() > 0*/) {
				curDiag.desc = ans;
				curDiag.desc = curDiag.desc.replace('\n', ' ');
				curDiag.desc = curDiag.desc.trim();
				curDiag.changed = true;
			}
			frame.repaint();
			return;

		}
		if (s.equals("New Block")) {
			// if (newItemMenu == null) {
			// newItemMenu = buildNewItemMenu(driver);
			// }
			// newItemMenu.setVisible(true);
			curDiag.xa = 100 + (new Random())
					.nextInt(curDiag.area.getWidth() - 200);
			curDiag.ya = 100 + (new Random())
					.nextInt(curDiag.area.getHeight() - 200);
			if (null != createBlock(blockType))
				curDiag.changed = true;
			frame.repaint();
			return;

		}
		if (s.equals("Block-related Actions")) {
			Block b = selBlock;
			if (b == null) {
				MyOptionPane.showMessageDialog(frame, "Block not selected", MyOptionPane.ERROR_MESSAGE);
				return;
			}
			b.buildBlockPopupMenu();
			use_drag_icon = false;
			curDiag.jpm.show(frame, curDiag.xa + 100, curDiag.ya + 100);
			frame.repaint();
			return;

		}
		if (s.equals("Arrow-related Actions")) {
			Arrow a = selArrow;
			if (a == null) {
				MyOptionPane.showMessageDialog(frame, "Arrow not selected", MyOptionPane.ERROR_MESSAGE);
				return;
			}
			a.buildArrowPopupMenu();
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
		defaultCompLang = gl;
		jtf.setText("Diagram Language: " +  gl.showLangs());
		jtf.repaint();
		if (!defaultCompLang.label.equals("Java")) {
			menuItem1.setEnabled(false);  
			menuItem2.setEnabled(false); 
		}
		else {
			menuItem1.setEnabled(true); 
			menuItem2.setEnabled(true); 
		}
		
		fileMenu.remove(gNMenuItem);
		
		String u = "Generate ";
		if (curDiag != null)
			u += curDiag.diagLang.label + " ";
		u += "Network";
		gNMenuItem = new JMenuItem(u);
		gNMenuItem.addActionListener(this);
		fileMenu.add(gNMenuItem, 10);
		curDiag.filterOptions[0] = gl.showLangs();

		curDiag.fCPArr[DrawFBP.PROCESS] = driver.new FileChooserParms(
				"Process",
				gl.srcDirProp, "Select "
						+ gl.showLangs()
						+ " component from directory",
				gl.suggExtn, gl.filter,
				"Components: " + gl.showLangs() + " "
						+ gl.showSuffixes());

		curDiag.fCPArr[DrawFBP.GENCODE] = driver.new FileChooserParms(
				"Generated code", gl.netDirProp,
				"Specify file name for generated code", "."
						+ gl.suggExtn,
				gl.filter, gl.showLangs());
		
		frame.repaint();

	}	

	Block createBlock(String blkType) {
		Block block = null;
		boolean oneLine = false;
		if (blkType == Block.Types.PROCESS_BLOCK) {
			block = new ProcessBlock(curDiag);
			block.isSubnet = willBeSubnet;
		}

		else if (blkType == Block.Types.EXTPORT_IN_BLOCK
				|| blkType == Block.Types.EXTPORT_OUT_BLOCK
				|| blkType == Block.Types.EXTPORT_OUTIN_BLOCK){
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
			int y = Math.max(curDiag.ya - block.height / 2, pt.y + 6);
			block.cy = ((curDiag.ya + block.height / 2) + y) / 2;
		}

		else if (blkType == Block.Types.PERSON_BLOCK)
			block = new PersonBlock(curDiag);

		else if (blkType == Block.Types.REPORT_BLOCK)
			block = new ReportBlock(curDiag);
		else
			return null;

		block.type = blkType;

		block.cx = curDiag.xa;
		block.cy = curDiag.ya;
		if (block.cx == 0 || block.cy == 0)
			return null; // fudge!
		
		//if (enterDesc) {  
		if (oneLine) {
			if (blkType != Block.Types.ENCL_BLOCK) {
				String d = "Enter single line value";
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
		//}
		block.calcEdges();
		curDiag.maxBlockNo++;
		block.id = curDiag.maxBlockNo;
		curDiag.blocks.put(new Integer(block.id), block);
		//curDiag.changed = true;
		selBlock = block;
		// selArrowP = null;
		return block;
	}

	void buildPropDescTable() {
		propertyDescriptions = new LinkedHashMap<String, String>();
		
		propertyDescriptions.put("Version #",
				"versionNo");
		propertyDescriptions.put("Date",
				"date");
		propertyDescriptions.put("Current C# source code directory",
				"currentCsharpSourceDir");
		propertyDescriptions.put("Current C# network code directory",
				"currentCsharpNetworkDir");
		propertyDescriptions.put("Current component class directory",
				"currentClassDir");
		propertyDescriptions.put("Current diagram directory",
				"currentDiagramDir");
		propertyDescriptions.put("Current diagram",
				"currentDiagram");
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
		propertyDescriptions
				.put("DrawFBP Help jar file", "jhallJarFile");
		propertyDescriptions
		.put("Additional Component Jar Files", "additionalJarFiles");
		  
	}

	void displayProperties() {
		final JFrame jf = new JFrame();
		jf.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				jf.dispose();
			}
		});
				
		JPanel panel = new JPanel(new GridBagLayout());
		JScrollPane jsp = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
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
					if (q.equals("additionalJarFiles") && first){
						first = false;
						continue;
					}
					if ((i = w.indexOf(";")) > -1) {
						u = w.substring(0, i);
						w = w.substring(i + 1);
						done = false;
					}
					else {
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

		//jsp.add(panel);
		jf.add(jsp);		
		jf.pack();
		Point p = frame.getLocation();
		jf.setLocation(p.x + 150, p.y + 50);
		//int height = 200 + properties.size() * 40;
		jf.setSize(1200, 800);
		//jsp.setVisible(true); 
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
		sa.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				escapeKS, "CLOSE");
		sa.getInputMap().put(escapeKS, "CLOSE");

		sa.getActionMap().put("CLOSE", escapeAction);
		/* experimental
		if (curDiag == null)
			sa.setPreferredSize(new Dimension(1200, 800));
		else {
			int w = frame.getWidth();
			int h = frame.getHeight();
			sa.setPreferredSize(new Dimension(w - wDiff, h - hDiff));
		}
		*/
		return sa;
	}

	File openAction(String fn) {

		File file = null;
		if (fn != null)
			file = new File(fn);
		String fname = fn;

		int i = jtp.getTabCount();
		// ButtonTabComponent b = (ButtonTabComponent) jtp.getTabComponentAt(i);
		// Diagram diag = b.diag;

		if (i > 1 || curDiag.diagFile != null || curDiag.changed)
			curDiag = getNewDiag();

		file = curDiag.open(file);
		if (file == null) {
			//CloseTabAction closeTabAction = new CloseTabAction();
			//closeTabAction.actionPerformed(new ActionEvent(jtp, 0, "CLOSE"));
			closeTab();
			//curDiag = null;    
			return null;
		}

		fname = file.getName();
		curDiag.diagFile = file;

		GenLang gl = null;

		String suff = curDiag.getSuffix(fname);

		if (suff.equals("fbp")) {
			gl = findGLFromLabel("FBP");
			CodeManager cm = new CodeManager(curDiag);
			cm.display(file, gl);
			return file;
		}
		if (!(suff.equals("drw"))) {
			gl = findGLFromLanguage(suff);
			CodeManager cm = new CodeManager(curDiag);
			cm.display(file, gl);
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

		curDiag.tabNum = i;

		curDiag.title = file.getName();
		curDiag.diagFile = file;
		if (curDiag.title.toLowerCase().endsWith(".drw"))
			curDiag.title = curDiag.title.substring(0,
					curDiag.title.length() - 4);

		File currentDiagramDir = file.getParentFile();
		frame.setTitle("Diagram: " + curDiag.title);
		properties
				.put("currentDiagramDir", currentDiagramDir.getAbsolutePath());
		propertiesChanged = true;

		curDiag.changed = false;
		frame.repaint();
	}

	/*
	static String makeRelFileName(String current, String parent) {
		if (!(current.startsWith("/") || current.substring(1, 2).equals(":"))) {
			return current;
		}
		String res = "";
		String cur = current.replace('\\', '/');
		String curLead;
		String par = parent.replace('\\', '/');
		String parLead;

		while (true) {
			int i = par.indexOf("/");
			if (i == -1)
				break;
			par = par.substring(i + 1);
			res += "../";
		}
		int is = 0, js = 0;
		int i = 0;
		par = parent.replace('\\', '/'); // restore full length par

		while (true) {
			i = cur.indexOf("/", is);
			if (i == -1)
				break;
			curLead = cur.substring(0, i);

			int j = par.indexOf("/", js);
			if (j == -1)
				break;
			parLead = par.substring(0, j);

			if (!(parLead.equals(curLead)))
				break;

			res = res.substring(3);
			is = i + 1;
			js = j + 1;
		}
		return res + cur.substring(is);
	}
*/
	static String makeAbsFileName(String current, String parent) {
		if (current.equals(""))
			return parent;
		if (current.startsWith("/") || current.substring(1, 2).equals(":"))
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
	public static BufferedImage readImageFromFile(File file) throws IOException {
		return ImageIO.read(file);
	}

	public void menuDeselected(MenuEvent e) {
	}

	public void menuCanceled(MenuEvent e) {
	}

	void changeFonts() {
		fontChooser = new MyFontChooser(frame,
				this);
		chooseFonts(fontChooser);
		
		if (gFontChanged) {
			properties.put("generalFont", generalFont);
			propertiesChanged = true;

			jfl.setText("Fixed font: " + fixedFont + "; general font: " + generalFont);
			fontg = new Font(generalFont, Font.PLAIN, (int) defaultFontSize);			
			adjustFonts();
			frame.repaint();
			repaint();
		}

		if (fFontChanged) {
			properties.put("fixedFont", fixedFont);
			propertiesChanged = true;

			jfl.setText("Fixed font: " + fixedFont + "; general font: " + generalFont);
			fontf = new Font(fixedFont, Font.PLAIN, (int) defaultFontSize);
			adjustFonts();
			frame.repaint();
			repaint();
			
		}

		return;
	}
	
void chooseFonts(MyFontChooser fontChooser){
		
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
			if (Float.compare(selectionValues[i].floatValue(), defaultFontSize) == 0)
				j = i;
		}
		Float fs = (Float) MyOptionPane.showInputDialog(frame, "Font size dialog",
				"Select a font size", MyOptionPane.PLAIN_MESSAGE, null,
				selectionValues, selectionValues[j]);
		if (fs == null)
			return;
		
		defaultFontSize = fs.floatValue();
		fontg = fontg.deriveFont(fs);
		fontf = fontf.deriveFont(fs);	
		jfs.setText("Font Size: " + defaultFontSize);		 
		adjustFonts();
		frame.repaint(); 
		properties.put("defaultFontSize", Float.toString(defaultFontSize));
		propertiesChanged = true;
		MyOptionPane.showMessageDialog(frame, "Font size changed");
		frame.repaint();
		repaint();
	}

	void adjustFonts() {
		fileMenu = new JMenu(" File ");
		editMenu = new JMenu(" Edit ");
		helpMenu = new JMenu(" Help ");		
		//runMenu = new JMenu(" Run ");
		
		int j = jtp.getTabCount();
		for (int i = 0; i < j; i++) {	
			ButtonTabComponent b = (ButtonTabComponent) jtp
					.getTabComponentAt(i);
			b.label.setFont(fontf);
		}
		jtp.repaint();
		
		//osg.setFont(fontg);
		jfl.setFont(fontg);
		jfs.setFont(fontg);
		jtp.setFont(fontg);
		zoom.setFont(fontg);
		jtf.setFont(fontg);
		pan.setFont(fontg);
		grid.setFont(fontg);
		scaleLab.setFont(fontg);
		fileMenu.setFont(fontg);
		editMenu.setFont(fontg);
		helpMenu.setFont(fontg);
		//runMenu.setFont(fontg);
		diagDesc.setFont(fontg);
		
		for (int i = 0; i < but.length; i++) {
			but[i].setFont(fontg);
			but[i].setFocusable(true);
			but[i].addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent ev) {				
					if (ev.getKeyCode() == KeyEvent.VK_ENTER){	
						JRadioButton rb = (JRadioButton)ev.getSource();							
						rb.setSelected(true);
						setBlkType(rb.getText());
					}
				}
			});	
		}
		
		UIDefaults def = UIManager.getLookAndFeelDefaults();
		//Hashtable<String, Font> ht = new Hashtable<String, Font>();
		final FontUIResource res = new FontUIResource(fontg);
		for (Enumeration<Object> e = def.keys(); e.hasMoreElements(); ) {
			Object item = e.nextElement();
			if (item instanceof String) {
				String s = (String) item;
				//System.out.println(s + " - " + def.get(item));
				if (def.get(item) instanceof Font) {
					//System.out.println(s + " - " + def.get(item));
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
		
		for (Block block : curDiag.blocks.values()) {
			block.draw(osg);
		}
		
		for (Arrow arrow : curDiag.arrows.values()) {
			arrow.draw(osg);
		}
		
		if (depDialog != null)
			depDialog.setFont(fontf);
		
		/*
		for (Object item : ht.keySet()) {
			UIManager.put(item, fontg);
			// System.out.println(item + " - " + fontg);
		}		
		 */
		// UIManager.put("Button.select", slateGray1);

		try {
			createMenuBar();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		frame.setJMenuBar(menuBar);

		frame.repaint();
	}

	// between just checks that the value val is >= lim1 and <= lim2 - or the inverse
	
	static boolean between(int val, int lim1, int lim2) {
		return between(val, (double) lim1, (double) lim2);
	}
	
	static boolean between(int val, double lim1, double lim2) {
		boolean res;
		res = (val >= lim1 && val <= lim2 && lim1 < lim2)
				|| (val >= lim2 && val <= lim1 && lim2 < lim1);
		return res;
	}

	boolean readPropertiesFile() {

		if (propertiesFile == null) {
			String uh = System.getProperty("user.home");
			propertiesFile = new File(uh + File.separator
					+ "DrawFBPProperties.xml");
			if (!propertiesFile.exists())
				return false;
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
				s = s.substring(j + 1);
				int k = s.indexOf("<");
				String u = "";
				if (k > 0) {
					if (!(key.equals("additionalJarFiles"))) {
					    s = s.substring(0, k).trim();
					    properties.put(key, s);
					}
					else {
						s = s.substring(0, k).trim();
						while (true) {
							int m = s.indexOf(";");
							if (m == -1){
								u = s;
								int n = u.indexOf(":");

								if (n == -1)
									break;

								properties.put("addnl_jf_" + u.substring(0, n), u.substring(n + 1));
								jarFiles.put(u.substring(0, n), u.substring(n + 1));
								break;
							}
							else {
								u = s.substring(0, m);
								s = s.substring(m + 1);	
								int n = u.indexOf(":");

								if (n == -1)
									break;

								properties.put("addnl_jf_" + u.substring(0, n), u.substring(n + 1));
								jarFiles.put(u.substring(0, n), u.substring(n + 1));
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
				if (k.startsWith("addnl_jf_") || k.startsWith("additionalJarFiles"))
					continue;
				String s = "<" + k + "> " + properties.get(k) + "</" + k
						+ "> \n";
				out.write(s);
			}
			Iterator<Entry<String, String>> entries = jarFiles.entrySet().iterator();
			String z = "";
			String cma = ""; 

			
			while (entries.hasNext()) {	
				Entry<String, String> thisEntry = entries.next();
				
			    			     				
			        z += cma + thisEntry.getKey() + ":" + thisEntry.getValue();
			        cma = ";"; 
			    		

			}
			String s = "<additionalJarFiles> " + z + "</additionalJarFiles> \n";
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

	
	
	

	boolean getJavaFBPJarFile() {
		if (javaFBPJarFile == null) {
			javaFBPJarFile = properties.get("javaFBPJarFile");
		}

		File jf = null;
		boolean res = true;
		if (javaFBPJarFile != null) {
			jf = new File(javaFBPJarFile);
		}
		if (jf != null && jf.exists()) {
			//jarFiles.put("JavaFBP Jar File", jf.getAbsolutePath());
			return res;
		}

		String msg = null;
		if (jf != null) {
			if (!jf.exists())
			    msg = "Unable to read JavaFBP jar file: " + javaFBPJarFile;
		}
		else
			msg = "JavaFBP jar file missing";

		if (msg != null)
		    MyOptionPane.showMessageDialog(frame, msg, MyOptionPane.ERROR_MESSAGE);
		
		int response = MyOptionPane.showConfirmDialog(frame, "Specify a JavaFBP jar file", "Locate JavaFBP jar file",
				MyOptionPane.OK_CANCEL_OPTION /*, MyOptionPane.PLAIN_MESSAGE */);
		if (response == MyOptionPane.OK_OPTION)
			res = locateJavaFBPJarFile();
		else {
			MyOptionPane.showMessageDialog(frame, "No JavaFBP jar file located", MyOptionPane.ERROR_MESSAGE);
			res = false;
		}

		return res;
	}
	
	String getSelFile(MyFileChooser fc) {
		String[] sa = new String[1]; 
		fc.getSelectedFile(sa);  // getSelectedFile puts result in sa[0]
		return sa[0];
	}
	
	boolean locateJavaFBPJarFile() {
		
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
						"Unable to read JavaFBP jar file " + cFile.getName(), MyOptionPane.ERROR_MESSAGE);
				return false;
			}
			// diag.driver.currentDir = new File(cFile.getParent());
			javaFBPJarFile = cFile.getAbsolutePath();
			properties.put("javaFBPJarFile", javaFBPJarFile);
			
			propertiesChanged = true;
			MyOptionPane.showMessageDialog(frame,
					"JavaFBP jar file location: " + cFile.getAbsolutePath(), MyOptionPane.INFORMATION_MESSAGE);
			//jarFiles.put("JavaFBP Jar File", cFile.getAbsolutePath());
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
	
	boolean addAdditionalJarFile(){
		
		String ans = (String) MyOptionPane.showInputDialog(frame,
				"Enter Description of jar file being added", "Enter Description", 
				MyOptionPane.PLAIN_MESSAGE, null, null, null);
		if (ans == null || ans.equals("")){
			MyOptionPane.showMessageDialog(frame,
					"No description entered", MyOptionPane.ERROR_MESSAGE);
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
						"Unable to read additional jar file " + cFile.getName(), MyOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			jarFiles.put(ans, cFile.getAbsolutePath());
			
			@SuppressWarnings("rawtypes")
			Iterator entries = jarFiles.entrySet().iterator();
			String t = "";
			String cma = ""; 
			
			while (entries.hasNext()) {	
				@SuppressWarnings("unchecked")	
				Entry<String, String> thisEntry = (Entry<String, String>) entries.next();
			    			       				
			        t += cma + thisEntry.getKey() + ":" + thisEntry.getValue();
			        cma = ";"; 
			    
			}
			properties.put("additionalJarFiles", t);
			
			propertiesChanged = true;
			
		}
		return true;
	}
	
	 
	boolean locateJhallJarFile() {

		String s = properties.get("jhallJarFile");
		File f = null;
		if (s == null)
			f = new File(System.getProperty("user.home"));
		else
			f = (new File(s)).getParentFile();

		MyFileChooser fc = new MyFileChooser(f, fCPArray[JHALL]);

		int returnVal = fc.showOpenDialog();

		File cFile = null;
		if (returnVal == MyFileChooser.APPROVE_OPTION) {
			cFile = new File(getSelFile(fc));
			if (cFile == null || !(cFile.exists())) {
				MyOptionPane.showMessageDialog(frame,
						"Unable to read DrawFBP Help jar file " + cFile.getName());
				return false;
			}
			// diag.driver.currentDir = new File(cFile.getParent());
			jhallJarFile = cFile.getAbsolutePath();
			properties.put("jhallJarFile", jhallJarFile);
			propertiesChanged = true;
			MyOptionPane.showMessageDialog(frame, "DrawFBP Help jar file location: " + cFile.getAbsolutePath());
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

	public final void getSamples() {
		Enumeration<?> entries;
		ZipFile zipFile;
		MyOptionPane.showMessageDialog(frame,
				"Select new project for sample networks");

		// zip file must have "src", "test" and "diagrams" directories
		// and optionally components...

		String zipname = "FBPSamples.zip";
		InputStream is = this.getClass().getClassLoader()
				.getResourceAsStream(zipname);

		String zfn = System.getProperty("user.home") + File.separator + zipname;
		File f = new File(zfn);
		String s = f.getParent();
		if (f.exists())
			f.delete();
		if (s != null)
			(new File(s)).mkdirs();
		try {
			copyInputStream(is, new FileOutputStream(f));
		} catch (IOException e) {

		}

		try {

			zipFile = new ZipFile(zfn);

			entries = zipFile.entries();

			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();

				if (entry.isDirectory()) {
					// Assume directories are stored parents first then
					// children.
					System.out.println("Extracting directory: "
							+ entry.getName());
					// This is not robust, just for demonstration purposes.
					(new File(entry.getName())).mkdirs();
				} else {
					System.out.println("Extracting file: " + entry.getName());
					f = new File(entry.getName());
					if (f.exists())
						f.delete();
					copyInputStream(zipFile.getInputStream(entry),
							new FileOutputStream(f));
				}
			}

			zipFile.close();
		} catch (IOException ioe) {
			System.err.println("Unhandled exception:");
			ioe.printStackTrace();
			return;
		}
	}
	
	*/

	void checkCompatibility(Arrow a) {
		Arrow a2 = a.findTerminalArrow();
		Block from = curDiag.blocks.get(new Integer(a.fromId));
		Block to = curDiag.blocks.get(new Integer(a2.toId));
		// String downPort = a2.downStreamPort;
		a.checkStatus = Status.UNCHECKED;
		if (!(from instanceof ProcessBlock)
				|| !(to instanceof ProcessBlock))
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
	/*
	 * Test if point (xp, yp) is "near" line defined by (x1, y1) and (x2, y2)
	 */
	boolean nearpln(int xp, int yp, int x1, int y1, int x2, int y2) {

		Rectangle2D r = new Rectangle2D.Double(xp - gridUnitSize
				* scalingFactor, yp - gridUnitSize * scalingFactor,
				gridUnitSize * 2 * scalingFactor, gridUnitSize * 2
						* scalingFactor);
		Line2D line = new Line2D.Float(x1, y1, x2, y2);
		return line.intersects(r);

	}

	java.awt.geom.Point2D.Double findIntersectPoint(Line2D.Float ln, int x3,
			int y3, int x4, int y4) {
		double x1 = ln.getX1();
		double x2 = ln.getX2();
		double y1 = ln.getY1();
		double y2 = ln.getY2();
		double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
		if (d == 0)
			return null;
		double xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2)
				* (x3 * y4 - y3 * x4))
				/ d;
		double yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2)
				* (x3 * y4 - y3 * x4))
				/ d;
		return new Point2D.Double(xi, yi);
	}

	void processSubnetPort(Arrow arr) {

		SubnetPort snPort = null;

		DrawFBP.Side s = null;
		Point2D.Double p = null;
		Line2D.Float line;
		int left = curDiag.cEncl.leftEdge; // for legibility
		int right = curDiag.cEncl.rgtEdge;
		int top = curDiag.cEncl.topEdge;
		int bottom = curDiag.cEncl.botEdge;
		int x = arr.fromX;
		int y = arr.fromY;
		if (arr.bends != null) {
			for (Bend b : arr.bends) {
				line = new Line2D.Float(x, y, b.x, b.y);
				if (line.intersectsLine(left, top, left, bottom)) {
					p = findIntersectPoint(line, left, top, left, bottom);
					s = DrawFBP.Side.LEFT;
				} else if (line.intersectsLine(right, top, right, bottom)) {
					p = findIntersectPoint(line, right, top, right, bottom);
					s = DrawFBP.Side.RIGHT;
				}
				if (s != null)
					break;
			}
		}
		if (s == null) {
			line = new Line2D.Float(x, y, arr.toX, arr.toY);
			if (line.intersectsLine(left, top, left, bottom)) {
				p = findIntersectPoint(line, left, top, left, bottom);
				s = DrawFBP.Side.LEFT;
			} else if (line.intersectsLine(right, top, right, bottom)) {
				p = findIntersectPoint(line, right, top, right, bottom);
				s = DrawFBP.Side.RIGHT;
			}
		}

		if (s == null) {
			curDiag.findArrowCrossing = false;
			curDiag.cEncl = null;
			return;
		}

		for (SubnetPort snp : curDiag.cEncl.subnetPorts) {
			if (snp.y >= p.y - 4 && snp.y <= p.y + 4 && snp.side == s)
				snPort = snp;
		}
		if (snPort == null) {
			snPort = new SubnetPort((int) p.y, s);
			curDiag.cEncl.subnetPorts.add(snPort);
		}
		
		if (curDiag.cEncl.editPortName) {
			String ans = (String) MyOptionPane.showInputDialog(frame,
					"Enter or change text", "Edit subnet port name",
					MyOptionPane.PLAIN_MESSAGE, null, null, snPort.name);
			if (ans != null/* && ans.length() > 0*/) 
				snPort.name = ans;
			curDiag.cEncl.editPortName = false;
		}
		if (curDiag.cEncl.changeSubstreamSensitivity) {
			snPort.substreamSensitive = !snPort.substreamSensitive;
			curDiag.cEncl.changeSubstreamSensitivity = false;
		}

		curDiag.findArrowCrossing = false;
		curDiag.cEncl = null;
		repaint();
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
			if (b != block && between(block.cx - block.width / 2, x - 6, x + 6)) {
				block.vNeighbour = b;
				break;
			}
		}
	}

	Point gridAlign(Point p) {
		Point p2 = p;
		if (curDiag.clickToGrid) {
			int x = ((int) (p.getX() + gridUnitSize / 2) / gridUnitSize)
					* gridUnitSize;
			int y = ((int) (p.getY() + gridUnitSize / 2) / gridUnitSize)
					* gridUnitSize;
			p2 = new Point(x, y);
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

	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		//oldW = getSize().width;
		//oldH = getSize().height;
		if (!source.getValueIsAdjusting()) {
			scalingFactor = ((int) source.getValue()) / 100.0;
			String scale = (int) source.getValue() + "%";
			scaleLab.setText(scale);
			frame.pack();
			frame.setPreferredSize(new Dimension(1200, 800)); 	
			frame.repaint();
		}
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
		//dim2.width = 50;
		//but[4].setMinimumSize(dim2);
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
				netDirProp = "currentCsharpNetworkDir";  // xml does not seem to like #'s
			}
			filter = f;

		}

		String showLangs() {
			String s = "";
			for (int i = 0; i < langs.length; i++) {
				if (i == 1)
					s += " (+ ";
				if (i > 1)
					s += ", ";
				s += langs[i].language;
				if (i > 0 && i == (langs.length - 1))
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

	public class FileChooserParms {
		String name;
		String propertyName;
		String prompt;
		String fileExt;
		FileFilter filter;
		String title;

		FileChooserParms(String x, String a, String b, String c, FileFilter d, String e) {
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
			//if (propertiesChanged) {
				writePropertiesFile();
			//}

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
			
			driver.properties.remove("currentDiagram");

			int j = jtp.getTabCount();
			if (j == 0) {
				// make one tab with "(untitled)"
				// Diagram curDiag = new Diagram(driver);
				curDiag = getNewDiag();
				frame.setTitle("Diagram: (untitled)");
			} else {
				jtp.setSelectedIndex(j - 1);
				b = (ButtonTabComponent) jtp
						.getTabComponentAt(j - 1);
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
			if (curDiag.currentArrow != null) {
				Integer aid = new Integer(curDiag.currentArrow.id);
				curDiag.arrows.remove(aid);
				curDiag.currentArrow = null;   // terminate arrow drawing
				repaint();
				return;				
			}
			
			if (e.getSource() == jHelpViewer){				
				popup2.dispose();
				repaint();
				return;
			}
			 
			if (-1 < jtp.getSelectedIndex())	{			
				closeTab();
			//else {
				ButtonTabComponent b = (ButtonTabComponent) jtp
						.getTabComponentAt(0);
				
				JLabel j = (JLabel) b.getComponent(0);
				String s = j.getText();
				
				if (
				 	1 == jtp.getTabCount() && (s.equals("(untitled)") || s.equals("")) &&
			   MyOptionPane.YES_OPTION == MyOptionPane.showConfirmDialog(frame,
					"Choose one option", "Leave DrawFBP?",
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
	

	public class SelectionArea extends JComponent implements MouseInputListener {
		static final long serialVersionUID = 111L;
		int oldx, oldy, oldoldx, oldoldy;

		public SelectionArea() {

			setOpaque(true);

			addMouseListener(this);
			addMouseMotionListener(this);
			// setFont(fontg);

			setBackground(Color.WHITE);
			setPreferredSize(new Dimension(4000, 3000)); // experimental

		}

		public void paint(Graphics g) {

			// Paint background if we're opaque.
			//osg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			//		RenderingHints.VALUE_ANTIALIAS_ON);

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
					RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		    osg.setRenderingHints(rh);
			if (isOpaque()) {
				//osg.setColor(getBackground());
				osg.setColor(Color.WHITE);
				
				osg.fillRect(0, 1, (int) (getWidth()/scalingFactor), (int) (getHeight()/scalingFactor - 1));
			}

			int i = jtp.getSelectedIndex();

			ButtonTabComponent b = (ButtonTabComponent) jtp
					.getTabComponentAt(i);
			Diagram diag = b.diag;

			// if (curDiag != diag) {
			// int x = 0; // problem!
			// }

			grid.setSelected(diag.clickToGrid);
			
			repaint();
			
			for (Block block : diag.blocks.values()) {
				block.draw(osg);
			}
			
			for (Arrow arrow : diag.arrows.values()) {
				arrow.draw(osg);
			}
			
			String s = diag.desc;
			if (s != null)
				s = s.replace('\n', ' ');
			else 
				s = " ";
			
			diagDesc.setText(s);
 
			Graphics2D g2d = (Graphics2D) g;
						
			g2d.scale(scalingFactor, scalingFactor);
			
			

			//g2d.translate(xTranslate, yTranslate);

			// Now copy that off-screen image onto the screen
			g2d.drawImage(buffer, 0, 0 , null); 	
		}
		
		
		
		FoundPoint findArrowStart(int xa, int ya) {

			FoundPoint fp = null;
			for (Block block : curDiag.blocks.values()) {
				
				if (!(between(xa, block.leftEdge - 6 * scalingFactor, block.rgtEdge + 6 * scalingFactor)))
					continue;

				if (!(between(ya, block.topEdge - 4 * scalingFactor, block.botEdge + 4 * scalingFactor)))
					continue;
				
				/* check for possible start of arrows */
				if (between(xa, block.leftEdge - 6 * scalingFactor,
						block.leftEdge + 6 * scalingFactor)
						&& between(ya, block.topEdge, block.botEdge)) {
					fp = new FoundPoint(block.leftEdge, ya, Side.LEFT, block);					
					break;
				}

				
					if (between(xa, block.rgtEdge - 6 * scalingFactor,
							block.rgtEdge + 6 * scalingFactor)
							&& between(ya, block.topEdge, block.botEdge)) {
					fp = new FoundPoint(block.rgtEdge, ya, Side.RIGHT, block);					
					break;
				}

				
						if (between(ya, block.topEdge - 4 * scalingFactor,
								block.topEdge + 4 * scalingFactor)
								&& between(xa, block.leftEdge, block.rgtEdge)) {
					fp = new FoundPoint(xa, block.topEdge, Side.TOP, block);					
					break;
				} 
							if (between(ya, block.botEdge - 4 * scalingFactor,
									block.botEdge + 4 * scalingFactor)
									&& between(xa, block.leftEdge,
											block.rgtEdge)) {
					fp = new FoundPoint(xa, block.botEdge, Side.BOTTOM, block);					
					break;
				}				
			}
			return fp;
		}

		public void mouseMoved(MouseEvent e) {			

			int x = (int) Math.round(e.getX() / scalingFactor);
			int y = (int) Math.round(e.getY() / scalingFactor);
			int xa, ya;
			arrowRoot = null;
			
			if (panSwitch) {
				Rectangle r = curDiag.area.getBounds();				
				r = new Rectangle(r.x, r.y, r.width - 20, r.height - 40);
				if (r.contains(x, y))
					frame.setCursor(openPawCursor);
				else
					frame.setCursor(defaultCursor);
			}  

			Point p = new Point(x, y);
			p = gridAlign(p);
			xa = (int) p.getX();
			ya = (int) p.getY();

			if (enclSelForArrow != null) {
				enclSelForArrow.corner = null;
				enclSelForArrow = null;
				repaint();
				return;
			}
			
				
			selBlockM = null;
			// look for corner of an enclosure	- if corner not null, you will see diagonal arrows at corners		
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
					if (between(xa, block.leftEdge - 6,
							block.leftEdge + 6)
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
				boolean udi;
				if (block.type.equals(Block.Types.ENCL_BLOCK)) {
					udi = between(xa, block.leftEdge + block.width / 5,
							block.rgtEdge - block.width / 5)
							&& between(ya, block.topEdge - hh, block.topEdge + hh / 2);						
				}
				else {
					udi = between(xa, block.leftEdge + 6 * scalingFactor,
							block.rgtEdge - 6 * scalingFactor)
							&& between(ya, block.topEdge + 6 * scalingFactor,
									block.botEdge - 6 * scalingFactor);
				}
				
				if (udi) {							 					 
					selBlockM = block;  // mousing select
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
            //curDiag.foundBlock = null;			
			
			FoundPoint fp = findArrowStart(xa, ya);
			if (fp != null)
				arrowRoot = fp;

			repaint();
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {

		}
		
		/*
		 * The following mouse actions are supported:
		 * 
		 *  - click on block - highlights block
		 *  - double-click on block - brings up popup menu if not subnet
		 *                          - brings up subnet if subnet
		 *  - press on side of block starts arrow
		 *  - release on side of block starts or ends arrow
		 *  - click on arrow - brings up popup menu
		 *  - press on block - starts drag		                         
		 * 
		 */

		public void mousePressed(MouseEvent e) {
			Side side = null;
			leftButton = (e.getModifiers() & InputEvent.BUTTON1_MASK) ==
			 InputEvent.BUTTON1_MASK;
			int x = e.getX();
			x = (int) Math.round(x / scalingFactor);
			int y = e.getY();
			y = (int) Math.round(y / scalingFactor);
			int xa, ya;
			
			xa = x;
			ya = y;
			curx = xa;
			cury = ya;
			
			if (panSwitch) {
				//Rectangle r = curDiag.area.getBounds();
				Dimension d = curDiag.area.getSize(); 
				//if (r.contains(x, y)) {
				if (x >= curDiag.area.getX() && x <= curDiag.area.getX() + d.width &&
					y >= curDiag.area.getY() && y <= curDiag.area.getY() + d.height) {	
					frame.setCursor(closedPawCursor);
					panX = xa;
					panY = ya;
					return;
				} else
					frame.setCursor(defaultCursor);
			}
			
			if (e.getClickCount() == 2)
				return;

			curDiag.foundBlock = null;
			selBlock = null;
			selArrow = null;
			blockSelForDragging = null;
			//enclSelForDragging = null;			
			//arrowEndForDragging = null;
			//bendForDragging = null;

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
							&& between(ya, block.topEdge - hh, block.topEdge
									+ hh / 2)) {
						oldoldx = oldx = xa;
						oldoldy = oldy = ya;
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
					 * the following leaves a strip around the outside of each
					 * block that cannot be used for dragging!
					 */
					if (between(xa, block.leftEdge + 4 * scalingFactor,
							block.rgtEdge - 4 * scalingFactor)
							&& between(ya, block.topEdge + 4 * scalingFactor,
									block.botEdge - 4 * scalingFactor)) {
						oldoldx = oldx = xa;
						oldoldy = oldy = ya;
						blockSelForDragging = block;						
						break;
					}
				}

				/* check for possible starts of arrows */
				if (between(xa, block.leftEdge - 4 * scalingFactor,
						block.leftEdge + 4 * scalingFactor)
						&& between(ya, block.topEdge, block.botEdge)) {
					curDiag.foundBlock = block;
					xa = block.leftEdge;
					// ya = ya;
					side = Side.LEFT;
					break;
				}
				if (between(xa, block.rgtEdge - 4 * scalingFactor,
						block.rgtEdge + 4 * scalingFactor)
						&& between(ya, block.topEdge, block.botEdge)) {
					curDiag.foundBlock = block;
					xa = block.rgtEdge;
					// ya = ya;
					side = Side.RIGHT;
					break;
				}
				if (between(ya, block.topEdge - 4 * scalingFactor,
						block.topEdge + 4 * scalingFactor)
						&& between(xa, block.leftEdge, block.rgtEdge)
						&& !(block instanceof Enclosure)) {
					curDiag.foundBlock = block;
					// xa = xa;
					ya = block.topEdge;
					side = Side.TOP;
					break;
				}
				if (between(ya, block.botEdge - 4 * scalingFactor,
						block.botEdge + 4 * scalingFactor)
						&& between(xa, block.leftEdge, block.rgtEdge)) {
					curDiag.foundBlock = block;
					// xa = xa;
					ya = block.botEdge;
					side = Side.BOTTOM;
					break;
				}
				// }

			}

			if (blockSelForDragging != null && blockSelForDragging instanceof Enclosure) {
				ox = blockSelForDragging.cx;
				oy = blockSelForDragging.cy;
				ow = blockSelForDragging.width;
				oh = blockSelForDragging.height;
				repaint();
				return;
			}
			/*
			if (curDiag.currentArrow != null) {
				if (between(xa,curDiag.currentArrow.toX - 4, curDiag.currentArrow.toX + 4) &&
						between(ya,curDiag.currentArrow.toY - 4, curDiag.currentArrow.toY + 4)) {
					curDiag.currentArrow.toX = xa;
					curDiag.currentArrow.toY = ya;				
				}	
				else {
					Integer aid = new Integer(curDiag.currentArrow.id);
				    curDiag.arrows.remove(aid);
				    curDiag.foundBlock = null;
				    curDiag.currentArrow = null;
					//repaint();
				}
				repaint();
			}
			*/
			
			// if no currentDiag.currentArrow, start an arrow
			if (curDiag.currentArrow == null && curDiag.foundBlock != null			
					&& arrowEndForDragging == null) {

				Arrow arrow = new Arrow(curDiag);
				selArrow = arrow;
				// selBlockP = null;
				arrow.fromX = xa;
				arrow.fromY = ya;
				
				arrow.fromId = curDiag.foundBlock.id;
				curDiag.currentArrow = arrow;
				arrow.lastX = xa; // save last x and y
				arrow.lastY = ya;
				Integer aid = new Integer(arrow.id);
				curDiag.arrows.put(aid, arrow);
				
				// curDiag.changed = true;
				//arrow.fromSide = side;	
				//if (from != null) {
					if (side == Side.TOP)
						arrow.fromY = curDiag.foundBlock.cy - curDiag.foundBlock.height / 2;
					else if (side == Side.BOTTOM)
						arrow.fromY = curDiag.foundBlock.cy + curDiag.foundBlock.height / 2;
					else if (side == Side.LEFT)
						arrow.fromX = curDiag.foundBlock.cx - curDiag.foundBlock.width / 2;
					else if (side == Side.RIGHT)
						arrow.fromX = curDiag.foundBlock.cx + curDiag.foundBlock.width / 2;
				//}
					curDiag.foundBlock = null;

				
			}
			repaint();
		}

		public void mouseDragged(MouseEvent e) {
			// boolean left = (e.getModifiers() & InputEvent.BUTTON1_MASK) ==
			// InputEvent.BUTTON1_MASK;

			int x = (int) Math.round(e.getX() / scalingFactor);
			int y = (int) Math.round(e.getY() / scalingFactor);
			int xa, ya;
			//curDiag.arrowRoot = null;

			Point p = new Point(x, y);
			p = gridAlign(p);
			xa = (int) p.getX();
			ya = (int) p.getY();
			
			if (e.getClickCount() == 2) {
			  
				blockSelForDragging = null;
				//enclSelForDragging = null;
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
				frame.repaint();
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
			
			if (blockSelForDragging != null && blockSelForDragging instanceof Enclosure) {
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

				//curDiag.changed = true;
				//repaint();
				//return;
			}

			if (blockSelForDragging != null) { // set in mouse_pressed

				if (curDiag.clickToGrid && Math.abs(xa - oldx) < 6
						&& Math.abs(ya - oldy) < 6) // do not respond to small
													// twitches
					return;
				Block block = blockSelForDragging;
				displayAlignmentLines(block);

				for (Arrow arrow : curDiag.arrows.values()) {

					if (arrow.fromId == block.id) {
						arrow.fromX += xa - oldx;
						arrow.fromY += ya - oldy;
					}
					if (arrow.toId == block.id && !arrow.endsAtLine) {
						arrow.toX += xa - oldx;
						arrow.toY += ya - oldy;
					}
				}
				block.cx += xa - oldx;
				block.cy += ya - oldy;
				block.calcEdges();
				if (arrowRoot != null  && arrowRoot.b == block) {
					arrowRoot.x += xa - oldx;
					arrowRoot.y += ya - oldy;
				}

				if (block instanceof Enclosure) {
					Enclosure enc = (Enclosure) block;

					if (enc.llb != null) {
						for (Block b : enc.llb) {
							b.cx += xa - oldx;
							b.cy += ya - oldy;
							b.calcEdges();
						}
					}
					if (enc.lla != null) {
						for (Arrow a : enc.lla) {
							a.fromX += xa - oldx;
							a.fromY += ya - oldy;
							a.toX += xa - oldx;
							a.toY += ya - oldy;
							if (a.bends != null)
								for (Bend b : a.bends) {
									b.x += xa - oldx;
									b.y += ya - oldy;
								}
						}
					}
				}

				oldx = xa;
				oldy = ya;
				curDiag.changed = true;
				repaint();

				// block.calcEdges();
			}

			if (curDiag.currentArrow != null) { // this ensures the line
												// stays visible
				curDiag.currentArrow.toX = xa;
				curDiag.currentArrow.toY = ya;
				curDiag.changed = true;
			}
			repaint();
		}

		public void mouseReleased(MouseEvent e) {

			int x = (int) e.getX();
			int y = (int) e.getY();

			if (curDiag.jpm != null) {
				curDiag.jpm = null;
				frame.repaint();
				return;
			}

			x = (int) Math.round(x / scalingFactor);
			y = (int) Math.round(y / scalingFactor);
			int xa, ya;
			
			Side side = null;
			Point p2 = new Point(x, y);
			p2 = gridAlign(p2);
			xa = (int) p2.getX();
			ya = (int) p2.getY();

			if (curDiag.area.contains(x, y) && panSwitch) {
				frame.setCursor(openPawCursor);
				repaint();
				return;
			}

			if (curDiag.currentArrow == null) {
				xa = curx; // used for bend dragging
				ya = cury;
			}

			if (e.getClickCount() == 2 || !leftButton) {
				// enclSelForDragging = null;
				// arrowEndForDragging = null;
				// bendForDragging = null;

				if (blockSelForDragging != null) {
					selBlock = blockSelForDragging;
					// this tests if mouse has moved (approximately) - ignore
					// small twitches and also big jumps!
					if (between(oldoldx, x - 4 * scalingFactor,
							x + 4 * scalingFactor)
							&& between(oldoldy, y - 4 * scalingFactor,
									y + 4 * scalingFactor)
							|| Math.abs(oldoldx - x) > 100
							|| Math.abs(oldoldy - y) > 100) {

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
								if (null == openAction(f.getAbsolutePath())
										|| curDiag.diagramIsOpen(
												f.getAbsolutePath()))
									curDiag = saveCurDiag;
							}
						} else {
							blockSelForDragging.buildBlockPopupMenu();
							curDiag.jpm.show(frame, curDiag.xa + 100,
									curDiag.ya + 100);

						}
						// blockSelForDragging = null;
					} else {
						curDiag.changed = true;
					}
					repaint();
					// return;
				}

			}

			if (arrowEndForDragging != null) {
				curDiag.currentArrow = null;
				curDiag.foundBlock = null;
				// curDiag.changed = true;
				Arrow arr = arrowEndForDragging;

				for (Block block : curDiag.blocks.values()) {
					if (arr.tailMarked) {
						arr.fromId = -1;
						if (arr.touches(block, arr.fromX, arr.fromY)) {
							arr.fromId = block.id;
							break;
						}
					}
					if (arr.headMarked) {
						arr.toId = -1;
						if (arrowEndForDragging.touches(block,
								arrowEndForDragging.toX,
								arrowEndForDragging.toY)) {
							arr.toId = block.id;
							break;
						}
					}
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

				// boolean atFromEnd = true;
				for (Arrow arrow : curDiag.arrows.values()) {
					if (arrow.fromId == blockSelForDragging.id) {
						// arrow.adjust(atFromEnd);
						// arrow.adjustSlope(fromEnd);
						arrow.fromX += blockSelForDragging.cx - savex;
						arrow.fromY += blockSelForDragging.cy - savey;
					}
					if (arrow.toId == blockSelForDragging.id
							&& !arrow.endsAtLine) {
						// arrow.adjust(!atFromEnd);
						// arrow.adjustSlope(!fromEnd);
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
			curDiag.foundBlock = null;
			if (curDiag.currentArrow == null) {

				// Look for a line to detect - for deletion, etc. - logic to end arrow at a line comes in a later section... 
				curDiag.foundArrow = null;
				// if (!leftButton) {
				for (Arrow arrow : curDiag.arrows.values()) {
					if (curDiag.matchArrow(xa, ya, arrow)) {
						curDiag.foundArrow = arrow;
						break;
					}
				}

				selArrow = curDiag.foundArrow;
				// selBlockP = null;
				// curDiag.changed = true;
				if (curDiag.foundArrow != null) {
					Arrow arr = curDiag.foundArrow;
					if (arr.endsAtLine || arr.endsAtBlock) {

						if (curDiag.findArrowCrossing) {
							processSubnetPort(arr);
							repaint();
							return;
						}
						arr.buildArrowPopupMenu();
						// curDiag.currentArrow.lastX = xa;
						// curDiag.currentArrow.lastY = ya;
						curDiag.jpm.show(e.getComponent(), xa, ya);
						repaint();
						return;
					}
				}
				else
					if (curDiag.findArrowCrossing)
						MyOptionPane.showMessageDialog(frame,								
						"No arrow detected");
				curDiag.findArrowCrossing = false;
				// }

				// if (curDiag.currentArrow == null) {

				curDiag.foundBlock = null;

				// Check if we are within a block
				
				for (Block block : curDiag.blocks.values()) {
					// block.calcEdges();
					if (!(block instanceof Enclosure)) {
						if (between(xa, block.cx - block.width / 2,
								block.cx + block.width / 2)
								&& between(ya, block.cy - block.height / 2,
										block.cy + block.height / 2)) {
							curDiag.foundBlock = block;
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
							curDiag.foundBlock = block;
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

				curDiag.xa = xa;
				curDiag.ya = ya;
				if (!(blockType.equals("")) && null != createBlock(blockType))
					curDiag.changed = true;
				frame.repaint();
				// repaint();

				repaint();
				return;
			}

			// curDiag.currentArrow is not null....

			// check for end of arrow

			curDiag.foundBlock = null;

			FoundPoint fp = findArrowStart(xa, ya);    
			if (fp != null) {
				curDiag.foundBlock = fp.b;
				side = fp.side;
			}

			if (curDiag.foundBlock != null // && leftButton
			) {
				if (between(curDiag.currentArrow.fromX, x - 4 * scalingFactor,
						x + 4 * scalingFactor)
						&& between(curDiag.currentArrow.fromY,
								y - 4 * scalingFactor, y + 4 * scalingFactor))
					return;
				if (curDiag.foundBlock.id == curDiag.currentArrow.fromId) {

					if (MyOptionPane.NO_OPTION == MyOptionPane.showConfirmDialog(
							frame,
							"Connecting arrow to originating block is deadlock-prone - do anyway?",
							"Allow?", MyOptionPane.YES_NO_OPTION)) {
						Integer aid = new Integer(curDiag.currentArrow.id);
						curDiag.arrows.remove(aid);
						curDiag.foundBlock = null;
						curDiag.currentArrow = null;

						repaint();
						return;
					}
				}
				boolean OK = true;
				Block from = curDiag.blocks
						.get(new Integer(curDiag.currentArrow.fromId));
				if ((curDiag.foundBlock instanceof ProcessBlock
						|| curDiag.foundBlock instanceof ExtPortBlock)
						&& !(from instanceof IIPBlock)) {
					if (side == Side.BOTTOM) {
						int answer = MyOptionPane.showConfirmDialog(frame,
								"Connect arrow to bottom of block?",
								"Please choose one", MyOptionPane.YES_NO_OPTION);
						if (answer != MyOptionPane.YES_OPTION)
							OK = false;
					}
					if (side == Side.RIGHT) {
						int answer = MyOptionPane.showConfirmDialog(frame,
								"Connect arrow to righthand side?",
								"Please choose one", MyOptionPane.YES_NO_OPTION);
						if (answer != MyOptionPane.YES_OPTION)
							OK = false;
					}
				}
				if (!OK) {
					// MyOptionPane.showMessageDialog(frame,
					// "Cannot end an arrow here");
					Integer aid = new Integer(curDiag.currentArrow.id);
					curDiag.arrows.remove(aid);
					curDiag.foundBlock = null;
					curDiag.currentArrow = null;
					repaint();
					return;
				}

				Arrow a = curDiag.currentArrow;
				a.endsAtBlock = true;
				a.toId = curDiag.foundBlock.id;

				if (xa != curx) { // make sure t not
					// zero!
					double s = ya - a.lastY;
					double t = xa - a.lastX;
					s = s / t;
					if (Math.abs(s) < FORCE_HORIZONTAL) // force horizontal
						ya = a.lastY;
					if (Math.abs(s) > FORCE_VERTICAL) // force vertical
						xa = a.lastX;
				}

				a.toX = xa;
				a.toY = ya;

				// a.toSide = side;
				from = curDiag.blocks.get(new Integer(a.fromId));
				Block to = curDiag.blocks.get(new Integer(a.toId));

				if (from != null
						&& (from instanceof ProcessBlock
								|| from instanceof ExtPortBlock
								|| from instanceof Enclosure
								|| from instanceof IIPBlock)
						&& (a.endsAtLine
								|| (to != null && (to instanceof ProcessBlock
										|| to instanceof ExtPortBlock
										|| to instanceof Enclosure)))) {

					if (!(from instanceof FileBlock || from instanceof PersonBlock || from instanceof ReportBlock || from instanceof LegendBlock ||
							to instanceof FileBlock || to instanceof PersonBlock || to instanceof ReportBlock || to instanceof LegendBlock 	) ) {
					if (!(from instanceof IIPBlock) && (a.upStreamPort == null
							|| a.upStreamPort.trim().equals("")))
						a.upStreamPort = "OUT";
					if (!a.endsAtLine && (a.downStreamPort == null
							|| a.downStreamPort.trim().equals("")))
						a.downStreamPort = "IN";
				}
				}

				Boolean error = false;
				if (to instanceof IIPBlock && from instanceof ProcessBlock) {
					a.reverseDirection();
					// MyOptionPane
					// .showMessageDialog(frame,
					// "Direction of arrow has been reversed");
				}
				if (from instanceof ExtPortBlock && (from.type
						.equals(Block.Types.EXTPORT_OUT_BLOCK)
						|| from.type.equals(Block.Types.EXTPORT_OUTIN_BLOCK)
								&& a.fromX < from.cx))
					error = true;
				else
					if (to instanceof ExtPortBlock && (to.type
							.equals(Block.Types.EXTPORT_IN_BLOCK)
							|| to.type.equals(Block.Types.EXTPORT_OUTIN_BLOCK)
									&& a.toX > to.cx))
					error = true;

				if (!a.checkSides())
					error = true;

				if (error) {
					MyOptionPane.showMessageDialog(frame,
							"Arrow attached to one or both wrong side(s) of blocks", MyOptionPane.WARNING_MESSAGE);
					Integer aid = new Integer(a.id);
					curDiag.arrows.remove(aid);
				} else {
					curDiag.changed = true;
					// checkCompatibility(a);
				}
				curDiag.foundBlock = null;

				curDiag.currentArrow = null;
				// curDiag.changed = true;

				repaint();
				return;
			}
			// currentDiag.foundBlock must be null
			// see if we can end an arrow on a line or line segment
			if (curDiag.currentArrow != null && curDiag.foundBlock == null
			// && leftButton
			) {

				curDiag.foundArrow = null;
				Arrow a = curDiag.currentArrow;
				for (Arrow arrow : curDiag.arrows.values()) {
					if (arrow != a && curDiag.matchArrow(xa, ya, arrow))
						curDiag.foundArrow = arrow;
				}

				if (curDiag.foundArrow != null) { // && leftButton

					if (x != curx) {
						double s = y - a.lastY;
						double t = x - a.lastX;
						s = s / t;
						if (Math.abs(s) < FORCE_HORIZONTAL) // force horizontal
							y = curDiag.currentArrow.lastY;
						if (Math.abs(s) > FORCE_VERTICAL) // force vertical
							x = curDiag.currentArrow.lastX;
					}
					a.toX = x; // ????????????
					// curDiag.currentArrow.toY = y; //????????????
					a.toY = y;
					curDiag.currentArrow.endsAtLine = true;

					// use id of target line, not of target block
					curDiag.currentArrow.toId = curDiag.foundArrow.id;

					Block from = curDiag.blocks.get(new Integer(a.fromId));
					// Block to = curDiag.blocks.get(new Integer(
					// a.toId));
					Arrow a2 = curDiag.currentArrow.findTerminalArrow();
					Block to = curDiag.blocks.get(new Integer(a2.toId));

					if (from != null
							&& (from instanceof ProcessBlock
									|| from instanceof ExtPortBlock
									|| from instanceof Enclosure
									|| from instanceof IIPBlock)
							&& (a.endsAtLine || (to != null
									&& (to instanceof ProcessBlock
											|| to instanceof ExtPortBlock
											|| to instanceof Enclosure)))) {

						if (!(from instanceof FileBlock || from instanceof PersonBlock || from instanceof ReportBlock || from instanceof LegendBlock ||
								to instanceof FileBlock || to instanceof PersonBlock || to instanceof ReportBlock || to instanceof LegendBlock 	) ) {
						if (!(from instanceof IIPBlock)
								&& (a.upStreamPort == null
										|| a.upStreamPort.trim().equals("")))
							a.upStreamPort = "OUT";
						if (!a.endsAtLine && (a.downStreamPort == null
								|| a.downStreamPort.trim().equals("")))
							a.downStreamPort = "IN";
						if (a.endsAtLine && (a2.downStreamPort == null
								|| a2.downStreamPort.trim().equals("")))
							a2.downStreamPort = "IN";
						}
					}
					a.downStreamPort = a2.downStreamPort;
					// Block from = curDiag.blocks.get(new Integer(
					// a.fromId));
					// Arrow a2 = curDiag.currentArrow.findTerminalArrow();
					// to = curDiag.blocks.get(new Integer(a2.toId));

					if (to == from) {
						if (MyOptionPane.NO_OPTION == MyOptionPane
								.showConfirmDialog(frame,
										"Connecting arrow to originating block is deadlock-prone - do anyway?",
										"Allow?", MyOptionPane.YES_NO_OPTION)) {
							Integer aid = new Integer(curDiag.currentArrow.id);
							curDiag.arrows.remove(aid);
							curDiag.foundBlock = null;
							curDiag.currentArrow = null;

							repaint();
							return;
						}

					}

					boolean error = true;
					if (from instanceof ExtPortBlock
							&& from.type.equals(Block.Types.EXTPORT_OUT_BLOCK))
						MyOptionPane.showMessageDialog(frame,
								"Arrow in wrong direction", MyOptionPane.ERROR_MESSAGE);
					else
						if (to instanceof ExtPortBlock
								&& to.type.equals(Block.Types.EXTPORT_IN_BLOCK))
						MyOptionPane.showMessageDialog(frame,
								"Arrow in wrong direction", MyOptionPane.ERROR_MESSAGE);
					else
						error = false;
					if (error) {
						Integer aid = new Integer(curDiag.currentArrow.id);
						curDiag.arrows.remove(aid);
					} else {
						curDiag.changed = true;
						// checkCompatibility(curDiag.currentArrow);
						if (to != null) {
							if (side == Side.TOP)
								curDiag.currentArrow.toY = to.cy
										- to.height / 2;
							else if (side == Side.BOTTOM)
								curDiag.currentArrow.toY = to.cy
										+ to.height / 2;
							else if (side == Side.LEFT)
								curDiag.currentArrow.toX = to.cx - to.width / 2;
							else if (side == Side.RIGHT)
								curDiag.currentArrow.toX = to.cx + to.width / 2;
						}
					}

					curDiag.currentArrow = null;

					repaint();
					return;
				}

				// else if (leftButton) { // currentDiag.foundArrow is null, so
				// we may
				// have a
				// bend

				if (curDiag.currentArrow != null) {
					if (!(between(xa, curDiag.currentArrow.toX - 4,
							curDiag.currentArrow.toX + 4)
							&& between(ya, curDiag.currentArrow.toY - 4,
									curDiag.currentArrow.toY + 4))) {
						// curDiag.currentArrow.toX = xa;
						// curDiag.currentArrow.toY = ya;
						// }
						// else {
						Integer aid = new Integer(curDiag.currentArrow.id);
						curDiag.arrows.remove(aid);
						curDiag.foundBlock = null;
						curDiag.currentArrow = null;
						repaint();
						return;
					}
					// repaint();
				}

				if (curDiag.currentArrow.bends == null) {
					curDiag.currentArrow.bends = new LinkedList<Bend>();
				}
				x = xa;
				y = ya;

				if (xa != curDiag.currentArrow.lastX) {
					double s = ya - curDiag.currentArrow.lastY;
					double t = xa - curDiag.currentArrow.lastX;
					s = s / t;
					if (Math.abs(s) < FORCE_HORIZONTAL) // force horizontal
						ya = curDiag.currentArrow.lastY;
					if (Math.abs(s) > FORCE_VERTICAL) // force vertical
						xa = curDiag.currentArrow.lastX;
				}
				Bend bend = new Bend(xa, ya);
				curDiag.currentArrow.bends.add(bend);
				curDiag.currentArrow.lastX = x;
				curDiag.currentArrow.lastY = y;
				curDiag.currentArrow.toX = x;
				curDiag.currentArrow.toY = y;
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
