// Module dependencies.
var express = require('express')
  , http = require('http')
  , path = require('path')
  , fs = require('fs');
 
var app = express();

// all environments
app.set('port', 80);
app.set('views', __dirname + '/views');
app.set('view engine', 'jade');
app.use(express.favicon());
app.use(express.logger('dev'));
app.use(express.bodyParser());
app.use(express.methodOverride());
app.use(app.router);

// development only
if ('development' == app.get('env')) {
  app.use(express.errorHandler());
}

app.get('/', function(req, res){
        res.render('content/index.jade');
});
app.get('/public/*', function(req, res){
        res.sendfile(__dirname + '/' + req.url); 
});
app.get('*', function(req, res){
  res.render('content/index.jade');
});

http.createServer(app).listen(app.get('port'), function(){
  console.log('Express server listening on port ' + app.get('port'));
});