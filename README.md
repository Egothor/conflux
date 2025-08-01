<img src=conflux.png width=20% align="right" />

# Conflux

**Conflux** is a Java library that provides a shared processing context
combined with an event bus, allowing independent components to share named
values and react to their changes in a clean and decoupled way.

## Key Features

- Shared context for storing and retrieving named values
- Publish/subscribe event bus for listening to value changes
- Simple API for easy integration into existing projects
- Enables modular design by decoupling components through events

## Getting Started

Add to your `build.gradle`:
```groovy
repositories {
    
    maven {
        name = "GiteaMaven"
        url = uri("https://gitea.egothor.org/api/packages/Egothor/maven")
    }
    
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    implementation 'org.egothor:conflux:x.x.x'
}
```

## Documentation

- [Conflux JavaDoc](https://www.egothor.org/javadoc/conflux/)
- [Conflux Wiki](https://gitea.egothor.org/Egothor/conflux/wiki)
