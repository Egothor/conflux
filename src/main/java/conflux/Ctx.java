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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
     * The default context instance used by this enum singleton. All interface
     * method calls delegate to this context.
     */
    private final CtxInterface defaultCtx = new CtxInstance();

    /**
     * A registry of named contexts, allowing for multiple isolated logical
     * contexts.
     */
    private final Map<String, NamedWeakRef> contexts = new ConcurrentHashMap<>();

    /**
     * A reference queue to learn when weakly referenced contexts are collected.
     */
    private final transient ReferenceQueue<CtxInterface> refQueue = new ReferenceQueue<>();

    /**
     * A weak reference wrapper that associates a {@link CtxInterface} instance (the
     * referent) with its corresponding context name.
     * <p>
     * Instances of this class are enqueued into a {@link ReferenceQueue} once their
     * referent becomes unreachable. The extra {@code name} field allows the owning
     * registry to efficiently remove the corresponding entry from the context map
     * without requiring a reverse lookup.
     * </p>
     *
     * <h2>Usage</h2>
     * <ul>
     * <li>Created whenever a new named context is registered.</li>
     * <li>Placed into a {@link ConcurrentHashMap} keyed by the same
     * {@code name}.</li>
     * <li>When the referent is garbage-collected, this reference is automatically
     * enqueued, and the registry removes the map entry associated with
     * {@link #name}.</li>
     * </ul>
     *
     * <p>
     * This design ensures that contexts are automatically deregistered when no
     * strong references to them remain, preventing memory leaks while still
     * allowing explicit removal via {@code removeContext}.
     * </p>
     */
    private static final class NamedWeakRef extends WeakReference<CtxInterface> {
        /**
         * The context name associated with the referent.
         */
        private final String name;

        /**
         * Creates a new weak reference to the given context instance and associates it
         * with the provided name.
         *
         * @param name     the context name; used as the key in the registry
         * @param referent the context instance being weakly referenced
         * @param q        the reference queue with which the weak reference is
         *                 registered
         */
        private NamedWeakRef(String name, CtxInterface referent, ReferenceQueue<CtxInterface> q) {
            super(referent, q);
            this.name = name;
        }
    }

    /**
     * Remove collected references from map.
     */
    private void drainQueue() {
        for (NamedWeakRef ref; (ref = (NamedWeakRef) refQueue.poll()) != null;) { // NOPMD
            contexts.remove(ref.name, ref);
        }
    }

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
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
        drainQueue();
        NamedWeakRef ref = contexts.compute(name, (k, existing) -> {
            CtxInterface alive = existing == null ? null : existing.get();
            if (alive != null) {
                return existing;
            }
            CtxInterface created = new CtxInstance();
            return new NamedWeakRef(k, created, refQueue);
        });
        CtxInterface ctx = ref.get();
        if (ctx == null) {
            CtxInterface created = new CtxInstance();
            NamedWeakRef fresh = new NamedWeakRef(name, created, refQueue);
            contexts.put(name, fresh);
            ctx = created;
        }
        return ctx;
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
        drainQueue();
        NamedWeakRef ref = contexts.remove(name);
        if (ref == null) {
            return false; // NOPMD
        }
        CtxInterface ctx = ref.get();
        if (ctx != null) {
            try {
                ctx.clear();
            } catch (RuntimeException ignore) { // NOPMD
            }
        }
        return true;
    }

    /**
     * Returns the set of names for all currently registered contexts.
     *
     * @return an immutable set of context names
     */
    public Set<String> contextNames() {
        drainQueue();
        return Set.copyOf(contexts.entrySet().stream().filter(e -> e.getValue().get() != null).map(Map.Entry::getKey)
                .collect(Collectors.toSet()));
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
