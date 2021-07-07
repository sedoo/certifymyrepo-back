package fr.sedoo.certifymyrepo.rest.export;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfDestination;
import com.itextpdf.text.pdf.PdfOutline;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@Component
public class PdfPrinter {
	
	private static final Logger LOG = LoggerFactory.getLogger(PdfPrinter.class);
	
	private Font h3Font;
	private Font h4Font;
	private Font h3FontBold;
	
	public byte[] print(String language, Report report, byte[] image) {
		
		try {
	        Locale locale = new Locale(language);
	        ResourceBundle messages = ResourceBundle.getBundle("messages", locale);
			
			PdfEventHelper helper = new PdfEventHelper();
			Document document = new Document(PageSize.A4, 36, 36, 36, 36);
			
			Font lemonFont = FontFactory.getFont("/fonts/AnyConv.com__LEMONMILK-Regular.ttf", BaseFont.IDENTITY_H,
					BaseFont.EMBEDDED, 0.8f, Font.NORMAL, BaseColor.BLACK);
	
			Font calibriLightFont = FontFactory.getFont("/fonts/CalibriLight.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED,
					0.8f, Font.NORMAL, BaseColor.BLACK);
	
			BaseFont baseFont = lemonFont.getBaseFont();
			BaseFont calibriLightBaseFont = calibriLightFont.getBaseFont();
			BaseFont calibriRegularBaseFont = calibriLightFont.getBaseFont();
	
			Font font40 = new Font(baseFont, 40);
	
			h3Font = new Font(Font.FontFamily.HELVETICA, 14, Font.NORMAL);
			h4Font = new Font(calibriRegularBaseFont, 12, Font.NORMAL);
			h3FontBold = new Font(calibriLightBaseFont, 12, Font.BOLD);
	
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfWriter writer;
		
			writer = PdfWriter.getInstance(document, baos);

			writer.setPageEvent(helper);
			
			document.open();
			
			// Bookmarks - root of the tree
			PdfOutline root = writer.getRootOutline();
			
			Paragraph ph = new Paragraph();
			ph.add(new Phrase(report.getTitle(), font40));
			PdfPCell cell = new PdfPCell();
			cell.setBorder(2);
			cell.setPadding(8f);
			cell.setBorderColor(new BaseColor(44, 67, 144));
			cell.setBorderWidth(3f);
			cell.setPaddingLeft(0);
			cell.addElement(ph);
			PdfPTable table = new PdfPTable(1);
			table.addCell(cell);
			table.setHorizontalAlignment(Element.ALIGN_LEFT);
			table.setWidthPercentage(100f);
			document.add(table);
			
			writePairValue(messages.getString("report.status"), report.getStatus(), document);
			SimpleDateFormat df = null;
			if(StringUtils.equals("fr", language)) {
				df = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.FRENCH);
			} else {
				df = new SimpleDateFormat("MMMM dd, yyyy hh:mm:ss aa", Locale.ENGLISH);
			}
	
			String formatedDate = df.format(report.getUpdateDate());
			writePairValue(messages.getString("report.date"), formatedDate, document);
			writePairValue(messages.getString("report.version"), report.getVersion(), document);
			
			if (image != null) {
				Image aux = Image.getInstance(image);
				float scaler = ((document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin() - 0)
						/ aux.getWidth()) * 100;
	
				aux.scalePercent(scaler);
				document.add(aux);
			}
			
			for(Requirement r : report.getRequirements()) {
				addBookmark(r.getRequirementLabel(), root, writer);
				h3(r.getRequirementLabel(), document);
				writePairValue(messages.getString("requirement.response"), r.getResponse(), document);
				writePairValue(messages.getString("requirement.compliance.level"), r.getLevelLabel(), document);
				writePairValues(messages.getString("requirement.attachments"), r.getAttachments(), document);
				if(r.getComments() != null) {
					writeKey(messages.getString("requirement.comments") , document);
					writeCommentsValue(r.getComments(), document);
				}
			}
	
			
			document.close();
			return baos.toByteArray();
		} catch (DocumentException e) {
			LOG.error("Error during PDF creation", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		} catch (MalformedURLException e) {
			LOG.error("Error with radar image", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		} catch (IOException e) {
			LOG.error("Error with radar image", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}
	
	/**
	 * 
	 * @param title requirement label
	 * @param root
	 * @param writer DocWriter class for PDF
	 */
	private void addBookmark(String title, PdfOutline root, PdfWriter writer) {
		PdfOutline requirementBookmark = new PdfOutline(root,
			    new PdfDestination(
			        PdfDestination.FITH, writer.getVerticalPosition(true)),
			    	title, true);
		root.addKid(requirementBookmark);
	}
	
	private void h3(String title, Document document) throws DocumentException {
		document.add(new Paragraph(new Phrase(" ")));
		PdfPCell cell = new PdfPCell(new Paragraph(new Phrase(title, h3Font)));
		cell.setBorder(2);
		cell.setPadding(8f);
		cell.setBorderColor(new BaseColor(44, 67, 144));
		cell.setBorderWidth(2f);
		cell.setPaddingLeft(0);
		PdfPTable table = new PdfPTable(1);
		table.addCell(cell);
		table.setHorizontalAlignment(Element.ALIGN_LEFT);
		table.setWidthPercentage(100f);
		document.add(table);
	}
	
	private void writePairValue(String key, String value, Document document) throws DocumentException {
		if (StringUtils.isNotEmpty(StringUtils.trimToEmpty(value))) {
			Paragraph ph = new Paragraph();
			ph.add(new Phrase(key + ": ", h3FontBold));
			ph.add(new Phrase(value, h4Font));
			document.add(ph);
		}
	}
	
	private void writeKey(String key, Document document) throws DocumentException {
		Paragraph ph = new Paragraph();
		ph.add(new Phrase(key + ": ", h3FontBold));
		document.add(ph);
	}
	
	private void writeCommentsValue(List<CommentExport> comments, Document document) throws DocumentException {
		for(CommentExport comment : comments) {
			Paragraph ph = new Paragraph();
			String commentValue = comment.getUserName().concat(" ").concat(comment.getCreationDate().concat(": ")).concat(comment.getValue());
			ph.add(new Phrase(commentValue, h4Font));
			document.add(ph);
		}
	}
	
	private void writePairValues(String key, List<String> values, Document document) throws DocumentException {
		if (values != null && !values.isEmpty()) {
			Paragraph ph = new Paragraph();
			ph.add(new Phrase(key + ": ", h3FontBold));
			for(int i=0 ; i<values.size() ; i++) {
				if(StringUtils.isNotEmpty(StringUtils.trimToEmpty(values.get(i)))) {
					if(i < (values.size()-1) ) {
						ph.add(new Phrase(values.get(i).concat(", "), h4Font));
					} else {
						ph.add(new Phrase(values.get(i), h4Font));
					}
				}
				
			}
			document.add(ph);
		}
	}

}
