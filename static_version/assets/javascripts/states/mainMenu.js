GameStates.MainMenu = function(game) {};
GameStates.MainMenu.prototype = {
    create: function() {
        this.game.state.start('MainGame');
    }
};