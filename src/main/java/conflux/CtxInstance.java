/**
 * Copyright (C) 2025, Leo Galambos
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. All advertising materials mentioning features or use of this software must
 *    display the following acknowledgement:
 *    This product includes software developed by the Egothor project.
 *
 * 4. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package conflux;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A globally accessible, type-safe context for sharing data between independent
 * parts of an application. Supports listeners that are notified whenever a
 * registered key's value changes.
 *
 * This implementation guarantees:
 * <ul>
 * <li>Strongly typed keys using {@link Key}</li>
 * <li>One consistent type per key name</li>
 * <li>Support for weak-referenced listeners to avoid memory leaks</li>
 * <li>Thread-safe operations</li>
 * </ul>
 *
 * @author Leo Galambos
 */
public final class CtxInstance implements CtxInterface {

    private final Map<Key<?>, Object> values = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> keyTypes = new ConcurrentHashMap<>();
    private final Map<Key<?>, List<WeakReference<Listener<?>>>> listeners = new ConcurrentHashMap<>();

    /**
     * Stores or updates the value for a given key. If the key is used for the first
     * time, its type is recorded. If the key has been used before with a different
     * type, an exception will be thrown.
     *
     * After setting the value, any registered listeners for that key will be
     * notified (if the value was modified).
     *
     * @param key   the key
     * @param value the value to store
     * @param <T>   the type of the value
     * @throws IllegalStateException if the key is reused with a different type
     */
    @Override
    public <T> void put(Key<T> key, T value) {
        keyTypes.compute(key.name(), (k, existingType) -> {
            if (existingType == null) {
                return key.type();
            } else {
                if (!existingType.equals(key.type())) {
                    throw new IllegalStateException(
                            "Key '" + key.name() + "' already associated with type " + existingType.getName());
                }
                return existingType;
            }
        });

        @SuppressWarnings("unchecked")
        T previous = (T) values.put(key, value);

        if (Objects.deepEquals(value, previous)) {
            // no change
            return;
        }

        // notify listeners
        var list = listeners.get(key);
        if (list != null) {
            for (var ref : list) {
                var listener = ref.get();
                if (listener != null) {
                    @SuppressWarnings("unchecked")
                    Listener<T> typedListener = (Listener<T>) listener;
                    typedListener.valueChanged(value);
                }
            }
            // clean up dead weak references
            list.removeIf(ref -> ref.get() == null);
        }
    }

    /**
     * Retrieves the value for the given key, or {@code null} if not set.
     *
     * @param key the key
     * @param <T> the type
     * @return the stored value or null
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Key<T> key) {
        return (T) values.get(key);
    }

    /**
     * Checks if a value exists for the given key.
     *
     * @param key the key
     * @return true if a value is present
     */
    @Override
    public boolean contains(Key<?> key) {
        return values.containsKey(key);
    }

    /**
     * Removes the value and any listeners for the given key.
     *
     * @param key the key
     * @return the removed value, or null if absent
     */
    @Override
    public Object remove(Key<?> key) {
        keyTypes.remove(key.name());
        var removed = values.remove(key);
        listeners.remove(key);
        return removed;
    }

    /**
     * Clears all keys and listeners from the context.
     */
    @Override
    public void clear() {
        values.clear();
        keyTypes.clear();
        listeners.clear();
    }

    /**
     * Registers a listener to be notified whenever the value for the key changes.
     * The listener is weakly referenced to avoid preventing its garbage collection.
     *
     * @param key      the key
     * @param listener the listener
     * @param <T>      the type
     */
    @Override
    public <T> void addListener(Key<T> key, Listener<T> listener) {
        listeners.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(new WeakReference<>(listener));
    }

    /**
     * Unregisters a previously registered listener for the key.
     *
     * @param key      the key
     * @param listener the listener to remove
     * @param <T>      the type
     */
    @Override
    public <T> void removeListener(Key<T> key, Listener<T> listener) {
        var list = listeners.get(key);
        if (list != null) {
            list.removeIf(ref -> {
                var l = ref.get();
                return l == null || l.equals(listener);
            });
            if (list.isEmpty()) {
                listeners.remove(key);
            }
        }
    }
}