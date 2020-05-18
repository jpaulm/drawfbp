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

	String desc;
	//String descMod;  // modified a lot for .fbp notation; slightly, for other notations (" -> _)

	String subnetFileName; // name of subnet diagram file 

	String fullClassName; // (file name plus class name) or NoFlo name (now
							// shifted to codeFileName)

	int cx, cy; // coords of centre

	int id;
	
	//int tlx, tly;
	int textX, textY;

	static final int BLOCKWIDTH = 92; 

	static final int BLOCKHEIGHT = 64; 

	boolean multiplex = false;
	boolean deleteOnSave = false;

	Diagram diag;

	//LegendBlock lblk;
	String mpxfactor = null;
	HashMap<String, AInPort> inputPortAttrs;
	HashMap<String, AOutPort> outputPortAttrs;
	//int scan_pos = 0;
	Block vNeighbour, hNeighbour; // block at same position, vert. or horiz.

	boolean visible = true;	
	boolean isSubnet;

	/* next three fields are not stored in .drw files */	
	
	URLClassLoader myURLClassLoader = null;
	Class<?> javaComp; // selected Java class for block (fullClassName is equivalent in String format)
	String compDescr;  // used for annotations only
	
	//JMenuItem[] sMenu;
	
	
	int ROWSIZE = 5;
	String codeFileName;
	
	HashMap<String, Integer> portlist;
	JDialog portInfo = null;
	Point ptInfoLoc = null;
	
	//boolean added = false;
	//boolean ghost = false;
	String compareFlag = null;
	Rectangle leftRect = null;
	Rectangle topRect = null;
	Rectangle botRect = null;
	Rectangle rightRect = null;

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
		//static String UP = "Z";
	}

	Block(Diagram d) {

		diag = d;   // Diagram containing block
		driver = d.driver;

		type = Block.Types.PROCESS_BLOCK;

		subnetFileName = null;
		fullClassName = null;
		d.maxBlockNo ++;
		id = d.maxBlockNo;
		//buildSides();

	}

	void buildSides(){		
		
		leftRect = new Rectangle(cx - width / 2 - driver.zWS / 2, cy - height / 2 - driver.zWS / 2, 
				driver.zWS, height + driver.zWS);
		topRect = new Rectangle(cx - width / 2 - driver.zWS / 2, cy - height / 2 - driver.zWS / 2, 
				width + driver.zWS, driver.zWS);		
		rightRect = new Rectangle(cx + width / 2 - driver.zWS / 2, cy - height / 2 - driver.zWS / 2, 
				driver.zWS, height + driver.zWS);
		if (!(this instanceof ReportBlock))
			botRect = new Rectangle(cx - width / 2 - driver.zWS / 2, cy + height / 2  - driver.zWS / 2, 
					width + driver.zWS, driver.zWS );
	}
	
	void draw(Graphics g) {

		if (diag == null) // fudge
			return;

		if (!visible && this != driver.selBlock) {			

			//showArrowEndAreas(g);
			showDetectionAreas(g);

			return;
		}

		//if (this == driver.selBlock && !(this instanceof ProcessBlock)) {
		//	showArrowEndAreas(g);
		//	return;
		//}

		int tlx = cx - width / 2; // top left corner
		int tly = cy - height / 2;
		g.setFont(driver.fontg);

		calcDiagMaxAndMin(tlx - 20, cx + width / 2 + 20, cy - height / 2,
				cy + height / 2 + 40);

		g.setColor(Color.BLACK);
		g.drawRoundRect(tlx, tly, width, height, 6, 6);

		if (compareFlag != null && compareFlag.equals("D"))
			g.setColor(DrawFBP.lg);
		else if (this == driver.selBlock)
			g.setColor(DrawFBP.ly); // light yellow
		else
			g.setColor(DrawFBP.lb); // light turquoise

		g.fillRoundRect(tlx + 1, tly + 1, width - 2, height - 2, 6, 6);

		showCompareFlag(g, tlx, tly);
		
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
				g.setColor(DrawFBP.ly); // light yellow
			else
				g.setColor(DrawFBP.lb); // light turquoise
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

		

		if (desc != null) {
			String str[] = centreDesc();
			int x = textX;
			int y = textY;
			for (int i = 0; i < str.length; i++) {
				g.drawString(str[i], x, y); 
				y += driver.gFontHeight;
			}
		}

		showDetectionAreas(g);
		


		if (!visible && this == driver.selBlock)
			g.drawLine(tlx, tly, cx + width / 2, cy + height / 2);

		int y = cy + height / 2 + driver.gFontHeight + driver.gFontHeight / 2;

		if (subnetFileName != null) {
			Font fontsave = g.getFont();
			g.setFont(driver.fontf);
			g.setColor(Color.GREEN);
			File gFile = new File(subnetFileName);
			String name = gFile.getName();
			int x = cx - name.length() * driver.gFontWidth / 2;
			g.drawString(name, x, y);
			g.setFont(fontsave);
			y += driver.gFontHeight;
			calcDiagMaxAndMin(x - 20,
					x + name.length() * driver.gFontWidth + 20, cy - height / 2,
					y + 40);
		}

		String name = null;

		if (diag.diagLang != null && (diag.diagLang.label.equals("Java")
				|| diag.diagLang.label.equals("C#"))) {

			if (javaComp != null) {
				// driver.locateJavaFBPJarFile(false);
				Font fontsave = g.getFont();
				g.setFont(driver.fontf);
				g.setColor(Color.BLUE);
				name = javaComp.getSimpleName();
				if (!(name.endsWith(".class")))
					name = name += ".class";
				int x = cx - name.length() * driver.gFontWidth / 2;
				g.drawString(name, x, y);
				g.setFont(fontsave);
				y += driver.gFontHeight;
				calcDiagMaxAndMin(x - 20,
						x + name.length() * driver.gFontWidth + 20,
						cy - height / 2, y + 40);
			}

			else if (fullClassName != null) {
				// driver.locateJavaFBPJarFile(false);
				Font fontsave = g.getFont();
				g.setFont(driver.fontf);
				//if (compareFlag == null || !compareFlag.equals("D"))
				name = "Class not found or out of date - rechoose comp/subnet";
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
				calcDiagMaxAndMin(x - 20,
						x + name.length() * driver.gFontWidth + 20,
						cy - height / 2, y + 40);
			}
		}
		if (codeFileName != null) {
			Font fontsave = g.getFont();
			g.setFont(driver.fontf);
			name = codeFileName;
			int i = name.lastIndexOf("/");
			if (i == -1)
				i = name.lastIndexOf("/");
			name = name.substring(i + 1);
			g.setColor(Color.BLACK);
			int x = cx - name.length() * driver.gFontWidth / 2;
			g.drawString(name, x, y);
			g.setFont(fontsave);
			g.setColor(Color.BLACK);
			calcDiagMaxAndMin(x - 20,
					x + name.length() * driver.gFontWidth + 20, cy - height / 2,
					y + 40);
		}
		if (hNeighbour != null) {
			g.setColor(Color.ORANGE);
			if (hNeighbour.cx < cx)
				g.drawLine(hNeighbour.cx - hNeighbour.width / 2,
						hNeighbour.cy + hNeighbour.height / 2, cx + width / 2,
						hNeighbour.cy + hNeighbour.height / 2);
			else
				g.drawLine(hNeighbour.cx + hNeighbour.width / 2,
						hNeighbour.cy + hNeighbour.height / 2, cx - width / 2,
						hNeighbour.cy + hNeighbour.height / 2);
			g.setColor(Color.BLACK);
		}
		if (vNeighbour != null) {
			g.setColor(Color.ORANGE);
			if (vNeighbour.cy < cy)
				g.drawLine(vNeighbour.cx - vNeighbour.width / 2,
						vNeighbour.cy - vNeighbour.height / 2,
						vNeighbour.cx - vNeighbour.width / 2, cy + height / 2);
			else
				g.drawLine(vNeighbour.cx - vNeighbour.width / 2,
						vNeighbour.cy + vNeighbour.height / 2,
						vNeighbour.cx - vNeighbour.width / 2, cy - height / 2);
			g.setColor(Color.BLACK);
		}

		

		//blueCircs(g);
		
		 
	}
	
	
	
	public void adjEdgeRects() {
		leftRect.x = cx - width / 2 - driver.zWS / 2;
		leftRect.y = cy - height / 2 - driver.zWS / 2;
		leftRect.width = driver.zWS;
		leftRect.height = height + driver.zWS;
		rightRect.x = cx + width / 2 - driver.zWS / 2; 
		rightRect.y = cy - height / 2 - driver.zWS / 2;
		rightRect.width = driver.zWS;
		rightRect.height = height + driver.zWS;
		topRect.x = cx - width / 2 - driver.zWS / 2;
		topRect.y = cy - height / 2 - driver.zWS / 2;
		topRect.width = width + driver.zWS;
		topRect.height = driver.zWS;
		if (botRect != null) {
			botRect.x = cx - width / 2 - driver.zWS / 2;
			botRect.y = cy + height / 2 - driver.zWS / 2;
			botRect.width = width + driver.zWS;
			botRect.height = driver.zWS;
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
	String[] centreDesc() {

		//g.setColor(Color.BLACK);

		int x = 0;
		int y = 0;
		//int minX = Integer.MAX_VALUE;
		int maxX = 0;

		String str[] = desc.split("\n");
		boolean nonBlankLineFound = false;
		//Graphics g = driver.getGraphics();
		//FontMetrics metrics = g.getFontMetrics(g.getFont());
		//FontMetrics metrics = g.getFontMetrics(driver.fontg);
		FontMetrics metrics = driver.osg.getFontMetrics(driver.fontg);

		for (int i = 0; i < str.length; i++) {
			//x = 0;
			/*
			for (int j = 0; j < str[i].length(); j++) {
				char c = str[i].charAt(j);
				if (c != ' ')
					minX = Math.min(x, minX);

				x += metrics.charWidth(c);
				//System.out.println(x);
			}
			*/		
			String t = str[i];
			
			byte[] str2 = t.getBytes();
			x = 2 + metrics.bytesWidth(str2, 0, t.length());
			x += x / 6;     // fudge - add 16%
			
			maxX = Math.max(x, maxX);
			//System.out.println(maxX);
			if (!(str[i].trim().equals(""))) {
				// minY = Math.min(minY, y);
				y += driver.gFontHeight;
				nonBlankLineFound = true;
			}
			if (nonBlankLineFound) {
				// maxY = y;
			}
		}

		x = (maxX) / 2; // find half width
		x = cx - x;
		x += 4;                     //fudge

		y = y / 2; // find half height
		if (this instanceof ReportBlock)
			y = cy - y;
		else if (this instanceof PersonBlock)
			y = cy + height / 2 + driver.gFontHeight;
		else
			y = cy - y + driver.gFontHeight;

		y -= driver.gFontHeight / 3; // fudge!
		int saveY = y; 
		textX = x; 
		textY = y;
		

		for (int i = 0; i < str.length; i++) {
		//	g.drawString(str[i], x, y); 
			y += driver.gFontHeight;
		}
		
		if (this instanceof LegendBlock) {
			height = y - saveY + 24;
			width = maxX + 24;
			buildSides();
			calcEdges();
		}
		return str;
	}

	 

	//void showZones(Graphics g) {

		//if (!(this instanceof LegendBlock))  // comment out for next release
		//	return;
		//if (driver.currentArrow == null && driver.selBlockM == this)
		//	showArrowEndAreas(g);
		 
		//else if (driver.currentArrow != null && this == driver.foundBlock)
		//	showArrowEndAreas(g);

	//}

 
	void showCompareFlag(Graphics g, int tlx, int tly){
		if (compareFlag != null) {
			g.setColor(Color.BLACK);
			g.drawRoundRect(tlx + 1, tly + 1, 24, 24, 6, 6);
			g.setColor(DrawFBP.ly);
			g.fillRoundRect(tlx + 2, tly + 2, 22, 22, 6, 6);
			g.setColor(Color.BLACK);
			g.drawString(compareFlag, tlx + 10, tly + 17);
		}
	}
	
	String serialize() {
		String s = "<block> <x> " + cx + " </x> <y> " + cy + " </y> <id> " + id
				+ " </id> <type>" + type + "</type> ";
		s += "<width>" + width + "</width> <height>" + height + "</height> ";
		if (desc != null) {
			s += "<description>";
			for (int i = 0; i < desc.length(); i++) {
				if (desc.charAt(i) == '<'
						|| desc.charAt(i) == '>') {
					s += '\\'; // protect the angle bracket
				}
				s += desc.charAt(i);
			}
			s += "</description> ";
		}

		if (subnetFileName != null) {
			//String relDiagFileName = DrawFBP.makeRelFileName(diagramFileName,
			//		diag.diagFile.getAbsolutePath());
			s += "<diagramfilename>" + subnetFileName + "</diagramfilename> ";			
		}
		if (codeFileName != null) {
			//String relCodeFileName = DrawFBP.makeRelFileName(codeFileName,
			//		diag.diagFile.getAbsolutePath());
			s += "<codefilename>" + codeFileName + "</codefilename> ";
		}
		if (fullClassName != null) {
			String t = fullClassName;
			//if (t.toLowerCase().endsWith(".class"))  // change to keep .class
			//	t = t.substring(0, t.length() - 6);
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
			
			s += "<issubnet>" + (isSubnet?"true":"false") + "</issubnet> \n" ;
		if (mpxfactor != null)
			s += "<mpxfactor>" + mpxfactor + "</mpxfactor> \n";
		if (this instanceof Enclosure) {
			
			s += "\n";
			
		}
		s += "</block> \n";
		return s;

	}

	void buildBlockFromXML(HashMap<String, String> item) {
		// Build a block using a HashMap built using the XML description

		String s;
		type = item.get("type");
		desc = item.get("description");
		if (type.equals("I") && desc != null && desc.length() > 0 && desc.substring(0,1).equals("\""))
			desc = desc.substring(1,desc.length() - 2);				
		
		if (type == null)
			type = Block.Types.PROCESS_BLOCK;
		codeFileName = item.get("codefilename");
		//if (codeFileName != null) { 
		//	codeFileName = DrawFBP.makeAbsFileName(codeFileName,
		//			diag.diagFile.getAbsolutePath());
			// diag.changeCompLang();
		//}

		subnetFileName = item.get("diagramfilename");		

		if (subnetFileName != null)
			isSubnet = true;
		
		s = item.get("issubnet");
		if (s != null) 
			isSubnet = s.equals("true");

		fullClassName = item.get("blockclassname");
		String w = fullClassName; 
		if (w != null) {
			w = w.replace("\\",  File.separator);
			w = w.replace("/",  File.separator);
		}
		if (diag.diagLang.label.equals("Java") && w != null){
			if (!fullClassName.endsWith(".class"))  
				fullClassName += ".class";
			else
				w = w.substring(0, w.length() - 6);
			getClassInfo(w); 
		}
		
		javaComp = loadJavaClass(fullClassName);           
		
		s = item.get("x").trim();
		cx = Integer.parseInt(s);
		s = item.get("y").trim();
		cy = Integer.parseInt(s);
		s = item.get("id").trim();
		id = Integer.parseInt(s);		
		s = item.get("width");
		width = Integer.parseInt(s.trim());
		
		if (this instanceof IIPBlock) {
			IIPBlock iip = (IIPBlock) this;
			width = iip.calcIIPWidth(driver.osg);
			buildSides();
		} 
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
			ol.desc = item.get("description");
		}
		buildSides();
		calcEdges();

		diag.maxBlockNo = Math.max(id, diag.maxBlockNo);

		// driver.setSize(driver.maxX, driver.maxY);
		if (this instanceof ProcessBlock && javaComp != null) {
			buildMetadata();
		}

	}
	
	void getClassInfo(String fcn){  
		fullClassName = fcn;
		
		//fullClassName = item.get("blockclassname");
		if (fullClassName != null) {
			if (fullClassName.indexOf("!") == -1) {// if no "!", language is not
													// Java...
				//driver.tryFindJarFile = false;
				if (fullClassName.toLowerCase().endsWith(".json")) {
					codeFileName = fullClassName;
					fullClassName = null;
				}
				return;
			}
			//if (driver.tryFindJarFile) {
				if (!driver.locateJavaFBPJarFile(false)) {
					MyOptionPane.showMessageDialog(driver,
							"JavaFBP jar file not found - try Locate JavaFBP Jar File", MyOptionPane.ERROR_MESSAGE);
					return;
				} 
				javaComp = loadJavaClass(fullClassName); 
			//}
			// diag.compLang = driver.findGLFromLabel("Java");
		}
		}
	

	// takes fullClassName and derives javaClass
	
	Class<?> loadJavaClass(String name){
		Class<?> retClass = null;
		if (fullClassName == null || fullClassName.equals("")) 
			return null;
		int i = fullClassName.indexOf("!");
		if (i == -1)
			return null;
		String fn = fullClassName.substring(0, i);  // jar file name or class folder
		String cn = fullClassName.substring(i + 1);  // class name
		
		if (cn.endsWith(".class"))
			cn = cn.substring(0,  cn.length() - 6);
	
		//LinkedList<URL> ll = new LinkedList<URL>();
		
		if (!(fn.endsWith("jar")))
				fn += "/";
		
		File f = new File(fn);	
		fn = fn.replace("\\", "/");
		driver.locateJavaFBPJarFile(false);
		driver.javaFBPJarFile = driver.javaFBPJarFile.replace("\\",  "/");
		if (!(driver.jarFiles.containsValue(fn)) && 
				!(fn.equals(driver.javaFBPJarFile))
				&& fn.endsWith(".jar"))
			driver.jarFiles.put("x" + driver.jarFiles.size(), fn);
				
		URL[] urls = driver.buildUrls(f);
		
		if (urls == null)
			retClass = null;
		else 
				 
		try {
			
			myURLClassLoader = new URLClassLoader(urls, driver.getClass()
					.getClassLoader());
					
			retClass = myURLClassLoader.loadClass(cn);
			
			} catch (ClassNotFoundException e) {
				//System.out.println("Missing class name in " + fullClassName);
				MyOptionPane.showMessageDialog(driver,
						"Class name not found: " + fullClassName, MyOptionPane.ERROR_MESSAGE);
				// e.printStackTrace();
				retClass = null;
			} catch (NoClassDefFoundError e) {
				//System.out.println("Missing internal class name in "
				//		+ fullClassName);
				MyOptionPane.showMessageDialog(driver,
						"Internal class name not found: " + fullClassName, MyOptionPane.ERROR_MESSAGE);
				// e.printStackTrace();
				retClass = null;
			} 
		
		return retClass;
	}

	// check validity of class - returns null if not
	
	Class<?> isValidClass(String jar, String jf, boolean injar) {
		
		// returns actual class if valid; otherwise null

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
		if (0 <= fn.substring(j, j + 3).compareTo("4.1"))  // if javafbp jar file version greater than 4.0.0
		    owner = "jpaulmorrison";
		
		Class<?> cls;	
			
		try {
			Class<?> compClass = myURLClassLoader
					.loadClass("com." + owner + ".fbp." + seg + "Component");

			Class<?> networkClass = myURLClassLoader
					.loadClass("com." + owner + ".fbp." + seg + "Network");

			Class<?> subnetClass = myURLClassLoader
					.loadClass("com." + owner + ".fbp." + seg + "SubNet");	
			
			//int i = jf.lastIndexOf(".class"); 			
			//if (i != -1)
			//	jf = jf.substring(0, i);
			jf = jf.replace("\\",  "/");
			jf = jf.replace('/', '.');
			cls = myURLClassLoader.loadClass(jf);
			
			Class<?> cs = (Class<?>) cls.getSuperclass();
			Object obj = new Object();
			/*
			System.out.println("Classloader of class:"
			        + cls.getClassLoader());
			System.out.println("Name of superclass:"
			        + cs.toString());
			System.out.println("Classloader of superclass:"
			        + cs.getClassLoader());
			System.out.println("Classloader of comp class:"
			        + compClass.getClassLoader());
			System.out.println("Classloader of subnet class:"
			        + subnetClass.getClassLoader());
			System.out.println("Classloader of obj class:"
			        + obj.getClass().getClassLoader());
					
			*/
			
			if (cls.getCanonicalName().equals(compClass.getCanonicalName()) || 
					cls.getCanonicalName().equals(networkClass.getCanonicalName())
					|| cls.getCanonicalName().equals(subnetClass.getCanonicalName())){
					return null;
			}

			//MethodHandle h2 = MethodHandles.lookup().findSpecial(Object.class, "toString",
			//        MethodType.methodType(String.class),
			//        Test.class);
			
				 
			
			//URLClassLoader ucl = (URLClassLoader) cls.getClassLoader();
			//URLClassLoader ucl2 = new URLClassLoader(ucl.getURLs(), ucl);
			try {
			//cs = ucl.loadClass(cs.getName()); 
			cs = myURLClassLoader.loadClass(cs.getName()); 
			}  catch (ClassNotFoundException e) {
				
			}
			
			//
			
			if (cs != null && cs == obj.getClass()){
				MyOptionPane.showMessageDialog(driver,
						"Class superclass is Object", MyOptionPane.ERROR_MESSAGE);				
				return null;
			}
			if (cs == null)
				return null;
			//String superCls = cs.getCanonicalName();
			//if (!(superCls.equals(compClass.getCanonicalName())  ||
			//		superCls.equals(subnetClass.getCanonicalName()))) {
			if (cs != compClass && cs != subnetClass) {
				MyOptionPane.showMessageDialog(driver,
						"Class file not a valid FBP component or subnet", MyOptionPane.ERROR_MESSAGE);				
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
				MyOptionPane.showMessageDialog(driver,
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


	void showDetectionAreas(Graphics g) {
		if (this instanceof Enclosure)
			return;
		
		if (driver.edgePoint != null) 
			if (driver.edgePoint.block == this)		
				showArrowEndAreas(g);
		
		if (driver.fpArrowEndB != null && driver.fpArrowEndB.block == this)  
				showArrowEndAreas(g);
		
		if (driver.selBlockM == this)
				showArrowEndAreas(g);
		
		//if (driver.fpArrowRoot != null && driver.fpArrowRoot.block == this) 
		//	showArrowEndAreas(g);
		
		
		
		if (!(type.equals(Types.ENCL_BLOCK)))
		    driver.blueCircs(g);
		
		driver.repaint();
		
		 
	}
	 
	void showArrowEndAreas(Graphics g) {
		//if (visible) {
		Color col = g.getColor();
		g.setColor(DrawFBP.grey);   
		Graphics2D g2 = (Graphics2D) g;
		
		g2.fill(leftRect);
		g2.fill(topRect);
		g2.fill(rightRect);
		g2.fill(botRect);
		g.setColor(col);
		//}
	}
	  

	void buildMetadata() {
		inputPortAttrs = new HashMap<String, AInPort>();
		outputPortAttrs = new HashMap<String, AOutPort>();
		String s = driver.javaFBPJarFile; 
		if (s == null)
			return;
		int j = s.lastIndexOf("javafbp") + 8;
		String seg = "engine.";
		if (0 <= s.substring(j, j + 1).compareTo("4"))  // if javafbp jar file version not less than 4.0.0
		    seg = "core.engine.";
		String owner = "jpmorrsn";
		if (0 <= s.substring(j, j + 3).compareTo("4.1"))  // if javafbp jar file version not less than 4.0.0
		    owner = "jpaulmorrison";
		
		try {

			Class<?> compdescCls = myURLClassLoader
					.loadClass("com." + owner + ".fbp." + seg + "ComponentDescription");
			Class<?> inportCls = myURLClassLoader
					.loadClass("com." + owner + ".fbp." + seg + "InPort");
			Class<?> outportCls = myURLClassLoader
					.loadClass("com." + owner + ".fbp." + seg + "OutPort");
			Class<?> inportsCls = myURLClassLoader
					.loadClass("com." + owner + ".fbp." + seg + "InPorts");
			Class<?> outportsCls = myURLClassLoader
					.loadClass("com." + owner + ".fbp." + seg + "OutPorts");

			Annotation[] annos = javaComp.getAnnotations();
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
						ipt2.arrayPort = ipt.arrayPort;
						ipt2.fixedSize = ipt.fixedSize;
						ipt2.description = ipt.description;
						ipt2.optional = ipt.optional;
						ipt2.type = ipt.type;
						inputPortAttrs.put(ipt2.value, ipt2);
					}
				} else {
					AInPort ipt2 = new AInPort();
					ipt2.value = s;
					ipt2.arrayPort = ipt.arrayPort;
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
						opt2.arrayPort = opt.arrayPort;
						opt2.fixedSize = opt.fixedSize;
						opt2.optional = opt.optional;
						opt2.description = opt.description;
						opt2.type = opt.type;
						outputPortAttrs.put(opt2.value, opt2);
					}
				} else {
					AOutPort opt2 = new AOutPort();
					opt2.value = s;
					opt2.arrayPort = opt.arrayPort;
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
		if (fullClassName == null)
			return;
		if (portInfo != null) {
			ptInfoLoc = portInfo.getLocation();
			portInfo.dispose();
		}
		buildMetadata();   
		// final JDialog portInfo = new JDialog(driver);
		portInfo = new JDialog(driver);		
		
		portInfo.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				ptInfoLoc = portInfo.getLocation();
				portInfo.dispose();
				portInfo = null;
			}
		});
		
		 
		
		/* 
		portInfo.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				//super.mouseDragged(e);
				if (pILocn != null) {
					pILocn = portInfo.getLocation();
					portInfo.repaint();
				}
				
			}

			//@Override
			//public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub
				
			//}
			
		});
	 */
		//portInfo.addMouseMotionListener(portInfo);
		//portInfo.addWindowListener(portInfo);
		//portInfo.addMouseMotionListener(portInfo);
		
		portInfo.setTitle("Description and Port Information");
		portInfo.toFront();
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
		
		
		int i = fullClassName.indexOf("!");
		String s1 = fullClassName.substring(0, i); 
		String s2 = fullClassName.substring(i + 1); 
		gbc.gridy = 1;
		JTextField tf0 = new JTextField(" " + s1 + " ");
		tf0.setEditable(false);
		gbl.setConstraints(tf0, gbc);
		tf0.setBackground(DrawFBP.lg);
		panel.add(tf0);
		
		gbc.gridy = 2;
		JTextField tf1 = new JTextField(" " + s2 + " ");
		tf1.setForeground(Color.BLUE);
		tf1.setEditable(false);
		gbl.setConstraints(tf1, gbc);
		tf1.setBackground(DrawFBP.lg);
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
		tf2.setBackground(DrawFBP.lg);
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
			JTextField[] tfi = new JTextField[ROWSIZE];  //array of text fields
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
	
		portInfo.add(panel);
		//jdialog.pack();  
		
		 
		//Point p = driver.getLocationOnScreen();
		//Dimension dim = driver.getSize();
	
		// int width = (int)portInfo.getPreferredSize().getWidth();		
		
		//int max_y = 0;
		//for (Block b: driver.curDiag.blocks.values()) {
		//	max_y = Math.max(max_y, b.cy + b.height / 2 );
		//}
		
		portInfo.pack();		
		
		//int width = portInfo.getWidth();
		//int height = portInfo.getHeight();
		
		// portInfo.setLocation(p.x + dim.width / 2 - width / 2, p.y + max_y + 50);
		
		//portInfo.setLocation(p.x + cx + width / 2 + 50, p.y + cy + height / 2 + 50); 
		//portInfo.setLocationRelativeTo(driver);
		
		if (ptInfoLoc != null){
			portInfo.setLocation(ptInfoLoc);
			portInfo.repaint();
		}
		else {
			portInfo.setLocationRelativeTo(driver);
			ptInfoLoc = portInfo.getLocation();
		}
		
		panel.setVisible(true);
		portInfo.setVisible(true);
		//portInfo.toFront();
		//jdialog.setPreferredSize(new Dimension(dim.width / 2, dim.height / 2));
		
		//portInfo.validate();
		panel.repaint();
		portInfo.repaint();
		driver.repaint();
	}

	
	void displayRow(GridBagConstraints gbc, GridBagLayout gbl, JTextField[] tf,
			JPanel panel, Color col) {
		gbc.gridx = 0;
		gbc.weightx = 0.5;
		for (int i = 0; i < ROWSIZE; i++) {
			if (i == ROWSIZE - 1)
				gbc.weightx = 0.75;
			gbl.setConstraints(tf[i], gbc);
			tf[i].setBackground(DrawFBP.lg);
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
		final int tMYes = 0;
		final int tMMissing = 1;
		final int tMOptional = 2;
		
		boolean input = (type.indexOf("input") > -1 || type.indexOf("param") > -1);
		boolean output = (type.indexOf("output") > -1);
		if (!input && !output) {
			MyOptionPane.showMessageDialog(driver, "Port type of \""
					+ port + "\" must be \"input\" or \"output\"", MyOptionPane.ERROR_MESSAGE);
			return tMMissing;
		}

		for (Arrow arrow : diag.arrows.values()) {
			//if (arrow.endsAtLine)
			//	continue;
			Arrow arr = arrow.findLastArrowInChain(); 
			if (arr == null)
				//return tMInvalid;
				continue;
			
			if (id == arr.toId && arr.downStreamPort != null
					&& stem(arr.downStreamPort).equals(port))
				if (input)
					return tMYes;
				else
					return tMMissing;

			if (id == arrow.fromId && arrow.upStreamPort != null
					&& stem(arrow.upStreamPort).equals(port))
				if (output)
					return tMYes;
				else
					return tMMissing;
		}
		
			if (type.indexOf("optional") > -1)
			return tMOptional;
		else
			return tMMissing; // If port missing, error
	}

	LinkedList<String> checkUnmatchedPorts() {
		LinkedList<String> lst = new LinkedList<String>();
		for (Arrow arrow : diag.arrows.values()) {

			boolean found = false;
			if (arrow.downStreamPort != null
					&& !arrow.downStreamPort.equals("*")) {
				if (!arrow.endsAtLine) {
					if (id == arrow.toId) {
						for (AInPort ip : inputPortAttrs.values()) {
							if (ip.value.equals(stem(arrow.downStreamPort))) {
								found = true;
								break;
							}
						}
						if (!found)
							lst.add("I" + arrow.downStreamPort);
					}
				}
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

	void buildBlockPopupMenu() {
		diag.jpm = new JPopupMenu("            Block-related Actions");
		// driver.curPopup = jpm;
		diag.jpm.setLocation(cx + 100, cy + 100);
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
			menuItem = new JMenuItem("Edit Enclosure Label");
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
					menuItem = new JMenuItem("Assign Subnet Diagram (.drw)");
					menuItem.addActionListener(this);
					diag.jpm.add(menuItem);
					
					diag.jpm.addSeparator();

					if (isSubnet) {
						menuItem = new JMenuItem("Display Subnet");
						menuItem.addActionListener(this);
						diag.jpm.add(menuItem);
					}
					

					else {

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
						
					menuItem1.setEnabled(diag.diagLang != null && 							
						(diag.diagLang.label.equals("Java") || diag.diagLang.label.equals("C#")));
					menuItem2.setEnabled(menuItem1.isEnabled());
					menuItem3.setEnabled(diag.diagLang != null && 							
							diag.diagLang.label.equals("Java")); 
											
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
		//	menuItem = new JMenuItem("Toggle Colour");
		//	diag.jpm.add(menuItem);
		//	menuItem.addActionListener(this);
		} else {
			menuItem = new JMenuItem("Toggle Visible/Invisible");
			diag.jpm.add(menuItem);
			menuItem.addActionListener(this);
		}
		diag.jpm.addSeparator();
		menuItem = new JMenuItem("Delete");
		diag.jpm.add(menuItem);
		menuItem.addActionListener(this);
		
		
	}

	public void actionPerformed(ActionEvent e) {
		String s = e.getActionCommand();
		diag.jpm = null;

		if (s.equals("Edit Process Name or Description") || s.equals("Edit Description")) {

			editDescription(DrawFBP.MODIFY);
			diag.changed = true;
			return;
		}

		if (s.equals("Edit Item")) { // one-line descriptions

			String ans = (String) MyOptionPane.showInputDialog(driver,
					"Enter or change text", "Edit Item",
					MyOptionPane.PLAIN_MESSAGE, null, null, desc);
			if (ans != null/* && ans.length() > 0*/) {
				desc = ans.trim();
				diag.changed = true;
				if (this instanceof IIPBlock) {
					IIPBlock ib = (IIPBlock) this;
					desc = ib.checkNestedChars(desc);					
					
					width = ib.calcIIPWidth(driver.osg);
					buildSides();

					for (Arrow arrow : diag.arrows.values()) {
						if (arrow.fromId == id && arrow.fromY == arrow.toY) // i.e.
							// horizontal
							arrow.fromX = cx + width / 2;
					}
					calcEdges();
				}
				else
					if (isSubnet) {
						int i = driver.getFileTabNo(fullClassName); 
						if (i > -1) {
							ButtonTabComponent b = (ButtonTabComponent) driver.jtp.getComponentAt(i);
							if (b != null && b.diag != null)
								b.diag.desc = desc; 
						}
							
					}
			}
			driver.update(driver.osg);
			diag.changed = true;
			return;
		}
		
		if (s.startsWith("Toggle Subnet On/Off")) {
			isSubnet = !isSubnet;
			diag.changed = true;
			return;
		}

		if (s.startsWith("Assign Subnet Diagram")) {			
 			assignSubnetDiagram(); 			
			//if (isSubnet)
			//	codeFileName = null;
			
			//isSubnet = true;
			return;
		}

		
		if (s.equals("Choose Source Code")) {
			selectSourceCode();
			diag.changed = true;
			return;
		}

		if (s.equals("Display Source Code")) {

			if (isSubnet)
				return;
			if (codeFileName == null) {
				MyOptionPane.showMessageDialog(driver,
						"No code associated with block", MyOptionPane.WARNING_MESSAGE);
				return;
			}

			showCode();
			return;
		}

		if (s.equals("Choose Component/Subnet Class")) {
			
			
			try {
				selectJavaClass();
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			diag.changed = true; 
			return;
		}

		if (s.equals("Display Subnet")) {

			if (subnetFileName == null) {
				MyOptionPane.showMessageDialog(driver,
						"Subnet not selected", MyOptionPane.ERROR_MESSAGE);
			} else {
				String t = subnetFileName;
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
			subnetFileName = null;    
			//description = null;   
			javaComp = null;
			fullClassName = null;
			diag.changed = true;
			
			inputPortAttrs = null;
			outputPortAttrs = null;
			isSubnet = false;
			diag.changed = true;
			//diag.driver.repaint();
			driver.repaint();
			return;

		}

		if (s.equals("Display Full Class Name")) {

			if (javaComp == null && fullClassName == null) {
				MyOptionPane.showMessageDialog(driver,
						"No component code assigned", MyOptionPane.ERROR_MESSAGE);
				return;
			}

			if ((javaComp == null) != (fullClassName == null)) {
				MyOptionPane.showMessageDialog(driver,
						"One of class name and full class name is null, but the other isn't:\n"
						+ "class name - " + javaComp + "\n"
						+ "full class name - " + fullClassName, MyOptionPane.ERROR_MESSAGE);
				return;
			}

			if (javaComp != null || fullClassName != null) {
				MyOptionPane.showMessageDialog(driver, fullClassName + ".class");
			}
			return;
		}
		if (s.equals("Toggle Multiplexing")) {
			multiplex = !multiplex;
			if (!multiplex)
				mpxfactor = null;
			driver.repaint();
			diag.changed = true;
			return;

		}
		if (s.equals("Set Multiplexing Factor")) {
			String ans = (String) MyOptionPane.showInputDialog(driver,
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
			if (javaComp == null) {
				MyOptionPane.showMessageDialog(driver,
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
			driver.repaint();
			diag.changed = true;
			return;

		}
		if (s.equals("Edit Enclosure Label")) {
			// Block must be an Enclosure
			//diag.cEncl = (Enclosure) this;
			String ans = (String) MyOptionPane.showInputDialog(driver,
					"Enter or change text", "Edit enclosure label",
					MyOptionPane.PLAIN_MESSAGE, null, null,
					/*diag.cEncl.*/desc);
			if (ans != null/* && ans.length() > 0*/) {
				/*diag.cEncl.*/desc = ans;
			}
			driver.repaint();
			diag.changed = true;
			return;

		}
		if (s.equals("Edit Subnet Port Name")) {
			// Block must be an Enclosure
			MyOptionPane.showMessageDialog(driver,
					"Deprecated - do excise first, then edit subnet");
			
			return;

		}
		if (s.equals("Toggle Substream Sensitivity")) {
			// Block must be an Enclosure
			MyOptionPane.showMessageDialog(driver,
					"Deprecated - do excise first, then edit subnet");
			
			return;

		}
 
		if (s.startsWith("Drag Contents")) {
			Enclosure enc = (Enclosure) this;
			enc.draggingContents = true;
			driver.blockSelForDragging = this;
			diag.findEnclosedBlocksAndArrows(enc);
			driver.repaint();
			diag.changed = true;
			return;
		}

		if (s.equals("Excise Subnet")) {
			// Block must be an Enclosure	
			
			// remember no file names need to have been filled in at this point
			
			//String ans = (String) MyOptionPane.showInputDialog(driver,					
			//		"Enter subnet diagram name - can be changed later",
			//		"Enter subnet name",
			//		MyOptionPane.PLAIN_MESSAGE, null, null, null);
			//if (ans == null/* && ans.length() > 0*/)  			 
			//	return;		
            //int i = ans.lastIndexOf(".");
            //if (i > -1)
            //	ans = ans.substring(0, i);
            
			/**
			 *  Excise will 
"excise" those blocks and arrows which are completely enclosed by the Enclosure block, and create a new 
subnet including those blocks and arrows.  Arrows that cross the Enclosure boundary will have External Ports
attached to them, which are edited to specify the external port names.

The "source" network will be shown with a new
"subnet" block - the description 
of the enclosure also becomes the title of the subnet diagram (shown in bold underneath the editing area). 
Arrows crossing the Enclosure boundary in the "source" diagram 
will be attached roughly to the new subnet block - if
the user wishes to change their position, they can be adjusted using the 
arrow "drag" function - either dragging the tail or the head of the arrow as appropriate. 

The old diagram will be modified, and a new subnet diagram created, with "external ports" filled in. 			
*/
			
			//--------------------
			diag.excise((Enclosure) this /*, ans  */);  
			
			// diag is the diagram being modified, this is the "enclosure" block within it
			// ans is the name chosen for the (new) subnet
			
			driver.repaint();
			return;

		}
		if (s.equals("Toggle Visible/Invisible")) {
			visible = !visible;
			diag.changed = true;
			driver.repaint();
			return;
		}
		if (s.equals("Toggle Colour")) {
			Enclosure enc = (Enclosure) this;
			enc.coloured = !enc.coloured;
			diag.changed = true;
			driver.repaint();
			return;
		}
		
		if (s.equals("Delete")) {
			final boolean CHOOSE = true;
			diag.delBlock(this, CHOOSE);
			//foundBlock = null;
			diag.changed = true;
			driver.selBlock = null;
			// diag.changeCompLang();
			driver.repaint();
		}
		// if (s.equals("Exit")) {
		// diag.foundBlock = null;
		// driver.repaint();
		// }

	}
	
	// Used if not single line block contents

	boolean editDescription(int option) {

		final JTextArea area = new JTextArea(4, 3);
		JScrollPane pane = new JScrollPane(area);
		
		area.setText(desc);
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
		

		int result = MyOptionPane.showOptionDialog(driver, new Object[]{ 
				"Enter/change " + t + " name or description", 
				pane
			}, init + t);		
		
		//if (option > DrawFBP.EDIT_NO_CANCEL) {
			if (result != MyOptionPane.OK_OPTION)
				return false;
		//}
        
		desc = area.getText();
		
		centreDesc();  
		buildSides();
		
		diag.changed = true;

		//driver.repaint();

		return true;
	}

	void assignSubnetDiagram() {
		//int xa, ya;
		if (subnetFileName != null) {
			if (MyOptionPane.YES_OPTION != MyOptionPane.showConfirmDialog(
					driver,
					"Block already associated with diagram (" + subnetFileName
							+ ") - change it?",
					"Change diagram", MyOptionPane.YES_NO_OPTION)) {
				return;
			}
			subnetFileName = null;
		}

		String t = driver.properties.get("currentDiagramDir");
		if (t == null) {
			t = driver.properties.get("currentDiagramDirectory");		
			if (t == null)
				t = System.getProperty("user.home");
		}
		MyFileChooser fc = new MyFileChooser(driver,new File(t),
				diag.fCParm[Diagram.DIAGRAM]);

		int returnVal = fc.showOpenDialog();
		String dFN = null;
		if (returnVal == MyFileChooser.APPROVE_OPTION) {
			dFN = driver.getSelFile(fc);
			String suff = diag.fCParm[Diagram.DIAGRAM].fileExt;
			if (!(dFN.endsWith(suff)))
				dFN += suff;
			driver.curDiag.changed = true;
			
			
			subnetFileName = dFN;
			dFN = dFN.replace("\\",  "/");
			
			//diag = driver.curDiag;
			isSubnet = true;
			//File df = driver.openAction(dFN);
			//if (df == null)
			//	return;

			int i = dFN.lastIndexOf("/");
			dFN = dFN.substring(i + 1);
			if (desc == null || desc.equals("")) {
				int j = dFN.lastIndexOf("/"); 
				desc = dFN.substring(j + 1);

				String ans = (String) MyOptionPane.showInputDialog(driver,
						"Enter or change text", "Edit block description",
						MyOptionPane.PLAIN_MESSAGE, null, null, desc);

				if (ans != null)
					desc = ans;
			}
			fullClassName = null;
			javaComp = null;
			
			
			MyOptionPane.showMessageDialog(driver,
					"Subnet " + dFN + " associated with " + desc + " block",
					MyOptionPane.INFORMATION_MESSAGE);
			
		}

		

		driver.repaint();
	}

	void selectJavaClass() throws MalformedURLException {

		//String oldFullClassName = fullClassName;
		

		if (javaComp != null) {
			if (MyOptionPane.YES_OPTION != MyOptionPane.showConfirmDialog(
					driver,
					"Block already associated with class ("
							+ javaComp.getName() + ") - change it?",
					"Change class", MyOptionPane.YES_NO_OPTION)) {
				return;
			}
			// javaClass = null;
			// fullClassName = null;
		}

		//javaComp = null;
		if (!driver.locateJavaFBPJarFile(false)) {
			MyOptionPane.showMessageDialog(driver,
					"JavaFBP jar file not found - try Locate JavaFBP Jar File", MyOptionPane.ERROR_MESSAGE);
			return;
		}

		String t = driver.properties.get("currentClassDir");
		if (t == null)
			t = System.getProperty("user.home");

		MyFileChooser fc = new MyFileChooser(driver,new File(t),
				diag.fCParm[Diagram.CLASS]);

		int returnVal = fc.showOpenDialog();

		boolean injar = true;
		File cFile = null;
		Class<?> tempComp = null;
		String tempFCN = null;
		if (returnVal == MyFileChooser.APPROVE_OPTION) {
			String res = driver.getSelFile(fc);

			int i = res.indexOf("!");  // res only has ! if from jar file
			if (i > -1) { 
				String res2 = res.substring(i + 2); // ! will be followed by
													// slash
				res2 = res2.replace("\\",  "/");
				res2 = res2.replace('/', '.');

				File f = new File(res.substring(0, i));

				if (res2.endsWith(".class"))
					res2 = res2.substring(0, res2.length() - 6); 
				
				res = res.substring(0, i) + "!" + res2;
				

				URL[] urls = driver.buildUrls(f);

				if (urls == null)
					tempComp = null;
				else {
					// Create a new class loader with the directory
					myURLClassLoader = new URLClassLoader(urls, driver.getClass()
							.getClassLoader());
					try {
						
						tempComp = myURLClassLoader.loadClass(res2);
					} catch (ClassNotFoundException e2) {
						tempComp = null;
					} catch (NoClassDefFoundError e2) {   
						tempComp = null;
					}
					if (tempComp == null) {
						MyOptionPane.showMessageDialog(driver,
								"Problem with classes in selected file: " + res2, MyOptionPane.ERROR_MESSAGE);
						return;
					} 
					tempComp = isValidClass(res.substring(0, i), res2,    
							injar);
					if (tempComp != null)
						tempFCN = res;
					//else
					//	fullClassName = null;
				}
			} else {
				// we are looking in local class hierarchy (not a jar file)
				String fs = res;
				injar = false;

				if (fs.endsWith("jar"))
					cFile = new File(driver.javaFBPJarFile); 
				else {
					cFile = new File(fs);
					if (cFile == null || !(cFile.exists())) {
						MyOptionPane.showMessageDialog(driver,
								"Unable to find file " + cFile.getName(), MyOptionPane.ERROR_MESSAGE);
						return;
					}
				}

				boolean classFound;
				File fp = null;

				String u = cFile.getName();
				
				String error = "";

				
				if (u.endsWith(".class")) {
				    u = u.substring(0, u.length() - 6);

				//String error = "";

				while (true) {

					fp = cFile.getParentFile();    
					if (fp == null)
						break;
					//try {
						classFound = true;

						URL[] urls = driver.buildUrls(fp);

						if (urls == null)
							tempComp = null;
						else {

							// Create a new class loader with the directory
							myURLClassLoader = new URLClassLoader(urls, driver.getClass()
									.getClassLoader());

							try {
								tempComp = myURLClassLoader.loadClass(u);
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
			}
				if (tempComp == null) {
					MyOptionPane.showMessageDialog(driver,
							"Class '" + driver.getSelFile(fc) + "' invalid class ("
									+ error + ")", MyOptionPane.ERROR_MESSAGE);
				}

				else {

					driver.saveProp("currentClassDir",
							fp.getAbsolutePath());
					//saveProperties();
					
					tempComp = isValidClass(fp.getAbsolutePath(),
							tempComp.getName(), !injar); 
					
					if (tempComp != null)
						tempFCN = fp.getAbsolutePath() + "!"
							+ tempComp.getName();
					//else
					//	fullClassName = null;
				}
			}
		}

		if (tempComp == null) {
			MyOptionPane.showMessageDialog(driver, "No class selected", MyOptionPane.ERROR_MESSAGE);
		} else {
			//if (!fullClassName.equals(oldFullClassName))
			javaComp = tempComp;
			//String w = fullClassName;
			//w = w.replace("\\",  File.separator);
			//w = w.replace("/",  File.separator);
			fullClassName = tempFCN;
			displayPortInfo();
			
		}
		
		diag.changed = true;
		// diag.changeCompLang();		

		return;
	}

	

	void selectSourceCode() {

		GenLang gl = driver.curDiag.diagLang;
		if (codeFileName != null) {
			if (MyOptionPane.YES_OPTION != MyOptionPane.showConfirmDialog(driver,
					"Block already associated with source code (" + codeFileName
							+ ") - change it?",
					"Change source code", MyOptionPane.YES_NO_OPTION))
				return;
			}
		 

		if (!(gl.label.equals("Java")) && javaComp != null) {
			if (MyOptionPane.NO_OPTION == MyOptionPane.showConfirmDialog(
					driver,
					"You have selected a non-Java language and there is a Java class associated with this block - go ahead?",
					"Java previously used", MyOptionPane.YES_NO_OPTION)) {

				javaComp = null;
				codeFileName = null;
				return;
			}
		}

		String ans = (String) MyOptionPane.showInputDialog(driver,
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

			MyFileChooser fc = new MyFileChooser(driver,new File(t),
					diag.fCParm[Diagram.PROCESS]);

			int returnVal = fc.showOpenDialog();

			File cFile = null;
			if (returnVal == MyFileChooser.APPROVE_OPTION) {

				cFile = new File(driver.getSelFile(fc));
				if (cFile == null || !(cFile.exists())) {
					if (MyOptionPane.NO_OPTION == MyOptionPane.showConfirmDialog(
							driver,
							"You have entered a file name that does not exist - go ahead?",
							"File does not exist", MyOptionPane.YES_NO_OPTION)) {
						return;
					}
					codeFileName = driver.getSelFile(fc);
				} else {
					codeFileName = cFile.getAbsolutePath();
					driver.saveProp(gl.srcDirProp,
							cFile.getParentFile().getAbsolutePath());
					//saveProperties();
				}

			}
		}

	}

	void showCode() {

		if (codeFileName == null) {
			MyOptionPane.showMessageDialog(driver,
					"No code associated with block", MyOptionPane.ERROR_MESSAGE);
			return;
		}
		String t = codeFileName;

		// int k = t.lastIndexOf(".");
		// String suff = t.substring(k + 1);

		// diag.changeCompLang();
		CodeManager cm = new CodeManager(diag);
		cm.displayDoc(new File(t), diag.diagLang, null);

	}
	
	
}
