/**
 * 
 */

var GWEMS = {
	REVISION : '71'
};

GWEMS.WebSocketClient = function(host, port, uri) {

	this.host = host;
	this.port = port;
	this.uri = uri;
};

GWEMS.WebSocketClient.prototype = {

	constructor : GWEMS.WebSocketClient,

	socket:{},

	start : function() {

		 socket = new WebSocket("ws://" + this.host + ":" + this.port + this.uri);

		socket.onmessage = function(event) {
			GWEMS.WebSocketClient.prototype.handleMessage(event);
		};
		socket.onerror = function(event) {
			GWEMS.WebSocketClient.prototype.handleError(event);
		};
		socket.onclose = function(event) {
			GWEMS.WebSocketClient.prototype.handleClose(event);
		};
		socket.onopen = function(event) {
			GWEMS.WebSocketClient.prototype.handleOpen(event);
		};
	},

	send : function ( string ){
		socket.send(string);
	} 
,
	handleMessage : function(event) {
		console.log(event.data);
	},
	handleError : function(event) {
		console.log(event.data);
	},
	handleOpen : function(event) {
		console.log("Have Open " + event);
	},
	handleClose : function(event) {
		console.log("Socket closed " + event);
	}

};