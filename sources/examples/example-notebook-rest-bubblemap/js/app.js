var AppController = function(){
	var baseURL = document.URL.split(window.location.pathname);
	var apimanager = '/api-manager/oauth/token';
	var notebookOpsBaseUrl = '/controlpanel/notebook-ops/';
	var executedOnce = false;
	if(baseURL.indexOf("localhost") > -1 || baseURL.indexOf("file://") > -1){
		apimanager = "http://localhost:19100" + apimanager;
		notebookOpsBaseUrl = "http://localhost:18000" + notebookOpsBaseUrl;
	}else{
		apimanager = baseURL[0] + apimanager;
		notebookOpsBaseUrl = baseURL[0] + notebookOpsBaseUrl;
	}
	
	var authenticated = false;
	var accessToken;
	
	var paragraphId = "20180530-084740_1247049587";
	var paragraphWithParam ="20180528-194132_115834276";
	var mapJson;
	var beautify = function(){
		editor.setValue(js_beautify(editor.getValue()));
	};
	var executeNotebook = function() {
		if(!$('.json-editor').hasClass('hide')){
			$('.json-editor').addClass('hide')
		}
		var srcId = {
			'params' :
			{
				'Depart Airport IATA' : $('#srcId').val()
			}
		}
		var ntId = $('#ntId').val();
		$('#runIcon').removeClass('icon icon-play').addClass('icon icon-refresh icon-spin');
		if(!executedOnce){
			$.ajax({
			'url' : notebookOpsBaseUrl + "run/notebook/" + ntId,
			'type' : 'POST',
			'dataType' : 'json',
			'headers' : {
				'Authorization' : accessToken,
				
			},
			'data':srcId,
			'success' : function(result) {
				$.ajax({
					'url' : notebookOpsBaseUrl + "run/notebook/" + ntId + "/paragraph/" + paragraphWithParam,
					'type' : 'POST',
					'contentType': 'application/json',
					'dataType' : 'json',
					'headers' : {
						'Authorization' : accessToken
					},
					'data': JSON.stringify(srcId),
					'success' : function(result) {				
						$.ajax({
					'url' : notebookOpsBaseUrl + "run/notebook/" + ntId + "/paragraph/" + paragraphId,
					'type' : 'POST',
					'contentType': 'application/json',
					'dataType' : 'json',
					'headers' : {
						'Authorization' : accessToken
					},
					'success' : function(result) {				
						$.ajax({
							'url' : notebookOpsBaseUrl + "result/notebook/" + ntId + "/paragraph/" + paragraphId,
							'type' : 'GET',
							'dataType' : 'json',
							'headers' : {
								'Authorization' : accessToken
							},
							'success' : function(result) {				
								$("#main-content").show();
								$('#title').html('Notebook data <span class="badge" id="notebookLoaded">'+$('#ntId').val()+'</span');
								mapJson = JSON.parse(result.body.results.msg[0].data);
								$('#runIcon').removeClass('icon icon-refresh icon-spin').addClass('icon icon-play');
								$('.json-editor').removeClass('hide');
								$('#notebookLoaded').text(ntId);
								editor.setValue(result.body.results.msg[0].data);
								beautify();
								loadBubbleMap(mapJson);
								executedOnce = true;
							},
							'error' : function(req, status, err) {
								console.log('Could not get paragraph info ' + paragraphId,
										req.responseText, status, err);
							}

						});
					},
					'error' : function(req, status, err) {
						console.log('Could not run paragraph ' + paragraphId,
								req.responseText, status, err);
						$.alert('Could not run paragraph ' + paragraphId,
								req.responseText,"Alert");

					}

				});
					},
					'error' : function(req, status, err) {
						console.log('Could not run paragraph ' + paragraphId,
								req.responseText, status, err);
						$.alert('Could not run paragraph ' + paragraphId,
								req.responseText,"Alert");
					}

				});

			},
			'error' : function(req, status, err) {
				console.log('Could not execute notebook ' + ntId,
						req.responseText, status, err);
						$.alert('Could not execute notebook ' + ntId,
						req.responseText,"Alert");
						
				//mockup
				$("#main-content").show();	
				var mockJson = '[{"city":"Tokyo","country":"Japan","lon":"140.386001587","IATA":"NRT","lat":"35.7647018433","z":10},{"city":"Bangkok","country":"Thailand","lon":"100.74700164794922","IATA":"BKK","lat":"13.681099891662598","z":10},{"city":"Hong Kong","country":"Hong Kong","lon":"113.915000916","IATA":"HKG","lat":"22.3089008331","z":10},{"city":"Cebu","country":"Philippines","lon":"123.97899627686","IATA":"CEB","lat":"10.307499885559","z":7},{"city":"Osaka","country":"Japan","lon":"135.24400329589844","IATA":"KIX","lat":"34.42729949951172","z":7},{"city":"San Francisco","country":"United States","lon":"-122.375","IATA":"SFO","lat":"37.61899948120117","z":7},{"city":"Taipei","country":"Taiwan","lon":"121.233002","IATA":"TPE","lat":"25.0777","z":7},{"city":"Fukuoka","country":"Japan","lon":"130.4510040283203","IATA":"FUK","lat":"33.585899353027344","z":7},{"city":"Nagoya","country":"Japan","lon":"136.80499267578125","IATA":"NGO","lat":"34.8583984375","z":6},{"city":"Manila","country":"Philippines","lon":"121.019997","IATA":"MNL","lat":"14.5086","z":6},{"city":"Jinan","country":"China","lon":"117.21600341796875","IATA":"TNA","lat":"36.857200622558594","z":6},{"city":"Qingdao","country":"China","lon":"120.374000549","IATA":"TAO","lat":"36.2661018372","z":6},{"city":"Yantai","country":"China","lon":"121.37200164794922","IATA":"YNT","lat":"37.40169906616211","z":5},{"city":"Vladivostok","country":"Russia","lon":"132.1479949951172","IATA":"VVO","lat":"43.39899826049805","z":5},{"city":"Singapore","country":"Singapore","lon":"103.994003","IATA":"SIN","lat":"1.35019","z":5},{"city":"Shanghai","country":"China","lon":"121.80500030517578","IATA":"PVG","lat":"31.143400192260742","z":5},{"city":"Los Angeles","country":"United States","lon":"-118.4079971","IATA":"LAX","lat":"33.94250107","z":5},{"city":"Kota Kinabalu","country":"Malaysia","lon":"116.0510025024414","IATA":"BKI","lat":"5.9372100830078125","z":4},{"city":"Tianjin","country":"China","lon":"117.346000671","IATA":"TSN","lat":"39.124401092499994","z":4},{"city":"Beijing","country":"China","lon":"116.58499908447266","IATA":"PEK","lat":"40.080101013183594","z":4},{"city":"Sydney","country":"Australia","lon":"151.177001953125","IATA":"SYD","lat":"-33.94609832763672","z":4},{"city":"Honolulu","country":"United States","lon":"-157.9219970703125","IATA":"HNL","lat":"21.318700790405273","z":4},{"city":"Changcha","country":"China","lon":"113.220001221","IATA":"CSX","lat":"28.189199447599997","z":4},{"city":"Dalian","country":"China","lon":"121.53900146484375","IATA":"DLC","lat":"38.9656982421875","z":4},{"city":"Seattle","country":"United States","lon":"-122.30899810791016","IATA":"SEA","lat":"47.44900131225586","z":4},{"city":"Dallas-Fort Worth","country":"United States","lon":"-97.03800201416016","IATA":"DFW","lat":"32.89680099487305","z":4},{"city":"Yanji","country":"China","lon":"129.451004028","IATA":"YNJ","lat":"42.8828010559","z":4},{"city":"Phuket","country":"Thailand","lon":"98.3169021606","IATA":"HKT","lat":"8.11320018768","z":4},{"city":"Sapporo","country":"Japan","lon":"141.69200134277344","IATA":"CTS","lat":"42.77519989013672","z":4},{"city":"Siem-reap","country":"Cambodia","lon":"103.81300354","IATA":"REP","lat":"13.410699844400002","z":4},{"city":"New York","country":"United States","lon":"-73.77890015","IATA":"JFK","lat":"40.63980103","z":4},{"city":"Abu Dhabi","country":"United Arab Emirates","lon":"54.651100158691406","IATA":"AUH","lat":"24.433000564575195","z":3},{"city":"Frankfurt","country":"Germany","lon":"8.5705556","IATA":"FRA","lat":"50.0333333","z":3},{"city":"Istanbul","country":"Turkey","lon":"28.814599990799998","IATA":"IST","lat":"40.9768981934","z":3},{"city":"Yangon","country":"Burma","lon":"96.1332015991","IATA":"RGN","lat":"16.907300949099998","z":3},{"city":"Chengdu","country":"China","lon":"103.9469985961914","IATA":"CTU","lat":"30.578500747680664","z":3},{"city":"Paris","country":"France","lon":"2.54999995232","IATA":"CDG","lat":"49.0127983093","z":3},{"city":"Kaohsiung","country":"Taiwan","lon":"120.3499984741211","IATA":"KHH","lat":"22.57710075378418","z":3},{"city":"Wuhan","country":"China","lon":"114.20800018310547","IATA":"WUH","lat":"30.78380012512207","z":3},{"city":"Hanoi","country":"Vietnam","lon":"105.80699920654297","IATA":"HAN","lat":"21.221200942993164","z":3},{"city":"Kuala Lumpur","country":"Malaysia","lon":"101.70999908447","IATA":"KUL","lat":"2.745579957962","z":3},{"city":"Macau","country":"Macau","lon":"113.59200286865234","IATA":"MFM","lat":"22.149599075317383","z":3},{"city":"Tashkent","country":"Uzbekistan","lon":"69.2811965942","IATA":"TAS","lat":"41.257900238","z":3},{"city":"Danang","country":"Vietnam","lon":"108.1989974975586","IATA":"DAD","lat":"16.043899536132812","z":3},{"city":"Ho Chi Minh City","country":"Vietnam","lon":"106.652000427","IATA":"SGN","lat":"10.8187999725","z":3},{"city":"Okinawa","country":"Japan","lon":"127.646003723","IATA":"OKA","lat":"26.1958007812","z":3},{"city":"Chiang Mai","country":"Thailand","lon":"98.962600708","IATA":"CNX","lat":"18.766799926799997","z":3},{"city":"Vancouver","country":"Canada","lon":"-123.183998108","IATA":"YVR","lat":"49.193901062","z":3},{"city":"Yuzhno-sakhalinsk","country":"Russia","lon":"142.71800231933594","IATA":"UUS","lat":"46.88869857788086","z":3},{"city":"London","country":"United Kingdom","lon":"-0.461941","IATA":"LHR","lat":"51.4706","z":3},{"city":"Denpasar","country":"Indonesia","lon":"115.16699981689","IATA":"DPS","lat":"-8.7481698989868","z":3},{"city":"Jakarta","country":"Indonesia","lon":"106.65599823","IATA":"CGK","lat":"-6.1255698204","z":3},{"city":"Hiroshima","country":"Japan","lon":"132.919006348","IATA":"HIJ","lat":"34.4361000061","z":3},{"city":"Kalibo","country":"Philippines","lon":"122.375999451","IATA":"KLO","lat":"11.679400444","z":3},{"city":"Guangzhou","country":"China","lon":"113.29900360107422","IATA":"CAN","lat":"23.39240074157715","z":3},{"city":"Chicago","country":"United States","lon":"-87.90480042","IATA":"ORD","lat":"41.97859955","z":3},{"city":"Xi\u0027an","country":"China","lon":"108.75199890136719","IATA":"XIY","lat":"34.44710159301758","z":3},{"city":"Tokyo","country":"Japan","lon":"139.779999","IATA":"HND","lat":"35.552299","z":3},{"city":"Agana","country":"Guam","lon":"144.796005249","IATA":"GUM","lat":"13.4834003448","z":3},{"city":"Shenzhen","country":"China","lon":"113.81099700927734","IATA":"SZX","lat":"22.639299392700195","z":3},{"city":"Khabarovsk","country":"Russia","lon":"135.18800354004","IATA":"KHV","lat":"48.52799987793","z":2},{"city":"Toyama","country":"Japan","lon":"137.18800354003906","IATA":"TOY","lat":"36.64830017089844","z":2},{"city":"Vientiane","country":"Laos","lon":"102.56300354","IATA":"VTE","lat":"17.988300323500003","z":2},{"city":"Changchun","country":"China","lon":"125.684997559","IATA":"CGQ","lat":"43.9962005615","z":2},{"city":"Niigata","country":"Japan","lon":"139.121002197","IATA":"KIJ","lat":"37.9558982849","z":2},{"city":"Dubai","country":"United Arab Emirates","lon":"55.3643989563","IATA":"DXB","lat":"25.2527999878","z":2},{"city":"Las Vegas","country":"United States","lon":"-115.1520004","IATA":"LAS","lat":"36.08010101","z":2},{"city":"Amsterdam","country":"Netherlands","lon":"4.763889789579999","IATA":"AMS","lat":"52.3086013794","z":2},{"city":"Chongqing","country":"China","lon":"106.64199829101562","IATA":"CKG","lat":"29.719200134277344","z":2},{"city":"Kagoshima","country":"Japan","lon":"130.718994140625","IATA":"KOJ","lat":"31.80340003967285","z":2},{"city":"Xiamen","country":"China","lon":"118.12799835205078","IATA":"XMN","lat":"24.54400062561035","z":2},{"city":"Ulan Bator","country":"Mongolia","lon":"106.76699829101562","IATA":"ULN","lat":"47.843101501464844","z":2},{"city":"Washington","country":"United States","lon":"-77.45580292","IATA":"IAD","lat":"38.94449997","z":2},{"city":"Guilin","country":"China","lon":"110.03900146484375","IATA":"KWL","lat":"25.21809959411621","z":2},{"city":"Nairobi","country":"Kenya","lon":"36.9277992249","IATA":"NBO","lat":"-1.31923997402","z":2},{"city":"Shenyang","country":"China","lon":"123.48300170898438","IATA":"SHE","lat":"41.639801025390625","z":2},{"city":"Moscow","country":"Russia","lon":"37.4146","IATA":"SVO","lat":"55.972599","z":2},{"city":"Miyazaki","country":"Japan","lon":"131.449005127","IATA":"KMI","lat":"31.877199173","z":2},{"city":"Mudanjiang","country":"China","lon":"129.569000244","IATA":"MDG","lat":"44.5241012573","z":2},{"city":"Nanjing","country":"China","lon":"118.86199951171875","IATA":"NKG","lat":"31.742000579833984","z":2},{"city":"Alma-ata","country":"Kazakhstan","lon":"77.04049682617188","IATA":"ALA","lat":"43.35210037231445","z":2},{"city":"Huangshan","country":"China","lon":"118.25599670410156","IATA":"TXN","lat":"29.733299255371094","z":2},{"city":"Harbin","country":"China","lon":"126.25","IATA":"HRB","lat":"45.6234016418457","z":2},{"city":"Miho","country":"Japan","lon":"133.23599243164062","IATA":"YGJ","lat":"35.4921989440918","z":2},{"city":"Kanazawa","country":"Japan","lon":"136.40699768066406","IATA":"KMQ","lat":"36.39459991455078","z":2},{"city":"Delhi","country":"India","lon":"77.10310363769531","IATA":"DEL","lat":"28.566499710083008","z":2},{"city":"Milano","country":"Italy","lon":"8.72811","IATA":"MXP","lat":"45.6306","z":2},{"city":"Kumamoto","country":"Japan","lon":"130.85499572753906","IATA":"KMJ","lat":"32.83729934692383","z":2},{"city":"Atlanta","country":"United States","lon":"-84.4281005859375","IATA":"ATL","lat":"33.63669967651367","z":2},{"city":"Prague","country":"Czech Republic","lon":"14.26","IATA":"PRG","lat":"50.1008","z":2},{"city":"Zhengzhou","country":"China","lon":"113.841003418","IATA":"CGO","lat":"34.519699096699995","z":2},{"city":"Kunming","country":"China","lon":"102.9291667","IATA":"KMG","lat":"25.1019444","z":2},{"city":"Phnom-penh","country":"Cambodia","lon":"104.84400177001953","IATA":"PNH","lat":"11.546600341796875","z":2},{"city":"Sendai","country":"Japan","lon":"140.917007446","IATA":"SDJ","lat":"38.1397018433","z":2},{"city":"Hangzhou","country":"China","lon":"120.43399810791016","IATA":"HGH","lat":"30.22949981689453","z":2},{"city":"Takamatsu","country":"Japan","lon":"134.01600647","IATA":"TAK","lat":"34.214199066199996","z":2},{"city":"Cheju","country":"South Korea","lon":"126.49299621582031","IATA":"CJU","lat":"33.51129913330078","z":2},{"city":"Matsuyama","country":"Japan","lon":"132.6999969482422","IATA":"MYJ","lat":"33.82720184326172","z":2},{"city":"Babelthuap","country":"Palau","lon":"134.54400634765625","IATA":"ROR","lat":"7.367650032043457","z":2},{"city":"Angeles City","country":"Philippines","lon":"120.559997559","IATA":"CRK","lat":"15.1859998703","z":2},{"city":"Helsinki","country":"Finland","lon":"24.963300704956","IATA":"HEL","lat":"60.317199707031","z":1},{"city":"Nagasaki","country":"Japan","lon":"129.914001465","IATA":"NGS","lat":"32.916900634799994","z":1},{"city":"Brisbane","country":"Australia","lon":"153.11700439453125","IATA":"BNE","lat":"-27.384199142456055","z":1},{"city":"Taegu","country":"South Korea","lon":"128.658996582","IATA":"TAE","lat":"35.894100189199996","z":1},{"city":"Vienna","country":"Austria","lon":"16.569700241089","IATA":"VIE","lat":"48.110298156738","z":1},{"city":"Oita","country":"Japan","lon":"131.736999512","IATA":"OIT","lat":"33.479400634799994","z":1},{"city":"Saga","country":"Japan","lon":"130.302001953","IATA":"HSG","lat":"33.149700164799995","z":1},{"city":"Saipan","country":"Northern Mariana Islands","lon":"145.729003906","IATA":"SPN","lat":"15.119000434899998","z":1},{"city":"Akita","country":"Japan","lon":"140.218994140625","IATA":"AXT","lat":"39.6156005859375","z":1},{"city":"St. Petersburg","country":"Russia","lon":"30.262500762939453","IATA":"LED","lat":"59.80030059814453","z":1},{"city":"Houston","country":"United States","lon":"-95.34140014648438","IATA":"IAH","lat":"29.984399795532227","z":1},{"city":"Ninbo","country":"China","lon":"121.46199798583984","IATA":"NGB","lat":"29.82670021057129","z":1},{"city":"Munich","country":"Germany","lon":"11.786100387573","IATA":"MUC","lat":"48.353801727295","z":1},{"city":"Mumbai","country":"India","lon":"72.8678970337","IATA":"BOM","lat":"19.0886993408","z":1},{"city":"Hefei","country":"China","lon":"117.2979965209961","IATA":"HFE","lat":"31.780000686645508","z":1},{"city":"Toronto","country":"Canada","lon":"-79.63059997559999","IATA":"YYZ","lat":"43.6772003174","z":1},{"city":"Nandi","country":"Fiji","lon":"177.4429931640625","IATA":"NAN","lat":"-17.755399703979492","z":1},{"city":"Lijiang","country":"China","lon":"100.246002197","IATA":"LJG","lat":"26.6800003052","z":1},{"city":"Tel-aviv","country":"Israel","lon":"34.88669967651367","IATA":"TLV","lat":"32.01139831542969","z":1},{"city":"Riyadh","country":"Saudi Arabia","lon":"46.69879913330078","IATA":"RUH","lat":"24.957599639892578","z":1},{"city":"Auckland","country":"New Zealand","lon":"174.792007446","IATA":"AKL","lat":"-37.008098602299995","z":1},{"city":"Madrid","country":"Spain","lon":"-3.56264","IATA":"MAD","lat":"40.471926","z":1},{"city":"Yamaguchi","country":"Japan","lon":"131.279006958","IATA":"UBJ","lat":"33.930000305200004","z":1},{"city":"Colombo","country":"Sri Lanka","lon":"79.88410186767578","IATA":"CMB","lat":"7.180759906768799","z":1},{"city":"Aomori","country":"Japan","lon":"140.6909942626953","IATA":"AOJ","lat":"40.73469924926758","z":1},{"city":"Detroit","country":"United States","lon":"-83.35340118408203","IATA":"DTW","lat":"42.212398529052734","z":1},{"city":"Nanchang","country":"China","lon":"115.9000015258789","IATA":"KHN","lat":"28.864999771118164","z":1},{"city":"Okayama","country":"Japan","lon":"133.854995728","IATA":"OKJ","lat":"34.7569007874","z":1},{"city":"Kathmandu","country":"Nepal","lon":"85.35910034179999","IATA":"KTM","lat":"27.6965999603","z":1}]';				
				mapJson = JSON.parse(mockJson);
				$('#runIcon').removeClass('icon icon-refresh icon-spin').addClass('icon icon-play');
				$('.json-editor').removeClass('hide');
				$('#notebookLoaded').text(ntId);
				editor.setValue(mockJson);
				beautify();				
				loadBubbleMap(mapJson);

			}

		});
		}else{
			$.ajax({
					'url' : notebookOpsBaseUrl + "run/notebook/" + ntId + "/paragraph/" + paragraphWithParam,
					'type' : 'POST',
					'contentType': 'application/json',
					'dataType' : 'json',
					'headers' : {
						'Authorization' : accessToken
					},
					'data': JSON.stringify(srcId),
					'success' : function(result) {				
						$.ajax({
					'url' : notebookOpsBaseUrl + "run/notebook/" + ntId + "/paragraph/" + paragraphId,
					'type' : 'POST',
					'contentType': 'application/json',
					'dataType' : 'json',
					'headers' : {
						'Authorization' : accessToken
					},
					'success' : function(result) {				
						$.ajax({
							'url' : notebookOpsBaseUrl + "result/notebook/" + ntId + "/paragraph/" + paragraphId,
							'type' : 'GET',
							'dataType' : 'json',
							'headers' : {
								'Authorization' : accessToken
							},
							'success' : function(result) {				
								$("#main-content").show();
								$('#title').html('Notebook data <span class="badge" id="notebookLoaded">'+$('#ntId').val()+'</span');
								mapJson = JSON.parse(result.body.results.msg[0].data);
								$('#runIcon').removeClass('icon icon-refresh icon-spin').addClass('icon icon-play');
								$('.json-editor').removeClass('hide');
								$('#notebookLoaded').text(ntId);
								editor.setValue(result.body.results.msg[0].data);
								beautify();
								loadBubbleMap(mapJson);
								executedOnce = true;
							},
							'error' : function(req, status, err) {
								console.log('Could not get paragraph info ' + paragraphId,
										req.responseText, status, err);
							}

						});
					},
					'error' : function(req, status, err) {
						console.log('Could not run paragraph ' + paragraphId,
								req.responseText, status, err);
						$.alert('Could not run paragraph ' + paragraphId,
								req.responseText,"Alert");
					}

				});
					},
					'error' : function(req, status, err) {
						console.log('Could not run paragraph ' + paragraphId,
								req.responseText, status, err);
						$.alert('Could not run paragraph ' + paragraphId,
								req.responseText,"Alert");
					}

				});
		}

	}
	var login = function (){

		//var username = $("#userName").val();
		//var password = $("#userPassword").val();
		var username = 'analytics';
		var password = 'Changed!';
		// The auth_token is the base64 encoded string for the API 
		// application.
		var auth_token = 'onesaitplatform:onesaitplatform';
		auth_token = window.btoa(auth_token);
		var requestPayload = {
			// Enter your inContact credentials for the 'username' and 
			// 'password' fields.
			'grant_type' : 'password',
			'username' : username,
			'password' : password
		}
		$.ajax({
			'url' : apimanager,
			'type' : 'POST',
			'content-Type' : 'x-www-form-urlencoded',
			'dataType' : 'json',
			'headers' : {
				'Authorization' : 'Basic ' + auth_token
			},
			'data' : requestPayload,
			'success' : function(result) {
				
				accessToken = result.access_token;
				if(accessToken != null) {
					accessToken = "Bearer " + accessToken;
					authenticated = true;
					//$('#login').addClass('hide');
					//$('#execute-nt').show();
				}
				return result;
			},
			'error' : function(req, status, err) {
				console.log('something went wrong on Login...',
						req.responseText, status, err);
						$.alert('something went wrong on Login...',
						req.responseText,"Alert");

			}

		});
		

	};
	
	
	var handleCodeMirror = function () {
		
        var myTextArea = document.getElementById('json');
        editor = CodeMirror.fromTextArea(myTextArea, {
        	mode: "application/ld+json",
        	autoCloseBrackets: true,
            matchBrackets: true,
            styleActiveLine: true,
            theme:"ambiance",
            lineWrapping: true

        });
		 editor.setSize(null, 424);
    };

	return{

		init: function(){
			//$("#btn-login").on('click', function(){	login();});
			login();
			$("#execute").on('click', function(){
				executeNotebook();
			});
			
			handleCodeMirror();
			 $('.popovers').popover();
		},
		getToken : function(){
			return accessToken;
		},
		beautify : function(){
			beautify();
		}


	};
}();

//AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	AppController.init();
	
});



