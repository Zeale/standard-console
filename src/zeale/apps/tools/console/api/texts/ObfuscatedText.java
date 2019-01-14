package zeale.apps.tools.console.api.texts;

import org.alixia.javalibrary.javafx.properties.NonNullSimpleObjectProperty;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;

public final class ObfuscatedText extends Text {

	private final static ObjectProperty<String> obfuscatedText = new NonNullSimpleObjectProperty<>("");
	private static final int POSSIBLE_OBFUSCATED_TEXT_CHARS_LOWER_BOUND = 33,
			POSSIBLE_OBFUSCATED_TEXT_CHARS_UPPER_BOUND = 150;
	private static final char[] POSSIBLE_OBFUSCATED_TEXT_CHARS = new char[POSSIBLE_OBFUSCATED_TEXT_CHARS_UPPER_BOUND
			- POSSIBLE_OBFUSCATED_TEXT_CHARS_LOWER_BOUND];
	static {
		for (char i = POSSIBLE_OBFUSCATED_TEXT_CHARS_LOWER_BOUND; i < POSSIBLE_OBFUSCATED_TEXT_CHARS_UPPER_BOUND; i++)
			POSSIBLE_OBFUSCATED_TEXT_CHARS[i - POSSIBLE_OBFUSCATED_TEXT_CHARS_LOWER_BOUND] = i;
	}

	private static Thread obfuscator = new Thread(new Runnable() {

		private final char[] getRandomCharArray(int length) {
			char[] arr = new char[length];
			for (int i = 0; i < length; i++)
				arr[i] = POSSIBLE_OBFUSCATED_TEXT_CHARS[(int) (Math.random() * POSSIBLE_OBFUSCATED_TEXT_CHARS.length)];
			return arr;
		}

		@Override
		public void run() {
			while (texts > 0) {

				obfuscatedText.set(new String(getRandomCharArray(20)));

				try {
					Thread.sleep((long) (1000d / 16));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			obfuscator = new Thread(this);
			obfuscator.setDaemon(true);
			obfuscator.setPriority(Thread.MIN_PRIORITY);
		}
	});
	static {
		obfuscator.setDaemon(true);
	}

	private static long texts = 0;

	{
		texts++;
		if (!obfuscator.isAlive())
			obfuscator.start();
	}

	private boolean disposed;

	/**
	 * @param length The length of the obfuscated text. The maximum allowed value
	 *               for this parameter is <code>20</code>.
	 */
	public ObfuscatedText(int length) {
		final int len = length > 20 ? 20 : length;
		textProperty().bind(Bindings.createStringBinding(
				() -> obfuscatedText.get().substring(0, Math.min(len, obfuscatedText.get().length())), obfuscatedText));
	}

	public void dispose() {
		if (disposed)
			return;
		else
			disposed = true;
		textProperty().unbind();
		texts--;
	}
}