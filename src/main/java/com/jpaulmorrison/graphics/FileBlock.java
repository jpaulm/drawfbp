package com.jpaulmorrison.graphics;

import java.awt.Color;
import java.awt.Graphics;
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
	void draw(Graphics g) {
		if (!visible && this != driver.selBlock) {
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
		if (this == driver.selBlock)
			g.setColor(DrawFBP.ly); // light yellow
			else
			g.setColor(DrawFBP.lb); // light turquoise	
		((Graphics2D)g).fill(gp);
		g.setColor(Color.BLACK);
		((Graphics2D)g).draw(gp);
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