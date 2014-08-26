var GameObject = function(game, img, x, y) {
    Phaser.Sprite.call(this, game, x, y, img);
    game.physics.p2.enable(this);
    this.inputEnabled = true;
    this.smoothed = false;
    this.anchor.set(0.5);
    game.add.existing(this);
};
GameObject.prototype = Object.create(Phaser.Sprite.prototype);
GameObject.prototype.constructor = GameObject;
//call in create()
//f(body)
GameObject.prototype.addCollisionCallback = function(f) {
    this.body.onBeginContact.add(f, this);
};
//call in create()
//f()
GameObject.prototype.addMouseDown = function(f) {
    this.onInputDown.add(f, this);
};
GameObject.prototype.addMouseOver = function(f) {
    this.onInputOver.add(f, this);
};
GameObject.prototype.removeMouseDown = function(f) {
    this.onInputDown.remove(f, this);
};
GameObject.prototype.removeMouseOver = function(f) {
    this.onInputOver.remove(f, this);
};

var keys;
var player;
var getPlayer = function(x, y) {
    if (player)
        return player;
    player = new GameObject(game, 'ship', x, y);
    game.camera.follow(player);
    player.update = function() {
        if (keys.left.isDown)
            this.body.rotateLeft(100);
        else if (keys.right.isDown)
            this.body.rotateRight(100);
        else
            this.body.setZeroRotation();

        if (keys.up.isDown)
            this.body.thrust(400);
        else if (keys.down.isDown)
            this.body.reverse(400);
    };
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