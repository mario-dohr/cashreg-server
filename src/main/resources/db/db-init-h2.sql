drop table beleg_zeile;
drop table beleg;
drop table umsatz;
drop table mitarbeiter;


create table beleg (id bigint auto_increment primary key,
	belegnr varchar(10) not null,
	datum date not null,
	uhrzeit time without time zone not null,
	mitarbeiter_id bigint not null,
	gesamtbetrag decimal(7,2) not null,
	qrcode varchar(200),
	umsatz_id bigint null
	);

create table mitarbeiter(
	id bigint auto_increment primary key,
	benutzer_id varchar(50) not null,
	vorname varchar(50) null,
	nachname varchar(50) null
);
create table beleg_zeile(
	id bigint auto_increment primary key,
	beleg_id bigint  not null,
	produkt varchar(50) not null,
	betrag decimal(7,2) not null);

create table umsatz (
	id bigint auto_increment primary key,
	jahr_monat date not null,
	mitarbeiter_id bigint not null,
	umsatz_r decimal(10,2) not null,
	umsatz_e decimal(10,2) null,
	arbeitstage int not null,
	arbeitstage_korrektur int null,
	arbeitstage_korrektur_text varchar(200) null
);

alter table beleg
	add foreign key(mitarbeiter_id)
	references mitarbeiter(id);

alter table beleg
	add foreign key(umsatz_id)
	references umsatz(id);

alter table umsatz
	add foreign key(mitarbeiter_id)
	references mitarbeiter(id);
	
alter table beleg_zeile
	add foreign key(beleg_id)
	references beleg(id);
	
create unique index umsatz_unique on umsatz (jahr_monat, mitarbeiter_id);	
create unique index mitarbeiter_unique on mitarbeiter (benutzer_id);
