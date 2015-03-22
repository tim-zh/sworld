GameStates.MainGame = function(game) {};
GameStates.MainGame.prototype = {
	create: function() {
		game.stage.backgroundColor = '#333';

		rebindKeys(Phaser.Keyboard.UP, Phaser.Keyboard.DOWN, Phaser.Keyboard.LEFT, Phaser.Keyboard.RIGHT);

		game.physics.startSystem(Phaser.Physics.ARCADE);

		var receive = function(message) {
			var msg = JSON.parse(message.data);

			if (msg.location) {
				document.title = msg.location.name;
				loadMap(msg.location.name, msg.location.map);
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

			if (msg.say) {
				var owner = msg.isOwner ? player : entities[msg.id];
				if (owner)
					Text.showMessage(Text.styleChat, msg.say, owner.x + 4, owner.y - 16, owner);
			}

			if (msg.chat) {
				var entry = msg.isOwner ? "<b>" + msg.user + "</b>" : msg.user;
				entry += ": " + msg.chat + "<br>";
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