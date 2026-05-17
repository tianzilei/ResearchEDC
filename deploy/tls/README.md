# TLS / SSL Certificate Strategy

## Recommended Approach: Let's Encrypt + Certbot (Auto-renewal)

For production deployments, use Let's Encrypt with automated renewal.

### Option A: Certbot standalone (recommended for single-server)

```bash
# Install certbot
apt-get install certbot

# Obtain certificate
certbot certonly --standalone -d openclinica.example.com

# Copy to nginx ssl directory
cp /etc/letsencrypt/live/openclinica.example.com/fullchain.pem deploy/nginx/ssl/cert.pem
cp /etc/letsencrypt/live/openclinica.example.com/privkey.pem deploy/nginx/ssl/key.pem

# Auto-renewal (cron)
echo "0 3 * * * certbot renew --quiet && docker compose -f deploy/compose/docker-compose.prod.yml exec nginx nginx -s reload" | crontab -
```

### Option B: Certbot with Nginx container

```yaml
# Add to docker-compose.prod.yml:
  certbot:
    image: certbot/certbot
    container_name: oc-certbot
    volumes:
      - ./nginx/ssl:/etc/letsencrypt
      - ./nginx/html:/var/www/html
    command: renew
```

### Option C: Self-signed (development only)

```bash
mkdir -p deploy/nginx/ssl
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout deploy/nginx/ssl/key.pem \
  -out deploy/nginx/ssl/cert.pem \
  -subj "/CN=openclinica.local"
```

## Certificate Directory Structure

```
deploy/nginx/ssl/
├── cert.pem    # TLS certificate (full chain)
├── key.pem     # Private key (keep secure, restrict permissions)
└── README.md
```

## Security Checklist

- [ ] Certificates use SHA-256 (not SHA-1)
- [ ] Private key permissions: 600 (root only)
- [ ] TLS 1.2 and 1.3 only (no TLS 1.0/1.1)
- [ ] HSTS enabled (see nginx.conf)
- [ ] Auto-renewal configured
- [ ] Revocation procedure documented
