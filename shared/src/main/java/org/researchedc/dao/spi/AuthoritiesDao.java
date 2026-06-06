package org.researchedc.dao.spi;

import org.researchedc.domain.user.AuthoritiesBean;

public interface AuthoritiesDao {

    AuthoritiesBean findByUsername(String username);

    default AuthoritiesBean saveOrUpdate(AuthoritiesBean authoritiesBean) {
        throw new UnsupportedOperationException();
    }

}
