/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.bean.login;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.researchedc.bean.core.AuditableEntityBean;
import org.researchedc.bean.core.Role;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.core.UserType;
import org.researchedc.bean.managestudy.StudyBean;

/**
 * @author thickerson
 */
public class UserAccountBean extends AuditableEntityBean {

    /*
     * since we extend entity bean, we already have the following: user_id, user_name, owner_id, date_created, date_updated, update_id
     */

    /**
     * LDAP/Active Directory users are identified by having this password stored in the database
     */
    public static final String LDAP_PASSWORD = "*";

    private String passwd;
    private String firstName;
    private String lastName;
    private String institutionalAffiliation;
    private Date lastVisitDate;
    private Date passwdTimestamp;
    private String passwdChallengeQuestion;
    private String passwdChallengeAnswer;
    private String phone;
    private Boolean enabled;
    private Boolean accountNonLocked;
    private Integer lockCounter;
    private Boolean runWebservices;
    private String accessCode;
    private String time_zone;
    private boolean enableApiKey;
    private String apiKey;

    /**
     * Counts the number of times the user visited Main Menu servlet.
     */
    private int numVisitsToMainMenu;

    private int activeStudyId;
    // private Study activeStudy;

    //
    // the following invariant is maintained at all times:
    // there is at most one object in userTypes
    // all elements of userTypes are UserType objects
    // userTypes has a UserType.SYS_ADMIN object IFF sysAdmin == true
    //
    // we store the userType as an ArrayList for forward compatibility
    // ie it may be possible for a user to have multiple usertypes in the future
    // we maintain the sysAdmin flag to speed up isSysAdmin queries
    //
    private boolean sysAdmin; // this is true if the user is the business
    // dmin, false otherwise
    private boolean techAdmin;
    private final ArrayList userTypes;

    //
    // the following invariant is maintained at all times:
    // all elements of roles are StudyUserRoleBean objects
    // if there is a StudyUserRoleBean object s which is at index i of roles,
    // then rolesByStudy has a key which is an Integer whose intValue is
    // s.getStudyId
    // and the value of that key is an Integer whose intValue is i
    // in other words, rolesByStudy is a hashmap whose keys are studyIds and
    // whose values
    // are indexes of the corresponding StudyUserRoleBean in roles
    //
    // we maintain rolesByStudy to speed up getRoleByStudy queries
    //

    // elements are StudyUserRoleBeans
    private ArrayList roles = new ArrayList();

    // key is Integer whose intValue is a studyId, value is StudyUserRoleBean
    // for that study
    private final HashMap rolesByStudy = new HashMap();

    private String notes; // not in the DB, only for showing some notes for

    // this acocunt on page

    public UserAccountBean() {
        super();

        passwd = "";
        firstName = "";
        lastName = "";
        institutionalAffiliation = "";
        lastVisitDate = new Date(0);
        passwdTimestamp = new Date(0);
        passwdChallengeQuestion = "";
        passwdChallengeAnswer = "";
        phone = "";
        sysAdmin = false;
        techAdmin = false;

        userTypes = new ArrayList();
        status = Status.AVAILABLE;
        numVisitsToMainMenu = 0;
        notes = "";
        enabled = true;
        accountNonLocked = true;
        lockCounter = 0;
        runWebservices = false;
        accessCode="";
        enableApiKey=false;
        apiKey="";
    }

    /**
     * @return Returns the firstName.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName
     *            The firstName to set.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return Returns the institutionalAffiliation.
     */
    public String getInstitutionalAffiliation() {
        return institutionalAffiliation;
    }

    /**
     * @param institutionalAffiliation
     *            The institutionalAffiliation to set.
     */
    public void setInstitutionalAffiliation(String institutionalAffiliation) {
        this.institutionalAffiliation = institutionalAffiliation;
    }

    /**
     * @return Returns the lastName.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName
     *            The lastName to set.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return Returns the lastVisitDate.
     */
    public Date getLastVisitDate() {
        return lastVisitDate;
    }

    /**
     * @param lastVisitDate
     *            The lastVisitDate to set.
     */
    public void setLastVisitDate(Date lastVisitDate) {
        this.lastVisitDate = lastVisitDate;
    }

    /**
     * @return Returns the passwd.
     */
    public String getPasswd() {
        return passwd;
    }

    /**
     * @param passwd
     *            The passwd to set.
     */
    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    /**
     * @return Returns the passwdChallengeAnswer.
     */
    public String getPasswdChallengeAnswer() {
        return passwdChallengeAnswer;
    }

    /**
     * @param passwdChallengeAnswer
     *            The passwdChallengeAnswer to set.
     */
    public void setPasswdChallengeAnswer(String passwdChallengeAnswer) {
        this.passwdChallengeAnswer = passwdChallengeAnswer;
    }

    /**
     * @return Returns the passwdChallengeQuestion.
     */
    public String getPasswdChallengeQuestion() {
        return passwdChallengeQuestion;
    }

    /**
     * @param passwdChallengeQuestion
     *            The passwdChallengeQuestion to set.
     */
    public void setPasswdChallengeQuestion(String passwdChallengeQuestion) {
        this.passwdChallengeQuestion = passwdChallengeQuestion;
    }

    /**
     * @return Returns the passwdTimestamp.
     */
    public Date getPasswdTimestamp() {
        return passwdTimestamp;
    }

    /**
     * @param passwdTimestamp
     *            The passwdTimestamp to set.
     */
    public void setPasswdTimestamp(Date passwdTimestamp) {
        this.passwdTimestamp = passwdTimestamp;
    }

    /**
     * @return Returns the phone.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone
     *            The phone to set.
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(Boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public Integer getLockCounter() {
        return lockCounter;
    }

    public void setLockCounter(Integer lockCounter) {
        this.lockCounter = lockCounter;
    }

    // ///////////////////////
    // SECURITY MODEL CODE //
    // ///////////////////////

    public void addUserType(UserType u) {
        if (userTypes.size() > 0) {
            userTypes.clear();
        }
        userTypes.add(u);
        if (u.equals(UserType.USER)) {
            sysAdmin = false;
            techAdmin = false;
        }
        if (u.equals(UserType.SYSADMIN)) {
            sysAdmin = true;
        }
        if (u.equals(UserType.TECHADMIN)) {
            sysAdmin = true;
            techAdmin = true;
        }
    }

    /**
     * @return Returns the sysAdmin.
     */
    public boolean isSysAdmin() {
        return sysAdmin;
    }

    /**
     * @return Returns the activeStudyId.
     */
    public int getActiveStudyId() {
        return activeStudyId;
    }

    /**
     * @param activeStudyId
     *            The activeStudyId to set.
     */
    public void setActiveStudyId(int activeStudyId) {
        this.activeStudyId = activeStudyId;
    }

    public StudyUserRoleBean getRoleByStudy(StudyBean study) {
        return getRoleByStudy(study.getId());
    }

    public StudyUserRoleBean getRoleByStudy(int studyId) {
        Integer key = Integer.valueOf(studyId);

        if (rolesByStudy.containsKey(key)) {
            Integer index = (Integer) rolesByStudy.get(key);
            StudyUserRoleBean s = (StudyUserRoleBean) roles.get(index.intValue());

            if (s != null && !s.getStatus().equals(Status.DELETED) && !s.getStatus().equals(Status.AUTO_DELETED)) {
                return s;
            }
        }

        return new StudyUserRoleBean();
    }

    // public boolean hasPrivilege(Privilege p) {
    // boolean returnMe = false;
    // Iterator it = userPrivileges.iterator();
    // while (it.hasNext()) {
    // Privilege myPriv = (Privilege)it.next();
    // if (myPriv.equals(p)) {
    // returnMe = true;
    // }
    // } // end of iterator
    // return returnMe;
    // }
    //
    // public Privilege getPrivilege(Privilege p) {
    // if (this.hasPrivilege(p)) {
    // return p;
    // }
    // else {
    // return Privilege.get(0);
    // }
    // }

    /**
     * @return Returns the notes.
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param notes
     *            The notes to set.
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getRunWebservices() {
        return runWebservices;
    }

    public void setRunWebservices(Boolean runWebservices) {
        this.runWebservices = runWebservices;
    }

	public String getAccessCode() {
		return accessCode;
	}

	public void setAccessCode(String accessCode) {
		this.accessCode = accessCode;
	}

	public String getTime_zone() {
		return time_zone;
	}

	public void setTime_zone(String time_zone) {
		this.time_zone = time_zone;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}


}