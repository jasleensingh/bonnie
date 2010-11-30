package edu.ucla.bonnie.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

public class Panel extends JPanel {
	private static final Class clazz = Panel.class;
	private static final int INPUT_TYPE_UPLOAD = 0, INPUT_TYPE_RECORD = 1;
	private static final Color PaletteColor1 = new Color(0xFF6600);
	private static final Color PaletteColor2 = Color.white;
	private static final FileFilter filter = new FileFilter() {
		private final String[] exts = { "mp3", "wav", "wma" };

		@Override
		public boolean accept(File pathname) {
			String name = pathname.getName();
			int ext_idx = name.lastIndexOf(".");
			if (ext_idx > 0 && ext_idx < name.length() - 1) {
				String ext = name.substring(ext_idx + 1);
				for (String e : exts) {
					if (e.equalsIgnoreCase(ext)) {
						return true;
					}
				}
			}
			return false;
		}
	};

	private int inputType;
	private Recorder recorder;
	private Recorder.Callback recorderCallback;

	private Waveform recordWaveform;
	private Progress recordProgress;
	private JTextField uploadField;
	private Progress uploadProgress;

	public Panel() {
		super(new BorderLayout());
		setBackground(Color.white);
		JLabel splash = new JLabel(new ImageIcon(clazz
				.getResource("resources/splash.png")));
		splash.setHorizontalAlignment(SwingConstants.CENTER);
		add(splash);

		final String[] toggleTypesStr = { "Record your voice", "Upload a clip" };
		JPanel inputTypeUpload = new JPanel(new BorderLayout());
		JPanel inputTypeRecord = new JPanel(new BorderLayout());
		final JPanel[] toggleTypesInput = { inputTypeUpload, inputTypeRecord };

		final JPanel inputPanel = new JPanel(new BorderLayout());
		inputPanel.setPreferredSize(new Dimension(800, 150));
		inputPanel.setOpaque(false);
		JPanel north = new JPanel(new BorderLayout());
		north.setOpaque(false);
		final JLabel text = new JLabel(
				"A query must contain at least 30 seconds of audio.");
		text.setHorizontalAlignment(SwingConstants.CENTER);
		text.setFont(new Font("Sans Serif", Font.PLAIN, 16));
		north.add(text, BorderLayout.SOUTH);
		final JButton toggleTypes = new CLink(toggleTypesStr[inputType]);
		toggleTypes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				inputPanel.remove(toggleTypesInput[inputType]);
				inputType = (inputType + 1) % 2;
				inputPanel.add(toggleTypesInput[inputType]);
				inputPanel.revalidate();
				inputPanel.repaint();
				toggleTypes.setText(toggleTypesStr[inputType]);
			}
		});
		north.add(toggleTypes, BorderLayout.EAST);
		inputPanel.add(north, BorderLayout.NORTH);

		inputTypeUpload.setOpaque(false);
		JButton upload = new CButton(new ImageIcon(clazz
				.getResource("resources/upload_normal.png")), new ImageIcon(
				clazz.getResource("resources/upload_pressed.png")));
		upload.setBorderPainted(false);
		upload.setContentAreaFilled(false);
		inputTypeUpload.add(upload, BorderLayout.WEST);
		JPanel uploadCenter = new JPanel(new BorderLayout());
		uploadCenter.setOpaque(false);
		uploadCenter.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		uploadField = new JTextField();
		uploadField.setEditable(false);
		uploadField.setFont(new Font("Sans Serif", Font.PLAIN, 28));
		uploadCenter.add(uploadField, BorderLayout.NORTH);
		uploadProgress = new Progress(0, 100, 0);
		uploadProgress.setVisible(false);
		uploadCenter.add(uploadProgress, BorderLayout.SOUTH);
		inputTypeUpload.add(uploadCenter);

		inputTypeRecord.setOpaque(false);
		JToggleButton record = new CToggleButton(new ImageIcon(clazz
				.getResource("resources/record_normal.png")), new ImageIcon(
				clazz.getResource("resources/record_pressed.png")));
		record.setBorderPainted(false);
		record.setContentAreaFilled(false);
		inputTypeRecord.add(record, BorderLayout.WEST);
		JPanel recordCenter = new JPanel(new BorderLayout());
		recordCenter.setOpaque(false);
		recordCenter.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		recordWaveform = new Waveform();
		recordCenter.add(recordWaveform, BorderLayout.NORTH);
		recordProgress = new Progress(0, 100, 0);
		recordProgress.setVisible(false);
		recordCenter.add(recordProgress, BorderLayout.SOUTH);
		inputTypeRecord.add(recordCenter);

		inputPanel.add(toggleTypesInput[inputType]);
		add(inputPanel, BorderLayout.SOUTH);

		upload.addActionListener(new ActionListener() {
			private String[] formats = { "mp3", "wav" };

			@Override
			public void actionPerformed(ActionEvent e) {
				FileDialog fd = new FileDialog(JOptionPane
						.getFrameForComponent(Panel.this), "Open");
				fd.setVisible(true);
				String file_str = fd.getFile();
				if (file_str != null) {
					String format = null;
					for (String f : formats) {
						if (file_str.endsWith("." + f))
							;
						format = f;
						break;
					}
					if (format == null) {
						String msg = "Format not supported: "
								+ file_str.substring(file_str.lastIndexOf("."));
						JOptionPane.showMessageDialog(Panel.this, msg, "Error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					File file = new File(fd.getDirectory() + "/" + file_str);
					uploadField.setText(file.getAbsolutePath());
					upload(file, uploadProgress);
				}
			}
		});
		recorder = new Recorder();
		recorderCallback = new Recorder.Callback() {
			private long startTime;

			@Override
			public void start(long time) {
				startTime = time;
				recordWaveform.setTime(0);
			}

			@Override
			public void newSample(long time, int value) {
				recordWaveform.tick((int) (time - startTime), value);
			}

			@Override
			public void error(String msg) {
				JOptionPane.showMessageDialog(Panel.this, msg, "Error",
						JOptionPane.ERROR_MESSAGE);
			}

			@Override
			public void end(long time) {
			}
		};
		record.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					try {
						recordWaveform.reset();
						recorder.startRecording(recorderCallback);
					} catch (Exception e1) {
						String msg = "Oops... an error occurred while recording audio."
								+ "\nPlease check the logs.";
						JOptionPane.showMessageDialog(Panel.this, msg, "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				} else {
					recorder.stopRecording();
				}
			}
		});
	}

	private void upload(final File file, final Progress progress) {
		progress.reset();
		progress.setVisible(true);
		new Thread() {
			@Override
			public void run() {
				// while (progress.getValue() < progress.getMax()) {
				// progress.setValue(progress.getValue() + 1);
				// try {
				// Thread.sleep(50);
				// } catch (InterruptedException e) {
				// }
				// }
				try {
					if (!filter.accept(file)) {
						String msg = "Not a valid file: "
								+ file.getAbsolutePath();
						throw new Exception(msg);
					}
					String name = file.getName();
					long length = file.length();
					String type = name.substring(name.lastIndexOf(".") + 1);
					InputStream in = new FileInputStream(file);
					OutputStream out = queryBegin(type);
					byte[] buf = new byte[4096];
					int n;
					long bytesTransferred = 0;
					while ((n = in.read(buf)) > 0) {
						out.write(buf, 0, n);
						bytesTransferred += n;
						int percentComplete = (int) (bytesTransferred * 100 / length);
						progress.setValue(percentComplete);
					}
					System.err.println(queryConn.getInputStream());
					String id = queryEnd();
					if (id == null) {
						throw new Exception("Invalid server response");
					}
					// wait some time
					Thread.sleep(3000);
					showResults(id);
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(Panel.this, e.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}.start();
	}

	private HttpURLConnection queryConn;

	public OutputStream queryBegin(String type) throws IOException {
		String queryStr = getProperty("BASE_URL") + "/" + getProperty("QUERY");
		System.err.println(queryStr);
		URL url = new URL(queryStr);
		queryConn = (HttpURLConnection) url.openConnection();
		queryConn.setRequestProperty("type", type);
		queryConn.setUseCaches(false);
		queryConn.setDoOutput(true);
		queryConn.setDoInput(true);
		System.out.println("returning outputstream");
		OutputStream out = queryConn.getOutputStream();
		System.out.println(queryStr + "\n" + out.getClass());
		return out;
	}

	public String queryEnd() throws IOException {
		System.out.println(queryConn);
		if (queryConn != null) {
			BufferedReader r = new BufferedReader(new InputStreamReader(
					queryConn.getInputStream()));
			String line = r.readLine();
			if (!line.equals(Common.OK)) {
				throw new IOException("An error occurred while uploading");
			}
			String queryId = r.readLine();
			r.close();
			return queryId;
		}
		return null;
	}

	private void showResults(String id) throws Exception {
		String resultsStr = getProperty("BASE_URL") + "/"
				+ getProperty("RESULTS") + "?" + "id=" + id;
		if (Applet.instance != null) {
			URL url = new URL(resultsStr);
			Applet.instance.getAppletContext().showDocument(url);
		} else {
			BrowserLauncher.openURL(resultsStr);
		}
	}

	private Properties props;

	private String getProperty(String name) throws IOException {
		if (props == null) {
			props = new Properties();
			props.load(getClass().getResourceAsStream(
					"resources/server.properties"));
		}
		return props.getProperty(name);
	}

	private static class CLink extends JButton {
		public CLink(String text) {
			setText(text);
			setForeground(Color.blue);
			setContentAreaFilled(false);
			setBorderPainted(false);
			setOpaque(false);
			setFocusable(false);
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
	}

	private static class CButton extends JButton {
		private Icon normal;
		private Icon pressed;

		private MouseListener ml = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				setIcon(pressed);
			}

			public void mouseReleased(MouseEvent e) {
				setIcon(normal);
			}
		};

		public CButton(Icon normal, Icon pressed) {
			this.normal = normal;
			this.pressed = pressed;
			setIcon(normal);
			setContentAreaFilled(false);
			setBorderPainted(false);
			setOpaque(false);
			setFocusable(false);
			addMouseListener(ml);
		}
	}

	private static class CToggleButton extends JToggleButton {
		private Icon normal;
		private Icon pressed;

		private ItemListener il = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				setIcon(e.getStateChange() == ItemEvent.SELECTED ? pressed
						: normal);
			}
		};

		public CToggleButton(Icon normal, Icon pressed) {
			this.normal = normal;
			this.pressed = pressed;
			setIcon(normal);
			setContentAreaFilled(false);
			setBorderPainted(false);
			setOpaque(false);
			setFocusable(false);
			addItemListener(il);
		}
	}

	private static class Waveform extends JPanel {
		private static class Sample {
			public final int time;
			public final int value;

			public Sample(int time, int value) {
				this.time = time;
				this.value = value;
			}
		}

		private int maxSampledValue;
		private List<Sample> samples = new ArrayList<Sample>();
		private Sample[] samplesCopy = new Sample[256];

		private static final int MARGIN = 10;
		private static final int TOTAL_DURATION = 30000; // ms
		private int time; // ms
		private ImageIcon knob = new ImageIcon(clazz
				.getResource("resources/knob.png"));

		public Waveform() {
			setOpaque(false);
			setPreferredSize(new Dimension(100, 80));
			reset();
		}

		public void reset() {
			maxSampledValue = 8;
			samples.clear();
			setTime(0);
		}

		public int getTime() {
			return time;
		}

		public void setTime(int time) {
			if (time < 0 || time > TOTAL_DURATION) {
				return;
			}
			this.time = time;
			repaint();
		}

		public void tick(int time, int value) {
			if (Math.abs(value) > maxSampledValue) {
				maxSampledValue = Math.abs(value);
			}
			samples.add(new Sample(time, value));
			setTime(time);
		}

		private static final Font WaveformFont = new Font("Sans Serif",
				Font.PLAIN, 10);
		private static final Color WaveformColor = PaletteColor1;

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			int width = getWidth() - 2 * MARGIN, height = getHeight() - 2
					* MARGIN;
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(Color.gray);
			g2.drawLine(MARGIN, MARGIN + height / 2, MARGIN + width, MARGIN
					+ height / 2);
			g2.setFont(WaveformFont);
			for (int i = 0; i <= TOTAL_DURATION; i += 5000) {
				int x = MARGIN + i * width / TOTAL_DURATION;
				int y = MARGIN + height / 2;
				g2.drawLine(x, y - 5, x, y + 5);
				String s = "" + (i / 1000);
				g2.drawString(s, x - g2.getFontMetrics().stringWidth(s) / 2,
						y - 10);
			}
			g2.drawImage(knob.getImage(),
					MARGIN + (time * width / TOTAL_DURATION)
							- knob.getIconWidth() / 2, MARGIN
							+ (height - knob.getIconHeight()) / 2, null);

			int size = samples.size();
			if (samplesCopy.length < size) {
				while ((samplesCopy = new Sample[samplesCopy.length * 2]).length < size)
					;
			}
			samples.toArray(samplesCopy);
			if (size > 1) {
				g2.setColor(WaveformColor);
				Sample sample;
				int x1, y1, x2, y2;
				sample = samplesCopy[0];
				x1 = MARGIN + sample.time * width / TOTAL_DURATION;
				y1 = MARGIN + height / 2 + sample.value * height
						/ (maxSampledValue * 2);
				for (int i = 1; i < size; i++) {
					sample = samplesCopy[i];
					x2 = MARGIN + sample.time * width / TOTAL_DURATION;
					y2 = MARGIN + height / 2 + sample.value * height
							/ (maxSampledValue * 2);
					g2.drawLine(x1, y1, x2, y2);
					x1 = x2;
					y1 = y2;
				}
			}
		}
	}

	private static class Progress extends JPanel {
		private int min;
		private int max;
		private int value;
		private String error;

		public Progress(int min, int max, int value) {
			this.min = min;
			this.max = max;
			this.value = value;
			setOpaque(false);
			setBorder(BorderFactory.createLineBorder(Color.darkGray));
			setPreferredSize(new Dimension(100, 20));
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			int width = getWidth(), height = getHeight();
			if (error == null) {
				g.setColor(PaletteColor1);
				g.fillRect(0, 0, (value - min) * width / (max - min), height);
			} else {
				g.setColor(Color.lightGray);
				g.fillRect(0, 0, width, height);
			}
		}

		public void reset() {
			value = min;
			error = null;
		}

		public int getMin() {
			return min;
		}

		public void setMin(int min) {
			this.min = min;
			repaint();
		}

		public int getMax() {
			return max;
		}

		public void setMax(int max) {
			this.max = max;
			repaint();
		}

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
			repaint();
		}

		public void setError(String error) {
			this.error = error;
			repaint();
		}
	}
}
