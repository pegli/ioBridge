# Titanium RealTime.io module

RealTime.io, powered by ioBridge technology, is a platform for connecting devices to cloud services 
and mobile apps seamlessly. The RealTime.io Titanium Modules for iOS and Android allow mobile app developers 
to build interactivity between mobile devices and embedded devices via Appcelerator Titanium.

## This document follows these conventions:

* Text in `code font` refer to module objects.  For example, device is a generic term
  but `device` refers to a module device object.
* The term "dictionary" is used to distinguish document properties objects from generic
  JavaScript Object types.  Parameters and return values of "dictionary" type are Objects
  with key-value pairs and no functions.
* Object functions are listed with parentheses and properties without.  Constants are
  implemented as read-only properties.

## Accessing the Module

To access this module from JavaScript, you would do the following:

    var iobridge = require("appersonlabs.iobridge");

<a name="module"/>
## Module

Global functions, properties, and constants.

### Functions

**createDevice**(params)

param keys must include:

* apikey (string): The unique key assigned to a device through the ioBridge
device registration process.
* serial (string): The serial number of the device to create.

Returns a [`device`](#device) object that can be used to remotely communicate
with an ioBridge device through the RealTime.io service.

<a name="device"/>
## Device

The app communicates with an ioBridge device connected to the RealTime.io
service through the `device` object.  To read data from the object, register
an event listener for the `stream` event:

    var device = iobridge.createDevice({
        apikey: "982656D1-368B-4FB3-9655-B6AFBDEB7D04",
        serial: "7640212975142365"
    });
    
    device.addEventListener("stream", function(e) {
        Ti.API.info(e.serial + " - " + e.payload);
    });

### Properties

**apikey**

string, read-only.  The API key associated with this device object.

**serial**

string, read-only.  The serial number associated with this device object.

### Functions

**fetchConnectionState**(callback)

* callback (function(err, data)): optional function to call when the request is
complete or in the event of an error.

Get the connection state of the device (either connected or disconnected).
*TODO* document the "data" object in the callback.

**fetchConnectionState**(callback)

* callback (function(err, data)): optional function to call when the request is
complete or in the event of an error.

Get the connection state of the device (either connected or disconnected).
*TODO* document the "data" object in the callback.

**readGPIORegister**(channel, register, callback)

* channel (number): the channel to use for reading
* register (string): the name of the register to read.
* callback (function(err, data)): optional function to call when the request is
complete or in the event of an error.

Read the value of a GPIO register for Iota-enabled devices.
*TODO* document the "data" object in the callback.

**sendData**(channel, payload, encoding, callback)

* channel (number): the channel to use for writing
* payload (string): the raw data to send to the device.  If the payload contains
non-ASCII data, the `encoding` parameter must be set to "base64".
* encoding (string): set to "base64" to encode non-ASCII data.  Default is "plain".
* callback (function(err, data)): optional function to call when the request is
complete or in the event of an error.

Send arbitrary data to the device.  The API key used to create the device must
have write permission.

**writeGPIORegister**(channel, register, content, callback)

* channel (number): the channel to use for writing
* register (string): the name of the register to read.
* content (string): the data to write to the named register.
* callback (function(err, data)): optional function to call when the request is
complete or in the event of an error.

Write the value of a GPIO register for Iota-enabled devices.
*TODO* document the "data" object in the callback.

### Events

**stream**: a stream event was received for this device from the ioBridge RealTime.io
server.  The following event properties may be present:

* serial: The serial ID if the device from which the data original data came
* channel: The channel on which the device used to send the data
* status: Connected or Disconnected
* source: ‘remote’ if the data is from a device, ‘server’ if this is an update from the server
* timestamp: Epoch time in seconds of the message’s receipt by the server
* ms: Milliseconds after the timestamp that defines the exact subsecond of message’s receipt
* encoding: ‘plain’ or ‘base64’
* payload: the actual event data

The payload of the event may be GPIO pin values, serial data input, or other device
data.
