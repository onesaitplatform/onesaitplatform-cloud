//var csv is the CSV file with headers
function csvJSON(csv){
  var separators = {
		        ",": "comma",
		        ";": "semicolon",
		        "\t": "tab"
		      };
  var separator = detectSeparator(csv);
  function detectSeparator(csv) {
	    var counts = {"separator":"", "count":0};
	    _.each(separators, function(sep, i) {
	      var re = new RegExp(i, 'g');
	      var sepCount = (csv.match(re) || []).length;
	      if(counts['count']< sepCount) {
	    	  counts['separator'] = i;
	    	  counts['count']= sepCount;
	      }
	    });
	    return counts['separator'];
	}
  csv = csv.replace(/[\r]/g, '');
  var lines=csv.split("\n");

  var result = [];

  var headers=lines[0].split(separator);

  for(var i=1;i<lines.length;i++){
	  if(lines[i]!=""){
		  var obj = {};
		  var currentline=lines[i].split(separator);
		  
		  for(var j=0;j<headers.length;j++){
			  if(isNaN(currentline[j]) && currentline[j] != ""){
				
				try{
					obj[headers[j].replace(/\"/g, '')] = JSON.parse(currentline[j]);
				}catch(e){
					obj[headers[j].replace(/\"/g, '')] = currentline[j].replace(/\"/g, '');
				}
			
			  
		  
			  }else if(!isNaN(currentline[j]) && currentline[j] != ""){
				  obj[headers[j].replace(/\"/g, '')] = parseFloat(currentline[j]);
			  }
				  
		  }

		  result.push(obj);
		  
	  }
	  

  }
  
  //return result; //JavaScript object
  return JSON.stringify(result); //JSON
}