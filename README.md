# rfc3161timestampingserver

<!---
[![start with why](https://img.shields.io/badge/start%20with-why%3F-brightgreen.svg?style=flat)](http://www.ted.com/talks/simon_sinek_how_great_leaders_inspire_action)
--->
[![GitHub release](https://img.shields.io/github/release/elbosso/rfc3161timestampingserver/all.svg?maxAge=1)](https://GitHub.com/elbosso/rfc3161timestampingserver/releases/)
[![GitHub tag](https://img.shields.io/github/tag/elbosso/rfc3161timestampingserver.svg)](https://GitHub.com/elbosso/rfc3161timestampingserver/tags/)
[![GitHub license](https://img.shields.io/github/license/elbosso/rfc3161timestampingserver.svg)](https://github.com/elbosso/rfc3161timestampingserver/blob/master/LICENSE)
[![GitHub issues](https://img.shields.io/github/issues/elbosso/rfc3161timestampingserver.svg)](https://GitHub.com/elbosso/rfc3161timestampingserver/issues/)
[![GitHub issues-closed](https://img.shields.io/github/issues-closed/elbosso/rfc3161timestampingserver.svg)](https://GitHub.com/elbosso/rfc3161timestampingserver/issues?q=is%3Aissue+is%3Aclosed)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/elbosso/rfc3161timestampingserver/issues)
[![GitHub contributors](https://img.shields.io/github/contributors/elbosso/rfc3161timestampingserver.svg)](https://GitHub.com/elbosso/rfc3161timestampingserver/graphs/contributors/)
[![Github All Releases](https://img.shields.io/github/downloads/elbosso/rfc3161timestampingserver/total.svg)](https://github.com/elbosso/rfc3161timestampingserver)
[![Website elbosso.github.io](https://img.shields.io/website-up-down-green-red/https/elbosso.github.io.svg)](https://elbosso.github.io/)

## Overview

This project offers a rfc 3161 compliant timestamping authority/server - you can build it by issuing

```
mvn compile package
```

and then starting the resulting monolithic jar file by issuing

```
$JAVA_HOME/bin/java -jar target/rfc3161timestampingserver-<version>-jar-with-dependencies.jar
```
*Note, however, that to be fully functional, the server needs cryptographic
material, namely*
 * *a certificate named _tsa.crt_*
 * *a private key matching the certificate named _tsa.key_*
 * *and the certificate chain for the certificate named _chain.pem_*
 
*all inside the directory _src/main/resources/rfc3161timestampingserver/priv_.*

Alternatively one could just start the server using maven by  issuing

```
mvn compile exec:java
```

In both cases, the server starts on port 7000 - at the moment
only POST requests are supported - either with a body of mimetype
`application/timestamp-query` consisting as the name hints an timestamping request
or if the mimetype is `multipart/form-data`, the form must contain a file
named _tsq_ again being a timestamp request. In case the request brings with it
a valid timestamp request - it is then answered with a matching timestamp reply.

At the moment, this is a prototype. It still lacks support for TLS and - most
crucial - it does not give out serial numbers: every timestamp gets serial number
`23`.

However the recommended mode of using this is to use the provided Dockerfile 
and docker-compose.yml file. It is probably better 
to actually use a proxy solution like traefik (the docker-compose is 
already prepared for this) or similar
solutions so the services are actually accessible with a sound hostname and 
some default port.
