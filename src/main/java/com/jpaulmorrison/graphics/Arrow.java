package com.jpaulmorrison.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;

import math.geom2d.Shape2D;
import math.geom2d.line.Line2D;
import java.awt.geom.PathIterator;
import java.util.*;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.jpaulmorrison.graphics.DrawFBP.ArrowSeg;

//import com.jpaulmorrison.graphics.DrawFBP.Side;

public class Arrow implements ActionListener {

	DrawFBP driver;
	int fromX, fromY, toX, toY;
	int lastX = -1, lastY = -1; // "last" x and y
	int fromId, toId, id = 0;
	boolean endsAtBlock, endsAtLine;
	int segNo; // only relevant if endsAtLine	
	LinkedList<Bend> bends;
	String type = "";  // "I" will result in an external input port; "O" -> external output port
	String upStreamPort, downStreamPort;
	//String uspMod;   //  upstream port after lowercasing
	//String dspMod;   // downStreamPort after lowercasing 
	//DrawFBP.Side fromSide, toSide;
	boolean deleteOnSave = false;
	static Color FOREST_GREEN = new Color(34, 139, 34);
	static Color ORANGE_RED = new Color(255, 69, 0);
	//boolean headMarked, tailMarked;
	//Point headMark;
	//Point tailMark;
	boolean dropOldest;
	int capacity;
	//int endX2, endY2;
	Arrow copy;   // this field and orig are set by Enclosure "excise" function 
	Arrow orig;   //                              do.
	//String type;   // "I" for input to subnet; "O" for output from subnet; null if wholly inside or outside
	
	//LegendBlock capLegend;   //Legend block associated with Arrow 
	LinkedList<Path2D.Double> pathList = null;
	int highlightedSeg = -1;
	
	Arrowhead ah = null;
	//Arrowhead tipArrowhead = null;    
	Arrowhead extraArrowhead = null;
	
	enum Status {
		UNCHECKED, COMPATIBLE, INCOMPATIBLE
	}

	//Status checkStatus = Status.UNCHECKED;

	Diagram diag;
	
	String compareFlag = null;
	
	//EPoly poly = null;
	
	Arrow(Diagram d) {
		super();
		endsAtBlock = false;
		endsAtLine = false;
		bends = null;
		upStreamPort = null;
		downStreamPort = null;
		toX = toY = -1; 
		toId = -1;
		diag = d;
		driver = d.driver;		
		capacity = -1;
		ah = null;
		pathList = new LinkedList<>();
		//endX2 = endY2 = -1;
		  
		
	}

	void draw(Graphics g) {
		//driver.validate();
		driver.repaint();
		
		pathList = new LinkedList<>();
		
		//int endX, endY;
		Block from = null;
		Block to = null;
		//if (fromId == -1 || toId == -1 || toX == -1) 
		//	return;
		
		from = diag.blocks.get(Integer.valueOf(fromId));		
		to = diag.blocks.get(Integer.valueOf(toId));			
		 
		Arrow a = findLastArrowInChain();
		if (a != null)
			to = diag.blocks.get(Integer.valueOf(a.toId));
 
		
		g.setColor(Color.GRAY);  
		

		Stroke stroke = ((Graphics2D)g).getStroke();
		ZigzagStroke zzstroke = new ZigzagStroke(stroke, 2, 4);		
		
		showCompareFlag(g);

		
		g.setColor(Color.BLACK);
		if (compareFlag == null || !(compareFlag.equals("D"))) {			

			if ((from instanceof ProcessBlock || from instanceof ExtPortBlock
					|| from instanceof Enclosure)
					&& (to instanceof ProcessBlock || to instanceof ExtPortBlock
							|| to instanceof Enclosure || endsAtLine)) {
				//if (compareFlag != null && compareFlag.equals("D"))
				//	g.setColor(DrawFBP.lg);
				//else
					// if (checkStatus == Status.UNCHECKED)
				/*
				if (driver.selArrow == this) { 
					Graphics2D g2d = (Graphics2D) g;
					g.setColor(Color.GRAY);					
					for (Path2D.Double pd: pathList) {
						g2d.draw(pd);
						g2d.fill(pd);
						}
					driver.repaint();
				}
				*/
				//else
			g.setColor(Color.BLACK);
			// else if (checkStatus == Status.COMPATIBLE)
			// g.setColor(FOREST_GREEN);
			// else
			// g.setColor(ORANGE_RED);
			}
			else if (from instanceof LegendBlock || to instanceof LegendBlock)
				g.setColor(Color.GRAY);
		}
		
		int fx, fy, tx, ty;
		fx = fromX;
		fy = fromY;
		
		 
		int segno = 0;
		boolean capDrawn = false;
		
		if (bends != null) {			
			for (Bend bend : bends) {
				//System.out.println("bend");
				tx = bend.x;
				ty = bend.y;
				Graphics2D g2d = (Graphics2D) g;				
				if (!dropOldest) {					
					g2d.setStroke(driver.bs);
					g2d.setRenderingHints(driver.rh);
					g.drawLine(fx, fy, tx, ty);
				}
				else {
					Shape shape = new java.awt.geom.Line2D.Double(fx, fy, tx, ty);
					shape = zzstroke.createStrokedShape(shape);
					((Graphics2D)g).draw(shape);					
				}
				
				if (capacity > 0 && segno == bends.size() / 2 && !capDrawn) {
					
					int x = (fx + tx) / 2;
					int y = (fy + ty) / 2;
					String s = "(" + capacity + ")";
					x -= s.length() / 2;
					g.drawString(s, x, y + 12);
					capDrawn = true;
				}	

				if (bend.marked) {
					driver.drawRedCircle(g, tx, ty);					
				}
				calcLimits(fx, tx, fy, ty);
				fx = tx;
				fy = ty;
				segno++;
			}
			
		} else  
			if (capacity > 0) {
				
				int x = (fx + toX) / 2;
				int y = (fy + toY) / 2;
				String s = "(" + capacity + ")";
				x -= s.length() * driver.gFontWidth / 2;
				g.drawString(s, x, y + 12);  // add "capacity" string to display
				calcLimits(x,  x + s.length() * driver.gFontWidth, y + 12, y + 12 + driver.gFontHeight);
				
			}
		 
		tx = toX;
		ty = toY;
		calcLimits(fx, tx, fy, ty);
		

		int x = toX;
		if (to != null && endsAtBlock && to.multiplex) {
			String s = to.mpxfactor;
			if (s == null)
				s = " ";
			int i = s.length() * driver.gFontWidth + 10;
			x -= i;
		}
		
		
		
		if (tx > -1 && ty > -1) {
			//rebuildFatLines();
			
			
			if (!dropOldest) {
				Graphics2D g2d = (Graphics2D) g;
				g2d.setStroke(driver.bs);
				g2d.setRenderingHints(driver.rh);
				g.drawLine(fx, fy, tx, ty);  
			}
			else {
				Shape shape = new java.awt.geom.Line2D.Double(fx, fy, tx, ty);
				shape = zzstroke.createStrokedShape(shape);
				((Graphics2D) g).draw(shape);
				// g.setStroke(stroke);
			}
			
			
			//System.out.println("arrow: " + fx + " " + fy + " " + tx + " " + ty);
		}
		
				
		calcLimits(fx, x, fy, toY);

		if (toId == -1) {
			   //g.drawRect(fromX - 3, fromY - 3, 6, 6);	
			   driver.drawBlackSquare(g, toX, toY);		
			   return;
		}
		
		if (toId == -2) {
			drawCircle(g, toX, toY, Color.BLUE, 8); 
			return;
		}
					
		if (endsAtBlock) {
			if ((from instanceof ProcessBlock || from instanceof ExtPortBlock  /* from instanceof Enclosure */   
					|| from instanceof IIPBlock ) && to != null && (to instanceof ProcessBlock
							|| to instanceof ExtPortBlock /* || to instanceof Enclosure */)) {
				//Arrowhead ah = buildArrowhead(toX, toY);  
				//ah.draw(g);	
				driver.fpArrowEndA = null;
				driver.fpArrowEndB = null;
				//driver.currentArrow = null;
			}

		} else if (endsAtLine)  
			drawCircle(g, toX, toY, Color.BLACK, 8);

		if (toX != -1 && (endsAtBlock || endsAtLine)) {
			if (upStreamPort != null
					&& (from instanceof ProcessBlock || from instanceof Enclosure /* || from instanceof ExtPortBlock */)) {
				if (upStreamPort.equals("*")) {
					drawCircleFrom(g, fromX, fromY, toX, toY, Color.BLUE, 8);
					
				} else if (from.visible) {
					//g.setColor(Color.BLUE);
					int y = fromY + driver.gFontHeight;
					int x2 = fromX + driver.gFontWidth;
					//g.setColor(Color.BLACK);
					//g.drawString(upStreamPort, x2, y);
				}
				g.setColor(Color.BLACK);
			}
			
			if (downStreamPort != null
					&& !endsAtLine
					&& to != null
					&& (to instanceof ProcessBlock || to instanceof Enclosure /* || to instanceof ExtPortBlock */)) {
				if (downStreamPort.equals("*")) {
					drawCircleTo(g, fx, fy, toX, toY, Color.BLUE, 8);
					
				} else if (to.visible) {
					//g.setColor(Color.BLUE);
					int y = toY - driver.gFontHeight / 2;
					x = toX - driver.gFontWidth * (downStreamPort.length() + 1);
					//g.setColor(Color.BLACK);
					if (!endsAtLine && to != null && to.multiplex)
						x -= 20;
					g.drawString(downStreamPort, x, y);
				}
				g.setColor(Color.BLACK);
			}
		}
		if (extraArrowhead != null)  
			extraArrowhead.draw(g); 
		
		if (driver.selArrow == this) { 
			rebuildFatLines();
			Graphics2D g2d = (Graphics2D) g;
			g.setColor(Color.LIGHT_GRAY);					
			for (Path2D.Double pd: pathList) {
				g2d.draw(pd);
				g2d.fill(pd);
				}
			driver.repaint();
		}
		
		if (highlightedSeg != -1) {
			Path2D.Double pd = pathList.get(highlightedSeg);
			Color col = g.getColor();
			g.setColor(ltBlue);  
			((Graphics2D)g).fill(pd);				
			g.setColor(col);	
		}
		if (!endsAtLine)
			ah.draw(g);   // draw arrowhead
	}
	
	
	void calcLimits(int x1, int x2, int y1, int y2) {
		if (x1 < x2) {
			diag.maxX = Math.max(x2 + 20, diag.maxX);
			diag.minX = Math.min(x1 - 20, diag.minX);
		} else {
			diag.maxX = Math.max(x1 + 20, diag.maxX);
			diag.minX = Math.min(x2 - 20, diag.minX);
		}
		if (y1 < y2) {
			diag.maxY = Math.max(y2 + 20, diag.maxY);
			diag.minY = Math.min(y1 - 20, diag.minY);
		} else {
			diag.maxY = Math.max(y1 + 20, diag.maxY);
			diag.minY = Math.min(y2 - 20, diag.minY);
		}
	}

	void drawCircle(Graphics g, int cx, int cy,
			Color color, int size) {
		Color col = g.getColor();
		g.setColor(color);		
		int x = cx;
		int y = cy;
		
		x -= size / 2;
		y -= size / 2;
		g.drawOval(x, y, size, size);
		g.fillOval(x, y, size, size);
		g.setColor(col);
	}

	void drawCircleFrom(Graphics g, int fx, int fy, int tx, int ty,
			Color color, int size) {
		Color col = g.getColor();
		g.setColor(color);
		int x, y;
		if (fx == tx) { // vertical line
			x = fx;
			if (ty > fy)
				y = fy + size / 2;
			else
				y = fy - size / 2;
		} else { // assume horizontal for now
			y = fy;
			if (tx > fx)
				x = fx + size / 2;
			else
				x = fx - size / 2;
		}
		// x and y are the centre of the circle
		// adjust to top left corner
		x -= size / 2;
		y -= size / 2;
		g.drawOval(x, y, size, size);
		g.fillOval(x, y, size, size);
		g.setColor(col);
	}

	void drawCircleTo(Graphics g, int fx, int fy, int tx, int ty,
			Color color, int size) {
		Color col = g.getColor();
		g.setColor(color);
		int x, y;
		x = tx;
		y = ty;
		if (color == Color.BLUE) {
			if (fx == tx) { // vertical line
				if (ty > fy)
					y = ty - size / 2;
				else
					y = ty + size / 2;
			} else {
				if (tx > fx)
					x = tx - size / 2;
				else
					x = tx + size / 2;
			}
		}
		// x and y are the centre of the circle
		// adjust to top left corner

		x -= size / 2;
		y -= size / 2;
		g.drawOval(x, y, size, size);
		g.fillOval(x, y, size, size);
		g.setColor(col);
	}
	
	
	void showCompareFlag(Graphics g){
		if (compareFlag != null && !compareFlag.equals(" ")) {
			Color col = g.getColor();
			int fx = fromX;
			int fy = fromY; 
			int tx = toX;
			int ty = toY;			
			
			if (bends != null) { 
				for (int index = 0; index < bends.size(); index ++) {
					tx = bends.get(index).x;
					ty = bends.get(index).y;
					if (index > 0) {
						fx = bends.get(index - 1).x;
						fy = bends.get(index - 1).y;
					}
				}
				
			}
			tx = toX;
			ty = toY;
			int x = (fx + tx) / 2 - 12;
			int y = (fy + ty) / 2 - 12;
			g.setColor(Color.BLACK);
			g.drawOval(x, y, 24, 24);
			g.setColor(DrawFBP.ly);
			g.fillOval(x + 2, y + 2, 22, 22);
			g.setColor(Color.BLACK);
			g.drawString(compareFlag, x + 10, y + 17);
			g.setColor(col);
		}
	}

	String serialize() {

		if (toX == -1 || toId == -1)
			return null;
		String s = "<connection> <fromx>" + fromX + "</fromx> " + "<fromy>"
				+ fromY + "</fromy> " + "<tox>" + toX + "</tox> " + "<toy>"
				+ toY + "</toy> " + "<fromid>" + fromId + "</fromid> "
				+ "<toid>" + toId + "</toid> " + "<id>" + id + "</id> ";
		//if (endsAtLine)
			s += "<endsatline>" + (endsAtLine?"true":"false") + "</endsatline>";
		if (upStreamPort != null) {
			s += "<upstreamport>" + upStreamPort + "</upstreamport>";
		}
		if (downStreamPort != null) {
			s += "<downstreamport>" + downStreamPort + "</downstreamport>";
		}
		if (dropOldest) {
			s += "<dropoldest>" + (dropOldest?"true":"false") + "</dropoldest>";
		}
		
		if (capacity > 0)
			s += "<capacity>" + capacity + "</capacity>";
		
		s += "<segno>" + segNo + "</segno>";
		
		if (bends != null) {
			s += "<bends> ";

			for (Bend bend : bends) {

				s += "<bend> <x>" + bend.x + "</x> <y> " + bend.y
						+ "</y> </bend>\n ";
			}
			s += "</bends> ";
		}
		s += "</connection> \n";
		return s;
	}

	void buildArrow(HashMap<String, String> item) {
		String s;
				

		s = item.get("fromx").trim();
		fromX = Integer.parseInt(s);
		s = item.get("fromy").trim();
		fromY = Integer.parseInt(s);
		s = item.get("tox").trim();
		toX = Integer.parseInt(s);
		s = item.get("toy").trim();
		toY = Integer.parseInt(s);
		
		//ah = buildArrowhead(toX, toY);  
		rebuildFatLines();  
		
		upStreamPort = item.get("upstreamport");		
		downStreamPort = item.get("downstreamport");
				
		s = item.get("endsatline");		
		if (s != null && s.equals("true"))			
			endsAtLine = true;
		
		endsAtBlock = !endsAtLine;		
		
		s = item.get("dropoldest");
		if (s != null && s.equals("true")) 
			dropOldest = true;
		
		s = item.get("capacity");
		if (s != null)
			capacity = Integer.parseInt(s); 
		
		s = item.get("segno");		
		if (s != null)
			segNo = Integer.parseInt(s); 
		
		s = item.get("fromid").trim();
		fromId = Integer.parseInt(s);
		
		//diag.portNames.add(upStreamPort + ":" + fromId);   

		s = item.get("toid").trim();
		toId = Integer.parseInt(s);
		
		//if (endsAtBlock)
		//	diag.portNames.add(downStreamPort + ":" + toId);   
		
		s = item.get("id");
		if (s == null)
			id = 0;
		else
			id = Integer.parseInt(s.trim());
		if (id == 0)
			id = diag.maxArrowNo + 1;

		diag.maxArrowNo = Math.max(id, diag.maxArrowNo);
		//System.out.println(diag.maxArrowNo);

	}

	void buildArrowPopupMenu() {
		diag.actionList = new JPopupMenu("            Arrow-related Actions");
		// driver.curPopup = jpm;
		//diag.jpm.setLocation(fromX + 100, fromY + 100);
		diag.actionList.setVisible(true);
		JLabel label2 = new JLabel();
		label2.setText(diag.actionList.getLabel());
		//label2.setFont(driver.fontg);
		label2.setFont(driver.fontf);
		diag.actionList.setFont(driver.fontf);
		// label2.setForeground(Color.BLUE);
		diag.actionList.add(label2);
		diag.actionList.addSeparator();
		JMenuItem menuItem;
		Block from = diag.blocks.get(Integer.valueOf(fromId));
		Block to = diag.blocks.get(Integer.valueOf(toId));
		Arrow a = this.findLastArrowInChain();
		if (a == null) {
			MyOptionPane.showMessageDialog(driver,
					"Can't find connecting arrow",
					MyOptionPane.ERROR_MESSAGE);
			return;
		}
		to = diag.blocks.get(Integer.valueOf(a.toId));
		if (!(from instanceof FileBlock || from instanceof PersonBlock || from instanceof ReportBlock || from instanceof LegendBlock ||
				to instanceof FileBlock || to instanceof PersonBlock || to instanceof ReportBlock || to instanceof LegendBlock 	) ) {
		if (!(from instanceof ExtPortBlock) && !(from instanceof IIPBlock)) {
			menuItem = new JMenuItem("Edit Upstream Port Name");
			menuItem.addActionListener(this);
			diag.actionList.add(menuItem);
		}
		if (!(to instanceof ExtPortBlock) && endsAtBlock) {
			menuItem = new JMenuItem("Edit Downstream Port Name");
			menuItem.addActionListener(this);
			diag.actionList.add(menuItem);
		}
		diag.actionList.addSeparator();
		}
		
		if ((from instanceof ProcessBlock || from instanceof ExtPortBlock) && (to instanceof ProcessBlock || from instanceof ExtPortBlock)) {

			menuItem = new JMenuItem("Toggle Upstream Port Automatic / Normal");
			menuItem.addActionListener(this);
			diag.actionList.add(menuItem);

			menuItem = new JMenuItem(
					"Toggle Downstream Port Automatic / Normal");
			menuItem.addActionListener(this);
			diag.actionList.add(menuItem);
		}
		
		diag.actionList.addSeparator();
		menuItem = new JMenuItem("Set Capacity");
		menuItem.addActionListener(this);
		diag.actionList.add(menuItem);
		menuItem = new JMenuItem("Remove Capacity");
		menuItem.addActionListener(this);
		diag.actionList.add(menuItem);
		diag.actionList.addSeparator();
		menuItem = new JMenuItem("Toggle DropOldest");
		menuItem.addActionListener(this);
		diag.actionList.add(menuItem);
		diag.actionList.addSeparator();
		menuItem = new JMenuItem("Add Logger");
		menuItem.addActionListener(this);
		diag.actionList.add(menuItem);
		diag.actionList.addSeparator();
		menuItem = new JMenuItem("Redraw Arrow");
		menuItem.addActionListener(this);
		diag.actionList.add(menuItem);
		diag.actionList.addSeparator();
		menuItem = new JMenuItem("Drag Tail");
		menuItem.addActionListener(this);
		diag.actionList.add(menuItem);
		menuItem = new JMenuItem("Drag Head");
		menuItem.addActionListener(this);
		diag.actionList.add(menuItem);
		menuItem = new JMenuItem("Drag New or Existing Bend");
		menuItem.addActionListener(this);
		diag.actionList.add(menuItem);
		diag.actionList.addSeparator();
		menuItem = new JMenuItem("Add Extra Arrowhead");
		menuItem.addActionListener(this);
		diag.actionList.add(menuItem);
		menuItem = new JMenuItem("Remove Extra Arrowhead");
		menuItem.addActionListener(this);
		diag.actionList.add(menuItem);

		diag.actionList.addSeparator();
		menuItem = new JMenuItem("Delete");
		diag.actionList.add(menuItem);
		menuItem.addActionListener(this);
		driver.currentArrow = null;
		driver.repaint();
		
	}

	public void actionPerformed(ActionEvent e) {
		String s = e.getActionCommand();

		diag.actionList = null;

		if (s.equals("Edit Upstream Port Name")) {

			String ans = (String) MyOptionPane.showInputDialog(driver,
					   "Enter or change text", "Edit upstream port name",
					MyOptionPane.PLAIN_MESSAGE, null, null, upStreamPort);
			
			if (ans != null /* && ans.length() > 0*/ ) {
				ans = ans.trim();
				Block b = diag.blocks.get(Integer.valueOf(fromId));
				// upStreamPort = ans;
				diag.changed = true;
				boolean found = false;
				for (Arrow a : diag.arrows.values()) {
					if (a.fromId == fromId && a.upStreamPort != null
							&& a.upStreamPort.equals(ans)
							/*&& !(upStreamPort.equals(ans) */ || 
						a.toId == fromId
							&& a.downStreamPort != null
							&& a.downStreamPort.equals(ans))
						found = true;					
				}
				if (found) {
					String proc = diag.blocks.get(fromId).desc;
					MyOptionPane.showMessageDialog(driver,
							"Duplicate port name: " + proc + "." + ans, MyOptionPane.WARNING_MESSAGE);
					upStreamPort = "";
					return;
				}
				upStreamPort = ans;

				if (b.typeCode.equals(Block.Types.EXTPORT_IN_BLOCK)
						|| b instanceof IIPBlock) {
					MyOptionPane.showMessageDialog(driver,
							"Upstream port must be blank", MyOptionPane.ERROR_MESSAGE);
					upStreamPort = "";
				}
				
				b.displayPortInfo();
			}
			driver.repaint();
			return;

		}  if (s.equals("Edit Downstream Port Name") && endsAtBlock) {
			String ans = (String) MyOptionPane.showInputDialog(driver,
					 "Enter or change text", "Edit downstream port name",
					MyOptionPane.PLAIN_MESSAGE, null, null, downStreamPort);
			
			
			if (ans != null /* && ans.length() > 0 */) {
				ans = ans.trim();
				Arrow arr = findLastArrowInChain();
				Block b = diag.blocks.get(Integer.valueOf(arr.toId));

				diag.changed = true;
				boolean found = false;
				for (Arrow a : diag.arrows.values()) {
					if (a.fromId == arr.toId && a.upStreamPort != null
							&& a.upStreamPort.equals(ans) || 
						a.toId == arr.toId
							&& a.downStreamPort != null
							&& a.downStreamPort.equals(ans) 
						    && !(arr.downStreamPort.equals(ans)))
						found = true;
				}
				if (found) {
					String proc = diag.blocks.get(toId).desc;
					MyOptionPane.showMessageDialog(driver,
							"Duplicate port name: " + proc + "." + ans, MyOptionPane.WARNING_MESSAGE);
					arr.downStreamPort = "";
											
					return;
				}
				
				arr.downStreamPort = ans;
				
				if (b.typeCode.equals(Block.Types.EXTPORT_OUT_BLOCK)) {
					MyOptionPane.showMessageDialog(driver,
							"Downstream port must be blank", MyOptionPane.ERROR_MESSAGE);
					arr.downStreamPort = "";
				}
				b.displayPortInfo();	
			}
			driver.repaint();
			return;			
		} 
		
		if (s.equals("Set Capacity")) {
			
			String capString = null;
			if (capacity < 1)
				capString = "";
			else
				capString = Integer.toString(capacity);
			
			String ans = (String) MyOptionPane.showInputDialog(driver,
					"Enter or change text", "Set Capacity",
					MyOptionPane.PLAIN_MESSAGE, null, null, capString);
			if ((ans != null) && (ans.length() > 0)) {
				try {
					capacity = Integer.parseInt(ans);
				} catch (NumberFormatException e2) {
					MyOptionPane.showMessageDialog(driver,
							"Capacity must be numeric", MyOptionPane.ERROR_MESSAGE);
					return;
				}
				
			}
			driver.repaint();
			diag.changed = true;
			return;
			
		}  if (s.equals("Remove Capacity")) {			
			capacity = -1;			
			driver.repaint();
			diag.changed = true;
			return;

		}   if (s.equals("Toggle Upstream Port Automatic / Normal")) {
			if (upStreamPort == null || !upStreamPort.equals("*"))
				upStreamPort = "*";
			else
				upStreamPort = null;
			driver.repaint();
			diag.changed = true;
			return;
		}  if (s.equals("Toggle Downstream Port Automatic / Normal")) {
			if (downStreamPort == null || !downStreamPort.equals("*"))
				downStreamPort = "*";
			else
				downStreamPort = null;
			driver.repaint();
			diag.changed = true;
			return;

		}  if (s.equals("Toggle DropOldest")) {
			dropOldest = !dropOldest;	
			driver.repaint();
			diag.changed = true;
			return;
			
		}  if (s.equals("Add Logger")) {
			//tailMarked = true;
			addLogger();			
			driver.repaint();
			diag.changed = true;
			return;

		} if (s.equals("Redraw Arrow")) {
			diag.oldArrow = this;
			diag.delTouchingArrows(this);
			diag.arrows.remove(Integer.valueOf(id));
			
			Arrow arr = new Arrow(diag);
			
			// try reusing id
			
			//int aid = (++diag.maxArrowNo);  
			//arr.id = aid;
			//diag.arrows.put(Integer.valueOf(arr.id), arr);
			
			arr.id = id;
			//diag.arrows.replace(Integer.valueOf(id), this, arr);
			arr.upStreamPort = this.upStreamPort;
			arr.downStreamPort = this.downStreamPort;
			arr.capacity = this.capacity;
			//driver.currentArrow = arr;
			
			return;
						
		} if (s.equals("Drag Tail")) {
			//tailMarked = true;
			driver.tailMark = new Point(fromX, fromY);
			driver.arrowHorTisBeingDragged = this;
			driver.repaint();
			diag.changed = true;
			return;

		}  if (s.equals("Drag Head")) {
			//headMarked = true;
			driver.headMark = new Point(toX, toY);
			driver.arrowHorTisBeingDragged = this;
			driver.repaint();
			diag.changed = true;
			return;

		}  if (s.equals("Drag New or Existing Bend")) {
			createBend(driver.curx, driver.cury);
			//buildFatLine(a.fromX, a.fromY, xa, ya); 
			diag.changed = true;
			driver.repaint();			
			return;
			
		} else if (s.equals("Add Extra Arrowhead")) {
			//Point2D p = new Point2D((double)driver.curx, (double)driver.cury);
			int fx = fromX;
			int fy = fromY; 
			int tx, ty;			
			if (bends != null) {				
				for (Bend bend : bends) {
					tx = bend.x;
					ty = bend.y;
					//if (driver.pointInLine(p, fx, fy, tx, ty)) {   
					Line2D line = new Line2D(fx, fy, tx, ty);
					if (driver.nearpln(driver.curx, driver.cury, line)) {
						extraArrowhead = buildArrowhead(driver.curx, driver.cury);
						diag.changed = true;
						return;
					}
					fx = tx;
					fy = ty;					
				}				
			}
			tx = toX;
			ty = toY;
			//if (driver.pointInLine(p, fx, fy, tx, ty)) 
			Line2D line = new Line2D(fx, fy, tx, ty);
			if (driver.nearpln(driver.curx, driver.cury, line)) 
				extraArrowhead = buildArrowhead(driver.curx, driver.cury);	
			diag.changed = true;
			driver.repaint();
			return;
			
		}  if (s.equals("Remove Extra Arrowhead")) {
			extraArrowhead = null;
			diag.changed = true;
			driver.repaint();
			return;
			 
		} else if (s.equals("Delete")) {

			if (MyOptionPane.YES_OPTION == MyOptionPane.showConfirmDialog(
					driver, "Do you want to delete this arrow?", "Delete arrow",
					 MyOptionPane.YES_NO_OPTION)) {
				diag.delTouchingArrows(this);
				Integer aid = Integer.valueOf(id);
				diag.arrows.remove(aid);

				diag.changed = true;
				driver.selArrow = null;
				driver.currentArrow = null;
				driver.repaint();
				
			}
			
			
		}
		 
	}
	
	// two cases here: 1) bend being created at end of arrow (no red circle) 
	//                 2) bend is being created/detected in the middle of an arrow
	//                     2a) new bend is being created
	//                     2b) existing bend is being detected

	void createBend(int x, int y) {
				
		int segNo = 0;				
		Bend bn = null;
		
		if (!endsAtBlock && !endsAtLine) {
			bends.add(new Bend(x, y));
			return;
		}
		
		int x1 = fromX;
		int y1 = fromY;
		int x2, y2;
		if (bends != null) {
			
			for (Bend b : bends) {
				x2 = b.x;
				y2 = b.y;

				if (sameBend(x, y, b)) {
					bn = b;
					bn.marked = true;
					driver.bendForDragging = bn;
					return;
				}
				ArrowSeg arrseg = driver.new ArrowSeg(x1, y1, x2, y2, this, segNo);
				if (driver.nearpln(x, y, arrseg)) {
					bn = new Bend(x, y);
					if (x1 == b.x) // if line vertical
						bn.x = x1;
					if (y1 == b.y) // if line horizontal
						bn.y = y1;
					bends.add(segNo, bn);
					bn.marked = true;
					driver.bendForDragging = bn;
					return;
				}
				x1 = x2;
				y1 = y2;
				segNo++;
			}
			
		}
		else
			bends = new LinkedList<>();
		x2 = toX;
		y2 = toY;
		ArrowSeg arrseg = driver.new ArrowSeg(x1, y1, x2, y2, this, segNo);
		if (driver.nearpln(x, y, arrseg)) {	
			bn = new Bend(x, y);
			if (x1 == toX) // if line vertical
				bn.x = x1;
			if (y1 == toY) // if line horizontal
				bn.y = y1;
			bends.add(bn);
			bn.marked = true;
			driver.bendForDragging = bn;
		}
	}	 

	boolean sameBend(int x1, int y1, Bend b) {
		return ((x1 - b.x) * (x1 - b.x) + (y1 - b.y) * (y1 - b.y)) < 6 * 6;
	}
	
	void addLogger() {
		int toX = this.toX;
		int toY = this.toY;
		if (bends != null) {
			Bend b = bends.getFirst();
			toX = b.x;
			toY = b.y;
		}
		
		int x = (fromX + toX) / 2;
		int y = (fromY + toY) / 2;
		//ProcessBlock p = new ProcessBlock(diag);
		ProcessBlock p = new ProcessBlock(diag);
		//block = (ProcessBlock) driver.createBlock(x, y, diag, false, true);	
		p.cx = x;
		p.cy = y;
		
		//p.cx = (fromX + toX) / 2;
		//p.cy = (fromY + toY) / 2;
		//p.buildSideRects();
		
		//Integer i = Integer.valueOf(id);
		
		p.desc = "Logger";
		p.centreDesc();
		//diag.maxBlockNo++;
		int pid = p.id;
		diag.blocks.put(Integer.valueOf(pid), p);
		
		//p.buildSideRects();
		//p.calcEdges();
		//p.adjEdgeRects();
		
		// test if arrow crosses left and right sides
		
		Line2D arrow = new Line2D(fromX, fromY, toX, toY);
		Line2D left = new Line2D(p.cx - p.width / 2, p.cy - p.height / 2, // left edge
				p.cx - p.width / 2, p.cy + p.height / 2);
		Line2D bot = new Line2D(p.cx - p.width / 2, p.cy + p.height / 2, // bottom edge
				 p.cx + p.width / 2, p.cy + p.height / 2);
		int hh = 0;
		int ww = 0;
		float sl = 0f;
		boolean lr = true;
		if (Line2D.intersects(arrow,left)) {
			//sl = (arrow.y2 - arrow.y1) / (arrow.x2 - arrow.x1);
			sl = (toY - fromY) / (toX - fromX);
			hh = (int) ((p.width/ 2) * sl);
		}		
		else {
			
			/**********************************************************************************
			 * If the arrow is exactly vertical, 'sl' involves a divide by 0, so it is infinity
			 * In the next statement 'ww' involves a divide by infinity, so by Java rules, it
			 *   will have a value of zero.  Works!  
			 *********************************************************************************/
			
			if (Line2D.intersects(arrow,bot)) {
				//sl = (arrow.y2 - arrow.y1) / (arrow.x2 - arrow.x1);
				sl = (toY - fromY) / (toX - fromX);
				ww = (int) ((p.height / 2) / sl);
				lr = false;
			}
			else {
				MyOptionPane.showMessageDialog(driver,"Not enough room between blocks", MyOptionPane.WARNING_MESSAGE);
				return;
			}
		}
		Arrow aL = new Arrow(diag);
		aL.fromX = fromX;		
		aL.fromY = fromY;
		aL.upStreamPort = this.upStreamPort;
		int xSign = 1;
		int ySign = 1;
		if (lr) {
			if (toX < fromX)  
				xSign = -1;			 
			aL.toX = p.cx - xSign * p.width / 2;
			aL.toY = p.cy - xSign * hh;
			//aL.ah = aL.buildArrowhead(aL.toX, aL.toY);  
			aL.rebuildFatLines();
		}
		else {
			if (toY < fromY)  
				ySign = -1;			 
			aL.toX = p.cx - ySign * ww;
			aL.toY = p.cy - ySign * p.height / 2;				
			//aL.ah = aL.buildArrowhead(aL.toX, aL.toY);   
			aL.rebuildFatLines();
		}
		aL.endsAtBlock = true;
		//diag.maxArrowNo++;
		aL.fromId = fromId;
		aL.toId = pid;
		aL.downStreamPort = "IN";
		int aid = (++diag.maxArrowNo);  
		aL.id = aid;
		diag.arrows.put(Integer.valueOf(aid), aL);
		
		// take old arrow; start it at new rectangle, and change fromId to refer to new rectangle 
		//Arrow aR = new Arrow(diag);
		Arrow aR = this;
		if (lr) {
			aR.fromX = p.cx + xSign * p.width / 2;
			aR.fromY = p.cy + xSign * hh;
		}
		else {			
			aR.fromX = p.cx + ySign * ww;
			aR.fromY = p.cy + ySign * p.height / 2;
		}
		//aR.toX = toX;
		//aR.toY = toY;
		//aR.endsAtBlock = true;
		//diag.maxArrowNo++;
		aR.fromId = pid;
		//aR.toId = toId;
		aR.upStreamPort  = "OUT";
		//aid = (++diag.maxArrowNo);
		//aR.id = aid;
		//diag.arrows.put(Integer.valueOf(aid), aR);
		
		//diag.arrows.remove(i);   
		//diag.delArrow(this);  

		diag.changed = true;
		driver.selArrow = null;
		driver.currentArrow = null;
		driver.repaint();
	}

	Arrow findLastArrowInChain() {
		if (endsAtBlock)  
			return this;
		if (!endsAtLine)
			return null;

		int id = toId; // endsAtLine, so toId must be an arrow ID
		
		Arrow a;		
		while (true) {
			if (id == -1)  			
				return this;
			
			a = null;
			for (Arrow arrow : diag.arrows.values()) {
				if (arrow == this)
					continue;
				if (id == arrow.id) {
					a = arrow;
					id = a.toId;
					break;
				}
			}
			
			if (a == null)  // no arrow matches id, so return this
				return this;			 
			
			if (a.endsAtBlock)
				return a;
			
			if (!a.endsAtLine)
				return this;
			id = a.toId;
		}
	}
	
	boolean checkSides() {
		Block from = diag.blocks.get(Integer.valueOf(fromId));
		Block to = diag.blocks.get(Integer.valueOf(toId));
		Arrow a = findLastArrowInChain();
		to = diag.blocks.get(Integer.valueOf(a.toId));
		if (!(from instanceof ProcessBlock)
				&& !(from instanceof ExtPortBlock) ||
			!(to instanceof ProcessBlock)
				&& !(to instanceof ExtPortBlock))
			return true;
		
		return true;

	}

	void reverseDirection() {
		int x, y, id;
		x = toX;
		toX = fromX;
		fromX = x;
		y = toY;
		toY = fromY;
		
		//ah = buildArrowhead(toX, toY);  
		rebuildFatLines();
		
		fromY = y;
		id = toId;
		toId = fromId;
		fromId = id;
		
		String st = upStreamPort;
		upStreamPort = downStreamPort;
		downStreamPort = st;
		
		}
	
	// Build rectangle around arrow segment, and add to Shape array
	
	// this only draws light blue rectangle if segment selected - it does not draw line
	
	static Color ltBlue = new Color(173, 216, 230); 
	
	void buildFatLine(int fx, int fy, int tx, int ty /*, int segNo */) {
		
		//GeneralPath path = new GeneralPath();
		Path2D.Double path = new Path2D.Double();
		double x, y;
		final int aDW = driver.zWS; // arrow Detect Width - same as detect edge size
		x = tx - fx;
		y = ty - fy;
		double hypoSqu = x * x + y * y;
		double aLth = Math.sqrt(hypoSqu);
		path.moveTo(0, -aDW / 2);
		path.lineTo(0, aDW / 2);
		path.lineTo(aLth, aDW / 2);
		path.lineTo(aLth, -aDW / 2);
		path.closePath();
		AffineTransform at = AffineTransform.getTranslateInstance(fx, fy);
		// at.rotate(Math.PI / 4); // 45 degrees
		at.rotate(tx - fx, ty - fy);
		//Shape sh = path.createTransformedShape(at);
		Path2D.Double pd =  (Path2D.Double) path.createTransformedShape(at);
		pathList.add(pd);		 	
		
			
	}
	
	void rebuildFatLines() {
		int fx2, fy2, tx2, ty2;
		fx2 = fromX;
		fy2 = fromY;
		//int segno = 0;

		if (bends != null)
			for (Bend bend : bends) {
				//System.out.println("bend");
				tx2 = bend.x;
				ty2 = bend.y;
				buildFatLine(fx2, fy2, tx2, ty2);
				fx2 = tx2;
				fy2 = ty2;
				//segno++;
			}
			
		//tx2 = toX;
		//ty2 = toY;
		
		buildFatLine(fx2, fy2, toX, toY);
		
		ah = buildArrowhead(toX, toY);  
		
	}
	
	Arrowhead buildArrowhead(int tx, int ty) {

		// tx/ty is tip of arrow and fx/fy will be used to determine direction & angle
		
		int fx = fromX;
		int fy = fromY;
		
		if (bends != null) {
			for (Bend bend : bends) {
				//System.out.println("bend");
				fx = bend.x;
				fy = bend.y;				
			}
		}

		ah = new Arrowhead(); 
		int b = 9;
		double theta = Math.toRadians(20);
		// The idea of using a GeneralPath is so we can
		// create the (three lines that make up the) arrow
		// (only) one time and then use AffineTransform to
		// place it anywhere we want.
		GeneralPath path = new GeneralPath();

		// distance between line and the arrow mark <** not **
		// Start a new line segment from the position of (0,0).
		path.moveTo(0, 0);
		// Create one of the two arrow head lines.
		int x = (int) (-b * Math.cos(theta));
		int y = (int) (b * Math.sin(theta));
		path.lineTo(x, y);

		// distance between line and the arrow mark <** not **
		// Make the other arrow head line.
		int x2 = (int) (-b * Math.cos(-theta));
		int y2 = (int) (b * Math.sin(-theta));
		// path.moveTo(0,0);
		path.lineTo(x2, y2);
		path.closePath();

		AffineTransform at = AffineTransform.getTranslateInstance(tx, ty);
		if (tx == fx) // vertical line
			if (fy > ty)
				at.quadrantRotate(3);
			else
				at.quadrantRotate(1);
		else
			at.rotate(tx - fx, ty - fy);
		ah.shape = at.createTransformedShape(path);

		return ah;

		// if (checkStatus == Status.UNCHECKED)

	}
		
	 
				
	/* Thanks to Jerry Huxtable 
	 *   http://www.jhlabs.com/java/java2d/strokes/
	 */
	
	public class ZigzagStroke implements Stroke {
		private float amplitude = 10.0f;
		private float wavelength = 10.0f;	    
		private static final float FLATNESS = 1;

		public ZigzagStroke( Stroke stroke, float amplitude, float wavelength ) {	        
	        this.amplitude = amplitude;
	        this.wavelength = wavelength;
		}

		public Shape createStrokedShape(Shape shape ) {
			GeneralPath result = new GeneralPath();
			PathIterator it = new FlatteningPathIterator(shape.getPathIterator( null ), FLATNESS );
			float points[] = new float[6];
			float moveX = 0, moveY = 0;
			float lastX = 0, lastY = 0;
			float thisX = 0, thisY = 0;
			int type = 0;			
			float next = 0;
	        int phase = 0;

			while ( !it.isDone() ) {
				type = it.currentSegment( points );
				switch( type ){
				case PathIterator.SEG_MOVETO:
					moveX = lastX = points[0];
					moveY = lastY = points[1];
					result.moveTo( moveX, moveY );					
	                next = wavelength/2;
					break;

				case PathIterator.SEG_CLOSE:
					points[0] = moveX;
					points[1] = moveY;
					// Fall into....

				case PathIterator.SEG_LINETO:
					thisX = points[0];
					thisY = points[1];
					float dx = thisX-lastX;
					float dy = thisY-lastY;
					float distance = (float)Math.sqrt( dx*dx + dy*dy );
					if ( distance >= next ) {
						float r = 1.0f / distance;						
						while ( distance >= next ) {
							float x = lastX + next*dx*r;
							float y = lastY + next*dy*r;	                        
							if ( (phase & 1) == 0 )
	                            result.lineTo( x+amplitude*dy*r, y-amplitude*dx*r );
	                        else
	                            result.lineTo( x-amplitude*dy*r, y+amplitude*dx*r );
							next += wavelength;
							phase++;
						}
					}
					next -= distance;					
					lastX = thisX;
					lastY = thisY;
	                if ( type == PathIterator.SEG_CLOSE )
	                    result.closePath();
					break;
				}
				it.next();
			}

			//return stroke.createStrokedShape( result );
			return result;
		}

	}
	
class Arrowhead  {
	
	Shape shape = null;
	
	
	void draw(Graphics g) {
		
		g.setColor(Color.BLACK);
		//else if (checkStatus == Status.COMPATIBLE)
		//	g.setColor(FOREST_GREEN);
		//else
		//	g.setColor(ORANGE_RED);
	
	
	((Graphics2D)g).fill(shape);
	((Graphics2D)g).draw(shape);
	}

	
}
	
	}
 

