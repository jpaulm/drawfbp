package com.jpaulmorrison.graphics;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;

import com.jpaulmorrison.graphics.DrawFBP.GenLang;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public class CodeManager implements ActionListener /* , DocumentListener */ {

	DrawFBP driver;
	HashSet<String> portNames;
	HashMap<String, Integer> blocklist;
	// HashMap<String, Integer> portlist;
	Style baseStyle, normalStyle, packageNameStyle, errorStyle,
			quotedStringStyle, commentStyle;
	JFrame jf;
	//StyledDocument doc;
	//boolean changed = false;
	boolean generated = false;
	boolean packageNameChanged = false;
	// String targetLang;

	boolean error = false;

	boolean fbpMode; // generating .fbp notation
	String ext;
	Diagram diag;
	// Font font;
	String packageName = null;
	LinkedList<String> counterList = new LinkedList<String>();
	JPanel panel;
	JScrollPane scrollPane;

	// GenLang compLang;
	HashMap<Integer, String> descArray = new HashMap<Integer, String>();
	// HashMap<Integer, String> cdescArray = new HashMap<Integer, String>();
	// int type;
	JLabel nsLabel = new JLabel();
	boolean SAVE_AS = true;
	//FileChooserParm[] saveFCPArr;
	//String langLabel;
	GenLang gl = null;
	String upPort = null;;
	String dnPort = null;
	//StyledDocument doc = null;
	MyDocument doc = null;
	String clsName = null;
	JTextPane docText = null;
	JTextArea lineNos = null;
	//boolean completeChange;

	CodeManager(Diagram d) {
		diag = d;
		gl = diag.diagLang;
		driver = d.driver;
		d.cm = this;
		jf = new JFrame();
		driver.depDialog = jf;
		nsLabel.setFont(driver.fontg);
		//completeChange = false;
		
		DrawFBP.applyOrientation(jf);

		//dialog.setAlwaysOnTop(false);
		
		jf.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		jf.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				boolean res = true;
				if (doc.changed)
					res = askAboutSaving();
				if (res){
					driver.depDialog = null;
					jf.dispose();
				}
			}

		});
		
		jf.setJMenuBar(createMenuBar());
		jf.repaint();

		BufferedImage image = driver.loadImage("DrawFBP-logo-small.png");
		jf.setIconImage(image);

		Point p = driver.getLocation();
		Dimension dim = driver.getSize();
		jf.setPreferredSize(new Dimension(dim.width - 100, dim.height - 50));
		jf.setLocation(p.x + 100, p.y + 50);
		jf.setForeground(Color.WHITE);
		
		// jframe.setVisible(false);
		

		StyleContext sc = new StyleContext();	
		//doc = new DefaultStyledDocument(); 
		doc = new MyDocument(sc); 
		setStyles(sc);
		
		panel = new JPanel();
		scrollPane = new JScrollPane();
		//scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
		//scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED); 
		jf.add(scrollPane);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		//scrollPane.add(panel);
		lineNos = new JTextArea();
		
		docText = new JTextPane();
		 
		panel.add(lineNos);
		panel.add(Box.createHorizontalStrut(20));
		panel.add(docText);	
		lineNos.setPreferredSize(new Dimension(80, Short.MAX_VALUE));
		lineNos.setMinimumSize(new Dimension(60, Short.MAX_VALUE));
		lineNos.setMaximumSize(new Dimension(100, Short.MAX_VALUE));
		lineNos.setBackground(DrawFBP.lb);
		lineNos.setEditable(false);
	
		jf.pack();
		 
		scrollPane.setViewportView(panel);
		/*
		String data = "";
		try {
			data = doc.getText(0, doc.getLength());
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int lines = 0;
		for (int i = 0; i < data.length(); i++) {
		    if (data.charAt(i) == '\n') {
		        lines++;
		    }
		}		
				
		JTextPane lineNos = new JTextPane();
		//lineNos.setMinimumSize(new Dimension(50, 10000));
		lineNos.setPreferredSize(new Dimension(50, 10000));
		lineNos.setBackground(DrawFBP.lb);
		String numbers = "";
		for (int i = 1; i < lines; i++) {
			numbers += String.format("%8s", i) + "\n";
		}
		lineNos.setText(numbers);
		lineNos.setVisible(true);
		JTextPane docText = new JTextPane(doc);
		docText.setVisible(true);
		panel.add(lineNos);
		panel.add(docText);		
		//scrollPane = new JScrollPane(panel,
		//		ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
		//		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		setStyles(sc);
		*/
		//dialog.setVisible(true);
		//panel.setVisible(true);
		//scrollPane.setVisible(true);
		
		//dialog.add(scrollPane);		
		//panel.setFont(driver.fontf);
		jf.setFont(driver.fontf);
		//dialog.pack();
		
		nsLabel.setText(doc.changed ? "Changed" : "Unchanged ");
	}
	
	void genCode() {
		if (!generateCode()) {
			jf.dispose();
		}
	}

	boolean generateCode() {

		fbpMode = false;
		//langLabel = diag.diagLang.label;
		//gl = diag.diagLang;
		if (gl.label.equals("FBP")) {
			genFbpCode();
			return true;
		}		

		// diag.targetLang = langLabel;
		doc.changed = true;
		
		String curDir = diag.diagFile.getParentFile().getAbsolutePath();
		driver.saveProp("currentDiagramDir", curDir);

		String component = (gl.label.equals("Java"))
				? "component"
				: "Component";
		// String connect = (gl.label.equals("Java")) ? "connect" : "Connect";
		String initialize = (gl.label.equals("Java"))
				? "initialize"
				: "Initialize";
		String _port = (gl.label.equals("Java")) ? "port" : "Port";
		String sDO = (gl.label.equals("Java"))
				? "setDropOldest()"
				: "SetDropOldest()";
		
		String fn = diag.diagFile == null ? "unknown" : diag.diagFile.getName();		
			
		jf.setTitle("Generated Code for " + fn);		

		jf.setJMenuBar(createMenuBar());		

		BufferedImage image = driver.loadImage("DrawFBP-logo-small.png");
		jf.setIconImage(image);
		
		JFrame.setDefaultLookAndFeelDecorated(true);

		String code = "";

		// boolean error = false;
		portNames = new HashSet<String>();
		blocklist = new HashMap<String, Integer>();

		ext = "Network";

		Style[] styles = new Style[20];

		for (Block block : diag.blocks.values()) {
			if (block instanceof ExtPortBlock) {
				ext = "SubNet";
				if (block.desc == null || block.desc.equals("")) {
					block.desc = "IN/OUT";
					block.desc = makeUniqueDesc(block.desc);
				}
			}
		}

		String w = diag.diagFile.getAbsolutePath();
		int i = w.lastIndexOf(".");
		w = w.substring(0, i);  // drop extension
		i = w.lastIndexOf(".");
		w = w.replace("\\", "/");
		int j = w.lastIndexOf("/");
		j = Math.max(i,  j);
		clsName = w.substring(j + 1);
		
		
		
		if (gl.label.equals("Java")) {
			packageName = driver.properties.get("currentPackageName");
			if (packageName == null || packageName.equals("") || packageName.equals("null")
					|| packageName.equals("(null)")) {
				packageName = (String) MyOptionPane.showInputDialog(jf,
						"Please fill in a package name or namespace here if desired", null);
				if (packageName == null || packageName.equals("") || packageName.equals("null"))
					//packageName = "(null)";
					packageName = null;
				else
					packageName = packageName.trim();				
				driver.saveProp("currentPackageName", packageName);
				//saveProperties();
			}
		}
		
		
		
		String[] contents;
		if (gl.label.equals("JSON")) {
			contents = new String[1];
			contents[0] = generateJSON();
			styles[0] = normalStyle;
		} else {
			contents = new String[20];  
			int k = 0;
			if (gl.label.equals("Java")) {
				if (packageName != null) {
					contents[0] = "package ";
					contents[1] = packageName + ";";
					contents[2] = " //change package name, or delete statement, if desired\n"; 
					k = 3;
				}
			} else {
				contents[0] = "using System;\nusing System.IO;\nusing FBPLIB;\nusing Components;\nnamespace Xxxxxxxxxx{";
				contents[1] = " ";
				contents[2] = " //change namespace name if desired\n";  
				k = 3;
			}

			if (gl.label.equals("Java"))
				contents[k + 0] = "import com.jpaulmorrison.fbp.core.engine.*; \n";

			if (ext.equals("SubNet"))
				contents[k + 1] = genMetadata(gl.label) + "\n";
			else
				contents[k + 1] = "";

			contents[k + 2] = "public class ";
			
			contents[k + 3] = clsName;
			
			if (gl.label.equals("Java"))
				contents[k + 4] = " extends ";
			else
				contents[k + 4] = " : ";
			contents[k + 5] = ext;
			
			if (gl.label.equals("Java"))
				contents[k + 6] = " {\nString description = ";
			else
				contents[k + 6] = " {\nstring description = ";
			
			if (diag.desc == null)
				diag.desc = "(no description)";
			contents[k + 7] = "\"" + diag.desc + "\"";
			
			if (gl.label.equals("Java"))
				contents[k + 8] = ";\nprotected void define() { \n";
			else
				contents[k + 8] = ";\npublic override void Define() { \n";

			k = 0;
			if (packageName != null) {
				styles[k + 0] = normalStyle;
				styles[k + 1] = packageNameStyle;
				styles[k + 2] = baseStyle;
				k = 3;
			}
			styles[k + 0] = normalStyle;
			styles[k + 1] = packageNameStyle;
			styles[k + 2] = normalStyle;
			styles[k + 3] = packageNameStyle;
			styles[k + 4] = normalStyle;
			styles[k + 5] = packageNameStyle;
			styles[k + 6] = normalStyle;
			styles[k + 7] = packageNameStyle;
			styles[k + 8] = normalStyle;
		
			
			for (Block block : diag.blocks.values()) {

				String t;

				if (block instanceof ProcessBlock) {
					if (block.desc == null) {
						MyOptionPane.showMessageDialog(driver,
								"One or more missing block descriptions", MyOptionPane.WARNING_MESSAGE);
						error = true;
						return false;
					}

					String s = cleanDesc(block);
					
					String c = "\"Invalid class\""; 
					error = true;
					 
					if (block.javaComp == null) {
						MyOptionPane.showMessageDialog(driver,
								"Class name missing for '" + s + "' - diagram needs to be updated",
								MyOptionPane.WARNING_MESSAGE);
						//return false;						 

					} else {
						c = cleanComp(block);
						//if (c.toLowerCase().endsWith(".class"))
						//	c = c.substring(0, c.length() - 6);
					}
					
					 
					
					descArray.put(new Integer(block.id), s);

					if (!block.multiplex){
						if (!block.visible)
							s += "(invisible)";
						code += "  " + genComp(s, c, gl.label) + "; \n";  						
					}
					else {
						if (block.mpxfactor == null) {
							String d = (String) MyOptionPane.showInputDialog(
									driver,
									"Multiplex factor for " + "\""
											+ block.desc + "\"",
									"Please enter number");
							if (d == null || d.equals("")) {
								block.mpxfactor = "????";
								error = true;
							} else
								block.mpxfactor = d;
							diag.changed = true;
						}

						jf.repaint();
						if (block.mpxfactor != null) {
							code += "int " + compress(s) + "_count = "
									+ block.mpxfactor + "; "
									+ "     //  multiplex counter for " + "\""
									+ s + "\"\n";
						}

						code += "for (int i = 0; i < " + compress(s)
								+ "_count; i++)\n";
						// code += component + "(\"" + s + ":\" + i, " + c +
						// "); ";

						code += "  " + genCompMpx(s, c, gl.label) + "; ";
						if (c.equals("????")) {
							code += "       // <=== fill in component name";
						}
						code += "\n";
					}

				}

				if (block instanceof ExtPortBlock) {
					ExtPortBlock eb = (ExtPortBlock) block;
					String s = "";
					if (block.type.equals(Block.Types.EXTPORT_IN_BLOCK)) {
						s = "SUBIN";
						if (eb.substreamSensitive)
							t = "SubInSS";
						else
							t = "SubIn";
					} else
						if (block.type.equals(Block.Types.EXTPORT_OUT_BLOCK)) {
						s = "SUBOUT";
						if (eb.substreamSensitive)
							t = "SubOutSS";
						else
							t = "SubOut";
					} else {
						s = "SUBOI";
						t = "SubOI";
					}
					s = makeUniqueDesc(s); // and make it unique
					//s = makeUniqueDesc(s); // and make it unique
					if (!(t.toLowerCase().endsWith(".class")))
					    t += ".class";
					//if (t.toLowerCase().endsWith(".class"))
					//	t = t.substring(0, t.length() - 6);

					code += "  " + genComp(s, t, gl.label) + "; \n";
					code += "  " + initialize + "(\"" + eb.desc + "\", " + component + "(\""
							+ s + "\"), " + _port + "(\"NAME\")); \n";
					descArray.put(new Integer(block.id), s);
				}

				if (block instanceof IIPBlock)
					descArray.put(new Integer(block.id), block.desc);
				
			}

			for (Arrow arrow : diag.arrows.values()) {
				// generate a connection or initialize
				if (arrow.toX == -1  || arrow.toId == -1)               // if toX or toId = -1, do not generate
					continue;
				Block from = diag.blocks.get(new Integer(arrow.fromId));
				Arrow a2 = arrow.findLastArrowInChain();
				if (a2 == null)
					continue;
				Block to = diag.blocks.get(new Integer(a2.toId));
				if (to == null) {
					String s = "Downstream block not found";
					if (from != null)
						s += ": from " + from.desc;
					MyOptionPane.showMessageDialog(driver,
							s, MyOptionPane.ERROR_MESSAGE);
					//break;
				}
				if (from == null || to == null || from instanceof FileBlock
						|| from instanceof ReportBlock
						|| from instanceof LegendBlock
						|| to instanceof FileBlock || to instanceof ReportBlock
						|| to instanceof LegendBlock)
					continue;

				if (!getPortNames(arrow))
					return false;

				jf.repaint();

				String fromDesc = descArray.get(new Integer(arrow.fromId));
				// String cFromDesc = cdescArray.get(new Integer(arrow.fromId));

				String toDesc = descArray.get(new Integer(a2.toId));
				// String cToDesc = cdescArray.get(new Integer(a2.toId));

				// boolean ok;

				// }

				jf.repaint();
				// String upPort = arrow.upStreamPort;
				// String dnPort = a2.downStreamPort;

				String cap = "";
				if (arrow.capacity > 0)
					cap = ", " + arrow.capacity;
				if (from instanceof ProcessBlock
						&& to instanceof ProcessBlock) {

					if (!arrow.endsAtLine && checkDupPort(dnPort, to)) {
						String proc = to.desc;
						MyOptionPane.showMessageDialog(driver,
								"Duplicate port name: " + proc + "." + dnPort, MyOptionPane.ERROR_MESSAGE);
						error = true;
					}
					if (checkDupPort(upPort, from)) {
						String proc = from.desc;
						MyOptionPane.showMessageDialog(driver,
								"Duplicate port name: " + proc + "." + upPort, MyOptionPane.ERROR_MESSAGE);
						error = true;
					}
					if (from.multiplex) {
						code += "for (int i = 0; i < " + compress(fromDesc)
								+ "_count; i++)\n";
						code += "  " + genConnect(arrow) + "(" + component + "(\""
								+ fromDesc + ":\" + i), " + _port + "("
								+ q(upPort) + "), " + component + "(\"" + toDesc
								+ "\"), " + _port + "(" + q(dnPort) + ")" + cap
								+ "); \n";
						if (arrow.dropOldest)
							code += "c" + arrow.id + "." + sDO + "; \n";
					} else if (to.multiplex) {
						code += "for (int i = 0; i < " + compress(toDesc)
								+ "_count; i++)\n";
						code += "  " + genConnect(arrow) + "(" + component + "("
								+ q(fromDesc) + "), " + _port + "(" + q(upPort)
								+ ",i), " + component + "(\"" + toDesc
								+ ":\" + i), " + _port + "(" + q(dnPort) + ")"
								+ cap + "); \n";
						if (arrow.dropOldest)
							code += "c" + arrow.id + "." + sDO + "; \n";
					} else {
						code += "  " + genConnect(arrow) + "(" + component + "("
								+ q(fromDesc) + "), " + _port + "(" + q(upPort)
								+ "), " + component + "(\"" + toDesc + "\"), "
								+ _port + "(" + q(dnPort) + ")" + cap + "); \n";
						if (arrow.dropOldest)
							code += "c" + arrow.id + "." + sDO + "; \n";
					}
				}

				else
					if (from instanceof IIPBlock
							&& to instanceof ProcessBlock) {
					if (!arrow.endsAtLine && checkDupPort(dnPort, to)) {
						String proc = to.desc;
						MyOptionPane.showMessageDialog(driver,
								"Duplicate port name: " + proc + "." + dnPort, MyOptionPane.ERROR_MESSAGE);
						error = true;
					}

					code += "  " + initialize + "(" + q(fromDesc) + ", " + component
							+ "(\"" + toDesc + "\"), " + _port + "(" + q(dnPort)
							+ ")" + cap + "); \n";
				}

				if (from instanceof ExtPortBlock) {

					code += "  " + genConnect(arrow) + "(" + component + "("
							+ q(fromDesc) + "), " + _port + "(\"OUT\"), "
							+ component + "(\"" + toDesc + "\"), " + _port + "("
							+ q(dnPort) + ")" + cap + "); \n";
					if (arrow.dropOldest)
						code += "c" + arrow.id + "." + sDO + "; \n";
				} else if (to instanceof ExtPortBlock) {

					code += "  " + genConnect(arrow) + "(" + component + "("
							+ q(fromDesc) + "), " + _port + "(" + q(upPort)
							+ "), " + component + "(\"" + toDesc + "\"), "
							+ _port + "(\"IN\" " + ")" + cap + "); \n";
					if (arrow.dropOldest)
						code += "c" + arrow.id + "." + sDO + "; \n";
				}
			}

			if (ext.equals("Network")) {
				String s = diag.title;
				i = s.indexOf(".");
				if (i > -1)
					s = s.substring(0, i);
				code += "} \n";
				if (gl.label.equals("Java"))
					code += "public static void main(String[] argv) throws Exception  { \n"
							+ "  new " + s + "().go(); \n";
				else
					code += "internal static void main(String[] argv) { \n"
							+ "  new " + s + "().Go();\n }\n";
			}

			code += "} \n";

			int sno = 12;
			contents[sno] = code;
			styles[sno] = normalStyle;

			sno++;

			contents[sno] = "}\n";
			styles[sno] = normalStyle;
			sno++;

			if (error) {
				contents[sno] = "\n /* Errors in generated code - they must be corrected for your program to run - \n\n"
						+ "               remove this comment when you are done  */  \n";
				styles[sno] = errorStyle;
				MyOptionPane.showMessageDialog(driver,
						"Error in generated code", MyOptionPane.ERROR_MESSAGE);
			}
		}
		// insert data from arrays
		try {
			
			for (i = 0; i < contents.length; i++) {
				if (contents[i] != null)
					doc.insertString(doc.getLength(), contents[i], styles[i]); // insert data at end of doc
			}
		} catch (BadLocationException ble) {
			MyOptionPane.showMessageDialog(driver,
					"Couldn't insert text into text pane", MyOptionPane.ERROR_MESSAGE);
			return false;
		}

		// if (diag.compLang != glcompLang)
		// diag.compLang = compLang;
		doc.changed = true;
		colourCode(); 

		generated = true;

		//nsLabel.setText("Not saved");
		nsLabel.setText(doc.changed ? "Changed" : " ");

		displayDoc(null, gl, null);

		jf.repaint();
		// jframe.update(jdriver.osg);

		return true;
	}

	String genComp(String name, String className, String lang) {
		if (className == null)
			className = "????";
		
		if (lang.equals("Java")) {
			//if (!(className.equals("\"Invalid class\"")))
			if (!(className.endsWith(".class")))
				className += ".class";
			return "component(\"" + name + "\"," + className + ")";
		}
		else {
			className.replace("\\",  "/");
			int i = className.lastIndexOf("/");
			int j = className.lastIndexOf("."); 
			i = Math.max(i, j);
			className = className.substring(i + 1);
			return "Component(\"" + name + "\", typeof(" + className + "))";
		}
	}

	String genConnect(Arrow arrow) {
		String connect = (gl.label.equals("Java")) ? "connect" : "Connect";
		if (arrow.dropOldest) {
			connect = "Connection c" + arrow.id + " = " + connect;  
		}
		return connect;
	}

	String genCompMpx(String name, String className, String lang) {
		if (className == null)
			className = "????";
		if (lang.equals("Java"))
			return "component(\"" + name + ":\" + i," + className + ".class)";
		else
			return "Component(\"" + name + ":\" + i, typeof(" + className
					+ "))";
	}
	
	final boolean SAVEAS = true;

	String displayDoc(File file, GenLang gl, String fileString) {
		
		if (file != null) {
			clsName = file.getName();
			int i = clsName.lastIndexOf(".");
			if (i > -1)
				clsName = clsName.substring(0, i);
			jf.setTitle("Displayed Code: " + file.getName());
		}
		else
			jf.setTitle("Displayed Code: " + clsName + "." + gl.suggExtn);
		
		// genLang = gl;
		//if (file != null) {
			//clsName = file.getName();
			//int i = clsName.lastIndexOf(".");
			//if (i > -1)
			//	clsName = clsName.substring(0, i);
			if (file != null) {
			if (fileString == null) {
				fileString = driver.readFile(file /* , !saveType */);
				if (fileString == null) {
					MyOptionPane.showMessageDialog(driver,
							"Couldn't read file: " + file.getAbsolutePath(),
							MyOptionPane.ERROR_MESSAGE);
					return fileString;
				}
				// changed = false;
				// if (file.getName().endsWith(".fbp")) {
				// nsLabel.setText("Not changed");

				MyDocListener1 myDocListener1 = new MyDocListener1();
				doc.addDocumentListener(myDocListener1);

				fbpMode = true;

				// }
				String suff = driver.getSuffix(file.getName());
				String packageName = null;

				if (suff != null && suff.toLowerCase().equals("java")) {
					packageName = findPackage(fileString);
					if (null == packageName) {

						packageName = (String) MyOptionPane.showInputDialog(
								driver,
								"Missing package name - please specify package name, if desired",
								null);

						if (packageName != null) {
							fileString = "package " + packageName + ";\n"
									+ fileString;
							doc.changed = true;
						}
					}

					driver.saveProp("currentPackageName", packageName);
					driver.saveProperties();

				}
			}
		 
		//completeChange = true;
		try {
			doc.remove(0, doc.getLength());
			doc.insertString(0, fileString, normalStyle);
		} catch (BadLocationException ble) {
			MyOptionPane.showMessageDialog(driver,
					"Couldn't insert text into text pane", MyOptionPane.ERROR_MESSAGE);
			return fileString;

		}
		 }
		
		//clsName = file.getName();
		//int i = clsName.lastIndexOf(".");
		//clsName = clsName.substring(0, i);

		colourCode();
		 
		String data = "";
		try {
			data = doc.getText(0, doc.getLength());
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int lines = 0;
		for (int j = 0; j < data.length(); j++) {
		    if (data.charAt(j) == '\n') {
		        lines++;
		    }
		}	
		
		panel = new JPanel();
	 
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		 
		String numbers = "";
		for (int j = 1; j < lines + 1; j++) {
			numbers += String.format("%12s", j) + "    \n";
		}
		lineNos.setText(numbers);
		lineNos.setForeground(Color.BLACK);
		
		lineNos.setVisible(true);
		//docText = new JTextPane(doc);
		
		docText.setStyledDocument(doc);
		docText.setVisible(true);
		//dialog.add(scrollPane);		
		panel.setFont(driver.fontf);
	 
		jf.pack();
		jf.setVisible(true);
		panel.setVisible(true);
		scrollPane.add(panel);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS); 
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS); 
		
		jf.add(scrollPane);
		
		scrollPane.setVisible(true);
		 
		
		nsLabel.setText(doc.changed ? "Changed" : "Unchanged ");
		
		//doc.changed = true;
		// if (file.getName().endsWith(".fbp"))
		// type = DrawFBP.DIAGRAM;
		
		jf.repaint();	
		
		return fileString;

	}

	String findPackage(String data){
		String pkg = null;
		//int lineNo = 0;
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
				}
				else
					bp.bsp();     // back up one character bc one slash has been consumed
			}
	
			if (bp.tc('/', 'o') && bp.tc('/', 'o')) {
					while (true) {
						if (bp.tc('\r', 'o')){
							bp.tc('\n', 'o');
								break;
						}
						if (bp.tc('\n', 'o')){
							bp.tc('\r', 'o');
							break;
						}
						bp.tu('o');
					}
				}
			else
				break;
			}
	
			if (bp.tc('p', 'o') &&
				bp.tc('a', 'o') &&
				bp.tc('c', 'o') &&
				bp.tc('k', 'o') &&
				bp.tc('a', 'o') &&
				bp.tc('g', 'o') &&
				bp.tc('e', 'o')) {
				
			 
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
	 
	String genMetadata(String lang) {
		String inData = "";
		String outData = "";
		String descr = " ";
		if (diag.desc != null && !(diag.desc.equals(" "))) {
			if (lang.equals("Java"))
				descr = "@ComponentDescription(\"" + diag.desc + "\") \n";
			else
				descr = "[ComponentDescription(\"" + diag.desc + "\")] \n";
		}

		int ins = 0;
		int outs = 0;
		for (Block block : diag.blocks.values()) {
			if (block instanceof ExtPortBlock) {
				if (block.type.equals(Block.Types.EXTPORT_IN_BLOCK)) {
					if (lang.equals("Java")) {
						ins++;
						inData += ", @InPort(\"" + block.desc + "\")";
					} else {
						inData += "[InPort(\"" + block.desc + "\")] \n";
					}
				} else if (block.type.equals(Block.Types.EXTPORT_OUT_BLOCK)) {
					if (lang.equals("Java")) {
						outs++;
						outData += ", @OutPort(\"" + block.desc + "\")";
					} else {
						outData += "[OutPort(\"" + block.desc
								+ "\")] \n";
					}
				}
			}
		}
		if (lang.equals("Java")) {
			if (!inData.equals("")) {
			    inData = inData.substring(2);
			    if (ins > 1)
			    	inData = "@InPorts({" + inData + "})";
			    inData += "\n";
			}
			if (!outData.equals("")) {
				outData = outData.substring(2);
				if (outs > 1)
					outData = "@OutPorts({" + outData + "})";
				outData += "\n";
			}
		}
		return descr + inData + outData;
	}

	String makeUniqueDesc(String s) {
		Integer i;
		String t = s;
		if (blocklist.containsKey(s)) {
			i = blocklist.get(s);
			i = new Integer(i.intValue() + 1);
			t = s + "_" + i.toString() + "_";
		} else
			i = new Integer(1);
		blocklist.put(s, i);
		return t;

	}

	String makeUniquePort(String s, Block b) {
		Integer i;
		String t = s;
		if (b.portlist == null)
			b.portlist = new HashMap<String, Integer>();
		if (b.portlist.containsKey(s)) {
			i = b.portlist.get(s);
			i = new Integer(i.intValue() + 1);
			t = s + "_" + i.toString() + "_";
		} else
			i = new Integer(1);
		b.portlist.put(s, i);
		return t;

	}

	boolean checkDupPort(String port, Block bl) {
		// return false if no duplication
		String s = port + ":" + bl.id;
		if (portNames.contains(s))
			return true;
		portNames.add(s);
		return false;
	}

	void setStyles(StyleContext sc) {

		Font font = driver.fontf;
		// baseStyle = new SimpleAttributeSet();
		Style defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
		baseStyle = sc.addStyle(null, defaultStyle);
		int s = font.getSize();

		StyleConstants.setSpaceAbove(baseStyle, 4);
		StyleConstants.setSpaceBelow(baseStyle, 4);
		StyleConstants.setForeground(baseStyle, Color.DARK_GRAY);
		//StyleConstants.setFontFamily(baseStyle, font.getFamily());
		StyleConstants.setFontFamily(baseStyle, font.getName());		
		StyleConstants.setFontSize(baseStyle, s);

		normalStyle = sc.addStyle(null, baseStyle);
		StyleConstants.setForeground(normalStyle, Color.BLUE);
		// StyleConstants.setFontSize(normalStyle, s);

		packageNameStyle = sc.addStyle(null, baseStyle);
		StyleConstants.setForeground(packageNameStyle, Color.MAGENTA);
		// StyleConstants.setFontSize(packageNameStyle, s);

		errorStyle = sc.addStyle(null, baseStyle);
		StyleConstants.setForeground(errorStyle, Color.RED);
		// StyleConstants.setFontSize(errorStyle, s);

		quotedStringStyle = sc.addStyle(null, baseStyle);
		StyleConstants.setForeground(quotedStringStyle, new Color(178, 34, 34));// Firebrick
		// StyleConstants.setFontSize(quotedStringStyle, s);

		commentStyle = sc.addStyle(null, baseStyle);
		StyleConstants.setForeground(commentStyle, new Color(46, 139, 87));// Sea
																			// green
		// StyleConstants.setFontSize(commentStyle, s);

	}

	void colourCode() {
		boolean packageRead = false;
		for (int i = 0; i < doc.getLength(); i++)
			try {
				if (!packageRead && doc.getText(i, 8).equals("package ")) {
					packageRead = true;
					i += 8;
					int j = doc.getText(i, doc.getLength() - 9).indexOf(";");
					doc.setCharacterAttributes(i, j, packageNameStyle, false); // Magenta
				}

				if (doc.getText(i, 4).equals("????"))
					doc.setCharacterAttributes(i, 4, errorStyle, false); // Red

				if (doc.getText(i, 10).equals("null.class"))
					doc.setCharacterAttributes(i, 10, errorStyle, false); // Red

				if (doc.getText(i, 1).equals("@")) {
					int j;
					for (j = i; j < doc.getLength(); j++) {
						if (doc.getText(j, 1).equals("\n"))
							break;
					}
					doc.setCharacterAttributes(i, j - i, packageNameStyle,
							false); // Magenta
				}

				if (doc.getText(i, 1).equals("\"")
						&& !(doc.getText(i - 1, 1).equals("/"))) {
					int j = i + 1;
					for (; j < doc.getLength(); j++) {
						if (doc.getText(j, 1).equals("\"") && !(doc
								.getText(j - 1, 1).equals("/")))
							break;
					}
					doc.setCharacterAttributes(i, j - i + 1, quotedStringStyle,
							false); // firebrick
					i = j; // skip quoted string
				}

				if (doc.getText(i, 2).equals("/*")) {
					int j = i + 2;
					for (; j < doc.getLength(); j++) {
						if (doc.getText(j, 2).equals("*/"))
							break;
					}
					doc.setCharacterAttributes(i, j - i + 2, commentStyle,
							false); // green
					i = j + 2; // skip comment
				}
				if (doc.getText(i, 2).equals("//")) {
					int j = i + 2;
					String ls = "\n";
					for (; j < doc.getLength(); j++) {
						if (doc.getText(j, ls.length()).equals(ls))
							break;
						// String x = doc.getText(j, ls.length());
						// x += "";
					}
					doc.setCharacterAttributes(i, j - i + 1, commentStyle,
							false); // green
					i = j + ls.length(); // skip line separator
				}

				if (doc.getText(i, 3).equals("<==")) {
					int j = i + 3;
					for (; j < doc.getLength(); j++) {
						if (doc.getText(j, 1).equals("\n"))
							break;
					}
					doc.setCharacterAttributes(i, j - i, baseStyle, false); // Gray
				}
			 
				if (doc.getText(i, clsName.length()).equals(clsName))
					doc.setCharacterAttributes(i, clsName.length(), packageNameStyle,
							false); // Magenta
				 

			} catch (BadLocationException e) {
			}
		//panel.setFont(driver.fontf);
		//scrollPane.setFont(driver.fontf);
		jf.setFont(driver.fontf);

	}

	public void actionPerformed(ActionEvent e) {
		String s = e.getActionCommand();
		
		if (s.equals("Save As")) {

			saveCode(SAVE_AS);

		} else if (s.equals("Exit")) {
			boolean res = true;
			//if (doc.changed)
				res = askAboutSaving();
			if (res)
				jf.dispose();

		}
		return;
	}

	// askAboutSaving returns true if Code Manager window can be deleted
	
	public boolean askAboutSaving() {
		if (!doc.changed)
			return true;
		int answer = MyOptionPane.showConfirmDialog(driver,
				"Save generated or modified code?", "Save code",
				MyOptionPane.YES_NO_CANCEL_OPTION);

		boolean b;
		if (answer == MyOptionPane.YES_OPTION) {
			// User clicked YES.
			b = saveCode(!SAVE_AS);
			// diag.diagLang = gl;
			return b;
		}

		b = (answer == MyOptionPane.NO_OPTION);
		// diag.diagLang = gl;
		return b;
	}

	/*
	public void changedUpdate(DocumentEvent e) {
		//Document doc = e.getDocument();
		//String s = null;
		//try {
		//	s = doc.getText(e.getOffset(), e.getLength());
		//} catch (BadLocationException e1) {
		//	// TODO Auto-generated catch block
		//	e1.printStackTrace();
		//}
		//if (e.getOffset() > 0)
		//	changed = true;

		if (packageName != null) {
			if (e.getOffset() >= 8 && e.getOffset() <= 8 + packageName.length() 
					&& generated) {
				packageNameChanged = true;
				changed = true;				
				driver.saveProp("currentPackageName", packageName);
			}
		}
		nsLabel.setText(changed ? "Not saved" : " ");
		dialog.repaint();
	}

	public void insertUpdate(DocumentEvent e) {
		if (e.getOffset() > 0)
			changed = true;

		if (packageName != null) {
			if (e.getOffset() >= 8 && e.getOffset() <= 8 + packageName.length()
					&& generated) {
				packageNameChanged = true;
			}
		}
		nsLabel.setText(changed ? "Not saved" : " ");
		dialog.repaint();
	}

	public void removeUpdate(DocumentEvent e) {
		if (e.getOffset() > 0)
			changed = true;

		if (packageName != null) {
			if (e.getOffset() >= 8 && e.getOffset() <= 8 + packageName.length()
					&& generated) {
				packageNameChanged = true;
			}
		}
		nsLabel.setText(changed ? "Not saved" : " ");
		dialog.repaint();
	}
*/
	
	String compress(String s) {
		if (counterList.indexOf(s) == -1)
			counterList.add(s);
		return "X$" + counterList.indexOf(s);
	}

	boolean saveCode(boolean saveType) {

		String fileString = null;
		try {
			fileString = doc.getText(0, doc.getLength());
		} catch (BadLocationException ble) {
			MyOptionPane.showMessageDialog(driver,
					"Couldn't get text from text pane", MyOptionPane.ERROR_MESSAGE);
			// diag.changeCompLang();
			return false;
		}

		String sfn = null;
		File f = diag.diagFile;
		String suggName = null;
		if (f != null)	{
			sfn = f.getAbsolutePath();
			sfn = sfn.replace("\\", "/");
	    	int ix = sfn.lastIndexOf(".drw");
	    	int j = sfn.substring(0, ix).lastIndexOf("/");
	    	suggName = sfn.substring(j, ix);
		}
	 
		String pkg = driver.properties.get("currentPackageName");
		if (pkg == null || pkg.equals("(null)") || pkg.trim().equals(""))
			pkg = "";
		else 
			pkg = pkg.replace(".", "/");
		//String dir = "current" + driver.currLang.label + "NetworkDir";
		String dir = diag.diagLang.netDirProp;
		String fn = driver.properties.get(dir);
		
		if (fn == null)							
			fn = System.getProperty("user.home") + "/src";				
			
		fn = fn.replace("\\",  "/");
		
		
		int i = fn.indexOf("/src");
		if (i > -1)
		    fn = fn.substring(0, i + 4);
		
		
		fn = fn.replace("\\",  "/");
		//int i = fn.lastIndexOf(".drw");
		//int k = fn.substring(0, i).lastIndexOf("/");
		//String suggName = ""; 
		//if (i > k)
		//	suggName = fn.substring(k + 1, i);
		//fn = fn.replace("\\",  "/");
		if (!(fn.endsWith("/")))
			fn += "/";
		if (!(pkg.equals("") ))
			suggName = fn + pkg + "/" + suggName +  "." + gl.suggExtn;			
		else
			suggName = fn + suggName +  "." + gl.suggExtn;	
		
		File file = diag.genSave(null, diag.fCParm[Diagram.NETWORK], fileString, 
		        new File(suggName));
		
		// note: suggName does not have to be a real file!

		// did save work?
		if (file == null) {
			// MyOptionPane.showMessageDialog(driver, "File not saved");
			// diag.changeCompLang();
			return false;
		}
		
		//checkPackage(file, fileString);
		
		String r = "public class ";
		int m = fileString.indexOf(r);
		int n = fileString.indexOf(" extends ");
		
		String oldFN = fileString.substring(m + r.length(), n);	
		
		String p = file.getName();
		//int q = diag.fCParm[Diagram.NETWORK].fileExt.length();
		//p = p.substring(0, p.length() - q);
		int q = file.getName().indexOf(".");
		p = p.substring(0, q);
		
		//doc.addDocumentListener(this);  // to allow late changes!
		
		if (!(p.equals(oldFN))) {
			MyOptionPane.showMessageDialog(driver,
					"File name in generated code doesn't match actual file name - "
							+ p,
					MyOptionPane.WARNING_MESSAGE);

			fileString = fileString.substring(0, m + r.length()) + p
					+ fileString.substring(n);

			int j2 = fileString.indexOf("().go()");
			int j3 = fileString.substring(0, j2).lastIndexOf(" ");
			fileString = fileString.substring(0, j3) + " " + p
					+ fileString.substring(j2);
			try {
				doc.remove(0, doc.getLength());
				doc.insertString(0, fileString, normalStyle);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			colourCode();
			if (!(driver.writeFile(file, fileString))) {
				MyOptionPane.showMessageDialog(driver, "File not saved");
				// diag.changeCompLang();
				return false;
			}

			MyOptionPane.showMessageDialog(driver,
					"File name references changed in " + file.getName()
							+ ", and file saved");
		}
		
		// genCodeFileName = file.getAbsolutePath();
		driver.saveProp(diag.diagLang.netDirProp, file.getParent());
		//saveProperties();
		doc.changed = false;
		jf.repaint();

		if (packageNameChanged) {
			driver.saveProp("currentPackageName", packageName);
			//saveProperties();
		}

		// diag.targetLang = gl.label;
		nsLabel.setText(doc.changed ? "Changed" : "Unchanged ");
		// diag.genCodeFileName = file.getAbsolutePath();
		jf.setTitle("Generated Code: " + file.getName());		
		jf.repaint();

		if (saveType != SAVE_AS)
			jf.dispose();
		return true;
	}

	
	 
	String checkPackage(File file, String fileString){
		//int s = fileString.indexOf("package ");
		//boolean res = false;
		String pkg = findPackage(fileString);
		//if (pkg != null) {
			//int t = fileString.substring(s + 8).indexOf(";");
			//String pkg = fileString.substring(s + 8, s + 8 + t);
			
			String fs = file.getAbsolutePath();
			fs = fs.replace("\\", "/");
			
			// look for src in name; if found, look for networks...; if found do comparison starting beyond that...
			
			int v = fs.indexOf("/src/");
			if (v == -1){
				MyOptionPane.showMessageDialog(driver,
			 			fs + " does not reference a 'src' directory!",
			 			MyOptionPane.WARNING_MESSAGE);
				return null;
			}
			//v += 5;
			int v2 = fs.substring(v).indexOf("networks/");
			if (v2 > -1)
				v += v2 + 9;
			else
				v += 5;
			int w = fs.indexOf(".java");
			int u = fs.substring(0, w).lastIndexOf("/");

			String fNPkg = null;   
			if (v < u) {
				fNPkg = fs.substring(v, u);
				fNPkg = fNPkg.replace('\\', '/');
				fNPkg = fNPkg.replace('/', '.');
			}
			
			fNPkg = String.valueOf(fNPkg);
			pkg = String.valueOf(pkg);
			
			if (!(fNPkg.equals(pkg))) {
				
				int ans = MyOptionPane.showConfirmDialog(
						driver,
						"Package name in file: " + pkg + ",\n"
								+ "   suggested package name based on file name is: "
								+ fNPkg
								+ ", \n   do you want to change to suggested name?",
						"Change package?",
						MyOptionPane.YES_NO_CANCEL_OPTION);

				if (ans != MyOptionPane.CANCEL_OPTION) {
					if (ans == MyOptionPane.YES_OPTION) {
						//fileString = fileString.replace(pkg, "@!@"); 
						// package must be first line
						int i = fileString.indexOf(";");
						int j = fileString.substring(0, i).indexOf("package");
						if (j == -1) {
							fileString = "package " + fNPkg + ";\n" + fileString;
							MyOptionPane.showMessageDialog(driver,
									"Package name added: " + fNPkg);
						}
						else {
							fileString = fileString.replace(pkg, "@!@"); 						
							MyOptionPane.showMessageDialog(driver,
									"Package name changed: " + pkg + " to " + fNPkg);
							pkg = fNPkg;
							fileString = fileString.replace("@!@", pkg);
							
						}
						try {
							doc.remove(0, doc.getLength());
							doc.insertString(0, fileString, normalStyle);
						} catch (BadLocationException ble) {
							MyOptionPane.showMessageDialog(driver,
									"Couldn't insert text into text pane", MyOptionPane.ERROR_MESSAGE);
							return fileString;

						}
						displayDoc(null, gl, fileString);	
						jf.repaint();
					}
					driver.saveProp("currentPackageName", pkg);
					// saveProperties();
				}
				//fileString = fileString.substring(0, s + 8) + pkg
				//		+ fileString.substring(s + 8 + t);
				
				doc.changed = true;
				//res = true;
				
			}
		//}
		return fileString;
	}
	
	
	public JMenuBar createMenuBar() {

		JMenuBar menuBar;

		// Create the menu bar.
		menuBar = new JMenuBar();

		menuBar.setBorderPainted(true);

		menuBar.add(Box.createHorizontalStrut(200));
		JMenuItem menuItem = new JMenuItem("Save As");
		menuBar.add(menuItem);
		menuItem.addActionListener(this);
		menuItem.setBorderPainted(true);
		menuItem = new JMenuItem("Exit");
		menuBar.add(menuItem);
		menuItem.addActionListener(this);
		menuItem.setBorderPainted(true);
		// menuBar.add(box);
		// int w = frame.getWidth();
		menuBar.add(Box.createHorizontalStrut(100));
		JPanel p = new JPanel();
		
		p.add(nsLabel, BorderLayout.LINE_END);
		menuBar.add(p, BorderLayout.LINE_END);
		nsLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		nsLabel.setBackground(Color.WHITE);

		return menuBar;
	}

	String generateJSON() {
		String data;
		data = "{\n\"properties\": {\n\"name\": ";
		data += q(diag.title) + "\n},\n";
		data += "\"processes\": {\n";
		portNames = new HashSet<String>();
		blocklist = new HashMap<String, Integer>();

		String comma = "";
		for (Block block : diag.blocks.values()) {
			// String s = "";
			if (block instanceof ProcessBlock && block.desc == null) {
				MyOptionPane.showMessageDialog(driver,
						"One or more missing block descriptions", MyOptionPane.WARNING_MESSAGE);
				// error = true;
				return null;
			}
			if (block instanceof ExtPortBlock) {
				ExtPortBlock eb = (ExtPortBlock) block;
				String s = "";
				String t;
				if (block.type.equals(Block.Types.EXTPORT_IN_BLOCK)) {
					s = "SUBIN";
					if (eb.substreamSensitive)
						t = "SubInSS";
					else
						t = "SubIn";
				} else
					if (block.type.equals(Block.Types.EXTPORT_OUT_BLOCK)) {
					s = "SUBOUT";
					if (eb.substreamSensitive)
						t = "SubOutSS";
					else
						t = "SubOut";
				} else {
					s = "SUBOI";
					t = "SubOI";
				}
				s = makeUniqueDesc(s); // and make it unique
				
				//if (t.toLowerCase().endsWith(".class"))
				//	t = t.substring(0, t.length() - 6);

				data += comma + q(s) + ":{ \"component\" :" + q(t)
				+ ", \"display\": { \"x\":" + block.cx + ", \"y\":"
				+ block.cy + "}}";
				comma = "\n,";
				//code += "  " + initialize + "(\"" + eb.description + "\", " + component + "(\""
				//		+ s + "\"), " + _port + "(\"NAME\")); \n";
				data += "\"data\":" + q(s) + ",\n";
			//} else
				//data += "\"src\": {\n \"process\" :" + q(fromDesc)
					//	+ ",\n\"port\":" + q(upPort) + "\n},";

			data += "\"tgt\": {\n \"process\" :" + q(s) + ",\n\"port\":"
					+ q(dnPort) + "}\n}";
			comma = "\n,";
		//}
		data += "\n]\n}";
				descArray.put(new Integer(block.id), s);
			}

			if (block instanceof ProcessBlock) {
				String s = cleanDesc(block);
				String t = cleanComp(block);
				data += comma + q(s) + ":{ \"component\" :" + q(t)
						+ ", \"display\": { \"x\":" + block.cx + ", \"y\":"
						+ block.cy + "}}";
				comma = "\n,";

				descArray.put(new Integer(block.id), s);
			}
			// cdescArray.put(new Integer(block.id), block.description);
			if (block instanceof IIPBlock) {
				descArray.put(new Integer(block.id), block.desc);
			}
		}
		data += "\n},\n \"connections\": [\n";
		comma = "";
		for (Arrow arrow : diag.arrows.values()) {
			// generate a connection or initialize
			Block from = diag.blocks.get(new Integer(arrow.fromId));
			Arrow a2 = arrow.findLastArrowInChain();
			Block to = diag.blocks.get(new Integer(a2.toId));
			if (to == null) {
				MyOptionPane.showMessageDialog(driver,
						"Downstream block not found: from " + from.desc, MyOptionPane.ERROR_MESSAGE);
				break;
			}
			if (from == null || to == null || from instanceof FileBlock
					|| from instanceof ReportBlock
					|| from instanceof LegendBlock || to instanceof FileBlock
					|| to instanceof ReportBlock || to instanceof LegendBlock)
				continue;

			if (!getPortNames(arrow))
				return "";
			String fromDesc = descArray.get(new Integer(arrow.fromId));
			// String cFromDesc = cdescArray.get(new Integer(arrow.fromId));

			String toDesc = descArray.get(new Integer(a2.toId));

			jf.repaint();
			if (!(from instanceof ProcessBlock) && !(from instanceof IIPBlock)
					|| !(to instanceof ProcessBlock))
				continue;

			data += comma + "{ ";
			// String upPort = arrow.upStreamPort;
			// String dnPort = a2.downStreamPort;
			if (upPort != null) {
				upPort = upPort.toLowerCase();
				upPort = makeUniquePort(upPort, from);
			}
			dnPort = dnPort.toLowerCase();
			if (a2.dspMod == null)
				a2.dspMod = makeUniquePort(dnPort, to);
			// upPort = arrow.uspMod;
			dnPort = a2.dspMod;
			if (from instanceof IIPBlock) {
				if (!arrow.endsAtLine && checkDupPort(dnPort, to)) {
					String proc = to.desc;
					MyOptionPane.showMessageDialog(driver,
							"Duplicate port name: " + proc + "." + dnPort, MyOptionPane.ERROR_MESSAGE);
					error = true;
				}
				data += "\"data\":" + q(fromDesc) + ",\n";
			} else
				data += "\"src\": {\n \"process\" :" + q(fromDesc)
						+ ",\n\"port\":" + q(upPort) + "\n},";

			data += "\"tgt\": {\n \"process\" :" + q(toDesc) + ",\n\"port\":"
					+ q(dnPort) + "}\n}";
			comma = "\n,";
		}
		data += "\n]\n}";

		return data;
	}

	boolean genFbpCode() {
		String code = "";
		String cma = "";
		// generated = false;
		portNames = new HashSet<String>();
		blocklist = new HashMap<String, Integer>();
		// portlist = new HashMap<String, Integer>();
		// diag.targetLang = "FBP";
		//FileChooserParm saveFCP = diag.fCParm[DrawFBP.NETWORK];
		// gl = diag.diagLang;
		gl = driver.findGLFromLabel("FBP");
		fbpMode = true;
		
		//diag.fCParm[DrawFBP.NETWORK] = diag.fCParm[DrawFBP.FBP];
		//diag.fCParm[DrawFBP.NETWORK].index = DrawFBP.NETWORK; 

		for (Block block : diag.blocks.values()) {
			if (block instanceof ExtPortBlock) {
				ExtPortBlock eb = (ExtPortBlock) block;
				String s = "";
				String t;
				if (block.type.equals(Block.Types.EXTPORT_IN_BLOCK)) {
					s = "SUBIN";
					if (eb.substreamSensitive)
						t = "SubInSS";
					else
						t = "SubIn";
				} else
					if (block.type.equals(Block.Types.EXTPORT_OUT_BLOCK)) {
					s = "SUBOUT";
					if (eb.substreamSensitive)
						t = "SubOutSS";
					else
						t = "SubOut";
				} else {
					s = "SUBOI";
					t = "SubOI";
				}
				s = makeUniqueDesc(s); // and make it unique
				
				//if (t.toLowerCase().endsWith(".class"))
				//	t = t.substring(0, t.length() - 6);

				code += cma + s + "(" + t + ")";
				//cma = "\n,";
		//}
		//code += "\n\n}";
				descArray.put(new Integer(block.id), s);
			}
			if (block instanceof ProcessBlock) {
				if (block.desc == null) {
					MyOptionPane.showMessageDialog(driver,
							"One or more missing block descriptions", MyOptionPane.ERROR_MESSAGE);
					error = true;
					return false;
				}

				String s = cleanDesc(block);
				// String s = cleanComp(block);
				code += cma + s + "(" + s + ")";
				
				descArray.put(new Integer(block.id), s);
				// cdescArray.put(new Integer(block.id), s);
			}
			if (block instanceof IIPBlock) {
				descArray.put(new Integer(block.id), block.desc);
			}
			cma  = ",\n";
		}

		for (Arrow arrow : diag.arrows.values()) {
			Block from = diag.blocks.get(new Integer(arrow.fromId));
			Arrow a2 = arrow.findLastArrowInChain();
			Block to = diag.blocks.get(new Integer(a2.toId));
			if (to == null) {
				MyOptionPane.showMessageDialog(driver,
						"Downstream block not found", MyOptionPane.ERROR_MESSAGE);
				break;
			}
			if (from == null || to == null || from instanceof FileBlock
					|| from instanceof ReportBlock
					|| from instanceof LegendBlock || to instanceof FileBlock
					|| to instanceof ReportBlock || to instanceof LegendBlock)
				continue;

			if (!getPortNames(arrow))
				return false;

			String fromDesc = descArray.get(new Integer(arrow.fromId));
			// String cFromDesc = cdescArray.get(new Integer(arrow.fromId));

			String toDesc = descArray.get(new Integer(a2.toId));
			// String cToDesc = cdescArray.get(new Integer(a2.toId));

			jf.repaint();

			if (!(from instanceof IIPBlock)) {
				upPort = a2.upStreamPort;
				upPort = upPort.replaceAll("-", "\\\\-");
				upPort = upPort.replaceAll("\\.", "\\\\.");
				upPort = makeUniquePort(upPort, from);
			}

			dnPort = a2.downStreamPort;
			// dnPort = dnPort.toLowerCase();

			if (a2.dspMod == null)
				a2.dspMod = makeUniquePort(dnPort, to);

			dnPort = a2.dspMod.replaceAll("-", "\\\\-");
			dnPort = dnPort.replaceAll("\\.", "\\\\.");

			if (from instanceof ProcessBlock
					&& to instanceof ProcessBlock) {
				if (!a2.endsAtLine && checkDupPort(dnPort, to)) {
					String proc = to.desc;
					MyOptionPane.showMessageDialog(driver,
							"Duplicate port name: " + proc + "." + dnPort, MyOptionPane.ERROR_MESSAGE);
					error = true;
				}
				if (checkDupPort(upPort, from)) {
					String proc = from.desc;
					MyOptionPane.showMessageDialog(driver,
							"Duplicate port name: " + proc + "." + upPort, MyOptionPane.ERROR_MESSAGE);
					error = true;
				}

				if (from.multiplex) {

					MyOptionPane.showMessageDialog(driver,
							"Multiplexing not supported", MyOptionPane.ERROR_MESSAGE);
					error = true;
				} else if (to.multiplex) {

					MyOptionPane.showMessageDialog(driver,
							"Multiplexing not supported", MyOptionPane.ERROR_MESSAGE);
					error = true;
				} else

					code += cma + fromDesc + " " + upPort + " -> " + dnPort
							+ " " + toDesc;
			} else
				if (from instanceof IIPBlock && to instanceof ProcessBlock) {
				if (checkDupPort(dnPort, to)) {
					String proc = to.desc;
					MyOptionPane.showMessageDialog(driver,
							"Duplicate port name: " + proc + "." + dnPort, MyOptionPane.ERROR_MESSAGE);
					error = true;
				}
				code += cma + "'" + fromDesc + "' -> " + dnPort + " " + toDesc;
			}

			if (from instanceof ExtPortBlock) {
				code += cma + fromDesc + " OUT -> " + dnPort + " " + toDesc;
				code +=  "\n";
				code += cma + "'" + from.desc + "' -> NAME " + fromDesc; 

			} else if (to instanceof ExtPortBlock) {
				code += cma + fromDesc + " " + upPort + " -> IN " + toDesc;
				code +=  "\n";
				code += cma + "'" + to.desc + "' -> NAME " + toDesc;
			}
			cma = ", ";
			code += "\n";
		}
		
		// insert string data
		try {
			doc.insertString(doc.getLength(), code, baseStyle);

		} catch (BadLocationException ble) {
			MyOptionPane.showMessageDialog(driver,
					"Couldn't insert text into text pane", MyOptionPane.ERROR_MESSAGE);
			// restore old language parameters
			
			return false;
		}

		doc.changed = true;
		// colourCode();

		generated = true;

		//nsLabel.setText(doc.changed ? "Changed" : " ");

		jf.repaint();
		// jframe.update(jdriver.osg);

		

		return true;

	}
	
	/*********************
	 * 
	 * The idea here is that, if the string already has double backslashes, 
	 * then the designer will be aware of the Java convention for showing 
	 * backslashes;  if not, then we will convert single backslashes to double
	 *
	 */

	String q(String s) {
		String t = "\"" + s + "\"";
		if (-1 == t.indexOf("\\\\"))
				t = t.replace("\\",  "\\\\");
		return t;
	}

	String cleanComp(Block b) {

		error = false;
		String c = b.fullClassName;
		if (c == null) {
			c = b.codeFileName;
			if (c == null) {
				MyOptionPane.showMessageDialog(driver,
						"Missing full class name for: " + b.desc, MyOptionPane.ERROR_MESSAGE);
			 
				error = true;
			}
		}
		if (!error) {
			
			int i = c.indexOf("!");
			if (i > -1 && i < c.length() - 1) {				
					c = c.substring(i + 1);				
			}
			
		}

		return c;
	}

	String cleanDesc(Block b) {

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

		return makeUniqueDesc(t); // and make it unique

	}

	boolean getPortNames(Arrow arrow) {
		Block from = diag.blocks.get(new Integer(arrow.fromId));
		Arrow a2 = arrow.findLastArrowInChain();
		Block to = diag.blocks.get(new Integer(a2.toId));
		String z;

		if (from instanceof ProcessBlock) {
			upPort = arrow.upStreamPort;
			while (true) {
				if (upPort != null && !(upPort.equals(""))) {
					z = validatePortName(upPort);
					if (z != null) {
						upPort = z;
						break;
					}
					if (MyOptionPane.NO_OPTION == MyOptionPane.showConfirmDialog(
							driver, "Invalid port name: " + upPort,
							"Invalid output port name - try again?",
							MyOptionPane.YES_NO_OPTION)) {
						// ok = false;
						upPort = "????";
						break;
					}
				}
				
				upPort = (String) MyOptionPane.showInputDialog(driver,
						"Output port from " + "\"" + from.desc + "\"",
						"Please enter port name");
				//if (upPort == null)
				//	return false;
				diag.changed = true;
				break;

			}

			arrow.upStreamPort = upPort;

		}

		dnPort = a2.downStreamPort;
		while (true) {
			if (dnPort != null && !(dnPort.equals(""))) {
				z = validatePortName(dnPort);
				if (z != null) {
					dnPort = z;
					break;
				}
				if (MyOptionPane.NO_OPTION == MyOptionPane.showConfirmDialog(
						driver, "Invalid port name: " + dnPort,
						"Invalid input port name - try again?",
						MyOptionPane.YES_NO_OPTION)) {
					// ok = false;
					dnPort = "????";
					break;
				}
			}
			dnPort = (String) MyOptionPane.showInputDialog(driver,
					"Input port to " + "\"" + to.desc + "\"",
					"Please enter port name");
			//if (dnPort == null)
			//	return false;
			diag.changed = true;
			break;

		}

		a2.downStreamPort = dnPort;

		return true;
	}

	String validatePortName(String s) {
		if (s == null || s.equals("") || s.equals("????"))
			return null;
		if (s.equals("*")) 
			return s;
		Pattern p = Pattern.compile("[a-zA-Z][\\d\\-\\_\\.\\[\\]a-zA-Z]*"); // Allow
																			// hyphen
																			// (for
																			// Humberto),
																			// period
																			// (for
																			// Tom),
																			// underscore
		// and square brackets
		Matcher ma = p.matcher(s);
		if (!ma.matches())
			return null;
		else
			return s;
	}
	
	public class MyDocListener1 implements DocumentListener {
		
		// used by displayDoc only

		//@Override
		public void insertUpdate(DocumentEvent e) {
			if (e.getOffset() > 0 || e.getLength() < doc.getLength())
				doc.changed = true;
			
		}
		//@Override
		public void removeUpdate(DocumentEvent e) {
			if (e.getOffset() > 0 || e.getLength() < doc.getLength())
				doc.changed = true;

			
		}
		//@Override
		public void changedUpdate(DocumentEvent e) {
				//doc.changed = true; 
					
		}

		
	}
	
	
	
}
