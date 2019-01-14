package zeale.apps.tools.console.std;

import java.util.HashMap;

import zeale.apps.tools.console.std.data.PropertyMap;
import zeale.apps.tools.console.std.data.PropertyMap.Key;

//I'm REALLY looking at changing this name, but it seems to stick out well...
final class StandardPropertyKey {

	private static final HashMap<String, Key<?>> INITIAL_KEYS = new HashMap<>(1);

	public static final Key<String> FILE_OUTPUT_LOCATION = getKey("file-output-location", String.class);

	public static final Key<Number> NUMBER_DUMMY_KEY = getKey("number dummy key", Number.class);

	public static final Key<String> STRING_DUMMY_KEY = getKey("string dummy key", String.class);

	private static final <T> Key<T> getKey(String id, Class<T> type) {
		Key<T> key = PropertyMap.getKey(id, type);
		INITIAL_KEYS.put(key.id, key);
		return key;
	}

	@SuppressWarnings("unchecked")
	public static final <T> Key<T> valueOf(String name) {

		String correct = PropertyMap.correct(name);
		return (Key<T>) INITIAL_KEYS.get(correct);
	}

}
