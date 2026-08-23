insert into users (
    id,
    email,
    password_hash,
    role,
    status,
    email_verified,
    created_at,
    updated_at,
    first_name,
    last_name,
    phone_verified
)
values (
    '00000000-0000-0000-0000-000000000001',
    'admin@carely.com',
    '$2a$10$0X7.Bdz9ErdaxD9rUC68necfNs2QUD8S33aMDYiCMkeDczPsuj8c.',
    'ADMIN',
    'ACTIVE',
    true,
    now(),
    now(),
    'Carely',
    'Admin',
    false
)
on conflict (email) do nothing;
