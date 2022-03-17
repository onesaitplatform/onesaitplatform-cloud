(function ($) {

    $.fn.createTable = function (data, options) {

        var element = this;
        var settings = $.extend({}, $.fn.createTable.defaults, options);
        var selector;

        if (element[0].className !== undefined) {
            var split = element[0].className.split(' ');
            selector = '.' + split.join('.') + ' ';
        } else if (element[0].id !== undefined) {
            selector = '#' + element[0].id + ' ';
        }

        //var table = '<div class="table-scrollable table-scrollable-borderless"><table class="json-to-table table table-hover table-striped">';
		var table = '<div class="table-scrollable table-scrollable-borderless"><table class="json-to-table table table-hover table-striped" dt:table="true">';
        table += '<thead><th class="jsl"></th>';
        table += $.fn.createTable.parseTableData(data, true);
        table += '</thead>';
        table += '<tbody>';
        table += $.fn.createTable.parseTableData(data, false);
        table += '</tbody>';
        table += '</table></div>';

        element.html(table);
		/*setTimeout(function(){
			$('.json-to-table').DataTable();
		$('.dataTables_info').addClass('col-md-6 col-sm-6');
		$('.dataTables_length').addClass('col-md-6 col-sm-6');
		$('#DataTables_Table_0_wrapper > div:nth-child(3)').addClass('table-scrollable table-scrollable-borderless');
		$('#DataTables_Table_0_wrapper > div:nth-child(3) > div.col-md-5.col-sm-5').append($('.dataTables_length'));
		$('.dataTables_paginate').attr('style', 'float:right !important');
		},2000);*/
		
        return function () {

            $(selector + '.json-to-table').css({
                borderCollapse: 'collapse',
                width: '100%',
                border: settings.borderWidth + ' ' + settings.borderStyle + ' ' + settings.borderColor,
                fontFamily: settings.fontFamily
            });

            $(selector + '.jsl').css({
                minWidth: '18px',
                width: '18px',
                padding: '0 10px 0 10px'
            });

            $(selector + '.json-to-table thead th:not(:first-child), .json-to-table tbody td:not(:first-child)').css({
                width: (100 / $.fn.createTable.getHighestColumnCount(data).max) + '%'
            });

            $(selector + '.json-to-table thead th, .json-to-table tbody td').css({
                border: settings.borderWidth + ' ' + settings.borderStyle + ' ' + settings.borderColor
            });

            $(selector + '.json-to-table thead th').css({
                backgroundColor: settings.thBg,
                color: settings.thColor,
                height: settings.thHeight,
                fontFamily: settings.thFontFamily,
                fontSize: settings.thFontSize,
                textTransform: settings.thTextTransform
            });

            $(selector + '.json-to-table tbody td').css({
                backgroundColor: settings.trBg,
                color: settings.trColor,
                paddingLeft: settings.tdPaddingLeft,
                paddingRight: settings.tdPaddingRight,
                height: settings.trHeight,
                fontSize: settings.trFontSize,
                fontFamily: settings.trFontFamily
            });

        }();
    };

    $.fn.createTable.getHighestColumnCount = function (data) {

        var count = 0, temp = 0, column = {max: 0, when: 0};

        for (var i = 0; i < data.length; i++) {
            count = $.fn.getObjectLength(data[i]);
            if (temp <= count) {
                temp = count;
                column.max = count;
                column.when = i;
            }
        }

        return column;
    };

    $.fn.createTable.parseTableData = function (data, thead) {

        var row = '';		
		var Cols = $.fn.createTable.getHighestColumnCount(data);
		var colspan = Cols["max"] + 1;
		var isObject = false;
		var objKey = '';
		var keyType = '';
		
				
		console.log(' Data: ' + JSON.stringify(data) + ' colspan: ' + colspan);
		
        for (var i = 0; i < data.length; i++) {			
            if (thead === false) row += '<tr><td class="jsl text-center">' + (i + 1) + '</td>';			
            $.each(data[i], function (key, value) {
				isObject = false;
				console.log('key: ' + key);
                if (thead === true) {
                    if (i === $.fn.createTable.getHighestColumnCount(data).when) {
						if(typeof value == 'object'){ keyType = "<small>(Object)</small>";   } else { keyType = ''; }
                        row += '<th>' + $.fn.humanize(key) + ' ' + keyType + '</th>';
                    }
                } else if (thead === false) {
                	if(typeof value =='object'){
                		//JSONObject
						 objKey = i + key;
                		 row += '<td class="text-center bg-grey-steel text-truncate-sm pointer" onclick="showData(\''+objKey +'\',\''+ key +'\')"><div class="label bg-grey-mint label-sm">' + JSON.stringify(value) + '</div></td>';
						 isObject = true;
						 valueObjects.push({"key":objKey, "json":JSON.stringify(value,null,2)});
                	}else{
                		 row += '<td class="text-truncate-sm no-wrap" title="'+ value +'">' + value + '</td>';						 
                	}
                }
            });
            if (thead === false) { 
				row += '</tr>';									
				
			}
        }

        return row;	
    };
	
	 // second iteration objects inside table.
	 $.fn.createTable.parseDataElement = function (data,id) {
		 
		var additionalRow = ''; 
		 
		return additionalRow;
	 }
	

    $.fn.getObjectLength = function (object) {

        var length = 0;

        for (var key in object) {
            if (object.hasOwnProperty(key)) {
                ++length;
            }
        }

        return length;
    };

    $.fn.humanize = function (text) {

        var string = text.split('_');

        for (i = 0; i < string.length; i++) {
            string[i] = string[i].charAt(0).toUpperCase() + string[i].slice(1);
        }

        return string.join(' ');
    };

    $.fn.createTable.defaults = {
        borderWidth: '1px',
        borderStyle: 'solid',
        borderColor: '#DDDDDD',
        fontFamily: 'Poppins, Arial, sans-serif',

        thBg: '#F3F3F3',
        thColor: '#0E0E0E',
        thHeight: '30px',
        thFontFamily: 'Poppins, Arial, sans-serif',
        thFontSize: '14px',
        thTextTransform: 'capitalize',

        trBg: '#FFFFFF',
        trColor: '#0E0E0E',
        trHeight: '25px',
        trFontFamily: 'Poppins, Arial, sans-serif',
        trFontSize: '13px',

        tdPaddingLeft: '10px',
        tdPaddingRight: '10px'
    }

}(jQuery));