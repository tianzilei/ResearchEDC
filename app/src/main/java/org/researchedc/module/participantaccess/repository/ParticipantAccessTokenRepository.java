package org.researchedc.module.participantaccess.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.researchedc.module.participantaccess.entity.ParticipantAccessToken;
import org.researchedc.module.participantaccess.enums.ParticipantAccessTokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantAccessTokenRepository extends JpaRepository<ParticipantAccessToken, Long> {
    Optional<ParticipantAccessToken> findByTokenHash(String tokenHash);

    List<ParticipantAccessToken> findByParticipantAccountIdOrderByIssuedDateDesc(Long participantAccountId);

    List<ParticipantAccessToken> findByStatusInAndExpiresAtBefore(Collection<ParticipantAccessTokenStatus> statuses,
                                                                  LocalDateTime expiresAt);
}
