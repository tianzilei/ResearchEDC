package org.researchedc.module.export.internal.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.researchedc.module.crf.entity.CrfEntity;
import org.researchedc.module.crf.entity.ItemEntity;
import org.researchedc.module.crf.repository.CrfRepository;
import org.researchedc.module.crf.repository.ItemRepository;
import org.researchedc.module.datacapture.entity.ItemDataEntity;
import org.researchedc.module.datacapture.entity.ItemGroupEntity;
import org.researchedc.module.datacapture.repository.ItemDataRepository;
import org.researchedc.module.datacapture.repository.ItemGroupRepository;
import org.researchedc.module.event.entity.EventCrfEntity;
import org.researchedc.module.event.entity.StudyEventDefinitionEntity;
import org.researchedc.module.event.entity.StudyEventEntity;
import org.researchedc.module.event.repository.EventCrfRepository;
import org.researchedc.module.event.repository.StudyEventDefinitionRepository;
import org.researchedc.module.event.repository.StudyEventRepository;
import org.researchedc.module.export.enums.OdmContractVersion;
import org.researchedc.module.export.internal.ExportDataProvider;
import org.researchedc.module.export.internal.OdmFormDataSnapshot;
import org.researchedc.module.export.internal.OdmItemDataSnapshot;
import org.researchedc.module.export.internal.OdmItemGroupDataSnapshot;
import org.researchedc.module.export.internal.OdmStudyEventDataSnapshot;
import org.researchedc.module.export.internal.OdmStudySnapshot;
import org.researchedc.module.export.internal.OdmSubjectDataSnapshot;
import org.researchedc.module.study.entity.StudyEntity;
import org.researchedc.module.study.repository.StudyRepository;
import org.researchedc.module.subject.entity.StudySubjectEntity;
import org.researchedc.module.subject.repository.StudySubjectRepository;
import org.springframework.stereotype.Component;

@Component
public class ExportDataProviderAdapter implements ExportDataProvider {

    private final StudyRepository studyRepository;
    private final StudySubjectRepository studySubjectRepository;
    private final StudyEventRepository studyEventRepository;
    private final StudyEventDefinitionRepository studyEventDefinitionRepository;
    private final EventCrfRepository eventCrfRepository;
    private final ItemDataRepository itemDataRepository;
    private final ItemGroupRepository itemGroupRepository;
    private final CrfRepository crfRepository;
    private final ItemRepository itemRepository;

    public ExportDataProviderAdapter(StudyRepository studyRepository,
                                      StudySubjectRepository studySubjectRepository,
                                      StudyEventRepository studyEventRepository,
                                      StudyEventDefinitionRepository studyEventDefinitionRepository,
                                      EventCrfRepository eventCrfRepository,
                                      ItemDataRepository itemDataRepository,
                                      ItemGroupRepository itemGroupRepository,
                                      CrfRepository crfRepository,
                                      ItemRepository itemRepository) {
        this.studyRepository = studyRepository;
        this.studySubjectRepository = studySubjectRepository;
        this.studyEventRepository = studyEventRepository;
        this.studyEventDefinitionRepository = studyEventDefinitionRepository;
        this.eventCrfRepository = eventCrfRepository;
        this.itemDataRepository = itemDataRepository;
        this.itemGroupRepository = itemGroupRepository;
        this.crfRepository = crfRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public OdmStudySnapshot getStudySnapshot(Integer studyId, OdmContractVersion contractVersion) {
        StudyEntity study = studyRepository.findById(studyId)
                .orElseThrow(() -> new IllegalArgumentException("Study not found: " + studyId));

        String facilityEmail = null;
        if (contractVersion == OdmContractVersion.OC2_0_COMPAT) {
            facilityEmail = study.getFacilityContactPhone() != null
                    ? study.getFacilityContactPhone() : "";
        }

        return new OdmStudySnapshot(
                study.getOcOid() != null ? study.getOcOid() : "S_" + studyId,
                study.getName(),
                study.getSummary(),
                study.getProtocolDescription() != null ? study.getProtocolDescription() : study.getName(),
                study.getFacilityName(),
                facilityEmail,
                "mdv_" + studyId + "_1",
                study.getName() + " MetaDataVersion"
        );
    }

    @Override
    public List<OdmSubjectDataSnapshot> getSubjectData(Integer studyId) {
        List<StudySubjectEntity> studySubjects = studySubjectRepository.findByStudyId(studyId);
        if (studySubjects.isEmpty()) {
            return Collections.emptyList();
        }

        List<StudyEventDefinitionEntity> allDefs = studyEventDefinitionRepository.findByStudyIdOrderByName(studyId);
        Map<Integer, StudyEventDefinitionEntity> defsById = allDefs.stream()
                .collect(Collectors.toMap(StudyEventDefinitionEntity::getStudyEventDefinitionId, d -> d));

        List<OdmSubjectDataSnapshot> result = new ArrayList<>();
        for (StudySubjectEntity ss : studySubjects) {
            List<StudyEventEntity> subjectEvents =
                    studyEventRepository.findByStudySubjectId(ss.getStudySubjectId());
            List<OdmStudyEventDataSnapshot> eventSnapshots = new ArrayList<>();

            for (StudyEventEntity event : subjectEvents) {
                StudyEventDefinitionEntity def = defsById.get(event.getStudyEventDefinitionId());
                String eventOid = def != null && def.getOcOid() != null
                        ? def.getOcOid() : "SE_" + event.getStudyEventId();

                List<EventCrfEntity> eventCrfs =
                        eventCrfRepository.findByStudyEventId(event.getStudyEventId());
                List<OdmFormDataSnapshot> formSnapshots = new ArrayList<>();

                for (EventCrfEntity ecrf : eventCrfs) {
                    List<ItemDataEntity> crfItems =
                            itemDataRepository.findByEventCrfId(ecrf.getEventCrfId());
                    if (crfItems.isEmpty()) {
                        formSnapshots.add(new OdmFormDataSnapshot(
                                "F_" + ecrf.getEventCrfId(), null, Collections.emptyList()));
                        continue;
                    }

                    List<ItemEntity> items = itemRepository.findAllById(
                            crfItems.stream().map(ItemDataEntity::getItemId).distinct().toList());
                    Map<Integer, ItemEntity> itemsById = items.stream()
                            .collect(Collectors.toMap(ItemEntity::getItemId, i -> i));

                    List<OdmItemDataSnapshot> itemSnapshots = new ArrayList<>();
                    for (ItemDataEntity id : crfItems) {
                        ItemEntity item = itemsById.get(id.getItemId());
                        String itemOid = item != null && item.getOcOid() != null
                                ? item.getOcOid() : "I_" + id.getItemId();
                        itemSnapshots.add(new OdmItemDataSnapshot(itemOid, id.getValue(), false));
                    }

                    formSnapshots.add(new OdmFormDataSnapshot(
                            "F_" + ecrf.getEventCrfId(), null,
                            List.of(new OdmItemGroupDataSnapshot("IG_DEFAULT", null, itemSnapshots))));
                }

                eventSnapshots.add(new OdmStudyEventDataSnapshot(eventOid, null, formSnapshots));
            }

            String subjectKey = ss.getOcOid() != null ? ss.getOcOid() : "SS_" + ss.getStudySubjectId();
            result.add(new OdmSubjectDataSnapshot(subjectKey, ss.getLabel(), eventSnapshots));
        }

        return result;
    }
}
