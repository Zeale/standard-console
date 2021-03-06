package zeale.apps.tools.console.std;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;

import org.alixia.javalibrary.javafx.bindings.BindingTools;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import main.alixia.javalibrary.javafx.tools.FXTools;
import zeale.apps.tools.console.CommandLineInterface.UserInput;
import zeale.apps.tools.console.Console;
import zeale.apps.tools.console.interfaces.windows.Closable;
import zeale.apps.tools.console.interfaces.windows.Fullscreenable;
import zeale.apps.tools.console.interfaces.windows.Repositionable;
import zeale.apps.tools.console.interfaces.windows.Resizable;
import zeale.apps.tools.console.interfaces.windows.Showable;
import zeale.apps.tools.console.std.MessageChannelManager.Channel;
import zeale.apps.tools.console.std.StandardConsole.StandardConsoleUserInput;
import zeale.apps.tools.console.std.data.PropertyMap;
import zeale.apps.tools.console.std.menus.ChannelSelectorMenu;
import zeale.apps.tools.guis.BasicWindow;

public class StandardConsole extends Console<StandardConsoleUserInput> {

	private static abstract class ConsoleWriter extends Writer {

		private volatile boolean closed;

		private final StringBuilder chars = new StringBuilder(20);
		private final boolean autoflush;

		public ConsoleWriter(boolean autoflush) {
			this.autoflush = autoflush;
		}

		@Override
		public final void close() throws IOException {
			closed = true;
		}

		@Override
		public final void flush() throws IOException {
			synchronized (lock) {
				if (closed)
					throw new IOException("Writer has been closed.");
				flush(chars.toString());
				chars.setLength(0);
			}
		}

		protected abstract void flush(String content);

		@Override
		public final void write(char[] cbuf, int off, int len) throws IOException {
			synchronized (lock) {
				if (closed)
					throw new IOException("Writer has been closed.");
				chars.append(cbuf, off, len);
			}
			if (autoflush)
				flush();
		}

	}

	public final class EmbeddedStandardConsoleView extends BasicWindow {

		public final class DefaultOptionButton extends OptionButton {

			{
				options.getChildren().add(this);
			}

			public DefaultOptionButton(Image icon, Runnable onClicked) {
				this(new ImageView(icon), onClicked);
			}

			public DefaultOptionButton(Image icon, String onClicked) {
				this(new ImageView(icon), onClicked);
			}

			public DefaultOptionButton(ImageView icon, Runnable onClicked) {
				super(icon, onClicked);
			}

			public DefaultOptionButton(ImageView icon, String onClicked) {
				this(icon, new Runnable() {
					@Override
					public void run() {
						EmbeddedStandardConsoleView.this.send(onClicked);
					}
				});

				StandardConsole.this.print("");
			}

			public void hide() {
				options.getChildren().remove(this);
			}

			public void show() {
				if (!options.getChildren().contains(this))
					options.getChildren().add(this);
			}

			public void show(int position) {
				if (!options.getChildren().contains(this))
					options.getChildren().add(position, this);
			}

		}

		// TODO Add scrolling mechanism to option buttons.

		/*
		 * OPTION BUTTON - for option menu in top right
		 */
		public class OptionButton extends Button {

			{
				setBackground(DEFAULT_NODE_BACKGROUND);

				setPrefSize(32, 32);
				FXTools.styleInputs(Color.BLACK, Color.WHITE, -1, this);
			}

			public OptionButton(Image icon, Runnable onClicked) {
				this(new ImageView(icon), onClicked);
			}

			public OptionButton(Image icon, String onClicked) {
				this(new ImageView(icon), onClicked);
			}

			public OptionButton(ImageView icon, Runnable onClicked) {
				setGraphic(icon);
				icon.setFitHeight(16);
				icon.setFitWidth(16);
				setOnAction(a -> onClicked.run());
			}

			public OptionButton(ImageView icon, String onClicked) {
				this(icon, new Runnable() {
					@Override
					public void run() {
						EmbeddedStandardConsoleView.this.send(onClicked);
					}
				});
			}

			protected final void send() {
				EmbeddedStandardConsoleView.this.send();
			}

			protected final void send(String text) {
				EmbeddedStandardConsoleView.this.send(text);
			}

		}

		private final TextFlow screen = new TextFlow();

		private final ObservableList<Node> items = FXCollections.synchronizedObservableList(screen.getChildren());
		// Binding
		{
			BindingConversion.bind(StandardConsole.this.getItems(), ConsoleItem::getNode, getItems());
		}

		private final ScrollPane flow = new ScrollPane(screen);
		/*
		 * STYLING RELATED CODE
		 */

		private final TextArea input = new TextArea();

		public TextArea getInput() {
			return input;
		}

		public TextFlow getScreen() {
			return screen;
		}

		public Button getSendButton() {
			return send;
		}

		public void setPrimaryColor(Paint color) {
			setBackground(new Background(new BackgroundFill(color, null, null)));
		}

		public void setSecondaryColor(Paint color) {
			input.setBackground(new Background(new BackgroundFill(color, null, null)));

		}

		public void setInputHeight(double height) {
			input.setPrefHeight(height);
		}

		private final Button send = new Button("Send");
		private final VBox options = new VBox(3);
		private final StackPane inputWrapper = new StackPane(input, send);
		private final SplitPane wrapper = new SplitPane(flow, inputWrapper);
		{
			send.opacityProperty().bind(BindingTools.mask(send.hoverProperty(), a -> a ? 1 : 0.2));

			input.setWrapText(true);
			screen.setBackground(DEFAULT_WINDOW_BACKGROUND);
			flow.setBackground(null);
			setBackground(FXTools.getBackgroundFromColor(new Color(0.3, 0.3, 0.3, 1)));

			FXTools.styleInputs(new Color(0.17, 0.17, 0.17, 1), Color.BLACK, -1, send, input);
			FXTools.clearScrollPaneBackground(flow);

			StackPane.setAlignment(send, Pos.CENTER_RIGHT);
			wrapper.setOrientation(Orientation.VERTICAL);

			AnchorPane.setTopAnchor(wrapper, 20d);
			AnchorPane.setTopAnchor(options, 20d);// Same as flow
			AnchorPane.setLeftAnchor(wrapper, 40d);
			AnchorPane.setRightAnchor(wrapper, 40d);
			AnchorPane.setBottomAnchor(wrapper, 40d);
			StackPane.setMargin(send, new Insets(0, 50, 0, 0));

			AnchorPane.setRightAnchor(options, 2d);
			wrapper.getStylesheets().add("zeale/apps/tools/console/standard-console.css");
			wrapper.setBackground(null);

			input.setFont(Font.font("Monospace", 17));
			input.setId("StandardConsoleInput");
			send.setId("SendButton");

			flow.setFitToHeight(true);
			flow.setFitToWidth(true);

			wrapper.getDividers().get(0).setPosition(0.9);
		}

		/*
		 * INITIALIZATION
		 */
		{
			input.textProperty().bindBidirectional(StandardConsole.this.input);
			input.addEventFilter(KeyEvent.KEY_PRESSED, a -> {
				if (a.getCode() == KeyCode.ENTER) {
					if (a.isShiftDown())
						input.replaceSelection("\n");
					else {
						send();
						a.consume();
					}
				}
			});
			send.setOnAction((a) -> send());
		}

		{
			// // When the default channel, (or any other channel that can handle these
			// // buttons' text), is not selected, these buttons may not work correctly;
			// (they
			// // will likely just print out their raw command with the tilde, since nothing
			// is
			// // there to handle the text and prevent it from being printed).
			// new
			// OptionButton(Images.loadImageInBackground("/zeale/apps/tools/resources/graphics/Gear-v1.png"),
			// () -> send("~open-window settings"));
			// new
			// OptionButton(Images.loadImageInBackground("/zeale/apps/tools/resources/graphics/Notepad-v1.png"),
			// () -> send("~set-output-file"));
//			new DefaultOptionButton(
//					Images.loadImageInBackground("/zeale/apps/tools/resources/graphics/Channels-v1.png"),
//					() -> getChannelSelectorMenu().show());
		}

		private EmbeddedStandardConsoleView() {
			getChildren().addAll(wrapper, options);
		}

//		{
//			// When the default channel, (or any other channel that can handle these
//			// buttons' text), is not selected, these buttons may not work correctly; (they
//			// will likely just print out their raw command with the tilde, since nothing is
//			// there to handle the text and prevent it from being printed).
//			new OptionButton(Images.loadImageInBackground("/zeale/apps/tools/resources/graphics/Gear-v1.png"),
//					() -> send("~open-window settings"));
//			new OptionButton(Images.loadImageInBackground("/zeale/apps/tools/resources/graphics/Notepad-v1.png"),
//					() -> send("~set-output-file"));
//			new OptionButton(Images.loadImageInBackground("/zeale/apps/tools/resources/graphics/Channels-v1.png"),
//					() -> getChannelSelectorMenu().show());
//		}

		private ObservableList<Node> getItems() {
			return items;
		}

		private void send() {
			send(input.getText());
			input.setText("");
		}

		private void send(String text) {
			StandardConsole.this.send(text);
		}

	}

	private static final class QuickConsoleWriter extends ConsoleWriter {

		private final Consumer<? super String> handler;

		public QuickConsoleWriter(boolean autoflush, Consumer<? super String> handler) {
			super(autoflush);
			this.handler = handler;
		}

		@Override
		protected void flush(String content) {
			handler.accept(content);
		}

	}

	public class StandardConsoleMenu implements StandardConsoleStage {

		protected final Stage stage = new Stage();
		protected final AnchorPane root = new AnchorPane();
		protected final Scene scene = new Scene(root);

		{
			stage.setScene(scene);
			root.setBackground(StandardConsole.DEFAULT_WINDOW_BACKGROUND);
		}

		@Override
		public void close() {
			stage.close();
		}

		@Override
		public void show() {
			stage.show();
		}

	}

	public final static class StandardConsoleUserInput extends UserInput {

		/**
		 * Determines whether this input was passed via the user physically clicking the
		 * <code>send</code> button with the mouse or hitting
		 * <code>enter</code>/<code>return</code>, (whatever you want to call it), while
		 * focus was on the input text box. This variable will be <code>true</code> if
		 * the user clicked the send button, and <code>false</code> if the user hit
		 * <code>enter</code>. If this variable is <code>null</code>, then whether or
		 * not the user clicked send to initiate this input is unknown.
		 */
		public final Boolean clicked;

		public StandardConsoleUserInput(String text) {
			this(text, null);
		}

		private StandardConsoleUserInput(String text, Boolean clicked) {
			super(text);
			this.clicked = clicked;
		}

	}

	// Note to self: This class uses its enclosing instance's properties. Any
	// classes that extend it should be nested here (for the most part).
	public final class StandardConsoleView implements Showable, Closable, Fullscreenable, Repositionable, Resizable {

		private final EmbeddedStandardConsoleView root = new EmbeddedStandardConsoleView();

		private final Scene scene = new Scene(root);
		private Stage stage;

		/*
		 * CONSTRUCTOR
		 */
		private StandardConsoleView() {
			this(new Stage());

			stage.setHeight(800);
			stage.setWidth(1000);
			stage.setMinHeight(400);
			stage.setMinWidth(600);
		}

		private StandardConsoleView(Stage stage) {
			this.stage = stage;
			stage.setScene(scene);
		}

		@Override
		public void close() {
			stage.close();
		}

		public Stage getStage() {
			return stage;
		}

		/*
		 * OPTION MENU INITIALIZATION
		 */

		@Override
		public void hide() {
			stage.hide();
		}

		@Override
		public boolean resize(double width, double height) {
			stage.setWidth(width);
			stage.setHeight(height);
			return width < stage.getMinWidth() || width > stage.getMaxWidth() || height < stage.getMinHeight()
					|| height > stage.getMaxHeight();
		}

		/*
		 * INSTANCE METHODS
		 */

		@Override
		public void setFullscreen(boolean fullscreen) {
			stage.setFullScreen(fullscreen);
		}

		@Override
		public boolean setPosition(double x, double y) {
			stage.setX(x);
			stage.setY(y);
			return true;
		}

		public void setStage(Stage stage) {
			this.stage.hide();
			stage.setScene(scene);
			this.stage.setScene(null);
			(this.stage = stage).show();
		}

		@Override
		public void show() {
			stage.show();
		}

	}

	/*
	 * CONSTANTS
	 */
	private static final String CONSOLE_VERSION = "1.0";

	private static final Color DEFAULT_NODE_COLOR = new Color(0.12, 0.12, 0.12, 0.7);

	private static final Background DEFAULT_WINDOW_BACKGROUND = FXTools.getBackgroundFromColor(DEFAULT_NODE_COLOR),
			DEFAULT_NODE_BACKGROUND = FXTools.getBackgroundFromColor(Color.gray(0.4));

	private static final Function<String, ConsoleItem> simpleConverter = t -> new ConsoleItem().setColor(Color.ORANGE)
			.setFontSize(20).setText(t);

	/*
	 * FIELDS
	 */
	private final ObservableList<ConsoleItem> items = FXCollections
			.synchronizedObservableList(FXCollections.observableList(new LinkedList<>()));

	public void clear() {
		items.clear();
	}

	private final StringProperty input = new SimpleStringProperty();
	/*
	 * FIELDS - PROPERTY MAP
	 */
	private final PropertyMap properties = new PropertyMap();
	/*
	 * INITIALIZATION MESSAGE
	 */
	{
		getItems().add(new ConsoleItem().setText("Standard Console V" + CONSOLE_VERSION + "\n").setColor(Color.GOLD)
				.setFontFamily("Monospace").setWeight(FontWeight.BOLD));
	}

	/*
	 * STANDARD CONSOLE VIEWS
	 */

	private final NamedChannel<StandardConsoleUserInput> defaultChannel = new NamedChannel<>("Default Channel",
			message -> pushInput(new StandardConsoleUserInput(message.text, null)));

	private final MessageChannelManager<StandardConsoleUserInput> channelManager = new MessageChannelManager<>(
			defaultChannel);

	private final Map<Consumer<?>, Channel<StandardConsoleUserInput>> userMadeHandlers = new HashMap<>(0);

	private ChannelSelectorMenu channelSelectorMenu;

	public ChannelSelectorMenu getChannelSelectorMenu() {
		return channelSelectorMenu == null ? (channelSelectorMenu = new ChannelSelectorMenu(this, channelManager))
				: channelSelectorMenu;
	}

	/**
	 * Adds a channel to this {@link StandardConsole} which will receive user input
	 * when selected. The user can select the channel that the user wishes to use
	 * via a GUI opened by a button on the console window.
	 *
	 * @param name    The name of the channel. This is solely to distinguish between
	 *                other channels.
	 * @param handler The {@link String} {@link Consumer} that will handle the
	 *                user's input. If wanted, this must print text to the console,
	 *                as the console won't print any input consumed by another
	 *                channel, by default.
	 */
	public void addChannel(String name, Consumer<String> handler) {
		NamedChannel<StandardConsoleUserInput> channel = new NamedChannel<>(name, t -> handler.accept(t.text));
		channelManager.addChannel(channel);
		userMadeHandlers.put(handler, channel);
	}

	private ObservableList<ConsoleItem> getItems() {
		return items;
	}

	public EmbeddedStandardConsoleView getEmbeddedView() {
		return new EmbeddedStandardConsoleView();
	}

	public StandardConsoleView getView() {
		return new StandardConsoleView();
	}

	public StandardConsoleView getView(Stage stage) {
		return new StandardConsoleView(stage);
	}

	public PrintWriter getWriter() {
		return getWriter(simpleConverter);
	}

	public PrintWriter getWriter(Consumer<? super String> handler) {// This isn't really a StandardConsole-specific
																	// method...
		return new PrintWriter(new QuickConsoleWriter(true, handler::accept));
	}

	public PrintWriter getWriter(Function<? super String, ? extends ConsoleItem> converter) {
		return getWriter((Consumer<String>) t -> StandardConsole.this.write(converter.apply(t)));
	}

	public PrintWriter getWriter(Color color, FontWeight weight, FontPosture posture) {
		return getWriter((Consumer<String>) t -> StandardConsole.this
				.write(new ConsoleItem().setColor(color).setWeight(weight).setPosture(posture).setText(t)));
	}

	@Override
	public void print(String text, Color color, boolean bold, boolean italicized) {
		write(new ConsoleItem().setText(text).setColor(color).setWeight(ConsoleItem.convertToValidWeight(bold))
				.setPosture(ConsoleItem.convertToValidPosture(italicized)));
	}

	public void removeChannel(Consumer<String> handler) {
		channelManager.removeChannel(userMadeHandlers.remove(handler));
	}

	private void send(String text) {

		channelManager.send(new StandardConsoleUserInput(text));
	}

	public void write(ConsoleItem item) {
		if (!Platform.isFxApplicationThread())
			Platform.runLater(() -> write(item));
		else
			getItems().add(item);
	}

	public void write(int pos, ConsoleItem item) {
		if (!Platform.isFxApplicationThread())
			Platform.runLater(() -> write(pos, item));
		else
			getItems().add(pos, item);
	}

	public void write(InputStream content) {
		write(content, simpleConverter);
	}

	public void write(InputStream content, Function<? super String, ? extends ConsoleItem> converter) {
		try (Scanner scanner = new Scanner(content)) {
			while (scanner.hasNextLine())
				write(converter.apply(scanner.nextLine()));
		} catch (Throwable e) {

		}

	}

	public PropertyMap getProperties() {
		return properties;
	}

}
