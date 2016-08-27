package com.jpaulmorrison.graphics;

import java.awt.*;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.*;
import java.awt.print.*;

public class PrintableDocument implements Printable {
	private Component compent;
	Rectangle rect;
	DrawFBP driver;

	public PrintableDocument(Component compent, DrawFBP drawFBP) {
		this.compent = compent;
		driver = drawFBP;
	}

	public void setRectangle(Rectangle rect) {
		this.rect = rect;
	}

	public void print() {
		PrinterJob printJob = PrinterJob.getPrinterJob();
		printJob.setPrintable(this);
		PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
		aset.add(MediaSizeName.ISO_A4);
		aset.add(OrientationRequested.LANDSCAPE);

		if (printJob.printDialog(aset))
			try {
				printJob.print(aset);
			} catch (Exception pe) {
				System.out.println("Print Error: " + pe);
				pe.printStackTrace();
			}
	}

	// http://it.toolbox.com/wiki/index.php/How_to_print_in_Java

	public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
		if (pageIndex > 0) {
			return (NO_SUCH_PAGE);
		} else {
			Graphics2D graph = (Graphics2D) g;
			/*
			double scale = driver.scalingFactor; // ???
			
			System.out.println(rect.width + ", " + rect.height + "; " + pageFormat.getImageableWidth() + ", " + pageFormat.getImageableHeight());
			if (rect.width > pageFormat.getImageableWidth()
					|| rect.height > pageFormat.getImageableHeight()) {

				// Calculate the scale factor to fit the window to the page.
				double scaleX = pageFormat.getImageableWidth() / rect.width;
				double scaleY = pageFormat.getImageableHeight() / rect.height;
				scale = Math.min(scaleX, scaleY); // Get minimum scale factor
				graph.scale(scale, scale);
			}
			*/
			graph.scale(.67, .67);   //  FUDGE !!!
			/*
			graph.setClip((int) (pageFormat.getImageableX() / scale),
					(int) (pageFormat.getImageableY() / scale),
					(int) (pageFormat.getImageableWidth() / scale),
					(int) (pageFormat.getImageableHeight() / scale));
			*/

			graph.translate(graph.getClipBounds().getX(), graph.getClipBounds()
					.getY());
			disableBuffering(compent);
			compent.paint(graph);
			enableBuffering(compent);
			return (PAGE_EXISTS);
		}
	}

	public static void disableBuffering(Component c) {
		RepaintManager currentManager = RepaintManager.currentManager(c);
		currentManager.setDoubleBufferingEnabled(false);
	}

	public static void enableBuffering(Component c) {
		RepaintManager currentManager = RepaintManager.currentManager(c);
		currentManager.setDoubleBufferingEnabled(true);
	}
}