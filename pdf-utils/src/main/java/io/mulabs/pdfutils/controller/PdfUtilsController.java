package io.mulabs.pdfutils.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.PDFMergerUtility.DocumentMergeMode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/pdf")
public class PdfUtilsController {

	@PostMapping("/merge")
	public ResponseEntity<byte[]> merge(@RequestParam("files") MultipartFile[] files) {

		List<PDDocument> PDDocumentList = Arrays.stream(files).map(file -> {
			try {
				return PDDocument.load(file.getInputStream());
			} catch (IOException e) {
				return null;
			}

		}).collect(Collectors.toList());

		if (files.length != PDDocumentList.size()) {
			// One of more files are either not PDF or corrupted
			// TODO return error;
		}

		long encryptrfFilesCount = PDDocumentList.stream().filter(doc -> doc.isEncrypted()).count();

		if (encryptrfFilesCount > 0) {
			// One of more files are encrypted
		}

		String mergedPdf = "MergedPdf.pdf";

		PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
		pdfMergerUtility.setDocumentMergeMode(DocumentMergeMode.OPTIMIZE_RESOURCES_MODE);
		pdfMergerUtility.setDestinationFileName(mergedPdf);
		try {

			for (MultipartFile multipartFile : files) {
				pdfMergerUtility.addSource(multipartFile.getInputStream());
			}

			pdfMergerUtility.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());

		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDispositionFormData(mergedPdf, mergedPdf);
		headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
		ResponseEntity<byte[]> response = null;
		try {
			response = new ResponseEntity<>(Files.readAllBytes(Paths.get(mergedPdf)), headers,
					HttpStatus.OK);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

}
