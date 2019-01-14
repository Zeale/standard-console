package zeale.apps.tools.console.logic;

import zeale.apps.tools.console.CommandLineInterface.UserInput;

public interface ConsoleLogic<T extends UserInput> {
	void handle(T input);
}
