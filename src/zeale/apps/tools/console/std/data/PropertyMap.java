package zeale.apps.tools.console.std.data;

import java.util.HashMap;

public class PropertyMap {

	public static class Key<T> {
		public final String id;
		public final Class<? extends T> type;

		private Key(String id, Class<? extends T> type) {
			this.id = correct(id);
			this.type = correct(type);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof Key<?>))
				return false;
			Key<?> other = (Key<?>) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (id == null ? 0 : id.hashCode());
			return result;
		}

		@Override
		public String toString() {
			return "Key[id=" + id + ", type=" + type.getSimpleName() + "]";
		}

	}

	@SuppressWarnings("unchecked")
	private static <T> Class<T> correct(Class<T> primitiveClass) {
		return !primitiveClass.isPrimitive() ? primitiveClass
				: primitiveClass == int.class ? (Class<T>) Integer.class
						: primitiveClass == double.class ? (Class<T>) Double.class
								: primitiveClass == boolean.class ? (Class<T>) Boolean.class
										: primitiveClass == long.class ? (Class<T>) Long.class
												: primitiveClass == short.class ? (Class<T>) Short.class
														: primitiveClass == char.class ? (Class<T>) Character.class
																: primitiveClass == byte.class ? (Class<T>) Byte.class
																		: (Class<T>) Float.class;

	}

	public static String correct(String name) {
		// If you're in this method because you're examining the stacktrace due to the
		// below exception, then know that this method is called to refine the input to
		// the PropertyKey constructor. ***A PropertyKey cannot have an empty name.***
		if (name.isEmpty())
			throw new IllegalArgumentException("The name can't be empty.");
		name = name.toUpperCase().replaceAll(" ", "_").replaceAll("-", "_");
		while (name.contains("__"))
			name = name.replaceAll("__", "_");
		while (name.endsWith("_") && name.length() > 1)
			name = name.substring(0, name.length() - 1);
		while (name.startsWith("_") && name.length() > 1)
			name = name.substring(1);
		return name;
	}

	public static <T> Key<T> getKey(String id, Class<? extends T> type) {
		return new Key<>(id, type);
	}

	public static boolean keysEqual(Key<?> key1, Key<?> key2) {
		return key1.type == key2.type;
	}

	private final HashMap<Key<?>, Object> map = new HashMap<>();

	private final HashMap<String, Key<?>> keyMap = new HashMap<>();

	@SuppressWarnings("unchecked")
	public <T> T get(Key<?> key) {
		return keyMap.containsKey(key.id) ? ((Key<T>) keyMap.get(key.id)).type.cast(map.get(key)) : null;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String id) {
		id = correct(id);
		return id == null ? null
				: keyMap.containsKey(id) ? this.<T>get((Key<? extends T>) keyMap.get(correct(id))) : null;
	}

	public <T> T getQuick(Key<T> key) {
		return get(key);
	}

	public <T> T getSafe(Key<T> key) {
		if (!keyMap.containsKey(key.id))
			return null;
		Object item = map.get(key);
		if (!key.type.isInstance(item))
			throw new ClassCastException(
					"The item inside the map can't be casted to the type inside the specified key.");
		return key.type.cast(item);
	}

	public <T> T put(Key<?> key, T value) {// Pays no attention to the type of Key
		return put(key.id, value);
	}

	public <T> T put(String id, T value) {
		id = correct(id);

		@SuppressWarnings("unchecked")
		Key<? extends T> key = new Key<>(id, (Class<? extends T>) value.getClass());
		boolean reput = keyMap.containsKey(id);
		if (reput) {
			Key<?> old = keyMap.get(id);
			if (!old.type.isInstance(value))
				throw new ClassCastException("A " + key.type.getSimpleName()
						+ " can't be placed into the position of this " + old.type.getSimpleName() + ".");
		}

		T old = get(key);

		map.put(key, value);
		if (!reput)
			keyMap.put(id, key);
		return old;
	}

	public <T> T putQuick(Key<T> key, T value) {// Pays no attention to the type of Key
		return put(key, value);
	}

	public <T> T putSafe(Key<T> key, T value) throws ClassCastException {
		if (key == null)
			throw new NullPointerException();
		if (!key.type.isInstance(value))
			throw new ClassCastException("The given key must be of the same type as the item to be placed under it.");
		boolean reput = keyMap.containsKey(key.id);
		if (reput) {
			Key<?> currentKey = keyMap.get(key.id);
			if (!currentKey.type.isInstance(value))
				throw new ClassCastException(
						"The given object is not a viable input for the place of the property under the current key. (value is of an unacceptable type; a "
								+ key.type.getSimpleName() + " can't be placed into the position of this "
								+ currentKey.type.getSimpleName() + ".");
		}
		T old = getSafe(key);// Also checks that return type is an instance of the given key. That's what
								// makes this method "Safe".
		map.put(key, value);
		if (!reput)
			keyMap.put(key.id, key);// Register this new item's key if we're not replacing an old item in the map.
		return old;
	}

	public <T> T remove(Key<?> key) {
		@SuppressWarnings("unchecked")
		Key<? extends T> currentKey = (Key<? extends T>) keyMap.get(key.id);
		if (keyMap.containsValue(key)) {
			if (!keyMap.get(key.id).type.isAssignableFrom(key.type))
				throw new ClassCastException("Type to remove can't be casted to the type of the given key.");
			T out = currentKey.type.cast(map.remove(key));
			keyMap.remove(key.id);
			return out;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> T remove(String id) {
		id = correct(id);
		if (keyMap.containsKey(id))
			return ((Key<T>) keyMap.get(id)).type.cast(map.remove(keyMap.remove(id)));
		else
			return null;
	}

	public <T> void setupKey(Key<T> key) {
		if (keyMap.containsKey(key.id))
			throw new IllegalArgumentException("This map currently contains the given key.");
		keyMap.put(key.id, key);
	}

	public <T> Key<T> setupKey(String id, Class<T> type) {
		id = correct(id);
		type = correct(type);
		if (keyMap.containsKey(id))
			throw new IllegalArgumentException("This map currently contains a key with the given information.");
		Key<T> key = new Key<>(id, type);
		keyMap.put(id, key);
		return key;
	}

	@Override
	public String toString() {
		return "Values: [" + map + "], Keys: [" + keyMap + "]";
	}

}
