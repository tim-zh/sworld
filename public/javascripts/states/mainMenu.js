GameStates.MainMenu = function(game) {};
GameStates.MainMenu.prototype = {
    create: function() {
        var game = this.game;
        var startButton = game.add.button(game.world.centerX, game.world.centerY, 'ship', null, this);
        startButton.alpha=0.8;
        startButton.fixedToCamera = true;
        startButton.onInputUp.add(function() {game.state.start('MainGame')}, this);
        startButton.onInputOver.add(function() {startButton.alpha=1});
        startButton.onInputOut.add(function() {startButton.alpha=0.8});
        startButton.anchor.set(0.5);
    }
};