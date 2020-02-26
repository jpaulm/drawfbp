package com.jpaulmorrison.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;


import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;

import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;

// look after handling 'x' on tabs

	public class TabButton extends JButton implements ActionListener {
		
    	static final long serialVersionUID = 111L;
    	JTabbedPaneWithCloseIcons jtp;
    	//Diagram diag;
    	    	
        public TabButton(JTabbedPane jtp) {
            int size = 17;
            this.jtp = (JTabbedPaneWithCloseIcons) jtp;
            setPreferredSize(new Dimension(size, size));
            setToolTipText("close this tab");
            //Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            //No need to be focusable
            setRequestFocusEnabled(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            //Making nice rollover effect
            //we use the same listener for all buttons
            //addMouseListener(buttonMouseListener);
            setRolloverEnabled(true);
            //Close the proper tab by clicking the button
            addActionListener(this);
            //jtp.add(this);
        }
 
        public void actionPerformed(ActionEvent e) {
            int i = jtp.indexOfTabComponent(getParent());
            if (i != -1) {
            	jtp.setSelectedIndex(i);
            	jtp.driver.closeTab();
           //   pane.remove(i);
            }
        }
 
        //we don't want to update UI for this button
        //public void updateUI() {
        //}

    //Paint the cross - goes red if moused over
        
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		// System.out.println("BTC");
		// shift the image for pressed buttons
		// g2 = null;

		// ButtonTabComponent b *contains* JLabel followed by TabButton,
		// which extends JButton

		if (getModel().isPressed()) {
			g2.translate(1, 1);
		}

		g2.setStroke(new BasicStroke(2));
		g2.setColor(Color.BLACK);
		if (getModel().isRollover()) {
			// g2.setColor(Color.MAGENTA);
			g2.setColor(Color.RED);
		}
		// int delta = 6;
		int delta = 4;
		g2.drawLine(delta, delta, getWidth() - delta - 1,
				getHeight() - delta - 1);
		g2.drawLine(getWidth() - delta - 1, delta, delta,
				getHeight() - delta - 1);

		g2.setColor(Color.BLACK);
	}
            
         
	// private final static MouseListener buttonMouseListener = new MouseAdapter() {
	public void mouseEntered(MouseEvent e) {
		Component component = e.getComponent();
		if (component instanceof AbstractButton) {
			AbstractButton button = (AbstractButton) component;
			button.setBorderPainted(true);
		}
	}

	public void mouseExited(MouseEvent e) {
		Component component = e.getComponent();
		if (component instanceof AbstractButton) {
			AbstractButton button = (AbstractButton) component;
			button.setBorderPainted(false);
		}
	}
	// };
    }
 
