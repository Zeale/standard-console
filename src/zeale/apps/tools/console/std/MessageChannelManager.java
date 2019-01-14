package zeale.apps.tools.console.std;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.alixia.chatroom.api.QuickList;

public class MessageChannelManager<T> {
	public interface Channel<T> {
		void handle(T message);
	}

	private volatile Channel<? super T> currentChannel;

	private final QuickList<Channel<? super T>> channels = new QuickList<>();

	@SafeVarargs
	public MessageChannelManager(Channel<? super T>... channels) {
		if (channels == null)
			throw null;
		for (Channel<? super T> c : channels)
			if (c == null)
				throw null;
		this.channels.addAll(channels);
		if (new HashSet<>(this.channels).size() < this.channels.size())
			throw new RuntimeException("This manager cannot contain duplicate channels.");
		finishConstruction();
	}

	public MessageChannelManager(Collection<Channel<? super T>> channels) {
		if (channels.contains(null))
			throw null;
		if (new HashSet<>(channels).size() < channels.size())
			throw new IllegalArgumentException("This manager cannot contain duplicate channels.");
		this.channels.addAll(channels);
		finishConstruction();
	}

	public void addChannel(Channel<? super T> channel) {
		if (channel == null)
			throw null;
		if (!channels.contains(channel))
			channels.add(channel);
	}

	private void finishConstruction() {
		selectChannel(0);
	}

	public List<Channel<? super T>> getChannels() {
		return Collections.unmodifiableList(channels);
	}

	public Channel<? super T> getCurrentChannel() {
		return currentChannel;
	}

	public void removeChannel(Channel<? super T> channel) {
		if (channels.size() == 1)
			throw new RuntimeException("Can't remove the last channel in a channel manager");
		channels.remove(channel);
		if (currentChannel == channel)
			currentChannel = channels.getFirst();
	}

	public void selectChannel(Channel<? super T> channel) {
		// assert channels.contains(channel);
		if (!channels.contains(channel))
			throw new IllegalArgumentException("That channel is not tracked by this manager.");
		currentChannel = channel;
	}

	public void selectChannel(int index) {
		selectChannel(channels.get(index));
	}

	public void send(T message) {
		currentChannel.handle(message);
	}

	public void sendToChannel(Channel<? super T> channel, T message) {
		// assert channels.contains(channel);
		if (!channels.contains(channel))
			throw new IllegalArgumentException("That channel is not tracked by this manager.");
		channel.handle(message);
	}

	public void sendToChannel(int index, T message) {
		channels.get(index).handle(message);
	}
}
