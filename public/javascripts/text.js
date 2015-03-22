var Text = {
	styleDamage: {
		color: '#f00',
		duration: 80,
		dx: 0,
		dy: -8
	},

	styleChat: {
		color: '#fff',
		duration: 400,
		dx: 0,
		dy: 0
	},

	showMessage: function(style, msg, x, y, owner, random) {
		if (random) {
			x = x + (Math.random() * 2 - 1) * random;
			y = y + (Math.random() * 2 - 1) * random;
		}
		var textStyle = { font: '13px Helvetica', stroke: '#000', strokeThickness: 2, fill: style.color };
		var text = game.add.text(x, y, msg, textStyle);
		text.dx = 0;
		text.dy = 0;
		var ownerStartX;
		var ownerStartY;
		if (owner) {
			ownerStartX = owner.x;
			ownerStartY = owner.y;
			text.update = function() {
				text.x = x + text.dx + owner.x - ownerStartX;
				text.y = y + text.dy + owner.y - ownerStartY;
			};
		}

		var stepInterval = 50;
		var fadeDuration = 200;
		var fadeSteps = fadeDuration / stepInterval;

		var t = 0;
		var easing = function(t) { return t * t; };

		game.time.events.add(style.duration, function() {
			game.time.events.repeat(stepInterval, fadeSteps, function() {
				t += 1 / fadeSteps;
				if (t > 1 - 1 / fadeSteps) {
					text.destroy();
					return;
				}
				text.alpha -= easing(t);
				if (style.dx)
					text.dx += style.dx / fadeSteps;
				if (style.dy)
					text.dy += style.dy / fadeSteps;
			}, this);
		}, this);
	}
};