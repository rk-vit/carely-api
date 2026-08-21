alter table users
    add column first_name varchar(100),
      add column last_name varchar(100),
      add column phone_number varchar(20),
      add column phone_verified boolean not null default false;