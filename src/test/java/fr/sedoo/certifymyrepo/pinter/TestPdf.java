package fr.sedoo.certifymyrepo.pinter;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import fr.sedoo.certifymyrepo.rest.domain.ReportStatus;
import fr.sedoo.certifymyrepo.rest.print.PdfPrinter;
import fr.sedoo.certifymyrepo.rest.print.PrintableReport;
import fr.sedoo.certifymyrepo.rest.print.PrintableRequirement;

@RunWith(SpringRunner.class)
public class TestPdf {
	@Test
	@Ignore
	public void basicTest() throws Exception {
		
		PrintableReport report = new PrintableReport();
		report.setTitle("SSS Report");
		report.setStatus(ReportStatus.IN_PROGRESS.name());
		report.setUpdateDate(new Date());
		report.setVersion("1.0");
		List<PrintableRequirement> requirements = new ArrayList<>();
		PrintableRequirement r0 = new PrintableRequirement();
		r0.setRequirement("R0. Veuillez fournir les éléments de contexte de votre entrepôt.");
		r0.setResponse("ceci est un test.");
		r0.setLevelLabel("3 - L’entrepôt est en phase d’implémentation");
		requirements.add(r0);
		PrintableRequirement r1 = new PrintableRequirement();
		r1.setRequirement("R1. L’entrepôt de données a pour mission explicite de donner accès à des données dans son domaine et de les préserver");
		r1.setResponse("ceci est un autre test.");
		r1.setLevelLabel("0 - Sans objet");
		requirements.add(r1);
		report.setRequirements(requirements);

		File result = new File("FR.pdf");
		if (result.exists()) {
			result.delete();
		}
		PdfPrinter printer = new PdfPrinter();
		byte[] print = printer.print("en", report, null);
		FileUtils.writeByteArrayToFile(result, print);
		assertTrue(result.getTotalSpace() > 0);
		System.out.println("Check the content:");
		System.out.println(result.getAbsoluteFile());

	}
}
