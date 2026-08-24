create table if not exists patient_profiles (
    user_id uuid primary key references users(id) on delete cascade,
    date_of_birth date not null,
    gender varchar(40) not null,
    address text not null,
    emergency_contact_name varchar(160) not null,
    emergency_contact_phone varchar(30) not null,
    allergies text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);
