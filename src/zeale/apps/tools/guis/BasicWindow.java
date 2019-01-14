package zeale.apps.tools.guis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alixia.chatroom.api.QuickList;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

public class BasicWindow extends AnchorPane {

	/**
	 * <p>
	 * A nice and tidy "Box" for storing children of this {@link BasicWindow} that
	 * will all be shown at the same time, a {@link ChildPack} can be constructed
	 * using a BasicWindow's {@link BasicWindow#getChildPack(Node...)} method. All
	 * the children given will be added to this {@link ChildPack}. Whenever the
	 * application needs to remove a group of items from the BasicWindow, it can
	 * call the {@link #remove()} method. If the application needs to add the
	 * children, the {@link #add()} method can be used. Note: Children are not added
	 * by default.
	 * <p>
	 * The purpose of this class is to allow applications to manage children easier.
	 * All functionality of a BasicWindow's {@link BasicWindow#getChildren()} list
	 * can still be invoked safely. The reason this class exists is because, when
	 * switching windows containing large amounts of children (all a part of a
	 * BasicWindow), the {@link ObservableList#setAll(Object...)} method was being
	 * invoked, to assure that no excess, untracked nodes were being kept in the
	 * BasicWindow when its children were being set. Using that method removed the
	 * {@link BasicWindow#backgroundCanvas} from the BasicWindow.
	 * <p>
	 * With this class, adding and removing children will be easier to accomplish.
	 * (BTW next commit will have ChildPacks that can be nested.)
	 *
	 * @author Zeale
	 *
	 */
	public final class ChildPack {

		private final List<Node> children;
		{
			childBoxes.add(this);
		}

		private ChildPack(boolean add, List<Node> children) {
			this.children = new ArrayList<>(children);
			if (add)
				add();
		}

		private ChildPack(boolean add, Node... children) {
			this.children = new QuickList<>(children);
			if (add)
				add();
		}

		private ChildPack(Node... children) {
			this.children = new QuickList<>(children);
		}

		public void add() {
			for (Node n : children)
				getChildren().add(n);
		}

		public void addAlone() {
			for (ChildPack cp : childBoxes)
				if (cp != this)
					cp.remove();
			add();
		}

		private boolean compareHolder(BasicWindow otherHolder) {
			return otherHolder == BasicWindow.this;
		}

		public ChildPack merge(ChildPack children) {
			if (!compareHolder(BasicWindow.this))
				throw new IllegalArgumentException("ChildPacks can only be merged if they are from the same parent.");

			// Adapted from stackoverflow answer. Maybe I should learn about Streams...
			return new ChildPack(false, Stream.of(children.children, this.children).flatMap(Collection::stream)
					.collect(Collectors.toList()));
		}

		public void remove() {
			for (Node n : children)
				getChildren().remove(n);
		}

	}

	private final List<ChildPack> childBoxes = new ArrayList<>();

	public BasicWindow() {
	}

	public BasicWindow(Node... children) {
		getChildren().addAll(children);// Children must be added after background canvas.
	}

	public ChildPack getChildPack(Node... children) {
		return new ChildPack(children);
	}

}
