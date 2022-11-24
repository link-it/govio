-- Servizio di test

INSERT INTO govio_templates(id,message_body,subject,has_due_date,has_payment) VALUES (1,'Il Comune di Empoli la informa che il ${expedition_date.date} alle ore ${expedition_date.time} scadrà la sua Carta di Identità con numero ${taxcode}.','Il Comune di Empoli la informa che il ${expedition_date.date} alle ore ${expedition_date.time} scadrà la sua Carta di Identità con numero ${taxcode}.',false,false);
ALTER SEQUENCE seq_govio_templates RESTART WITH 2;	

INSERT INTO govio_service_instances(id,id_govio_service,id_govio_template,apikey) VALUES (1,'1','1','17886617e07d47e8b1ba314f2f1e3052');
ALTER SEQUENCE seq_govio_service_instances RESTART WITH 2;

INSERT INTO govio_placeholders(id,name,type,example) VALUES (1,'expedition_date','DATE','example');
INSERT INTO govio_placeholders(id,name,type,example) VALUES (2,'taxcode','STRING','example');
ALTER SEQUENCE seq_govio_placeholders RESTART WITH 3;

INSERT INTO govio_template_placeholders(id_govio_template,id_govio_placeholder) VALUES ('1','1');
INSERT INTO govio_template_placeholders(id_govio_template,id_govio_placeholder) VALUES ('1','2');
