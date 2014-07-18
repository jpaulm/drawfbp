package com.jpmorrsn.graphics;

import java.awt.Color;
import java.awt.Graphics2D;

	public class PersonBlock extends Block {
		
		PersonBlock (Diagram ctlr){
			super(ctlr);
			type = Block.Types.PERSON_BLOCK;
			width = 40;
		  	height = 60;
		  	//calcEdges();
		}
		
		@Override
		void draw (Graphics2D g) {
			if (!visible && this != driver.selBlockP) {
				showZones(g);
				return;
			}
			g.setColor(Color.LIGHT_GRAY);
			g.fillRect(cx - width / 2, cy - height / 2, width, height);
			g.setColor(Color.BLACK);
			g.drawOval(cx - 8, cy - height/2 + 2, 16, 20);
			if (this == driver.selBlockP)
				g.setColor(new Color(255, 255, 200)); // light yellow
				else
				g.setColor(new Color(200, 255, 255)); // light turquoise			
			
			g.fillOval(cx - 7, cy - height/2 + 3, 14, 18);
			g.setColor(Color.BLACK);
			g.drawLine(cx, cy - height/2 + 20, cx, cy + 4);  // vert
			g.drawLine(cx, cy + 4, cx - 12, cy + height/2);  // legs
			g.drawLine(cx, cy + 4, cx + 12, cy + height/2);
			g.drawLine(cx, cy - 2, cx - 14, cy + 5);  // arms
			g.drawLine(cx, cy - 2, cx + 14, cy + 5);
			//showZones(g);
			calcDiagMaxAndMin(cx - width / 2, cx + width / 2,
					cy - height / 2, cy + height / 2);
			if (description != null) {
				centreDesc(g);
			}
			}
}
