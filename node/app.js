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
app.set('port', process.env.PORT || 3000);
app.set('views', __dirname + '/views');
app.set('view engine', 'jade');
app.use(express.favicon());
app.use(express.logger('dev'));
app.use(express.bodyParser());
app.use(express.methodOverride());
app.use(app.router);
app.use(express.static(path.join(__dirname, 'public')));

// development only
if ('development' == app.get('env')) {
  app.use(express.errorHandler());
}

app.get('/', function(req, res){
		var eps = JSON.parse(fs.readFileSync('./examples/index.json'));
        res.render('content/index.jade',{
            configInstanceTitle: configApp.get('configInstanceTitle'),
            eps: eps,
			configInterop: JSON.parse(fs.readFileSync('./texts/interoperability.json')),
			configPerformance: JSON.parse(fs.readFileSync('./texts/performance.json')),
			configDisco: JSON.parse(fs.readFileSync('./texts/discoverability.json'))
            });
});

app.get('/endpoint', function(req, res){
		var ep = JSON.parse(fs.readFileSync('./examples/endpoint.json'));
	//TODO deal with no URI
        res.render('content/endpoint.jade',{
            ep: ep,
            lastUpdate: 'Monday 02 September 2013, 22:22',
			configInterop: JSON.parse(fs.readFileSync('./texts/interoperability.json')),
			configPerformance: JSON.parse(fs.readFileSync('./texts/performance.json')),
			configDisco: JSON.parse(fs.readFileSync('./texts/discoverability.json'))
            });
});

app.get('/availability', function(req, res){
		var epsAvail = JSON.parse(fs.readFileSync('./examples/availability.json'));
		mongoDBProvider.getAvailView( function(error,docs){
		    var nbEndpointsUp=0;
			for (i in docs){
				if(docs[i].upNow==true){
					nbEndpointsUp++;
				}
			}
			res.render('content/availability.jade',{
				lastUpdate: 'Monday 02 September 2013, 22:22',
				epsAvail: epsAvail,
				atasks_agg: docs,
				nbEndpointsUp:nbEndpointsUp,
				nbEndpointsTotal:docs.length
				});
		});
});

app.get('/discoverability', function(req, res){
		var epsDisco = JSON.parse(fs.readFileSync('./examples/discoverability.json'));
        res.render('content/discoverability.jade',{
			lastUpdate: 'Monday 02 September 2013, 22:22',
			epsDisco: epsDisco,
			configDisco: JSON.parse(fs.readFileSync('./texts/discoverability.json'))
            });
});

app.get('/performance', function(req, res){
		var epsPerf = JSON.parse(fs.readFileSync('./examples/performance.json'));
        res.render('content/performance.jade',{
			lastUpdate: 'Monday 02 September 2013, 22:22',
			epsPerf: epsPerf,
			configPerformance: JSON.parse(fs.readFileSync('./texts/performance.json'))
            });
});

app.get('/interoperability', function(req, res){
		var epsInter = JSON.parse(fs.readFileSync('./examples/interoperability.json'));
        res.render('content/interoperability.jade',{
			lastUpdate: 'Monday 02 September 2013, 22:22',
			epsInter: epsInter,
			configInterop: JSON.parse(fs.readFileSync('./texts/interoperability.json'))
            });
});

http.createServer(app).listen(app.get('port'), function(){
  console.log('Express server listening on port ' + app.get('port'));
});
 
 
 
/*
var express = require('express');
var ArticleProvider = require('./articleprovider-mongodb').ArticleProvider;

var app = module.exports = express();

app.configure(function(){
  app.set('views', __dirname + '/views');
  app.set('view engine', 'jade');
  app.use(express.bodyParser());
  app.use(express.methodOverride());
  app.use(require('stylus').middleware({ src: __dirname + '/public' }));
  app.use(app.router);
  app.use(express.static(__dirname + '/public'));
});

app.configure('development', function(){
  app.use(express.errorHandler({ dumpExceptions: true, showStack: true }));
});

app.configure('production', function(){
  app.use(express.errorHandler());
});

var articleProvider = new ArticleProvider('localhost', 27017);

app.get('/', function(req, res){
    articleProvider.findAll( function(error,docs){
        res.render('index.jade', {
            title: 'Blog',
            articles:docs
            }
        );
    })
});

app.get('/blog/new', function(req, res) {
    res.render('blog_new.jade', {
        title: 'New Post'
    });
});

app.post('/blog/new', function(req, res){
    articleProvider.save({
        title: req.param('title'),
        body: req.param('body')
    }, function( error, docs) {
        res.redirect('/')
    });
});

app.post('/blog/addComment', function(req, res) {
    articleProvider.addCommentToArticle(req.param('_id'), {
        person: req.param('person'),
        comment: req.param('comment'),
        created_at: new Date()
       } , function( error, docs) {
           res.redirect('/blog/' + req.param('_id'))
       });
});

app.get('/blog/:id', function(req, res) {
    articleProvider.findById(req.params.id, function(error, article) {
        res.render('blog_show-final.jade',
        { locals: {
            title: article.title,
            article:article
        }
        });
    });
});

app.listen(3000);
//console.log("Express server listening on port %d in %s mode", app.address().port, app.settings.env);
*/