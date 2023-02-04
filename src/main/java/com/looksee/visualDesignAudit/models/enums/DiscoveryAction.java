package com.looksee.visualDesignAudit.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum DiscoveryAction {
	START("start"), STOP("stop");
	
	private String shortName;

	DiscoveryAction (String shortName) {
        this.shortName = shortName;
    }

    @Override
    public String toString() {
        return shortName;
    }

    @JsonCreator
    public static DiscoveryAction create (String value) {
        if(value == null) {
            throw new IllegalArgumentException();
        }
        for(DiscoveryAction v : values()) {
            if(value.equalsIgnoreCase(v.getShortName())) {
                return v;
            }
        }
        throw new IllegalArgumentException();
    }

    public String getShortName() {
        return shortName;
    }
}
