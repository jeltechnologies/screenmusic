package com.jeltechnologies.screenmusic.opticalmusicrecognition;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.screenmusic.User;
import com.jeltechnologies.screenmusic.db.DBUsers;
import com.jeltechnologies.screenmusic.jsonpayloads.UserPreferences;
import com.jeltechnologies.screenmusic.library.Book;
import com.jeltechnologies.screenmusic.library.Library;
import com.jeltechnologies.screenmusic.servlet.BaseServlet;
import com.jeltechnologies.screenmusic.servlet.ScreenMusicContext;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/download-musicxml")
public class DownloadMusicXMLServlet extends BaseServlet {

    private static final long serialVersionUID = 4141018645883829065L;

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadMusicXMLServlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	User user = getUser(request);
	JobData jobData;
	try {
	    jobData = (JobData) getJsonFromBody(request, JobData.class);
	    LOGGER.info(jobData.toString());
	} catch (Exception e) {
	    throw new ServletException(e.getMessage());
	}
	jobData.setUserName(user.name());
	storeUserPreferences(jobData);
	ScreenMusicContext context = new ScreenMusicContext(request);
	Library library = new Library(user, context);
	Book book = library.getBookWithFileName(jobData.getBookId());
	if (book == null) {
	    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	} else {
	    jobData.setBook(book);
	    Job job = new Job(jobData);
	    String label = library.getPageLabel(job.getBook(), job.getFrom(), job.getTo());
	    jobData.setLabel(label);
	    JobList.getInstance().add(job);
	    JobQueue.getInstance().add(job);
	    respondText(response, job.getId());
	}
    }

    private void storeUserPreferences(JobData jobData) {
	DBUsers db = null;
	try {
	    db = new DBUsers();
	    String user = jobData.getUserName();
	    UserPreferences preferences = db.getPreferences(user);
	    if (preferences == null) {
		preferences = new UserPreferences();
	    }
	    preferences.getOcr().setPreferences(jobData.getOptions());
	    db.addPreferences(user, preferences);
	} catch (Exception e) {
	    LOGGER.warn("Could not store the user preferences because " + e.getMessage(), e);
	} finally {
	    if (db != null) {
		db.close();
	    }
	}
    }

}
