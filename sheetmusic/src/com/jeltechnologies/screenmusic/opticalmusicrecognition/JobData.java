package com.jeltechnologies.screenmusic.opticalmusicrecognition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.jeltechnologies.screenmusic.config.AudiverisOption;
import com.jeltechnologies.screenmusic.library.Book;

public class JobData implements Serializable {
    private static final long serialVersionUID = -6906922393761321359L;
    private String bookId;
    private int from;
    private int to;
    private Book book;
    private String userName;
    private String label;
    private List<AudiverisOption> options = new ArrayList<AudiverisOption>();

    public int getFrom() {
	return from;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setFrom(int from) {
	this.from = from;
    }

    public int getTo() {
	return to;
    }

    public void setTo(int to) {
	this.to = to;
    }
    
    public int getPages() {
	return to - from + 1;
    }

    public List<AudiverisOption> getOptions() {
        return options;
    }

    public void setOptions(List<AudiverisOption> options) {
        this.options = options;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    @Override
    public String toString() {
	return "JobData [bookId=" + bookId + ", from=" + from + ", to=" + to + ", book=" + book + ", userName=" + userName + ", options=" + options + "]";
    }
}
