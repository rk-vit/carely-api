# Carely TODO

## Product roadmap from Healthcare Appointment & Follow-up Manager

### Appointments and booking

- [x] Create a `carely-appointments` module and wire it into the root/server POMs.
- [x] Add the appointments migration and jOOQ generation.
- [x] Implement patient booking with server-side availability and leave re-checks.
- [x] Prevent double-booking with a transaction and PostgreSQL overlap protection.
- [x] Add a five-minute slot hold and expiry mechanism before confirmation.
- [x] Add appointment statuses: `HELD`, `BOOKED`, `CANCELLED`, `COMPLETED`, and `NO_SHOW`.
- [x] Implement patient/doctor/admin appointment views.
- [x] Implement cancellation and rescheduling.
- [ ] Replace frontend appointment mocks with real API calls.

### AI visit summaries

- [ ] Add the pre-visit symptom-summary endpoint and persist the generated result.
- [ ] Generate urgency, chief complaint, and suggested doctor questions.
- [ ] Add doctor clinical notes, diagnosis, prescription, and follow-up submission.
- [ ] Add the post-visit patient-friendly summary endpoint and persist its result.
- [ ] Handle LLM failures gracefully without failing booking or visit completion.

### Notifications and integrations

- [ ] Add an email notification/outbox module with retries and failure tracking.
- [ ] Send booking, cancellation, rescheduling, reminder, leave-conflict, and summary emails.
- [ ] Add Google Calendar OAuth 2.0 connection management.
- [ ] Create, update, and delete Google Calendar events for appointment lifecycle changes.
- [ ] Retry calendar failures asynchronously without breaking appointment operations.
- [ ] Add medication reminder persistence and a background worker.
- [ ] Respect prescription frequency, duration, and patient timezone for reminders.

### Leave and appointment conflicts

- [ ] When approved leave overlaps bookings, mark affected appointments and notify patients.
- [ ] Provide cancellation/rescheduling handling for leave-affected appointments.

### Security and authorization

- [ ] Move portal-role validation from frontend-only checks into the backend login flow.
- [ ] Add module-specific authorization for patient, doctor, and admin endpoints.
- [ ] Verify ownership on every patient/doctor appointment and clinical-data operation.

### Documentation and delivery

- [ ] Complete README setup and deployment instructions.
- [ ] Document environment variables in `.env.example`.
- [ ] Document API contracts and database schema.
- [ ] Document LLM prompts and graceful-failure behavior.
- [ ] Document Google Calendar OAuth setup.
- [ ] Write the system-design explanation covering locking, slot holds, leave conflicts, and notification retries.
- [ ] Verify the hosted application URL and complete end-to-end testing.

## Authentication and authorization

- [ ] Move portal-role validation to the backend. When a user with a valid account role (for example, `PATIENT`) attempts to sign in to a different portal (for example, `DOCTOR`), the server must reject the request instead of relying only on frontend role checking.
  - Decide whether `/auth/login` should receive a `requestedRole` field or whether separate role-specific login endpoints should be used.
  - Required behavior for the selected design: reject a wrong-portal login with `401 Unauthorized`, return a clear response message, and do not leave an authenticated session active.
  - Keep backend authorization on protected resources as the final security boundary.
- [ ] Add module-specific backend authorization after the patient, doctor, and admin modules are created. Each module should protect its own endpoints with the appropriate role rules (`PATIENT`, `DOCTOR`, or `ADMIN`).

##Check
##hi
