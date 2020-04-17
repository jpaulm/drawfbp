package com.jpaulmorrison.graphics;

//import java.awt.AlphaComposite;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.geom.GeneralPath;

import java.util.HashMap;
import java.util.LinkedList;

import com.jpaulmorrison.graphics.DrawFBP.Corner;

public class Enclosure extends Block {
	
	
	DrawFBP.Corner corner = Corner.NONE;
	//LinkedList<SubnetPort> subnetPorts = null;
	boolean editPortName = false;
	boolean changeSubstreamSensitivity = false;
	boolean coloured = true;
	Color vLightBlue = new Color(220, 235, 255);
	
	Color lightBlue = new Color(160, 220, 250);
	
	LinkedList<Block> llb = null;   // blocks wholly enclosed in enclosure
	LinkedList<Arrow> lla = null;   // arrows wholly enclosed in enclosure
    boolean draggingContents = false;
	
	Enclosure(Diagram diag) {
		super(diag);
		type = Block.Types.ENCL_BLOCK; 
		width = 250;
		height = 100;
		corner = Corner.NONE;
		//subnetPorts = new LinkedList<SubnetPort>();
	}
	void buildEncl(HashMap<String, String> item) {
		String s;
		s = item.get("x").trim();
		cx = Integer.parseInt(s);
		s = item.get("y").trim();
		cy = Integer.parseInt(s);
		s = item.get("id").trim();
		id = Integer.parseInt(s);
        type = item.get("type");
        desc = item.get("description");
		s = item.get("height").trim();
		height = Integer.parseInt(s);
		s = item.get("width").trim();
		width = Integer.parseInt(s);
		diag.maxBlockNo = Math.max(id, diag.maxBlockNo);
		calcEdges();
	}
	

	@Override
	void draw(Graphics g) {
		if (!visible && this != driver.selBlock) {

			showArrowEndAreas(g);

			return;
		}
		if (!deleteOnSave)
			g.setColor(Color.BLACK);
		else
			g.setColor(Color.BLUE);
		float dash[] = {4.0f};
		BasicStroke str = (BasicStroke) ((Graphics2D)g).getStroke();
		((Graphics2D)g).setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER, 6.0f, dash, 0.0f));
		int x = cx - width / 2;
		int y = cy - height / 2;
		int x1 = x  + width / 5 ;
		int x2 = x + 4 * width / 5;
		Color col = g.getColor();		
		 
		g.drawLine(x, y, x1, y); // top line
		g.drawLine(x2, y, x + width, y); // top line
		g.drawLine(x + width, y, x + width, y + height);
		g.drawLine(x, y + height, x + width, y + height);
		g.drawLine(x, y, x, y + height);
		((Graphics2D)g).setStroke(str);		
		//int xs = x;
		//int ys = y;
		
		int hh = driver.gFontHeight;
		if (draggingContents) {
			//col = g.getColor();	
			g.drawString("   Use enclosure title area to drag", x1, y - hh - 8);
			//g.setColor(Color.LIGHT_GRAY);
			 g.setColor(new Color(221, 221, 221));			 				
			 ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)); 
			 g.fillRect(x, y, width, height);
			 g.setColor(col);
		}	
		else
			((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));

		// draw small rectangle at top
		
		y += 4;
		g.drawLine(x1, y - hh, x2, y - hh) ; // top
		g.drawLine(x1, y - hh, x1, y  + hh /2 ); // left
		g.drawLine(x1, y  + hh /2 , x2, y + hh /2 ); // bottom
		g.drawLine(x2, y - hh, x2, y + hh /2 ); // right
		
		if (coloured) {
		  g.drawRect(x1, y - hh, x2 - x1, (int)(1.5 * hh));
		  //Color col = g.getColor();
		  g.setColor(vLightBlue);
		  //g.setColor(new Color(221, 221, 221));
		  g.fillRect(x1 + 1, y - hh + 1, x2 - x1 - 2, (int) (1.5 * hh) - 2);
		  g.setColor(col);
		}
		if (desc != null) {
			x = (x1 + x2 - desc.length() * driver.gFontWidth) / 2;
			g.drawString(desc, x, y);
		}

		// following logic draws diagonal arrow at selected corner
		if (corner != Corner.NONE) {
			int leftEdge = cx - width / 2;
			int rgtEdge = cx + width / 2;
			int topEdge = cy - height / 2;
			int botEdge = cy + height / 2;
			if (corner == DrawFBP.Corner.TOPLEFT) {
				x = leftEdge;
				y = topEdge;
				g.drawLine(x - 8, y - 8, x + 8, y + 8);
				g.drawLine(x - 8, y - 8, x - 8, y - 4);
				g.drawLine(x - 8, y - 8, x - 4, y - 8);
				g.drawLine(x + 8, y + 8, x + 8, y + 4);
				g.drawLine(x + 8, y + 8, x + 4, y + 8);
			}
			if (corner == DrawFBP.Corner.BOTTOMLEFT) {
				x = leftEdge;
				y = botEdge;
				g.drawLine(x - 8, y + 8, x + 8, y - 8);
				g.drawLine(x - 8, y + 8, x - 8, y + 4);
				g.drawLine(x - 8, y + 8, x - 4, y + 8);
				g.drawLine(x + 8, y - 8, x + 8, y - 4);
				g.drawLine(x + 8, y - 8, x + 4, y - 8);
			}
			if (corner == DrawFBP.Corner.TOPRIGHT) {
				x = rgtEdge;
				y = topEdge;
				g.drawLine(x - 8, y + 8, x + 8, y - 8);
				g.drawLine(x - 8, y + 8, x - 8, y + 4);
				g.drawLine(x - 8, y + 8, x - 4, y + 8);
				g.drawLine(x + 8, y - 8, x + 8, y - 4);
				g.drawLine(x + 8, y - 8, x + 4, y - 8);
			}
			if (corner == DrawFBP.Corner.BOTTOMRIGHT) {
				x = rgtEdge;
				y = botEdge;
				g.drawLine(x - 8, y - 8, x + 8, y + 8);
				g.drawLine(x - 8, y - 8, x - 8, y - 4);
				g.drawLine(x - 8, y - 8, x - 4, y - 8);
				g.drawLine(x + 8, y + 8, x + 8, y + 4);
				g.drawLine(x + 8, y + 8, x + 4, y + 8);
			}
		}
		/*
		if (subnetPorts != null) {
			g.setColor(Color.RED);
			for (SubnetPort snp : subnetPorts) {
								
				snp.name = snp.eb.description;
				
				if (snp.side == DrawFBP.Side.LEFT) {
					snp.x = cx - width / 2;
					
					//if (snp.name != null && !(snp.name.equals("")))
					//	g.drawString(snp.name, snp.x - 10 - driver.gFontWidth
					//			* snp.name.length(), snp.y - driver.gFontHeight
					//			/ 2);
					if (snp.substreamSensitive) {
						GeneralPath gp = drawSemicircle(snp.x, snp.y, +1);
						((Graphics2D)g).fill(gp);						
					}
				} else { // if RIGHT
					snp.x = cx + width / 2;
					//if (snp.name != null && !(snp.name.equals("")))
					//	g.drawString(snp.name, snp.x + 10, snp.y
					//			- driver.gFontHeight / 2);
					if (snp.substreamSensitive) {
						GeneralPath gp = drawSemicircle(snp.x, snp.y, -1);
						((Graphics2D)g).fill(gp);	
					}
				}
			}
			g.setColor(Color.BLACK);
		}
		*/

		showDetectionAreas(g);

		calcDiagMaxAndMin(x - width / 2, x + width / 2,
				y - height / 2, y + height / 2);
		// blueCircs(g);
		//calcDiagMaxAndMin(xs, xs + width,   // enclosure may have been stretched...
		//		ys, ys + height);
	}
	
	static GeneralPath drawSemicircle(int sx, int sy, int multiplier) {
		GeneralPath gp = new GeneralPath();
		int y = sy - 10;
		gp.moveTo(sx, y);
		gp.curveTo(sx + 15 * multiplier, y + 5, sx + 15 * multiplier, y + 15, sx,
				y + 20);
		gp.lineTo(sx, y);
		gp.closePath();	
		return gp;
	}
	
	void showArrowEndAreas(Graphics g) {
		 
		Color col = g.getColor();
		g.setColor(DrawFBP.grey);   

		//int zW = (int) Math.round(zoneWidth * DrawFBP.scalingFactor / 2);
		g.fillRect(cx - width / 2 - driver.zWS / 2, cy - height / 2 - driver.zWS / 2, driver.zWS, height); // left
		//if (!(this instanceof Enclosure))
		//	g.fillRect(cx - width / 2 - 1, cy - height / 2 - 1, width + 3, 4); // top
		//if (!(this instanceof ReportBlock)) {
			g.fillRect(cx - width / 2 - driver.zWS / 2, cy + height / 2 - driver.zWS / 2 * 2, width + 3, driver.zWS ); // bottom
			g.fillRect(cx + width / 2 - driver.zWS / 2, cy - height / 2 - driver.zWS / 2, driver.zWS, height); // right
		//} else
		//	g.fillRect(cx + width / 2 - 1, cy - height / 2 - 1, 4, height - 12); // right
		g.setColor(col);
		 
	} 
}
