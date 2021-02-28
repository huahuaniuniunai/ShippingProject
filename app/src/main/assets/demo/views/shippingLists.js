var noteNumber = document.getElementById('note_number').innerText;
var serialNumber = document.getElementById('serial_number').innerText;
var startCode = document.getElementById('start_code').innerText;
var endCode = document.getElementById('end_code').innerText;

var shipping = function() {
	return {
	    scan: function() {
            window.WebViewJavascriptBridge.callHandler(
              'scanCode',
              {},
              function(responseData){
                    console.log(responseData);
                    var noteNumber = JSON.parse(responseData).noteNumber;
                    var serialNumber = JSON.parse(responseData).serialNumber;
                    var startCode = JSON.parse(responseData).startCode;
                    var endCode = JSON.parse(responseData).endCode;
                    document.getElementById('note_number').innerText = noteNumber;
                    document.getElementById('serial_number').innerText = serialNumber;
                    document.getElementById('start_code').innerText = startCode;
                    document.getElementById('end_code').innerText = endCode;
              });
	    },
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
    Idea.init(function() {});
});