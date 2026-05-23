/*
 * Created on Sep 1, 2005
 *
 *
 */
package org.researchedc.logic.core;

import org.researchedc.bean.core.EntityBean;

/**
 * @author thickerson
 *
 *
 */
public interface BusinessRule {
    public abstract boolean isPropertyTrue(String s);

    public abstract EntityBean doAction(EntityBean o);
}
