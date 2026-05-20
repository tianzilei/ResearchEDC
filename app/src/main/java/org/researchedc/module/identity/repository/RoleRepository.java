package org.researchedc.module.identity.repository;

import java.util.List;
import org.researchedc.module.identity.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    List<RoleEntity> findByUserName(String userName);

    List<RoleEntity> findByStudyId(Integer studyId);
}
