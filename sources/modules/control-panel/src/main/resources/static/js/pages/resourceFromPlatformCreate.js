var ResourceFromPlatformController = function() {
	
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Menu Controller';	
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.
	var currentFormat = '' // date format depends on currentLanguage.
	var internalFormat = 'yyyy/mm/dd';
	var internalLanguage = 'en';
	var reader = new FileReader();
	
	reader.onload = function (e) {
        $('#showedImg').attr('src', e.target.result);
    }
	
	var calculateVersion = function() {
		configurarApi();
        var identification = $('#identification').val();
        var apiType = $('#apiType').val();
    	var csrf_value = $("meta[name='_csrf']").attr("content");
    	var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
    	
        if ((identification!=null) && (identification.length>0) && (apiType!='')) {
            $.ajax({
                url: '/controlpanel/apimanager/numVersion',
                headers: {
					[csrf_header]: csrf_value
			    },
                type: 'POST',
                data: JSON.stringify({"identification":identification,"apiType":apiType}),
                dataType: 'text',
                contentType: 'text/plain',
                mimeType: 'text/plain',
                success: function(data) {
                    if(data != null && data != "") {
                        $('#numversion').val(data);
                        // VISUAL-UPDATE
                        configurarApi();
                    }
                },
                error: function(data,status,er) {
                    $('#dialog-error').val("ERROR");
                }
            });
        } else {
            configurarApi();
        }
    }

	var configurarApi = function () {
        apiType = $('#apiType').val();
        apiName = $('#identification').val();
        apiVersion = $('#numversion').val();
        apiEndPoint = $('#id_endpoint');
        apiSwagger = $('#id_endpoint_swagger');
        
        apiEndPoint.val(endpoint + "server/api/v" + apiVersion + "/" + apiName);
        apiSwagger.val(endpoint + "/services/management/api-docs?url=/services/management/swagger" + "/" + apiName + "/swagger.json");
        //myCodeMirror.refresh();
    }
	
    var updateApiLimit = function () {
        var checkCache= $('#checkboxLimit').prop('checked');
        if (checkCache) {
        	$('#id_limit').val("5");
        	$('#id_limit').prop('disabled', false);
        } else {
        	$('#id_limit').val("");
        	$('#id_limit').prop('disabled', true);
        }
    }
    
	var handleCodeMirrorJson = function () {
        var myTextArea = document.getElementById('jsonTextArea');
        myCodeMirror = CodeMirror.fromTextArea(myTextArea, {
        	mode: "application/ld+json",
        	autoCloseBrackets: true,
            matchBrackets: true,
            styleActiveLine: true,
            theme:"elegant",
            lineWrapping: true

        });
		myCodeMirror.setSize("100%", 500);
		myTextArea = document.getElementById('jsPostProcessExternal');
		myCodeMirrorJsExternal = CodeMirror.fromTextArea(myTextArea, {
	    	mode: "text/javascript",
	    	autoRefresh: {delay: 500},
	    	autoCloseBrackets: true,
	        matchBrackets: true,
	        styleActiveLine: true,
	        theme:"material",
	        lineWrapping: true
	
	    });
		myCodeMirrorJsExternal.setSize("100%",200);
		myCodeMirrorJsExternal.refresh();
    };
	
	// CONTROLLER PUBLIC FUNCTIONS 
	return{
		
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';			
			handleCodeMirrorJson();
		},
		// CALCULATE VERSIONS
		calculateNumVersion: function() {
			logControl ? console.log(LIB_TITLE + ': calculateNumVersion()') : '';
			calculateVersion();
		},
			
		// CHANGE API LIMIT
		changeApiLimit: function() {
			logControl ? console.log(LIB_TITLE + ': changeApiLimit()') : '';
			updateApiLimit();
		}
		
	};
}();

