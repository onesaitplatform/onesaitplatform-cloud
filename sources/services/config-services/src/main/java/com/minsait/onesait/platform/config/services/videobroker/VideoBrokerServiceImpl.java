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
package com.minsait.onesait.platform.config.services.videobroker;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.VideoCapture;
import com.minsait.onesait.platform.config.model.VideoCapture.State;
import com.minsait.onesait.platform.config.repository.VideoCaptureRepository;
import com.minsait.onesait.platform.config.services.datamodel.DataModelService;
import com.minsait.onesait.platform.config.services.user.UserService;

@Service
public class VideoBrokerServiceImpl implements VideoBrokerService {
	@Autowired
	private VideoCaptureRepository videoCaptureRepository;
	@Autowired
	private DataModelService dataModelService;
	@Autowired
	private UserService userService;
	public static final String VIDEO_RESULT_DATA_MODEL = "VideoResult";

	@Override
	public List<VideoCapture> getVideoCaptures(String userId) {
		final User user = userService.getUser(userId);
		if (userService.isUserAdministrator(user))
			return videoCaptureRepository.findAll();
		else
			return videoCaptureRepository.findByUser(user);
	}

	@Override
	public VideoCapture get(String id) {
		return videoCaptureRepository.findById(id).orElse(null);
	}

	@Override
	@Transactional
	public String create(VideoCapture videoCapture) {
		videoCapture.setState(State.START);
		if (videoCapture.getSamplingInterval() == 0)
			videoCapture.setSamplingInterval(1000L);
		videoCapture = videoCaptureRepository.save(videoCapture);
		return videoCapture.getId();
	}

	@Override
	public Ontology createOntologyVideoResults(VideoCapture videoCapture) {
		final Ontology ontology = new Ontology();
		ontology.setUser(videoCapture.getUser());
		ontology.setActive(true);
		ontology.setAllowsCreateTopic(true);
		ontology.setDataModel(dataModelService.getDataModelByName(VIDEO_RESULT_DATA_MODEL));
		ontology.setDescription("Ontology created for storing processed results");
		ontology.setIdentification("VideoResults_" + videoCapture.getIdentification().replaceAll(" ", ""));
		ontology.setJsonSchema(dataModelService.getDataModelByName(VIDEO_RESULT_DATA_MODEL).getJsonSchema());
		ontology.setMetainf("video,processing,yolo,ocr");
		ontology.setPublic(false);
		ontology.setRtdbToHdb(false);
		ontology.setRtdbDatasource(RtdbDatasource.MONGO);
		ontology.setRtdbClean(false);
		return ontology;

	}

	@Override
	public List<VideoCapture> getAll() {
		return videoCaptureRepository.findAll();
	}

	@Override
	public void update(VideoCapture videoCapture) {
		videoCaptureRepository.findById(videoCapture.getId()).ifPresent(db -> {
			db.setIdentification(videoCapture.getIdentification());
			db.setOntology(videoCapture.getOntology());
			db.setUrl(videoCapture.getUrl());
			db.setProcessor(videoCapture.getProcessor());
			db.setSamplingInterval(videoCapture.getSamplingInterval());
			videoCaptureRepository.save(db);
		});

	}

	@Override
	public void delete(String id) {
		videoCaptureRepository.deleteById(id);

	}

	@Override
	public void updateState(VideoCapture videoCapture) {
		if (videoCapture.getState().equals(State.STOP))
			videoCapture.setState(State.START);
		else
			videoCapture.setState(State.STOP);
		videoCaptureRepository.save(videoCapture);

	}

	@Override
	public boolean hasUserAccess(String id, String userid) {
		final VideoCapture vc = videoCaptureRepository.findById(id).orElse(null);
		final User user = userService.getUser(userid);
		return (vc != null && vc.getUser().equals(user) || userService.isUserAdministrator(user));
	}

}
