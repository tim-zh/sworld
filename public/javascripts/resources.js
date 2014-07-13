game.resources = [
	/**
	 * Graphics.
	 */
	// the main player spritesheet
	{name: "gripe_run_right",     type:"image",	src: "assets/images/sprites/gripe_run_right.png"},
	// the spinning coin spritesheet
	{name: "spinning_coin_gold",  type:"image",	src: "assets/images/sprites/spinning_coin_gold.png"},
	// our enemty entity
	{name: "wheelie_right",       type:"image",	src: "assets/images/sprites/wheelie_right.png"},
	// game font
	{name: "32x32_font",          type:"image",	src: "assets/images/fonts/32x32_font.png"},
	// title screen
	{name: "title_screen",        type:"image",	src: "assets/images/title_screen.png"},
	// the parallax background
	{name: "area01_bkg0",         type:"image",	src: "assets/images/area01_bkg0.png"},
	{name: "area01_bkg1",         type:"image",	src: "assets/images/area01_bkg1.png"},
	// our level tileset
	{name: "area01_level_tiles",  type:"image",	src: "assets/images/map/area01_level_tiles.png"},
	// our metatiles
	{name: "metatiles32x32",      type:"image", src: "assets/images/map/metatiles32x32.png"},
	
	/* 
	 * Maps. 
 	 */
	{name: "area01",              type: "tmx",	src: "assets/data/maps/area01.tmx"},
	{name: "area02",              type: "tmx",	src: "assets/data/maps/area02.tmx"},

	/* 
	 * Background music. 
	 */	
	{name: "dst-inertexponent", type: "audio", src: "assets/data/music/"},
	
	/* 
	 * Sound effects. 
	 */
	{name: "cling", type: "audio", src: "assets/data/sounds/"},
	{name: "stomp", type: "audio", src: "assets/data/sounds/"},
	{name: "jump",  type: "audio", src: "assets/data/sounds/"}
];
