package edu.ucla.bonnie.common;

import java.io.File;
import java.io.FileInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

public class Base {
	protected static File convert = new AppData.Store("Bin")
			.getFile("convert.bat");
	protected static File tempDir = new AppData.Store("Temp").getDir("");
	protected static final File indexDir = new AppData.Store("Index")
			.getDir("");
	protected static final File searchDir = new AppData.Store("Search")
			.getDir("");
	protected static final File mapTableFile = new AppData.Store("Index")
			.getFile(".maptable");

	protected static int[] getSamples(File file) throws Exception {
		AudioInputStream ais = AudioSystem
				.getAudioInputStream(new FileInputStream(file));
		AudioFormat format = ais.getFormat();
		int sampleRate = (int) format.getSampleRate();
		int channels = format.getChannels();
		byte[] buf = new byte[sampleRate * channels];
		int[] samples = new int[sampleRate * channels];
		int n, count = 0;
		while ((n = ais.read(buf)) > 0) {
			for (int i = 0; i < n; i += 2) {
				samples[count++] = (((int) buf[i + 1]) << 8) | (buf[i] & 0xff);
				if (count == samples.length) {
					int[] expand = new int[samples.length * 2];
					System.arraycopy(samples, 0, expand, 0, samples.length);
					samples = expand;
				}
			}
		}
		int[] trim = new int[count];
		System.arraycopy(samples, 0, trim, 0, count);
		samples = trim;
		return samples;
	}

	// file must be 44100 Hz, mono, 16-bit signed wav file
	public static String[] getHashes(File file) throws Exception {
		String[] hashes = null;
		int[] samples = getSamples(file);
		float[] complex_samples = new float[samples.length * 2];
		for (int i = 0; i < samples.length; i++) {
			complex_samples[i * 2] = samples[i] / 1000;
		}
		hashes = spectrogram(complex_samples, samples.length);
		return hashes;
	}

	protected static final int[] RANGE = { 50, 80, 120, 210, 300 };
	protected static final int LOWER_LIMIT = RANGE[0];
	protected static final int UPPER_LIMIT = RANGE[RANGE.length - 1];

	protected static String[] spectrogram(float[] complex_samples, int length) {
		int height = 1200;
		int window = 1;
		int seg_length = height * window;
		int width = length / height;
		int total = 0;
		String[] hashes = new String[width];
		for (int d = 0; d < width; d++) {
			// System.err.println(d + ":" + width);
			int seg_start = (int) ((long) length * d / width);
			if (seg_start % 2 > 0) {
				seg_start--;
			}
			// System.err.println(seg_start + "," +(seg_length * 2) + ":"
			// + complex_samples.length);
			FloatFFT_1D fft = new FloatFFT_1D(seg_length);
			float[] segment = new float[seg_length * 2];
			System.arraycopy(complex_samples, seg_start * 2, segment, 0,
					seg_length * 2);
			fft.complexForward(segment);
			int pos;
			double max_freq = 0;
			int max_freq_index = 0;
			int next_boundary_index = 1;
			StringBuilder hash = new StringBuilder();
			for (int i = 0; i < seg_length; i += window) {
				double sum = 0;
				for (int j = 0; j < window
						&& (pos = (i + j) * 2) < segment.length; j++) {
					float re = segment[pos];
					float im = segment[pos + 1];
					double val = Math.sqrt(re * re + im * im);
					sum += val;
				}
				double mag = sum / window;
				if (i >= LOWER_LIMIT && i <= UPPER_LIMIT && mag > max_freq) {
					max_freq = mag;
					max_freq_index = i;
				}
				if (i >= LOWER_LIMIT && i <= UPPER_LIMIT
						&& i == RANGE[next_boundary_index]) {
					hash.append(String.format("%02d", max_freq_index
							- RANGE[next_boundary_index - 1]));
					max_freq = 0;
					max_freq_index = i + 1;
					next_boundary_index++;
				}
			}
			total++;
			hashes[d] = "" + hash;
		}
		return hashes;
		// show(buf);
	}
}
