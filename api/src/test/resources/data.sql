-- Servizio di test

-- INSERT INTO govio_templates(id,message_body,subject,has_due_date,has_payment) VALUES (1,'Il Comune la informa che il ${duedate.date} scadrà la sua Carta di Identità. Potrà rinnovarlo il ${appointment.verbose} presso {at}.','Scadenza CIE ${taxcode}',true,false);
INSERT INTO govio_templates(id) VALUES (1);
ALTER SEQUENCE seq_govio_templates RESTART WITH 2;	

-- INSERT INTO govio_service_instances(id,id_govhub_service,id_govhub_organization,id_govio_template,apikey) VALUES (1,1,1,1,'17886617e07d47e8b1ba314f2f1e3052');
-- ALTER SEQUENCE seq_govio_service_instances RESTART WITH 2;

-- INSERT INTO govio_placeholders(id,name,type,example) VALUES (1,'appointment','DATE','2100-12-31T12:00');
-- INSERT INTO govio_placeholders(id,name,type,example) VALUES (2,'at','STRING','Ufficio numero 1');
-- ALTER SEQUENCE seq_govio_placeholders RESTART WITH 3;

-- INSERT INTO govio_template_placeholders(id_govio_template,id_govio_placeholder, mandatory, index) VALUES ('1','1', true, 1);
-- INSERT INTO govio_template_placeholders(id_govio_template,id_govio_placeholder, mandatory, index) VALUES ('1','2', true, 2);