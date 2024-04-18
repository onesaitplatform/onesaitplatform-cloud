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
package com.minsait.onesait.platform.config.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.Internationalization;
import com.minsait.onesait.platform.config.model.User;

public interface InternationalizationRepository extends JpaRepository<Internationalization, String> {

	public List<Internationalization> findByIdentification(String identification);

	public void deleteByIdentificationAndUser(String identification, User user);
    
    public List<Internationalization> findByUser(User user);
    
    @Query("SELECT i FROM Internationalization AS i WHERE i.user=:user OR i.isPublic=true ORDER BY i.identification ASC")
    public List<Internationalization> findByUserOrIsPublic(@Param("user") User user);

	public List<Internationalization> findAllByOrderByIdentificationAsc();

	public Internationalization findInternationalizationByIdentification(String identification);

	public List<Internationalization> findByUserOrderByIdentificationAsc(User user);

	public List<Internationalization> findByUserOrderByIdentificationDesc(User user);

	public List<Internationalization> findByUserOrderByCreatedAtAsc(User user);

	public List<Internationalization> findByUserOrderByCreatedAtDesc(User user);

	public List<Internationalization> findByUserOrderByUpdatedAtAsc(User user);
    
    @Query("SELECT i FROM Internationalization AS i WHERE i.identification LIKE %:identification% AND i.description LIKE %:description% ORDER BY i.identification ASC")
    public List<Internationalization> findByIdentificationContainingAndDescriptionContaining(@Param("identification") String identification, @Param("description") String description);
    
    @Query("SELECT i FROM Internationalization AS i WHERE i.identification LIKE %:identification% AND i.description LIKE %:description% AND (i.user=:user OR i.isPublic=True) ORDER BY i.identification ASC")
    public List<Internationalization> findByUserAndIdentificationContainingAndDescriptionContainingOrderByIdentificationAsc(@Param("user") User user, @Param("identification") String identification, @Param("description") String description);

	public List<Internationalization> findByUserOrderByUpdatedAtDesc(User user);

	public List<Internationalization> findByDescription(String description);

	public List<Internationalization> findByIdentificationContaining(String identification);

	public List<Internationalization> findByDescriptionContaining(String description);

	public List<Internationalization> findByUserAndIdentificationContainingAndDescriptionContaining(User user,
			String identification, String description);

	public List<Internationalization> findByUserAndIdentificationContaining(User user, String identification);

	public List<Internationalization> findByUserAndDescriptionContaining(User user, String description);

	public List<Internationalization> findByIdentificationAndDescriptionAndUser(String identification,
			String description, User user);

	public List<Internationalization> findByIdentificationAndDescription(String identification, String description);

}
