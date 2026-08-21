create table users (
                       id uuid primary key,
                       email varchar(320) not null unique,
                       password_hash varchar(255) not null,
                       role varchar(32) not null,
                       status varchar(32) not null,
                       email_verified boolean not null default false,
                       created_at timestamptz not null default now(),
                       updated_at timestamptz not null default now(),

                       constraint users_role_check
                           check (role in ('PATIENT', 'DOCTOR', 'ADMIN')),

                       constraint users_status_check
                           check (status in (
                                             'PENDING_VERIFICATION',
                                             'ACTIVE',
                                             'SUSPENDED',
                                             'DISABLED'
                               ))
);
