package com.jpaulmorrison.graphics;

import java.awt.*;

public class LegendBlock extends Block {
	//private int maxw = 0, totht = 0;

	LegendBlock(Diagram ctlr) {
		super(ctlr);
		type = Block.Types.LEGEND_BLOCK;
		width = driver.gFontWidth * 12 + 4;
		height = driver.gFontHeight * 4 + 4;
		// calcEdges();
	}

	@Override
	void draw(Graphics2D g) {
		if (!visible && this != driver.selBlock) {
			showZones(g);
			return;
		}
		
		if (description != null && !(description.trim().equals(""))) {
			g.setColor(Color.BLACK);
			// g.setColor(Color.GRAY);
			//drawDesc(g);	
			centreDesc(g);
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