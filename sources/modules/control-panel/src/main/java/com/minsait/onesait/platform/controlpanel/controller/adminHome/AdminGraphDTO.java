/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.controlpanel.controller.adminHome;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AdminGraphDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Getter
    @Setter
    private String source;
    @Getter
    @Setter
    private String target;

    @Getter
    @Setter
    private String title;
    @Getter
    @Setter
    private String linkCreate;
    @Getter
    @Setter
    private String linkSource;
    @Getter
    @Setter
    private String linkTarget;

    @Getter
    @Setter
    private String classSource;
    @Getter
    @Setter
    private String classTarget;

    @Getter
    @Setter
    private String nameSource;
    @Getter
    @Setter
    private String nameTarget;

    @Getter
    @Setter
    private String type;

    @Getter
    @Setter
    private Boolean isExternal;

    @Getter
    @Setter
    private Integer total;

    public AdminGraphDTO(String source, String target, String linkTarget, String classSource,
        String classTarget, String nameSource, String nameTarget, String type, String title, int total) {
        this.source = source;
        this.target = target;
        this.linkTarget = linkTarget;
        this.classSource = classSource;
        this.classTarget = classTarget;
        this.nameSource = nameSource;
        this.nameTarget = nameTarget;
        this.type = type;
        this.title = title;
        this.total = total;
    }

    public AdminGraphDTO(String source, String target, String linkTarget, String classSource,
        String classTarget, String nameSource, String nameTarget, String type, String title, String linkCreate,
        Integer total) {
        super();
        this.source = source;
        this.target = target;
        this.linkTarget = linkTarget;
        this.classSource = classSource;
        this.classTarget = classTarget;
        this.title = title;
        this.linkCreate = linkCreate;
        this.nameSource = nameSource;
        this.nameTarget = nameTarget;
        this.type = type;
        this.total = total;
    }
    
    public static AdminGraphDTO constructSingleNode(String source, String linkSource, String classSource,
        String nameSource, String title, Integer total) {
        return new AdminGraphDTO(source, source, linkSource, linkSource, classSource, classSource, nameSource,
            nameSource, title, null, total);
    }

    public static AdminGraphDTO constructSingleNodeWithTitleAndCreateLink(String source, String linkSource,
        String classSource, String nameSource, String title, String linkCreate, Integer total) {
        return new AdminGraphDTO(source, source, linkSource, classSource, classSource, nameSource, nameSource,
            null, title, linkCreate, total);
    }

    @Override
    @JsonRawValue
    @JsonIgnore
    public String toString() {
        final ObjectMapper mapper = new ObjectMapper();
        String result = null;
        try {
            result = mapper.writeValueAsString(this);
        } catch (final JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return result;

    }
}
