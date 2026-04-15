# React + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react/README.md) uses [Babel](https://babeljs.io/) for Fast Refresh
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react-swc) uses [SWC](https://swc.rs/) for Fast Refresh

## API configuration

This project reads backend base URL from `VITE_API_BASE_URL`.

- Development default: `/api` (proxied by Vite to `http://localhost:8080`)
- Production: define `VITE_API_BASE_URL` according to your deployed backend URL

Local development flow:

1. Run backend on `http://localhost:8080`
2. Run frontend with `npm run dev`
3. Frontend requests to `/api/...` are forwarded by Vite proxy to backend

This avoids browser CORS issues in local development because requests are same-origin from the browser perspective.
