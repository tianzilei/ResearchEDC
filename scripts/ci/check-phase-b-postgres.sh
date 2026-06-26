#!/usr/bin/env bash
set -euo pipefail

echo "=== Phase B PostgreSQL validation ==="

required_env=(
  PHASE_B_PGHOST
  PHASE_B_PGPORT
  PHASE_B_PGDATABASE
  PHASE_B_PGUSER
  PHASE_B_PGPASSWORD
)

for name in "${required_env[@]}"; do
  if [ -z "${!name:-}" ]; then
    echo "SKIP: ${name} is not set. This check only runs against an explicit disposable PostgreSQL database."
    exit 0
  fi
done

export PGPASSWORD="${PHASE_B_PGPASSWORD}"

psql_args=(
  -v ON_ERROR_STOP=1
  -h "${PHASE_B_PGHOST}"
  -p "${PHASE_B_PGPORT}"
  -U "${PHASE_B_PGUSER}"
  -d "${PHASE_B_PGDATABASE}"
)

psql "${psql_args[@]}" <<'SQL'
\echo 'Checking Phase B sync trigger and function inventory'

CREATE TEMP TABLE phase_b_expected_functions(function_name text);
INSERT INTO phase_b_expected_functions(function_name) VALUES
  ('sync_study_to_module_study'),
  ('sync_module_study_to_study'),
  ('sync_subject_to_module_subject'),
  ('sync_module_subject_to_subject'),
  ('sync_study_subject_to_module_study_subject'),
  ('sync_module_study_subject_to_study_subject'),
  ('sync_study_event_def_to_module_study_event_def'),
  ('sync_module_study_event_def_to_study_event_def'),
  ('sync_study_event_to_module_study_event'),
  ('sync_module_study_event_to_study_event'),
  ('sync_event_crf_to_module_event_crf'),
  ('sync_module_event_crf_to_event_crf'),
  ('sync_event_definition_crf_to_module_event_definition_crf'),
  ('sync_module_event_definition_crf_to_event_definition_crf'),
  ('sync_item_data_to_module_item_data'),
  ('sync_module_item_data_to_item_data'),
  ('sync_item_group_to_module_item_group'),
  ('sync_module_item_group_to_item_group'),
  ('sync_item_group_metadata_to_module_item_group_metadata'),
  ('sync_module_item_group_metadata_to_item_group_metadata'),
  ('sync_response_set_to_module_response_set'),
  ('sync_module_response_set_to_response_set'),
  ('sync_crf_to_module_crf'),
  ('sync_module_crf_to_crf'),
  ('sync_crf_version_to_module_crf_version'),
  ('sync_module_crf_version_to_crf_version'),
  ('sync_item_to_module_item'),
  ('sync_module_item_to_item'),
  ('sync_item_form_metadata_to_module_ifm'),
  ('sync_module_ifm_to_item_form_metadata'),
  ('sync_section_to_module_section'),
  ('sync_module_section_to_section'),
  ('sync_user_account_to_module_user_account'),
  ('sync_module_user_account_to_user_account'),
  ('sync_study_user_role_to_module_study_user_role'),
  ('sync_module_study_user_role_to_study_user_role'),
  ('sync_rule_to_module_rule'),
  ('sync_module_rule_to_rule'),
  ('sync_rule_set_to_module_rule_set'),
  ('sync_module_rule_set_to_rule_set'),
  ('sync_rule_set_rule_to_module_rule_set_rule'),
  ('sync_module_rule_set_rule_to_rule_set_rule'),
  ('sync_rule_expression_to_module_rule_expression'),
  ('sync_module_rule_expression_to_rule_expression'),
  ('sync_dataset_to_module_dataset'),
  ('sync_module_dataset_to_dataset'),
  ('sync_filter_to_module_filter'),
  ('sync_module_filter_to_filter'),
  ('sync_discrepancy_note_to_module_discrepancy_note'),
  ('sync_module_discrepancy_note_to_discrepancy_note'),
  ('sync_study_group_class_to_module_study_group_class'),
  ('sync_module_study_group_class_to_study_group_class'),
  ('sync_study_group_to_module_study_group'),
  ('sync_module_study_group_to_study_group'),
  ('neutralize_retired_user_account_email'),
  ('neutralize_retired_study_contact_email');

DO $$
DECLARE missing text;
BEGIN
  SELECT string_agg(function_name, ', ') INTO missing
  FROM phase_b_expected_functions
  WHERE to_regprocedure(function_name || '()') IS NULL;
  IF missing IS NOT NULL THEN
    RAISE EXCEPTION 'Missing Phase B sync functions: %', missing;
  END IF;
END $$;

\echo 'All expected sync functions present'

CREATE TEMP TABLE phase_b_expected_triggers(table_name text, trigger_name text);
INSERT INTO phase_b_expected_triggers(table_name, trigger_name) VALUES
  ('study', 'trg_sync_study_to_module'),
  ('module_study', 'trg_sync_module_study'),
  ('subject', 'trg_sync_subject_to_module'),
  ('module_subject', 'trg_sync_module_subject'),
  ('study_subject', 'trg_sync_study_subject_to_module'),
  ('module_study_subject', 'trg_sync_module_study_subject'),
  ('study_event_definition', 'trg_sync_study_event_def_to_module'),
  ('module_study_event_definition', 'trg_sync_module_study_event_def'),
  ('study_event', 'trg_sync_study_event_to_module'),
  ('module_study_event', 'trg_sync_module_study_event'),
  ('event_crf', 'trg_sync_event_crf_to_module'),
  ('module_event_crf', 'trg_sync_module_event_crf'),
  ('event_definition_crf', 'trg_sync_event_definition_crf_to_module'),
  ('module_event_definition_crf', 'trg_sync_module_event_definition_crf'),
  ('item_data', 'trg_sync_item_data_to_module'),
  ('module_item_data', 'trg_sync_module_item_data'),
  ('item_group', 'trg_sync_item_group_to_module'),
  ('module_item_group', 'trg_sync_module_item_group'),
  ('item_group_metadata', 'trg_sync_item_group_metadata_to_module'),
  ('module_item_group_metadata', 'trg_sync_module_item_group_metadata'),
  ('response_set', 'trg_sync_response_set_to_module'),
  ('module_response_set', 'trg_sync_module_response_set'),
  ('crf', 'trg_sync_crf_to_module'),
  ('module_crf', 'trg_sync_module_crf'),
  ('crf_version', 'trg_sync_crf_version_to_module'),
  ('module_crf_version', 'trg_sync_module_crf_version'),
  ('item', 'trg_sync_item_to_module'),
  ('module_item', 'trg_sync_module_item'),
  ('item_form_metadata', 'trg_sync_item_form_metadata_to_module'),
  ('module_item_form_metadata', 'trg_sync_module_ifm'),
  ('section', 'trg_sync_section_to_module'),
  ('module_section', 'trg_sync_module_section'),
  ('user_account', 'trg_sync_user_account_to_module'),
  ('module_user_account', 'trg_sync_module_user_account'),
  ('study_user_role', 'trg_sync_study_user_role_to_module'),
  ('module_study_user_role', 'trg_sync_module_study_user_role'),
  ('rule', 'trg_sync_rule_to_module'),
  ('module_rule', 'trg_sync_module_rule'),
  ('rule_set', 'trg_sync_rule_set_to_module'),
  ('module_rule_set', 'trg_sync_module_rule_set'),
  ('rule_set_rule', 'trg_sync_rule_set_rule_to_module'),
  ('module_rule_set_rule', 'trg_sync_module_rule_set_rule'),
  ('rule_expression', 'trg_sync_rule_expression_to_module'),
  ('module_rule_expression', 'trg_sync_module_rule_expression'),
  ('dataset', 'trg_sync_dataset_to_module'),
  ('module_dataset', 'trg_sync_module_dataset_to_dataset'),
  ('filter', 'trg_sync_filter_to_module'),
  ('module_filter', 'trg_sync_module_filter_to_filter'),
  ('discrepancy_note', 'trg_sync_discrepancy_note_to_module'),
  ('module_discrepancy_note', 'trg_sync_module_discrepancy_note'),
  ('study_group_class', 'trg_sync_study_group_class_to_module'),
  ('module_study_group_class', 'trg_sync_module_study_group_class'),
  ('study_group', 'trg_sync_study_group_to_module'),
  ('module_study_group', 'trg_sync_module_study_group'),
  ('user_account', 'trg_neutralize_user_account_email'),
  ('module_user_account', 'trg_neutralize_module_user_account_email'),
  ('study', 'trg_neutralize_study_contact_email'),
  ('module_study', 'trg_neutralize_module_study_contact_email');

DO $$
DECLARE missing text;
BEGIN
  SELECT string_agg(e.table_name || '.' || e.trigger_name, ', ') INTO missing
  FROM phase_b_expected_triggers e
  WHERE NOT EXISTS (
    SELECT 1
    FROM information_schema.triggers t
    WHERE t.trigger_schema = 'public'
      AND t.event_object_table = e.table_name
      AND t.trigger_name = e.trigger_name
  );
  IF missing IS NOT NULL THEN
    RAISE EXCEPTION 'Missing Phase B sync triggers: %', missing;
  END IF;
END $$;

\echo 'All expected sync triggers present'

\echo 'Checking representative bidirectional data sync'

DO $$
DECLARE
  legacy_id int := 910000001;
  module_id int := 910000002;
  loop_id int := 910000003;
BEGIN
  DELETE FROM module_discrepancy_note WHERE discrepancy_note_id IN (legacy_id, module_id, loop_id);
  DELETE FROM discrepancy_note WHERE discrepancy_note_id IN (legacy_id, module_id, loop_id);
  DELETE FROM module_filter WHERE filter_id IN (legacy_id, module_id, loop_id);
  DELETE FROM filter WHERE filter_id IN (legacy_id, module_id, loop_id);
  DELETE FROM module_user_account WHERE user_id IN (legacy_id, module_id, loop_id);
  DELETE FROM user_account WHERE user_id IN (legacy_id, module_id, loop_id);
  DELETE FROM module_study WHERE study_id IN (legacy_id, module_id, loop_id);
  DELETE FROM study WHERE study_id IN (legacy_id, module_id, loop_id);

  INSERT INTO study (study_id, unique_identifier, name, owner_id, status_id, type_id, oc_oid, feature_flags)
  VALUES (legacy_id, 'phase-b-legacy', 'Phase B legacy study', 1, 1, 1, 'S_PHASE_B_LEGACY', '{}'::jsonb);
  IF NOT EXISTS (SELECT 1 FROM module_study WHERE study_id = legacy_id AND name = 'Phase B legacy study') THEN
    RAISE EXCEPTION 'study -> module_study insert did not sync';
  END IF;
  UPDATE study SET name = 'Phase B legacy study updated' WHERE study_id = legacy_id;
  IF NOT EXISTS (SELECT 1 FROM module_study WHERE study_id = legacy_id AND name = 'Phase B legacy study updated') THEN
    RAISE EXCEPTION 'study -> module_study update did not sync';
  END IF;
  DELETE FROM study WHERE study_id = legacy_id;
  IF EXISTS (SELECT 1 FROM module_study WHERE study_id = legacy_id) THEN
    RAISE EXCEPTION 'study -> module_study delete did not sync';
  END IF;

  INSERT INTO module_study (study_id, unique_identifier, name, owner_id, status_id, type_id, oc_oid, feature_flags)
  VALUES (module_id, 'phase-b-module', 'Phase B module study', 1, 1, 1, 'S_PHASE_B_MODULE', '{}'::jsonb);
  IF NOT EXISTS (SELECT 1 FROM study WHERE study_id = module_id AND name = 'Phase B module study') THEN
    RAISE EXCEPTION 'module_study -> study insert did not sync';
  END IF;
  UPDATE module_study SET name = 'Phase B module study updated' WHERE study_id = module_id;
  IF NOT EXISTS (SELECT 1 FROM study WHERE study_id = module_id AND name = 'Phase B module study updated') THEN
    RAISE EXCEPTION 'module_study -> study update did not sync';
  END IF;
  DELETE FROM module_study WHERE study_id = module_id;
  IF EXISTS (SELECT 1 FROM study WHERE study_id = module_id) THEN
    RAISE EXCEPTION 'module_study -> study delete did not sync';
  END IF;

  INSERT INTO study (study_id, unique_identifier, name, owner_id, status_id, type_id, oc_oid, facility_contact_email, feature_flags)
  VALUES (legacy_id, 'phase-b-legacy-email', 'Phase B legacy email study', 1, 1, 1, 'S_PHASE_B_LEGACY_EMAIL', 'legacy@example.com', '{}'::jsonb);
  IF EXISTS (SELECT 1 FROM study WHERE study_id = legacy_id AND facility_contact_email <> '') THEN
    RAISE EXCEPTION 'study facility_contact_email was not neutralized';
  END IF;
  IF EXISTS (SELECT 1 FROM module_study WHERE study_id = legacy_id AND facility_contact_email <> '') THEN
    RAISE EXCEPTION 'neutralized study facility_contact_email did not propagate to module_study';
  END IF;
  UPDATE module_study SET facility_contact_email = 'module@example.com' WHERE study_id = legacy_id;
  IF EXISTS (SELECT 1 FROM module_study WHERE study_id = legacy_id AND facility_contact_email <> '') THEN
    RAISE EXCEPTION 'module_study facility_contact_email was not neutralized';
  END IF;
  IF EXISTS (SELECT 1 FROM study WHERE study_id = legacy_id AND facility_contact_email <> '') THEN
    RAISE EXCEPTION 'neutralized module_study facility_contact_email did not propagate to study';
  END IF;
  DELETE FROM study WHERE study_id = legacy_id;

  INSERT INTO user_account (
    user_id, user_name, first_name, last_name, email, status_id, user_type_id, owner_id
  )
  VALUES (
    legacy_id, 'phase-b-legacy-user', 'Phase', 'Legacy', 'legacy-user@example.com', 1, 1, 1
  );
  IF EXISTS (SELECT 1 FROM user_account WHERE user_id = legacy_id AND email <> '') THEN
    RAISE EXCEPTION 'user_account email was not neutralized';
  END IF;
  IF EXISTS (SELECT 1 FROM module_user_account WHERE user_id = legacy_id AND email <> '') THEN
    RAISE EXCEPTION 'neutralized user_account email did not propagate to module_user_account';
  END IF;
  UPDATE module_user_account SET email = 'module-user@example.com' WHERE user_id = legacy_id;
  IF EXISTS (SELECT 1 FROM module_user_account WHERE user_id = legacy_id AND email <> '') THEN
    RAISE EXCEPTION 'module_user_account email was not neutralized';
  END IF;
  IF EXISTS (SELECT 1 FROM user_account WHERE user_id = legacy_id AND email <> '') THEN
    RAISE EXCEPTION 'neutralized module_user_account email did not propagate to user_account';
  END IF;
  DELETE FROM user_account WHERE user_id = legacy_id;

  INSERT INTO filter (filter_id, name, description, sql_statement, status_id, date_created, owner_id)
  VALUES (legacy_id, 'phase-b-legacy-filter', 'legacy filter', 'select 1', 1, now(), 1);
  IF NOT EXISTS (SELECT 1 FROM module_filter WHERE filter_id = legacy_id AND name = 'phase-b-legacy-filter') THEN
    RAISE EXCEPTION 'filter -> module_filter insert did not sync';
  END IF;
  UPDATE filter SET name = 'phase-b-legacy-filter-updated' WHERE filter_id = legacy_id;
  IF NOT EXISTS (SELECT 1 FROM module_filter WHERE filter_id = legacy_id AND name = 'phase-b-legacy-filter-updated') THEN
    RAISE EXCEPTION 'filter -> module_filter update did not sync';
  END IF;
  DELETE FROM filter WHERE filter_id = legacy_id;
  IF EXISTS (SELECT 1 FROM module_filter WHERE filter_id = legacy_id) THEN
    RAISE EXCEPTION 'filter -> module_filter delete did not sync';
  END IF;

  INSERT INTO module_filter (filter_id, name, description, sql_statement, status_id, date_created, owner_id)
  VALUES (module_id, 'phase-b-module-filter', 'module filter', 'select 2', 1, now(), 1);
  IF NOT EXISTS (SELECT 1 FROM filter WHERE filter_id = module_id AND name = 'phase-b-module-filter') THEN
    RAISE EXCEPTION 'module_filter -> filter insert did not sync';
  END IF;
  UPDATE module_filter SET name = 'phase-b-module-filter-updated' WHERE filter_id = module_id;
  IF NOT EXISTS (SELECT 1 FROM filter WHERE filter_id = module_id AND name = 'phase-b-module-filter-updated') THEN
    RAISE EXCEPTION 'module_filter -> filter update did not sync';
  END IF;
  DELETE FROM module_filter WHERE filter_id = module_id;
  IF EXISTS (SELECT 1 FROM filter WHERE filter_id = module_id) THEN
    RAISE EXCEPTION 'module_filter -> filter delete did not sync';
  END IF;

  INSERT INTO discrepancy_note (
    discrepancy_note_id, description, discrepancy_note_type_id, resolution_status_id,
    detailed_notes, date_created, owner_id, entity_type, study_id
  )
  VALUES (legacy_id, 'legacy dn', 1, 1, 'legacy details', now(), 1, 'ItemData', 1);
  IF NOT EXISTS (SELECT 1 FROM module_discrepancy_note WHERE discrepancy_note_id = legacy_id AND description = 'legacy dn') THEN
    RAISE EXCEPTION 'discrepancy_note -> module_discrepancy_note insert did not sync';
  END IF;
  UPDATE discrepancy_note SET description = 'legacy dn updated' WHERE discrepancy_note_id = legacy_id;
  IF NOT EXISTS (SELECT 1 FROM module_discrepancy_note WHERE discrepancy_note_id = legacy_id AND description = 'legacy dn updated') THEN
    RAISE EXCEPTION 'discrepancy_note -> module_discrepancy_note update did not sync';
  END IF;
  DELETE FROM discrepancy_note WHERE discrepancy_note_id = legacy_id;
  IF EXISTS (SELECT 1 FROM module_discrepancy_note WHERE discrepancy_note_id = legacy_id) THEN
    RAISE EXCEPTION 'discrepancy_note -> module_discrepancy_note delete did not sync';
  END IF;

  INSERT INTO module_discrepancy_note (
    discrepancy_note_id, description, discrepancy_note_type_id, resolution_status_id,
    detailed_notes, date_created, owner_id, entity_type, study_id
  )
  VALUES (module_id, 'module dn', 1, 1, 'module details', now(), 1, 'ItemData', 1);
  IF NOT EXISTS (SELECT 1 FROM discrepancy_note WHERE discrepancy_note_id = module_id AND description = 'module dn') THEN
    RAISE EXCEPTION 'module_discrepancy_note -> discrepancy_note insert did not sync';
  END IF;
  UPDATE module_discrepancy_note SET description = 'module dn updated' WHERE discrepancy_note_id = module_id;
  IF NOT EXISTS (SELECT 1 FROM discrepancy_note WHERE discrepancy_note_id = module_id AND description = 'module dn updated') THEN
    RAISE EXCEPTION 'module_discrepancy_note -> discrepancy_note update did not sync';
  END IF;
  DELETE FROM module_discrepancy_note WHERE discrepancy_note_id = module_id;
  IF EXISTS (SELECT 1 FROM discrepancy_note WHERE discrepancy_note_id = module_id) THEN
    RAISE EXCEPTION 'module_discrepancy_note -> discrepancy_note delete did not sync';
  END IF;

  INSERT INTO study (study_id, unique_identifier, name, owner_id, status_id, type_id, oc_oid, feature_flags)
  VALUES (loop_id, 'phase-b-loop', 'Phase B loop study', 1, 1, 1, 'S_PHASE_B_LOOP', '{}'::jsonb);
  FOR i IN 1..20 LOOP
    UPDATE study SET name = 'Phase B loop study ' || i WHERE study_id = loop_id;
    UPDATE module_study SET name = 'Phase B module loop study ' || i WHERE study_id = loop_id;
  END LOOP;
  IF NOT EXISTS (
    SELECT 1
    FROM study s
    JOIN module_study ms ON ms.study_id = s.study_id
    WHERE s.study_id = loop_id
      AND s.name = 'Phase B module loop study 20'
      AND ms.name = 'Phase B module loop study 20'
  ) THEN
    RAISE EXCEPTION 'repeated bidirectional updates did not converge';
  END IF;

  DELETE FROM module_study WHERE study_id IN (legacy_id, module_id, loop_id);
  DELETE FROM study WHERE study_id IN (legacy_id, module_id, loop_id);
  DELETE FROM module_filter WHERE filter_id IN (legacy_id, module_id, loop_id);
  DELETE FROM filter WHERE filter_id IN (legacy_id, module_id, loop_id);
  DELETE FROM module_discrepancy_note WHERE discrepancy_note_id IN (legacy_id, module_id, loop_id);
  DELETE FROM discrepancy_note WHERE discrepancy_note_id IN (legacy_id, module_id, loop_id);
END $$;

\echo 'Phase B PostgreSQL validation passed.'
SQL
