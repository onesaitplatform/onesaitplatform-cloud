function processJSON(jsonString) {
    if (this.tryParseJSON(jsonString) === true) {

        var json = JSON.parse(jsonString);
        var yamlReady = this.buildSwaggerJSON(json);
        //console.log(yamlReady);
        return JSON.stringify(yamlReady, null, 4);
        //var x = stringify(yamlReady);
        //yamlEditor.setValue(x);
        //tinyToast.show('✔ Conversion complete')

    } else {
        tinyToast.show('Invalid JSON. Have properties names in quotes')
    }
}

function tryParseJSON(jsonString) {
    try {
        var o = JSON.parse(jsonString);
        if (o && typeof o === "object") {
            return true;
        }
    } catch (e) {
        console.error(e, jsonString);
        return false;
    }

    return false;
}

function buildSwaggerJSON(data) {
    var keys = Object.keys(data)
    var op = {
       // required: keys,
        properties: {}
    };
    keys.forEach(function (x) {
        var typeData = typeOf(data[x]);
        if (["array", "object", "null", "string"].indexOf(typeData) === -1)
            op.properties[x] = {
                "type": typeData
            };
        else {
            switch (typeData) {
                case "array":
                	 op.properties[x] = {
                        "type": "array",
                        "items": []
                    };
                	 
                	data[x].forEach(function(o,i){
                		if(typeOf(o) == "object"){
                			op.properties[x]["items"].push(buildSwaggerJSON(o));
                			op.properties[x]["items"][i]["type"] = typeOf(o);
                			
                		}else{
                			op.properties[x]["items"].push({"type" : typeOf(o)});
                		}
                		
                		
                		
                		
                	});
                	/*
                    typeData = typeOf(data[x][0]);
                    if (typeData === "array") {
                        op.properties[x] = { "type": "array", "items": { type: typeData } };
                        //throw new Error("Complex object (array of array etc...)", data[x][0]);
                    }
                    if (typeData === "object") {
                        op.properties[x] = {
                            "type": "array",
                            "items": {
                                type: typeData,
                                properties: buildSwaggerJSON(data[x][0]).properties
                            }
                        };
                        break;
                    }*/
                   

                    break;
                case "object":
         
                    op.properties[x] = buildSwaggerJSON((data[x]));
                    op.properties[x].type = "object";
                    
                    break;
                case "null":
//                	op.properties[x] ={
//                		"type" : ["string", "number", "null"]
//                	};
                	/*for(var i= op.required.length-1; i>=0; i--){
                		if(op.required[i] == x){
                			op.required.splice(i,1);
                			break;
                		}
                	}*/
                	break;
                case "string":
                	try{
                		asDateTime(data[x]);
                		op.properties[x] = {
                                "type": typeData,
                                "format":"date-time"
                		};
                	}catch(e){
	            		op.properties[x] = {
	                            "type": typeData
	                    };
                	}
            		break;
          
                default:
                    console.warn("skipping ", typeData);
                    break;
            }
        }
    });
    return op;
}
function asDateTime(prop) {
	// Implementation based up on http://stackoverflow.com/questions/8178598/xml-datetime-to-javascript-date-object
	// Improved to support full spec and optional parts
	var bits = prop.split(/[-T:+Z]/g);
	
	var d = new Date(bits[0], bits[1]-1, bits[2]);			
	var secondBits = bits[5].split("\.");
	d.setHours(bits[3], bits[4], secondBits[0]);
	if(secondBits.length>1)
		d.setMilliseconds(secondBits[1]);

	// Get supplied time zone offset in minutes
	if(bits[6] && bits[7]) {
		var offsetMinutes = bits[6] * 60 + Number(bits[7]);
		var sign = /\d\d-\d\d:\d\d$/.test(prop)? '-' : '+';

		// Apply the sign
		offsetMinutes = 0 + (sign == '-'? -1 * offsetMinutes : offsetMinutes);

		// Apply offset and local timezone
		d.setMinutes(d.getMinutes() - offsetMinutes - d.getTimezoneOffset())
	}
	else
		if(prop.indexOf("Z", prop.length - 1) !== -1) {
			d = new Date(Date.UTC(d.getFullYear(), d.getMonth(), d.getDate(), d.getHours(), d.getMinutes(), d.getSeconds(), d.getMilliseconds()));					
		}

	// d is now a local time equivalent to the supplied time
	return d;
}
function typeOf(t){return null===t||void 0===t?String(t):class2type[Object.prototype.toString.call(t)]||"object"}function isEmpty(t){var e,r;if("object"===typeOf(t))for(e in t)if(r=t[e],void 0!==r&&"function"!==typeOf(r))return!1;return!0}function stringify(t){var e,r="";return e={undefined:function(){return"null"},null:function(){return"null"},number:function(t){return t},boolean:function(t){return t?"true":"false"},string:function(t){return JSON.stringify(t)},array:function(t){var n="";return 0===t.length?n+="[]":(r=r.replace(/$/," "),t.forEach(function(t){var o=e[typeOf(t)];if(!o)throw new Error("what the crap: "+typeOf(t));n+="\n"+r+"- "+o(t)}),r=r.replace(/ /,""),n)},object:function(t){var n="";return 0===Object.keys(t).length?n+="{}":(r=r.replace(/$/," "),Object.keys(t).forEach(function(o){var i=t[o],a=e[typeOf(i)];if("undefined"!=typeof i){if(!a)throw new Error("what the crap: "+typeOf(i));n+="\n"+r+o+": "+a(i)}}),r=r.replace(/ /,""),n)},function:function(){return"[object Function]"}},"---"+e[typeOf(t)](t)+"\n"}var global=Function("return this")(),classes="Boolean Number String Function Array Date RegExp Object".split(" "),i,name,class2type={};for(i in classes)classes.hasOwnProperty(i)&&(name=classes[i],class2type["[object "+name+"]"]=name.toLowerCase());String.prototype.entityify||(String.prototype.entityify=function(){return this.replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;")}),String.prototype.quote||(String.prototype.quote=function(){var t,e,r=this.length,n='"';for(e=0;e<r;e+=1)if(t=this.charAt(e),t>=" ")"\\"!==t&&'"'!==t||(n+="\\"),n+=t;else switch(t){case"\b":n+="\\b";break;case"\f":n+="\\f";break;case"\n":n+="\\n";break;case"\r":n+="\\r";break;case"\t":n+="\\t";break;default:t=t.charCodeAt(),n+="\\u00"+Math.floor(t/16).toString(16)+(t%16).toString(16)}return n+'"'}),String.prototype.supplant||(String.prototype.supplant=function(t){return this.replace(/{([^{}]*)}/g,function(e,r){var n=t[r];return"string"==typeof n||"number"==typeof n?n:e})}),String.prototype.trim||(String.prototype.trim=function(){return this.replace(/^\s*(\S*(?:\s+\S+)*)\s*$/,"$1")}),global.typeOf=global.typeOf||typeOf,global.isEmpty=global.isEmpty||isEmpty;