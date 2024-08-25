package com.jeltechnologies.screenmusic.pdf;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.screenmusic.User;
import com.jeltechnologies.screenmusic.library.Book;
import com.jeltechnologies.utils.StringUtils;

import javaxt.io.Image;

public class PdfExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(PdfExtractor.class);

    private PDDocument pdDocument = null;

    public static final int DOTS_PER_INCH = 300;

    private static final int ONE_MILLION = 1000000;

    private final static int MAX_PIXELS_PER_IMAGE = 20 * ONE_MILLION; // maximum pixels Audiveris can handle

    public File extractPdf(User user, String outFileName, Book book, int startPage, int endPage) throws IOException {
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("extractPdf " + book.getLabel() + ", startPage: " + startPage + ", endPage: " + endPage + " to outFileName " + outFileName);
	}
	File outFile = null;
	try {
	    File inFile = user.getFile(book.getRelativeFileName());
	    LOGGER.info("Opening [" + inFile.getAbsolutePath() + "]");
	    pdDocument = PdfManager.openPdf(inFile);
	    List<BufferedImage> images = new ArrayList<BufferedImage>();
	    for (int i = startPage - 1; i < endPage; i++) {
		BufferedImage image = getPageAsImage(i, MAX_PIXELS_PER_IMAGE);
		images.add(image);
	    }
	    if (images.size() == 1) {
		outFile = createTempFile(outFileName + ".jpg");
		Image javaXtImage = new Image(images.get(0));
		javaXtImage.saveAs(outFile);
	    } else {
		outFile = createTempFile(outFileName + ".pdf");
		saveImagesAsPdf(images, outFile);
	    }
	} catch (Exception e) {
	    LOGGER.warn("Cannot extract pages " + book.getLabel() + ", startPage: " + startPage + ", endPage: " + endPage, e);
	    if (pdDocument != null) {
		pdDocument.close();
	    }
	}
	return outFile;
    }

    private BufferedImage getPageAsImage(int pageIndex, int maximumPixels) throws Exception {
	BufferedImage originalImage = new PDFRenderer(pdDocument).renderImageWithDPI(pageIndex, DOTS_PER_INCH);
	BufferedImage resizedImage;
	int height = originalImage.getHeight();
	int width = originalImage.getWidth();
	int pixels = height * width;
	if (pixels <= maximumPixels) {
	    resizedImage = originalImage;
	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("Image does not need resizing, pixels = " + StringUtils.formatNumber(pixels));
	    }
	} else {
	    float shrinkPercentage = ((float) maximumPixels / (float) pixels);
	    float newHeightBd = shrinkPercentage * (float) height;
	    int newHeight = Math.round(newHeightBd);
	    Image javaXtImage = new Image(originalImage);
	    javaXtImage.setHeight(newHeight);
	    if (LOGGER.isDebugEnabled()) {
		int newWidth = javaXtImage.getWidth();
		int newPixels = newHeight * newWidth;
		LOGGER.debug("Resized from [" + height + " * " + width + " = " + StringUtils.formatNumber(pixels) + " pixels] to [" + newHeight + " * "
			+ newWidth + " = " + StringUtils.formatNumber(newPixels) + " pixels], shrink = " + StringUtils.formatNumber(shrinkPercentage));
	    }
	    resizedImage = javaXtImage.getBufferedImage();
	}
	return resizedImage;
    }

    private void saveImagesAsPdf(List<BufferedImage> images, File fileToSaveTo) throws IOException {
	PDDocument pdf = null;
	try {
	    pdf = new PDDocument();
	    PDRectangle pageSize = PDRectangle.A4;
	    float pageWidth = pageSize.getWidth();
	    float pageHeight = pageSize.getHeight();

	    for (BufferedImage bufferedImage : images) {
		PDImageXObject image = LosslessFactory.createFromImage(pdf, bufferedImage);

		int originalWidth = image.getWidth();
		int originalHeight = image.getHeight();
		float ratio = Math.min(pageWidth / originalWidth, pageHeight / originalHeight);
		float scaledWidth = originalWidth * ratio;
		float scaledHeight = originalHeight * ratio;
		float x = (pageWidth - scaledWidth) / 2;
		float y = (pageHeight - scaledHeight) / 2;
		PDPage page = new PDPage(pageSize);
		pdf.addPage(page);

		try (PDPageContentStream contents = new PDPageContentStream(pdf, page)) {
		    contents.drawImage(image, x, y, scaledWidth, scaledHeight);
		}
	    }
	    pdf.save(fileToSaveTo);
	} finally {
	    if (pdf != null) {
		pdf.close();
	    }
	}
    }

    private File createTempFile(String fileName) {
	File tempFolder = new File(System.getProperty("java.io.tmpdir"));
	File temp = new File(tempFolder, fileName);
	if (temp.exists()) {
	    temp.delete();
	}
	return temp;
    }

}
