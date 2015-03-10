var layer;

GameStates.MainGame = function(game) {};
GameStates.MainGame.prototype = {
	create: function() {
		game.stage.backgroundColor = '#333';

		rebindKeys(Phaser.Keyboard.UP, Phaser.Keyboard.DOWN, Phaser.Keyboard.LEFT, Phaser.Keyboard.RIGHT);

		game.physics.startSystem(Phaser.Physics.ARCADE);

		var map = game.add.tilemap('map');
		map.addTilesetImage('tiles');
		layer = map.createLayer('Tile Layer 1');
		layer.resizeWorld();
		map.setCollisionBetween(1, 25);

		var receive = function(message) {
			var msg = JSON.parse(message.data);
			if (msg.newLocation) {
				document.title = msg.newLocation;
				return;
			}
			if (msg.newEntities) {
				msg.newEntities.forEach(function(e) {
					entities[e.id] = new GameObject(game, 'bot', e.x, e.y, function() {});
				});
				msg.goneEntities.forEach(function(e) {
					entities[e.id].destroy();
					delete entities[e.id];
				});
				msg.changedEntities.forEach(function(e) {
					moveAt(entities[e.id], e.x, e.y);
					entities[e.id].body.velocity.set(e.dx, e.dy);
					updateAnimation(e.dx, e.dy);
					console.log(e.dx+" "+e.dy)
				});
				return;
			}
			if (msg.move) {
				getPlayer().x = msg.move.x;
				getPlayer().y = msg.move.y;
				return;
			}
			var entry = msg.isOwner ? "<b>" + msg.user + "</b>" : msg.user;
			if (msg.chat)
				entry += " chats: " + msg.chat + "<br>";
			if (msg.say)
				entry += " says: " + msg.say + "<br>";
			el("screen").innerHTML += entry;
		};

		getPlayer(playerStartPosition[0], playerStartPosition[1]);
		connect('socket', receive);
	},

	update: function() {
		game.physics.arcade.collide(player, layer);
	}
};