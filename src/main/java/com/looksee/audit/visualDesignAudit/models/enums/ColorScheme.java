package com.looksee.audit.visualDesignAudit.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Complementary
Two colors that are on opposite sides of the color wheel. This combination provides a high contrast and high impact color combination – together, these colors will appear brighter and more prominent.

* 
* Monochromatic
Three shades, tones and tints of one base color. Provides a subtle and conservative color combination. This is a versatile color combination that is easy to apply to design projects for a harmonious look.

Analogous
Three colors that are side by side on the color wheel. This color combination is versatile, but can be overwhelming. To balance an analogous color scheme, choose one dominant color, and use the others as accents.
 
 * SPLIT_COMPLIMENTARY
* 	Three shades with uneven spacing between them
*
 * Triadic
Three colors that are evenly spaced on the color wheel. This provides a high contrast color scheme, but less so than the complementary color combination — making it more versatile. This combination creates bold, vibrant color palettes.

 * Tetradic
Four colors that are evenly spaced on the color wheel. Tetradic color schemes are bold and work best if you let one color be dominant, and use the others as accents. The more colors you have in your palette, the more difficult it is to balance,


 */
public enum ColorScheme {
	COMPLEMENTARY("complementary"), 
	MONOCHROMATIC("monochromatic"), 
	ANALOGOUS("analogous"), 
	TRIADIC("triadic"), 
	TETRADIC("tetradic"), 
	UNKNOWN("unknown"), 
	GRAYSCALE("grayscale"), 
	SPLIT_COMPLIMENTARY("split_complimentary");
	
	private String shortName;

	ColorScheme (String shortName) {
        this.shortName = shortName;
    }

    @Override
    public String toString() {
        return shortName;
    }

    @JsonCreator
    public static ColorScheme create (String value) {
        if(value == null) {
            return UNKNOWN;
        }
        for(ColorScheme v : values()) {
            if(value.equalsIgnoreCase(v.getShortName())) {
                return v;
            }
        }
        return UNKNOWN;
    }

    public String getShortName() {
        return shortName;
    }
}
