package com.jeltechnologies.screenmusic.opticalmusicrecognition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AudiverisSteps {
    private record Step(String name, String value) {};
    private final List<Step> steps = new ArrayList<Step>();
    private int counter;
    private final int totalSteps;

    public AudiverisSteps(int pages) {
	counter = 0;
	steps.add(new Step("EXPORT_PDF", "Exporting PDF"));
	steps.add(new Step("LOAD", "Get the sheet gray picture"));
	steps.add(new Step("BINARY", "Binarize the sheet gray picture"));
	steps.add(new Step("SCALE", "Compute sheet line thickness, interline, beam thickness"));
	steps.add(new Step("GRID", "Retrieve staff lines, barlines, systems & parts"));
	steps.add(new Step("HEADERS", "Retrieve Clef-Key-Time systems headers"));
	steps.add(new Step("STEM_SEEDS", "Retrieve stem thickness & seeds for stems"));
	steps.add(new Step("BEAMS", "Retrieve beams"));
	steps.add(new Step("LEDGERS", "Retrieve ledgers"));
	steps.add(new Step("HEADS", "Retrieve note heads"));
	steps.add(new Step("STEMS", "Retrieve stems connected to heads & beams"));
	steps.add(new Step("REDUCTION", "Reduce conflicts in heads, stems & beams"));
	steps.add(new Step("CUE_BEAMS", "Retrieve cue beams"));
	steps.add(new Step("TEXTS", "Call OCR on textual items"));
	steps.add(new Step("MEASURES", "Retrieve raw measures from groups of barlines"));
	steps.add(new Step("CHORDS", "Gather notes heads into chords"));
	steps.add(new Step("CURVES", "Retrieve slurs, wedges & endings"));
	steps.add(new Step("SYMBOLS", "Retrieve fixed-shape symbols"));
	steps.add(new Step("LINKS", "Link and reduce symbols"));
	steps.add(new Step("RHYTHMS", "Handle rhythms within measures"));
	steps.add(new Step("PAGE", "Connect systems within page"));
	this.totalSteps = steps.size() * pages;
    }

    public String getStep(String key) {
	Step found = null;
	Iterator<Step> iterator = steps.iterator();
	while (found == null && iterator.hasNext()) {
	    Step current = iterator.next();
	    if (current.name().equals(key)) {
		found = current;
	    }
	}
	String result = null;
	if (found != null) {
	    result = found.value();
	    counter++;
	}
	return result;
    }
    
    public int getPercentageCompleted() {
	float percentage = (float) (counter * 100) / totalSteps;
	int rounded = Math.round(percentage);
	return rounded;
    }
	
}
