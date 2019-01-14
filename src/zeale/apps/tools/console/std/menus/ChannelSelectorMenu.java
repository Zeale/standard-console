package zeale.apps.tools.console.std.menus;

import java.util.Iterator;

import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;
import main.alixia.javalibrary.javafx.tools.FXTools;
import zeale.apps.tools.console.std.MessageChannelManager;
import zeale.apps.tools.console.std.MessageChannelManager.Channel;
import zeale.apps.tools.console.std.NamedChannel;
import zeale.apps.tools.console.std.StandardConsole;
import zeale.apps.tools.console.std.StandardConsole.StandardConsoleMenu;
import zeale.apps.tools.console.std.StandardConsole.StandardConsoleUserInput;
import zeale.apps.tools.fxnodes.boxes.OptionBox;
import zeale.apps.tools.fxnodes.boxes.OptionBox.Menu;

public class ChannelSelectorMenu extends StandardConsoleMenu {

	private static final Object IS_CHANNEL_TOGGLE_BUTTON__KEY = new Object();
	private final MessageChannelManager<StandardConsoleUserInput> manager;

	private final ToggleGroup channelGroup = new ToggleGroup();
	private final OptionBox options = new OptionBox(Color.GRAY);
	private final Menu activeChannelMenu = options.new Menu("Active Channel");

	{
		root.getChildren().add(options);
		FXTools.setAllAnchors(50, options);
	}

	public ChannelSelectorMenu(StandardConsole owner, MessageChannelManager<StandardConsoleUserInput> manager) {
		owner.super();
		this.manager = manager;
	}

	private void addUpdatedMenuToggles() {
		for (Channel<? super StandardConsoleUserInput> c : manager.getChannels()) {
			ToggleButton button = makeButton(c);
			button.setOnAction(event -> manager.selectChannel(c));
			FXTools.styleInputs(Color.BLACK, Color.WHITE, -1, button);
			channelGroup.getToggles().add(button);
			if (manager.getCurrentChannel() == c)
				channelGroup.selectToggle(button);
			activeChannelMenu.getChildren().add(button);
		}
	}

	private void clearMenuToggles() {
		channelGroup.getToggles().clear();
		for (Iterator<Node> iterator = activeChannelMenu.getChildren().iterator(); iterator.hasNext();) {
			ObservableMap<Object, Object> properties = iterator.next().getProperties();
			if (properties.containsKey(IS_CHANNEL_TOGGLE_BUTTON__KEY)
					&& (boolean) properties.get(IS_CHANNEL_TOGGLE_BUTTON__KEY))
				iterator.remove();
		}
	}

	private RadioButton makeButton(Channel<? super StandardConsoleUserInput> channel) {
		RadioButton button = new RadioButton(
				channel instanceof NamedChannel ? ((NamedChannel<?>) channel).getName() : null);
		button.getProperties().put(IS_CHANNEL_TOGGLE_BUTTON__KEY, true);
		return button;
	}

	public void refreshChannelList() {
		clearMenuToggles();
		addUpdatedMenuToggles();
	}

	@Override
	public void show() {
		stage.toFront();
		if (stage.isShowing())
			return;
		refreshChannelList();
		super.show();
	}

}
