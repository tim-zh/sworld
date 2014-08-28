var GameStates = {};
GameStates.Load = function(game) {};
GameStates.Load.prototype = {
    preload: function() {
        game.load.tilemap('map', 'assets/data/test_map.json', null, Phaser.Tilemap.TILED_JSON);
        game.load.image('tiles', 'assets/images/map/test_tiles.png');
        game.load.image('ship', 'assets/images/sprites/thrust_ship2.png');
    },

    create: function() {
        this.game.state.start('MainMenu');
    }
};