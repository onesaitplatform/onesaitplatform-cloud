var fromTriggerToMessage = function(trigger, dsname) {
	var baseMsg = trigger.params;
	baseMsg.ds = dsname;
	return baseMsg;
}

var datasourceCache = {};
datasourceCache.values = [];
datasourceCache.exist = function(key) {
	if (datasourceCache.values.length == 0) {
		return false;
	}
	 
	if (datasourceCache.values.find(element => element.key == key)) {
		return true;
	}
	return false;
}
datasourceCache.put = function(key, values) {
	if(!datasourceCache.exist(key)) {
		datasourceCache.values.push({ key: key, value: values });
	}
}
datasourceCache.get = function(key) {
	if (datasourceCache.values.length == 0) {		return [];
	}
	 
	var elem = datasourceCache.values.find(element => element.key == key)
	if (elem) {
		return elem.value;
	}
	return [];
}
datasourceCache.getKey = function(datasourcename, triggers){
	return JSON.stringify(datasourcename) + JSON.stringify(triggers);
}

var getdatasource = function(datasourcename, triggers, type) {
    if (undefined == type || 'datasource' == type) {
	  type = 'datasource';
    }else{
	  type = 'entity';
	}
	var key = datasourceCache.getKey(datasourcename, triggers);
	key = type + key;	
	if (window.useCache && datasourceCache.exist(key)) {
		return new Promise(function(resolve) {
			resolve(datasourceCache.get(key));
		});

	}
	else {
		return new Promise(function(resolve) {			
			var headers = {
					'accept': '*/*',					 
					'Content-Type': 'application/json'
				}
			if(window.authorization && window.authorization.token_type == 'bearer' ){
		    		headers['Authorization'] = 'Bearer '+window.authorization.access_token
			}
			let service = type=='datasource'?'/datasource/':'/entitydata/';
			 
			fetch(window.formsBaseURLCreate + service, {
			  method: "POST",
			  body: JSON.stringify(fromTriggerToMessage({ "params": triggers ? triggers : {} }, datasourcename)),
			  headers: headers
			})
			  .then((msg) => {				  
				  if(msg.status==200){
					  msg.json().then((json) => {
						  if(window.useCache) {
						  	datasourceCache.put(key, json);
						  }
						  resolve(json);
					  });
					
				}else{
					resolve([]);
				}			
			  })	
		});
	}
}





var from = function(datasource,type) {
	var datasourceCallBuilder = {
		datasource: datasource,
		type: type,
		params: {},
		filter: function(field_filters, value, op) {
			var filterList;
			if (Array.isArray(field_filters)) {
				filterList = field_filters.map(function(filter) { filter.op = (filter.op ? filter.op : "="); return filter });
			}
			else {
				filterList = [{ "field": field_filters, "op": op ? op : "=", "exp": value }]
			}
			this.params.filter = (this.params.filter || []).concat(filterList)
			return this;
		},
		skip: function(skip) {
			this.params.offset = skip;
			return this;
		},
		limit: function(limit) {
			this.params.limit = limit;
			return this;
		},
		group: function(group_groups) {
			var groupList;
			if (Array.isArray(group_groups)) {
				groupList = group_groups;
			}
			else {
				groupList = [group_groups]
			}
			this.params.group = (this.params.group || []).concat(groupList)
			return this;
		},
		project: function(field_projects, alias, op) {
			var projectList;
			if (Array.isArray(field_projects)) {
				projectList = field_projects;
			}
			else {
				var project = { "field": field_projects };
				if (alias) {
					project.alias = alias
				}
				else {
					project.alias = field_projects
				}
				if (op) {
					project.op = op
				}
				projectList = [project]
			}
			this.params.project = (this.params.project || []).concat(projectList)
			return this;
		},
		sort: function(field_sorts, asc) {
			var sortList;
			if (Array.isArray(field_sorts)) {
				sortList = field_sorts;
			}
			else {
				var sort = { "field": field_sorts };
				if (asc) {
					sort.asc = asc;
				}
				else {
					sort.asc = true;
				}
				sortList = [sort]
			}
			this.params.sort = (this.params.sort || []).concat(sortList)
			return this;
		},
		param: function(field_params, value) {
			var paramList;
			if (Array.isArray(field_params)) {
				paramList = field_params;
			}
			else {
				var param = { "field": field_params, "param": param };
				paramList = [param]
			}
			this.params.param = (this.params.param || []).concat(paramList)
			return this;
		},
		execute: function() {
			return getdatasource(this.datasource, this.params, this.type);
		}
	}
	//aliases
	datasourceCallBuilder.where = datasourceCallBuilder.filter;
	datasourceCallBuilder.offset = datasourceCallBuilder.skip;
	datasourceCallBuilder.max = datasourceCallBuilder.limit;
	datasourceCallBuilder.select = datasourceCallBuilder.project;
	datasourceCallBuilder.exec = datasourceCallBuilder.execute;

	return datasourceCallBuilder;
}
//delete record by form and oid
var deleteRecordFromForm = function(form,oid,title,message) {
	if(document.getElementById("delete-modal")){
		document.getElementById("delete-modal").remove()
	}
	if(!form){
		form = formId;
	}
	
	var body = document.body;
	
	var modal = `<div id="delete-modal" style="padding:20px;width:400px;height:200px;position:fixed;left:40%;top:20%;background-color:white; box-shadow: 0 2px 6px rgba(0,0,0,0.2); border-radius: 10px; border-top-left-radius: 10px; border-top-right-radius: 10px; border-bottom-right-radius: 10px; border-bottom-left-radius: 10px;" >
	<label style="font-size:17px;font-weight:500;" >${title}</label></br>
	<label style="font-size:12px;font-stretch:100%;font-style:normal;font-weight:400">${message}</label></br>	
	<label id="errordeleteMessage"></label></br>	
	<button id="cancelDelete" class="btn btn-secondary " style="margin: 5px 0px 5px 5px;text-transform: capitalize;position: absolute;bottom: 20px;right: 92px;" onclick="document.getElementById('delete-modal').remove()">Cancel</button>	
	<button id="okDelete" class="btn btn-primary " style="margin: 5px 0px 5px 5px;text-transform: capitalize;position: absolute;bottom: 20px;right: 20px;" onclick="okDeleteRecordFromForm('${form}','${oid}')">${title}</button> 
	</div>
	`
	body.insertAdjacentHTML("afterend", modal); 
	
}


//delete record by form and oid
var okDeleteRecordFromForm = function(form,oid) {
	if(!form){
		form = formId;
	}
	var headers = {
		'accept': '*/*',
		'Content-Type': 'application/json'
	}
	if (window.authorization && window.authorization.token_type == 'bearer') {
		headers['Authorization'] = 'Bearer ' + window.authorization.access_token
	}	 
	fetch(`${window.formsBaseURLCreate}/${form}/${oid}`, {
		method: "DELETE",
		body: null,
		headers: headers
	}).then((msg) => {
			if (msg.status == 200) {
				    document.getElementById('delete-modal').remove();
					window.listThat.redraw(); 
			} else {
				    document.getElementById("errordeleteMessage").style.backgroundColor = "#f8d7da";
					document.getElementById("errordeleteMessage").innerHTML = 'A '+ msg.status+' error has occurred ' ;
					console.log(msg.statusText);
			}
		})
}


var deletePropoertyPath = function (obj, path) {
  if (!obj || !path) {
    return;
  }
  if (typeof path === 'string') {
    path = path.split('.');
  }
  for (var i = 0; i < path.length - 1; i++) {
    obj = obj[path[i]];
    if (typeof obj === 'undefined') {
      return;
    }
  }
  delete obj[path.pop()];
};

var cleanFormForSubmit = function (ff,submission){
	if(ff.components && ff.components.length > 0 ) {
		for(var i = 0;i < ff.components.length ;i ++){
			//delete buttons values
			if(ff.components[i].component && ff.components[i].component.type ==='button'){
				deletePropoertyPath(submission, ff.components[i].component.key);
			}
		}		
	}
	return submission;	
}

//clone record by form and oid
var cloneRecordFromForm = function(form,oid,cloneIdentification,title,message) {
	if(document.getElementById("clone-modal")){
		document.getElementById("clone-modal").remove()
	}
	if(!form){
		form = formId;
	}
	
	var body = document.body;
	
	var modal = `<div id="clone-modal" style="padding:20px;width:400px;height:200px;position:fixed;left:40%;top:20%;background-color:white; box-shadow: 0 2px 6px rgba(0,0,0,0.2); border-radius: 10px; border-top-left-radius: 10px; border-top-right-radius: 10px; border-bottom-right-radius: 10px; border-bottom-left-radius: 10px;" >
	<label style="font-size:17px;font-weight:500;" >${title}</label></br>
	<label style="font-size:12px;font-stretch:100%;font-style:normal;font-weight:400">${message}</label></br>
	<label style="font-size:12px;font-stretch:100%;font-style:normal;font-weight:400">${cloneIdentification}<span style="color:red"> *</span></label>
	<input class="form-control" type="text" id="newIdentifierClone" >
	<label id="errorcloneMessage"></label></br>	
	<button id="cancelClone" class="btn btn-secondary "style="margin: 5px 0px 5px 5px;text-transform: capitalize;position: absolute;bottom: 20px;right: 84px;" onclick="document.getElementById('clone-modal').remove()">Cancel</button>	
	<button id="okClone" class="btn btn-primary pull-right" style="margin: 5px 0px 5px 5px;text-transform: capitalize;position: absolute;bottom: 20px;right: 20px;" onclick="okCloneRecordFromForm('${form}','${oid}','${cloneIdentification}')">Save</button> 
	</div>
	`
	body.insertAdjacentHTML("afterend", modal); 
	
	
}

var okCloneRecordFromForm = function(form, oid, cloneIdentification) {

	var newValue = document.getElementById("newIdentifierClone").value
	if (!newValue || newValue.length == 0) {		 
		document.getElementById("newIdentifierClone").style.backgroundColor = "#f8d7da";
	} else {
		document.getElementById("newIdentifierClone").style.backgroundColor = "white";
		var headers = {
			'accept': '*/*',
			'Content-Type': 'application/json'
		}
		if (window.authorization && window.authorization.token_type == 'bearer') {
			headers['Authorization'] = 'Bearer ' + window.authorization.access_token
		}
		var body=JSON.stringify({form:form,oid:oid,cloneIdentification:cloneIdentification,newValue:newValue})
		fetch(`${window.formsBaseURLCreate}/clonevalue`, {
			method: "POST",
			body: body,
			headers: headers
		}).then((msg) => {
				if (msg.status == 200) {
					document.getElementById('clone-modal').remove();
					window.listThat.redraw();
				} else {
					document.getElementById("errorcloneMessage").style.backgroundColor = "#f8d7da";
					document.getElementById("errorcloneMessage").innerHTML = 'A '+ msg.status+' error has occurred ' ;
					console.log(msg.statusText);
				}
			})
	}

}