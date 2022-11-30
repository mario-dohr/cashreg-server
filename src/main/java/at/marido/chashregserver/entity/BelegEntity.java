package at.marido.chashregserver.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "Beleg")
public class BelegEntity extends EntityBase {

	@Column(name = "belegnr")
	private String belegNr;

	@Column(name = "datum")
	private LocalDate datum;

	@Column(name = "uhrzeit")
	private LocalTime uhrzeit;

	@ManyToOne
	private MitarbeiterEntity mitarbeiter;

	@Column(name = "gesamtbetrag")
	private BigDecimal gesamtbetrag;

	@Column(name = "qrcode")
	private String qrCode;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "beleg")
	@JsonManagedReference
	private Set<BelegZeileEntity> belegZeilen = new HashSet<>();

	public String getBelegNr() {
		return this.belegNr;
	}

	public void setBelegNr(String belegNr) {
		this.belegNr = belegNr;
	}

	public LocalDate getDatum() {
		return this.datum;
	}

	public void setDatum(LocalDate datum) {
		this.datum = datum;
	}

	public LocalTime getUhrzeit() {
		return this.uhrzeit;
	}

	public void setUhrzeit(LocalTime uhrzeit) {
		this.uhrzeit = uhrzeit;
	}


	public BigDecimal getGesamtbetrag() {
		return this.gesamtbetrag;
	}

	public void setGesamtbetrag(BigDecimal gesamtbetrag) {
		this.gesamtbetrag = gesamtbetrag;
	}

	public String getQrCode() {
		return this.qrCode;
	}

	public void setQrCode(String qrCode) {
		this.qrCode = qrCode;
	}

	public Set<BelegZeileEntity> getBelegZeilen() {
		return this.belegZeilen;
	}

	public MitarbeiterEntity getMitarbeiter() {
		return mitarbeiter;
	}

	public void setMitarbeiter(MitarbeiterEntity mitarbeiter) {
		this.mitarbeiter = mitarbeiter;
	}

}
