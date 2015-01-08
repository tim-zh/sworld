var socket;

var connect = function(url, serverResponseProcessingCallback) {
	socket = new WebSocket('ws://' + window.location.host + '/' + url);
	setMessageProcessing(serverResponseProcessingCallback);
};

var setMessageProcessing = function(callback) {
	if (socket)
		socket.onmessage = callback;
	else
		throw "no socket initialized";
};

var sendMessage = function(message) {
	if (socket)
		socket.send(JSON.stringify(message));
	else
		throw "no socket initialized";
};