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
package com.minsait.onesait.platform.persistence.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ElasticSearchFileUtil {
    
    public static String readAllBytes(String filePath) {
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (final IOException e) {
            log.error(e.getMessage());
        }
        return content;
    }

    public static List<String> readLines(File file) {
        if (!file.exists()) {
            return new ArrayList<>();
        }

        final List<String> results = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file));) {

            String line = reader.readLine();
            while (line != null) {
                results.add(line);
                line = reader.readLine();
            }
            return results;
        } catch (final Exception e) {
            return new ArrayList<>();
        }

    }



}
