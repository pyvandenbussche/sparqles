// Module dependencies.
var express = require('express')
  , routes = require('./routes')
  , user = require('./routes/user')
  , http = require('http')
  , path = require('path')
  , fs = require('fs');
 
var ConfigProvider = require('./configprovider').ConfigProvider;
var MongoDBProvider = require('./mongodbprovider').MongoDBProvider;
var mongoDBProvider = new MongoDBProvider('localhost', 27017);
var configApp = new ConfigProvider('../config.json');

var app = express();

// all environments
app.set('port', process.env.PORT || 3001);
app.set('views', __dirname + '/views');
app.set('view engine', 'jade');
app.use(express.favicon());
app.use(express.logger('dev'));
app.use(express.bodyParser());
app.use(express.methodOverride());
app.use(app.router);
app.use(express.static(path.join(__dirname, 'public')));
app.use('/dumps',express.static('/usr/local/sparqles/dumps'));

// development only
if ('development' == app.get('env')) {
  app.use(express.errorHandler());
}

app.get('/', function(req, res){
		/*var eps = JSON.parse(fs.readFileSync('./examples/index.json'));*/
		mongoDBProvider.endpointsCount(function(error,nbEndpointsSearch){
		//console.log(docs);
			mongoDBProvider.getLastUpdate( function(error,lastUpdate){
			//console.log(lastUpdate);
				mongoDBProvider.getIndex( function(error,index){
				//console.log(index);
          mongoDBProvider.getAMonths( function(error,amonths){
            //console.log(JSON.stringify(amonths));
            var indexInterop = JSON.parse(JSON.stringify(index.interoperability.data), function(k, v) {
              if (k === "data") 
                this.values = v;
              else
                return v;
            });
            //PERFORMANCE
            mongoDBProvider.getPerfView( function(error,docs){
            //TODO precompute the data?
              var thresholds=[];
              var avgASKCold=0;
              var avgASKWarm=0;
              var avgJOINCold=0;
              var avgJOINWarm=0;
              var nbEndpointsTotal=0;
              for (i in docs){
                if(docs[i].threshold>0 && docs[i].threshold%100==0){
                  if(thresholds[docs[i].threshold])thresholds[docs[i].threshold]++;
                  else thresholds[docs[i].threshold]=1;
                }
                if(docs[i].askMeanCold+docs[i].joinMeanCold>0) nbEndpointsTotal++;
                avgASKCold+=docs[i].askMeanCold;
                avgASKWarm+=docs[i].askMeanWarm;
                avgJOINCold+=docs[i].joinMeanCold;
                avgJOINWarm+=docs[i].joinMeanWarm;
              }
              avgASKCold=avgASKCold/nbEndpointsTotal;
              avgASKWarm=avgASKWarm/nbEndpointsTotal;
              avgJOINCold=avgJOINCold/nbEndpointsTotal;
              avgJOINWarm=avgJOINWarm/nbEndpointsTotal;
              var mostCommonThreshold = [0,0];
              for (i in thresholds){
                if(thresholds[i]>mostCommonThreshold[1]){
                  mostCommonThreshold[0]=i;
                  mostCommonThreshold[1]=thresholds[i];
                }
              }
              res.render('content/index.jade',{
                configInstanceTitle: configApp.get('configInstanceTitle'),
                amonths: amonths,
                index:index,
                indexInterop:indexInterop,
                nbEndpointsSearch: nbEndpointsSearch,
                lastUpdate: lastUpdate[0].lastUpdate,
                perf: {"threshold":mostCommonThreshold[0],"data":[{"key": "Cold Tests","color": "#1f77b4","values": [{"label" : "Average ASK" ,"value" : avgASKCold },{"label" : "Average JOIN" ,"value" : avgJOINCold}]},{"key": "Warm Tests","color": "#2ca02c","values": [{"label" : "Average ASK" ,"value" : avgASKWarm} ,{"label" : "Average JOIN" ,"value" : avgJOINWarm}]}]},
                configInterop: JSON.parse(fs.readFileSync('./texts/interoperability.json')),
                configPerformance: JSON.parse(fs.readFileSync('./texts/performance.json')),
                configDisco: JSON.parse(fs.readFileSync('./texts/discoverability.json'))
                });
            });
          });
				});
			});
		});
});

app.get('/api/endpointsAutoComplete', function(req, res){
		mongoDBProvider.autocomplete(req.param('q'), function(error,docs){
			//for(i in docs)console.log(docs[i].uri);
			if(docs){
				res.json(docs);
			}
			else res.end();
		});
});

app.get('/api/endpoint/list', function(req, res){
                mongoDBProvider.endpointsList(function(error,docs){
                        //for(i in docs)console.log(docs[i].uri);
                        if(docs){
                                res.header("Content-Type", "application/json; charset=utf-8");
                                res.json(docs);
                        }
                        else res.end();
                });
});

app.get('/api/endpoint/autocomplete', function(req, res){
                mongoDBProvider.autocomplete(req.param('q'), function(error,docs){
                        //for(i in docs)console.log(docs[i].uri);
                        if(docs){
                                res.json(docs);
                        }
                        else res.end();
                });
});

app.get('/api/endpoint/info', function(req, res){
  var uri = req.query.uri;
               mongoDBProvider.getEndpointView(uri, function(error,docs){
                        if(docs){
                                res.json(docs);
                        }
                        else res.end();
                });
});

app.get('/api/availability', function(req, res){
               mongoDBProvider.getAvailView(function(error,docs){
                        if(docs){
                                res.json(docs);
                        }
                        else res.end();
                });
});
app.get('/api/discoverability', function(req, res){
               mongoDBProvider.getDiscoView(function(error,docs){
                        if(docs){
                                res.json(docs);
                        }
                        else res.end();
                });
});
app.get('/api/performance', function(req, res){
               mongoDBProvider.getPerfView(function(error,docs){
                        if(docs){
                                res.json(docs);
                        }
                        else res.end();
                });
});
app.get('/api/interoperability', function(req, res){
               mongoDBProvider.getInteropView(function(error,docs){
                        if(docs){
                                res.json(docs);
                        }
                        else res.end();
                });
});


app.get('/endpoint', function(req, res){
		var uri = req.query.uri;
                var ep = JSON.parse(fs.readFileSync('./examples/endpoint.json'));
                //console.log(req.param('uri'))
                mongoDBProvider.endpointsCount(function(error,nbEndpointsSearch){
                //TODO deal with no URI
                        console.log(uri);
                        mongoDBProvider.getEndpointView(uri, function(error,docs){
				mongoDBProvider.getCollection('endpoints', function(error, collection) {
					collection.find({ "uri": uri })
					.toArray(function(err, results) {
						var perfParsed = JSON.parse(JSON.stringify(docs[0].performance), function(k, v) {
							if (k === "data")
								this.values = v;
							else
								return v;
						});
						console.log(docs[0].availability);
						res.render('content/endpoint.jade',{
							ep: ep,
							nbEndpointsSearch:nbEndpointsSearch,
							lastUpdate: uri,
							configInterop: JSON.parse(fs.readFileSync('./texts/interoperability.json')),
							configPerf: JSON.parse(fs.readFileSync('./texts/performance.json')),
							configDisco: JSON.parse(fs.readFileSync('./texts/discoverability.json')),
							epUri: uri,
							epDetails: /*docs[0].endpoint*/ results[0],
							epPerf: perfParsed,
							epAvail: docs[0].availability,
							epInterop: docs[0].interoperability,
							epDisco: docs[0].discoverability
						});

					})
				})

                        });
                });
});

app.get('/endpoint-no-agg', function(req, res){
		var uri = req.query.uri;
                var ep = JSON.parse(fs.readFileSync('./examples/endpoint.json'));
                //console.log(req.param('uri'))
                mongoDBProvider.endpointsCount(function(error,nbEndpointsSearch){
                //TODO deal with no URI
			var docs = [];
			var obj = { "endpoint" : { "uri" : "http://roma.rkbexplorer.com/sparql/", "datasets" : [ 	{ 	"uri" : "http://thedatahub.org/dataset/rkb-explorer-roma", 	"label" : "Universi degli studi di Roma \"La Sapienza\" (RKBExplorer)" } ] }, "availability" : { "upNow" : true, "testRuns" : 8127, "uptimeLast24h" : 1, "uptimeLast7d" : 1, "uptimeLast31d" : 0.9615384615384616, "uptimeOverall" : 0.9764980927771626, "data" : { "key" : "Availability", "values" : [] } }, "performance" : { "threshold" : 35587, "ask" : [ 	{ 	"key" : "Cold ASK Tests", 	"color" : "#1f77b4", 	"data" : [ 	{ 	"label" : "s", 	"value" : 162.337, 	"exception" : null }, 	{ 	"label" : "sp", 	"value" : 0.131, 	"exception" : null }, 	{ 	"label" : "so", 	"value" : 3.027, 	"exception" : null }, 	{ 	"label" : "p", 	"value" : 0.278, 	"exception" : null }, 	{ 	"label" : "o", 	"value" : 0.408, 	"exception" : null }, 	{ 	"label" : "po", 	"value" : 8.713, 	"exception" : null }, 	{ 	"label" : "spo", 	"value" : 0, 	"exception" : { 	"string" : "ResultSetException msg:Failed when initializing the StAX parsing engine cause:com.hp.hpl.jena.sparql.resultset.ResultSetException: Unknown XML element: error" } } ] }, 	{ 	"key" : "Warm ASK Tests", 	"color" : "#2ca02c", 	"data" : [ 	{ 	"label" : "s", 	"value" : 5.154, 	"exception" : null }, 	{ 	"label" : "sp", 	"value" : 8.171, 	"exception" : null }, 	{ 	"label" : "so", 	"value" : 2.525, 	"exception" : null }, 	{ 	"label" : "p", 	"value" : 2.865, 	"exception" : null }, 	{ 	"label" : "o", 	"value" : 0.386, 	"exception" : null }, 	{ 	"label" : "po", 	"value" : 9.784, 	"exception" : null }, 	{ 	"label" : "spo", 	"value" : 0, 	"exception" : { 	"string" : "ResultSetException msg:Failed when initializing the StAX parsing engine cause:com.hp.hpl.jena.sparql.resultset.ResultSetException: Unknown XML element: error" } } ] } ], "join" : [ 	{ 	"key" : "Cold JOIN Tests", 	"color" : "#1f77b4", 	"data" : [ 	{ 	"label" : "ss", 	"value" : 2.691, 	"exception" : null }, 	{ 	"label" : "so", 	"value" : 0.404, 	"exception" : null }, 	{ 	"label" : "oo", 	"value" : 0.567, 	"exception" : null } ] }, 	{ "key" : "Warm JOIN Tests", 	"color" : "#2ca02c", 	"data" : [ 	{ 	"label" : "ss", 	"value" : 5.584, 	"exception" : null }, 	{ "label" : "so", 	"value" : 2.214, 	"exception" : null }, 	{ 	"label" : "oo", 	"value" : 0.444, 	"exception" : null } ] } ] }, "interoperability" : { "SPARQL1Features" : [ 	{ 	"label" : "sel[graph]", 	"value" : true, 	"exception" : null }, 	{ 	"label" : "sel[.]*orderby-desc", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Failed when initializing the StAX parsing engine cause:com.hp.hpl.jena.sparql.resultset.ResultSetException: Unknown XML element: error" } }, 	{ 	"label" : "sel-distinct[.]", 	"value" : true, 	"exception" : null }, 	{ 	"label" : "sel[.]*orderby", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Failed when initializing the StAX parsing engine cause:com.hp.hpl.jena.sparql.resultset.ResultSetException: Unknown XML element: error" } }, 	{ 	"label" : "ask[.]", 	"value" : true, 	"exception" : null }, 	{ 	"label" : "con[join]", 	"value" : true, 	"exception" : null }, 	{ 	"label" : "sel[empty]", 	"value" : true, 	"exception" : null }, 	{ 	"label" : "sel[fil(str)]", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Not a ResultSet result cause:null" } }, 	{ 	"label" : "sel[.]*orderby-asc", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Failed when initializing the StAX parsing engine cause:com.hp.hpl.jena.sparql.resultset.ResultSetException: Unknown XML element: error" } }, 	{ 	"label" : "sel[fil(bool)]", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Not a ResultSet result cause:null" } }, 	{ 	"label" : "sel[fil(!bound)]", 	"value" : true, 	"exception" : null }, 	{ 	"label" : "sel[fil(regex-i)]", 	"value" : true, 	"exception" : null }, 	{ 	"label" : "sel[.]", 	"value" : true, 	"exception" : null }, 	{ 	"label" : "sel[union]", 	"value" : true, 	"exception" : null }, 	{ 	"label" : "sel[opt]", 	"value" : true, 	"exception" : null }, 	{ 	"label" : "sel[from]", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Failed when initializing the StAX parsing engine cause:com.hp.hpl.jena.sparql.resultset.ResultSetException: Unknown XML element: error" } }, 	{ 	"label" : "con[.]", 	"value" : true, 	"exception" : null }, 	{ 	"label" : "sel[fil(num)]", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Not a ResultSet result cause:null" } }, 	{ 	"label" : "sel[.]*orderby*offset", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Failed when initializing the StAX parsing engine cause:com.hp.hpl.jena.sparql.resultset.ResultSetException: Unknown XML element: error" } }, 	{ 	"label" : "sel[bnode]", 	"value" : true, 	"exception" : null }, 	{ 	"label" : "sel[graph;join]", 	"value" : true, 	"exception" : null }, 	{ 	"label" : "sel[fil(iri)]", 	"value" : true, 	"exception" : null }, 	{ 	"label" : "sel[graph;union]", 	"value" : true, 	"exception" : null }, 	{ 	"label" : "sel[fil(blank)]", 	"value" : true, 	"exception" : null }, 	{ 	"label" : "sel[join]", 	"value" : true, 	"exception" : null }, 	{ 	"label" : "sel-reduced[.]", 	"value" : true, 	"exception" : null }, 	{ 	"label" : "sel[fil(regex)]", 	"value" : true, 	"exception" : null }, 	{ 	"label" : "con[opt]", 	"value" : true, 	"exception" : null } ], "SPARQL11Features" : [ 	{ 	"label" : "sel[max]", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Failed when initializing the StAX parsing engine cause:com.hp.hpl.jena.sparql.resultset.ResultSetException: Unknown XML element: error" } }, 	{ 	"label" : "sel[paths]", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Failed when initializing the StAX parsing engine cause:com.hp.hpl.jena.sparql.resultset.ResultSetException: Unknown XML element: error" } }, 	{ 	"label" : "sel[subq;graph]", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Failed when initializing the StAX parsing engine cause:com.hp.hpl.jena.sparql.resultset.ResultSetException: Unknown XML element: error" } }, 	{ 	"label" : "sel[fil(contains)]", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Failed when initializing the StAX parsing engine cause:com.hp.hpl.jena.sparql.resultset.ResultSetException: Unknown XML element: error" } }, 	{ 	"label" : "sel[count]*groupby", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Failed when initializing the StAX parsing engine cause:com.hp.hpl.jena.sparql.resultset.ResultSetException: Unknown XML element: error" } }, 	{ 	"label" : "sel[avg]*groupby", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Failed when initializing the StAX parsing engine cause:com.hp.hpl.jena.sparql.resultset.ResultSetException: Unknown XML element: error" } }, 	{ 	"label" : "sel[subq]", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Failed when initializing the StAX parsing engine cause:com.hp.hpl.jena.sparql.resultset.ResultSetException: Unknown XML element: error" } }, 	{ 	"label" : "ask[fil(!in)]", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Failed when initializing the StAX parsing engine cause:com.hp.hpl.jena.sparql.resultset.ResultSetException: Unknown XML element: error" } }, 	{ 	"label" : "sel[bind]", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Failed when initializing the StAX parsing engine cause:com.hp.hpl.jena.sparql.resultset.ResultSetException: Unknown XML element: error" } }, 	{ 	"label" : "sel[avg]", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Failed when initializing the StAX parsing engine cause:com.hp.hpl.jena.sparql.resultset.ResultSetException: Unknown XML element: error" } }, 	{ 	"label" : "sel[fil(!exists)]", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Failed when initializing the StAX parsing engine cause:com.hp.hpl.jena.sparql.resultset.ResultSetException: Unknown XML element: error" } }, 	{ 	"label" : "sel[sum]", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Failed when initializing the StAX parsing engine cause:com.hp.hpl.jena.sparql.resultset.ResultSetException: Unknown XML element: error" } }, 	{ 	"label" : "sel[values]", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Failed when initializing the StAX parsing engine cause:com.hp.hpl.jena.sparql.resultset.ResultSetException: Unknown XML element: error" } }, 	{ 	"label" : "sel[fil(abs)]", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Failed when initializing the StAX parsing engine cause:com.hp.hpl.jena.sparql.resultset.ResultSetException: Unknown XML element: error" } }, 	{ 	"label" : "sel[min]", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Failed when initializing the StAX parsing engine cause:com.hp.hpl.jena.sparql.resultset.ResultSetException: Unknown XML element: error" } }, 	{ 	"label" : "sel[minus]", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Failed when initializing the StAX parsing engine cause:com.hp.hpl.jena.sparql.resultset.ResultSetException: Unknown XML element: error" } }, 	{ 	"label" : "sel[fil(exists)]", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Failed when initializing the StAX parsing engine cause:com.hp.hpl.jena.sparql.resultset.ResultSetException: Unknown XML element: error" } }, 	{ 	"label" : "sel[service]", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Failed when initializing the StAX parsing engine cause:com.hp.hpl.jena.sparql.resultset.ResultSetException: Unknown XML element: error" } }, 	{ "label" : "con-[.]", 	"value" : true, 	"exception" : null }, 	{ 	"label" : "sel[fil(start)]", 	"value" : false, 	"exception" : { 	"string" : "ResultSetException msg:Failed when initializing the StAX parsing engine cause:com.hp.hpl.jena.sparql.resultset.ResultSetException: Unknown XML element: error" } } ] }, "discoverability" : { "serverName" : "Apache", "VoIDDescription" : [ 	{ 	"label" : "HTTP Get", 	"value" : false }, 	{ "label" : "/.well-known/void", 	"value" : true } ], "SDDescription" : [ ] } };

			mongoDBProvider.getCollection('atasks', function(error, collection) {
				collection.find({ "endpointResult.endpoint.uri": uri })
					.toArray(function(err, results) {

					//obj.endpoint.uri = r;
					var arr = []
					for(var i in results) {
						var res = results[i];
					}
					obj.availability.data.values = [
						{ 	"x" : 1436133600000, 	"y" : 0.9583333333333334 }
					];
					docs.push(obj);
					var perfParsed = JSON.parse(JSON.stringify(docs[0].performance), function(k, v) {
						if (k === "data")
							this.values = v;
						else
							return v;
					});
					res.render('content/endpoint.jade',{
						ep: ep,
						nbEndpointsSearch:nbEndpointsSearch,
						lastUpdate: uri,
						configInterop: JSON.parse(fs.readFileSync('./texts/interoperability.json')),
						configPerf: JSON.parse(fs.readFileSync('./texts/performance.json')),
						configDisco: JSON.parse(fs.readFileSync('./texts/discoverability.json')),
						epUri: /*uri*/ results.length,
						epDetails: docs[0].endpoint,
						epPerf: perfParsed,
						epAvail: docs[0].availability,
						epInterop: docs[0].interoperability,
						epDisco: docs[0].discoverability
					});
				})
				
			})
                });
});


app.get('/fix-encoding', function(req, res){
	mongoDBProvider.getCollection('endpoints', function(error, coll) {
		coll.find({}).toArray(function(err, endpoints) {
			mongoDBProvider.getCollection('atasks_agg', function(error, taskColl) {
				for(var i in endpoints) {
					var endpoint = endpoints[i];
					taskColl.update({ "endpoint.uri": endpoint.uri }, { $set :  { "endpoint.datasets" : endpoint.datasets } }, function(err, result) {
					})
				}
			})

			mongoDBProvider.getCollection('dtasks_agg', function(error, taskColl) {
				for(var i in endpoints) {
					var endpoint = endpoints[i];
					taskColl.update({ "endpoint.uri": endpoint.uri }, { $set :  { "endpoint.datasets" : endpoint.datasets } }, function(err, result) {
					})
				}
			})

			mongoDBProvider.getCollection('ptasks_agg', function(error, taskColl) {
				for(var i in endpoints) {
					var endpoint = endpoints[i];
					taskColl.update({ "endpoint.uri": endpoint.uri }, { $set :  { "endpoint.datasets" : endpoint.datasets } }, function(err, result) {
					})
				}
			})
		})
	})
})
app.get('/availability', function(req, res){
		mongoDBProvider.endpointsCount(function(error,nbEndpointsSearch){
			mongoDBProvider.getAvailView( function(error,docs){
				var lastUpdate=0;
				var nbEndpointsUp=0;
console.log(error);
				for (i in docs){
					if(docs[i].upNow==true) nbEndpointsUp++;
					if(docs[i].lastUpdate>lastUpdate)lastUpdate=docs[i].lastUpdate;
				}
				res.render('content/availability.jade',{
					lastUpdate: new Date(lastUpdate).toUTCString(),
					nbEndpointsSearch:nbEndpointsSearch,
					atasks_agg: docs,
					nbEndpointsUp:nbEndpointsUp,
					nbEndpointsTotal:docs.length
					});
			});
		});
});

app.get('/discoverability', function(req, res){
		mongoDBProvider.endpointsCount(function(error,nbEndpointsSearch){
			mongoDBProvider.getDiscoView( function(error,docs){
				var lastUpdate=0;
				var nbEndpointsVoID=0;
				var nbEndpointsSD=0;
				var nbEndpointsServerName=0;
				var nbEndpointsTotal=0;
				for (i in docs){
					nbEndpointsTotal++;
					if(docs[i].lastUpdate>lastUpdate) lastUpdate=docs[i].lastUpdate;
					if(docs[i].VoID==true)nbEndpointsVoID++;
					if(docs[i].SD==true)nbEndpointsSD++;
					if(docs[i].serverName.length>0&&docs[i].serverName!="missing") nbEndpointsServerName++;
				}
				res.render('content/discoverability.jade',{
					lastUpdate: new Date(lastUpdate).toUTCString(),
					nbEndpointsSearch:nbEndpointsSearch,
					nbEndpointsVoID: nbEndpointsVoID,
					nbEndpointsSD: nbEndpointsSD,
					nbEndpointsServerName: nbEndpointsServerName,
					nbEndpointsTotal: nbEndpointsTotal,
					dtasks_agg: docs,
					configDisco: JSON.parse(fs.readFileSync('./texts/discoverability.json'))
					});
			});
		});
});

app.get('/performance', function(req, res){
		mongoDBProvider.endpointsCount(function(error,nbEndpointsSearch){
			mongoDBProvider.getPerfView( function(error,docs){
				var lastUpdate=0;
				var nbEndpointsWithThreshold=0;
				var nbEndpointsTotal=0;
				var thresholds=[];
				for (i in docs){
					if(docs[i].lastUpdate>lastUpdate) lastUpdate=docs[i].lastUpdate;
					if(docs[i].threshold>0 && docs[i].threshold%100==0){
						nbEndpointsWithThreshold++;
						if(thresholds[docs[i].threshold])thresholds[docs[i].threshold]++;
						else thresholds[docs[i].threshold]=1;
					}
					if(docs[i].askMeanCold+docs[i].joinMeanCold>0) nbEndpointsTotal++;
				}
				var mostCommonThreshold = [0,0];
				for (i in thresholds){
					if(thresholds[i]>mostCommonThreshold[1]){
						mostCommonThreshold[0]=i;
						mostCommonThreshold[1]=thresholds[i];
					}
				}
				//console.log(mostCommonThreshold);
				res.render('content/performance.jade',{
					lastUpdate: new Date(lastUpdate).toUTCString(),
					nbEndpointsSearch:nbEndpointsSearch,
					configPerformance: JSON.parse(fs.readFileSync('./texts/performance.json')),
					ptasks_agg: docs,
					nbEndpointsWithThreshold: nbEndpointsWithThreshold,
					nbEndpointsTotal: nbEndpointsTotal,
					mostCommonThreshold: mostCommonThreshold[0]
					});
			});
		});
});

app.get('/interoperability', function(req, res){
		mongoDBProvider.endpointsCount(function(error,nbEndpointsSearch){
			var nbSPARQL1Features=28;
			var nbSPARQL11Features=20;
			mongoDBProvider.getInteropView( function(error,docs){
				var lastUpdate=0;
				var nbCompliantSPARQL1Features=0;
				var nbFullCompliantSPARQL1Features=0;
				var nbCompliantSPARQL11Features=0;
				var nbEndpointsTotal=0;
				var nbFullCompliantSPARQL11Features=0;
				for (i in docs){
					if(docs[i].nbCompliantSPARQL1Features+docs[i].nbCompliantSPARQL11Features>0)nbEndpointsTotal++;
					if(docs[i].nbCompliantSPARQL1Features>0){
						nbCompliantSPARQL1Features++;
						if(docs[i].nbCompliantSPARQL1Features==nbSPARQL1Features)nbFullCompliantSPARQL1Features++;
					}
					if(docs[i].nbCompliantSPARQL11Features>0){
						nbCompliantSPARQL11Features++;
						if(docs[i].nbCompliantSPARQL11Features==nbSPARQL11Features)nbFullCompliantSPARQL11Features++;
					}
					if(docs[i].lastUpdate>lastUpdate)lastUpdate=docs[i].lastUpdate;
				}
				//console.log(nbCompliantSPARQL1Features+' - '+nbFullCompliantSPARQL1Features+' - '+nbCompliantSPARQL11Features+' - '+nbFullCompliantSPARQL11Features);
				res.render('content/interoperability.jade',{
				lastUpdate: new Date(lastUpdate).toUTCString(),
				nbEndpointsSearch: nbEndpointsSearch,
				configInterop: JSON.parse(fs.readFileSync('./texts/interoperability.json')),
				nbSPARQL1Features: nbSPARQL1Features,
				nbSPARQL11Features: nbSPARQL11Features,
				nbCompliantSPARQL1Features: nbCompliantSPARQL1Features,
				nbFullCompliantSPARQL1Features: nbFullCompliantSPARQL1Features,
				nbCompliantSPARQL11Features: nbCompliantSPARQL11Features,
				nbFullCompliantSPARQL11Features: nbFullCompliantSPARQL11Features,
				ftasks_agg: docs,
				nbEndpointsTotal: nbEndpointsTotal
				});
			});
		});
});

app.get('/data', function(req, res){
  mongoDBProvider.endpointsCount(function(error,nbEndpointsSearch){
    var dir = '../dumps/'; // data dir
    function bytesToSize(bytes) {
       if(bytes == 0) return '0 Byte';
       var k = 1000;
       var sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
       var i = Math.floor(Math.log(bytes) / Math.log(k));
       return (bytes / Math.pow(k, i)).toPrecision(3) + ' ' + sizes[i];
    }
    var files = fs.readdirSync(dir)
                .map(function(v) { 
                    return { name:v,
                             time:fs.statSync(dir + v).mtime.getTime(),
                             size:bytesToSize(fs.statSync(dir + v).size)
                           }; 
                 });
    res.render('content/data.jade',{files:files, nbEndpointsSearch:nbEndpointsSearch});
  });
});

app.get('/iswc2013', function(req, res){
		mongoDBProvider.endpointsCount(function(error,nbEndpointsSearch){
			mongoDBProvider.autocomplete(req.param('q'), function(error,docs){
				res.render('content/iswc2013.jade',{nbEndpointsSearch:nbEndpointsSearch});
			});
		});
});

app.get('/api', function(req,res){
                mongoDBProvider.endpointsCount(function(error,nbEndpointsSearch){
                        mongoDBProvider.autocomplete(req.param('q'), function(error,docs){
                                res.render('content/api.jade',{nbEndpointsSearch:nbEndpointsSearch});
                        });
                });

});

http.createServer(app).listen(app.get('port'), function(){
  console.log('Express server listening on port ' + app.get('port'));
});
