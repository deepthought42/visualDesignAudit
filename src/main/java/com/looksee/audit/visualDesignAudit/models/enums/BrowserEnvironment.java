package com.looksee.audit.visualDesignAudit.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum BrowserEnvironment {
	TEST("test"), DISCOVERY("discovery");
	
	private String shortName;

	BrowserEnvironment(String shortName) {
        this.shortName = shortName;
    }

    @Override
    public String toString() {
        return shortName;
    }

    @JsonCreator
    public static BrowserEnvironment create(String value) {
        if(value == null) {
            throw new IllegalArgumentException();
        }
        for(BrowserEnvironment v : values()) {
            if(value.equals(v.getShortName())) {
                return v;
            }
        }
        throw new IllegalArgumentException();
    }

    public String getShortName() {
        return shortName;
    }
}
