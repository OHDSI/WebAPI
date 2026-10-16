package org.ohdsi.webapi.security.apikey;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import org.ohdsi.webapi.security.authz.UserEntity;

import java.time.Instant;

@Entity(name = "ApiKey")
@Table(name = "sec_api_key")
public class ApiKeyEntity {

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "sec_api_key_seq", sequenceName = "sec_api_key_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sec_api_key_seq")
    private Long id;

    /**
     * Public, indexed portion of the key used for O(1) database lookup.
     * This is NOT a secret — it is stored as plain text.
     */
    @Column(name = "key_identifier", unique = true, nullable = false, length = 64)
    private String keyIdentifier;

    /**
     * BCrypt hash of the secret portion of the key.
     * The plaintext secret is never stored.
     */
    @Column(name = "key_hash", nullable = false, length = 255)
    private String keyHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "disabled", nullable = false)
    private boolean disabled = false;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getKeyIdentifier() { return keyIdentifier; }
    public void setKeyIdentifier(String keyIdentifier) { this.keyIdentifier = keyIdentifier; }

    public String getKeyHash() { return keyHash; }
    public void setKeyHash(String keyHash) { this.keyHash = keyHash; }

    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public boolean isDisabled() { return disabled; }
    public void setDisabled(boolean disabled) { this.disabled = disabled; }

    public Instant getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(Instant lastUsedAt) { this.lastUsedAt = lastUsedAt; }
}
