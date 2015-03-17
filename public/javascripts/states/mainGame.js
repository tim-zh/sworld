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
			}

			if (msg.eUpdate) {
				msg.eUpdate.forEach(function(e) {
					entities[e.id].x = e.x;
					entities[e.id].y = e.y;
					entities[e.id].body.velocity.set(e.dx, e.dy);
					updateAnimation(entities[e.id], e.dx, e.dy);
				});
			}

			if (msg.eNew) {
				msg.eNew.forEach(function(e) {
					entities[e.id] = new GameObject(game, e.type, e.x, e.y);
				});
			}

			if (msg.eGone) {
				msg.eGone.forEach(function(e) {
					entities[e.id].destroy();
					delete entities[e.id];
				});
			}

			if (msg.move) {
				getPlayer().x = msg.move.x;
				getPlayer().y = msg.move.y;
			}

			if (msg.say || msg.chat) {
				var entry = msg.isOwner ? "<b>" + msg.user + "</b>" : msg.user;
				if (msg.chat)
					entry += " chats: " + msg.chat + "<br>";
				if (msg.say)
					entry += " says: " + msg.say + "<br>";
				el("screen").innerHTML += entry;
			}
		};

		getPlayer(0, 0);
		connect('socket', receive);
	},

	update: function() {
		game.physics.arcade.collide(player, layer);
	}
};