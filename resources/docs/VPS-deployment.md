---
title: "VPS Deployment"
layout: :page
page-index: 4
section: "Deployment"
---

## Docker Deployment

The template comes with a `Dockerfile` for running the application using Docker

Once you've run `lein package`, you can build and run a Docker container as follows:

```
docker build -t {{name}}:latest .
docker run -p 3000:3000 {{name}}:latest
```
