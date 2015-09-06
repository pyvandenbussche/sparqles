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
  var ASKwarm = [];
  var ASKcold = [];
  var JOINwarm = [];
  var JOINcold = [];
  for(var endpoint in results) {
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
  }

  // insert median into collection "performance_widget"
  var performance_widget = db.collection('performance_widget');
  var obj = {
    median_ASK_warm: median(ASKwarm),
    median_ASK_cold: median(ASKcold),
    median_JOIN_warm: median(JOINwarm),
    median_JOIN_cold:  median(JOINcold),
    date_calculated: new Date()
  };

  performance_widget.insert(obj, function(err, result) {
    if(!err) console.log('successfully added data to performance_widget');
    db.close();
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
