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
import fr.sedoo.certifymyrepo.rest.export.PdfPrinter;
import fr.sedoo.certifymyrepo.rest.export.Report;
import fr.sedoo.certifymyrepo.rest.export.Requirement;

@RunWith(SpringRunner.class)
public class TestPdf {
	@Test
	@Ignore
	public void basicTest() throws Exception {
		
		Report report = new Report();
		report.setTitle("SSS Report");
		report.setStatus(ReportStatus.IN_PROGRESS.name());
		report.setUpdateDate(new Date());
		report.setVersion("1.0");
		List<Requirement> requirements = new ArrayList<>();
		Requirement r0 = new Requirement();
		r0.setCode("R0");
		r0.setRequirementLabel("R0. Veuillez fournir les éléments de contexte de votre entrepôt.");
		r0.setResponse("ceci est un test.");
		r0.setLevel("3");
		r0.setLevelLabel("3 - L’entrepôt est en phase d’implémentation");
		requirements.add(r0);
		Requirement r1 = new Requirement();
		r0.setCode("R1");
		r1.setRequirementLabel("R1. L’entrepôt de données a pour mission explicite de donner accès à des données dans son domaine et de les préserver");
		r1.setResponse("ceci est un autre test.");
		r0.setLevel("0");
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
