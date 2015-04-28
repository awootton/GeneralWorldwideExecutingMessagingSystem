/**
 * 32 times per second we want to broadcast a position of ourself in the 4-16 meter range (and less).<br>
 * 2 times per second in the 16-64 meter range.<br>
 * 1/8 times per second in the 64 to 256 <br>
 * 256 is 1/32 or half a minute<br>
 * 1km every 4 minutes<br>
 * 4 km every 16 minutes (useless) and after 4km we'll be invisible.<br>
 * so, it's 6 levels<br>
 * 
 * We start, at time zero, by broadcasting to all at once.<br>
 * if min spacing is 2 meters apart so that a 4m square can only hold 4 people<br>
 * that's 4 * 32hz = 128 hz<br>
 * for the 16 m it's 64 people * 2 hz = 128 hz<br>
 * etc....<br>
 * or 128 hz * 6 levels = 768 messages per sec incoming. Sounds like a lot.<br>
 * if the messages are eg. {p=[1234,978675,7665],v=[12,.012,33],id="aikwndrtkmngrtnsdoks"}<br>
 * which is position, velocity and id (closer avatars may have longer messages) and is ~64 bytes<br>
 * then 64 bytes * 768 hz = ~50k bytes /sec or 400k bits per second. <br>
 * 
 * Less than a lo rez movie. And, this is a worst case crowd. Normally we should do better than that.
 * 
 */

/**
 * All the variables and stuff that we need to publish and subscribe to a VR world. socket is a GWEMS.WebSocketClient
 * scene is the top level scene graph where we can add and subtract avatars. 
 * userHash is some kind of (tbd) name or id of the operator person/thing/avatar/whatever.
 */

QuadSpaces.Tracker = function(socket, scene, userHash) {

	this.socket = socket;
	this.scene = scene;
	this.userHash = "" + userHash;// an id of the user/camera/avatar
	this.id = "" + userHash;
	this.selfCount = 0;// should increment

	this.allSubscribedChannels = {};// a set.
	this.nextSubscribedChannels = {};// a set.

	// powers of 2 and must be even. We count by 2's.
	this.minLevel = 2;// means there is no finer detail inside of 4 meters
	this.maxLevel = 14;// 2^12 = 4km is the max we can see.

	this.intervals = [];// the successive cubes that we're inside.
	// smallest to largest
	// populate them from 1 to 7 = 4m to 4km
	var time = Date.now();
	var interval = 10;// 100;// 30;// ms
	for (i = this.minLevel; i < this.maxLevel; i += 2) {
		if ( interval < 20 )
			interval = 20;//hack max at 50hz
		var newi = new QuadSpaces.Level(i);
		newi.nextSend = time;
		newi.interval = interval | 0;
		this.intervals.push(newi);
		interval *= 2;// hack for faster rates was 4;
		// TODO: override makeMessage on larger layers
		// to leave off v and etc.
	}
	// this.intervals[0].interval = 300;// slow down for debugging
	// this.intervals[1].interval = 300;// slow down for debugging

	this.avatars = {};// id to Object3D map

	this.socket.tracker = this;
	this.socket.handleMessage = function(data) {
		this.tracker.handleIncoming(data);
	}
}

QuadSpaces.Tracker.prototype.getId = function() {
	return this.id;
}

QuadSpaces.Tracker.prototype.handleIncoming = function(string) {
	try {
		if ( string['@'] )
			return;// ignore ack and others
		var payload = JSON.parse(string);// how does it get double json'ed?
		// there's a parsing flaw in gwems. frack.
	} catch (e) {
		console.log("err " + e);
	}
	if ( ! payload || ! payload['id'] ){
		return;
	}
	if (payload.id == this.id) {
		this.selfCount++;
	} else {
		// console.log("Tracker has message: -------------->> " + payload);
		var theObject = this.avatars[payload.id];
		if (theObject == null) {
			var theObject = avatarBuilder.build(payload.id);
			this.avatars[payload.id] = theObject;
			this.scene.add(theObject);
		}
		// actually, we should just pass the payload and let the object do it.
		theObject.position.x = payload.p[0];
		theObject.position.y = payload.p[1];
		theObject.position.z = payload.p[2];
	}
}

QuadSpaces.Tracker.prototype.update = function(position, velocity) {
	// don't start if the socket is not done yet
	if (this.socket.isOpen == 0)
		return;
	var time = Date.now();

	var nextSubscribedChannels = {};// a set.
	var nextUnsubs = {};// a set.

	var lodlist = QuadSpaces.decompose(position, this.minLevel);
	// re-do the channels before we publish
	// find out which ones we have moved outside of
	// if we are still inside then we are still inside all the larger ones as well.
	// the first pass of this does them all.
	for (var i = 0; i < this.intervals.length; i++) {
		// smallest to largest
		var level = this.intervals[i];
		var str;
		if (i < lodlist.length) {
			str = QuadSpaces.listToString(lodlist, i);
		} else {
			str = "[0_0_0_" + level.logscale + "]";
		}
		if (level.string != str) {
			// it's different, we've moved
			for (key in level.subscribeSpaces) {
				nextUnsubs[key] = level.subscribeSpaces[key];
			}
			level.string = str;
			var some = QuadSpaces.surroundingSpaceNames(position, level.logscale, level.logscale + 1);
			level.subscribeSpaces = some;
			for (key in some) {
				nextSubscribedChannels[key] = some[key];
			}
		} else
			break;
	}

	GWEMS.unsubscribe(nextUnsubs, this.socket)
	GWEMS.subscribe(nextSubscribedChannels, this.socket)

	var p = [];// position of myself
	p.push(position.x);
	p.push(position.y);
	p.push(position.z);
	var v = [];// velocity of myself
	v.push(velocity.x);
	v.push(velocity.y);
	v.push(velocity.z);

	// what messages do we send?
	// walk the list anc check for which ones have timed out.
	for (i in this.intervals) {
		var level = this.intervals[i];
		if (level.nextSend < time) {
			// send message
			var msg = level.makeMessage(p, v, this.id);
			msg = JSON.stringify(msg);
			// there's a parsing flaw in gwems. frack.
			var pubstr = GWEMS.getPublishString(level.string, msg);
			this.socket.send(pubstr);
			level.nextSend += level.interval;
		}
	}
}

QuadSpaces.protoMessage1 = {
	p : [],
	v : [],
	id : ""
};

QuadSpaces.Level = function(logscale) {
	this.nextSend = 0;
	this.interval = 1;
	this.scale = Math.pow(2, logscale);
	// 1 or 4 or 16 or 64, etc. powers of 4
	this.logscale = logscale;// 0 or 2 or 4 or 6, etc. always even

	// the publish channel
	this.string = "";// might be "[0_0_0_0]", or whatever

	// the channels for this level that we subscribe to.
	this.subscribeSpaces = {};

	this.calcChannelString = function(x, y, z) {
		var list = QuadSpaces.decompose({
			x : x,
			y : y,
			z : z
		}, logscale);
		var s = QuadSpaces.listToString(list);
		return s;
	}

	this.makeMessage = function(p, v, id, etc) {
		var msg = QuadSpaces.protoMessage1;
		msg.p = p;
		msg.v = v;
		msg.id = id;
		return msg;// JSON.stringify(msg);
	}

}/**
	 * 
	 */
