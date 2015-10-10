var MongoClient = require('mongodb').MongoClient
  , assert = require('assert');

 
// Connection URL 
var url = 'mongodb://localhost:27017/sparqles';
// Use connect method to connect to the Server 
MongoClient.connect(url, function(err, db) {
  assert.equal(null, err);
  console.log("Connected correctly to server");

  var collection = db.collection('endpoints');

  // get list of endpoints
  collection.find({}).toArray(function(err, endpoints) {
    //endpoints = endpoints.slice(0, 2); // first 5 endpoints just to test
    var results = {};
    function run(idx) {
      if(idx >= endpoints.length) {
        parseResults(db, results);
        return;
      }

		  var endpoint = endpoints[idx];

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
  console.log(JSON.stringify(fields, null, 2))
  db.close();
}

