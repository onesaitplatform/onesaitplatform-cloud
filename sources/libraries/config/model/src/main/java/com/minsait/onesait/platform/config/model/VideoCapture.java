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
package com.minsait.onesait.platform.config.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "VIDEO_CAPTURE")
public class VideoCapture extends OPResource {

	private static final long serialVersionUID = 1L;

	public enum Protocol {
		HTTP, RTSP, TCP, UDP
	}

	public enum Processor {
		PEOPLE, TEXT, PLATES, STATS, YOLO
	}

	public enum State {
		START, STOP
	}

	@Column(name = "PROTOCOL")
	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private Protocol protocol;

	@Column(name = "PROCESSOR", nullable = false)
	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private Processor processor;

	@Column(name = "IP")
	@Getter
	@Setter
	private String ip;

	@Column(name = "PORT")
	@Getter
	@Setter
	private String port;

	@Column(name = "PATH")
	@Getter
	@Setter
	private String path;

	@Column(name = "USERNAME")
	@Getter
	@Setter
	private String username;

	@Column(name = "PASSWORD")
	@Getter
	@Setter
	private String password;

	@Column(name = "CONNECTION_URL")
	@Getter
	@Setter
	private String url;

	@ManyToOne
	@JoinColumn(name = "ONTOLOGY_ID", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	private Ontology ontology;

	@Column(name = "STATE", nullable = false)
	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private State state = State.STOP;

	@Column(name = "SAMPLING_INTERVAL", nullable = false)
	@Getter
	@Setter
	private long samplingInterval;
	
	@Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof VideoCapture))
            return false;
        final VideoCapture that = (VideoCapture) o;
        return this.getIdentification() != null && this.getIdentification().equals(that.getIdentification());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(this.getIdentification());
    }

}
