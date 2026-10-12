# Monate REST API (PHP + MySQL)

This API is the online backend for the Android app. It uses PDO prepared statements, password_hash/password_verify, hashed bearer tokens, server-side Google ID-token verification, and JSON responses.

## Requirements
- PHP 8.1+ with PDO MySQL and `allow_url_fopen` enabled (used to verify Google ID tokens)
- MySQL 8+ / MariaDB 10.5+
- HTTPS for public/production deployments

## Install locally
1. Import `schema.sql` into MySQL. It creates `monate_chicken` and seeds the menu.
2. Copy `config/config.example.php` to `config/config.php` and set database credentials and the Google **Web application** client ID. Never commit `config.php`. Environment variables `MONATE_DB_HOST`, `MONATE_DB_NAME`, `MONATE_DB_USER`, `MONATE_DB_PASSWORD`, and `GOOGLE_WEB_CLIENT_ID` are also supported.
3. For quick local development, from the project root run `php -S 0.0.0.0:8000 -t backend/public`. For Apache/production, point the document root at `backend/public`; the included `public/.htaccess` routes requests such as `/auth/login` to `index.php`.
4. Create a least-privilege MySQL user for the API. Do not use MySQL root for deployment.
5. Test `GET /health` and `GET /menu`.

## Android configuration
Edit `app/build.gradle.kts`: set `API_BASE_URL` to the API root URL ending in `/`. The emulator default is `http://10.0.2.2:8000/` when using the PHP development server command above. For a physical phone use `http://YOUR_COMPUTER_LAN_IP:8000/`; for production use an HTTPS URL. Android cleartext traffic is enabled for local development in this project; disable it and require HTTPS before release.

Set `google_web_client_id` in `app/src/main/res/values/strings.xml` to the same Google OAuth **Web client ID** used in `GOOGLE_WEB_CLIENT_ID` on the API. In Google Cloud Console configure the OAuth consent screen, create a Web client, and add the Android app's package/SHA-1 OAuth client as needed. The server verifies the ID token audience before creating a session. Never put a client secret in Android.

## Endpoints
- `POST /auth/register` `{ "name", "email", "password" }`
- `POST /auth/login` `{ "email", "password" }`
- `POST /auth/google` `{ "id_token" }`
- `POST /auth/change-password` (Bearer token)
- `POST /auth/logout` (Bearer token)
- `GET /menu`, `GET /menu/{id}`
- `GET /profile`, `PATCH /profile` (Bearer token)
- `GET /orders`, `POST /orders` (Bearer token)

Responses are JSON. The app currently uses the online API for registration/login/Google sign-in/profile-name update/menu retrieval; remaining local demo features such as full order-history UI, reviews and loyalty screens still need to be migrated to their API endpoints before production. The backend's order endpoints are available, but no card payment gateway is implemented; the payment-method field is only an order preference, not proof of payment.
