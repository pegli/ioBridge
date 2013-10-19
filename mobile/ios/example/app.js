var iobridge = require('appersonlabs.iobridge');
Ti.API.info("module is => " + iobridge);

var device = iobridge.createDevice({
  apikey: "your-api-key-here",
  serial: "your-board-serial-here"
});

// open a single window
var win = Ti.UI.createWindow({
	backgroundColor:'white',
	layout: "vertical"
});

// channel
var channelView = Ti.UI.createView({
  layout: "horizontal",
  height: Ti.UI.SIZE
});
channelView.add(Ti.UI.createLabel({ text: "channel:" }));
var channelText = Ti.UI.createTextField({
  value: "2",
  width: Ti.UI.FILL,
  borderStyle: Ti.UI.INPUT_BORDERSTYLE_ROUNDED,
  autocapitalization: false,
  clearButtonMode: Ti.UI.INPUT_BUTTONMODE_ALWAYS,
});
channelView.add(channelText);
win.add(channelView);

// register name
var registerView = Ti.UI.createView({
  layout: "horizontal",
  height: Ti.UI.SIZE
});
registerView.add(Ti.UI.createLabel({ text: "register:" }));
var registerText = Ti.UI.createTextField({
  value: "gpio.value.6",
  width: Ti.UI.FILL,
  height: '38dp',
  borderStyle: Ti.UI.INPUT_BORDERSTYLE_ROUNDED,
  autocapitalization: false,
  clearButtonMode: Ti.UI.INPUT_BUTTONMODE_ALWAYS,
});
registerView.add(registerText);
win.add(registerView);

// register value
var valueView = Ti.UI.createView({
  layout: "horizontal",
  height: Ti.UI.SIZE
});
valueView.add(Ti.UI.createLabel({ text: "value:" }));
var valueText = Ti.UI.createTextField({
  width: Ti.UI.FILL,
  height: '38dp',
  borderStyle: Ti.UI.INPUT_BORDERSTYLE_ROUNDED,
  autocapitalization: false,
  clearButtonMode: Ti.UI.INPUT_BUTTONMODE_ALWAYS,
});
valueView.add(valueText);
win.add(valueView);

// row of buttons
var buttonView = Ti.UI.createView({
  layout: "horizontal",
  height: Ti.UI.SIZE
});

var readBtn = Ti.UI.createButton({
  title: "Read"
});
readBtn.addEventListener('click', function() {
  channelText.blur();
  registerText.blur();
  valueText.blur();
  device.readGPIORegister(parseInt(channelText.value), registerText.value, function(err, data) {
    label.text = (err ? "err:" : ":") + JSON.stringify(err || data); 
  });
});
buttonView.add(readBtn);

var writeBtn = Ti.UI.createButton({
  title: "Write"
});
writeBtn.addEventListener('click', function() {
  channelText.blur();
  registerText.blur();
  valueText.blur();
  device.writeGPIORegister(parseInt(channelText.value), registerText.value, valueText.value, function(err, data) {
    label.text = (err ? "err:" : ":") + JSON.stringify(err || data); 
  });
});
buttonView.add(writeBtn);

var connStateBtn = Ti.UI.createButton({
  title: "Conn State"
});
connStateBtn.addEventListener("click", function(e) {
  device.fetchConnectionState(function(err, data) {
    if (!err) {
      label.text = "connection state: "+JSON.stringify(data);
    }
    else {
      label.text = "connection state error: "+JSON.stringify(err);
    }
  });
});
buttonView.add(connStateBtn);

var streaming = false;
var streamToggleBtn = Ti.UI.createButton({
  title: "Start Streaming"
});
streamToggleBtn.addEventListener("click", function() {
  var listener = function(e) {
    label.text = JSON.stringify(e);
    Ti.API.info("stream event: "+JSON.stringify(e));
  };
  
  if (streaming) {
    device.removeAllListeners("stream"); // removeEventListener not firing on Android
    streamToggleBtn.title = "Start Streaming";
    streaming = false;
  }
  else {
    device.addEventListener("stream", listener);
    streamToggleBtn.title = "Stop Streaming";
    streaming = true;
  }
});
buttonView.add(streamToggleBtn);

win.add(buttonView);

var label = Ti.UI.createLabel();
win.add(label);

win.open();

