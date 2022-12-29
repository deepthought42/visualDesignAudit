package com.looksee.visualDesignAudit.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AlertChoice {
	DISMISS("dismiss"), ACCEPT("accept");

	private String shortName;

	AlertChoice (String shortName) {
        this.shortName = shortName;
    }

    @Override
    public String toString() {
        return shortName;
    }

    @JsonCreator
    public static AlertChoice create (String value) {
        if(value == null) {
            throw new IllegalArgumentException();
        }
        for(AlertChoice v : values()) {
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
