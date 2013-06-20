# SPARQL Endpoint Status

## Introduction

SPARQL Endpoint Status project aims at monitoring SPARQL Endpoints based on 4 aspects:

* **Discoverability** analyses how SPARQL endpoints can be located, what meta-data are available for them, etc.
* **Interoperability** identifies which features of SPARQL 1.0 and SPARQL 1.1 standards are supported by an endpoint
* **Performance** measures generic performance aspects such as result-streaming, atomic lookups and simple-joins over a HTTP connection.
* **Availability** monitors the uptimes of a SPARQL endpoint.


## Details

The SPARQL Endpoint Status project is divided in several components:
 
* **Core component**: The core component contains all the code used to test the different status of a SPARQL endpoint. This component also contains a scheduler to take care of running tests periodically or on demand.
* **UI Component**: The UI Component contains the code developped in GWT technology to generates a Web interface for either the SPARQL Endpoint Status website and the standalone tool
* **Wrapper component**: This component generates a complete environment for the standalone version of the tool.

## Building

Get the code from GitHub: https://github.com/pyvandenbussche/ends

Build using ant:
	
	ant clean all

