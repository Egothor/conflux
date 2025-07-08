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

/**
 * A type-safe, thread-safe context interface for storing and retrieving
 * key-value pairs.
 *
 * Implementations of this interface support:
 * <ul>
 * <li>Recording type-safe associations between keys and values</li>
 * <li>Automatic listener notification when a value changes</li>
 * <li>Context isolation (e.g., per name or use case)</li>
 * </ul>
 *
 * This interface is sealed to only permit known, safe implementations.
 *
 * @see Key
 * @see Listener
 * 
 * @author Leo Galambos
 */
public sealed interface CtxInterface permits Ctx, CtxInstance {

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
    <T> void put(Key<T> key, T value);

    /**
     * Retrieves the value for the given key, or {@code null} if not set.
     *
     * @param key the key
     * @param <T> the type
     * @return the stored value or null
     */
    <T> T get(Key<T> key);

    /**
     * Checks if a value exists for the given key.
     *
     * @param key the key
     * @return true if a value is present
     */
    boolean contains(Key<?> key);

    /**
     * Removes the value and any listeners for the given key.
     *
     * @param key the key
     * @return the removed value, or null if absent
     */
    Object remove(Key<?> key);

    /**
     * Clears all keys and listeners from the context.
     */
    void clear();

    /**
     * Registers a listener to be notified whenever the value for the key changes.
     * The listener is weakly referenced to avoid preventing its garbage collection.
     *
     * @param key      the key
     * @param listener the listener
     * @param <T>      the type
     */
    <T> void addListener(Key<T> key, Listener<T> listener);

    /**
     * Unregisters a previously registered listener for the key.
     *
     * @param key      the key
     * @param listener the listener to remove
     * @param <T>      the type
     */
    <T> void removeListener(Key<T> key, Listener<T> listener);

}