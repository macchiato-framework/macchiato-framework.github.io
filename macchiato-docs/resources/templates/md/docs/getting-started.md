{:title "Getting Started"
 :layout :page
 :page-index 1
 :section "Getting Started"}

Macchiato aims to be as simple as possible. This section will guide you through building a simple application.
You'll need the following to get started:

* [JDK](http://www.azul.com/downloads/zulu/)
* [Leiningen](http://leiningen.org/)
* [Node.js](https://nodejs.org/)

```
~ $ lein new macchiato my-app
~ $ cd my-app
~ $ lein build
```

This will start the Figwheel compiler for ClojureScript. Once the sources are compiler, open a new terminal window and
run the project with Node.js as follows:

```
~ $ cd my-app
~ $ node target/out/my-app.js
```

Once the server starts, you can visit your site at `localhost:3000`.
