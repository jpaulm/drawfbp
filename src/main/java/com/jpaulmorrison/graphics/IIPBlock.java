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
		//width = driver.fontWidth * 20 + 4;
		height = driver.gFontHeight + 4;
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
			String t = description;
			if (t.length() < 2)
				t = " " + t;
			byte[] str = t.getBytes();
			width = 2 + metrics.bytesWidth(str, 0, t.length());
		}
		g.drawRoundRect(cx - width / 2, cy - height / 2, width + 4, height, 6, 6);
		if (this == driver.selBlock)
			g.setColor(ly); // light yellow
		else
			g.setColor(lb); // light turquoise
		g.fillRoundRect(cx - width / 2 + 1, cy - height / 2 + 1, width + 4 - 1,
				height - 1, 6, 6);
		g.setColor(Color.GRAY);
		if (description != null) {
			g.setColor(Color.GRAY);
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
					"Brackets not balanced in IIP string", MyOptionPane.ERROR_MESSAGE);
		return res;
	}
}