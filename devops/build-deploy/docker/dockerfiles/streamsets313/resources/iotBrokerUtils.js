function httpGet(theURL) {
    var con = new java.net.URL(theURL).openConnection();

    con.requestMethod = "GET";
    if (sessionKey != null) {
      log.info("set sessionKey:" + sessionKey)
      con.setRequestProperty ("Authorization", sessionKey);
    } else {
      log.info("No sessionKey")
    }
    return asResponse(con);
}

function asResponse(con) {
    var d = read(con.inputStream);

    return {data : d, statusCode : con.responseCode};
}

function read(inputStream) {
    var inReader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
    var inputLine;
    var response = new java.lang.StringBuffer();

    while ((inputLine = inReader.readLine()) != null) {
           response.append(inputLine);
    }
    inReader.close();
    return JSON.parse(response.toString());
}

function doJoin(iotBrokerURL, token, device) {
   var joinURL = iotBrokerURL + "/client/join" + "?" + "token=" + token + "&clientPlatform=" + device + "&clientPlatformId=" + device + ":siteInt"
   var response = httpGet(joinURL);
   if (response["statusCode"] == 200) {
     sessionKey = response["data"]["sessionKey"]
     log.info("Integrity Join OK, sessionKey:" + sessionKey)
     return sessionKey
   } else {
         log.error("Join false, error: " + JSON.stringify(response))
     return null
  }
}

function doQuery(iotBrokerURL, ontology, query, type) {
  var getURL = iotBrokerURL + "/ontology/" + ontology + "?" + "query=" + java.net.URLEncoder.encode(query,"UTF-8") + "&queryType=NATIVE"
  var response = httpGet(getURL);
  if (response["statusCode"] == 200) {
    var data = response["data"]
    log.info("Integrity " + type + " get OK, data:" + JSON.stringify(data))
    return data;
  } else {
    log.error("Get false " + type + " get, error: " + JSON.stringify(response))
    return null
  }
}

function doLeave(iotBrokerURL){
  var leaveURL = iotBrokerURL + "/client/leave"
  var response = httpGet(leaveURL);
  if(response["statusCode"] == 200){
    log.info("Integrity leave OK")
    return true;
  } else {
    log.error("Integrity leave, error: " + JSON.stringify(response))
    return false;
  }
}
