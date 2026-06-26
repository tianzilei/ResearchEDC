package org.researchedc.module.identity.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.researchedc.module.identity.entity.UserAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccountEntity, Integer> {

    Optional<UserAccountEntity> findByUserName(String userName);

    List<UserAccountEntity> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        String firstName, String lastName);

    List<UserAccountEntity> findByUserNameIn(Collection<String> userNames);
}
