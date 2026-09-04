# React + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is enabled on this template. See [this documentation](https://react.dev/learn/react-compiler) for more information.

Note: This will impact Vite dev & build performances.
You can also try [the experimental native React Compiler support in plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react/README.md#rust-react-compiler) by using `compiler: true` in the plugin options instead of using the Babel plugin.

## Expanding the ESLint configuration

If you are developing a production application, we recommend using TypeScript with type-aware lint rules enabled. Check out the [TS template](https://github.com/vitejs/vite/tree/main/packages/create-vite/template-react-ts) for information on how to integrate TypeScript and [`typescript-eslint`](https://typescript-eslint.io) in your project.

## Backend

The API for this app lives in [`backend/`](backend) - a Spring Boot (Java 17) service backed by PostgreSQL. It handles login (email + password, with an email OTP step required once at account creation), browsing PC stations, booking them with server-side double-booking prevention, UPI QR codes for prepaid bookings, and admin booking management.

### Prerequisites

- JDK 17
- Maven
- Docker (for Postgres)

### Running it

```bash
cd backend
docker compose up -d        # starts Postgres
.\run-local.ps1              # Windows: builds + runs the backend, real OTP emails via the shared Gmail sender
# or, without real email sending (OTP codes land in backend/app.log instead):
mvn spring-boot:run
```

The API serves on `http://localhost:8081` by default. Interactive API docs (Swagger UI) are at `http://localhost:8081/swagger-ui.html`.

The frontend expects the API at the URL set in the root [`.env`](.env) (`VITE_API_BASE_URL`), which already points at `http://localhost:8081` for local dev.

### Config

All backend config is environment-variable driven (see `backend/src/main/resources/application.yml` for the full list and defaults) - database connection, JWT secret, CORS allowed origins, the bootstrap admin account, SMTP/email, and the UPI payee details used to build payment QR codes.
