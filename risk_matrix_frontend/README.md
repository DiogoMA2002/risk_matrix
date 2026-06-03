# Risk Matrix — Frontend

Vue 3 frontend for the Risk Matrix cybersecurity assessment platform.

## Tech Stack

- Vue 3 (Options API)
- Vuex 4 (state management)
- Vue Router 4
- Axios (cookie-based auth)
- Tailwind CSS

## Development Setup

```bash
npm install
npm run serve
```

Development server starts at `http://localhost:8081`. Requires the Spring Boot backend running at `http://localhost:8080`.

## Production Build

```bash
npm run build
```

Output is in `dist/`. Serve with Nginx or any static file server. See [SETUP.md](../SETUP.md) for the full Nginx configuration.

## Lint

```bash
npm run lint
```

## Project Structure

```text
src/
├── components/
│   ├── views/              # Top-level page components
│   ├── AdminDashboard/     # Admin management UI modules
│   ├── Questionnaire/      # Questionnaire-specific components
│   └── Static/             # Shared UI (dialogs, headers, glossary drawer)
├── composables/            # Reusable composition logic
├── router/                 # Route definitions and navigation guards
├── store/                  # Vuex store (state, actions, mutations)
└── utils/                  # Shared utilities (formatters, token manager)
```

## User Flow

1. `/` — Enter email to receive a session token
2. `/risk-info` — Learn about the risk matrix methodology
3. `/requirements` — Review assessment prerequisites
4. `/category` — Select a risk category
5. `/questions/:questionnaireId/:category` — Complete the questionnaire
6. `/feedback-form` — Optional feedback submission

Admin access: `/login` → `/admin`

## Authentication

Tokens are stored in HttpOnly cookies set by the backend — JavaScript has no direct access. Role state (`admin` / `public`) is tracked in `localStorage` for UI routing only. Session progression (completed steps) is tracked in `sessionStorage`.
