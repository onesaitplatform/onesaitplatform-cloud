var stompClient = null;
var ctxTemp = null;
var ctxHum = null;
var ctxAtm = null;

var optionsTemp = {
	    type: 'line',
	    data: {
	        labels: [],
	        datasets: [
	            {
	                label: "Temperature (degrees)",
	                fillColor: "rgba(220,220,220,0.2)",
	                strokeColor: "rgba(220,220,220,1)",
	                pointColor: "rgba(220,220,220,1)",
	                pointStrokeColor: "#fff",
	                pointHighlightFill: "#fff",
	                pointHighlightStroke: "rgba(220,220,220,1)",
	                data: []
	            }
	        ]
	    },
	    options: {
	        responsive: true
	    }    
	};

var optionsHum = {
	    type: 'line',
	    data: {
	        labels: [],
	        datasets: [
	            {
	                label: "Humidity (%)",
	                fillColor: "rgba(220,220,220,0.2)",
	                strokeColor: "rgba(220,220,220,1)",
	                pointColor: "rgba(220,220,220,1)",
	                pointStrokeColor: "#fff",
	                pointHighlightFill: "#fff",
	                pointHighlightStroke: "rgba(220,220,220,1)",
	                data: []
	            }
	        ]
	    },
	    options: {
	        responsive: true
	    }    
	};

var optionsAtm = {
	    type: 'line',
	    data: {
	        labels: [],
	        datasets: [
	            {
	                label: "Pressure (%)",
	                fillColor: "rgba(220,220,220,0.2)",
	                strokeColor: "rgba(220,220,220,1)",
	                pointColor: "rgba(220,220,220,1)",
	                pointStrokeColor: "#fff",
	                pointHighlightFill: "#fff",
	                pointHighlightStroke: "rgba(220,220,220,1)",
	                data: []
	            }
	        ]
	    },
	    options: {
	        responsive: true
	    }    
	};

// MAIN WHEN READY
$( document ).ready(function() {

	$.ajax({ 
		url: "https://loadbalancerservice-onesait.ocp.52.233.186.149.nip.io/digitaltwinbroker/sensehat/getSensehatDevices", 
		type: 'GET',
		crossDomain: true,
		success: function (data) {			 
			var devices = JSON.parse(data);
			$.each(devices, function(key, object){
				$("#devices").append("<option id='"+object.identification+"' value='"+object.identification+"'>"+object.identification+"</option>");
				$("#digitaltwin_key").val(object.digitalKey);
			});
		}
	});
	
	
	ctxTemp = $("#tempChart").get(0).getContext('2d');
	ctxHum = $("#humChart").get(0).getContext('2d');
	ctxAtm = $("#atmChart").get(0).getContext('2d');
	
});

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
	$("#devices").attr("disabled", "disabled");
    var socket = new SockJS('https://rancher.sofia4cities.com/digitaltwinbroker/websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/api/custom/'+$("#devices").val(), function (notification) {
        	//Joystick events
        	 var obj=JSON.parse(notification.body)
        	 
        	 $("#joystick").val(obj.event);
        	 if(obj.event=="joystickEventUp"){
        		 $("#sendUp").css("background-color", "red");
        		 $("#sendDown").css("background-color", "");
        		 $("#sendLeft").css("background-color", "");
        		 $("#sendRight").css("background-color", "");
        		 $("#up").show();
        		 $("#down").hide();
        		 $("#left").hide();
        		 $("#right").hide();
        	 }if(obj.event=="joystickEventDown"){
        		 $("#sendDown").css("background-color", "red");
        		 $("#sendUp").css("background-color", "");
        		 $("#sendLeft").css("background-color", "");
        		 $("#sendRight").css("background-color", "");
        		 $("#down").show();
        		 $("#up").hide();
        		 $("#left").hide();
        		 $("#right").hide();
        	 }if(obj.event=="joystickEventLeft"){
        		 $("#sendLeft").css("background-color", "red");
        		 $("#sendDown").css("background-color", "");
        		 $("#sendUp").css("background-color", "");
        		 $("#sendRight").css("background-color", "");
        		 $("#left").show();
        		 $("#down").hide();
        		 $("#up").hide();
        		 $("#right").hide();
        	 }if(obj.event=="joystickEventRight"){
        		 $("#sendRight").css("background-color", "red");
        		 $("#sendDown").css("background-color", "");
        		 $("#sendLeft").css("background-color", "");
        		 $("#sendUp").css("background-color", "");
        		 $("#right").show();
        		 $("#down").hide();
        		 $("#left").hide();
        		 $("#up").hide();
        	 }
        });
       stompClient.subscribe('/api/shadow/' + $("#devices").val(), function (notification) {
    	   //Temp/Hum/Atm events
           var obj=JSON.parse(notification.body)
           
           $("#temperature").val(obj.status.temperature);
           $("#humidity").val(obj.status.humidity);
           $("#pressure").val(obj.status.pressure);
           
           var d = new Date();
           if(d.getHours().length==1){
        	   var curr_hour = "0"+d.getHours();
           }else{
        	   var curr_hour = d.getHours();
           }
           
           if(d.getMinutes().length==1){
        	   var curr_min = "0" + d.getMinutes();
           }else{
        	   var curr_min = d.getMinutes();
           }
           
           if(d.getSeconds().length==1){
        	   var curr_sec = "0"+d.getSeconds();
           }else{
        	   var curr_sec = d.getSeconds();
           }
		   
		   var date = curr_hour + ":" + curr_min + ":" + curr_sec;
		   
		   if(optionsHum.data.labels.length==15){
			   optionsHum.data.labels.shift();
			   optionsHum.data.datasets[0].data.shift();
		   }
		   
		   optionsHum.data.labels.push(date);
		   optionsHum.data.datasets[0].data.push(obj.status.humidity);

		   new Chart(ctxHum, optionsHum);
		   
		   if(optionsTemp.data.labels.length==15){
			   optionsTemp.data.labels.shift();
			   optionsTemp.data.datasets[0].data.shift();
		   }
		   
		   optionsTemp.data.labels.push(date);
		   optionsTemp.data.datasets[0].data.push(obj.status.temperature);

		   new Chart(ctxTemp, optionsTemp);
		   
		   if(optionsAtm.data.labels.length==15){
			   optionsAtm.data.labels.shift();
			   optionsAtm.data.datasets[0].data.shift();
		   }
		   
		   optionsAtm.data.labels.push(date);
		   optionsAtm.data.datasets[0].data.push(obj.status.pressure);

		  new Chart(ctxAtm, optionsAtm);
        });
       stompClient.subscribe('/api/action/' + $("#devices").val(), function (notification) {
    	 console.log(notification);
    	 //Joystick events
    	 var obj=JSON.parse(notification.body)
      	 
      	 $("#joystick").val(obj.name);
      	
        });
       
       
    });
}

function disconnect() {
	$("#devices").removeAttr("disabled");
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendCustomLeftEvent() {
	 $("#sendUp").css("background-color", "");
	 $("#sendDown").css("background-color", "");
	 $("#sendLeft").css("background-color", "red");
	 $("#sendRight").css("background-color", "");
	 $("#up").hide();
	 $("#down").hide();
	 $("#left").show();
	 $("#right").hide();
    stompClient.send("/api/sendAction", {'Authorization': $("#digitaltwin_key").val()}, JSON.stringify({'id':$("#devices").val(),'name':'joystickLeft'}));
}

function sendCustomRightEvent() {
	 $("#sendUp").css("background-color", "");
	 $("#sendDown").css("background-color", "");
	 $("#sendLeft").css("background-color", "");
	 $("#sendRight").css("background-color", "red");
	 $("#up").hide();
	 $("#down").hide();
	 $("#left").hide();
	 $("#right").show();
    stompClient.send("/api/sendAction", {'Authorization': $("#digitaltwin_key").val()}, JSON.stringify({'id':$("#devices").val(),'name':'joystickRight'}));
}

function sendCustomUpEvent() {
	 $("#sendUp").css("background-color", "red");
	 $("#sendDown").css("background-color", "");
	 $("#sendLeft").css("background-color", "");
	 $("#sendRight").css("background-color", "");
	 $("#up").show();
	 $("#down").hide();
	 $("#left").hide();
	 $("#right").hide();
    stompClient.send("/api/sendAction", {'Authorization': $("#digitaltwin_key").val()}, JSON.stringify({'id':$("#devices").val(),'name':'joystickUp'}));
}

function sendCustomDownEvent() {
	 $("#up").hide();
	 $("#down").show();
	 $("#left").hide();
	 $("#right").hide();
	 $("#sendUp").css("background-color", "");
	 $("#sendDown").css("background-color", "red");
	 $("#sendLeft").css("background-color", "");
	 $("#sendRight").css("background-color", "");
    stompClient.send("/api/sendAction", {'Authorization': $("#digitaltwin_key").val()}, JSON.stringify({'id':$("#devices").val(),'name':'joystickDown'}));
}


$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#sendLeft" ).click(function() { sendCustomLeftEvent(); });
    $( "#sendRight" ).click(function() { sendCustomRightEvent(); });
    $( "#sendUp" ).click(function() { sendCustomUpEvent(); });
    $( "#sendDown" ).click(function() { sendCustomDownEvent(); });
    
});

