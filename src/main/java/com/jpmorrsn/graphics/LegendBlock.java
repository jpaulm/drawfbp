package com.jpmorrsn.graphics;

import java.awt.*;

public class LegendBlock extends Block {
	//private int maxw = 0, totht = 0;

	LegendBlock(Diagram ctlr) {
		super(ctlr);
		type = Block.Types.LEGEND_BLOCK;
		width = driver.fontWidth * 12 + 4;
		height = driver.fontHeight * 4 + 4;
		// calcEdges();
	}

	@Override
	void draw(Graphics2D g) {
		
		
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
		
		if (this == driver.selBlock) {
			showZones(g);
			//return;
		}		
	}
	
	/*

	void drawDesc(Graphics2D g) {
		String str = null;
		String st2 = description;
		maxw = 0;
		totht = 0;

		int y = cy - height / 2 + driver.fontHeight;
		int j;
		for (int i = 0;; i++) {
			j = st2.indexOf('\n');
			if (j > -1) {
				str = st2.substring(0, j);
				st2 = st2.substring(j + 1);
			} else
				str = st2;

			maxw = Math.max(maxw, str.length() * driver.fontWidth
					+ driver.fontWidth * 2);
			if (g != null)
				g.drawString(str, cx - maxw / 2 - driver.fontWidth, y);

			if (j == -1)
				break;
			y += driver.fontHeight;
		}
		totht += 2 * driver.fontHeight;
		height = totht + 2 * driver.fontHeight;  // to provide enough space to detect clicka
		width = maxw + 2 * driver.fontWidth;   //   ditto
		calcEdges();
	}
	
	 
	void setLegendSize() {
		String str = null;
		String st2 = description;
		maxw = 0;
		totht = 0;

		int j;
		for (int i = 0;; i++) {
			j = st2.indexOf('\n');
			if (j > -1) {
				str = st2.substring(0, j);
				st2 = st2.substring(j + 1);
			} else
				str = st2;

			maxw = Math.max(maxw, str.length() * driver.fontWidth
					+ driver.fontWidth * 2);
			if (j == -1)
				break;

			totht = i * driver.fontHeight;
		}
		totht += 2 * driver.fontHeight;
		height = totht;
		width = maxw;

		calcEdges();
	}
	*/
}