
	
	// GENERARATE PROPERTY TYPES [GEOMETRY, OBJECT, ARRAY OR BASIC]
	var generateBasicType = function(propType, obj, parent, thevalue){
		showLog ? console.log('        |--->   generateBasicType()') : '';

    	// if enum, return enum value
    	if (thevalue != null && thevalue != ""){ return "\"" + thevalue + "\""; }
    	// string
        if (propType == "string") {
        	if 		(parent == "media" && obj == "storageArea")	  { return "\"SERIALIZED\""; }
			else if (parent == "media" && obj == "binaryEncoding"){ return "\"Base64\""; }
			else if (parent == "media" && obj == "name")		  {	return "\"fichero.pdf\""; }
			else if (parent == "media" && obj == "mime")		  {	return "\"application/pdf\""; }
			else{ return "\"string\""; }
        }
		// integer,boolean, object, number and geometry
		else if ( propType == "integer" ){ return "1" }
		else if ( propType == "boolean" ){ return "true"; }
		else if ( propType == "object" ){ return "{}"; }
		else if ( propType == "geometry-point" || obj =="geometry-point"){ return "{\"type\":\"Point\", \"coordinates\":[9,19.3]}"; }
		else if ( propType == "geometry-linestring" || obj =="geometry-linestring"){ return "{\"type\":\"LineString\", \"coordinates\":[[9,19.3],[19,9.3]]}"; }
		else if ( propType == "geometry-polygon" || obj =="geometry-polygon"){ return "{\"type\":\"Polygon\", \"coordinates\":[ [[9,-19.3],[-19,-9.3],[-9,19.3],[19,-9.3]] ,[[9,19.3],[19,9.3],[-9,19.3],[-19,9.3]] ]}"; }
		else if ( propType == "geometry-multipoint" || obj =="geometry-multipoint"){ return "{\"type\":\"MultiPoint\", \"coordinates\":[[9,19.3],[19,19.3]]}"; }
		else if ( propType == "geometry-multilinestring" || obj =="geometry-multilinestring"){ return "{\"type\":\"MultiLineString\", \"coordinates\":[ [[9,19.3],[19,9.3]], [[9,19.3],[19,9.3]] ]}"; }
		else if ( propType == "geometry-multipolygon" || obj =="geometry-multipolygon"){ return "{\"type\":\"MultiPolygon\", \"coordinates\":[[[[9,2.5],[9,2],[-9,3],[9,-3],[-9,2]]],[[[9,0],[9,-0.5],[9,-1.5],[9,1],[9,0.4]],[[9,0.2],[-9,0.2],[9,0.8],[-9,0.8],[9,3.2]]]]}"; }
		else if ( propType == "number"   || propType == "numeric" ){	return "28.6" }
		else if ( propType == "array") {return ""; }
    }
	

	// GENERARATE PROPERTY TYPES [GEOMETRY, OBJECT, ARRAY ]
	var generateObject = function(ontology, instance, parent){
		showLog ? console.log('        |--->   generateObject()') : '';

       	instance = "{";
       	if ( ontology.properties ){
	        for ( var obj in ontology.properties ){

				var objtype = ontology.properties[obj].type;
	             // if obj <> date or geometry, iterates recursive for
					// treatment.
	             if ((objtype.toLowerCase() == "object") && (obj != "geometry") && ontology.properties[obj].properties && ontology.properties[obj].properties.$date == null ){
	            	 showLog ? console.log('        |--->   generateObject() --> object: ' + JSON.stringify(ontology.properties[obj])) : '';
	             	instance = instance + "\"" +obj+"\":"+ generateObject(ontology.properties[obj], "", obj);

	             }
				 // date obj
				 else if ((ontology.properties && ontology.properties.$date != null) || (ontology.properties && ontology.properties[obj] && ontology.properties[obj].properties && ontology.properties[obj].properties.$date!= null)){
	                 // date root node or date children node
	            	 if (obj == "$date"){  instance = instance + "\"$date\": \"2014-01-30T17:14:00Z\""; } else { instance = instance + "\"" +obj+"\":"+ "{\"$date\": \"2014-01-30T17:14:00Z\"}"; }
				 }
	             // geometry with direct reference to point
				 else if (ontology[obj] && ontology[obj].properties &&  ontology[obj].properties[propertyName].properties && ontology[obj].properties[propertyName].properties.type && ontology.properties[obj].properties.type.enum[0]== "Point"){
	                 instance = instance + "\"" +obj+"\":"+ "{\"type\":\"Point\", \"coordinates\":[9,19.3]}";

	             }
				 else if (ontology[obj] && ontology[obj].properties &&  ontology[obj].properties[propertyName].properties && ontology[obj].properties[propertyName].properties.type && ontology.properties[obj].properties.type.enum[0]== "LineString"){
	                 instance = instance + "\"" +obj+"\":"+ "{\"type\":\"LineString\", \"coordinates\":[[9,19.3],[19,9.3]]}";

	             }
				 else if (ontology[obj] && ontology[obj].properties &&  ontology[obj].properties[propertyName].properties && ontology[obj].properties[propertyName].properties.type && ontology.properties[obj].properties.type.enum[0]== "Polygon"){
	                 instance = instance + "\"" +obj+"\":"+ "{\"type\":\"Polygon\", \"coordinates\":[ [[9,-19.3],[-19,-9.3],[-9,19.3],[19,-9.3]] ,[[9,19.3],[19,9.3],[-9,19.3],[-19,9.3]] ]}";

	             }
				 else if (ontology[obj] && ontology[obj].properties &&  ontology[obj].properties[propertyName].properties && ontology[obj].properties[propertyName].properties.type && ontology.properties[obj].properties.type.enum[0]== "MultiPoint"){
	                 instance = instance + "\"" +obj+"\":"+ "{\"type\":\"MultiPoint\", \"coordinates\":[[9,19.3],[19,19.3]]}";

	             }
				 else if (ontology[obj] && ontology[obj].properties &&  ontology[obj].properties[propertyName].properties && ontology[obj].properties[propertyName].properties.type && ontology.properties[obj].properties.type.enum[0]== "MultiLineString"){
	                 instance = instance + "\"" +obj+"\":"+ "{\"type\":\"MultiLineString\", \"coordinates\":[ [[9,19.3],[19,9.3]], [[9,19.3],[19,9.3]] ]}";

	             }
				 else if (ontology[obj] && ontology[obj].properties &&  ontology[obj].properties[propertyName].properties && ontology[obj].properties[propertyName].properties.type && ontology.properties[obj].properties.type.enum[0]== "MultiPolygon"){
	                 instance = instance + "\"" +obj+"\":"+ "{\"type\":\"MultiPolygon\", \"coordinates\":[[[[9,2.5],[9,2],[-9,3],[9,-3],[-9,2]]],[[[9,0],[9,-0.5],[9,-1.5],[9,1],[9,0.4]],[[9,0.2],[-9,0.2],[9,0.8],[-9,0.8],[9,3.2]]]]}";

	             }
				// array
				 else if (objtype.toLowerCase() == "array"){
					 showLog ? console.log('        |--->   generateObject() --> array: ' + JSON.stringify(ontology.properties[obj])) : '';
					instance = instance + "\""+ obj + "\":" + generateArray(ontology.properties[obj], "", obj);
	             }
				 else if (obj.format != null){
					 instance = instance +"\"" +obj+"\":\"2014-01-30T17:14:00Z\"}"; 
				 }
				 // Basic
				 else {
	            	 var valor = "";
	            	 // if enum getr first value
	                 if (ontology.properties[obj].enum != null){
	                	  valor = ontology.properties[obj].enum[0];
	                 }
	            	 instance = instance + "\""+ obj + "\":" + generateBasicType(objtype, obj, parent, valor);
	             }
	             instance = instance + ",";
	        }
	        instance = instance.substring(0,instance.length-1);
	     // if obj is null, generate default
       	} else {
       		instance = instance + "\"object\"";
       	}
        return instance + "}";
    }
	
