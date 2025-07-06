/**
 * Provides a lightweight, type-safe, application-wide context mechanism for
 * sharing strongly typed data among otherwise decoupled classes.
 * <p>
 * The {@link conflux.Ctx} class acts as a central, generic context storage
 * supporting key-based put/get operations. Each {@link conflux.Key} defines a
 * unique, strongly typed entry in the context, ensuring type consistency and
 * preventing misuse. {@link conflux.Listener} interfaces allow clients to
 * observe value changes for specific keys. Listeners are held with weak
 * references to avoid memory leaks.
 * <p>
 * Typical usage involves defining {@link conflux.Key} instances with explicit
 * types, storing values through {@code Ctx.INSTANCE.put()}, and retrieving them
 * with {@code Ctx.INSTANCE.get()}. Listeners can be attached via
 * {@code Ctx.INSTANCE.addListener()} to react to context changes in a decoupled
 * and thread-safe manner. Values and listeners can be removed individually or
 * the entire context can be cleared.
 * <p>
 * <b>Best Practices:</b>
 * <ul>
 * <li>Define keys as constants to maintain consistency and avoid
 * collisions.</li>
 * <li>Remove listeners when no longer needed (although weak references help
 * prevent leaks).</li>
 * <li>Use unique key names to ensure no accidental type conflicts.</li>
 * </ul>
 *
 * @see conflux.Ctx
 * @see conflux.Key
 * @see conflux.Listener
 */
package conflux;