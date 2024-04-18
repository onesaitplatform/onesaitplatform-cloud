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
package com.minsait.onesait.platform.controlpanel.controller.user;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.business.services.user.UserOperationsService;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyUserAccess;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserToken;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.deletion.EntityDeletionService;
import com.minsait.onesait.platform.config.services.deletion.EntityDeletionServiceImpl;
import com.minsait.onesait.platform.config.services.exceptions.UserServiceException;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.libraries.mail.MailService;
import com.minsait.onesait.platform.libraries.mail.util.HtmlFileAttachment;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.model.MasterUser;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/users")
@Slf4j
public class UserController {

	@Autowired
	private MailService mailService;
	@Autowired
	private UserService userService;
	@Autowired
	private AppWebUtils utils;

	@Autowired
	private UserOperationsService operations;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private EntityDeletionService entityDeleteService;

	@Autowired()
	@Qualifier("cachePendingRegistryUsers")
	private Map<String, UserPendingValidation> cachePendingRegistryUsers;

	@Autowired()
	@Qualifier("cacheResetPasswordUsers")
	private Map<String, String> cacheResetPasswordUsers;

	@Autowired()
	@Qualifier("cachePasswordChangedByAdministrator")
	private Map<String, UserPendingShowPassword> cachePasswordChangedByAdministrator;

	@Autowired
	private IntegrationResourcesService resourcesService;

	@Autowired
	private MultitenancyService multitenancyService;

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private HttpSession httpSession;

	@Autowired
	private EntityDeletionServiceImpl deletionService;

	@Value("${onesaitplatform.user.registry.validation.url:http://localhost:18000/controlpanel/users/validateNewUserFromLogin/}")
	private String validationUrlNewUser;

	@Value("${onesaitplatform.user.reset.validation.url:http://localhost:18000/controlpanel/users/validateResetPassword/}")
	private String resetPasswordUrl;

	@Value("${onesaitplatform.user.password.generated.url:http://localhost:18000/controlpanel/users/showGeneratedCredentials/}")
	private String passworGeneratedByAdministratordUrl;

	@Value("${onesaitplatform.multitenancy.enabled}")
	private boolean isMultitenantEnabled;

	private static final String REDIRECT_USER_LIST = "redirect:/users/list";
	private static final String ERROR_403 = "error/403";
	private static final String REDIRECT_USER_CREATE = "redirect:/users/create";
	private static final String TRUE_STR = "/true";
	private static final String USERS_UPDATE_STR = "/users/update/";
	private static final String USER_REMOVE_DATA_ERROR = "user.remove.data.error";
	private static final String REDIRECT_USER_SHOW = "redirect:/users/show/";
	private static final String OBSOLETE_STR = "obsolete";
	private static final String REDIRECT_LOGIN = "redirect:/login";
	private static final String DOES_NOT_EXIST = "\" does not exist";
	private static final String USER_STR = "User \"";
	private static final String USERS_CONSTANT = "users";
	private static final String PASS_CONSTANT = "passwordPattern";
	private static final String VALIDATION_ID_CONSTANT = "validationId";
	private static final String ACTION_CONSTANT = "action";
	private static final String CREDENTIALS_CONSTANT = "credentials";
	private static final String MESSAGE_CONSTANT = "message";
	private static final String PASSWORD_PATTERN = "password-pattern";
	private static final String APP_ID = "appId";
	private static final String USER_EMAIL_IN_USE_ERROR = "user.email.use.error";

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/create", produces = "text/html")
	public String createForm(Model model) {

		model.addAttribute("user", new User());
		model.addAttribute("passwordPattern", getPasswordPattern());
		model.addAttribute("roleTypes", userService.getAllRoles());
		model.addAttribute("tenants", multitenancyService.getTenantsForCurrentVertical());
		return "users/create";

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id) {
		if (!utils.getUserId().equals(id)) {
			utils.deactivateSessions(id);
			userService.deleteUser(id);
		}
		return REDIRECT_USER_LIST;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@DeleteMapping("/hardDelete/{id}")
	public ResponseEntity<String> hardDelete(Model model, @PathVariable("id") String id) {
		log.info("Hard deleting user \"{}\"", id);
		try {
			utils.deactivateSessions(id);
			operations.deleteAuditOntology(id);
			userService.hardDeleteUser(id);
		} catch (final Exception e) {
			log.error("Error deleting user \"{}\"", id);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping(value = "/update/{id}/{bool}")
	public String updateForm(@PathVariable("id") String id, @PathVariable(name = "bool", required = false) boolean bool,
			Model model) {
		// If non admin user tries to update any other user-->forbidden
		if (!utils.getUserId().equals(id) && !utils.isAdministrator()) {
			return ERROR_403;
		}
		populateFormData(model, id);
		model.addAttribute("AccessToUpdate", bool);
		model.addAttribute("tenants", multitenancyService.getTenantsForCurrentVertical());
		multitenancyService.findUser(id).ifPresent(u -> model.addAttribute("tenant", u.getTenant().getName()));

		final User user = userService.getUser(id);
		// If user does not exist redirect to create
		if (user == null) {
			return REDIRECT_USER_CREATE;
		} else {
			user.setPassword(null);
			model.addAttribute("user", user);
			model.addAttribute("passwordPattern", getPasswordPattern());
		}
		return "users/create";
	}

	@PostMapping(value = "/deleteSimpleData", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody Map<String, String> deleteSimpleData(RedirectAttributes redirect,
			@RequestBody OntologyRemoveRevokeDto data) {

		try {

			if (data.getOntologies() == null) {
				return Collections.singletonMap("url", USERS_UPDATE_STR + data.getUserId() + TRUE_STR);
			}
			for (final String ontToDelete : data.getOntologies()) {
				if (log.isDebugEnabled()) {
					log.debug("remove: {} \n", ontToDelete);
				}
				entityDeleteService.deleteOntology(ontToDelete, data.getUserId(), false);
			}

			return Collections.singletonMap("url", USERS_UPDATE_STR + data.getUserId() + TRUE_STR);

		} catch (final UserServiceException e) {
			log.debug("Cannot update  data user");
			utils.addRedirectMessage(USER_REMOVE_DATA_ERROR, redirect);
			return Collections.singletonMap("url", "/users/show/" + data.getUserId());
		}
	}

	@PostMapping(value = "/revokeSimpleData", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody Map<String, String> revokeSimpleData(RedirectAttributes redirect,
			@RequestBody OntologyRemoveRevokeDto data) {

		try {

			Ontology ont;

			if (data.getOntologies() == null) {
				return Collections.singletonMap("url", USERS_UPDATE_STR + data.getUserId() + TRUE_STR);
			}
			for (final String ontToRevoke : data.getOntologies()) {
				if (log.isDebugEnabled()) {
					log.debug("revoke: {} \n", ontToRevoke);
				}

				ont = ontologyService.getOntologyById(ontToRevoke, data.getUserId());
				for (final OntologyUserAccess access : ont.getOntologyUserAccesses()) {
					ontologyService.deleteOntologyUserAccess(access.getId(), data.getUserId());
				}
			}

			return Collections.singletonMap("url", USERS_UPDATE_STR + data.getUserId() + TRUE_STR);

		} catch (final UserServiceException e) {
			log.debug("Cannot update  data user");
			utils.addRedirectMessage(USER_REMOVE_DATA_ERROR, redirect);
			return Collections.singletonMap("url", "/users/show/" + data.getUserId());

		}

	}

	@PutMapping(value = "/update/{id}/{bool}")
	public String update(@PathVariable("id") String id, @Valid User user,
			@PathVariable(name = "bool", required = false) boolean bool, BindingResult bindingResult,
			RedirectAttributes redirect, HttpServletRequest request, Model model,
			@RequestParam(value = "tenant", required = false) String tenant) {

		if (!user.getUserId().equals(id) || !utils.getUserId().equals(id) && !utils.isAdministrator()) {
			return ERROR_403;
		}

		final String Pass = request.getParameter("passwordbox");
		final String newPass = request.getParameter("newpasswordbox");
		final String repeatPass = request.getParameter("repeatpasswordbox");
	


		if (bindingResult.hasErrors()) {
			log.error("Some user properties missing: ");
			bindingResult.getAllErrors().forEach(error -> {
				log.error(error.getDefaultMessage());
			});

			return "redirect:/users/update/" + user.getUserId() + "/" + bool;
		}

		model.addAttribute("AccessToUpdate", bool);

		// If the user is not admin, the RoleType is not in the request by default
		if (!utils.isAdministrator()) {
			user.setRole(userService.getUserRole(utils.getRole()));
		}

		try {
			if (userService.canUserUpdateMail(user.getUserId(), user.getEmail())) {
				if (!newPass.isEmpty() && !repeatPass.isEmpty()) {
					if (newPass.equals(repeatPass) && utils.paswordValidation(newPass)) {
						user.setPassword(newPass);
						final Configuration configuration = configurationService
								.getConfiguration(Configuration.Type.EXPIRATIONUSERS, "default", null);
						final Map<String, Object> ymlExpirationUsersPassConfig = (Map<String, Object>) configurationService
								.fromYaml(configuration.getYmlConfig()).get("Authentication");
						final int numberLastEntriesToCheck = (Integer) ymlExpirationUsersPassConfig
								.get("numberLastEntriesToCheck");
						 	
						
						if (!utils.isAdministrator()) {
							if(!multitenancyService.checkCurrentPasword(user.getUserId(), Pass)) {
								throw new UserServiceException(
										"The current password is not correct, please check the current password or contact the administrator");
							}
						}
						
						if (!multitenancyService.isValidPass(user.getUserId(), newPass, numberLastEntriesToCheck)) {
							throw new UserServiceException(
									"Password not valid because it has already been used before");
						
						}
						userService.updatePassword(user);
						userService.updateUser(user);
						if (utils.isAdministrator()) {
							this.sendShowCredentialsMail(user.getEmail(), newPass, false);

							if (!utils.getUserId().equals(user.getUserId())) {
								utils.addRedirectInfoMessage("user.update.password.admin", redirect);
								return REDIRECT_USER_SHOW + user.getUserId() + "/";
							}
						}
					} else {
						utils.addRedirectMessage("user.update.error.password", redirect);
						return REDIRECT_USER_SHOW + user.getUserId() + "/";
					}
				}
			} else {
				log.error("Cannot update user, email in use", "");
				utils.addRedirectMessage(USER_EMAIL_IN_USE_ERROR, redirect);
				return "redirect:/users/update/".concat(id).concat("/").concat(String.valueOf(bool));
			}

			if (utils.isAdministrator() && isMultitenantEnabled && !StringUtils.isEmpty(tenant)) {
				multitenancyService.changeUserTenant(user.getUserId(), tenant);
			}
			if (utils.isAdministrator()) {
				userService.updateUser(user);
			}

		} catch (final Exception e) {
			log.error("Cannot update user", e);
			utils.addRedirectException(e, redirect);
			return "redirect:/users/update/".concat(id).concat("/").concat(String.valueOf(bool));
		}
		// utils.addRedirectMessage("user.update.success", redirect);
		return REDIRECT_USER_SHOW + user.getUserId() + "/";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = "/create")
	public String create(@Valid User user, BindingResult bindingResult, RedirectAttributes redirect,
			HttpServletRequest request, @RequestParam(value = "tenant", required = false) String tenant) {
		if (bindingResult.hasErrors()) {

			final StringBuilder builder = new StringBuilder();
			bindingResult.getAllErrors().forEach(e -> {

				builder.append(utils.getMessage(e.getDefaultMessage(), "Validation error"));
				builder.append(String.format("%n", ""));
			});
			redirect.addFlashAttribute("message", builder.toString());
			return REDIRECT_USER_CREATE;
		}
		try {
			if (!StringUtils.isEmpty(tenant)) {
				MultitenancyContextHolder.setTenantName(tenant);
			}
			final String newPass = request.getParameter("newpasswordbox");
			final String repeatPass = request.getParameter("repeatpasswordbox");
			if (!userService.emailExists(user)) {
				if (!newPass.isEmpty() && !repeatPass.isEmpty() && newPass.equals(repeatPass)
						&& utils.paswordValidation(newPass)) {
					user.setPassword(newPass);
					userService.createUser(user);
					operations.createPostOperationsUser(user);
					operations.createPostOntologyUser(user);
					return REDIRECT_USER_LIST;
				}

				log.debug("Password is not valid");
				utils.addRedirectMessage("user.create.error.password", redirect);
				return REDIRECT_USER_CREATE;
			}

			log.debug("Email is not valid");
			utils.addRedirectMessage("user.create.error.email", redirect);
			return REDIRECT_USER_CREATE;

		} catch (final UserServiceException e) {
			log.debug("Cannot update user that does not exist");
			utils.addRedirectMessage("user.create.error", redirect);
			return REDIRECT_USER_CREATE;
		}

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/list", produces = "text/html")
	public String list(Model model, @RequestParam(required = false) String userId,
			@RequestParam(required = false) String fullName, @RequestParam(required = false) String roleType,
			@RequestParam(required = false) String email, @RequestParam(required = false) Boolean active) {
		// CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);

		Boolean filtered = false;
		
		if (userId == null || userId.equals("")) {
			userId = "%%";
		} else if (userId != "") {
			userId = "%" + userId + "%";
			filtered = true;
		}
		
		if (fullName == null || fullName.equals("")) {
			fullName = "%%";
		} else if (fullName != "") {
			fullName = "%" + fullName + "%";
			filtered = true;
		}
		
		if (email == null || email.equals("")) {
			email = "%%";
		} else if (email != "") {
			email = "%" + email + "%";
			filtered = true;
		}
		
		if (roleType == null || roleType.equals("")) {
			roleType = "%%";
		} else if (roleType != "") {
			filtered = true;
		}

		model.addAttribute("roleTypes", userService.getAllRoles());

		if (!filtered && active == null) {
			log.debug("No params for filtering, loading all users");
			if (userService.countUsers() < 200L) {
				model.addAttribute("users", userService.getAllUsersList());
			}

		} else {
			log.debug("Params detected, filtering users...");
			model.addAttribute("users",
					userService.getAllUsersByCriteriaList(userId, fullName, email, roleType, active));
		}


		return "users/list";

	}

	@GetMapping(value = "/show/{id}", produces = "text/html")
	public String showUser(@PathVariable("id") String id, Model model) {
		User user = null;
		if (id != null) {
			// If non admin user tries to update any other user-->forbidden
			if (!utils.getUserId().equals(id) && !utils.isAdministrator()) {
				return ERROR_403;
			}
			user = userService.getUser(id);
		}
		// If user does not exist
		if (user == null) {
			return "error/404";
		}

		model.addAttribute("user", user);
		UserToken userToken = null;
		try {
			userToken = userService.getUserToken(user).get(0);
		} catch (final Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("No token found for user: {}", user);
			}
		}

		model.addAttribute("userToken", userToken);
		model.addAttribute("itemId", user.getUserId());
		multitenancyService.findUser(id).ifPresent(u -> model.addAttribute("tenant", u.getTenant().getName()));

		final Date today = new Date();
		if (user.getDateDeleted() != null) {
			if (user.getDateDeleted().before(today)) {
				model.addAttribute(OBSOLETE_STR, true);
			} else {
				model.addAttribute(OBSOLETE_STR, false);
			}
		} else {
			model.addAttribute(OBSOLETE_STR, false);
		}

		return "users/show";

	}

	private void populateFormData(Model model, String userId) {
		model.addAttribute("roleTypes", userService.getAllRoles());
		model.addAttribute("ontologies", ontologyService.getOntologiesByOwner(userId));
		model.addAttribute("ontologies1", ontologyService.getOntologiesByOwner(userId));
		model.addAttribute("ontologies2", ontologyService.getOntologiesByOwner(userId));

	}

	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public String registerUserLogin(@ModelAttribute User user, RedirectAttributes redirectAttributes,
			HttpServletRequest request) {

		log.info("Received request to register new user: {}", user.getUserId());

		final String nameRole = request.getParameter("roleName");

		if (user != null) {
			if (userService.emailExists(user)) {
				log.debug("There is already an user with this email");
				utils.addRedirectMessage("login.error.email.duplicate", redirectAttributes);
				return REDIRECT_LOGIN;
			}
			if (userService.userExists(user)) {
				log.debug("There is already an user with this identifier");
				utils.addRedirectMessage("login.error.username", redirectAttributes);
				return REDIRECT_LOGIN;
			}

			if (!userService.emailExists(user)) {

				try {
					if (nameRole == null) {
						log.debug("A role must be selected");
						utils.addRedirectMessage("login.error.user.register", redirectAttributes);
						return REDIRECT_LOGIN;
					}

					boolean inFlight = false;
					for (final UserPendingValidation cached : cachePendingRegistryUsers.values()) {
						if (cached.getUser().getEmail().equals(user.getEmail())) {
							inFlight = true;
						}
					}

					if (!inFlight) {

						final String temporalUuid = UUID.randomUUID().toString();
						final UserPendingValidation userPendingValidation = new UserPendingValidation();
						userPendingValidation.setRoleName(nameRole);
						userPendingValidation.setUser(user);

						final String defaultTitle = "[Onesait Plaform] New Account";
						final String defaultMessage = "To complete your registry in Onesait Plaform, click in the link|Register User|In case of not being redirected, please copy and paste this url in your browser|If after 10 minutes you don't activate your user, it will be deleted";

						final String emailTitle = utils.getMessage("user.create.mail.title", defaultTitle);
						final String emailMessage = utils.getMessage("user.create.mail.body", defaultMessage);

						final String[] emailParts = emailMessage.split("\\|");

						final String validationUrl = validationUrlNewUser.concat(temporalUuid);

						final String htmlText = "<html><body>"
								+ "<div><img src='cid:onesaitplatformimg' style='height:230px;' /></div>" + "<div>"
								+ emailParts[0] + "</div>" + "<br/>" + "<div>" + "<a href='" + validationUrl + "'>"
								+ emailParts[1] + "</a></div>" + "<br/>" + "<div>" + emailParts[2] + ":</div>"
								+ "<div><strong>" + validationUrl + "</strong></div>" + "<br/>" + "<div>"
								+ emailParts[3] + "</div>" + "</body></html>";

						InputStream imgOnesaitPlatformIS = new ClassPathResource("static/img/onesaitplatform.jpeg").getInputStream();
						File imgOnesaitPlatform = File.createTempFile("onesaitplatform", ".jpeg");
						FileUtils.copyInputStreamToFile(imgOnesaitPlatformIS, imgOnesaitPlatform);

						final HtmlFileAttachment demoImg = new HtmlFileAttachment();
						demoImg.setFile(imgOnesaitPlatform);
						demoImg.setFileKey("onesaitplatformimg");

						log.info("Send email to: {} in order to register new user", user.getEmail());

						mailService.sendConfirmationMailMessage(user.getEmail(), emailTitle, htmlText, demoImg);

						cachePendingRegistryUsers.put(temporalUuid, userPendingValidation);

						utils.addRedirectMessage("user.create.mail.sended", redirectAttributes);
						return REDIRECT_LOGIN;

					} else {// There is a previous request in flight
						if (log.isDebugEnabled()) {
							log.debug("There is a previous request to create a user using email: {}", user.getEmail());
						}
						utils.addRedirectMessage("user.create.mail.inflight", redirectAttributes);
						return REDIRECT_LOGIN;
					}

				} catch (final UserServiceException e) {
					log.error("This user already exist", e);
					utils.addRedirectMessage("login.error.register", redirectAttributes);
					return REDIRECT_LOGIN;
				} catch (final MailSendException e) {
					log.error("Error sending mail to finish user creation", e);
					utils.addRedirectMessage("login.error.email.fail", redirectAttributes);
					return REDIRECT_LOGIN;
				} catch (final Exception e) {
					log.error("Error creating user", e);
					utils.addRedirectMessage("user.error.mailservice", redirectAttributes);
					return REDIRECT_LOGIN;
				}
			}
		}
		return "redirect:/login?errorRegister";

	}

	@GetMapping(value = "/validateNewUserFromLogin/{uuid}")
	public String validateUser(@PathVariable(name = "uuid") String uuid, HttpServletRequest request,
			HttpServletResponse response, Model model, RedirectAttributes redirectAttributes) {
		log.info("Received request to validate new user");

		final UserPendingValidation userPendingValidation = cachePendingRegistryUsers.get(uuid);

		if (null == userPendingValidation) {// Expired
			log.debug("Link to reset password is expired");
			utils.addRedirectMessage("user.create.expired", redirectAttributes);
			return REDIRECT_LOGIN;
		}

		model.addAttribute(USERS_CONSTANT, new User());
		model.addAttribute(PASS_CONSTANT, getPasswordPattern());
		model.addAttribute(VALIDATION_ID_CONSTANT, uuid);
		model.addAttribute(ACTION_CONSTANT, "createNewUserFromLogin");

		return "users/setPassword";

	}

	@PostMapping(value = "/createNewUserFromLogin")
	public String createNewUserFromLogin(@RequestParam(required = false, name = "validationId") String uuid,
			@ModelAttribute User user, Model model, RedirectAttributes redirectAttributes) {
		log.info("Received request to validate new user");

		final UserPendingValidation userPendingValidation = cachePendingRegistryUsers.get(uuid);
		try {
			if (null == userPendingValidation) {// Expired
				log.debug("Link to reset password is expired");
				utils.addRedirectMessage("user.create.expired", redirectAttributes);
				return REDIRECT_LOGIN;
			}

			final String nameRole = userPendingValidation.getRoleName();
			final User firstFormUser = userPendingValidation.getUser();

			if (user.getPassword() == null || user.getPassword().trim().equals("")
					|| !utils.paswordValidation(user.getPassword())) {
				log.info("Password not valid because does not apply the pattern");
				utils.addRedirectMessage("login.pattern.password", redirectAttributes);

				model.addAttribute(USERS_CONSTANT, new User());
				model.addAttribute(PASS_CONSTANT, getPasswordPattern());
				model.addAttribute(VALIDATION_ID_CONSTANT, uuid);
				model.addAttribute(ACTION_CONSTANT, "resetPasswordFromLogin");
				return "redirect:/users/validateResetPassword/" + uuid;

			}

			firstFormUser.setPassword(user.getPassword());

			if (nameRole.equalsIgnoreCase("user")) {
				userService.registerRoleUser(firstFormUser);
				operations.createPostOperationsUser(firstFormUser);
				operations.createPostOntologyUser(firstFormUser);

			} else {
				userService.registerRoleDeveloper(firstFormUser);
				operations.createPostOperationsUser(firstFormUser);
				operations.createPostOntologyUser(firstFormUser);

			}

			log.info("User created from login");

			utils.addRedirectMessage("user.create.success", redirectAttributes);
		} finally {
			cachePendingRegistryUsers.remove(uuid);
		}
		return REDIRECT_LOGIN;
	}

	@GetMapping(value = "/deactivateUser/{userId}")
	public String deactivateUser(Model model, RedirectAttributes redirect,
			@PathVariable(name = "userId") String userId) {

		try {
			if (!utils.isAdministrator() && utils.getUserId().equals(userId)
					|| utils.isAdministrator() && !utils.getUserId().equals(userId)) {

				utils.deactivateSessions(userId);
				userService.deleteUser(userId);

			}

			if (utils.isAdministrator()) {
				return REDIRECT_USER_LIST;
			} else {
				return "redirect:/logi";
			}

		} catch (final UserServiceException e) {
			log.error("Cannot deactivatea user", e);
			utils.addRedirectMessage("Cannot deactivatea user", redirect);
			return REDIRECT_USER_SHOW + utils.getUserId() + "/";
		}

	}

	@GetMapping(value = "/forgetDataUser/{userId}")
	public String forgetDataUser(Model model, RedirectAttributes redirect,
			@PathVariable(name = "userId") String userId) {

		try {

			deletionService.hardDeleteUser(userId);

			if (utils.isAdministrator()) {
				return REDIRECT_USER_LIST;
			} else {
				return "redirect:/login";
			}

		} catch (final Exception e) {

			log.error("Cannot deleted  data user", e);
			utils.addRedirectMessage(USER_REMOVE_DATA_ERROR, redirect);
			return REDIRECT_USER_SHOW + utils.getUserId() + "/";
		}

	}

	@PostMapping(value = "/reset-password")
	public String resetPassword(Model model, @RequestParam("resetEmail") String email,
			RedirectAttributes redirectAttributes, HttpServletRequest request) {

		log.info("Received request to reset password for email: {}", email);
		final User user = userService.getUserByEmail(email);

		if (user == null) {
			log.debug("Mail invalid");
			utils.addRedirectMessage("user.error.mail", redirectAttributes);
		} else {

			boolean inFlight = false;
			for (final String cachedUserIdentifier : cacheResetPasswordUsers.values()) {
				final MasterUser currentUser = multitenancyService.getUser(cachedUserIdentifier);

				if (currentUser != null && currentUser.getEmail().equals(user.getEmail())) {
					inFlight = true;
				}
			}

			if (!inFlight) {

				final String resetIdentifier = UUID.randomUUID().toString();

				final String defaultTitle = "[Onesait Plaform] Reset Password";

				final String defaultMessage = "To reset your password in Onesait Plaform, click in the link|Reset Password|In case of not being redirected, please copy and paste this url in your browser|If of you have not requested this operation, please ignore this message. This link will be available 10 minutes.";

				final String emailTitle = utils.getMessage("user.reset.mail.title", defaultTitle);
				final String emailMessage = utils.getMessage("user.reset.mail.body", defaultMessage);

				final String[] emailParts = emailMessage.split("\\|");

				final String validationResetPasswordUrl = resetPasswordUrl.concat(resetIdentifier);

				final String htmlText = "<html><body>"
						+ "<div><img src='cid:onesaitplatformimg' style='height:230px;' /></div>" + "<div>"
						+ emailParts[0] + "</div>" + "<br/>" + "<div>" + "<a href='" + validationResetPasswordUrl + "'>"
						+ emailParts[1] + "</a></div>" + "<br/>" + "<div>" + emailParts[2] + ":</div>" + "<div><strong>"
						+ validationResetPasswordUrl + "</strong></div>" + "<br/>" + "<div>" + emailParts[3] + "</div>"
						+ "</body></html>";

				final HtmlFileAttachment demoImg = new HtmlFileAttachment();

				try {
					InputStream imgOnesaitPlatformIS = new ClassPathResource("static/img/onesaitplatform.jpeg").getInputStream();
					File imgOnesaitPlatform = File.createTempFile("onesaitplatform", ".jpeg");
					FileUtils.copyInputStreamToFile(imgOnesaitPlatformIS, imgOnesaitPlatform);

					demoImg.setFile(imgOnesaitPlatform);
					demoImg.setFileKey("onesaitplatformimg");

				} catch (final IOException e) {
					log.warn("Image could not be attached to mail", e);
				}

				log.info("Send email to: {} in order to register new user", user.getEmail());

				try {
					mailService.sendConfirmationMailMessage(user.getEmail(), emailTitle, htmlText, demoImg);
					cacheResetPasswordUsers.put(resetIdentifier, user.getUserId());
					utils.addRedirectMessage("user.reset.mail.sended", redirectAttributes);

				} catch (final Exception e) {
					log.error("Error sending message", e);
					utils.addRedirectMessage("login.error.email.fail", redirectAttributes);
				}
			} else {
				utils.addRedirectMessage("user.reset.mail.inflight", redirectAttributes);
			}

		}

		return REDIRECT_LOGIN;
	}

	@GetMapping(value = "/validateResetPassword/{uuid}")
	public String validateResetPassword(@PathVariable(name = "uuid") String uuid, HttpServletRequest request,
			HttpServletResponse response, Model model, RedirectAttributes redirectAttributes) {
		log.info("Received request to validate new user");

		final String userPendingReset = cacheResetPasswordUsers.get(uuid);

		if (null == userPendingReset) {// Expired
			log.debug("Link to reset password is expired");
			utils.addRedirectMessage("user.create.expired", redirectAttributes);
			return REDIRECT_LOGIN;
		}
		model.addAttribute(USERS_CONSTANT, new User());
		model.addAttribute(PASS_CONSTANT, getPasswordPattern());
		model.addAttribute(VALIDATION_ID_CONSTANT, uuid);
		model.addAttribute(ACTION_CONSTANT, "resetPasswordFromLogin");

		return "users/setPassword";

	}

	@PostMapping(value = "/resetPasswordFromLogin")
	public String resetPasswordFromLogin(@RequestParam(required = false, name = "validationId") String uuid,
			@ModelAttribute User user, Model model, RedirectAttributes redirectAttributes) {

		log.info("Received request to reset password for validationId: {}", uuid);

		final String userPendingResetPassword = cacheResetPasswordUsers.get(uuid);

		if (null == userPendingResetPassword) {// Expired
			log.debug("Link to reset password is expired");
			utils.addRedirectMessage("user.create.expired", redirectAttributes);
			return REDIRECT_LOGIN;
		}

		final MasterUser userToResetPassword = multitenancyService.getUser(userPendingResetPassword);

		if (userToResetPassword == null) {
			log.debug("Mail invalid");
			utils.addRedirectMessage("user.error.mail", redirectAttributes);
		} else {
			final Configuration configuration = configurationService
					.getConfiguration(Configuration.Type.EXPIRATIONUSERS, "default", null);
			@SuppressWarnings("unchecked")
			final Map<String, Object> ymlExpirationUsersPassConfig = (Map<String, Object>) configurationService
					.fromYaml(configuration.getYmlConfig()).get("Authentication");
			final int numberLastEntriesToCheck = (Integer) ymlExpirationUsersPassConfig.get("numberLastEntriesToCheck");

			if (user.getPassword() == null || user.getPassword().trim().equals("")
					|| !utils.paswordValidation(user.getPassword())) {
				log.info("Password not valid because does not apply the pattern");
				utils.addRedirectMessage("login.pattern.password", redirectAttributes);

				model.addAttribute(USERS_CONSTANT, new User());
				model.addAttribute(PASS_CONSTANT, getPasswordPattern());
				model.addAttribute(VALIDATION_ID_CONSTANT, uuid);
				model.addAttribute(ACTION_CONSTANT, "resetPasswordFromLogin");
				return "redirect:/users/validateResetPassword/" + uuid;

			} else if (!multitenancyService.isValidPass(userToResetPassword.getUserId(), user.getPassword(),
					numberLastEntriesToCheck)) {
				log.info("Password not valid because it has already been used before");
				utils.addRedirectMessage("user.update.error.reuse.password", redirectAttributes);

				model.addAttribute(USERS_CONSTANT, new User());
				model.addAttribute(PASS_CONSTANT, getPasswordPattern());
				model.addAttribute(VALIDATION_ID_CONSTANT, uuid);
				model.addAttribute(ACTION_CONSTANT, "resetPasswordFromLogin");
				return "redirect:/users/validateResetPassword/" + uuid;

			} else {
				multitenancyService.updateMasterUserPassword(userPendingResetPassword, user.getPassword());
				utils.addRedirectMessage("user.reset.success", redirectAttributes);
				log.debug("Pass reset");

				cacheResetPasswordUsers.remove(uuid);
			}
		}

		return REDIRECT_LOGIN;

	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/reset-password/{userId}")
	public ResponseEntity<String> getResetPasswordUserId(@PathVariable(name = "userId") String userId) {

		log.info("Received request to reset password for userId: {}", userId);
		final User user = userService.getUser(userId);
		if (user == null) {
			log.debug("Mail invalid");
			return new ResponseEntity<>(USER_STR + userId + DOES_NOT_EXIST, HttpStatus.NOT_FOUND);
		} else {
			final String upperCase = String.valueOf((char) (new Random().nextInt(26) + 'a')).toUpperCase();
			final String newPassword = upperCase + UUID.randomUUID().toString().substring(0, 10) + "$";
			user.setPassword(newPassword);
			userService.updatePassword(user);
			multitenancyService.setResetPass(user.getUserId());
			log.info("Send new password to user by email {}", user.getEmail());

			try {
				this.sendShowCredentialsMail(user.getEmail(), newPassword, true);
			} catch (final Exception e) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}

			return new ResponseEntity<>(HttpStatus.OK);
		}

	}

	private void sendShowCredentialsMail(String email, String newPassword, boolean expiresPassword) throws Exception {

		final String showIdentifier = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString() + "-"
				+ UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString() + "-"
				+ UUID.randomUUID().toString();

		final String defaultTitle = "[Onesait Plaform] Reset Password";
		final String defaultMessage = "An administrator of Onesait Plaform modified your password, click in the link to get your new password|Get Password|In case of not being redirected, please copy and paste this url in your browser|This link will be available 2 hours";

		final String emailTitle = utils.getMessage("user.reset.mail.title", defaultTitle);
		final String emailMessage = utils.getMessage("user.reset.administrator.mail.body", defaultMessage);

		final String[] emailParts = emailMessage.split("\\|");

		final String validationResetPasswordUrl = passworGeneratedByAdministratordUrl.concat(showIdentifier);

		final String htmlText = "<html><body>" + "<div><img src='cid:onesaitplatformimg' style='height:230px;' /></div>"
				+ "<div>" + emailParts[0] + "</div>" + "<br/>" + "<div>" + "<a href='" + validationResetPasswordUrl
				+ "'>" + emailParts[1] + "</a></div>" + "<br/>" + "<div>" + emailParts[2] + ":</div>" + "<div><strong>"
				+ validationResetPasswordUrl + "</strong></div>" + "<br/>" + "<div>" + emailParts[3] + "</div>"
				+ "</body></html>";

		final HtmlFileAttachment demoImg = new HtmlFileAttachment();

		try {
			InputStream imgOnesaitPlatformIS = new ClassPathResource("static/img/onesaitplatform.jpeg").getInputStream();
			File imgOnesaitPlatform = File.createTempFile("onesaitplatform", ".jpeg");
			FileUtils.copyInputStreamToFile(imgOnesaitPlatformIS, imgOnesaitPlatform);

			demoImg.setFile(imgOnesaitPlatform);
			demoImg.setFileKey("onesaitplatformimg");

		} catch (final IOException e) {
			log.warn("Image could not be attached to mail", e);
		}

		try {
			mailService.sendConfirmationMailMessage(email, emailTitle, htmlText, demoImg);

			final UserPendingShowPassword userPendingShowPassword = new UserPendingShowPassword();
			userPendingShowPassword.setCredentials(newPassword);
			userPendingShowPassword.setCredentialsExpires(expiresPassword);

			this.cachePasswordChangedByAdministrator.put(showIdentifier, userPendingShowPassword);

		} catch (final Exception e) {
			log.error("Error sending message", e);
			throw e;
		}

		log.debug("Pass reset");
	}

	@GetMapping(value = "/showGeneratedCredentials/{uuid}")
	public String showGeneratedCredentials(@PathVariable(name = "uuid") String uuid, HttpServletRequest request,
			HttpServletResponse response, Model model, RedirectAttributes redirectAttributes) {
		log.info("Received show password");

		final UserPendingShowPassword userWithPasswordToshow = this.cachePasswordChangedByAdministrator.get(uuid);

		if (null == userWithPasswordToshow) {// Expired
			log.debug("Link to show password is expired");
			utils.addRedirectMessage("user.reset.expired", redirectAttributes);

		} else {
			if (userWithPasswordToshow.isCredentialsExpires()) {
				final Configuration configuration = configurationService
						.getConfiguration(Configuration.Type.EXPIRATIONUSERS, "default", null);

				@SuppressWarnings("unchecked")
				final Map<String, Object> ymlExpirationUsersPassConfig = (Map<String, Object>) configurationService
						.fromYaml(configuration.getYmlConfig()).get("ResetUserPass");
				final int hours = ((Integer) ymlExpirationUsersPassConfig.get("hours")).intValue();

				final StringBuilder message = new StringBuilder();
				message.append(utils.getMessage("user.new.pass.for.user.time.for.update.1", " You have "));
				message.append(" ");
				message.append(hours);
				message.append(" ");
				message.append(utils.getMessage("user.new.pass.for.user.time.for.update.2",
						" hours to change this password or your user account will be blocked and you must contact the administrator. "));

				model.addAttribute(MESSAGE_CONSTANT, message);
			}
			model.addAttribute(CREDENTIALS_CONSTANT, userWithPasswordToshow.getCredentials());
		}

		return "users/showGeneratedCredentials";

	}

	private String getPasswordPattern() {
		return ((String) resourcesService.getGlobalConfiguration().getEnv().getControlpanel().get(PASSWORD_PATTERN));
	}

}
