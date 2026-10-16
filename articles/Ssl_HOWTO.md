# SSL/TLS Configuration — Setup Guide

This document describes how to configure SSL/TLS (HTTPS) for WebAPI. When SSL
is enabled, the application serves traffic over HTTPS and automatically
redirects HTTP requests to HTTPS.

---

## Overview

WebAPI supports SSL in two deployment scenarios:

1. **Embedded Tomcat (standalone JAR)**: SSL terminates at the application.
   Configure the keystore directly in `application.yaml`.

2. **Reverse Proxy (nginx, Apache, load balancer)**: SSL terminates at the
   proxy. The proxy forwards HTTP to WebAPI with `X-Forwarded-Proto` headers.
   WebAPI detects the original protocol and redirects if needed.

When `server.ssl.enabled=true`, WebAPI activates the `SslRedirectFilter` which
intercepts all HTTP requests and redirects them to HTTPS, preserving the
request path and query string.

---

## Architecture

```
                           ┌─────────────────────────────┐
                           │     SslRedirectFilter       │
                           │  (Ordered.HIGHEST_PRECEDENCE)│
                           └─────────────────────────────┘
                                        │
           ┌────────────────────────────┼────────────────────────────┐
           │                            │                            │
     request.isSecure()?           HTTP detected              HTTPS detected
           │                            │                            │
           │                    302 Redirect to HTTPS         Continue to
           │                            │                   Spring Security
           │                            │                            │
           └────────────────────────────┴────────────────────────────┘
```

### Key Components

| Component | Location | Purpose |
|-----------|----------|---------|
| `SslRedirectFilter` | `security` | Servlet filter redirecting HTTP → HTTPS |
| `server.ssl.*` | `application.yaml` | SSL/TLS configuration properties |
| `forward-headers-strategy` | `application.yaml` | Enables X-Forwarded-* header trust |

---

## Configuration (application.yaml)

All SSL settings are under the standard Spring Boot `server.ssl` prefix:

```yaml
server:
  port: 8443                              # Port for HTTPS traffic
  forward-headers-strategy: NATIVE        # Trust X-Forwarded-* from proxies
  ssl:
    enabled: true                         # Enable SSL and HTTP→HTTPS redirect
    key-store: /path/to/keystore.p12      # Path to the keystore file
    key-store-password: changeit          # Password to open the keystore
    key-store-type: PKCS12                # Keystore format (PKCS12 or JKS)
    key-password: changeit                # Password for the private key (if different)
```

### Property Reference

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `server.ssl.enabled` | Yes | `false` | Enables HTTPS and activates redirect filter |
| `server.ssl.key-store` | Yes | — | Absolute path to keystore file containing certificate and private key |
| `server.ssl.key-store-password` | Yes | — | Password used to access the keystore |
| `server.ssl.key-store-type` | No | `PKCS12` | Keystore format: `PKCS12` (recommended) or `JKS` |
| `server.ssl.key-password` | No | — | Password for the private key entry (only if different from keystore password) |
| `server.port` | No | `8080` | The port WebAPI listens on (used in redirect URL if not 443) |
| `server.forward-headers-strategy` | No | `NONE` | Set to `NATIVE` to trust reverse proxy headers |

---

## Creating a Keystore (Windows)

SSL requires a keystore containing your certificate and private key. Below are
instructions for Windows using the `keytool` utility (included with Java).

### Option 1: Self-Signed Certificate (Development/Testing)

Generate a self-signed certificate for local development:

```powershell
# Navigate to your Java bin directory or ensure keytool is in PATH
# Generate a PKCS12 keystore with a self-signed certificate

keytool -genkeypair `
  -alias webapi `
  -keyalg RSA `
  -keysize 2048 `
  -validity 365 `
  -keystore C:\path\to\webapi-keystore.p12 `
  -storetype PKCS12 `
  -storepass changeit `
  -keypass changeit `
  -dname "CN=localhost, OU=Development, O=OHDSI, L=City, ST=State, C=US"
```

Parameters:
- `-alias`: Name for the certificate entry (use any identifier)
- `-keyalg RSA -keysize 2048`: RSA algorithm with 2048-bit key
- `-validity 365`: Certificate valid for 365 days
- `-keystore`: Output path for the keystore file
- `-storetype PKCS12`: Modern keystore format (recommended over JKS)
- `-storepass`: Password to protect the keystore
- `-keypass`: Password for the private key (use same as storepass for simplicity)
- `-dname`: Distinguished name fields for the certificate

### Option 2: Certificate from CA (Production)

For production, obtain a certificate from a Certificate Authority (CA). You'll
typically receive:
- Certificate file (`.crt` or `.pem`)
- Private key file (`.key`)
- CA chain/intermediate certificates

**Step 1: Combine certificate and key into PKCS12**

Using OpenSSL (install via [Git for Windows](https://git-scm.com/download/win)
or [Win64 OpenSSL](https://slproweb.com/products/Win32OpenSSL.html)):

```powershell
openssl pkcs12 -export `
  -in certificate.crt `
  -inkey private.key `
  -certfile ca-chain.crt `
  -out webapi-keystore.p12 `
  -name webapi `
  -passout pass:changeit
```

**Step 2: Verify the keystore**

```powershell
keytool -list -keystore webapi-keystore.p12 -storetype PKCS12 -storepass changeit
```

You should see an entry with alias `webapi` of type `PrivateKeyEntry`.

### Option 3: Import Existing JKS Keystore

If you have an existing JKS keystore, convert it to PKCS12:

```powershell
keytool -importkeystore `
  -srckeystore old-keystore.jks `
  -srcstoretype JKS `
  -destkeystore webapi-keystore.p12 `
  -deststoretype PKCS12 `
  -srcstorepass oldpassword `
  -deststorepass newpassword
```

---

## Example Configurations

### Standalone with SSL (Development)

```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: C:/webapi/ssl/webapi-keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

Access at: `https://localhost:8443/WebAPI/`

### Behind Reverse Proxy (Production)

When SSL terminates at nginx/Apache/load balancer:

```yaml
server:
  port: 8080                              # Internal HTTP port
  forward-headers-strategy: NATIVE        # REQUIRED: Trust proxy headers
  ssl:
    enabled: true                         # Enables redirect filter only
    # No keystore needed - proxy handles SSL
```

The proxy must send these headers:
- `X-Forwarded-Proto: https` (or `http`)
- `X-Forwarded-Host: your-domain.com`
- `X-Forwarded-Port: 443`

Example nginx configuration:

```nginx
server {
    listen 443 ssl;
    server_name webapi.example.com;

    ssl_certificate     /etc/ssl/certs/webapi.crt;
    ssl_certificate_key /etc/ssl/private/webapi.key;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Port $server_port;
    }
}
```

---

## Troubleshooting

### Browser shows "Not Secure" warning

Self-signed certificates are not trusted by browsers. Options:
1. Add an exception in your browser (development only)
2. Import the certificate into Windows Trusted Root store
3. Use a CA-signed certificate (production)

### "Password was incorrect" or keystore errors

```powershell
# Verify keystore is readable
keytool -list -keystore C:\path\to\keystore.p12 -storepass yourpassword
```

Check:
- Path uses forward slashes (`/`) or escaped backslashes (`\\`)
- Password is correct for both keystore and key entry
- File permissions allow the Java process to read the keystore

### Redirect loop when behind proxy

Ensure `server.forward-headers-strategy: NATIVE` is set. Without this, the
application cannot detect that the original request was HTTPS, causing infinite
redirects.

### Port mismatch in redirect URL

The redirect URL uses `server.port`. If your proxy listens on a different port
(e.g., 443) but WebAPI runs on 8080, clients will be redirected to the wrong
port. Solutions:
1. Set `server.port: 443` and let the proxy forward to that port
2. Configure proxy to rewrite the redirect response

---

## Security Recommendations

1. **Use strong passwords**: Keystore and key passwords should be complex
2. **Secure password storage**: Use environment variables or secrets management:
   ```yaml
   key-store-password: ${SSL_KEYSTORE_PASSWORD}
   ```
3. **Restrict keystore file access**: Limit read permissions to the application user
4. **Use PKCS12 format**: Modern, portable, and widely supported
5. **Renew certificates before expiry**: Set calendar reminders for cert renewal
6. **TLS 1.2+ only**: Spring Boot defaults to secure TLS versions; avoid downgrading

---

## Related Documentation

- [Spring Boot SSL Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#application-properties.server.server.ssl.enabled)
- [Java Keytool Documentation](https://docs.oracle.com/en/java/javase/17/docs/specs/man/keytool.html)
