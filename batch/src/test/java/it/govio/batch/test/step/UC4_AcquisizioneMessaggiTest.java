	package it.govio.batch.test.step;

	import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

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
	import it.govio.batch.exception.TemplateValidationException;


@SpringBootTest(classes = Application.class)
class UC4_AcquisizioneMessaggiTest {
		@Test
		@DisplayName("template senza placeholders")
		void Test1(){
			Set<GovioTemplatePlaceholderEntity>	govioTemplatePlaceholders = new HashSet<> ();
			GovioTemplateEntity govioTemplate = GovioTemplateEntity
					.builder()
					.hasDueDate(false)
					.hasPayment(false)
					.name("no placeholders")
					.messageBody("Il Comune di Empoli le dice hello, il markdown deve essere di almeno 80 caratteri")
					.subject("hello, il subject deve essere almeno dieci caratteri")
					.govioTemplatePlaceholders(govioTemplatePlaceholders)
					.build();
			TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
			
		    assertThrows(TemplateValidationException.class, () -> {
		    	templateApplier.buildGovioMessageEntity("");
		    });
			}
				
		@Test
		@DisplayName("Messaggio senza due date, senza payment, senza placeholder")
		void Test4(){
			Set<GovioTemplatePlaceholderEntity>	govioTemplatePlaceholders = new HashSet<> ();
			GovioTemplateEntity govioTemplate = GovioTemplateEntity
					.builder()
					.hasDueDate(false)
					.hasPayment(false)
					.name("Messaggio senza due date, senza payment, senza placeholder")
					.messageBody("il comune di Empoli avvisa il signor ${taxcode}, , il markdown deve essere di almeno 80 caratteri")
					.subject("hello, il subject deve essere almeno dieci caratteri")
					.govioTemplatePlaceholders(govioTemplatePlaceholders)
					.build();
			TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
			
			GovioMessageEntity messaggio = templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30");
			assertEquals("il comune di Empoli avvisa il signor RSSMRO00A00A000A, , il markdown deve essere di almeno 80 caratteri",messaggio.getMarkdown());
		}

		@Test
		@DisplayName("template con due_date e data valida")
		void Test5(){
			Set<GovioTemplatePlaceholderEntity>	govioTemplatePlaceholders = new HashSet<> ();
			
			GovioTemplateEntity govioTemplate = GovioTemplateEntity
					.builder()
					.hasDueDate(true)
					.hasPayment(false)
					.name("template con due_date e data valida")
					.messageBody("il comune di Empoli lo avvisa che la data di scadenza è ${due_date.date} alle ${due_date.time},il markdown deve essere di almeno 80 caratteri")
					.subject("hello, il subject deve essere almeno dieci caratteri")
					.govioTemplatePlaceholders(govioTemplatePlaceholders)
					.build();
			
			TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
			GovioMessageEntity messaggio = templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03T10:15:30");
			assertEquals("il comune di Empoli lo avvisa che la data di scadenza è 03/12/2007 alle 10:15,il markdown deve essere di almeno 80 caratteri",messaggio.getMarkdown());
		}
		@Test
		@DisplayName("template con due_date e data non valida")
		void Test6(){
			Set<GovioTemplatePlaceholderEntity>	govioTemplatePlaceholders = new HashSet<> ();
			
			GovioTemplateEntity govioTemplate = GovioTemplateEntity
					.builder()
					.hasDueDate(true)
					.hasPayment(false)
					.name("template con due date e data non valida")
					.messageBody("il comune di Empoli lo avvisa che la data di scadenza è ${due_date.date} alle ${due_date.time}")
					.subject("hello, il subject deve essere almeno dieci caratteri")
					.govioTemplatePlaceholders(govioTemplatePlaceholders)
					.build();
			
			TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
			    assertThrows(TemplateValidationException.class, () -> {
					templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-0310:15:30");
			    });
		}

		@Test
		@DisplayName("Template con due date e data assente")
		void Test7(){
			Set<GovioTemplatePlaceholderEntity>	govioTemplatePlaceholders = new HashSet<> ();
			
			GovioTemplateEntity govioTemplate = GovioTemplateEntity
					.builder()
					.hasDueDate(true)
					.hasPayment(false)
					.name("Template con due date e data assente")
					.messageBody("il comune di Empoli lo avvisa che la data di scadenza è ${due_date.date} alle ${due_date.time}")
					.subject("hello, il subject deve essere almeno dieci caratteri")
					.govioTemplatePlaceholders(govioTemplatePlaceholders)
					.build();
			
			TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
			    assertThrows(TemplateValidationException.class, () -> {
					templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30");
			    });
		}

		@Test
		@DisplayName("template con payment valido")
		void Test8(){
			Set<GovioTemplatePlaceholderEntity>	govioTemplatePlaceholders = new HashSet<> ();
			
			GovioTemplateEntity govioTemplate = GovioTemplateEntity
					.builder()
					.hasDueDate(true)
					.hasPayment(true)
					.name("template con payment valido")
					.messageBody("il comune di Empoli lo avvisa che il pagamento è di ${amount} effettuato da ${payee} in data ${due_date.date} alle ore ${due_date.time}")
					.subject("hello, il subject deve essere almeno dieci caratteri")
					.govioTemplatePlaceholders(govioTemplatePlaceholders)
					.build();
			
			TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
			GovioMessageEntity messaggio = templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03T10:15:30,200000000000000000,100000000000000000,false,12345678901");

			assertEquals("il comune di Empoli lo avvisa che il pagamento è di 100000000000000000 effettuato da 12345678901 in data 03/12/2007 alle ore 10:15",messaggio.getMarkdown());
		}
		
		@Test
		@DisplayName("Template con payment ed uno o piu' parametri non validi o assenti")
		void Test9(){
			Set<GovioTemplatePlaceholderEntity>	govioTemplatePlaceholders = new HashSet<> ();
			
			GovioTemplateEntity govioTemplate = GovioTemplateEntity
					.builder()
					.hasDueDate(false)
					.hasPayment(true)
					.name("Template con payment ed uno o piu' parametri non validi o assenti")
					.messageBody("il comune di Empoli lo avvisa che il pagamento è di ${amount} effettuato da ${payee} in data ${due_date.date} alle ore ${due_date.time}")
					.subject("hello, il subject deve essere almeno dieci caratteri")
					.govioTemplatePlaceholders(govioTemplatePlaceholders)
					.build();
			
			TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
		    assertThrows(TemplateValidationException.class, () -> {
				templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30");
		    });
		}
		
		@Test
		@DisplayName("template con due date e payment e invalid_after_due_date true")
		void Test10(){
			Set<GovioTemplatePlaceholderEntity>	govioTemplatePlaceholders = new HashSet<> ();
			
			GovioTemplateEntity govioTemplate = GovioTemplateEntity
					.builder()
					.hasDueDate(true)
					.hasPayment(true)
					.name("template con due date e payment e invalid_after_due_date true")
					.messageBody("il comune di Empoli lo avvisa che il pagamento è di ${amount} effettuato da ${payee} in data ${due_date.date} alle ore ${due_date.time}")
					.subject("hello, il subject deve essere almeno dieci caratteri")
					.govioTemplatePlaceholders(govioTemplatePlaceholders)
					.build();
			
			TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
			GovioMessageEntity messaggio = templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03T10:15:30,200000000000000000,100000000000000000,true,12345678901");

			assertEquals("il comune di Empoli lo avvisa che il pagamento è di 100000000000000000 effettuato da 12345678901 in data 03/12/2007 alle ore 10:15",messaggio.getMarkdown());
			assertEquals(true,messaggio.getInvalidAfterDueDate());
		}

		
		@Test
		@DisplayName("template con due date e payment e invalid_after_due_date false")
		void Test11(){
			Set<GovioTemplatePlaceholderEntity>	govioTemplatePlaceholders = new HashSet<> ();
			
			GovioTemplateEntity govioTemplate = GovioTemplateEntity
					.builder()
					.hasDueDate(true)
					.hasPayment(true)
					.name("template con due date e payment e invalid_after_due_date false")
					.messageBody("il comune di Empoli lo avvisa che il pagamento è di ${amount} effettuato da ${payee} in data ${due_date.date} alle ore ${due_date.time}")
					.subject("hello, il subject deve essere almeno dieci caratteri")
					.govioTemplatePlaceholders(govioTemplatePlaceholders)
					.build();
			
			TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
			GovioMessageEntity messaggio = templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03T10:15:30,200000000000000000,100000000000000000,false,12345678901");

			assertEquals("il comune di Empoli lo avvisa che il pagamento è di 100000000000000000 effettuato da 12345678901 in data 03/12/2007 alle ore 10:15",messaggio.getMarkdown());
			assertEquals(false,messaggio.getInvalidAfterDueDate());
		}
		
		@Test
		@DisplayName("template con due date e payment e invalid_after_due_date assente")
		void Test12(){
			Set<GovioTemplatePlaceholderEntity>	govioTemplatePlaceholders = new HashSet<> ();
			
			GovioTemplateEntity govioTemplate = GovioTemplateEntity
					.builder()
					.hasDueDate(true)
					.hasPayment(true)
					.name("template con due date e payment e invalid_after_due_date assente")
					.messageBody("il comune di Empoli lo avvisa che il pagamento è di ${amount} effettuato da ${payee} in data ${due_date.date} alle ore ${due_date.time}")
					.subject("hello, il subject deve essere almeno dieci caratteri")
					.govioTemplatePlaceholders(govioTemplatePlaceholders)
					.build();
			
			TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
			GovioMessageEntity messaggio = templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03T10:15:30,200000000000000000,100000000000000000,,12345678901");

			assertEquals("il comune di Empoli lo avvisa che il pagamento è di 100000000000000000 effettuato da 12345678901 in data 03/12/2007 alle ore 10:15",messaggio.getMarkdown());
			assertEquals(false,messaggio.getInvalidAfterDueDate());
		}
		
		@Test
		@DisplayName("template con due date e payment e invalid_after_due_date non valido")
		void Test13(){
			Set<GovioTemplatePlaceholderEntity>	govioTemplatePlaceholders = new HashSet<> ();
			
			GovioTemplateEntity govioTemplate = GovioTemplateEntity
					.builder()
					.hasDueDate(true)
					.hasPayment(true)
					.name("template con due date e payment e invalid_after_due_date non valido")
					.messageBody("il comune di Empoli lo avvisa che il pagamento è di ${amount} effettuato da ${payee} in data ${due_date.date} alle ore ${due_date.time}")
					.subject("hello, il subject deve essere almeno dieci caratteri")
					.govioTemplatePlaceholders(govioTemplatePlaceholders)
					.build();
			
			TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
			GovioMessageEntity messaggio = templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03T10:15:30,200000000000000000,100000000000000000,due_date_non_valido,12345678901");

			assertEquals("il comune di Empoli lo avvisa che il pagamento è di 100000000000000000 effettuato da 12345678901 in data 03/12/2007 alle ore 10:15",messaggio.getMarkdown());
			assertEquals(false,messaggio.getInvalidAfterDueDate());

		}
		

		@Test
		@DisplayName("template con placeholder stringa senza pattern")
		void Test14(){
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
					.mandatory(true)
					.govioPlaceholder(placeHolder)
					.govioTemplate(null)
					.build();

			govioTemplatePlaceholders.add(placeHolderEntity);
			GovioTemplateEntity govioTemplate = GovioTemplateEntity
					.builder()
					.hasDueDate(false)
					.hasPayment(false)
					.name("template con placeholder stringa senza pattern")
					.messageBody("messaggio semplice con un placeholder senza pattern :  ${placeholder_senza_pattern},il markdown deve essere di almeno 80 caratteri")
					.subject("hello, il subject deve essere almeno dieci caratteri")
					.govioTemplatePlaceholders(govioTemplatePlaceholders)
					.build();

			TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
			GovioMessageEntity messaggio = templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30,PlaceHolderDiProva");

			assertEquals("messaggio semplice con un placeholder senza pattern :  PlaceHolderDiProva,il markdown deve essere di almeno 80 caratteri",messaggio.getMarkdown());
		}
		
		
		
		@Test
		@DisplayName("Template con placeholder stringa con pattern rispettato")
		void Test15(){
			Set<GovioTemplatePlaceholderEntity>	govioTemplatePlaceholders = new HashSet<> ();
			GovioPlaceholderEntity placeHolder = GovioPlaceholderEntity
			.builder()
			.name("placeholder_senza_pattern")
			.type(Type.STRING)
			.pattern("^[0123][0-9]")
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
					.name("Template con placeholder stringa con pattern rispettato")
					.messageBody("messaggio semplice con un placeholder stringa con pattern rispettato :  ${placeholder_senza_pattern},il markdown deve essere di almeno 80 caratteri")
					.subject("hello, il subject deve essere almeno dieci caratteri")
					.govioTemplatePlaceholders(govioTemplatePlaceholders)
					.build();

			TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
			GovioMessageEntity messaggio = templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30,11");

			assertEquals("messaggio semplice con un placeholder stringa con pattern rispettato :  11,il markdown deve essere di almeno 80 caratteri",messaggio.getMarkdown());
		}

		@Test
		@DisplayName("Template con placeholder stringa con pattern non rispettato")
		void Test16(){
			Set<GovioTemplatePlaceholderEntity>	govioTemplatePlaceholders = new HashSet<> ();
			GovioPlaceholderEntity placeHolder = GovioPlaceholderEntity
			.builder()
			.name("placeholder_senza_pattern")
			.type(Type.STRING)
			.pattern("^[0123][0-9]")
			.build();
			
			GovioTemplatePlaceholderEntity placeHolderEntity = GovioTemplatePlaceholderEntity
					.builder()
					.index(1)
					.mandatory(true)
					.govioPlaceholder(placeHolder)
					.govioTemplate(null)
					.build();

			govioTemplatePlaceholders.add(placeHolderEntity);
			GovioTemplateEntity govioTemplate = GovioTemplateEntity
					.builder()
					.hasDueDate(false)
					.hasPayment(false)
					.name("Template con placeholder stringa con pattern non rispettato")
					.messageBody("messaggio semplice con un placeholder senza pattern :  ${placeholder_senza_pattern}")
					.subject("hello, il subject deve essere almeno dieci caratteri")
					.govioTemplatePlaceholders(govioTemplatePlaceholders)
					.build();

			TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
		    assertThrows(TemplateValidationException.class, () -> {
		    	templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30,aa");
		    });
		}

		
		@Test
		@DisplayName("template con placeholder data valida")
		void Test17(){
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
					.mandatory(true)
					.govioPlaceholder(placeHolder)
					.govioTemplate(null)
					.build();

			govioTemplatePlaceholders.add(placeHolderEntity);
			GovioTemplateEntity govioTemplate = GovioTemplateEntity
					.builder()
					.hasDueDate(false)
					.hasPayment(false)
					.name("template con placeholder data valida")
					.messageBody("messaggio semplice con un placeholder con data :  ${place_holder_date},il markdown deve essere di almeno 80 caratteri")
					.subject("hello, il subject deve essere almeno dieci caratteri")
					.govioTemplatePlaceholders(govioTemplatePlaceholders)
					.build();

			TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
			GovioMessageEntity messaggio = templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03");

			assertEquals("messaggio semplice con un placeholder con data :  03/12/2007,il markdown deve essere di almeno 80 caratteri",messaggio.getMarkdown());
		}

		
		@Test
		@DisplayName("template con placeholder data non valida")
		void Test18(){
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
					.name("template con placeholder data non valida")
					.messageBody("messaggio semplice con un placeholder con data :  ${place_holder_date}")
					.subject("hello, il subject deve essere almeno dieci caratteri")
					.govioTemplatePlaceholders(govioTemplatePlaceholders)
					.build();

			TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
		    assertThrows(TemplateValidationException.class, () -> {
				templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30,30,2007-12-12");
		    });
		}
		
		@Test
		@DisplayName("template con placeholder data verbosa")
		void Test19(){
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
					.name("template con placeholder data verbosa")
					.messageBody("messaggio semplice con un placeholder con data :  ${place_holder_date.verbose},il markdown deve essere di almeno 80 caratteri")
					.subject("hello, il subject deve essere almeno dieci caratteri")
					.govioTemplatePlaceholders(govioTemplatePlaceholders)
					.build();

			TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
			GovioMessageEntity messaggio = templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-12");
				
				assertEquals("messaggio semplice con un placeholder con data :  mer 12 12 2007,il markdown deve essere di almeno 80 caratteri",messaggio.getMarkdown());
		}
		
		@Test
		@DisplayName("template con placeholder date time in tutti i formati")
		void Test20(){
			Set<GovioTemplatePlaceholderEntity>	govioTemplatePlaceholders = new HashSet<> ();
			GovioPlaceholderEntity placeHolder = GovioPlaceholderEntity
			.builder()
			.name("appointment")
			.type(Type.DATETIME)
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
					.name("template con placeholder date time in tutti i formati")
					.messageBody("messaggio semplice con un placeholder con date time :  ${appointment}, ${appointment.date}, ${appointment.date.verbose}, ${appointment.time}, ${appointment.verbose}")
					.subject("hello, il subject deve essere almeno dieci caratteri")
					.govioTemplatePlaceholders(govioTemplatePlaceholders)
					.build();

			TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
			GovioMessageEntity messaggio = templateApplier.buildGovioMessageEntity("RSSMRO00A00A000A,2007-12-03T10:15:30,2007-12-03T10:15:30");
				
				assertEquals("messaggio semplice con un placeholder con date time :  03/12/2007 10:15, 03/12/2007, lun 03 12 2007, 10:15, lun 03 12 2007 alle ore 10:15",messaggio.getMarkdown());
		}


	}