// This is a test harness for your module
// You should do something interesting in this harness 
// to test out the module and to provide instructions 
// to users on how to use it by example.


// open a single window
var win = Ti.UI.createWindow({
	backgroundColor:'white'
});
var label = Ti.UI.createLabel();
win.add(label);
win.open();

// TODO: write your module tests here
var iobridge = require('appersonlabs.iobridge');
Ti.API.info("module is => " + iobridge);

var device = iobridge.createDevice({
  apikey: "A4VPPSNH49U6KGBP",
  serial: "0002C312381E9E86"
});
label.text = device.apikey;

device.fetchConnectionState(function(err, data) {
  if (!err) {
    label.text = "connection state: "+JSON.stringify(data);
  }
  else {
    label.text = "connection state error: "+JSON.stringify(err);
  }
});