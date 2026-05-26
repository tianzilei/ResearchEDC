package org.researchedc.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.researchedc.bean.core.NumericComparisonOperator;
import org.researchedc.bean.core.Role;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.core.TermType;
import org.researchedc.bean.core.UserType;
import org.researchedc.bean.login.StudyUserRoleBean;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.login.UserDTO;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.control.SpringServletAccess;
import org.researchedc.control.form.Validator;
import org.researchedc.core.SecurityManager;
import org.researchedc.dao.hibernate.AuthoritiesDao;
import org.researchedc.dao.login.UserAccountDAO;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.dao.managestudy.StudyDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.domain.user.AuthoritiesBean;
import org.researchedc.i18n.core.LocaleResolver;
import org.researchedc.i18n.util.ResourceBundleProvider;
import org.researchedc.service.pmanage.ParticipantPortalRegistrar;
import org.researchedc.view.Page;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.researchedc.dao.managestudy.DiscrepancyNoteDAO;

@Controller
@RequestMapping(value = "/auth/api/v1")
@ResponseStatus(value = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
public class UserAccountController {

    @Autowired
    protected IUserAccountDAO userAccountDao;

    @Autowired
    protected IStudyDAO studyDao;

	@Autowired
	@Qualifier("dataSource")
	private BasicDataSource dataSource;

	@Autowired
	ServletContext context;

	@Autowired
	AuthoritiesDao authoritiesDao;

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	IUserAccountDAO udao;
	IStudyDAO sdao;
	IStudySubjectDAO ssdao;
	UserAccountBean uBean;

	/**
	 * @api {post} /pages/auth/api/v1/createuseraccount Create a user account
	 * @apiName createOrUpdateAccount2
	 * @apiPermission admin
	 * @apiVersion 3.8.0
	 * @apiParam {String} username UserName
	 * @apiParam {String} fName First Name
	 * @apiParam {String} lName Last Name
	 * @apiParam {String} institution Institution
	 * @apiParam {String} email Email Address
	 * @apiParam {String} study_name Study Name
	 * @apiParam {String} role_name Role Name
	 * @apiParam {String} user_type User Type
	 * @apiParam {String} authorize_soap Authorize Soap
	 *
	 * @apiGroup User Account
	 * @apiDescription Creates a user account
	 * @apiParamExample {json} Request-Example:
	 *                  {
	 *                  "username": "testingUser",
	 *                  "fName": "Jimmy",
	 *                  "lName": "Sander",
	 *                  "institution": "OC",
	 *                  "email": "abcde@yahoo.com",
	 *                  "study_name": "Baseline Study 101",
	 *                  "role_name": "Data Manager",
	 *                  "user_type": "user",
	 *                  "authorize_soap":"false"
	 *                  }
	 * @apiErrorExample {json} Error-Response:
	 *                  HTTP/1.1 400 Bad Request
	 *                  {
	 *                  }
	 * @apiSuccessExample {json} Success-Response:
	 *                    HTTP/1.1 200 OK
	 *                    {
	 *                    "lastName": "Sander",
	 *                    "username": "testingUser",
	 *                    "firstName": "Jimmy",
	 *                    "password": "rgluVsO0",
	 *                    "apiKey": "5f462a16b3b04b1b9747262968bd5d2f"
	 *                    }
	 */

	@RequestMapping(value = "/createuseraccount", method = RequestMethod.POST)
	public ResponseEntity<HashMap> createOrUpdateAccount(HttpServletRequest request, @RequestBody HashMap<String, String> map) throws Exception {
		logger.info("I'm in createUserAccount");
		System.out.println("I'm in createUserAccount");
		uBean = null;

		String username = map.get("username");
		String fName = map.get("fName");
		String lName = map.get("lName");
		String institution = map.get("institution");
		String email = map.get("email");
		String studyName = map.get("study_name");
		String roleName = map.get("role_name");
		String userType = map.get("user_type");
		String authorizeSoap = map.get("authorize_soap"); // true or false

		request.setAttribute("username", username);
		request.setAttribute("fName", fName);
		request.setAttribute("lName", lName);
		request.setAttribute("institution", institution);
		request.setAttribute("email", email);
		request.setAttribute("study_name", studyName);
		request.setAttribute("role_name", roleName);

		// UserAccountBean ownerUserAccount = getUserAccountByApiKey(apiKey);
		UserAccountBean ownerUserAccount = (UserAccountBean) request.getSession().getAttribute("userBean");
		if (!ownerUserAccount.isActive() && (!ownerUserAccount.isTechAdmin() || !ownerUserAccount.isSysAdmin())) {
			logger.info("The Owner User Account is not Valid Account or Does not have Admin user type");
			System.out.println("The Owner User Account is not Valid Account or Does not have Admin user type");
			return new ResponseEntity<HashMap>(new HashMap(), org.springframework.http.HttpStatus.BAD_REQUEST);
		}

		// generate password
		String password = ""; // generate
		String passwordHash = UserAccountBean.LDAP_PASSWORD;
		SecurityManager secm = (SecurityManager) SpringServletAccess.getApplicationContext(context).getBean("securityManager");
		password = secm.genPassword();
		passwordHash = secm.encrytPassword(password, null);

		// Validate Entry Fields
        request.getSession().setAttribute(LocaleResolver.getLocaleSessionAttributeName(), Locale.of("en_US"));
		Validator v = new Validator(request);
		addValidationToFields(v, username);
		HashMap errors = v.validate();
		if (!errors.isEmpty()) {
			logger.info("Validation Error: " + errors.toString());
			System.out.println("Validation Error: " + errors.toString());
			return new ResponseEntity<HashMap>(new HashMap(), org.springframework.http.HttpStatus.BAD_REQUEST);
		}

		StudyBean study = getStudyByName(studyName);
		if (!study.isActive()) {
			logger.info("The Study Name is not Valid");
			System.out.println("The Study Name is not Valid");
			return new ResponseEntity<HashMap>(new HashMap(), org.springframework.http.HttpStatus.BAD_REQUEST);
		}

		// Role
		ResourceBundle resterm = org.researchedc.i18n.util.ResourceBundleProvider.getTermsBundle();
		Map<Integer, String> roleMap = buildRoleMap(study, resterm);
		boolean found = false;
		Role role = null;
		for (Map.Entry<Integer, String> entry : roleMap.entrySet()) {
			if (roleName.equalsIgnoreCase(entry.getValue())) {
				Integer key = entry.getKey();
				role = Role.get(key);
				found = true;
				break;
			}
		}

		if (!found) {
			logger.info("The Role is not a Valid Role for the Study or Site");
			System.out.println("The Role is not a Valid Role for the Study or Site");
			return new ResponseEntity<HashMap>(new HashMap(), org.springframework.http.HttpStatus.BAD_REQUEST);
		}

		// User Types
		found = false;
		UserType uType = null;
		ArrayList<UserType> types = UserType.toArrayList();
		types.remove(UserType.INVALID);
		for (UserType type : types) {
			if (userType.equalsIgnoreCase(type.getName())) {
				uType = UserType.get(type.getId());
				found = true;
				break;
			}
		}

		if (!found) {
			logger.info("The Type is not a Valid User Type");
			System.out.println("The Type is not a Valid User Type");

			return new ResponseEntity<HashMap>(new HashMap(), org.springframework.http.HttpStatus.BAD_REQUEST);
		}
		// build UserName

		uBean = buildUserAccount(username, fName, lName, password, institution, study, ownerUserAccount, email, passwordHash, Boolean.valueOf(authorizeSoap), role, uType);
		HashMap<String, Object> userDTO = null;
		UserAccountBean uaBean = getUserAccount(uBean.getName());
		if (!uaBean.isActive()) {
			createUserAccount(uBean);
			uBean.setUpdater(uBean.getOwner());
			updateUserAccount(uBean);
			logger.info("***New User Account is created***");
			System.out.println("***New User Account is created***");
			uBean.setPasswd(password);

			userDTO = new HashMap<String, Object>();

			userDTO.put("username", uBean.getName());
			userDTO.put("password", uBean.getPasswd());
			userDTO.put("firstName", uBean.getFirstName());
			userDTO.put("lastName", uBean.getLastName());
			userDTO.put("apiKey", uBean.getApiKey());
		}
		return new ResponseEntity<HashMap>(userDTO, org.springframework.http.HttpStatus.OK);
	}

	private UserAccountBean buildUserAccount(String username, String fName, String lName, String password, String institution, StudyBean study, UserAccountBean ownerUserAccount, String email,
			String passwordHash, Boolean authorizeSoap, Role roleName, UserType userType) throws Exception {

		UserAccountBean createdUserAccountBean = new UserAccountBean();

		createdUserAccountBean.setName(username);
		createdUserAccountBean.setFirstName(fName);
		createdUserAccountBean.setLastName(lName);
		createdUserAccountBean.setEmail(username);
		createdUserAccountBean.setInstitutionalAffiliation(institution);
		createdUserAccountBean.setLastVisitDate(null);
		createdUserAccountBean.setActiveStudyId(study.getId());
		createdUserAccountBean.setPasswdTimestamp(null);
		createdUserAccountBean.setPasswdChallengeQuestion("");
		createdUserAccountBean.setPasswdChallengeAnswer("");
		createdUserAccountBean.setOwner(ownerUserAccount);
		createdUserAccountBean.setRunWebservices(false);
		createdUserAccountBean.setPhone("");
		createdUserAccountBean.setAccessCode("");
		createdUserAccountBean.setPasswd(password);
		createdUserAccountBean.setEmail(email);
		createdUserAccountBean.setEnableApiKey(true);
		createdUserAccountBean.setPasswd(passwordHash);
		createdUserAccountBean.setRunWebservices(authorizeSoap);

		String apiKey = null;
		do {
			apiKey = getRandom32ChApiKey();
		} while (isApiKeyExist(apiKey));
		createdUserAccountBean.setApiKey(apiKey);

		createdUserAccountBean = addActiveStudyRole(createdUserAccountBean, study.getId(), roleName, ownerUserAccount);
		createdUserAccountBean.addUserType(userType);

		authoritiesDao.saveOrUpdate(new AuthoritiesBean(createdUserAccountBean.getName()));

		return createdUserAccountBean;
	}

	private void createUserAccount(UserAccountBean userAccountBean) {
		udao = this.userAccountDao;
		udao.create(userAccountBean);
	}

	private StudyBean getParentStudy(String studyOid) {
		StudyBean study = getStudy(studyOid);
		if (study.getParentStudyId() == 0) {
			return study;
		} else {
			StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
			return parentStudy;
		}

	}

	private StudyBean getParentStudy(Integer studyId) {
		StudyBean study = getStudy(studyId);
		if (study.getParentStudyId() == 0) {
			return study;
		} else {
			StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
			return parentStudy;
		}

	}

	private StudyBean getStudyByName(String name) {
		sdao = this.studyDao;
		StudyBean studyBean = (StudyBean) sdao.findByName(name);
		return studyBean;
	}

	private StudyBean getStudy(String oid) {
		sdao = this.studyDao;
		StudyBean studyBean = (StudyBean) sdao.findByOid(oid);
		return studyBean;
	}

	private StudyBean getStudy(Integer id) {
		sdao = this.studyDao;
		StudyBean studyBean = (StudyBean) sdao.findByPK(id);
		return studyBean;
	}

	private UserAccountBean addActiveStudyRole(UserAccountBean createdUserAccountBean, int studyId, Role r, UserAccountBean ownerUserAccount) {
		StudyUserRoleBean studyUserRole = new StudyUserRoleBean();
		studyUserRole.setStudyId(studyId);
		studyUserRole.setRoleName(r.getName());
		studyUserRole.setStatus(Status.AVAILABLE);
		studyUserRole.setOwner(ownerUserAccount);
		createdUserAccountBean.addRole(studyUserRole);
		createdUserAccountBean.setAccountNonLocked(false);
		return createdUserAccountBean;
	}

	private ArrayList<UserAccountBean> getUserAccountByStudy(String userName, ArrayList allStudies) {
		udao = this.userAccountDao;
		ArrayList<UserAccountBean> userAccountBeans = udao.findStudyByUser(userName, allStudies);
		return userAccountBeans;
	}

	private UserAccountBean getUserAccount(String userName) {
		udao = this.userAccountDao;
		UserAccountBean userAccountBean = (UserAccountBean) udao.findByUserName(userName);
		return userAccountBean;
	}

	private UserAccountBean getUserAccountByApiKey(String apiKey) {
		udao = this.userAccountDao;
		UserAccountBean userAccountBean = (UserAccountBean) udao.findByApiKey(apiKey);
		return userAccountBean;
	}

	private void updateUserAccount(UserAccountBean userAccountBean) {
		udao.update(userAccountBean);
	}

	private ArrayList getRoles() {

		ArrayList roles = Role.toArrayList();
		roles.remove(Role.ADMIN);

		return roles;
	}

	public Boolean isApiKeyExist(String uuid) {
		IUserAccountDAO udao = this.userAccountDao;
		UserAccountBean uBean = (UserAccountBean) udao.findByApiKey(uuid);
		if (uBean == null || !uBean.isActive()) {
			return false;
		} else {
			return true;
		}
	}

	public String getRandom32ChApiKey() {
		String uuid = UUID.randomUUID().toString();
		return uuid.replaceAll("-", "");
	}

	protected UserDetails getUserDetails() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetails) {
			return (UserDetails) principal;
		} else {
			return null;
		}
	}

	public void addValidationToFields(Validator v, String username) {
		v.addValidation("username", Validator.NO_BLANKS);
		v.addValidation("username", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 64);
		if (!username.equals("root"))
			v.addValidation("username", Validator.IS_A_USERNAME);

		v.addValidation("username", Validator.USERNAME_UNIQUE, (org.researchedc.dao.core.EntityDAO) udao);
		v.addValidation("fName", Validator.NO_BLANKS);
		v.addValidation("fName", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 50);
		v.addValidation("lName", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 50);

		v.addValidation("email", Validator.NO_BLANKS);
		v.addValidation("email", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 120);
		v.addValidation("email", Validator.IS_A_EMAIL);

		v.addValidation("institution", Validator.NO_BLANKS);
		v.addValidation("institution", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);

		// v.addValidation("study_name", Validator.ENTITY_EXISTS, sdao);
		// v.addValidation("role_name", Validator.IS_VALID_TERM, TermType.ROLE);

	}

	public Map buildRoleMap(StudyBean study, ResourceBundle resterm) {
		Map roleMap = new LinkedHashMap();

		if (study.getParentStudyId() > 0) {
			for (Iterator it = getRoles().iterator(); it.hasNext();) {
				Role role = (Role) it.next();
				switch (role.getId()) {
				// case 2: roleMap.put(role.getId(), resterm.getString("site_Study_Coordinator").trim());
				// break;
				// case 3: roleMap.put(role.getId(), resterm.getString("site_Study_Director").trim());
				// break;
				case 4:
					roleMap.put(role.getId(), resterm.getString("site_investigator").trim());
					break;
				case 5:
					roleMap.put(role.getId(), resterm.getString("site_Data_Entry_Person").trim());
					break;
				case 6:
					roleMap.put(role.getId(), resterm.getString("site_monitor").trim());
					break;
				case 7:
					roleMap.put(role.getId(), resterm.getString("site_Data_Entry_Person2").trim());
					break;
				default:
					// logger.info("No role matched when setting role description");
				}
			}
		} else {
			for (Iterator it = getRoles().iterator(); it.hasNext();) {
				Role role = (Role) it.next();
				switch (role.getId()) {
				case 2:
					roleMap.put(role.getId(), resterm.getString("Study_Coordinator").trim());
					break;
				case 3:
					roleMap.put(role.getId(), resterm.getString("Study_Director").trim());
					break;
				case 4:
					roleMap.put(role.getId(), resterm.getString("Investigator").trim());
					break;
				case 5:
					roleMap.put(role.getId(), resterm.getString("Data_Entry_Person").trim());
					break;
				case 6:
					roleMap.put(role.getId(), resterm.getString("Monitor").trim());
					break;
				default:
					// logger.info("No role matched when setting role description");
				}
			}
		}
		return roleMap;
	}

}
