# Getting started

This walkthrough connects a producer and a listener through a named context. You need a Java 21 development environment and a Gradle application.

## Add Conflux

Choose a published version from [Maven Central](https://central.sonatype.com/artifact/org.egothor/conflux) and replace `YOUR_VERSION` below.

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.egothor:conflux:YOUR_VERSION'
}
```

## Store and observe a value

Create a `ConfluxExample.java` file in your application:

```java
import conflux.Ctx;
import conflux.CtxInterface;
import conflux.Key;
import conflux.Listener;

public final class ConfluxExample {
    public static void main(String[] args) {
        CtxInterface context = Ctx.INSTANCE.getContext("example");
        Key<String> status = Key.of("status", String.class);
        Listener<String> listener = value -> System.out.println("Status: " + value);

        context.addListener(status, listener);
        context.put(status, "ready");
        context.put(status, "ready");
        System.out.println("Stored: " + context.get(status));

        context.removeListener(status, listener);
        Ctx.INSTANCE.removeContext("example");
    }
}
```

Compile and run this class using your application's normal Java launch configuration. The output is:

```text
Status: ready
Stored: ready
```

The first `put` stores the value and invokes the listener on the calling thread. The second stores an equal value, so it does not invoke the listener again. Removing the listener ends the subscription; removing the named context clears its stored state and registrations.

## Choose the next step

Read [Contexts and events](guide/context-and-events.md) to decide how your components share scope and ownership. Consult the [behaviour reference](reference/behaviour.md) before relying on equality, error handling or concurrent access.

Documentation owner: Conflux maintainers · Reviewed: 18 October 2026 · Next review: 18 January 2027.
