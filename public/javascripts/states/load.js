var animatedSprites = [];
var GameStates = {};
GameStates.Load = function(game) {};
GameStates.Load.prototype = {
	preload: function() {
		game.load.tilemap('map', 'assets/data/test_map.json', null, Phaser.Tilemap.TILED_JSON);
		game.load.image('tiles', 'assets/images/map/test_tiles.png');
		game.load.image('ship', 'assets/images/sprites/thrust_ship2.png');
		game.load.spritesheet('char', 'assets/images/sprites/char.png', 16, 16);
		animatedSprites['char'] = { name: 'char', animations: [
			{ name: 'up_walk', frames: [8, 9, 10, 9], frameRate: 6, loop: true },
			{ name: 'down_walk', frames: [4, 5, 6, 5], frameRate: 6, loop: true },
			{ name: 'side_walk', frames: [2, 3], frameRate: 6, loop: true }],
			directionUp: 'up_walk', directionDown: 'down_walk', directionSide: 'side_walk' };
	},

	create: function() {
		this.game.state.start('MainMenu');
	}
};