package org.researchedc.ws.internal.adapter;

import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.dao.login.UserAccountDAO;
import org.springframework.stereotype.Repository;

@Repository
public class UserAccountAdapter {

    private final UserAccountDAO delegate;

    public UserAccountAdapter(UserAccountDAO delegate) {
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
