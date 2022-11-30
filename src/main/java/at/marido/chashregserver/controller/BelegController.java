package at.marido.chashregserver.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
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

import at.marido.chashregserver.dto.Chart;
import at.marido.chashregserver.dto.ChartData;
import at.marido.chashregserver.dto.ChartDataset;
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
	public ResponseEntity<HashMap<String,Object>> getBelege(@RequestParam(name = "page",required = false) Integer page,@RequestParam(name = "von",required = false) String von,
			@RequestParam(name = "bis",required = false) String bis) {
		List<BelegEntity> belege = new ArrayList<>();
		Pageable pageAble = PageRequest.of(page == null ? 0 : page, 15, Sort.by(Direction.ASC, "belegNr"));
		LocalDate datVon = isEmpty(von) ? LocalDate.of(1900, 1, 1) : toDate(von);
		LocalDate datBis = isEmpty(bis) ? LocalDate.of(2100, 12, 31) : toDate(bis);
		long count = this.belegRepository.countByDatumBetween(datVon,datBis);
		this.belegRepository.findAllByDatumBetween(datVon,datBis, pageAble).forEach(belege::add);
        
		var result = new HashMap<String,Object>();
		result.put("count", count);
		result.put("data", belege);
		var resultEntity = new ResponseEntity<>(result, HttpStatus.OK);
		System.out.println(datVon);
		System.out.println(datBis);
		return resultEntity;
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
			Path fileStorageLocation = Paths.get("/home/mario/Projects/cashreg/uploads").toAbsolutePath().normalize();
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
	private boolean isEmpty(String s) {
		return org.apache.commons.lang3.StringUtils.isBlank(s);
	}
	private LocalDate toDate(String s) {
		String[] dateParts = s.split("-");
		return LocalDate.of(Integer.parseInt(dateParts[0]),Integer.parseInt(dateParts[1]),Integer.parseInt(dateParts[2]));
	}
}
