create extension if not exists btree_gist;

create table appointments (
    id uuid primary key,
    doctor_id uuid not null references doctors(id),
    patient_id uuid not null references users(id),
    start_at timestamptz not null,
    end_at timestamptz not null,
    status varchar(20) not null default 'HELD',
    symptoms text not null,
    hold_expires_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint appointments_time_check check (start_at < end_at),
    constraint appointments_status_check check (status in ('HELD','BOOKED','CANCELLED','COMPLETED','NO_SHOW'))
);

create index appointments_patient_idx on appointments(patient_id, start_at desc);
create index appointments_doctor_idx on appointments(doctor_id, start_at);
alter table appointments add constraint appointments_no_overlap
    exclude using gist (doctor_id with =, tstzrange(start_at, end_at, '[)') with &&)
    where (status in ('HELD','BOOKED'));
