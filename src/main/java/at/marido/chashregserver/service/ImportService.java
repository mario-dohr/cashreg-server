package at.marido.chashregserver.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import at.marido.chashregserver.entity.BelegEntity;
import at.marido.chashregserver.entity.BelegZeileEntity;
import at.marido.chashregserver.entity.MitarbeiterEntity;
import at.marido.chashregserver.repository.BelegRepository;
import at.marido.chashregserver.repository.MitarbeiterRepository;

@Service
public class ImportService {

	@Autowired
	private BelegRepository belegRepository;

	@Autowired
	private MitarbeiterRepository mitarbeiterRepository;
	
	private Pattern datumUhrzeitPattern = Pattern
			.compile("(\\w*)\\s*(\\d{1,2}-\\d{1,2}-\\d{1,2})\\s*(\\d{1,2}:\\d{1,2}:\\d{1,2})");
	private Pattern belegNrUndNamePattern = Pattern.compile(".(\\d*)\\s*([\\w\\d]*)\\s*([\\w\\s\\.]+)\\s*");
	private Pattern gesamtbetragPattern = Pattern.compile("\\d+\\s+\\w+\\s+.(\\d+,?\\d*)");
	private Pattern belegStartPattern = Pattern.compile("ATU\s+60388618\s*");
	private Pattern belegZeilePattern = Pattern.compile("(\\d{1,2})\\s+([\\w\\.\\s]+?)\\s+.(\\d+,?\\d{0,2})");
	private Pattern belegZeile2Pattern = Pattern.compile("^\\s+([\\w\\.\\s]+?)\\s*");

	@Transactional(rollbackOn = Exception.class)
	public boolean importFile(Path filePath) {
		Queue<String> linesQueue = readFile(filePath);
		String line = linesQueue.remove();
		MatchResult r = this.belegNrUndNamePattern.matcher(line).toMatchResult();
		System.out.print(r);

		try {
			while (!linesQueue.isEmpty()) {
				Optional<MatchResult> matchResult = searchPattern(this.belegStartPattern, linesQueue);

				if (matchResult.isPresent()) {
					Optional<BelegEntity> beleg = extractBeleg(linesQueue);
					if (beleg.isPresent()) {
						this.belegRepository.save(beleg.get());
					}
				}
			}
			return true;
		} catch (Exception ex) {
			System.out.println(ex);
			if (linesQueue.isEmpty()) {
				System.out.println("Queue ist leer!");
			} else {
				System.out.println("Fehler vor Zeile: " + linesQueue.remove());
			}
			return false;
		}
	}

	private Optional<BelegEntity> extractBeleg(Queue<String> linesQueue) {
		BelegEntity beleg = new BelegEntity();

		if (extractBelegZeilen(beleg, linesQueue) && setGesamtBetrag(beleg, linesQueue)
				&& setDatumUhrzeit(beleg, linesQueue) && setBelegNrUndName(beleg, linesQueue)) {
			return Optional.of(beleg);
		}
		return Optional.empty();

	}

	private boolean extractBelegZeilen(BelegEntity beleg, Queue<String> linesQueue) {
		while (!linesQueue.isEmpty()) {
			String line = linesQueue.remove();
			Matcher m = this.belegZeilePattern.matcher(line);
			if (m.find()) {
				BelegZeileEntity zeile = new BelegZeileEntity();
				zeile.setProdukt(m.group(2));
				zeile.setBetrag(toBetrag(m.group(3)));
				zeile.setBeleg(beleg);
				beleg.getBelegZeilen().add(zeile);
				String line2 = linesQueue.peek();
				m = this.belegZeile2Pattern.matcher(line2);
				if (m.find()) {
					if (!line2.contains("ZW-Summe")) {
						zeile.setProdukt(zeile.getProdukt() + " " + m.group(1));
					}
					linesQueue.remove();
				}
			}
			if (line.startsWith("----------")) {
				return true;
			}
		}
		return true;
	}

	private boolean setBelegNrUndName(BelegEntity beleg, Queue<String> linesQueue) {
		Optional<MatchResult> matchResult = searchPattern(this.belegNrUndNamePattern, linesQueue);
		if (matchResult.isPresent()) {
			beleg.setBelegNr(matchResult.get().group(1));
			beleg.setMitarbeiter(findMitarbeiter(matchResult.get().group(3).trim()));
			return true;
		}
		return false;
	}

	private boolean setGesamtBetrag(BelegEntity beleg, Queue<String> linesQueue) {
		Optional<MatchResult> matchResult = searchPattern(this.gesamtbetragPattern, linesQueue);
		if (matchResult.isPresent()) {
			beleg.setGesamtbetrag(toBetrag(matchResult.get().group(1)));
			return true;
		}
		return false;
	}

	private boolean setDatumUhrzeit(BelegEntity beleg, Queue<String> linesQueue) {
		Optional<MatchResult> matchResult = searchPattern(this.datumUhrzeitPattern, linesQueue);
		if (matchResult.isPresent()) {
			beleg.setDatum(toDate(matchResult.get().group(2)));
			beleg.setUhrzeit(toTime(matchResult.get().group(3)));
			return true;
		}
		return false;
	}

	private Optional<MatchResult> searchPattern(Pattern pattern, Queue<String> linesQueue) {

		while (!linesQueue.isEmpty()) {
			String line = linesQueue.remove();
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				return Optional.of(matcher.toMatchResult());
			}
		}
		return Optional.empty();
	}

	private Queue<String> readFile(Path filePath) {
		Queue<String> queue = new LinkedList<>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()));
			String line = reader.readLine();
			while (line != null) {
				queue.add(line);
				line = reader.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return queue;
	}

	private LocalDate toDate(String s) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yy");
		return LocalDate.parse(s, formatter);
	}

	private LocalTime toTime(String s) {
		Matcher m = Pattern.compile("(\\d{1,2}):(\\d{1,2}):(\\d{1,2})").matcher(s);
		if (m.find()) {
			return LocalTime.of(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)),
					Integer.parseInt(m.group(3)));
		} else {
			return LocalTime.now();
		}
	}

	private BigDecimal toBetrag(String s) {
		return BigDecimal.valueOf(Double.parseDouble(s.replace(',', '.')));
	}
	
	private MitarbeiterEntity findMitarbeiter(String benutzerId) {
		MitarbeiterEntity result = mitarbeiterRepository.findByBenutzerId(benutzerId);
		if (result == null) {
			result = new MitarbeiterEntity();
			result.setBenutzerId(benutzerId);
		}
		return result;
	}
	
}
