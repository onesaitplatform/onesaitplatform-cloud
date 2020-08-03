var authServiceUrl='http://localhost:20100/flowengine/node/services/user/validate';


var http = require('http');
var querystring = require('querystring');
var httpProxy = require('http-proxy');
var cache = require('memory-cache');
var request = require('sync-request');

var proxyPort
var proxy;
var pathArray;



function serverProxy(_proxyPort, usersPorts) {


    proxyPort = _proxyPort;

    proxy = httpProxy.createProxyServer({});
    proxy.on('error', (e) => {
            console.log("Node-Red Manager. proxy-nodered.js. ERROR on proxyServer!  " + e );
    });

    var serverProxy = http.createServer(function(req, res) {

		var pathArray = req.url.split('/');
        var domain = pathArray[1];


        if (pathArray.length == 2) { //llega solo la raiz que debaria traer autenticacion
            domainArray = pathArray[1].split('?authentication='); //[ 'proyecto02', 'authentication=1:Sm4r7P14tf0rm!' ]

            if (domainArray.length == 2) {
                domain = domainArray[0];
                var authentication = domainArray[1];

                var user = domain + '-' + authentication;

                if ((cache.get('cachingUser') == null || cache.get('cachingUser') == undefined) || 
                	(cache.get('cachingUser')!=null && cache.get('cachingUser')!=user)) {


                    var respuesta = request('POST', authServiceUrl, {
                        json: {
                            'authentication': authentication,
                            'dominio': domain
                        }
                    });

		   console.log("Respuesta servicio autenticacion: "+respuesta.statusCode);

                    if (respuesta.statusCode == 200) {
                        cache.put('cachingUser', user, 1000, function(key, value) {
                            //console.log(key + '->' + value);
                        });
                    }
                    

                }

            } else {
                res.statusCode = 403;
                res.end();
                return;
            }
        } else if (pathArray.length > 2) {
            if (pathArray[2].startsWith('?authentication=')) {

                var authentication = pathArray[2].split('?authentication=')[1];
                var domain = pathArray[1];

                var user = domain + '-' + authentication;

                if ((cache.get('cachingUser') == null || cache.get('cachingUser') == undefined) || 
                	(cache.get('cachingUser')!=null && cache.get('cachingUser')!=user)) {

                    var respuesta = request('POST', authServiceUrl, {
                        json: {
                            'authentication': authentication,
                            'dominio': domain
                        }
                    });

		   console.log("Respuesta servicio autenticacion: "+respuesta.statusCode);

                    if (respuesta.statusCode == 200) {
                        cache.put('cachingUser', user, 60000, function(key, value) {
                            //console.log(key + '->' + value);
                        });

                    }


                }



            } else if (pathArray[2].trim() == '' || pathArray[2].trim() == '#') {
                res.statusCode = 403;
                res.end();
                return
            }
        }




        if (usersPorts[domain] != undefined) {

            proxy.web(req, res, {
                target: 'http://localhost:' + usersPorts[domain]
            });

        } else {
            res.statusCode = 404;
            res.end();
            return
        }

    });



    serverProxy.on('upgrade', function(req, socket, head) {
        var pathArray = req.url.split('/');
        var domain = pathArray[1];
        if (usersPorts[domain] != undefined) {
            proxy.ws(req, socket, head, {
                target: 'http://localhost:' + usersPorts[domain]
            });
        } else {
            console.log("Node-Red Manager. proxy-nodered.js. Error, domain not found: "+domain);
            socket.end();
        }

    });

    serverProxy.on('error', (e) => {
        if (e.code == 'EADDRINUSE') {
            console.log("Node-Red Manager. proxy-nodered.js. WARNING! Port: " + proxyPort + " is in use, Cannot start proxy");
            setTimeout(() => {
                server.listen(proxyPort);
            }, 1000);
        }
    });

    serverProxy.on('close', function(res, socket, head) {
    });


    serverProxy.listen(proxyPort);

}

function close() {
    proxy.close();
}



exports.serverProxy = serverProxy;

exports.close = close;