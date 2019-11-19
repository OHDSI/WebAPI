create database cdm;

create user ohdsi with encrypted password 'ohdsi';

grant all privileges on database cdm to ohdsi;