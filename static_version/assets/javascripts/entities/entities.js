game.Player = me.ObjectEntity.extend({
    init: function(x, y) {
        var name = 'player';
        var image = me.loader.getImage('gripe_run_right');
        this.parent(x, y, {name: name, image: image, width: image.height, height: image.height});
        this.setVelocity(5, 5);
        this.gravity = 0;
    },

    update: function(dt) {
        var dx = 0;
        if (me.input.isKeyPressed('left')) {
            dx = -1;
            this.flipX(true);
            //me.audio.play("jump");
            //this.renderable.flicker(750);
        }
        if (me.input.isKeyPressed('right')) {
            dx = 1;
            this.flipX(false);
        }
        if (dx == 0)
            this.vel.x = 0;
        var dy = 0;
        if (me.input.isKeyPressed('up'))
            dy = -1;
        if (me.input.isKeyPressed('down'))
            dy = 1;
        if (dy == 0)
            this.vel.y = 0;
        this.vel.x += dx * this.accel.x * me.timer.tick;
        this.vel.y += dy * this.accel.y * me.timer.tick;

        this.updateMovement();
        me.game.viewport.follow({x: this.pos.x + this.width / 2, y: this.pos.y + this.height / 2});

        return this.vel.x != 0 || this.vel.y != 0;
    }
});

/**
 * Coin Entity
 */
game.CoinEntity = me.CollectableEntity.extend(
{	

	init: function (x, y, settings)
	{
		// call the parent constructor
		this.parent(x, y , settings);
	},		
		
	onCollision : function ()
	{
		// do something when collide
		me.audio.play("cling");
		// give some score
		game.data.score += 250;
		// make sure it cannot be collected "again"
		this.collidable = false;
		// remove it
		me.game.world.removeChild(this);
	}

	
});

/**
 * Enemy Entity
 */
game.EnemyEntity = me.ObjectEntity.extend(
{	
	init: function (x, y, settings)
	{
		// define this here instead of tiled
		settings.image = "wheelie_right";
          
        // save the area size defined in Tiled
		var width = settings.width;
		var height = settings.height;;

		// adjust the size setting information to match the sprite size
        // so that the entity object is created with the right size
		settings.spritewidth = settings.width = 64;
		settings.spritewidth = settings.height = 64;
		
		// call the parent constructor
		this.parent(x, y , settings);
		
		// set start/end position based on the initial area size
		x = this.pos.x;
		this.startX = x;
		this.endX   = x + width - settings.spritewidth
		this.pos.x  = x + width - settings.spritewidth;

		// walking & jumping speed
		this.setVelocity(4, 6);
		
		// make it collidable
		this.collidable = true;
		this.type = me.game.ENEMY_OBJECT;
	},
	
		
	onCollision : function (res, obj)
	{
			
		// res.y >0 means touched by something on the bottom
		// which mean at top position for this one
		if (this.alive && (res.y > 0) && obj.falling)
		{
			// make it flicker
			this.renderable.flicker(750);
		}
	},

	
	// manage the enemy movement
	update : function (dt)
	{
		// do nothing if not in viewport
		if (!this.inViewport)
			return false;
			
		if (this.alive)
		{
			if (this.walkLeft && this.pos.x <= this.startX)
			{
				this.walkLeft = false;
			}
			else if (!this.walkLeft && this.pos.x >= this.endX)
			{
				this.walkLeft = true;
			}
			
			this.flipX(this.walkLeft);
			this.vel.x += (this.walkLeft) ? -this.accel.x * me.timer.tick : this.accel.x * me.timer.tick;
		}
		else
		{
			this.vel.x = 0;
		}
		// check & update movement
		this.updateMovement();
		
		if (this.vel.x!=0 ||this.vel.y!=0)
		{
			// update the object animation
			this.parent(dt);
			return true;
		}
		return false;
	}
});
