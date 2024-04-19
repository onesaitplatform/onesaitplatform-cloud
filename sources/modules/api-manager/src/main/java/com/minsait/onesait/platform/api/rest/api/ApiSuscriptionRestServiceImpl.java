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
package com.minsait.onesait.platform.api.rest.api;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.api.rest.api.fiql.ApiSuscripcionFIQL;
import com.minsait.onesait.platform.api.service.api.ApiServiceRest;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.services.apimanager.dto.ApiSuscripcionDTO;

@Component("apiSuscripcionRestServiceImpl")
public class ApiSuscriptionRestServiceImpl implements ApiSuscriptionRestService {

	@Autowired
	private ApiServiceRest apiService;

	@Autowired
	private ApiSuscripcionFIQL apiSuscripcionFIQL;

	@Override
	public Response getApiSuscripciones(String identificacionApi, String tokenUsuario) throws GenericOPException {
		return Response.ok(
				apiSuscripcionFIQL.toApiSuscripcionDTO(apiService.findApiSuscriptions(identificacionApi, tokenUsuario)))
				.build();
	}

	@Override
	public Response getApiSuscripcionesUsuario(String identificacionUsuario, String tokenUsuario)
			throws GenericOPException {
		return Response.ok(
				apiSuscripcionFIQL.toApiSuscripcionesDTO(apiService.findApiSuscripcionesUser(identificacionUsuario)))
				.build();
	}

	@Override
	public Response autorize(ApiSuscripcionDTO suscripcion, String tokenUsuario) throws GenericOPException {
		apiService.createSuscripcion(apiSuscripcionFIQL.copyProperties(suscripcion), tokenUsuario);
		final Object[] parametros = { suscripcion.getApiIdentification(), suscripcion.getUserId() };
		return Response.ok(parametros).type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response autorizeUpdate(ApiSuscripcionDTO suscripcion, String tokenUsuario) throws GenericOPException {
		apiService.updateSuscripcion(apiSuscripcionFIQL.copyProperties(suscripcion), tokenUsuario);
		final Object[] parametros = { suscripcion.getApiIdentification(), suscripcion.getUserId() };
		return Response.ok(parametros).type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response deleteAutorizacion(ApiSuscripcionDTO suscripcion, String tokenUsuario) throws GenericOPException {
		apiService.removeSuscripcionByUserAndAPI(apiSuscripcionFIQL.copyProperties(suscripcion), tokenUsuario);
		final Object[] parametros = { suscripcion.getApiIdentification(), suscripcion.getUserId() };
		return Response.ok(parametros).type(MediaType.APPLICATION_JSON).build();
	}

}