create table doctor_leave_requests (
    id uuid primary key,
    doctor_id uuid not null references doctors(id) on delete cascade,
    start_date date not null,
    end_date date not null,
    reason varchar(1000) not null,
    status varchar(20) not null default 'PENDING',
    reviewed_by uuid references users(id),
    reviewed_at timestamptz,
    reviewer_note varchar(1000),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),

    constraint doctor_leave_date_range_check check (start_date <= end_date),
    constraint doctor_leave_status_check check (status in ('PENDING', 'APPROVED', 'REJECTED'))
);

create index doctor_leave_requests_doctor_idx
    on doctor_leave_requests (doctor_id, created_at desc);

create index doctor_leave_requests_status_idx
    on doctor_leave_requests (status, start_date);
