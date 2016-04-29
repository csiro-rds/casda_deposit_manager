alter table casda.image_cube rename column image_type to type;

alter table casda.image_cube drop constraint if exists imtypefk;

alter table casda.image_cube add constraint imtypefk foreign key (type) references casda.image_type (type_name);