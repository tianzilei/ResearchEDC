package org.researchedc.bean.rule.action;

import java.util.HashMap;

/*
 * Use this enum as operator holder
 * @author Krikor Krumlian
 *
 */

public enum ActionType {

    FILE_DISCREPANCY_NOTE(1);

    private int code;

    ActionType(int code) {
        this(code, null);
    }

    ActionType(int code, String longName) {
        this.code = code;
    }

    public static ActionType getByName(String name) {
        HashMap<String, ActionType> operators = new HashMap<String, ActionType>();
        for (ActionType operator : ActionType.values()) {
            operators.put(operator.name(), operator);
        }
        return operators.get(name);
    }

    public static ActionType getByCode(int code) {
        HashMap<Integer, ActionType> operators = new HashMap<Integer, ActionType>();
        for (ActionType operator : ActionType.values()) {
            operators.put(operator.getCode(), operator);
        }
        return operators.get(Integer.valueOf(code));
    }

    public Integer getCode() {
        return code;
    }

}
