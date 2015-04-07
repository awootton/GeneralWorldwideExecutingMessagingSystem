/** 
 * Note that everything before the 'opencurly' is stripped off
 * before eval
 * 
 */
var dummy =
{
	id : "none", // a guid, or key
	subscene : new THREE.Object3D() ,
	
	setPosition :  function(position) {
		this.subscene.position.set(position);
	},

	setOrientation :  function(orientation) {
		// sigh
	},

	build :  function() { // the data will be visible

		 subscene = new THREE.Object3D();

		 subscene.userData = this;

		var geometry = new THREE.BoxGeometry(1/2,0.75,1/2);// one meter
		var material = new THREE.MeshLambertMaterial({
			color : 0x7777FF
		})

		var cube = new THREE.Mesh(geometry , material);
		cube.position.set(0, 0.75 / 2, 0);
		subscene.add(cube);
	 
		var rad = 0.5;//this.size.x/2;
		var sphereg = new THREE.SphereGeometry(rad,this.segments);
		var sphere = new THREE.Mesh(sphereg, material);
		sphere.position.set(0, 1, 0);
		sphere.scale.set(1/2,1,1/2);
		
		subscene.add(sphere);
		return subscene;
	}

}
