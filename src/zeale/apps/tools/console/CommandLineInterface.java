package zeale.apps.tools.console;

import java.util.LinkedList;

import zeale.apps.tools.console.CommandLineInterface.UserInput;
import zeale.apps.tools.console.interfaces.FullyPrintable;
import zeale.apps.tools.console.logic.ConsoleLogic;

public abstract class CommandLineInterface<UIT extends UserInput> implements FullyPrintable {

	public static abstract class UserInput {
		public final String text;

		protected UserInput(String text) {
			this.text = text;
		}

	}

	private volatile ConsoleLogic<? super UIT> logic;

	private final LinkedList<UIT> inputs = new LinkedList<>();

	public final void applyLogic(ConsoleLogic<? super UIT> logic) {
		this.logic = logic;
	}

	public final UIT popInput() {
		if (inputs.isEmpty())
			try {
				inputs.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		return inputs.pop();
	}

	protected final void pushInput(UIT input) {
		if (logic != null)
			logic.handle(input);
		else
			synchronized (inputs) {
				inputs.push(input);
				inputs.notify();
			}
	}

}
