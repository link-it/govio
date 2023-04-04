/*
 * GovIO - Notification system for AppIO
 *
 * Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.govio.template.test;

import it.govio.template.CsvTemplateApplier;
import it.govio.template.Message;
import it.govio.template.Placeholder;
import it.govio.template.Placeholder.Type;
import it.govio.template.Template;
import it.govio.template.TemplateApplierFactory;
import it.govio.template.exception.TemplateValidationException;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class UC1_CsvTemplateTest {
	
	@Test
	@DisplayName("template senza placeholders")
	void UC_1_1_NO_PLACEHOLDERS(){
		List<Placeholder> placeholders = new ArrayList<> ();
		Template template = Template
				.builder()
				.hasDueDate(false)
				.hasPayment(false)
				.messageBody("Il Comune di Empoli le dice hello, il markdown deve essere di almeno 80 caratteri")
				.subject("hello, il subject deve essere almeno dieci caratteri")
				.placeholders(placeholders)
				.build();
		
		CsvTemplateApplier templateApplier = TemplateApplierFactory.buildCSVTemplateApplier(template);

		assertThrows(TemplateValidationException.class, () -> {
			templateApplier.buildMessage("");
		});
	}

	@Test
	@DisplayName("template senza ScheduledExpeditionDate")
	void UC_1_2_ScheduledExpeditionDateNull(){
		List<Placeholder> placeholders = new ArrayList<> ();
		Template template = Template
				.builder()
				.hasDueDate(false)
				.hasPayment(false)
				.messageBody("Il Comune di Empoli le dice hello, il markdown deve essere di almeno 80 caratteri")
				.subject("hello, il subject deve essere almeno dieci caratteri")
				.placeholders(placeholders)
				.build();
		CsvTemplateApplier templateApplier = TemplateApplierFactory.buildCSVTemplateApplier(template);

		assertThrows(TemplateValidationException.class, () -> {
			templateApplier.buildMessage("RSSMRO00A00A000A");
		});
	}

	@Test
	@DisplayName("template con Taxcode blank")
	void UC_1_3_TaxcodeBlank(){
		List<Placeholder> placeholders = new ArrayList<> ();
		Template template = Template
				.builder()
				.hasDueDate(false)
				.hasPayment(false)
				.messageBody("Il Comune di Empoli le dice hello, il markdown deve essere di almeno 80 caratteri ${taxcode}")
				.subject("hello, il subject deve essere almeno dieci caratteri")
				.placeholders(placeholders)
				.build();
		CsvTemplateApplier templateApplier = TemplateApplierFactory.buildCSVTemplateApplier(template);
		assertThrows(TemplateValidationException.class, () -> {
			templateApplier.buildMessage(",2007-12-03T10:15:30");
		});
	}

	@Test
	@DisplayName("Messaggio senza due date, senza payment, senza placeholder")
	void UC_1_4_OnlyTaxcodeExpeditionDate(){
		List<Placeholder> placeholders = new ArrayList<> ();
		Template template = Template
				.builder()
				.hasDueDate(false)
				.hasPayment(false)
				.messageBody("il comune di Empoli avvisa il signor ${taxcode},,il markdown deve essere di almeno 80 caratteri")
				.subject("hello, il subject deve essere almeno dieci caratteri")
				.placeholders(placeholders)
				.build();
		CsvTemplateApplier templateApplier = TemplateApplierFactory.buildCSVTemplateApplier(template);

		Message messaggio = templateApplier.buildMessage("RSSMRO00A00A000A,2007-12-03T10:15:30");
		assertEquals("il comune di Empoli avvisa il signor RSSMRO00A00A000A,,il markdown deve essere di almeno 80 caratteri",messaggio.getMarkdown());
	}

	@Test
	@DisplayName("template con due_date e data valida")
	void UC_1_5_DueDateValido(){
		List<Placeholder> placeholders = new ArrayList<> ();

		Template template = Template
				.builder()
				.hasDueDate(true)
				.hasPayment(false)
				.messageBody("il comune di Empoli lo avvisa che la data di scadenza è ${due_date.date} alle ${due_date.time},il markdown deve essere di almeno 80 caratteri")
				.subject("hello, il subject deve essere almeno dieci caratteri")
				.placeholders(placeholders)
				.build();

		CsvTemplateApplier templateApplier = TemplateApplierFactory.buildCSVTemplateApplier(template);
		Message messaggio = templateApplier.buildMessage("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03T10:15:30");
		assertEquals("il comune di Empoli lo avvisa che la data di scadenza è 03/12/2007 alle 10:15,il markdown deve essere di almeno 80 caratteri",messaggio.getMarkdown());
	}

	@Test
	@DisplayName("template con due_date e data non valida")
	void UC_1_6_DueDateNonValido(){
		List<Placeholder> placeholders = new ArrayList<> ();

		Template template = Template
				.builder()
				.hasDueDate(true)
				.hasPayment(false)
				.messageBody("il comune di Empoli lo avvisa che la data di scadenza è ${due_date.date} alle ${due_date.time}")
				.subject("hello, il subject deve essere almeno dieci caratteri")
				.placeholders(placeholders)
				.build();

		CsvTemplateApplier templateApplier = TemplateApplierFactory.buildCSVTemplateApplier(template);
		assertThrows(TemplateValidationException.class, () -> {
			templateApplier.buildMessage("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-0310:15:30");
		});
	}

	@Test
	@DisplayName("Template con due date e data assente")
	void UC_1_7_DueDateNull(){
		List<Placeholder> placeholders = new ArrayList<> ();

		Template template = Template
				.builder()
				.hasDueDate(true)
				.hasPayment(false)
				.messageBody("il comune di Empoli lo avvisa che la data di scadenza è ${due_date.date} alle ${due_date.time}")
				.subject("hello, il subject deve essere almeno dieci caratteri")
				.placeholders(placeholders)
				.build();

		CsvTemplateApplier templateApplier = TemplateApplierFactory.buildCSVTemplateApplier(template);
		assertThrows(TemplateValidationException.class, () -> {
			templateApplier.buildMessage("RSSMRO00A00A000A,2007-12-03T10:15:30");
		});
	}

	@Test
	@DisplayName("template con payment valido")
	void UC_1_8_PaymentValido(){
		List<Placeholder> placeholders = new ArrayList<> ();

		Template template = Template
				.builder()
				.hasDueDate(true)
				.hasPayment(true)
				.messageBody("il comune di Empoli lo avvisa che il pagamento è di ${amount} effettuato da ${payee} in data ${due_date.date} alle ore ${due_date.time}")
				.subject("hello, il subject deve essere almeno dieci caratteri")
				.placeholders(placeholders)
				.build();

		CsvTemplateApplier templateApplier = TemplateApplierFactory.buildCSVTemplateApplier(template);
		Message messaggio = templateApplier.buildMessage("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03T10:15:30,200000000000000000,100000000000000000,false,12345678901");

		assertEquals("il comune di Empoli lo avvisa che il pagamento è di 100000000000000000 effettuato da 12345678901 in data 03/12/2007 alle ore 10:15",messaggio.getMarkdown());
	}

	@Test
	@DisplayName("Template con payment ed uno o piu' parametri non validi o assenti")
	void UC_1_9_PaymentNonValido(){
		List<Placeholder> placeholders = new ArrayList<> ();

		Template template = Template
				.builder()
				.hasDueDate(false)
				.hasPayment(true)
				.messageBody("il comune di Empoli lo avvisa che il pagamento è di ${amount} effettuato da ${payee} in data ${due_date.date} alle ore ${due_date.time}")
				.subject("hello, il subject deve essere almeno dieci caratteri")
				.placeholders(placeholders)
				.build();

		CsvTemplateApplier templateApplier = TemplateApplierFactory.buildCSVTemplateApplier(template);
		assertThrows(TemplateValidationException.class, () -> {
			templateApplier.buildMessage("RSSMRO00A00A000A,2007-12-03T10:15:30");
		});
	}

	@Test
	@DisplayName("template con due date e payment e invalid_after_due_date true")
	void UC_1_10_DueDatePaymentInvalidAfterDueDateTrue(){
		List<Placeholder> placeholders = new ArrayList<> ();

		Template template = Template
				.builder()
				.hasDueDate(true)
				.hasPayment(true)
				.messageBody("il comune di Empoli lo avvisa che il pagamento è di ${amount} effettuato da ${payee} in data ${due_date.date} alle ore ${due_date.time}")
				.subject("hello, il subject deve essere almeno dieci caratteri")
				.placeholders(placeholders)
				.build();

		CsvTemplateApplier templateApplier = TemplateApplierFactory.buildCSVTemplateApplier(template);
		Message messaggio = templateApplier.buildMessage("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03T10:15:30,200000000000000000,100000000000000000,true,12345678901");

		assertEquals("il comune di Empoli lo avvisa che il pagamento è di 100000000000000000 effettuato da 12345678901 in data 03/12/2007 alle ore 10:15",messaggio.getMarkdown());
		assertEquals(true,messaggio.getInvalidAfterDueDate());
	}


	@Test
	@DisplayName("template con due date e payment e invalid_after_due_date false")
	void UC_1_12_DueDatePaymentInvalidAfterDueDateFalse(){
		List<Placeholder> placeholders = new ArrayList<> ();

		Template template = Template
				.builder()
				.hasDueDate(true)
				.hasPayment(true)
				.messageBody("il comune di Empoli lo avvisa che il pagamento è di ${amount} effettuato da ${payee} in data ${due_date.date} alle ore ${due_date.time}")
				.subject("hello, il subject deve essere almeno dieci caratteri")
				.placeholders(placeholders)
				.build();

		CsvTemplateApplier templateApplier = TemplateApplierFactory.buildCSVTemplateApplier(template);
		Message messaggio = templateApplier.buildMessage("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03T10:15:30,200000000000000000,100000000000000000,false,12345678901");

		assertEquals("il comune di Empoli lo avvisa che il pagamento è di 100000000000000000 effettuato da 12345678901 in data 03/12/2007 alle ore 10:15",messaggio.getMarkdown());
		assertEquals(false,messaggio.getInvalidAfterDueDate());
	}

	@Test
	@DisplayName("template con due date e payment e invalid_after_due_date assente")
	void UC_1_13_DueDatePaymentInvalidAfterDueDateAssente(){
		List<Placeholder> placeholders = new ArrayList<> ();

		Template template = Template
				.builder()
				.hasDueDate(true)
				.hasPayment(true)
				.messageBody("il comune di Empoli lo avvisa che il pagamento è di ${amount} effettuato da ${payee} in data ${due_date.date} alle ore ${due_date.time}")
				.subject("hello, il subject deve essere almeno dieci caratteri")
				.placeholders(placeholders)
				.build();

		CsvTemplateApplier templateApplier = TemplateApplierFactory.buildCSVTemplateApplier(template);
		Message messaggio = templateApplier.buildMessage("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03T10:15:30,200000000000000000,100000000000000000,,12345678901");

		assertEquals("il comune di Empoli lo avvisa che il pagamento è di 100000000000000000 effettuato da 12345678901 in data 03/12/2007 alle ore 10:15",messaggio.getMarkdown());
		assertEquals(false,messaggio.getInvalidAfterDueDate());
	}

	@Test
	@DisplayName("template con due date e payment e invalid_after_due_date non valido")
	void UC_1_14_DueDatePaymentValidiInvalidAfterDueDateKO(){
		List<Placeholder> placeholders = new ArrayList<> ();

		Template template = Template
				.builder()
				.hasDueDate(true)
				.hasPayment(true)
				.messageBody("il comune di Empoli lo avvisa che il pagamento è di ${amount} effettuato da ${payee} in data ${due_date.date} alle ore ${due_date.time}")
				.subject("hello, il subject deve essere almeno dieci caratteri")
				.placeholders(placeholders)
				.build();

		CsvTemplateApplier templateApplier = TemplateApplierFactory.buildCSVTemplateApplier(template);

		assertThrows(TemplateValidationException.class, () -> {
			templateApplier.buildMessage("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03T10:15:30,200000000000000000,100000000000000000,due_date_non_valido,12345678901");
		});

	}


	@Test
	@DisplayName("template con placeholder stringa senza pattern")
	void UC_1_15_PlaceholderStringaSenzaPattern(){
		List<Placeholder> placeholders = new ArrayList<> ();
		Placeholder placeHolder = Placeholder
				.builder()
				.name("placeholder_senza_pattern")
				.type(Type.STRING)
				.pattern(null)
				.position(1)
				.mandatory(true)
				.build();

		placeholders.add(placeHolder);
		Template template = Template
				.builder()
				.hasDueDate(false)
				.hasPayment(false)
				.messageBody("messaggio semplice con un placeholder senza pattern :  ${placeholder_senza_pattern},il markdown deve essere di almeno 80 caratteri")
				.subject("hello, il subject deve essere almeno dieci caratteri")
				.placeholders(placeholders)
				.build();

		CsvTemplateApplier templateApplier = TemplateApplierFactory.buildCSVTemplateApplier(template);
		Message messaggio = templateApplier.buildMessage("RSSMRO00A00A000A,2007-12-03T10:15:30,PlaceHolderDiProva");

		assertEquals("messaggio semplice con un placeholder senza pattern :  PlaceHolderDiProva,il markdown deve essere di almeno 80 caratteri",messaggio.getMarkdown());
	}



	@Test
	@DisplayName("Template con placeholder stringa con pattern rispettato")
	void UC_1_16_PlaceholderStringaConPatternOK(){
		List<Placeholder> placeholders = new ArrayList<> ();
		Placeholder placeHolder = Placeholder
				.builder()
				.name("placeholder_con_pattern")
				.type(Type.STRING)
				.pattern("^[0123][0-9]")
				.position(1)
				.mandatory(false)
				.build();

		placeholders.add(placeHolder);
		Template template = Template
				.builder()
				.hasDueDate(false)
				.hasPayment(false)
				.messageBody("messaggio semplice con un placeholder stringa con pattern rispettato :  ${placeholder_con_pattern},il markdown deve essere di almeno 80 caratteri")
				.subject("hello, il subject deve essere almeno dieci caratteri")
				.placeholders(placeholders)
				.build();

		CsvTemplateApplier templateApplier = TemplateApplierFactory.buildCSVTemplateApplier(template);
		Message messaggio = templateApplier.buildMessage("RSSMRO00A00A000A,2007-12-03T10:15:30,11");

		assertEquals("messaggio semplice con un placeholder stringa con pattern rispettato :  11,il markdown deve essere di almeno 80 caratteri",messaggio.getMarkdown());
	}

	@Test
	@DisplayName("Template con placeholder stringa con pattern non rispettato")
	void UC_1_17_PlaceholderStringaConPatternKO(){
		List<Placeholder> placeholders = new ArrayList<> ();
		Placeholder placeHolder = Placeholder
				.builder()
				.name("placeholder_con_pattern")
				.type(Type.STRING)
				.pattern("^[0123][0-9]")
				.position(1)
				.mandatory(true)
				.build();

		placeholders.add(placeHolder);
		Template template = Template
				.builder()
				.hasDueDate(false)
				.hasPayment(false)
				.messageBody("messaggio semplice con un placeholder senza pattern :  ${placeholder_con_pattern}")
				.subject("hello, il subject deve essere almeno dieci caratteri")
				.placeholders(placeholders)
				.build();

		CsvTemplateApplier templateApplier = TemplateApplierFactory.buildCSVTemplateApplier(template);
		assertThrows(TemplateValidationException.class, () -> {
			templateApplier.buildMessage("RSSMRO00A00A000A,2007-12-03T10:15:30,aa");
		});
	}


	@Test
	@DisplayName("template con placeholder data valida")
	void UC_1_18_PlaceholderDate(){
		List<Placeholder> placeholders = new ArrayList<> ();
		Placeholder placeHolder = Placeholder
				.builder()
				.name("place_holder_date")
				.type(Type.DATE)
				.pattern(null)
				.position(1)
				.mandatory(true)
				.build();

		placeholders.add(placeHolder);
		Template template = Template
				.builder()
				.hasDueDate(false)
				.hasPayment(false)
				.messageBody("messaggio semplice con un placeholder con data :  ${place_holder_date},il markdown deve essere di almeno 80 caratteri")
				.subject("hello, il subject deve essere almeno dieci caratteri")
				.placeholders(placeholders)
				.build();

		CsvTemplateApplier templateApplier = TemplateApplierFactory.buildCSVTemplateApplier(template);
		Message messaggio = templateApplier.buildMessage("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03");

		assertEquals("messaggio semplice con un placeholder con data :  03/12/2007,il markdown deve essere di almeno 80 caratteri",messaggio.getMarkdown());
	}


	@Test
	@DisplayName("template con placeholder data non valida")
	void UC_1_19_PlaceholderDateKO(){
		List<Placeholder> placeholders = new ArrayList<> ();
		Placeholder placeHolder = Placeholder
				.builder()
				.name("place_holder_date")
				.type(Type.DATE)
				.pattern(null)
				.position(1)
				.mandatory(false)
				.build();

		placeholders.add(placeHolder);
		Template template = Template
				.builder()
				.hasDueDate(false)
				.hasPayment(false)
				.messageBody("messaggio semplice con un placeholder con data :  ${place_holder_date}")
				.subject("hello, il subject deve essere almeno dieci caratteri")
				.placeholders(placeholders)
				.build();

		CsvTemplateApplier templateApplier = TemplateApplierFactory.buildCSVTemplateApplier(template);
		assertThrows(TemplateValidationException.class, () -> {
			templateApplier.buildMessage("RSSMRO00A00A000A,2007-12-03T10:15:30,30,2007-12-12");
		});
	}

	@Test
	@DisplayName("template con placeholder data verbosa")
	void UC_1_20_PlaceholderDateVerbosa(){
		List<Placeholder> placeholders = new ArrayList<> ();
		Placeholder placeHolder = Placeholder
				.builder()
				.name("place_holder_date")
				.type(Type.DATE)
				.pattern(null)
				.position(1)
				.mandatory(false)
				.build();

		placeholders.add(placeHolder);
		Template template = Template
				.builder()
				.hasDueDate(false)
				.hasPayment(false)
				.messageBody("messaggio semplice con un placeholder con data :  ${place_holder_date.verbose},il markdown deve essere di almeno 80 caratteri")
				.subject("hello, il subject deve essere almeno dieci caratteri")
				.placeholders(placeholders)
				.build();

		CsvTemplateApplier templateApplier = TemplateApplierFactory.buildCSVTemplateApplier(template);
		Message messaggio = templateApplier.buildMessage("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-12");

		assertEquals("messaggio semplice con un placeholder con data :  mer 12 12 2007,il markdown deve essere di almeno 80 caratteri",messaggio.getMarkdown());
	}

	@Test
	@DisplayName("template con placeholder date time in tutti i formati")
	void UC_1_21_PlaceholderEveryDateTime(){
		List<Placeholder> placeholders = new ArrayList<> ();
		Placeholder placeHolder = Placeholder
				.builder()
				.name("appointment")
				.type(Type.DATETIME)
				.pattern(null)
				.position(1)
				.mandatory(false)
				.build();
		placeholders.add(placeHolder);
		Template template = Template
				.builder()
				.hasDueDate(false)
				.hasPayment(false)
				.messageBody("messaggio semplice con un placeholder con date time :  ${appointment}, ${appointment.date}, ${appointment.date.verbose}, ${appointment.time}, ${appointment.verbose}")
				.subject("hello, il subject deve essere almeno dieci caratteri")
				.placeholders(placeholders)
				.build();

		CsvTemplateApplier templateApplier = TemplateApplierFactory.buildCSVTemplateApplier(template);
		Message messaggio = templateApplier.buildMessage("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03T10:15:30");

		assertEquals("messaggio semplice con un placeholder con date time :  03/12/2007 10:15, 03/12/2007, lun 03 12 2007, 10:15, lun 03 12 2007 alle ore 10:15",messaggio.getMarkdown());
	}
}
