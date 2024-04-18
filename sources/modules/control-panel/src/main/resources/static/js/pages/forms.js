
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

var getdatasource = function(datasourcename, triggers) {
	
	var key = datasourceCache.getKey(datasourcename, triggers);
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
			 
			fetch(window.formsBaseURLCreate + '/datasource/', {
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





var from = function(datasource) {
	var datasourceCallBuilder = {
		datasource: datasource,
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
			return getdatasource(this.datasource, this.params);
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
var deleteRecordFromForm = function(oid,form) {
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
	 
	fetch(`${window.formsBaseURLCreate}${form}/${oid}`, {
		method: "DELETE",
		body: null,
		headers: headers
	})
		.then((msg) => {
			if (msg.status == 200) {
					ff.redraw(); 
			} else {
				console.log(msg.status);
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

