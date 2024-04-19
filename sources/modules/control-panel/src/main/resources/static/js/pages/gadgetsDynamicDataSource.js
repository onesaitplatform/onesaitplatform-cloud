
		var valueObjects = [];
		var queryResponse = {};
		var queryErrorResponse = {};
		var dataJSON = {};
		function getQueryExecutedFragment() {	
			var ontologyIdentification = $("#datasources").val();
			if(typeof dataSourceForEdit!== "undefined" && dataSourceForEdit!=null ){
				if (dataSourceForEdit.hasOwnProperty("ontology") && dataSourceForEdit.ontology != null && dataSourceForEdit.ontology.hasOwnProperty("identification") && dataSourceForEdit.ontology.identification != null){
					ontologyIdentification=dataSourceForEdit.ontology.identification;
				}
				else{
					ontologyIdentification = getOntologyFromQuery(dataSourceForEdit.query);
				}
			}
			var csrf_value = $("meta[name='_csrf']").attr("content");
			var csrf_header = $("meta[name='_csrf_header']").attr("content");
			
			$.ajaxSetup({'headers': {
				[csrf_header]: csrf_value
		    }});
			$("#Canvasrespuesta").load('/controlpanel/querytool/query', { 'query': $("#querySql").val(), 'queryType': $("#typeQuery").val(), 'ontologyIdentification': ontologyIdentification},function(response,status,xhr){
			
				
				document.getElementById("historicalQuery").value = null; 
				queryResponse = $(response);	
				debugger
				// check for valid json, or server string error like java.lang.Exception...
				var IS_JSON = true;
				try{ 
					var json = $.parseJSON(queryResponse.text());
					/*if(json.length>0){
						$('#createDataSourceButton').show();	
					}else{
						$('#createDataSourceButton').hide();
					}*/
				} catch(err){ IS_JSON = false; }
				
				if (!IS_JSON){					
					var text=""+queryResponse.text();
					// Our own JSON string to mark non JSON ERRORs
					queryErrorResponse = text;	
					//$('#createDataSourceButton').hide();
				}
				
				if ($('#jsoneditor').attr('data-loaded') == 'false') { createEditor(); $('#jsoneditor').attr('data-loaded', true);	}				
				if (IS_JSON) { 
					dataJSON = queryResponse.text(); 
					editor.setText(queryResponse.text()); 
					editor.setMode('view');
					if ($('.table-viewer').is(':visible')){ $('.btn-table-toggle').trigger('click'); } }else { editor.setMode('text');  editor.setText(queryErrorResponse); } 
						
			});		
			
			
			$('#query-ontologia-result').text('' + ontologyIdentification);		
			if ($('#result-panel').hasClass('hide')){ $('#result-panel').toggleClass('hide'); } 			
		}
	
		
		function getRtdbFromOntology(){
			var csrf_value = $("meta[name='_csrf']").attr("content");
			var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
			var datasour ="";
			if(typeof dataSourceForEdit !='undefined' && typeof dataSourceForEdit.ontology !='undefined' &&  dataSourceForEdit.ontology != null ){
				datasour = dataSourceForEdit.ontology.identification;
			}else{
				datasour = $("#datasources").val();
			}			
			
			$.ajax({ url: "/controlpanel/querytool/rtdb/" + datasour, 
				headers: {
					[csrf_header]: csrf_value
			    },
				type: 'GET', 
				contentType: 'text/html',
				async : false,
				success: function (result) {	
					bdtrType=result;
					//ERROR 
						$("#typeQuery").removeAttr("disabled");
					/*
					if(rtdb!="Mongo"){
						$("#typeQuery").val("native");
						$("#typeQuery").attr("disabled", "disabled");
					}else{
						$("#typeQuery").removeAttr("disabled");
					}	
					*/
				},
				error: function(error){
					alert(error);
				}
			});
		}
		
	
	
		
		function getOntologyFields() {
			var ontologyIdentification = $("#datasources").val();
			if(typeof dataSourceForEdit!== "undefined" && dataSourceForEdit!=null ){
				
				
				if (dataSourceForEdit.hasOwnProperty("ontology.identification")){
						if ( dataSourceForEdit.ontology.identification !== ''){
							ontologyIdentification = dataSourceForEdit.ontology.identification;
						} else {
							myOntology = getOntologyFromQuery(dataSourceForEdit.query);
							ontologyIdentification = myOntology;							
						}
				}
				else {
					myOntology = getOntologyFromQuery(dataSourceForEdit.query);
					ontologyIdentification = myOntology;		
				}
			}
			var csrf_value = $("meta[name='_csrf']").attr("content");
			var csrf_header = $("meta[name='_csrf_header']").attr("content");
			
			$.ajaxSetup({'headers': {
				[csrf_header]: csrf_value
		    }});
			$("#fields").load('/controlpanel/querytool/ontologyfields', { 'ontologyIdentification': ontologyIdentification})
		}		
		
		
		function showData(key,str){					
			$('#jsonDatatTitle').text(str);
			// WALK THE ARRAY AND RETURN .JSON PROPERTY OF THE OBJECT WICH KEY IS THE KEY I WANT TO FIND.
			var str = valueObjects.find(x => x.key === key).json;
			if (str === 'null') { return false; }
			jsonToModal = syntaxHighlight(str);
			document.getElementById("jsonDataView").innerHTML = jsonToModal;
			$('#jsonModal').modal();
	
		};
		
		function syntaxHighlight(json) {
			json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
			return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
				var cls = 'number';
				if (/^"/.test(match)) {
					if (/:$/.test(match)) {
						cls = 'key';
					} else {
						cls = 'string';
					}
				} else if (/true|false/.test(match)) {
					cls = 'boolean';
				} else if (/null/.test(match)) {
					cls = 'null';
				}
				return '<span class="' + cls + '">' + match + '</span>';
			});
		};
	
		
		var showLog = 1; // GLOBAL VAR FOR SHOW CONSOLE LOG
		var bdtrType, bdtrDWR, haspermission; 
		bdtrType = 'rtdb';
		
		var queryKp = gadgetQueryToolJson.query;
		console.log('changeQuery() -> queryKp -> '+ queryKp);
		
				

		//SHOW / HIDE THE INTERNAL CONTEXT DATA FROM THE RECORDS IN THE TABLE.
		function toggleContext(){
			icon = $('#btnContextToggle').children('i');
			if (icon.hasClass('fa fa-eye-slash')) { icon.removeClass('fa fa-eye-slash').addClass('fa fa-eye'); } else { icon.removeClass('fa fa-eye').addClass('fa fa-eye-slash'); }
			$('.context').toggle('hide');
	
		}		
		
		// UN A-HREF
		function navigateUrl(url){  window.location.href = url; }
	   
	   //	FUNCTION THAT INITIALIZES THE ASSEMBLY PANELS OF THE CONSULTATION [SELECT, WHERE, GROUP, ORDERBY]
	   function changeQuery(){
			getRtdbFromOntology();
			//Load fields 
			getOntologyFields();			

			var base_ontology = 'Sensor';
		
			// All panels are deleted with the query change.
			var queryPanels = $("#where_ul,#select_ul,#from_ul,#groupBy_ul,#checks,#campos,#orderby_ul");
			queryPanels.each(function(){ $(this).children().not('.panel-sofia2').remove(); });
			
			// IF WE ALREADY HAVE SELECTED A WHERE, WE DO NOT REMOVE IT SIMPLY WE ERASE EVERYTHING, TO ASSEMBLE THE NEW CONSULTATION, BUT
			// LET THE WHERE.
			var ontology = $('#datasources').val();
			
			if(ontology== null || ontology === ""){
				if (dataSourceForEdit.hasOwnProperty("ontology.identification")){
					ontology = dataSourceForEdit.ontology.identification;
				}
				else {
					ontology = getOntologyFromQuery(dataSourceForEdit.query);				
				}
				console.log('changeQuery() -> has ontology selected ? ' + ontology);
			}
			if (ontology !== '') {
				
				if (typeof dataSourceForEdit !== 'undefined'){
					if ( dataSourceForEdit.hasOwnProperty("ontology.identification")){
						ontology = dataSourceForEdit.ontology.identification;
					}
					else {
						ontology = getOntologyFromQuery(dataSourceForEdit.query);				
					}
				}
				var from =  '<li id="from" name="'+ ontology +'" id="'+ ontology +'" class="list-group-item bold uppercase"> '+ ontology +' </li>';
				$("#from_ul").append(from);
			}
			
						  
			// QUERY  TYPE
			var combo = document.getElementById('typeQuery');       
			var option = combo.options[combo.selectedIndex].value;
			
			// BBDD TYPE
			var combodb = document.getElementById('targetDatabase');
			var targetdb = combodb.options[combodb.selectedIndex].value;
		   
		
		   //if( targetdb == "bdtr"){
		   if( targetdb == "rtdb"){
			   $('#json_deprecated').css('display','none'); 
			   
			   if(ontology > 0){  	   
				   $('#querySql').val('select * from '+ ontology );           	  
				   showLog ?  console.log('START  changeQuery() -> querySql -> ' + $('#querySql').val()) : '';		   
			   }

			  
			   combo.disabled = false;
			   
			  // DEFAULT CONSULTATIONS, YOU MUST ADJUST THEM
			  // IF WE HAVE AN ALREADY SELECTED, WE MAKE THE QUERY BASE BE WITH THE ONE WE HAVE
			   if ( $('#datasources').val() != '' ){ base_ontology = ontology }
			   console.log('OPTION: ' + option + ' queryKP: ' + queryKp + ' BDTR: ' + bdtrType );
			   if 	   ((option == "sql")     && ( queryKp != null) && ( bdtrType != "ORACLE")) { $('#querySql').innerHTML =  queryKp;  $('#querySql').val(queryKp); }
			   else if (( option == "native") && ( queryKp  != null)){ $('#querySql').innerHTML =  "" ; $('#querySql').val(""); }
			   else if (( option == "native") && ( bdtrType == "rtdb") && (queryKp == "null")){ $('#querySql').innerHTML =  "db." + base_ontology +".find().limit(3);";  $('#querySql').val("db." + base_ontology +".find().limit(3);"); }
			   else if (( bdtrType == "ORACLE") && (queryKp == null)){ 
			   		$('#querySql').innerHTML =  "select * from " + base_ontology +" where ROWNUM <= 3;"; $('#querySql').val("select * from " + base_ontology +" where ROWNUM <= 3;"); 
			   	}
			   else if (( option == "sql")    && (queryKp == null) && (bdtrType == "rtdb")) { $('#querySql').innerHTML =  "select * from " + base_ontology ; $('#querySql').val("select * from " + base_ontology ); }
			  
			   if( document.getElementById("enviar_consulta_descarga") ){ document.getElementById("enviar_consulta_descarga").style.display = ''; }
		   }

		}  
	     
		
			
			// RETURN ID
			function $id(id){ return document.getElementById(id); }
			
			// RETURN IS AN ARRAY
			function IsArray(obj) {  return obj && typeof obj === 'object' && typeof obj.length === 'number' && !(obj.propertyIsEnumerable('length')); }
			
			

			
			//SELECTION ONTOLOGY
			function ontologySelection(idOntology){
				
				// ACCESS AND CONTROL OF ONTOLOGY LOAD
				console.log('ontologySelection(): ' + idOntology);
				if ( idOntology === '' ) { alert(gadgetQueryToolJson.tools_select_ontology); return; }       
				
				
				
			   
				if( idOntology === ''){
				  alert(gadgetQueryToolJson.tools_select_ontology);
				}
				else{
				
					ontology = idOntology;              
					var comboTypeQuery = document.getElementById('typeQuery');
					if(comboTypeQuery.selectedIndex == -1){
						alert(gadgetQueryToolJson.tools_select_type_query);
					}
					else{
						var option = comboTypeQuery.options[comboTypeQuery.selectedIndex].value;
						if(option == "sql"){ $('#querySql').val("select * from "+ ontology ); }
						else if( option == "native" ){ $('#querySql').val("db."+ ontology +".find()"); }
						
					}
					$("#groupBy_time_ul").hide();
					$( "#from_ul" ).children().not('.panel-sofia2').remove();
					$( "#select_ul" ).children().not('.panel-sofia2').remove();
					from =  '<li id="from" name="'+ ontology +'" id="'+ ontology +'" class="list-group-item bold uppercase"> '+ ontology +' </li>';

					$("#from_ul").append(from);
				}
				
						 
				// we show the consultation panel. 
				if ($('#query-panel').hasClass('hide')){ $('#query-panel').toggleClass('hide');  } 
				$('#query-ontologia').text('' + idOntology);		
			}
			
			
			var nextinput = 0;
			// ARRAY OF FIELDS OF EACH ONTOLOGY, IS CHARGED WITH EACH ONTOLOGY
			var fields = [];
			
					
		
			//FUNCTION FOR AGGREGATE FIELD AT SELECT FROM THE QUERY
			function addRemoveFieldToSelect(fields){
			
				var duplicated = false;
				var fieldSelected = document.getElementById("checkselect").value;
					if( fieldSelected !== "Seleccione un campo" && fieldSelected !== "Select fields" ){
						
						// select id field selected.
						for(var i = 0; i < fields.length; i++){
						 if( fields[i].value == fieldSelected ){ 
						 	var fieldId=fields[i].getAttribute("id"); 
						 	break; 
						 }
						}
						fieldId = fieldId.replace(/\./g, "_");
						// if all clean list
						if( fieldId === "Todos" || fieldId ==="all"  ){ 
							$("#select_ul").children().not('.panel-sofia2').remove();
						}
						else{
						
							var listSelect = $("#select_ul").children().not('.panel-sofia');
							if( listSelect.length == 0){}
							else{
								for(var i=0; i<listSelect.length; i++){
									var li_id = listSelect[i].getAttribute("id");
									if( fieldId === "Todos" || fieldId ==="all"  ){ $("#select_ul").children().not('.panel-sofia2').remove(); }
									else{
										if( li_id == fieldId ){ duplicated = true; }
									}
								}	  
							}
						}
						if( duplicated == false ){							
							// create new li with fieldSelected
							select = '<li id="'+fieldId+'" name="'+fieldSelected+'" class="selectValues" ><div class="input-group" style="border: 1px solid #ddd; margin-top:-1px;"> <input type="text"  readonly style="font-size: smaller; border: none; background-color: white" condition="'+fieldSelected+'"  class="form-control bold " value="'+ fieldSelected +'"/> <span class="input-group-btn"> <button class="btn btn-circle btn-xs btn-outline blue no-border" onclick="deleteCondition(\''+fieldId+'\')" type="button"> <i class="fa fa-trash-o" style="font-size: 14px;"></i></button></span></div></li>'
							$("#select_ul").append(select);
							console.log('regenerando query tras agregar o quitar: ' + fieldSelected);
							createQuery();
						}
						// close dialog
						
						document.getElementById("selectFields").selected = true;
					} 
				
			};
			
				
			
			//FUNCTIONS TO LAUNCH THE DIALOGUES OF THE CONSULTATION PANELS
			function addSelect()	{ mostrarDialogoSelect(); }
			function addCondition()	{ mostrarDialogoCondicion(); }    
			function addOrderBy()	{ mostrarDialogoOrderBy(); }

		
			
			// CHECK CONDITIONS BEFORE INJECTING THEM
			function checkCondition(queryCondition){
				
				var conditions = document.getElementById('where_ul').getElementsByTagName("li");
				var add=true;
				
				if(queryCondition.indexOf('Select fields')!==-1 || queryCondition.indexOf('Seleccione un campo')!==-1){
					return false;
				}
				if(queryCondition.indexOf('All fields')!==-1 || queryCondition.indexOf('Todos los campos')!==-1){
					return false;
				}
				
				if(conditions.length > 0){
					for( var i=0; i < conditions.length; i++){
						if( conditions[i].getAttribute("name") == queryCondition ){
							add = false;
						}
					}
				}
				return add;
			}
			
			// CHECK conditions GROUPBY BEFORE INJECTING, TO-DO: try an ontology that groupby can do
			function checkGroupBy(queryGroupby){
			
				var groups = document.getElementById('groupBy_ul').getElementsByTagName("li");
				var add = true;
				if(queryGroupby.indexOf('Select fields')!==-1 || queryGroupby.indexOf('Seleccione un campo')!==-1){
					return false;
				}
				if( groups.length > 0){
					for(var i=0; i <groups.length; i++){
						if(groups[i].getAttribute("name") == queryGroupby){
							add=false;
						}
					}
				}
				return add;
			}
			
			//CHECK ORDERBY CONDITIONS BEFORE INJECTING
			function checkOrderby(queryOrderby){
				
				var orders = $('#orderby_ul li input:not(".panel-sofia2")');
				var add = true;
				if(queryOrderby.indexOf('Select fields')!==-1 || queryOrderby.indexOf('Seleccione un campo')!==-1){
					return false;
				}
				if(queryOrderby.indexOf('All fields')!==-1 || queryOrderby.indexOf('Todos los campos')!==-1){
					return false;
				}
				
				if( orders.length > 0 ){
					 add = false;
				}
				return add;
			}
			

			
			//REMOVE CONDITION, REGENERA LA QUERY AUTOMATICALLY TO NOT HAVE TO DO IT MANUAL AS BEFORE.
			function deleteCondition(id){
				
				$('#'+ id +'').remove();			
				createQuery();
			}




			
			//DISPATCHER TO CALL THE CORRESPONDING FUNCTION THAT MOUNTS THE QUERY STRING DEPENDING ON THE BBDD AND THE TYPE OF CONSULTATION, AND EXIT.
			function createQuery(){
				// CHECK ONTOLOGY
				ontology = $('#datasources').selectpicker('val');	
				showLog ? console.log('createQuery() -> ontology selected: ' + ontology) : '';
				
				if ( ontology === ''){ 			
					mostrarDialogo(gadgetQueryToolJson.tools_select_ontology); 			
				}
				else {
				
					var comboTypeQuery = document.getElementById('typeQuery');		
					var option = comboTypeQuery.options[comboTypeQuery.selectedIndex].value;			
						
					
					// SQL-RELATIONALORACLE
					if(option=="sql"){ console.log('createQuery()->createQuerySql'); $('#querySql').val(createQuerySql()); }			
								
				}
			};
			
			
			function createQuerylimit(){				
				var limit = $("#maxvaluesDataSource").val();
			    var query =	$('#querySql').val();
			    if(query.trim().length===0){
			    	createQuery();
			    	return;
			    }else{
				    var index = query.toLowerCase().lastIndexOf(" limit ");
				    if(index>0){
				    	var isSubquery = query.substr(index,query.length).lastIndexOf(")");
				    	if(isSubquery<0){
				    		query = query.substr(0,index) + " limit "+ limit;	
				    	}else{
				    		query = query + " limit "+ limit;
				    				    		
				    	}
				    }else{
				    	query = query + " limit "+ limit;
				    }
				    $('#querySql').val(query);
			    }
			}
			
			
			//GENERATION OF THE QUERY STRING SQL NO-RELATIONALORACLE AND RELATIONALORACLE WITH SQL
			function createQuerySql(){
				
				// Initialization of elements and strings of the query.       
				var allFields = false;
				var query 		= "select ";
				var queryFields = "";
				var ontology 	= document.getElementById("from").getAttribute("name");
				var conditions = $('#where_ul li:not(".panel-sofia2")');
				var groupby 	= document.getElementById('groupby_check');
				var orderby 	= $('#orderby_ul li:not(".panel-sofia2")');
				var fields 		= $('#select_ul li:not(".panel-sofia2")');
				var limit = $("#maxvaluesDataSource").val();
				var condition, queryGroupBy, queryOrderBy = "";
				var aliasTable; 
						
				// element controls of the query
				hasCondition 	= false;
				hasGroupBy	 	= false;
				hasOrderBy 		= false;
				
				console.log('createQuerySql --> conditions: ' + conditions + ' JSON: ' + JSON.stringify(conditions));
				
				// conditions WHERE
				if( conditions.length == 1){
					hasCondition = true;
					condition = conditions.attr('condition');
					condition = obtainCondition(condition);
					if(bdtrType !== "ORACLE"){
						condition = condition.replace(/'/g,"\"");
					}
					condition= "c." + condition;
				}
				else if( conditions.length > 0){
					hasCondition = true;
					conditions.each(function(index){		
						showLog ? console.log('condición aux en EACH-'+ index +' es: ' + $(this).attr('condition')) : '';
						var conditionAux = obtainCondition($(this).attr('condition'));
						if(bdtrType !== "ORACLE"){
							conditionAux = conditionAux.replace(/'/g,"\"");
						}	
						if( index == 0){ condition = "c." + conditionAux; } else {  condition = condition + " AND " + "c." + conditionAux; }			
					});           
				}
				
				if ( bdtrType == "ORACLE"){
					if(!hasCondition){
						hasCondition = true;
						condition = "ROWNUM <= " + limit;
					}
					else{
						condition += " AND ROWNUM <= " + limit;
					}
					aliasTable = "";
				}
				else{
					aliasTable = " as"
				} 
				

				// ORDERBY 
				if( orderby.length == 1){ hasOrderBy = true; queryOrderBy = orderby.find('input').val(); }
				
				// fields (SELECT)		
				if( fields.length == 1 ){ queryFields = fields[0].getAttribute("name");	if(( queryFields == "Todos los campos")||(queryFields == "All fields")){ allFields = true;}}
				else if( fields.length == 0){ 
					allFields = true; 
				}
				else{			
					for( i=0; i < fields.length; i++){
						if(( fields[i].getAttribute("name") == "Todos los campos")||( fields[i].getAttribute("name") == "All fields" )){ 
							allFields = true; 
						}
						else{					
							queryFields = queryFields + fields[i].getAttribute("name");					
							if( i <fields.length-1){ queryFields = queryFields + ", "; } // último campo.
						}
					}
				}

				// SELECT ALL.
				if( allFields ){ queryFields = "*"; }
				
				// GROUPBY
				/*if( groupby.checked == true && queryFields != "*"){ hasGroupBy = true; queryGroupBy = queryFields;	}
				else if( groupby.checked == true && queryFields == "*"){ hasGroupBy = false; mostrarDialogo(gadgetQueryToolJson.queries_error_groupbyform); }
				*/


				// QUERY ASSEMBLY
		        if( hasCondition && hasGroupBy && hasOrderBy )		  { query = query + queryFields + " from " + ontology + aliasTable + " c where " + condition + " group by " + queryGroupBy + " order by "+ queryOrderBy;}
				else if( !hasCondition && !hasGroupBy && !hasOrderBy) { query = query + queryFields + " from " + ontology + aliasTable + " c"; }
				else if( hasCondition  && !hasGroupBy && !hasOrderBy) { query = query + queryFields + " from " + ontology + aliasTable + " c where " + condition; }
				else if( !hasCondition && hasGroupBy  && !hasOrderBy) { query = query + queryFields + " from " + ontology + aliasTable + " c group by " + queryGroupBy; }
				else if( hasCondition  && !hasGroupBy && hasOrderBy ) { query = query + queryFields + " from " + ontology + aliasTable + " c where " + condition + " order by "+ queryOrderBy;}
				else if( !hasCondition && hasGroupBy  && hasOrderBy ) { query = query + queryFields + " from " + ontology + aliasTable + " c group by " + queryGroupBy + " order by "+ queryOrderBy; }
				else if( hasCondition  && hasGroupBy  && !hasOrderBy) { query = query + queryFields + " from " + ontology + aliasTable + " c where " + condition + " group by " + queryGroupBy; }
				else if( !hasCondition && !hasGroupBy && hasOrderBy ) { query = query + queryFields + " from " + ontology + aliasTable + " c order by " + queryOrderBy;}
		        
		    if(bdtrType !== "ORACLE"){
		      query = query + " limit "+ limit;
		    }
			
				console.log('the query: ' + query);
				return query;
			}
			
			
			// OBTAINS THE CONDITION OF THE WHERE MAKING AN EXHAUST OF CHARACTERS FOR THE QUERY STRING
			function obtainCondition(condition){
				console.log('condition: ' + condition + ' type: ' + typeof(condition));
				if( condition.includes('&eq'))		{ condition = condition.replace("&eq;", "=");  }
				else if( condition.includes('&lt'))	{ condition = condition.replace("&lt;", "<");  }
				else if( condition.includes('&lte')){ condition = condition.replace("&lte;", "<=");}
				else if( condition.includes('&gt'))	{ condition = condition.replace("&gt;", ">");  }
				else if( condition.includes('&gte')){ condition = condition.replace("&gte;", ">=");}
				else if( condition.includes('&ne'))	{ condition = condition.replace("&ne;", "!="); }
				
				return condition;
			}
			
			
			
			
			
			// CREATE EDITOR FOR JSON SCHEMA 
			var createEditor = function(){
				
				showLog ? console.log('|--->   createEditor()') : '';
				var container = document.getElementById('jsoneditor');	
				var options = {
					mode: 'text',
					theme: 'bootstrap3',
					required_by_default: true,
					modes: ['text', 'tree', 'view'], // allowed modes
					error: function (err) {
						$.alert({title: 'ERROR!', theme: 'dark', style: 'red', content: err.toString()});
						return false;
					},
					onChange: function(){
						
						console.log('se modifica el editor en modo:' + editor.mode + ' contenido: ' + editor.getText());
					}
				};
				
				editor = new jsoneditor.JSONEditor(container, options, "");		
				
			}
			
			function getOntologyFromQuery(query){			
				query = query.replace(/(\r\n\t|\n|\r\t)/gm," ");
				query = query.replace(/  +/g, ' ');
				query = query.replace(/\,/g,' ');
				var list = query.split(/from /i);
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
			
			function initOntology(update){
				
				let myOntology = '';
				
				// INIT QUERY STRING		
				var combo = document.getElementById('datasources');
				if(typeof update ==='undefined'){
					if(combo.length > 0){	       
						$('#querySql').val("select * from "+ combo.value );        	
						showLog ? console.log('init combo ontologias: ' + combo.length) : '';
						changeQuery();
					}
				}else{
					if (dataSourceForEdit.hasOwnProperty("ontology") && dataSourceForEdit.ontology != null && dataSourceForEdit.ontology.hasOwnProperty("identification") && dataSourceForEdit.ontology.identification != null){
						if ( dataSourceForEdit.ontology.identification !== ''){
							ontologySelection(dataSourceForEdit.ontology.identification);
						} else {
							myOntology = getOntologyFromQuery(dataSourceForEdit.query);
							ontologySelection(myOntology);
							
						}
					}
					else {
						myOntology = getOntologyFromQuery(dataSourceForEdit.query);
						ontologySelection(myOntology);						
						
					}
				}
				
				console.log(' STARTED BDTR...');			
				// INIT SELECT-PICKERS ONTOLOGY
				$('.selectpicker').on('changed.bs.select', function (e, clickedIndex, newValue, oldValue) {
					var selected = $(e.currentTarget).val();
					var group = $('option:selected',this).attr('data-type');
					var ontologiaID = $('option:selected',this).attr('id');
					console.log('ontology selected: ' + selected);
					console.log('Charge ontology: ' + selected + ' group: ' + group + ' ID: '+ ontologiaID);
					
					if ( group === 'ontology') { 
						ontologySelection(selected);
						
					}
				});	
				
				console.log('BDTRTYPE: ' + bdtrType);
				if(gadgetQueryToolJson.query != "null"){                 
		            $('#querySql').innerHTML = gadgetQueryToolJson.query;
		        }
				
				$('.btn-table-toggle').on('click',function(){
					
					if (editor){ 
						if ($('.table-viewer').is(':visible')){					
							$('#jsoneditor').fadeIn();
							$('.table-viewer').fadeOut();
						}
						else{
						$('#jsoneditor').fadeOut();
						$('.table-viewer').createTable(JSON.parse(dataJSON), {});
						$('.table-viewer').fadeIn();
						
						}		
					}
					else { return; }
					
				
				});
				//Refresh page and reset localStorage.clickount
				$(window).unload(function(){
					  localStorage.clickcount = 0;
					  localStorage.clear();
					});
				changeQuery();
				
			}
			
			
	    
