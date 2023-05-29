package se.sundsvall.templating.api.domain.filter.util;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Stream-related utilities.
 */
public final class StreamUtil {

    private StreamUtil() { }

    /**
     * Creates a {@code Stream<T>} from the provided {@code Iterable<T>}.
     *
     * @param iterable an iterable
     * @return a stream
     * @param <T> the type of both the iterable and the resulting stream
     */
    public static <T> Stream<T> fromIterable(final Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    /**
     * Creates a {@code Stream<T>} from the provided {@code Iterator<T>}.
     *
     * @param iterator an iterator
     * @return a stream
     * @param <T> the type of both the iterator and the resulting stream
     */
    public static <T> Stream<T> fromIterator(final Iterator<T> iterator) {
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(iterator, Spliterator.NONNULL), false);
    }
}
