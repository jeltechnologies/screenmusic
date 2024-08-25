package com.jeltechnologies.screenmusic.jsonpayloads;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.jeltechnologies.screenmusic.config.AudiverisConfiguration;
import com.jeltechnologies.screenmusic.config.AudiverisDefaultOptions;
import com.jeltechnologies.screenmusic.config.AudiverisOption;
import com.jeltechnologies.screenmusic.config.Configuration;
import com.jeltechnologies.screenmusic.config.OpticalMusicRecognizationConfiguration;

public class MusicXMLPreferences implements Serializable {
    private static final String ORG_AUDIVERIS_OMR_TEXT_LANGUAGE_DEFAULT_SPECIFICATION = "org.audiveris.omr.text.Language.defaultSpecification";

    private static final long serialVersionUID = 627741646188284246L;

    private static final String[] SUPPORTED_OPTIONS = {
	    ORG_AUDIVERIS_OMR_TEXT_LANGUAGE_DEFAULT_SPECIFICATION,
	    "org.audiveris.omr.ui.symbol.TextFont.defaultTextFamily",
	    "org.audiveris.omr.ui.symbol.MusicFont.defaultMusicFamily",
	    "org.audiveris.omr.sheet.ProcessingSwitches.lyrics",
	    "org.audiveris.omr.sheet.ProcessingSwitches.chordNames",
	    "org.audiveris.omr.score.LogicalPart.defaultSingleStaffPartName",
	    "org.audiveris.omr.sheet.ProcessingSwitches.articulations" };

    private List<AudiverisOption> preferences;

    public MusicXMLPreferences() {
	setDefaultsForNewUser();
    }

    private void setDefaultsForNewUser() {
	preferences = new ArrayList<AudiverisOption>();
	List<AudiverisOption> defaultOptions = null;
	OpticalMusicRecognizationConfiguration defaultConfig = Configuration.getInstance().opticalmusicrecognition();
	if (defaultConfig != null) {
	    AudiverisConfiguration audiverisConfiguration = defaultConfig.audiveris();
	    if (audiverisConfiguration != null) {
		defaultOptions = new AudiverisDefaultOptions().getOptions();
	    }
	}
	if (defaultOptions == null) {
	    defaultOptions = new ArrayList<AudiverisOption>();
	}
	for (String optionName : SUPPORTED_OPTIONS) {
	    AudiverisOption found = null;
	    for (int i = 0; i < defaultOptions.size() && found == null; i++) {
		AudiverisOption current = defaultOptions.get(i);
		if (current.name().equals(optionName)) {
		    found = current;
		}
	    }
	    if (found != null) {
		AudiverisOption option = found;
		// Make sure default language for new users is English
		if (optionName.equals(ORG_AUDIVERIS_OMR_TEXT_LANGUAGE_DEFAULT_SPECIFICATION)) {
		    preferences.add(new AudiverisOption(ORG_AUDIVERIS_OMR_TEXT_LANGUAGE_DEFAULT_SPECIFICATION, found.dataType(), "eng", found.description()));
		} else {
		    preferences.add(option);
		}
	    }
	}
    }

    public List<AudiverisOption> getPreferences() {
	return preferences;
    }

    public void setPreferences(List<AudiverisOption> preferences) {
	this.preferences = preferences;
    }

}
