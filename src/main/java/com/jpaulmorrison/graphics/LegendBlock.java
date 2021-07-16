package com.jpaulmorrison.graphics;

import java.awt.*;

public class LegendBlock extends Block {
	//private int maxw = 0, totht = 0;

	LegendBlock(Diagram diag) {
		super(diag);
		typeCode = Block.Types.LEGEND_BLOCK;
		width = driver.gFontWidth * 12 + 4;
		height = driver.gFontHeight * 4 + 4;
		buildSideRects();
		centreDesc();
	}

	@Override
	void draw(Graphics g) {
		if (!visible && this != driver.selBlock) {

			showArrowEndAreas(g);

			//blueCircs(g);
			return;
		}
		
		if (desc != null && !(desc.trim().equals(""))) {
			
			
			g.setColor(Color.BLACK);			
			//g.drawRect(cx - 2, cy - 2, 6, 6);
			
			//drawDesc(g);
			//Font oldf = g.getFont();
			//float fl = oldf.getSize2D();
			//Font f = oldf.deriveFont((float)(fl * 1.2));
			//g.setFont(f);
			g.setFont(driver.fontg);
			//String str[] = centreDesc();
			String str[] = desc.split("\n");
			//int x = textX;
			//int y = textY;
			
			int x = cx - width / 2;
			int y = cy - height / 2;
			
			//textY = y;
			//textX = x;
			
			y += driver.gFontHeight + 2;
			//int right = 0;
			for (int i = 0; i < str.length; i++) {
				g.drawString(str[i], x, y); 
				y += driver.gFontHeight;
				//right = Math.max(right,  x + driver.gFontWidth * str[i].length());
			}
			
			
			topEdge = cy - height / 2;;
			botEdge = cy + height / 2;
			leftEdge = cx - width / 2;
			rightEdge = cx + width / 2;
			//width = rgtEdge - leftEdge;
			//height = botEdge - topEdge;
			
			buildSideRects();
			//calcEdges();

			showDetectionAreas(g);

		}
		 
		else {
			width = 40;
			height = 15;
			//showArrowEndAreas(g);
			buildSideRects();

			showDetectionAreas(g);

			leftEdge = cx - width / 2 - 20;
			rightEdge = cx + width / 2 + 20;
			topEdge = cy - height / 2;
			botEdge = cy + height / 2 + 40;
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