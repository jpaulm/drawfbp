package com.jpaulmorrison.graphics;

import java.awt.*;

public class LegendBlock extends Block {
	//private int maxw = 0, totht = 0;

	LegendBlock(Diagram diag) {
		super(diag);
		type = Block.Types.LEGEND_BLOCK;
		width = driver.gFontWidth * 12 + 4;
		height = driver.gFontHeight * 4 + 4;
		// calcEdges();
	}

	@Override
	void draw(Graphics g) {
		if (!visible && this != driver.selBlock) {
			showZones(g);
			return;
		}
		
		if (desc != null && !(desc.trim().equals(""))) {
			g.setColor(Color.BLACK);
			// g.setColor(Color.GRAY);
			//drawDesc(g);
			Font oldf = g.getFont();
			float fl = oldf.getSize2D();
			Font f = oldf.deriveFont((float)(fl * 1.2));
			g.setFont(f);
			centreDesc(g);
			g.setFont(oldf);
		}
		 
		else {
			width = 40;
			height = 15;
			showArrowEndAreas(g);
		}
		 
		// g.setColor(Color.BLACK);
		calcDiagMaxAndMin(cx - width / 2, cx + width / 2, cy - height / 2, cy
				+ height / 2);
		
		 
		if (this == driver.selBlockM) {
			showZones(g);
			//return;
		}
		 
	} 
}