package com.jeltechnologies.screenmusic.tags;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.screenmusic.db.DBUsers;
import com.jeltechnologies.screenmusic.jsonpayloads.UserPreferences;
import com.jeltechnologies.screenmusic.library.Library;
import com.jeltechnologies.screenmusic.servlet.ScreenMusicContext;

public class SeriesSelectTag extends AbstractSelectTag {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeriesSelectTag.class);

    @Override
    protected List<String> getOptions() {
	return new Library(getUser(), new ScreenMusicContext(getRequest())).getSeries();
    }

    @Override
    protected String getSelected() {
	String user = getUser().name();
	String selected = null;
	DBUsers db = null;
	try {
	    db = new DBUsers();
	    UserPreferences preferences = db.getPreferences(user);
	    if (preferences != null) {
		selected = preferences.getSeriesSelected();
	    }

	} catch (Exception e) {
	    LOGGER.warn("Cannot get user preferences for " + user, e);
	} finally {
	    if (db != null) {
		db.close();
	    }
	}
	if (selected == null) {
	    List<String> options = getOptions();
	    if (!options.isEmpty()) {
		selected = options.get(0);
	    }
	}
	return selected;
    }

}
