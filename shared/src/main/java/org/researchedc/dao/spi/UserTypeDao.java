package org.researchedc.dao.spi;

import org.researchedc.domain.user.UserType;

public interface UserTypeDao {

    UserType findByUserTypeId(Integer userTypeId);

}
