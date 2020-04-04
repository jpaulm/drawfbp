package com.jpaulmorrison.graphics;


import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import javax.swing.JPopupMenu;

import com.jpaulmorrison.graphics.DrawFBP.FileChooserParm;
import com.jpaulmorrison.graphics.DrawFBP.GenLang;
import com.jpaulmorrison.graphics.DrawFBP.Side;

public class Diagram {
	//private static final DrawFBP DrawFBP = null;	

	// LinkedList<Block> blocks;
	ConcurrentHashMap<Integer, Block> blocks;

	ConcurrentHashMap<Integer, Arrow> arrows;

	File diagFile;
	//String suggFile = null;

	DrawFBP driver = null;
	//int tabNum = -1;
	DrawFBP.SelectionArea area; 	

	String title;

	String desc;  // description at bottom

	GenLang diagLang;

	boolean changed = false;
	//boolean saving;

	// Arrow currentArrow = null;

	int maxBlockNo = 0;

	int maxArrowNo = 0;

	int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;

	int maxX = 0, maxY = 0;

	//Block foundBlock;

	boolean clickToGrid;

	//int xa, ya;
	
	Block parent = null;

	// File imageFile = null;

	//boolean findArrowCrossing = false;
	//Enclosure cEncl = null;
	

	//String genCodeFileName;
	
	JPopupMenu jpm;
	//String targetLang;
	public static int DIAGRAM = 0;
	public static int IMAGE = 1;	
	//public static int JHELP = 2;
	public static int JARFILE = 2;   
	public static int CLASS = 3;   
	public static int PROCESS = 4;   
	public static int NETWORK = 5;   
	public static int DLL = 6; 
	public static int EXE = 7;   
	public static int FBP = 8;    
	
	FileChooserParm[] fCParm;
	//int tabNum;   // index in driver.jtp
	
	CodeManager cm = null;
	Block motherBlock = null;
	
	Diagram(DrawFBP drawFBP) {
		driver = drawFBP;
		driver.curDiag = this;
		blocks = new ConcurrentHashMap<Integer, Block>();
		arrows = new ConcurrentHashMap<Integer, Arrow>();
		clickToGrid = true;
		parent = null;
		//StyleContext sc = new StyleContext();
		//doc = new DefaultStyledDocument(sc);   
		
		//file = null;
		diagLang = driver.currLang;	
		
		fCParm = new FileChooserParm[10];
		//motherBlock = null;
		String cTG = driver.properties.get("clicktogrid");
		if (cTG == null) {
			clickToGrid = true;
			driver.saveProp("clicktogrid", "true");
		} else 
			clickToGrid = (new Boolean(cTG)).booleanValue();
		
		driver.grid.setSelected(clickToGrid);
	}			
		
	/* General save function */
	
	public File genSave(File file, FileChooserParm fCP, Object contents) { 
		return genSave(file, fCP, contents, null);  
	}
	 

	public File genSave(File file, FileChooserParm fCP, Object contents, File suggFile) {  

	//  contents only used for (generated) java files, and images 
		
		boolean saveAs = false;
		File newFile = null;
		String fileString = null;
		//Diagram.FileChooserParms si = curDiag.fCPArray[type];
		if (file == null)
			saveAs = true;
		
		if (diagFile == null)    
			saveAs = true;

		if (saveAs) {
			
					
			String s = driver.properties.get(fCP.propertyName);  
			if (s == null) 
				s = System.getProperty("user.home");			 

			File f = new File(s);
			if (!f.exists()) {
				MyOptionPane.showMessageDialog(driver, "Directory '" + s
						+ "' does not exist - create it or reselect", MyOptionPane.ERROR_MESSAGE);

				f = new File(System.getProperty("user.home"));
			}
						
			String suggestedFileName = null;
			if (suggFile != null)
				suggestedFileName = suggFile.getAbsolutePath();
			else
				if (saveAs && title != null && !(title.equals("(untitled)")))
					suggestedFileName = s + "/" + title + fCP.fileExt;
			

			MyFileChooser fc = new MyFileChooser(driver, f, fCP);

			fc.setSuggestedName(suggestedFileName);
			//}
			//else
			//	fc = new MyFileChooser(driver,f, fCP);

			int returnVal = fc.showOpenDialog(saveAs, true);

			if (returnVal == MyFileChooser.CANCEL_OPTION)
				return null;
			if (returnVal != MyFileChooser.APPROVE_OPTION) {
				if (parent.isSubnet) {   
					int answer = MyOptionPane.showConfirmDialog(driver, 
						 "Subnet will be deleted - are you sure you want to do this?", "Save changes",
							MyOptionPane.YES_NO_CANCEL_OPTION);
					
					if (answer != MyOptionPane.YES_OPTION)  
						return f;
				}
			} else {
				newFile = new File(driver.getSelFile(fc));
				s = newFile.getAbsolutePath();
				
				if (s.endsWith("(empty folder)")) {
					MyOptionPane.showMessageDialog(driver, "Invalid file name: "
							+ newFile.getName(), MyOptionPane.ERROR_MESSAGE);
					return null;
				}

				
				 if (newFile.getParentFile() == null) {
				 	MyOptionPane.showMessageDialog(driver, "Missing parent file for: "
				 			+ newFile.getName(), MyOptionPane.ERROR_MESSAGE);
				 	return null;
				 }
				
				 //if (!(newFile.getParentFile().exists())) {
				 //	MyOptionPane.showMessageDialog(driver, "Invalid file name: "
				 //			+ newFile.getAbsolutePath(), MyOptionPane.ERROR_MESSAGE);
				 //	return null;
				// }

				if (s.toLowerCase().endsWith("~")) {
					MyOptionPane.showMessageDialog(driver,
							"Cannot save into backup file: " + s, MyOptionPane.ERROR_MESSAGE);
					return null;
				}
				//File f2 = new File(s);
				if (!(newFile.exists()) || newFile.isDirectory()) {

					String suff = driver.getSuffix(s);
					if (suff == null)
						newFile = new File(s + fCP.fileExt);
					else {
						
						if (!fCP.filter.accept(new File(s))) {    
							
							int answer = MyOptionPane.showConfirmDialog(driver, 
									"\"" + suff + "\" not valid suffix for " +
								            fCP.title + " files - change suffix?", "Change suffix?",
									MyOptionPane.YES_NO_CANCEL_OPTION);
							if (answer == MyOptionPane.CANCEL_OPTION)
								return null;
							if (answer == MyOptionPane.YES_OPTION)
								newFile = new File(s.substring(0,    
									s.lastIndexOf(suff))                              
									+ fCP.fileExt.substring(1));                    
						}
					}
				}

				  //newFile.getParentFile().mkdirs();
				  driver.saveProp(fCP.propertyName, newFile.getParentFile().getAbsolutePath());
			}
		 

			if (newFile == null)
				return null;
											
			file = newFile;
			
		}
				    
		
		if (fCP.fileExt.equals(".java") && driver.currLang.label.equals("Java")) {			
			fileString = (String) contents;			 
			fileString = cm.checkPackage(file, fileString);
			if (fileString == null)
				return new File(fileString);
		}
		
		// finished choosing file...
		
		if  (saveAs) { 
		if (file.exists()) {   
			if (file.isDirectory()) {
				MyOptionPane.showMessageDialog(driver,
						file.getName() + " is a directory",
						MyOptionPane.WARNING_MESSAGE);
				return null;
			}
			if (!(MyOptionPane.YES_OPTION == MyOptionPane.showConfirmDialog(
					driver,
					"Overwrite existing file: " + file.getAbsolutePath()
							+ "?",
					"Confirm overwrite", MyOptionPane.YES_NO_OPTION))) {
				
				return null;
			}
			
			
			
		} else {
			
			// if file doesn't exist
			
			if (MyOptionPane.YES_OPTION == MyOptionPane.showConfirmDialog(
					driver,
					"Create new file: " + file.getAbsolutePath() + "?", 
					"Confirm create", MyOptionPane.YES_NO_OPTION)) {
				try {
					file.createNewFile();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} else {
				MyOptionPane.showMessageDialog(driver, file.getAbsolutePath() + " not saved");
				return null;
			}
		}
	}
		if (fCP.fileExt.equals(".drw")) {	
			fileString = driver.readFile(file /*, saveAs*/); // read previous version
			diagFile = file;

			if (fileString != null && !(fileString.equals(""))) {
				String s = file.getAbsolutePath();
				File oldFile = file;
				file = new File(s.substring(0, s.length() - 1) + "~");
				driver.writeFile(file, fileString);
				file = oldFile;
			}
		}
		
		if (fCP == fCParm[IMAGE]) {
			Path path = file.toPath();
			try {
				Files.deleteIfExists(path);
				file = null;
				file = path.toFile();

				String suff = driver.getSuffix(file.getAbsolutePath());
				BufferedImage bi = (BufferedImage) contents;

				ImageIO.write(bi, suff, file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			
			// if not image
			
			if (fCP.fileExt.equals(".drw")) {

				//fileString = driver.readFile(file /*, saveAs*/); // read previous version
				diagFile = file;

				//if (fileString!= null) {
				//	String s = file.getAbsolutePath();
				//	File oldFile = file;
				//	file = new File(s.substring(0, s.length() - 1) + "~");
				//	driver.writeFile(file, fileString);
				//	file = oldFile;
				//}
				fileString = buildFile();  // build .drw file from internal classes (blocks and arrows)

			}

			
			driver.writeFile(file, fileString);

			// return diagFile;
		}
		String w = fCP.name;
		if (motherBlock!= null) {
			//motherBlock.subnetFileName = file.getPath();
			w = "Subnet";    
		}
		
		//suggFile = null;
		MyOptionPane.showMessageDialog(driver, w + " saved: " + file.getName());
		changed = false;
		return file;
	}
	
	
	// returns option chosen in MyOptionPane
	
	public int askAboutSaving() {

		String t = Integer.toString(driver.getBounds().x);
		driver.saveProp("x", t);
		t = Integer.toString(driver.getBounds().y);
		driver.saveProp("y", t);
		t = Integer.toString(driver.getSize().width);
		driver.saveProp("width", t);
		t = Integer.toString(driver.getSize().height);
		driver.saveProp("height", t);
		
		String name = null;
		//String fileString = null;
		if (diagFile != null)			
			name = diagFile.getAbsolutePath();
		else 
			name = "(untitled)";		 
			
		//int res;
		
		if (!changed) {
			if (diagFile != null) {
				String s = diagFile.getAbsolutePath();
				if (s.endsWith(".drw"))
					driver.saveProp("currentDiagram", s);
			}
			return MyOptionPane.YES_OPTION;

		}
		
		int answer = MyOptionPane.showConfirmDialog(driver,
				"Save changes to " + name + "?", "Save changes",
				MyOptionPane.YES_NO_CANCEL_OPTION);
		File file = null;
		if (answer == MyOptionPane.YES_OPTION) {

			// User clicked YES.

			file = genSave(diagFile, fCParm[DIAGRAM], null);
			if (file == null) {
				MyOptionPane.showMessageDialog(driver, "File not saved");
				answer = MyOptionPane.CANCEL_OPTION;
			} else
				changed = false;

		}
		if (answer == -1) 
			answer = MyOptionPane.CANCEL_OPTION;
		
		if (answer != MyOptionPane.CANCEL_OPTION) {
			int i = driver.jtp.getSelectedIndex();
			driver.jtp.remove(i);
		}

		File currentDiagramDir = null;
		
		if (diagFile != null) {
		    currentDiagramDir = diagFile.getParentFile();
		    if (currentDiagramDir != null)
		    	driver.saveProp("currentDiagramDir",
		    			currentDiagramDir.getAbsolutePath());
		    if (answer == MyOptionPane.YES_OPTION) {
		    	String s = diagFile.getAbsolutePath();
		    	if (s.endsWith(".drw"))
		    		driver.saveProp("currentDiagram", s);
		    }
		    else
		    	driver.properties.remove("currentDiagram");
		}
		
		return answer;
	}
		
	
	// Build data string for filing, from blocks and arrows...

	public String buildFile() {
		String fileString = "<?xml version=\"1.0\"?> \n ";
		fileString += "<drawfbp_file " +
"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
"xsi:noNamespaceSchemaLocation=\"https://github.com/jpaulm/drawfbp/blob/master/lib/drawfbp_file.xsd\">\n";

		fileString += "<net>\n";
		if (desc != null)
			fileString += "<desc>" + desc + "</desc>\n ";

		// if (title != null)
		// fileString += "<title>" + title + "</title> ";

		if (diagLang != null)
			fileString += "<complang>" + diagLang.label + "</complang>\n ";

		// if (genCodeFileName != null)
		// fileString += "<genCodeFileName>" + genCodeFileName
		// + "</genCodeFileName> ";

				
		fileString += "<clicktogrid>" + (clickToGrid?"true":"false") + "</clicktogrid> \n" ;
		
		fileString += "<sortbydate>" + (driver.sortByDate?"true":"false") + "</sortbydate> \n" ;

		fileString += "<blocks>";

		for (Block block : blocks.values()) {
			if (!block.deleteOnSave && 
					(block.compareFlag == null || !(block.compareFlag.equals("D")))) { // exclude deleteOnSave & ghost blocks
				// String s = block.diagramFileName;
				// block.diagramFileName = DrawFBP.makeRelFileName(
				// s, file.getAbsolutePath());
				fileString += block.serialize();
			}
		}
		fileString += "</blocks> <connections>\n";

		for (Arrow arrow : arrows.values()) {
			if (!arrow.deleteOnSave) {// exclude deleteOnSave arrows
				String s = arrow.serialize();
				if (s != null)
					fileString += s;
			}
		}
		fileString += "</connections> ";


		fileString += "</net> </drawfbp_file>";


		return fileString;
	}

	
	
	
	void delArrow(Arrow arrow) {
		//LinkedList<Arrow> ll = new LinkedList<Arrow>();
		// go down list looking for arrows which end at this arrow
		
		for (Arrow arr : arrows.values()) {
			if (arr.endsAtLine && arr.toId == arrow.id) {
				Integer aid = new Integer(arr.id);
				arrows.remove(aid);
				arr.toId = -1;									
			}
		}			
				
		Integer aid = new Integer(arrow.id);
		arrows.remove(aid);
	}

	void delBlock(Block block, boolean choose) {
		if (block == null)
			return;
		if (choose
				&& MyOptionPane.YES_OPTION != MyOptionPane.showConfirmDialog( 
						driver, "Do you want to delete this block?", "Delete block",
						 MyOptionPane.YES_NO_OPTION))
			return;

		// go down list repeatedly - until no more arrows to remove
		 
		for (Arrow arrow : arrows.values()) {
			if (arrow.fromId == block.id) 
				delArrow(arrow);
			
			if (arrow.endsAtBlock) {
				if (arrow.toId == block.id) 
					delArrow(arrow);	
			}			
		}

		changed = true;
		Integer aid = new Integer(block.id);
		blocks.remove(aid);
		// changeCompLang();

		driver.repaint();
	}

	void excise(Enclosure enc /*, String subnetName */) {	
		
		// *this* is the *old* diagram; enc is the Enclosure block within it 
		//String fn = subnetName;
		//fn = fn.trim();
		
		
		Diagram sbnDiag = driver.getNewDiag();  // sets curDiag as well
		Diagram origDiag = this;
		 
		String w = "(subnet)";
		//if (!w.endsWith(".drw"))
		//	w += ".drw";
		//w = w.substring(0, 1).toUpperCase() + w.substring(1);
		sbnDiag.title = w;
		sbnDiag.desc = w;
		
		
		// *driver.sbnDiag* will contain new subnet diagram, which will eventually contain all enclosed blocks and
		// arrows, plus external ports
			

		sbnDiag.maxBlockNo = origDiag.maxBlockNo + 2; // will be used for
															// new double-lined
															// block

		enc.calcEdges();		
		
		findEnclosedBlocksAndArrows(enc);
		
						
		// delete enclosed blocks from old diagram, and add to new one
		
		for (Block blk : enc.llb) {
			
			//changed = true;
			Integer bid = new Integer(blk.id);
			blocks.remove(bid);
			sbnDiag.maxBlockNo = Math.max(blk.id, sbnDiag.maxBlockNo);
			blk.diag = sbnDiag;
			//blk.id = sbnDiag.maxBlockNo++;
			sbnDiag.blocks.put(new Integer(blk.id), blk);
		}
			
		ProcessBlock subnetBlock = buildSubnetBlock(sbnDiag, origDiag, enc, enc.cx, enc.cy);
		sbnDiag.motherBlock = subnetBlock;
		
		
		
		// get arrows that were totally enclosed, delete from old diagram and add to new diagram
		
		for (Arrow arrow : enc.lla) {
			Integer aid = new Integer(arrow.id);
			arrows.remove(aid);
			sbnDiag.arrows.put(aid, arrow); // add arrow to new diagram
			//System.out.println("Added to new diag: " + arrow.id);
			arrow.diag = sbnDiag;
			driver.selArrow = arrow;
			// changed = true;
		}

		// categorize arrows in (old) Diagram, making copies of "crossers"

		for (Arrow arrow : arrows.values()) {

			Block from = blocks.get(new Integer(arrow.fromId));
			Block to = blocks.get(new Integer(arrow.toId));
			Arrow a2 = arrow.findLastArrowInChain();
			if (a2 != null)
				to = blocks.get(new Integer(a2.toId));
			// arrow.type = " ";

			// test if arrow crosses a boundary; if so, copy

			if (to == null && from != null || from == null && to != null) {
				int id = ++maxArrowNo;
				copyArrow(arrow, sbnDiag, from, id);
				if (from == null) {
					arrow.type = "O";
					arrow.fromId = subnetBlock.id;
				} else {
					arrow.type = "I";
					arrow.toId = subnetBlock.id;
				}

			}
		}
				
		// now go through remaining arrows, creating appropriate ExtPortBlock's
		
		
		for (Arrow arrow : origDiag.arrows.values()) {
						
			if (arrow.type.equals("I")) {
				sbnDiag.arrows.put(new Integer(arrow.copy.id), arrow.copy);
				//System.out.println("I-block added to new diag: " + arrow.copy.id);
				
				ExtPortBlock eb = new ExtPortBlock(sbnDiag);
				
				eb.cx = arrow.copy.fromX - eb.width / 2;
				eb.cy = arrow.copy.fromY;
				eb.type = Block.Types.EXTPORT_IN_BLOCK;					
				sbnDiag.maxBlockNo++;
				eb.id = sbnDiag.maxBlockNo;
				arrow.copy.fromId = eb.id;
				//arrow.toId = subnetBlock.id;
				arrow.copy.upStreamPort = "OUT";
				//arrow.type = "I";
				sbnDiag.blocks.put(new Integer(eb.id), eb);
				driver.selBlock = eb;
				driver.repaint();
				
				String ans = (String) MyOptionPane.showInputDialog(driver,
						"Enter or change portname", 
						"Enter external input port name",
						MyOptionPane.PLAIN_MESSAGE, null, null, null);
				if (ans != null) {
					ans = ans.trim();					
				}				
				eb.desc = ans;
				
				arrow.downStreamPort = ans;
				
				
				
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
				eb.buildSides();
			}
			
			
			if (arrow.type.equals("O")) {		
				sbnDiag.arrows.put(new Integer(arrow.copy.id), arrow.copy);
				//System.out.println("O-block added to new diag: " + arrow.copy.id);
				ExtPortBlock eb = new ExtPortBlock(sbnDiag);
				eb.cx = arrow.copy.toX + eb.width / 2;
				eb.cy = arrow.copy.toY;
				eb.type = Block.Types.EXTPORT_OUT_BLOCK;				
				sbnDiag.maxBlockNo++;
				eb.id = sbnDiag.maxBlockNo;
				arrow.copy.toId = eb.id;
				//arrow.fromId = subnetBlock.id;
				arrow.copy.downStreamPort = "IN";
				//arrow.type = "O";
				sbnDiag.blocks.put(new Integer(eb.id), eb);
				driver.selBlock = eb;
				driver.repaint();
				
				String ans = (String) MyOptionPane.showInputDialog(driver,
						"Enter or change portname",   
						"Enter external output port name",
						MyOptionPane.PLAIN_MESSAGE, null, null, null);
				if (ans != null) 
					ans = ans.trim();					
				 
				eb.desc = ans;
				
				arrow.upStreamPort = ans;
				
				eb.buildSides();			
				eb.calcEdges();
				//arrow.fromId = subnetBlock.id;
				Point fixed = new Point(arrow.toX, arrow.toY);				
				Point var = computeArrowVar(fixed, subnetBlock);
				
				arrow.fromX = var.x;
				arrow.fromY = var.y;
				arrow.fromId = subnetBlock.id;
			}
		}
		
				
		//driver.repaint();		
 
		subnetBlock.desc = w;
		
			
		driver.repaint();

		
		// driver.curDiag.changed = true;
		sbnDiag.changed = true;

		driver.curDiag = sbnDiag;
		sbnDiag.motherBlock.isSubnet = true;
		driver.repaint();
		
		File file = null;
		
		driver.jtp.setSelectedIndex(driver.jtp.getTabCount());  
		
		//String s = buildFile();  within gensave...
		if (MyOptionPane.YES_OPTION == MyOptionPane.showConfirmDialog(    
				driver, "Subnet created - please assign .drw file and save",
				"Name and save subnet", MyOptionPane.YES_NO_CANCEL_OPTION)) {
						
			file = sbnDiag.genSave(null, fCParm[Diagram.DIAGRAM], null);
			
			if (file != null) {
				
			int i = driver.jtp.getSelectedIndex(); 
			
			//int i = driver.getFileTabNo(sbnDiag.diagFile.getAbsolutePath());
			ButtonTabComponent b = (ButtonTabComponent) driver.jtp.getTabComponentAt(i);      
			b.label.setText(sbnDiag.diagFile.getAbsolutePath());
			driver.repaint();

			MyOptionPane.showMessageDialog(driver, "Give subnet diagram a description",    
			 	//"Enter subnet description",
			 	MyOptionPane.PLAIN_MESSAGE);
			
			sbnDiag.motherBlock.editDescription(DrawFBP.MODIFY);
			
			sbnDiag.desc = sbnDiag.motherBlock.desc.replace("\n", " "); 			
			
			sbnDiag.changed = true;
			sbnDiag.diagFile = file;
			origDiag.changed = true;   
			sbnDiag.title = sbnDiag.desc;
					
			if (sbnDiag.motherBlock!= null)  {
				sbnDiag.motherBlock.subnetFileName = sbnDiag.diagFile.getAbsolutePath();
				//sbnDiag.motherBlock.desc = sbnDiag.desc;
			}
		
			//for (Arrow arrow : sbnDiag.arrows.values()) {
			//	System.out.println("Subnet diag arrow: " + arrow.id);
			//}
			driver.repaint();
			return;
			}
			//if (subnetBlock.subnetFileName != null)
			//	subnetBlock.diag.diagFile = new File(subnetBlock.subnetFileName);  
			MyOptionPane.showMessageDialog(driver,
					"File not modified - exiting Excise function",
					MyOptionPane.ERROR_MESSAGE);
		   
			sbnDiag.changed = false; 
			driver.closeTab();   // close selected tab
		}
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
					Block from = blocks.get(new Integer(arrow.fromId));
					Block to = blocks.get(new Integer(arrow.toId));
					Arrow a2 = arrow.findLastArrowInChain(); 
					if (a2 != null)
						to = blocks.get(new Integer(a2.toId));
					
					if (enc.llb.contains(from)  && enc.llb.contains(to)) 
						enc.lla.add(arrow);
				
				}
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
				s = Side.LEFT;
			else if (line.intersectsLine(right, top, right, bottom))
				s = Side.RIGHT;
			else if (line.intersectsLine(left, top, right, top))
				s = Side.TOP;
			else if (line.intersectsLine(left, bottom, right, bottom))
				s = Side.BOTTOM;
			
			float slope = (float) (subnetBlock.cy - fix.y) / (subnetBlock.cx - fix.x);  
			
			if (s == Side.LEFT) {				
				int yDiff = (int) ((subnetBlock.cx - subnetBlock.width / 2 - fix.x) * slope + .5);    // with rounding 			 
				var.y = (int) (fix.y + yDiff );
				var.x = subnetBlock.cx - subnetBlock.width / 2;
			}
			else if (s == Side.RIGHT) {				
				int yDiff = (int) ((fix.x - (subnetBlock.cx + subnetBlock.width / 2)) * slope + .5);    // with rounding 				 
				var.y = (int) (fix.y - yDiff );
				var.x = subnetBlock.cx + subnetBlock.width / 2;
			}
			else if (s == Side.TOP) {				
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
	
	
	/**
	 * build double-lined block in old diagram
	 */
	
	ProcessBlock buildSubnetBlock(Diagram sbnDiag, Diagram origDiag, Enclosure enc, int x, int y) {
		ProcessBlock subnetBlock = new ProcessBlock(origDiag);

		subnetBlock.cx = x;
		subnetBlock.cy = y;
		subnetBlock.calcEdges();
		origDiag.maxBlockNo++;
		subnetBlock.id = origDiag.maxBlockNo;
		origDiag.blocks.put(new Integer(subnetBlock.id), subnetBlock);
		//changed = true;
		driver.selBlock = subnetBlock;
		//subnetBlock.diagramFileName = enc.diag.desc;
		subnetBlock.isSubnet = true;
		sbnDiag.parent = subnetBlock;
		// block.diagramFileName = "newname";

		// block.description = enc.description;
		//subnetBlock.description = enc.diag.desc;
		enc.desc = "Enclosure can be deleted";

		/*
		 * In this part of the code, we have two Diagram objects (driver.sbnDiag and driver.origDiag), each with
		 * their own blocks list and arrows list attached....
		 */

		//sbnDiag.desc = subnetBlock.description;
		//if (sbnDiag.desc != null)
		//	sbnDiag.desc = sbnDiag.desc.replace('\n', ' ');

		subnetBlock.cx = enc.cx;
		subnetBlock.cy = enc.cy;
		//subnetBlock.diag = sbnDiag;   
		subnetBlock.buildSides();
		subnetBlock.calcEdges();
		return subnetBlock;
	}
	
	Arrow copyArrow(Arrow arrow, Diagram diag, Block from, int id){
		Arrow arrCopy = new Arrow(diag);
		//arrCopy.orig = arrow;
		arrow.copy = arrCopy;
		arrCopy.orig = arrow;
		//if (from == null)
		//	arrow.type = "O";
		//else 
		//	arrow.type = "I";
		
		arrCopy.type = arrow.type;
		
		arrCopy.fromX = arrow.fromX;
		arrCopy.fromY = arrow.fromY;
		arrCopy.toX = arrow.toX;
		arrCopy.toY = arrow.toY;				
		
		arrCopy.fromId = arrow.fromId;
		arrCopy.toId = arrow.toId;
		
		//arrCopy.id = diag.maxArrowNo++;
		arrCopy.id = id;
		arrCopy.capacity = arrow.capacity;
		arrCopy.segNo = arrow.segNo;
		arrCopy.endsAtBlock = arrow.endsAtBlock;
		arrCopy.endsAtLine = arrow.endsAtLine;
		if (arrow.bends != null) {
			//Rectangle r = new Rectangle(enc.cx - enc.width / 2, enc.cy - enc.height / 2, enc.width, enc.height);
			arrCopy.bends = new LinkedList<Bend>();
			for (Bend b : arrow.bends) {
				/*
				if (from == null){
					arrCopy.toX = b.x;   
					arrCopy.toY = b.y;
					break;
				}
				else {
					arrCopy.fromX = b.x;  
					arrCopy.fromY = b.y;							
				}
				*/
				Bend b2 = new Bend();
				b2.x = b.x;
				b2.y = b.y;
				arrCopy.bends.add(b2);
			}
		}
		arrCopy.upStreamPort = arrow.upStreamPort;
		arrCopy.downStreamPort = arrow.downStreamPort;	
		
		//arr.fromSide = arrow.fromSide;
		//arr.toSide = arrow.toSide;
		//Diagram d = diag;
		arrCopy.diag = diag;	
		//if (arrow.type.equals("I"))
		//	arrow.toId = snBlock.id;
		//else
		//	arrow.fromId = snBlock.id;
		//arrow.diag = diag.origDiag;
		arrow.diag = this;
		Integer aid = new Integer(arrCopy.id);
		diag.arrows.put(aid, arrCopy);
		//arrCopy.orig = arrow;
		//cl.add(arrCopy); 
		// keep old arrow
		// arrow.deleteOnSave = true;
		changed = true;
		return arrCopy;
	}
	 
}
