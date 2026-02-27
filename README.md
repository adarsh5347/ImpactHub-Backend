# ImpactHub Backend

## Deploy (Railway/Render)

### Build command
```bash
./mvnw clean package -DskipTests
```

### Start command
```bash
java -jar target/*.jar
```

### Required environment variables
```env
PORT=8080
DB_URL=jdbc:mysql://HOST:3306/DBNAME?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USER=your_user
DB_PASS=your_password
JWT_SECRET=change_me
JWT_EXPIRATION_MS=86400000
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD_HASH=change_me_bcrypt_hash
CLOUDINARY_CLOUD_NAME=change_me
CLOUDINARY_API_KEY=change_me
CLOUDINARY_API_SECRET=change_me
CLOUDINARY_FOLDER=impacthub/ngos
CLOUDINARY_COVER_FOLDER=impacthub/ngos/covers
APP_UPLOAD_LOGO_MAX_SIZE_BYTES=5242880
APP_UPLOAD_COVER_MAX_SIZE_BYTES=10485760
SMTP_HOST=smtp.example.com
SMTP_PORT=587
SMTP_USER=your_smtp_user
SMTP_PASS=your_smtp_password
SMTP_FROM=no-reply@example.com
MAIL_ENABLED=false
APP_CORS_ALLOWED_ORIGINS=https://<REPLACE_WITH_VERCEL_DOMAIN>,https://<REPLACE_WITH_CUSTOM_DOMAIN_IF_ANY>
```

### MySQL DB_URL format example
```text
jdbc:mysql://HOST:3306/DBNAME?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC
```
