package org.researchedc.controller;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import jakarta.servlet.ServletContext;

import org.researchedc.bean.core.Role;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.core.UserType;
import org.researchedc.bean.login.StudyUserRoleBean;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.login.UserDTO;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.service.StudyParameterValueBean;
import org.researchedc.control.SpringServletAccess;
import org.researchedc.dao.spi.AuthoritiesDao;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.spi.IStudyParameterValueDAO;
import org.researchedc.domain.user.AuthoritiesBean;
import org.researchedc.i18n.util.ResourceBundleProvider;
import org.researchedc.service.pmanage.ParticipantPortalRegistrar;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(value = "/accounts")
@ResponseStatus(value = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
public class AccountController {

    @Autowired
    protected IStudyParameterValueDAO studyParameterValueDao;

    @Autowired
    protected IUserAccountDAO userAccountDao;

    @Autowired
    protected IStudySubjectDAO studySubjectDao;

    @Autowired
    protected IStudyDAO studyDao;

    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;

    @Autowired
    ServletContext context;

    @Autowired
    AuthenticationManager authenticationManager;

    public static final String FORM_CONTEXT = "ecid";

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String INPUT_INSTITUTION = "PFORM";
    UserDTO uDTO;
    AuthoritiesDao authoritiesDao;
    ParticipantPortalRegistrar participantPortalRegistrar;

    /**
     * @api {post} /pages/accounts/login Retrieve a user account
     * @apiName getAccountByUserName
     * @apiPermission admin
     * @apiVersion 3.8.0
     * @apiParam {String} username OC login Username.
     * @apiParam {String} password OC login Password .
     * @apiGroup User Account
     * @apiDescription Retrieve a user account
     * @apiParamExample {json} Request-Example:
     *                  {
     *                  "username": "usera",
     *                  "password": "password"
     *                  }
     * @apiErrorExample {json} Error-Response:
     *                  HTTP/1.1 401 Bad Credentials
     *                  {
     *                  }
     * @apiSuccessExample {json} Success-Response:
     *                    HTTP/1.1 200 OK
     *                    {
     *                    "lastName": "User",
     *                    "username": "root",
     *                    "roles": [
     *                    {"roleName": "director", "studyOID": "S_DEFAULTS1"},
     *                    {"roleName": "Data Specialist", "studyOID": "S_JAPSTUDY_5293"}
     *                    ],
     *                    "firstName": "Root",
     *                    "password": "5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8",
     *                    "apiKey": "6e8b69f6fb774e899f9a6c349c5adace"
     *                    }
     */


    @RequestMapping(value = "/study/{studyOid}/crc/{crcUserName}", method = RequestMethod.GET)
    public ResponseEntity<UserDTO> getAccount1(@PathVariable("studyOid") String studyOid, @PathVariable("crcUserName") String crcUserName) throws Exception {
        ResourceBundleProvider.updateLocale(Locale.of("en_US"));
        IUserAccountDAO udao = this.userAccountDao;
        uDTO = null;

        StudyBean parentStudy = getParentStudy(studyOid);
        Integer pStudyId = parentStudy.getId();
        String oid = parentStudy.getOid();

        if (isStudyASiteLevelStudy(studyOid))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        if (!mayProceed(oid))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        if (isStudyDoesNotExist(oid))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        if (isCRCUserAccountDoesNotExist(crcUserName))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        if (doesCRCNotHaveStudyAccessRole(crcUserName, pStudyId))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        UserAccountBean userAccountBean = (UserAccountBean) udao.findByUserName(crcUserName);
        buildUserDTO(userAccountBean);
        return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.OK);
    }

    public Boolean isCRCHasAccessToStudySubject(String studyOid, String crcUserName, String studySubjectId) {
        uDTO = null;
        StudyBean parentStudy = getParentStudy(studyOid);
        Integer pStudyId = parentStudy.getId();
        String oid = parentStudy.getOid();

        if (isStudySubjecAndCRCRolesMatch(studySubjectId, crcUserName, studyOid))
            return true;

        return false;
    }

    /**
     * @api {get} /pages/accounts/study/:studyOid/accesscode/:accessCode Retrieve a user account - participant
     * @apiName getAccount2
     * @apiPermission Module participate - enabled & admin
     * @apiVersion 3.8.0
     * @apiParam {String} studyOid Study Oid.
     * @apiParam {String} accessCode Participant Access code .
     * @apiGroup User Account
     * @apiDescription Retrieves the participant user account with the given accessCode and studyOid
     * @apiParamExample {json} Request-Example:
     *                  {
     *                  "studyOid": " S_BL101",
     *                  "accessCode": "yfzqpvDpiJftIZgNDphvxg=="
     *                  }
     * @apiSuccessExample {json} Success-Response:
     *                    HTTP/1.1 200 OK
     *                    {
     *                    "lName": "",
     *                    "mobile": "jLGQwxkuVpPBLJCtnLdrAw==",
     *                    "accessCode": "yfzqpvDpiJftIZgNDphvxg==",
     *                    "password": "5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8",
     *                    "email": "XzJadh3l3V7uUoPCggbSoIfoNW8IQU3qsvrtHfJH7J0=",
     *                    "userName": "S_BL101.SS_SUBA101",
     *                    "studySubjectId": null,
     *                    "fName": "07hQGfwT6LRXk0rLLYwkviwNdOEycnj4lOjrNMBdesk="
     *                    }
     */

    @RequestMapping(value = "/study/{studyOid}/accesscode/{accessCode}", method = RequestMethod.GET)
    public ResponseEntity<UserDTO> getAccount2(@PathVariable("studyOid") String studyOid, @PathVariable("accessCode") String accessCode) throws Exception {
        ResourceBundleProvider.updateLocale(Locale.of("en_US"));
        uDTO = null;

        accessCode = URLDecoder.decode(accessCode, "UTF-8");

        StudyBean parentStudy = getParentStudy(studyOid);
        String oid = parentStudy.getOid();

        if (isStudyASiteLevelStudy(studyOid))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        if (!mayProceed(oid))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        if (isStudyDoesNotExist(oid))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        if (isAccessCodeIsNull(accessCode))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        UserAccountBean accessCodeAccountBean = getAccessCodeAccount(accessCode);
        if (!accessCodeAccountBean.isActive())
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        // Since 3.8, openclinica participate needs to be able to use api from openclinica using api_key
        // Copied from UserAccountController.java
        // This code should've been in liquibase migration for better readability.
        if (accessCodeAccountBean.getApiKey() == null || accessCodeAccountBean.getApiKey().isEmpty()) {
            String apiKey = null;
            do {
                apiKey = getRandom32ChApiKey();
            } while (isApiKeyExist(apiKey));
            accessCodeAccountBean.setEnableApiKey(true);
            accessCodeAccountBean.setApiKey(apiKey);
            updateUserAccount(accessCodeAccountBean);
        }

        buildUserDTO(accessCodeAccountBean);
        // Client want to trade access_code for api_key, for later usage of our api.
        if (accessCodeAccountBean.isEnableApiKey()) {
            uDTO.setApiKey(accessCodeAccountBean.getApiKey());
        }
        return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.OK);
    }

    /**
     * @api {get} /pages/accounts/study/:studyOid/studysubject/:studySubjectId Retrieve a user account - participant
     * @apiName getAccount3
     * @apiPermission Module participate - enabled & admin
     * @apiVersion 3.8.0
     * @apiParam {String} studyOid Study Oid.
     * @apiParam {String} studySubjectId Study Subject Id .
     * @apiGroup User Account
     * @apiDescription Retrieves the participant user account with the given studySubjectId and studyOid
     * @apiParamExample {json} Request-Example:
     *                  {
     *                  "studyOid": " S_BL101",
     *                  "studySubjectId": "Sub100"
     *                  }
     * @apiSuccessExample {json} Success-Response:
     *                    HTTP/1.1 200 OK
     *                    {
     *                    "lName": "",
     *                    "mobile": "JTaa7WGRdH5dGs42XyTrgA==",
     *                    "accessCode": "5s02UFpiMBijWuzaxSOojg==",
     *                    "password": "5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8",
     *                    "email": "XzJadh3l3V7uUoPCggbSoIfoNW8IQU3qsvrtHfJH7J0=",
     *                    "userName": "S_BL101.SS_SUB100",
     *                    "studySubjectId": null,
     *                    "fName": "pdyGCN1CdAKIGOUEERz/yQ=="
     *                    }
     */
    @RequestMapping(value = "/study/{studyOid}/studysubject/{studySubjectId}", method = RequestMethod.GET)
    public ResponseEntity<UserDTO> getAccount3(@PathVariable("studyOid") String studyOid, @PathVariable("studySubjectId") String studySubjectId)
            throws Exception {
        ResourceBundleProvider.updateLocale(Locale.of("en_US"));
        uDTO = null;

        StudyBean parentStudy = getParentStudy(studyOid);
        String oid = parentStudy.getOid();

        StudySubjectBean studySubjectBean = getStudySubject(studySubjectId, parentStudy);

        if (isStudyASiteLevelStudy(studyOid))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        if (!mayProceed(oid, studySubjectBean))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        if (isStudyDoesNotExist(oid))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        if (isStudySubjectDoesNotExist(studySubjectBean))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        // build UserName
        HashMap<String, String> mapValues = buildParticipantUserName(studySubjectBean);
        String pUserName = mapValues.get("pUserName"); // Participant User Name

        IUserAccountDAO udao = this.userAccountDao;
        UserAccountBean userAccountBean = (UserAccountBean) udao.findByUserName(pUserName);
        if (!userAccountBean.isActive()) {
            uDTO = new UserDTO();
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.OK);
        } else {
            buildUserDTO(userAccountBean);
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.OK);
        }
    }

    /**
     * @api {post} /pages/accounts/ Create a user account - participant
     * @apiName createParticipantUserAccount
     * @apiPermission Module participate - enabled & admin
     * @apiVersion 3.8.0
     * @apiParam {String} studyOid Study Oid.
     * @apiParam {String} studySubjectId Study Subject Id .
     * @apiParam {String} fName First Name
     * @apiParam {String} lName Last Name
     * @apiParam {String} mobile Mobile Phone
     * @apiParam {String} accessCode Access Code
     * @apiParam {String} crcUserName CRC UserName
     * @apiParam {String} email Email Address
     *
     * @apiGroup User Account
     * @apiDescription Creates a participant user account
     * @apiParamExample {json} Request-Example:
     *                  {
     *                  "studyOid": "S_BL101",
     *                  "studySubjectId": "Sub100",
     *                  "fName": "Dany",
     *                  "lName": "Keegan",
     *                  "mobile": "617 865 4567",
     *                  "accessCode": "5s02UFpiMBijWuzaxSOojg==",
     *                  "crcUserName": "crc_user",
     *                  "email": "abc@yahoo.com"
     *                  }
     * @apiSuccessExample {json} Success-Response:
     *                    HTTP/1.1 200 OK
     *                    {
     *                    "studySubjectId": null,
     *                    "email": "abc@yahoo.com",
     *                    "accessCode": "5s02UFpiMBijWuzaxSOojg==",
     *                    "password": "5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8",
     *                    "userName": "S_BL101.SS_SUB100",
     *                    "fName": "Dany",
     *                    "lName": "Keegan",
     *                    "mobile": "617 865 4567"
     *                    }
     */
    /**
     * @api {post} /pages/accounts/ Update a user account - participant
     * @apiName updateParticipantUserAccount
     * @apiPermission Module participate - enabled & admin
     * @apiVersion 3.8.0
     * @apiParam {String} studyOid Study Oid.
     * @apiParam {String} studySubjectId Study Subject Id .
     * @apiParam {String} fName First Name
     * @apiParam {String} lName Last Name
     * @apiParam {String} mobile Mobile Phone
     * @apiParam {String} accessCode Access Code
     * @apiParam {String} crcUserName CRC UserName
     * @apiParam {String} email Email Address
     *
     * @apiGroup User Account
     * @apiDescription Updates a participant user account
     * @apiParamExample {json} Request-Example:
     *                  {
     *                  "studyOid": "S_BL101",
     *                  "studySubjectId": "Sub100",
     *                  "fName": "Dany",
     *                  "lName": "Keegan",
     *                  "mobile": "617 865 4567",
     *                  "accessCode": "5s02UFpiMBijWuzaxSOojg==",
     *                  "crcUserName": "crc_user",
     *                  "email": "abc@yahoo.com"
     *                  }
     * @apiSuccessExample {json} Success-Response:
     *                    HTTP/1.1 200 OK
     *                    {
     *                    "studySubjectId": null,
     *                    "email": "abc@yahoo.com",
     *                    "accessCode": "5s02UFpiMBijWuzaxSOojg==",
     *                    "password": "5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8",
     *                    "userName": "S_BL101.SS_SUB100",
     *                    "fName": "Dany",
     *                    "lName": "Keegan",
     *                    "mobile": "617 865 4567"
     *                    }
     */

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity<UserDTO> createOrUpdateAccount(@RequestBody HashMap<String, String> map) throws Exception {
        uDTO = null;

        StudyBean parentStudy = getParentStudy(map.get("studyOid"));
        String oid = parentStudy.getOid();

        String studySubjectId = map.get("studySubjectId");
        String fName = map.get("fName");
        String lName = map.get("lName");
        String mobile = map.get("mobile");
        String accessCode = map.get("accessCode");
        String crcUserName = map.get("crcUserName");

        ResourceBundleProvider.updateLocale(Locale.of("en_US"));

        UserAccountBean uBean = null;

        StudySubjectBean studySubjectBean = getStudySubject(studySubjectId, parentStudy);
        UserAccountBean ownerUserAccount = getUserAccount(crcUserName);

        if (!mayProceed(oid, studySubjectBean))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        if (isStudyDoesNotExist(oid))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        if (isStudySubjectDoesNotExist(studySubjectBean))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        if (isFistNameInValid(fName))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        if (isPhoneFieldIsNull(mobile))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        if (isAccessCodeIsNull(accessCode))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        if (isAccessCodeExistInSystem(accessCode))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        // build UserName
        HashMap<String, String> mapValues = buildParticipantUserName(studySubjectBean);
        String pUserName = mapValues.get("pUserName"); // Participant User Name
        String studySubjectOid = mapValues.get("studySubjectOid");
        Integer pStudyId = Integer.valueOf(mapValues.get("pStudyId"));

        if (isCRCUserAccountDoesNotExist(crcUserName))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        // Verify CRC_user has the appropriate role as 'data entry person'or 'data entry person 2' and have access to
        // the specific study/site
        // This also verifies that fact that the CRC and the Participant both have access to same study/site
        if (doesCRCNotHaveStudyAccessRole(crcUserName, pStudyId))
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        // Participant user account create (if does not exist in user table) or Update(if exist in user table)
        uBean = buildUserAccount(oid, studySubjectOid, fName, lName, mobile, accessCode, ownerUserAccount, pUserName);
        UserAccountBean participantUserAccountBean = getUserAccount(pUserName);
        if (!participantUserAccountBean.isActive()) {
            createUserAccount(uBean);
            uBean.setUpdater(uBean.getOwner());
            updateUserAccount(uBean);
            disableUserAccount(uBean);
            logger.info("***New User Account is created***");
            uDTO = buildUserDTO(uBean);
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.OK);

        } else {
            uBean.setId(getUserAccount(uBean.getName()).getId());
            uBean.setUpdater(uBean.getOwner());
            updateUserAccount(uBean);
            logger.info("***User Account already exist in the system and data is been Updated ***");
            uDTO = buildUserDTO(uBean);
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.OK);
        }
    }

    /**
     * @api {post} /pages/accounts/timezone Update subject time zone
     * @apiName updateTimezone
     * @apiPermission admin
     * @apiVersion 3.8.0
     * @apiParam {String} studyOid Study Oid.
     * @apiParam {String} studySubjectId Study Subject Oid .
     * @apiParam {String} timeZone Time Zone .
     * @apiGroup Subject
     * @apiDescription Updates the subject time zone
     * @apiParamExample {json} Request-Example:
     *                  {
     *                  "studyOid": "S_BL101",
     *                  "studySubjectId": "SS_SUB100",
     *                  "timeZone": "America/New_York"
     *                  }
     * @apiSuccessExample {json} Success-Response:
     *                    HTTP/1.1 200 OK
     *                    {
     *                    }
     */

    @RequestMapping(value = "/timezone", method = RequestMethod.POST)
    public ResponseEntity<UserDTO> updateTimezone(@RequestBody HashMap<String, String> map) throws Exception {
        uDTO = null;

        StudyBean parentStudy = getParentStudy(map.get("studyOid"));
        String oid = parentStudy.getOid();

        String studySubjectId = map.get("studySubjectId");
        String timeZone = map.get("timeZone");

        ResourceBundleProvider.updateLocale(Locale.of("en_US"));
        IUserAccountDAO udao = this.userAccountDao;

        StudySubjectBean studySubjectBean = getStudySubjectByOidAndStudy(studySubjectId, parentStudy.getId());
        HashMap<String, String> mapValues = buildParticipantUserName(studySubjectBean);
        String pUserName = mapValues.get("pUserName"); // Participant User Name

        udao = this.userAccountDao;
        UserAccountBean userAccountBean = (UserAccountBean) udao.findByUserName(pUserName);

        if (studySubjectBean.isActive()) {
            studySubjectBean.setTime_zone(timeZone);
            studySubjectBean.setUpdater(userAccountBean);
            updateStudySubjectBean(studySubjectBean);
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.OK);
        }
        return null;
    }

    private UserDTO buildUserDTO(UserAccountBean userAccountBean) {
        uDTO = new UserDTO();
        uDTO.setfName(userAccountBean.getFirstName());
        uDTO.setlName(userAccountBean.getLastName());
        uDTO.setMobile(userAccountBean.getPhone());
        uDTO.setUserName(userAccountBean.getName());
        uDTO.setAccessCode(userAccountBean.getAccessCode());
        uDTO.setPassword(userAccountBean.getPasswd());
        return uDTO;
    }

    private UserAccountBean buildUserAccount(String studyOid, String studySubjectOid, String fName, String lName, String mobile, String accessCode,
            UserAccountBean ownerUserAccount, String pUserName) throws Exception {

        UserAccountBean createdUserAccountBean = new UserAccountBean();

        createdUserAccountBean.setName(pUserName);
        createdUserAccountBean.setFirstName(fName);
        createdUserAccountBean.setLastName(lName);
        createdUserAccountBean.setInstitutionalAffiliation(INPUT_INSTITUTION);
        createdUserAccountBean.setLastVisitDate(null);
        createdUserAccountBean.setActiveStudyId(getStudy(studyOid).getId());
        createdUserAccountBean.setPasswdTimestamp(null);
        createdUserAccountBean.setPasswdChallengeQuestion("");
        createdUserAccountBean.setPasswdChallengeAnswer("");
        createdUserAccountBean.setOwner(ownerUserAccount);
        createdUserAccountBean.setRunWebservices(false);
        createdUserAccountBean.setPhone(mobile);
        createdUserAccountBean.setAccessCode(accessCode);
        createdUserAccountBean.setPasswd("5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8");

        // Since 3.8, openclinica participate needs to be able to use api from openclinica using api_key
        // Copied from UserAccountController.java
        String apiKey = null;
        do {
            apiKey = getRandom32ChApiKey();
        } while (isApiKeyExist(apiKey));
        createdUserAccountBean.setEnableApiKey(true);
        createdUserAccountBean.setApiKey(apiKey);

        Role r = Role.RESEARCHASSISTANT2;
        createdUserAccountBean = addActiveStudyRole(createdUserAccountBean, getStudy(studyOid).getId(), r, ownerUserAccount);
        UserType type = UserType.get(2);
        createdUserAccountBean.addUserType(type);

        authoritiesDao = (AuthoritiesDao) SpringServletAccess.getApplicationContext(context).getBean("authoritiesDao");
        authoritiesDao.saveOrUpdate(new AuthoritiesBean(createdUserAccountBean.getName()));

        return createdUserAccountBean;
    }

    private void createUserAccount(UserAccountBean userAccountBean) {
        IUserAccountDAO udao = this.userAccountDao;
        udao.create(userAccountBean);
    }

    private void updateUserAccount(UserAccountBean userAccountBean) {
        IUserAccountDAO udao = this.userAccountDao;
        udao.update(userAccountBean);
    }

    private void disableUserAccount(UserAccountBean userAccountBean) {
        IUserAccountDAO udao = this.userAccountDao;
        udao.delete(userAccountBean);
    }

    private UserAccountBean addActiveStudyRole(UserAccountBean createdUserAccountBean, int studyId, Role r, UserAccountBean ownerUserAccount) {
        StudyUserRoleBean studyUserRole = new StudyUserRoleBean();
        studyUserRole.setStudyId(studyId);
        studyUserRole.setRoleName(r.getName());
        studyUserRole.setStatus(Status.AUTO_DELETED);
        studyUserRole.setOwner(ownerUserAccount);
        createdUserAccountBean.addRole(studyUserRole);
        createdUserAccountBean.setLockCounter(3);
        createdUserAccountBean.setAccountNonLocked(false);
        return createdUserAccountBean;
    }

    private ArrayList<UserAccountBean> getUserAccountByStudy(String userName, ArrayList allStudies) {
        IUserAccountDAO udao = this.userAccountDao;
        ArrayList<UserAccountBean> userAccountBeans = udao.findStudyByUser(userName, allStudies);
        return userAccountBeans;
    }

    private UserAccountBean getUserAccount(String userName) {
        IUserAccountDAO udao = this.userAccountDao;
        UserAccountBean userAccountBean = (UserAccountBean) udao.findByUserName(userName);
        return userAccountBean;
    }

    private UserAccountBean getAccessCodeAccount(String accessCode) {
        IUserAccountDAO udao = this.userAccountDao;
        UserAccountBean userAccountBean = (UserAccountBean) udao.findByAccessCode(accessCode);
        return userAccountBean;
    }

    private StudyBean getStudy(String oid) {
        IStudyDAO sdao = this.studyDao;
        StudyBean studyBean = (StudyBean) sdao.findByOid(oid);
        return studyBean;
    }

    private StudyBean getStudy(Integer id) {
        IStudyDAO sdao = this.studyDao;
        StudyBean studyBean = (StudyBean) sdao.findByPK(id);
        return studyBean;
    }

    private StudySubjectBean getStudySubjectByOidAndStudy(String oid, int studyId) {
        IStudySubjectDAO ssdao = this.studySubjectDao;
        StudySubjectBean studySubjectBean = (StudySubjectBean) ssdao.findByOidAndStudy(oid, studyId);
        return studySubjectBean;
    }

    private StudySubjectBean getStudySubject(String label, StudyBean study) {
        IStudySubjectDAO ssdao = this.studySubjectDao;
        StudySubjectBean studySubjectBean = (StudySubjectBean) ssdao.findByLabelAndStudy(label, study);
        return studySubjectBean;
    }

    private StudySubjectBean getStudySubject(String oid) {
        IStudySubjectDAO ssdao = this.studySubjectDao;
        StudySubjectBean studySubjectBean = (StudySubjectBean) ssdao.findByOid(oid);
        return studySubjectBean;
    }

    private void updateStudySubjectBean(StudySubjectBean sBean) {
        IStudySubjectDAO ssdao = this.studySubjectDao;
        ssdao.update(sBean);
    }

    private Boolean isStudyDoesNotExist(String studyOid) {
        StudyBean studyBean = getStudy(studyOid);
        if (studyBean == null) {
            logger.info("***Study  Does Not Exist ***");
            return true;
        }
        return false;
    }

    private Boolean isStudyASiteLevelStudy(String studyOid) {
        StudyBean studyBean = getStudy(studyOid);
        if (studyBean.getParentStudyId() != 0) {
            logger.info("***Study provided in the URL is a Site study***");
            return true;
        }
        return false;
    }

    private Boolean isStudySubjectDoesNotExist(StudySubjectBean studySubjectBean) {
        if (studySubjectBean == null || !studySubjectBean.isActive()) {
            logger.info("***Study Subject Does Not Exist OR the Study Subject is not associated with the Study_Oid in the URL   ***");
            return true;
        }
        return false;
    }

    private Boolean isFistNameInValid(String fName) {
        if (fName.length() < 1) {
            logger.info("***     First Name length is less than 1 characters    ***");
            return true;
        }
        return false;
    }

    private Boolean isPhoneFieldIsNull(String mobile) {
        if (mobile.length() == 0) {
            logger.info("***     Phone # is a Required Field   ***");
            return true;
        }
        return false;
    }

    private Boolean isAccessCodeIsNull(String accessCode) {
        if (accessCode.length() == 0) {
            logger.info("***Access Code is a Required field and can't be null ***");
            return true;
        }
        return false;
    }

    private Boolean isAccessCodeExistInSystem(String accessCode) {
        UserAccountBean accessCodeAccountBean = getAccessCodeAccount(accessCode);
        if (accessCodeAccountBean.isActive()) {
            logger.info("***Access Code already Exist in the User Table ***");
            System.out.println("***Access Code already Exist in the User Table ***");
            return true;
        }
        return false;
    }

    private Boolean isCRCUserAccountDoesNotExist(String crcUserName) {
        UserAccountBean ownerUserAccount = getUserAccount(crcUserName);
        if (!ownerUserAccount.isActive()) {
            logger.info("***  CRC user acount does not Exist in the User Table ***");
            return true;
        }
        return false;
    }

    private HashMap buildParticipantUserName(StudySubjectBean studySubjectBean) {
        HashMap<String, String> map = new HashMap();
        String studySubjectOid = studySubjectBean.getOid();
        Integer studyId = studySubjectBean.getStudyId();
        StudyBean study = getParentStudy(studyId);
        Integer pStudyId = study.getId();

        String pUserName = study.getOid() + "." + studySubjectOid;
        map.put("pUserName", pUserName);
        map.put("pStudyId", pStudyId.toString());
        map.put("studySubjectOid", studySubjectOid);

        return map;
    }

    private Boolean doesCRCNotHaveStudyAccessRole(String crcUserName, Integer pStudyId) {
        IUserAccountDAO udao = this.userAccountDao;
        boolean found = false;
        ArrayList<StudyUserRoleBean> studyUserRoleBeans = (ArrayList<StudyUserRoleBean>) udao.findAllRolesByUserName(crcUserName);
        for (StudyUserRoleBean studyUserRoleBean : studyUserRoleBeans) {
            StudyBean study = getParentStudy(studyUserRoleBean.getStudyId());

            if ((study.getId() == pStudyId) && (studyUserRoleBean.getRoleName().equals("ra") || studyUserRoleBean.getRoleName().equals("ra2"))
                    && studyUserRoleBean.getStatus().isAvailable()) {
                found = true;
                System.out.println("if found :" + found);
                break;
            }
        }
        if (!found) {
            logger.info("*** CRC Does not have access to the study/site OR CRC Does not have 'Data Entry Person' role ***");
            return true;
        }
        return false;
    }

    private Boolean doesStudySubjecAndCRCRolesMatch(String crcUserName, Integer subjectStudyId) {
        boolean found = false;
        IUserAccountDAO udao = this.userAccountDao;
        ArrayList<StudyUserRoleBean> studyUserRoleBeans = (ArrayList<StudyUserRoleBean>) udao.findAllRolesByUserName(crcUserName);
        for (StudyUserRoleBean studyUserRoleBean : studyUserRoleBeans) {

            if (studyUserRoleBean.getStudyId() == getParentStudy(subjectStudyId).getId()) {
                subjectStudyId = getParentStudy(subjectStudyId).getId();
            }

            if ((studyUserRoleBean.getStudyId() == subjectStudyId)
                    && (studyUserRoleBean.getRoleName().equals("ra") || studyUserRoleBean.getRoleName().equals("ra2"))
                    && studyUserRoleBean.getStatus().isAvailable()) {
                found = true;
                break;
            }
        }
        if (!found) {
            logger.info("*** CRC Role does not match with StudySubject assignment ***");
            return true;
        }
        logger.info("*** CRC Role does match with StudySubject assignment ***");
        return false;
    }

    private Boolean isStudySubjecAndCRCRolesMatch(String studySubjectId, String crcUserName, String studyOid) {
        // crc is siteA studySubject is siteA , pass (same site)
        // crc is siteA studySubject is siteB , Fail
        // crc is siteA studySubject is study , Fail

        // crc is study studySubject is siteA , pass
        // crc is study studySubject is siteB , pass
        // crc is study studySubject is study , pass

        StudyBean parentStudy = getParentStudy(studyOid);
        Integer studyIdFromStudyOid = parentStudy.getId();
        StudySubjectBean studySubjectBean = getStudySubject(studySubjectId, parentStudy);
        Integer studyIdFromStudySubjectId = studySubjectBean.getStudyId();

        return doesStudySubjecAndCRCRolesMatch(crcUserName, studyIdFromStudySubjectId);

    }

    private StudyBean getParentStudy(Integer studyId) {
        IStudyDAO sdao = this.studyDao;
        StudyBean study = getStudy(studyId);
        if (study.getParentStudyId() == 0) {
            return study;
        } else {
            StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
            return parentStudy;
        }

    }

    private StudyBean getParentStudy(String studyOid) {
        IStudyDAO sdao = this.studyDao;
        StudyBean study = getStudy(studyOid);
        if (study.getParentStudyId() == 0) {
            return study;
        } else {
            StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
            return parentStudy;
        }

    }

    private boolean mayProceed(String studyOid, StudySubjectBean ssBean) throws Exception {
        boolean accessPermission = false;
        if (ssBean.isActive()) {
            if (mayProceed(studyOid) && ssBean.getStatus() == Status.AVAILABLE) {
                accessPermission = true;
            }
        }
        return accessPermission;
    }

    private boolean mayProceed(String studyOid) throws Exception {
        boolean accessPermission = false;
        StudyBean siteStudy = getStudy(studyOid);
        StudyBean study = getParentStudy(studyOid);
        IStudyParameterValueDAO spvdao = this.studyParameterValueDao;
        StudyParameterValueBean pStatus = spvdao.findByHandleAndStudy(study.getId(), "participantPortal");
        participantPortalRegistrar = new ParticipantPortalRegistrar();
        String pManageStatus = participantPortalRegistrar.getRegistrationStatus(study.getOid()).toString(); // ACTIVE ,
                                                                                                            // PENDING ,
                                                                                                            // INACTIVE
        String participateStatus = pStatus.getValue().toString(); // enabled , disabled
        String studyStatus = study.getStatus().getName().toString(); // available , pending , frozen , locked
        String siteStatus = siteStudy.getStatus().getName().toString(); // available , pending , frozen , locked
        logger.info("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus + "   siteStatus: "
                + siteStatus);
        if (participateStatus.equalsIgnoreCase("enabled") && studyStatus.equalsIgnoreCase("available") && siteStatus.equalsIgnoreCase("available")
                && pManageStatus.equalsIgnoreCase("ACTIVE")) {
            accessPermission = true;
        }

        return accessPermission;
    }

    @RequestMapping(value = "/study/{studyOid}", method = RequestMethod.GET)
    public ResponseEntity<ArrayList<UserDTO>> getAllParticipantPerStudy(@PathVariable("studyOid") String studyOid) throws Exception {
        ResourceBundleProvider.updateLocale(Locale.of("en_US"));
        IUserAccountDAO udao = this.userAccountDao;
        ArrayList<UserDTO> uDTOs = null;

        StudyBean parentStudy = getParentStudy(studyOid);
        String oid = parentStudy.getOid();

        if (isStudyDoesNotExist(oid))
            return new ResponseEntity<ArrayList<UserDTO>>(uDTOs, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        udao = this.userAccountDao;
        ArrayList<UserAccountBean> uBeans = (ArrayList<UserAccountBean>) udao.findAllParticipantsByStudyOid(oid);
        if (uBeans != null) {
            uDTOs = new ArrayList<>();
            for (UserAccountBean uBean : uBeans) {
                UserDTO uDTO = new UserDTO();

                String username = uBean.getName();
                String studySubjectOid = username.substring(username.indexOf(".") + 1);
                IStudySubjectDAO ssdao = this.studySubjectDao;
                String studySubjectId = ssdao.findByOid(studySubjectOid).getLabel();

                uDTO.setfName(uBean.getFirstName());
                uDTO.setMobile(uBean.getPhone());
                uDTO.setAccessCode(uBean.getAccessCode());
                uDTO.setUserName(uBean.getName());
                uDTO.setPassword(uBean.getPasswd());
                uDTO.setlName(uBean.getLastName());
                uDTO.setStudySubjectId(studySubjectId);

                uDTOs.add(uDTO);
            }
            return new ResponseEntity<ArrayList<UserDTO>>(uDTOs, org.springframework.http.HttpStatus.OK);
        } else {
            return new ResponseEntity<ArrayList<UserDTO>>(uDTOs, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ResponseEntity<UserDTO> updateAccount(@RequestBody HashMap<String, String> map) throws Exception {
        uDTO = null;

        StudyBean parentStudy = getParentStudy(map.get("studyOid"));
        String oid = parentStudy.getOid();

        String studySubjectId = map.get("studySubjectId");
        String fName = map.get("fName");
        String lName = map.get("lName");
        String mobile = map.get("mobile");
        String accessCode = map.get("accessCode");
        String crcUserName = map.get("crcUserName");

        ResourceBundleProvider.updateLocale(Locale.of("en_US"));

        UserAccountBean uBean = null;

        StudySubjectBean studySubjectBean = getStudySubject(studySubjectId, parentStudy);
        UserAccountBean ownerUserAccount = getUserAccount(crcUserName);

        // build UserName
        HashMap<String, String> mapValues = buildParticipantUserName(studySubjectBean);
        String pUserName = mapValues.get("pUserName"); // Participant User Name
        String studySubjectOid = mapValues.get("studySubjectOid");
        Integer pStudyId = Integer.valueOf(mapValues.get("pStudyId"));

        // Participant user account create (if does not exist in user table) or Update(if exist in user table)
        uBean = buildUserAccount(oid, studySubjectOid, fName, lName, mobile, accessCode, ownerUserAccount, pUserName);
        UserAccountBean participantUserAccountBean = getUserAccount(pUserName);
        if (!participantUserAccountBean.isActive()) {
            createUserAccount(uBean);
            uBean.setUpdater(uBean.getOwner());
            updateUserAccount(uBean);
            disableUserAccount(uBean);
            logger.info("***New User Account is created***");
            uDTO = buildUserDTO(uBean);
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.OK);

        } else {
            uBean.setId(getUserAccount(uBean.getName()).getId());
            uBean.setUpdater(uBean.getOwner());
            updateUserAccount(uBean);
            logger.info("***User Account already exist in the system and data is been Updated ***");
            uDTO = buildUserDTO(uBean);
            return new ResponseEntity<UserDTO>(uDTO, org.springframework.http.HttpStatus.OK);
        }
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
}
