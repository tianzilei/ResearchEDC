package org.researchedc.ws.internal.adapter;

import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.springframework.stereotype.Repository;

@Repository
public class UserAccountAdapter {

    private final IUserAccountDAO delegate;

    public UserAccountAdapter(IUserAccountDAO delegate) {
        this.delegate = delegate;
    }

    public UserAccountBean findByPK(int id) {
        return (UserAccountBean) delegate.findByPK(id);
    }

    public UserAccountBean findByPK(int id, boolean findOwner) {
        return (UserAccountBean) delegate.findByPK(id, findOwner);
    }

    public UserAccountBean findByUserName(String userName) {
        return (UserAccountBean) delegate.findByUserName(userName);
    }
}
