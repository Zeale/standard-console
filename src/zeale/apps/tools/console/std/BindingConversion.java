package zeale.apps.tools.console.std;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import com.sun.javafx.binding.ContentBinding;

import javafx.beans.WeakListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 *
 * @author <b>Zeale</b>;
 *         <p>
 *         I did make this class, but I took a heavy dose of
 *         {@link ContentBinding} to do so. A lot of the code here, as you may
 *         find, is similar to (or the same as) that of {@link ContentBinding}.
 *
 */
final class BindingConversion {

	private static final class ListBinder<F, T> implements ListChangeListener<F>, WeakListener {

		private final WeakReference<List<T>> out;
		private final Function<? super F, ? extends T> converter;

		public ListBinder(List<T> out, Function<? super F, ? extends T> converter) {
			this.out = new WeakReference<>(out);
			this.converter = converter;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;

			final List<T> list1 = out.get();
			if (list1 == null)
				return false;

			if (obj instanceof ListBinder) {
				final ListBinder<?, ?> other = (ListBinder<?, ?>) obj;
				final List<?> list2 = other.out.get();
				return list1 == list2;
			}
			return false;
		}

		@Override
		public int hashCode() {
			final List<T> list = out.get();
			return list == null ? 0 : list.hashCode();
		}

		@Override
		public void onChanged(Change<? extends F> c) {
			List<T> out = this.out.get();

			if (out == null)
				c.getList().removeListener(this);
			else
				while (c.next())
					if (c.wasPermutated()) {
						out.subList(c.getFrom(), c.getTo()).clear();
						out.addAll(c.getFrom(), convert(c.getList().subList(c.getFrom(), c.getTo()), converter));
					} else if (c.wasRemoved())
						out.subList(c.getFrom(), c.getFrom() + c.getRemovedSize()).clear();
					else if (c.wasAdded())
						out.addAll(c.getFrom(), convert(c.getAddedSubList(), converter));
		}

		@Override
		public boolean wasGarbageCollected() {
			return out.get() == null;
		}

	}

	public static <F, T> void bind(ObservableList<F> from, Function<? super F, ? extends T> converter, List<T> to) {
		if (to == null || from == null || converter == null)
			throw new NullPointerException("Both parameters must be specified.");
		else if (to == from)
			throw new IllegalArgumentException("Cannot bind object from itself");

		to.clear();
		to.addAll(convert(from, converter));

		ListBinder<F, T> contentBinding = new ListBinder<>(to, converter);
		from.addListener(contentBinding);
	}

	private static <F, T> List<T> convert(List<F> from, Function<? super F, ? extends T> converter) {
		List<T> out = new LinkedList<>();
		for (F f : from)
			out.add(converter.apply(f));
		return out;
	}

	private BindingConversion() {
	}

}
