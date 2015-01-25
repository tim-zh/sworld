GameStates.MainGame = function(game) {};
GameStates.MainGame.prototype = {
	create: function() {
		game.stage.backgroundColor = '#333';

		rebindKeys(Phaser.Keyboard.UP, Phaser.Keyboard.DOWN, Phaser.Keyboard.LEFT, Phaser.Keyboard.RIGHT);

		game.physics.startSystem(Phaser.Physics.P2JS);

		var map = game.add.tilemap('map');
		map.addTilesetImage('tiles');
		var layer = map.createLayer('Tile Layer 1');
		layer.resizeWorld();
		map.setCollisionBetween(1, 12);
		game.physics.p2.convertTilemap(map, layer);
		game.physics.p2.setBoundsToWorld(true, true, true, true, false);

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
				msg.changedEntities.forEach(function(e) {
					move(entities[e.id], e.x - entities[e.id].body.x, e.y - entities[e.id].body.y);
				});
				msg.goneEntities.forEach(function(e) {
					entities[e.id].destroy();
					delete entities[e.id];
				});
				return;
			}
			if (msg.move) {
				getPlayer().body.x = msg.move.x;
				getPlayer().body.y = msg.move.y;
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
	}
};