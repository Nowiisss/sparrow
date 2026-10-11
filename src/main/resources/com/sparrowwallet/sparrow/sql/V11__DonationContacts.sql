CREATE TABLE donationContact (
    id VARCHAR(36) PRIMARY KEY NOT NULL,
    name VARCHAR(255) NOT NULL,
    silentPaymentAddress VARCHAR(255) NOT NULL UNIQUE
);
