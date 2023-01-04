CREATE SEQUENCE seq_govio_file_messages START 1 INCREMENT 1;

CREATE SEQUENCE seq_govio_files START 1 INCREMENT 1;

CREATE SEQUENCE seq_govio_messages START 1 INCREMENT 1;

CREATE SEQUENCE seq_govio_placeholders START 1 INCREMENT 1;

CREATE SEQUENCE seq_govio_service_instances START 1 INCREMENT 1;

CREATE SEQUENCE seq_govio_templates START 1 INCREMENT 1;

CREATE SEQUENCE seq_govio_services START 1 INCREMENT 1;

CREATE TABLE govio_templates
  (
     id           BIGINT DEFAULT nextval('seq_govio_templates') NOT NULL,
     description  VARCHAR(255),
     has_due_date BOOLEAN NOT NULL,
     has_payment  BOOLEAN NOT NULL,
     message_body VARCHAR(255) NOT NULL,
     name         VARCHAR(255),
     subject      VARCHAR(255) NOT NULL,
     PRIMARY KEY (id)
  );
  
CREATE TABLE govio_services 
  (
     id                 BIGINT DEFAULT nextval('seq_govio_services') NOT NULL,
     id_govio_template  BIGINT NOT NULL,
     id_govhub_service  BIGINT NOT NULL,
     primary key (id)
  );
  
ALTER TABLE govio_services 
  ADD CONSTRAINT fk_govioserv_templ FOREIGN KEY (id_govio_template) 
  REFERENCES govio_templates; 
  
ALTER TABLE govio_services
  ADD CONSTRAINT fk_govioserv_hubserv FOREIGN KEY (id_govhub_service)
  REFERENCES govhub_services;    
  

CREATE TABLE govio_placeholders
  (
     id          BIGINT DEFAULT nextval('seq_govio_placeholders') NOT NULL,
     description TEXT,
     example     VARCHAR(255) NOT NULL,
     name        VARCHAR(255) NOT NULL,
     pattern     VARCHAR(255),
     TYPE        VARCHAR(35) NOT NULL,
     PRIMARY KEY (id)
  );

CREATE TABLE govio_template_placeholders
  (
     id_govio_placeholder BIGINT NOT NULL,
     id_govio_template    BIGINT NOT NULL,
     index                BIGINT NOT NULL,
     mandatory            BOOLEAN NOT NULL,
     PRIMARY KEY (id_govio_placeholder, id_govio_template)
  );

ALTER TABLE govio_template_placeholders
  ADD CONSTRAINT fk_govio_tp_placeholder FOREIGN KEY (id_govio_placeholder)
  REFERENCES govio_placeholders;

ALTER TABLE govio_template_placeholders
  ADD CONSTRAINT fk_govio_tp_template FOREIGN KEY (id_govio_template)
  REFERENCES govio_templates; 
  
CREATE TABLE govio_service_instances
  (
     id                       BIGINT DEFAULT nextval('seq_govio_service_instances') NOT NULL,
     apikey                   VARCHAR(255) NOT NULL,
     id_govio_service         BIGINT NOT NULL,
     id_govhub_organization   BIGINT NOT NULL,
     id_govio_template        BIGINT,
     PRIMARY KEY (id)
  );

ALTER TABLE govio_service_instances
  ADD CONSTRAINT fk_govio_srvinst_template FOREIGN KEY (id_govio_template)
  REFERENCES govio_templates;
  
ALTER TABLE govio_service_instances
  ADD CONSTRAINT fk_govio_srvinst_srv FOREIGN KEY (id_govio_service)
  REFERENCES govio_services;
  
ALTER TABLE govio_service_instances
  ADD CONSTRAINT fk_govio_srvinst_huborg FOREIGN KEY (id_govhub_organization)
  REFERENCES govhub_organizations;  
    
CREATE TABLE govio_files
  (
     id                        BIGINT DEFAULT nextval('seq_govio_files') NOT NULL,
     acquired_messages         BIGINT,
     creation_date             TIMESTAMP NOT NULL,
     processing_date           TIMESTAMP NOT NULL,
     error_messages            BIGINT,
     location                  VARCHAR(255) NOT NULL,
     name                      VARCHAR(255) NOT NULL,
     status                    VARCHAR(255) NOT NULL,
     status_detail             TEXT NOT NULL,
     size                      BIGINT NOT NULL,
     id_govio_service_instance BIGINT NOT NULL,
     id_govhub_user            BIGINT NOT NULL,
     PRIMARY KEY (id)
  );
  
ALTER TABLE govio_files
  ADD CONSTRAINT fk_govio_files_srvinst FOREIGN KEY (id_govio_service_instance) 
  REFERENCES govio_service_instances;  

ALTER TABLE govio_files
  ADD CONSTRAINT fk_govio_files_hubuser FOREIGN KEY (id_govhub_user) 
  REFERENCES govhub_users;  
  
CREATE TABLE govio_file_messages
  (
     id               BIGINT DEFAULT nextval('seq_govio_file_messages') NOT NULL,
     error            TEXT,
     line_number      BIGINT,
     line_record      TEXT,
     id_govio_file    BIGINT NOT NULL,
     id_govio_message BIGINT,
     PRIMARY KEY (id)
  );
  
ALTER TABLE govio_file_messages
  ADD CONSTRAINT fk_govio_filemsg_file FOREIGN KEY (id_govio_file)
  REFERENCES govio_files;

ALTER TABLE govio_file_messages
  ADD CONSTRAINT fk_govio_filemsg_msg FOREIGN KEY (id_govio_message)
  REFERENCES govio_messages;

CREATE TABLE govio_messages
  (
     id                        BIGINT DEFAULT nextval('seq_govio_messages') NOT NULL,
     amount                    BIGINT,
     appio_message_id          VARCHAR(255),
     creation_date             TIMESTAMP NOT NULL,
     due_date                  TIMESTAMP,
     email                     VARCHAR(255),
     expedition_date           TIMESTAMP,
     invalid_after_due_date    BOOLEAN,
     last_update_status        TIMESTAMP,
     markdown                  TEXT NOT NULL,
     notice_number             VARCHAR(35),
     payee                     VARCHAR(35),
     scheduled_expedition_date TIMESTAMP NOT NULL,
     status                    VARCHAR(35) NOT NULL,
     subject                   VARCHAR(255) NOT NULL,
     taxcode                   VARCHAR(35) NOT NULL,
     id_govio_service_instance BIGINT NOT NULL,
     id_govhub_user            BIGINT,
     PRIMARY KEY (id)
  );

ALTER TABLE govio_messages
  ADD CONSTRAINT fk_govio_msg_srvinst FOREIGN KEY (
  id_govio_service_instance) REFERENCES govio_service_instances;

ALTER TABLE govio_messages
  ADD CONSTRAINT fk_govio_msg_hubuser FOREIGN KEY (id_govhub_user) 
  REFERENCES govhub_users;  



