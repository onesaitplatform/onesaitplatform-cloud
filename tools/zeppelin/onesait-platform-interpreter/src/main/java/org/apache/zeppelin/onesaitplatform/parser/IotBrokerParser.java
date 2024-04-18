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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.zeppelin.interpreter.InterpreterContext;
import org.apache.zeppelin.onesaitplatform.base.Client;
import org.apache.zeppelin.onesaitplatform.converter.TableConverter;
import org.apache.zeppelin.onesaitplatform.enums.QueryType;
import org.apache.zeppelin.onesaitplatform.help.IotBrokerHelp;
import org.apache.zeppelin.onesaitplatform.iotbroker.IotBrokerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IotBrokerParser {
    private static final Logger log = LoggerFactory.getLogger(IotBrokerParser.class);
    private static boolean developMode = false;
    public static Map<String, Object> contextExample = new HashMap<String, Object>();
    // function patterns
    private static Pattern patternZput = Pattern.compile("^[ ]*z\\.put\\([ ]*\\\"([a-zA-Z0-9_$#-]+)\\\"[ ]*\\,[ ]*([ ]*.*[ ]*)\\)[ ]*$");
    private static Pattern patternZget = Pattern.compile("([ ]*z\\.get\\([ ]*\\\"([a-zA-Z0-9_$#-]+)\\\"[ ]*\\)[ ]*)");
    private static Pattern patternSetDebugMode = Pattern.compile("[ ]*setDebugMode\\([ ]*(.*)[ ]*\\)[ ]*");
    private static Pattern patternInsert = Pattern.compile("^insert\\(\\\"([a-zA-Z0-9_]+)\\\"\\,([ ]*.*[ ]*)\\)[ ]*$");
    private static Pattern patterAsZTable = Pattern.compile("^[ ]*asZTable\\([ ]*([\\*\\: \\{\\}\\(\\)\\:\\,\\\"\\.\\[\\]a-zA-Z0-9_$#-]+[ ]*)\\)[ ]*$");
    private static Pattern patternInitConnection = Pattern.compile("^[ ]*initConnection\\([ ]*\\\"([a-zA-Z0-9_$#-]+)\\\"[ ]*\\,[ ]*\\\"([a-zA-Z0-9_$#-]+)\\\"[ ]*\\)[ ]*$");
    private static Pattern patternPaginatedQuery = Pattern.compile("^[ ]*paginatedQuery\\([ ]*(.*)[ ]*\\)[ ]*");
    // ontology from sql statements
    private static Pattern patternOntologySqlSelect = Pattern.compile("from\\s+(?:\\w+\\.)*(\\w+)(\\s*$|\\s+(OFFSET|LIMIT|AS|WHERE|JOIN|START\\s+WITH|ORDER\\s+BY|GROUP\\s+BY))", Pattern.CASE_INSENSITIVE);
    private static Pattern patternOntologySqlUpdate = Pattern.compile("[ ]*update[ ]*(.*)[ ][ ]*set.*where.*", Pattern.CASE_INSENSITIVE);
    private static Pattern patternOntologySqlInsert = Pattern.compile("[ ]*insert[ ]*into[ ]*(.*)[ ][ ]*\\(.*\\)[ ]*values[ ]*\\(.*\\)", Pattern.CASE_INSENSITIVE);
    private static Pattern patternOntologySqlDelete = Pattern.compile("[ ]*delete[ ]*from[ ]*(.*)[ ][ ]*where[ ]*.*", Pattern.CASE_INSENSITIVE);
    
    public static List<String> parseAndExecute(InterpreterContext context, IotBrokerClient ospc, String sentence) throws Exception, UnsupportedEncodingException {
        String cleanedSentence = cleanSentente(sentence);
        
        if (getQueryTypeFromQuery(cleanedSentence) == QueryType.SQL) {  //SQL Query
        	if(!ospc.isConnected()) {
        		List<String> l = new LinkedList<String>();
        		l.add("Error sql query, please connect to onesait platform with sentence: initConnection(\"client\",\"token\")");
        		ospc.addDebugTrace("Error sql query, please connect to onesait platform with sentence: initConnection(\"client\",\"token\")");
        		throw new Exception(l.toString());
        	}
            //return ospc.doQuery(ospc.generateURLQuery(getOntologyFromSQLQuery(sentence),sentence, "SQL"));
            return ospc.query(getOntologyFromSQLQuery(cleanedSentence), cleanedSentence, QueryType.SQL.name());
        }
        else if (getQueryTypeFromQuery(cleanedSentence) == QueryType.NATIVE) {  //Native Query
        	if(!ospc.isConnected()) {
        		List<String> l = new LinkedList<String>();
        		l.add("Error native query, please connect to onesait platform with sentence: initConnection(\"client\",\"token\")");
        		ospc.addDebugTrace("Error native query, please connect to onesait platform with sentence: initConnection(\"client\",\"token\")");
        		throw new Exception(l.toString());
        	}
            //return ospc.doQuery(ospc.generateURLQuery(getOntologyFromMongoQuery(sentence),sentence, "NATIVE"));
            return ospc.query(getOntologyFromMongoQuery(cleanedSentence), cleanedSentence, QueryType.NATIVE.name());
        }
        else if (cleanedSentence.toLowerCase().startsWith("insert")) {
        	if(!ospc.isConnected()) {
        		List<String> l = new LinkedList<String>();
        		l.add("Error insert, please connect to onesait platform with sentence: initConnection(\"client\",\"token\")");
        		ospc.addDebugTrace("Error insert, please connect to onesait platform with sentence: initConnection(\"client\",\"token\")");
        		return l;
        	}
            String resourcePoolStr = null;
            log.info("Detected insert query " + cleanedSentence);
            ospc.addDebugTrace("Detected insert query " + cleanedSentence);
            Matcher mzget = patternZget.matcher(cleanedSentence);
            String zGetFunction = null;
            String zVar = null;
            if (mzget.find()) {
                log.info("Pattern z.get detected");
                ospc.addDebugTrace("Pattern z.get detected");
                zGetFunction = mzget.group(1);
                zVar = mzget.group(2);
                log.info("Pattern z.get detected: " + zGetFunction + " with " + zVar);
                ospc.addDebugTrace("Pattern z.get detected");
                
                if(context.getResourcePool().get(zVar).get() instanceof String){
                    log.info("Is String");
                    ospc.addDebugTrace("Is String");
                    resourcePoolStr = (String) context.getResourcePool().get(zVar).get();
                }
                else{
                    log.info("Is Array String");
                    ospc.addDebugTrace("Is Array String");
                    resourcePoolStr = Arrays.toString(((String[])context.getResourcePool().get(zVar).get()));
                }
                ospc.addDebugTrace("Result pool String: " + resourcePoolStr);
            }
            Matcher mzsentence = patternInsert.matcher(cleanedSentence);
            String ontology = null;
            String instances = null;
            if (mzsentence.find()) {
                ontology = mzsentence.group(1);
                instances = resourcePoolStr!=null?resourcePoolStr:mzsentence.group(2);
                //return  ospc.doInsert(ontology,instances); 
                return  ospc.insert(ontology, instances);
            }
        }
        else if (cleanedSentence.toLowerCase().startsWith("setdebugmode")){ // debug mode 
        	Matcher matcher = patternSetDebugMode.matcher(cleanedSentence);
            String inPattern = null;
            String dMode = null;
            boolean dModeBool = false;
            if (matcher.find()) {
            	inPattern = matcher.group(0);
            	dMode = matcher.group(1);
            	if (dMode.startsWith("\"")) {
            		dMode = dMode.substring(1, dMode.length()-1);
            	}
            	if (dMode.toLowerCase().equals("true") || dMode.toLowerCase().equals("t") || dMode.toLowerCase().equals("1") ) {
            		dModeBool = true;
            	}
                log.info("Pattern setDebugMode detected: " + inPattern + " with " + dMode);
            }
            return ospc.setDebugMode(dModeBool);
        }
        else {//Custom sentence
            if(cleanedSentence.startsWith("z.put")){ //Zeppelin put
            	log.info("Custom sentence " + cleanedSentence);
            	ospc.addDebugTrace("Custom sentence " + cleanedSentence);
                ArrayList<String> astr = new ArrayList<String>();
                Matcher mzput = patternZput.matcher(cleanedSentence);
                String keyPut = null;
                String query = null;
                String valuePut = null;
                
                if (mzput.find()) {
                    keyPut = mzput.group(1);
                    valuePut = mzput.group(2);
                    log.info("keyPut: " + keyPut);
                    ospc.addDebugTrace("keyPut: " + keyPut);
                    log.info("valuePut: " + valuePut);
                    ospc.addDebugTrace("valuePut: " + valuePut);
                    
                    try{
                    	Matcher mzget = patternZget.matcher(valuePut);
                        String zGetFunction = null;
                        String zVar = null;
                        
                        if (mzget.find()) {
                        	log.info("Pattern z.get detected");
                        	ospc.addDebugTrace("Pattern z.get detected");
                            zGetFunction = mzget.group(1);
                            zVar = mzget.group(2);
                            log.info("Pattern z.get detected: " + zGetFunction + " with " + zVar);
                            ospc.addDebugTrace("Pattern z.get detected: " + zGetFunction + " with " + zVar);
                            
                            if(context.getResourcePool().get(zVar).get() instanceof String){
                                query = (String) context.getResourcePool().get(zVar).get();
                                log.info("Is String, query: " + query);
                                ospc.addDebugTrace("Is String, query: " + query);
                            }
                            else{
                                String errorMsg = "Invalid " + query + " value, not string for query";
                                log.error(errorMsg);
                                ospc.addDebugTrace(errorMsg);
                                astr.add(errorMsg);
                                return astr;
                            }
                        }
                        else {
                        	query = valuePut;
                        }
                        List<String> res = parseAndExecute(context, ospc, query);
                        log.info("Response of: " + res.size() + " instances");
                        ospc.addDebugTrace("Response of: " + res.size() + " instances");
                        String [] resArray = new String[res.size()];
                        for(int i=0;i<res.size();i++){
                            resArray[i] = res.get(i);
                        }
                        
                        if (developMode) {
                			contextExample.put(keyPut, resArray);
                		}
                		else {
                			context.getResourcePool().put(keyPut,resArray);
                		}
                        
                        astr.add("Zeppelin Context: Successfully inyected in " + keyPut);
                        ospc.addDebugTrace("Zeppelin Context: Successfully inyected in " + keyPut);
                        return astr;
                    }
                    catch(Exception e){
                        String errorMsg = "Error inyecting in zeppelin context: " + e.getMessage();
                        log.error(errorMsg);
                        ospc.addDebugTrace(errorMsg);
                        astr.add(errorMsg);
                        return astr;
                    }
                }
                else{
                    return astr;
                }

            }
            else if(cleanedSentence.startsWith("asZTable")){ //Transform to zeppelin output table
            	Matcher mztable = patterAsZTable.matcher(cleanedSentence);
            	if (mztable.find()) {
            		String query = mztable.group(1);
            		log.info("Query inside: " + query);
            		ospc.addDebugTrace("Query inside: " + query);
                	List<String> res = parseAndExecute(context, ospc, query);
                	log.info("Done query");
                	ospc.addDebugTrace("Done query");
                    return TableConverter.fromListStr(res); 
            	}
            	return null;
            }
            else if (cleanedSentence.startsWith("initConnection")) {
            	Matcher mzinitconnect = patternInitConnection.matcher(cleanedSentence);
            	if (mzinitconnect.find()) {
            		String client = mzinitconnect.group(1);
            		String token = mzinitconnect.group(2);
            		log.info("Join with client: " + client + " , token: " + token);
            		ospc.addDebugTrace("Join with client: " + client + " , token: " + token);
            		if(ospc.isConnected()) {
            			//ospc.doLeave();
            			ospc.leave();
            		}
            		List<String> l = new LinkedList<String>();
            		//if(ospc.doJoin(client, token)) {
            		if(ospc.join(client, token)) {
                		l.add("Connected with client: " + client + " , token: " + token);
                		ospc.addDebugTrace("Connected with client: " + client + " , token: " + token);
                	}
                	else {
                		l.add("Error in connection with client: " + client + " , token: " + token);
                		ospc.addDebugTrace("Error in connection with client: " + client + " , token: " + token);
                	}
                	return l;
                	
            	}
            	return null;
            }
            
            else if (cleanedSentence.startsWith("paginatedQuery")){
            	if(!ospc.isConnected()) {
            		List<String> l = new LinkedList<String>();
            		l.add("Error batch query, please connect to onesait platform with sentence: initConnection(\"client\",\"token\")");
            		ospc.addDebugTrace("Error batch query, please connect to onesait platform with sentence: initConnection(\"client\",\"token\")");
            		throw new Exception(l.toString());
            	}
            	
            	Matcher mzpaginatedquery = patternPaginatedQuery.matcher(cleanedSentence);
            	if (mzpaginatedquery.find()) {
            		List<String> l = new LinkedList<String>();
            		String query = mzpaginatedquery.group(1);
            		log.info("Paginated query: " + query);
            		ospc.addDebugTrace("Paginated query: " + query);
            		l = parseAndExecutePaginatedQuery(ospc, query);
            		return l;
            		
            	}
            	
            	return null;
            }
            
            
            return help(); //null;
        }
        return null;
    }
    
    static List<String> parseAndExecutePaginatedQuery(IotBrokerClient ospc, String query) throws Exception {
    	log.info("Parse and execute paginatedQuery() on " + query);
    	ospc.addDebugTrace("Parse and execute paginatedQuery() on " + query);
    	int batchSize = ospc.getBatchSize();
    	List<String> results = new LinkedList<String>();
    	
    	// sql query
    	if (getQueryTypeFromQuery(cleanSentente(query)) == QueryType.SQL) { 
			log.info("sql type query!");
    		ospc.addDebugTrace("sql type query!");
    		
    		if (!query.toLowerCase().startsWith("select")) {
    			log.info("Function paginatedQuery() is only available with select statement");
    			ospc.addDebugTrace("Function paginatedQuery() is only available with select statement");
    			results.add("Function paginatedQuery() is only available with select statement");
    			return results;
        	}
    		
    		int offset = 0;
    		int limit = batchSize;
    		String ontology = getOntologyFromSQLQuery(query);
    		int resultsStepCount = batchSize;
    		
    		while (resultsStepCount == batchSize) {
    			List<String> resultsStep = new LinkedList<String>();
    			
    			String stepQuery = query + " offset " + offset + " limit " + limit;    			
    			log.info("Page query " + stepQuery);
    	    	ospc.addDebugTrace("Page query: " + stepQuery);
    	    	//resultsStep = ospc.doQuery(ospc.generateURLQuery(ontology, stepQuery, QueryType.SQL.name()));
    	    	resultsStep = ospc.query(ontology, stepQuery, QueryType.SQL.name());
    	    	resultsStepCount = resultsStep.size();
    	    	
    	    	if (resultsStepCount > 0) {
    	    		results.addAll(resultsStep);
    	    	}
    	    	
    	    	offset += batchSize;
    			
    		}
    		
			return results;
		}
    	// mongo query
		else if (getQueryTypeFromQuery(cleanSentente(query)) == QueryType.NATIVE) {
			log.info("mongo type query!");
    		ospc.addDebugTrace("mongo type query!");
    		
    		if (!query.contains(".find")) {
    			log.info("Function paginatedQuery() is only available with .find() statement");
    			ospc.addDebugTrace("Function paginatedQuery() is only available with .find() statement");
    			results.add("Function paginatedQuery() is only available with .find() statement");
    			return results;
        	}
    		
    		int offset = 0;
    		int limit = batchSize;
    		String ontology = getOntologyFromMongoQuery(query);
    		int resultsStepCount = batchSize;
    		
    		while (resultsStepCount == batchSize) {
    			List<String> resultsStep = new LinkedList<String>();
    			
    			String stepQuery = query + ".skip(" + offset + ")" + ".limit(" + limit + ")";    			
    			log.info("Page query " + stepQuery);
    	    	ospc.addDebugTrace("Page query: " + stepQuery);
    	    	//resultsStep = ospc.doQuery(ospc.generateURLQuery(ontology, stepQuery, QueryType.NATIVE.name()));
    	    	resultsStep = ospc.query(ontology, stepQuery, QueryType.NATIVE.name());
    	    	resultsStepCount = resultsStep.size();
    	    	
    	    	if (resultsStepCount > 0) {
    	    		results.addAll(resultsStep);
    	    	}
    	    	
    	    	offset += batchSize;
    			
    		}
    		
			return results;
			
		}
    	

   	return results;
   	
   }

    private static String cleanSentente(String sentence) {
        String cleanedSentence = sentence.trim();
        if(cleanedSentence.endsWith(";")){
            cleanedSentence = cleanedSentence.substring(0, -1);
        }
        return cleanedSentence;
    }

    private static QueryType getQueryTypeFromQuery(String cleannedQuery) {
    	QueryType qType = null;
    	
    	// SQL
    	if (cleannedQuery.toLowerCase().startsWith("select")) {
    		qType = QueryType.SQL;
    	}
    	else if (cleannedQuery.toLowerCase().startsWith("update")) {
    		qType = QueryType.SQL;
    	}
    	else if (cleannedQuery.toLowerCase().startsWith("insert into")) {
    		qType = QueryType.SQL;
    	}
    	else if (cleannedQuery.toLowerCase().startsWith("delete")) {
    		qType = QueryType.SQL;
    	}
    	
    	// MONGO (NATIVE)
    	else if (cleannedQuery.toLowerCase().startsWith("db.")) {
    		qType = QueryType.NATIVE;
    	}
    	
		return qType;
    	
    }
    
    private static String getOntologyFromSQLQuery(String sqlQuery) {
    	String ontology = null; 
    	Pattern p = null;
    
    	if (sqlQuery.toLowerCase().startsWith("select")) {
    		p = patternOntologySqlSelect;
    	} 
    	else if (sqlQuery.toLowerCase().startsWith("update")) {
    		p = patternOntologySqlUpdate;
    	}
    	else if (sqlQuery.toLowerCase().startsWith("insert into")) {
    		p = patternOntologySqlInsert;
    	}
    	else if (sqlQuery.toLowerCase().startsWith("delete")) {
    		p = patternOntologySqlDelete;
    	}
    	else {
    		return "Error: Not valid query - " + sqlQuery;
    	}
    	
        Matcher m = p.matcher(sqlQuery);
        while (m.find()) {
            ontology = m.group(1);
            break;
        }
        return ontology;
    }
    
    private static String getOntologyFromMongoQuery(String mongoQuery){
        String[] lqm =  mongoQuery.split("\\.");
        if(lqm.length>1) {
        	return lqm[1];
        }
        return "";
    }
    
    public static List<String> resultsOrDebugTrace(Client osp, List<String> results) {
    	
    	if (osp.isDebugMode()) {
    		return osp.getDebugTrace();
		}
    	return results;
    	
    }
    
    static List<String> help() {
    	
    	List<String> results = new ArrayList<String>();

    	results.add(IotBrokerHelp.HEADER);
    	results.add(IotBrokerHelp.DEBUG_MODE);
    	results.add(IotBrokerHelp.INIT_CONNECTION);
    	results.add(IotBrokerHelp.SQL_QUERY);
    	results.add(IotBrokerHelp.NATIVE_QUERY);
    	results.add(IotBrokerHelp.AS_Z_TABLE);
    	results.add(IotBrokerHelp.INSERT);
    	results.add(IotBrokerHelp.PAGINATED_QUERY);
    	
    	return results;
    }
}

