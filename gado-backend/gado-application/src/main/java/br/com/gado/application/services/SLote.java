package br.com.gado.application.services;

import br.com.gado.application.dto.loteDto.LoteCadastroDto;
import br.com.gado.application.dto.loteDto.LotePutDto;
import br.com.gado.application.dto.loteDto.LoteRespostaDto;
import br.com.gado.application.dto.loteDto.LoteSetorCadastroDto;
import br.com.gado.application.dto.loteDto.LoteSetorRespostaDto;
import br.com.gado.domain.entities.EAnimal;
import br.com.gado.domain.entities.ELote;
import br.com.gado.domain.entities.ELoteSetor;
import br.com.gado.domain.entities.ESetor;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.domain.enums.EnPerfilUsuario;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.domain.enums.EnStatusAnimal;
import br.com.gado.infrastructure.persistence.repositories.IAnimal;
import br.com.gado.infrastructure.persistence.repositories.ILote;
import br.com.gado.infrastructure.persistence.repositories.ILoteSetor;
import br.com.gado.infrastructure.persistence.repositories.IMetaSetor;
import br.com.gado.infrastructure.persistence.repositories.ISetor;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SLote {

    // ── Prefixo e largura do código de negócio ────────────────────────────
    private static final String PREFIXO_CODIGO = "LOT";
    private static final int LARGURA_NUMERO   = 3; // LOT001 … LOT999

    @Autowired private ILote       loteInterface;
    @Autowired private ILoteSetor  loteSetorInterface;
    @Autowired private ISetor      setorInterface;
    @Autowired private IAnimal     animalInterface;
    @Autowired private IUsuario    usuarioInterface;
    @Autowired private IMetaSetor  metaSetorInterface;

    // ── Controle de acesso ────────────────────────────────────────────────

    /**
     * Valida que o usuário (por e-mail) tem perfil ADMINISTRADOR, GERENTE ou CUIDADOR.
     * Segue o mesmo padrão de {@code SUsuario.validaAdmin}.
     */
    public void validaPermissao(String emailUsuario) {
        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new IllegalArgumentException(
                    "Informe o e-mail do usuário responsável pela operação.");
        }
        EUsuario usuario = usuarioInterface.findByEmailAndStatus(emailUsuario.trim(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (usuario.getPerfil() != EnPerfilUsuario.ADMINISTRADOR
                && usuario.getPerfil() != EnPerfilUsuario.GERENTE
                && usuario.getPerfil() != EnPerfilUsuario.CUIDADOR) {
            throw new IllegalArgumentException(
                    "Apenas Administradores, Gerentes e Cuidadores podem gerenciar lotes.");
        }
    }

    // ── Leitura ───────────────────────────────────────────────────────────

    public LoteRespostaDto buscaPorid(Long id) {
        ELote lote = loteInterface.findByIdAndStatus(id, EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Nenhum lote ativo encontrado para o ID: " + id));
        return toRespostaDto(lote);
    }

    public List<LoteRespostaDto> listarTodos() {
        return loteInterface.findAllByStatus(EnStatus.A)
                .stream()
                .map(this::toRespostaDto)
                .toList();
    }

    // ── Cadastro ──────────────────────────────────────────────────────────

    @Transactional
    public String cadastra(String emailUsuario, LoteCadastroDto dto) {
        validaPermissao(emailUsuario);

        EUsuario usuario = usuarioInterface.findByEmailAndStatus(emailUsuario.trim(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        ELote lote = new ELote();
        lote.setCodigo(gerarProximoCodigo());
        lote.setCorBrinco(dto.getCorBrinco());
        lote.setDescricao(dto.getDescricao());
        lote.setRacaPredominante(dto.getRacaPredominante());
        lote.setDataCriacao(
                dto.getDataCriacao() != null ? dto.getDataCriacao() : LocalDate.now());
        lote.setCriadoPor(usuario);

        ELote loteSalvo = loteInterface.save(lote);

        aplicarAlocacoes(loteSalvo, dto.getAlocacoes(), Set.of());

        log.info("Lote {} criado por {}", loteSalvo.getCodigo(), emailUsuario);
        return "Lote " + loteSalvo.getCodigo() + " cadastrado com sucesso.";
    }

    // ── Edição ────────────────────────────────────────────────────────────

    @Transactional
    public String altera(Long id, String emailUsuario, LotePutDto dto) {
        validaPermissao(emailUsuario);

        ELote lote = loteInterface.findByIdAndStatus(id, EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Nenhum lote ativo encontrado para o ID: " + id));

        EUsuario usuario = usuarioInterface.findByEmailAndStatus(emailUsuario.trim(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (dto.getCorBrinco() != null && !dto.getCorBrinco().isBlank()) {
            lote.setCorBrinco(dto.getCorBrinco());
        }
        if (dto.getDescricao() != null) {
            lote.setDescricao(dto.getDescricao());
        }
        if (dto.getRacaPredominante() != null) {
            lote.setRacaPredominante(dto.getRacaPredominante());
        }
        lote.setAlteradoPor(usuario);

        // Substituição completa das alocações se fornecida
        if (dto.getAlocacoes() != null && !dto.getAlocacoes().isEmpty()) {
            List<ELoteSetor> alocacoesAntigas = loteSetorInterface.findByLote_Id(lote.getId());

            // IDs dos animais atualmente no lote (antes da substituição)
            Set<Long> animaisJaNesteLote = alocacoesAntigas.stream()
                    .flatMap(ls -> ls.getAnimais().stream())
                    .map(EAnimal::getId)
                    .collect(Collectors.toSet());

            // Verifica que nenhum animal congelado está sendo removido
            Set<Long> animaisNoDto = dto.getAlocacoes().stream()
                    .filter(aloc -> aloc.getAnimaisIds() != null)
                    .flatMap(aloc -> aloc.getAnimaisIds().stream())
                    .collect(Collectors.toSet());

            for (Long animalId : animaisJaNesteLote) {
                if (!animaisNoDto.contains(animalId)) {
                    animalInterface.findById(animalId).ifPresent(animal -> {
                        if (isStatusBloqueado(animal.getStatusAnimal())) {
                            throw new IllegalArgumentException(
                                    "O animal " + animal.getCodigoBrinco()
                                            + " (status " + animal.getStatusAnimal().name() + ")"
                                            + " está congelado neste lote e não pode ser removido."
                                            + " Restaure o status para ATIVO ou OBSERVACAO antes"
                                            + " de realizar esta operação.");
                        }
                    });
                }
            }

            loteSetorInterface.deleteAll(alocacoesAntigas);
            lote.getAlocacoes().clear();

            aplicarAlocacoes(lote, dto.getAlocacoes(), animaisJaNesteLote);
        }

        loteInterface.save(lote);
        log.info("Lote {} alterado por {}", lote.getCodigo(), emailUsuario);
        return "Lote " + lote.getCodigo() + " atualizado com sucesso.";
    }

    // ── Exclusão (smart delete) ───────────────────────────────────────────

    @Transactional
    public String deleta(Long id, String emailUsuario) {
        validaPermissao(emailUsuario);

        ELote lote = loteInterface.findByIdAndStatus(id, EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Nenhum lote ativo encontrado para o ID: " + id));

        boolean temMeta          = temMetaVinculada(lote.getId());
        boolean temMovimentacao  = temMovimentacaoFinanceira(lote.getId()); // placeholder

        if (temMeta || temMovimentacao) {
            // Soft delete: apenas inativa
            lote.setStatus(EnStatus.I);
            loteInterface.save(lote);
            log.info("Lote {} inativado (vínculos presentes) por {}", lote.getCodigo(), emailUsuario);
            return "Lote " + lote.getCodigo()
                    + " inativado pois possui vínculos com "
                    + (temMeta ? "metas" : "movimentações financeiras") + ".";
        }

        // Hard delete: remove alocações e o lote
        loteSetorInterface.deleteAll(loteSetorInterface.findByLote_Id(lote.getId()));
        loteInterface.delete(lote);
        log.info("Lote {} excluído definitivamente por {}", lote.getCodigo(), emailUsuario);
        return "Lote " + lote.getCodigo() + " excluído com sucesso.";
    }

    // ── Geração de código ─────────────────────────────────────────────────

    /**
     * Gera o próximo código sequencial no formato LOTnnn.
     *
     * <p>Busca o último código salvo (ex: "LOT042") via query,
     * extrai o número inteiro (42), incrementa em 1 (43) e
     * formata com zeros à esquerda até {@code LARGURA_NUMERO} dígitos (043),
     * resultando em "LOT043".</p>
     *
     * <p>Se não existir nenhum lote ainda, começa em "LOT001".</p>
     */
    String gerarProximoCodigo() {
        String ultimoCodigo = loteInterface.findUltimoCodigoGerado()
                .orElse(PREFIXO_CODIGO + "000");

        // Extrai a parte numérica, que começa após o prefixo
        String parteNumerica = ultimoCodigo.substring(PREFIXO_CODIGO.length());
        int proximoNumero;
        try {
            proximoNumero = Integer.parseInt(parteNumerica) + 1;
        } catch (NumberFormatException e) {
            // Tolerância: se o formato estiver corrompido, começa do 1
            proximoNumero = 1;
        }

        String formatStr = "%0" + LARGURA_NUMERO + "d"; // "%03d"
        return PREFIXO_CODIGO + String.format(formatStr, proximoNumero);
    }

    // ── Lógica de alocação ────────────────────────────────────────────────

    private void aplicarAlocacoes(ELote lote, List<LoteSetorCadastroDto> alocacoesDto,
                                   Set<Long> animaisJaNesteLote) {
        for (LoteSetorCadastroDto alocDto : alocacoesDto) {
            ESetor setor = setorInterface.findByIdAndStatus(alocDto.getSetorId(), EnStatus.A)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Setor não encontrado ou inativo: ID " + alocDto.getSetorId()));

            ELoteSetor loteSetor = new ELoteSetor();
            loteSetor.setLote(lote);
            loteSetor.setSetor(setor);

            if (alocDto.getAnimaisIds() != null && !alocDto.getAnimaisIds().isEmpty()) {
                List<EAnimal> animais = animalInterface.findAllById(alocDto.getAnimaisIds());

                // Validação: todos os IDs informados devem existir
                if (animais.size() != alocDto.getAnimaisIds().size()) {
                    throw new IllegalArgumentException(
                            "Um ou mais animais informados para o setor "
                                    + setor.getNome() + " não foram encontrados.");
                }

                for (EAnimal animal : animais) {
                    // Validação: animal não pode estar alocado em outro lote ativo
                    List<ELoteSetor> conflitos =
                            loteSetorInterface.findByAnimais_IdAndLote_IdNot(animal.getId(), lote.getId());
                    if (!conflitos.isEmpty()) {
                        String loteConflito = conflitos.get(0).getLote().getCodigo();
                        throw new IllegalArgumentException(
                                "O animal " + animal.getCodigoBrinco()
                                        + " já está alocado ao lote " + loteConflito
                                        + ". Desvincule-o antes de adicioná-lo a outro lote.");
                    }

                    // Validação: animal com status bloqueado só pode permanecer — não ser adicionado
                    if (!animaisJaNesteLote.contains(animal.getId())
                            && isStatusBloqueado(animal.getStatusAnimal())) {
                        throw new IllegalArgumentException(
                                "O animal " + animal.getCodigoBrinco()
                                        + " possui status " + animal.getStatusAnimal().name()
                                        + " e não pode ser movimentado entre lotes."
                                        + " Restaure o status para ATIVO ou OBSERVACAO antes"
                                        + " de realizar esta operação.");
                    }
                }

                // Validação de capacidade do setor
                int jaAlocados = loteSetorInterface.findBySetor_Id(setor.getId())
                        .stream()
                        .mapToInt(ls -> ls.getAnimais().size())
                        .sum();
                int totalDepois = jaAlocados + animais.size();

                if (setor.getCapacidadeMaxima() > 0 && totalDepois > setor.getCapacidadeMaxima()) {
                    throw new IllegalArgumentException(
                            "O setor '" + setor.getNome() + "' excede a capacidade máxima ("
                                    + setor.getCapacidadeMaxima() + "). "
                                    + "Já há " + jaAlocados + " animais; tentativa de adicionar "
                                    + animais.size() + ".");
                }

                loteSetor.setAnimais(animais);
            }

            loteSetorInterface.save(loteSetor);
        }
    }

    private boolean isStatusBloqueado(EnStatusAnimal status) {
        return status == EnStatusAnimal.VENDIDO
                || status == EnStatusAnimal.OBITO
                || status == EnStatusAnimal.ABATIDO;
    }

    // ── Verificações de vínculo ───────────────────────────────────────────

    private boolean temMetaVinculada(Long loteId) {
        // Um lote participa de metas via ELoteSetor; verifica se algum setor
        // associado a este lote possui metas ativas.
        return loteSetorInterface.findByLote_Id(loteId)
                .stream()
                .anyMatch(ls -> !metaSetorInterface
                        .findBySetor_Id(ls.getSetor().getId())
                        .isEmpty());
    }

    /**
     * Placeholder para verificação de vínculo com Movimentações Financeiras.
     *
     * <p>Quando o módulo financeiro for implementado, substitua este método
     * pela consulta real no repositório de movimentações.</p>
     *
     * @return {@code false} por enquanto (nenhum vínculo financeiro existe ainda)
     */
    boolean temMovimentacaoFinanceira(Long loteId) {
        // TODO: injetar IMovimentacaoFinanceira e verificar existsByLote_Id(loteId)
        log.debug("Verificação de movimentações financeiras para lote {} — placeholder ativo", loteId);
        return false;
    }

    // ── Mapeamento para resposta ──────────────────────────────────────────

    private LoteRespostaDto toRespostaDto(ELote lote) {
        LoteRespostaDto dto = new LoteRespostaDto();
        dto.setId(lote.getId());
        dto.setStatus(lote.getStatus());
        dto.setCreatedAt(lote.getCreatedAt());
        dto.setUpdatedAt(lote.getUpdatedAt());
        dto.setCodigo(lote.getCodigo());
        dto.setDescricao(lote.getDescricao());
        dto.setRacaPredominante(lote.getRacaPredominante());
        dto.setCorBrinco(lote.getCorBrinco());
        dto.setDataCriacao(lote.getDataCriacao());
        dto.setStatusLote(lote.getStatus());

        if (lote.getCriadoPor() != null) {
            dto.setCriadoPorNome(lote.getCriadoPor().getNome());
            dto.setCriadoPorEmail(lote.getCriadoPor().getEmail());
        }
        if (lote.getAlteradoPor() != null) {
            dto.setAlteradoPorNome(lote.getAlteradoPor().getNome());
            dto.setAlteradoPorEmail(lote.getAlteradoPor().getEmail());
        }

        List<ELoteSetor> alocacoes = loteSetorInterface.findByLote_Id(lote.getId());

        List<LoteSetorRespostaDto> alocacoesDto = alocacoes.stream().map(ls -> {
            LoteSetorRespostaDto lsDto = new LoteSetorRespostaDto();
            lsDto.setLoteSectorId(ls.getId());
            lsDto.setSetorId(ls.getSetor().getId());
            lsDto.setSetorNome(ls.getSetor().getNome());
            lsDto.setCapacidadeMaxima(ls.getSetor().getCapacidadeMaxima());

            List<LoteSetorRespostaDto.AnimalResumoDto> animaisDto = ls.getAnimais()
                    .stream()
                    .map(a -> {
                        LoteSetorRespostaDto.AnimalResumoDto ar = new LoteSetorRespostaDto.AnimalResumoDto();
                        ar.setId(a.getId());
                        ar.setCodigoBrinco(a.getCodigoBrinco());
                        ar.setNome(a.getNome());
                        return ar;
                    })
                    .toList();

            lsDto.setAnimais(animaisDto);
            return lsDto;
        }).toList();

        dto.setAlocacoes(alocacoesDto);
        dto.setTotalAnimais(
                alocacoesDto.stream().mapToInt(a -> a.getAnimais().size()).sum());

        return dto;
    }
}

