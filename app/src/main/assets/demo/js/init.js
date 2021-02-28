
(function() {
	var idea = {
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

		},
		debug: function(callback) {
			window.WebViewJavascriptBridge.callHandler(
				'debug', {},
				function(responseData) {
					if (callback != null)
						callback(responseData)
				}
			);
		},
		/**
		 * 扫码
		 * @param {Object} options : param(参数) callback（回调函数，返回data ）
		 */
		scanGetCode: function(options) {
			var params = options.params;
			var str = "";
			if (params != null) {
				str = typeof(params) == "object" ? JSON.stringify(params) : params + str;
			}
			window.WebViewJavascriptBridge.callHandler(
				'scanGetCode', {
					'params': str
				},
				function(responseData) {
					if (options.callback)
						options.callback(responseData);
				}
			);
		},
		/**
		 * 定位
		 * @param {Object} options : param(参数) callback（回调函数，返回data ）
		 */
		getLocationInfo: function(options) {
			window.WebViewJavascriptBridge.callHandler('getLocationInfo');
		},

		/**
		 * 定位
		 * @param {Object} options : param(参数) callback（回调函数，返回data ）
		 */
		getLocationInfo: function(callback) {
			window.WebViewJavascriptBridge.callHandler(
				'getLocationInfo', {},
				function(responseData) {
					if (callback != null)
						callback(responseData);
				}
			);
		},


		/**
		 * 弹出Dialog
		 * @param {Object} options : param(参数) callback（回调函数，返回data ）
		 */
		showDialog: function(options) {
			var params = options.params;
			var str = "";
			if (params != null) {
				str = typeof(params) == "object" ? JSON.stringify(params) : params + str;
			}
			window.WebViewJavascriptBridge.callHandler(
				'showDialog', {
					"params": str
				},
				function(responseData) {
					if (options.callback != null)
						options.callback(responseData);
				}
			);
		},
		
	}
	window.Idea = idea;
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
