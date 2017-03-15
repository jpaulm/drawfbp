package com.jpaulmorrison.graphics;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;


import javax.swing.*;

import com.jpaulmorrison.graphics.DrawFBP.GenLang;

public class Block implements ActionListener {
	String type;   // One of Types (single character strings)
	DrawFBP driver;
	int leftEdge, rgtEdge, topEdge, botEdge;
	int width, height;

	String description;
	//String descMod;  // modified a lot for .fbp notation; slightly, for other notations (" -> _)

	String diagramFileName;

	String fullClassName; // (file name plus class name) or NoFlo name (now
							// shifted to codeFileName)

	int cx, cy; // coords of centre

	// int x, y; // used for dragging

	// int sdcx, sdcy; // center before adjustment

	int id;

	int tlx, tly;

	static final int BLOCKWIDTH = 92; // was 76;

	static final int BLOCKHEIGHT = 64; // was 52;

	boolean multiplex = false;
	boolean deleteOnSave = false;

	Diagram diag;

	LegendBlock lblk;
	String mpxfactor = null;
	HashMap<String, AInPort> inputPortAttrs;
	HashMap<String, AOutPort> outputPortAttrs;
	int scan_pos = 0;
	Block vNeighbour, hNeighbour; // block at same position, vert. or horiz.

	boolean visible = true;	

	/* next four fields are not stored in .drw files */	
	
	URLClassLoader classLoader = null;
	Class<?> javaClass; // selected Java class for block (fullClassName is equivalent in String format)
	String compDescr;  // used for annotations only
	boolean isSubnet;
	
	//JMenuItem[] sMenu;
	Color lg = new Color(240, 240, 240); // very light gray
	Color ly = new Color(255, 255, 200); // light yellow
	Color lb = new Color(200, 255, 255); // light blue (turquoise actually)
	Color grey = new Color(170, 244, 255); // sort of grey (?)
	
	int ROWSIZE = 5;
	String codeFileName;
	
	HashMap<String, Integer> portlist;

	static public class Types {
		static String PROCESS_BLOCK = "B";
		static String EXTPORT_IN_BLOCK = "C";
		static String EXTPORT_OUT_BLOCK = "D";
		static String EXTPORT_OUTIN_BLOCK = "E";
		static String FILE_BLOCK = "F";
		static String IIP_BLOCK = "I";
		static String LEGEND_BLOCK = "L";
		static String ENCL_BLOCK = "O";
		static String PERSON_BLOCK = "P";
		static String REPORT_BLOCK = "R";
	}

	Block(Diagram d) {

		diag = d;
		driver = d.driver;

		type = Block.Types.PROCESS_BLOCK;

		diagramFileName = null;
		fullClassName = null;
		// HashMap<String, String> associatedCode = null;

	}

	void draw(Graphics2D g) {

		 
		if (!visible && this != driver.selBlock) {
			showZones(g);
			return;
		}

		 
		if (this == driver.selBlock && !(this instanceof ProcessBlock)) {
			showArrowEndAreas(g);
			return;
		}
         	
		int tlx = cx - width / 2;
		int tly = cy - height / 2;
		g.setFont(driver.fontg);
		
		calcDiagMaxAndMin(tlx - 20, cx + width / 2 + 20, cy - height / 2, cy + height / 2 + 40);

		g.setColor(Color.BLACK);
		g.drawRoundRect(tlx, tly, width, height, 6, 6);
		if (this == driver.selBlock)
			g.setColor(ly); // light yellow
		else  
			g.setColor(lb); // light turquoise

		g.fillRoundRect(tlx + 1, tly + 1, width - 1, height - 1, 6, 6);

		if (multiplex) {
			int x, y;
			String s = mpxfactor;
			if (s == null)
				s = " ";
			int i = s.length() * driver.gFontWidth + 10;
			x = tlx - i;
			y = cy - 20 / 2;
			g.setColor(Color.BLACK);
			g.drawRoundRect(x, y, i - 1, 20, 2, 2);
			if (this == driver.selBlock)
				g.setColor(ly); // light yellow
			else
				g.setColor(lb); // light turquoise
			g.fillRoundRect(x + 1, y + 1, i - 2, 19, 2, 2);
			g.setColor(Color.BLACK);
			if (mpxfactor != null)
				g.drawString(mpxfactor, x + 5, y + 15);
		}
		g.setColor(Color.BLACK);

		// if composite, draw with an inner outline
		// if (diagramFileName != null)
		if (isSubnet) // block is a subnet
			g.drawRoundRect(tlx + 2, tly + 2, width - 4, height - 4, 6, 6);

		// g.drawLine(tlx, tly + driver.fontHeight + driver.fontHeight
		// / 2 +
		// 3, tlx +
		// width,
		// tly + driver.fontHeight + driver.fontHeight / 2 + 3);

		// showZones(g);

		if (description != null) {
			centreDesc(g);
		}

		if (!visible && this == driver.selBlock)
			g.drawLine(tlx, tly, cx + width / 2, cy + height / 2);

		int y = cy + height / 2 + driver.gFontHeight + driver.gFontHeight / 2;

		if (diagramFileName != null) {
			Font fontsave = g.getFont();
			g.setFont(driver.fontf);
			g.setColor(Color.GRAY);
			File gFile = new File(diagramFileName);
			String name = gFile.getName();
			int x = cx - name.length() * driver.gFontWidth / 2;
			g.drawString(name, x, y);
			g.setFont(fontsave);
			y += driver.gFontHeight;
			calcDiagMaxAndMin(x - 20, x + name.length() * driver.gFontWidth + 20, cy - height / 2, y + 40);
		}

		String name = null;
		if (diag.diagLang != null
				&& diag.diagLang.label.equals("Java")) {
			if (javaClass != null) {
				Font fontsave = g.getFont();
				g.setFont(driver.fontf);
				g.setColor(Color.BLUE);
				name = javaClass.getSimpleName()  + ".class";
				int x = cx - name.length() * driver.gFontWidth / 2;
				g.drawString(name, x, y);
				g.setFont(fontsave);
				y += driver.gFontHeight;
				calcDiagMaxAndMin(x - 20, x + name.length() * driver.gFontWidth + 20, cy - height / 2, y + 40);
			}

			else if (fullClassName != null) {
				Font fontsave = g.getFont();
				g.setFont(driver.fontf);
				name = "Class not found or out of date - rechoose comp/subnet";   // ??????????????????
				int x = cx - name.length() * driver.gFontWidth / 2;
				g.drawString(name, x, y);
				g.setFont(fontsave);
				y += driver.gFontHeight;
				name = fullClassName;
				g.setColor(Color.RED);
				x = cx - name.length() * driver.gFontWidth / 2;
				g.drawString(name, x, y);
				g.setFont(fontsave);
				g.setColor(Color.BLACK);
				y += driver.gFontHeight;
				calcDiagMaxAndMin(x - 20, x + name.length() * driver.gFontWidth + 20, cy - height / 2, y + 40);
			}
		}
		if (codeFileName != null) {
			Font fontsave = g.getFont();
			g.setFont(driver.fontf);
			name = codeFileName;
			int i = name.lastIndexOf(File.separator);
			if (i == -1)
				i = name.lastIndexOf("/");
			name = name.substring(i + 1);
			g.setColor(Color.BLACK);
			int x = cx - name.length() * driver.gFontWidth / 2;
			g.drawString(name, x, y);
			g.setFont(fontsave);
			g.setColor(Color.BLACK);
			calcDiagMaxAndMin(x - 20, x + name.length() * driver.gFontWidth + 20, cy - height / 2, y + 40);
		}
		if (hNeighbour != null) {
			g.setColor(Color.ORANGE);
			if (hNeighbour.cx < cx)
				g.drawLine(hNeighbour.cx - hNeighbour.width / 2, hNeighbour.cy
						+ hNeighbour.height / 2, cx + width / 2, hNeighbour.cy
						+ hNeighbour.height / 2);
			else
				g.drawLine(hNeighbour.cx + hNeighbour.width / 2, hNeighbour.cy
						+ hNeighbour.height / 2, cx - width / 2, hNeighbour.cy
						+ hNeighbour.height / 2);
			g.setColor(Color.BLACK);
		}
		if (vNeighbour != null) {
			g.setColor(Color.ORANGE);
			if (vNeighbour.cy < cy)
				g.drawLine(vNeighbour.cx - vNeighbour.width / 2, vNeighbour.cy
						- vNeighbour.height / 2, vNeighbour.cx
						- vNeighbour.width / 2, cy + height / 2);
			else
				g.drawLine(vNeighbour.cx - vNeighbour.width / 2, vNeighbour.cy
						+ vNeighbour.height / 2, vNeighbour.cx
						- vNeighbour.width / 2, cy - height / 2);
			g.setColor(Color.BLACK);
		}
		
		if (driver.arrowRoot != null && driver.arrowRoot.b == this) {
			if (driver.arrowEndForDragging == null
					|| !driver.arrowEndForDragging.headMarked
							&& !driver.arrowEndForDragging.tailMarked) {	
				Enclosure enc = null;
				if (type == Block.Types.ENCL_BLOCK)
					enc = (Enclosure) this;

				if (enc == null || enc.corner == null) {
					Color col = g.getColor();
					g.setColor(Color.BLUE);
					g.drawOval(driver.arrowRoot.x - 4,
							driver.arrowRoot.y - 4, 8, 8);
					g.setColor(col);
				}
			}
		}
	}
	 
	
	void calcEdges() {
		leftEdge = cx - width / 2;
		rgtEdge = cx + width / 2;
		topEdge = cy - height / 2;
		botEdge = cy + height / 2;
	}
	void calcDiagMaxAndMin(int xmin, int xmax, int ymin, int ymax) {
		//if (visible) {
			diag.maxX = Math.max(xmax + 20, diag.maxX);
			diag.minX = Math.min(xmin - 20, diag.minX);
			diag.maxY = Math.max(ymax, diag.maxY);
			diag.minY = Math.min(ymin - 20, diag.minY);
		//}
	}
	void centreDesc(Graphics2D g) {

		g.setColor(Color.BLACK);

		int x = 0;
		int y = 0;
		int minX = Integer.MAX_VALUE;
		int maxX = 0;

		String str[] = description.split("\n");
		boolean nonBlankLineFound = false;
		FontMetrics metrics = g.getFontMetrics(g.getFont());

		for (int i = 0; i < str.length; i++) {
			x = 0;
			for (int j = 0; j < str[i].length(); j++) {
				char c = str[i].charAt(j);
				if (c != ' ')
					minX = Math.min(x, minX);

				x += metrics.charWidth(c);
			}
			maxX = Math.max(x, maxX);
			if (!(str[i].trim().equals(""))) {
				// minY = Math.min(minY, y);
				y += driver.gFontHeight;
				nonBlankLineFound = true;
			}
			if (nonBlankLineFound) {
				// maxY = y;
			}
		}

		x = (maxX - minX) / 2; // find half width
		x = cx - x;

		y = y / 2; // find half height
		if (this instanceof ReportBlock)
			y = cy - y;
		else if (this instanceof PersonBlock)
			y = cy + height / 2 + driver.gFontHeight;
		else
			y = cy - y + driver.gFontHeight;

		y -= driver.gFontHeight / 3; // fudge!
		int saveY = y;

		for (int i = 0; i < str.length; i++) {
			g.drawString(str[i], x, y);
			y += driver.gFontHeight;
		}
		if (this instanceof LegendBlock) {
			height = y - saveY + 24;
			width = maxX - minX + 24;
			// calcEdges();
		}
	}

	 
	void showZones(Graphics2D g) {
		if (diag.currentArrow == null) {
			if (driver.selBlockM == this)
				showArrowEndAreas(g);
		} else if (diag.currentArrow.fromId != id)
			showArrowEndAreas(g);
	}
 
	String serialize() {
		String s = "<block> <x> " + cx + " </x> <y> " + cy + " </y> <id> " + id
				+ " </id> <type>" + type + "</type> ";
		s += "<width>" + width + "</width> <height>" + height + "</height> ";
		if (description != null) {
			s += "<description>";
			for (int i = 0; i < description.length(); i++) {
				if (description.charAt(i) == '<'
						|| description.charAt(i) == '>') {
					s += '\\'; // protect the angle bracket
				}
				s += description.charAt(i);
			}
			s += "</description> ";
		}

		if (diagramFileName != null) {
			//String relDiagFileName = DrawFBP.makeRelFileName(diagramFileName,
			//		diag.diagFile.getAbsolutePath());
			s += "<diagramfilename>" + diagramFileName + "</diagramfilename> ";			
		}
		if (codeFileName != null) {
			//String relCodeFileName = DrawFBP.makeRelFileName(codeFileName,
			//		diag.diagFile.getAbsolutePath());
			s += "<codefilename>" + codeFileName + "</codefilename> ";
		}
		if (fullClassName != null) {
			String t = fullClassName;
			if (t.toLowerCase().endsWith(".class"))
				t = t.substring(0, t.length() - 6);
			s += "<blockclassname>" + t + "</blockclassname> ";
		}
		if (this instanceof ExtPortBlock) {
			ExtPortBlock eb = (ExtPortBlock) this;
			//if (eb.substreamSensitive)
				s += "<substreamsensitive>" + (eb.substreamSensitive?"true":"false") + "</substreamsensitive>";
		}
		// if (multiplex)
			s += "<multiplex>" + (multiplex?"true":"false") + "</multiplex>";
		//if (!visible)
			s += "<invisible>" + (!visible?"true":"false") + "</invisible>";
		if (mpxfactor != null)
			s += "<mpxfactor>" + mpxfactor + "</mpxfactor> \n";
		if (this instanceof Enclosure) {
			Enclosure ol = (Enclosure) this;
			// if (ol.description != null)
			// s += "<description>" + ol.description + "</description> ";
			s += "\n";
			s += "<subnetports>";
			for (SubnetPort snp : ol.subnetPorts) {
				s += "<subnetport> <y>" + snp.y + "</y>";
				if (snp.name != null)
					s += " <name>" + snp.name + "</name>";
				String side = (snp.side == DrawFBP.Side.LEFT) ? "L" : "R";
				s += " <side>" + side + "</side>";
				//if (snp.substreamSensitive)
					s += "<substreamsensitive>" + (snp.substreamSensitive?"true":"false") + "</substreamsensitive>";
				s += "</subnetport> \n";
			}
			s += "</subnetports>";
		}
		s += "</block> \n";
		return s;

	}

	void buildBlockFromXML(HashMap<String, String> item) {
		// Build a block using a HashMap built using the XML description

		String s;
		type = item.get("type");
		description = item.get("description");
		if (type.equals("I") && description != null && description.substring(0,1).equals("\""))
			description = description.substring(1,description.length() - 1);				
		
		if (type == null)
			type = Block.Types.PROCESS_BLOCK;
		codeFileName = item.get("codefilename");
		//if (codeFileName != null) { 
		//	codeFileName = DrawFBP.makeAbsFileName(codeFileName,
		//			diag.diagFile.getAbsolutePath());
			// diag.changeCompLang();
		//}

		diagramFileName = item.get("diagramfilename");
		//if (diagramFileName != null) {
		//	File fp = diag.diagFile.getParentFile();
		//	diagramFileName = DrawFBP.makeAbsFileName(diagramFileName,
		//			fp.getAbsolutePath());
			// diagramFileName = file.getAbsolutePath();
		//}

		if (diagramFileName != null)
			isSubnet = true;

		fullClassName = item.get("blockclassname");
		getClassInfo(fullClassName); 
		
		s = item.get("x").trim();
		cx = Integer.parseInt(s);
		s = item.get("y").trim();
		cy = Integer.parseInt(s);
		s = item.get("id").trim();
		id = Integer.parseInt(s);
		s = item.get("width");
		width = Integer.parseInt(s.trim());
		s = item.get("height");
		if (s != null)
			height = Integer.parseInt(s.trim());
		s = item.get("multiplex");
		if (s != null)
			multiplex = true;
		s = item.get("mpxfactor");
		if (s != null)
			mpxfactor = s;
		s = item.get("invisible");
		if (s != null)
			visible = false;
		if (this instanceof Enclosure) {
			Enclosure ol = (Enclosure) this;
			ol.description = item.get("description");
		}
		calcEdges();

		diag.maxBlockNo = Math.max(id, diag.maxBlockNo);

		// driver.frame.setSize(driver.maxX, driver.maxY);
		if (this instanceof ProcessBlock && javaClass != null) {
			buildMetadata();
		}

	}
	
	void getClassInfo(String fcn){  
		fullClassName = fcn;
		//fullClassName = item.get("blockclassname");
		if (fullClassName != null) {
			if (fullClassName.indexOf("!") == -1) {// if no "!", language is not
													// Java...
				driver.tryFindJarFile = false;
				if (fullClassName.toLowerCase().endsWith(".json")) {
					codeFileName = fullClassName;
					fullClassName = null;
				}
				return;
			}
			if (driver.tryFindJarFile) {
				if (!driver.getJavaFBPJarFile()) {
					MyOptionPane
							.showMessageDialog(
									driver.frame,
									"Unable to read JavaFBP jar file - "
											+ "so cannot process class information for "
											+ description, MyOptionPane.ERROR_MESSAGE);
					driver.tryFindJarFile = false;
				} else 
					loadClass();
			}
			// diag.compLang = driver.findGLFromLabel("Java");
		}
		}
	

	
	
	void loadClass(){
		int i = fullClassName.indexOf("!");
		if (i == -1)
			return;
		String fn = fullClassName.substring(0, i);  // jar file name or class folder
		String cn = fullClassName.substring(i + 1);  // class name
	
		//LinkedList<URL> ll = new LinkedList<URL>();
		
		if (!(fn.endsWith("jar")))
				fn += File.separator;
		
		File f = new File(fn);	
		
		URL[] urls = buildUrls(f);
		
		if (urls == null)
			javaClass = null;
		else 
				 
		try {
			
			classLoader = new URLClassLoader(urls);
		
			javaClass = classLoader.loadClass(cn);
			
			} catch (ClassNotFoundException e) {
				//System.out.println("Missing class name in " + fullClassName);
				MyOptionPane.showMessageDialog(driver.frame,
						"Class name not found: " + fullClassName, MyOptionPane.ERROR_MESSAGE);
				// e.printStackTrace();
				javaClass = null;
			} catch (NoClassDefFoundError e) {
				//System.out.println("Missing internal class name in "
				//		+ fullClassName);
				MyOptionPane.showMessageDialog(driver.frame,
						"Internal class name not found: " + fullClassName, MyOptionPane.ERROR_MESSAGE);
				// e.printStackTrace();
				javaClass = null;
			} 
		
		
	}

	
	Class<?> getSelectedClass(String jar, String jf, boolean injar) {

		if (jf.trim().equals("")) {
			return null;
		}
				
		//tentative...
		String fn = "";
		
		if (injar) {
		    fn = jar;
		    if (fn.lastIndexOf("javafbp") == -1){
		    	fn = driver.javaFBPJarFile;
		    }
		}
		else
			fn = driver.javaFBPJarFile;
		
		int j = fn.lastIndexOf("javafbp") + 8;
		String seg = "engine.";
		if (0 <= fn.substring(j, j + 1).compareTo("4"))  // if javafbp jar file version not less than 4.0.0
		    seg = "core.engine.";
		String owner = "jpmorrsn";
		if (0 <= fn.substring(j, j + 3).compareTo("4.1"))  // if javafbp jar file version not less than 4.0.0
		    owner = "jpaulmorrison";
		
		Class<?> cls;	
			
		try {
			Class<?> compClass = classLoader
					.loadClass("com." + owner + ".fbp." + seg + "Component");

			Class<?> networkClass = classLoader
					.loadClass("com." + owner + ".fbp." + seg + "Network");

			Class<?> subnetClass = classLoader
					.loadClass("com." + owner + ".fbp." + seg + "SubNet");

			int i = jf.lastIndexOf(".class"); 			
			if (i != -1)
				jf = jf.substring(0, i);
			jf = jf.replace('/', '.');
			cls = classLoader.loadClass(jf);

			if (cls.getCanonicalName().equals(compClass.getCanonicalName()) || 
					cls.getCanonicalName().equals(networkClass.getCanonicalName())
					|| cls.getCanonicalName().equals(subnetClass.getCanonicalName())){
					return null;
			}

			Class<?> cs = cls.getSuperclass();
			if (cs == null || !(cs.getCanonicalName().equals(compClass.getCanonicalName())  ||
					cs.getCanonicalName().equals(subnetClass.getCanonicalName()))) {
				MyOptionPane.showMessageDialog(driver.frame,
						"Class file not a valid FBP component", MyOptionPane.ERROR_MESSAGE);				
				return null;
			}

			boolean mainPresent = true;
			try {
				cls.getMethod("main", String[].class);
			} catch (NoSuchMethodException e) {
				mainPresent = false;
			} catch (NoClassDefFoundError e) {
				mainPresent = false;
			} catch (SecurityException e2) {
				mainPresent = false;
			}

			if (mainPresent) {
				MyOptionPane.showMessageDialog(driver.frame,
						"Class file contains a 'main' method", MyOptionPane.ERROR_MESSAGE);
				return null;
			} else {
				//this.classLoader = classLoader;
				return cls;
			}

		} catch (ClassNotFoundException e) {
			System.err.println("Class not found exception:");
			e.printStackTrace();
			return null;
		} 
	}

	 
	void showArrowEndAreas(Graphics2D g) {
		g.setColor(grey);   

		g.fillRect(cx - width / 2 - 1, cy - height / 2 - 1, 4, height); // left
		if (!(this instanceof Enclosure))
			g.fillRect(cx - width / 2 - 1, cy - height / 2 - 1, width + 3, 4); // top
		if (!(this instanceof ReportBlock)) {
			g.fillRect(cx - width / 2 - 1, cy + height / 2 - 2, width + 3, 4); // bottom
			g.fillRect(cx + width / 2 - 1, cy - height / 2 - 1, 4, height); // right
		} else
			g.fillRect(cx + width / 2 - 1, cy - height / 2 - 1, 4, height - 12); // right
		g.setColor(Color.BLACK);
	}
	  

	void buildMetadata() {
		inputPortAttrs = new HashMap<String, AInPort>();
		outputPortAttrs = new HashMap<String, AOutPort>();
		String s = driver.javaFBPJarFile; 
		int j = s.lastIndexOf("javafbp") + 8;
		String seg = "engine.";
		if (0 <= s.substring(j, j + 1).compareTo("4"))  // if javafbp jar file version not less than 4.0.0
		    seg = "core.engine.";
		String owner = "jpmorrsn";
		if (0 <= s.substring(j, j + 3).compareTo("4.1"))  // if javafbp jar file version not less than 4.0.0
		    owner = "jpaulmorrison";
		
		try {

			Class<?> compdescCls = classLoader
					.loadClass("com." + owner + ".fbp." + seg + "ComponentDescription");
			Class<?> inportCls = classLoader
					.loadClass("com." + owner + ".fbp." + seg + "InPort");
			Class<?> outportCls = classLoader
					.loadClass("com." + owner + ".fbp." + seg + "OutPort");
			Class<?> inportsCls = classLoader
					.loadClass("com." + owner + ".fbp." + seg + "InPorts");
			Class<?> outportsCls = classLoader
					.loadClass("com." + owner + ".fbp." + seg + "OutPorts");

			Annotation[] annos = javaClass.getAnnotations();
			for (Annotation a : annos) {
				if (compdescCls.isInstance(a)) {
					Method meth = compdescCls.getMethod("value");
					compDescr = (String) meth.invoke(a);
				}
				if (inportCls.isInstance(a)) {
					getInPortAnnotation(a, inportCls);
				}
				if (inportsCls.isInstance(a)) {
					Method meth = inportsCls.getMethod("value");
					Object[] oa = (Object[]) meth.invoke(a);
					for (Object o : oa) {
						getInPortAnnotation((Annotation) o, inportCls);
					}
				}
				if (outportCls.isInstance(a)) {
					getOutPortAnnotation(a, outportCls);
				}
				if (outportsCls.isInstance(a)) {
					Method meth = outportsCls.getMethod("value");
					Object[] oa = (Object[]) meth.invoke(a);
					for (Object o : oa) {
						getOutPortAnnotation((Annotation) o, outportCls);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void getInPortAnnotation(Annotation a, Class<?> inport) {
		AInPort ipt = new AInPort();
		try {
			Method meth = inport.getMethod("value");
			ipt.value = (String) meth.invoke(a);

			meth = inport.getMethod("arrayPort");
			Boolean b = (Boolean) meth.invoke(a);
			ipt.arrayPort = b.booleanValue();

			meth = inport.getMethod("fixedSize");
			b = (Boolean) meth.invoke(a);
			ipt.fixedSize = b.booleanValue();
			
			meth = inport.getMethod("optional");
			b = (Boolean) meth.invoke(a);
			ipt.optional = b.booleanValue();

			meth = inport.getMethod("description");
			ipt.description = (String) meth.invoke(a);

			meth = inport.getMethod("type");
			ipt.type = (Class<?>) meth.invoke(a);

			meth = inport.getMethod("setDimension");
			Integer ic = (Integer) meth.invoke(a);
			int i = ic.intValue();

			meth = inport.getMethod("valueList");
			String[] sa = (String[]) meth.invoke(a);
			for (String s : sa) {
				if (s.toLowerCase().endsWith("*")) {
					for (int j = 0; j < i; j++) {
						AInPort ipt2 = new AInPort();
						ipt2.value = s.substring(0, s.length() - 1) + j;
						ipt2.arrayPort = ipt2.arrayPort;
						ipt2.fixedSize = ipt.fixedSize;
						ipt2.description = ipt.description;
						ipt2.optional = ipt.optional;
						ipt2.type = ipt.type;
						inputPortAttrs.put(ipt2.value, ipt2);
					}
				} else {
					AInPort ipt2 = new AInPort();
					ipt2.value = s;
					ipt2.arrayPort = ipt2.arrayPort;
					ipt2.fixedSize = ipt.fixedSize;
					ipt2.description = ipt.description;
					ipt2.optional = ipt.optional;
					ipt2.type = ipt.type;
					inputPortAttrs.put(ipt2.value, ipt2);
					}
			}
			if (sa.length == 0)
				inputPortAttrs.put(ipt.value, ipt);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	AOutPort getOutPortAnnotation(Annotation a, Class<?> outport) {
		AOutPort opt = new AOutPort();
		try {
			Method meth = outport.getMethod("value");
			opt.value = (String) meth.invoke(a);

			meth = outport.getMethod("arrayPort");
			Boolean b = (Boolean) meth.invoke(a);
			opt.arrayPort = b.booleanValue();

			meth = outport.getMethod("fixedSize");
			b = (Boolean) meth.invoke(a);
			opt.fixedSize = b.booleanValue();

			meth = outport.getMethod("optional");
			b = (Boolean) meth.invoke(a);
			opt.optional = b.booleanValue();
			
			meth = outport.getMethod("description");
			opt.description = (String) meth.invoke(a);

			meth = outport.getMethod("type");
			opt.type = (Class<?>) meth.invoke(a);

			meth = outport.getMethod("setDimension");
			Integer ic = (Integer) meth.invoke(a);
			int i = ic.intValue();

			meth = outport.getMethod("valueList");
			String[] sa = (String[]) meth.invoke(a);
			for (String s : sa) {
				if (s.toLowerCase().endsWith("*")) {
					for (int j = 0; j < i; j++) {
						AOutPort opt2 = new AOutPort();
						opt2.value = s.substring(0, s.length() - 1) + j;
						opt2.arrayPort = opt2.arrayPort;
						opt2.fixedSize = opt.fixedSize;
						opt2.optional = opt.optional;
						opt2.description = opt.description;
						opt2.type = opt.type;
						outputPortAttrs.put(opt2.value, opt2);
					}
				} else {
					AOutPort opt2 = new AOutPort();
					opt2.value = s;
					opt2.arrayPort = opt2.arrayPort;
					opt2.fixedSize = opt.fixedSize;
					opt2.optional = opt.optional;
					opt2.description = opt.description;
					opt2.type = opt.type;
					outputPortAttrs.put(opt2.value, opt2);
				}
			}
			if (sa.length == 0)
				outputPortAttrs.put(opt.value, opt);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return opt;
	}

	void displayPortInfo() {
		buildMetadata();   
		final JDialog jdialog = new JDialog(driver.frame);
		jdialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				jdialog.dispose();
			}
		});

		jdialog.setTitle("Description and Port Information");
		jdialog.toFront();
		JPanel panel = new JPanel(new GridBagLayout());

		panel.setBackground(Color.GRAY);
				
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		panel.setLayout(gbl);

		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.5;
		gbc.weighty = 0.5;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = ROWSIZE;
		String desc = description.replace('\n',  ' '); 
		JTextField tfd = new JTextField(" " + desc + " ");
		Font ft = tfd.getFont();
		ft = ft.deriveFont(Font.BOLD);
		tfd.setFont(ft);
		tfd.setForeground(Color.BLUE);
		tfd.setEditable(false);
		gbl.setConstraints(tfd, gbc);
		//tfd.setBackground(lg);
		panel.add(tfd);
		
		int i = fullClassName.indexOf("!");
		String s1 = fullClassName.substring(0, i); 
		String s2 = fullClassName.substring(i + 1); 
		gbc.gridy = 1;
		JTextField tf0 = new JTextField(" " + s1 + " ");
		tf0.setEditable(false);
		gbl.setConstraints(tf0, gbc);
		tf0.setBackground(lg);
		panel.add(tf0);
		
		gbc.gridy = 2;
		JTextField tf1 = new JTextField(" " + s2 + " ");
		tf1.setForeground(Color.BLUE);
		tf1.setEditable(false);
		gbl.setConstraints(tf1, gbc);
		tf1.setBackground(lg);
		panel.add(tf1);

		gbc.weightx = 1.5;
		gbc.weighty = 0.5;
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = ROWSIZE;
		if (compDescr == null || compDescr.equals("")) {
			compDescr = "(no description)";
		}
		JTextField tf2 = new JTextField(compDescr);
		tf2.setEditable(false);
		gbl.setConstraints(tf2, gbc);
		tf2.setBackground(lg);
		panel.add(tf2);

		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.5;

		gbc.gridwidth = 1;
		gbc.gridy = 4;

		JTextField[] tft = new JTextField[ROWSIZE];
		tft[0] = new JTextField(" Port ");
		tft[1] = new JTextField(" Type ");
		tft[2] = new JTextField(" Class ");
		tft[3] = new JTextField(" Function ");
		tft[4] = new JTextField(" Connected? ");
		displayRow(gbc, gbl, tft, panel, Color.BLUE);

		for (AInPort ip : inputPortAttrs.values()) {
			JTextField[] tfi = new JTextField[ROWSIZE];
			tfi[0] = new JTextField(ip.value);
			String s = "input";
			if (ip.arrayPort)
				s += ", array";
			if (ip.fixedSize)
				s += ", fixed size";
			if (ip.optional)
				s += ", optional";
			tfi[1] = new JTextField(s);
			if (ip.type == null)
				tfi[2] = new JTextField("");
			else
				tfi[2] = new JTextField(ip.type.getName());
			tfi[3] = new JTextField(ip.description);
			int res = testMatch(tfi[0].getText(), tfi[1].getText());
			String results[] = {"Yes", "Missing", "Optional"};
			tfi[4] = new JTextField(results[res]);
			gbc.gridx = 0;
			gbc.weightx = 0.5;
			displayRow(gbc, gbl, tfi, panel, Color.BLACK);
		}

		for (AOutPort op : outputPortAttrs.values()) {
			JTextField[] tfo = new JTextField[ROWSIZE];
			tfo[0] = new JTextField(op.value);
			String s = "output";
			if (op.arrayPort)
				s += ", array";
			if (op.fixedSize)
				s += ", fixed size";
			if (op.optional)
				s += ", optional";
			tfo[1] = new JTextField(s);
			if (op.type == null)
				tfo[2] = new JTextField("");
			else
				tfo[2] = new JTextField(op.type.getName());
			tfo[3] = new JTextField(op.description);
			int res = testMatch(tfo[0].getText(), tfo[1].getText());
			String results[] = {"Yes", "Missing", "Optional"};
			tfo[4] = new JTextField(results[res]);
			displayRow(gbc, gbl, tfo, panel, Color.BLACK);
		}

		LinkedList<String> lst = checkUnmatchedPorts();
		for (String ls : lst) {
			JTextField[] tfu = new JTextField[ROWSIZE];
			tfu[0] = new JTextField(ls.substring(1));
			if (ls.substring(0, 1).equals("I"))
				tfu[1] = new JTextField("(input)");
			else
				tfu[1] = new JTextField("(output)");
			tfu[2] = new JTextField("");
			tfu[3] = new JTextField("");
			tfu[4] = new JTextField("?");
			displayRow(gbc, gbl, tfu, panel, Color.BLACK);
		}

		jdialog.add(panel);
		//jdialog.pack();  
		
		 
		Point p = driver.frame.getLocation();
		Dimension dim = driver.frame.getSize();
		//int x_off = 100;
		//int y_off = 100;
		//jdialog.setPreferredSize(new Dimension(dim.width - x_off, dim.height - y_off));
		//jdialog.pack();
		//int height = 200 + inputPortAttrs.size() * 40 + outputPortAttrs.size() * 40;
		int width = (int)jdialog.getPreferredSize().getWidth();		
		
		int max_y = 0;
		for (Block b: driver.curDiag.blocks.values()) {
			max_y = Math.max(max_y, b.cy + b.height / 2 );
		}
		
		jdialog.pack();
		//jdialog.setLocation(p.x + dim.width / 2 - width / 2, p.y + dim.height - height - y_off);
		jdialog.setLocation(p.x + dim.width / 2 - width / 2, p.y + max_y + 50);
		
		
		panel.setVisible(true);
		jdialog.setVisible(true);
		jdialog.toFront();
		//jdialog.setPreferredSize(new Dimension(dim.width / 2, dim.height / 2));
		
		jdialog.validate();
		panel.repaint();
		jdialog.repaint();
		driver.frame.repaint();
	}

	void displayRow(GridBagConstraints gbc, GridBagLayout gbl, JTextField[] tf,
			JPanel panel, Color col) {
		gbc.gridx = 0;
		gbc.weightx = 0.5;
		for (int i = 0; i < ROWSIZE; i++) {
			if (i == ROWSIZE - 1)
				gbc.weightx = 0.75;
			gbl.setConstraints(tf[i], gbc);
			tf[i].setBackground(lg);
			tf[i].setForeground(col);
			gbc.gridx++;
			panel.add(tf[i]);
			tf[i].setEditable(false);
		}

		gbc.gridy++;
	}

	int testMatch(String port, String type) {
		// this logic is somewhat over-constrained!
		// this routine returns a value of 0, 1 or 2  (Yes, Missing or Optional)
		boolean input = (type.indexOf("in") > -1 || type.indexOf("param") > -1);
		boolean output = (type.indexOf("out") > -1);
		if (!input && !output) {
			MyOptionPane.showMessageDialog(driver.frame, "Port type of \""
					+ port + "\" must be \"in\" or \"out\"", MyOptionPane.ERROR_MESSAGE);
			return 1;
		}

		for (Arrow arrow : diag.arrows.values()) {
			//if (arrow.endsAtLine)
			//	continue;
			Arrow arr = arrow.findTerminalArrow(); 
			if (arr == null)
				return 2;
			if (id == arr.toId && arr.downStreamPort != null
					&& stem(arr.downStreamPort).equals(port))
				if (input)
					return 0;
				else
					return 1;

			if (id == arrow.fromId && arrow.upStreamPort != null
					&& stem(arrow.upStreamPort).equals(port))
				if (output)
					return 0;
				else
					return 1;
		}
		//if (input)
		//	return 3; // If input port missing, that's OK
		//else if (type.indexOf("optional") > -1)
		//	return 2;
		//else
		//	return 1; // If output port missing, error
		
		//if (input)
		//	return 3; // If input port missing, that's OK
		//else 
			if (type.indexOf("optional") > -1)
			return 2;
		else
			return 1; // If port missing, error
	}

	LinkedList<String> checkUnmatchedPorts() {
		LinkedList<String> lst = new LinkedList<String>();
		for (Arrow arrow : diag.arrows.values()) {
			if (arrow.endsAtLine)
				continue;
			boolean found = false;
			if (arrow.downStreamPort != null
					&& !arrow.downStreamPort.equals("*") && id == arrow.toId) {
				for (AInPort ip : inputPortAttrs.values()) {
					if (ip.value.equals(stem(arrow.downStreamPort))) {
						found = true;
						break;
					}
				}
				if (!found)
					lst.add("I" + arrow.downStreamPort);
			}

			if (arrow.upStreamPort != null && !arrow.upStreamPort.equals("*")
					&& id == arrow.fromId) {
				for (AOutPort op : outputPortAttrs.values()) {
					if (op.value.equals(stem(arrow.upStreamPort))) {
						found = true;
						break;
					}
				}
				if (!found)
					lst.add("O" + arrow.upStreamPort);
			}
		}
		return lst;
	}

	String stem(String s) {
		int i = s.indexOf("[");
		if (i == -1)
			return s;
		else
			return s.substring(0, i);
	}
/*
	// check arrows attached to this block
	// this is called from DiagramBuilder, but it causes tight loops, so I am not calling it right now
	void checkArrows() {
		for (Arrow a : diag.arrows.values()) {
			if (id == a.fromId)
				//driver.checkCompatibility(a);
			else {
				Arrow a2 = a.findTerminalArrow();
				if (id == a2.toId)
					//driver.checkCompatibility(a);
			}
		}
*/	

	void buildBlockPopupMenu() {
		diag.jpm = new JPopupMenu("            Block-related Actions");
		// driver.curPopup = jpm;
		diag.jpm.setVisible(true);
		JMenuItem menuItem = null;
		JLabel label2 = new JLabel();
		label2.setFont(driver.fontg);
		// label2.setHorizontalAlignment(SwingConstants.CENTER);
		label2.setText(diag.jpm.getLabel());
		// label2.setForeground(Color.BLUE);
		diag.jpm.add(label2);
		diag.jpm.addSeparator();
		//int j = driver.genLangs.length;
		//sMenu = new JMenuItem[j];
		//for (int i = 0; i < j; i++) {
		//	sMenu[i] = new JMenuItem(driver.genLangs[i].label);
		//}

		if (this instanceof Enclosure) {
			menuItem = new JMenuItem("Edit Subnet Label");
			menuItem.addActionListener(this);
			diag.jpm.add(menuItem);
			menuItem = new JMenuItem("Edit Subnet Port Name");
			menuItem.addActionListener(this);
			diag.jpm.add(menuItem);
			menuItem = new JMenuItem("Toggle Substream Sensitivity");
			menuItem.addActionListener(this);
			diag.jpm.add(menuItem);
			diag.jpm.addSeparator();
			menuItem = new JMenuItem("Excise Subnet");
			menuItem.addActionListener(this);
			diag.jpm.add(menuItem);
			diag.jpm.addSeparator();
			menuItem = new JMenuItem("Drag Contents");
			menuItem.addActionListener(this);
			diag.jpm.add(menuItem);

		} else {
			if (this instanceof ProcessBlock || this instanceof FileBlock
					|| this instanceof ReportBlock
					|| this instanceof LegendBlock) {
				if (this instanceof ProcessBlock)
					menuItem = new JMenuItem("Edit Process Name or Description");
				else
					menuItem = new JMenuItem("Edit Description");
				menuItem.addActionListener(this);
				diag.jpm.add(menuItem);
				if (this instanceof ProcessBlock) {

					menuItem = new JMenuItem("Toggle Subnet On/Off");
					menuItem.addActionListener(this);
					diag.jpm.add(menuItem);
					menuItem = new JMenuItem("Select Subnet Diagram (.drw)");
					menuItem.addActionListener(this);
					diag.jpm.add(menuItem);
					diag.jpm.addSeparator();

					if (isSubnet) {
						menuItem = new JMenuItem("Display Subnet");
						menuItem.addActionListener(this);
						diag.jpm.add(menuItem);
					}
					

					if (!isSubnet) {

						menuItem = new JMenuItem("Choose Source Code");
						menuItem.addActionListener(this);
						diag.jpm.add(menuItem);

						menuItem = new JMenuItem("Display Source Code");
						menuItem.addActionListener(this);
						diag.jpm.add(menuItem);
					}

					diag.jpm.addSeparator();
					menuItem = new JMenuItem("Toggle Multiplexing");
					menuItem.addActionListener(this);
					diag.jpm.add(menuItem);
					menuItem = new JMenuItem("Set Multiplexing Factor");
					menuItem.addActionListener(this);
					diag.jpm.add(menuItem);
					menuItem = new JMenuItem("Clear Multiplexing Factor");
					menuItem.addActionListener(this);
					diag.jpm.add(menuItem);

					
					diag.jpm.addSeparator();
					JMenuItem menuItem1 = new JMenuItem(
								"Choose Component/Subnet Class");
					menuItem1.addActionListener(this);
					diag.jpm.add(menuItem1);					
					JMenuItem menuItem2 = new JMenuItem("Display Full Class Name");
					menuItem2.addActionListener(this);
					diag.jpm.add(menuItem2);
					JMenuItem menuItem3 = new JMenuItem("Display Description and Port Info");
					menuItem3.addActionListener(this);
					diag.jpm.add(menuItem3);
						
					if (diag.diagLang == null) {							
						menuItem1.setEnabled(false);
					}
					if (diag.diagLang.label.equals("JSON")){
						menuItem1.setEnabled(false);
						menuItem2.setEnabled(false);
					}
					if (!(diag.diagLang.label.equals("Java"))){						
						menuItem3.setEnabled(false);
					}
						
					diag.jpm.addSeparator();
					menuItem = new JMenuItem(
							"Clear Associated Diagram and/or Class");
					menuItem.addActionListener(this);
					diag.jpm.add(menuItem);

				}
			} else {

				menuItem = new JMenuItem("Edit Item");
				menuItem.addActionListener(this);
				diag.jpm.add(menuItem);
				if (type.equals(Block.Types.EXTPORT_IN_BLOCK)
						|| type.equals(Block.Types.EXTPORT_OUT_BLOCK)) {
					diag.jpm.addSeparator();
					menuItem = new JMenuItem(
							"Toggle Substream Sensitive / Normal");
					menuItem.addActionListener(this);
					diag.jpm.add(menuItem);
				}
			}
			diag.jpm.addSeparator();
		}
		if (this instanceof Enclosure) {
			menuItem = new JMenuItem("Toggle Colour");
			diag.jpm.add(menuItem);
			menuItem.addActionListener(this);
		} else {
			menuItem = new JMenuItem("Toggle Visible/Invisible");
			diag.jpm.add(menuItem);
			menuItem.addActionListener(this);
		}
		diag.jpm.addSeparator();
		menuItem = new JMenuItem("Delete");
		diag.jpm.add(menuItem);
		menuItem.addActionListener(this);
		// menuItem = new JMenuItem("Exit");
		// jpm.add(menuItem);
		// menuItem.addActionListener(this);
		// diag.curMenuRect = new Rectangle(p.x, p.y, d.width, d.height);
		// return jpm;
		
	}

	public void actionPerformed(ActionEvent e) {
		String s = e.getActionCommand();
		diag.jpm = null;

		if (s.equals("Edit Process Name or Description") || s.equals("Edit Description")) {

			editDescription(DrawFBP.MODIFY);
			return;
		}

		if (s.equals("Edit Item")) { // one-line descriptions

			String ans = (String) MyOptionPane.showInputDialog(driver.frame,
					"Enter or change text", "Edit Item",
					MyOptionPane.PLAIN_MESSAGE, null, null, description);
			if (ans != null/* && ans.length() > 0*/) {
				description = ans.trim();
				diag.changed = true;
				if (this instanceof IIPBlock) {
					IIPBlock ib = (IIPBlock) this;
					description = ib.checkNestedChars(description);					
					//height = driver.gFontHeight + 6;
					FontMetrics metrics = driver.osg.getFontMetrics(driver.fontf);			
					String t = description;
					if (t.length() == 1)
						t = " " + t + " ";
					byte[] str = t.getBytes();
					width = metrics.bytesWidth(str, 0, t.length());
					

					for (Arrow arrow : diag.arrows.values()) {
						if (arrow.fromId == id && arrow.fromY == arrow.toY) // i.e.
							// horizontal
							arrow.fromX = cx + width / 2;
					}
					calcEdges();
				}
			}
			driver.frame.update(driver.osg);
			return;
		}
		
		if (s.startsWith("Toggle Subnet On/Off"))
			isSubnet = !isSubnet;

		if (s.startsWith("Select Subnet Diagram")) {			
			linkToSubnetDiagram();
			if (isSubnet)
				codeFileName = null;
			return;
		}

		//if (!isSubnet) {
			//for (int j = 0; j < sMenu.length; j++) {
			//	if (e.getSource() == sMenu[j]) {
			//		selectSourceCode(driver.genLangs[j]);

			//		driver.jfl.setText("General font: " + driver.generalFont
			//				+ "; fixed: " + driver.fixedFont);
			//		return;
			//	}
			//}
			
		//}

		if (s.equals("Choose Source Code")) {
			selectSourceCode();
			return;
		}

		if (s.equals("Display Source Code")) {

			if (isSubnet)
				return;
			if (codeFileName == null) {
				MyOptionPane.showMessageDialog(driver.frame,
						"No code associated with block", MyOptionPane.WARNING_MESSAGE);
				return;
			}

			showCode();
			return;
		}

		if (s.equals("Choose Component/Subnet Class")) {
			
			if (codeFileName != null) {
				if (!(codeFileName.toLowerCase().endsWith(".java"))) {
					if (MyOptionPane.YES_OPTION != MyOptionPane
							.showConfirmDialog(
									driver.frame,
									"Non-Java source is associated with block - if you choose a Java class, you will lose this - go ahead?",
									"Previous non-Java source",
									MyOptionPane.YES_NO_OPTION)) {
						return;
					}
					codeFileName = null;
				}
			}
			try {
				selectJavaClass();
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			 
			
		}

		if (s.equals("Display Subnet")) {

			if (diagramFileName == null) {
				MyOptionPane.showMessageDialog(driver.frame,
						"Subnet not selected", MyOptionPane.ERROR_MESSAGE);
			} else {
				String t = diagramFileName;
				File file = null;
				// if (t.startsWith("/") || t.substring(1, 2).equals(":"))
				file = new File(t);
				// else
				// file = new File(DrawFBP.makeAbsFileName(t,
				// diag.file.getAbsolutePath()));
				// File f = new File(diagramFileName);
				driver.openAction(file.getAbsolutePath());
			}
			return;

		}
		if (s.equals("Clear Associated Diagram and/or Class")) {

			codeFileName = null;
			diagramFileName = null;
			javaClass = null;
			fullClassName = null;
			diag.changed = true;
			driver.frame.repaint();
			inputPortAttrs = null;
			outputPortAttrs = null;
			isSubnet = false;
			// diag.changeCompLang();
			return;

		}

		if (s.equals("Display Full Class Name")) {

			if (javaClass == null && fullClassName == null) {
				MyOptionPane.showMessageDialog(driver.frame,
						"No component code assigned", MyOptionPane.ERROR_MESSAGE);
				return;
			}

			if ((javaClass == null) != (fullClassName == null)) {
				MyOptionPane.showMessageDialog(driver.frame,
						"One of class name and full class name is null, but the other isn't:\n"
						+ "class name - " + javaClass + "\n"
						+ "full class name - " + fullClassName, MyOptionPane.ERROR_MESSAGE);
				return;
			}

			if (javaClass != null || fullClassName != null) {
				MyOptionPane.showMessageDialog(driver.frame, fullClassName);
			}
			return;
		}
		if (s.equals("Toggle Multiplexing")) {
			multiplex = !multiplex;
			if (!multiplex)
				mpxfactor = null;
			driver.frame.repaint();
			diag.changed = true;
			return;

		}
		if (s.equals("Set Multiplexing Factor")) {
			String ans = (String) MyOptionPane.showInputDialog(driver.frame,
					"Enter or change text", "Set Multiplexing Factor",
					MyOptionPane.PLAIN_MESSAGE, null, null, mpxfactor);
			if ((ans != null) && (ans.length() > 0)) {
				mpxfactor = ans;
				multiplex = true;
			}
			diag.changed = true;
			return;
		}
		if (s.equals("Clear Multiplexing Factor")) {
			mpxfactor = null;
			diag.changed = true;
			return;

		}
		if (s.equals("Display Description and Port Info")) {
			if (javaClass == null) {
				MyOptionPane.showMessageDialog(driver.frame,
						"No class information associated with block", MyOptionPane.ERROR_MESSAGE);
				return;
			}

			displayPortInfo();
			return;

		}
		if (this instanceof ExtPortBlock
				&& s.equals("Toggle Substream Sensitive / Normal")) {
			ExtPortBlock eb = (ExtPortBlock) this;
			eb.substreamSensitive = !eb.substreamSensitive;
			driver.frame.repaint();
			diag.changed = true;
			return;

		}
		if (s.equals("Edit Subnet Label")) {
			// Block must be an Enclosure
			diag.cEncl = (Enclosure) this;
			String ans = (String) MyOptionPane.showInputDialog(driver.frame,
					"Enter or change text", "Edit subnet label",
					MyOptionPane.PLAIN_MESSAGE, null, null,
					diag.cEncl.description);
			if (ans != null/* && ans.length() > 0*/) {
				diag.cEncl.description = ans;
			}
			driver.frame.repaint();
			diag.changed = true;
			return;

		}
		if (s.equals("Edit Subnet Port Name")) {
			// Block must be an Enclosure
			// this sets switch, which next click on arrow will test
			diag.cEncl = (Enclosure) this;
			diag.cEncl.editPortName = true;
			MyOptionPane.showMessageDialog(driver.frame,
					"Select arrow crossing left or right side");
			diag.findArrowCrossing = true;
			driver.frame.repaint();
			diag.changed = true;
			return;

		}
		if (s.equals("Toggle Substream Sensitivity")) {
			// Block must be an Enclosure
			// this sets switch, which next click on arrow will test
			diag.cEncl = (Enclosure) this;
			diag.cEncl.changeSubstreamSensitivity = !diag.cEncl.changeSubstreamSensitivity;
			MyOptionPane.showMessageDialog(driver.frame,
					"Select arrow crossing left or right side");
			diag.findArrowCrossing = true;
			driver.frame.repaint();
			diag.changed = true;
			return;

		}

		if (s.startsWith("Drag Contents")) {
			Enclosure enc = (Enclosure) this;
			enc.draggingContents = true;
			diag.findEnclosedBlocksAndArrows(enc);
			driver.frame.repaint();
			diag.changed = true;
			return;
		}

		if (s.equals("Excise Subnet")) {
			// Block must be an Enclosure
			
			Diagram diag = driver.getNewDiag();
			//diag.title = ans;
			driver.jtp.setSelectedIndex(diag.tabNum);
			String ans = (String) MyOptionPane.showInputDialog(driver.frame,
					"Enter or change text",
					"Enter subnet diagram relative file name",
					MyOptionPane.PLAIN_MESSAGE, null, null, null);
			if (ans != null/* && ans.length() > 0*/) {
				ans = ans.trim();
				if (!(ans.toLowerCase().endsWith(".drw")))
					ans += ".drw";
			}
			else
				return;
			
			diag.desc= ans;
			diag.title = ans;
			
			//--------------------
			diag.excise((Enclosure) this, ans);
			//--------------------

			final boolean NOCHOOSE = false;
			driver.curDiag.delBlock(this, NOCHOOSE);
			driver.curDiag.foundBlock = null;
			driver.frame.repaint();
			driver.curDiag.changed = true;
			return;

		}
		if (s.equals("Toggle Visible/Invisible")) {
			visible = !visible;
			diag.changed = true;
			driver.frame.repaint();
			return;
		}
		if (s.equals("Toggle Colour")) {
			Enclosure enc = (Enclosure) this;
			enc.coloured = !enc.coloured;
			diag.changed = true;
			driver.frame.repaint();
			return;
		}
		
		if (s.equals("Delete")) {
			final boolean CHOOSE = true;
			diag.delBlock(this, CHOOSE);
			diag.foundBlock = null;
			diag.changed = true;
			// diag.changeCompLang();
			driver.frame.repaint();
		}
		// if (s.equals("Exit")) {
		// diag.foundBlock = null;
		// driver.frame.repaint();
		// }

	}
	
	// Used if not single line block contents

	boolean editDescription(int option) {

		final JTextArea area = new JTextArea(4, 3);
		JScrollPane pane = new JScrollPane(area);
		
		area.setText(description);
		area.setFont(driver.fontg);
		//JScrollPane pane = new JScrollPane(area);
		 
		
		// ensure area within frame gets focus
		area.addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent he) {
				if ((he.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
					//if (area.isShowing()) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								area.requestFocusInWindow();
							}
						});
					//}
				}
			}
		});
		

		String t = "";
		for (int i = 0; i < driver.blockTypes.length; i++) {
			if (driver.blockTypes[i].equals(type)) {
				t = driver.blockNames[i];
				break;
			}
		}
		String init = (option < DrawFBP.MODIFY) ? "Create " : "Modify ";		
		

		int result = MyOptionPane.showOptionDialog(driver.frame, new Object[]{ 
				"Enter/change name or description", pane}, init + t);		
		
		//if (option > DrawFBP.EDIT_NO_CANCEL) {
			if (result != MyOptionPane.OK_OPTION)
				return false;
		//}

		description = area.getText();

		diag.changed = true;

		driver.frame.repaint();

		return true;
	}

	void linkToSubnetDiagram() {
		if (diagramFileName != null) {
			if (MyOptionPane.YES_OPTION != MyOptionPane.showConfirmDialog(
					driver.frame, "Block already associated with diagram ("
							+ diagramFileName + ") - change it?",
					"Change diagram", MyOptionPane.YES_NO_OPTION)) {
				return;
			}
			diagramFileName = null;
		}
		String t = driver.properties.get("currentDiagramDir");
		if (t == null)
			t = System.getProperty("user.home");

		MyFileChooser fc = new MyFileChooser(new File(t),
				diag.fCPArr[DrawFBP.DIAGRAM]);

		int returnVal = fc.showOpenDialog();

		File cFile = null;
		if (returnVal == MyFileChooser.APPROVE_OPTION) {
			cFile = new File(driver.getSelFile(fc));
			if (cFile == null || !(cFile.exists())) {
				MyOptionPane.showMessageDialog(driver.frame,
						"Unable to read file " + cFile.getName(), MyOptionPane.ERROR_MESSAGE);
			}

			File currentDiagramDir = cFile.getParentFile();
			driver.properties.put("currentDiagramDir",
					currentDiagramDir.getAbsolutePath());
			driver.propertiesChanged = true;
		}
		if (cFile != null) {
			diagramFileName = cFile.getAbsolutePath();
			diag.changed = true;
			isSubnet = true;
		}

		driver.frame.repaint();
		return;
	}
	void selectJavaClass() throws MalformedURLException {

		String oldFullClassName = fullClassName;

		if (javaClass != null) {
			if (MyOptionPane.YES_OPTION != MyOptionPane.showConfirmDialog(
					driver.frame,
					"Block already associated with class ("
							+ javaClass.getName() + ") - change it?",
					"Change class", MyOptionPane.YES_NO_OPTION)) {
				return;
			}
			// javaClass = null;
			// fullClassName = null;
		}

		if (!driver.getJavaFBPJarFile()) {
			MyOptionPane.showMessageDialog(driver.frame,
					"JavaFBP jar file not specified or found", MyOptionPane.ERROR_MESSAGE);
			return;
		}

		String t = driver.properties.get("currentClassDir");
		if (t == null)
			t = System.getProperty("user.home");

		MyFileChooser fc = new MyFileChooser(new File(t),
				diag.fCPArr[DrawFBP.CLASS]);

		int returnVal = fc.showOpenDialog();

		boolean injar = true;
		File cFile = null;
		if (returnVal == MyFileChooser.APPROVE_OPTION) {
			String res = driver.getSelFile(fc);

			int i = res.indexOf("!");
			if (i > -1) { // looking in a jar file (?)
				String res2 = res.substring(i + 2); // ! will be followed by
													// slash
				res2 = res2.replace('/', '.');

				File f = new File(res.substring(0, i));

				res = res.substring(0, i) + "!" + res2;

				URL[] urls = buildUrls(f);

				if (urls == null)
					javaClass = null;
				else {
					// Create a new class loader with the directory
					classLoader = new URLClassLoader(urls);
					try {
						javaClass = classLoader.loadClass(res2);
					} catch (ClassNotFoundException e2) {
						javaClass = null;
					} catch (NoClassDefFoundError e2) {
						javaClass = null;
					}
					javaClass = getSelectedClass(res.substring(0, i), res2,
							injar);
					fullClassName = res;
				}
			} else {
				// we are looking in local class hierarchy (not a jar file)
				String fs = res;

				if (fs.endsWith("jar"))
					cFile = new File(driver.javaFBPJarFile); // ????
				else {
					cFile = new File(fs);
					if (cFile == null || !(cFile.exists())) {
						MyOptionPane.showMessageDialog(driver.frame,
								"Unable to find file " + cFile.getName(), MyOptionPane.ERROR_MESSAGE);
						return;
					}
				}

				boolean classFound;
				File fp = null;

				String u = cFile.getName();
				i = u.lastIndexOf(".class");
				if (i > -1)
					u = u.substring(0, i);

				String error = "";

				while (true) {

					fp = cFile.getParentFile();
					if (fp == null)
						break;
					//try {
						classFound = true;

						URL[] urls = buildUrls(fp);

						if (urls == null)
							javaClass = null;
						else {

							// Create a new class loader with the directory
							classLoader = new URLClassLoader(urls);

							try {
								javaClass = classLoader.loadClass(u);
							} catch (ClassNotFoundException e2) {
								classFound = false;
								error = "ClassNotFoundException";
							} catch (NoClassDefFoundError e2) {
								classFound = false;
								error = "NoClassDefFoundError";
							}

							if (classFound)
								break;
							String v = fp.getName();
							u = v + "." + u;
							cFile = fp;
						}
					//} finally {

					//}
				}
				
				if (javaClass == null) {
					MyOptionPane.showMessageDialog(driver.frame,
							"Class '" + driver.getSelFile(fc) + "' not found ("
									+ error + ")", MyOptionPane.ERROR_MESSAGE);
				}

				else {

					driver.properties.put("currentClassDir",
							fp.getAbsolutePath());
					driver.propertiesChanged = true;
					fullClassName = fp.getAbsolutePath() + "!"
							+ javaClass.getName();

					javaClass = getSelectedClass(fp.getAbsolutePath(),
							javaClass.getName(), !injar);
				}
			}
		}

		if (javaClass == null) {
			MyOptionPane.showMessageDialog(driver.frame, "No class selected", MyOptionPane.ERROR_MESSAGE);
		} else {
			if (!fullClassName.equals(oldFullClassName))
				displayPortInfo();
			diag.changed = true;
			// diag.changeCompLang();
		}

		return;
	}

	URL[] buildUrls(File f) {
		LinkedList<URL> ll = new LinkedList<URL>();
		URL[] urls = null;
		try {
			ll.add(f.toURI().toURL());

			String curClsDir = driver.properties.get("currentClassDir")
					+ File.separator;

			if (null != curClsDir) {
				f = new File(curClsDir);
				ll.add(f.toURI().toURL());
			}

			
			f = new File(driver.javaFBPJarFile);
			ll.add(f.toURI().toURL());

			for (String jfv : driver.jarFiles.values()) {
				f = new File(jfv);
				ll.add(f.toURI().toURL());
			}
			urls = ll.toArray(new URL[ll.size()]);
		} catch (MalformedURLException e) {
			MyOptionPane.showMessageDialog(driver.frame,
					"Malformed URL: " + fullClassName, MyOptionPane.ERROR_MESSAGE);
			// e.printStackTrace();
			// javaClass = null;
			urls = null;
			e.printStackTrace();
		}

		return urls;
	}

	void selectSourceCode() {

		GenLang gl = driver.curDiag.diagLang;
		if (codeFileName != null) {
			if (MyOptionPane.YES_OPTION != MyOptionPane.showConfirmDialog(driver.frame,
					"Block already associated with source code (" + codeFileName
							+ ") - change it?",
					"Change source code", MyOptionPane.YES_NO_OPTION))
				return;
			}
		 

		if (!(gl.label.equals("Java")) && javaClass != null) {
			if (MyOptionPane.NO_OPTION == MyOptionPane.showConfirmDialog(
					driver.frame,
					"You have selected a non-Java language and there is a Java class associated with this block - go ahead?",
					"Java previously used", MyOptionPane.YES_NO_OPTION)) {

				javaClass = null;
				codeFileName = null;
				return;
			}
		}

		String ans = (String) MyOptionPane.showInputDialog(driver.frame,
				"Edit arbitrary string or browse (enter # to browse)",
				"Enter/change source code name", MyOptionPane.PLAIN_MESSAGE,
				null, null, codeFileName);

		if (ans != null/* && ans.length() > 0*/) {
			codeFileName = ans.trim();
			// javaClass = null;
			diag.changed = true;
		}

		if (codeFileName.equals("#")) {

			String t = driver.properties.get(gl.srcDirProp);
			if (t == null)
				t = System.getProperty("user.home");

			MyFileChooser fc = new MyFileChooser(new File(t),
					diag.fCPArr[DrawFBP.PROCESS]);

			int returnVal = fc.showOpenDialog();

			File cFile = null;
			if (returnVal == MyFileChooser.APPROVE_OPTION) {

				cFile = new File(driver.getSelFile(fc));
				if (cFile == null || !(cFile.exists())) {
					if (MyOptionPane.NO_OPTION == MyOptionPane.showConfirmDialog(
							driver.frame,
							"You have entered a file name that does not exist - go ahead?",
							"File does not exist", MyOptionPane.YES_NO_OPTION)) {
						return;
					}
					codeFileName = driver.getSelFile(fc);
				} else {
					codeFileName = cFile.getAbsolutePath();
					driver.properties.put(gl.srcDirProp,
							cFile.getParentFile().getAbsolutePath());
					driver.propertiesChanged = true;
				}

			}
		}

	}

	void showCode() {

		if (codeFileName == null) {
			MyOptionPane.showMessageDialog(driver.frame,
					"No code associated with block", MyOptionPane.ERROR_MESSAGE);
			return;
		}
		String t = codeFileName;

		// int k = t.lastIndexOf(".");
		// String suff = t.substring(k + 1);

		// diag.changeCompLang();
		CodeManager cm = new CodeManager(diag);
		cm.display(new File(t), diag.diagLang);

	}
}
