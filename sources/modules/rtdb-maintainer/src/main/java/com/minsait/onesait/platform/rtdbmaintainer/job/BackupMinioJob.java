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
package com.minsait.onesait.platform.rtdbmaintainer.job;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ibm.icu.text.SimpleDateFormat;
import com.minsait.onesait.platform.commons.ActiveProfileDetector;
import com.minsait.onesait.platform.config.services.objectstorage.ObjectStorageService;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreBucketCreateException;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreCreatePolicyException;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreCreateUserException;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreLoginException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.usertoken.UserTokenService;
import com.minsait.onesait.platform.config.services.utils.ZipUtil;
import com.minsait.onesait.platform.rtdbmaintainer.audit.aop.RtdbMaintainerAuditable;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BackupMinioJob {

	@Autowired
	private ZipUtil zipUtil;

	@Autowired
	private ObjectStorageService objectStorageService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserTokenService userTokenService;

	@Autowired
	private ActiveProfileDetector profileDetector;

	@Value("${onesaitplatform.database.configdb.username:username}")
	private String username;

	@Value("${onesaitplatform.database.configdb.password:password}")
	private String password;

	@Value("${onesaitplatform.database.configdb.export.tmp.path}")
	private String exportPath;

	@Value("${onesaitplatform.database.configdb.export.default.path}")
	private String defaultPath;

	private static final String EXPORT_CONFIG = "mysqldump -h configdb -P 3306 -u %s -p%s --ignore-table=onesaitplatform_config.market_asset onesaitplatform_config > %s/onesaitplatform_config.sql";
	private static final String EXPORT_MASTER_CONFIG = "mysqldump -h configdb -P 3306 -u %s -p%s onesaitplatform_master_config > %s/onesaitplatform_master_config.sql";
	private static final String EXPORT_SCHEDULER = "mysqldump -h configdb -P 3306 -u %s -p%s onesaitplatform_scheduler> %s/onesaitplatform_scheduler.sql";
	private static final String EXPORT_CONFIG_DEFAULT = "%s -u %s -p%s --ignore-table=onesaitplatform_config.market_asset onesaitplatform_config > %sonesaitplatform_config.sql";
	private static final String EXPORT_MASTER_CONFIG_DEFAULT = "%s -u %s -p%s onesaitplatform_master_config > %sonesaitplatform_master_config.sql";
	private static final String EXPORT_SCHEDULER_DEFAULT = "%s -u %s -p%s onesaitplatform_scheduler > %sonesaitplatform_scheduler.sql";
	private static final String SYSADMIN_USER = "sysadmin";

	@RtdbMaintainerAuditable
	public void execute(JobExecutionContext context) throws IOException {
		String date = new SimpleDateFormat("YYMMDD HHMMSS").format(new Date()).replace(" ", "_");
		File file = new File(exportPath);
		log.debug("Export directory, absolute path: {}", file.getAbsolutePath());
		if (file.exists() && file.isDirectory()) {
			File exportFile = new File(file.getAbsolutePath(), date + ".zip");
			FileInputStream input = null;
			try {
				String c1, c2, c3 = null;
				if (profileDetector.getActiveProfile().equals("default")) {
					c1 = String.format(EXPORT_CONFIG_DEFAULT, defaultPath, username, password, exportPath);
					c2 = String.format(EXPORT_MASTER_CONFIG_DEFAULT, defaultPath, username, password, exportPath);
					c3 = String.format(EXPORT_SCHEDULER_DEFAULT, defaultPath, username, password, exportPath);
					Process p1 = Runtime.getRuntime().exec(new String[] { "cmd.exe", "/c", c1 });
					readResponse(p1);
					Process p2 = Runtime.getRuntime().exec(new String[] { "cmd.exe", "/c", c2 });
					readResponse(p2);
					Process p3 = Runtime.getRuntime().exec(new String[] { "cmd.exe", "/c", c3 });
					readResponse(p3);
				} else {
					c1 = String.format(EXPORT_CONFIG, username, password, file.getAbsolutePath());
					c2 = String.format(EXPORT_MASTER_CONFIG, username, password, file.getAbsolutePath());
					c3 = String.format(EXPORT_SCHEDULER, username, password, file.getAbsolutePath());
					Process p1 = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", c1 });
					p1.waitFor();
					readResponse(p1);
					Process p2 = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", c2 });
					p2.waitFor();
					readResponse(p2);
					Process p3 = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", c3 });
					p3.waitFor();
					readResponse(p3);
				}
				try {
					createZip(file, exportFile);
				} catch (final IOException e) {
					log.error("Zip file viewer failed", e);
				}

				String objectStoreAuthToken = null;
				try {
					objectStoreAuthToken = this.objectStorageService.logIntoObjectStorageWithSuperUser();
				} catch (ObjectStoreLoginException e) {
					log.error("Error loing with superuser in MinIO", e);
					throw e;
				}

				String requesterUserToken = userTokenService.getToken(userService.getUser(SYSADMIN_USER)).getToken();

				if (!this.objectStorageService.existUserInObjectStore(objectStoreAuthToken, SYSADMIN_USER)) {
					objectStorageService.createBucketForUser(SYSADMIN_USER);
					objectStorageService.createPolicyForBucketUser(objectStoreAuthToken, SYSADMIN_USER);
					objectStorageService.createUserInObjectStore(objectStoreAuthToken, SYSADMIN_USER,
							requesterUserToken);
				}

				String userToken = this.objectStorageService.logIntoAdministrationObjectStorage(SYSADMIN_USER,
						requesterUserToken, objectStoreAuthToken);
				input = new FileInputStream(exportFile);
				MultipartFile multipartFile = new MockMultipartFile(date, exportFile.getName(), "application/gzip",
						IOUtils.toByteArray(input));

				this.objectStorageService.uploadObject(userToken,
						this.objectStorageService.getUserBucketName(SYSADMIN_USER), "", multipartFile);

			} catch (ObjectStoreCreateUserException | ObjectStoreCreatePolicyException
					| ObjectStoreBucketCreateException | ObjectStoreLoginException | InterruptedException e) {
				log.error("Error creating uploading configdb backup into MinIO.", e);
			} finally {
				if (input != null)
					input.close();
				deleteDirectory(file);
			}
		}
	}

	private void readResponse(Process process) {
		InputStream is = process.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String aux;
		try {
			aux = br.readLine();

			while (aux != null) {
				log.info(aux);
				aux = br.readLine();
			}
		} catch (IOException e) {
			log.error("Error readind response.", e);
		}
	}

	private boolean deleteDirectory(File directoryToBeDeleted) {
		final File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (final File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}

	public void createZip(File exportFiles, File zipFile) throws IOException {

		File[] files = exportFiles.listFiles();
		zipFile.createNewFile();
		FileOutputStream fos = new FileOutputStream(zipFile);
		ZipOutputStream zipOut = new ZipOutputStream(fos);
		for (File fileToZip : files) {
			FileInputStream fis = new FileInputStream(fileToZip);
			ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
			log.debug("Adding file {} to zip file.", fileToZip.getName());
			zipOut.putNextEntry(zipEntry);

			byte[] bytes = new byte[1024];
			int length;
			while ((length = fis.read(bytes)) >= 0) {
				zipOut.write(bytes, 0, length);
			}
			fis.close();
		}
		zipOut.close();
		fos.close();
	}

}
