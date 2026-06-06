package org.researchedc.control.submit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import jakarta.servlet.http.HttpServletResponse;

import org.researchedc.dao.spi.SubjectGroupMapDao;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.login.StudyUserRoleBean;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.bean.managestudy.StudyGroupClassBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.control.AbstractTableFactory;
import org.researchedc.control.DefaultActionsEditor;

import org.researchedc.control.submit.ListDiscNotesSubjectTableFactory.StatusFilterMatcher;
import org.researchedc.control.submit.ListDiscNotesSubjectTableFactory.SubjectEventStatusFilterMatcher;

import org.researchedc.dao.spi.IDiscrepancyNoteDAO;
import org.researchedc.dao.spi.EventDefinitionCRFDao;
import org.researchedc.dao.spi.StudyGroupClassDao;
import org.researchedc.dao.spi.StudyGroupDao;
import org.researchedc.dao.managestudy.ListDiscNotesSubjectFilter;
import org.researchedc.dao.managestudy.ListDiscNotesSubjectSort;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.ISubjectDAO;
import org.researchedc.i18n.util.ResourceBundleProvider;
import org.jmesa.core.filter.MatcherKey;
import org.jmesa.facade.TableFacade;
import org.jmesa.limit.Filter;
import org.jmesa.limit.FilterSet;
import org.jmesa.limit.Limit;
import org.jmesa.limit.Sort;
import org.jmesa.limit.SortSet;
import org.jmesa.view.component.Row;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * 
 * @author jnyayapathi
 *
 */
public class DiscNotesSubjectStatisticsFactory extends AbstractTableFactory{
	private String[] columnNames = new String[] {};
    private IStudyEventDefinitionDAO studyEventDefinitionDao;
    private IStudySubjectDAO studySubjectDAO;
    private ISubjectDAO subjectDAO;
    private IStudyEventDAO studyEventDAO;
    private StudyGroupClassDao studyGroupClassDAO;
    private SubjectGroupMapDao subjectGroupMapDAO;
    private StudyGroupDao studyGroupDAO;
    private IStudyDAO studyDAO;
    private EventCRFDao eventCRFDAO;
    private EventDefinitionCRFDao eventDefintionCRFDAO;
    private IDiscrepancyNoteDAO discrepancyNoteDAO;
    private StudyBean studyBean;

    private ArrayList<StudyEventDefinitionBean> studyEventDefinitions;
    private ArrayList<StudyGroupClassBean> studyGroupClasses;
    private StudyUserRoleBean currentRole;
    private UserAccountBean currentUser;
    private ResourceBundle resword;
    private ResourceBundle resformat;
    private ResourceBundle resterm;
    private String module;
    private Integer resolutionStatus;
    private Integer discNoteType;
    private Boolean studyHasDiscNotes;
    private Set<Integer> resolutionStatusIds;
    private Map<Object,Map> discrepancyMap;

	@Override
	protected String getTableName() {
		return "discNotesSummary";
	}

	@Override
	protected void configureColumns(TableFacade tableFacade, Locale locale) {
		//resword = ResourceBundleProvider.getWordsBundle(locale);
        //resformat = ResourceBundleProvider.getFormatBundle(locale);
        tableFacade.setColumnProperties(columnNames);
        Row row = tableFacade.getTable().getRow();
        HashMap<Object,Map> items = (HashMap<Object,Map>) getDiscrepancyMap();
        Set theKeys  = items.keySet();
        Iterator theKeysItr = theKeys.iterator();
        configureColumn(row.getColumn(columnNames[0]), "_", null, null);
        configureColumn(row.getColumn(columnNames[1]), theKeysItr.next().toString(), null, null);
        configureColumn(row.getColumn(columnNames[2]), theKeysItr.next().toString(), null, null);
        configureColumn(row.getColumn(columnNames[3]), theKeysItr.next().toString(), null, null);
        configureColumn(row.getColumn(columnNames[4]), theKeysItr.next().toString(), null, null);
        configureColumn(row.getColumn(columnNames[5]), "Totals", null, null);
        // study event definition columns
      
        //String actionsHeader = resword.getString("rule_actions") + "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;";
        //configureColumn(row.getColumn(columnNames[columnNames.length - 1]), "Summary Statistics", null, null, false,
         //       false);					
	}

	 @Override
	    public void configureTableFacade(HttpServletResponse response, TableFacade tableFacade) {
	        super.configureTableFacade(response, tableFacade);
	        
	        getColumnNamesMap();
	       // tableFacade.addFilterMatcher(new MatcherKey(Status.class), new StatusFilterMatcher());
	        // tableFacade.addFilterMatcher(new MatcherKey(Integer.class), new
	        // SubjectEventStatusFilterMatcher());

	       

	    }
	@Override
	public void setDataAndLimitVariables(TableFacade tableFacade) {
        StudyBean study = this.getStudyBean();
        Limit limit = tableFacade.getLimit();

        ListDiscNotesSubjectFilter subjectFilter = getSubjectFilter(limit);
       // subjectFilter.addFilter("dn.discrepancy_note_type_id", this.discNoteType);
        StringBuffer constraints = new StringBuffer();
        /*  if (this.discNoteType > 0 && this.discNoteType < 10) {
            constraints.append(" and dn.discrepancy_note_type_id=" + this.discNoteType);
        }
        if (this.resolutionStatusIds != null && this.resolutionStatusIds.size() > 0) {
            String s = " and (";
            for (Integer resolutionStatusId : this.resolutionStatusIds) {
                s += "dn.resolution_status_id = " + resolutionStatusId + " or ";
            }
            s = s.substring(0, s.length() - 3) + " )";
            subjectFilter.addFilter("dn.resolution_status_id", s);
            constraints.append(s);
        }
*/
        if (!limit.isComplete()) {
//            int totalRows = getStudySubjectDAO().getCountWithFilter(subjectFilter, study);
            tableFacade.setTotalRows(6);
        }


        int rowStart = limit.getRowSelect().getRowStart();
        int rowEnd = 6;

        ListDiscNotesSubjectSort subjectSort = getSubjectSort(limit);
        HashMap<Object,Map> items = (HashMap<Object,Map>) getDiscrepancyMap();

        Collection<HashMap<Object, Object>> theItems = new ArrayList<HashMap<Object, Object>>();
        Collection<HashMap<Object, Object>> theItemsKeys = new ArrayList<HashMap<Object, Object>>();
        Collection<HashMap<Object, Object>> theItemsVals = new ArrayList<HashMap<Object, Object>>();
        Iterator keyIt = null;
        if(items.values().iterator().hasNext())
        keyIt = items.values().iterator().next().keySet().iterator();
        HashMap<Object, Object> theItem = new HashMap();

   	 Set theKeys  = items.keySet();
   
   	 
   	 
   	 List<Object> existingKey = new ArrayList();
   	Iterator theKeysItr = theKeys.iterator();
   	while(keyIt.hasNext())   
   	{
     	 String key = "",val = "";	

	       key = keyIt.next().toString();
	     //  val=keyIt.ne.toString();
//	if(!existingKey.contains(key))
//		{
//		existingKey.add(key);
//		break;
//		}
//	else
//	{
//		key = keyIt.next().toString();
//		// val=firstVals.get(key).toString();
//		existingKey.add( key);
//		break;
//	}
 	
   		
   		for(Map<String,String[]> firstVals:items.values())
        {
        	
   			
   			
   			
   			
   			theItem = new HashMap();
            Iterator it = firstVals.values().iterator();
        	// keyIt = firstVals.keySet().iterator();
            String label = (String)theKeysItr.next();
        	
          	
 
              
          
               while(it.hasNext())
                    {
                	    
                	theItem.put("_", key);
                	theItem.put(label, it.next());
                	
                    }
                	
                
           	 theItems.add(theItem);
  
        }
        

       
        theItemsVals.addAll(theItems);
  
		
		tableFacade.setItems(theItemsVals);
	}
	}
	   private void getColumnNamesMap() {
	        ArrayList<String> columnNamesList = new ArrayList<String>();
	        HashMap<Object,Map> items = (HashMap<Object,Map>) getDiscrepancyMap();
	        Set theKeys  = items.keySet();
	        Iterator theKeysItr = theKeys.iterator();
	        columnNamesList.add("_");
	        columnNamesList.add(theKeysItr.next().toString());
	        columnNamesList.add(theKeysItr.next().toString());
	        
	        columnNamesList.add(theKeysItr.next().toString());
	        columnNamesList.add(theKeysItr.next().toString());
	        
	        columnNamesList.add("Totals");
	        columnNames = columnNamesList.toArray(columnNames);
	    }
	  
	 protected ListDiscNotesSubjectSort getSubjectSort(Limit limit) {
	        ListDiscNotesSubjectSort listDiscNotesSubjectSort = new ListDiscNotesSubjectSort();
	        SortSet sortSet = limit.getSortSet();
	        Collection<Sort> sorts = sortSet.getSorts();
	        for (Sort sort : sorts) {
	            String property = sort.getProperty();
	            String order = sort.getOrder().toParam();
	            listDiscNotesSubjectSort.addSort(property, order);
	        }

	        return listDiscNotesSubjectSort;
	    }

	
	
	
	   protected ListDiscNotesSubjectFilter getSubjectFilter(Limit limit) {
	        ListDiscNotesSubjectFilter listDiscNotesSubjectFilter = new ListDiscNotesSubjectFilter();
	        FilterSet filterSet = limit.getFilterSet();
	        Collection<Filter> filters = filterSet.getFilters();
	        for (Filter filter : filters) {
	            String property = filter.getProperty();
	            String value = filter.getValue();
	            listDiscNotesSubjectFilter.addFilter(property, value);
	        }

	        return listDiscNotesSubjectFilter;
	    }
	
	
	
	
		public Map<Object, Map> getDiscrepancyMap() {
			return discrepancyMap;
		}

		public void setDiscrepancyMap(Map<Object, Map> discrepancyMap) {
			this.discrepancyMap = discrepancyMap;
		}

	   public IStudyEventDefinitionDAO getStudyEventDefinitionDao() {
	        return studyEventDefinitionDao;
	    }

	    public void setStudyEventDefinitionDao(IStudyEventDefinitionDAO studyEventDefinitionDao) {
	        this.studyEventDefinitionDao = studyEventDefinitionDao;
	    }

	    public StudyBean getStudyBean() {
	        return studyBean;
	    }

	    public void setStudyBean(StudyBean studyBean) {
	        this.studyBean = studyBean;
	    }

	    public IStudySubjectDAO getStudySubjectDAO() {
	        return studySubjectDAO;
	    }

	    public void setStudySubjectDAO(IStudySubjectDAO studySubjectDAO) {
	        this.studySubjectDAO = studySubjectDAO;
	    }

	    public ISubjectDAO getSubjectDAO() {
	        return subjectDAO;
	    }

	    public void setSubjectDAO(ISubjectDAO subjectDAO) {
	        this.subjectDAO = subjectDAO;
	    }

	    public IStudyEventDAO getStudyEventDAO() {
	        return studyEventDAO;
	    }

	    public void setStudyEventDAO(IStudyEventDAO studyEventDAO) {
	        this.studyEventDAO = studyEventDAO;
	    }

	    public StudyGroupClassDao getStudyGroupClassDAO() {
	        return studyGroupClassDAO;
	    }

	    public void setStudyGroupClassDAO(StudyGroupClassDao studyGroupClassDAO) {
	        this.studyGroupClassDAO = studyGroupClassDAO;
	    }

	    public SubjectGroupMapDao getSubjectGroupMapDAO() {
	        return subjectGroupMapDAO;
	    }

	    public void setSubjectGroupMapDAO(SubjectGroupMapDao subjectGroupMapDAO) {
	        this.subjectGroupMapDAO = subjectGroupMapDAO;
	    }

	    public IStudyDAO getStudyDAO() {
	        return studyDAO;
	    }

	    public void setStudyDAO(IStudyDAO studyDAO) {
	        this.studyDAO = studyDAO;
	    }

	    public StudyUserRoleBean getCurrentRole() {
	        return currentRole;
	    }

	    public void setCurrentRole(StudyUserRoleBean currentRole) {
	        this.currentRole = currentRole;
	    }

	    public EventCRFDao getEventCRFDAO() {
	        return eventCRFDAO;
	    }

	    public void setEventCRFDAO(EventCRFDao eventCRFDAO) {
	        this.eventCRFDAO = eventCRFDAO;
	    }

	    public EventDefinitionCRFDao getEventDefintionCRFDAO() {
	        return eventDefintionCRFDAO;
	    }

	    public void setEventDefintionCRFDAO(EventDefinitionCRFDao eventDefintionCRFDAO) {
	        this.eventDefintionCRFDAO = eventDefintionCRFDAO;
	    }

	    public StudyGroupDao getStudyGroupDAO() {
	        return studyGroupDAO;
	    }

	    public void setStudyGroupDAO(StudyGroupDao studyGroupDAO) {
	        this.studyGroupDAO = studyGroupDAO;
	    }

	    public IDiscrepancyNoteDAO getDiscrepancyNoteDAO() {
	        return discrepancyNoteDAO;
	    }

	    public void setDiscrepancyNoteDAO(IDiscrepancyNoteDAO discrepancyNoteDAO) {
	        this.discrepancyNoteDAO = discrepancyNoteDAO;
	    }

	    public ResourceBundle getResword() {
	        return resword;
	    }

	    public void setResword(ResourceBundle resword) {
	        this.resword = resword;
	    }

	    public ResourceBundle getResterm() {
	        return resterm;
	    }

	    public void setResterm(ResourceBundle resterm) {
	        this.resterm = resterm;
	    }

	    public String getModule() {
	        return module;
	    }

	    public void setModule(String module) {
	        this.module = module;
	    }

	    public UserAccountBean getCurrentUser() {
	        return currentUser;
	    }

	    public Integer getResolutionStatus() {
	        return resolutionStatus;
	    }

	    public void setResolutionStatus(Integer resolutionStatus) {
	        this.resolutionStatus = resolutionStatus;
	    }

	    public Integer getDiscNoteType() {
	        return discNoteType;
	    }

	    public void setDiscNoteType(Integer discNoteType) {
	        this.discNoteType = discNoteType;
	    }

	    public Boolean isStudyHasDiscNotes() {
	        return studyHasDiscNotes;
	    }

	    public void setStudyHasDiscNotes(Boolean studyHasDiscNotes) {
	        this.studyHasDiscNotes = studyHasDiscNotes;
	    }

	    public void setCurrentUser(UserAccountBean currentUser) {
	        this.currentUser = currentUser;
	    }

}
