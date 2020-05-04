package com.jpaulmorrison.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.EmptyStackException;
import java.util.Stack;



public class IIPBlock extends Block {
		
	IIPBlock(Diagram diag) {
		super(diag);
		type = Block.Types.IIP_BLOCK;
		
		width = 60;  // will be overwritten - depends on contents of desc

		height = driver.gFontHeight + 4;
		
		//buildSides();
		//calcEdges();
	}
	
	@Override
	void draw(Graphics g) {
		if (!visible && this != driver.selBlock) {

			showArrowEndAreas(g);

			return;
		}
		
		Font fontsave = g.getFont();
		g.setFont(driver.fontf);
		g.setColor(Color.GRAY);
		//width = calcIIPWidth((Graphics2D) g);
		
		
		g.drawRoundRect(cx - width / 2, cy - height / 2, width /* + 12 */ , height, 6, 6);   
		
		if (this == driver.selBlock)
			g.setColor(DrawFBP.ly); // light yellow
		else
			g.setColor(DrawFBP.lb); // light turquoise
		
		g.fillRoundRect(cx - width / 2 + 1, cy - height / 2 + 1, width /* + 12 */ - 1, height - 1, 6, 6);
				
		g.setColor(Color.GRAY);
		if (desc != null) {
			g.setColor(Color.GRAY);
			g.drawString(desc, cx - width / 2 + 4 /*+ 6 */, cy + 4);
		}

		buildSides();
		
		showDetectionAreas(g);

		int tlx = cx - width / 2;
		int tly = cy - height / 2;
		showCompareFlag(g, tlx, tly);
		calcDiagMaxAndMin(tlx, cx + width / 2, tly, cy + height / 2);
		g.setFont(fontsave);
		//blueCircs(g);
	}
	
	int calcIIPWidth(Graphics2D g2d) {
		int w = 10;
		if (desc != null) {
			//FontMetrics metrics = g2d.getFontMetrics(g2d.getFont());	
			FontMetrics metrics = g2d.getFontMetrics(driver.fontf);
			height = driver.gFontHeight + metrics.getMaxAscent();
			String t = desc;
			if (t.length() < 2)
				t = " " + t;
			byte[] str = t.getBytes();
			w = 4 + metrics.bytesWidth(str, 0, t.length());
			//width = 60;    // fudge
						
		}
		return w;
	}
	

	/*
	
	void buildSides(){		
		
		leftRect = new Rectangle(cx - width / 2 - driver.zWS / 2, cy - height / 2 - driver.zWS / 2, 
				driver.zWS, height + driver.zWS);
		topRect = new Rectangle(cx - width / 2 - driver.zWS / 2, cy - height / 2 - driver.zWS / 2, 
				width + driver.zWS, driver.zWS);		
		rightRect = new Rectangle(cx + width / 2 - driver.zWS / 2, cy - height / 2 - driver.zWS / 2, 
				driver.zWS, height + driver.zWS);		
		botRect = new Rectangle(cx - width / 2 - driver.zWS / 2, cy + height / 2  - driver.zWS / 2, 
					width + driver.zWS, driver.zWS );
	}
*/	
	String checkNestedChars(String s) {
		Stack<String> stk = new Stack<String>();
		String chars = "{[(<}])>";
		boolean inQuotes = false;
		boolean warning = false;
		String res = "";
		for (int i = 0; i < s.length(); i++) {
			String c = s.substring(i, i + 1);
			if (i + 1 < s.length() && s.substring(i, i + 2).equals("\\\"")){
				res += "\"";
				i++;
				continue;
			}
			res += c;
			if (c.equals("\"")) {
				inQuotes = !inQuotes;
				continue;
			}
			if (inQuotes)
				continue;
			
			int j = chars.indexOf(c);
			if (j == -1)
				continue;
			if (j <= 3)
				stk.push(c);
			else {
				String c2 = null;
				try {
				c2 = stk.pop();
				} catch (EmptyStackException e) {
					warning = true;	
					continue;
				}
				
				int k = chars.indexOf(c2);
				if (k != j - 4) {
					warning = true;
				}
			}
		}
		if (!stk.isEmpty() || inQuotes || warning)
			MyOptionPane.showMessageDialog(driver,
					"Brackets not balanced in IIP string", MyOptionPane.ERROR_MESSAGE);
		return res;
	}
	void showArrowEndAreas(Graphics g) {
		Color col = g.getColor();
		g.setColor(DrawFBP.grey);   

		Graphics2D g2d = (Graphics2D) g;
		//g.fillRect(cx - width / 2 - driver.zWS / 2, cy - height / 2 - driver.zWS / 2, driver.zWS, height); // left
		
		//	g.fillRect(cx - width / 2 - driver.zWS / 2, cy - height / 2 - driver.zWS / 2, width + 2 * driver.zWS, driver.zWS); // top
		
		//	g.fillRect(cx + width / 2 + driver.zWS, cy - height / 2 - driver.zWS / 2, driver.zWS / 2 * 2, height + driver.zWS); // right
		//	g.fillRect(cx - width / 2 - driver.zWS / 2, cy + height / 2 - driver.zWS / 2, width + 4 * driver.zWS / 2, driver.zWS);// bottom
			
			g2d.fill(leftRect);
			g2d.fill(rightRect);
			g2d.fill(topRect);
			g2d.fill(botRect);
			
			g.setColor(col);
	}
	  
}