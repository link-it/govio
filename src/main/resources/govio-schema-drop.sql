

    alter table govio_files 
       drop constraint FK37pou8bxyyfnjs03b7fj6w3fb;

    alter table govio_files 
       drop constraint FK9te7wpln11mq48levwtliu8u9;

    alter table govio_service_instances 
       drop constraint FK5uo97cy13rloojmau7405huj1;

    alter table govio_service_instances 
       drop constraint FK3a3kwf12ve1fst23lqlaj1jji;

    alter table govio_service_instances 
       drop constraint FKhjvivd89ic6jgic207vymsdby;

    drop sequence if exists seq_govio_files;

    drop sequence if exists seq_govio_service_instances;

    drop sequence if exists seq_govio_templates;

    alter table govio_files 
       drop constraint FK37pou8bxyyfnjs03b7fj6w3fb;

    alter table govio_files 
       drop constraint FK9te7wpln11mq48levwtliu8u9;

    alter table govio_service_instances 
       drop constraint FK5uo97cy13rloojmau7405huj1;

    alter table govio_service_instances 
       drop constraint FK3a3kwf12ve1fst23lqlaj1jji;

    alter table govio_service_instances 
       drop constraint FKhjvivd89ic6jgic207vymsdby;

    drop table if exists govio_files cascade;

    drop table if exists govio_service_instances cascade;

    drop table if exists govio_templates cascade;
	
	drop sequence if exists seq_govio_files;

    drop sequence if exists seq_govio_service_instances;

    drop sequence if exists seq_govio_templates;
