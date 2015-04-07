/**
 * The tests for this are in the java tests and they run in nashorn awootton
 */

/**
 * Some math test's I'm going to have to write in js. I'm just making notes here. The GWEMS is not likely to have any 3d
 * math at all.
 * 
 * We need to enumerate 3d space into quad trees (not oct trees or tri trees?). In absolute coordinates.
 * 
 * We would like to have a string format to subscribe to like: 1_2_-1_6 or x_y_z_p where x_y_z have to have values
 * [-2..1] and p is an even integer that is interpreted as a power of 2. I need to put them in url's.
 * 
 * what are they called ? Quadspaces
 * 
 * 0_0_0_0 is the minimum corner of the zero meter cube. 0_0_0_1 is an error. 0_0_0_2 is a 4 by 4 by 4 meter cube at the
 * origin. .
 * 
 */

var QuadSpaces = {};

/**
 * Return a list of quads, low quads first, that contain this vector. large quads that start with 0_0_0 are suppressed.
 * 
 * fractional quads, less than 1 meter, are left out unless bias is < 0. Bias must be even! It's a power of 2. We
 * 
 * @param v
 * @return
 */

QuadSpaces.decompose = function(v, bias) {

	var result = [];
	var power = Math.pow(2, -bias);
	var x = (v.x * power) | 0;
	var y = (v.y * power) | 0;
	var z = (v.z * power) | 0;
	var p = bias;
	var i1, i2, i3;
	var s;
	while (true) {
		x += 2;
		y += 2;
		z += 2;
		i1 = x & 3;
		i2 = y & 3;
		i3 = z & 3;
		s = "" + (i1 - 2) + "_" + (i2 - 2) + "_" + (i3 - 2) + "_" + (p);
		x -= i1;
		y -= i2;
		z -= i3;
		x = (x >> 2) | 0;
		y = (y >> 2) | 0;
		z = (z >> 2) | 0;
		p += 2;
		// we could suppress zero fraction parts here
		// if ( s.startsWith("0_0_0_"))
		result.push(s);
		if (x == 0 && y == 0 && z == 0) {
			break;
		}
	}
	return result;
}

/**
 * Return the vector of the corner of the cube described by s where is is of the form x_y_x_p and x, y, and z MUST be
 * between -2 and 1 and p MUST be an even number. p is a power of 2
 * 
 * @param s
 * @return
 * @throws EncodeException
 */

QuadSpaces.getMinCorner = function(str) {
	var parts = str.split("_");
	if (parts.length != 4) {
		throw new NumberFormatException(s + " needs 4 _parts");
	}
	var i0, i1, i2;

	i0 = parseInt(parts[0]);
	i1 = parseInt(parts[1]);
	i2 = parseInt(parts[2]);

	var off = 2;
	if ((((i0 + off) & 0xFFFFFFFC) != 0) || (((i1 + off) & 0xFFFFFFFC) != 0) || (((i2 + off) & 0xFFFFFFFC) != 0)) {
		throw new NumberFormatException(s + " must be signed 2 bit");
	}
	var scaleFactor = parseInt(parts[3]);
	var vmin = new THREE.Vector3(i0, i1, i2);
	// how absurd do we have to get with the powers?
	var power = Math.pow(2, scaleFactor);
	vmin.x *= power;
	vmin.y *= power;
	vmin.z *= power;
	return vmin;
}

/**
 * The complete opposite of decompose. A list of quadspaces can be added to recover a vector in 3 space. Returns a
 * THREE.Vector3
 * 
 * @throws EncodeException
 * 
 */
QuadSpaces.reconstitute = function(quadspaces) {
	sum = new THREE.Vector3(0, 0, 0);
	for (i in quadspaces) {
		var str = quadspaces[i];
		sum.add(this.getMinCorner(str));
	}
	return sum;
}

QuadSpaces.listToString = function(list) {
	var res = '[';
	var i = 0;
	for (s in list) {
		if (i != 0)
			res += ", ";
		res += list[s];
		i++;
	}
	res += ']';
	return res;
}

QuadSpaces.vector = function(x, y, z) {
	return new THREE.Vector3(x, y, z);
}

QuadSpaces.getX = function(vector) {
	return vector.x;
}
QuadSpaces.getY = function(vector) {
	return vector.y;
}
QuadSpaces.getZ = function(vector) {
	return vector.z;
}

/**
 * 32 times per second we want to broadcast a position of ourself in the 4-16 meter range (and less).<br>
 * 2 times per second in the 16-64 meter range.<br>
 * 1/8 times per second in the 64 to 256 <br>
 * 256 is 1/32 or half a minute<br>
 * 1km  every 4 minutes<br>
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
 * 
 */

QuadSpaces.Tracker = function(socket, scene, userHash) {

	this.allSubscribed = {};// a set.
	this.socket = socket;
	this.scene = scene;
	this.userHash = userHash;// an id of the user/camera/avatar

	this.intervals = [];// 
	// populate them from 1 to 7 = 4m to 4km
	var time = Date.now();
	var interval = 30;// ms
	for ( i = 0 ; i < 6 ; i ++ ){
		var newi = new QuadSpaces.Level  ( i + 1); 
		newi.nextSend = time;
		newi.interval = interval;
		this.intervals.push(newi);
		interval *= 4;
		// TODO: override makeMessage on larger layers
		// to leave off v and etc.
	}
}

QuadSpaces.Tracker.prototype.handleIncoming = function(string) {
	console.log("Tracker has message:  " + string);
}

QuadSpaces.Tracker.prototype.update = function(position, velocity, id ) {
	var time = Date.now();
	// recalc all the interval/level strings
	// unsub and sub as necessary
	// reset times when resub.
	var p = [];// position of myself
	p.push(position.x);
	p.push(position.y);
	p.push(position.z);
	var v = [];// velocity of myself
	v.push(velocity.x);
	v.push(velocity.y);
	v.push(velocity.z);
	
	// what messages do we send
	for (i in this.intervals) {
		var level = this.intervals[i];
		if (level.nextSend < time) {
			// send message
			var msg = level.makeMessage(p,v,id);
			var pubstr = GWEMS.getPublishString(level.string,msg);
			this.socket.send(pubstr);
			level.nextSend += level.interval;
		}
	}
}

QuadSpaces.protoMessage1 = {
	 p:[],
	 v:[],
	 id:""
};

QuadSpaces.Level =  function( logscale ) {
	this.nextSend = 0;
	this.interval = 1;
	this.scale = Math.pow(2, logscale);;// 1 or 4 or 16 or 64, etc. powers of 4
	this.logscale = logscale;// 0 or 2 or 4 or 6, etc. always even

	// the publish channel
	this.string = "";// might be "[0_0_0_0]", or whatever

	this.calcChannelString = function(x, y, z) {
		var list = QuadSpaces.decompose({
			x : x,
			y : y,
			z : z
		}, logscale);
		var s = QuadSpaces.listToString(list);
		return s;
	}
	
	this.makeMessage = function( p, v, id , etc){
		var msg = QuadSpaces.protoMessage1;
		msg.p = p;
		msg.v = v;
		msg.id = id;
		return msg;//JSON.stringify(msg);
	 }
 
}

/**
 * var v = new THREE.Vector3(1, 2, 3);
 * 
 * var strList = QuadSpaces.decompose(v, 0);
 * 
 * console.log(QuadSpaces.listToString(strList));
 * 
 * var vect2 = QuadSpaces.getMinCorner(strList[0]);
 * 
 * var vect3 = QuadSpaces.reconstitute(strList);
 * 
 */
