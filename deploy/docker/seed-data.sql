-- ResearchEDC Dev Seed Data
-- Runs on first PostgreSQL initialization via docker-entrypoint-initdb.d
-- Order: sequences -> lookup tables -> core tables -> CRF definitions -> subjects/events/data

-- ── Sequences ──────────────────────────────────────────────
CREATE SEQUENCE IF NOT EXISTS user_account_user_id_seq START 100;
CREATE SEQUENCE IF NOT EXISTS study_study_id_seq START 100;
CREATE SEQUENCE IF NOT EXISTS study_event_definition_id_seq START 100;
CREATE SEQUENCE IF NOT EXISTS study_event_id_seq START 100;
CREATE SEQUENCE IF NOT EXISTS event_crf_id_seq START 100;
CREATE SEQUENCE IF NOT EXISTS crf_crf_id_seq START 100;
CREATE SEQUENCE IF NOT EXISTS crf_version_id_seq START 100;
CREATE SEQUENCE IF NOT EXISTS section_id_seq START 100;
CREATE SEQUENCE IF NOT EXISTS item_id_seq START 100;
CREATE SEQUENCE IF NOT EXISTS item_data_id_seq START 100;
CREATE SEQUENCE IF NOT EXISTS response_set_id_seq START 100;
CREATE SEQUENCE IF NOT EXISTS item_group_id_seq START 100;
CREATE SEQUENCE IF NOT EXISTS item_form_metadata_id_seq START 100;
CREATE SEQUENCE IF NOT EXISTS study_subject_id_seq START 100;
CREATE SEQUENCE IF NOT EXISTS subject_id_seq START 100;
CREATE SEQUENCE IF NOT EXISTS module_user_account_id_seq START 100;

-- ── Lookup Tables ──────────────────────────────────────────

CREATE TABLE IF NOT EXISTS status (
    status_id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS user_type (
    user_type_id SERIAL PRIMARY KEY,
    user_type VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS user_role (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    parent_id INT,
    role_desc VARCHAR(2000)
);

CREATE TABLE IF NOT EXISTS study_type (
    study_type_id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS response_type (
    response_type_id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS item_data_type (
    item_data_type_id SERIAL PRIMARY KEY,
    code VARCHAR(20),
    name VARCHAR(255),
    definition VARCHAR(1000),
    reference VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS item_reference_type (
    item_reference_type_id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS subject_event_status (
    subject_event_status_id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS completion_status (
    completion_status_id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(1000),
    status_id INT
);

CREATE TABLE IF NOT EXISTS audit_log_event_type (
    audit_log_event_type_id SERIAL PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS discrepancy_note_type (
    discrepancy_note_type_id SERIAL PRIMARY KEY,
    name VARCHAR(50),
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS resolution_status (
    resolution_status_id SERIAL PRIMARY KEY,
    name VARCHAR(50),
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS study_parameter (
    study_parameter_id SERIAL PRIMARY KEY,
    handle VARCHAR(50),
    name VARCHAR(50),
    description VARCHAR(255),
    default_value VARCHAR(50),
    inheritable BOOLEAN DEFAULT TRUE,
    overridable BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS group_class_types (
    group_class_type_id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(1000)
);

-- ── Core Tables ────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS user_account (
    user_id SERIAL PRIMARY KEY,
    user_name VARCHAR(64),
    passwd VARCHAR(255),
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(120),
    phone VARCHAR(64),
    institutional_affiliation VARCHAR(255),
    user_type_id INT,
    status_id INT,
    active_study INT,
    owner_id INT,
    update_id INT,
    date_created DATE DEFAULT CURRENT_DATE,
    date_updated DATE,
    date_lastvisit TIMESTAMP,
    passwd_timestamp DATE,
    passwd_challenge_question VARCHAR(64),
    passwd_challenge_answer VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    lock_counter INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS study_user_role (
    study_user_role_id BIGSERIAL,
    role_name VARCHAR(40),
    study_id INT,
    status_id INT,
    owner_id INT,
    date_created DATE DEFAULT CURRENT_DATE,
    date_updated DATE,
    update_id INT,
    user_name VARCHAR(40)
);
CREATE SEQUENCE IF NOT EXISTS study_user_role_id_seq START 100;

CREATE TABLE IF NOT EXISTS study (
    study_id SERIAL PRIMARY KEY,
    parent_study_id INT,
    unique_identifier VARCHAR(30),
    secondary_identifier VARCHAR(255),
    name VARCHAR(255),
    summary VARCHAR(255),
    date_planned_start DATE,
    date_planned_end DATE,
    date_created DATE DEFAULT CURRENT_DATE,
    date_updated DATE,
    owner_id INT,
    update_id INT,
    type_id INT,
    status_id INT,
    principal_investigator VARCHAR(255),
    facility_name VARCHAR(255),
    facility_city VARCHAR(255),
    facility_state VARCHAR(20),
    facility_zip VARCHAR(64),
    facility_country VARCHAR(64),
    facility_recruitment_status VARCHAR(60),
    facility_contact_name VARCHAR(255),
    facility_contact_degree VARCHAR(255),
    facility_contact_phone VARCHAR(255),
    facility_contact_email VARCHAR(255),
    protocol_type VARCHAR(30),
    protocol_description VARCHAR(1000),
    protocol_date_verification DATE,
    phase VARCHAR(30),
    expected_total_enrollment INT,
    sponsor VARCHAR(255),
    collaborators VARCHAR(1000),
    medline_identifier VARCHAR(255),
    url VARCHAR(255),
    url_description VARCHAR(255),
    conditions VARCHAR(500),
    keywords VARCHAR(255),
    eligibility VARCHAR(500),
    gender VARCHAR(30),
    age_max VARCHAR(3),
    age_min VARCHAR(3),
    healthy_volunteer_accepted BOOLEAN,
    purpose VARCHAR(64),
    allocation VARCHAR(64),
    masking VARCHAR(30),
    control VARCHAR(30),
    assignment VARCHAR(30),
    endpoint VARCHAR(64),
    interventions VARCHAR(1000),
    duration VARCHAR(30),
    selection VARCHAR(30),
    timing VARCHAR(30),
    official_title VARCHAR(255),
    results_reference BOOLEAN,
    oc_oid VARCHAR(40) NOT NULL
);

CREATE TABLE IF NOT EXISTS study_parameter_value (
    study_parameter_value_id SERIAL PRIMARY KEY,
    study_id INT,
    value VARCHAR(50),
    parameter VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS study_event_definition (
    study_event_definition_id SERIAL PRIMARY KEY,
    study_id INT,
    name VARCHAR(2000),
    description VARCHAR(2000),
    repeating BOOLEAN,
    type VARCHAR(20),
    category VARCHAR(2000),
    owner_id INT,
    status_id INT,
    date_created DATE DEFAULT CURRENT_DATE,
    date_updated DATE,
    update_id INT,
    ordinal INT,
    oc_oid VARCHAR(40) NOT NULL
);

CREATE TABLE IF NOT EXISTS crf (
    crf_id SERIAL PRIMARY KEY,
    status_id INT,
    name VARCHAR(255),
    description VARCHAR(2048),
    owner_id INT,
    date_created DATE DEFAULT CURRENT_DATE,
    date_updated DATE,
    update_id INT,
    oc_oid VARCHAR(40) NOT NULL
);

CREATE TABLE IF NOT EXISTS crf_version (
    crf_version_id SERIAL PRIMARY KEY,
    crf_id INT NOT NULL,
    name VARCHAR(255),
    description VARCHAR(4000),
    revision_notes VARCHAR(255),
    status_id INT,
    date_created DATE DEFAULT CURRENT_DATE,
    date_updated DATE,
    owner_id INT,
    update_id INT,
    oc_oid VARCHAR(40) NOT NULL
);

CREATE TABLE IF NOT EXISTS event_definition_crf (
    event_definition_crf_id SERIAL PRIMARY KEY,
    study_event_definition_id INT,
    study_id INT,
    crf_id INT,
    required_crf BOOLEAN DEFAULT FALSE,
    double_entry BOOLEAN DEFAULT FALSE,
    require_all_text_filled BOOLEAN DEFAULT FALSE,
    decision_conditions BOOLEAN DEFAULT FALSE,
    null_values VARCHAR(255),
    default_version_id INT,
    status_id INT,
    owner_id INT,
    date_created DATE DEFAULT CURRENT_DATE,
    date_updated DATE,
    update_id INT,
    ordinal INT,
    electronic_signature BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS subject (
    subject_id SERIAL PRIMARY KEY,
    father_id INT,
    mother_id INT,
    status_id INT,
    date_of_birth DATE,
    gender CHAR(1),
    unique_identifier VARCHAR(255),
    date_created DATE DEFAULT CURRENT_DATE,
    owner_id INT,
    date_updated DATE,
    update_id INT,
    dob_collected BOOLEAN
);

CREATE TABLE IF NOT EXISTS study_subject (
    study_subject_id SERIAL PRIMARY KEY,
    label VARCHAR(30),
    secondary_label VARCHAR(30),
    subject_id INT,
    study_id INT,
    status_id INT,
    enrollment_date DATE,
    date_created DATE DEFAULT CURRENT_DATE,
    date_updated DATE,
    owner_id INT,
    update_id INT,
    oc_oid VARCHAR(40) NOT NULL
);

CREATE TABLE IF NOT EXISTS study_event (
    study_event_id SERIAL PRIMARY KEY,
    study_event_definition_id INT,
    study_subject_id INT,
    location VARCHAR(2000),
    sample_ordinal INT,
    date_start TIMESTAMP,
    date_end TIMESTAMP,
    owner_id INT,
    status_id INT,
    date_created DATE DEFAULT CURRENT_DATE,
    date_updated DATE,
    update_id INT,
    subject_event_status_id INT,
    start_time_flag BOOLEAN,
    end_time_flag BOOLEAN
);

CREATE TABLE IF NOT EXISTS event_crf (
    event_crf_id SERIAL PRIMARY KEY,
    study_event_id INT,
    crf_version_id INT,
    date_interviewed DATE,
    interviewer_name VARCHAR(255),
    completion_status_id INT,
    status_id INT,
    annotations VARCHAR(4000),
    date_completed TIMESTAMP,
    validator_id INT,
    date_validate DATE,
    date_validate_completed TIMESTAMP,
    validator_annotations VARCHAR(4000),
    validate_string VARCHAR(256),
    owner_id INT,
    date_created DATE DEFAULT CURRENT_DATE,
    study_subject_id INT,
    date_updated DATE,
    update_id INT,
    electronic_signature_status BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS section (
    section_id SERIAL PRIMARY KEY,
    crf_version_id INT NOT NULL,
    status_id INT,
    label VARCHAR(2000),
    title VARCHAR(2000),
    subtitle VARCHAR(2000),
    instructions VARCHAR(2000),
    page_number_label VARCHAR(5),
    ordinal INT,
    parent_id INT,
    date_created DATE DEFAULT CURRENT_DATE,
    date_updated DATE,
    owner_id INT NOT NULL,
    update_id INT,
    borders INT
);

CREATE TABLE IF NOT EXISTS item_group (
    item_group_id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    crf_id INT NOT NULL,
    status_id INT,
    date_created DATE DEFAULT CURRENT_DATE,
    date_updated DATE,
    owner_id INT,
    update_id INT,
    oc_oid VARCHAR(40) NOT NULL
);

CREATE TABLE IF NOT EXISTS item (
    item_id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(4000),
    units VARCHAR(64),
    phi_status BOOLEAN,
    item_data_type_id INT,
    item_reference_type_id INT,
    status_id INT,
    owner_id INT,
    date_created DATE DEFAULT CURRENT_DATE,
    date_updated DATE,
    update_id INT,
    oc_oid VARCHAR(40) NOT NULL
);

CREATE TABLE IF NOT EXISTS response_set (
    response_set_id SERIAL PRIMARY KEY,
    response_type_id INT,
    label VARCHAR(80),
    options_text VARCHAR(4000),
    options_values VARCHAR(4000),
    version_id INT
);

CREATE TABLE IF NOT EXISTS item_form_metadata (
    item_form_metadata_id SERIAL PRIMARY KEY,
    item_id INT NOT NULL,
    crf_version_id INT,
    header VARCHAR(2000),
    subheader VARCHAR(240),
    parent_id INT,
    parent_label VARCHAR(120),
    column_number INT,
    page_number_label VARCHAR(5),
    question_number_label VARCHAR(20),
    left_item_text VARCHAR(4000),
    right_item_text VARCHAR(2000),
    section_id INT NOT NULL,
    decision_condition_id INT,
    response_set_id INT NOT NULL,
    regexp VARCHAR(1000),
    regexp_error_msg VARCHAR(255),
    ordinal INT NOT NULL,
    required BOOLEAN,
    default_value VARCHAR(4000),
    response_layout VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS item_group_metadata (
    item_group_metadata_id SERIAL PRIMARY KEY,
    item_group_id INT NOT NULL,
    header VARCHAR(255),
    subheader VARCHAR(255),
    layout VARCHAR(100),
    repeat_number INT,
    repeat_max INT,
    repeat_array VARCHAR(255),
    row_start_number INT,
    crf_version_id INT NOT NULL,
    item_id INT NOT NULL,
    ordinal INT NOT NULL,
    borders INT
);

CREATE TABLE IF NOT EXISTS item_data (
    item_data_id SERIAL PRIMARY KEY,
    item_id INT NOT NULL,
    event_crf_id INT,
    status_id INT,
    value VARCHAR(4000),
    date_created DATE DEFAULT CURRENT_DATE,
    date_updated DATE,
    owner_id INT,
    update_id INT,
    ordinal INT
);

CREATE TABLE IF NOT EXISTS discrepancy_note (
    discrepancy_note_id SERIAL PRIMARY KEY,
    description VARCHAR(255),
    discrepancy_note_type_id INT,
    resolution_status_id INT,
    detailed_notes VARCHAR(1000),
    date_created DATE DEFAULT CURRENT_DATE,
    owner_id INT,
    parent_dn_id INT,
    entity_type VARCHAR(30),
    study_id INT
);

-- ── Lookup Data ────────────────────────────────────────────

INSERT INTO status (status_id, name, description) VALUES
    (1, 'available', 'this is the active status'),
    (2, 'unavailable', 'this is the inactive status'),
    (3, 'private', NULL),
    (4, 'pending', NULL),
    (5, 'removed', 'this indicates that a record is specifically removed by user'),
    (6, 'locked', NULL),
    (7, 'auto-removed', 'this indicates that a record is removed due to the removal of its parent record'),
    (8, 'signed', 'this indicates all StudyEvents has been signed')
ON CONFLICT (status_id) DO NOTHING;

INSERT INTO user_type (user_type_id, user_type) VALUES
    (1, 'admin'),
    (2, 'user'),
    (3, 'tech-admin')
ON CONFLICT (user_type_id) DO NOTHING;

INSERT INTO user_role (role_id, role_name, parent_id, role_desc) VALUES
    (1, 'admin', 1, NULL),
    (2, 'coordinator', 1, NULL),
    (3, 'director', 1, NULL),
    (4, 'investigator', 1, NULL),
    (5, 'ra', 1, NULL),
    (6, 'monitor', 1, NULL)
ON CONFLICT (role_id) DO NOTHING;

INSERT INTO study_type (study_type_id, name, description) VALUES
    (1, 'genetic', NULL),
    (2, 'non-genetic', NULL)
ON CONFLICT (study_type_id) DO NOTHING;

INSERT INTO response_type (response_type_id, name, description) VALUES
    (1, 'text', 'free form text entry limited to one line'),
    (2, 'textarea', 'free form text area display'),
    (3, 'checkbox', 'selecting one from many options'),
    (4, 'file', 'for upload of files'),
    (5, 'radio', 'selecting one from many options'),
    (6, 'single-select', 'pick one from a list'),
    (7, 'multi-select', 'pick many from a list'),
    (8, 'calculation', 'value calculated automatically'),
    (9, 'group-calculation', 'value calculated automatically from an entire group of items')
ON CONFLICT (response_type_id) DO NOTHING;

INSERT INTO item_data_type (item_data_type_id, code, name, definition, reference) VALUES
    (1, 'BL', 'Boolean', NULL, NULL),
    (2, 'BN', 'BooleanNonNull', NULL, NULL),
    (3, 'ST', 'String', NULL, NULL),
    (4, 'INT', 'Integer', NULL, NULL),
    (5, 'REAL', 'Real', NULL, NULL),
    (6, 'DATE', 'Date', NULL, NULL),
    (7, 'PDATE', 'PartialDate', NULL, NULL)
ON CONFLICT (item_data_type_id) DO NOTHING;

INSERT INTO item_reference_type (item_reference_type_id, name, description) VALUES
    (1, 'literal', NULL)
ON CONFLICT (item_reference_type_id) DO NOTHING;

INSERT INTO subject_event_status (subject_event_status_id, name, description) VALUES
    (1, 'scheduled', ''),
    (2, 'not scheduled', ''),
    (3, 'data entry started', ''),
    (4, 'completed', ''),
    (5, 'stopped', ''),
    (6, 'skipped', ''),
    (7, 'locked', ''),
    (8, 'signed', '')
ON CONFLICT (subject_event_status_id) DO NOTHING;

INSERT INTO completion_status (completion_status_id, name, description, status_id) VALUES
    (1, 'completion status', 'place filler for completion status', 1)
ON CONFLICT (completion_status_id) DO NOTHING;

INSERT INTO study_parameter (study_parameter_id, handle, name, description, default_value, inheritable, overridable) VALUES
    (1, 'collectDob', 'collect subject''s date of birth', 'In study creation, Subject Birthdate can be set to require collect full birthdate, year of birth, or not used', 'required', TRUE, FALSE),
    (2, 'discrepancyManagement', '', '', 'true', TRUE, FALSE),
    (3, 'subjectPersonIdRequired', '', 'In study creation, Person ID can be set to required, optional, or not used', 'required', TRUE, FALSE),
    (4, 'genderRequired', '', 'In study creation, Subject Gender can be set to required or not used', 'required', TRUE, FALSE),
    (5, 'subjectIdGeneration', '', 'In study creation, Study Subject ID can be set to Manual Entry, Auto-generate (editable), Auto-generate (non-editable)', 'manual', TRUE, FALSE),
    (6, 'subjectIdPrefixSuffix', '', 'In study and/or site creation, if Study Subject ID is set to Auto-generate, user can optionally specify a prefix and suffix', 'false', TRUE, FALSE),
    (7, 'interviewerNameRequired', '', 'In study creation, CRF Interviewer Name can be set as optional or required fields', 'required', TRUE, TRUE),
    (8, 'interviewerNameDefault', '', 'In study creation, CRF Interviewer Name can be set to default to blank or to be pre-populated', 'blank', TRUE, TRUE),
    (9, 'interviewerNameEditable', '', 'In study creation, CRF Interviewer Name can be set to editable or not editable', 'editable', TRUE, FALSE),
    (10, 'interviewDateRequired', '', 'In study or site creation, CRF Interviewer Date can be set as optional or required fields', 'required', TRUE, TRUE),
    (11, 'interviewDateDefault', '', 'In study or site creation, CRF Interviewer Date can be set to default to blank or to be pre-populated', 'eventDate', TRUE, TRUE),
    (12, 'interviewDateEditable', '', 'In study creation, CRF Interview Name and Date can be set to editable or not editable', 'editable', TRUE, FALSE),
    (13, 'personIdShownOnCRF', '', '', 'false', TRUE, FALSE)
ON CONFLICT (study_parameter_id) DO NOTHING;

INSERT INTO discrepancy_note_type (discrepancy_note_type_id, name, description) VALUES
    (1, 'Failed Validation Check', ''),
    (2, 'Answered', ''),
    (3, 'Annotation', ''),
    (4, 'Flagged', ''),
    (5, 'NotAnswered', ''),
    (6, 'Note', '')
ON CONFLICT (discrepancy_note_type_id) DO NOTHING;

INSERT INTO resolution_status (resolution_status_id, name, description) VALUES
    (1, 'New', ''),
    (2, 'Updated', ''),
    (3, 'Resolved', ''),
    (4, 'Closed', ''),
    (5, 'Not Applicable', '')
ON CONFLICT (resolution_status_id) DO NOTHING;

INSERT INTO group_class_types (group_class_type_id, name, description) VALUES
    (1, 'Arm', 'Study Arm'),
    (2, 'Site Group', 'Study Site Group')
ON CONFLICT (group_class_type_id) DO NOTHING;

-- ── Test Data: User Accounts ───────────────────────────────

INSERT INTO user_account (user_id, user_name, passwd, first_name, last_name, email, user_type_id, status_id, owner_id, update_id, enabled, account_non_locked, date_created)
VALUES
    (1, 'admin', '{bcrypt}$2a$10$05GbJCqZurBvHvAVkZ9mJO/fAkXPyYxrGlMzYAsgI2XgOga382AMa', 'Admin', 'User', 'admin@researchedc.local', 1, 1, 1, 1, TRUE, TRUE, CURRENT_DATE),
    (2, 'investigator', '{bcrypt}$2a$10$05GbJCqZurBvHvAVkZ9mJO/fAkXPyYxrGlMzYAsgI2XgOga382AMa', 'Investigator', 'User', 'investigator@researchedc.local', 2, 1, 1, 1, TRUE, TRUE, CURRENT_DATE)
ON CONFLICT (user_id) DO NOTHING;

-- ── Test Data: Study ───────────────────────────────────────

INSERT INTO study (study_id, unique_identifier, secondary_identifier, name, summary, date_planned_start, date_planned_end, date_created, owner_id, type_id, status_id, principal_investigator, phase, expected_total_enrollment, sponsor, conditions, oc_oid)
VALUES
    (1, 'DEMO-SLEEP-001', 'DEMO-SLEEP-001', 'Demo Sleep Study', 'A demonstration study for E2E testing of sleep quality assessment', CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', CURRENT_DATE, 1, 2, 1, 'Dr. Smith', 'Phase II', 100, 'ResearchEDC Demo', 'Sleep Disorder', 'S_DEMOSLEEP01')
ON CONFLICT (study_id) DO NOTHING;

-- Study parameter values for study 1
INSERT INTO study_parameter_value (study_id, value, parameter) VALUES
    (1, '1', 'collectDob'),
    (1, 'true', 'discrepancyManagement'),
    (1, 'true', 'genderRequired'),
    (1, 'required', 'subjectPersonIdRequired'),
    (1, 'true', 'interviewerNameRequired'),
    (1, 'blank', 'interviewerNameDefault'),
    (1, 'true', 'interviewerNameEditable'),
    (1, 'false', 'personIdShownOnCRF'),
    (1, 'editable', 'interviewDateEditable'),
    (1, 'eventDate', 'interviewDateDefault'),
    (1, 'required', 'interviewDateRequired'),
    (1, 'false', 'subjectIdPrefixSuffix'),
    (1, 'manual', 'subjectIdGeneration');

-- ── Test Data: User Roles ──────────────────────────────────

INSERT INTO study_user_role (role_name, study_id, status_id, owner_id, user_name, date_created) VALUES
    ('admin', 1, 1, 1, 'admin', CURRENT_DATE),
    ('director', 1, 1, 1, 'admin', CURRENT_DATE),
    ('investigator', 1, 1, 1, 'investigator', CURRENT_DATE);

-- ── Test Data: CRF Definitions ─────────────────────────────

INSERT INTO crf (crf_id, status_id, name, description, owner_id, oc_oid)
VALUES (1, 1, 'Sleep Diary CRF', 'Daily sleep quality assessment form', 1, 'CRF_SLEEP01')
ON CONFLICT (crf_id) DO NOTHING;

INSERT INTO crf_version (crf_version_id, crf_id, name, description, status_id, owner_id, oc_oid)
VALUES (1, 1, 'v1.0', 'Initial version of the Sleep Diary CRF', 1, 1, 'CRF_SLEEP01_V01')
ON CONFLICT (crf_version_id) DO NOTHING;

INSERT INTO section (section_id, crf_version_id, status_id, label, title, ordinal, owner_id)
VALUES
    (1, 1, 1, 'Sleep Schedule', 'Morning Assessment', 1, 1),
    (2, 1, 1, 'Sleep Quality', 'Evening Assessment', 2, 1)
ON CONFLICT (section_id) DO NOTHING;

INSERT INTO response_set (response_set_id, response_type_id, label, options_text, options_values, version_id) VALUES
    (1, 6, 'Sleep Quality Rating', 'Poor|Fair|Good|Excellent', '1|2|3|4', 1),
    (2, 1, 'Hours Slept', NULL, NULL, 1),
    (3, 6, 'Yes/No', 'Yes|No', 'true|false', 1)
ON CONFLICT (response_set_id) DO NOTHING;

INSERT INTO item (item_id, name, description, item_data_type_id, item_reference_type_id, status_id, owner_id, oc_oid) VALUES
    (1, 'sleep_hours', 'Number of hours slept last night', 4, 1, 1, 1, 'I_SLEEP_HOURS'),
    (2, 'sleep_quality', 'Self-reported sleep quality rating', 3, 1, 1, 1, 'I_SLEEP_QUALITY'),
    (3, 'wake_count', 'Number of times woke up during the night', 4, 1, 1, 1, 'I_WAKE_COUNT'),
    (4, 'medication_taken', 'Did you take sleep medication?', 3, 1, 1, 1, 'I_MED_TAKEN'),
    (5, 'dream_recall', 'Do you recall any dreams?', 3, 1, 1, 1, 'I_DREAM_RECALL')
ON CONFLICT (item_id) DO NOTHING;

INSERT INTO item_form_metadata (item_form_metadata_id, item_id, crf_version_id, section_id, response_set_id, ordinal, left_item_text) VALUES
    (1, 1, 1, 1, 2, 1, 'How many hours did you sleep last night?'),
    (2, 3, 1, 1, 2, 2, 'How many times did you wake up?'),
    (3, 5, 1, 1, 3, 3, 'Do you recall any dreams?'),
    (4, 2, 1, 2, 1, 1, 'How would you rate your sleep quality?'),
    (5, 4, 1, 2, 3, 2, 'Did you take sleep medication?')
ON CONFLICT (item_form_metadata_id) DO NOTHING;

INSERT INTO item_group (item_group_id, name, crf_id, status_id, owner_id, oc_oid) VALUES
    (1, 'Morning Assessment', 1, 1, 1, 'IG_MORNING'),
    (2, 'Evening Assessment', 1, 1, 1, 'IG_EVENING')
ON CONFLICT (item_group_id) DO NOTHING;

INSERT INTO item_group_metadata (item_group_metadata_id, item_group_id, crf_version_id, item_id, ordinal) VALUES
    (1, 1, 1, 1, 1),
    (2, 1, 1, 3, 2),
    (3, 1, 1, 5, 3),
    (4, 2, 1, 2, 1),
    (5, 2, 1, 4, 2)
ON CONFLICT (item_group_metadata_id) DO NOTHING;

-- ── Test Data: Event Definitions ───────────────────────────

INSERT INTO study_event_definition (study_event_definition_id, study_id, name, description, repeating, type, owner_id, status_id, ordinal, oc_oid) VALUES
    (1, 1, 'Baseline Visit', 'Initial baseline assessment visit', FALSE, 'scheduled', 1, 1, 1, 'SE_BASELINE'),
    (2, 1, 'Follow-up Visit', '30-day follow-up assessment', TRUE, 'scheduled', 1, 1, 2, 'SE_FOLLOWUP')
ON CONFLICT (study_event_definition_id) DO NOTHING;

INSERT INTO event_definition_crf (event_definition_crf_id, study_event_definition_id, study_id, crf_id, required_crf, default_version_id, status_id, owner_id, ordinal) VALUES
    (1, 1, 1, 1, TRUE, 1, 1, 1, 1),
    (2, 2, 1, 1, TRUE, 1, 1, 1, 1)
ON CONFLICT (event_definition_crf_id) DO NOTHING;

-- ── Test Data: Subjects ────────────────────────────────────

INSERT INTO subject (subject_id, status_id, date_of_birth, gender, unique_identifier, owner_id, dob_collected) VALUES
    (1, 1, '1990-01-15', 'M', 'SUBJ-001', 1, TRUE),
    (2, 1, '1985-06-22', 'F', 'SUBJ-002', 1, TRUE)
ON CONFLICT (subject_id) DO NOTHING;

INSERT INTO study_subject (study_subject_id, label, secondary_label, subject_id, study_id, status_id, enrollment_date, owner_id, oc_oid) VALUES
    (1, 'S-001', NULL, 1, 1, 1, CURRENT_DATE - INTERVAL '7 days', 1, 'SS_S001'),
    (2, 'S-002', NULL, 2, 1, 1, CURRENT_DATE - INTERVAL '5 days', 1, 'SS_S002')
ON CONFLICT (study_subject_id) DO NOTHING;

-- ── Test Data: Study Events ────────────────────────────────

INSERT INTO study_event (study_event_id, study_event_definition_id, study_subject_id, date_start, date_end, owner_id, status_id, subject_event_status_id) VALUES
    (1, 1, 1, CURRENT_DATE - INTERVAL '5 days', CURRENT_DATE - INTERVAL '5 days', 1, 1, 4),
    (2, 1, 2, CURRENT_DATE - INTERVAL '3 days', CURRENT_DATE - INTERVAL '3 days', 1, 1, 4),
    (3, 2, 1, CURRENT_DATE + INTERVAL '25 days', NULL, 1, 1, 1)
ON CONFLICT (study_event_id) DO NOTHING;

-- ── Test Data: Event CRFs ──────────────────────────────────

INSERT INTO event_crf (event_crf_id, study_event_id, crf_version_id, completion_status_id, status_id, owner_id, study_subject_id, date_completed) VALUES
    (1, 1, 1, 1, 1, 1, 1, CURRENT_DATE - INTERVAL '5 days'),
    (2, 2, 1, 1, 1, 1, 2, CURRENT_DATE - INTERVAL '3 days'),
    (3, 3, 1, 1, 1, 1, 1, NULL)
ON CONFLICT (event_crf_id) DO NOTHING;

-- ── Test Data: Item Data ───────────────────────────────────

INSERT INTO item_data (item_data_id, item_id, event_crf_id, status_id, value, owner_id, ordinal) VALUES
    (1, 1, 1, 1, '7', 1, 1),
    (2, 3, 1, 1, '1', 1, 2),
    (3, 5, 1, 1, 'false', 1, 3),
    (4, 2, 1, 1, '3', 1, 1),
    (5, 4, 1, 1, 'false', 1, 2),
    (6, 1, 2, 1, '6', 1, 1),
    (7, 3, 2, 1, '2', 1, 2),
    (8, 5, 2, 1, 'true', 1, 3),
    (9, 2, 2, 1, '2', 1, 1),
    (10, 4, 2, 1, 'true', 1, 2)
ON CONFLICT (item_data_id) DO NOTHING;

-- ── Module Tables (sync data from legacy tables) ───────────

CREATE TABLE IF NOT EXISTS module_study (
    study_id INT NOT NULL,
    parent_study_id INT,
    unique_identifier VARCHAR(30),
    secondary_identifier VARCHAR(255),
    name VARCHAR(255),
    summary VARCHAR(255),
    date_planned_start DATE,
    date_planned_end DATE,
    date_created DATE,
    date_updated DATE,
    owner_id INT,
    update_id INT,
    type_id INT,
    status_id INT,
    principal_investigator VARCHAR(255),
    facility_name VARCHAR(255),
    facility_city VARCHAR(255),
    facility_state VARCHAR(20),
    facility_zip VARCHAR(64),
    facility_country VARCHAR(64),
    facility_recruitment_status VARCHAR(60),
    facility_contact_name VARCHAR(255),
    facility_contact_degree VARCHAR(255),
    facility_contact_phone VARCHAR(255),
    facility_contact_email VARCHAR(255),
    protocol_type VARCHAR(30),
    protocol_description VARCHAR(1000),
    protocol_date_verification DATE,
    phase VARCHAR(30),
    expected_total_enrollment INT,
    sponsor VARCHAR(255),
    collaborators VARCHAR(1000),
    medline_identifier VARCHAR(255),
    url VARCHAR(255),
    url_description VARCHAR(255),
    conditions VARCHAR(500),
    keywords VARCHAR(255),
    eligibility VARCHAR(500),
    gender VARCHAR(30),
    age_max VARCHAR(3),
    age_min VARCHAR(3),
    healthy_volunteer_accepted BOOLEAN,
    purpose VARCHAR(64),
    allocation VARCHAR(64),
    masking VARCHAR(30),
    control VARCHAR(30),
    assignment VARCHAR(30),
    endpoint VARCHAR(64),
    interventions VARCHAR(1000),
    duration VARCHAR(30),
    selection VARCHAR(30),
    timing VARCHAR(30),
    official_title VARCHAR(255),
    results_reference BOOLEAN,
    oc_oid VARCHAR(40),
    old_status_id INT,
    feature_flags VARCHAR(255),
    version INT
);

INSERT INTO module_study SELECT *, NULL AS old_status_id, NULL AS feature_flags, NULL AS version FROM study ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS module_crf (
    crf_id INT NOT NULL,
    name VARCHAR(255),
    description VARCHAR(4000),
    oc_oid VARCHAR(40),
    status_id INT,
    owner_id INT,
    source_study_id INT,
    date_created DATE,
    date_updated DATE,
    update_id INT,
    version INT
);
INSERT INTO module_crf (crf_id, name, description, oc_oid, status_id, owner_id, source_study_id, date_created)
SELECT crf_id, name, description, oc_oid, status_id, owner_id, 1, date_created FROM crf ON CONFLICT DO NOTHING;

-- ── Update Sequence Values ─────────────────────────────────

SELECT setval('user_account_user_id_seq', COALESCE((SELECT MAX(user_id) FROM user_account), 100));
SELECT setval('study_study_id_seq', COALESCE((SELECT MAX(study_id) FROM study), 100));
SELECT setval('study_event_definition_id_seq', COALESCE((SELECT MAX(study_event_definition_id) FROM study_event_definition), 100));
SELECT setval('study_event_id_seq', COALESCE((SELECT MAX(study_event_id) FROM study_event), 100));
SELECT setval('event_crf_id_seq', COALESCE((SELECT MAX(event_crf_id) FROM event_crf), 100));
SELECT setval('crf_crf_id_seq', COALESCE((SELECT MAX(crf_id) FROM crf), 100));
SELECT setval('crf_version_id_seq', COALESCE((SELECT MAX(crf_version_id) FROM crf_version), 100));
SELECT setval('section_id_seq', COALESCE((SELECT MAX(section_id) FROM section), 100));
SELECT setval('item_id_seq', COALESCE((SELECT MAX(item_id) FROM item), 100));
SELECT setval('item_data_id_seq', COALESCE((SELECT MAX(item_data_id) FROM item_data), 100));
SELECT setval('response_set_id_seq', COALESCE((SELECT MAX(response_set_id) FROM response_set), 100));
SELECT setval('item_group_id_seq', COALESCE((SELECT MAX(item_group_id) FROM item_group), 100));
SELECT setval('item_form_metadata_id_seq', COALESCE((SELECT MAX(item_form_metadata_id) FROM item_form_metadata), 100));
SELECT setval('study_subject_id_seq', COALESCE((SELECT MAX(study_subject_id) FROM study_subject), 100));
SELECT setval('subject_id_seq', COALESCE((SELECT MAX(subject_id) FROM subject), 100));
SELECT setval('module_user_account_id_seq', COALESCE((SELECT MAX(user_id) FROM user_account), 100));
