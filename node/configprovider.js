var nconf = require('nconf');

var ConfigProvider = function(){
	nconf.use('file', { file: '../config.json' });
	nconf.load();
};
	  
ConfigProvider.prototype.save = function (){
	nconf.save(function (err) {
    if (err) {
      console.error(err.message);
      return;
    }
    console.log('Configuration saved successfully.');
  });
};

ConfigProvider.prototype.get = function(key) {
  return nconf.get(key);
};


exports.ConfigProvider = ConfigProvider;
	   
	   
  //
  // Save the configuration object to disk
  //
  /*
  nconf.save(function (err) {
    if (err) {
      console.error(err.message);
      return;
    }
    console.log('Configuration saved successfully.');
  });
  */