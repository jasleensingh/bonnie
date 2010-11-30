package edu.ucla.bonnie.player;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javazoom.jlgui.basicplayer.BasicPlayer;

public class Panel extends JPanel {
	private static final Class clazz = Panel.class;

	private String path;
	private BasicPlayer player = new BasicPlayer();
	private Icon playIcon = new ImageIcon(clazz
			.getResource("res/player_play.png"));
	private Icon pauseIcon = new ImageIcon(clazz
			.getResource("res/player_pause.png"));

	private JLabel play;
	private boolean playing;
	private byte[] byteArray;

	public Panel(String p) {
		super(new BorderLayout());
		this.path = p;
		setBackground(Color.white);
		setPreferredSize(new Dimension(32, 32));
		play = new JLabel(playIcon);
		play.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (playing) {
					pause();
				} else {
					resume();
				}
			}
		});
		add(play);
		play();
	}

	public void play() {
		try {
			if (byteArray == null) {
				read();
			}
			player.seek(0);
			player.open(new ByteArrayInputStream(byteArray));
			player.play();
			playing = true;
			play.setIcon(pauseIcon);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void resume() {
		try {
			player.resume();
			playing = true;
			play.setIcon(pauseIcon);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void pause() {
		try {
			player.pause();
			playing = false;
			play.setIcon(playIcon);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void read() throws Exception {
		FileInputStream in = new FileInputStream(path);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		readFully(in, out);
		byteArray = out.toByteArray();
	}

	private static final void readFully(InputStream in, OutputStream out)
			throws IOException {
		byte[] buf = new byte[4096];
		int n;
		while ((n = in.read(buf)) > 0) {
			out.write(buf, 0, n);
		}
		out.flush();
	}
}
