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
package org.apache.zeppelin.onesaitplatform.interpreter;

import java.util.List;
import java.util.Properties;

import org.apache.zeppelin.interpreter.Interpreter;
import org.apache.zeppelin.interpreter.InterpreterContext;
import org.apache.zeppelin.interpreter.InterpreterResult;
import org.apache.zeppelin.interpreter.InterpreterResult.Code;
import org.apache.zeppelin.onesaitplatform.enums.RestProtocols;
import org.apache.zeppelin.onesaitplatform.iotbroker.IotBrokerClient;
import org.apache.zeppelin.onesaitplatform.parser.IotBrokerParser;
import org.apache.zeppelin.scheduler.Scheduler;
import org.apache.zeppelin.scheduler.SchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Onesait Platform interpreter
 *
 */
public class IotBrokerInterpreter extends Interpreter {

  private static final Logger LOGGER = LoggerFactory.getLogger(IotBrokerInterpreter.class);

  private IotBrokerClient osp = null;

  public IotBrokerInterpreter(Properties property) {
    super(property);
  }

  @Override
  public void open() {
	  // get args
	  String protocol_in = getProperty("onesaitplatform.server.protocol");
	  String host_in = getProperty("onesaitplatform.server.host");
	  String port_in = getProperty("onesaitplatform.server.port");
	  String batchSize_in = getProperty("onesaitplatform.server.batch_size");
	  String avoidSSLC_in = getProperty("onesaitplatform.server.avoid_SSL_certificate");
	  String restPath_in = getProperty("onesaitplatform.server.restpath");
	  
	  // create client
	  try {
		  int port = Integer.parseInt(port_in);
		  osp = new IotBrokerClient(host_in, port);
	  }
	  catch (Exception e) {
		  osp = new IotBrokerClient(host_in);
	  }
	  
	  // set protocol
	  RestProtocols protocol = RestProtocols.HTTP;
	  if (protocol_in.equals("https")) {
		  protocol = RestProtocols.HTTPS;
	  }
	  osp.setProtocol(protocol);
	  
	  // set avoid SSL certificate
	  try {
		  boolean avoidSSLC = Boolean.getBoolean(avoidSSLC_in);
		  osp.avoidSSLCertificate(avoidSSLC);
	  }
	  catch (Exception e) {
		  // Nothing
	  }
	  
	  // set batch size
	  try {
		  int batchSize = Integer.parseInt(batchSize_in);
		  osp.setBatchSize(batchSize);
	  }
	  catch (Exception e) {
		  // Nothing
	  }
	  
	  // set rest path
	  osp.setPath(restPath_in);
	  
  }

  @Override
  public void close() {
    osp.leave();
  }

  @Override
  public FormType getFormType() {
    return FormType.SIMPLE;
  }

  @Override
  public InterpreterResult interpret(String script, InterpreterContext context) {
    LOGGER.debug("Run onesait platform query: {}", script);
    
    InterpreterResult result = null;
    if ("".equals(script)) {
      return new InterpreterResult(Code.SUCCESS);
    }
    else{
    	List<String> resultQuery; 
    	try{
    		osp.restartDebugTrace();
	        resultQuery = IotBrokerParser.parseAndExecute(context, osp, script);
	        resultQuery = IotBrokerParser.resultsOrDebugTrace(osp, resultQuery);
	        result = new InterpreterResult(InterpreterResult.Code.SUCCESS);
	        result.add(formatOutputZeppelin(resultQuery));
      }
      catch(Exception e){
        result = new InterpreterResult(InterpreterResult.Code.ERROR);
        result.add("Error in onesait platform query: " + e.getMessage());
      }
      
      
    }

    return result;
  }

  private String formatOutputZeppelin(List<String> alist){
    return String.join("\n", alist);
  }

  @Override
  public int getProgress(InterpreterContext context) {
    return 0;
  }

  @Override
  public void cancel(InterpreterContext context) {
  }

  @Override
  public Scheduler getScheduler() {
    return SchedulerFactory.singleton().createOrGetParallelScheduler("onesaitplatform", 10);
  }
  
}
