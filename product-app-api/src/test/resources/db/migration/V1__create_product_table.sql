CREATE TABLE product (
                         id BIGSERIAL PRIMARY KEY,
                         code VARCHAR(10) NOT NULL UNIQUE,
                         name VARCHAR(50) NOT NULL,
                         price_eur NUMERIC(10, 2) NOT NULL,
                         is_available BOOLEAN NOT NULL,
                         created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                         deleted_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE INDEX idx_product_code ON product (code);
