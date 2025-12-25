CREATE TABLE user_credentials
(
    username VARCHAR(255) NOT NULL PRIMARY KEY,
    password VARBINARY(255) NOT NULL,
    CONSTRAINT username_not_empty CHECK (octet_length(username) > 0),
    CONSTRAINT password_not_empty CHECK (octet_length(password) > 0)
);
