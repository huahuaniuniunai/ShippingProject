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
                'noteNumber' : noteNumber,
                'serialNumber' : serialNumber,
                'startCode' : startCode,
                'endCode' : endCode
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