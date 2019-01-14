package zeale.apps.tools.guis;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

public class Home extends BasicWindow {

	private final BorderPane layout = new BorderPane();

	{
		getChildPack(layout).addAlone();
	}

	public Home() {
	}

	public Home(Node... children) {
		super(children);
	}

}
