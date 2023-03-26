package io.mulabs.pdfutils.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.PDFMergerUtility.DocumentMergeMode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.mulabs.pdfutils.exceptions.InvalidPdfException;
import io.mulabs.pdfutils.exceptions.PdfMergeException;

@RestController
public class PdfUtilsController {

	
	private static Logger log = LoggerFactory.getLogger(PdfUtilsController.class);
	
	@PostMapping("/pdf/merge")
	public ResponseEntity<byte[]> merge(@RequestParam("files") MultipartFile[] files, @RequestParam("finalName") String finalName) {

		ResponseEntity<byte[]> response = null;
		List<PDDocument> PDDocumentList = Arrays.stream(files).map(file -> {
			try {
				return PDDocument.load(file.getInputStream());
			} catch (IOException e) {
				log.error("One or more file is not a valid PDF document", e);
				throw new InvalidPdfException("One or more file is not a valid PDF document");
			}

		}).collect(Collectors.toList());

		long encryptrfFilesCount = PDDocumentList.stream().filter(doc -> doc.isEncrypted()).count();

		if (encryptrfFilesCount > 0) {
			log.error("One or more PDF file password protected");
			throw new InvalidPdfException("One or more PDF file password protected");
		}

		String mergedPdf = finalName + ".pdf";

		PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
		pdfMergerUtility.setDocumentMergeMode(DocumentMergeMode.OPTIMIZE_RESOURCES_MODE);
		pdfMergerUtility.setDestinationFileName(mergedPdf);
		try {

			for (MultipartFile multipartFile : files) {
				pdfMergerUtility.addSource(multipartFile.getInputStream());
			}

			pdfMergerUtility.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());

		} 
		catch (IOException e) {
			log.error("One or more PDF file is password protected", e);
			throw new PdfMergeException("One or more PDF file is password protected", e);
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDispositionFormData(mergedPdf, mergedPdf);
		headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

		try {
			response = new ResponseEntity<>(Files.readAllBytes(Paths.get(mergedPdf)), headers, HttpStatus.OK);
		} 
		catch (IOException e) {
			log.error("Failed to merge PDFs", e);
			throw new PdfMergeException("Failed to merge PDFs", e);
		}
		return response;
	}

}
