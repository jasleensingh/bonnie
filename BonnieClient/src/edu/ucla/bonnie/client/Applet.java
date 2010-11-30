package edu.ucla.bonnie.client;

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JApplet;
import javax.swing.UIManager;

public class Applet extends JApplet {
	public static Applet instance;
	
	public void init() {
		instance = this;
		initLnf();
	}

	public void start() {
		initLnf();
		Container c = getContentPane();
		c.setPreferredSize(new Dimension(1024, 900));
		c.add(new Panel());
	}

	private void initLnf() {
		UIManager.put("ClassLoader", Applet.class.getClassLoader());
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
