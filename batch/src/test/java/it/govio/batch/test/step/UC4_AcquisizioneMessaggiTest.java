package it.govio.batch.test.step;

import org.springframework.boot.test.context.SpringBootTest;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import it.govio.batch.Application;
import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioPlaceholderEntity;
import it.govio.batch.entity.GovioPlaceholderEntity.Type;
import it.govio.batch.entity.GovioTemplateEntity;
import it.govio.batch.entity.GovioTemplatePlaceholderEntity;
import it.govio.batch.step.template.TemplateApplier;
import it.govio.batch.step.template.GovioTemplateApplierFactory;

@SpringBootTest(classes = Application.class)
class UC4_AcquisizioneMessaggiTest {
	@Test
	@DisplayName("template senza placeholders")
	void UC4_1_NoPlaceholders(){
		Set<GovioTemplatePlaceholderEntity>	govioTemplatePlaceholders = new HashSet<> ();
		GovioTemplateEntity govioTemplate = GovioTemplateEntity
				.builder()
				.hasDueDate(false)
				.hasPayment(false)
				.name("no placeholders")
				.messageBody("Il Comune di Empoli le dice hello")
				.subject("hello")
				.govioTemplatePlaceholders(govioTemplatePlaceholders)
				.build();
		TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);

		GovioMessageEntity messaggio = templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03T10:15:30");
		assertEquals("Il Comune di Empoli le dice hello", messaggio.getMarkdown());
	}

	@Test
	@DisplayName("messaggio vuoto")
	void UC4_2_EmptyMessage(){
		Set<GovioTemplatePlaceholderEntity>	govioTemplatePlaceholders = new HashSet<> ();
		GovioTemplateEntity govioTemplate = GovioTemplateEntity
				.builder()
				.hasDueDate(false)
				.hasPayment(false)
				.name("no placeholders")
				.messageBody("")
				.subject("hello")
				.govioTemplatePlaceholders(govioTemplatePlaceholders)
				.build();
		TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
		GovioMessageEntity messaggio = templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03T10:15:30");
		assertEquals("",messaggio.getMarkdown());
	}

	@Test
	@DisplayName("messaggio assente")
	void UC4_3_MissingMessage(){
		Set<GovioTemplatePlaceholderEntity>	govioTemplatePlaceholders = new HashSet<> ();
		GovioTemplateEntity govioTemplate = GovioTemplateEntity
				.builder()
				.hasDueDate(false)
				.hasPayment(false)
				.name("no placeholders")
				.messageBody(null)
				.subject("hello")
				.govioTemplatePlaceholders(govioTemplatePlaceholders)
				.build();
		TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
		GovioMessageEntity messaggio = templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03T10:15:30");
		assertNull(messaggio.getMarkdown());
	}

	@Test
	@DisplayName("template con due_date e data valida")
	void UC4_4_DueDate(){
		Set<GovioTemplatePlaceholderEntity>	govioTemplatePlaceholders = new HashSet<> ();

		GovioTemplateEntity govioTemplate = GovioTemplateEntity
				.builder()
				.hasDueDate(true)
				.hasPayment(false)
				.name("template con due date")
				.messageBody("il comune di Empoli lo avvisa che la data di scadenza è ${due_date.date} alle ${due_date.time}")
				.subject("hello")
				.govioTemplatePlaceholders(govioTemplatePlaceholders)
				.build();

		TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
		GovioMessageEntity messaggio = templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03T10:15:30");
		assertEquals("il comune di Empoli lo avvisa che la data di scadenza è 03/12/2007 alle 10:15", messaggio.getMarkdown());
	}

	@Test
	@DisplayName("template con payment valido")
	void UC4_5_Payment(){
		Set<GovioTemplatePlaceholderEntity>	govioTemplatePlaceholders = new HashSet<> ();

		GovioTemplateEntity govioTemplate = GovioTemplateEntity
				.builder()
				.hasDueDate(false)
				.hasPayment(true)
				.name("template con due date")
				.messageBody("il comune di Empoli lo avvisa che il pagamento è di ${amount} effettuato da ${payee} in data ${due_date.date} alle ore ${due_date.time}")
				.subject("hello")
				.govioTemplatePlaceholders(govioTemplatePlaceholders)
				.build();

		TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
		GovioMessageEntity messaggio = templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03T10:15:30,200000000000000000,100000000000000000,false,12345678901");

		assertEquals("il comune di Empoli lo avvisa che il pagamento è di 100000000000000000 effettuato da 12345678901 in data 03/12/2007 alle ore 10:15", messaggio.getMarkdown());
	}


	@Test
	@DisplayName("template con due date e payment e invalid_after_due_date true")
	void UC4_6_DueDateAndPaymentAndInvalid(){
		Set<GovioTemplatePlaceholderEntity>	govioTemplatePlaceholders = new HashSet<> ();

		GovioTemplateEntity govioTemplate = GovioTemplateEntity
				.builder()
				.hasDueDate(true)
				.hasPayment(true)
				.name("template con due date")
				.messageBody("il comune di Empoli lo avvisa che il pagamento è di ${amount} effettuato da ${payee} in data ${due_date.date} alle ore ${due_date.time}")
				.subject("hello")
				.govioTemplatePlaceholders(govioTemplatePlaceholders)
				.build();

		TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
		GovioMessageEntity messaggio = templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03T10:15:30,200000000000000000,100000000000000000,true,12345678901");

		assertEquals("il comune di Empoli lo avvisa che il pagamento è di 100000000000000000 effettuato da 12345678901 in data 03/12/2007 alle ore 10:15", messaggio.getMarkdown());
		assertTrue(messaggio.getInvalidAfterDueDate());
	}


	@Test
	@DisplayName("template con due date e payment e invalid_after_due_date false")
	void UC4_7_DueDateAndPaymentAndNotInvalid(){
		Set<GovioTemplatePlaceholderEntity>	govioTemplatePlaceholders = new HashSet<> ();

		GovioTemplateEntity govioTemplate = GovioTemplateEntity
				.builder()
				.hasDueDate(true)
				.hasPayment(true)
				.name("template con due date")
				.messageBody("il comune di Empoli lo avvisa che il pagamento è di ${amount} effettuato da ${payee} in data ${due_date.date} alle ore ${due_date.time}")
				.subject("hello")
				.govioTemplatePlaceholders(govioTemplatePlaceholders)
				.build();

		TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
		GovioMessageEntity messaggio = templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03T10:15:30,200000000000000000,100000000000000000,false,12345678901");

		assertEquals("il comune di Empoli lo avvisa che il pagamento è di 100000000000000000 effettuato da 12345678901 in data 03/12/2007 alle ore 10:15",messaggio.getMarkdown());
		assertFalse(messaggio.getInvalidAfterDueDate());
	}

	/*
		@Test
		@DisplayName("template con due date e payment e invalid_after_due_date assente")
		void Test8(){
			Set<GovioTemplatePlaceholderEntity>	govioTemplatePlaceholders = new HashSet<> ();

			GovioTemplateEntity govioTemplate = GovioTemplateEntity
					.builder()
					.hasDueDate(true)
					.hasPayment(true)
					.name("template con due date")
					.messageBody("il comune di Empoli lo avvisa che il pagamento è di ${amount}")
					.subject("hello")
					.govioTemplatePlaceholders(govioTemplatePlaceholders)
					.build();

			TemplateApplier a = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
			GovioMessageEntity b = a.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03T10:15:30,200000000000000000,100000000000000000,12345678901");

			System.out.println(b.getMarkdown());
		}
	 */

	@Test
	@DisplayName("template con due date e payment e invalid_after_due_date non valido")
	void UC4_8_MalformedInvalidAfterDueDate(){
		Set<GovioTemplatePlaceholderEntity>	govioTemplatePlaceholders = new HashSet<> ();

		GovioTemplateEntity govioTemplate = GovioTemplateEntity
				.builder()
				.hasDueDate(true)
				.hasPayment(true)
				.name("template con due date")
				.messageBody("il comune di Empoli lo avvisa che il pagamento è di ${amount} effettuato da ${payee} in data ${due_date.date} alle ore ${due_date.time}")
				.subject("hello")
				.govioTemplatePlaceholders(govioTemplatePlaceholders)
				.build();

		TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
		GovioMessageEntity messaggio = templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03T10:15:30,200000000000000000,100000000000000000,due_date_non_valido,12345678901");


		assertEquals("il comune di Empoli lo avvisa che il pagamento è di 100000000000000000 effettuato da 12345678901 in data 03/12/2007 alle ore 10:15", messaggio.getMarkdown());
		assertFalse(messaggio.getInvalidAfterDueDate());

	}


	@Test
	@DisplayName("template con placeholder stringa senza pattern")
	void UC4_9_PlaceholderWithoutPattern(){
		Set<GovioTemplatePlaceholderEntity>	govioTemplatePlaceholders = new HashSet<> ();
		GovioPlaceholderEntity placeHolder = GovioPlaceholderEntity
				.builder()
				.name("placeholder_senza_pattern")
				.type(Type.STRING)
				.pattern(null)
				.build();

		GovioTemplatePlaceholderEntity placeHolderEntity = GovioTemplatePlaceholderEntity
				.builder()
				.index(1)
				.mandatory(false)
				.govioPlaceholder(placeHolder)
				.govioTemplate(null)
				.build();

		govioTemplatePlaceholders.add(placeHolderEntity);
		GovioTemplateEntity govioTemplate = GovioTemplateEntity
				.builder()
				.hasDueDate(true)
				.hasPayment(true)
				.name("template con due date")
				.messageBody("messaggio semplice con un placeholder senza pattern :  ${placeholder_senza_pattern}")
				.subject("hello")
				.govioTemplatePlaceholders(govioTemplatePlaceholders)
				.build();

		TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
		GovioMessageEntity messaggio = templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03T10:15:30,200000000000000000,100000000000000000,false,12345678901,PlaceHolderDiProva");

		assertEquals("messaggio semplice con un placeholder senza pattern :  PlaceHolderDiProva", messaggio.getMarkdown());
		assertFalse(messaggio.getInvalidAfterDueDate());
	}

	@Test
	@DisplayName("template con placeholder data valida")
	void UC4_11_DatePlaceholder(){
		Set<GovioTemplatePlaceholderEntity>	govioTemplatePlaceholders = new HashSet<> ();
		GovioPlaceholderEntity placeHolder = GovioPlaceholderEntity
				.builder()
				.name("place_holder_date")
				.type(Type.DATE)
				.pattern(null)
				.build();

		GovioTemplatePlaceholderEntity placeHolderEntity = GovioTemplatePlaceholderEntity
				.builder()
				.index(1)
				.mandatory(false)
				.govioPlaceholder(placeHolder)
				.govioTemplate(null)
				.build();

		govioTemplatePlaceholders.add(placeHolderEntity);
		GovioTemplateEntity govioTemplate = GovioTemplateEntity
				.builder()
				.hasDueDate(false)
				.hasPayment(false)
				.name("template con due date")
				.messageBody("messaggio semplice con un placeholder con data :  ${place_holder_date}")
				.subject("hello")
				.govioTemplatePlaceholders(govioTemplatePlaceholders)
				.build();

		TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
		GovioMessageEntity messaggio = templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03T10:15:30,2007-12-03");

		assertEquals("messaggio semplice con un placeholder con data :  03/12/2007", messaggio.getMarkdown());
	}

}