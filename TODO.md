# Carely TODO

## Authentication and authorization

- [ ] Move portal-role validation to the backend. When a user with a valid account role (for example, `PATIENT`) attempts to sign in to a different portal (for example, `DOCTOR`), the server must reject the request instead of relying only on frontend role checking.
  - Decide whether `/auth/login` should receive a `requestedRole` field or whether separate role-specific login endpoints should be used.
  - Required behavior for the selected design: reject a wrong-portal login with `401 Unauthorized`, return a clear response message, and do not leave an authenticated session active.
  - Keep backend authorization on protected resources as the final security boundary.
- [ ] Add module-specific backend authorization after the patient, doctor, and admin modules are created. Each module should protect its own endpoints with the appropriate role rules (`PATIENT`, `DOCTOR`, or `ADMIN`).
