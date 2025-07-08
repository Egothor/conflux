/**
 * Provides a lightweight, type-safe, application-wide context mechanism for
 * sharing strongly typed data among otherwise decoupled classes.
 *
 * The {@link conflux.Ctx} enum serves as a central, generic context storage,
 * supporting key-based put/get operations. Each {@link conflux.Key} instance
 * defines a unique, strongly typed entry in the context, ensuring type
 * consistency and preventing misuse.
 *
 * The {@link conflux.Listener} interface allows clients to observe value
 * changes for specific keys. Listeners are weakly referenced to prevent memory
 * leaks.
 *
 * Typical usage:
 * <ul>
 * <li>Define {@link conflux.Key} instances with explicit types</li>
 * <li>Store values via {@code Ctx.INSTANCE.put(key, value)}</li>
 * <li>Retrieve values via {@code Ctx.INSTANCE.get(key)}</li>
 * <li>Register listeners with {@code Ctx.INSTANCE.addListener()}</li>
 * </ul>
 *
 * Values and listeners can be removed individually, or the entire context can
 * be cleared.
 *
 * Best practices:
 * <ul>
 * <li>Define keys as constants to maintain consistency and avoid
 * collisions</li>
 * <li>Remove listeners when no longer needed</li>
 * <li>Use unique key names to avoid accidental type conflicts</li>
 * </ul>
 *
 * @see conflux.Ctx
 * @see conflux.Key
 * @see conflux.Listener
 */
package conflux;