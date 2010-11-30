package edu.ucla.bonnie.client;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class Recorder extends Thread {
	public static interface Callback {
		public void start(long time);

		public void newSample(long time, int value);

		public void error(String msg);

		public void end(long time);
	}

	private TargetDataLine line;

	private AudioFormat audioFormat;

	private AudioInputStream audioInputStream;

	private boolean cancel = false;

	private Callback cb;

	public void startRecording(Callback cb) throws Exception {
		this.cb = cb;
		if (line != null && line.isOpen()) {
			throw new Exception(
					"Line already open. Please close and try again.");
		}
		float sampleRate = 8000.0f;
		audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
				sampleRate, 16, 2, 4, sampleRate, false);

		DataLine.Info info = new DataLine.Info(TargetDataLine.class,
				audioFormat);
		line = null;
		try {
			line = (TargetDataLine) AudioSystem.getLine(info);
			line.open(audioFormat);
			audioInputStream = new AudioInputStream(line);
			new Thread(this).start();
		} catch (LineUnavailableException e) {
			throw new Exception("Unable to get a recording line.");
		}
	}

	public void stopRecording() {
		cancel = true;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (cb != null) {
			cb.end(System.currentTimeMillis());
		}
	}

	@Override
	public void run() {
		line.start();
		byte[] buf = new byte[line.getBufferSize() / 5];
		int n;
		int sampleRate = 8000;
		int outputRate = 32;
		int nSamples = sampleRate * line.getFormat().getChannels() / outputRate;
		if (cb != null) {
			cb.start(System.currentTimeMillis());
		}
		try {
			int sampleAccum = 0;
			int sc = 0;
			while (!cancel) {
				while ((n = audioInputStream.read(buf)) > 0) {
					for (int i = 0; i < n; i += 2) {
						sampleAccum += (short) (((buf[i + 1] & 0xff) << 8) | (buf[i] & 0xff));
						sc++;
						if (sc == nSamples) {
							int val = sampleAccum / nSamples;
							if (cb != null) {
								cb.newSample(System.currentTimeMillis(), val);
							}
							// System.out.println(val);
							sampleAccum = 0;
							sc = 0;
						}
					}
				}
			}
		} catch (IOException e) {
			if (cb != null) {
				cb.error("Error: " + e.getMessage());
			}
			e.printStackTrace();
		}
		line.stop();
		line.close();
	}
}
