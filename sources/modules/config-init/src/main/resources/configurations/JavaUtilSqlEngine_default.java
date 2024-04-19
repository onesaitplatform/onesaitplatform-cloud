/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
import com.github.vincentrussell.query.mongodb.sql.converter.holder.MongoDBQueryHolder;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.bson.Document;

import java.util.LinkedList;
import java.util.List;

public final class UtilsCode {
    public enum WINDOWTS {
        SECONDS,
        MINUTES,
        HOURS,
        DAYS,
        MONTHS
    }

    private static final int[] TIMETOSEG =  new int[] {1, 60, 3600, 86400};

    private static final int[] TIMETONEXT =  new int[] {1, 60, 60, 24};

    /**
     * previous steps for timeseries unzip by n levels.
     * This use the period and frecuency for calculating this variable steps
     * @param le params: 0 upper(period), 1 lower(frecuency)
     * @param mongoDBQueryHolder
     * @return list of documents for previous steps in unzip ts
     */
    public static List<Document> tsPreSteps(final List<Expression> le, final MongoDBQueryHolder mongoDBQueryHolder) {
        WINDOWTS windowh = WINDOWTS.valueOf(((StringValue) le.get(0)).getValue().toUpperCase());
        WINDOWTS windowl = WINDOWTS.valueOf(((StringValue) le.get(1)).getValue().toUpperCase());

        List<Document> ldocs = new LinkedList<>();
        ldocs.add(
                Document.parse("{\n"
                        + "            \"$project\": {\n"
                        + "                \"TimeSerie\": 1,\n"
                        + "                \"tmpArray\": { \"$objectToArray\": \"$TimeSerie.values.v\" }\n"
                        + "                \"tmpIndex\": { \"$literal\": 0 }\n"
                        + "            },\n"
                        + "        }")
        );

        ldocs.add(
                Document.parse("{\n"
                        + "            \"$unwind\": \"$tmpArray\"\n"
                        + "        }")
        );

        for (int i = windowh.ordinal() - 1; i > windowl.ordinal(); i--) {
            ldocs.add(
                    Document.parse("{\n"
                            + "            \"$project\": {\n"
                            + "                \"TimeSerie\": 1,\n"
                            + "                \"tmpIndex\": { "
                            + "                     \"$multiply\": ["
                            +                            "{\"$sum\":[\"$tmpIndex\",{\"$toInt\":\"$tmpArray.k\"}]},"
                            +                            TIMETONEXT[i]
                            + "                     ]},\n"
                            + "                \"tmpArray\": { \"$objectToArray\": \"$tmpArray.v\" }\n"
                            + "            }\n"
                            + "        }")
            );
            ldocs.add(
                    Document.parse("{\n"
                            + "            \"$unwind\": \"$tmpArray\"\n"
                            + "        }")
            );
        }

        ldocs.add(
                Document.parse("{\n"
                        + "            \"$addFields\": {\n"
                        + "                \"TimeSerie.value\": \"$tmpArray.v\",\n"
                        + "                \"TimeSerie.timestamp\": {\n"
                        + "                    \"$dateFromParts\": {\n"
                        + "                        \"year\": { \"$year\": \"$TimeSerie.timestamp\" },\n"
                        + "                        \"month\": { \"$month\": \"$TimeSerie.timestamp\" },\n"
                        + "                        \"day\": { \"$dayOfMonth\": \"$TimeSerie.timestamp\" },\n"
                        + "                        \"hour\": { \"$hour\": \"$TimeSerie.timestamp\" },\n"
                        + "                        \"minute\": { \"$minute\": \"$TimeSerie.timestamp\" },\n"
                        + "                        \"second\": { \"$multiply\": ["
                        + "                                 {\"$sum\":[\"$tmpIndex\",{\"$toInt\":\"$tmpArray.k\"}]},"
                        +                                   TIMETOSEG[windowl.ordinal()] + "]}\n"
                        + "                    }\n"
                        + "                }\n"
                        + "            }\n"
                        + "        }")
        );

        return ldocs;
    }

    /**
     * include ts fields in project, combine then with previous.
     * @param le
     * @param mongoDBQueryHolder
     * @return null
     */
    public static Document tsExp(final List<Expression> le, final MongoDBQueryHolder mongoDBQueryHolder) {
        mongoDBQueryHolder.getProjection().put("tmpArray", 0);
        mongoDBQueryHolder.getProjection().put("tmpIndex", 0);
        mongoDBQueryHolder.getProjection().put("TimeSerie.values", 0);
        return null;
    }
}