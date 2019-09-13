

var GenericFunctions = function() {

	// DEFAULT PARAMETERS, VAR, CONSTS.
	var APPNAME = 'Onesait Platform Control Panel', LIB_TITLE = 'Generic Functions', logControl = 0;

	//Read  query and get a ontology 
	var getOntologyFromQuery = function (query){			
			query = query.replace(/(\r\n\t|\n|\r\t)/gm," ");
			query = query.replace(/  +/g, ' ');
			var list = query.split("from ");
			if (list.length === 1){
				list = query.split("FROM ");
			}
			if(list.length>1){
				for (var i=1; i< list.length;i++){
					if(!list[i].startsWith("(")){
						var indexOf = list[i].toLowerCase().indexOf(" ",0);
						var indexOfCloseBracket = list[i].toLowerCase().indexOf(")",0);
						indexOf = (indexOfCloseBracket != -1 && indexOfCloseBracket < indexOf)?indexOfCloseBracket:indexOf;
						if(indexOf == -1) {
							indexOf = list[i].length;
						}
						return  list[i].substring(0, indexOf).trim();
					}
				}
			}else{ 
				return "";
			}
		}

	// CONTROLLER PUBLIC FUNCTIONS
	return {		
		getOntologyFromQuery : function(query) {
			logControl ? console.log(LIB_TITLE + ': getOntologyFromQuery()') : '';
			return  getOntologyFromQuery(query);
		},

	
	};
}();




