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
package com.minsait.onesait.platform.config.services.migration;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.Id;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.JpaRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MigrationUtils {

	private static final String PROPERTIES_PREFIX = "onesaitplatform.migrationconfig";

	static Serializable getId(Object o) throws IllegalAccessException {
		final Class<?> clazz = o.getClass();

		final Map<String, Field> fields = new HashMap<>();
		MigrationUtils.getAllFields(fields, clazz);
		for (final Field field : fields.values()) {
			final Annotation[] annotations = field.getDeclaredAnnotations();
			for (final Annotation annotation : annotations) {
				if (annotation.annotationType().equals(Id.class)) {
					final boolean accessible = field.isAccessible();
					field.setAccessible(true);
					// returns the id value if it has @id annotation
					final Serializable id = (Serializable) field.get(o);
					field.setAccessible(accessible);
					return id;
				}
			}
		}

		// if the object is not an entity then null is returned
		return null;

	}

	static String getIdentificationField(Object o) throws IllegalAccessException {
		if (o != null) {
			final Class<?> clazz = o.getClass();

			final Map<String, Field> fields = new HashMap<>();
			MigrationUtils.getAllFields(fields, clazz);
			for (final Field field : fields.values()) {
				if (field.getName().equalsIgnoreCase("identification")) {
					final ObjectMapper mapper = new ObjectMapper();
					JSONObject json;
					try {
						json = new JSONObject(mapper.writeValueAsString(o));
					} catch (JSONException | JsonProcessingException e) {
						return null;
					}
					if (json.has("identification")) {
						return json.getString("identification");
					}
				}
			}
		}
		return null;

	}

	static Map<String, Field> getAllFields(Class<?> type) {
		return getAllFields(new HashMap<>(), type);
	}

	static Map<String, Field> getAllFields(Map<String, Field> fields, Class<?> type) {
		final Field[] ownFields = type.getDeclaredFields();
		for (final Field f : ownFields) {
			if (!f.getType().getName().startsWith("org.aspectj") && !fields.containsKey(f.getName())) {
				fields.put(f.getName(), f);
			}
		}
		if (type.getSuperclass() != null) {
			getAllFields(fields, type.getSuperclass());
		}
		return fields;
	}

	static Collection<Object> createCollection(Class<?> collectionType) {
		if (java.util.List.class.isAssignableFrom(collectionType)) {
			return new ArrayList<>();
		} else if (java.util.Set.class.isAssignableFrom(collectionType)) {
			return new HashSet<>();
		} else {
			// by default it return a list.
			return new ArrayList<>();
		}
	}

	static <T> Class<? extends Serializable> getIdType(Class<T> clazz) {
		final Map<String, Field> fields = new HashMap<>();
		MigrationUtils.getAllFields(fields, clazz);
		for (final Field field : fields.values()) {
			final Annotation[] annotations = field.getDeclaredAnnotations();
			for (final Annotation annotation : annotations) {
				if (annotation.annotationType().equals(Id.class)) {
					// JPA ids are serializables
					@SuppressWarnings("unchecked")
					final Class<? extends Serializable> type = (Class<? extends Serializable>) field.getType();
					return type;
				}
			}
		}
		return null;
	}

	static <T, K extends Serializable> JpaRepository<T, K> getRepository(Class<T> clazz, ApplicationContext ctx) {
		final Class<? extends Serializable> idType = MigrationUtils.getIdType(clazz);
		final String[] beanNamesForType = ctx
				.getBeanNamesForType(ResolvableType.forClassWithGenerics(JpaRepository.class, clazz, idType));

		// If several beans are expected then extract the correct one.
		// At the moment there is only one repository per entity.
		if (beanNamesForType.length > 0) {
			@SuppressWarnings("unchecked")
			final JpaRepository<T, K> repository = (JpaRepository<T, K>) ctx.getBean(beanNamesForType[0]);
			return repository;
		} else {
			return null;
		}
	}

	static boolean isManagedEntity(EntityManager em, Class<?> clazz) {
		try {
			em.getMetamodel().managedType(clazz);
		} catch (final Exception e) {
			return false;
		}
		return true;
	}

	// to load properties from application.yml without spring context.
	// this is necessary because DataFromDB need some properties, but it is used
	// in a Jackson deserializer outside of the Spring context.
	// It uses the ImportExportClasses class as if it was done with spring
	// @Autowire.
	static ImportExportClasses blacklist() {

		final ClassPathResource resource = new ClassPathResource("/migration.blacklist.yml");

		final YamlPropertiesFactoryBean factoryBean = new YamlPropertiesFactoryBean();
		factoryBean.setSingleton(true);
		factoryBean.setResources(resource);

		final Properties properties = factoryBean.getObject();

		final MutablePropertySources propertySources = new MutablePropertySources();
		propertySources.addLast(new PropertiesPropertySource("classes", properties));

		final ImportExportClasses classes = new ImportExportClasses();

		final Binder binder = new Binder(ConfigurationPropertySources.from(propertySources));
		binder.bind(PROPERTIES_PREFIX, Bindable.ofInstance(classes));
		return classes;

	}

	// to load properties from application.yml without spring context.
	// this is necessary because DataFromDB need some properties, but it is used
	// in a Jackson deserializer outside of the Spring context.
	// It uses the ImportExportClasses class as if it was done with spring
	// @Autowire.
	static ImportExportClasses whitelist() {

		final ClassPathResource resource = new ClassPathResource("/migrationbyuser.whitelist.yml");

		final YamlPropertiesFactoryBean factoryBean = new YamlPropertiesFactoryBean();
		factoryBean.setSingleton(true);
		factoryBean.setResources(resource);

		final Properties properties = factoryBean.getObject();

		final MutablePropertySources propertySources = new MutablePropertySources();
		propertySources.addLast(new PropertiesPropertySource("classes", properties));

		final ImportExportClasses classes = new ImportExportClasses();

		final Binder binder = new Binder(ConfigurationPropertySources.from(propertySources));
		binder.bind(PROPERTIES_PREFIX, Bindable.ofInstance(classes));
		return classes;

	}

	// to load properties from application.yml without spring context.
	// this is necessary because DataFromDB need some properties, but it is used
	// in a Jackson deserializer outside of the Spring context.
	// It uses the ImportExportClasses class as if it was done with spring
	// @Autowire.
	static ImportExportClasses trimlist() {

		final ClassPathResource resource = new ClassPathResource("/migrationbyuser.trimlist.yml");

		final YamlPropertiesFactoryBean factoryBean = new YamlPropertiesFactoryBean();
		factoryBean.setSingleton(true);
		factoryBean.setResources(resource);

		final Properties properties = factoryBean.getObject();

		final MutablePropertySources propertySources = new MutablePropertySources();
		propertySources.addLast(new PropertiesPropertySource("classes", properties));

		final ImportExportClasses classes = new ImportExportClasses();
		final Binder binder = new Binder(ConfigurationPropertySources.from(propertySources));
		binder.bind(PROPERTIES_PREFIX, Bindable.ofInstance(classes));

		return classes;

	}

	// to load properties from application.yml without spring context.
	// this is necessary because DataFromDB need some properties, but it is used
	// in a Jackson deserializer outside of the Spring context.
	// It uses the ImportExportClasses class as if it was done with spring
	// @Autowire.
	static ImportExportClasses blackProjectlist() {

		final ClassPathResource resource = new ClassPathResource("/migration.blacklist.project.yml");

		final YamlPropertiesFactoryBean factoryBean = new YamlPropertiesFactoryBean();
		factoryBean.setSingleton(true);
		factoryBean.setResources(resource);

		final Properties properties = factoryBean.getObject();

		final MutablePropertySources propertySources = new MutablePropertySources();
		propertySources.addLast(new PropertiesPropertySource("classes", properties));

		final ImportExportClasses classes = new ImportExportClasses();
		final Binder binder = new Binder(ConfigurationPropertySources.from(propertySources));
		binder.bind(PROPERTIES_PREFIX, Bindable.ofInstance(classes));
		// TO-DO Review Bind changes SB 2
		// PropertiesConfigurationFactory<ImportExportClasses> configurationFactory =
		// new PropertiesConfigurationFactory<>(
		// classes);
		// configurationFactory.setPropertySources(propertySources);
		// configurationFactory.setTargetName(PROPERTIES_PREFIX); //
		// it's the same prefix as the one
		// // defined in the
		// // @ConfigurationProperties
		// configurationFactory.bindPropertiesToTarget();

		return classes;

	}
}
