-- Servizio di test

INSERT INTO govio_service_instances(id,id_govio_service,apikey) VALUES (1,'1','17886617e07d47e8b1ba314f2f1e3052');
ALTER SEQUENCE seq_govio_service_instances RESTART WITH 2;

INSERT INTO govio_templates(id,id_govio_service_instance,message_body) VALUES (1,'1','Il Comune di Empoli la informa che il ${expeditionDate.date} alle ore ${expeditionDate.time} scadrà la sua Carta di Identità con numero ${taxcode}.');
ALTER SEQUENCE seq_govio_templates RESTART WITH 2;

INSERT INTO govio_placeholders(id,name,type) VALUES (1,'expeditionDate','Date');
INSERT INTO govio_placeholders(id,name,type) VALUES (2,'taxcode','String');
ALTER SEQUENCE seq_govio_placeholders RESTART WITH 3;

INSERT INTO govio_template_placeholders(id,id_govio_template,id_govio_placeholder) VALUES (1,'1','1');
INSERT INTO govio_template_placeholders(id,id_govio_template,id_govio_placeholder) VALUES (2,'1','2');
ALTER SEQUENCE seq_govio_template_placeholders RESTART WITH 3;

