package zeale.apps.tools.console.std;

import java.util.function.Consumer;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import zeale.apps.tools.console.std.MessageChannelManager.Channel;

public class NamedChannel<T> implements Channel<T> {
	private final StringProperty name = new SimpleStringProperty();
	private final Consumer<T> handler;

	public NamedChannel(String name, Consumer<T> handler) {
		this.name.set(name);
		this.handler = handler;
	}

	public final String getName() {
		return this.nameProperty().get();
	}

	@Override
	public void handle(T message) {
		handler.accept(message);
	}

	public final StringProperty nameProperty() {
		return this.name;
	}

	public final void setName(final String name) {
		this.nameProperty().set(name);
	}

}
