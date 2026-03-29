# Leiningen Microsevice Template for `net.clojars.macielti`

## Usage

Create a new project with all components included (default):

```bash
lein new net.clojars.macielti/microservice your-new-project-name
```

### Selecting components

Use `+component` flags to opt in to specific components only, or `-component` flags to opt out from the default set.

Available components: `datalevin`, `http-server`, `telegram`, `http-client`

> **Note:** `telegram` always requires `http-client` and will enable it automatically.

```bash
# Only telegram (+ http-client, required by telegram)
lein new net.clojars.macielti/microservice your-new-project-name -- +telegram

# Only HTTP server and datalevin
lein new net.clojars.macielti/microservice your-new-project-name -- +http-server +datalevin

# Everything except telegram
lein new net.clojars.macielti/microservice your-new-project-name -- -telegram

# Everything except telegram and datalevin
lein new net.clojars.macielti/microservice your-new-project-name -- -telegram -datalevin
```

## License

Copyright © 2022 Bruno do Nascimento Maciel

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
