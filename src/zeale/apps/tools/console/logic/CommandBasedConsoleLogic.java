package zeale.apps.tools.console.logic;

import org.alixia.chatroom.api.commands.CommandManager;

import zeale.apps.tools.console.CommandLineInterface.UserInput;

public final class CommandBasedConsoleLogic<IT extends UserInput> extends CommandManager implements ConsoleLogic<IT> {

	@Override
	public final void handle(IT input) {
		runCommand(input.text);
	}

}
