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

import org.alixia.chatroom.api.commands.Command;
import org.alixia.chatroom.api.commands.CommandManager;
import org.alixia.javalibrary.strings.StringTools;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import main.alixia.javalibrary.javafx.tools.FXTools;
import zeale.apps.tools.api.backgrounds.RedVelvetBackground;
import zeale.apps.tools.console.CommandLineInterface.UserInput;
import zeale.apps.tools.console.Console;
import zeale.apps.tools.console.interfaces.windows.Closable;
import zeale.apps.tools.console.interfaces.windows.Fullscreenable;
import zeale.apps.tools.console.interfaces.windows.Repositionable;
import zeale.apps.tools.console.interfaces.windows.Resizable;
import zeale.apps.tools.console.interfaces.windows.Showable;
import zeale.apps.tools.console.std.MessageChannelManager.Channel;
import zeale.apps.tools.console.std.StandardConsole.StandardConsoleUserInput;
import zeale.apps.tools.console.std.bots.Taige;
import zeale.apps.tools.console.std.data.PropertyMap;
import zeale.apps.tools.console.std.data.PropertyMap.Key;
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

	public final class EmbeddedStandardConsoleView extends BasicWindow {

		/*
		 * OPTION BUTTON - for option menu in top right
		 */
		public class OptionButton extends Button {

			{
				setBackground(DEFAULT_NODE_BACKGROUND);

				setPrefSize(32, 32);
				FXTools.styleInputs(Color.BLACK, Color.WHITE, -1, this);
			}

			private OptionButton(Image icon, Runnable onClicked) {
				this(new ImageView(icon), onClicked);
			}

			private OptionButton(Image icon, String onClicked) {
				this(new ImageView(icon), onClicked);
			}

			private OptionButton(ImageView icon, Runnable onClicked) {
				setGraphic(icon);
				icon.setFitHeight(16);
				icon.setFitWidth(16);
				setOnAction(a -> onClicked.run());
			}

			private OptionButton(ImageView icon, String onClicked) {
				this(icon, new Runnable() {

					@Override
					public void run() {
						EmbeddedStandardConsoleView.this.send(onClicked);
					}
				});
			}

			protected final void send(String text) {
				EmbeddedStandardConsoleView.this.send(text);
			}

			protected final void send() {
				EmbeddedStandardConsoleView.this.send();
			}

		}

		private EmbeddedStandardConsoleView() {
			getChildren().addAll(input, flow, send, options);
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
		private final Button send = new Button("Send");
		private final VBox options = new VBox(3);

		{
			screen.setBackground(DEFAULT_WINDOW_BACKGROUND);
			flow.setBackground(null);
			setBackground(FXTools.getBackgroundFromColor(new Color(0.3, 0.3, 0.3, 1)));

			FXTools.styleInputs(Color.BLACK, Color.DIMGRAY, -1, send, input);
			FXTools.clearScrollPaneBackground(flow);

			AnchorPane.setBottomAnchor(input, 0d);
			AnchorPane.setLeftAnchor(input, 0d);
			AnchorPane.setRightAnchor(input, 0d);
			input.setPrefHeight(200);
			input.setMaxHeight(200);

			AnchorPane.setTopAnchor(flow, 20d);
			AnchorPane.setTopAnchor(options, 20d);// Same as flow
			AnchorPane.setLeftAnchor(flow, 40d);
			AnchorPane.setRightAnchor(flow, 40d);
			AnchorPane.setBottomAnchor(flow, 300d);

			AnchorPane.setRightAnchor(options, 2d);
			input.getStylesheets().add("zeale/apps/tools/console/standard-console.css");

			AnchorPane.setBottomAnchor(send, 135d);
			AnchorPane.setRightAnchor(send, 75d);

			input.setFont(Font.font("Monospace", 17));

			flow.setFitToHeight(true);
			flow.setFitToWidth(true);
		}

		/*
		 * INITIALIZATION
		 */
		{
			input.textProperty().bindBidirectional(StandardConsole.this.input);
			input.setOnKeyPressed((a) -> {
				if (!a.isShiftDown() && a.getCode() == KeyCode.ENTER) {
					send();
					a.consume();
				}
			});
			send.setOnAction((a) -> send());
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
//					() -> channelSelectorMenu.show());
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

	// Note to self: This class uses its enclosing instance's properties. Any
	// classes that extend it should be nested here (for the most part).
	public final class StandardConsoleView implements Showable, Closable, Fullscreenable, Repositionable, Resizable {

		private final EmbeddedStandardConsoleView root = new EmbeddedStandardConsoleView();

		private final Scene scene = new Scene(root);
		private Stage stage;

		public void setStage(Stage stage) {
			this.stage.hide();
			stage.setScene(scene);
			this.stage.setScene(null);
			(this.stage = stage).show();
		}

		public Stage getStage() {
			return stage;
		}

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

		/*
		 * OPTION MENU INITIALIZATION
		 */

		@Override
		public void close() {
			stage.close();
		}

		@Override
		public void hide() {
			stage.hide();
		}

		/*
		 * INSTANCE METHODS
		 */

		@Override
		public boolean resize(double width, double height) {
			stage.setWidth(width);
			stage.setHeight(height);
			return width < stage.getMinWidth() || width > stage.getMaxWidth() || height < stage.getMinHeight()
					|| height > stage.getMaxHeight();
		}

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

		@Override
		public void show() {
			stage.show();
		}

	}

	public final static class App extends Application {

		@Override
		public void start(Stage primaryStage) throws Exception {
			StandardConsole console = new StandardConsole();
			console.getView(primaryStage).show();

		}

	}

	public static void main(String[] args) {
		Application.launch(App.class, args);
	}

	/*
	 * CONSTANTS
	 */
	private static final String CONSOLE_VERSION = "1.0";

	private static final Color DEFAULT_NODE_COLOR = new Color(0.12, 0.12, 0.12, 0.7);

	private static final Background DEFAULT_WINDOW_BACKGROUND = FXTools.getBackgroundFromColor(DEFAULT_NODE_COLOR),
			DEFAULT_NODE_BACKGROUND = FXTools.getBackgroundFromColor(Color.gray(0.4));

	private static final Function<String, ConsoleItem> simpleConverter = t -> new ConsoleItem().setColor(Color.ORANGE)
			.setFontSize(20).setText(t);// DO NOT make this into a method reference; it will cause the text objects
										// inside the TextFlow to be empty.

	/*
	 * FIELDS
	 */
	private final ObservableList<ConsoleItem> items = FXCollections
			.synchronizedObservableList(FXCollections.observableList(new LinkedList<>()));

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

	private final Taige bot = new Taige(getClass().getResourceAsStream("Taige.bot")) {

		@Override
		protected void outputMessage(String message) {
			println(message, Color.ORANGE);
		};

		@Override
		public void reply(StandardConsoleUserInput input) {
			println(input.text, Color.ORANGERED);
			super.reply(input);
		}
	};

	private final CommandManager privateCommandManager = new CommandManager("~");

	/*
	 * STANDARD CONSOLE VIEWS
	 */

	private final NamedChannel<StandardConsoleUserInput> defaultChannel = new NamedChannel<>("Default Channel",
			message -> {
				if (!privateCommandManager.runCommand(message.text))
					pushInput(new StandardConsoleUserInput(message.text, null));
			}), botChannel = new NamedChannel<>("Bot Channel", bot::reply),
			rawChannel = new NamedChannel<>("Raw Text Channel", t -> println(t.text));

	private final MessageChannelManager<StandardConsoleUserInput> channelManager = new MessageChannelManager<>(
			defaultChannel, botChannel, rawChannel);

	private final Map<Consumer<?>, Channel<StandardConsoleUserInput>> userMadeHandlers = new HashMap<>(0);

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
		NamedChannel<StandardConsoleUserInput> channel = new NamedChannel<StandardConsoleUserInput>(name,
				t -> handler.accept(t.text));
		channelManager.addChannel(channel);
		userMadeHandlers.put(handler, channel);
	}

	public void removeChannel(Consumer<String> handler) {
		channelManager.removeChannel(userMadeHandlers.remove(handler));
	}

	private final ChannelSelectorMenu channelSelectorMenu = new ChannelSelectorMenu(this, channelManager);

	/*
	 * COMMANDS
	 */
	{
		privateCommandManager.addCommand(new Command() {

			@Override
			protected void act(String name, String... args) {
				// TODO
				// hide();
			}

			@Override
			protected boolean match(String name) {
				return equalsAnyIgnoreCase(name, "Close", "Quit") || name.equals("X");
			}
		});
		privateCommandManager.addCommand(new Command() {

			@Override
			protected void act(String name, String... args) {
				// TODO
				// stage.setIconified(true);
			}

			@Override
			protected boolean match(String name) {
				return equalsAnyIgnoreCase(name, "minimize", "hide", "iconify");
			}
		});

		// TODO Impl.
		privateCommandManager.addCommand(new Command() {

			@Override
			protected void act(String name, String... args) {
				if (args.length < 1)
					println("Not enough arguments!", Color.FIREBRICK);
				else
					println("Window not found.", Color.RED);
			}

			@Override
			protected boolean match(String name) {
				return name.equalsIgnoreCase("open-window");
			}
		});

		privateCommandManager.addCommand(new Command() {

			private final PrintWriter writer = getWriter();

			@Override
			@SuppressWarnings("unchecked")
			protected void act(String name, String... args) {
				if (args.length < 1)
					println("Not enough arguments!", Color.FIREBRICK);
				else {
					Key<?> key = StandardPropertyKey.valueOf(args[0]);
					if (key == null) {
						print("No property was found with the id: ", Color.FIREBRICK);
						print(args[0], Color.RED);
						println(".", Color.FIREBRICK);
					} else
					// Handle key being null (meaning that no property by the given ID was found).
					if (args.length == 2)
						try {
							properties.putSafe((Key<String>) key, args[1]);
							print(key.id, Color.MINTCREAM);
							print(" was set to a value of ", Color.BROWN);
							print(args[1], Color.MINTCREAM);
							println(".", Color.BROWN);
						} catch (ClassCastException e) {
							e.printStackTrace(writer);
							print("Failed to assign the property: ", Color.FIREBRICK);
							print(key.id, Color.RED);
							print(" the value: ", Color.FIREBRICK);
							print(args[1], Color.RED);
							println(".", Color.FIREBRICK);
							print(args[1], Color.RED);
							print(", (a String), is not of the type permitted by the property, ", Color.FIREBRICK);
							print(key.id, Color.RED);
							print(", (", Color.FIREBRICK);
							print(key.type.getCanonicalName(), Color.RED);
							println(").", Color.FIREBRICK);
						}
					else if (args.length == 3) {// Assume conversion, treat args[1] as a cast.
						// TODO Handle conversion
					}
				}
			}

			@Override
			protected boolean match(String name) {
				return name.equalsIgnoreCase("set");
			}
		});

		privateCommandManager.addCommand(new Command() {

			@Override
			protected void act(String name, String... args) {
				try (PrintWriter writer = getWriter()) {
					new RuntimeException().printStackTrace(writer);
				}
			}

			@Override
			protected boolean match(String name) {
				return name.equalsIgnoreCase("test");
			}
		});

		privateCommandManager.addCommand(new Command() {

			private final Stage saver = new Stage(StageStyle.UNDECORATED);
			private final Canvas canvas = new Canvas();
			private final StackPane canvasWrapper = new StackPane(canvas);
			private final zeale.apps.tools.api.backgrounds.Background background = new RedVelvetBackground(canvas);
			private final Scene scene = new Scene(canvasWrapper);
			{
				saver.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
				saver.setFullScreen(true);
				EventHandler<Event> hideHandler = event -> saver.hide();
				saver.addEventFilter(KeyEvent.KEY_PRESSED, hideHandler);
				saver.addEventFilter(MouseEvent.MOUSE_PRESSED, hideHandler);
				saver.addEventFilter(MouseEvent.MOUSE_RELEASED, hideHandler);
				canvas.widthProperty().bind(canvasWrapper.widthProperty());
				canvas.heightProperty().bind(canvasWrapper.heightProperty());
				saver.setScene(scene);
			}

			@Override
			protected void act(String name, String... args) {
				println("Showing screen saver");
				saver.show();
				background.show();
			}

			@Override
			protected boolean match(String name) {
				return StringTools.equalsAnyIgnoreCase(name, "screensaver", "screen-saver", "ss", "scs");
			}
		});

	}

	public StandardConsole() {
	}

	private ObservableList<ConsoleItem> getItems() {
		return items;
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

	/**
	 * <b>Important Note</b> to the user: For some odd reason that I'm too sleepy to
	 * think about right now, if your <code>converter</code> is a method reference
	 * (specifically, the part, of your converter, that sets the text of the
	 * {@link ConsoleItem} that your converter returns, is a method reference
	 * (simple e.g.:
	 *
	 * <pre>
	 * <code>getWriter(new ConsoleItem()::setItem()</code>
	 * </pre>
	 *
	 * )), then things that quickly print to the {@link PrintWriter} that you get
	 * from this method, will end up showing empty {@link Text} objects to the user.
	 * This goes for the {@link #getWriter(Consumer)} method as well (but I'm sleepy
	 * and I figured out this problem finally so I'm only typing this once right
	 * now), and it did go for the {@link #getWriter()} during testing (which used a
	 * {@link #simpleConverter}, which caused issues for the aforementioned reason.
	 * Feel free to look at it).
	 *
	 * @param converter A custom converter that's given a String and gives back a
	 *                  {@link ConsoleItem}. The {@link ConsoleItem} will be printed
	 *                  to the console.
	 * @return The {@link PrintWriter} you can print to.
	 */
	public PrintWriter getWriter(Function<? super String, ? extends ConsoleItem> converter) {
		return getWriter((Consumer<String>) t -> StandardConsole.this.write(converter.apply(t)));
	}

	@Override
	public void print(String text, Color color, boolean bold, boolean italicized) {
		write(new ConsoleItem().setText(text).setColor(color).setWeight(ConsoleItem.convertToValidWeight(bold))
				.setPosture(ConsoleItem.convertToValidPosture(italicized)));
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

}
