-- demo customer
INSERT INTO customer(id, document_id, name)
VALUES('5ce15c7e-3589-444a-a260-1c3a5a970c96', 12345, 'First Customer');

-- savings account
INSERT INTO account(id, balance, branch_number, iban, locked, number, type, customer_id)
VALUES('5f34a652-9207-4af2-8784-c3f863e1925c', 0, 123, 'DE70977433509451310526', FALSE, 1234501, 'SAVINGS', '5ce15c7e-3589-444a-a260-1c3a5a970c96');
INSERT INTO account_capabilities(account_id, allowed_transactions) VALUES('5f34a652-9207-4af2-8784-c3f863e1925c', 'DEPOSIT');
INSERT INTO account_capabilities(account_id, allowed_transactions) VALUES('5f34a652-9207-4af2-8784-c3f863e1925c', 'WITHDRAWN');
INSERT INTO account_capabilities(account_id, allowed_transactions) VALUES('5f34a652-9207-4af2-8784-c3f863e1925c', 'TRANSFER_IN');
INSERT INTO account_capabilities(account_id, allowed_transactions) VALUES('5f34a652-9207-4af2-8784-c3f863e1925c', 'TRANSFER_OUT');

-- checking account
INSERT INTO account(id, balance, branch_number, iban, locked, number, type, customer_id, saving_account_id)
VALUES('c45f49b9-46f5-481c-96a8-85576a68d627', 0, 123, 'DE06286592751368036575', FALSE, 1234502, 'CHECKING', '5ce15c7e-3589-444a-a260-1c3a5a970c96', '5f34a652-9207-4af2-8784-c3f863e1925c');
INSERT INTO account_capabilities(account_id, allowed_transactions) VALUES('c45f49b9-46f5-481c-96a8-85576a68d627', 'DEPOSIT');
INSERT INTO account_capabilities(account_id, allowed_transactions) VALUES('c45f49b9-46f5-481c-96a8-85576a68d627', 'WITHDRAWN');
INSERT INTO account_capabilities(account_id, allowed_transactions) VALUES('c45f49b9-46f5-481c-96a8-85576a68d627', 'TRANSFER_IN');
INSERT INTO account_capabilities(account_id, allowed_transactions) VALUES('c45f49b9-46f5-481c-96a8-85576a68d627', 'TRANSFER_OUT');

-- private loan account
INSERT INTO account(id, balance, branch_number, iban, locked, number, type, customer_id)
VALUES('3216f4b6-2e18-4801-9c5d-b5cc228780b2', 0, 123, 'DE87735439886306781387', FALSE, 1234503, 'PRIVATE_LOAN', '5ce15c7e-3589-444a-a260-1c3a5a970c96');
INSERT INTO account_capabilities(account_id, allowed_transactions) VALUES('3216f4b6-2e18-4801-9c5d-b5cc228780b2', 'DEPOSIT');
INSERT INTO account_capabilities(account_id, allowed_transactions) VALUES('3216f4b6-2e18-4801-9c5d-b5cc228780b2', 'TRANSFER_IN');
