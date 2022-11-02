-- Servizio di test

INSERT INTO govio_service_instances(id,id_govio_service,apikey) VALUES (1,'1','17886617e07d47e8b1ba314f2f1e3052');

ALTER SEQUENCE seq_govio_service_instances RESTART WITH 2;

-- Messaggio di test

INSERT INTO govio_messages(id,id_govio_service_instance,subject,markdown) VALUES (1,'1','secondo tentativo!','# This is a markdown header\n\nto show how easily markdown can be converted to **HTML**\n\nRemember: this has to be a long text.');

ALTER SEQUENCE seq_govio_messages RESTART WITH 2;