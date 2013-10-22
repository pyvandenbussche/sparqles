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
        collection.find().sort({"endpoint.datasets.0.label":1,"endpoint.uri":1}).toArray(function(error, results) {
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
        collection.find().sort({"endpoint.datasets.0.label":1,"endpoint.uri":1}).toArray(function(error, results) {
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
        collection.find().sort({"endpoint.datasets.0.label":1,"endpoint.uri":1}).toArray(function(error, results) {
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
        collection.find().sort({"endpoint.datasets.0.label":1,"endpoint.uri":1}).toArray(function(error, results) {
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
        collection.find({"endpoint.uri":epUri}).toArray(function(error, results) {
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
		collection.find({$or: [{ 'datasets.label': {$regex: '.*'+query+'.*', $options: 'i'}}, {'uri': {$regex: '.*'+query+'.*', $options: 'i'}}]}).sort({"datasets.0.label":1,"uri":1}).toArray(function(error, results) {
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

exports.MongoDBProvider = MongoDBProvider;
