BEGIN;
DROP TABLE IF EXISTS sat_client;
COMMIT;


BEGIN;
CREATE TABLE sat_client
(
    id             INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    rfc            VARCHAR(100) NOT NULL,
    password_plain VARCHAR(254) NOT NULL,
    keystore       VARCHAR(200) NOT NULL,
    INDEX idx_client_rfc (rfc)
);
COMMIT;

BEGIN;
CREATE TABLE sat_token
(
    id         INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    rfc        VARCHAR(100) NOT NULL,
    token      LONGTEXT     NOT NULL,
    expiration TIMESTAMP    NOT NULL,
    created    TIMESTAMP    NOT NULL,
    INDEX idx_token_rfc (rfc)
);
COMMIT;

BEGIN;
CREATE TABLE sat_packet
(
    id              INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    rfc             VARCHAR(100) NOT NULL,
    status          VARCHAR(100) NOT NULL,
    state           VARCHAR(100) NOT NULL,
    message         LONGTEXT     NOT NULL,
    request_id      VARCHAR(254) NULL,
    date_end        TIMESTAMP    NULL,
    date_start      TIMESTAMP    NULL,
    times_requested INT       DEFAULT 0,
    last_requested  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    packet_id       VARCHAR(254) NULL,
    path            VARCHAR(255) NULL,
    INDEX idx_rfc_packet (rfc, request_id)
);
COMMIT;
