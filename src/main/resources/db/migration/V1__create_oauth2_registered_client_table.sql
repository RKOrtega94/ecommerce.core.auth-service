-- Create oauth2_registered_client table
CREATE TABLE PUBLIC.OAUTH2_REGISTERED_CLIENT (
    ID UUID NOT NULL DEFAULT UUID_GENERATE_V4 (),
    CLIENT_ID CHARACTER VARYING(100) NOT NULL,
    CLIENT_ID_ISSUED_AT TIMESTAMP WITH TIME ZONE NOT NULL,
    CLIENT_SECRET CHARACTER VARYING(255),
    CLIENT_SECRET_EXPIRES_AT TIMESTAMP WITH TIME ZONE,
    CLIENT_NAME CHARACTER VARYING(200) NOT NULL,
    CLIENT_AUTHENTICATION_METHODS TEXT NOT NULL,
    AUTHORIZATION_GRANT_TYPES TEXT NOT NULL,
    REDIRECT_URIS TEXT,
    POST_LOGOUT_REDIRECT_URIS TEXT,
    SCOPES TEXT,
    CLIENT_SETTINGS TEXT NOT NULL,
    TOKEN_SETTINGS TEXT NOT NULL,
    CREATED_AT TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (ID),
    UNIQUE (CLIENT_ID),
    UNIQUE (CLIENT_NAME),
    CONSTRAINT CHK_CLIENT_AUTH_METHODS_VALUES CHECK (
        CLIENT_AUTHENTICATION_METHODS ~ '^(client_secret_basic|client_secret_post|private_key_jwt|client_secret_jwt|none)(,(client_secret_basic|client_secret_post|private_key_jwt|client_secret_jwt|none))*$'
    ),
    CONSTRAINT CHK_AUTHORIZATION_GRANT_TYPES_VALUES CHECK (
        AUTHORIZATION_GRANT_TYPES ~ '^(authorization_code|client_credentials|refresh_token|urn:ietf:params:oauth:grant-type:device_code|urn:ietf:params:oauth:grant-type:token-exchange)(,(authorization_code|client_credentials|refresh_token|urn:ietf:params:oauth:grant-type:device_code|urn:ietf:params:oauth:grant-type:token-exchange))*$'
    )
);