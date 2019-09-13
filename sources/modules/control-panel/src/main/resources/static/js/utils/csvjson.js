// Start from https://gist.github.com/iwek/7154578#file-csv-to-json-js
// and fix the issue with double quoted values

function csvTojs(csv) {
  var lines="";
  if (csv.search("\r\n") == -1) {
	  if(csv.search("\r") == -1) { lines = csv.split("\n"); }
	  else { lines = csv.split("\r"); }
  }
  else { lines = csv.split("\r\n");}
  
  if(lines.length<=1) lines=csv.split("\r");
  if(lines.length<=1) lines=csv.split("\n");
  var result = [];
  var sep = ";";
  var headers = lines[0].split(sep);
  if(headers.length==1) sep=",";
  var headers = lines[0].split(sep);
  headers[headers.length-1]=headers[headers.length-1].trim();
	  
  for(var i=1; i<lines.length; i++) {
    var obj = {};

    var row = lines[i],
      queryIdx = 0,
      startValueIdx = 0,
      idx = 0;

    if (row.trim() === '') { continue; }

    while (idx < row.length) {
      /* if we meet a double quote we skip until the next one */
      var c = row[idx];

      if (c === '"') {
        do { c = row[++idx]; } while (c !== '"' && idx < row.length - 1);
      }

      if (c === sep || /* handle end of line with no comma */ idx === row.length - 1) {
        /* we've got a value */
    	var value = "";
    	if(idx === row.length - 1) {
    	   value = row.substr(startValueIdx, row.length - startValueIdx).trim();
    	}
    	else {
  		   value = row.substr(startValueIdx, idx - startValueIdx).trim();
    	}
    	
        /* skip first double quote */
        if (value[0] === '"') { value = value.substr(1); }
        /* skip last comma */
        if (value[value.length - 1] === sep) { value = value.substr(0, value.length - 1); }
        /* skip last double quote */
        if (value[value.length - 1] === '"') { value = value.substr(0, value.length - 1); }

        var key = headers[queryIdx++];
        /* parsing values*/
        if(isNaN(value) && value != ""){
			
			try{
				obj[key] = JSON.parse(value);
			}catch(e){
				obj[key] = value.replace(/\"/g, '');
			}
		
		  
	  
		}else if(!isNaN(value) && value != ""){
			  obj[key] = parseFloat(value);
		}else {
			obj[key] = "";
		}
         
        //obj[key] = value;
        startValueIdx = idx + 1;
      }

      ++idx;
    }

    result.push(obj);
  }
  return result;
}