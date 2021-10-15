INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
VALUES (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'reusable:get', 'List reusable'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'reusable:post', 'Create reusable'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'reusable:*:exists:get', 'Check name uniqueness of reusable'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'reusable:*:put', 'Update reusable'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'reusable:*:post', 'Copy reusable'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'reusable:*:get', 'Get reusable'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'reusable:*:delete', 'Delete reusable');

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP,
     ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
                   'reusable:get',
                   'reusable:post',
                   'reusable:*:post',
                   'reusable:*:exists:get',
                   'reusable:*:get')
  AND sr.name IN ('Atlas users');

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP,
     ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
                   'reusable:*:put', 'reusable:*:delete'
    ) AND sr.name IN ('Moderator');

CREATE SEQUENCE ${ohdsiSchema}.reusable_seq;

CREATE TABLE ${ohdsiSchema}.reusable
(
    id             int4                     NOT NULL DEFAULT nextval('${ohdsiSchema}.reusable_seq'),
    name           VARCHAR                  NOT NULL,
    description    varchar                  NULL,
    data           text                     NOT NULL,
    created_by_id  INTEGER,
    created_date   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
    modified_by_id INTEGER,
    modified_date  TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_reusable_id PRIMARY KEY (id),
    CONSTRAINT fk_reusable_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user (id),
    CONSTRAINT fk_reusable_sec_user_updater FOREIGN KEY (modified_by_id) REFERENCES ${ohdsiSchema}.sec_user (id)
);

CREATE UNIQUE INDEX reusable_name_idx ON ${ohdsiSchema}.reusable USING btree (LOWER(name));

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
VALUES (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'reusable:*:tag:post',
        'Assign tag to reusable'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'reusable:*:tag:*:delete',
        'Unassign tag from reusable'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'reusable:*:protectedtag:post',
        'Assign tag to reusable'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'reusable:*:protectedtag:*:delete',
        'Unassign tag from reusable');

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP,
     ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
                   'reusable:*:tag:post',
                   'reusable:*:tag:*:delete')
  AND sr.name IN ('Atlas users');

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP,
     ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
                   'reusable:*:protectedtag:post',
                   'reusable:*:protectedtag:*:delete')
  AND sr.name IN ('admin');

CREATE TABLE ${ohdsiSchema}.reusable_tag
(
    asset_id int4 NOT NULL,
    tag_id   int4 NOT NULL,
    CONSTRAINT pk_reusable_tag_id PRIMARY KEY (asset_id, tag_id),
    CONSTRAINT reusable_tag_fk_reusable FOREIGN KEY (asset_id) REFERENCES ${ohdsiSchema}.reusable (id) ON DELETE CASCADE,
    CONSTRAINT reusable_tag_fk_tag FOREIGN KEY (tag_id) REFERENCES ${ohdsiSchema}.tag (id) ON DELETE CASCADE
);

CREATE INDEX reusable_tag_reusableidx ON ${ohdsiSchema}.reusable_tag USING btree (asset_id);
CREATE INDEX reusable_tag_tag_id_idx ON ${ohdsiSchema}.reusable_tag USING btree (tag_id);

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
VALUES (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'reusable:*:version:get',
        'Get list of reusables versions'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'reusable:*:version:*:get',
        'Get reusable version'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'reusable:*:version:*:put',
        'Update reusable version info'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'reusable:*:version:*:delete',
        'Delete reusable version info'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'reusable:*:version:*:createAsset:put',
        'Copy reusable version as new reusable');

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP,
     ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
                   'reusable:*:version:get',
                   'reusable:*:version:*:get',
                   'reusable:*:version:*:put',
                   'reusable:*:version:*:delete',
                   'reusable:*:version:*:createAsset:put')
  AND sr.name IN ('Atlas users');

-- Reusables
CREATE TABLE ${ohdsiSchema}.reusable_version
(
    asset_id      int8                     NOT NULL,
    comment       varchar                  NULL,
    description   varchar                  NULL,
    version       int4                     NOT NULL DEFAULT 1,
    asset_json    varchar                  NOT NULL,
    archived      bool                     NOT NULL DEFAULT FALSE,
    created_by_id INTEGER,
    created_date  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
    CONSTRAINT pk_reusable_version_id PRIMARY KEY (asset_id, version),
    CONSTRAINT fk_reusable_version_sec_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user (id),
    CONSTRAINT fk_reusable_version_asset_id FOREIGN KEY (asset_id) REFERENCES ${ohdsiSchema}.reusable (id) ON DELETE CASCADE
);

CREATE INDEX reusable_version_asset_idx ON ${ohdsiSchema}.reusable_version USING btree (asset_id);
