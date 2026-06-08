package org.researchedc.module.openrosa.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.researchedc.module.crf.entity.CrfVersionEntity;
import org.researchedc.module.crf.entity.ItemEntity;
import org.researchedc.module.crf.repository.CrfVersionRepository;
import org.researchedc.module.crf.repository.ItemRepository;
import org.researchedc.module.datacapture.entity.ItemDataEntity;
import org.researchedc.module.datacapture.repository.ItemDataRepository;
import org.researchedc.module.event.entity.EventCrfEntity;
import org.researchedc.module.event.entity.StudyEventDefinitionEntity;
import org.researchedc.module.event.entity.StudyEventEntity;
import org.researchedc.module.event.repository.EventCrfRepository;
import org.researchedc.module.event.repository.StudyEventDefinitionRepository;
import org.researchedc.module.event.repository.StudyEventRepository;
import org.researchedc.module.study.entity.StudyEntity;
import org.researchedc.module.study.repository.StudyRepository;
import org.researchedc.module.subject.entity.StudySubjectEntity;
import org.researchedc.module.subject.repository.StudySubjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class DataPersistProcessor implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(DataPersistProcessor.class);

    private static final int STATUS_DATA_ENTRY_STARTED = 1;
    private static final int STATUS_AVAILABLE = 1;

    private final StudyRepository studyRepository;
    private final StudySubjectRepository studySubjectRepository;
    private final StudyEventDefinitionRepository studyEventDefinitionRepository;
    private final StudyEventRepository studyEventRepository;
    private final EventCrfRepository eventCrfRepository;
    private final ItemDataRepository itemDataRepository;
    private final ItemRepository itemRepository;
    private final CrfVersionRepository crfVersionRepository;

    public DataPersistProcessor(StudyRepository studyRepository,
                                StudySubjectRepository studySubjectRepository,
                                StudyEventDefinitionRepository studyEventDefinitionRepository,
                                StudyEventRepository studyEventRepository,
                                EventCrfRepository eventCrfRepository,
                                ItemDataRepository itemDataRepository,
                                ItemRepository itemRepository,
                                CrfVersionRepository crfVersionRepository) {
        this.studyRepository = studyRepository;
        this.studySubjectRepository = studySubjectRepository;
        this.studyEventDefinitionRepository = studyEventDefinitionRepository;
        this.studyEventRepository = studyEventRepository;
        this.eventCrfRepository = eventCrfRepository;
        this.itemDataRepository = itemDataRepository;
        this.itemRepository = itemRepository;
        this.crfVersionRepository = crfVersionRepository;
    }

    @Override
    @Transactional
    public void process(SubmissionContext ctx) {
        if (ctx.hasErrors()) {
            logger.warn("Skipping persist — context has {} validation errors", ctx.getErrors().size());
            return;
        }

        Map<String, String> ctxMap = ctx.getSubjectContextMap();
        String studyOid = ctxMap.get("studyOid");
        String studySubjectOid = ctxMap.get("studySubjectOid");
        String sedOid = ctxMap.get("studyEventDefinitionId");
        String sampleOrdinalStr = ctxMap.get("studyEventOrdinal");
        String crfVersionOid = ctxMap.get("crfVersionOid");

        if (crfVersionOid == null) {
            ctx.addError("Missing crfVersionOid in submission context");
            return;
        }

        StudyEntity study = resolveStudy(studyOid, ctx);
        if (study == null) return;
        ctx.setStudy(study);

        StudySubjectEntity subject = resolveSubject(studySubjectOid, ctx);
        if (subject == null) return;
        ctx.setSubject(subject);

        StudyEventDefinitionEntity sed = resolveEventDefinition(sedOid, study.getStudyId(), ctx);
        if (sed == null) return;

        CrfVersionEntity crfVersion = resolveCrfVersion(crfVersionOid, ctx);
        if (crfVersion == null) return;
        ctx.setCrfVersion(crfVersion);

        int sampleOrdinal = parseSampleOrdinal(sampleOrdinalStr);

        StudyEventEntity studyEvent = findOrCreateStudyEvent(
                subject.getStudySubjectId(), sed.getStudyEventDefinitionId(), sampleOrdinal, ctx);
        if (studyEvent == null) return;
        ctx.setStudyEvent(studyEvent);

        EventCrfEntity eventCrf = findOrCreateEventCrf(
                studyEvent.getStudyEventId(), subject.getStudySubjectId(),
                crfVersion.getCrfVersionId(), ctx);
        ctx.setEventCrf(eventCrf);

        createItemDataRecords(eventCrf.getEventCrfId(), ctx);

        logger.info("Submission persisted: studyEventId={}, eventCrfId={}, itemCount={}",
                studyEvent.getStudyEventId(), eventCrf.getEventCrfId(), ctx.getItems().size());
    }

    private StudyEntity resolveStudy(String studyOid, SubmissionContext ctx) {
        if (studyOid == null) return ctx.getStudy();
        return studyRepository.findByOcOid(studyOid).orElse(null);
    }

    private StudySubjectEntity resolveSubject(String ocOid, SubmissionContext ctx) {
        if (ocOid == null) return ctx.getSubject();
        return studySubjectRepository.findByOcOid(ocOid).orElse(null);
    }

    private StudyEventDefinitionEntity resolveEventDefinition(String ocOid, Integer studyId,
                                                                SubmissionContext ctx) {
        if (ocOid == null || studyId == null) return null;
        return studyEventDefinitionRepository.findByOcOidAndStudyId(ocOid, studyId).orElse(null);
    }

    private CrfVersionEntity resolveCrfVersion(String ocOid, SubmissionContext ctx) {
        if (ocOid == null) {
            ctx.addError("Missing CRF version OID");
            return null;
        }
        return crfVersionRepository.findByOcOid(ocOid).orElse(null);
    }

    private int parseSampleOrdinal(String sampleOrdinalStr) {
        if (sampleOrdinalStr == null || sampleOrdinalStr.isBlank()) return 1;
        try {
            return Integer.parseInt(sampleOrdinalStr);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private StudyEventEntity findOrCreateStudyEvent(Integer studySubjectId, Integer studyEventDefinitionId,
                                                     int sampleOrdinal, SubmissionContext ctx) {
        if (studySubjectId == null || studyEventDefinitionId == null) return null;

        List<StudyEventEntity> existing = studyEventRepository
                .findByStudySubjectIdAndStudyEventDefinitionIdAndSampleOrdinal(
                        studySubjectId, studyEventDefinitionId, sampleOrdinal);

        if (!existing.isEmpty()) {
            logger.debug("Reusing existing StudyEvent id={}", existing.get(0).getStudyEventId());
            return existing.get(0);
        }

        StudyEventEntity se = new StudyEventEntity();
        se.setStudySubjectId(studySubjectId);
        se.setStudyEventDefinitionId(studyEventDefinitionId);
        se.setSampleOrdinal(sampleOrdinal);
        se.setStatusId(STATUS_AVAILABLE);
        se.setSubjectEventStatusId(STATUS_AVAILABLE);
        se.setOwnerId(ctx.getOwnerId());
        se.setDateCreated(LocalDateTime.now());

        return studyEventRepository.save(se);
    }

    private EventCrfEntity findOrCreateEventCrf(Integer studyEventId, Integer studySubjectId,
                                                 Integer crfVersionId, SubmissionContext ctx) {
        if (studyEventId == null || crfVersionId == null) return null;

        List<EventCrfEntity> existing = eventCrfRepository
                .findByStudyEventIdAndCrfVersionId(studyEventId, crfVersionId);

        if (!existing.isEmpty()) {
            logger.debug("Reusing existing EventCrf id={}", existing.get(0).getEventCrfId());
            return existing.get(0);
        }

        EventCrfEntity ec = new EventCrfEntity();
        ec.setStudyEventId(studyEventId);
        ec.setStudySubjectId(studySubjectId);
        ec.setCrfVersionId(crfVersionId);
        ec.setStatusId(STATUS_DATA_ENTRY_STARTED);
        ec.setOwnerId(ctx.getOwnerId());
        ec.setDateCreated(LocalDateTime.now());

        return eventCrfRepository.save(ec);
    }

    private void createItemDataRecords(Integer eventCrfId, SubmissionContext ctx) {
        int ordinal = 0;
        for (ItemValue itemValue : ctx.getItems()) {
            if (!itemValue.hasValue()) continue;

            ItemEntity item = findItemByOid(itemValue.getItemOid());
            if (item == null) {
                logger.warn("Item not found for OID: {}", itemValue.getItemOid());
                continue;
            }

            ItemDataEntity id = new ItemDataEntity();
            id.setItemId(item.getItemId());
            id.setEventCrfId(eventCrfId);
            id.setValue(itemValue.getValue());
            id.setOrdinal(++ordinal);
            id.setStatusId(STATUS_AVAILABLE);
            id.setDeleted(false);
            id.setOwnerId(ctx.getOwnerId());
            id.setDateCreated(LocalDateTime.now());

            itemDataRepository.save(id);
        }
        logger.debug("Created {} item_data records for eventCrfId={}", ordinal, eventCrfId);
    }

    private ItemEntity findItemByOid(String ocOid) {
        if (ocOid == null) return null;
        List<ItemEntity> items = itemRepository.findByOcOid(ocOid);
        return items.isEmpty() ? null : items.get(0);
    }
}
