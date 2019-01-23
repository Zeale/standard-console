package zeale.apps.tools.fxnodes.boxes;

import java.io.File;

import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import main.alixia.javalibrary.javafx.tools.FXTools;

public class OptionBox extends ScrollPane {

	public class Menu extends VBox {

		public class FileChooserListing extends MenuListing {
			public final Button button = new Button("...");
			public final FileChooser chooser = new FileChooser();
			public final TextField textBox = new TextField();

			{
				FXTools.styleInputs(laxColor, activeColor, -1, textBox, button);

				textBox.setPrefWidth(400);
				textBox.setStyle("-fx-text-fill: white");
				setHgrow(textBox, Priority.ALWAYS);
				setHgrow(button, Priority.NEVER);

				button.setOnAction(event -> show(window));

				getChildren().addAll(textBox, button);
			}

			public FileChooserListing() {
			}

			/**
			 * @return <code>null</code> if the {@link #textBox} is empty. Otherwise,
			 *
			 *         <pre>
			 * <code>new File(textBox.getText())</code>
			 *         </pre>
			 *
			 *         is returned.
			 */
			public File getFile() {
				return textBox.getText().isEmpty() ? null : new File(textBox.getText());
			}

			public void show(Window owner) {
				File result = chooser.showOpenDialog(owner);
				if (result != null)
					textBox.setText(result.getAbsolutePath());
			}

		}

		public class MenuListing extends HBox {
			{
				Menu.this.getChildren().add(this);
				setAlignment(Pos.CENTER);
			}

			public MenuListing() {
				super(5);
			}

			public MenuListing(double spacing) {
				super(spacing);
			}

			public MenuListing(double spacing, Node... children) {
				super(spacing, children);
			}

			public MenuListing(Node... children) {
				this(5, children);
			}

		}

		private final Text title = new Text();

		{
			title.setFont(Font.font("Monospace", FontWeight.BOLD, 22));
			title.setFill(Color.WHITE);
			getChildren().add(title);
			setFillWidth(true);
			setAlignment(Pos.CENTER);
			box.getChildren().add(this);
			setPadding(new Insets(20));
			setBorder(FXTools.DEFAULT_NODE_BORDER);
			setSpacing(20);
		}

		public Menu(double spacing) {
			super(spacing);
		}

		public Menu(double spacing, Node... children) {
			super(spacing, children);
		}

		public Menu(Node... children) {
			super(children);
		}

		public Menu(String title) {
			setTitle(title);
		}

		public Menu(String title, double spacing, Node... children) {
			this(spacing, children);
			setTitle(title);
		}

		public String getTitle() {
			return title.getText();
		}

		public void setTitle(String text) {
			title.setText(text);
		}

		public StringProperty titleProperty() {
			return title.textProperty();
		}

	}

	private Window window;

	private final Color laxColor, activeColor;

	private final VBox box = new VBox();

	{
		box.setFillWidth(true);
		box.setAlignment(Pos.TOP_CENTER);
		box.setPadding(new Insets(45));
		box.setBorder(FXTools.DEFAULT_NODE_BORDER);
		setContent(box);
		FXTools.clearScrollPaneBackground(this);
		setBackground(null);
		setFitToHeight(true);
		setFitToWidth(true);
		// contentProperty().bind(new SimpleObjectProperty<>(box));
	}

	public OptionBox() {
		laxColor = Color.BLACK;
		activeColor = Color.RED;
	}

	public OptionBox(Color activeColor) {
		laxColor = Color.BLACK;
		this.activeColor = activeColor;
	}

	public OptionBox(Color laxColor, Color activeColor) {
		this.laxColor = laxColor;
		this.activeColor = activeColor;
	}

	public Window getWindow() {
		return window;
	}

	public void setWindow(Window window) {
		this.window = window;
	}
}
