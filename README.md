# SPARQL Endpoint Status

## Introduction

SPARQL Endpoint Status project aims at monitoring SPARQL Endpoints based on 4 aspects:

* **Discoverability** analyses how SPARQL endpoints can be located, what meta-data are available for them, etc.
* **Interoperability** identifies which features of SPARQL 1.0 and SPARQL 1.1 standards are supported by an endpoint
* **Performance** measures generic performance aspects such as result-streaming, atomic lookups and simple-joins over a HTTP connection.
* **Availability** monitors the uptimes of a SPARQL endpoint.

## Directory structure
```
-node/ (frontend code based on NodeJS technology)
-sampleData/ (sample data to populate MongoDB for setting up or testing purposes)
-scripts/ (shell scripts used to run global operations such as dumping the data)
-src/ (backend Java code used to monitor the SPARQL Endpoints)
```

## Deploying the application

Get the code from GitHub: https://github.com/pyvandenbussche/sparqles

Build using maven

## License
SPARQLES code and dataset are licensed under a [Creative Commons Attribution 4.0 International License]( https://creativecommons.org/licenses/by/4.0/).
