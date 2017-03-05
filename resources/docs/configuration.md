---
title: "Configuration"
layout: :post
page-index: 2
section: "Getting Started"
---

# Environment Variables

The configuration is represented by a map. the map is constructed by aggregating variables from
multiple sources in the environment. The default sources include EDN configuration and shell variables.

### EDN Based Configuration

The library will first look for a `config.edn` file on the resource path. This will be used as the
base configuration for the application. An external configuration file can be specified using the
`conf` environment variable at runtime, e.g: `export CONF="prod-config.edn"`.

The configuration placed in the configuration file should consist of a map such as the following:

```clojure
{:port 4000
 :host "127.0.0.1"}
```

This configuration will be merged on top of the configuration found in the environment.
The library uses a deep merge strategy, so any nested structures will be merged intelligently.

### Managing Environment Variables

Environment variable names are converted into Clojure style keywords. These variables are parsed using the following strategy:

* `PORT=3000` -> `{:port 3000}`
* `NREPL_PORT=7000` - `{:nrepl-port 7000}`
* `DATABASE_URL="jdbc:h2:./guestbook_dev.db"` -> `{:database-url "jdbc:h2:./guestbook_dev.db"}`
* `IO__HTTP_MAX_CONNECTIONS="{:value 10}"` -> `{:io {:http-max-connections {:value 10}}}`

Note that the `_` is converted to `-`, while `__` is used to indicate nesting for shell variables. These
conventions can be mixed as seen with `IO__HTTP_MAX_CONNECTIONS`.

The library will attempt to parse variables in the following order:

* attempt to parse numeric value
* attempt to parse boolean
* variables that start with a letter are treated as strings
* any other variables are parsed as EDN

### Configuration Order

The environment configuration will be read as the default.

Next, the library will look for the `conf` environment variable. When present, it will attempt to read configuration from the file pointed to by this variable.

When the `conf` environment variable is not present, the library will look for a file called `config.edn` at the location where the app was run.

When a configuration file is found, it will be deep merged on top of the configuration read from the envrionment.


