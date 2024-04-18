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
package com.minsait.onesait.platform.spark.launcher.executor;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
//
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.util.LineReader;

public class InputOutputUtil {

    public static void close(LineReader reader) {
        if (reader == null) {
            return;
        }
        //
        try {
            reader.close();
        } 
        catch (Exception ignore) {
        }
    }

    public static void close(OutputStream stream) {
        if (stream == null) {
            return;
        }
        //
        try {
            stream.close();
        } 
        catch (Exception ignore) {
        }
    }

    public static void close(InputStream stream) {
        if (stream == null) {
            return;
        }
        //
        try {
            stream.close();
        } 
        catch (Exception ignore) {
        }
    }

    public static void close(FSDataInputStream stream) {
        if (stream == null) {
            return;
        }
        //
        try {
            stream.close();
        } 
        catch (Exception ignore) {
        }
    }

    public static void close(BufferedReader reader) {
        if (reader == null) {
            return;
        }
        //
        try {
            reader.close();
        } 
        catch (Exception ignore) {
        }
    }
}
