/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zeppelin.onesaitplatform.parser;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.zeppelin.interpreter.InterpreterContext;
import org.apache.zeppelin.onesaitplatform.apimanager.ApiManagerClient;
import org.apache.zeppelin.onesaitplatform.base.Client;
import org.apache.zeppelin.onesaitplatform.help.ApiManagerHelp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ApiManagerParser {
	
	// debugg - log
	private static final Logger log = LoggerFactory.getLogger(ApiManagerParser.class);
	private static boolean developMode = false;
	public static Map<String, Object> contextExample = new HashMap<String, Object>();
	// mode paterns
	private static Pattern patternSetDebugMode = Pattern.compile("[ ]*setDebugMode\\([ ]*(.*)[ ]*\\)[ ]*");
    // call patterns
	private static Pattern patternSetToken = Pattern.compile("[ ]*setToken\\([ ]*(.*)[ ]*\\)[ ]*");
	private static Pattern patternFindAPI = Pattern.compile("^[ ]*findAPI\\([ ]*(.*)[ ]*\\,"
																		+ "[ ]*(.*)[ ]*\\,"
																		+ "[ ]*(.*)[ ]*\\)[ ]*$");
	private static Pattern patternCreateAPI = Pattern.compile("^[ ]*createAPI\\([ ]*(.*)[ ]*\\)[ ]*$");
	private static Pattern patternDeleteAPI = Pattern.compile("^[ ]*deleteAPI\\([ ]*(.*)[ ]*\\,"
																			+ "[ ]*(.*)[ ]*\\)[ ]*$");
	private static Pattern patternCallAPI = Pattern.compile("^[ ]*callAPI\\([ ]*(.*)[ ]*\\,"
																			+ "[ ]*(.*)[ ]*\\,"
																			+ "[ ]*(.*)[ ]*\\)[ ]*$");
	private static Pattern patternListAPIS = Pattern.compile("[ ]*listAPIS\\([ ]*(.*)[ ]*\\)[ ]*");
	// context patterns
	private static Pattern patternZput = Pattern.compile("^[ ]*z\\.put\\([ ]*\\\"([a-zA-Z0-9_$#-]+)\\\"[ ]*\\,[ ]*([ ]*.*[ ]*)\\)[ ]*$");
	private static Pattern patternZget = Pattern.compile("([ ]*z\\.get\\([ ]*\\\"([a-zA-Z0-9_$#-]+)\\\"[ ]*\\)[ ]*)");

    
    public static List<String> parseAndExecute(InterpreterContext context, ApiManagerClient osp, String sentence) throws Exception, UnsupportedEncodingException {

    	List<String> results = new LinkedList<String>();
    	String cleanedSentence = cleanSentente(sentence);
    	
    	if (cleanedSentence.startsWith("setDebugMode")) {
    		results = parseAndExecuteSetDebugMode(context, osp, cleanedSentence);
    	} 
    	
    	else if (cleanedSentence.startsWith("setToken")) {
    		results = parseAndExecuteSetToken(context, osp, cleanedSentence);
    	} 
    	
    	else if (cleanedSentence.startsWith("findAPI")) {
    		osp.raiseExceptionIfNotToken();
    		results = parseAndExecuteFindAPI(context, osp, cleanedSentence);
    	}
    	
    	else if (cleanedSentence.startsWith("deleteAPI")) {
    		osp.raiseExceptionIfNotToken();
    		results = parseAndExecuteDeleteAPI(context, osp, cleanedSentence);
    	}
    	
    	else if (cleanedSentence.startsWith("createAPI")) {
    		osp.raiseExceptionIfNotToken();
    		results = parseAndExecuteCreateAPI(context, osp, cleanedSentence);
    	} 
    	
    	else if (cleanedSentence.startsWith("callAPI")) {
    		osp.raiseExceptionIfNotToken();
    		results = parseAndExecuteCallAPI(context, osp, cleanedSentence);
    	}
    	
    	else if (cleanedSentence.startsWith("listAPIS")) {
    		osp.raiseExceptionIfNotToken();
    		results = parseAndExecuteListAPIS(context, osp, cleanedSentence);
    	} 
    	
    	else if (cleanedSentence.startsWith("z.put")) {
    		results = parseAndExecutePut(context, osp, cleanedSentence);
    		
    	}
    	// is (cleaned == ativateDebug() : nueva funcion para activar el debug y listo, como setToken
    	
    	else {
    		results = help();
    	}
    	
        return results;
    }
    
    static String parseSetDebugMode(String cleanedSentence) {
    	// setDebugMode("debug_value") || setDebugMode("z.get("debug_key"))"
    	Matcher matcher = patternSetDebugMode.matcher(cleanedSentence);
        String inPattern = null;
        String dMode = null;
        if (matcher.find()) {
        	inPattern = matcher.group(0);
        	dMode = matcher.group(1);
            log.info("Pattern setDebugMode detected: " + inPattern + " with " + dMode);
        }
        
        return dMode;
    }
    
    static List<String> parseAndExecuteSetDebugMode(InterpreterContext context, ApiManagerClient osp, String cleanedSentence) throws Exception {
    	List<String> results = null;
    	boolean dbugMode = false;
    	
    	// arguments from expression
    	String dMode = parseSetDebugMode(cleanedSentence);
    	
    	// arguments from context if needed
        dMode = parseArgument(context, osp, dMode);	
        
        if (dMode.toLowerCase().equals("true") || dMode.toLowerCase().equals("t") || dMode.toLowerCase().equals("1") ) {
        	dbugMode = true;
        }
        
    	// execution
    	results = osp.setDebugMode(dbugMode);
    	
    	return results;
    	
    }
    
    public static List<String> resultsOrDebugTrace(Client osp, List<String> results) {
    	
    	if (osp.isDebugMode()) {
    		return osp.getDebugTrace();
		}
    	return results;
    	
    }
    
    static String parseSetToken(String cleanedSentence) {
    	// setToken("token_value") || setToken("z.get("token_key"))"
    	Matcher matcher = patternSetToken.matcher(cleanedSentence);
        String inPattern = null;
        String token = null;
        if (matcher.find()) {
        	inPattern = matcher.group(0);
        	token = matcher.group(1);
            log.info("Pattern setToken detected: " + inPattern + " with " + token);
        }
        
        return token;
    }
    
    static List<String> parseAndExecuteSetToken(InterpreterContext context, ApiManagerClient osp, String cleanedSentence) throws Exception {
    	List<String> results = new ArrayList<String>();
    	
    	// arguments from expression
    	String token = parseSetToken(cleanedSentence);
    	
    	// arguments from context if needed
        token = parseArgument(context, osp, token);	
        
    	// execution
    	//results = osp.setToken(token);
        if (osp.setToken(token)) {
        	results.add("Token " + token + " setted correctly!");
        }
        else {
        	results.add("Not possible to set token and create HttpClient");
        }
    	
    	return results;
    	
    }
    
    static List<String> parseFindAPI(String cleanedSentence) {
    	// findAPI("identification_value", "state_value", ""user_value", "token_value")
    	Matcher matcher = patternFindAPI.matcher(cleanedSentence);
        String inPattern = null;
        List<String> results = new ArrayList<String>();
        
        String identification = null;
        String state = null;
        String user = null;
        if (matcher.find()) {
        	inPattern = matcher.group(0);
        	identification = matcher.group(1); 
        	state = matcher.group(2);
        	user = matcher.group(3);
        	results.add(identification);
        	results.add(state);
        	results.add(user);
            log.info("Pattern findAPI detected: " + inPattern + " with " + results.toString());
        }
        return results;
    }
    
    static List<String> parseAndExecuteFindAPI(InterpreterContext context, ApiManagerClient osp, String cleanedSentence) throws Exception {
    	osp.addDebugTrace("Parse and execute findAPI() on " + cleanedSentence);
    	List<String> results = null;
   	 	
   	 	// arguments from expression
    	List<String> params = parseFindAPI(cleanedSentence);
    	String identification = params.get(0); 
    	String state = params.get(1);
    	String user = params.get(2);
    	
    	// arguments from context if needed
    	identification = parseArgument(context, osp, identification);	
    	state = parseArgument(context, osp, state);
    	user = parseArgument(context, osp, user);
    	
    	// execution
    	//results = osp.doFind(identification, state, user);
    	results = osp.find(identification, state, user);

   	return results;
   	
   }

    static List<String> parseCreateAPI(String cleanedSentence) {
    	// findAPI("identification_value", "state_value", ""user_value", "token_value")
    	Matcher matcher = patternCreateAPI.matcher(cleanedSentence);
        String inPattern = null;
        List<String> results = new ArrayList<String>();
        
        String body = null;
        if (matcher.find()) {
        	inPattern = matcher.group(0);
        	body = matcher.group(1); 
        	results.add(body);
            log.info("Pattern createAPI detected: " + inPattern + " with " + results.toString());
        }
        return results;
    }
    
    static List<String> parseAndExecuteCreateAPI(InterpreterContext context, ApiManagerClient osp, String cleanedSentence) throws Exception {
    	osp.addDebugTrace("Parse and execute createAPI() on " + cleanedSentence);
    	List<String> results = null;
    	
    	// arguments from expression
    	List<String> params = parseCreateAPI(cleanedSentence);
    	String body = params.get(0);
    	
    	// arguments from context if needed
    	body = parseArgument(context, osp, body);	

    	// execution
    	//results = osp.doCreate(body);
    	results = osp.create(body);

	   	return results;
	   	
    }
    
    static List<String> parseDeleteAPI(String cleanedSentence) {
    	// deleteAPI("identification_value", "version_value", "token_value")
    	Matcher matcher = patternDeleteAPI.matcher(cleanedSentence);
        String inPattern = null;
        List<String> results = new ArrayList<String>();
        
        String identification = null;
        String version = null;
        if (matcher.find()) {
        	inPattern = matcher.group(0);
        	identification = matcher.group(1); 
        	version = matcher.group(2);
        	results.add(identification);
        	results.add(version);
            log.info("Pattern deleteAPI detected: " + inPattern + " with " + results.toString());
        }
        return results;
    }
    
    static List<String> parseAndExecuteDeleteAPI(InterpreterContext context, ApiManagerClient osp, String cleanedSentence) throws Exception {
    	osp.addDebugTrace("Parse and execute deleteAPI() on " + cleanedSentence);
    	List<String> results = null;
    	
    	// arguments from expression
	   	List<String> params = parseDeleteAPI(cleanedSentence);
	   	String identification = params.get(0); 
	   	String version = params.get(1);
	   	
	   	// arguments from context if needed
	   	identification = parseArgument(context, osp, identification);	
	   	version = parseArgument(context, osp, version);

    	// execution
	   	//results = osp.doDelete(identification, version);
	   	results = osp.delete(identification, version);

      	return results;
      	
	}
    
    static List<String> parseCallAPI(String cleanedSentence) {
    	// deleteAPI("identification_value", "version_value", "token_value")
    	Matcher matcher = patternCallAPI.matcher(cleanedSentence);
        String inPattern = null;
        List<String> results = new ArrayList<String>();
        
        String url = null;
        String method = null;
        String body = null;
        if (matcher.find()) {
        	inPattern = matcher.group(0);
        	url = matcher.group(1); 
        	method = matcher.group(2);
        	body = matcher.group(3);
        	results.add(url);
        	results.add(method);
        	results.add(body);
            log.info("Pattern callAPI detected: " + inPattern + " with " + results.toString());
        }
        return results;
    }
    
    static List<String> parseAndExecuteCallAPI(InterpreterContext context, ApiManagerClient osp, String cleanedSentence) throws Exception {
    	osp.addDebugTrace("Parse and execute callAPI() on " + cleanedSentence);
    	List<String> results = null;
    	
    	// arguments from expression
    	List<String> params = parseCallAPI(cleanedSentence);
    	String url = params.get(0); 
    	String method = params.get(1);
    	String body = params.get(2);

    	// arguments from context if needed
    	url = parseArgument(context, osp, url);	
    	method = parseArgument(context, osp, method);
    	body = parseArgument(context, osp, body);
    	
    	if (url.startsWith("/")) {
    		url = url.substring(1, url.length());
    	}

    	// execution
    	//results = osp.doCall(url, method, body);
    	results = osp.request(url, method, body);
      	
    	return results;
      	
      }
    
    static String parseListAPIS(String cleanedSentence) {
    	// setToken("token_value") || setToken("z.get("token_key"))"
    	Matcher matcher = patternListAPIS.matcher(cleanedSentence);
        String inPattern = null;
        String idUser = null;
        if (matcher.find()) {
        	inPattern = matcher.group(0);
        	idUser = matcher.group(1);
            log.info("Pattern listAPIS detected: " + inPattern + " with " + idUser);
        }
        
        return idUser;
    }
    
    static List<String> parseAndExecuteListAPIS(InterpreterContext context, ApiManagerClient osp, String cleanedSentence) throws Exception {
    	osp.addDebugTrace("Parse and execute listAPIS() on " + cleanedSentence);
    	List<String> results = null;
    	
    	// arguments from expression
    	String idUser = parseListAPIS(cleanedSentence);
    	
    	// arguments from context if needed
    	idUser = parseArgument(context, osp, idUser);	
        
    	// execution
    	//results = osp.doList(idUser);
    	results = osp.list(idUser);
    	
    	return results;
    	
    }
    
    public static String parseGet(String argumentSentence) {
    	Matcher mzget = patternZget.matcher(argumentSentence);
        String zGetFunction = null;
        String zVar = argumentSentence;
        if (mzget.find()) {
            zGetFunction = mzget.group(1);
            zVar = mzget.group(2);
            log.info("Pattern z.get detected: " + zGetFunction + " with " + zVar);
        }
        
        return zVar;
    	
    }
    
    public static String parseAndExecuteGet(InterpreterContext context, String argumentSentence) throws Exception {
    	String keyParam = parseGet(argumentSentence);	
    	String keyValue = argumentSentence;
    	Object valueToken = null;
		if (!keyParam.equals(argumentSentence)) {	
			
			if (developMode) {
        	valueToken = contextExample.get(keyParam);
			}
			else {
				valueToken = context.getResourcePool().get(keyParam).get();
			}
        	
        	if (valueToken instanceof String) {
                keyValue = (String) valueToken;
                log.info("Extracted from context: " + keyParam + ": " + keyValue);
            }
        	else {
                String errorMsg = "Invalid " + keyParam + " value in context";
                log.error(errorMsg);
                throw new Exception(errorMsg); 
            }
		}
		return keyValue;
    }
    
    private static List<String> parsePut(String cleanedSentence) {
    	// z.put("key", "value") || z.put("key", z.get("value")) 
    	Matcher matcher = patternZput.matcher(cleanedSentence);
        String inPattern = null;
        List<String> results = new ArrayList<String>();
        
        String keyPut = null;
        String valuePut = null;
        if (matcher.find()) {
        	inPattern = matcher.group(0);
        	keyPut = matcher.group(1); 
        	valuePut = matcher.group(2);
        	results.add(keyPut);
        	results.add(valuePut);
            log.info("Pattern z.put detected: " + inPattern + " with " + results.toString());
        }
        
    	return results;

    }
    
    public static List<String> parseAndExecutePut(InterpreterContext context, ApiManagerClient osp, String cleanedSentence) throws Exception {
    	osp.addDebugTrace("Parse and execute z.put() on " + cleanedSentence);
    	List<String> results = null;
    	
    	// arguments from expression
    	List<String> params = parsePut(cleanedSentence);
    	String keyPut = params.get(0); 
    	String valuePut = params.get(1);

    	// arguments from context if needed
    	keyPut = parseArgument(context, osp, keyPut);
    	valuePut = parseArgument(context, osp, valuePut);	

    	// execution
    	if (isFunction(valuePut)) {
    		results = parseAndExecute(context, osp, valuePut);
    		
    		String [] resArray = new String[results.size()];
    		for(int i=0; i<results.size(); i++){
                resArray[i] = results.get(i);
            }
    		if (developMode) {
    			contextExample.put(keyPut, results);
    		}
    		else {
    			context.getResourcePool().put(keyPut, resArray);
    		}
    		log.info("Response of: " + results.size() + " instances");
    		osp.addDebugTrace("Response of: " + results.size() + " instances");
    		
    	}
    	else {
    		results = new LinkedList<String>();
    		if (developMode) {
    			contextExample.put(keyPut, valuePut);
    		}
    		else {
    			context.getResourcePool().put(keyPut, valuePut);
    		}
    		log.info("Response of: " + results.size() + " instances");
    		osp.addDebugTrace("Response of: " + results.size() + " instances");
    		results.add("Added " + keyPut + " to context correctly!");
    		osp.addDebugTrace("Added " + keyPut + " to context correctly!");
    		
    	}
    	
        
      	return results;

    }
    
    public static String parseArgument(InterpreterContext context, ApiManagerClient osp, String argumentExpression) throws Exception {
		
    	String value = argumentExpression;
    	
    	if (argumentExpression.startsWith("z.get")) {
    		value = parseAndExecuteGet(context, argumentExpression);
    	}
    	
    	else if (argumentExpression.startsWith("\"")) {
    		value = argumentExpression.substring(1, argumentExpression.length()-1);
    	}
    	
    	return value;
    	
    }
    
    private static List<String> help() {
    	
    	List<String> results = new ArrayList<String>();
    	
    	results.add(ApiManagerHelp.HEADER);
    	results.add(ApiManagerHelp.DEBUG_MODE);
    	results.add(ApiManagerHelp.SET_TOKEN);
    	results.add(ApiManagerHelp.LIST);
    	results.add(ApiManagerHelp.FIND);
    	results.add(ApiManagerHelp.CREATE);
    	results.add(ApiManagerHelp.DELETE);
    	results.add(ApiManagerHelp.CALL);
    	
    	return results;
    }

    private static String cleanSentente(String sentence) {
        String cleanedSentence = sentence.trim();
        if(cleanedSentence.endsWith(";")){
            cleanedSentence = cleanedSentence.substring(0, -1);
        }
        return cleanedSentence;
    }
    
    public static boolean isFunction(String cleanedSentence) {
    	boolean isFunc = false;
    	
    	if (cleanedSentence.startsWith("setToken")) {
    		isFunc = true;
    	}
    	else if (cleanedSentence.startsWith("findAPI")) {
    		isFunc = true;
    	}
    	else if (cleanedSentence.startsWith("deleteAPI")) {
    		isFunc = true;
    	}
    	else if (cleanedSentence.startsWith("createAPI")) {
    		isFunc = true;
    	}
    	else if (cleanedSentence.startsWith("callAPI")) {
    		isFunc = true;
    	}
    	else if (cleanedSentence.startsWith("listAPIS")) {
    		isFunc = true;
    	}
    	
    	return isFunc;
    }
    
    public static void activateDevelopMode() {
    	developMode = true;
    }
    
    public static void deactivateDevelopMode() {
    	developMode = false;
    }
    
}

