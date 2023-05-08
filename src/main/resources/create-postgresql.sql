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
     description  TEXT,
     has_due_date BOOLEAN NOT NULL,
     has_payment  BOOLEAN NOT NULL,
     message_body TEXT NOT NULL,
     name         VARCHAR(255) NOT NULL,
     subject      VARCHAR(255) NOT NULL,
     PRIMARY KEY (id)
  );
  

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

alter table govio_placeholders add constraint UniqueGovioPlaceholderName unique (name);

CREATE TABLE govio_template_placeholders
  (
     id_govio_placeholder BIGINT NOT NULL,
     id_govio_template    BIGINT NOT NULL,
     position             BIGINT NOT NULL,
     mandatory            BOOLEAN NOT NULL,
     PRIMARY KEY (id_govio_placeholder, id_govio_template)
  );

alter table govio_template_placeholders 
   add constraint GovioTemplatePlaceholder_GovioPlaceholder 
   foreign key (id_govio_placeholder) 
   references govio_placeholders;

alter table govio_template_placeholders 
   add constraint GovioTemplatePlaceholder_GovioTemplate 
   foreign key (id_govio_template) 
   references govio_templates;  

alter table govio_template_placeholders 
       add constraint UniqueTemplatePlaceholderPosition unique (id_govio_template, position);

CREATE TABLE govio_service_instances
  (
     id                       BIGINT DEFAULT nextval('seq_govio_service_instances') NOT NULL,
     apikey                   VARCHAR(255) NOT NULL,
     id_govhub_service        BIGINT NOT NULL,
     id_govhub_organization   BIGINT NOT NULL,
     id_govio_template        BIGINT NOT NULL,
	 enabled                  BOOLEAN NOT NULL DEFAULT TRUE,
	 io_service_id 	          VARCHAR(255) NOT NULL,
     PRIMARY KEY (id)
  );

alter table govio_service_instances 
   add constraint GovioServiceInstance_GovhubOrganization 
   foreign key (id_govhub_organization) 
   references govhub_organizations;

alter table govio_service_instances 
   add constraint GovioServiceInstance_GovhubService 
   foreign key (id_govhub_service) 
   references govhub_services;

alter table govio_service_instances 
   add constraint GovioServiceInstance_GovioTemplate 
   foreign key (id_govio_template) 
   references govio_templates;

ALTER table govio_service_instances
       add constraint UniqueServiceOrganizationTemplate unique (id_govhub_service, id_govio_template, id_govhub_organization);

    
CREATE TABLE govio_files
  (
     id                        BIGINT DEFAULT nextval('seq_govio_files') NOT NULL,
     acquired_messages         BIGINT,
     creation_date             TIMESTAMP NOT NULL,
     processing_date           TIMESTAMP,
     error_messages            BIGINT,
     location                  VARCHAR(255) NOT NULL,
     name                      VARCHAR(255) NOT NULL,
     status                    VARCHAR(255) NOT NULL,
     status_detail             TEXT,
     size                      BIGINT NOT NULL,
     id_govio_service_instance BIGINT NOT NULL,
     id_govhub_user            BIGINT NOT NULL,
     PRIMARY KEY (id)
  );
 
alter table govio_files 
       add constraint GovioFile_GovioServiceInstance 
       foreign key (id_govio_service_instance) 
       references govio_service_instances;
 
ALTER TABLE govio_files
  ADD CONSTRAINT GovioFile_GovhubUser FOREIGN KEY (id_govhub_user) 
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

create table govio_messages_idempotency_keys (
	id_govio_message BIGINT not null,
	bean_hashcode BIGINT,
	idempotency_key uuid,
	primary key (id_govio_message)
);

alter table govio_messages_idempotency_keys 
   add constraint UniqueIdempotencykeyHashcode unique (idempotency_key, bean_hashcode);

create index BeanHashcodeIdx on govio_messages_idempotency_keys (bean_hashcode);

alter table govio_messages 
   add constraint GovioMessage_GovioServiceInstance 
   foreign key (id_govio_service_instance) 
   references govio_service_instances;

alter table govio_messages 
   add constraint GovioMessage_GovhubUser 
   foreign key (id_govhub_user) 
   references govhub_users;

alter table govio_file_messages 
   add constraint GovioFileMessage_GovioFile 
   foreign key (id_govio_file) 
   references govio_files;

alter table govio_file_messages 
   add constraint GovioFileMessage_GovioMessage 
   foreign key (id_govio_message) 
   references govio_messages;

alter table govio_messages_idempotency_keys 
   add constraint IdempotencyKey_GovioMessage 
   foreign key (id_govio_message) 
   references govio_messages;
