var MongoClient = require('mongodb').MongoClient
  , assert = require('assert');

 
// Connection URL 
var url = 'mongodb://localhost:27017/sparqles';
// Use connect method to connect to the Server 
MongoClient.connect(url, function(err, db) {
  assert.equal(null, err);
  console.log("Connected correctly to server");

  var collection = db.collection('atasks_agg');

  // get list of available endpoints
  collection.find({upNow: true}).toArray(function(err, endpoints) {
    //endpoints = endpoints.slice(0, 2); // first 5 endpoints just to test
    var results = {};
    function run(idx) {
      if(idx >= endpoints.length) {
        parseResults(db, results);
        return;
      }

		  var endpoint = endpoints[idx].endpoint;

      // for each endpoint, get the last interop results
      console.log('running ftasks query with uri: '+ endpoint.uri);

      db.collection('ftasks').find({ "endpointResult.endpoint.uri": endpoint.uri })
                .sort({ "endpointResult.end" : -1})
                .limit(1)
                .toArray(function(err, perResults) {
                  results[endpoint.uri] = perResults;
                  run(idx + 1)
                })
    }
    run(0); // recursion, yuk
  });
});

function parseResults(db, objResults) {
  var fields = {};

  for(var i in objResults) {
    var endpointUrl = i;
    var arr = objResults[i];
    if (!arr) continue;
    for(var x=0; x<arr.length; x++) {
      var obj = arr[x];
      var results = obj.results;
      for(var y in results) {
        if(!fields[y]) {
          // init field
          fields[y] = {
            hadException: 0,
            hadNoException: 0,
            total: 0
          }
        }
        if(results[y].run['Exception'] == null)
          fields[y].hadNoException += 1;
        else
          fields[y].hadException += 1;

        fields[y].total += 1;
      }
    }
  }

  
  var sparql1 = [];
  var sparql11 = [];
  for(var i in fields) {
    if(i.indexOf('SPARQL1_') === 0) { // SPARQL 1
      var p = (fields[i].hadNoException / fields[i].total) * 100;
      sparql1.push(p);

    } else if(i.indexOf('SPARQL11_') === 0) { // sparql 1.1
      var p = (fields[i].hadNoException / fields[i].total) * 100;
      sparql11.push(p);
    }
  }

  var interoperability = {
    data : [
    {
      "key" : "SPARQL 1.0",
      "color" : "#1f77b4",
      "data" : [
        {
          "label" : "SPARQL 1.0",
          "value" : average(sparql1) / 100
        }

        /*
        {
          "label" : "Solution Modifiers",
          "value" : 0.7947598253275109
        },
        {
          "label" : "Common Operators and Filters",
          "value" : 0.7205240174672489
        },
        {
          "label" : "Graph and other",
          "value" : 0.8951965065502183
        }
        */
      ]
    },
    {
      "key" : "SPARQL 1.1",
      "color" : "#2ca02c",
      "data" : [
        {
          "label" : "SPARQL 1.1",
          "value" : average(sparql11) / 100
        }
        /*
        {
          "label" : "Aggregate",
          "value" : 0.22270742358078602
        },
        {
          "label" : "Filter",
          "value" : 0.22270742358078602
        },
        {
          "label" : "Other",
          "value" : 0.11353711790393013
        }
        */
      ]
    }
    ]
  };


  var coll = db.collection('index');
  coll.findOne(function(err, doc) {
    coll.update({ _id: doc._id}, {$set: { interoperability: interoperability}}, function() {
      db.close()
    });
  });

}

function average(elmt) {
  var sum = 0;
  for( var i = 0; i < elmt.length; i++ ){
      sum += parseInt( elmt[i], 10 ); //don't forget to add the base
  }

  var avg = sum/elmt.length;
  return avg;
}
