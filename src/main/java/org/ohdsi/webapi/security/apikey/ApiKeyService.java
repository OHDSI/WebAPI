package org.ohdsi.webapi.security.apikey;

import org.ohdsi.webapi.security.authz.User;
import org.ohdsi.webapi.security.authz.UserEntity;
import org.ohdsi.webapi.security.authz.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/**
 * Service for creating, validating, listing, and revoking personal API keys.
 *
 * <p>Key format: {@code wa_<identifier>_<secret>}
 * <ul>
 *   <li>{@code identifier} — 8 random bytes as lowercase hex (16 chars). Stored in plain text
 *       in an indexed column for O(1) lookup during authentication.</li>
 *   <li>{@code secret}     — 32 random bytes as Base64-URL without padding (43 chars). Never
 *       stored; only its BCrypt hash is persisted.</li>
 * </ul>
 *
 * <p>Authentication flow:
 * <ol>
 *   <li>Parse the key into {@code identifier} and {@code secret}.</li>
 *   <li>Look up the record by {@code identifier} (indexed, single-row read).</li>
 *   <li>Verify {@code secret} against the stored BCrypt hash via {@code matches()}.</li>
 * </ol>
 */
@Service
public class ApiKeyService {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyService.class);

    private static final String KEY_PREFIX = "wa";
    private static final int MAX_IDENTIFIER_RETRIES = 5;

    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;

    public ApiKeyService(ApiKeyRepository apiKeyRepository, UserRepository userRepository) {
        this.apiKeyRepository = apiKeyRepository;
        this.userRepository = userRepository;
        // BCrypt strength 12 is deliberately chosen for API keys: slow enough to resist brute-force,
        // fast enough that per-request validation is acceptable at normal traffic volumes.
        this.passwordEncoder = new BCryptPasswordEncoder(12);
        this.secureRandom = new SecureRandom();
    }

    // -------------------------------------------------------------------------
    // Public DTOs
    // -------------------------------------------------------------------------

    /**
     * Returned once at key creation time. {@code rawKey} is the full plaintext key
     * and is never retrievable again after this response.
     */
    public record ApiKeyResult(
            String name,
            String keyIdentifier,
            String rawKey,
            Instant createdAt,
            Instant expiresAt) {
    }

    /** Safe metadata about a key — never exposes the hash or the plaintext secret. */
    public record ApiKeyInfo(
            String name,
            String description,
            String keyIdentifier,
            Instant createdAt,
            Instant expiresAt,
            boolean disabled,
            Instant lastUsedAt) {
    }

    // -------------------------------------------------------------------------
    // Operations
    // -------------------------------------------------------------------------

    /**
     * Generates a new API key for the given user and stores only the BCrypt hash
     * of its secret in the database.
     *
     * @return {@link ApiKeyResult} including the one-time plaintext key.
     */
    @Transactional
    public ApiKeyResult generate(String login, String name, String description, Instant expiresAt) {
        UserEntity user = userRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalStateException("User not found: " + login));

        String identifier = generateUniqueIdentifier();

        byte[] secretBytes = new byte[32];
        secureRandom.nextBytes(secretBytes);
        String secret = Base64.getUrlEncoder().withoutPadding().encodeToString(secretBytes);

        String rawKey = KEY_PREFIX + "_" + identifier + "_" + secret;
        String keyHash = passwordEncoder.encode(secret);

        ApiKeyEntity entity = new ApiKeyEntity();
        entity.setKeyIdentifier(identifier);
        entity.setKeyHash(keyHash);
        entity.setUser(user);
        entity.setName(name);
        entity.setDescription(description);
        entity.setCreatedAt(Instant.now());
        entity.setExpiresAt(expiresAt);
        entity.setDisabled(false);
        apiKeyRepository.save(entity);

        log.info("API key created: identifier={}, user={}", entity.getKeyIdentifier(), login);
        return new ApiKeyResult(name, identifier, rawKey, entity.getCreatedAt(), expiresAt);
    }

    /**
     * Validates a raw API key presented in a request.
     *
     * <p>Steps: parse → indexed lookup by identifier → disabled/expiry check → BCrypt verify.
     * On success, updates {@code last_used_at} and returns the owning {@link UserEntity}.
     *
     * @return the owning user DTO, or empty if the key is invalid, disabled, or expired.
     */
    @Transactional
    public Optional<User> validate(String rawKey) {
        ParsedKey parsed = parseKey(rawKey);
        if (parsed == null) {
            return Optional.empty();
        }

        Optional<ApiKeyEntity> keyOpt = apiKeyRepository.findByKeyIdentifier(parsed.identifier());
        if (keyOpt.isEmpty()) {
            return Optional.empty();
        }

        ApiKeyEntity key = keyOpt.get();

        if (key.isDisabled()) {
            return Optional.empty();
        }
        if (key.getExpiresAt() != null && key.getExpiresAt().isBefore(Instant.now())) {
            return Optional.empty();
        }
        // BCrypt.matches() is the expensive step — it only runs after the O(1) identifier lookup.
        if (!passwordEncoder.matches(parsed.secret(), key.getKeyHash())) {
            return Optional.empty();
        }

        key.setLastUsedAt(Instant.now());
        apiKeyRepository.save(key);

        return Optional.of(User.fromEntity(key.getUser()));
    }

    /**
     * Returns metadata for all API keys owned by the given user.
     * The hash and plaintext secret are never included.
     */
    @Transactional(readOnly = true)
    public List<ApiKeyInfo> list(String login) {
        UserEntity user = userRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalStateException("User not found: " + login));
        return apiKeyRepository.findByUser(user).stream()
                .map(k -> new ApiKeyInfo(
                        k.getName(), k.getDescription(), k.getKeyIdentifier(),
                        k.getCreatedAt(), k.getExpiresAt(), k.isDisabled(), k.getLastUsedAt()))
                .toList();
    }

    /**
     * Disables an API key. Ownership is verified before revoking.
     *
     * @throws IllegalArgumentException if the key is not found or does not belong to {@code ownerLogin}.
     */
    @Transactional
    public void revoke(String keyIdentifier, String ownerLogin) {
        ApiKeyEntity key = apiKeyRepository.findByKeyIdentifier(keyIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("API key not found: " + keyIdentifier));
        if (!key.getUser().getLogin().equals(ownerLogin)) {
            throw new IllegalArgumentException("API key " + keyIdentifier + " does not belong to the current user");
        }
        key.setDisabled(true);
        apiKeyRepository.save(key);
        log.info("API key revoked: identifier={}, by user={}", keyIdentifier, ownerLogin);
    }

    /**
     * Permanently deletes an API key record. Ownership is verified before deletion.
     *
     * @throws IllegalArgumentException if the key is not found or does not belong to {@code ownerLogin}.
     */
    @Transactional
    public void delete(String keyIdentifier, String ownerLogin) {
        ApiKeyEntity key = apiKeyRepository.findByKeyIdentifier(keyIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("API key not found: " + keyIdentifier));
        if (!key.getUser().getLogin().equals(ownerLogin)) {
            throw new IllegalArgumentException("API key " + keyIdentifier + " does not belong to the current user");
        }
        apiKeyRepository.delete(key);
        log.info("API key permanently deleted: identifier={}, by user={}", keyIdentifier, ownerLogin);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Generates a hex-encoded identifier that does not already exist in the database.
     * Collisions are astronomically unlikely (64-bit space) but the retry loop handles them.
     */
    private String generateUniqueIdentifier() {
        for (int attempt = 0; attempt < MAX_IDENTIFIER_RETRIES; attempt++) {
            byte[] bytes = new byte[8];
            secureRandom.nextBytes(bytes);
            String identifier = HexFormat.of().formatHex(bytes);
            if (apiKeyRepository.findByKeyIdentifier(identifier).isEmpty()) {
                return identifier;
            }
            log.warn("API key identifier collision on attempt {}, retrying", attempt + 1);
        }
        throw new IllegalStateException(
                "Failed to generate a unique API key identifier after " + MAX_IDENTIFIER_RETRIES + " attempts");
    }

    private record ParsedKey(String identifier, String secret) {
    }

    /**
     * Parses a raw key in the form {@code wa_<identifier>_<secret>}.
     * The identifier is the portion before the first {@code _} after the prefix.
     * The secret is everything after that {@code _}, so the secret itself may safely
     * contain {@code _} characters (as Base64-URL encoding produces).
     */
    private ParsedKey parseKey(String rawKey) {
        final String prefixWithDelimiter = KEY_PREFIX + "_";
        if (rawKey == null || !rawKey.startsWith(prefixWithDelimiter)) {
            return null;
        }
        String rest = rawKey.substring(prefixWithDelimiter.length());
        int underscoreIdx = rest.indexOf('_');
        if (underscoreIdx < 1) {
            return null;
        }
        String identifier = rest.substring(0, underscoreIdx);
        String secret = rest.substring(underscoreIdx + 1);
        if (secret.isBlank()) {
            return null;
        }
        return new ParsedKey(identifier, secret);
    }
}
