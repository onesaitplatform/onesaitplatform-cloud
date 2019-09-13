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
package com.minsait.onesait.platform.config.services.templates;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.minsait.onesait.platform.config.services.templates.MatchResult;
import com.minsait.onesait.platform.config.services.templates.SqlComparator;

import net.sf.jsqlparser.JSQLParserException;


@RunWith(MockitoJUnitRunner.class)
public class SqlComparatorTest {

    @Test
    public void given_twoIdenticalQueries_When_TheyAreCompared_Then_TheResultIsTrue() throws JSQLParserException {
        String query1 =
                "SELECT * "
              + "FROM AssetTestArray2 "
              + "WHERE Test1.temperature.timestamp >= '2017-01-01T00:00:00.000Z' AND "
              + "      Test1.temperature.timestamp <= '2017-01-01T00:00:00.000Z'";
      
        String query2 = 
              "SELECT * "
            + "FROM AssetTestArray2 "
            + "WHERE Test1.temperature.timestamp >= '2017-01-01T00:00:00.000Z' AND "
            + "      Test1.temperature.timestamp <= '2017-01-01T00:00:00.000Z'";
        
        MatchResult result = SqlComparator.match(query1, query2);
        assertTrue("The result of two identical queries should be true", result.isMatch());
    }
    
    @Test
    public void given_OneQueryAndOneTemplateWithTheSameStructure_When_TheTemplateUsesParametersAndTheQuerySpecificValuesAndAreCompared_Then_TheResultIsTrue() throws JSQLParserException {
        String query =
                "SELECT * "
              + "FROM AssetTestArray2 "
              + "WHERE Test1.temperature.timestamp >= '2017-01-01T00:00:00.000Z' AND "
              + "      Test1.temperature.timestamp <= '2017-01-01T00:00:00.000Z'";
      
        String template = 
              "SELECT * "
            + "FROM AssetTestArray2 "
            + "WHERE Test1.temperature.timestamp >= @from AND "
            + "      Test1.temperature.timestamp <= @to";
        
        MatchResult result = SqlComparator.match(query, template);
        assertTrue("The result of two identical queries should be true", result.isMatch());
        
        assertTrue("The @from parameter should have the value '2017-01-01T00:00:00.000Z'", result.getVariable("from").getStringValue().equals("2017-01-01T00:00:00.000Z"));
        assertTrue("The @to parameter should have the value '2017-01-01T00:00:00.000Z'", result.getVariable("to").getStringValue().equals("2017-01-01T00:00:00.000Z"));
        
    }
    
    @Test
    public void given_OneQueryWithSeveralFiltersInWhere_When_ItHasAnAssociatedTemplate_Then_TheyMatchAndTheParametersAreObtained() throws JSQLParserException {
        String query =
                "SELECT * "
              + "FROM AssetTestArray2 "
              + "WHERE Test1.temperature.timestamp >= '2017-01-01T00:00:00.000Z' AND "
              + "      Test1.temperature.timestamp <= '2017-01-01T00:00:00.000Z' AND "
              + "      Test1.temperature.id = 'algo' AND "
              + "      Test1.temperature.numero = 10";
      
        String template = 
              "SELECT * "
            + "FROM AssetTestArray2 "
            + "WHERE Test1.temperature.timestamp >= @from AND "
            + "      Test1.temperature.timestamp <= @to AND "
            + "      Test1.temperature.id = @id AND "
            + "      Test1.temperature.numero = @numero";
        
        MatchResult result = SqlComparator.match(query, template);
        
        assertTrue("The @from parameter should have the value '2017-01-01T00:00:00.000Z'", result.getVariable("from").getStringValue().equals("2017-01-01T00:00:00.000Z"));
        assertTrue("The @to parameter should have the value '2017-01-01T00:00:00.000Z'", result.getVariable("to").getStringValue().equals("2017-01-01T00:00:00.000Z"));
        assertTrue("The @id parameter should have the value 'algo'", result.getVariable("id").getStringValue().equals("algo"));
        assertTrue("The @numero parameter should have the value 10", result.getVariable("numero").getStringValue().equals("10"));
        
    }
    
    @Test
    public void given_OneTimeSeriesQuery_When_ItIsMatched_Then_AllTheParametersAreObtained() throws JSQLParserException {
        String query =
                "SELECT * "
              + "FROM timeseries(AssetTestArray2,'d') "
              + "WHERE Test1.temperature.timestamp >= '2017-01-01T00:00:00.000Z' AND "
              + "      Test1.temperature.timestamp <= '2017-01-01T00:00:00.000Z' ";
      
        String template = 
              "SELECT * "
            + "FROM timeseries(AssetTestArray2, @scale) "
            + "WHERE Test1.temperature.timestamp >= @from AND "
            + "      Test1.temperature.timestamp <= @to ";

        
        MatchResult result = SqlComparator.match(query, template);
        assertTrue("The result of two identical queries should be true", result.isMatch());
        
        assertTrue("The @from parameter should have the value '2017-01-01T00:00:00.000Z'", result.getVariable("from").getStringValue().equals("2017-01-01T00:00:00.000Z"));
        assertTrue("The @to parameter should have the value '2017-01-01T00:00:00.000Z'", result.getVariable("to").getStringValue().equals("2017-01-01T00:00:00.000Z"));
        assertTrue("The @scale parameter should have the value 'd'", result.getVariable("scale").getStringValue().equals("d"));
        
    }
}
