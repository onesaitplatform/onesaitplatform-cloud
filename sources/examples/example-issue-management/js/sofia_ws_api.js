function SofiaClient() {
			
		var _this = this;
		var config = null;
		var stompClient;
		var sessionKey;
		var queuePromises;
		var version = "";
		var status;
		
		
		this.configure = function (config) {
			_this.config = config;
		}
		
		this.onConnect = function (frame, callback) {
			console.log('Connected: ' + frame);
			_this.status = 'CONNECTED';
			_this.join(callback);
		}
		
		this.onError = function (error) {
			console.log('error: ' + error);
		}
		
		this.connect = function (callback) {
		
			if (this.config === null) {
				throw Error('Configuration required!');
			}
			
			_this.status = 'CONNECTING';
			_this.queuePromises = [];
			
			var dfd = $.Deferred();
			
			var socket = new SockJS(this.config.url);
			
			this.stompClient = Stomp.over(socket);
			
			if (this.config.debug) {
				socket.debug = function(str) {
					console.log(str);
				};
				
				this.stompClient.debug = function (str) {
					console.log(str);
				};
				
			}else {
				this.stompClient.debug = false;
			}
			
			this.stompClient.connect({}, 
					function (frame) {	
						dfd.resolve(_this.onConnect(frame,callback));
					}, 
					function (err) {
						dfd.reject(_this.onError(err));
						
					}
			);

			return dfd.promise();
		}
		
		var parseResponse = function (response, callback) {
		
			response.body = response.body.replace(/"{/g, '{');
			response.body = response.body.replace(/}\"/g, '}');
			response.body = response.body.replace(/\\"/g, '"');
			
			body = JSON.parse(response.body);
			
			if(body.messageType =="JOIN"){
				sessionKey = body.sessionKey;
			}
			
			if(callback)
			  return (callback(body));
			else
			  return body;
			  
			susbcription.unsubscribe();  
		}
		
		var escapeDoubleQuotes = function (str) {
			return str.replace(/\\([\s\S])|(")/g,"\\$1$2");
		}
		
		var sendMessage = function (message, callback) {
		
			var UUID = (new Date()).getTime();
			
			var dfd = $.Deferred();
			
			var susbcription = _this.stompClient.subscribe('/topic/message/' + UUID, function(response) {					
					dfd.resolve(parseResponse(response,callback));
			});

			_this.stompClient.send("/stomp/message/" + UUID, {}, message);
			
			return dfd.promise();	
		}
		
		this.join = function (callback) {
		
			messageId = "";
			var joinMessage = '{'
							+ '"messageId":"' 
							+ messageId
							+ '","sessionKey":' 
							+ ((this.sessionKey != null)?'"' + this.sessionKey + '"': null)
							+ ',"direction":"REQUEST","messageType":"JOIN"'
							+ ',"body": {'
							+ '"@type": "SSAPBodyJoinMessage",'
							+ '"token":"'
							+ this.config.token
							+ '","deviceTemplate":"'
							+ this.config.deviceTemplate
							+ '","device":"'
							+ this.config.device
							+ '"'
							+ '}'
							+ '}';
			
			return sendMessage(joinMessage, callback);
			
		};
		
		this.leave = function () {
		
			if(stompClient != null) {
				stompClient.disconnect();
			}
			//setConnected(false);
			console.log("Disconnected");
		};
		
		this.query = function (ontology, query, queryType, callback) {
			messageId = "";
			var queryMessage = '{'
							+ '"messageId":"' 
							+ messageId
							+ '","sessionKey":' 
							+ ((sessionKey != null)?'"' + sessionKey + '"': null)
							+ ',"direction":"REQUEST","messageType":"QUERY"'
							+ ',"body": {'
							+ '"@type": "SSAPBodyQueryMessage",'
							+ '"ontology":"'
							+ ontology
							+ '","query":"'
							+ escapeDoubleQuotes(query)
							+ '","queryType":"'
							+ queryType
							+ '"'
							+ '}'
							+ '}';
											
			return sendMessage(queryMessage, callback);
			
		}
		
		this.insert = function (ontology, data, callback) {
			messageId = "";
			var insertMessage = '{'
							+ '"messageId":"' 
							+ messageId
							+ '","sessionKey":' 
							+ ((sessionKey != null)?'"' + sessionKey + '"': null)
							+ ',"direction":"REQUEST","messageType":"INSERT"'
							+ ',"body": {'
							+ '"@type": "SSAPBodyInsertMessage",'
							+ '"ontology":"'
							+ ontology
							+ '","data":'
							+ data
							+ ''
							+ '}'
							+ '}';
											
			return sendMessage(insertMessage, callback);
		};
		
		this.update = function (ontology, query, callback) {
		
			messageId = "";
			var updateMessage = '{'
								+ '"messageId":"' 
								+ messageId
								+ '","sessionKey":' 
								+ ((sessionKey != null)?'"' + sessionKey + '"': null)
								+ ',"direction":"REQUEST","messageType":"UPDATE"'
								+ ',"body": {'
								+ '"@type": "SSAPBodyUpdateMessage",'
								+ '"ontology":"'
								+ ontology
								+ '","query":"'
								+ escapeDoubleQuotes(query)
								+ '"}'
								+ '}';
							
						
			return sendMessage(updateMessage, callback);
			
		};
		
		this.updateById = function (ontology, id, data, callback) {

			messageId = "";
			var updateMessage = '{'
								+ '"messageId":"' 
								+ messageId
								+ '","sessionKey":' 
								+ ((sessionKey != null)?'"' + sessionKey + '"': null)
								+ ',"direction":"REQUEST","messageType":"UPDATE_BY_ID"'
								+ ',"body": {'
								+ '"@type": "SSAPBodyUpdateByIdMessage",'
								+ '"ontology":"'
								+ ontology
								+ '","id":"'
								+ id
								+ '","data":'
								+ data
								+ ''
								+ '}'
								+ '}';
															
			return sendMessage(updateMessage, callback);
		};
		
		this.remove = function (ontology, query, callback) {
			
			messageId = "";
			var deleteMessage = '{'
								+ '"messageId":"' 
								+ messageId
								+ '","sessionKey":' 
								+ ((sessionKey != null)?'"' + sessionKey + '"': null)
								+ ',"direction":"REQUEST","messageType":"DELETE"'
								+ ',"body": {'
								+ '"@type": "SSAPBodyDeleteMessage",'
								+ '"ontology":"'
								+ ontology
								+ '","query":"'
								+ escapeDoubleQuotes(query)
								+ '"'
								+ ''
								+ '}'
								+ '}';
															
			return sendMessage(deleteMessage, callback);
		};
		
		this.removeById = function (ontology, id, callback) {
			
			messageId = "";
			var deleteMessage = '{'
								+ '"messageId":"' 
								+ messageId
								+ '","sessionKey":' 
								+ ((sessionKey != null)?'"' + sessionKey + '"': null)
								+ ',"direction":"REQUEST","messageType":"DELETE_BY_ID"'
								+ ',"body": {'
								+ '"@type": "SSAPBodyDeleteByIdMessage",'
								+ '"ontology":"'
								+ ontology
								+ '","id":"'
								+ id
								+ '"'
								+ '}'
								+ '}';
															
			return sendMessage(deleteMessage, callback);
		};
		
}
			