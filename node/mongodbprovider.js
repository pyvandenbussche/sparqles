var Db = require('mongodb').Db;
var Connection = require('mongodb').Connection;
var Server = require('mongodb').Server;
var BSON = require('mongodb').BSON;
var ObjectID = require('mongodb').ObjectID;

MongoDBProvider = function(host, port) {
  this.db= new Db('sparqles', new Server(host, port, {auto_reconnect: true}, {}),{safe:true});
  this.db.open(function(){});
};

//getCollection

MongoDBProvider.prototype.getCollection= function(collectionName, callback) {
  this.db.collection(collectionName, function(error, collection) {
    if( error ) callback(error);
    else callback(null, collection);
  });
};

//Availability
MongoDBProvider.prototype.getAvailView = function(callback) {
    this.getCollection('atasks_agg',function(error, collection) {
      if( error ) callback(error)
      else {
        collection.find({},{"_id":0}).sort({"endpoint.datasets.0.label":1,"endpoint.uri":1}).toArray(function(error, results) {
	//collection.find().toArray(function(error, results) {

          if( error ) callback(error)
          else callback(null, results)
        });
      }
    });
};

//Interoperability
MongoDBProvider.prototype.getInteropView = function(callback) {
    this.getCollection('ftasks_agg',function(error, collection) {
      if( error ) callback(error)
      else {
        collection.find({},{"_id":0}).sort({"endpoint.datasets.0.label":1,"endpoint.uri":1}).toArray(function(error, results) {
          if( error ) callback(error)
          else callback(null, results)
        });
      }
    });
};

//Performance
MongoDBProvider.prototype.getPerfView = function(callback) {
    this.getCollection('ptasks_agg',function(error, collection) {
      if( error ) callback(error)
      else {
        collection.find({},{"_id":0}).sort({"endpoint.datasets.0.label":1,"endpoint.uri":1}).toArray(function(error, results) {
          if( error ) callback(error)
          else callback(null, results)
        });
      }
    });
};

//Discoverability
MongoDBProvider.prototype.getDiscoView = function(callback) {
    this.getCollection('dtasks_agg',function(error, collection) {
      if( error ) callback(error)
      else {
        collection.find({},{"_id":0}).sort({"endpoint.datasets.0.label":1,"endpoint.uri":1}).toArray(function(error, results) {
          if( error ) callback(error)
          else callback(null, results)
        });
      }
    });
};

//Endpoint view
MongoDBProvider.prototype.getEndpointView = function(epUri, callback) {
    this.getCollection('epview',function(error, collection) {
      if( error ) callback(error)
      else {
        collection.find({"endpoint.uri":epUri},{"_id":0}).toArray(function(error, results) {
          if( error ) callback(error)
          else callback(null, results)
        });
      }
    });
};

//autocomplete
MongoDBProvider.prototype.autocomplete = function(query, callback) {
    this.getCollection('endpoints',function(error, collection) {
      if( error ) callback(error)
      else {
		collection.find({$or: [{ 'datasets.label': {$regex: '.*'+query+'.*', $options: 'i'}}, {'uri': {$regex: '.*'+query+'.*', $options: 'i'}}]}, {"_id":0}).sort({"datasets.0.label":1,"uri":1}).toArray(function(error, results) {
          if( error ) callback(error)
          else callback(null, results)
        });
      }
    });
};

//endpoints count
MongoDBProvider.prototype.endpointsCount = function(callback) {
    this.getCollection('endpoints',function(error, collection) {
      if( error ) callback(error)
      else {
		collection.count(function(err, count) {
            if( error ) callback(error)
          else callback(null, count)
          });
      }
    });
};

//endpoints list
MongoDBProvider.prototype.endpointsList = function(callback) {
    this.getCollection('endpoints',function(error, collection) {
      if( error ) callback(error)
      else {
         collection.find({},{"_id":0}).sort({"uri":1}).toArray(function(error, results) {
          if( error ) callback(error)
          else callback(null, results)
        });
      }
    });
};


//Last Update date
MongoDBProvider.prototype.getLastUpdate = function(callback) {
    this.getCollection('atasks_agg',function(error, collection) {
      if( error ) callback(error)
      else {
        collection.find({},{"lastUpdate":1}).sort({"lastUpdate":-1}).limit(1).toArray(function(error, results) {
          if( error ) callback(error)
          else callback(null, results)
        });
      }
    });
};

//Index
MongoDBProvider.prototype.getIndex = function(callback) {
    this.getCollection('index',function(error, collection) {
      if( error ) callback(error)
      else {
        collection.findOne(function(error, results) {
          if( error ) callback(error)
          else callback(null, results)
        });
      }
    });
};

//Amonths
MongoDBProvider.prototype.getAMonths = function(callback) {
    this.getCollection('amonths',function(error, collection) {
      if( error ) callback(error)
      else {
        collection.find({}).sort({"date":1}).toArray(function(error, months) {
          if( error ) callback(error)
          else{
          
            // transform the months view into d3js expected format
            var valZeroFive=[];
            var valfiveSeventyfive=[];
            var valseventyfiveNintyfive=[];
            var valnintyfiveNintynine=[];
            var valnintynineHundred=[];
            for(var i in months){
              var d = Date.parse(months[i].date);
              valZeroFive.push([d,months[i].zeroFive]);
              valfiveSeventyfive.push([d,months[i].fiveSeventyfive]);
              valseventyfiveNintyfive.push([d,months[i].seventyfiveNintyfive]);
              valnintyfiveNintynine.push([d,months[i].nintyfiveNintynine]);
              valnintynineHundred.push([d,months[i].nintynineHundred]);
            }
            var res = [
              {"key":"0-5","index":1,"values":valZeroFive},
              {"key":"5-75","index":2,"values":valfiveSeventyfive},
              {"key":"75-95","index":3,"values":valseventyfiveNintyfive},
              {"key":"95-99","index":4,"values":valnintyfiveNintynine},
              {"key":"99-100","index":5,"values":valnintynineHundred}
            ];
            callback(null, res)
          }
        });
      }
    });
};

MongoDBProvider.prototype.getLastTenPerformanceMedian = function(uri, callback) {
    this.getCollection('ptasks',function(error, collection) {
      if( error ) callback(error)
      else {
        collection.find({ "endpointResult.endpoint.uri": uri })
          .sort({ "endpointResult.end" : -1})
          .limit(10)
          .toArray(function(error, results) {
            if( error ) return callback(error);

            var ret = {};
            for(var i=0; i<results.length; i++) {
              var obj = results[i];
              for(var x in obj.results) {
                var coldKey = x + '_cold';
                var warmKey = x + '_warm';
                if(!ret[coldKey]) ret[coldKey] = [];
                if(!ret[warmKey]) ret[warmKey] = [];
                ret[warmKey].push(obj.results[x].warm.exectime);
                ret[coldKey].push(obj.results[x].cold.exectime);
              }
            }

            // calculate median
            for(var y in ret) {
              ret[y] = median(ret[y]) / 1000; // 1000 is for millisceonds to seconds
            }

            callback(null, ret);

          })
      }
    });
};

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

exports.MongoDBProvider = MongoDBProvider;
