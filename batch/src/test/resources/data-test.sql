-- Servizio di test

-- Le tabelle del batch vengono create DOPO l'esecuzione di questo script.
-- Per fare in modo che i test si ritrovino una situazione pulita, droppo le tabelle ora in modo
-- che poi vengano ricreate nuove.

DROP TABLE IF EXISTS BATCH_JOB_EXECUTION_PARAMS;
DROP TABLE IF EXISTS  BATCH_JOB_EXECUTION_CONTEXT;

DROP TABLE IF EXISTS BATCH_STEP_EXECUTION_CONTEXT;
DROP TABLE IF EXISTS BATCH_STEP_EXECUTION;

DROP TABLE IF EXISTS BATCH_JOB_EXECUTION;

DROP TABLE IF EXISTS BATCH_JOB_INSTANCE;



INSERT INTO govio_templates(id,message_body,subject,has_due_date,has_payment,name,description) VALUES (1,'Il Comune la informa che il ${due_date_date} scadrà la sua Carta di Identità. Potrà rinnovarlo il ${appointment_verbose} presso {at}.','Scadenza CIE ${taxcode}',true,false,'Template CIE','Template CIE');
INSERT INTO govio_templates(id,message_body,subject,has_due_date,has_payment,name,description) VALUES (2 ,'Benvenuto ${nome}','Ciao ${nome}',false,false,'Test freemarker','Test freemarker');
ALTER SEQUENCE seq_govio_templates RESTART WITH 3;

ALTER SEQUENCE seq_govio_service_instances RESTART WITH 2;

-- Come id_govhub_service mettiamo un id a caso, tanto la fk sul batch non esiste
INSERT INTO govio_service_instances(id,id_govhub_service,id_govio_template,apikey) VALUES (1,12,'1','17886617e07d47e8b1ba314f2f1e3052');
INSERT INTO govio_service_instances(id,id_govhub_service,id_govio_template,apikey) VALUES (2,12,'1','17886617e07d47e8b1ba314f2f1e3052');
INSERT INTO govio_service_instances(id,id_govhub_service,id_govio_template,apikey) VALUES (3,12,'2','17886617e07d47e8b1ba314f2f1e3052');


ALTER SEQUENCE seq_govio_service_instances RESTART WITH 3;

INSERT INTO govio_placeholders(id,name,type,example) VALUES (1,'appointment','DATE','2100-12-31T12:00');
INSERT INTO govio_placeholders(id,name,type,example) VALUES (2,'at','STRING','Ufficio numero 1');
INSERT INTO govio_placeholders(id,name,type,example) VALUES (3,'nome','STRING','Mario');
ALTER SEQUENCE seq_govio_placeholders RESTART WITH 4;

INSERT INTO govio_template_placeholders(id_govio_template,id_govio_placeholder, mandatory, position) VALUES ('1','1', true, 1);
INSERT INTO govio_template_placeholders(id_govio_template,id_govio_placeholder, mandatory, position) VALUES ('1','2', true, 2);
INSERT INTO govio_template_placeholders(id_govio_template,id_govio_placeholder, mandatory, position) VALUES ('2','3', true, 1);
