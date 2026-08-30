-- Create sec_external_role_map table for runtime authentication-based role mapping
-- Maps external identities (LDAP groups, OIDC claims, Windows groups, etc.) to WebAPI roles

CREATE SEQUENCE ${ohdsiSchema}.sec_external_role_map_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE ${ohdsiSchema}.sec_external_role_map (
    id INTEGER DEFAULT nextval('${ohdsiSchema}.sec_external_role_map_seq'::regclass) NOT NULL,
    origin VARCHAR(32) NOT NULL,
    external_claim VARCHAR(255) NOT NULL,
    role_id INTEGER NOT NULL,
    description VARCHAR(500)
);

ALTER TABLE ONLY ${ohdsiSchema}.sec_external_role_map
    ADD CONSTRAINT sec_external_role_map_pkey PRIMARY KEY (id);

ALTER TABLE ONLY ${ohdsiSchema}.sec_external_role_map
    ADD CONSTRAINT unique_origin_claim_role UNIQUE (origin, external_claim, role_id);

ALTER TABLE ONLY ${ohdsiSchema}.sec_external_role_map
    ADD CONSTRAINT fk_external_role_map_role FOREIGN KEY (role_id) REFERENCES ${ohdsiSchema}.sec_role(id) ON DELETE CASCADE;

CREATE INDEX idx_external_role_map_origin_claim ON ${ohdsiSchema}.sec_external_role_map(origin, external_claim);
