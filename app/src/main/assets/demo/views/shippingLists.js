var noteNumber = document.getElementById('note_number').innerText;
var serialNumber = document.getElementById('serial_number').innerText;
var startCode = document.getElementById('start_code').innerText;
var endCode = document.getElementById('end_code').innerText;

var shipping = function() {
	return {
		startDispatch: function() {
		    window.WebViewJavascriptBridge.callHandler(
              'startTransport',
              {
                'noteNumber' : noteNumber,    // 运单号
                'serialNumber' : serialNumber,// 分单号
                'startCode' : startCode,      // 启运区域代码
                'endCode' : endCode           // 到达区域代码
              },
              function(responseData){

              });
		},

        endDispatch: function() {
            window.WebViewJavascriptBridge.callHandler(
              'stopTransport',
              {
                'noteNumber' : noteNumber,
                'serialNumber' : serialNumber
              },
              function(responseData){

              });
        }
	}
}();

$(function() {
    JsBridge.init(function() {});
});