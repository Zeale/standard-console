package zeale.apps.tools.console.interfaces;

import org.alixia.chatroom.api.printables.StyledPrintable;

public interface FullyPrintable extends RawPrintable, StyledPrintable {
	@Override
	default void printRaw(String text) {
		print(text);
	}
}
