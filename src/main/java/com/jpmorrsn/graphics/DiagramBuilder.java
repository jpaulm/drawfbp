package com.jpmorrsn.graphics;

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
		 * logic takes place when endtags are encountered. There are two
		 * situations where things get more complex: a) when a connection
		 * (arrow) has embedded bends, and b) when an enclosure block has
		 * embedded subnetports
		 * 
		 */

		DrawFBP driver = diag.driver;
		Integer errNo = new Integer(0);
		BabelParser2 bp = new BabelParser2(input, errNo);
		HashMap<String, HashMap<String, String>> tagInfo = new HashMap<String, HashMap<String, String>>();
		createTables(tagInfo);

		boolean done = false;
		Arrow thisArrow = null;

		String sym = "";
		String data = null;
		String saveData = null;
		// Object object;

		HashMap<String, String> item = new HashMap<String, String>();

		boolean atomic = false;
		HashMap<String, String> curFl = null;
		boolean debugging = false;

		// 3 stacks
		Stack<String> names = new Stack<String>(); // names (atomic and
		// non-atomic)
		Stack<HashMap<String, String>> fldLists = new Stack<HashMap<String, String>>();
		// field lists
		Stack<String> clsNames = new Stack<String>(); // class names

		boolean endsw; // if true, it is an end tag
		boolean control = false; // if true, it is a comment or control tag
		boolean arrowBuilt = false;
		boolean EALsw = false;
		boolean SSsw = false;
		boolean MPsw = false;
		boolean IVsw = false;
		boolean DOsw = false;
		Enclosure cEncl = null;
		SubnetPort snp = null;

		String tag = null;

		String type;

		diag.clickToGrid = false;
		driver.grid.setSelected(diag.clickToGrid);

		while (true) { // skip blanks, CRs or tabs
			if (bp.tb('o'))
				continue;
			break;
		}

		if (!bp.tc('<', 'o')) {
			MyOptionPane.showMessageDialog(frame, "1st non-blank char not '<'");
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
			while (true) {
				if (!bp.tb('o'))
					break;
			}
			while (true) { // scan off a symbol within <>
				if (bp.tc('>', 'n'))
					break;
				if (bp.tb('n'))
					break; // 'n' is equivalent to 'io'

				bp.tu();
			}
			while (true) {
				if (bp.tc('>', 'o'))
					break;
				bp.tu('o'); // skip all characters within tag until >
			}

			// at end, we can use getOutStr to get output into String (sym)

			if (!control) {

				sym = bp.getOutStr(); // get this symbol - check if ends in /

				if (sym.charAt(sym.length() - 1) == '/') { // stand-alone field
					// (no data)
					endsw = true;
					sym = sym.substring(0, sym.length() - 1); // drop final
					// slash from symbol
					names.push(sym);
					data = null;
					atomic = true;
				}

				if (!endsw) {

					names.push(sym);
					type = sym;

					if (curFl != null) {

						type = (String) curFl.get(sym);
						if (type == null) {
							MyOptionPane.showMessageDialog(frame, "Field '"
									+ sym + "' not defined for this class");
							return;
						}
						if (type.charAt(0) == '*') // test if it's atomic
							// this is set on if the value in curFl is an
							// asterisk, meaning that tag just has a value,
							// not a list
							atomic = true; // indicate atomic

					}

					if (!atomic) { // end switch not on, and not atomic
						// get permissible tags within this one
						curFl = (HashMap<String, String>) tagInfo.get(type);
						// set current field list
						fldLists.push(curFl); // push it on stack
						clsNames.push(type); // ??? // push class name

						if (debugging)
							System.out.println("Starting class " + type);

						if (sym.equals("connection")) {
							Arrow arrow = new Arrow(diag);
							thisArrow = arrow;
							arrowBuilt = false;
							EALsw = false;
							DOsw = false;

						} else if (sym.equals("bends")) {
							Integer aid = new Integer(item.get("id"));
							diag.arrows.put(aid, thisArrow);
							thisArrow.buildArrow(item);
							arrowBuilt = true;

						} else if (sym.equals("subnetport")) {
							snp = new SubnetPort();

						} else if (sym.equals("subnetports")) {
							cEncl = new Enclosure(diag);
							cEncl.buildEncl(item);
						}
						item.clear();
					}

				} else { // if endsw is on...
					tag = (String) names.pop();
					if (debugging)
						System.out.println(tag + " popped");
					if (!sym.equals(tag)) {
						MyOptionPane
								.showMessageDialog(frame, "Tags don't match: " + sym + " - " + tag);
						return;
					}
					if (atomic) { // patterns as follows:
						// <xx> data </xx> OR <yy/>
						if (data == null) { // <yy/>
							if (debugging)
								System.out.println("Stand-alone tag: " + tag);
							if (sym.equals("endsatline"))
								EALsw = true;
							if (sym.equals("substreamsensitive"))
								SSsw = true;
							if (sym.equals("multiplex"))
								MPsw = true;
							if (sym.equals("invisible"))
								IVsw = true;
							if (sym.equals("dropoldest"))
								DOsw = true;
							if (sym.equals("clicktogrid")) {
								diag.clickToGrid = true;
								driver.grid.setSelected(diag.clickToGrid);
							}

						} else { // <xx> data </xx> OR <yy/>
							saveData = new String(data);

							if (tag.equals("desc")) {
								diag.desc = saveData;
								diag.desc = diag.desc.replace('\n', ' ');
							}

							//if (tag.equals("title")) {
							//	diag.title = saveData;
							//}

							if (tag.equals("complang")) {
								if (saveData.equals("NoFlo"))
									saveData = "JSON";   // transitional! 
								diag.diagLang = driver
										.findGLFromLabel(saveData);
								 
								/*
								driver.jtf.setText(diag.diagLang.showLangs());
								diag.fCPArr[DrawFBP.PROCESS] = driver.new FileChooserParms(diag.diagLang.srcDirProp, "Select "
										+ diag.diagLang.showLangs() + " component from directory",
										diag.diagLang.suggExtn, diag.diagLang.filter, "Components: "
												+ diag.diagLang.showLangs() + " " + diag.diagLang.showSuffixes());
								
								diag.fCPArr[DrawFBP.GENCODE] = driver.new FileChooserParms(diag.diagLang.netDirProp,
										"Specify file name for generated code",
										"." + diag.diagLang.suggExtn, diag.diagLang.filter,
										diag.diagLang.showLangs());	
								*/
							}

							if (tag.equals("generatedCodeFileName")) {
								// diag.generatedCodeFileName = saveData; //
								// there may be some in old diagrams, so do
								// nothing!
							}

							if (tag.equals("clicktogrid")) {
								diag.clickToGrid = saveData.equals("true");
							}

							item.put(tag, saveData);

							if (debugging)
								System.out.println("Data at " + tag + ": "
										+ saveData);
							data = null;
						}
						atomic = false;
					}

					else { // if not atomic and endsw is on
						if (tag.equals("block")) {
							Block block = null;
							String stype;
							
							if (cEncl != null) {
								block = cEncl;
								stype = Block.Types.ENCL_BLOCK;
								cEncl = null;
							} else {
								stype = item.get("type");
								if (null == stype) {
									MyOptionPane.showMessageDialog(frame,
											"No block type specified");
									block = new ProcessBlock(diag);

								} else if (stype
										.equals(Block.Types.PROCESS_BLOCK)) {
									block = new ProcessBlock(diag);

									if (MPsw) {
										block.multiplex = true;
										MPsw = false;
									}
									// String s = item.get("compname"); //
									// deprecated
									// String t = item.get("codefilename");
									// block.codeFileName = t;
									// if (t == null && s != null)
									// block.codeFileName = s;
									// block.fullClassName = item
									// .get("blockclassname");
									// block.diagramFileName = item
									// .get("diagramfilename");
								} else if (stype
										.equals(Block.Types.REPORT_BLOCK)) {
									block = new ReportBlock(diag);

								} else if (stype.equals(Block.Types.FILE_BLOCK)) {
									block = new FileBlock(diag);

								} else if (stype
										.equals(Block.Types.EXTPORT_IN_BLOCK)
										|| stype.equals(Block.Types.EXTPORT_OUT_BLOCK)
										|| stype.equals(Block.Types.EXTPORT_OUTIN_BLOCK)) {
									block = new ExtPortBlock(diag);

									if (stype
											.equals(Block.Types.EXTPORT_OUTIN_BLOCK)) {
										block.width = 2 * block.width;
									}
									block.type = stype;
									if (SSsw) {
										if (block instanceof ExtPortBlock) {
											ExtPortBlock eb = (ExtPortBlock) block;
											eb.substreamSensitive = true;
										}
										if (snp != null)
											snp.substreamSensitive = true;
										SSsw = false;
									}
								} else if (stype.equals(Block.Types.IIP_BLOCK)) {
									block = new IIPBlock(diag);

								} else if (stype
										.equals(Block.Types.LEGEND_BLOCK)) {
									block = new LegendBlock(diag);

								} else if (stype
										.equals(Block.Types.PERSON_BLOCK)) {
									block = new PersonBlock(diag);

								} else if (stype.equals(Block.Types.ENCL_BLOCK)) {
									block = new Enclosure(diag);

								} else {
									MyOptionPane.showMessageDialog(frame,
											"Undefined block type");
									block = new ProcessBlock(diag);
								}
								block.buildBlockFromXML(item);
								block.calcEdges();
								if (IVsw) {
									block.visible = false;
									IVsw = false;
								}
							}
							
							String s = item.get("multiplex");
							if (s != null)
							   block.multiplex = s.equals("true");
							
							s = item.get("invisible");
							if (s != null)
							   block.visible = s.equals("false");
							
							diag.blocks.put(new Integer(block.id), block);
							
						} else if (tag.equals("connection")) {
							if (!arrowBuilt) {
								Integer aid = new Integer(item.get("id"));
								diag.arrows.put(aid, thisArrow);
								thisArrow.buildArrow(item);
							}
							thisArrow.endsAtLine = EALsw;							
							String s = item.get("endsatline");
							if (s != null )
								thisArrow.endsAtLine = s.equals("true");
							thisArrow.endsAtBlock = !thisArrow.endsAtLine;
							thisArrow.dropOldest = DOsw;
							s = item.get("dropoldest");
							if (s != null )
								thisArrow.dropOldest = s.equals("true");
							thisArrow = null;
						} else if (tag.equals("bend")) {
							if (thisArrow.bends == null)
								thisArrow.bends = new LinkedList<Bend>();
							Bend bend = new Bend();
							bend.buildBend(item);
							thisArrow.bends.add(bend);
						} else if (tag.equals("subnetport")) {

							snp.buildBlockFromXML(item);
							if (SSsw)
								snp.substreamSensitive = true;
							cEncl.subnetPorts.add(snp);
							snp = null;
						} // else if (tag.equals("subnetports")) {
							// item = block_item;
							// }
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
					if (bp.tb('o'))
						continue;
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
						MyOptionPane.showMessageDialog(frame, "Tags remaining: " + names.elementAt(0));
					done = true;
					break;
				}
			}

			if (done)
				break;

			data = bp.getOutStr();
			if (data != null) {
				if (tag == null || !(sym.equals("description")))
					data = data.trim();
				Pattern p = Pattern.compile("\\s*");
				Matcher m = p.matcher(data);
				if (!(data.equals("")) && !m.matches())

					if (endsw || !atomic)
						MyOptionPane.showMessageDialog(frame,
								"Characters found not preceded by field start tag: \""
										+ data + "\"");
			}

		} // end of loop

		if (curFl != null)
			MyOptionPane.showMessageDialog(frame,
					"Tags not completely processed");

		// check type compatibility for all arrows
		// for (Arrow a: currentDiag.arrows.values()){
		// checkCompatibility(a);
		// }
		//for (Block b : diag.blocks.values()) {
		//	b.checkArrows();
		//}
		if (diag.diagLang == null) {
			diag.diagLang = driver.defaultCompLang;
			diag.changed = true;
		} 
		//else {
		//	driver.properties.put("defaultCompLang", diag.compLang.label);
		//	driver.propertiesChanged = true;
		//}
		driver.jtf.setText(diag.diagLang.showLangs());
		frame.repaint();
	}

	public static void createTables(
			HashMap<String, HashMap<String, String>> tagInfo) {

		HashMap<String, String> fl1 = new HashMap<String, String>();
		fl1.put("title", "*");  // deprecated
		fl1.put("desc", "*");
		fl1.put("complang", "*");
		fl1.put("clicktogrid", "*");
		fl1.put("genCodeFileName", "*"); //deprecated
		// fl1.put("scalingFactor", "*");
		fl1.put("generatedCodeFileName", "*"); // deprecated
		fl1.put("genCodeFileNames", "*"); // deprecated
		fl1.put("blocks", "LinkedList");
		fl1.put("connections", "LinkedList");

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
		fl3.put("invisible", "*");
		fl3.put("description", "*");
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
