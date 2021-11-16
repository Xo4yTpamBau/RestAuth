delete
from users_roles;
delete
from users;
delete
from roles;


insert into users(id_user, email, first_name, last_name, password, phone, status, username, avatar)
values ('1',
        'test@mail.com',
        'test',
        'test',
        '$2a$10$trZ2KInnDx0/4u6/ABMuL.1t70/QXk/FUHFtJYHlv.WlHsIR3OVk.',
        '375251234567',
        'ACTIVE',
        'test',
        false);

insert into users(id_user, email, first_name, last_name, password, phone, status, username)
values ('2',
        'test2@mail.com',
        'test',
        'test',
        '$2a$10$trZ2KInnDx0/4u6/ABMuL.1t70/QXk/FUHFtJYHlv.WlHsIR3OVk.',
        '375331234567',
        'NOT_ACTIVE',
        'test2');

insert into users(id_user, email, first_name, last_name, password, phone, status, username)
values ('3',
        'test3@mail.com',
        'test',
        'test',
        '$2a$10$trZ2KInnDx0/4u6/ABMuL.1t70/QXk/FUHFtJYHlv.WlHsIR3OVk.',
        '375291234567',
        'BLOCKED',
        'test3');

insert into users(id_user, email, first_name, last_name, password, phone, status, username, avatar)
values ('4',
        'test4@mail.com',
        'test',
        'test',
        '$2a$10$trZ2KInnDx0/4u6/ABMuL.1t70/QXk/FUHFtJYHlv.WlHsIR3OVk.',
        '375441234567',
        'ACTIVE',
        'test4',
        true);



insert into roles(id_role, name_role)
values (1, 'USER');

insert into users_roles(id_user, id_role)
values (1, 1);

insert into users_roles(id_user, id_role)
values (2, 1);

