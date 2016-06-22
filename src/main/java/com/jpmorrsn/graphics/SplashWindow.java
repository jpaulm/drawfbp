package com.jpmorrsn.graphics;

import java.awt.*;

import javax.swing.*;

import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import javax.imageio.ImageIO;

class SplashWindow extends JWindow {
	static final long serialVersionUID = 111L;
	static boolean READFILE = true;
	
	public SplashWindow(String filename, JFrame f, int waitTime,
			final DrawFBP driver) {
		super(f);
		
		Image i = null;
								
		BufferedImage image = driver.loadImage(filename);
		int x = image.getWidth();
		int y = image.getHeight();
		i = image.getScaledInstance(320, 320 * y / x, Image.SCALE_SMOOTH);
		
		Container c = getContentPane();
		ImageIcon icon = new ImageIcon(i);
		JLabel l = new JLabel(icon);
		c.add(l, BorderLayout.CENTER);
		pack();

		Dimension screenSize =
		// Toolkit.getDefaultToolkit().getScreenSize();
		f.getSize();

		Dimension labelSize = l.getPreferredSize();
		setLocation(screenSize.width / 2 - (labelSize.width / 2),
				screenSize.height / 2 - (labelSize.height / 2) + 30);

		final int pause = waitTime;
		final Runnable closerRunner = new Runnable() {
			public void run() {
				setVisible(false);
				dispose();
			}
		};
		Runnable waitRunner = new Runnable() {
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
