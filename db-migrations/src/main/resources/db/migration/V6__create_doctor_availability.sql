create table doctor_availability (
    id uuid primary key,
    doctor_id uuid not null references doctors(id) on delete cascade,
    day_of_week varchar(9) not null,
    start_time time not null,
    end_time time not null,
    timezone varchar(64) not null default 'UTC',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),

    constraint doctor_availability_day_check check (day_of_week in
        ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')),
    constraint doctor_availability_time_check check (start_time < end_time),
    constraint doctor_availability_doctor_day_unique unique (doctor_id, day_of_week)
);

create table doctor_availability_override (
    id uuid primary key,
    doctor_id uuid not null references doctors(id) on delete cascade,
    override_date date not null,
    start_time time not null,
    end_time time not null,
    type varchar(7) not null,
    reason varchar(500),
    created_at timestamptz not null default now(),

    constraint doctor_availability_override_type_check check (type in ('BLOCKED', 'EXTRA')),
    constraint doctor_availability_override_time_check check (start_time < end_time)
);

create extension if not exists btree_gist;

alter table doctor_availability_override
    add constraint doctor_availability_override_no_overlap
    exclude using gist (
        doctor_id with =,
        tsrange(override_date + start_time, override_date + end_time, '[)') with &&
    );

create index doctor_availability_override_lookup_idx
    on doctor_availability_override (doctor_id, override_date, start_time);
