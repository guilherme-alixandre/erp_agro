-- Forma de pagamento (à vista/parcelado) nas notas de entrada e saída, para alimentar o
-- resumo de contas a pagar/receber por parceiro na aba Rentabilidade. Vencimento único cobre
-- o caso à vista; quando parcelado, as parcelas seguintes vencem +1 mês cada (calculadas em
-- tempo de consulta, não persistidas individualmente).

ALTER TABLE documento_entrada ADD COLUMN IF NOT EXISTS forma_pagamento VARCHAR(15);
ALTER TABLE documento_entrada ADD COLUMN IF NOT EXISTS data_vencimento DATE;
ALTER TABLE documento_entrada ADD COLUMN IF NOT EXISTS numero_parcelas INTEGER;
ALTER TABLE documento_entrada ADD COLUMN IF NOT EXISTS valor_parcela NUMERIC(15,2);

ALTER TABLE documento_saida ADD COLUMN IF NOT EXISTS forma_pagamento VARCHAR(15);
ALTER TABLE documento_saida ADD COLUMN IF NOT EXISTS data_vencimento DATE;
ALTER TABLE documento_saida ADD COLUMN IF NOT EXISTS numero_parcelas INTEGER;
ALTER TABLE documento_saida ADD COLUMN IF NOT EXISTS valor_parcela NUMERIC(15,2);

UPDATE documento_entrada SET forma_pagamento = 'A_VISTA', numero_parcelas = 1, data_vencimento = data_emissao WHERE forma_pagamento IS NULL;
UPDATE documento_saida SET forma_pagamento = 'A_VISTA', numero_parcelas = 1, data_vencimento = data_emissao WHERE forma_pagamento IS NULL;
