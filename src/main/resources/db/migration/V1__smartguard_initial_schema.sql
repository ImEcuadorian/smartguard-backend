create table if not exists user_accounts (
    id uuid primary key,
    username varchar(80) not null unique,
    password_hash varchar(120) not null,
    display_name varchar(120) not null,
    role varchar(30) not null,
    status varchar(30) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint
);

create table if not exists refresh_tokens (
    id uuid primary key,
    user_id uuid not null references user_accounts(id),
    token_hash varchar(64) not null unique,
    expires_at timestamptz not null,
    revoked_at timestamptz,
    created_at timestamptz not null
);

create table if not exists devices (
    id uuid primary key,
    code varchar(80) not null unique,
    name varchar(120) not null,
    location varchar(160),
    status varchar(30) not null,
    ip_address varchar(45),
    firmware_version varchar(40),
    api_key_hash varchar(120),
    last_seen_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint
);

create table if not exists sensors (
    id uuid primary key,
    device_id uuid not null references devices(id),
    code varchar(80) not null unique,
    name varchar(120) not null,
    type varchar(40) not null,
    unit varchar(40),
    location varchar(160),
    status varchar(30) not null,
    last_reading_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint
);

create table if not exists sensor_readings (
    id uuid primary key,
    sensor_id uuid not null references sensors(id),
    device_id uuid not null references devices(id),
    numeric_value numeric(12, 4),
    boolean_value boolean,
    text_value varchar(255),
    recorded_at timestamptz not null,
    created_at timestamptz not null
);

create table if not exists sensor_alert_rules (
    id uuid primary key,
    sensor_id uuid not null references sensors(id),
    type varchar(40) not null,
    operator varchar(40),
    threshold_value numeric(12, 4),
    expected_boolean_value boolean,
    duration_minutes integer,
    alert_type varchar(40) not null,
    severity varchar(30) not null,
    message varchar(255) not null,
    enabled boolean not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint
);

create table if not exists alerts (
    id uuid primary key,
    device_id uuid references devices(id),
    sensor_id uuid references sensors(id),
    type varchar(40) not null,
    severity varchar(30) not null,
    status varchar(30) not null,
    message varchar(255) not null,
    occurred_at timestamptz not null,
    created_at timestamptz not null,
    acknowledged_at timestamptz,
    resolved_at timestamptz
);

create table if not exists access_readers (
    id uuid primary key,
    device_id uuid not null references devices(id),
    code varchar(80) not null unique,
    type varchar(40) not null,
    location varchar(160),
    status varchar(30) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint
);

create table if not exists rfid_cards (
    id uuid primary key,
    uid varchar(80) not null unique,
    owner_name varchar(120) not null,
    status varchar(30) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint
);

create table if not exists access_events (
    id uuid primary key,
    device_id uuid not null references devices(id),
    reader_id uuid not null references access_readers(id),
    card_id uuid references rfid_cards(id),
    card_uid varchar(80) not null,
    result varchar(30) not null,
    reason varchar(180) not null,
    occurred_at timestamptz not null,
    created_at timestamptz not null
);

create table if not exists actuators (
    id uuid primary key,
    device_id uuid not null references devices(id),
    code varchar(80) not null unique,
    name varchar(120) not null,
    type varchar(40) not null,
    location varchar(160),
    status varchar(30) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint
);

create table if not exists actuator_commands (
    id uuid primary key,
    actuator_id uuid not null references actuators(id),
    device_id uuid not null references devices(id),
    command varchar(40) not null,
    status varchar(30) not null,
    payload varchar(255),
    created_at timestamptz not null,
    sent_at timestamptz
);

create index if not exists idx_refresh_tokens_token_hash on refresh_tokens(token_hash);
create index if not exists idx_refresh_tokens_user_id on refresh_tokens(user_id);
create index if not exists idx_refresh_tokens_expires_at on refresh_tokens(expires_at);
create index if not exists idx_devices_status on devices(status);
create index if not exists idx_devices_code on devices(code);
create index if not exists idx_sensors_device_id on sensors(device_id);
create index if not exists idx_sensors_type on sensors(type);
create index if not exists idx_sensors_status on sensors(status);
create index if not exists idx_sensors_last_reading_at on sensors(last_reading_at);
create index if not exists idx_sensor_readings_sensor_id on sensor_readings(sensor_id);
create index if not exists idx_sensor_readings_device_id on sensor_readings(device_id);
create index if not exists idx_sensor_readings_recorded_at on sensor_readings(recorded_at);
create index if not exists idx_sensor_alert_rules_sensor_id on sensor_alert_rules(sensor_id);
create index if not exists idx_sensor_alert_rules_type on sensor_alert_rules(type);
create index if not exists idx_sensor_alert_rules_enabled on sensor_alert_rules(enabled);
create index if not exists idx_alerts_device_id on alerts(device_id);
create index if not exists idx_alerts_sensor_id on alerts(sensor_id);
create index if not exists idx_alerts_status on alerts(status);
create index if not exists idx_alerts_occurred_at on alerts(occurred_at);
create index if not exists idx_access_readers_device_id on access_readers(device_id);
create index if not exists idx_access_events_device_id on access_events(device_id);
create index if not exists idx_access_events_reader_id on access_events(reader_id);
create index if not exists idx_access_events_occurred_at on access_events(occurred_at);
create index if not exists idx_access_events_result on access_events(result);
create index if not exists idx_actuators_device_id on actuators(device_id);
create index if not exists idx_actuators_type on actuators(type);
create index if not exists idx_actuator_commands_actuator_id on actuator_commands(actuator_id);
create index if not exists idx_actuator_commands_device_id on actuator_commands(device_id);
create index if not exists idx_actuator_commands_status on actuator_commands(status);
