-- Servizio di test

INSERT INTO govio_templates(id,message_body,subject,has_due_date,has_payment) VALUES (1,'Il Comune la informa che il ${duedate.date} scadrà la sua Carta di Identità. Potrà rinnovarlo il ${appointment.verbose} presso {at}.','Scadenza CIE ${taxcode}',true,false);
INSERT INTO govio_templates(id,message_body,subject,has_due_date,has_payment) VALUES (2,'Salve, con la presente la informiamo che in data ${due_date} scadrà la Carta di Identità elettronica numero ${cie.uppercase}. Per maggiori informazioni sulle modalità di rinnovo può consultare https://comune.dimostrativo.it.','Scadenza CIE n. ${cie.uppercase}',true,false);
ALTER SEQUENCE seq_govio_templates RESTART WITH 3;	

INSERT INTO govio_services(id,id_govio_template) VALUES (1,'1');
INSERT INTO govio_services(id,id_govio_template) VALUES (2,'2');
 ALTER SEQUENCE seq_govio_service_instances RESTART WITH 3;

INSERT INTO govio_service_instances(id,id_govio_service,id_govio_template,apikey) VALUES (1,'1','1','17886617e07d47e8b1ba314f2f1e3052');
 INSERT INTO govio_service_instances(id,id_govio_service,id_govio_template,apikey) VALUES (2,'2','2','17886617e07d47e8b1ba314f2f1e3052');
 ALTER SEQUENCE seq_govio_service_instances RESTART WITH 3;

INSERT INTO govio_placeholders(id,name,type,example) VALUES (1,'appointment','DATE','2100-12-31T12:00');
INSERT INTO govio_placeholders(id,name,type,example) VALUES (2,'at','STRING','Ufficio numero 1');
INSERT INTO govio_placeholders(id,name,type,example) VALUES (3,'cie','STRING','CA000000AA');
ALTER SEQUENCE seq_govio_placeholders RESTART WITH 4;

INSERT INTO govio_template_placeholders(id_govio_template,id_govio_placeholder, mandatory, index) VALUES ('1','1', true, 1);
INSERT INTO govio_template_placeholders(id_govio_template,id_govio_placeholder, mandatory, index) VALUES ('1','2', true, 2);
INSERT INTO govio_template_placeholders(id_govio_template,id_govio_placeholder, mandatory, index) VALUES ('2','3', true, 1);