package com.jeltechnologies.screenmusic.config;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AudiverisConfiguration {
    private String audiveris_lib;
    private String tessdata_prefix;
    private List<AudiverisOption> allOptionsInAudiveris = null;
    @JsonProperty(value = "options")
    private List<AudiverisOption> defaultOptions;
    private List<TessdataLanguage> languages = null;

    public AudiverisConfiguration() {
    }

    public String getAudiveris_lib() {
	return audiveris_lib;
    }

    public void setAudiveris_lib(String audiveris_lib) {
	this.audiveris_lib = audiveris_lib;
    }

    public String getTessdata_prefix() {
	return tessdata_prefix;
    }

    public void setTessdata_prefix(String tessdata_prefix) {
	this.tessdata_prefix = tessdata_prefix;
    }

    public List<AudiverisOption> getAllOptionsInAudiveris() {
	if (allOptionsInAudiveris == null) {
	    allOptionsInAudiveris = new AudiverisDefaultOptions().getOptions();
	}
	return allOptionsInAudiveris;
    }

    public List<TessdataLanguage> getLanguages() {
	if (languages == null) {
	    languages = new TessdataLanguages(tessdata_prefix).getLanguages();
	}
	return languages;
    }

    public List<AudiverisOption> getDefaultOptions() {
	return defaultOptions;
    }

    public void setDefaultOptions(List<AudiverisOption> defaultOptions) {
	this.defaultOptions = defaultOptions;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("AudiverisConfiguration [audiveris_lib=");
	builder.append(audiveris_lib);
	builder.append(", tessdata_prefix=");
	builder.append(tessdata_prefix);
	builder.append(", allOptionsInAudiveris=");
	builder.append(allOptionsInAudiveris);
	builder.append(", defaultOptions=");
	builder.append(defaultOptions);
	builder.append(", languages=");
	builder.append(languages);
	builder.append("]");
	return builder.toString();
    }

}