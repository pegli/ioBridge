var iobridge = require('appersonlabs.iobridge');
Ti.API.info("module is => " + iobridge);

var device = iobridge.createDevice({
  apikey: "A4VPPSNH49U6KGBP",
  serial: "0002C312381E9E86"
});

// open a single window
var win = Ti.UI.createWindow({
	backgroundColor:'white',
	layout: "vertical"
});

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
win.add(connStateBtn);

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
  height: 38,
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
  height: 38,
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
})
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
})
buttonView.add(writeBtn);
win.add(buttonView);

var label = Ti.UI.createLabel();
win.add(label);

win.open();

