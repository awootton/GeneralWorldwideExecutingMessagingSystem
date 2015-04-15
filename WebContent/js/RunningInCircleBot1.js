RunInCircle = function(gwems, id, center, radius, angularVelocity, startAngle) {

	var gwems = gwems;
	var id = id;
	var center = center || new THREE.Vector3();
	var radius = radius || 10;

	var angularVelocity = angularVelocity || 0.4;// radians/sec
	var angle = startAngle || 4;

	var publishSpace = "0_0_400_10";// 1 km cubes 2^20 away

	var interval = 1;// in s

	// eg. the 8 spaces bordering 0,0,1<<20 of size 1024
	// -1_-1_3ff_10
	// -1_-1_400_10
	// -1_0_3ff_10
	// -1_0_400_10
	// 0_-1_3ff_10
	// 0_-1_400_10
	// 0_0_3ff_10
	// 0_0_400_10

	this.timer = window.setInterval(function() {

		var x = radius * Math.cos(angle);
		var z = radius * Math.sin(angle);

		var message = {};
		message["@"] = "m.P";
		message.id = id;
		message.position = {};// position
		message.position["@"] = "m.P";
		message.position.x = deres(x + center.x);
		message.position.y = deres(0 + center.y);
		message.position.z = deres(z + center.z);

		angle += angularVelocity * interval;
		var nextx = radius * Math.cos(angle);
		var nextz = radius * Math.sin(angle);

		message.velocity = {};
		message.velocity["@"] = "m.P";
		message.velocity.x = (nextx - x) / interval;
		message.velocity.y = 0;
		message.velocity.z = (nextz - z) / interval;

		var str = GWEMS.getPublishString(publishSpace, message);
		gwems.send(str);

	}, interval * 1000);

};

var deres = function(double) {
	return (1.0 / 128) * ((double * 128) | 0);
}

RunInCircle.prototype.stop = function() {
	this.timer.clearInterval();
}

// TODO: move to own file

VelocityObjects = function() {
	this.map = {};
	this.container = new THREE.Object3D();
}

VelocityObjects.prototype.getContainer = function() {
	return this.container;
}

VelocityObjects.prototype.get = function(id) {
	return this.map[id];
}

VelocityObjects.prototype.getOrConstructDefault = function(message) {
	var anAvatar = this.get(message.id);
	if (!anAvatar) {
		console.log("new moving " + message.id);
		var material = new THREE.MeshLambertMaterial({
			color : 0xFFFFFF
		});
		anAvatar = new THREE.Mesh(new THREE.BoxGeometry(1, 1, 1), material);

		if (message.velocity) {// copy it
			anAvatar.velocity = new THREE.Vector3();
			anAvatar.velocity.x = message.velocity.x;
			anAvatar.velocity.y = message.velocity.y;
			anAvatar.velocity.z = message.velocity.z;
			moving.put(message.id, anAvatar);
		} else {
			scene.add(anAvatar);
		}
	}
	return anAvatar;
}

/**
 * obj needs to be a THREE.Object3D of some kind.
 * 
 * @param id
 * @param obj
 */
VelocityObjects.prototype.put = function(id, obj) {
	var previous = this.map[id];
	this.map[id] = obj;
	this.container.remove(previous);
	this.container.add(obj);
}

/**
 * Delta is in seconds
 * 
 */
VelocityObjects.prototype.update = function(delta) {
	for ( var key in this.map) {
		// key is an Object3D with a velocity
		var obj = this.map[key];
		var v = obj.velocity;
		if (v) {
			obj.position.x += v.x * delta;
			obj.position.y += v.y * delta;
			obj.position.z += v.z * delta;
		}
	}
}
