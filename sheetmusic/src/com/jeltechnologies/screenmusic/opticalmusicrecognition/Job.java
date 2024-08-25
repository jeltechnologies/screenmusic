package com.jeltechnologies.screenmusic.opticalmusicrecognition;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jeltechnologies.screenmusic.library.Book;

public class Job implements Serializable {
    private static final long serialVersionUID = 3793990943547388929L;
    private JobStatus status = JobStatus.QUEUED;
    private final String id;
   
    private final LocalDateTime startTime = LocalDateTime.now();
    @JsonIgnore
    private File inputFile;
    @JsonIgnore
    private File outputFolder;
    private File outputFile;
    @JsonIgnore
    private final JobData jobData;
    private volatile boolean canceled = false;
    private String step = "";
    private int percentageCompleted = 0;
    
    public Job(JobData jobData) {
	this.jobData = jobData;
	this.id = UUID.randomUUID().toString();
    }
    
    public void cancel() {
	canceled = true;
    }
    
    public JobData getJobData() {
        return jobData;
    }

    public int getPercentageCompleted() {
        return percentageCompleted;
    }

    public void setPercentageCompleted(int percentageCompleted) {
        this.percentageCompleted = percentageCompleted;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public File getInputFile() {
        return inputFile;
    }
    
    public File getOutputFolder() {
        return outputFolder;
    }
    
    public void setOutputFolder(File outputFolder) {
        this.outputFolder = outputFolder;
    }

    public String getId() {
        return id;
    }
    
    public String getStatus() {
        return status.toString();
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }
    
    @JsonIgnore
    public Book getBook() {
        return jobData.getBook();
    }
    
    public String getLabel() {
	return jobData.getLabel();
    }
    
    public File getOutputFile() {
        return outputFile;
    }

    public void setDownloadFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public String getUserName() {
        return jobData.getUserName();
    }

    public int getFrom() {
        return jobData.getFrom();
    }

    public int getTo() {
        return jobData.getTo();
    }
    
    public int getPages() {
	return jobData.getPages();
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }


}
