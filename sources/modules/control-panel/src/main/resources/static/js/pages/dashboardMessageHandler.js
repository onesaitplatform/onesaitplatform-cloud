var eventMethod = window.addEventListener ? "addEventListener": "attachEvent";
var eventer = window[eventMethod];
var messageEvent = eventMethod === "attachEvent" ? "onmessage" : "message";

eventer(messageEvent, function (e) {

  
    var message;
    var messageString;
 
    
    if(typeof e.data != "undefined"){ 
    	try {
    		if(typeof e.data !=="object"){
    			message = JSON.parse(e.data);
    			messageString = e.data;
    		}else{
    			message = e.data ;
    			messageString = JSON.stringify(e.data);
    		}    	
		} catch (e) {			
		}    
    }else if (typeof e.message != "undefined"){
    	console.log('handlerMessage e.message ',e.message);    	
    	try {
    		if(typeof e.message !=="object"){
    			message = JSON.parse(e.message) ;
    			messageString = e.message;
    		}else{
    			message = e.message ;
    			messageString = JSON.stringify(e.message);
    		}
		} catch (e) {			
		}
    }
   
    //handler editGadgetFrom when push tool CLOSE GADGET
    if (typeof messageString !=="undefined" && (messageString.indexOf('closeGadgetFromIframe') >= 0 || messageString.indexOf('closecreateGadgetFromIframe') >=0  )){
        console.log(angular.element( document.querySelector( '#dialogCreateGadget' ) ).scope());    
        angular.element( document.querySelector( '#dialogCreateGadget' ) ).scope().closeDialog();        
        return ;
    } 
	//handler editGadgetFrom when push tool CREATE GADGET	
	if(typeof messageString !=="undefined" && message!=null && messageString.indexOf('createGadgetFromIframe') >= 0){
	    console.log(message);            
	    angular.element( document.querySelector( '#dialogCreateGadget' ) ).scope().addGadgetFromIframe(message.createGadgetFromIframe.data.type,message.createGadgetFromIframe.data.id,message.createGadgetFromIframe.data.identification);          
	}    

	//handler external commands

	// {"command":"newGadget",
	// "information":{"dashboard":"identificationDashboard","dashboardStyle":"notitle"
	// "gadgetType":"trend", "refresh":20, "ontology":"ontologyTest",
	// "nameX":"","nameY":"","measuresY":[{"name":"a","path":"ontologyTest.ontologyTest.a"},{"name":"b","path":"ontologyTest.ontologyTest.b"}],
	// "measuresX":[{"name":"time","path":"ontologyTest.ontologyTest.time"}]}}
	
	

    if (typeof messageString!=='undefined' && messageString.indexOf('command') >= 0){
    	if(message.command === "newGadget"){    	
    		apiCreateGadget(messageString);
    		return ;
    	}else if(message.command === "saveDashboard"){    	
    		apiSaveDashboard(messageString);
    		return ;
    	}else if(message.command === "dropOnElement"){    	
    		dropOnElement(messageString);
    		return ;
    	}else if(message.command === "updateGadget"){    	
    		apiUpdateGadget(messageString);
    		return ;
    	}
    } 
    
    
    }); 
     
       //Functions 
    
       function apiCreateGadget(commandData){
    	    var command= JSON.parse(commandData)
      		var url = '/controlpanel/dashboardapi/createGadget/';   
      		var token =    'Bearer '+ command.authorization;
      		 console.log('apiCreateGadget');
		 $.ajax({
           'url':url,
           'type': 'POST',
           'headers': {
               'Authorization':token            	  
           },        
           'data': {
  			json : commandData },
         
           'success': function(data) {
        	   console.log('successCreateGadget: ',data);
               if(data.status!=null && data.status === "OK"){
            	    console.log('apiCreateGadget: ',data);        	  
            	    angular.element(document.querySelector("dashboard")).controller("dashboard").api.createGadget(data.type,data.id,command.information.gadgetName,data.gadgetTemplate,data.gadgetDatasource,data.filters,data.customMenuOptions,data.setupLayout);          	
              		sendMessageParent(data);               		
               }else{
            		console.log("success apiCreateGadget not created"); 
            		sendMessageParent(data);
               }
           },
           'error': function(data,status,er) {  
        	   console.log('errorCreateGadget: ', data);
        	   sendMessageParent(data);
          	
           }
		 });
       }    
   

     
       
       
       function apiUpdateGadget(commandData){
   	    var command= JSON.parse(commandData)
     		var url = '/controlpanel/dashboardapi/updateGadget/';   
     		var token =    'Bearer '+ command.authorization;
     		 console.log('apiEditGadget'); 
		 $.ajax({
          'url':url,
          'type': 'POST',
          'headers': {
              'Authorization':token            	  
          },        
          'data': {
 			json : commandData },        
          'success': function(data) {
       	   console.log('successUpdateGadget: ',data);
       	   if(data.status!=null && data.status === "Template"){
       		    angular.element(document.querySelector("dashboard")).controller("dashboard").api.updateFilterGadget(command.information.gadgetId,data.filters,data.customMenuOptions,data.merge);
       		    data.status = "OK";
       		    sendMessageParent(data);
       	   	  } else if(data.status!=null && data.status === "OK"){
            	    var response = angular.element(document.querySelector("dashboard")).controller("dashboard").api.refreshGadgets();
              		console.log("success apiUpdateGadget data ok");              		
              		sendMessageParent(data);
              }else{
           			console.log("success apiUpdateGadget not created"); 
           			sendMessageParent(data);
              }
          },
          'error': function(data,status,er) {  
       	   console.log('errorUpdateGadget: ', data);
       	   sendMessageParent(data);
         	
          }
		 });
      }    
  

       function dropOnElement(commandData){
    	    var command= JSON.parse(commandData)
      	
      		console.log('dropOnElement');
    	    console.log(commandData);
      		var response = angular.element(document.querySelector("dashboard")).controller("dashboard").api.dropOnElement(command.information.x,command.information.y); 
      		console.log(response);
      		sendMessageParent(response)
       }    
   
       
       function apiSaveDashboard(commandData){  
    	  console.log('apiSaveDashboard');
    	  var command= JSON.parse(commandData)  
    	  var data =  angular.element(document.querySelector("edit-dashboard")).controller("edit-dashboard").getDataToSavePage();  
    	  var url = '/controlpanel/dashboardapi/savemodel/'+data.id;
    	  var token = 'Bearer '+ command.authorization;
    	  $.ajax({
              'url':url,
              'type': 'PUT',
              'headers': {
                  'Authorization':token               	  
              },        
              'data':{json:data.data.model},            
              'success': function(data) {
            	  console.log('successSaveDashboard: ', data);
            	  angular.element(document.querySelector("edit-dashboard")).controller("edit-dashboard").showSaveOK();
                  
              },
              'error': function(data,status,er) {  
              console.log('errorSaveDashboard: ', data);
           	   sendMessageParent(data);             	
              }
   		 });
        }
     
       function inIframe () {
    	    try {
    	        return window.self !== window.top;
    	    } catch (e) {
    	        return true;
    	    }
    	}
       function sendMessageParent (t){
    	   if(inIframe ()){
    		   parent.postMessage(t,"*");
    	   }
       }
          