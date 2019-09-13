var FrontendController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Frontend'; 
	var LIB_TITLE = 'Frontend Configuration Controller';	
    var logControl = 1;
	var LANGUAGE = ['en'];
	var currentLanguage = ''; // loaded from template.
	var currentYear = new Date().getFullYear();
	
	// CONTROLLER PRIVATE FUNCTIONS ---------------------------------------------------------
	
	
	// Login using oauth Server 
	var doLogin = function(){
		
		let username = $("input#username").val() || '';
		let password = $("input#password").val() || '';
		let accessToken = '';
		let loggedUser	= '';
		let loggedEmail	= '';
		
		if (!username || !password) { $.alert({title: 'Login:', icon:'la la-warning', theme:'light', type:'red', content: 'Fill login and password!'}); return false; }
		
		// api endpoint
		var url_login = frontReg.access.urlBase + '/oauth-server/oauth/token';

		// auth_token base64
		var auth_token = 'onesaitplatform:onesaitplatform';	
		auth_token = window.btoa(auth_token);
		
		// json data
		var requestPayload = {'grant_type' : 'password', 'username' : username, 'password' : password};
		
		// api call
		$.ajax({'url': url_login, 'type':'POST', 'content-Type': 'x-www-form-urlencoded', 'headers': {'Authorization' : 'Basic ' + auth_token}, 'data' : requestPayload,

			'success': function(result) {			
				accessToken = result.access_token;			
				saveSession(accessToken);
			},
			'error' : function(req, status, err) {
					$.alert({title: 'Login error:' + err, icon:'la la-warning', theme:'light', type:'red', content: 'Something went wrong on login: ' + req.responseText});
					return false
			},
			'complete': function(){
				
				$.ajax({
					type : "GET",					
					url: frontReg.access.urlBase + "/controlpanel/management/users/" + $("input#username").val(),
					dataType : 'json',
					contentType : 'application/json',															
					headers : {
						"Authorization": 'Bearer ' + accessToken						
					},				
					success : function(response) {						
						if (response){			
							var extraFields = response.hasOwnProperty(extraFields) ? JSON.parse(response.extraFields) : '';
							loggedUser	= response.username || '';
							loggedEmail	= response.mail || '';
							if ( loggedUser && loggedEmail ){ sessionStorage.setItem('user',loggedUser); sessionStorage.setItem('email', loggedEmail); }
							location.href = frontReg.access.urlBasePath + "index.html";	
						}					
					},
					error : function(req, status, err) {
						console.log('Something went wrong getting user extended Info.',req.responseText, status, err);						
					}					
				})				
			}
		})		
	}
	
	
	// get Current Page loaded
	var getCurrentPage = function(){
		let templ = top.location.pathname ? top.location.pathname : window.location.pathname;
		let page  = templ.split("/").pop();		
		return page;		
	}
	
	
	// store user data in session
	var saveSession = function(accessToken){
		sessionStorage.setItem('accessToken', accessToken);				
	}	
	
	
	// clear session and go to Login.
	var closeSession = function(){
		sessionStorage.clear();
		window.location.href = frontReg.access.urlBasePath + "login.html";				
	}
	
	
	// check if the user data are stored in session if not, closeSession.
	var hasSession = function(access){		
		
		var page = getCurrentPage();		
		if ( page === 'login.html'){ return false; }		
		if ( access === 'PRIVATE'){
			if( sessionStorage.getItem("accessToken") ){
				$('.loading').fadeOut('slow');
				// to-do: session conf.
			}
			else {
				$('.loading').fadeIn('slow');
				closeSession(); 
			}
		} else { $('.loading').fadeOut('slow'); return true; }
	}
	
	
	// Get API authorization
	var getAuthorization = function(){		
		return sessionStorage.getItem('accessToken') || '';		
	}
	
	
	// Configure all elements from page: Access, info, header, initial content, footer,...
	var handleConfiguration = function(configuration){			
			
		logControl ? console.log('|---> handleConfiguration() -> Configuration of Frontend.') : '';
		
		// no configuration, no fun!
		if (!configuration){ $.alert({title: 'Configuration ERROR!',content: 'No Configuration Data!'}); return false; }
		
		// loading image conf.
		configuration.app.appLoading !== '' ? $('#app-loading').attr('src',configuration.app.appLoading) : '';

		// check Access in session
		hasSession(configuration.access.entry);
		
		// page settings after checkAccess
		document.title = configuration.title || 'FRONTEND';
		document.head.querySelector("[name='description']").content = configuration.description || 'FRONTEND Description';
		$('#app-home').text(configuration.app.appHome || 'FRONTEND');
		$('#content-title').text(configuration.content.contentTitle || 'FRONTEND BASE CARD');
		if ( configuration.app.appLogo != ''){
			$('#app-logo').attr('src',configuration.app.appLogo);
			configuration.app.appLogoCss ? $('#app-logo').attr('style',configuration.app.appLogoCss) : '';
			configuration.app.appLogoBackground ? $('.m-brand__logo').attr('style',configuration.app.appLogoBackground) : '';
		}
		
		// check exception login page
		var page = getCurrentPage();
		if ( page === 'login.html'){ handleLogin(configuration); return false; }	
		
			
		// element visualization after check exception
		configuration.app.appFooter				 ? $('#app-footer').removeClass('m--hide')				: $('#app-footer').addClass('m--hide');
		configuration.app.appStickymenu 		 ? $('#app-stickymenu').removeClass('m--hide')			: $('#app-stickymenu').addClass('m--hide');
		configuration.header.headerDashboads	 ? $('#header-dashboards').removeClass('m--hide')		: $('#header-dashboards').addClass('m--hide');
		configuration.header.headerReports		 ? $('#header-reports').removeClass('m--hide')			: $('#header-reports').addClass('m--hide');
		configuration.header.headerSearch		 ? $('#m_quicksearch').removeClass('m--hide')			: $('#m_quicksearch').addClass('m--hide');
		configuration.header.headerNotifications ? $('#header-notifications').removeClass('m--hide') 	: $('#header-notifications').addClass('m--hide');
		configuration.header.headerQuickactions	 ? $('#header-quickactions').removeClass('m--hide')		: $('#header-quickactions').addClass('m--hide');
		configuration.header.headerUser			 ? $('#header-user').removeClass('m--hide')				: $('#header-user').addClass('m--hide');
		configuration.header.headerSidebarToggle ? $('#m_quick_sidebar_toggle').removeClass('m--hide')	: $('#m_quick_sidebar_toggle').addClass('m--hide');
		if ( configuration.content.contentHead ) {
			$('#content-head').removeClass('m--hide');
			configuration.content.contentHeadCss !== '' ? $('#content-head').attr('style', configuration.content.contentHeadCss ) : '';
			configuration.content.contentTitleCss !== '' ? $('h3.m-portlet__head-text').attr('style', configuration.content.contentTitleCss) : '';
		}
		else {
			$('#content-head').addClass('m--hide');	
		}
		configuration.content.contentTools		 ? $('#content-tools').removeClass('m--hide')			: $('#content-tools').addClass('m--hide');
		
		// loading initial Dashboard.
		if ( configuration.content.contentDashboard.enabled ){
			let dashboard = configuration.content.contentDashboard;			
			getDashboard(dashboard.src, dashboard.dashboardName, dashboard.background, dashboard.height, dashboard.mode);
		} else { $('#list-portlet').addClass('m--hide'); }		
		
		// wellcome msg
		configuration.app.appWelcome ? toastr.info('Wellcome to ' + configuration.app.appHome + ' :)', configuration.app.appHome +':') : '';		
		
		// user Elements
		if ( configuration.header.headerUser ){	
		
			configuration.user.showAvatar ? configuration.user.avatar ? $('.m-card-user__pic').find('img').attr('src',configuration.user.avatar) : $('.m-card-user').removeClass('m--hide') : $('.m-card-user').addClass('m--hide');
		
			// Profile
			let profile = configuration.user.profile;
			profile.visible == true ? $('#user-profile').removeClass('m--hide').find('a.m-nav__link').attr('href',profile.link).find('span.m-nav__link-text').text(profile.text) : $('#user-profile').addClass('m--hide');
			
			// support
			let support = configuration.user.support;
			support.visible == true ? $('#user-support').removeClass('m--hide').find('a.m-nav__link').attr('href',support.link).find('span.m-nav__link-text').text(support.text) : $('#user-support').addClass('m--hide');
			
			// Activity
			let activity = configuration.user.activity;
			activity.visible == true ? $('#user-activity').removeClass('m--hide').find('a.m-nav__link').attr('href',activity.link).find('span.m-nav__link-text').text(activity.text) : $('#user-activity').addClass('m--hide');
			
			// messages
			let messages = configuration.user.messages;
			messages.visible == true ? $('#user-messages').removeClass('m--hide').find('a.m-nav__link').attr('href',messages.link).find('span.m-nav__link-text').text(messages.text) : $('#user-messages').addClass('m--hide');
			
			// faq			
			let faq = configuration.user.faq;
			faq.visible == true ? $('#user-faq').removeClass('m--hide').find('a.m-nav__link').attr('href',faq.link).find('span.m-nav__link-text').text(faq.text) : $('#user-faq').addClass('m--hide');						
			
			// logout
			let logout = configuration.user.logout;
			logout.visible == true ? $('#user-logout').find('a.btn').text(logout.text) : $('#user-logout').addClass('m--hide');
			
			//user and email 			
			if (sessionStorage.getItem('user') && sessionStorage.getItem('email') ){
				$('.m-card-user__name').text(sessionStorage.getItem('user'));
				$('.m-card-user__email').text(sessionStorage.getItem('email'));
			}
		}
		
		// footer links and elements
		if ( configuration.app.appFooter ){			
			$('#footer-copyright').html(configuration.footer.footerCopyright || currentYear + ' &copy; FRONTEND');
			if ( configuration.footer.footerLinks ){
				
				// About
				let about = configuration.footer.footerLinkAbout;
				about.visible == true ? $('#footer-link-about').find('a.m-nav__link').attr('href',about.link).find('span').text(about.text): $('#footer-link-about').addClass('m--hide');
				
				// privacy 
				let privacy = configuration.footer.footerLinkPrivacy;
				privacy.visible == true ? $('#footer-link-privacy').find('a.m-nav__link').attr('href',privacy.link).find('span').text(privacy.text): $('#footer-link-privacy').addClass('m--hide');
				
				// terms 
				let terms = configuration.footer.footerLinkTerms;
				terms.visible == true ? $('#footer-link-terms').find('a.m-nav__link').attr('href',terms.link).find('span').text(terms.text): $('#footer-link-terms').addClass('m--hide');
				
				// company 
				let company = configuration.footer.footerLinkCompany;
				company.visible == true ? $('#footer-link-company').find('a.m-nav__link').attr('href',company.link).find('span').text(company.text): $('#footer-link-company').addClass('m--hide');
				
				// support
				let support = configuration.footer.footerLinkSupport;
				support.visible == true ? $('#footer-link-support').find('a.m-nav__link').attr('title',support.text).attr('data-original-title',support.text) : $('#footer-link-support').addClass('m--hide');
			}
			
		}
		// themes and styling		
		let themes = configuration.themes;
		themes.changeSkin 			!== ''	? changeTheme(configuration.currentSkin, themes.changeSkin): '';
		themes.contentPadding		!== ''	? $('#app-content').attr('style','padding: '+ themes.contentPadding + ';') : $('#app-content').attr('style','padding: 30px 30px;');
		themes.contentBackground	!== ''	? $('#app-content').attr('style', $('#app-content').attr('style') + ' background-color: ' + themes.contentBackground +';') : '';
		themes.menu 				!== ''	? changeMenu(configuration.currentSkin, themes.menu) : '';
		
		
						
	}	
	
	
	// Configure all elements in Login page: Access, info, header, initial content
	var handleLogin = function(configuration){
		
		logControl ? console.log('|---> handleLogin() -> Login Configuration.') : '';
		
		// no configuration, no fun!
		if (!configuration){ $.alert({title: 'Configuration ERROR!',content: 'No Configuration Data!'}); return false; }
		
		// conf Public or Private Mode
		if ( configuration.access.entry === 'PUBLIC'){			
			
			$('#m_login_visit').removeClass('m--hide');
			$('#m_login_signup').addClass('m--hide');
			$('.m-login__border,.m-login__wrapper-2').addClass('m--hide');			
		}
		else {
			// PRIVATE
			$('#m_login_visit').addClass('m--hide');
			$('#m_login_signup').removeClass('m--hide');
			$('.m-login__border,.m-login__wrapper-2').removeClass('m--hide');
		}		
		
		// conf. texts, image, background and texts
		configuration.login.loginLogo ? $('#login-logo').attr('src',configuration.login.loginLogo).attr('style',configuration.login.loginLogoStyle) : '';
		configuration.login.loginDescription ? $('#login-description').text(configuration.login.loginDescription) : '';
		configuration.login.signInTitle ? $('#signin-title').text(configuration.login.signInTitle) : '';
		configuration.login.loginBackground ? $('#m_login').attr('style','background: url('+ configuration.login.loginBackground +') no-repeat; background-position: center; background-size: cover;') : '';
		
		// remember me , signUp, forgot Passwords forms only on private mode.
		if ( configuration.access.entry === 'PRIVATE'){
			if ( configuration.login.signUp ){
				$('.m-login__signup').removeClass('m--hide');
				$('#m_login_signup').removeClass('m--hide');
			}
			else{
				$('.m-login__signup').addClass('m--hide');
				$('#m_login_signup').addClass('m--hide');
			}		
			if ( configuration.login.forgotPassword ){
				$('.m-login__forget-password').removeClass('m--hide')
				$('#forget-password').removeClass('m--hide');
			}
			else{
				$('.m-login__forget-password').addClass('m--hide');
				$('#forget-password').addClass('m--hide');
			}
			configuration.login.rememberMe ? $('#remember-me').removeClass('m--hide') : $('#remember-me').addClass('m--hide');
		}
			
	}
	
	
	// change Template Theme
	var changeTheme = function(currentSkin,newSkin){
	
		if ( newSkin === currentSkin ) { return false }
		if ( !isValidSkin(newSkin)) { newSkin = ''}
		
		// no skin, no fun!
		if ( !newSkin ){ $.alert({title: 'Configuration ERROR!',content: 'No Skin or not valid for Change Theme!'}); return; }
		
		// change all the elements in page to new skin.
				
	}
	
 
 
	// change menu theme light or dark one	
	var changeMenu = function(currentSkin, newSkin ){
		
		if ( newSkin === currentSkin ) { return false }
		if ( !isValidSkin(newSkin)) { newSkin = ''}
		
		// no skin, no fun!
		if ( !newSkin ){ $.alert({title: 'Configuration ERROR!',content: 'No Skin or not valid for Change Menu Theme!'}); return; }
		
		// change menu classes for new skin.		
		let elements = ['m-aside-left--','m-aside-menu--','m-aside-menu--submenu-'];
		let currentElement	= '';
		let newElement		=  '';
		$.each(elements,function(index){			
			currentElement	= elements[index] + currentSkin;
			newElement		= elements[index] + newSkin;
			$('body').find('.'+ currentElement).removeClass(currentElement).addClass(newElement);			
		})		
	}
	
	
	// check if skin to change is a valid defined skin on main json on availableSkins[]
	var isValidSkin = function(skin){
		let isValid = false;		
		let skins = frontReg.themes.availableSkin || [];		
		
		jQuery.inArray(skin, skins) < 0 ? isValid = false : isValid = true;		
		return isValid;
	}
		
	
	//  TOASTR NOTIFICATIONS OPTIONS
	toastr.options = {
		"closeButton": true,
		"debug": false,
		"newestOnTop": true,
		"progressBar": true,
		"positionClass": "toast-top-right",
		"preventDuplicates": true,
		"onclick": null,
		"showDuration": "300",
		"hideDuration": "1000",
		"timeOut": "5000",
		"extendedTimeOut": "1000",
		"showEasing": "swing",
		"hideEasing": "linear",
		"showMethod": "fadeIn",
		"hideMethod": "fadeOut"
	};

	
	// TOASTR NOTIFICATION  
	// type: INFO, SUCCESS, WARNING, ERROR, show different colors
	var notification = function( type , title, description ){
		
		let notificationType  = type || 'info';
		let notificationTitle = title ? title : 'Information:';
		let notificationDesc  = description ? description : '';
		var types = {
			'success':	'toastr.success(notificationDesc, notificationTitle)',
			'info':		'toastr.info(notificationDesc, notificationTitle)',
			'warning':	'toastr.warning(notificationDesc, notificationTitle)',
			'error':	'toastr.error(notificationDesc, notificationTitle)',
		};		
		showLog ? console.log('notification with type: ' + notificationType + ' title: ' + notificationTitle + ' and description: ' + notificationDesc) : '';		
		if (types[ notificationType ]) { eval(types[ notificationType ]);}	
	};
	
	
	// GET DASHBOARD AND LOAD ONTO PAGE
	var getDashboard = function(src, title, background, height, mode){
			
		let backgroundParam = background || '#FFF';
		let heightParam		= height || '800px';
		let modeParam		= mode || 'INSERT';
		let authorization	= getAuthorization();
		let srcParam		= src +'?oauthtoken=' + authorization;
		let dashboardName	= title || frontReg.content.contentDashboard.dashboardName || 'Dashboard';
		
		logControl ? console.log('|---> getDashboard() -> ' + srcParam + ' name: ' + dashboardName ) : '';
		$('.lds-ellipsis').removeClass('m--hide');
		
		if (!src){ 
			$('.lds-ellipsis').addClass('m--hide');
			$('#loading-info-error').removeClass('m--hide');			
			return false; 
		}
		
		// if there´s an dashboard allready loaded
		if ( $("#main-dashboard-iframe").attr('data-loaded') ){
			$('#main-dashboard-container').addClass('m--hide');
			$('.lds-ellipsis').removeClass('m--hide');
			
		}
		
		// notification
		frontReg.content.contentDashboard.notification ? toastr.success('The Dashboard ' + dashboardName + ' is loading...', 'Info:') : '';
		
		// iframe name
		dashboardName ? $('#content-title').text(dashboardName) : '';
		
		// iframe config.		
		$("#main-dashboard-iframe").attr('src', srcParam).attr('height',heightParam).attr('data-loaded',1).css('background-color', backgroundParam);		
		!$('#loading-info-error').hasClass('m--hide') ? $('#loading-info-error').addClass('m--hide') : '';
		
		return true;
	}


	// CONTROLLER PUBLIC FUNCTIONS --------------------------------------------------------- 
	return{
		
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) { 
			logControl ? console.log("\n" + LIB_TITLE + ': load()') : '';
			return frontReg = Data;
		},		
		
		
		// LANG() CONTROLLER INIT LANGUAGE
		lang: function(lang){
			logControl ? console.log(LIB_TITLE + ': lang()') : '';
			logControl ? console.log('|---> lang() -> assign current Language to Console Menu: ' + lang) : '';
			return currentLanguage = lang;			
		},		
		
		
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log("\n" +LIB_TITLE + ': init()') : '';
			
			// SETTING IFRAME CONTROL
				$('iframe').on('load', function(){
					$('#main-dashboard-container').removeClass('m--hide');
					$('.lds-ellipsis').addClass('m--hide');
				});
						
			// MAIN CONFIGURATION
			handleConfiguration(frontReg);			
		},
		
		
		// LOGIN() CONTROLLER LOGIN 
		login: function(){
			logControl ? console.log("\n" +LIB_TITLE + ': login()') : '';
			doLogin();			
		},
		
		
		// LOAD DASHBOARDS
		loadDashboard: function(src, title, background, height, mode){
			logControl ? console.log("\n" +LIB_TITLE + ': loadDashboard()') : '';
			src ? getDashboard(src, title, background, height, mode) : '';			
		},
		
		// CLOSE SESSION
		close: function(){
			logControl ? console.log("\n" +LIB_TITLE + ': close()') : '';
			closeSession();	
		}
	}
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONF, i18, ...)
	FrontendController.load(mainJson);
	
	// LOADING CURRENT LANGUAGE FROM THE TEMPLATE
	FrontendController.lang(currentLanguage);
	
	// AUTO INIT CONTROLLER.
	FrontendController.init();
	
});