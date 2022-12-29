package com.looksee.visualDesignAudit.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Defines all types of {@link Audit audits} that exist in the system
 */
public enum ObservationType {	
	ELEMENT("Element"), 
	TYPOGRAPHY("Typography"), 
	COLOR_PALETTE("Color Palette"), 
	PROPERTY_MAP("Property Map"),
	STYLE_MISSING("Style Missing"),
	PAGE_STATE("Page"),
	TYPEFACE("Typeface"),
	COLOR_CONTRAST("Color Contrast"),
	SECURITY("Security"),
	SEO("SEO"),
	UNKNOWN("Unknown");
	
	private String shortName;

	ObservationType (String shortName) {
        this.shortName = shortName;
    }

    @Override
    public String toString() {
        return shortName;
    }

    @JsonCreator
    public static ObservationType create (String value) {
        if(value == null) {
            return UNKNOWN;
        }
        for(ObservationType v : values()) {
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
