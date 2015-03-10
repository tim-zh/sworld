var GameObject = function(game, img, x, y, onUpdate) {
	var animatedSprite = animatedSprites[img];
	Phaser.Sprite.call(this, game, x, y, animatedSprite.name);
	game.physics.arcade.enable(this);
	this.inputEnabled = true;
	this.smoothed = false;
	//this.scale.set(4);
	this.body.allowRotation = false;
	this.body.collideWorldBounds = true;
	var a = animatedSprite.animations;
	for (var i in a)
		if (a.hasOwnProperty(i))
			this.animations.add(a[i].name, a[i].frames, a[i].frameRate, a[i].loop);
	this.directionUp = animatedSprite.directionUp;
	this.directionDown = animatedSprite.directionDown;
	this.directionSide = animatedSprite.directionSide;
	this.anchor.set(0.5);
	game.add.existing(this);
	this.update = onUpdate;
};
GameObject.prototype = Object.create(Phaser.Sprite.prototype);
GameObject.prototype.constructor = GameObject;
//call in create()
//f()
GameObject.prototype.addMouseDown = function(f) { this.events.onInputDown.add(f, this); };
GameObject.prototype.addMouseOver = function(f) { this.events.onInputOver.add(f, this); };
GameObject.prototype.removeMouseDown = function(f) { this.events.onInputDown.remove(f, this); };
GameObject.prototype.removeMouseOver = function(f) { this.events.onInputOver.remove(f, this); };

var stopAnimation = function(object) {
	object.animations.stop();
	object.frame = 0;
	object.direction = null;
};

var updateAnimation = function(object, dx, dy) {
	if (Math.abs(dx) < 1 && Math.abs(dy) < 1) {
		stopAnimation(object);
		return;
	}

	var direction;
	if (Math.abs(dx) < Math.abs(dy)) {
		if (dy < 0)
			direction = object.directionUp;
		else
			direction = object.directionDown;
	} else {
		direction = object.directionSide;
		object.scale.x = dx < 0 ? -1 : 1;
	}
	if (object.direction != direction) {
		object.direction = direction;
		object.animations.play(direction);
	}
};

var moveAt = function(object, x, y) {
	object.body.velocity.set(0);
	if (object.x = x)
		object.body.prev.x = x;
	else
		object.x = x;
	if (object.y = y)
		object.body.prev.y = y;
	else
		object.y = y;
};

var keys;
var player;
var entities = [];
var getPlayer = function(x, y) {
	if (player)
		return player;
	var onUpdate = function() {
		var dx = 0;
		var dy = 0;
		if (keys.up.isDown)
			dy = -playerMaxSpeed;
		else if (keys.down.isDown)
			dy = playerMaxSpeed;
		if (keys.left.isDown)
			dx = -playerMaxSpeed;
		else if (keys.right.isDown)
			dx = playerMaxSpeed;

		updateAnimation(player, dx, dy);
		player.body.velocity.set(dx, dy);

		if (dx != 0 || dy !=0) {
			if (player.stopped)
				sendMessage({move: {x: this.x, y: this.y}});
			player.stopped = false;
			sendMessage({move: {x: this.x, y: this.y}});
		} else if (!player.stopped) {
			player.stopped = true;
			sendMessage({ move: { x: this.x, y: this.y } });
		}
	};
	player = new GameObject(game, 'char', x, y, onUpdate);
	player.stopped = true;
	game.camera.follow(player);
	return player;
};

var rebindKeys = function(up, down, left, right) {
	for (var i in keys) {
		game.input.keyboard.removeKeyCapture(keys[i].keyCode);
		game.input.keyboard.removeKey(keys[i].keyCode);
	}
	game.input.keyboard.addKeyCapture([up, down, left, right]);
	keys = {
		up: game.input.keyboard.addKey(up),
		down: game.input.keyboard.addKey(down),
		left: game.input.keyboard.addKey(left),
		right: game.input.keyboard.addKey(right)
	};
};
var rebindKey = function(key) {
	game.input.keyboard.removeKeyCapture(key);
	game.input.keyboard.removeKey(key);
	game.input.keyboard.addKeyCapture(key);
	return game.input.keyboard.addKey(key);
};