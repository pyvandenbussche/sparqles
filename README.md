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

### Prerequisites
In order to run both backend and frontend of SPARQLES application you need to install the following programs:
- Java (tested with version 1.7)
- MongoDB (tested with version 2.4.9)
- NodeJS (tested with version 0.12.4)
- npm

Get the code from GitHub: https://github.com/pyvandenbussche/sparqles

### Loading sample data
For you to test the frontend, you can load the sample data provided in the **sampleData** folder. Use **mongorestore** command to load the unzipped data into a database named **sparqles**:

    git clone https://github.com/pyvandenbussche/sparqles
    cd sampleData
    unzip mongoDump.zip
    mongod &
    mongorestore -d sparqles dump/sparqles
    rm -rf dump/    

### Running the frontend
Make sure the **sparqles** database is present in MongoDB and populated. You can now run the frontend by executing the following command:

```
cd node
npm install
npm start
```

You should see the following message:

```
Express server listening on port 3001
```
You can then access your application at the following URL: [http://localhost:3001/](http://localhost:3001/)

## Running the backend

- Git clone the project.
- Copy the cloned folder under Eclipse "workspace" and then run "create project" using that path (make sure the folder is in your workspace otherwise Eclipse complains)
- Install Maven plugin for Eclipse to handle dependencies http://www.eclipse.org/m2e/index.html 
- Once plugin installed, select Configure>Convert to Maven Project
- That's it, you should be able to run from command line using these arguments: `SPARQLES -p src/main/resources/sparqles.properties -h`


## License
SPARQLES code and dataset are licensed under a [Creative Commons Attribution 4.0 International License]( https://creativecommons.org/licenses/by/4.0/).
