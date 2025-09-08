CREATE SEQUENCE ${ohdsiSchema}.review_action_seq START WITH 1;
CREATE TABLE ${ohdsiSchema}.review_action (
  id BIGINT PRIMARY KEY DEFAULT nextval('${ohdsiSchema}.review_action_seq'),
  timestamp TIMESTAMP NOT NULL,
  asset_type VARCHAR(50) NOT NULL,
  asset_id BIGINT NOT NULL,
  version INT4,
  user_id INTEGER,
  action VARCHAR NOT NULL,
  comment VARCHAR(2000),
  revoke_comment VARCHAR(2000),
  supporting_info VARCHAR(4000),
  representative_id INTEGER,

  CONSTRAINT fk_review_action__user FOREIGN KEY (user_id) REFERENCES ${ohdsiSchema}.sec_user (id),
  CONSTRAINT fk_review_action__representative FOREIGN KEY (representative_id) REFERENCES ${ohdsiSchema}.sec_user (id)
);
