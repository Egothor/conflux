# Behaviour reference

This page describes the current implementation contract. For signatures and class details, open the [JavaDoc](https://www.egothor.org/conflux/javadoc/).

## Values and keys

| Operation | Behaviour |
| --- | --- |
| `Key.of(name, type)` | Creates a key whose equality and hash code depend on its name. Use a non-null name and class. |
| `put(key, value)` | Records the declared class for the name, stores the value, then notifies listeners if it changed. A different previously recorded class causes `IllegalStateException`. |
| `get(key)` | Returns the stored value, or `null` if absent. Does not check the key's declared class. |
| `contains(key)` | Reports whether the context contains a value for the key. |
| `remove(key)` | Removes the value, recorded type and listener registrations for that name; returns the previous value or `null`. Does not notify listeners. |
| `clear()` | Clears values, recorded types and all listener registrations. Does not notify listeners. |

The backing value map rejects null keys and values. A key name's recorded class can be reused after `remove` or `clear`. Avoid raw types and unchecked casts: the runtime class check compares key declarations, rather than checking each value with `Class.cast`.

## Change detection

Notifications use `Objects.deepEquals(newValue, previousValue)` to detect equality. Equal writes suppress notifications, including arrays with equal contents. The value is stored even when notification is suppressed.

Mutable values need care. Mutating an object already held in the context does not call `put`, so no event occurs. Putting the same mutated instance back also compares it with itself and suppresses notification. Publish a distinct value when consumers need to observe a change.

## Listener delivery

| Concern | Behaviour |
| --- | --- |
| Execution | Synchronous, on the thread calling `put`. |
| Initial value | Registration does not deliver the current value. |
| Lifetime | Listener references are weak; subscribers must retain their own strong references. |
| Repeated registration | Each call adds a registration; the same live listener can receive repeated callbacks for one change. |
| Unregistration | Removes registrations matching the listener by equality and cleans dead references. |
| Callback failure | Propagates out of `put`; later listeners in that delivery are skipped. The new value has already been stored. |
| Removal | Removing a value or clearing a context also removes its listener registrations without callbacks. |

## Named contexts

`getContext(name)` lazily creates a context. Null or blank names cause `IllegalArgumentException`, including in `removeContext(name)`. The registry uses weak references and cleans collected entries during registry operations.

`removeContext(name)` returns `false` when no registry entry exists, and `true` when it removes an entry. It clears the referenced context when still live. `contextNames()` returns an immutable snapshot of registered names whose references were live when inspected.

## Concurrency boundaries

The implementation uses concurrent maps for values, type declarations and named contexts, and copy-on-write lists for listeners. These collections support concurrent access, but the operations spanning them are not a transaction. Do not assume atomic read-modify-write sequences, coordinated `clear` and `put`, or globally ordered notifications across writing threads. Concurrent writers can invoke the same listener concurrently.

Coordinate multi-step application invariants outside the context. Listener implementations must protect their own shared mutable state when writers can run on multiple threads.

Documentation owner: Conflux maintainers · Reviewed: 18 October 2026 · Next review: 18 January 2027.
