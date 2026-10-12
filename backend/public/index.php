<?php
declare(strict_types=1);

header('Content-Type: application/json; charset=utf-8');
header('X-Content-Type-Options: nosniff');
header('Cache-Control: no-store');

$configFile = __DIR__ . '/../config/config.php';
$config = require is_file($configFile) ? $configFile : __DIR__ . '/../config/config.example.php';

function respond(int $status, array $payload): never {
    http_response_code($status);
    echo json_encode($payload, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    exit;
}
function body(): array {
    $raw = file_get_contents('php://input');
    $data = json_decode($raw ?: '{}', true);
    if (!is_array($data)) respond(400, ['success'=>false, 'message'=>'Request body must be valid JSON']);
    return $data;
}
function cleanEmail(string $email): string { return strtolower(trim($email)); }
function publicUser(array $u): array {
    return ['id'=>(int)$u['id'], 'name'=>$u['name'], 'email'=>$u['email'], 'loyalty_points'=>(int)$u['loyalty_points']];
}
function issueToken(PDO $db, int $userId, int $days): string {
    $token = bin2hex(random_bytes(32));
    $hash = hash('sha256', $token);
    $expires = (new DateTimeImmutable('now', new DateTimeZone('UTC')))->modify('+' . max(1, $days) . ' days')->format('Y-m-d H:i:s');
    $stmt = $db->prepare('INSERT INTO api_tokens (user_id, token_hash, expires_at) VALUES (?, ?, ?)');
    $stmt->execute([$userId, $hash, $expires]);
    return $token;
}
function currentUser(PDO $db): ?array {
    $header = $_SERVER['HTTP_AUTHORIZATION'] ?? '';
    if (!preg_match('/^Bearer\s+([A-Za-z0-9]+)$/i', $header, $m)) return null;
    $stmt = $db->prepare('SELECT u.* FROM api_tokens t JOIN users u ON u.id=t.user_id WHERE t.token_hash=? AND t.expires_at > UTC_TIMESTAMP() LIMIT 1');
    $stmt->execute([hash('sha256', $m[1])]);
    $u = $stmt->fetch(PDO::FETCH_ASSOC);
    return $u ?: null;
}
function requireUser(PDO $db): array {
    $u = currentUser($db);
    if (!$u) respond(401, ['success'=>false, 'message'=>'Authentication required. Please sign in again.']);
    return $u;
}
try {
    $dsn = sprintf('mysql:host=%s;dbname=%s;charset=utf8mb4', $config['db_host'], $config['db_name']);
    $db = new PDO($dsn, $config['db_user'], $config['db_password'], [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES => false,
    ]);
} catch (Throwable $e) {
    error_log('Monate API database connection failed: ' . $e->getMessage());
    respond(503, ['success'=>false, 'message'=>'Database unavailable. Check backend configuration.']);
}

$method = strtoupper($_SERVER['REQUEST_METHOD'] ?? 'GET');
$path = parse_url($_SERVER['REQUEST_URI'] ?? '/', PHP_URL_PATH) ?: '/';
// Support deployment under /monate-api/public as well as a virtual-host document root.
$path = preg_replace('#^.*?/public/?#', '/', $path);
$path = '/' . trim($path, '/');
$input = in_array($method, ['POST','PATCH','PUT'], true) ? body() : [];

try {
    if ($method === 'GET' && $path === '/health') respond(200, ['success'=>true, 'service'=>'Monate REST API']);

    if ($method === 'POST' && $path === '/auth/register') {
        $name = trim((string)($input['name'] ?? ''));
        $email = cleanEmail((string)($input['email'] ?? ''));
        $password = (string)($input['password'] ?? '');
        if (mb_strlen($name) < 2 || mb_strlen($name) > 100) respond(422, ['success'=>false, 'message'=>'Enter a name between 2 and 100 characters']);
        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) respond(422, ['success'=>false, 'message'=>'Enter a valid email address']);
        if (strlen($password) < 8 || !preg_match('/[A-Z]/', $password) || !preg_match('/[a-z]/', $password) || !preg_match('/[0-9]/', $password) || !preg_match('/[^A-Za-z0-9]/', $password)) respond(422, ['success'=>false, 'message'=>'Password must be 8+ characters and include upper/lowercase letters, a number and a symbol']);
        $check = $db->prepare('SELECT id FROM users WHERE email=? LIMIT 1'); $check->execute([$email]);
        if ($check->fetch()) respond(409, ['success'=>false, 'message'=>'An account with this email already exists']);
        $stmt = $db->prepare('INSERT INTO users (name,email,password_hash,auth_provider) VALUES (?,?,?,?)');
        $stmt->execute([$name, $email, password_hash($password, PASSWORD_DEFAULT), 'password']);
        $id = (int)$db->lastInsertId();
        $q = $db->prepare('SELECT * FROM users WHERE id=?'); $q->execute([$id]); $user = $q->fetch();
        respond(201, ['success'=>true, 'token'=>issueToken($db,$id,(int)$config['token_ttl_days']), 'user'=>publicUser($user)]);
    }

    if ($method === 'POST' && $path === '/auth/login') {
        $email = cleanEmail((string)($input['email'] ?? '')); $password = (string)($input['password'] ?? '');
        $stmt = $db->prepare('SELECT * FROM users WHERE email=? LIMIT 1'); $stmt->execute([$email]); $user = $stmt->fetch();
        if (!$user || empty($user['password_hash']) || !password_verify($password, $user['password_hash'])) respond(401, ['success'=>false, 'message'=>'Incorrect email or password']);
        if (password_needs_rehash($user['password_hash'], PASSWORD_DEFAULT)) {
            $u = $db->prepare('UPDATE users SET password_hash=? WHERE id=?'); $u->execute([password_hash($password, PASSWORD_DEFAULT),$user['id']]);
        }
        respond(200, ['success'=>true, 'token'=>issueToken($db,(int)$user['id'],(int)$config['token_ttl_days']), 'user'=>publicUser($user)]);
    }

    if ($method === 'POST' && $path === '/auth/google') {
        $idToken = (string)($input['id_token'] ?? '');
        $clientId = (string)($config['google_web_client_id'] ?? '');
        if ($idToken === '' || $clientId === '' || str_starts_with($clientId, 'YOUR_')) respond(503, ['success'=>false, 'message'=>'Google sign-in is not configured on the API server']);
        $url = 'https://oauth2.googleapis.com/tokeninfo?id_token=' . urlencode($idToken);
        $ctx = stream_context_create(['http'=>['timeout'=>8, 'ignore_errors'=>true]]);
        $raw = @file_get_contents($url, false, $ctx);
        $claims = $raw ? json_decode($raw, true) : null;
        if (!is_array($claims) || !hash_equals($clientId, (string)($claims['aud'] ?? '')) || ($claims['email_verified'] ?? '') !== 'true' || empty($claims['email'])) respond(401, ['success'=>false, 'message'=>'Google token could not be verified']);
        $email = cleanEmail((string)$claims['email']);
        $name = trim((string)($claims['name'] ?? $claims['given_name'] ?? 'Google user'));
        $stmt = $db->prepare('SELECT * FROM users WHERE email=? LIMIT 1'); $stmt->execute([$email]); $user = $stmt->fetch();
        if (!$user) {
            $stmt = $db->prepare('INSERT INTO users (name,email,password_hash,auth_provider) VALUES (?,?,NULL,?)');
            $stmt->execute([$name, $email, 'google']);
            $stmt = $db->prepare('SELECT * FROM users WHERE id=?'); $stmt->execute([(int)$db->lastInsertId()]); $user = $stmt->fetch();
        } else {
            $stmt = $db->prepare('UPDATE users SET google_sub=COALESCE(google_sub, ?), auth_provider=CASE WHEN password_hash IS NULL THEN ? ELSE auth_provider END WHERE id=?');
            $stmt->execute([(string)($claims['sub'] ?? ''), 'google', $user['id']]);
            $stmt = $db->prepare('SELECT * FROM users WHERE id=?'); $stmt->execute([$user['id']]); $user = $stmt->fetch();
        }
        if (!empty($claims['sub'])) { $u=$db->prepare('UPDATE users SET google_sub=? WHERE id=?'); $u->execute([(string)$claims['sub'],$user['id']]); }
        respond(200, ['success'=>true, 'token'=>issueToken($db,(int)$user['id'],(int)$config['token_ttl_days']), 'user'=>publicUser($user)]);
    }

    if ($method === 'POST' && $path === '/auth/change-password') {
        $u=requireUser($db);
        $current=(string)($input['current_password'] ?? '');
        $next=(string)($input['new_password'] ?? '');
        if (empty($u['password_hash']) || !password_verify($current, $u['password_hash'])) respond(400,['success'=>false,'message'=>'Current password is incorrect']);
        if (strlen($next)<8 || !preg_match('/[A-Z]/',$next) || !preg_match('/[a-z]/',$next) || !preg_match('/[0-9]/',$next) || !preg_match('/[^A-Za-z0-9]/',$next)) respond(422,['success'=>false,'message'=>'New password must be 8+ characters and include upper/lowercase letters, a number and a symbol']);
        if (hash_equals($current,$next)) respond(422,['success'=>false,'message'=>'New password must differ from the current password']);
        $q=$db->prepare('UPDATE users SET password_hash=?, auth_provider=CASE WHEN google_sub IS NOT NULL THEN ? ELSE auth_provider END WHERE id=?');
        $q->execute([password_hash($next,PASSWORD_DEFAULT), 'mixed', $u['id']]);
        respond(200,['success'=>true,'message'=>'Password updated','user'=>publicUser($u)]);
    }

    if ($method === 'GET' && $path === '/menu') {
        $rows = $db->query('SELECT id,name,category,description,price,emoji,image_url FROM menu_items WHERE is_available=1 ORDER BY category,name')->fetchAll();
        respond(200, ['success'=>true, 'items'=>array_map(static function($r) { $r['id']=(int)$r['id']; $r['price']=(float)$r['price']; return $r; }, $rows)]);
    }
    if ($method === 'GET' && preg_match('#^/menu/(\d+)$#', $path, $m)) {
        $q=$db->prepare('SELECT id,name,category,description,price,emoji,image_url FROM menu_items WHERE id=? AND is_available=1'); $q->execute([(int)$m[1]]); $r=$q->fetch();
        if (!$r) respond(404,['success'=>false,'message'=>'Menu item not found']); $r['id']=(int)$r['id']; $r['price']=(float)$r['price']; respond(200,['success'=>true,'item'=>$r]);
    }

    if ($path === '/profile' && $method === 'GET') {
        $u=requireUser($db); respond(200,['success'=>true,'user'=>publicUser($u)]);
    }
    if ($path === '/profile' && $method === 'PATCH') {
        $u=requireUser($db); $name=trim((string)($input['name'] ?? ''));
        if (mb_strlen($name)<2 || mb_strlen($name)>100) respond(422,['success'=>false,'message'=>'Name must be between 2 and 100 characters']);
        $q=$db->prepare('UPDATE users SET name=? WHERE id=?'); $q->execute([$name,$u['id']]);
        $q=$db->prepare('SELECT * FROM users WHERE id=?'); $q->execute([$u['id']]); respond(200,['success'=>true,'user'=>publicUser($q->fetch())]);
    }

    if ($path === '/orders' && $method === 'GET') {
        $u=requireUser($db); $q=$db->prepare('SELECT id,status,total,collection_time,payment_method,created_at FROM orders WHERE user_id=? ORDER BY id DESC'); $q->execute([$u['id']]); respond(200,['success'=>true,'orders'=>$q->fetchAll()]);
    }
    if ($path === '/orders' && $method === 'POST') {
        $u=requireUser($db); $items=$input['items'] ?? []; $collection=trim((string)($input['collection_time'] ?? 'ASAP')); $payment=trim((string)($input['payment_method'] ?? 'Cash on Collection'));
        if (!is_array($items) || count($items)<1 || count($items)>50) respond(422,['success'=>false,'message'=>'An order must contain between 1 and 50 items']);
        $db->beginTransaction(); $total=0.0; $validated=[];
        foreach ($items as $item) {
            $id=(int)($item['menu_item_id'] ?? 0); $qty=(int)($item['quantity'] ?? 0);
            if ($id<1 || $qty<1 || $qty>99) { $db->rollBack(); respond(422,['success'=>false,'message'=>'Invalid order item']); }
            $q=$db->prepare('SELECT id,price FROM menu_items WHERE id=? AND is_available=1'); $q->execute([$id]); $menu=$q->fetch();
            if (!$menu) { $db->rollBack(); respond(422,['success'=>false,'message'=>'One of the selected items is unavailable']); }
            $total+=(float)$menu['price']*$qty; $validated[]=['id'=>$id,'qty'=>$qty,'price'=>(float)$menu['price']];
        }
        // Keep the Android app's current R25 service/delivery fee; price items from the database, never from the client.
        $total += 25.00;
        $q=$db->prepare('INSERT INTO orders (user_id,status,total,collection_time,payment_method) VALUES (?,\'RECEIVED\',?,?,?)'); $q->execute([$u['id'],$total,$collection,$payment]); $orderId=(int)$db->lastInsertId();
        $q=$db->prepare('INSERT INTO order_items (order_id,menu_item_id,quantity,unit_price) VALUES (?,?,?,?)');
        foreach ($validated as $item) $q->execute([$orderId,$item['id'],$item['qty'],$item['price']]);
        $points=(int)floor($total/10); if ($points>0) { $db->prepare('UPDATE users SET loyalty_points=loyalty_points+? WHERE id=?')->execute([$points,$u['id']]); $db->prepare('INSERT INTO loyalty_transactions (user_id,points,reason,order_id) VALUES (?,?,?,?)')->execute([$u['id'],$points,'Order reward',$orderId]); }
        $db->commit(); respond(201,['success'=>true,'message'=>'Order received','order'=>['id'=>$orderId,'status'=>'RECEIVED','total'=>round($total,2),'loyalty_points_earned'=>$points]]);
    }
    if ($method === 'POST' && $path === '/auth/logout') {
        $header=$_SERVER['HTTP_AUTHORIZATION'] ?? ''; if (preg_match('/^Bearer\s+([A-Za-z0-9]+)$/i',$header,$m)) $db->prepare('DELETE FROM api_tokens WHERE token_hash=?')->execute([hash('sha256',$m[1])]);
        respond(200,['success'=>true,'message'=>'Signed out']);
    }
    respond(404, ['success'=>false, 'message'=>'Endpoint not found']);
} catch (Throwable $e) {
    if ($db->inTransaction()) $db->rollBack();
    error_log('Monate API error: ' . $e->getMessage());
    respond(500, ['success'=>false, 'message'=>'Unexpected server error']);
}
