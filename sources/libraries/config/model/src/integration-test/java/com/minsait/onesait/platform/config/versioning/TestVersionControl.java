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
package com.minsait.onesait.platform.config.versioning;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.commons.git.CommitWrapper;
import com.minsait.onesait.platform.commons.git.GitException;
import com.minsait.onesait.platform.commons.git.GitServiceManager;
import com.minsait.onesait.platform.commons.git.GitlabConfiguration;
import com.minsait.onesait.platform.commons.git.VersioningCommitContextHolder;
import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.DashboardConf;
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.GadgetFavorite;
import com.minsait.onesait.platform.config.model.GadgetMeasure;
import com.minsait.onesait.platform.config.model.GadgetTemplate;
import com.minsait.onesait.platform.config.model.GadgetTemplateType;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;
import com.minsait.onesait.platform.config.repository.DashboardConfRepository;
import com.minsait.onesait.platform.config.repository.DashboardRepository;
import com.minsait.onesait.platform.config.repository.DashboardUserAccessTypeRepository;
import com.minsait.onesait.platform.config.repository.GadgetDatasourceRepository;
import com.minsait.onesait.platform.config.repository.GadgetFavoriteRepository;
import com.minsait.onesait.platform.config.repository.GadgetMeasureRepository;
import com.minsait.onesait.platform.config.repository.GadgetRepository;
import com.minsait.onesait.platform.config.repository.GadgetTemplateRepository;
import com.minsait.onesait.platform.config.repository.GadgetTemplateTypeRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.RoleRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(IntegrationTest.class)
@Slf4j
@Ignore
public class TestVersionControl {

	@Autowired
	private GadgetRepository gadgetRepository;
	@Autowired
	private GadgetMeasureRepository gadgetMeasureRepository;
	@Autowired
	private GadgetFavoriteRepository gadgetFavoriteRepository;
	@Autowired
	private GadgetDatasourceRepository gadgetDatasourceRepository;
	@Autowired
	private GadgetTemplateRepository gadgetTemplateRepository;
	@Autowired
	private GadgetTemplateTypeRepository gadgetTemplateTypeRepository;
	@Autowired
	private DashboardRepository dashboardRepository;
	@Autowired
	private DashboardConfRepository dashboardConfRepository;
	@Autowired
	private OntologyRepository ontologyRepository;

	private static final String ADMIN = "fjgcornejo";

	private static final String DIR = "/tmp/resources/";

	@Test
	public void testSerialize() {
		ontologyRepository.findAll().forEach(g -> {

			try {
				final String jsonAsYaml = g.serialize();
				FileUtils.writeStringToFile(new File(DIR
						+ Tenant2SchemaMapper
						.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema())
						+ "/" + g.getClass().getSimpleName() + "/" + g.fileName()), jsonAsYaml);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		});
		gadgetRepository.findAll().forEach(g -> {

			try {
				final String jsonAsYaml = g.serialize();
				FileUtils.writeStringToFile(new File(DIR
						+ Tenant2SchemaMapper
						.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema())
						+ "/" + g.getClass().getSimpleName() + "/" + g.fileName()), jsonAsYaml);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		});
		gadgetMeasureRepository.findAll().forEach(g -> {

			try {
				final String jsonAsYaml = g.serialize();
				FileUtils.writeStringToFile(new File(DIR
						+ Tenant2SchemaMapper
						.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema())
						+ "/" + g.getClass().getSimpleName() + "/" + g.fileName()), jsonAsYaml);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		});
		gadgetFavoriteRepository.findAll().forEach(g -> {

			try {
				final String jsonAsYaml = g.serialize();
				FileUtils.writeStringToFile(new File(DIR
						+ Tenant2SchemaMapper
						.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema())
						+ "/" + g.getClass().getSimpleName() + "/" + g.fileName()), jsonAsYaml);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		});
		gadgetDatasourceRepository.findAll().forEach(g -> {

			try {
				final String jsonAsYaml = g.serialize();
				FileUtils.writeStringToFile(new File(DIR
						+ Tenant2SchemaMapper
						.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema())
						+ "/" + g.getClass().getSimpleName() + "/" + g.fileName()), jsonAsYaml);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		});
		gadgetTemplateRepository.findAll().forEach(g -> {

			try {
				final String jsonAsYaml = g.serialize();
				FileUtils.writeStringToFile(new File(DIR
						+ Tenant2SchemaMapper
						.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema())
						+ "/" + g.getClass().getSimpleName() + "/" + g.fileName()), jsonAsYaml);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		});
		gadgetTemplateTypeRepository.findAll().forEach(g -> {

			try {
				final String jsonAsYaml = g.serialize();
				FileUtils.writeStringToFile(new File(DIR
						+ Tenant2SchemaMapper
						.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema())
						+ "/" + g.getClass().getSimpleName() + "/" + g.fileName()), jsonAsYaml);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		});
		dashboardRepository.findAll().forEach(d -> {
			try {
				final String jsonAsYaml = d.serialize();
				FileUtils.writeStringToFile(new File(DIR
						+ Tenant2SchemaMapper
						.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema())
						+ "/" + d.getClass().getSimpleName() + "/" + d.fileName()), jsonAsYaml);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		});
		dashboardConfRepository.findAll().forEach(d -> {
			try {
				final String jsonAsYaml = d.serialize();
				FileUtils.writeStringToFile(new File(DIR
						+ Tenant2SchemaMapper
						.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema())
						+ "/" + d.getClass().getSimpleName() + "/" + d.fileName()), jsonAsYaml);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		});
	}

	@Test
	public void testDeserialize() {
		File dir = new File(
				DIR + Tenant2SchemaMapper.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema())
				+ "/" + Dashboard.class.getSimpleName() + "/");
		Arrays.asList(dir.listFiles()).stream().map(f -> {
			Dashboard d = new Dashboard();
			try {
				d = d.deserialize(FileUtils.readFileToString(f, StandardCharsets.UTF_8));
				versioningIOService.restoreFromFileSystem(d);
			} catch (final IOException e) {
				//
			}
			return d;
		}).collect(Collectors.toList());
		dir = new File(
				DIR + Tenant2SchemaMapper.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema())
				+ "/" + DashboardConf.class.getSimpleName() + "/");
		Arrays.asList(dir.listFiles()).stream().map(f -> {
			DashboardConf d = new DashboardConf();
			try {
				d = d.deserialize(FileUtils.readFileToString(f, StandardCharsets.UTF_8));
				versioningIOService.restoreFromFileSystem(d);
			} catch (final IOException e) {
				//
			}
			return d;
		}).collect(Collectors.toList());
		dir = new File(
				DIR + Tenant2SchemaMapper.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema())
				+ "/" + Gadget.class.getSimpleName() + "/");
		Arrays.asList(dir.listFiles()).stream().map(f -> {
			Gadget d = new Gadget();
			try {
				d = d.deserialize(FileUtils.readFileToString(f, StandardCharsets.UTF_8));
				versioningIOService.restoreFromFileSystem(d);
			} catch (final IOException e) {
				//
			}
			return d;
		}).collect(Collectors.toList());
		dir = new File(
				DIR + Tenant2SchemaMapper.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema())
				+ "/" + GadgetMeasure.class.getSimpleName() + "/");
		Arrays.asList(dir.listFiles()).stream().map(f -> {
			GadgetMeasure d = new GadgetMeasure();
			try {
				d = d.deserialize(FileUtils.readFileToString(f, StandardCharsets.UTF_8));
				versioningIOService.restoreFromFileSystem(d);
			} catch (final IOException e) {
				//
			}
			return d;
		}).collect(Collectors.toList());
		dir = new File(
				DIR + Tenant2SchemaMapper.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema())
				+ "/" + GadgetDatasource.class.getSimpleName() + "/");
		Arrays.asList(dir.listFiles()).stream().map(f -> {
			GadgetDatasource d = new GadgetDatasource();
			try {
				d = d.deserialize(FileUtils.readFileToString(f, StandardCharsets.UTF_8));
				versioningIOService.restoreFromFileSystem(d);
			} catch (final IOException e) {
				//
			}
			return d;
		}).collect(Collectors.toList());
		dir = new File(
				DIR + Tenant2SchemaMapper.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema())
				+ "/" + GadgetTemplate.class.getSimpleName() + "/");
		Arrays.asList(dir.listFiles()).stream().map(f -> {
			GadgetTemplate d = new GadgetTemplate();
			try {
				d = d.deserialize(FileUtils.readFileToString(f, StandardCharsets.UTF_8));
				versioningIOService.restoreFromFileSystem(d);
			} catch (final IOException e) {
				//
			}
			return d;
		}).collect(Collectors.toList());
		dir = new File(
				DIR + Tenant2SchemaMapper.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema())
				+ "/" + GadgetTemplateType.class.getSimpleName() + "/");
		Arrays.asList(dir.listFiles()).stream().map(f -> {
			GadgetTemplateType d = new GadgetTemplateType();
			try {
				d = d.deserialize(FileUtils.readFileToString(f, StandardCharsets.UTF_8));
				versioningIOService.restoreFromFileSystem(d);
			} catch (final IOException e) {
				//
			}
			return d;
		}).collect(Collectors.toList());
		dir = new File(
				DIR + Tenant2SchemaMapper.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema())
				+ "/" + GadgetFavorite.class.getSimpleName() + "/");

	}

	@Test
	public void testDeserializeAndRestore() {
		final File dir = new File(
				DIR + Tenant2SchemaMapper.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema())
				+ "/" + Dashboard.class.getSimpleName() + "/");
		final List<Dashboard> ddss = Arrays.asList(dir.listFiles()).stream().map(f -> {
			Dashboard d = new Dashboard();
			try {
				d = d.deserialize(FileUtils.readFileToString(f, StandardCharsets.UTF_8));

			} catch (final IOException e) {
				//
			}
			return d;
		}).collect(Collectors.toList());
		dashboardRepository.saveAll(ddss);

	}

	@Test
	public void testVersioningListener_withNewEntity() throws InterruptedException {
		VersioningCommitContextHolder.setCommitMessage("Cambiando cosas");
		VersioningCommitContextHolder.setUserId(ADMIN);
		Dashboard d = new Dashboard();
		d.setDescription("Test description");
		d.setIdentification("Test_dashboard");
		final User u = new User();
		u.setUserId(ADMIN);
		d.setUser(u);
		d = dashboardRepository.save(d);
		dashboardRepository.delete(d);
	}

	@Autowired
	private VersioningManager versioningManager;
	@Autowired
	private VersioningIOService versioningIOService;

	@Test
	public void testVersioningListener_withUpdatedEntity() {
		List<Dashboard> ddss = dashboardRepository.findAll();
		if (!ddss.isEmpty()) {
			final Dashboard d = ddss.get(0);
			d.setDescription(new StringBuilder(d.getDescription()).reverse().toString());
			VersioningCommitContextHolder.setCommitMessage("Cambiando cosas");
			VersioningCommitContextHolder.setUserId(ADMIN);
			dashboardRepository.save(d);
			ddss = dashboardRepository.findAll();
		}
	}

	@Test
	public void testRestore() throws IOException {
		// final List<Dashboard> ddss = dashboardRepository.findAll();
		// if (!ddss.isEmpty()) {
		// final Dashboard d = ddss.get(0);
		// versioningIOService.restoreFromFileSystem(d);
		// }
		final Dashboard d = dashboardRepository.findById("36f73183-dc5d-4563-9d57-e8feb4bb24a4").orElse(null);
		versioningIOService.restoreFromFileSystem(d);
		log.info("{}", dashboardRepository.findById("36f73183-dc5d-4563-9d57-e8feb4bb24a4").orElse(null).serialize());
	}

	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private UserRepository userRepository;

	@Test
	public void createTestUser() {
		final User u = new User();
		u.setUserId(ADMIN);
		u.setEmail(ADMIN + "@minsait.com");
		u.setFullName("Javier Gómez-Cornejo Gil");
		u.setActive(true);
		u.setPassword("Changed2020!");
		u.setRole(roleRepository.findById(Role.Type.ROLE_ADMINISTRATOR.name()).orElse(null));
		userRepository.save(u);
	}

	@Autowired
	private GitServiceManager gitServiceManager;

	@Test
	public void testCommits() {
		final GitlabConfiguration gitConfig = GitlabConfiguration.builder().site("https://github.com/")
				.user("Fjgcornejo").privateToken("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").projectName("versioning")
				.email("fjgcornejo@minsait.com").branch("main")
				.projectURL("https://github.com/Fjgcornejo/versioning.git").build();
		final List<Dashboard> ddss = dashboardRepository.findAll();
		if (!ddss.isEmpty()) {
			final Dashboard d = ddss.get(0);
			final List<CommitWrapper> commits = gitServiceManager.dispatchService(gitConfig.getSite())
					.getCommitsForFile(gitConfig, versioningIOService.relativePath(d), null);
			log.info("Commits: {}", commits.size());
		}
	}

	@Test
	public void restoreCommit() {
		final GitlabConfiguration gitConfig = GitlabConfiguration.builder().site("https://github.com/")
				.user("Fjgcornejo").privateToken("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").projectName("versioning")
				.email("fjgcornejo@minsait.com").branch("main")
				.projectURL("https://github.com/Fjgcornejo/versioning.git").build();
		final List<Dashboard> ddss = dashboardRepository.findAll();
		if (!ddss.isEmpty()) {
			final Dashboard d = ddss.get(0);
			final List<CommitWrapper> commits = gitServiceManager.dispatchService(gitConfig.getSite())
					.getCommitsForFile(gitConfig, versioningIOService.relativePath(d), null);
			final CommitWrapper commit = commits.get(commits.size() - 2);
			log.info("Commit message of restore: {}, id: {}, file: {}", commit.getCommit().getMessage(),
					commit.getCommitId(), d.fileName());
			versioningManager.restoreSerialization(d, commit.getCommitId(), ADMIN, "Restoring file from second commit");

		}
	}

	@Test
	public void testFindVersionableBeans()
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		final BeanDefinitionRegistry bdr = new SimpleBeanDefinitionRegistry();
		final ClassPathBeanDefinitionScanner s = new ClassPathBeanDefinitionScanner(bdr);

		final TypeFilter tf = new AssignableTypeFilter(Versionable.class);
		s.addIncludeFilter(tf);
		s.setIncludeAnnotationConfig(false);
		s.scan("com.minsait.onesait.platform.config.model");
		final String[] beans = bdr.getBeanDefinitionNames();
		Class.forName("com.minsait.onesait.platform.config.model.Gadget").newInstance();
		for (final String b : beans) {
			log.info("Bean of type {}", b);
		}
	}

	@Autowired
	private DashboardUserAccessTypeRepository dashboardUserAccessTypeRepository;

	@Autowired
	VersioningRepositoryFacade versioningRepositoryFacade;

	@Test
	public void testRepositories() throws Exception {
		dashboardUserAccessTypeRepository.findByUser(userRepository.findByUserId(ADMIN));
		gadgetMeasureRepository.findByUser(userRepository.findByUserId("administrator"));
		gadgetTemplateTypeRepository.findByUser(userRepository.findByUserId("administrator"));
		final DashboardConf conf = dashboardConfRepository.findAll().get(0);
		final JpaRepository<Versionable<?>, Object> repo = versioningRepositoryFacade.getJpaRepository(conf);
		final Method findByUser = repo.getClass().getMethod("findByUser", User.class);
		findByUser.invoke(repo, userRepository.findByUserId("administrator"));
	}

	@Test
	public void test() {
		final Versionable<Ontology> o = ontologyRepository.findByIdentification("WoWCharacter");
		try {
			o.serialize();
		} catch (final Exception e) {
			// TODO: handle exception
		}
	}

	@Test
	public void testGitCommands() {
		try {

			final ProcessBuilder pb = new ProcessBuilder("git", "show",
					"1e997a9fe9378bf0d18cb9f2842a2eb098e0b854:onesaitplatform/Dashboard/MyDashboard_36f73182-dc5d-4563-9d57-e8feb4bb24a4.yaml");
			pb.redirectErrorStream(true);
			log.info(pb.command().toString());
			pb.directory(new File("/tmp/resources"));
			executeAndReadOutput(pb.start());
		} catch (final Exception e) {
			throw new GitException("", e);
		}
	}

	private String executeAndReadOutput(Process p) throws IOException, InterruptedException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		final StringBuilder builder = new StringBuilder();
		String line = null;
		p.waitFor();
		while ((line = reader.readLine()) != null) {
			builder.append(line);
			builder.append(System.getProperty("line.separator"));
		}
		final String result = builder.toString();
		log.debug(result);
		return result;
	}

	@Test
	public void testSnakeYAML() throws IOException {
		final GadgetTemplate template = gadgetTemplateRepository.findById("bar").orElse(null);
		final String s = template.serialize();
		log.info(s);
	}

	@Test
	public void testReadAll() {
		final List<Versionable<?>> versionables = versioningIOService.readAllVersionables(VersioningIOService.DIR);
		if (log.isDebugEnabled()) {
			log.debug("Versionables count: {}", versionables.size());
		}
	}

	@Test
	public void testExclusions() {
		final Map<String, Set<String>> exclusions = new HashMap<>();
		final Set<String> ids = new HashSet<>();
		ids.add("MASTER-Ontology-10");
		exclusions.put("Ontology", ids);
		final Set<String> userIds = new HashSet<>();
		userIds.add("developer");
		exclusions.put("User", userIds);
		final List<Versionable<?>> versionables = getCurrentDbVersionables(getVersionableClases());
		final List<Versionable<?>> vfiltered = versionables.stream().map(v -> v.runExclusions(exclusions, userIds))
				.filter(Objects::nonNull).collect(Collectors.toList());

		log.info("Initial vs {}", versionables.size());
		log.info("Final vs {}", vfiltered.size());
	}

	public Map<String, String> getVersionableClases() {
		final Map<String, String> map = new LinkedHashMap<>();
		final BeanDefinitionRegistry bdr = new SimpleBeanDefinitionRegistry();
		final ClassPathBeanDefinitionScanner s = new ClassPathBeanDefinitionScanner(bdr);

		final TypeFilter tf = new AssignableTypeFilter(Versionable.class);
		s.addIncludeFilter(tf);
		s.setIncludeAnnotationConfig(false);
		s.scan(VersioningTxBusinessServiceImpl.SCAN_PACKAGE);
		Arrays.asList(bdr.getBeanDefinitionNames()).stream().sorted()
		.forEach(b -> map.put(bdr.getBeanDefinition(b).getBeanClassName(), b));
		return map;
	}

	private List<Versionable<?>> getCurrentDbVersionables(Map<String, String> listedClasses) {
		return listedClasses.entrySet().stream().map(e -> {
			Versionable<?> o;
			try {
				o = (Versionable<?>) Class.forName(e.getKey()).newInstance();
				return versioningRepositoryFacade.getJpaRepository(o).findAll();
			} catch (final Exception e1) {
				log.error("Could not instantiate class of type {}", e.getKey(), e1);
			}
			return null;
		}).filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toList());
	}

}
