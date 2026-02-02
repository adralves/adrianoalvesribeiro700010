CREATE TABLE regionais (
    id bigserial PRIMARY KEY,
    regional_id INTEGER NOT NULL,
    nome VARCHAR(200) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT true,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uq_regionais_regional_id_ativo
ON regionais (regional_id)
WHERE ativo = true;