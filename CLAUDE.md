# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Project Is

A Leiningen template (`net.clojars.macielti/lein-template.microservice`) that scaffolds Clojure microservices. The root project is the template plugin itself; the actual generated project files live under `resources/leiningen/new/microservice/`.

Usage: `lein new net.clojars.macielti/microservice your-new-project-name`

## Commands

### Root template project
```bash
lein test          # Run tests
lein install       # Install template locally
lein deploy clojars  # Publish to Clojars
```

### Generated project (inside resources/leiningen/new/microservice/)
```bash
lein test                  # Run all tests (unit + integration)
lein lint                  # clean-ns + format + clj-kondo diagnostics
lein lint-fix              # Fix linting issues automatically
lein native                # Build GraalVM native image
lein uberjar               # Build fat JAR
```

The `lein lint` alias runs: `clean-ns` → `format` → `diagnostics` (clj-kondo).

## Architecture of the Generated Microservice

The generated service uses **Integrant** for component lifecycle management. The system map in `src/{{name}}/components.clj` wires together:

- `:config` — loads `resources/config.edn` (`:prod` environment)
- `:datalevin` — embedded Datomic-like database, schema defined in `db/datalevin/config.clj`
- `:http-client` — httpkit-based HTTP client component
- `:telegrama/consumer` — Telegram bot polling handler
- `:routes` — Pedestal route definitions
- `:service` — Pedestal + Jetty web server

### Source layout (after generation)
```
src/{{name}}/
├── components.clj               # Integrant system entry point
├── db/datalevin/config.clj      # Database schema
└── diplomat/
    ├── http_server.clj          # Pedestal routes
    ├── http_server/hello_world.clj  # HTTP handlers
    └── telegram/consumer.clj   # Telegram command handlers
```

The "diplomat" layer is the interface boundary — HTTP and Telegram handlers live here. Business logic and DB access go in separate namespaces.

### Configuration
`resources/config.edn` holds environment-keyed config (`:prod`). It is gitignored in generated projects — only a template copy exists in the resources directory.

## Build & Deploy

The generated project uses a two-stage Docker build:
1. **Build stage** (`oracle.com/graalvm/native-image:23`): runs `lein uberjar` then `lein native` to produce a native binary
2. **Runtime stage** (`gcr.io/distroless/base`): copies only the native binary

GraalVM reflection config is in `reflect-config.json` (currently covers Jetty's `ServerConnector`).

GitHub Actions workflows (in `.github/workflows/`):
- `tests.yml` — runs `lein test` on every push
- `lint.yml` — runs `lein lint` on every push
- `build.yml` — builds and pushes Docker image to ghcr.io on push to main

## Template Generator

`src/leiningen/new/microservice.clj` defines the `microservice` template function. It uses Leiningen's `render` for variable substitution (`{{name}}`, `{{sanitized}}`). When adding new template files, register them in the `->files` call in this namespace.