# Development and documentation

The [GitHub repository](https://github.com/Egothor/conflux) contains the Java library, tests, manual and deployment workflows. Use Java 21 and the checked-in Gradle wrapper to work on the library.

## Check the library

From the repository root:

```sh
./gradlew clean check
```

The build runs JUnit tests and PMD checks. Tests under `src/test/java/conflux` cover stored values, key type consistency, change notifications, array equality and context isolation.

## Preview the manual

Create a Python virtual environment and install the documentation dependencies:

```sh
python3 -m venv .venv
.venv/bin/python -m pip install -r requirements-docs.txt
.venv/bin/mkdocs serve
```

Open the local URL printed by MkDocs. Manual sources live in `docs/`; navigation and theme settings live in `mkdocs.yml`.

## Build the complete site

Build the manual first, then add the generated JavaDoc:

```sh
.venv/bin/mkdocs build --strict
./gradlew javadoc
cp -R build/docs/javadoc site/javadoc
```

MkDocs writes the manual to `site/`. Gradle writes API HTML to `build/docs/javadoc/`; copying that directory into `site/javadoc/` produces the combined Pages artifact. The manual links to the public JavaDoc URL, so those links open the deployed API pages during a local MkDocs preview.

## Publish to GitHub Pages

The documentation workflow is separate from the release workflow. Changes to Java sources or documentation and their build configuration trigger a fresh site build; pull requests validate it, while the default branch publishes it. The workflow can also be started manually.

The deployed manual lives at [www.egothor.org/conflux](https://www.egothor.org/conflux/), with generated API documentation at [www.egothor.org/conflux/javadoc/](https://www.egothor.org/conflux/javadoc/). This project site inherits the custom domain configured for the Egothor account's user site. It needs no project-level `CNAME`; the account's root website remains at `/`. Select **GitHub Actions** as the source in the repository's **Settings → Pages** before the first deployment.

Maven Central publishing remains part of the separate release workflow. See the [release instructions in the README](https://github.com/Egothor/conflux#releases-on-github) for configuration and tag naming.

## Maintain these pages

Update behaviour guidance alongside API changes. Run the strict documentation build and regenerate JavaDoc before submitting changes. Check code examples against the current implementation, retain strong references in listener examples and review the rendered pages for readable code, tables and navigation.

Documentation owner: Conflux maintainers · Reviewed: 18 October 2026 · Next review: 18 January 2027.
