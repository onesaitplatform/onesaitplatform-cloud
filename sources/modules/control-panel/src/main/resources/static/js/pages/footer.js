var footerTheme = function (currentLanguage){
	var csrf_value = $("meta[name='_csrf']").attr("content");
	var csrf_header = $("meta[name='_csrf_header']").attr("content");
	$.ajax({
		url : "/controlpanel/themes/getThemeJson",
		type : "GET",
		async : true,
		headers: {
			[csrf_header]: csrf_value
	    },
		success : function(response){
			switch (currentLanguage){
				case "en":
					if (response.FOOTER_TEXT != null && response.FOOTER_TEXT != ""){
						$('#footerText_original').attr('class', 'hide');
						$('#footerText').append("<span class='m-link m-link--metal'>"+response.FOOTER_TEXT+"</span>");
					}
					break;
				case "es":
					if (response.FOOTER_TEXT_ES != null && response.FOOTER_TEXT_ES != ""){
						$('#footerText_original').attr('class', 'hide');
						$('#footerText').append("<span class='m-link m-link--metal'>"+response.FOOTER_TEXT_ES+"</span>");
					}
					break;
				default:
					break;
			}
		}
	})
}