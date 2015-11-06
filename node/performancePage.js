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
    //endpoints = endpoints.slice(0, 5); // first 5 endpoints just to test
    var results = {};
    function run(idx) {
      if(idx >= endpoints.length) {
        parseResults(db, results);
        return;
      }

		  var endpoint = endpoints[idx];

      // for each endpoint, get the last 10 performance results
      console.log('running ptasks query with uri: '+ endpoint.uri);

      db.collection('ptasks').find({ "endpointResult.endpoint.uri": endpoint.uri })
                .sort({ "endpointResult.end" : -1})
                .limit(10)
                .toArray(function(err, perResults) {
                  results[endpoint.uri] = perResults;
                  run(idx + 1)
                })
    }
    run(0); // recursion, yuk
  });
});

function parseResults(db, results) {
  var data = [];
  for(var endpoint in results) {
    var ASKwarm = [];
    var ASKcold = [];
    var JOINwarm = [];
    var JOINcold = [];
    var arr = results[endpoint];
    for(var i=0; i<arr.length; i++) {
      var obj = arr[i];
      ASKcold.push(obj.results.ASKS.cold.exectime);
      ASKcold.push(obj.results.ASKP.cold.exectime);
      ASKcold.push(obj.results.ASKO.cold.exectime);
      ASKcold.push(obj.results.ASKSP.cold.exectime);
      ASKcold.push(obj.results.ASKSO.cold.exectime);
      ASKcold.push(obj.results.ASKPO.cold.exectime);

      ASKwarm.push(obj.results.ASKS.warm.exectime);
      ASKwarm.push(obj.results.ASKP.warm.exectime);
      ASKwarm.push(obj.results.ASKO.warm.exectime);
      ASKwarm.push(obj.results.ASKSP.warm.exectime);
      ASKwarm.push(obj.results.ASKSO.warm.exectime);
      ASKwarm.push(obj.results.ASKPO.warm.exectime);

      JOINcold.push(obj.results.JOINSS.cold.exectime);
      JOINcold.push(obj.results.JOINSO.cold.exectime);
      JOINcold.push(obj.results.JOINOO.cold.exectime);

      JOINwarm.push(obj.results.JOINSS.warm.exectime);
      JOINwarm.push(obj.results.JOINSO.warm.exectime);
      JOINwarm.push(obj.results.JOINOO.warm.exectime);
    }

    // insert median into view collection
    //var performance_widget = db.collection('performance_widget');

    data.push({
      endpoint: endpoint,
      median_ASK_warm: median(ASKwarm),
      median_ASK_cold: median(ASKcold),
      median_JOIN_warm: median(JOINwarm),
      median_JOIN_cold:  median(JOINcold),
      date_calculated: new Date()
    });
  }

  // update ptasks_agg with this median info
  function updateEndpoint(idx) {
    var obj = data[idx];
    if(!obj) {
      db.close();
      return;
    }
    console.log('updating ptasks_agg '+ obj.endpoint)
    db.collection('ptasks_agg')
      .findAndModify(
          { 'endpoint.uri': obj.endpoint },
          [],
          { $set: {
                    "askMeanCold" : obj.median_ASK_cold / 1000,
                    "askMeanWarm" : obj.median_ASK_warm/ 1000,
                    "joinMeanCold" : obj.median_JOIN_cold/1000,
                    "joinMeanWarm" : obj.median_JOIN_warm/1000
                  }
          },
          {},
          function(err, res) {
            // next!
            console.log(err, res)
            updateEndpoint(idx + 1);
          });
  }

  // start recursion
  updateEndpoint(0);

}

function average(elmt) {
  var sum = 0;
  for( var i = 0; i < elmt.length; i++ ){
      sum += parseInt( elmt[i], 10 ); //don't forget to add the base
  }

  var avg = sum/elmt.length;
  return avg;
}
function median(values) {
  var arr = []
  for(var i in values) {
    if(values[i] == 0) continue;
    arr.push(values[i])
  }

  values = arr;

  values.sort( function(a,b) {return a - b;} );

  var half = Math.floor(values.length/2);

  if(values.length % 2)
      return values[half];
  else
      return (values[half-1] + values[half]) / 2.0;
}
