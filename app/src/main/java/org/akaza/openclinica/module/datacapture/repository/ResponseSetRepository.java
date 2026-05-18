package org.akaza.openclinica.module.datacapture.repository;

import org.akaza.openclinica.module.datacapture.entity.ResponseSetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResponseSetRepository extends JpaRepository<ResponseSetEntity, Integer> {
}
