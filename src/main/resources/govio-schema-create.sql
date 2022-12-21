create sequence seq_govio_files start 1 increment 1;
create sequence seq_govio_service_instances start 1 increment 1;
create sequence seq_govio_templates start 1 increment 1;


    create table govio_files (
       id int8 not null,
        expiration_date timestamp,
        location varchar(1024),
        name varchar(256) not null,
        processing_date timestamp,
        status varchar(256),
        status_detail varchar(1024),
        id_govauth_user int8,
        id_govio_service_instance int8,
        primary key (id)
    );

    create table govio_service_instances (
       id int8 not null,
        apikey varchar(255),
        id_govhub_organizations int8 not null,
        id_govhub_service int8 not null,
        id_govio_template int8,
        primary key (id)
    );

    create table govio_templates (
       id int8 not null,
        primary key (id)
    );
    alter table govio_files 
       add constraint FK37pou8bxyyfnjs03b7fj6w3fb 
       foreign key (id_govauth_user) 
       references govhub_users;

    alter table govio_files 
       add constraint FK9te7wpln11mq48levwtliu8u9 
       foreign key (id_govio_service_instance) 
       references govio_service_instances;

    alter table govio_service_instances 
       add constraint FK5uo97cy13rloojmau7405huj1 
       foreign key (id_govhub_organizations) 
       references govhub_organizations;

    alter table govio_service_instances 
       add constraint FK3a3kwf12ve1fst23lqlaj1jji 
       foreign key (id_govhub_service) 
       references govhub_services;

    alter table govio_service_instances 
       add constraint FKhjvivd89ic6jgic207vymsdby 
       foreign key (id_govio_template) 
       references govio_templates;

create sequence seq_govio_files start 1 increment 1;
create sequence seq_govio_service_instances start 1 increment 1;
create sequence seq_govio_templates start 1 increment 1;


