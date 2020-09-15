CREATE TABLE customer (
    id uuid PRIMARY KEY,
    name character varying(255) NOT NULL,
    document_id integer NOT NULL
);

CREATE TABLE account (
    id uuid PRIMARY KEY,
    iban character varying(255) UNIQUE,
    branch_number smallint NOT NULL,
    number integer NOT NULL,
    type character varying(255) NOT NULL,
    balance numeric(19,2),
    customer_id uuid REFERENCES customer(id),
    saving_account_id uuid UNIQUE REFERENCES account(id),
    locked boolean NOT NULL
);
CREATE UNIQUE INDEX uk_cuc1pxk2ofct9xra2nm0oon ON account(iban);
CREATE UNIQUE INDEX uk_i10940jhraso8slduhpo5tbtu ON account(saving_account_id);

CREATE TABLE account_capabilities (
    account_id uuid NOT NULL REFERENCES account(id),
    allowed_transactions character varying(255) NOT NULL
);

CREATE TABLE account_transaction (
    id uuid PRIMARY KEY,
    account_id uuid REFERENCES account(id),
    type character varying(25) NOT NULL,
    amount numeric(19,2) NOT NULL,
    payee character varying(255),
    created_at timestamp without time zone NOT NULL
);
