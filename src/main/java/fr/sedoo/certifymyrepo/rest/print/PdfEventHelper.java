package fr.sedoo.certifymyrepo.rest.print;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfPage;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

public class PdfEventHelper extends PdfPageEventHelper {
	
	private static final Logger LOG = LoggerFactory.getLogger(PdfEventHelper.class);

	protected PdfNumber orientation = PdfPage.PORTRAIT;

	public void setOrientation(PdfNumber orientation) {
		this.orientation = orientation;
	}
	
	public void onStartPage(PdfWriter writer, Document document) {
		PdfContentByte cb = writer.getDirectContent();
		cb.saveState();
		if (this.orientation.intValue() == PdfPage.PORTRAIT.intValue()) {
			cb.setColorStroke(new BaseColor(88, 88, 88));
			cb.setColorFill(new BaseColor(190, 190, 190));
			cb.rectangle(585, 0, 10, 560);
			cb.fill();
			cb.restoreState();
			cb.saveState();
			cb.setColorStroke(new BaseColor(88, 88, 88));
			cb.setColorFill(new BaseColor(44, 67, 144));
			cb.rectangle(555, 0, 30, 30);
			cb.fill();
			cb.restoreState();
			cb.saveState();
		} else {
			cb.setColorStroke(new BaseColor(88, 88, 88));
			cb.setColorFill(new BaseColor(190, 190, 190));
			cb.rectangle(832, 0, 10, 360);
			cb.fill();
			cb.restoreState();
			cb.saveState();
			cb.setColorStroke(new BaseColor(88, 88, 88));
			cb.setColorFill(new BaseColor(44, 67, 144));
			cb.rectangle(802, 0, 30, 30);
			cb.fill();
			cb.restoreState();
			cb.saveState();
		}
	
		BaseFont bf = null;
		try {
			bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
		} catch (DocumentException | IOException e) {
			LOG.error("Error with font creation", e);
		}
		cb.beginText();
		cb.setFontAndSize(bf, 12);
		cb.setColorFill(BaseColor.WHITE);
		if (this.orientation.intValue() == PdfPage.PORTRAIT.intValue()) {
			cb.showTextAligned(PdfContentByte.ALIGN_CENTER, Integer.toString(writer.getPageNumber()), 570, 10, 0);
		} else {
			cb.showTextAligned(PdfContentByte.ALIGN_CENTER, Integer.toString(writer.getPageNumber()), 817, 10, 0);
		}

		cb.endText();
		cb.restoreState();
	}

}
