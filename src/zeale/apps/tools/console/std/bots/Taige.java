package zeale.apps.tools.console.std.bots;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.alixia.javalibrary.strings.StringTools;

import zeale.apps.tools.console.std.StandardConsole.StandardConsoleUserInput;

public abstract class Taige implements Bot<StandardConsoleUserInput> {

	public abstract class Input {

		{
			inputs.add(this);
		}

		public void add() {
			if (!inputs.contains(this))
				inputs.add(this);
		}

		protected abstract void handle(StandardConsoleUserInput input);

		protected abstract boolean match(StandardConsoleUserInput input);

		public void remove() {
			inputs.remove(this);
		}
	}

	/**
	 * Enables checks to see if any two {@link Input} objects conflict for every
	 * piece of input given to this {@link Taige}.
	 */
	private boolean debugging;

	private final List<Input> inputs = new ArrayList<>();

	public Taige(boolean debugging) {
		this.debugging = debugging;
	}

	public Taige(InputStream file) {
		loadFromInputStream(file);
	}

	public Taige(InputStream file, boolean debugging) {
		this.debugging = debugging;
		loadFromInputStream(file);
	}

	public List<Input> getInputs() {
		return Collections.unmodifiableList(inputs);
	}

	public boolean isDebugging() {
		return debugging;
	}

	public void loadFromInputStream(InputStream inputStream) {
		try (Scanner scanner = new Scanner(inputStream)) {
			class FileInput extends Input {

				private final String[] matches;
				private final String output;

				public FileInput(String output, String... matches) {
					this.output = output;
					this.matches = matches;
				}

				@Override
				protected void handle(StandardConsoleUserInput input) {
					outputMessage(output);
				}

				@Override
				protected boolean match(StandardConsoleUserInput input) {
					return StringTools.equalsAnyIgnoreCase(input.text, matches);
				}

			}
			String output = null;
			List<String> inputs = new ArrayList<>();
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				if (line.isEmpty() || line.startsWith("#"))
					continue;
				if (line.endsWith(":") && line.length() > 1 && line.charAt(line.length() - 2) != '\\') {
					if (output != null)
						new FileInput(output, inputs.toArray(new String[0]));
					output = line.substring(0, line.length() - 1);
				} else
					inputs.add(line);

			}
			if (output != null)
				new FileInput(output, inputs.toArray(new String[0]));
		}
	}

	protected abstract void outputMessage(String message);

	@Override
	public void reply(StandardConsoleUserInput input) {
		Input matched = null;
		for (Input i : inputs)
			if (i.match(input)) {
				if (matched != null)
					throw new RuntimeException("Multiple inputs match, " + matched + " and " + i + ".");
				i.handle(input);
				if (debugging) {
					matched = i;
					continue;
				}
				return;
			}
	}

	public void setDebugging(boolean debugging) {
		this.debugging = debugging;
	}

}
