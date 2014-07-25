game.ActionController = Object.extend({
    updateCharacterPosition: function (c) {},

    onCollision: function (obj) {}
});

game.PlayerController = game.ActionController.extend({
    updateCharacterPosition: function (c) {
        var dx = 0;
        if (me.input.isKeyPressed('left'))
            dx = -1;
        if (me.input.isKeyPressed('right'))
            dx = 1;
        var dy = 0;
        if (me.input.isKeyPressed('up'))
            dy = -1;
        if (me.input.isKeyPressed('down'))
            dy = 1;
        c.vel.x += dx * c.accel.x * me.timer.tick;
        c.vel.y += dy * c.accel.y * me.timer.tick;

        this.updateMovement();

        return dx != 0 || dy != 0;
    }
});