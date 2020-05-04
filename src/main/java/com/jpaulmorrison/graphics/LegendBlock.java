package com.jpaulmorrison.graphics;

import java.awt.*;

public class LegendBlock extends Block {
	//private int maxw = 0, totht = 0;

	LegendBlock(Diagram diag) {
		super(diag);
		type = Block.Types.LEGEND_BLOCK;
		width = driver.gFontWidth * 12 + 4;
		height = driver.gFontHeight * 4 + 4;
		buildSides();
		calcEdges();
	}

	@Override
	void draw(Graphics g) {
		if (!visible && this != driver.selBlock) {

			showArrowEndAreas(g);

			//blueCircs(g);
			return;
		}
		
		if (desc != null && !(desc.trim().equals(""))) {
			
			if (compareFlag != null && compareFlag.equals("D"))
				g.setColor(Color.GRAY);
			else
				g.setColor(Color.BLACK);
			
			//drawDesc(g);
			//Font oldf = g.getFont();
			//float fl = oldf.getSize2D();
			//Font f = oldf.deriveFont((float)(fl * 1.2));
			//g.setFont(f);
			String str[] = centreDesc();
			int x = textX;
			int y = textY;
			for (int i = 0; i < str.length; i++) {
				g.drawString(str[i], x, y); 
				y += driver.gFontHeight;
			}
			//g.setFont(oldf);
			buildSides();

			showDetectionAreas(g);

		}
		 
		else {
			width = 40;
			height = 15;
			//showArrowEndAreas(g);
			buildSides();

			showDetectionAreas(g);

		}
		
		int tlx = cx - width / 2;
		int tly = cy - height / 2;		
		showCompareFlag(g, tlx, tly);
		 
		// g.setColor(Color.BLACK);
		calcDiagMaxAndMin(tlx, cx + width / 2, tly, cy
				+ height / 2);
		
		 

		 if (this == driver.selBlockM) {
			showArrowEndAreas(g);

			//return;
		}
		//blueCircs(g);	 
	} 
	
}