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

import java.awt.geom.PathIterator;

import math.geom2d.*;

import java.util.*;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

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
	String dspMod;   // downStreamPort after lowercasing 
	//DrawFBP.Side fromSide, toSide;
	boolean deleteOnSave = false;
	static Color FOREST_GREEN = new Color(34, 139, 34);
	static Color ORANGE_RED = new Color(255, 69, 0);
	//boolean headMarked, tailMarked;
	//Point headMark;
	//Point tailMark;
	boolean dropOldest;
	int capacity;
	int endX2, endY2;
	Arrow copy;   // this field and orig are set by Enclosure "excise" function 
	Arrow orig;   //                              do.
	//String type;   // "I" for input to subnet; "O" for output from subnet; null if wholly inside or outside
	
	//LegendBlock capLegend;   //Legend block associated with Arrow 
	LinkedList<Shape> shapeList = null;
	
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
		endX2 = endY2 = -1;
		  
		
	}

	void draw(Graphics g) {
		//driver.validate();
		//driver.repaint();

			
		shapeList = new LinkedList<Shape>(); 
		
		//int endX, endY;
		Block from = null;
		Block to = null;
		//if (fromId == -1 || toId == -1 || toX == -1) 
		//	return;
		
		from = diag.blocks.get(new Integer(fromId));		
		to = diag.blocks.get(new Integer(toId));			
		 
		Arrow a = findLastArrowInChain();
		if (a != null)
			to = diag.blocks.get(new Integer(a.toId));
 
		
		g.setColor(Color.GRAY);  
		

		Stroke stroke = ((Graphics2D)g).getStroke();
		ZigzagStroke zzstroke = new ZigzagStroke(stroke, 2, 4);		
		
		showCompareFlag(g);

		if (driver.selArrow == this)
			g.setColor(Color.BLUE);
		
		if (compareFlag == null || !(compareFlag.equals("D"))) {			

			if ((from instanceof ProcessBlock || from instanceof ExtPortBlock
					|| from instanceof Enclosure)
					&& (to instanceof ProcessBlock || to instanceof ExtPortBlock
							|| to instanceof Enclosure || endsAtLine))
				//if (compareFlag != null && compareFlag.equals("D"))
				//	g.setColor(DrawFBP.lg);
				//else
					// if (checkStatus == Status.UNCHECKED)
					g.setColor(Color.BLACK);
			// else if (checkStatus == Status.COMPATIBLE)
			// g.setColor(FOREST_GREEN);
			// else
			// g.setColor(ORANGE_RED);

			else if (from instanceof LegendBlock || to instanceof LegendBlock)
				g.setColor(Color.GRAY);
		}
		
		int fx, fy, tx, ty;
		fx = fromX;
		fy = fromY;
		 
		int segno = 0;
		
		if (bends != null) {
			
			boolean capDrawn = false;
			
			for (Bend bend : bends) {
				//System.out.println("bend");
				tx = bend.x;
				ty = bend.y;
				buildFatLine(fx, fy, tx, ty, segno, g);
				
				if (!dropOldest)
					g.drawLine(fx, fy, tx, ty);
				else {
					Shape shape = (Shape) new java.awt.geom.Line2D.Double(fx, fy, tx, ty);
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
			//tx = toX;
			//ty = toY;
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
			buildFatLine(fx, fy, tx, ty, segno, g);
			
			
			if (!dropOldest)
				g.drawLine(fx, fy, tx, ty);
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
		
		
		
		if (endsAtBlock) {
			if ((from instanceof ProcessBlock || from instanceof ExtPortBlock  /* from instanceof Enclosure */   
					|| from instanceof IIPBlock ) && to != null && (to instanceof ProcessBlock
							|| to instanceof ExtPortBlock /* || to instanceof Enclosure */)) {
				Arrowhead ah = new Arrowhead(fx, fy, toX, toY);  
				ah.draw(g);	
				driver.fpArrowEndA = null;
				driver.fpArrowEndB = null;
				//driver.currentArrow = null;
			}

		} else if (endsAtLine)  
			drawCircle(g, toX, toY, Color.BLACK, 6);

		if (toX != -1 && (endsAtBlock || endsAtLine)) {
			if (upStreamPort != null
					&& (from instanceof ProcessBlock || from instanceof Enclosure /* || from instanceof ExtPortBlock */)) {
				if (upStreamPort.equals("*")) {
					drawCircleFrom(g, fromX, fromY, toX, toY, Color.BLUE, 8);
					
				} else if (from.visible) {
					g.setColor(Color.BLUE);
					int y = fromY + driver.gFontHeight;
					int x2 = fromX + driver.gFontWidth;
					g.setColor(Color.BLACK);
					g.drawString(upStreamPort, x2, y);
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
					g.setColor(Color.BLUE);
					int y = toY - driver.gFontHeight / 2;
					x = toX - driver.gFontWidth * (downStreamPort.length() + 1);
					g.setColor(Color.BLACK);
					if (!endsAtLine && to != null && to.multiplex)
						x -= 20;
					g.drawString(downStreamPort, x, y);
				}
				g.setColor(Color.BLACK);
			}
		}
		if (extraArrowhead != null)  
			extraArrowhead.draw(g); 
		
		
		
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

		s = item.get("toid").trim();
		toId = Integer.parseInt(s);
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
		diag.jpm = new JPopupMenu("            Arrow-related Actions");
		// driver.curPopup = jpm;
		//diag.jpm.setLocation(fromX + 100, fromY + 100);
		diag.jpm.setVisible(true);
		JLabel label2 = new JLabel();
		label2.setText(diag.jpm.getLabel());
		label2.setFont(driver.fontg);
		// label2.setForeground(Color.BLUE);
		diag.jpm.add(label2);
		diag.jpm.addSeparator();
		JMenuItem menuItem;
		Block from = diag.blocks.get(new Integer(fromId));
		Block to = diag.blocks.get(new Integer(toId));
		Arrow a = this.findLastArrowInChain();
		if (a == null) {
			MyOptionPane.showMessageDialog(driver,
					"Can't find connecting arrow",
					MyOptionPane.ERROR_MESSAGE);
			return;
		}
		to = diag.blocks.get(new Integer(a.toId));
		if (!(from instanceof FileBlock || from instanceof PersonBlock || from instanceof ReportBlock || from instanceof LegendBlock ||
				to instanceof FileBlock || to instanceof PersonBlock || to instanceof ReportBlock || to instanceof LegendBlock 	) ) {
		if (!(from instanceof ExtPortBlock) && !(from instanceof IIPBlock)) {
			menuItem = new JMenuItem("Edit Upstream Port Name");
			menuItem.addActionListener(this);
			diag.jpm.add(menuItem);
		}
		if (!(to instanceof ExtPortBlock) && endsAtBlock) {
			menuItem = new JMenuItem("Edit Downstream Port Name");
			menuItem.addActionListener(this);
			diag.jpm.add(menuItem);
		}
		diag.jpm.addSeparator();
		}
		
		if ((from instanceof ProcessBlock || from instanceof ExtPortBlock) && (to instanceof ProcessBlock || from instanceof ExtPortBlock)) {

			menuItem = new JMenuItem("Toggle Upstream Port Automatic / Normal");
			menuItem.addActionListener(this);
			diag.jpm.add(menuItem);

			menuItem = new JMenuItem(
					"Toggle Downstream Port Automatic / Normal");
			menuItem.addActionListener(this);
			diag.jpm.add(menuItem);
		}
		
		diag.jpm.addSeparator();
		menuItem = new JMenuItem("Set Capacity");
		menuItem.addActionListener(this);
		diag.jpm.add(menuItem);
		menuItem = new JMenuItem("Remove Capacity");
		menuItem.addActionListener(this);
		diag.jpm.add(menuItem);
		diag.jpm.addSeparator();
		menuItem = new JMenuItem("Toggle DropOldest");
		menuItem.addActionListener(this);
		diag.jpm.add(menuItem);

		diag.jpm.addSeparator();
		menuItem = new JMenuItem("Drag Tail");
		menuItem.addActionListener(this);
		diag.jpm.add(menuItem);
		menuItem = new JMenuItem("Drag Head");
		menuItem.addActionListener(this);
		diag.jpm.add(menuItem);
		menuItem = new JMenuItem("Drag New or Existing Bend");
		menuItem.addActionListener(this);
		diag.jpm.add(menuItem);
		diag.jpm.addSeparator();
		menuItem = new JMenuItem("Add Extra Arrowhead");
		menuItem.addActionListener(this);
		diag.jpm.add(menuItem);
		menuItem = new JMenuItem("Remove Extra Arrowhead");
		menuItem.addActionListener(this);
		diag.jpm.add(menuItem);

		diag.jpm.addSeparator();
		menuItem = new JMenuItem("Delete");
		diag.jpm.add(menuItem);
		menuItem.addActionListener(this);
		diag.driver.currentArrow = null;
		
	}

	public void actionPerformed(ActionEvent e) {
		String s = e.getActionCommand();

		diag.jpm = null;

		if (s.equals("Edit Upstream Port Name")) {

			String ans = (String) MyOptionPane.showInputDialog(driver,
					   "Enter or change text", "Edit upstream port name",
					MyOptionPane.PLAIN_MESSAGE, null, null, upStreamPort);
			
			if (ans != null /* && ans.length() > 0*/ ) {
				ans = ans.trim();
				Block b = diag.blocks.get(new Integer(fromId));
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
					String proc = driver.curDiag.blocks.get(fromId).desc;
					MyOptionPane.showMessageDialog(driver,
							"Duplicate port name: " + proc + "." + ans, MyOptionPane.WARNING_MESSAGE);
					upStreamPort = "";
					return;
				}
				upStreamPort = ans;

				if (b.type.equals(Block.Types.EXTPORT_IN_BLOCK)
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
				Block b = diag.blocks.get(new Integer(arr.toId));

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
					String proc = driver.curDiag.blocks.get(toId).desc;
					MyOptionPane.showMessageDialog(driver,
							"Duplicate port name: " + proc + "." + ans, MyOptionPane.WARNING_MESSAGE);
					arr.downStreamPort = "";
											
					return;
				}
				
				arr.downStreamPort = ans;
				
				if (b.type.equals(Block.Types.EXTPORT_OUT_BLOCK)) {
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
			
		}  if (s.equals("Drag Tail")) {
			//tailMarked = true;
			driver.tailMark = new Point(fromX, fromY);
			driver.arrowEndForDragging = this;
			driver.repaint();
			diag.changed = true;
			return;

		}  if (s.equals("Drag Head")) {
			//headMarked = true;
			driver.headMark = new Point(toX, toY);
			driver.arrowEndForDragging = this;
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
			Point2D p = new Point2D((double)driver.curx, (double)driver.cury);
			int fx = fromX;
			int fy = fromY; 
			int tx, ty;			
			if (bends != null) {				
				for (Bend bend : bends) {
					tx = bend.x;
					ty = bend.y;
					if (driver.pointInLine(p, fx, fy, tx, ty)) {
						extraArrowhead = new Arrowhead(fx, fy, driver.curx, driver.cury);
						diag.changed = true;
						return;
					}
					fx = tx;
					fy = ty;					
				}				
			}
			tx = toX;
			ty = toY;
			if (driver.pointInLine(p, fx, fy, tx, ty)) 
				extraArrowhead = new Arrowhead(fx, fy, driver.curx, driver.cury);	
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
				diag.delArrow(this);

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

	void createBend(int bendx, int bendy) {
				
		int segNo = 0;				
		Bend bn = null;
		
		if (!endsAtBlock && !endsAtLine) {
			bends.add(new Bend(bendx, bendy));
			return;
		}
		
		int x1 = fromX;
		int y1 = fromY;
		if (bends != null) {
			
			for (Bend b : bends) {

				if (sameBend(bendx, bendy, b)) {
					bn = b;
					bn.marked = true;
					driver.bendForDragging = bn;
					return;
				}
				
				if (driver.nearpln(bendx, bendy, this, segNo)) {
					bn = new Bend(bendx, bendy);
					if (x1 == b.x) // if line vertical
						bn.x = x1;
					if (y1 == b.y) // if line horizontal
						bn.y = y1;
					bends.add(segNo, bn);
					bn.marked = true;
					driver.bendForDragging = bn;
					return;
				}
				x1 = b.x;
				y1 = b.y;
				segNo++;
			}
		}
		else
			bends = new LinkedList<Bend>();
				
		if (driver.nearpln(bendx, bendy, this, segNo)) {	
			bn = new Bend(bendx, bendy);
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
	
	

	Arrow findLastArrowInChain() {
		if (endsAtBlock)  
			return this;
		if (!endsAtLine)
			return null;

		int id = toId; // endsAtine, so toId must be a line ID
		
		Arrow a;		
		while (true) {
			if (id == -1)  			
				return null;
			
			a = null;
			for (Arrow arrow : diag.arrows.values()) {
				if (id == arrow.id) {
					a = arrow;
					id = a.toId;
					break;
				}
			}
			
			if (a == null)  // no arrow matches id, so return null
				return null;			 
			
			if (a.endsAtBlock)
				return a;
			
			if (!a.endsAtLine)
				return null;
			
			id = a.toId;
		}
	}
	
	boolean checkSides() {
		Block from = diag.blocks.get(new Integer(fromId));
		Block to = diag.blocks.get(new Integer(toId));
		Arrow a = findLastArrowInChain();
		to = diag.blocks.get(new Integer(a.toId));
		if (!(from instanceof ProcessBlock)
				&& !(from instanceof ExtPortBlock))
			return true;
		if (!(to instanceof ProcessBlock) && !(to instanceof ExtPortBlock))
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
		fromY = y;
		id = toId;
		toId = fromId;
		fromId = id;
		
		String st = upStreamPort;
		upStreamPort = downStreamPort;
		downStreamPort = st;
		
		}
	
	// Build rectangle around arrow segment, and add to Shape array
	static Color ltBlue = new Color(173, 216, 230); 
	
	void buildFatLine(int fx, int fy, int tx, int ty, int segNo, Graphics g) {
		
		GeneralPath path = new GeneralPath();
		double x, y;
		final int aDW = driver.zWS; // arrow Detect Width - same as detect edge size
		x = tx - fx;
		y = ty - fy;
		double hypoSqu = (double) (x * x) + (double) (y * y);
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
		Shape sh = path.createTransformedShape(at);

		shapeList.add(sh);
		
		//int xPoints[] = {fx + 6, tx + 6, tx - 6, fx - 6}; 
		//int yPoints[] = {fy - 6, ty - 6, ty + 6, fy + 6}; 
		//Polygon poly = new Polygon(xPoints, yPoints, 4);
		//shapeList.add(poly);
		/*
		Line2D line = new Line2D(fx, fy, tx, ty);
		Point2D p = new Point2D(driver.moveX, driver.moveY);
		double d = 0.0;
		try {
			d = line.distance(p);
		} catch (DegeneratedLine2DException e) {

		}
		*/
									
		if (this == driver.detArr && segNo == driver.detArrSegNo) {
			Color col = g.getColor();
			g.setColor(ltBlue);  
			((Graphics2D)g).fill(sh);				
			g.setColor(col);	
		}
		 
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
			PathIterator it = new FlatteningPathIterator(((Shape) shape).getPathIterator( null ), FLATNESS );
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
	
class Arrowhead {
	int fx, fy, toX, toY;	
	Shape shape;
		
	Arrowhead(int fx, int fy, int toX, int toY) {
		this.fx = fx; 
		this.fy = fy;
		this.toX = toX; 
		this.toY = toY;		
		
		}
	
	void draw(Graphics g) {
		
			// toX/toY is tip of arrow and fx/fy is a point on the line -
			// fx/fy is used to determine direction & angle

			//AffineTransform at = AffineTransform.getTranslateInstance(toX, toY);
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

			/*
			// theta is in radians
			double s, t;
			s = toY - fy; // calculate slopes.
			t = toX - fx;
			if (t != 0) {
				s = s / t;
				theta = Math.atan(s);
				if (t < 0)
					theta += Math.PI;
			} else if (s < 0)
				theta = -(Math.PI / 2);
			else
				theta = Math.PI / 2;
			*/

			AffineTransform at = AffineTransform.getTranslateInstance(toX, toY);
			if (toX == fx)   // vertical line
				if (fy > toY)
					at.quadrantRotate(3);
				else
					at.quadrantRotate(1);
			else		
				at.rotate(toX - fx, toY - fy);
			shape = at.createTransformedShape(path);   
			
				//if (checkStatus == Status.UNCHECKED)
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
 

