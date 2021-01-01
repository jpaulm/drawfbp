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

public class CodeManager implements ActionListener {

	DrawFBP driver;
	HashSet<String> portNames;
	HashMap<String, Integer> blocklist;
	// HashMap<String, Integer> portlist;
	Style baseStyle, normalStyle, packageNameStyle, errorStyle,
			quotedStringStyle, commentStyle;
	
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
	JTextPane lineNos = null;	
	//boolean completeChange;
    //final boolean SAVEAS = true;
    CloseAction closeAction = null;
    
	File file = null;
	boolean create = true;
	StyleContext sc = null;

	CodeManager(Diagram d, boolean create) {
		this.create = create;
		diag = d;
		gl = diag.diagLang;
		driver = d.driver;
		d.cm = this;
		closeAction = new CloseAction();
		
		
		
		sc = new StyleContext();	
		//doc = new DefaultStyledDocument(); 
		doc = new MyDocument(sc); 
		//docText = new JTextPane(doc);
		//doc = (MyDocument) docText.getStyledDocument();
		//addStylesToDocument(doc);
		setStyles(sc);
		
	}
	
	//void genCode() {
	//	if (!generateCode()) {
	//		//driver.jf.dispose();
	//	}
	//}

	boolean genCode() {

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
		
		/*
		String fn = diag.diagFile == null ? "unknown" : diag.diagFile.getName();		
			
		driver.jf.setTitle("Generated Code for " + fn);		

		driver.jf.setJMenuBar(createMenuBar());		

		BufferedImage image = driver.loadImage("DrawFBP-logo-small.png");
		driver.jf.setIconImage(image);
		
		JFrame.setDefaultLookAndFeelDecorated(true);
		*/

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
		}
		/*
			if (packageName == null || packageName.equals("") || packageName.equals("null")
					|| packageName.equals("(null)")) {
				packageName = (String) MyOptionPane.showInputDialog(driver.jf,
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
		
		*/
		
		String[] contents;
		if (gl.label.equals("JSON")) {
			contents = new String[1];
			contents[0] = generateJSON();
			styles[0] = normalStyle;
		} else {
			contents = new String[20];  
			int k = 0;
			if (gl.label.equals("Java")) {
				//if (packageName != null) {
					contents[0] = "package ";
					contents[1] = packageName;
					contents[2] = ";  //change package name, or delete statement, if desired\n"; 
					k = 3;
				//}
				//else {
				//	contents[0] = "package ";
				//	contents[1] = "(null) ;";
				//	contents[2] = " //change package name, or delete statement, if desired\n ";
				//	k = 3;
				//}
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

					String s = driver.cleanDesc(block, false);
					s = makeUniqueDesc(s);
					
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
					
					 
					
					descArray.put(Integer.valueOf(block.id), s);

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

						driver.repaint();
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
					descArray.put(Integer.valueOf(block.id), s);
				}

				if (block instanceof IIPBlock)
					descArray.put(Integer.valueOf(block.id), block.desc);
				
			}

			for (Arrow arrow : diag.arrows.values()) {
				// generate a connection or initialize
				if (arrow.toX == -1  || arrow.toId == -1)               // if toX or toId = -1, do not generate
					continue;
				Block from = diag.blocks.get(Integer.valueOf(arrow.fromId));
				Arrow a2 = arrow.findLastArrowInChain();
				if (a2 == null)
					continue;
				Block to = diag.blocks.get(Integer.valueOf(a2.toId));
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

				//driver.jf.jf.repaint();

				String fromDesc = descArray.get(Integer.valueOf(arrow.fromId));
				// String cFromDesc = cdescArray.get(new Integer(arrow.fromId));

				String toDesc = descArray.get(Integer.valueOf(a2.toId));
				// String cToDesc = cdescArray.get(new Integer(a2.toId));

				// boolean ok;

				// }

				//driver.jf.repaint();
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
		nsLabel.repaint();

		//displayDoc(null, gl, null);
		try {
			displayDoc(null, gl, doc.getText(0,  doc.getLength()));
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		driver.jf.repaint();
		// driver.jframe.update(jdriver.osg);

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
	
	

	String displayDoc(File filex, GenLang gl, String fileString) {
		
		JFrame.setDefaultLookAndFeelDecorated(true);
		
		driver.jf = new JFrame();
		
		driver.jf.setFont(driver.fontf);

		driver.jf.setTitle("Generated Code for " + diag.diagFile);		

		driver.jf.setJMenuBar(createMenuBar());		

		BufferedImage image = driver.loadImage("DrawFBP-logo-small.png");
		driver.jf.setIconImage(image);
		
		file = filex;
		//String fileString = null;
		
		
		
		if (file != null) {
			//file = diag.diagFile;
			clsName = file.getName();
			int i = clsName.lastIndexOf(".");
			if (i > -1)
				clsName = clsName.substring(0, i);
			driver.jf.setTitle("Displayed Code: " + file.getName());
		} else
			driver.jf.setTitle("Displayed Code: " + clsName + "." + gl.suggExtn);

		
		//driver.depDialog = driver.jf;
		nsLabel.setFont(driver.fontg);
		//completeChange = false;
		
		DrawFBP.applyOrientation(driver.jf);

		//dialog.setAlwaysOnTop(false);
		
		driver.jf.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		driver.jf.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				closeAction.actionPerformed(new ActionEvent(ev, 0, "CLOSE"));
			}

		});
		
		driver.jf.setJMenuBar(createMenuBar());
		driver.jf.repaint();

		image = driver.loadImage("DrawFBP-logo-small.png");
		driver.jf.setIconImage(image);

		Point p = driver.getLocation();
		Dimension dim = driver.getSize();
		driver.jf.setPreferredSize(new Dimension(dim.width - 100, dim.height - 50));
		driver.jf.setLocation(p.x + 100, p.y + 50);
		driver.jf.setForeground(Color.WHITE);
		
				
		panel = new JPanel();
		scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED); 
		driver.jf.add(scrollPane);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		scrollPane.setViewportView(panel);
		//scrollPane.add(panel);
		
		lineNos = new JTextPane();		
		docText = new JTextPane();
		 
		panel.add(lineNos);
		panel.add(Box.createHorizontalStrut(20));
		panel.add(docText);	
		
		MyDocument doc2 = new MyDocument(sc);
		lineNos.setStyledDocument(doc2); 		
		
		FontMetrics metrics = driver.osg.getFontMetrics(driver.fontf);
		String str = "        ";  // 8 blanks
		byte[] str2 = str.getBytes();
		int xx = 2 + metrics.bytesWidth(str2, 0, str.length());
		
		lineNos.setPreferredSize(new Dimension(80, xx + 2));
		lineNos.setMinimumSize(new Dimension(60, xx));  
		lineNos.setMaximumSize(new Dimension(100, Short.MAX_VALUE));
		lineNos.setBackground(DrawFBP.lb);
		lineNos.setEditable(false);		
				
		nsLabel.setText(doc.changed ? "Changed" : "Unchanged ");
		//nsLabel.paint(driver.jf.getGraphics());
		nsLabel.repaint();
		
		if (file != null) {
			
			if (fileString == null) {
				fileString = driver.readFile(file /* , !saveType */);
				if (fileString == null || fileString.equals("")) {
					MyOptionPane.showMessageDialog(driver, "Couldn't read file: " + file.getAbsolutePath(),
							MyOptionPane.ERROR_MESSAGE);
					return null;
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
					packageName = driver.getPackageFromCode(fileString);
					if (null == packageName) {

						packageName = (String) MyOptionPane.showInputDialog(driver,
								"Missing package name - please specify package name, if desired", null);

						if (packageName != null) {
							fileString = "package " + packageName + ";\n" + fileString;
							doc.changed = true;
						}
					}

					driver.saveProp("currentPackageName", packageName);
					driver.saveProperties();

				}
			}

			// completeChange = true;
			try {
				doc.remove(0, doc.getLength());
				doc.insertString(0, fileString, normalStyle);
			} catch (BadLocationException ble) {
				MyOptionPane.showMessageDialog(driver.jf, "Couldn't insert text into text pane",
						MyOptionPane.ERROR_MESSAGE);
				return fileString;

			}
		}

		colourCode();

		// count lines
		
		int lines = 0;
		for (int j = 0; j < fileString.length(); j++) {
			if (fileString.charAt(j) == '\n') {
				lines++;
			}
		}
		
		
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED); 
	
	
		for (int j = 1; j < lines + 1; j++) {
			String num = String.format("%8s", j) + "\n";
			try {				
				doc2.insertString(doc2.getLength(), num, baseStyle);				
			} catch (BadLocationException ble) {
				System.err.println("Couldn't insert initial text into text pane.");
			}
		}
		
		//lineNos.setForeground(Color.BLACK);
		
		docText.setStyledDocument(doc);  
	
		docText.setVisible(true);
		
		scrollPane.setViewportView(panel);
		
		driver.jf.pack();
		driver.jf.setVisible(true);
		
		driver.jf.repaint();

		return fileString;

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
			i = Integer.valueOf(i.intValue() + 1);
			t = s + "_" + i.toString() + "_";
		} else
			i = Integer.valueOf(1);
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
			i = Integer.valueOf(i.intValue() + 1);
			t = s + "_" + i.toString() + "_";
		} else
			i = Integer.valueOf(1);
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

		//Font font = driver.fontf;
		// baseStyle = new SimpleAttributeSet();
		Style defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
		baseStyle = sc.addStyle(null, defaultStyle);
		int s = driver.fontf.getSize();

		//StyleConstants.setSpaceAbove(baseStyle, 4);
		//StyleConstants.setSpaceBelow(baseStyle, 4);
		StyleConstants.setForeground(baseStyle, Color.DARK_GRAY);
		//StyleConstants.setFontFamily(baseStyle, font.getFamily());
		StyleConstants.setFontFamily(baseStyle, driver.fontf.getName());		
		StyleConstants.setFontSize(baseStyle, s);
		//StyleConstants.setLineSpacing(baseStyle, 4.0f);


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
		
	}

	public void actionPerformed(ActionEvent e) {
		String s = e.getActionCommand();
		
		if (s.equals("Save")) {

			saveCode(!SAVE_AS);   
		
		} else if (s.equals("Save As")) {

			saveCode(SAVE_AS);

		} else if (s.equals("Exit")) {			
			closeAction.actionPerformed(new ActionEvent(e, 0, "CLOSE"));			
		}
		return;
	}

		
	//public boolean askAboutSaving() {
				
		
	//}

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

		// saveType = true = SAVE_AS
		
		String fileString = null;
		try {
			fileString = doc.getText(0, doc.getLength());
		} catch (BadLocationException ble) {
			MyOptionPane.showMessageDialog(driver.jf,
					"Save Code: Couldn't get text from text pane", MyOptionPane.ERROR_MESSAGE);
			// diag.changeCompLang();
			return false;
		}

		try {
		String sfn = null;
		File f = diag.diagFile;
		String suggName = null;
		if (f != null)	{
			sfn = f.getAbsolutePath();
			//System.out.println("File name for diagram: " + sfn);
			sfn = sfn.replace("\\", "/");
	    	int ix = sfn.lastIndexOf(".drw");
	    	if (ix == -1)
	    		System.out.println("File name for diagram missing '.drw' suffix: " + sfn);
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
		else
			fn = System.getProperty("user.home") + "/src";	
		
		
		fn = fn.replace("\\",  "/");
		if (!(fn.endsWith("/")))
			fn += "/";
		if (!(pkg.equals("") ))
			suggName = fn + pkg + "/" + suggName +  "." + gl.suggExtn;			
		else
			suggName = fn + suggName +  "." + gl.suggExtn;	
		
		File x = null;
		if (!saveType)
			x = file;
			
		File file = diag.genSave(x, diag.fCParm[Diagram.NETWORK], fileString, 
		        new File(suggName), driver.jf);
		
		
		// note: suggName does not have to be a real file!

		// did save work?
		if (file == null) {
			// MyOptionPane.showMessageDialog(driver, "File not saved");
			// diag.changeCompLang();
			return false;
		}
		fileString = checkMain(file, fileString);
		fileString = checkPackage(file, fileString);
		
		//if(packageNameChanged) {
		
		
			try {
				doc.remove(0, doc.getLength());
				doc.insertString(0, fileString, normalStyle);
			} catch (BadLocationException e) {
				MyOptionPane.showMessageDialog(driver.jf,
						"Save Code: Couldn't insert text into text pane", MyOptionPane.ERROR_MESSAGE);
				// diag.changeCompLang();
				return false;
			}

			colourCode();
			if (!(driver.writeFile(file, fileString))) {
				MyOptionPane.showMessageDialog(driver.jf, "File not saved");
				// diag.changeCompLang();
				return false;
			}

			MyOptionPane.showMessageDialog(driver.jf,
					"File " + file.getName() + " (re)saved");
			
			docText.repaint();
						
		//}
		
		// genCodeFileName = file.getAbsolutePath();
		driver.saveProp(diag.diagLang.netDirProp, file.getParent());
		//saveProperties();
		doc.changed = false;
		driver.jf.repaint();

		if (packageNameChanged) {
			driver.saveProp("currentPackageName", packageName);
			//saveProperties();
		}

		// diag.targetLang = gl.label;
		nsLabel.setText(doc.changed ? "Changed" : "Unchanged ");
		nsLabel.repaint();
		// diag.genCodeFileName = file.getAbsolutePath();
		driver.jf.setTitle("Generated Code: " + file.getName());		
		driver.jf.repaint();

		//if (saveType != SAVE_AS)
		//	driver.jf.dispose();
		return true;
		
		} catch(Exception e ) {
			e.printStackTrace();  
			return false;
		}
	}
	
	String checkMain(File file, String fileString) {
		String f = file.getName();
		int i = f.lastIndexOf(".");
		String ff = f.substring(0, i);
		int j = fileString.indexOf("public class ");
		int k = fileString.lastIndexOf(" extends");
		fileString = fileString.substring(0, j + 13) + ff + fileString.substring(k);
		
		k = fileString.lastIndexOf("().go()");		
		if (k > -1) {
			j = fileString.lastIndexOf("new ");
			fileString = fileString.substring(0, j + 4) + ff + fileString.substring(k);
		}
		return fileString;
	}

	/*      
	 *  Output of method is (perhaps modified) fileString     
	 */
	 
	String checkPackage(File file, String fileString) {
		
		String pkg = driver.getPackageFromCode(fileString);
		
		String fs = file.getAbsolutePath();
		fs = fs.replace("\\", "/");

		int v = fs.indexOf("/src/");
		if (v == -1) {
			MyOptionPane.showMessageDialog(driver.jf, fs + " does not reference a 'src' directory!",
					MyOptionPane.WARNING_MESSAGE);
			return null;
		}
		
		String s = fs.substring(v + 5);
		
		v = s.indexOf("main/java/");
		
		if (v > -1)
			s = s.substring(v + 10);
		
		int w = s.lastIndexOf("/");		
		s = s.substring(0, w);
		s = s.replace("/",  ".");   // package deduced from file name
		
		String fNPkg = s;
		if (!fNPkg.equals(pkg)) {
 	

				int ans = MyOptionPane.showConfirmDialog(driver.jf,
						"Package name in file: " + pkg + "\n" + "   does not match package name based on file name:\n" +
				             fNPkg	+ ", \n   do you want to change it?",
						"Change package name?", MyOptionPane.YES_NO_CANCEL_OPTION);

				if (ans != MyOptionPane.CANCEL_OPTION) {
					if (ans == MyOptionPane.YES_OPTION) {
						// fileString = fileString.replace(pkg, "@!@");
						// package must be first line
						int i = fileString.indexOf(";");
						int j = fileString.substring(0, i).indexOf("package");
						if (j == -1) {
							fileString = "package " + fNPkg + ";\n" + fileString;
							MyOptionPane.showMessageDialog(driver.jf, "Package name added: " + fNPkg);
						} else {
							if (pkg == null) {								
								String t = "package " + fNPkg + ";\n"; 
								int k = fileString.substring(j + 7).indexOf("\n"); 
								fileString = t + fileString.substring(j + k + 8);
							}
							else {
								fileString = fileString.replace(pkg, "@!@");							
								//pkg = fNPkg;
								fileString = fileString.replace("@!@", fNPkg);
							}
							MyOptionPane.showMessageDialog(driver.jf, "Package name changed: " + pkg + " to " + fNPkg);
							driver.jf.setVisible(false);
							//driver.jf = null;
							//driver.writeFile(file,  fileString);  done in saveCode
							//displayDoc(file, gl, fileString);
							
							packageNameChanged = true;
							doc.changed = false;  // don't want to save again!
						}
						/*
						try {
							//System.out.println(doc.getText(0, doc.getLength()));
						
							doc.remove(0, doc.getLength());
							doc.insertString(0, fileString, normalStyle);
						} catch (BadLocationException ble) {
							MyOptionPane.showMessageDialog(driver.jf, "Couldn't insert text into text pane",
									MyOptionPane.ERROR_MESSAGE);
							return fileString;

						}
						*/
						//docText.setText(doc); 
						//driver.jf.dispose();
						displayDoc(null, gl, fileString);
						//driver.writeFile(file,  fileString);
						//driver.saveProp("currentPackageName", pkg);
						doc.changed = false; 
						driver.jf.repaint();
					}
					
					// saveProperties();
				}
				// fileString = fileString.substring(0, s + 8) + pkg
				// + fileString.substring(s + 8 + t);

				
				// res = true;

			 
		}
		
		driver.saveProp("currentPackageName", fNPkg) ;
		
		return fileString;
	}
	
	
	public JMenuBar createMenuBar() {

		JMenuBar menuBar;

		// Create the menu bar.
		menuBar = new JMenuBar();

		menuBar.setBorderPainted(true);

		menuBar.add(Box.createHorizontalStrut(200));
		JMenuItem menuItem = null;
		if (!create) {
			menuItem = new JMenuItem("Save");
		menuBar.add(menuItem);
		menuItem.addActionListener(this);
		menuItem.setBorderPainted(true);
		}
		menuItem = new JMenuItem("Save As");
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
				MyOptionPane.showMessageDialog(driver.jf,
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
				descArray.put(Integer.valueOf(block.id), s);
			}

			if (block instanceof ProcessBlock) {
				String s = driver.cleanDesc(block, false);
				s = makeUniqueDesc(s);
				String t = cleanComp(block);
				data += comma + q(s) + ":{ \"component\" :" + q(t)
				//		+ ", \"display\": { \"x\":" + block.cx + ", \"y\":"
				//		+ block.cy + "}"
						;
				data += "}";
				comma = "\n,";

				descArray.put(Integer.valueOf(block.id), s);
			}
			// cdescArray.put(new Integer(block.id), block.description);
			if (block instanceof IIPBlock) {
				descArray.put(Integer.valueOf(block.id), block.desc);
			}
		}
		data += "\n},\n \"connections\": [\n";
		comma = "";
		for (Arrow arrow : diag.arrows.values()) {
			// generate a connection or initialize
			Block from = diag.blocks.get(Integer.valueOf(arrow.fromId));
			Arrow a2 = arrow.findLastArrowInChain();
			Block to = diag.blocks.get(Integer.valueOf(a2.toId));
			if (to == null) {
				MyOptionPane.showMessageDialog(driver.jf,
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
			String fromDesc = descArray.get(Integer.valueOf(arrow.fromId));
			// String cFromDesc = cdescArray.get(new Integer(arrow.fromId));

			String toDesc = descArray.get(Integer.valueOf(a2.toId));

			//driver.jf.repaint();
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
					MyOptionPane.showMessageDialog(driver.jf,
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
				descArray.put(Integer.valueOf(block.id), s);
			}
			if (block instanceof ProcessBlock) {
				if (block.desc == null) {
					MyOptionPane.showMessageDialog(driver.jf,
							"One or more missing block descriptions", MyOptionPane.ERROR_MESSAGE);
					error = true;
					return false;
				}

				String s = driver.cleanDesc(block, true);
				s = makeUniqueDesc(s);
				// String s = cleanComp(block);
				code += cma + s + "(" + s + ")";
				
				descArray.put(Integer.valueOf(block.id), s);
				// cdescArray.put(new Integer(block.id), s);
			}
			if (block instanceof IIPBlock) {
				descArray.put(Integer.valueOf(block.id), block.desc);
			}
			cma  = ",\n";
		}

		for (Arrow arrow : diag.arrows.values()) {
			Block from = diag.blocks.get(Integer.valueOf(arrow.fromId));
			Arrow a2 = arrow.findLastArrowInChain();
			Block to = diag.blocks.get(Integer.valueOf(a2.toId));
			if (to == null) {
				MyOptionPane.showMessageDialog(driver.jf,
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

			String fromDesc = descArray.get(Integer.valueOf(arrow.fromId));
			// String cFromDesc = cdescArray.get(new Integer(arrow.fromId));

			String toDesc = descArray.get(Integer.valueOf(a2.toId));
			// String cToDesc = cdescArray.get(new Integer(a2.toId));

			driver.jf.repaint();

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
					MyOptionPane.showMessageDialog(driver.jf,
							"Duplicate port name: " + proc + "." + dnPort, MyOptionPane.ERROR_MESSAGE);
					error = true;
				}
				if (checkDupPort(upPort, from)) {
					String proc = from.desc;
					MyOptionPane.showMessageDialog(driver.jf,
							"Duplicate port name: " + proc + "." + upPort, MyOptionPane.ERROR_MESSAGE);
					error = true;
				}

				if (from.multiplex) {

					MyOptionPane.showMessageDialog(driver.jf,
							"Multiplexing not supported", MyOptionPane.ERROR_MESSAGE);
					error = true;
				} else if (to.multiplex) {

					MyOptionPane.showMessageDialog(driver.jf,
							"Multiplexing not supported", MyOptionPane.ERROR_MESSAGE);
					error = true;
				} else

					code += cma + fromDesc + " " + upPort + " -> " + dnPort
							+ " " + toDesc;
			} else
				if (from instanceof IIPBlock && to instanceof ProcessBlock) {
				if (checkDupPort(dnPort, to)) {
					String proc = to.desc;
					MyOptionPane.showMessageDialog(driver.jf,
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
			MyOptionPane.showMessageDialog(driver.jf,
					"Couldn't insert text into text pane", MyOptionPane.ERROR_MESSAGE);
			// restore old language parameters
			
			return false;
		}

		doc.changed = true;
		// colourCode();

		generated = true;

		//nsLabel.setText(doc.changed ? "Changed" : " ");

		driver.jf.repaint();
		// driver.jframe.update(jdriver.osg);

		

		return true;

	}
	
	/**
	 * Convert back slashes to double; double quotes to \"
	 */

	String q(String s) {
		if (s == null)
			return "";
		String t = s.replace("\\",  "\\\\");
		t = t.replace("\"", "\\\"");
		t = "\"" + t + "\"";
		//if (-1 == t.indexOf("\\\\"))
		
		return t;
	}

	String cleanComp(Block b) {

		error = false;
		String c = b.fullClassName;
		if (c == null) {
			c = b.codeFileName;
			if (c == null) {
				MyOptionPane.showMessageDialog(driver.jf,
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

	/*
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

	*/
	boolean getPortNames(Arrow arrow) {
		Block from = diag.blocks.get(Integer.valueOf(arrow.fromId));
		Arrow a2 = arrow.findLastArrowInChain();
		Block to = diag.blocks.get(Integer.valueOf(a2.toId));
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
							driver.jf, "Invalid port name: " + upPort,
							"Invalid output port name - try again?",
							MyOptionPane.YES_NO_OPTION)) {
						// ok = false;
						upPort = "????";
						break;
					}
				}
				
				upPort = (String) MyOptionPane.showInputDialog(driver.jf,
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
						driver.jf, "Invalid port name: " + dnPort,
						"Invalid input port name - try again?",
						MyOptionPane.YES_NO_OPTION)) {
					// ok = false;
					dnPort = "????";
					break;
				}
			}
			dnPort = (String) MyOptionPane.showInputDialog(driver.jf,
					"Input port to " + "\"" + to.desc + "\"",
					"Please enter port name for arrow from " + from.desc);
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
			if (e.getOffset() > 0 || e.getLength() < doc.getLength()) {
				if (create)
					doc.changed = true;
				nsLabel.setText(doc.changed ? "Changed" : "Unchanged ");
				nsLabel.repaint();
				driver.jf.repaint();
			}
			
		}
		//@Override
		public void removeUpdate(DocumentEvent e) {
			if (e.getOffset() > 0 || e.getLength() < doc.getLength()) {
				if (create)
					doc.changed = true;
				nsLabel.setText(doc.changed ? "Changed" : "Unchanged ");
				nsLabel.repaint();
				driver.jf.repaint();
			}

			
		}
		//@Override
		public void changedUpdate(DocumentEvent e) {
			if (create)
				doc.changed = true; 
			nsLabel.setText(doc.changed ? "Changed" : "Unchanged ");
			nsLabel.repaint();
			driver.jf.repaint();					
		}

		
	}

	
	public class CloseAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {

			if (!doc.changed) {
				driver.jf.dispose();
				return;
			}

			int answer = MyOptionPane.showConfirmDialog(driver.jf, "Save generated or modified code?", 
					"Save code", MyOptionPane.YES_NO_CANCEL_OPTION);

			if (answer == MyOptionPane.NO_OPTION) {
				doc.changed = false;
				driver.jf.setVisible(false);
				driver.jf.dispose();
				
			} else if (answer != MyOptionPane.CANCEL_OPTION) {  // i.e. YES
				doc.changed = false;
				saveCode(false);
				//if (doc.changed)
				//	return;

				//driver.jf.dispose();
			}
		}
	}

}
