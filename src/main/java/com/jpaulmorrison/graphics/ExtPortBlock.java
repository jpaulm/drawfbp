package com.jpaulmorrison.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

public class ExtPortBlock extends Block {
	
	boolean substreamSensitive = false;

	ExtPortBlock(Diagram diag) {
		super(diag);
		//this.diag = diag;
		width = 36;
		height = 28;
		type = Block.Types.EXTPORT_IN_BLOCK;
				
		//calcEdges();
	}
	
	@Override
	void draw(Graphics g) {
		if (!visible)
			return;
		int c = cx;
		if (type.equals(Block.Types.EXTPORT_IN_BLOCK))
			drawIn(g, cx, width);
		else if (type.equals(Block.Types.EXTPORT_OUT_BLOCK))
			drawOut(g, cx, width);
		else if (type.equals(Block.Types.EXTPORT_OUTIN_BLOCK)){	
			c = cx - width / 4;
			drawOut(g, c, width / 2);	
			c = cx + width / 4;
			drawIn(g, c, width / 2);			
		}

		if (desc != null) {
			g.setColor(Color.RED);
			g.drawString(desc, cx - desc.length()
					* driver.gFontWidth / 2, cy + 24);
			g.setColor(Color.BLACK);
		}

		showDetectionAreas(g);

		//blueCircs(g);
	}

	void drawIn(Graphics g, int ctr, int w) {
		int ptx[] = new int[7];
		int pty[] = new int[7];
		
		int left = ctr - w / 2;
		int right = ctr + w / 2;
		int top = cy - height / 2;
		int bottom = cy + height / 2;
		ptx[0] = left;
		pty[0] = top;
		ptx[1] = right - 10;
		pty[1] = top;
		ptx[2] = right;
		pty[2] = cy;
		ptx[3] = right - 10;
		pty[3] = bottom;
		ptx[4] = left;
		pty[4] = bottom;
		ptx[5] = left;
		pty[5] = bottom - 6;
		ptx[6] = left;
		pty[6] = top + 6;
		g.setColor(Color.BLACK);
		g.drawPolygon(ptx, pty, 7);
		if (this == driver.selBlock)
			g.setColor(DrawFBP.ly); // light yellow
		else
			g.setColor(DrawFBP.lb); // light turquoise
		int tlx = cx - width / 2;
		int tly = cy - height / 2;
		showCompareFlag(g, tlx, tly);
		ptx[0] += 1;
		pty[0] += 1;
		ptx[1] -= 1;
		pty[1] += 1;
		ptx[2] -= 1;
		ptx[3] -= 1;
		ptx[4] += 1;
		ptx[5] += 1;
		ptx[6] += 1;
		g.fillPolygon(ptx, pty, 7);
		
		g.setColor(Color.BLACK);
		if (substreamSensitive) {
			/*
			//GeneralPath gp = Enclosure.drawSemicircle(right + 4, cy, +1);
			//g.fill(gp);
			int ptx2[] = new int[3];
			int pty2[] = new int[3];
			ptx2[0] = right - 10;
			ptx2[1] = right;
			ptx2[2] = right - 10;
			pty2[0] = top;
			pty2[1] = cy;
			pty2[2] = bottom;
			g.setColor(Color.BLUE);
			g.drawPolygon(ptx2, pty2, 3);		
			g.fillPolygon(ptx2, pty2, 3);
			*/
			Font ff = g.getFont();			
			Font ffb = ff.deriveFont(Font.BOLD, 18.0f);
			g.setFont(ffb);			
			FontMetrics metrics = g.getFontMetrics(ffb);
			String t = "SS";
			byte[] str = t.getBytes();
			int width = metrics.bytesWidth(str, 0, t.length());

			g.drawString(t, cx - width / 2, cy + 5);			
			g.setFont(ff);
		}
		
		g.drawLine(left, cy - height, left, cy + height);
		
		calcDiagMaxAndMin(left, right, top, bottom);
	}

	void drawOut(Graphics g, int ctr, int w) {
		int ptx[] = new int[7];
		int pty[] = new int[7];
		
		int left = ctr - w / 2;
		int right = ctr + w / 2;
		int top = cy - height / 2;
		int bottom = cy + height / 2;
		ptx[0] = right;
		pty[0] = top;
		ptx[1] = right;
		pty[1] = top + 6;
		ptx[2] = right;
		pty[2] = bottom - 6;
		ptx[3] = right;
		pty[3] = bottom;
		ptx[4] = left + 10;
		pty[4] = bottom;
		ptx[5] = left;
		pty[5] = cy;
		ptx[6] = left + 10;
		pty[6] = top;
		g.setColor(Color.BLACK);
		g.drawPolygon(ptx, pty, 7);
		if (this == driver.selBlock)
			g.setColor(DrawFBP.ly); // light yellow
		else
			g.setColor(DrawFBP.lb); // light turquoise
		int tlx = cx - width / 2;
		int tly = cy - height / 2;		
		showCompareFlag(g, tlx, tly);
		ptx[0] -= 1;
		pty[0] += 1;
		ptx[1] -= 1;
		pty[1] += 1;
		ptx[2] -= 1;
		ptx[3] -= 1;
		ptx[4] += 1;
		ptx[5] += 1;
		ptx[6] += 1;
		pty[6] += 1;
		g.fillPolygon(ptx, pty, 7);
		g.setColor(Color.BLACK);
		if (substreamSensitive){
			/*
			//g.setColor(Color.RED);			
			//GeneralPath gp = Enclosure.drawSemicircle(left - 4, cy, +1);
			//g.fill(gp);	
			int ptx2[] = new int[3];
			int pty2[] = new int[3];
			ptx2[0] = left + 10;
			ptx2[1] = left;
			ptx2[2] = left + 10;
			pty2[0] = top;
			pty2[1] = cy;
			pty2[2] = bottom;
			g.setColor(Color.BLUE);
			g.drawPolygon(ptx2, pty2, 3);		
			g.fillPolygon(ptx2, pty2, 3);
			*/
			Font ff = g.getFont();			
			Font ffb = ff.deriveFont(Font.BOLD, 18.0f);
			g.setFont(ffb);			
			FontMetrics metrics = g.getFontMetrics(ffb);
			String t = "SS";
			byte[] str = t.getBytes();
			int width = metrics.bytesWidth(str, 0, t.length());

			g.drawString(t, cx - width / 2, cy + 5);			
			g.setFont(ff);
		}
		
		if (!type.equals(Block.Types.EXTPORT_OUTIN_BLOCK))
			g.drawLine(right, cy - height, right, cy + height);
		
		calcDiagMaxAndMin(left, right, top, bottom);
	}

}