package zeale.apps.tools.console.api.texts;

import javafx.beans.binding.Bindings;

public class Text extends javafx.scene.text.Text {

	public static final Text getMissingNodeText() {
		return new Text("Missing Item");
	}

	private boolean unclearable;

	public Text() {
		super();
	}

	public Text(double x, double y, String text) {
		super(x, y, text);
	}

	public Text(String text) {
		super(text);
	}

	private Text(Text original) {
		effectProperty().bind(original.effectProperty());
		textProperty().bind(original.textProperty());
		fillProperty().bind(original.fillProperty());
		fontProperty().bind(original.fontProperty());
		opacityProperty().bind(original.opacityProperty());
		disableProperty().bind(original.disableProperty());
		Bindings.bindContentBidirectional(getProperties(), original.getProperties());
	}

	@Override
	public Text clone() {
		return new Text(this);
	}

	public boolean isUnclearable() {
		return unclearable;
	}

	public void setUnclearable(boolean unclearable) {
		this.unclearable = unclearable;
	}

}