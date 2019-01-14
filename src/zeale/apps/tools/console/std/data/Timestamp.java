package zeale.apps.tools.console.std.data;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class Timestamp {

	private final Instant stamp = Instant.now();

	public Timestamp() {
	}

	public Duration getPassedTime(Timestamp other) {
		return Duration.between(stamp, other.stamp);
	}

	public Instant getStamp() {
		return stamp;
	}

	public String print(DateTimeFormatter formatter) {
		return formatter.format(stamp);
	}

}
