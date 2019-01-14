package zeale.apps.tools.console.std;

public interface StandardConsoleStage {
	void close();

	default void hide() {
		close();
	}

	void show();
}
