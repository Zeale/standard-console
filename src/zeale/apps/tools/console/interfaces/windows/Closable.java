package zeale.apps.tools.console.interfaces.windows;

public interface Closable {

	void close();

	default void hide() {
		close();
	}

}
