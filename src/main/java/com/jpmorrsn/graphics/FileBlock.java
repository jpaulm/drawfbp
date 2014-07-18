package com.jpmorrsn.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

public class FileBlock extends Block {
	
	FileBlock(Diagram ctlr) {
		super(ctlr);
		type = Block.Types.FILE_BLOCK;
		width = 64;
		height = 72;
		//calcEdges();
	}
	
	@Override
	void draw(Graphics2D g) {
		if (!visible && this != driver.selBlockP) {
			showZones(g);
			return;
		}
		GeneralPath gp = new GeneralPath();
		g.setColor(Color.BLACK);
		int x = cx - width / 2;		
		int y = cy - height/2 + 4;		
		gp.moveTo(x, y);
		x += width;
		gp.quadTo(x - width/2, y - 10, x, y);
		y += height - 8;
		gp.lineTo(x, y);
		x -= width;
		gp.quadTo(x + width/2, y + 10, x, y);
		gp.closePath();
		//g.draw(gp);
		if (this == driver.selBlockP)
			g.setColor(new Color(255, 255, 200)); // light yellow
			else
			g.setColor(new Color(200, 255, 255)); // light turquoise	
		g.fill(gp);
		g.setColor(Color.BLACK);
		g.draw(gp);
		y = cy - height + height / 2 - 8;
		x = cx - width / 2;
		g.drawArc(x, y, width, 20, 190, 160);	
		if (description != null) {
			centreDesc(g);
		}
		//showZones(g);
		calcDiagMaxAndMin(cx - width / 2, cx + width / 2,
				cy - height / 2, cy + height / 2);
	}
}