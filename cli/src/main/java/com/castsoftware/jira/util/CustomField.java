package com.castsoftware.jira.util;

public class CustomField {
    public final static String CUSTOM_TEXT_FIELD_TYPE = "text";
    public final static String CUSTOM_SELECT_FIELD_TYPE = "select";

    private String name;
    private String type;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    
    public boolean isValidType() {
        return type.equalsIgnoreCase(CUSTOM_TEXT_FIELD_TYPE)
                || type.equalsIgnoreCase(CUSTOM_SELECT_FIELD_TYPE);
    }
}
