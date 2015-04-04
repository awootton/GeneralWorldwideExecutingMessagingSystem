/** An interface to the GeneralWorldwideExecutingMessageSystem on github.
Mostly it's generic websockets.
 * Copyright 2015 Alan Wootton see included license.
 */

var GWEMS = {
	REVISION : '1'
};

GWEMS.WebSocketClient = function(host, port, uri) {

	this.host = host;
	this.port = port;
	this.uri = uri;

	this.socket = {};
};

GWEMS.WebSocketClient.prototype.start = function() {

	this.socket = new WebSocket("ws://" + this.host + ":" + this.port + this.uri);
	this.socket.GWEMS = this;

	this.socket.onopen = function(event) {
		this.GWEMS.handleOpen(event);
	};
	this.socket.onclose = function(event) {
		this.GWEMS.handleClose(event);
	};
	this.socket.onerror = function(event) {
		this.GWEMS.handleError(event);
	};
	this.socket.onmessage = function(event) {
     	var msg = event.data;
     	console.log(msg);
     	this.GWEMS.handleMessage(msg);
	}
};

GWEMS.WebSocketClient.prototype.send = function(string) {
	if ( string && string.length>0) {
		this.socket.send(string);
	}
};

GWEMS.WebSocketClient.prototype.handleOpen = function(event) {
	console.log("Have Open " + event);
};

GWEMS.WebSocketClient.prototype.handleClose = function(event) {
	console.log("Have Close " + event);
};

GWEMS.WebSocketClient.prototype.handleError = function(event) {
	this.socket.close();
	console.log("Have error " + event);
};

GWEMS.WebSocketClient.prototype.handleMessage = function(string) {
	console.log("Have message " + string);
};
