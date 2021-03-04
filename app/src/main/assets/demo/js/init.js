
(function() {
	var jsBridge = {
		init: function(callback) {
			connectWebViewJavascriptBridge(function(bridge) {
				bridge.init(function(message, responseCallback) {
					var data = {
						'Javascript Responds': 'Wee!'
					};
					alert(message);
					responseCallback(data);
				});
				if (callback) {
					callback();
				}

			})

		}
	}
	window.JsBridge = jsBridge;
})()

function connectWebViewJavascriptBridge(callback) {
	if (window.WebViewJavascriptBridge) {
		callback(WebViewJavascriptBridge)
	} else {
		document.addEventListener(
			'WebViewJavascriptBridgeReady',
			function() {
				callback(WebViewJavascriptBridge)
			},
			false
		);
	}
}
