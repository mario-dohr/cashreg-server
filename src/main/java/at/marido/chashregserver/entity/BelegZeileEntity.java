package at.marido.chashregserver.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "Beleg_Zeile")
public class BelegZeileEntity extends EntityBase {

	@Column(name = "produkt")
	private String produkt;

	@Column(name = "betrag")
	private BigDecimal betrag;

	@ManyToOne(fetch = FetchType.LAZY)
	@JsonBackReference
	private BelegEntity beleg;


	public String getProdukt() {
		return this.produkt;
	}

	public void setProdukt(String produkt) {
		this.produkt = produkt;
	}

	public BigDecimal getBetrag() {
		return this.betrag;
	}

	public void setBetrag(BigDecimal betrag) {
		this.betrag = betrag;
	}

	public BelegEntity getBeleg() {
		return this.beleg;
	}

	public void setBeleg(BelegEntity beleg) {
		this.beleg = beleg;
	}

}
