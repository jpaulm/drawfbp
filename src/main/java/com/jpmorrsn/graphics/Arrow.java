package com.jpmorrsn.graphics;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import com.jpmorrsn.graphics.DrawFBP.Side;

public class Arrow implements ActionListener {

	DrawFBP driver;
	int fromX, fromY, toX, toY;
	int lastX, lastY; // "last" x and y
	int fromId, toId, id = 0;
	boolean endsAtBlock, endsAtLine;
	LinkedList<Bend> bends;
	String upStreamPort, downStreamPort;
	//String uspMod;   //  upstream port after lowercasing
	String dspMod;   // downStreamPort after lowercasing 
	DrawFBP.Side fromSide, toSide;
	boolean deleteOnSave = false;
	static Color FOREST_GREEN = new Color(34, 139, 34);
	static Color ORANGE_RED = new Color(255, 69, 0);
	boolean headMarked, tailMarked;
	boolean dropOldest;
	int capacity;
	LegendBlock capLegend;   //Legend block associated with Arrow 
	
	//Arrowhead tipArrowhead = null;    
	Arrowhead extraArrowhead = null;
	
	enum Status {
		UNCHECKED, COMPATIBLE, INCOMPATIBLE
	}

	Status checkStatus = Status.UNCHECKED;

	Diagram diag;

	Arrow(Diagram ctlr) {
		super();
		endsAtBlock = false;
		endsAtLine = false;
		bends = null;
		upStreamPort = null;
		downStreamPort = null;
		toX = -1;   //OK!
		toY = -1;
		toId = -1;
		diag = ctlr;
		driver = ctlr.driver;
		diag.maxArrowNo++;
		id = diag.maxArrowNo;
		//uspMod = null;
		dspMod = null;
	}

	void draw(Graphics2D g) {

		int endX, endY;
		Block from = null;
		if (fromId > -1)
			from = diag.blocks.get(new Integer(fromId));
		Block to = null;
		if (!endsAtLine && toId > -1)
			to = diag.blocks.get(new Integer(toId));
		
		if (toX == -1) 
			endX = diag.xa;
		else
			endX = toX;
		
		if (toY == -1) 
			endY = diag.ya;
		else
			endY = toY;

		g.setColor(Color.GRAY);

		Stroke stroke = g.getStroke();
		ZigzagStroke zzstroke = new ZigzagStroke(stroke, 2, 4);

		if (toX == -1) {
		 g.drawRect(fromX - 3, fromY - 3, 6, 6);
		 return;
		 }

		if (from != null) {
			if (fromSide == Side.TOP)
				fromY = from.cy - from.height / 2;
			else if (fromSide == Side.BOTTOM)
				fromY = from.cy + from.height / 2;
			else if (fromSide == Side.LEFT)
				fromX = from.cx - from.width / 2;
			else if (fromSide == Side.RIGHT)
				fromX = from.cx + from.width / 2;
		}

		if (to != null) {
			if (toSide == Side.TOP)
				toY = to.cy - to.height / 2;
			else if (toSide == Side.BOTTOM)
				toY = to.cy + to.height / 2;
			else if (toSide == Side.LEFT)
				toX = to.cx - to.width / 2;
			else if (toSide == Side.RIGHT)
				toX = to.cx + to.width / 2;
		}

		if (driver.selArrowP == this)
			g.setColor(Color.BLUE);
		else if ((from instanceof ComponentBlock
				|| from instanceof ExtPortBlock || from instanceof Enclosure)
				&& (to instanceof ComponentBlock || to instanceof ExtPortBlock
						|| to instanceof Enclosure || endsAtLine))
			if (checkStatus == Status.UNCHECKED)
				g.setColor(Color.BLACK);
			else if (checkStatus == Status.COMPATIBLE)
				g.setColor(FOREST_GREEN);
			else
				g.setColor(ORANGE_RED);

		else if (from instanceof LegendBlock || to instanceof LegendBlock)
			g.setColor(Color.GRAY);

		int fx, fy, tx, ty;
		fx = fromX;
		fy = fromY;
		//tx = toX;
		//ty = toY;
		//int autoX = -1, autoY = -1;  // only used for automatic ports
		if (bends != null) {
			for (Bend bend : bends) {
				tx = bend.x;
				ty = bend.y;
				if (!dropOldest)
					g.drawLine(fx, fy, tx, ty);
				else {
					Shape shape = new Line2D.Double(fx, fy, tx, ty);
					shape = zzstroke.createStrokedShape(shape);
					g.draw(shape);
					// g.setStroke(stroke);
				}

				if (bend.marked) {
					Color col = g.getColor();
					g.setColor(Color.RED);
					g.drawOval(tx - 5, ty - 5, 10, 10);
					g.setColor(col);
				}
				calcLimits(fx, tx, fy, ty);
				fx = tx;
				fy = ty;
				
			}
		}
		tx = endX;
		ty = endY;
		

		int x = endX;
		if (to != null && endsAtBlock && to.multiplex) {
			String s = to.mpxfactor;
			if (s == null)
				s = " ";
			int i = s.length() * driver.fontWidth + 10;
			x -= i;
		}
		
		if (headMarked) {
			Color col = g.getColor();
			g.setColor(Color.RED);
			g.drawOval(x - 5, toY - 5, 10, 10);
			g.setColor(col);
		}

		if (!dropOldest)
			g.drawLine(fx, fy, tx, ty);
		else {
			Shape shape = new Line2D.Double(fx, fy, tx, ty);
			shape = zzstroke.createStrokedShape(shape);
			g.draw(shape);
			// g.setStroke(stroke);
		}

		if (tailMarked) {
			Color col = g.getColor();
			g.setColor(Color.RED);
			g.drawOval(fromX - 5, fromY - 5, 10, 10);
			g.setColor(col);
		}
		
		calcLimits(fx, x, fy, toY);

		if (!endsAtBlock && !endsAtLine) {
			g.drawRect(fromX - 3, fromY - 3, 6, 6);
			g.drawRect(x - 3, toY - 3, 6, 6);
		} else if (endsAtBlock) {
			if ((from instanceof ComponentBlock || from instanceof ExtPortBlock || from instanceof Enclosure)
					&& (to instanceof ComponentBlock
							|| to instanceof ExtPortBlock || to instanceof Enclosure)) {
				Arrowhead ah = new Arrowhead(fx, fy, toX, toY);  
				ah.draw(g);				
			}

		} else if (endsAtLine) {
			drawCircleTo(g, fx, fy, x, toY, Color.BLACK, 4);
			// g.drawOval(toX - 2, toY - 2, 4, 4);
			// g.fillOval(toX - 2, toY - 2, 4, 4);
		}

		if (toX != -1 && (endsAtBlock || endsAtLine)) {
			if (upStreamPort != null
					&& (from instanceof ComponentBlock || from instanceof Enclosure)) {
				if (upStreamPort.equals("*")) {
					drawCircleFrom(g, fromX, fromY, endX, endY, Color.BLUE, 8);
					// g.setColor(Color.BLUE);
					// g.drawOval(fromX, fromY - 4, 8, 8);
					// g.fillOval(fromX, fromY - 4, 8, 8);
				} else if (from.visible) {
					g.setColor(Color.BLUE);
					int y = fromY + driver.fontHeight;
					int x2 = fromX + driver.fontWidth;
					g.drawString(upStreamPort, x2, y);
				}
				g.setColor(Color.BLACK);
			}
			if (downStreamPort != null
					&& !endsAtLine
					&& to != null
					&& (to instanceof ComponentBlock || to instanceof Enclosure)) {
				if (downStreamPort.equals("*")) {
					drawCircleTo(g, fx, fy, toX, toY, Color.BLUE, 8);
					// g.setColor(Color.BLUE);
					// g.drawOval(x - 8, toY - 4, 8, 8);
					// g.fillOval(x - 8, toY - 4, 8, 8);
				} else if (to.visible) {
					g.setColor(Color.BLUE);
					int y = toY - driver.fontHeight / 2;
					x = toX - driver.fontWidth * (downStreamPort.length() + 1);
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
			diag.maxX = Math.max(x2, diag.maxX);
			diag.minX = Math.min(x1, diag.minX);
		} else {
			diag.maxX = Math.max(x1, diag.maxX);
			diag.minX = Math.min(x2, diag.minX);
		}
		if (y1 < y2) {
			diag.maxY = Math.max(y2, diag.maxY);
			diag.minY = Math.min(y1, diag.minY);
		} else {
			diag.maxY = Math.max(y2, diag.maxY);
			diag.minY = Math.min(y1, diag.minY);
		}
	}

	

	void drawCircleFrom(Graphics2D g, int fx, int fy, int tx, int ty,
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

	void drawCircleTo(Graphics2D g, int fx, int fy, int tx, int ty,
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

	String serialize() {

		String s = "<connection> <fromx>" + fromX + "</fromx> " + "<fromy>"
				+ fromY + "</fromy> " + "<tox>" + toX + "</tox> " + "<toy>"
				+ toY + "</toy> " + "<fromid>" + fromId + "</fromid> "
				+ "<toid>" + toId + "</toid> " + "<id>" + id + "</id> ";
		if (endsAtLine)
			s += "<endsatline/> ";
		if (upStreamPort != null) {
			s += "<upstreamport>" + upStreamPort + "</upstreamport>";
		}
		if (downStreamPort != null) {
			s += "<downstreamport>" + downStreamPort + "</downstreamport>";
		}
		if (dropOldest) {
			s += "<dropoldest/>";
		}
		if (bends != null) {
			s += "<bends> ";

			for (Bend bend : bends) {

				s += "<bend> <x>" + bend.x + "</x> <y> " + bend.y
						+ "</y> </bend>\n ";
			}
			s += "</bends> ";
		}
		if (fromSide == Side.LEFT)
			s += "<fromside> L </fromside>";
		else if (fromSide == Side.RIGHT)
			s += "<fromside> R </fromside>";
		else if (fromSide == Side.TOP)
			s += "<fromside> T </fromside>";
		else if (fromSide == Side.BOTTOM)
			s += "<fromside> B </fromside>";

		if (toSide == Side.LEFT)
			s += "<toside> L </toside>";
		else if (toSide == Side.RIGHT)
			s += "<toside> R </toside>";
		else if (toSide == Side.TOP)
			s += "<toside> T </toside>";
		else if (toSide == Side.BOTTOM)
			s += "<toside> B </toside>";

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
		s = item.get("dropoldest");
		if (s != null)
			dropOldest = true;
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

		endsAtBlock = true;
		endsAtLine = false;
		s = item.get("fromside");
		if (s != null) {
			s = s.trim();
			if (s.equals("L"))
				fromSide = Side.LEFT;
			else if (s.equals("R"))
				fromSide = Side.RIGHT;
			else if (s.equals("T"))
				fromSide = Side.TOP;
			else if (s.equals("B"))
				fromSide = Side.BOTTOM;
		}
		s = item.get("toside");
		if (s != null) {
			s = s.trim();
			if (s.equals("L"))
				toSide = Side.LEFT;
			else if (s.equals("R"))
				toSide = Side.RIGHT;
			else if (s.equals("T"))
				toSide = Side.TOP;
			else if (s.equals("B"))
				toSide = Side.BOTTOM;
		}

	}

	void buildArrowPopupMenu() {
		diag.jpm = new JPopupMenu("            Arrow-related Actions");
		// driver.curPopup = jpm;
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
		if (!(from instanceof ExtPortBlock) && !(from instanceof IIPBlock)) {
			menuItem = new JMenuItem("Edit Upstream Port Name");
			menuItem.addActionListener(this);
			diag.jpm.add(menuItem);
		}
		if (!(to instanceof ExtPortBlock)) {
			menuItem = new JMenuItem("Edit Downstream Port Name");
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
		menuItem = new JMenuItem("Toggle Upstream Port Automatic / Normal");
		menuItem.addActionListener(this);
		diag.jpm.add(menuItem);
		
		menuItem = new JMenuItem("Toggle Downstream Port Automatic / Normal");
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
		//menuItem = new JMenuItem("Exit");
		//jpm.add(menuItem);
		//menuItem.addActionListener(this);
		//diag.curMenuRect = new Rectangle(p.x, p.y, d.width, d.height);
		//return jpm;

	}

	public void actionPerformed(ActionEvent e) {
		String s = e.getActionCommand();

		diag.jpm = null;

		if (s.equals("Edit Upstream Port Name")) {

			String ans = (String) MyOptionPane.showInputDialog(driver.frame,
					   "Enter or change text", "Edit upstream port name",
					JOptionPane.PLAIN_MESSAGE, null, null, upStreamPort);
			
			if (ans != null/* && ans.length() > 0 */) {
				Block b = diag.blocks.get(new Integer(fromId));
				// upStreamPort = ans;
				diag.changed = true;
				boolean found = false;
				for (Arrow a : diag.arrows.values()) {
					if (a.fromId == fromId && a.upStreamPort != null
							&& a.upStreamPort.equals(ans)
							&& !(upStreamPort.equals(ans)) || 
						a.toId == fromId
							&& a.downStreamPort != null
							&& a.downStreamPort.equals(ans))
						found = true;					
				}
				if (found) {
					MyOptionPane.showMessageDialog(driver.frame,
							"Duplicate port name: " + ans);
					// upStreamPort = "";
					// return;
				}
				upStreamPort = ans;

				if (b.type.equals(Block.Types.EXTPORT_IN_BLOCK)
						|| b instanceof IIPBlock) {
					MyOptionPane.showMessageDialog(driver.frame,
							"Upstream port must be blank");
					upStreamPort = "";
				}
			}
			driver.frame.repaint();

		} else if (s.equals("Edit Downstream Port Name") && endsAtBlock) {
			String ans = (String) MyOptionPane.showInputDialog(driver.frame,
					 "Enter or change text", "Edit downstream port name",
					JOptionPane.PLAIN_MESSAGE, null, null, downStreamPort);
			
			
			if (ans != null /* && ans.length() > 0 */) {
				
				Block b = diag.blocks.get(new Integer(toId));

				diag.changed = true;
				boolean found = false;
				for (Arrow a : diag.arrows.values()) {
					if (a.fromId == toId && a.upStreamPort != null
							&& a.upStreamPort.equals(ans) || 
						a.toId == toId
							&& a.downStreamPort != null
							&& a.downStreamPort.equals(ans) 
						    && !(downStreamPort.equals(ans)))
						found = true;
				}
				if (found) {
					MyOptionPane.showMessageDialog(driver.frame,
							"Duplicate port name: " + ans);
					// downStreamPort = "";
					// return;
				}
				
				downStreamPort = ans;
				
				if (b.type.equals(Block.Types.EXTPORT_OUT_BLOCK)) {
					MyOptionPane.showMessageDialog(driver.frame,
							"Downstream port must be blank");
					downStreamPort = "";
				}
			}
			driver.frame.repaint();
			
		} else if (s.equals("Set Capacity")) {
			
			String capString = null;
			if (capacity == 0)
				capString = "";
			else
				capString = Integer.toString(capacity);
			String ans = (String) MyOptionPane.showInputDialog(driver.frame,
					"Enter or change text", "Set Capacity",
					JOptionPane.PLAIN_MESSAGE, null, null, capString);
			if ((ans != null) && (ans.length() > 0)) {
				capacity = Integer.parseInt(ans);
				if (capLegend == null) {
					diag.xa = 2;  // get around fudge in DrawFBP
					diag.ya = 2;  // get around fudge in DrawFBP
					capLegend = (LegendBlock) diag.driver.createBlock(
							Block.Types.LEGEND_BLOCK, false);

					int x = toX;
					int y = toY;
					if (bends != null) {
						Bend bd = bends.peek();
						x = bd.x;
						y = bd.y;
					}

					capLegend.cx = (fromX + x) / 2;
					capLegend.cy = (fromY + y) / 2 + 20;
					if (fromX == x)
						capLegend.cx -= 20;
				}
				capLegend.description = "(" + capacity + ")";
			}
			driver.frame.repaint();
			diag.changed = true;
			
		} else if (s.equals("Remove Capacity")) {
			capacity = 0;
			diag.delBlock(capLegend, false);
			capLegend = null;
			driver.frame.repaint();
			diag.changed = true;

		} else if (s.equals("Toggle Upstream Port Automatic / Normal")) {
			if (upStreamPort == null || !upStreamPort.equals("*"))
				upStreamPort = "*";
			else
				upStreamPort = null;
			driver.frame.repaint();
			diag.changed = true;

		} else if (s.equals("Toggle Downstream Port Automatic / Normal")) {
			if (downStreamPort == null || !downStreamPort.equals("*"))
				downStreamPort = "*";
			else
				downStreamPort = null;
			driver.frame.repaint();
			diag.changed = true;

		} else if (s.equals("Toggle DropOldest")) {
			dropOldest = !dropOldest;		
			
		} else if (s.equals("Drag Tail")) {
			tailMarked = true;
			driver.arrowEndForDragging = this;

		} else if (s.equals("Drag Head")) {
			headMarked = true;
			driver.arrowEndForDragging = this;

		} else if (s.equals("Drag New or Existing Bend")) {
			createBend(driver.curx, driver.cury);
			
		} else if (s.equals("Add Extra Arrowhead")) {
			Point p = new Point(driver.curx, driver.cury);
			int fx = fromX;
			int fy = fromY; 
			int tx, ty;			
			if (bends != null) {				
				for (Bend bend : bends) {
					tx = bend.x;
					ty = bend.y;
					if (pointInLine(p, fx, fy, tx, ty)) {
						extraArrowhead = new Arrowhead(fx, fy, driver.curx, driver.cury);
						return;
					}
					fx = tx;
					fy = ty;					
				}				
			}
			tx = toX;
			ty = toY;
			if (pointInLine(p, fx, fy, tx, ty)) 
				extraArrowhead = new Arrowhead(fx, fy, driver.curx, driver.cury);			
			return;
			
		} else if (s.equals("Remove Extra Arrowhead")) {
			extraArrowhead = null;
			return;
			 
		} else if (s.equals("Delete")) {

			if (JOptionPane.YES_OPTION == MyOptionPane.showConfirmDialog(
					driver.frame, "Do you want to delete this arrow?", "Delete arrow",
					 JOptionPane.YES_NO_OPTION)) {
				diag.delArrow(this);

				diag.changed = true;
				diag.currentArrow = null;

			}

			driver.frame.repaint();
			diag.foundArrow = null;

		}
		//if (s.equals("Exit")) {
		//	diag.foundArrow = null;
		//	driver.frame.repaint();
		//}
	}

	void createBend(int bendx, int bendy) {
		Bend bn = null;
		int index = 0;
		if (bends == null) {
			if (driver.nearpln(bendx, bendy, fromX, fromY, toX, toY)) {
				bends = new LinkedList<Bend>();
				bn = new Bend(bendx, bendy);
				if (fromX == toX) // if line vertical
					bn.x = fromX;
				if (fromY == toY) // if line horizontal
					bn.y = fromY;
				bends.add(bn);
				bn.marked = true;
				driver.bendForDragging = bn;
				return;
			}
		} else {
			int x = fromX;
			int y = fromY;
			Object[] oa = bends.toArray();
			for (Object o : oa) {
				Bend b = (Bend) o;
				if (sameBend(bendx, bendy, b)) {
					bn = b;
					bn.marked = true;
					driver.bendForDragging = bn;
					return;
				}
				if (driver.nearpln(bendx, bendy, x, y, b.x, b.y)) {
					bn = new Bend(bendx, bendy);
					if (x == b.x) // if line vertical
						bn.x = x;
					if (y == b.y) // if line horizontal
						bn.y = y;
					bends.add(index, bn);
					bn.marked = true;
					driver.bendForDragging = bn;
					return;
				}
				x = b.x;
				y = b.y;
				index++;
			}

			if (driver.nearpln(bendx, bendy, x, y, toX, toY)) {
				bn = new Bend(bendx, bendy);
				if (x == toX) // if line vertical
					bn.x = x;
				if (y == toY) // if line horizontal
					bn.y = y;
				bends.add(bn);
				bn.marked = true;
				driver.bendForDragging = bn;
			}
		}
	}

	boolean sameBend(int x1, int y1, Bend b) {
		return ((x1 - b.x) * (x1 - b.x) + (y1 - b.y) * (y1 - b.y)) < 6 * 6;
	}
	
	boolean pointInLine(Point p, int fx, int fy, int tx, int ty){
		double d = Line2D.ptLineDist((double) fx, (double) fy, (double) tx, (double) ty, (double) p.x, (double) p.y);
		return d <= 6.0;
	}

	Arrow findTerminalArrow() {
		if (endsAtBlock)
			return this;
		int id = toId;		
		while (true) {
			for (Arrow arrow : diag.arrows.values()) {
				if (id == arrow.id) {
					if (arrow.endsAtBlock)
						return arrow;
					
					//else id = arrow.toId;
						
				}
			}
			return null;
		}
	}

	boolean touches(Block b, int x, int y) {
		DrawFBP.Side side = null;
		if (driver.nearpln(x, y, b.cx - b.width / 2, b.cy - b.height / 2, b.cx
				- b.width / 2, b.cy + b.height / 2)) {
			side = Side.LEFT;
		}
		if (driver.nearpln(x, y, b.cx - b.width / 2, b.cy - b.height / 2, b.cx
				+ b.width / 2, b.cy - b.height / 2)) {
			side = Side.TOP;
		}
		if (driver.nearpln(x, y, b.cx + b.width / 2, b.cy - b.height / 2, b.cx
				+ b.width / 2, b.cy + b.height / 2)) {
			side = Side.RIGHT;
		}
		if (driver.nearpln(x, y, b.cx - b.width / 2, b.cy + b.height / 2, b.cx
				+ b.width / 2, b.cy + b.height / 2)) {
			side = Side.BOTTOM;
		}
		if (side != null) {
			if (tailMarked)
				fromSide = side;
			if (headMarked)
				toSide = side;
			return true;
		}
		return false;
	}

	boolean checkSides() {
		Block from = diag.blocks.get(new Integer(fromId));
		Block to = diag.blocks.get(new Integer(toId));
		if (!(from instanceof ComponentBlock)
				&& !(from instanceof ExtPortBlock))
			return true;
		if (!(to instanceof ComponentBlock) && !(to instanceof ExtPortBlock))
			return true;
		// if (fromSide == DrawFBP.Side.LEFT || fromSide == DrawFBP.Side.TOP)
		// return false;
		// if (toSide == DrawFBP.Side.BOTTOM)
		// return false;
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
		DrawFBP.Side s = toSide;
		toSide = fromSide;
		fromSide = s;
		String st = upStreamPort;
		upStreamPort = downStreamPort;
		downStreamPort = st;
		
		}
	
	

	Arrow makeCopy(Diagram d) {
		Arrow arr = new Arrow(d);
		arr.fromX = this.fromX;
		arr.fromY = this.fromY;
		arr.toX = this.toX;
		arr.toY = this.toY;
		arr.lastX = this.lastX;
		arr.lastY = this.lastY;
		arr.fromId = this.fromId;
		arr.toId = this.toId;
		arr.id = this.id;
		arr.endsAtBlock = this.endsAtBlock;
		arr.endsAtLine = this.endsAtLine;
		if (this.bends != null) {
			arr.bends = new LinkedList<Bend>();
			for (Bend b : this.bends) {
				Bend b2 = new Bend(b.x, b.y);
				arr.bends.add(b2);
			}
		}
		arr.upStreamPort = this.upStreamPort;
		arr.downStreamPort = this.downStreamPort;
		arr.fromSide = this.fromSide;
		arr.toSide = this.toSide;
		arr.diag = d;
		return arr;

	}
	
	/* Thanks to Jerry Huxtable 
	 *   http://www.jhlabs.com/java/java2d/strokes/
	 */
	
	public class ZigzagStroke implements Stroke {
		private float amplitude = 10.0f;
		private float wavelength = 10.0f;
	    private Stroke stroke;
		private static final float FLATNESS = 1;

		public ZigzagStroke( Stroke stroke, float amplitude, float wavelength ) {
	        this.stroke = stroke;
	        this.amplitude = amplitude;
	        this.wavelength = wavelength;
		}

		public Shape createStrokedShape( Shape shape ) {
			GeneralPath result = new GeneralPath();
			PathIterator it = new FlatteningPathIterator( shape.getPathIterator( null ), FLATNESS );
			float points[] = new float[6];
			float moveX = 0, moveY = 0;
			float lastX = 0, lastY = 0;
			float thisX = 0, thisY = 0;
			int type = 0;
			boolean first = false;
			float next = 0;
	        int phase = 0;

			float factor = 1;

			while ( !it.isDone() ) {
				type = it.currentSegment( points );
				switch( type ){
				case PathIterator.SEG_MOVETO:
					moveX = lastX = points[0];
					moveY = lastY = points[1];
					result.moveTo( moveX, moveY );
					first = true;
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
						float r = 1.0f/distance;
						float angle = (float)Math.atan2( dy, dx );
						while ( distance >= next ) {
							float x = lastX + next*dx*r;
							float y = lastY + next*dy*r;
	                        float tx = amplitude*dy*r;
	                        float ty = amplitude*dx*r;
							if ( (phase & 1) == 0 )
	                            result.lineTo( x+amplitude*dy*r, y-amplitude*dx*r );
	                        else
	                            result.lineTo( x-amplitude*dy*r, y+amplitude*dx*r );
							next += wavelength;
							phase++;
						}
					}
					next -= distance;
					first = false;
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
		
	Arrowhead(int fx, int fy, int toX, int toY) {
		this.fx = fx; 
		this.fy = fy;
		this.toX = toX; 
		this.toY = toY;
		}
	
	void draw(Graphics2D g) {
		
			// toX/toY is tip of arrow and fx/fy is a point on the line -
			// fx/fy is used to determine direction & angle

			AffineTransform at = AffineTransform.getTranslateInstance(toX, toY);
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

			at.rotate(theta);
			// at.rotate(theta,toX,toY);
			Shape shape = at.createTransformedShape(path);
			if (checkStatus == Status.UNCHECKED)
				g.setColor(Color.BLACK);
			else if (checkStatus == Status.COMPATIBLE)
				g.setColor(FOREST_GREEN);
			else
				g.setColor(ORANGE_RED);
			g.fill(shape);
			g.draw(shape);
		}
	}
}
 

