CREATE TABLE activation_requests (
    id UUID PRIMARY KEY,
    external_id VARCHAR(100) NOT NULL UNIQUE,
    customer_id VARCHAR(100) NOT NULL,
    product_code VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_activation_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'))
);
CREATE INDEX idx_activation_requests_customer_status ON activation_requests(customer_id, status);
