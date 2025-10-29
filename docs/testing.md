# Testing & Quality Notes

## Backend

- Run unit tests: `mvn -B test`
- New suites cover `AuthService`, `JwtService`, `ProductoService`, inventory flows, and full Spring context bootstrap.
- Mockito-based specs assert token generation, duplicate validations, and error branches.
- Replace H2 with PostgreSQL Testcontainers for integration coverage when needed (add profile `test-postgres`).

## Frontend

- Run component tests in CI: `npm run test -- --run --reporter=dot`
- `EmpresasPage.test.tsx` verifies create flow, optimistic feedback, and admin permissions.
- `ProductosPage.test.tsx` exercises price handling, duplicate protection, and deletion feedback.
- `InventarioPage.test.tsx` asserts PDF download/email actions plus error states.
- Use `vitest --coverage` to view React Query hook coverage.

## Linting & Formatting

- Java: enable `mvn spotless:apply` or Checkstyle (see `pom.xml` plugin section to extend).
- React: run `npm run lint` to catch accessibility and typing issues.

## Recommended Next Steps

1. Integrate Jacoco + Sonar (backend) and Vitest coverage thresholds (frontend).
2. Add Playwright smoke tests once the App Runner deployment is ready.
3. Automate seed data reset with `CommandLineRunner` profile toggles to keep staging clean.
