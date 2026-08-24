create table if not exists visit_consultations (
    appointment_id uuid primary key references appointments(id) on delete cascade,
    clinical_notes text not null,
    diagnosis text,
    prescription text,
    summary text,
    follow_up_date date,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);
