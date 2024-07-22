package com.jeltechnologies.screenmusic.tags;

import com.jeltechnologies.screenmusic.User;
import com.jeltechnologies.screenmusic.library.Book;
import com.jeltechnologies.screenmusic.library.Library;
import com.jeltechnologies.screenmusic.servlet.ScreenMusicContext;
import com.jeltechnologies.utils.StringUtils;

public class TitleTag extends BaseTag {

    @Override
    public void processTag() throws Exception {
	String title;
	String folder = getRequestParameter("folder");
	if (folder != null && !folder.isBlank()) {
	    title = folder;
	} else {
	    String id = getRequestParameter("id");
	    Book book = null;
	    User user = getUser();
	    if (user != null) {
		ScreenMusicContext context = new ScreenMusicContext(this.getPageContext().getServletContext());
		Library library = new Library(user, context);
		if (id != null && !id.isBlank()) {
		    book = library.getBook(id);
		} else {
		    String file = getRequestParameter("file");
		    if (file != null && !file.isBlank()) {
			book = library.getBookByFileName(file);
		    }
		}
	    }
	    if (book == null) {
		title = "";
	    } else {
		title = book.getLabel() + " - ";
	    }
	}
	title = title + "Screen Music";
	String titlEncoded = StringUtils.encodeHtml(title);
	add("<title>" + titlEncoded + "</title>");
    }
}
