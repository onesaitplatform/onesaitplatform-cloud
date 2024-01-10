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
package com.minsait.onesait.platform.services.jenkins;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.cdancy.jenkins.rest.JenkinsClient;
import com.cdancy.jenkins.rest.domain.job.BuildInfo;
import com.cdancy.jenkins.rest.domain.job.JobInfo;
import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.components.JenkinsConfiguration;
import com.minsait.onesait.platform.controlpanel.ControlPanelWebApplication;
import com.minsait.onesait.platform.controlpanel.services.jenkins.JenkinsService;

@SpringBootTest(classes = ControlPanelWebApplication.class)
@Category(IntegrationTest.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@TestPropertySource(locations = "/application-integration-test.yml")
@RunWith(SpringRunner.class)
public class JenkinsTest {

	@Autowired
	private JenkinsService jenkinsService;

	private final static String JENKINS_URL = "https://devops.onesaitplatform.com/jenkins";

	private final static String USERNAME = "fjgcornejo";

	@Value("${test.jenkins.token}")
	private String TOKEN;
	private final static String NEW_JOB_NAME = "microserviceTest";
	private final static String DOCKER_PARAM = "DOCKER_MODULETAGVALUE";
	private final static String GIT_PARAM = "GIT_URL";
	private final static String DOCKER_TAG = "latest";

	private final static String CONFIG_XML = "<?xml version='1.1' encoding='UTF-8'?>\n"
			+ "<flow-definition plugin=\"workflow-job@2.12.2\">\n" + "  <actions>\n"
			+ "    <org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobPropertyTrackerAction plugin=\"pipeline-model-definition@1.1.9\">\n"
			+ "      <jobProperties/>\n" + "      <triggers/>\n" + "      <parameters>\n"
			+ "        <string>DOCKER_MODULETAGVALUE</string>\n" + "        <string>DOCKER_USERNAMEVALUE</string>\n"
			+ "        <string>DOCKER_PUSHREGISTRY_PRIVATE</string>\n"
			+ "        <string>ENVIRONMENT_DEV_UPGRADE</string>\n" + "        <string>GIT_URL</string>\n"
			+ "        <string>GIT_BRANCHNAME</string>\n" + "        <string>PRIVATE_REGISTRY</string>\n"
			+ "      </parameters>\n"
			+ "    </org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobPropertyTrackerAction>\n"
			+ "  </actions>\n" + "  <description>Microservice CI pipeline</description>\n"
			+ "  <keepDependencies>false</keepDependencies>\n" + "  <properties>\n"
			+ "    <jenkins.model.BuildDiscarderProperty>\n" + "      <strategy class=\"hudson.tasks.LogRotator\">\n"
			+ "        <daysToKeep>1</daysToKeep>\n" + "        <numToKeep>1</numToKeep>\n"
			+ "        <artifactDaysToKeep>-1</artifactDaysToKeep>\n"
			+ "        <artifactNumToKeep>-1</artifactNumToKeep>\n" + "      </strategy>\n"
			+ "    </jenkins.model.BuildDiscarderProperty>\n"
			+ "    <org.jenkinsci.plugins.workflow.job.properties.DisableConcurrentBuildsJobProperty/>\n"
			+ "    <hudson.model.ParametersDefinitionProperty>\n" + "      <parameterDefinitions>\n"
			+ "        <hudson.model.StringParameterDefinition>\n" + "          <name>GIT_URL</name>\n"
			+ "          <description>URL to the Git Repository</description>\n"
			+ "          <defaultValue>https://onesait-git.cwbyminsait.com/onesait-platform/onesait-cloud-platform.git</defaultValue>\n"
			+ "          <trim>false</trim>\n" + "        </hudson.model.StringParameterDefinition>\n"
			+ "         <hudson.model.StringParameterDefinition>\n" + "          <name>GIT_BRANCHNAME</name>\n"
			+ "          <description>compilation branch name</description>\n"
			+ "          <defaultValue>master</defaultValue>\n" + "          <trim>false</trim>\n"
			+ "        </hudson.model.StringParameterDefinition>\n"
			+ "        <hudson.model.StringParameterDefinition>\n" + "          <name>DOCKER_MODULETAGVALUE</name>\n"
			+ "          <description>Image generation tag value</description>\n"
			+ "          <defaultValue>1.0.0</defaultValue>\n" + "          <trim>false</trim>\n"
			+ "        </hudson.model.StringParameterDefinition>\n"
			+ "        <hudson.model.StringParameterDefinition>\n" + "          <name>DOCKER_USERNAMEVALUE</name>\n"
			+ "          <description>Image registry image namespace</description>\n"
			+ "          <defaultValue>onesaitplatform</defaultValue>\n" + "          <trim>false</trim>\n"
			+ "        </hudson.model.StringParameterDefinition>\n"
			+ "        <hudson.model.StringParameterDefinition>\n"
			+ "          <name>DOCKER_PUSHREGISTRY_PRIVATE</name>\n"
			+ "          <description>Push image to private registry</description>\n"
			+ "          <defaultValue>true</defaultValue>\n" + "          <trim>false</trim>\n"
			+ "        </hudson.model.StringParameterDefinition>\n"
			+ "        <hudson.model.StringParameterDefinition>\n" + "          <name>PRIVATE_REGISTRY</name>\n"
			+ "          <description>URL to the docker registry</description>\n"
			+ "          <defaultValue>registry.onesaitplatform.com</defaultValue>\n" + "          <trim>false</trim>\n"
			+ "        </hudson.model.StringParameterDefinition>\n"
			+ "        <hudson.model.StringParameterDefinition>\n" + "          <name>ENVIRONMENT_DEV_UPGRADE</name>\n"
			+ "          <description>Automatically upgrade the service in the development environment</description>\n"
			+ "          <defaultValue>false</defaultValue>\n" + "          <trim>false</trim>\n"
			+ "        </hudson.model.StringParameterDefinition>\n" + "      </parameterDefinitions>\n"
			+ "    </hudson.model.ParametersDefinitionProperty>\n"
			+ "    <com.dabsquared.gitlabjenkins.connection.GitLabConnectionProperty plugin=\"gitlab-plugin@1.4.5\">\n"
			+ "      <gitLabConnection>gitlab-jenkins</gitLabConnection>\n"
			+ "    </com.dabsquared.gitlabjenkins.connection.GitLabConnectionProperty>\n"
			+ "    <org.jenkinsci.plugins.gitlablogo.GitlabLogoProperty plugin=\"gitlab-logo@1.0.3\">\n"
			+ "      <repositoryName></repositoryName>\n"
			+ "    </org.jenkinsci.plugins.gitlablogo.GitlabLogoProperty>\n"
			+ "    <org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>\n"
			+ "      <triggers/>\n"
			+ "    </org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>\n" + "  </properties>\n"
			+ "  <definition class=\"org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition\" plugin=\"workflow-cps@2.39\">\n"
			+ "    <script>pipeline {\n" + "   // Execute the Pipeline, or stage, on any available agent	\n"
			+ "   agent { \n" + "           node { \n" + "              label &apos;sofia2master&apos; \n"
			+ "            } \n" + "          }\n" + "   \n" + "   tools { \n" + "   	  maven &apos;maven&apos;\n"
			+ "   	  jdk &apos;java8&apos;\n" + "   }   \n" + "\n" + "   environment {          \n"
			+ "      // Base sources path \n"
			+ "      BASEPATH = &apos;/datadrive/jenkins/.jenkins/jobs/microserviceTest/workspace&apos;\n"
			+ "      SOURCESPATH = &apos;/datadrive/jenkins/.jenkins/jobs/microserviceTest/workspace/sources&apos;\n"
			+ "      DOCKERPATH = &apos;/datadrive/jenkins/.jenkins/jobs/microserviceTest/workspace/docker&apos;\n"
			+ "      MICROSERVICE_NAME = &apos;microservice&apos;\n"
			+ "	    //CATTLE_ACCESS_KEY = &apos;1CD18DB0F67C50608DC3&apos;\n"
			+ "      //CATTLE_SECRET_KEY = &apos;VXAowSGp27LnNXATGFqqVPwxx9EiPDBSgkjPx8nv&apos;\n" + "   }\n" + "   \n"
			+ "   parameters { \n" + "       string(name: &apos;GIT_URL&apos;, \n"
			+ "              defaultValue: &apos;https://onesait-git.cwbyminsait.com/onesait-platform/onesait-cloud-platform.git&apos;, \n"
			+ "              description: &apos;URL Git Repository&apos;)\n"
			+ "       string(name: &apos;GIT_BRANCHNAME&apos;, \n"
			+ "              defaultValue: &apos;master&apos;, \n"
			+ "              description: &apos;compilation branch name&apos;)\n"
			+ "       string(name: &apos;DOCKER_MODULETAGVALUE&apos;, \n"
			+ "              defaultValue: &apos;1.0.0&apos;, \n"
			+ "              description: &apos;Image generation tag value&apos;)\n"
			+ "       string(name: &apos;DOCKER_USERNAMEVALUE&apos;, \n"
			+ "              defaultValue: &apos;onesaitplatform&apos;, \n"
			+ "              description: &apos;Image registry image namespace&apos;)\n"
			+ "       string(name: &apos;DOCKER_PUSHREGISTRY_PRIVATE&apos;, \n"
			+ "              defaultValue: &apos;false&apos;, \n"
			+ "              description: &apos;Push image to private registry&apos;)\n"
			+ "       string(name: &apos;PRIVATE_REGISTRY&apos;, \n"
			+ "              defaultValue: &apos;registry.onesaitplatform.com&apos;, \n"
			+ "              description: &apos;URL to the docker registry&apos;)\n"
			+ "       string(name: &apos;ENVIRONMENT_DEV_UPGRADE&apos;,\n"
			+ "              defaultValue: &apos;false&apos;,\n"
			+ "              description: &apos;Automatically upgrade the service in the development environment&apos;)\n"
			+ "   }    \n" + "   \n" + "   stages {\n" + "       stage(&apos;Fetching from repository&apos;) {	\n"
			+ "	   		steps {	 \n"
			+ "           sh &quot;/opt/gitlab/embedded/bin/git clone ${params.GIT_URL} ${env.BASEPATH}&quot;		 	   		\n"
			+ "		      //Fetch code from develop branch		      \n"
			+ "		      //git url: ${params.GIT_URL}, \n"
			+ "		      //credentialsId: &apos;moaf-onesaitplatform-credentials&apos;  ,\n"
			+ "		      //branch: params.GIT_BRANCHNAME\n" + "	   		}          \n" + "       }\n" + "	   \n"
			+ "	   stage(&apos;Build Artifacts&apos;) {\n" + "	   		steps {\n" + "		     // Run maven build\n"
			+ "		     dir(&quot;${env.SOURCESPATH}&quot;) {\n" + "	         	sh &quot;mvn clean install&quot;\n"
			+ "	         }\n" + "	   		}\n" + "	   }\n" + "	   \n"
			+ "	   stage(&apos;Generate Docker images&apos;) {\n" + "	   		steps {\n"
			+ "		     // variable sustitution\n" + "	      	 dir(&quot;${env.BASEPATH}&quot;)	{\n"
			+ "            sh &quot;cp sources/target/*.jar docker/&quot;\n"
			+ "	         	sh &quot;docker build -t ${params.DOCKER_USERNAMEVALUE}/${env.MICROSERVICE_NAME}:${params.DOCKER_MODULETAGVALUE} docker/&quot;\n"
			+ "	         }\n" + "	   		}\n" + "	   }\n" + "\n"
			+ "     stage(&apos;Push Docker images&apos;) {\n" + "        when {\n"
			+ "          expression { params.DOCKER_PUSHREGISTRY_PRIVATE == &apos;true&apos; }\n" + "        }\n"
			+ "        steps {\n"
			+ "          sh &quot;docker tag ${params.DOCKER_USERNAMEVALUE}/${env.MICROSERVICE_NAME}:${params.DOCKER_MODULETAGVALUE} $PRIVATE_REGISTRY/${params.DOCKER_USERNAMEVALUE}/${env.MICROSERVICE_NAME}:${params.DOCKER_MODULETAGVALUE}&quot;\n"
			+ "          sh &quot;docker push $PRIVATE_REGISTRY/${params.DOCKER_USERNAMEVALUE}/${env.MICROSERVICE_NAME}:${params.DOCKER_MODULETAGVALUE}&quot;\n"
			+ "         \n" + "        }\n" + "     }\n" + "	   \n"
			+ "	   stage(&apos;Upgrade Rancher module via cURL&apos;) {\n" + "	       when {\n"
			+ "	           expression { params.ENVIRONMENT_DEV_UPGRADE == &apos;true&apos; }\n"
			+ "	       } 	       \n" + "	       steps {\n" + "	           script {\n"
			+ "                def launchConfig = sh (returnStdout: true, script: &quot;&quot;&quot;	               \n"
			+ "                        curl -k -u &apos;${env.CATTLE_ACCESS_KEY}:${env.CATTLE_SECRET_KEY}&apos; \\\n"
			+ "                        -X GET \\\n"
			+ "                        -H &apos;Accept: application/json&apos; \\\n"
			+ "                        -H &apos;Content-Type: application/json&apos; \\\n"
			+ "                        &quot;https://rancher.sofia4cities.com/v2-beta/projects/1a68/services/1s754/&quot; | jq &apos;.upgrade.inServiceStrategy.launchConfig&apos;	 \n"
			+ "                    &quot;&quot;&quot;)\n" + "                echo launchConfig\n" + "                \n"
			+ "	            def output = sh (returnStdout: true, script: &quot;&quot;&quot;\n"
			+ "                        curl -k -u &apos;${env.CATTLE_ACCESS_KEY}:${env.CATTLE_SECRET_KEY}&apos; \\\n"
			+ "                        -X POST \\\n"
			+ "                        -H &apos;Accept: application/json&apos; \\\n"
			+ "                        -H &apos;Content-Type: application/json&apos; \\\n"
			+ "                        -d &apos;{&quot;inServiceStrategy&quot;: { &quot;startFirst&quot;: false, &quot;secondaryLaunchConfigs&quot;: [], &quot;launchConfig&quot;: ${launchConfig} }, &quot;toServiceStrategy&quot;:null}&apos; \\\n"
			+ "                        &apos;https://rancher.sofia4cities.com/v2-beta/projects/1a68/services/1s754/?action=upgrade&apos;\n"
			+ "                    &quot;&quot;&quot;)\n" + "                echo output\n" + "                \n"
			+ "                sleep 30\n" + "                \n"
			+ "	            output = sh (returnStdout: true, script: &quot;&quot;&quot;\n"
			+ "						curl -k -u &apos;${env.CATTLE_ACCESS_KEY}:${env.CATTLE_SECRET_KEY}&apos; \\\n"
			+ "						-X POST \\\n"
			+ "						-H &apos;Accept: application/json&apos; \\\n"
			+ "						-H &apos;Content-Type: application/json&apos; \\\n"
			+ "						-d &apos;{&quot;rollingRestartStrategy&quot;:&quot;&quot;}&apos; \\\n"
			+ "						&apos;https://rancher.sofia4cities.com/v2-beta/projects/1a68/services/1s760/?action=restart&apos;\n"
			+ "                    &quot;&quot;&quot;)\n" + "                echo output                \n"
			+ "	           }\n" + "	       }\n" + "	   }\n" + "	  \n" + "   }\n" + "\n" + "    post {\n"
			+ "        always {\n" + "          echo \"Delete Untagged images\"\n"
			+ "          sh \"docker rmi -f \\$(docker images -f dangling=true -q) || true\"\n" + "          \n"
			+ "          echo \"Delete pushed image\"\n"
			+ "          sh \"docker rmi -f \\$(docker images | grep \\$DOCKER_USERNAMEVALUE/ | awk '{print \\$3}' | uniq) || true\"\n"
			+ "          \n" + "          echo 'Clean up workspace...'\n" + "          deleteDir() \n" + "        }  \n"
			+ "    }\n" + "  \n" + "}</script>\n" + "    <sandbox>true</sandbox>\n" + "  </definition>\n"
			+ "  <triggers/>\n" + "  <disabled>false</disabled>\n" + "</flow-definition>";

	@Test
	@Order(1)
	public void testA_WhenJobIsCreated_ThenJobInfoIsFetched() {
		final JenkinsClient client = jenkinsService.getJenkinsClient(JENKINS_URL, USERNAME, TOKEN);
		assertTrue(client.endPoint().equals(JENKINS_URL));
		jenkinsService.createJob(JENKINS_URL, USERNAME, TOKEN, NEW_JOB_NAME, null, CONFIG_XML);
		final JobInfo job = jenkinsService.getJobInfo(JENKINS_URL, USERNAME, TOKEN, NEW_JOB_NAME, null);
		assertTrue(job.displayName().equalsIgnoreCase(NEW_JOB_NAME));

	}

	@Test
	@Order(2)
	public void testB_WhenJobExists_ThenWeFetchPipelineParametersAreFetched() {
		final Map<String, Object> parameters = jenkinsService.getParametersFromJob(JENKINS_URL, USERNAME, TOKEN,
				NEW_JOB_NAME);
		assertTrue(parameters.containsKey(DOCKER_PARAM));
		assertTrue(parameters.containsKey(GIT_PARAM));
	}

	@Test
	@Order(3)
	public void testC_WhenJobsPipelineIsExecuted_ThenQueueIdAndBuildInfoAreFetched() throws InterruptedException {
		final Map<String, List<String>> params = new HashMap<>();
		params.put(DOCKER_PARAM, Arrays.asList(DOCKER_TAG));
		params.put(GIT_PARAM, Arrays.asList("https://onesait-git.cwbyminsait.com/fjgcornejo/microservices-test.git"));
		final int queueId = jenkinsService.buildWithParameters(JENKINS_URL, USERNAME, TOKEN, NEW_JOB_NAME, null,
				params);
		assertTrue(queueId != 0);
		BuildInfo info = jenkinsService.buildInfo(
				JenkinsConfiguration.builder().username(USERNAME).token(TOKEN).jenkinsUrl(JENKINS_URL).build(),
				NEW_JOB_NAME, null, queueId);
		while (info == null) {
			Thread.sleep(3000);
			info = jenkinsService.buildInfo(
					JenkinsConfiguration.builder().username(USERNAME).token(TOKEN).jenkinsUrl(JENKINS_URL).build(),
					NEW_JOB_NAME, null, queueId);
		}
		assertTrue(info.building());
		info = jenkinsService.lastBuildInfo(
				JenkinsConfiguration.builder().username(USERNAME).token(TOKEN).jenkinsUrl(JENKINS_URL).build(),
				NEW_JOB_NAME, null);
		assertNotNull(info);
	}

	@Test
	@Order(4)
	public void testD_WhenJobExists_ThenJobIsDeleted() {
		jenkinsService.deleteJob(JENKINS_URL, USERNAME, TOKEN, NEW_JOB_NAME, null);
	}

}
