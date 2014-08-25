GameStates.MainGame = function(game) {};
GameStates.MainGame.prototype = {
    create: function() {
        game.stage.backgroundColor = '#000';

        rebindKeys(Phaser.Keyboard.UP, Phaser.Keyboard.DOWN, Phaser.Keyboard.LEFT, Phaser.Keyboard.RIGHT);

        game.physics.startSystem(Phaser.Physics.P2JS);

        var map = game.add.tilemap('map');
        map.addTilesetImage('ground_1x1');
        map.addTilesetImage('walls_1x2');
        map.addTilesetImage('tiles2');
        var layer = map.createLayer('Tile Layer 1');
        map.createLayer('Tile Layer 2');
        layer.resizeWorld();
        map.setCollisionBetween(1, 12);
        game.physics.p2.convertTilemap(map, layer);
        game.physics.p2.setBoundsToWorld(true, true, true, true, false);

        var ship0 = getPlayer(200, 200);
        var ship1 = new GameObject(game, 'ship', 240, 200);
        ship1.update = function() {
            var angle = Math.atan2(ship0.body.y - this.body.y, ship0.body.x - this.body.x);
            this.body.rotation = angle + game.math.degToRad(90);
            this.body.thrust(400);
        };
        ship1.addMouseDown(function() {
            console.log(123)
        });
        ship1.addCollisionCallback(function(b) {
            console.log(b)
        });
    }
};