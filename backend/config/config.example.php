<?php
// Copy to config.php or set the equivalent environment variables on your host.
return [
    'db_host' => getenv('MONATE_DB_HOST') ?: '127.0.0.1',
    'db_name' => getenv('MONATE_DB_NAME') ?: 'monate_chicken',
    'db_user' => getenv('MONATE_DB_USER') ?: 'monate_api',
    'db_password' => getenv('MONATE_DB_PASSWORD') ?: 'CHANGE_ME',
    'google_web_client_id' => getenv('GOOGLE_WEB_CLIENT_ID') ?: 'YOUR_GOOGLE_WEB_CLIENT_ID.apps.googleusercontent.com',
    'token_ttl_days' => 30,
];
