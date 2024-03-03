CREATE TABLE users (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(50)  NOT NULL DEFAULT 'ADMIN',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE endpoints (
    id                      UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name                    VARCHAR(255) NOT NULL,
    provider                VARCHAR(50)  NOT NULL,
    base_url                VARCHAR(500) NOT NULL,
    model                   VARCHAR(255) NOT NULL,
    api_key_encrypted       TEXT,
    enabled                 BOOLEAN      NOT NULL DEFAULT TRUE,
    probe_interval_seconds  INT          NOT NULL DEFAULT 300,
    consecutive_failures    INT          NOT NULL DEFAULT 0,
    next_probe_at           TIMESTAMPTZ,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE probe_runs (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    trigger      VARCHAR(50) NOT NULL,
    started_at   TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ
);

CREATE TABLE probe_results (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    probe_run_id      UUID        NOT NULL REFERENCES probe_runs(id),
    endpoint_id       UUID        NOT NULL REFERENCES endpoints(id),
    success           BOOLEAN     NOT NULL,
    latency_ms        INT,
    http_status       INT,
    prompt_tokens     INT,
    completion_tokens INT,
    error_message     TEXT,
    response_snippet  TEXT,
    probed_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_probe_results_endpoint_probed ON probe_results (endpoint_id, probed_at DESC);
CREATE INDEX idx_endpoints_next_probe ON endpoints (next_probe_at) WHERE enabled = TRUE;
