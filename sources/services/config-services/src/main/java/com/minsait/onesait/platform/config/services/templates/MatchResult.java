/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.config.services.templates;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

public class MatchResult {

    @Getter 
    private boolean match = true;
    
    @Getter
    private Map<String,VariableData> variables = new HashMap<>();
    
    public void addVariable(String variable, String value, VariableData.Type type) {
        variables.put(variable, new VariableData(variable, type, value));
    }
    
    public VariableData getVariable(String variable) {
        return variables.get(variable);
    }
    
    public void setResult(boolean result) {
        if(this.match) {
            this.match = result;
        }
    }
}
