package at.marido.chashregserver.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import at.marido.chashregserver.entity.BelegEntity;
import at.marido.chashregserver.repository.BelegRepository;
import at.marido.chashregserver.service.ImportService;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api")
public class BelegController {

	@Autowired
	private BelegRepository belegRepository;

	@Autowired
	private ImportService importService;

	@GetMapping("/hello")
	public String hello() {
		return "Hello";
	}

	@GetMapping("/belege")
	public ResponseEntity<List<BelegEntity>> getBelege() {
		List<BelegEntity> result = new ArrayList<>();
		this.belegRepository.findAll().forEach(result::add);

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("/belege/{id}")
	public ResponseEntity<BelegEntity> getBeleg(@PathVariable("id") long id) {
		Optional<BelegEntity> beleg = this.belegRepository.findById(id);

		if (beleg.isPresent()) {
			return new ResponseEntity<>(beleg.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping("/belege/charts")
	public ResponseEntity<Chart> getChartData() {
		TreeMap<String, BigDecimal> result = new TreeMap<String, BigDecimal>(this.belegRepository.findAll().stream()
				.collect(Collectors.groupingBy(x -> this.toMonth(x.getDatum()), Collectors.mapping(
						BelegEntity::getGesamtbetrag, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)))));
		List<String> labels = new ArrayList<>();
		List<BigDecimal> data = new ArrayList<>();
		for (Entry<String, BigDecimal> entry : result.entrySet()) {
			labels.add(entry.getKey());
			data.add(entry.getValue());
		}
		var chartDataSet = new ChartDataset("Umsatz", data, "red", "red");
		var chartData = new ChartData(labels, Arrays.asList(chartDataSet));
		var chart = new Chart("bar", chartData);
		return new ResponseEntity<>(chart, HttpStatus.OK);
	}

	private String toMonth(LocalDate date) {
		return date.toString().substring(0, 7);
	}

	@PostMapping("/belege/upload")
	public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
		String fileName = file.getOriginalFilename();
		Path filePath = storeFile(file);
		this.importService.importFile(filePath);
		return new UploadFileResponse(fileName);
	}

	private Path storeFile(MultipartFile file) {
		// Normalize file name
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());

		try {
			// Check if the file's name contains invalid characters
			if (fileName.contains("..")) {
				throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
			}

			// Copy file to the target location (Replacing existing file with the same name)
			Path fileStorageLocation = Paths.get("/uploads").toAbsolutePath().normalize();
			Path targetLocation = fileStorageLocation.resolve(fileName);
			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

			return targetLocation;
		} catch (IOException ex) {
			throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
		}
	}

	public static class UploadFileResponse {
		private String fileName;

		public UploadFileResponse(String fileName) {
			this.fileName = fileName;
		}

		public String getFileName() {
			return this.fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}
	}
}
