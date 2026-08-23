create table if not exists doctors (
    id uuid primary key,
    user_id uuid not null unique references users(id) on delete cascade,
    specialization varchar(100) not null,
    medical_license_number varchar(100) not null unique,
    years_of_experience integer not null default 0,
    consultation_fee numeric(10, 2) not null default 0,
    biography text,
    working_start_time time,
    working_end_time time,
    slot_duration_minutes integer not null default 30,
    active boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),

    constraint doctors_years_experience_check check (years_of_experience >= 0),
    constraint doctors_consultation_fee_check check (consultation_fee >= 0),
    constraint doctors_slot_duration_check check (slot_duration_minutes between 15 and 240),
    constraint doctors_working_hours_check check (
        working_start_time is null or working_end_time is null or working_start_time < working_end_time
    )
);
