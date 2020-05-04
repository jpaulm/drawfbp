package com.jpaulmorrison.graphics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;

public class DiagramBuilder {

	public static void buildDiag(String input, JFrame frame, Diagram diag) {

		/***********************************************************************
		 * 
		 * This method scans off the XML-like network definition. Most of the
		 * logic takes place when endtags are encountered. There is one
		 * situation where things get more complex: when a connection
		 * (arrow) has embedded bends
		 * 
		 */

		DrawFBP driver = diag.driver;
		Integer errNo = new Integer(0);
		BabelParser2 bp = new BabelParser2(input, errNo);
		HashMap<String, HashMap<String, String>> tagInfo = new HashMap<String, HashMap<String, String>>();
		createTables(tagInfo);

		boolean done = false;
		Arrow thisArrow = null;

		String starttag = "";
		String data = null;
		String saveData = null;
		// Object object;

		HashMap<String, String> item = new HashMap<String, String>();

		boolean atomic = false;
		HashMap<String, String> curFl = null;
		boolean debugging = false;
		//boolean debugging = true;


		// 3 stacks
		Stack<String> names = new Stack<String>(); // names (atomic and
		// non-atomic)
		Stack<HashMap<String, String>> fldLists = new Stack<HashMap<String, String>>();
		// field lists
		Stack<String> clsNames = new Stack<String>(); // class names

		boolean endsw; // if true, it is an end tag
		boolean control = false; // if true, it is a comment or control tag
		boolean arrowBuilt = false;
	
		Enclosure cEncl = null;
		//SubnetPort snp = null;

		String endtag = null;

		//String type;

		diag.clickToGrid = true;
		driver.grid.setSelected(diag.clickToGrid);

		while (true) { // skip blanks, CRs or tabs
			if (!(bp.tb('o')))				
			    break;
		}

		if (bp.finished())
			return;
		
		if (!bp.tc('<', 'o')) {
			MyOptionPane.showMessageDialog(frame, "1st non-blank char not '<'", MyOptionPane.ERROR_MESSAGE);
			// terminate();
			return;
		}

		while (true) { // main loop - we should be just after a <

			if (bp.tc('/', 'o'))
				endsw = true;
			else
				endsw = false;

			if (bp.tc('!', 'o'))
				control = true;
			if (bp.tc('?', 'o'))
				control = true;
			
			while (true) { // scan off a symbol within <>
				if (bp.tc('>', 'o'))
					break;			

				bp.tu();
			}
			
			// at end, we can use getOutStr to get output into String (sym)

			if (!control) {

				starttag = bp.getOutStr(); // get this symbol - check if ends in /
				if (starttag.startsWith("drawfbp_file"))
					starttag = "drawfbp_file";

				if (starttag.charAt(starttag.length() - 1) == '/') { // stand-alone field
					// (no data)
					endsw = true;
					starttag = starttag.substring(0, starttag.length() - 1); // drop final
					endtag = starttag;
					// slash from symbol
					names.push(starttag);
					data = null;
					atomic = true;
				}

				if (!endsw) {

					names.push(starttag);
					String t = starttag;
					endtag = null;

					if (curFl != null) {

						t = (String) curFl.get(starttag);
						if (t == null) {
							MyOptionPane.showMessageDialog(frame, "Field '"
									+ starttag + "' not defined for this class", MyOptionPane.ERROR_MESSAGE);
							return;
						}
						if (t.charAt(0) == '*') // test if it's atomic
							// this is set on if the value in curFl is an
							// asterisk, meaning that tag just has a value,
							// not a list
							atomic = true; // indicate atomic

					}

					if (!atomic) { // end switch not on, and not atomic
						// get permissible tags within this one
						curFl = (HashMap<String, String>) tagInfo.get(starttag);
						// set current field list
						fldLists.push(curFl); // push it on stack
						clsNames.push(starttag); //  push class name
						
						
						if (debugging)
							System.out.println("Starting class " + starttag);

						if (starttag.equals("connection")) {
							Arrow arrow = new Arrow(diag);
							thisArrow = arrow;
							arrowBuilt = false;							

						} else if (starttag.equals("bends")) {
							Integer aid = new Integer(item.get("id"));
							diag.arrows.put(aid, thisArrow);
							thisArrow.buildArrow(item);
							arrowBuilt = true;

						//} else if (starttag.equals("subnetport")) {
						//	snp = new SubnetPort();

						//} else if (starttag.equals("subnetports")) {
						//	cEncl = new Enclosure(diag);
						//	cEncl.buildEncl(item);
						} 
						item.clear();
					}

				} else { // if endsw is on...
					endtag = (String) names.pop();
					if (debugging)
						System.out.println(endtag + " popped");
					if (!starttag.equals(endtag)) {
						if (starttag.equals("net"))  // from code before 2.13.4
							return;
						MyOptionPane
								.showMessageDialog(frame, "Tags don't match: " + starttag + " - " + endtag, MyOptionPane.ERROR_MESSAGE);
						return;
					}
					if (atomic) {   // i.e. a leaf in the XML tree
						if (data == null) { 
							// patterns as follows:
							// <xx> </xx> OR <yy/>
							if (debugging)
								System.out.println("Stand-alone tag: " + endtag);
							if (starttag.equals("endsatline") ||
								starttag.equals("substreamsensitive") ||
								starttag.equals("multiplex") ||
								starttag.equals("invisible") ||
								starttag.equals("clicktogrid") ||
								starttag.equals("sortbydate") ||
								starttag.equals("dropoldest"))
								    item.put(starttag, "true");

						} else { // <xx> data </xx> 
							saveData = new String(data);

						if (endtag.equals("desc") /* || endtag.equals("description") */) {
							diag.desc = saveData;
							diag.desc = diag.desc.replace('\n', ' ');
							if (diag.desc.equals(""))
								diag.desc = " ";
							}

							//if (tag.equals("title")) {
							//	diag.title = saveData;
							//}

						else if (endtag.equals("complang")) {
								if (saveData.equals("NoFlo"))
									saveData = "JSON";   // transitional! 
								diag.diagLang = driver
										.findGLFromLabel(saveData);
								 
								
							}

						else if (endtag.equals("generatedCodeFileName")) {
								// diag.generatedCodeFileName = saveData; //
								// there may be some in old diagrams, so do
								// nothing!
							}

						else if (endtag.equals("clicktogrid")) {
							diag.clickToGrid = saveData.equals("true");
						}
						else if (endtag.equals("sortbydate")) {
							driver.sortByDate = saveData.equals("true");
						}
						else
							item.put(endtag, saveData);

						if (debugging)
								System.out.println("Data at " + endtag + ": "
										+ saveData);
						data = null;
						}
						atomic = false;
					}

					else { // if not atomic and endsw is on - we're processing the end tag of a tag pair
						if (endtag.equals("block")) {
							Block block = null;
							String type;
							
							if (cEncl != null) {
								block = cEncl;
								type = Block.Types.ENCL_BLOCK;
								cEncl = null;
							} else {
								type = item.get("type");
								if (null == type) {
									MyOptionPane.showMessageDialog(frame,
											"No block type specified", MyOptionPane.ERROR_MESSAGE);
									block = new ProcessBlock(diag);

								} else if (type.equals(Block.Types.PROCESS_BLOCK)) {
									block = new ProcessBlock(diag);								
									
								} else if (type
										.equals(Block.Types.REPORT_BLOCK)) {
									block = new ReportBlock(diag);

								} else if (type.equals(Block.Types.FILE_BLOCK)) {
									block = new FileBlock(diag);

								} else if (type
										.equals(Block.Types.EXTPORT_IN_BLOCK)
										|| type.equals(Block.Types.EXTPORT_OUT_BLOCK)
										|| type.equals(Block.Types.EXTPORT_OUTIN_BLOCK)) {
									block = new ExtPortBlock(diag);

									if (type
											.equals(Block.Types.EXTPORT_OUTIN_BLOCK)) {
										block.width = 2 * block.width;
									}
									block.type = type;
									
									String sbs = item.get("substreamsensitive");
									if (sbs != null && sbs.equals("true")){											
										if (block instanceof ExtPortBlock) {
											ExtPortBlock eb = (ExtPortBlock) block;
											eb.substreamSensitive = true;
										}
										//if (snp != null)
										//	snp.substreamSensitive = true;										
									}
								} else if (type.equals(Block.Types.IIP_BLOCK)) {
									block = new IIPBlock(diag);
									IIPBlock ib = (IIPBlock) block;
									//ib.width = ib.calcIIPWidth(driver.osg);
									//block.buildSides();

								} else if (type
										.equals(Block.Types.LEGEND_BLOCK)) {
									block = new LegendBlock(diag);

								} else if (type
										.equals(Block.Types.PERSON_BLOCK)) {
									block = new PersonBlock(diag);

								} else if (type.equals(Block.Types.ENCL_BLOCK)) {
									block = new Enclosure(diag);

								} else {
									MyOptionPane.showMessageDialog(frame,
											"Undefined block type", MyOptionPane.ERROR_MESSAGE);
									block = new ProcessBlock(diag);
								}
								
								block.buildBlockFromXML(item);
								block.buildSides();
								block.calcEdges();				
								
							}
							
							String s = item.get("multiplex");							
							block.multiplex = s != null && s.equals("true");
							
							s = item.get("invisible");	
							block.visible = true;
							if (s != null)
							   block.visible = s.equals("false");
							
							diag.blocks.put(new Integer(block.id), block);
							
						} else if (endtag.equals("connection")) {
							if (!arrowBuilt) {
								thisArrow.buildArrow(item);
								if (thisArrow.fromId > -1 && thisArrow.toId > -1 && thisArrow.toX > -1)  { 
									Integer aid = new Integer(item.get("id"));
									diag.arrows.put(aid, thisArrow);
								}								
							}													
														
							thisArrow = null;
							
						} else if (endtag.equals("bend")) {
							if (thisArrow.bends == null)
								thisArrow.bends = new LinkedList<Bend>();
							Bend bend = new Bend();
							bend.buildBend(item);
							thisArrow.bends.add(bend);
							/*
						} else if (endtag.equals("subnetport")) {

							snp.buildBlockFromXML(item);
							if (item.get("substreamsensitive").equals("true"))
								snp.substreamSensitive = true;
							cEncl.subnetPorts.add(snp);
							snp = null;
							*/
						} 
						
						clsNames.pop();
						fldLists.pop();
						if (!fldLists.empty())
							curFl = (HashMap<String, String>) fldLists.peek();
						else
							curFl = null;

					}
				}
			}

			// if we have just read an end tag, a non-atomic name, or control
			// (!?),
			// skip following blanks to next non-blank (should be a <)
			if (endsw || !atomic || control) {
				while (true) { // skip blanks and tabs
					if (!(bp.tb('o')))
					    break;
				}
				bp.eraseOutput(); // make sure no symbol outstanding
				control = false;
			}

			// the effect of this is that data will include leading and trailing
			// blanks
			// or tabs, if any

			while (true) {

				if (bp.tc('\\', 'o')) { // back-slash followed by angle bracket
										// -> angle bracket only
					if (bp.tc('>'))
						continue;
					if (bp.tc('<'))
						continue;
					bp.w('\\');
					continue;
				}

				if (bp.tc('<', 'o'))
					break; // look for left bracket

				// here we have found something that is not a < - it could be
				// end of file, or it could be data
				if (!bp.tu()) {
					// at this point, we have processed the whole input stream
					if (!names.empty())
						MyOptionPane.showMessageDialog(frame, "Tags remaining: " + names.elementAt(0), MyOptionPane.WARNING_MESSAGE);
					done = true;
					break;
				}
			}

			if (done)
				break;

			data = bp.getOutStr();
			
			if (data != null) {	
				data = data.trim();
				if (data.equals("null"))  // .drw builder occasionally inserting "null" 
					data = "";
				Pattern p = Pattern.compile("\\s*");
				Matcher m = p.matcher(data);
				if (!(data.equals("")) && !m.matches())

					if (endsw || !atomic)
						MyOptionPane.showMessageDialog(frame,
								"Characters found not preceded by field start tag: \""
										+ data + "\"", MyOptionPane.ERROR_MESSAGE);
			}

		} // end of loop
		
		
		for (Arrow a : diag.arrows.values()) {
			Block fromBlock = diag.blocks.get(new Integer(a.fromId));
			if (fromBlock != null && (fromBlock.type.equals(Block.Types.EXTPORT_IN_BLOCK)
					|| fromBlock.type.equals(Block.Types.EXTPORT_OUTIN_BLOCK)))
				a.upStreamPort = "OUT";
			
			Arrow a2 = a.findLastArrowInChain();
			if (a2 == null) {
				MyOptionPane.showMessageDialog(driver,
						"Can't find connecting arrow",
						MyOptionPane.ERROR_MESSAGE);
				break;
			}
			Block toBlock = diag.blocks.get(new Integer(a2.toId));

			if (toBlock != null && (
			 toBlock.type.equals(Block.Types.EXTPORT_OUT_BLOCK)
					|| toBlock.type.equals(Block.Types.EXTPORT_OUTIN_BLOCK)))

				a2.downStreamPort = "IN";
		}
		
		if (curFl != null)
			MyOptionPane.showMessageDialog(frame,
					"Tags not completely processed", MyOptionPane.WARNING_MESSAGE);

		// check type compatibility for all arrows
		// for (Arrow a: currentDiag.arrows.values()){
		// checkCompatibility(a);
		// }
		//for (Block b : diag.blocks.values()) {
		//	b.checkArrows();
		//}
		if (diag.diagLang == null) {
			diag.diagLang = driver.currLang;
			//diag.changed = true;
		} 
		else {
			driver.saveProp("defaultCompLang", diag.diagLang.label);
			//driver.saveProperties();
		}
		driver.jtf.setText("Diagram Language: " + diag.diagLang.showLangs());
		frame.repaint();
	}

	public static void createTables(
			HashMap<String, HashMap<String, String>> tagInfo) {

		HashMap<String, String> fl1 = new HashMap<String, String>();
		fl1.put("title", "*");  // deprecated
		//fl1.put("drawfbp_file", "LinkedList");
		fl1.put("net", "LinkedList");
		fl1.put("desc", "*");
		fl1.put("complang", "*");
		fl1.put("clicktogrid", "*");
		fl1.put("sortbydate", "*");
		fl1.put("genCodeFileName", "*"); //deprecated
		// fl1.put("scalingFactor", "*");
		fl1.put("generatedCodeFileName", "*"); // deprecated
		fl1.put("genCodeFileNames", "*"); // deprecated
		fl1.put("blocks", "LinkedList");
		fl1.put("connections", "LinkedList");
		fl1.put("description", "*");  // deprecated

		HashMap<String, String> fl2 = new HashMap<String, String>();
		fl2.put("block", "Block");

		// fields in Block
		HashMap<String, String> fl3 = new HashMap<String, String>();
		fl3.put("x", "*");
		fl3.put("y", "*");
		fl3.put("id", "*");
		fl3.put("description", "*");
		fl3.put("codefilename", "*");
		fl3.put("diagramfilename", "*");
		fl3.put("compname", "*"); // deprecated
		fl3.put("blockclassname", "*");
		fl3.put("type", "*");
		fl3.put("width", "*");
		fl3.put("height", "*");
		fl3.put("multiplex", "*");
		fl3.put("mpxfactor", "*");
		fl3.put("substreamsensitive", "*");
		fl3.put("invisible", "*");		
		fl3.put("issubnet", "*");	
		fl3.put("subnetports", "LinkedList");

		HashMap<String, String> fl4 = new HashMap<String, String>();
		fl4.put("connection", "LinkedList");

		// fields in Connection (Arrow)
		HashMap<String, String> fl5 = new HashMap<String, String>();
		fl5.put("fromx", "*");
		fl5.put("fromy", "*");
		fl5.put("tox", "*");
		fl5.put("toy", "*");
		fl5.put("fromid", "*");
		fl5.put("toid", "*");
		fl5.put("id", "*");
		fl5.put("fromside", "*");  // deprecated
		fl5.put("toside", "*");  // deprecated
		fl5.put("upstreamport", "*");
		fl5.put("downstreamport", "*");
		fl5.put("dropoldest", "*");
		fl5.put("capacity", "*");
		fl5.put("segno", "*");
		fl5.put("endsatline", "*");	
		fl5.put("bends", "LinkedList");

		HashMap<String, String> fl6 = new HashMap<String, String>();
		fl6.put("bend", "Bend");

		HashMap<String, String> fl7 = new HashMap<String, String>();
		fl7.put("x", "*");
		fl7.put("y", "*");

		HashMap<String, String> fl8 = new HashMap<String, String>();
		fl8.put("subnetport", "SubnetPort");

		// field in SubnetPort
		HashMap<String, String> fl9 = new HashMap<String, String>();
		fl9.put("y", "*");
		fl9.put("name", "*");
		fl9.put("side", "*");
		fl9.put("substreamsensitive", "*");

		tagInfo.put("net", fl1);
		tagInfo.put("drawfbp_file", fl1);		
		tagInfo.put("blocks", fl2);
		tagInfo.put("block", fl3);
		tagInfo.put("connections", fl4);
		tagInfo.put("connection", fl5);
		tagInfo.put("bends", fl6);
		tagInfo.put("bend", fl7);
		tagInfo.put("subnetports", fl8);
		tagInfo.put("subnetport", fl9);

	}

}
