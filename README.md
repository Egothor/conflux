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
    mavenCentral()
}

dependencies {
    implementation 'org.egothor:conflux:x.x.x'
}
```

## Documentation

- [Conflux JavaDoc](https://www.egothor.org/javadoc/conflux/)
- [Conflux Wiki](https://gitea.egothor.org/Egothor/conflux/wiki)

## Releases on GitHub

Push a tag such as `release@1.2.3` to run `.github/workflows/release.yml`.
The tag suffix becomes the Maven version. The workflow runs `check`, builds
and signs the main, sources and Javadoc JARs and POM, uploads a bundle to
Maven Central, waits for `PUBLISHED`, then creates a GitHub Release.
The `org.egothor` namespace must be verified for your Central account.

Configure these GitHub Actions repository secrets before the first release:

- `CENTRAL_BEARER_TOKEN`: the Central Publisher API bearer value, used as supplied.
- `MAVEN_SIGNING_KEY`: the ASCII-armored OpenPGP private signing key.
- `MAVEN_SIGNING_PASSWORD`: the key passphrase (empty for an unencrypted key).

The corresponding public key must be available on a public keyserver that
Maven Central supports. The bearer value authenticates publishing; OpenPGP
signatures independently satisfy Central's artifact signing requirement.

To check a release locally without uploading it:

```sh
./gradlew clean check
# With MAVEN_SIGNING_KEY and MAVEN_SIGNING_PASSWORD set in the environment:
./gradlew publishMavenJavaPublicationToCentralBundleRepository -PreleaseVersion=1.2.3
```

The local Maven layout is written to `build/central-repository`. Publication
is irreversible once Central reports `PUBLISHED`; subsequent corrections
require a new version. If the workflow fails after upload, inspect its
reported deployment ID in the Central Portal before retrying. An already
published version cannot be uploaded again; if only GitHub Release creation
failed, create that release from the existing tag and the saved JAR artifacts.
Workflow/build configuration can be rolled back by reverting the change.
