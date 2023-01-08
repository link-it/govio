-- Dati di test per configurare un template per la spedizione di un messaggio che avvisa della scadenza di una carta di identità

INSERT INTO govio_templates(id,message_body,subject,has_due_date,has_payment) VALUES (1,'Salve, con la presente la informiamo che in data ${due_date} scadrà la Carta di Identità elettronica numero ${cie.uppercase}. Per maggiori informazioni sulle modalità di rinnovo può consultare https://comune.dimostrativo.it.','Scadenza CIE n. ${cie.uppercase}',true,false);
ALTER SEQUENCE seq_govio_templates RESTART WITH 2;

INSERT INTO govio_services(id,id_govio_template) VALUES (1,'1');
ALTER SEQUENCE seq_govio_service_instances RESTART WITH 2;

INSERT INTO govio_service_instances(id,id_govio_service,id_govio_template,apikey) VALUES (1,'1','1','17886617e07d47e8b1ba314f2f1e3052');
ALTER SEQUENCE seq_govio_service_instances RESTART WITH 2;

INSERT INTO govio_placeholders(id,name,type,example) VALUES (1,'cie','STRING','CA000000AA');
ALTER SEQUENCE seq_govio_placeholders RESTART WITH 2;

INSERT INTO govio_template_placeholders(id_govio_template,id_govio_placeholder, mandatory, position) VALUES ('1','1', true, 1);