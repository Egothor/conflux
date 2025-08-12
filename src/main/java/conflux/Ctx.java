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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A central context manager for storing and retrieving key-value pairs in a
 * thread-safe, type-safe manner.
 * 
 * This enum serves as both:
 * <ul>
 * <li>A singleton context via {@code Ctx.INSTANCE} for global/shared usage</li>
 * <li>A factory and registry for multiple named independent contexts</li>
 * </ul>
 * 
 * Each context provides listener support for reactive value changes.
 *
 * @see Key
 * @see Listener
 * 
 * @author Leo Galambos
 */
public enum Ctx implements CtxInterface {
    /**
     * The singleton instance representing the default/global context.
     */
    INSTANCE;

    /**
     * A registry of named contexts, allowing for multiple isolated logical
     * contexts.
     */
    private final Map<String, CtxInterface> contexts = new ConcurrentHashMap<>();
    /**
     * The default context instance used by this enum singleton. All interface
     * method calls delegate to this context.
     */
    private final CtxInterface defaultCtx = new CtxInstance();

    /**
     * Returns a named context. If the context does not exist, it is lazily created.
     * 
     * This method allows isolation between different logical scopes (e.g.,
     * sessions, users).
     *
     * @param name the name of the context
     * @return the associated context instance
     */
    public CtxInterface getContext(String name) {
        return contexts.computeIfAbsent(name, k -> new CtxInstance());
    }

    /**
     * Removes a named context previously created via {@link #getContext(String)}.
     * <p>
     * The context is atomically removed from the internal registry and then
     * {@linkplain CtxInterface#clear() cleared} to release stored values and
     * listener references. This operation is thread-safe and idempotent: if the
     * specified context does not exist, the method returns {@code false} and
     * performs no action.
     * </p>
     * <p>
     * Calling {@code getContext(name)} again after a successful removal will create
     * a brand-new, empty context. The default/global context represented by
     * {@link #INSTANCE} is not affected.
     * </p>
     *
     * @param name the name of the context to remove; must not be {@code null} or
     *             blank
     * @return {@code true} if a context with the given name existed and was
     *         removed; {@code false} otherwise
     * @throws IllegalArgumentException if {@code name} is {@code null} or blank
     * @implNote Uses {@link ConcurrentHashMap#remove(Object)} for atomic removal,
     *           then invokes {@link CtxInterface#clear()} on the removed instance.
     * @since 2025.08
     */
    public boolean removeContext(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
        CtxInterface ctx = contexts.remove(name);
        final boolean existed = ctx != null;
        if (existed) {
            try {
                ctx.clear();
            } catch (RuntimeException ignore) { // NOPMD
                // Best-effort cleanup; deregistration already succeeded.
            }
        }
        return existed;
    }

    /**
     * Returns the set of names for all currently registered contexts.
     *
     * @return an immutable set of context names
     */
    public Set<String> contextNames() {
        return Set.copyOf(contexts.keySet());
    }

    /**
     * {@inheritDoc}
     * 
     * Delegates to the default context instance.
     */
    @Override
    public <T> void put(Key<T> key, T value) {
        defaultCtx.put(key, value);
    }

    /**
     * {@inheritDoc}
     * 
     * Delegates to the default context instance.
     */
    @Override
    public <T> T get(Key<T> key) {
        return defaultCtx.get(key);
    }

    /**
     * {@inheritDoc}
     * 
     * Delegates to the default context instance.
     */
    @Override
    public boolean contains(Key<?> key) {
        return defaultCtx.contains(key);
    }

    /**
     * {@inheritDoc}
     * 
     * Delegates to the default context instance.
     */
    @Override
    public Object remove(Key<?> key) {
        return defaultCtx.remove(key);
    }

    /**
     * {@inheritDoc}
     * 
     * Delegates to the default context instance.
     */
    @Override
    public void clear() {
        defaultCtx.clear();
    }

    /**
     * {@inheritDoc}
     * 
     * Delegates to the default context instance.
     */
    @Override
    public <T> void addListener(Key<T> key, Listener<T> listener) {
        defaultCtx.addListener(key, listener);
    }

    /**
     * {@inheritDoc}
     * 
     * Delegates to the default context instance.
     */
    @Override
    public <T> void removeListener(Key<T> key, Listener<T> listener) {
        defaultCtx.removeListener(key, listener);
    }
}
