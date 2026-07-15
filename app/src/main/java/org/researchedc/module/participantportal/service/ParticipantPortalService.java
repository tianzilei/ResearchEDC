package org.researchedc.module.participantportal.service;

import java.util.Comparator;
import java.util.List;

import org.researchedc.module.ecoa.dto.EcoaAssignmentDTO;
import org.researchedc.module.ecoa.enums.EcoaAssignmentStatus;
import org.researchedc.module.ecoa.service.EcoaService;
import org.researchedc.module.econsent.dto.ParticipantConsentDTO;
import org.researchedc.module.econsent.enums.ConsentAssignmentStatus;
import org.researchedc.module.econsent.service.EconsentService;
import org.researchedc.module.participantaccess.dto.ParticipantBootstrapDTO;
import org.researchedc.module.participantaccess.service.ParticipantAccessService;
import org.researchedc.module.participantportal.dto.ParticipantPortalDTO;
import org.researchedc.module.participantportal.dto.ParticipantPortalSummaryDTO;
import org.researchedc.module.participantportal.dto.ParticipantPortalTaskDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class ParticipantPortalService {

    private final ParticipantAccessService participantAccessService;
    private final EcoaService ecoaService;
    private final EconsentService econsentService;

    public ParticipantPortalService(ParticipantAccessService participantAccessService,
                                    EcoaService ecoaService,
                                    EconsentService econsentService) {
        this.participantAccessService = participantAccessService;
        this.ecoaService = ecoaService;
        this.econsentService = econsentService;
    }

    @Transactional
    public ParticipantPortalDTO bootstrap(String rawToken) {
        ParticipantBootstrapDTO participant = participantAccessService.verifyToken(rawToken);
        List<ParticipantPortalTaskDTO> questionnaireTasks = ecoaService
                .listParticipantAssignmentsForAccount(participant.getParticipantAccountId())
                .stream()
                .map(ecoaService::toAssignmentDto)
                .map(this::toQuestionnaireTask)
                .toList();
        List<ParticipantPortalTaskDTO> consentTasks = econsentService
                .listParticipantConsentsForAccount(participant.getParticipantAccountId())
                .stream()
                .map(this::toConsentTask)
                .toList();

        List<ParticipantPortalTaskDTO> tasks = new java.util.ArrayList<>();
        tasks.addAll(questionnaireTasks);
        tasks.addAll(consentTasks);
        tasks.sort(Comparator
                .comparing(ParticipantPortalTaskDTO::getDueAt,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(ParticipantPortalTaskDTO::getTitle));

        ParticipantPortalDTO dto = new ParticipantPortalDTO();
        dto.setParticipant(participant);
        dto.setTasks(tasks);
        dto.setSummary(summarize(tasks));
        return dto;
    }

    private ParticipantPortalTaskDTO toQuestionnaireTask(EcoaAssignmentDTO assignment) {
        ParticipantPortalTaskDTO task = new ParticipantPortalTaskDTO();
        task.setId("ecoa:" + assignment.getId());
        task.setType("ECOA");
        task.setAssignmentId(assignment.getId());
        task.setTaskInstanceId(assignment.getTaskInstanceId());
        task.setTitle(StringUtils.hasText(assignment.getQuestionnaireAssignmentId())
                ? assignment.getQuestionnaireAssignmentId()
                : "Questionnaire task");
        task.setSubtitle("eCOA / ePRO");
        task.setDescription(assignment.getScoreSummary());
        task.setStatus(assignment.getStatus().name());
        task.setDueAt(assignment.getDueAt());
        task.setActionable(assignment.getStatus() == EcoaAssignmentStatus.PENDING
                || assignment.getStatus() == EcoaAssignmentStatus.IN_PROGRESS);
        task.setQuestionnaireAssignmentId(assignment.getQuestionnaireAssignmentId());
        return task;
    }

    private ParticipantPortalTaskDTO toConsentTask(ParticipantConsentDTO consent) {
        ParticipantPortalTaskDTO task = new ParticipantPortalTaskDTO();
        task.setId("consent:" + consent.getAssignment().getId());
        task.setType("CONSENT");
        task.setAssignmentId(consent.getAssignment().getId());
        task.setTaskInstanceId(consent.getAssignment().getTaskInstanceId());
        task.setTitle(consent.getTemplate().getName());
        task.setSubtitle("Consent " + consent.getVersion().getVersionLabel());
        task.setDescription(consent.getTemplate().getDescription());
        task.setStatus(consent.getAssignment().getStatus().name());
        task.setDueAt(consent.getAssignment().getDueAt());
        task.setActionable(consent.getAssignment().getStatus() == ConsentAssignmentStatus.ASSIGNED);
        task.setConsentVersionLabel(consent.getVersion().getVersionLabel());
        task.setConsentBodyText(consent.getVersion().getBodyText());
        return task;
    }

    private ParticipantPortalSummaryDTO summarize(List<ParticipantPortalTaskDTO> tasks) {
        ParticipantPortalSummaryDTO summary = new ParticipantPortalSummaryDTO();
        summary.setTotalTasks(tasks.size());
        summary.setQuestionnaireTasks((int) tasks.stream().filter(task -> "ECOA".equals(task.getType())).count());
        summary.setConsentTasks((int) tasks.stream().filter(task -> "CONSENT".equals(task.getType())).count());
        summary.setOverdueTasks((int) tasks.stream().filter(task -> "OVERDUE".equals(task.getStatus())).count());
        summary.setActionableTasks((int) tasks.stream().filter(ParticipantPortalTaskDTO::isActionable).count());
        return summary;
    }
}
