var GameStates = {};
GameStates.Load = function(game) {};
GameStates.Load.prototype = {
    preload: function() {
        game.load.tilemap('map', 'assets/data/collision_test.json', null, Phaser.Tilemap.TILED_JSON);
        game.load.image('ground_1x1', 'assets/images/map/ground_1x1.png');
        game.load.image('walls_1x2', 'assets/images/map/walls_1x2.png');
        game.load.image('tiles2', 'assets/images/map/tiles2.png');
        game.load.image('ship', 'assets/images/sprites/thrust_ship2.png');
    },

    create: function() {
        this.game.state.start('MainMenu');
    }
};