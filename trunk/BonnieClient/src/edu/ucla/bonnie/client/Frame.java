package edu.ucla.bonnie.client;

import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.UIManager;

public class Frame extends JFrame {

	private Panel panel;

	public Frame() {
		super("Bonnie Longears");
		Container c = getContentPane();
		c.add(panel = new Panel());
		pack();

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}

	private static void launch() {
		Frame f = new Frame();
		f.setVisible(true);
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		launch();
	}
}
