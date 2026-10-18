# Contexts and events

A context owns a set of named values and listener registrations. Use it where several components need to exchange state without holding direct references to one another.

## Choose a context boundary

`Ctx.INSTANCE` implements `CtxInterface` and delegates its data operations to the default context. Use `Ctx.INSTANCE.getContext("name")` to obtain a separate named context, for example for a session or processing job.

```java
CtxInterface first = Ctx.INSTANCE.getContext("first-job");
CtxInterface second = Ctx.INSTANCE.getContext("second-job");
Key<Integer> progress = Key.of("progress", Integer.class);

first.put(progress, 25);
second.put(progress, 80);
```

The two values are independent, and neither is stored in the default context. `contextNames()` returns an immutable set of currently live registered names.

Named contexts are held weakly by the registry. The component that owns a session or job must keep a strong reference to its context. A later lookup can create an empty context after the previous instance has been collected.

Use `removeContext(name)` when that scope ends. It removes the registry entry and clears a still-live context. Existing references still point to that cleared object; a subsequent lookup creates a new context. Removing a named context does not clear the default context.

## Define keys as a shared contract

Create a key with `Key.of(name, type)`. Its generic type helps Java check normal `put` and `get` calls. Give each concept a stable name and a single agreed class, ideally in a shared constants class.

Key equality uses the **name alone**. Two separately created keys named `progress` address the same entry even if their class declarations differ. On `put`, a context records the declared class for that name and rejects a different class with `IllegalStateException`. Reads and listener registration do not perform that check. Reuse the agreed key rather than creating conflicting declarations.

## Own the listener lifetime

`Listener<T>` is a functional interface with one method, `valueChanged(T newValue)`. Register a listener using `addListener` and unregister it using the same listener object and `removeListener`.

```java
Listener<Integer> progressListener = value -> System.out.println(value + "%");
first.addListener(progress, progressListener);
first.put(progress, 50);
first.removeListener(progress, progressListener);
```

Registrations hold weak references. Store the listener in a field owned by the subscribing component when it must survive beyond the current method. An inline lambda with no retained reference can be collected. Registering a listener does not replay an existing value; use `get` when you also need the current state.

## Treat notifications as part of the write

`put` stores a value before notifying listeners. Callbacks run synchronously on the thread calling `put`, so a slow callback delays the write's return. An unchecked exception from a callback propagates to the caller and interrupts delivery to later listeners; the stored value remains updated.

Use callbacks for bounded work. If your application needs asynchronous processing, submit work to an executor that your application owns, and define its ordering and failure handling there.

See [Behaviour reference](../reference/behaviour.md) for equality and removal semantics, or the [JavaDoc](https://www.egothor.org/conflux/javadoc/) for method signatures.

Documentation owner: Conflux maintainers · Reviewed: 18 October 2026 · Next review: 18 January 2027.
