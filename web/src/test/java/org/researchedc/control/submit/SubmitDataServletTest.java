package org.researchedc.control.submit;

import java.util.Locale;

import org.researchedc.bean.core.Role;
import org.researchedc.bean.login.StudyUserRoleBean;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.i18n.util.ResourceBundleProvider;

import static org.mockito.Mockito.*;

import junit.framework.TestCase;

public class SubmitDataServletTest extends TestCase {

    public SubmitDataServletTest() {
    }

    public static void setup(){
    	ResourceBundleProvider.updateLocale(Locale.of("us"));
    	
    }
    
    // Scenario
    // Data Entry Person (site) can access Subject    

    public void test_SubmitDataServlet_MayViewData() {    	
    	UserAccountBean ub = new UserAccountBean();
    	//StudyUserRoleBean currentRole = new StudyUserRoleBean();
    	//currentRole.setRole(Role.COORDINATOR);
    	
    	StudyUserRoleBean studyUserRoleBeanMock = mock(StudyUserRoleBean.class);

    	// Positive Testing
    	when(studyUserRoleBeanMock.getRole()).thenReturn(Role.RESEARCHASSISTANT);
    	boolean result1 = SubmitDataServlet.mayViewData(ub, studyUserRoleBeanMock);
        assertEquals(true, result1);

        // Positive Testing
    	when(studyUserRoleBeanMock.getRole()).thenReturn(Role.RESEARCHASSISTANT2);
    	boolean result2 = SubmitDataServlet.mayViewData(ub, studyUserRoleBeanMock);
        assertEquals(true, result2);

        // Negative Testing
    	when(studyUserRoleBeanMock.getRole()).thenReturn(Role.ADMIN);
        boolean result3 = SubmitDataServlet.mayViewData(ub, studyUserRoleBeanMock);
        assertEquals(false, result3);
    
        // Negative Testing
    	when(studyUserRoleBeanMock.getRole()).thenReturn(Role.INVALID);
        boolean result4 = SubmitDataServlet.mayViewData(ub, studyUserRoleBeanMock);
        assertEquals(false, result4);
    }
    
    // Scenario
    // Data Entry Person (site) can access Notes & Discrepancies
    public void test_SubmitDataServlet_MaySubmitData() {    	
    	UserAccountBean ub = new UserAccountBean();
    	//StudyUserRoleBean currentRole = new StudyUserRoleBean();
    	//currentRole.setRole(Role.COORDINATOR);
    	
    	StudyUserRoleBean studyUserRoleBeanMock = mock(StudyUserRoleBean.class);

    	// Positive Testing
    	when(studyUserRoleBeanMock.getRole()).thenReturn(Role.RESEARCHASSISTANT);
    	boolean result1 = SubmitDataServlet.maySubmitData(ub, studyUserRoleBeanMock);
        assertEquals(true, result1);

        // Positive Testing
    	when(studyUserRoleBeanMock.getRole()).thenReturn(Role.RESEARCHASSISTANT2);
    	boolean result2 = SubmitDataServlet.maySubmitData(ub, studyUserRoleBeanMock);
        assertEquals(true, result2);

        // Negative Testing
    	when(studyUserRoleBeanMock.getRole()).thenReturn(Role.ADMIN);
        boolean result3 = SubmitDataServlet.maySubmitData(ub, studyUserRoleBeanMock);
        assertEquals(false, result3);
    
        // Negative Testing
    	when(studyUserRoleBeanMock.getRole()).thenReturn(Role.INVALID);
        boolean result4 = SubmitDataServlet.maySubmitData(ub, studyUserRoleBeanMock);
        assertEquals(false, result4);
    }
  



}