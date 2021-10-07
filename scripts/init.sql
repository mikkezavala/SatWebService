BEGIN;
DROP TABLE IF EXISTS sat_client;
COMMIT;


BEGIN;
CREATE TABLE sat_client
(
    id             int          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    rfc            VARCHAR(254) NOT NULL,
    password_plain VARCHAR(254) NOT NULL,
    keystore       VARCHAR(200) NOT NULL
);
COMMIT;
