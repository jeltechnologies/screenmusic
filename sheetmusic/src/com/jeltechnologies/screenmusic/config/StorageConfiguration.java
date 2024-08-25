package com.jeltechnologies.screenmusic.config;

import java.io.File;

public record StorageConfiguration(String cache, String cacheExtracted, String cacheDeleted, String blackList, String temp) 
{
    public StorageConfiguration(String cache, String cacheExtracted, String cacheDeleted, String blackList, String temp) {
	this.cache = cache;
	this.cacheExtracted = getDefault(cacheExtracted, cache + "/img");
	this.cacheDeleted = getDefault(cacheDeleted, cache + "/_Recycled_");
	this.blackList = getDefault(blackList, cache + "/blacklisted_pdfs.txt");
	String defaultTemp = System.getProperty("java.io.tmpdir") + "/" + "sheetmusic-tmp/";
	this.temp = getDefault(temp, defaultTemp);
    }
    
    private String getDefault(String value, String defaultValue) {
	if (value == null || value.isBlank()) {
	    return defaultValue;
	} else {
	    return value;
	}
    }
    
    /**
     * Temporary folder to for PDFBox and other temp files
     */
    public File getTempFolder() {
	return new File(temp);
    }

    /**
     * Folder where cached / extracted PDF info is stored
     */
    public File getCacheFolder() {
	return new File(cache);
    }

    /**
     * Folder inside the cache folder where extracted images and thumbnails are stored
     */
    public File getCacheFolderExtracted() {
	return new File(cacheExtracted);
    }

    /**
     * Folder inside the cache folder where extracted images are stored into when PDF is removed
     */
    public File getCacheFolderDeleted() {
	return new File(cacheDeleted);
    }

    /**
     * Text file where all PDFs are stored that did not work for some reason, these files will be ignored
     */
    public File getBlackListFile() {
	return new File(blackList());
    }
    
}
