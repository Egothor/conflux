---
title: Conflux
hide:
  - navigation
  - toc
---

<div class="conflux-hero" markdown>
<div class="conflux-eyebrow">EGOTHOR · JAVA LIBRARY</div>

# Shared state. Connected components.

Conflux combines a named, typed context with value-change notifications. Share data between Java components and let each component react through a small, explicit API.

[Get started](getting-started.md){ .md-button .md-button--primary }
[Explore the JavaDoc](https://www.egothor.org/conflux/javadoc/){ .md-button }

<div class="conflux-facts"><span>Java 21</span><span>Typed keys</span><span>Named contexts</span><span>Synchronous listeners</span></div>
</div>

## A small API for shared work

Use a context as the meeting point between producers and consumers. Producers store values with `put`; consumers read them with `get` or register a `Listener` for changes. Named contexts separate application scopes, while `Ctx.INSTANCE` supplies a default context.

```java
import conflux.Ctx;
import conflux.CtxInterface;
import conflux.Key;
import conflux.Listener;

CtxInterface context = Ctx.INSTANCE.getContext("search-session");
Key<String> query = Key.of("query", String.class);
Listener<String> onQuery = value -> System.out.println("Query: " + value);

context.addListener(query, onQuery);
context.put(query, "information retrieval");
context.removeListener(query, onQuery);
```

Keep a strong reference to the context and its listeners for as long as they are needed: the named-context registry and listener registrations use weak references.

<div class="grid cards" markdown>

- **Start with a working example**

    Add the dependency, create a context and observe your first change.

    [Getting started →](getting-started.md)

- **Design the scope of shared state**

    Choose keys, isolate contexts and manage listener lifetimes.

    [Contexts and events →](guide/context-and-events.md)

- **Understand the contract**

    Look up equality, notification, removal and concurrency behaviour.

    [Behaviour reference →](reference/behaviour.md)

- **Build and contribute**

    Run checks and build the manual and generated API documentation.

    [Development →](development.md)

</div>

## Go deeper

The [JavaDoc](https://www.egothor.org/conflux/javadoc/) describes individual classes and methods. The [source repository](https://github.com/Egothor/conflux) contains the implementation, tests and release configuration.

Documentation owner: Conflux maintainers · Reviewed: 18 October 2026 · Next review: 18 January 2027.
