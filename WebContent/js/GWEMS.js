/**
 * An interface to the GeneralWorldwideExecutingMessageSystem on github. Mostly it's generic websockets. Copyright 2015
 * Alan Wootton see included license.
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
	// ReconnectingWebSocket 
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
		this.GWEMS.handleMessage(msg);
	}
};

GWEMS.WebSocketClient.prototype.send = function(string) {
	try {  
	if (string && string.length > 0) {
		this.socket.send(string);
	}
	} catch (e) {
		// say something
	};
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

/**
 * Subtract the before Set from the after Set and return a new Set. Expects an object where keys and value are the same.
 * eg. { key:key } Because this I show I do a Set.
 * 
 */
GWEMS.addedToSet = function(before, after) {
	var res = JSON.parse(JSON.stringify(after));
	for (key in before) {
		res[key] = null;
	}
	var res2 = {};
	for (key in res) {
		if (res[key]) {
			res2[key] = key;
		}
	}
	return res2;
}

GWEMS.removedFromSet = function(before, after) {
	return GWEMS.addedToSet(after, before);
}

GWEMS.subscribeJsonObject = {
	"@C" : "gwems.Subscribe",
	"channel" : ""
};

GWEMS.getSubscribeString = function(channel) {
	GWEMS.subscribeJsonObject.channel = channel;
	var s = JSON.stringify(GWEMS.subscribeJsonObject);
	return s;
};

GWEMS.unsubscribeJsonObject = {
	"@C" : "gwems.Unubscribe",
	"channel" : ""
};

GWEMS.getUnsubscribeString = function(channel) {
	GWEMS.unsubscribeJsonObject.channel = channel;
	var s = JSON.stringify(GWEMS.unsubscribeJsonObject);
	return s;
}

GWEMS.publishObject = {
	"@C" : "gwems.Publish",
	"channel" : "",
	"msg" : {
		"@C" : "gwems.Push2Client",
		"msg" : ""
	}
};

GWEMS.getPublishString = function(channel, message) {
	GWEMS.publishObject.channel = channel;
	GWEMS.publishObject.msg.msg = message;
	var s = JSON.stringify(GWEMS.publishObject);
	return s;
}
