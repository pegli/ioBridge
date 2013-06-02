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
var label = Ti.UI.createLabel();
win.add(label);

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

var readGPIOBtn = Ti.UI.createButton({
  title: "Read GPIO 6"
});
readGPIOBtn.addEventListener('click', function() {
  device.readGPIORegister("gpio.value.6", function(err, data) {
    label.text = "GPIO.6 " + (err ? "err:" : ":") + JSON.stringify(err || data); 
  });
})
win.add(readGPIOBtn);

var writeGPIOBtn = Ti.UI.createButton({
  title: "Write GPIO 345"
});
writeGPIOBtn.addEventListener('click', function() {
  device.writeGPIORegister("gpio.value", ",,,1000,0,1000", function(err, data) {
    label.text = "gpio.value " + (err ? "err:" : ":") + JSON.stringify(err || data); 
  });
})
win.add(writeGPIOBtn);

win.open();
