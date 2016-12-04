package com.jpaulmorrison.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.EmptyStackException;
import java.util.Stack;


public class IIPBlock extends Block {
	
	IIPBlock(Diagram ctlr) {
		super(ctlr);
		type = Block.Types.IIP_BLOCK;
		width = driver.fontWidth * 20 + 4;
		height = driver.fontHeight + 4;
		//calcEdges();
	}
	
	@Override
	void draw(Graphics2D g) {
		if (!visible && this != driver.selBlock) {
			showZones(g);
			return;
		}
		
		Font fontsave = g.getFont();
		g.setFont(driver.fontf);
		g.setColor(Color.GRAY);
		if (description != null) {
			FontMetrics metrics = g.getFontMetrics(g.getFont());
			//width = metrics.charWidth('n'); 
			//width = description.length() * width + 4;
			width = 4;
			for (int j = 0; j < description.length(); j++) {				
				width += metrics.charWidth(description.charAt(j));
			}
		}
		g.drawRoundRect(cx - width / 2, cy - height / 2, width, height, 6, 6);
		if (this == driver.selBlock)
			g.setColor(new Color(255, 255, 200)); // light yellow
			else
			g.setColor(new Color(200, 255, 255)); // light turquoise
		g.fillRoundRect(cx - width / 2 + 1, cy - height / 2 + 1, width - 1,
				height - 1, 6, 6);
		g.setColor(Color.GRAY);
		if (description != null) {
			g.setColor(Color.GRAY);
			// int len = description.length();
			// String s = description.substring(0,len);
			g.drawString(description, cx - width / 2 + 4, cy + 4);
		}
		// showZones(g);
		calcDiagMaxAndMin(cx - width / 2, cx + width / 2, cy - height / 2, cy + height
				/ 2);
		g.setFont(fontsave);
	}
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
			MyOptionPane.showMessageDialog(driver.frame,
					"Brackets not balanced in IIP string");
		return res;
	}
}