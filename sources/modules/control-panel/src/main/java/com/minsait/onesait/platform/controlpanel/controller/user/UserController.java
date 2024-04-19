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
package com.minsait.onesait.platform.controlpanel.controller.user;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
import com.minsait.onesait.platform.config.services.exceptions.UserServiceException;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.libraries.mail.MailService;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;

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

	@Autowired
	private MultitenancyService multitenancyService;

	@Autowired
	private ConfigurationService configurationService;

	@Value("${onesaitplatform.user.registry.validation.url:http://localhost:18000/controlpanel/users/validateNewUser/}")
	private String validationUrlNewUser;

	@Value("${onesaitplatform.user.reset.validation.url:http://localhost:18000/controlpanel/users/validateResetPassword/}")
	private String validationUrlResetPassword;

	@Value("${onesaitplatform.password.pattern}")
	private String passwordPattern;

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

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/create", produces = "text/html")
	public String createForm(Model model) {

		model.addAttribute("user", new User());
		model.addAttribute("passwordPattern", passwordPattern);
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
			model.addAttribute("user", user);
			model.addAttribute("passwordPattern", passwordPattern);
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
				log.debug("remove: " + ontToDelete + " \n");
				entityDeleteService.deleteOntology(ontToDelete, data.getUserId());
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
				log.debug("revoke: " + ontToRevoke + " \n");

				ont = ontologyService.getOntologyById(ontToRevoke, data.getUserId());
				for (OntologyUserAccess access : ont.getOntologyUserAccesses()) {
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

		final String newPass = request.getParameter("newpasswordbox");
		final String repeatPass = request.getParameter("repeatpasswordbox");

		if (bindingResult.hasErrors()) {
			log.debug("Some user properties missing");

			return "redirect:/users/update/" + user.getUserId() + "/" + bool;
		}

		model.addAttribute("AccessToUpdate", bool);

		// If the user is not admin, the RoleType is not in the request by default
		if (!utils.isAdministrator()) {
			user.setRole(userService.getUserRole(utils.getRole()));
		}

		try {
			if (!newPass.isEmpty() && !repeatPass.isEmpty()) {
				if (newPass.equals(repeatPass) && utils.paswordValidation(newPass)) {
					user.setPassword(newPass);
					final Configuration configuration = configurationService
							.getConfiguration(Configuration.Type.EXPIRATIONUSERS, "default", null);
					final Map<String, Object> ymlExpirationUsersPassConfig = (Map<String, Object>) configurationService
							.fromYaml(configuration.getYmlConfig()).get("Authentication");
					final int numberLastEntriesToCheck = (Integer) ymlExpirationUsersPassConfig
							.get("numberLastEntriesToCheck");

					if (!multitenancyService.isValidPass(user.getUserId(), newPass, numberLastEntriesToCheck)) {
						throw new UserServiceException("Password not valid because it has already been used before");
					}
					userService.updatePassword(user);
					if (utils.isAdministrator()) {
						final String defaultMessage = "Your password has been changed. Your new password for onesait Platform: ";
						final String emailMessage = utils.getMessage("user.update.password.email", defaultMessage)
								.concat(" " + newPass);
						try {
							mailService.sendMail(user.getEmail(), emailMessage, emailMessage);

						} catch (final Exception e) {
							log.warn("Problem sending mail on update User ", e);
						}

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
			if (utils.isAdministrator() && !StringUtils.isEmpty(tenant)) {
				multitenancyService.changeUserTenant(user.getUserId(), tenant);
			}
			userService.updateUser(user);
		} catch (final RuntimeException e) {
			log.debug("Cannot update user");
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
		if (userId != null && userId.equals("")) {
			userId = null;
		}
		if (fullName != null && fullName.equals("")) {
			fullName = null;
		}
		if (email != null && email.equals("")) {
			email = null;
		}
		if (roleType != null && roleType.equals("")) {
			roleType = null;
		}

        model.addAttribute("roleTypes", userService.getAllRoles());
        
		if (userId == null && email == null && fullName == null && active == null && roleType == null) {
			log.debug("No params for filtering, loading all users");
			if(userService.countUsers() < 200L) {
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
			log.debug("No token found for user: " + user);
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
						final String defaultMessage = "To complete your registry in Onesait Plaform, please follow this link:";

						final String emailTitle = utils.getMessage("user.create.mail.title", defaultTitle);
						String emailMessage = utils.getMessage("user.create.mail.body", defaultMessage);

						emailMessage = emailMessage.concat(" ").concat(validationUrlNewUser).concat(temporalUuid);

						log.info("Send email to: {} in order to register new user", user.getEmail());
						mailService.sendMail(user.getEmail(), emailTitle, emailMessage);

						cachePendingRegistryUsers.put(temporalUuid, userPendingValidation);

						utils.addRedirectMessage("user.create.mail.sended", redirectAttributes);
						return REDIRECT_LOGIN;

					} else {// There is a previous request in flight
						log.debug("There is a previous request to create a user using email: {}", user.getEmail());
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

	@GetMapping(value = "/validateNewUser/{uuid}")
	public String validateUser(@PathVariable(name = "uuid") String uuid, RedirectAttributes redirectAttributes) {
		log.info("Received request to validate new user");

		final UserPendingValidation userPendingValidation = cachePendingRegistryUsers.get(uuid);
		try {
			if (null == userPendingValidation) {// Expired
				log.debug("Link to reset password is expired");
				utils.addRedirectMessage("user.create.expired", redirectAttributes);
				return REDIRECT_LOGIN;
			}

			final String nameRole = userPendingValidation.getRoleName();
			final User user = userPendingValidation.getUser();

			if (nameRole.equalsIgnoreCase("user")) {
				userService.registerRoleUser(user);
				operations.createPostOperationsUser(user);
				operations.createPostOntologyUser(user);

			} else {
				userService.registerRoleDeveloper(user);
				operations.createPostOperationsUser(user);
				operations.createPostOntologyUser(user);

			}

			log.info("User created from login");

			utils.addRedirectMessage("user.create.success", redirectAttributes);
		} finally {
			cachePendingRegistryUsers.remove(uuid);
		}
		return REDIRECT_LOGIN;

	}
	
	@GetMapping(value = "/deactivateUser/{userId}")
	public String deactivateUser(Model model, RedirectAttributes redirect, @PathVariable(name = "userId") String userId) {

		try {
			if (!utils.isAdministrator() && utils.getUserId().equals(userId)
					|| utils.isAdministrator() && !utils.getUserId().equals(userId)) {

				utils.deactivateSessions(userId);
				userService.deleteUser(userId);
			}
			
			if (utils.isAdministrator()) {
				return REDIRECT_USER_LIST;
			} else {
				return "redirect:/logout";
			}
			
		} catch (final UserServiceException e) {
			log.error("Cannot deactivatea user", e);
			utils.addRedirectMessage("Cannot deactivatea user", redirect);
			return REDIRECT_USER_SHOW + utils.getUserId() + "/";
		}

	}

	@GetMapping(value = "/forgetDataUser/{userId}/{forgetMe}")
	public String forgetDataUser(Model model, RedirectAttributes redirect, @PathVariable(name = "userId") String userId,
			@PathVariable boolean forgetMe) {

		try {

			if (!utils.isAdministrator() && utils.getUserId().equals(userId)
					|| utils.isAdministrator() && !utils.getUserId().equals(userId)) {
				for (Ontology ontToDelete : ontologyService.getAllOntologiesByUser(userId)) {
					log.debug("remove: " + ontToDelete.getIdentification() + " \n");
					entityDeleteService.deleteOntology(ontToDelete.getId(), userId);
				}
			}

			if (utils.isAdministrator()) {
				return REDIRECT_USER_LIST;
			} else {
				return REDIRECT_USER_SHOW + utils.getUserId() + "/";
			}

		} catch (final UserServiceException e) {
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

			final String upperCase = String.valueOf((char) (new Random().nextInt(26) + 'a')).toUpperCase();
			final String newPassword = upperCase + UUID.randomUUID().toString().substring(0, 10) + "$";
			user.setPassword(newPassword);
			userService.updatePassword(user);
			multitenancyService.setResetPass(user.getUserId());
			log.info("Send new password to user by email {}", user.getEmail());

			final Configuration configuration = configurationService
					.getConfiguration(Configuration.Type.EXPIRATIONUSERS, "default", null);

			@SuppressWarnings("unchecked")
			final Map<String, Object> ymlExpirationUsersPassConfig = (Map<String, Object>) configurationService
			.fromYaml(configuration.getYmlConfig()).get("ResetUserPass");
			final int hours = ((Integer) ymlExpirationUsersPassConfig.get("hours")).intValue();

			final StringBuilder body = new StringBuilder();
			body.append(utils.getMessage("user.new.pass.for.user", "Your new password for user: "));
			body.append(user.getUserId());
			body.append(System.lineSeparator());
			body.append(utils.getMessage("user.new.pass.for.user.is", " is : "));
			body.append(" ");
			body.append(newPassword);
			body.append(System.lineSeparator());
			body.append(utils.getMessage("user.new.pass.for.user.time.for.update.1", " You have "));
			body.append(" ");
			body.append(hours);
			body.append(" ");
			body.append(utils.getMessage("user.new.pass.for.user.time.for.update.2",
					" hours to change this password but your user account will be blocked and you must contact the administrator. "));
			try {
				mailService.sendMail(user.getEmail(),
						utils.getMessage("user.new.pass.title.message", "[Onesait Plaform] Reset Password"),
						body.toString());
			} catch (final Exception e) {
				utils.addRedirectMessage("user.error.mailservice", redirectAttributes);
				return REDIRECT_LOGIN;
			}
			utils.addRedirectMessage("user.reset.success", redirectAttributes);
			log.debug("Pass reset");
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

			final Configuration configuration = configurationService
					.getConfiguration(Configuration.Type.EXPIRATIONUSERS, "default", null);

			@SuppressWarnings("unchecked")
			final Map<String, Object> ymlExpirationUsersPassConfig = (Map<String, Object>) configurationService
			.fromYaml(configuration.getYmlConfig()).get("ResetUserPass");
			final int hours = ((Integer) ymlExpirationUsersPassConfig.get("hours")).intValue();

			final StringBuilder body = new StringBuilder();
			body.append(utils.getMessage("user.new.pass.for.user", "Your new password for user: "));
			body.append(user.getUserId());
			body.append(System.lineSeparator());
			body.append(utils.getMessage("user.new.pass.for.user.is", " is : "));
			body.append(" ");
			body.append(newPassword);
			body.append(System.lineSeparator());
			body.append(utils.getMessage("user.new.pass.for.user.time.for.update.1", " You have "));
			body.append(" ");
			body.append(hours);
			body.append(" ");
			body.append(utils.getMessage("user.new.pass.for.user.time.for.update.2",
					" hours to change this password but your user account will be blocked and you must contact the administrator. "));
			try {
				mailService.sendMail(user.getEmail(),
						utils.getMessage("user.new.pass.title.message", "[Onesait Plaform] Reset Password"),
						body.toString());
			} catch (final Exception e) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

			}
			log.debug("Pass reset");
			return new ResponseEntity<>(HttpStatus.OK);
		}

	}

}
