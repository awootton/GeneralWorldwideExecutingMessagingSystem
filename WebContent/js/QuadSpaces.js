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
	// this version will go further than a double.
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

// for a series of levels we need the quad names of the surrounding quads.
// It's practically an interview question. It's weird.
// I'm not sure that we need to do it that often or that it's a performance
// problem and I'm in a hurry to write it.
// when we are kn the center of a cube then we are fine just subscribing to that cube
// but, when we are near a corner then we need the neighbors as well.

QuadSpaces.surroundingSpaceNames = function(  position,   start,   end) {
	var set = {};
	var tmp = new THREE.Vector3(0, 0, 0);
	for (var bias = start; bias < end; bias++) {
		var  length = Math.pow(2, bias);
		length = length * 3.0/4.0; // we don't really need all 27. In some cases 1 will do.
		var lenEnd = length;
		// iterate -len, 0, + len
		for (var x = -length; x <= lenEnd; x += length) {
			for (var y = -length; y <= lenEnd; y += length) {
				for (var z = -length; z <= lenEnd; z += length) {
					tmp.x = position.x + x;
					tmp.y = position.y + y;
					tmp.z = position.z + z;
					var quad = this.decompose(tmp, bias);
					var str = this.listToString(quad);
					set[str] = str;
				}
			}
		}
	}
	return set;
}


QuadSpaces.listToString = function(list) {
	var res = '[';
	var i = 0;
	for (s = 0; s < list.length; s++) {
		if (i != 0)
			res += ", ";
		res += list[s];
		i++;
	}
	res += ']';
	return res;
}

QuadSpaces.listToStringOffset = function(list, offset1) {
	var res = '[';
	var i = 0;
	for (s = offset1; s < list.length; s++) {
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


