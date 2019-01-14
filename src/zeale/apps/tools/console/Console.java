package zeale.apps.tools.console;

import zeale.apps.tools.console.CommandLineInterface.UserInput;
import zeale.apps.tools.console.std.StandardConsole;

public abstract class Console<UIT extends UserInput> extends CommandLineInterface<UIT> {

	/**
	 * Attempts to open the best possible {@link Console} available. This method has
	 * the drawback of not knowing what {@link Console} will be returned beforehand,
	 * (beforehand being compile-time), so it returns its result of the type
	 * {@link Console}. This method should include later include documentation
	 * specifying (to the best of its ability) what, exactly, will be returned by
	 * it, and the conditions under which those returns will be.
	 *
	 * @return The best suitable {@link Console}. This factory method follows suit
	 *         to the general convention of the other factory methods in this class
	 *         that it will return <code>null</code> if no applicable
	 *         {@link Console} is found. In this case, "no 'applicable'
	 *         {@link Console} is found," means that <em>no</em> consoles were
	 *         found.
	 */
	public static final Console<? extends UserInput> openBestConsole() {
		return openStandardConsole();
	}

	/**
	 * <p>
	 * Attempts to open a {@link StandardConsoleFullImpl}. If the
	 * {@link StandardConsoleFullImpl} opens successfully, this method will return
	 * it so that you can manipulate it. If not, <code>null</code> will be returned.
	 * <p>
	 * If an exception, thrown from the creation of the {@link StandardConsole}, is
	 * caught, its stack trace is printed to {@link System#err}.
	 *
	 * @return A <code>new </code>{@link StandardConsole}, or <code>null</code> if
	 *         one can't be made.
	 */
	public static final StandardConsole openStandardConsole() {
		try {
			return new StandardConsole();
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}

	public Console() {
	}

}
