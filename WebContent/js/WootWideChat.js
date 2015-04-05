/**
 * Copyright Alan Wootton see GPLv3 in license file
 * 
 * Utility functions for dealing with text containing #tags and differencing maps
 */

// some utility functions
var wwc = {};

/**
 * Returns a Set of strings without # No white spaces.
 * No quotes allowed. 
 */

wwc.extractTags = function(text) {

	var result = {};
	var parts = text.split("#");
	var count = 0;
	for ( var i in parts) {
		// the first one doesn't count
		if ( count ++ > 0 ){
		var str = parts[i];
		str = str.trim();
		var a = str.split(' ');
		str = a[0];
		str = str.replace('"','\\"');
		if (str.length >= 3)
			result[str] = str;
		}
	}
	return result;
};

wwc.toTags = function(map) {
	var s = "";
	var start = 0;
	for (i in map) {
		if (start != 0)
			s += ' ';
		s += '#' + i;
		start++;
	}
	return s;
};

/** Subtract the before Set from the after Set and return a new Set.
 */
wwc.addedToMap = function(before, after) {
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

wwc.removedFromMap = function(before, after) {
	return wwc.addedToMap(after, before);
}
