package org.researchedc.dao.spi;

import org.researchedc.bean.core.EntityBean;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.researchedc.bean.submit.ItemFormMetadataBean;
import org.researchedc.bean.submit.ResponseSetBean;
import org.researchedc.domain.crfdata.InstantOnChangePairContainer;
import org.researchedc.exception.OpenClinicaException;

public interface IItemFormMetadataDAO {

    public ArrayList<ItemFormMetadataBean> findByMultiplePKs(ArrayList ints) throws org.researchedc.exception.OpenClinicaException;

    public Object getEntityFromHashMap(HashMap hm);

    public void setTypesExpected();

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws org.researchedc.exception.OpenClinicaException;

    public Collection<ItemFormMetadataBean> findAll() throws org.researchedc.exception.OpenClinicaException;

    public int findCountAllHiddenByCRFVersionId(int crfVersionId);

    public int findCountAllHiddenButShownByEventCRFId(int eventCrfId);

    public ArrayList<ItemFormMetadataBean> findAllByCRFVersionId(int crfVersionId) throws org.researchedc.exception.OpenClinicaException;

    public ArrayList<ItemFormMetadataBean> findAllItemsRequiredAndShownByCrfVersionId(int crfVersionId);

    public ArrayList<ItemFormMetadataBean> findAllItemsRequiredAndHiddenByCrfVersionId(int crfVersionId);

    public ArrayList<ItemFormMetadataBean> findAllByCRFIdItemIdAndHasValidations(int crfId, int itemId);

    public ArrayList<ItemFormMetadataBean> findAllByCRFVersionIdAndResponseTypeId(int crfVersionId, int responseTypeId) throws org.researchedc.exception.OpenClinicaException;

    public ArrayList<ItemFormMetadataBean> findAllByItemId(int itemId);

    public ArrayList<ItemFormMetadataBean> findAllByItemIdAndHasValidations(int itemId);

    public ArrayList<ItemFormMetadataBean> findAllBySectionId(int sectionId) throws org.researchedc.exception.OpenClinicaException;

    public ArrayList<ItemFormMetadataBean> findAllByCRFVersionIdAndSectionId(int crfVersionId, int sectionId) throws org.researchedc.exception.OpenClinicaException;

    public EntityBean findByPK(int id) throws org.researchedc.exception.OpenClinicaException;

    public EntityBean create(EntityBean eb) throws org.researchedc.exception.OpenClinicaException;

    public EntityBean update(EntityBean eb) throws org.researchedc.exception.OpenClinicaException;

    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws org.researchedc.exception.OpenClinicaException;

    public Collection findAllByPermission(Object objCurrentUser, int intActionType) throws org.researchedc.exception.OpenClinicaException;

    public ItemFormMetadataBean findByItemIdAndCRFVersionId(int itemId, int crfVersionId);

    public ItemFormMetadataBean findByItemIdAndCRFVersionIdNotInIGM(int itemId, int crfVersionId);

    public ResponseSetBean findResponseSetByPK(int id);

    public ArrayList<ItemFormMetadataBean> findSCDItemsBySectionId(Integer sectionId);

    public int findMaxId();

    public boolean instantTypeExistsInSection(int sectionId);

    public Map<Integer,List<InstantOnChangePairContainer>> sectionInstantMapInSameSection(int crfVersionId);

}
