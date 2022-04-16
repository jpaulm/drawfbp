package com.jpaulmorrison.graphics;

import java.awt.*;

import javax.swing.*;

import java.awt.event.*;
import java.awt.image.BufferedImage;


class SplashWindow extends JWindow {
	static final long serialVersionUID = 111L;
	static boolean READFILE = true;
	
	public SplashWindow(/* JFrame f,*/ int waitTime,
			final DrawFBP driver, boolean small) {
		super(driver);
		
		Image i = null;
		String fn = "DrawFBP-logo.jpg";					
		 
		BufferedImage image = driver.loadImage(fn);
		int x = image.getWidth();
		int y = image.getHeight();
		
		int m = small ? 120 : 320;
		i = image.getScaledInstance(m, m * y / x, Image.SCALE_SMOOTH);
		
		Container c = getContentPane();
		ImageIcon icon = new ImageIcon(i);
		JLabel l = new JLabel(icon);
		c.add(l, BorderLayout.CENTER);
		pack();
		driver.repaint();

		Point p = driver.getLocation();
		Dimension labelSize = l.getPreferredSize();
		Dimension screenSize = driver.getSize();
		if (small) {
			//labelSize = new Dimension((int) (labelSize.width * .6), (int) (labelSize.height * .6));
			p = new Point(p.x + 80, p.y + 120);
			setLocation(p);
			//l.setPreferredSize(labelSize);
		} else {			
		    		    
		    setLocation(p.x + screenSize.width / 2 - (labelSize.width / 2),
		    	p.y + screenSize.height / 2 - (labelSize.height / 2) + 30);
		}

		pack();
		final int pause = waitTime;
		final Runnable closerRunner = new Runnable() {
			public void run() {
				setVisible(false);
				dispose();
			}
		};
		Runnable waitRunner  = new Runnable() {
			public void run() {
				try {
					Thread.sleep(1000);
					addMouseMotionListener(new MouseMotionListener() {

						public void mouseMoved(MouseEvent e) {	
							setVisible(false);
							dispose();
						}

						public void mouseDragged(MouseEvent e) {
							setVisible(false);
							dispose();
						}
					});
					Thread.sleep(pause - 1000);
					SwingUtilities.invokeAndWait(closerRunner);
				} catch (Exception e) {
					e.printStackTrace();
					// can catch InvocationTargetException
					// can catch InterruptedException
				}
			}
		};
		setVisible(true);
		Thread splashThread = new Thread(waitRunner, "SplashThread");
		splashThread.start();		

	}

}
