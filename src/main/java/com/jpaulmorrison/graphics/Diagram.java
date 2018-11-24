package com.jpaulmorrison.graphics;


import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import javax.swing.JPopupMenu;

import com.jpaulmorrison.graphics.DrawFBP.FileChooserParms;
import com.jpaulmorrison.graphics.DrawFBP.GenLang;

public class Diagram {
	// LinkedList<Block> blocks;
	ConcurrentHashMap<Integer, Block> blocks;

	ConcurrentHashMap<Integer, Arrow> arrows;

	File diagFile;

	DrawFBP driver;
	int tabNum = -1;
	DrawFBP.SelectionArea area; 

	String title;

	String desc;

	GenLang diagLang;

	boolean changed = false;

	Arrow currentArrow = null;

	int maxBlockNo = 0;

	int maxArrowNo = 0;

	int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;

	int maxX = 0, maxY = 0;

	Block foundBlock;

	Arrow foundArrow;

	boolean clickToGrid;

	int xa, ya;
	
	Block parent = null;

	// File imageFile = null;

	//boolean findArrowCrossing = false;
	//Enclosure cEncl = null;
	

	//String genCodeFileName;
	
	JPopupMenu jpm;
	//String targetLang;
	
	FileChooserParms[] fCPArr = new FileChooserParms[7];
	String[] filterOptions = {"", "All (*.*)"};
	
	
	Diagram(DrawFBP drawFBP) {
		driver = drawFBP;
		driver.curDiag = this;
		blocks = new ConcurrentHashMap<Integer, Block>();
		arrows = new ConcurrentHashMap<Integer, Arrow>();
		clickToGrid = true;
		parent = null;
		driver.grid.setSelected(clickToGrid);
		//file = null;
		diagLang = driver.defaultCompLang;
		for (int i = 0; i < fCPArr.length; i++){
			fCPArr[i] = driver.fCPArray[i];
		}
		
		fCPArr[DrawFBP.CLASS] = driver.new FileChooserParms("Class", "currentClassDir",
				"Select component from class directory", ".class",
				driver.new JavaClassFilter(), "Class files");
		
		fCPArr[DrawFBP.PROCESS] = driver.new FileChooserParms("Process", diagLang.srcDirProp, "Select "
				+ diagLang.showLangs() + " component from directory",
				diagLang.suggExtn, diagLang.filter, "Components: "
						+ diagLang.showLangs() + " " + diagLang.showSuffixes());
		
		fCPArr[DrawFBP.GENCODE] = driver.new FileChooserParms("Generated code",
				diagLang.netDirProp,
				"Specify file name for generated code",
				"." + diagLang.suggExtn, diagLang.filter,
				diagLang.showLangs());		
				
	}

	public File open(File f) {
		File file = null;  
		String fileString = null;

		if (f != null) 
			file = f;
		
		if (f == null || f.isDirectory()) {		
			String s = driver.properties.get("currentDiagramDir");
			if (s == null)
				s = System.getProperty("user.home");
			File f2 = new File(s);
			if (!f2.exists()) {
				MyOptionPane.showMessageDialog(driver.frame, "Directory '" + s
						+ "' does not exist - reselect", MyOptionPane.ERROR_MESSAGE);
				// return null;
				f2 = new File(".");
			}

			MyFileChooser fc = new MyFileChooser(f2, fCPArr[DrawFBP.DIAGRAM]);

			int returnVal = fc.showOpenDialog();

			if (returnVal == MyFileChooser.APPROVE_OPTION)  
				file = new File(driver.getSelFile(fc));
			if (file == null)
				return null; 
		}
		
		if (file.isDirectory()) {
			MyOptionPane.showMessageDialog(driver.frame,
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
			//if (file.getName().equals("(empty folder)"))
			//	MyOptionPane.showMessageDialog(driver.frame,
			//			"Invalid file name: " + file.getAbsolutePath());
			//else 
			//	MyOptionPane.showMessageDialog(driver.frame,
			//			"File does not exist: " + file.getAbsolutePath());

			//return null;
		//}
		if (null == (fileString = readFile(file, false))) {
			MyOptionPane.showMessageDialog(driver.frame, "Unable to read file: "
					+ file.getName(), MyOptionPane.ERROR_MESSAGE);
			return null;
		}
		 
		File currentDiagramDir = file.getParentFile();
		driver.properties.put("currentDiagramDir",
				currentDiagramDir.getAbsolutePath());
		driver.propertiesChanged = true;

		//if (!(file.getName().toLowerCase().endsWith(".drw"))) {
		int i = diagramIsOpen(file.getAbsolutePath());
		if (-1 != i) {
			ButtonTabComponent b = (ButtonTabComponent) driver.jtp
					.getTabComponentAt(i);
			//driver.curDiag = b.diag;
			driver.jtp.setSelectedIndex(i);
			diagFile = b.diag.diagFile;
			return file;
		}

		title = file.getName();
		if (title.toLowerCase().endsWith(".drw"))
			title = title.substring(0, title.length() - 4);
		//if (diagramIsOpen(file.getAbsolutePath()))
		//	return null;
		diagFile = file;
		blocks.clear();
		arrows.clear();
		desc = " ";

		DiagramBuilder.buildDiag(fileString, driver.frame, this);
		// driver.jtp.setRequestFocusEnabled(true);
		// driver.jtp.requestFocusInWindow();
		if (diagLang != null)
			driver.changeLanguage(diagLang);
		return file;

	}

	/* General save function */

	public File genSave(File file, DrawFBP.FileChooserParms fCP, Object contents) {

		boolean saveAs = false;
		File newFile = null;
		String fileString = null;
		//Diagram.FileChooserParms si = curDiag.fCPArray[type];
		if (file == null)
			saveAs = true;
		

		if (saveAs) {

			String suggestedFileName = "";
			// if (f == null) {

			String s = driver.properties.get(fCP.propertyName);
			if (s == null)
				s = System.getProperty("user.home");

			File f = new File(s);
			if (!f.exists()) {
				MyOptionPane.showMessageDialog(driver.frame, "Directory '" + s
						+ "' does not exist - reselect", MyOptionPane.ERROR_MESSAGE);

				f = new File(System.getProperty("user.home"));
			}
			// } else {

			String fn = "";
			suggestedFileName = "";
			File g = diagFile; 
			MyFileChooser fc = null;
			if (g != null) {
				fn = g.getName();
				suggestedFileName = s + File.separator + fn;
				int i = suggestedFileName.lastIndexOf(".");
				suggestedFileName = suggestedFileName.substring(0, i)
						+ fCP.fileExt;
			

				fc = new MyFileChooser(f, fCP);

				fc.setSuggestedName(suggestedFileName);
			}
			else
				fc = new MyFileChooser(f, fCP);

			int returnVal = fc.showOpenDialog(saveAs);

			// String s;
			if (returnVal == MyFileChooser.APPROVE_OPTION) {
				newFile = new File(driver.getSelFile(fc));
				s = newFile.getAbsolutePath();
				
				if (s.endsWith("(empty folder)")) {
					MyOptionPane.showMessageDialog(driver.frame, "Invalid file name: "
							+ newFile.getName(), MyOptionPane.ERROR_MESSAGE);
					return null;
				}

				if (!s.endsWith(fCP.fileExt)){
					s += fCP.fileExt;
					newFile = new File(s);
				}
				 if (newFile.getParentFile() == null) {
				 	MyOptionPane.showMessageDialog(driver.frame, "Missing parent file for: "
				 			+ newFile.getName(), MyOptionPane.ERROR_MESSAGE);
				 	return null;
				 }
				
				 if (!(newFile.getParentFile().exists())) {
				 	MyOptionPane.showMessageDialog(driver.frame, "Invalid file name: "
				 			+ newFile.getAbsolutePath(), MyOptionPane.ERROR_MESSAGE);
				 	return null;
				 }

				if (s.toLowerCase().endsWith("~")) {
					MyOptionPane.showMessageDialog(driver.frame,
							"Cannot save into backup file: " + s, MyOptionPane.ERROR_MESSAGE);
					return null;
				}
				File f2 = new File(s);
				if (!(f2.exists()) || f2.isDirectory()) {

					String suff = getSuffix(s);
					if (suff == null)
						newFile = new File(s + fCP.fileExt);
					else {
						// if (!(s.toLowerCase().endsWith(fCP.fileExt))) {
						if (!((driver.new ImageFilter()).accept(new File(s)))) {
							newFile = new File(s.substring(0,
									s.lastIndexOf(suff))
									+ fCP.fileExt.substring(1));
						}
					}
				}

				// newFile.getParentFile().mkdirs();
			}

			if (newFile == null)
				return null;

			if (fCP.fileExt.equals(".drw")
					&& -1 != diagramIsOpen(newFile.getAbsolutePath()))
				return null;

			//int response;
			if (newFile.exists()) {
				if (newFile.isDirectory()) {
					MyOptionPane.showMessageDialog(driver.frame, newFile.getName()
							+ " is a directory", MyOptionPane.WARNING_MESSAGE);
					return null;
				}
				if (!(MyOptionPane.YES_OPTION == MyOptionPane.showConfirmDialog(
						driver.frame, "Overwrite existing file: " + newFile.getAbsolutePath()
							+ "?", "Confirm overwrite",
						 MyOptionPane.YES_NO_OPTION)))  
			    	 return null;
			} else {
				if (!(MyOptionPane.YES_OPTION == MyOptionPane.showConfirmDialog(
						driver.frame, "Create new file: " + newFile.getAbsolutePath()
						+ "?", "Confirm create",
						 MyOptionPane.YES_NO_OPTION))) 
					return null;
			}
			file = newFile;
			
			//	diagFile = file;

			
		}

	 
		if (fCP == fCPArr[DrawFBP.IMAGE]) {
			Path path = file.toPath();
			try {
				Files.deleteIfExists(path);
				file = null;
				file = path.toFile();

				String suff = getSuffix(file.getAbsolutePath());
				BufferedImage bi = (BufferedImage) contents;

				ImageIO.write(bi, suff, file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//return file;
		} else {
			if (fCP.fileExt.equals(".drw")) {
				
				fileString = readFile(file, saveAs);
				diagFile = file;
				
				if (fileString != null) {
					String s = file.getAbsolutePath();
					File oldFile = file;
					file = new File(s.substring(0, s.length() - 1) + "~");
					writeFile(file, fileString);
					file = oldFile;
				}
				fileString = buildFile();
				//file = diagFile;
			} else
				fileString = (String) contents;

			writeFile(file, fileString);
			
			//return diagFile;
		}

		
		MyOptionPane.showMessageDialog(driver.frame, fCP.name + " saved: " + file.getName());
		return file;
	}
	
	
	// returns false if CANCEL option chosen
	public boolean askAboutSaving() {

		String fileString = null;
		String name;
		boolean res = true;
		if (changed) {

			if (title == null)
				name = "(untitled)";
			else {
				name = title;
				if (!name.toLowerCase().endsWith(".drw"))
					name += ".drw";
			}

			int answer = MyOptionPane.showConfirmDialog(driver.frame, 
					 "Save changes to " + name + "?", "Save changes",
					MyOptionPane.YES_NO_CANCEL_OPTION);
			File file = null;
			if (answer == MyOptionPane.YES_OPTION) {
				// User clicked YES.
				if (diagFile == null) { // choose file

					file = genSave(null, fCPArr[DrawFBP.DIAGRAM], name);
					if (file == null) {
						MyOptionPane.showMessageDialog(driver.frame,
								"File not saved");
						res = false;
					}
				} else {
					file = diagFile;
					fileString = buildFile();

					writeFile(file, fileString);


				}
				

			}
			if (answer == MyOptionPane.CANCEL_OPTION)				
				res = false;

		}
		File currentDiagramDir = null;
		
		if (diagFile != null) {
		    currentDiagramDir = diagFile.getParentFile();
		    driver.properties.put("currentDiagramDir",
				currentDiagramDir.getAbsolutePath());
		    if (res) {
		    	String s = diagFile.getAbsolutePath();
		    	if (s.endsWith(".drw"))
		    		driver.properties.put("currentDiagram", s);
		    }
		    else
		    	driver.properties.remove("currentDiagram");
		}
		
		String t = Integer.toString(driver.frame.getX());
		driver.properties.put("x", t);
		t = Integer.toString(driver.frame.getY());
		driver.properties.put("y", t);
		t = Integer.toString(driver.frame.getWidth());
		driver.properties.put("width", t);
		t = Integer.toString(driver.frame.getHeight());
		driver.properties.put("height", t);
		driver.propertiesChanged = true;
		
		return res;

	}

	public String buildFile() {
		String fileString = "<?xml version=\"1.0\"?> \n ";
		fileString += "<drawfbp_file " +
"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
"xsi:noNamespaceSchemaLocation=\"https://github.com/jpaulm/drawfbp/blob/master/lib/drawfbp_file.xsd\">";

		fileString += "<net>";
		if (desc != null)
			fileString += "<desc>" + desc + "</desc> ";

		// if (title != null)
		// fileString += "<title>" + title + "</title> ";

		if (diagLang != null)
			fileString += "<complang>" + diagLang.label + "</complang> ";

		// if (genCodeFileName != null)
		// fileString += "<genCodeFileName>" + genCodeFileName
		// + "</genCodeFileName> ";

				
		fileString += "<clicktogrid>" + (clickToGrid?"true":"false") + "</clicktogrid> \n" ;

		fileString += "<blocks>";

		for (Block block : blocks.values()) {
			if (!block.deleteOnSave) { // exclude deleteOnSave blocks
				// String s = block.diagramFileName;
				// block.diagramFileName = DrawFBP.makeRelFileName(
				// s, file.getAbsolutePath());
				fileString += block.serialize();
			}
		}
		fileString += "</blocks> <connections>\n";

		for (Arrow arrow : arrows.values()) {
			if (!arrow.deleteOnSave) // exclude deleteOnSave arrows
				fileString += arrow.serialize();
		}
		fileString += "</connections> ";


		fileString += "</net> </drawfbp_file>";


		return fileString;
	}


	public String readFile(File file, boolean saveAs) {
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
				MyOptionPane.showMessageDialog(driver.frame, "I/O Exception: "
						+ file.getName(), MyOptionPane.ERROR_MESSAGE);
				fileString = "";
			}

		} catch (FileNotFoundException e) {
			if (!saveAs)
				MyOptionPane.showMessageDialog(driver.frame, "File not found: "
					+ file.getName(), MyOptionPane.ERROR_MESSAGE);
			return null;
		} catch (IOException e) {
			MyOptionPane.showMessageDialog(driver.frame, "I/O Exception 2: "
					+ file.getName(), MyOptionPane.ERROR_MESSAGE);
			return null;
		}
		return fileString;
	} // readFile

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
	
	int diagramIsOpen(String s) {
		int j = driver.jtp.getTabCount();
		for (int i = 0; i < j; i++) {
			ButtonTabComponent b = (ButtonTabComponent) driver.jtp
					.getTabComponentAt(i);

			Diagram d = b.diag;
			if (d == null)
				continue;
			if (i == tabNum)
				continue;
			File f = d.diagFile;
			if (f != null) {

				String t = f.getAbsolutePath();
				if (t.endsWith(s)) {
					return i;
				}
			}
			if (s.endsWith(d.title))
				return i;
		}
		return -1;
	}
	
	String getSuffix(String s) {
		int i = s.lastIndexOf(File.separator);
		if (i == -1)
			i = s.lastIndexOf("/");
		String t = s.substring(i + 1);
		int j = t.lastIndexOf(".");
		if (j == -1)
			return null;
		else
			return t.substring(j + 1);
	}

	boolean hasSuffix(String s) {
		int i = s.lastIndexOf(File.separator);
		int j = s.substring(i + 1).lastIndexOf(".");
		return j > -1;
	}

	boolean matchArrow(int x, int y, Arrow arrow) {

		int x1 = arrow.fromX;
		int y1 = arrow.fromY;
		int x2, y2;
		if (arrow.bends != null) {
			for (Bend bend : arrow.bends) {
				x2 = bend.x;
				y2 = bend.y;
				if (driver.nearpln(x, y, x1, y1, x2, y2)) 
					return true;				
				x1 = x2;
				y1 = y2;				
			}
		}

		x2 = arrow.toX;
		y2 = arrow.toY;
		return driver.nearpln(x, y, x1, y1, x2, y2);
	}
	void delArrow(Arrow arrow) {
		LinkedList<Arrow> ll = new LinkedList<Arrow>();
		// go down list repeatedly - until no more arrows to remove
		while (true) {
			boolean found = false;
			for (Arrow arr : arrows.values()) {
				if (arr.endsAtLine && arr.toId == arrow.id) {
					arr.toId = -1;
					ll.add(arr);
					found = true;
					break;
				}
			}
			if (!found)
				break;
		}
		for (Arrow arr : ll) {
			Integer aid = new Integer(arr.id);
			arrows.remove(aid);
		}
		Integer aid = new Integer(arrow.id);
		arrows.remove(aid);
	}

	void delBlock(Block block, boolean choose) {
		if (block == null)
			return;
		if (choose
				&& MyOptionPane.YES_OPTION != MyOptionPane.showConfirmDialog( 
						driver.frame, "Do you want to delete this block?", "Delete block",
						 MyOptionPane.YES_NO_OPTION))
			return;

		// go down list repeatedly - until no more arrows to remove
		LinkedList<Arrow> ll = new LinkedList<Arrow>();
		//while (true) {
			//boolean found = false;
			for (Arrow arrow : arrows.values()) {
				if (arrow.fromId == block.id) {					
					ll.add(arrow);
					arrow.fromId = -1;
					//found = true;
					continue;
				}
				//if (arrow.endsAtBlock && arrow.toId == block.id) {					
				//	ll.add(arrow);
				//	arrow.toId = -1;
				//	found = true;
				//	break;
				//}
				Arrow a = arrow;				
				while (a.endsAtLine){
					Integer aid = new Integer(a.id);
					//ll.add(a);
					//a.toId = -1;
					a = arrows.get(aid);
				}	
				
				if (a.toId != block.id) 				
					continue; 
				
				a = arrow;
				while (a.endsAtLine){
					Integer aid = new Integer(a.id);
					ll.add(a);
					a.toId = -1;
					a = arrows.get(aid);
				}	
				if (a.toId == block.id){
					ll.add(a);
					a.toId = -1;
				}
			}
			

		for (Arrow arrow : ll)
			delArrow(arrow);

		changed = true;
		Integer aid = new Integer(block.id);
		blocks.remove(aid);
		// changeCompLang();

		driver.frame.repaint();
	}

	void excise(Enclosure enc, String subnetName) {

		// *this* is the *old* diagram; enc is the Enclosure block within it 
		
		driver.getNewDiag();               // creates new Diagram, and puts reference in driver.curDiag
		driver.sbnDiag = driver.curDiag;   // subnet Diagram
		driver.origDiag = this;
		
		// *driver.sbnDiag* will contain new subnet diagram, which will eventually contain all enclosed blocks and
		// arrows, plus external ports
			

		//LinkedList<Arrow> clla = new LinkedList<Arrow>(); // crossing arrows

		driver.sbnDiag.maxBlockNo = driver.origDiag.maxBlockNo + 2; // will be used for
															// new double-lined
															// block

		enc.calcEdges();		
		
		findEnclosedBlocksAndArrows(enc);
				
		// delete enclosed blocks from old diagram, and add to new one
		
		for (Block blk : enc.llb) {
			
			changed = true;
			Integer bid = new Integer(blk.id);
			blocks.remove(bid);
			driver.sbnDiag.maxBlockNo = Math.max(blk.id, driver.sbnDiag.maxBlockNo);
			blk.diag = driver.sbnDiag;
			//blk.id = driver.sbnDiag.maxBlockNo++;
			driver.sbnDiag.blocks.put(new Integer(blk.id), blk);
		}
			
		ProcessBlock subnetBlock = buildSubnetBlock(driver.origDiag, enc);
		
		driver.origDiag.copyArrows(enc, subnetBlock);   // categorize arrows in (old) Diagram, making copies of "crossers"
		
		// get arrows that were totally enclosed, delete from old diagram and add to new diagram
		
		for (Arrow arrow : enc.lla) {
			Integer aid = new Integer(arrow.id);
			arrows.remove(aid);
			driver.sbnDiag.arrows.put(aid, arrow); // add arrow to new
													// diagram
			arrow.diag = driver.sbnDiag;
			driver.selArrow = arrow;
			changed = true;
		}
				
		// now go through remaining arrows, creating appropriate ExtPortBlock's
		
		for (Arrow arrow : driver.origDiag.arrows.values()) {
						
			if (arrow.type.equals("I")) {
				driver.sbnDiag.arrows.put(new Integer(arrow.copy.id), arrow.copy);
				
				ExtPortBlock eb = new ExtPortBlock(driver.sbnDiag);
				eb.cx = arrow.copy.fromX - eb.width / 2;
				eb.cy = arrow.copy.fromY;
				eb.type = Block.Types.EXTPORT_IN_BLOCK;					
				driver.sbnDiag.maxBlockNo++;
				eb.id = driver.sbnDiag.maxBlockNo;
				arrow.copy.fromId = eb.id;
				//arrow.toId = subnetBlock.id;
				arrow.copy.upStreamPort = "OUT";
				//arrow.type = "I";
				driver.sbnDiag.blocks.put(new Integer(eb.id), eb);
				driver.selBlock = eb;
				driver.repaint();
				
				String ans = (String) MyOptionPane.showInputDialog(driver.frame,
						"Enter or change portname", 
						"Enter external input port name",
						MyOptionPane.PLAIN_MESSAGE, null, null, null);
				if (ans != null) {
					ans = ans.trim();					
				}				
				eb.description = ans;
				
				arrow.downStreamPort = ans;
				
				//SubnetPort snp = new SubnetPort();
				//enc.subnetPorts.add(snp);
				//snp.name = ans;
                //determine side
				//snp.y = to.cx; 
				//snp.eb = eb;  // cross reference from the external port block to the subnet port object
				
				//snp.side = DrawFBP.Side.LEFT;
				//side, sssensitive?
				
				eb.calcEdges();
				//arrow.toId = subnetBlock.id;
				Point fixed = new Point(arrow.fromX, arrow.fromY);
				if (arrow.bends != null) {
					for (Bend bend : arrow.bends) {
						fixed.x = bend.x;   // use first section
						fixed.y = bend.y;
					}
				}				
				
				Point var = computeArrowVar(fixed, subnetBlock);
				
				arrow.toX = var.x;
				arrow.toY = var.y;	
				arrow.toId = subnetBlock.id;
			}
			
			
			if (arrow.type.equals("O")) {		
				driver.sbnDiag.arrows.put(new Integer(arrow.copy.id), arrow.copy);
				ExtPortBlock eb = new ExtPortBlock(driver.sbnDiag);
				eb.cx = arrow.copy.toX + eb.width / 2;
				eb.cy = arrow.copy.toY;
				eb.type = Block.Types.EXTPORT_OUT_BLOCK;				
				driver.sbnDiag.maxBlockNo++;
				eb.id = driver.sbnDiag.maxBlockNo;
				arrow.copy.toId = eb.id;
				//arrow.fromId = subnetBlock.id;
				arrow.copy.downStreamPort = "IN";
				//arrow.type = "O";
				driver.sbnDiag.blocks.put(new Integer(eb.id), eb);
				driver.selBlock = eb;
				driver.repaint();
				
				String ans = (String) MyOptionPane.showInputDialog(driver.frame,
						"Enter or change portname",   
						"Enter external output port name",
						MyOptionPane.PLAIN_MESSAGE, null, null, null);
				if (ans != null) {
					ans = ans.trim();					
				}
				eb.description = ans;
				
				arrow.upStreamPort = ans;
				
				//SubnetPort snp = new SubnetPort();
				//enc.subnetPorts.add(snp);
				//snp.name = ans;
				 
				//snp.y = from.cx; 
				//snp.eb = eb;  // cross reference from the external port block to the subnet port object
				
				//snp.side = DrawFBP.Side.RIGHT;
				//side, sssensitive?	
				
				eb.calcEdges();
				//arrow.fromId = subnetBlock.id;
				Point fixed = new Point(arrow.toX, arrow.toY);				
				Point var = computeArrowVar(fixed, subnetBlock);
				
				arrow.fromX = var.x;
				arrow.fromY = var.y;
				arrow.fromId = subnetBlock.id;
			}
		}
		
				
		driver.repaint();		
 
		subnetBlock.description = subnetName;
		subnetBlock.diagramFileName = subnetName;
		//block.diag.diagFile = new File(block.diagramFileName);
		
		return;
	}

	Point computeArrowVar (Point fix, Block subnetBlock){
		Point var = new Point();
		if (fix.x == subnetBlock.cx ) {
			var.x = subnetBlock.cx;
			var.y = subnetBlock.cy - subnetBlock.height / 2;
		}
		else {
			Line2D.Float line = new Line2D.Float(fix.x, fix.y, subnetBlock.cx, subnetBlock.cy);
			DrawFBP.Side s = null;
			double left = subnetBlock.cx - subnetBlock.width / 2;			
			double right = subnetBlock.cx + subnetBlock.width / 2;
			double top = subnetBlock.cy - subnetBlock.height / 2;
			double bottom = subnetBlock.cy + subnetBlock.height / 2;
			if (line.intersectsLine(left, top, left, bottom)) 
				s = DrawFBP.Side.LEFT;
			else if (line.intersectsLine(right, top, right, bottom))
				s = DrawFBP.Side.RIGHT;
			else if (line.intersectsLine(left, top, right, top))
				s = DrawFBP.Side.TOP;
			else if (line.intersectsLine(left, bottom, right, bottom))
				s = DrawFBP.Side.BOTTOM;
			
			float slope = (float) (subnetBlock.cy - fix.y) / (subnetBlock.cx - fix.x);  
			
			if (s == DrawFBP.Side.LEFT) {				
				int yDiff = (int) ((subnetBlock.cx - subnetBlock.width / 2 - fix.x) * slope + .5);    // with rounding 			 
				var.y = (int) (fix.y + yDiff );
				var.x = subnetBlock.cx - subnetBlock.width / 2;
			}
			else if (s == DrawFBP.Side.RIGHT) {				
				int yDiff = (int) ((fix.x - (subnetBlock.cx + subnetBlock.width / 2)) * slope + .5);    // with rounding 				 
				var.y = (int) (fix.y - yDiff );
				var.x = subnetBlock.cx + subnetBlock.width / 2;
			}
			else if (s == DrawFBP.Side.TOP) {				
				int xDiff = (int) ((subnetBlock.cy - subnetBlock.height / 2 - fix.y) / slope + .5);     // with rounding				
				var.x = (int) (fix.x + xDiff );				 
				var.y = subnetBlock.cy - subnetBlock.height / 2;
			}
			else {
				int xDiff = (int) ((fix.y - (subnetBlock.cy + subnetBlock.height / 2))  / slope + .5) ;  // with rounding				
				var.x = (int) (fix.x - xDiff );				 
				var.y = subnetBlock.cy + subnetBlock.height / 2;
			}
		}
		return var;
	}
	
	void findEnclosedBlocksAndArrows(Enclosure enc) {
		
		// look for blocks which are within enclosure
		  
		enc.llb = new LinkedList<Block>();
		for (Block block : blocks.values()) {
			if (block == enc)
				continue;
			if (block.leftEdge >= enc.leftEdge
					&& block.rgtEdge <= enc.rgtEdge
					&& block.topEdge >= enc.topEdge
					&& block.botEdge <= enc.botEdge) {
				enc.llb.add(block); // set aside for action
			}
		}
 
		// look for arrows which are within enclosure
		enc.lla = new LinkedList<Arrow>();
		for (Arrow arrow : arrows.values()) {
			boolean included = true;
			int x1 = arrow.fromX;
			int y1 = arrow.fromY;
			int x2 = arrow.toX;
			int y2 = arrow.toY;
			if (arrow.bends != null) {
				//int i = 0;
				for (Bend bend : arrow.bends) {
					x2 = bend.x;
					y2 = bend.y;
					if (!(x1 > enc.cx - enc.width / 2
							&& x1 < enc.cx + enc.width / 2
							&& x2 > enc.cx - enc.width / 2
							&& x2 < enc.cx + enc.width / 2
							&& y1 > enc.cy - enc.height / 2
							&& y1 < enc.cy + enc.height / 2
							&& y2 > enc.cy - enc.height / 2
							&& y2 < enc.cy + enc.height / 2)) {
						included = false;
						break;
					}
					x1 = x2;
					y1 = y2;
				}
				x2 = arrow.toX;
				y2 = arrow.toY;
			}
			if (!(x1 > enc.cx - enc.width / 2 
					&& x1 < enc.cx + enc.width / 2
					&& x2 > enc.cx - enc.width / 2
					&& x2 < enc.cx + enc.width / 2
					&& y1 > enc.cy - enc.height / 2
					&& y1 < enc.cy + enc.height / 2
					&& y2 > enc.cy - enc.height / 2
					&& y2 < enc.cy + enc.height / 2))
				included = false;

			if (included)
				enc.lla.add(arrow);
		}
		
		 
	}
	
	
	
	void copyArrows(Enclosure enc, Block snBlock) {
		
		for (Arrow arrow : arrows.values()) {
					 
			Block from = blocks.get(new Integer(arrow.fromId));
			Block to = blocks.get(new Integer(arrow.toId));
			Arrow a2 = arrow.findLastArrowInChain(); 
			if (a2 != null)
				to = blocks.get(new Integer(a2.toId));
			arrow.type = " ";
			
			// test if arrow crosses a boundary; if so, copy 
			
			if (to == null && from != null ||
					from == null && to != null) { 			
				
				// copy arrow
				
				Arrow arrCopy = new Arrow(driver.sbnDiag);
				//arrCopy.orig = arrow;
				arrow.copy = arrCopy;
				arrCopy.orig = arrow;
				if (from == null)
					arrow.type = "O";
				else 
					arrow.type = "I";
				
				arrCopy.type = arrow.type;
				
				arrCopy.fromX = arrow.fromX;
				arrCopy.fromY = arrow.fromY;
				arrCopy.toX = arrow.toX;
				arrCopy.toY = arrow.toY;				
				
				arrCopy.fromId = arrow.fromId;
				arrCopy.toId = arrow.toId;
				driver.sbnDiag.maxArrowNo = Math.max(driver.sbnDiag.maxArrowNo, arrow.id);
				arrCopy.id = driver.sbnDiag.maxArrowNo++;
				arrCopy.capacity = arrow.capacity;
				arrCopy.endsAtBlock = arrow.endsAtBlock;
				arrCopy.endsAtLine = arrow.endsAtLine;
				if (arrow.bends != null) {
					//Rectangle r = new Rectangle(enc.cx - enc.width / 2, enc.cy - enc.height / 2, enc.width, enc.height);
					//arrCopy.bends = new LinkedList<Bend>();
					for (Bend b : arrow.bends) {
						
						if (from == null){
							arrCopy.toX = b.x;   
							arrCopy.toY = b.y;
							break;
						}
						else {
							arrCopy.fromX = b.x;  
							arrCopy.fromY = b.y;							
						}
						
						//Bend b2 = new Bend(b.x, b.y);
						//arrCopy.bends.add(b2);
					}
				}
				arrCopy.upStreamPort = arrow.upStreamPort;
				arrCopy.downStreamPort = arrow.downStreamPort;	
				
				//arr.fromSide = arrow.fromSide;
				//arr.toSide = arrow.toSide;
				//Diagram d = diag;
				arrCopy.diag = driver.sbnDiag;	
				if (arrow.type.equals("I"))
					arrow.toId = snBlock.id;
				else
					arrow.fromId = snBlock.id;
				arrow.diag = driver.origDiag;
				Integer aid = new Integer(arrCopy.id);
				driver.sbnDiag.arrows.put(aid, arrCopy);
				//arrCopy.orig = arrow;
				//cl.add(arrCopy); 
				// keep old arrow
				// arrow.deleteOnSave = true;
				changed = true;
			}
		}
	}

	/*
	 * build double-lined block in old diagram
	 */
	ProcessBlock buildSubnetBlock(Diagram diag, Enclosure enc) {
		ProcessBlock subnetBlock = new ProcessBlock(diag);

		subnetBlock.cx = xa;
		subnetBlock.cy = ya;
		subnetBlock.calcEdges();
		diag.maxBlockNo++;
		subnetBlock.id = diag.maxBlockNo;
		diag.blocks.put(new Integer(subnetBlock.id), subnetBlock);
		changed = true;
		driver.selBlock = subnetBlock;
		subnetBlock.diagramFileName = enc.diag.desc;
		subnetBlock.isSubnet = true;
		diag.parent = subnetBlock;
		// block.diagramFileName = "newname";

		// block.description = enc.description;
		subnetBlock.description = enc.diag.desc;
		enc.description = "Enclosure can be deleted";

		/*
		 * In this part of the code, we have two Diagram objects (driver.sbnDiag and driver.origDiag), each with
		 * their own blocks list and arrows list attached....
		 */

		diag.desc = subnetBlock.description;
		if (diag.desc != null)
			diag.desc = diag.desc.replace('\n', ' ');

		subnetBlock.cx = enc.cx;
		subnetBlock.cy = enc.cy;
		subnetBlock.diag = driver.origDiag;
		subnetBlock.calcEdges();
		return subnetBlock;
	}

}
