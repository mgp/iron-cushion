package co.adhoclabs;

import java.util.Random;

/**
 * Generates values for documents created by a {@link DocumentSchema}.
 */
public class ValueGenerator {
	/**
	 * Seeds this value generator from the given values.
	 */
	public ValueGenerator(int state1, int state2, int state3, int state4,
			int state5) {
		this.state1 = state1;
		this.state2 = state2;
		this.state3 = state3;
		this.state4 = state4;
		this.state5 = state5;

		wordJoiner = new WordJoiner();
	}

	/**
	 * Seeds this value generator from the given {@link Random} instance.
	 */
	public ValueGenerator(Random rng) {
		this(rng.nextInt(), rng.nextInt(), rng.nextInt(), rng.nextInt(), rng
				.nextInt());
	}

	private int state1;
	private int state2;
	private int state3;
	private int state4;
	private int state5;
	private final WordJoiner wordJoiner;

	private int next(int bits) {
		int t = (state1 ^ (state1 >> 7));
		state1 = state2;
		state2 = state3;
		state3 = state4;
		state4 = state5;
		state5 = (state5 ^ (state5 << 6)) ^ (t ^ (t << 13));
		int value = (state2 + state2 + 1) * state5;
		return value >>> (32 - bits);
	}

	/**
	 * @return the next {@code boolean} value
	 */
	public boolean nextBoolean() {
		return next(1) != 0;
	}

	/**
	 * @return the next {@code int} value
	 */
	public int nextInt() {
		return next(32);
	}

	/**
	 * Returns the next {@code int} value between {@code 0} and {@code n}.
	 * 
	 * @param n
	 *            the maximum value to return
	 * @return the next {@code int} value
	 */
	public int nextInt(int n) {
		if ((n & -n) == n) {
			// n is a power of 2
			return (int) ((n * (long) next(31)) >> 31);
		}

		int bits, val;
		do {
			bits = next(31);
			val = bits % n;
		} while (bits - val + (n - 1) < 0);
		return val;
	}

	/**
	 * @return the next {@code float} value
	 */
	public float nextFloat() {
		return next(24) / ((float) (1 << 24));
	}

	/**
	 * @return the next {@link String}, which consists of anywhere from 1 to 5
	 *         words, inclusive
	 */
	public String nextString() {
		wordJoiner.reset();

		int numWords = 1 + nextInt(4);
		for (int i = 0; i < numWords; ++i) {
			int wordIndex = nextInt(NUM_WORDS);
			String word = WORDS[wordIndex];
			wordJoiner.appendWord(word);
		}
		return wordJoiner.toString();
	}

	private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!?";
	private static final int ALPHABET_SIZE = ALPHABET.length();
	private static final int NUM_WORDS = 4096;
	private static final String[] WORDS = new String[NUM_WORDS];
	private static final int MIN_WORD_LENGTH = 3;
	private static final int MAX_WORD_LENGTH = 16;

	/**
	 * A class that joins words together to create strings returned by
	 * {@link ValueGenerator#nextString()}.
	 * 
	 */
	private static final class WordJoiner {
		private final char[] chars;
		private int nextIndex;

		private WordJoiner() {
			chars = new char[1024];
		}

		private void appendWord(String word) {
			if (nextIndex != 0) {
				chars[nextIndex++] = ' ';
			}
			int length = word.length();
			word.getChars(0, length, chars, nextIndex);
			nextIndex += length;
		}

		private void reset() {
			nextIndex = 0;
		}

		public String toString() {
			return new String(chars, 0, nextIndex);
		}
	}

	/**
	 * Fills the {@link ValueGenerator#WORDS} array.
	 */
	{
		final int wordSizeRange = MAX_WORD_LENGTH - MIN_WORD_LENGTH;
		final char[] chars = new char[MAX_WORD_LENGTH];
		int nextIndex = 0;
		for (int i = 0; i < NUM_WORDS; ++i) {
			int wordLength = MIN_WORD_LENGTH + nextInt(wordSizeRange);
			for (int j = 0; j < wordLength; ++j) {
				chars[j] = ALPHABET.charAt(nextInt(ALPHABET_SIZE));
			}

			WORDS[i] = new String(chars, 0, nextIndex);
			nextIndex = 0;
		}
	}
}
