/**
 * Copyright Alan Wootton see GPLv3 in license file
 * 
 * Utility functions for dealing with text containing #tags.
 */

var wwc = {};

/**
 * Returns a Set of strings without # No white spaces.
 * No quotes allowed. 
 * If delim is present then it splits by something other than #
 */

wwc.extractTags = function(text, delim ) {

	if ( !delim )
		delim = "#";
	var result = {};
	var parts = text.split(delim);
	for ( var i in parts) {
		// the first one doesn't count
		var str = parts[i];
		str = str.trim();
		var a = str.split(' ');
		str = a[0];
		str = str.replace('"','\\"');
		if (str.length >= 3)
			result[str] = str;
	}
	return result;
};

wwc.toTags = function(map, delim) {
	if ( delim != "" )
		delim = '#';
	var s = "";
	var start = 0;
	for (i in map) {
		if (start != 0)
			s += ' ';
		s += delim + i;
		start++;
	}
	return s;
};

