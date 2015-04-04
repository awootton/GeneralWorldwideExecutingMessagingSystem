/**
 * 
 */

var GWEMS = {
	REVISION : '71'
};

GWEMS.WebSocketClient = function(host, port, uri) {

	this.host = host;
	this.port = port;
	this.host = uri;

	this.socket = null;
};

GWEMS.WebSocketClient.protype = {

	start : function(host, port, uri) {

		socket = new WebSocket("ws://" + host + ":" + port + uri);

		socket.onmessage = function(event) {
			this.handleMessage(event);
		};
		socket.onerror = function(event) {
			this.handleError(event);
		};
		socket.onclose = function(event) {
			this.handleClose(event);
		};
		socket.onopen = function(event) {
			this.handleOpen(event);
		};
	},

	handleMessage : function(event) {
		console.log(event.data);
	},
	handleError : function(event) {
		console.log(event.data);
	},
	handleOpen : function(event) {
		console.log(event.data);
	},
	handleClose : function(event) {
		console.log(event.data);
	}

};