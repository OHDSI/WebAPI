INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
VALUES (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'reusable:get', 'List reusable'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'reusable:post', 'Create reusable'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'reusable:*:put', 'Update reusable'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'reusable:*:get', 'Get reusable'),
       (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'reusable:*:delete', 'Delete reusable');

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP,
     ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
                   'reusable:get',
                   'reusable:post',
                   'reusable:*:put',
                   'reusable:*:get',
                   'reusable:*:delete')
  AND sr.name IN ('Atlas users');

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
