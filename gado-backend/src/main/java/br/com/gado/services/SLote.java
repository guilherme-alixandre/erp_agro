package br.com.gado.services;

import br.com.gado.dto.loteDto.CustoRacaoLoteDto;
import br.com.gado.dto.loteDto.LoteCadastroDto;
import br.com.gado.dto.loteDto.LoteDto;
import br.com.gado.dto.loteDto.LotePutDto;
import br.com.gado.dto.loteDto.LoteSetorCadastroDto;
import br.com.gado.dto.loteDto.LoteSetorRespostaDto;
import br.com.gado.dto.loteDto.TransferenciaAnimalDto;
import br.com.gado.entities.EAnimal;
import br.com.gado.entities.EConsumoInsumo;
import br.com.gado.entities.ELote;
import br.com.gado.entities.ELoteSetor;
import br.com.gado.entities.ESetor;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnStatusAnimal;
import br.com.gado.repositories.IAnimal;
import br.com.gado.repositories.IConsumoInsumo;
import br.com.gado.repositories.ILote;
import br.com.gado.repositories.ILoteSetor;
import br.com.gado.repositories.ISetor;
import br.com.gado.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SLote {

    private static final String PREFIXO_CODIGO = "LOT";
    private static final int LARGURA_NUMERO = 3;

    @Autowired
    private ILote loteInterface;

    @Autowired
    private ILoteSetor loteSetorInterface;

    @Autowired
    private ISetor setorInterface;

    @Autowired
    private IAnimal animalInterface;

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private IConsumoInsumo consumoInsumoInterface;

    // ── Controle de acesso ───────────────────────────────────────────────

    public void validaPermissao(String emailUsuario) {
        EUsuario usuario = resolveUsuarioObrigatorio(emailUsuario);
        if (usuario.getPerfil() != EnPerfilUsuario.ADMINISTRADOR
                && usuario.getPerfil() != EnPerfilUsuario.GERENTE) {
            throw new IllegalArgumentException(
                    "Apenas Administradores e Gerentes podem criar ou excluir lotes.");
        }
    }

    public void validaPermissaoEdicao(String emailUsuario) {
        EUsuario usuario = resolveUsuarioObrigatorio(emailUsuario);
        if (usuario.getPerfil() != EnPerfilUsuario.ADMINISTRADOR
                && usuario.getPerfil() != EnPerfilUsuario.GERENTE
                && usuario.getPerfil() != EnPerfilUsuario.CUIDADOR_CHEFE) {
            throw new IllegalArgumentException(
                    "Apenas Administradores, Gerentes e Cuidadores Chefe podem editar lotes.");
        }
    }

    public void validaPermissaoTransferencia(String emailUsuario) {
        EUsuario usuario = resolveUsuarioObrigatorio(emailUsuario);
        if (usuario.getPerfil() != EnPerfilUsuario.ADMINISTRADOR
                && usuario.getPerfil() != EnPerfilUsuario.GERENTE
                && usuario.getPerfil() != EnPerfilUsuario.CUIDADOR_CHEFE) {
            throw new IllegalArgumentException(
                    "Apenas Administradores, Gerentes e Cuidadores Chefe podem transferir animais entre lotes.");
        }
    }

    private EUsuario resolveUsuarioObrigatorio(String emailUsuario) {
        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new IllegalArgumentException("Informe o e-mail do usuário responsável pela operação.");
        }
        return usuarioInterface.findByEmailAndStatus(emailUsuario.trim(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }

    // ── Leitura ───────────────────────────────────────────────────────────

    public LoteDto buscaPorId(Long id) {
        ELote lote = loteInterface.findByIdAndStatus(id, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Nenhum lote ativo encontrado para o ID: " + id));
        return toDto(lote);
    }

    public List<LoteDto> listarTodos() {
        return loteInterface.findAllByStatus(EnStatus.A).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ── Cadastro ──────────────────────────────────────────────────────────

    @Transactional
    public String cadastra(String emailUsuario, LoteCadastroDto dto) {
        validaPermissao(emailUsuario);
        EUsuario usuario = resolveUsuarioObrigatorio(emailUsuario);

        ELote lote = new ELote();
        lote.setCodigo(gerarProximoCodigo());
        lote.setCorBrinco(dto.getCorBrinco());
        lote.setDescricao(dto.getDescricao());
        lote.setRacaPredominante(dto.getRacaPredominante());
        lote.setDataCriacao(dto.getDataCriacao() != null ? dto.getDataCriacao() : LocalDate.now());
        lote.setCriadoPor(usuario);

        ELote loteSalvo = loteInterface.save(lote);
        aplicarAlocacoes(loteSalvo, dto.getAlocacoes(), Set.of());

        log.info("Lote {} criado por {}", loteSalvo.getCodigo(), emailUsuario);
        return "Lote " + loteSalvo.getCodigo() + " cadastrado com sucesso.";
    }

    // ── Edição ────────────────────────────────────────────────────────────

    @Transactional
    public String altera(Long id, String emailUsuario, LotePutDto dto) {
        validaPermissaoEdicao(emailUsuario);
        EUsuario usuario = resolveUsuarioObrigatorio(emailUsuario);

        ELote lote = loteInterface.findByIdAndStatus(id, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Nenhum lote ativo encontrado para o ID: " + id));

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

        if (dto.getAlocacoes() != null && !dto.getAlocacoes().isEmpty()) {
            List<ELoteSetor> alocacoesAntigas = loteSetorInterface.findByLote_Id(lote.getId());

            Set<Long> animaisJaNesteLote = alocacoesAntigas.stream()
                    .flatMap(ls -> ls.getAnimais().stream())
                    .map(EAnimal::getId)
                    .collect(Collectors.toSet());

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
                                            + " Restaure o status para ATIVO ou OBSERVACAO antes de realizar esta operação.");
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

    // ── Exclusão (sempre soft-delete) ────────────────────────────────────

    @Transactional
    public String deleta(Long id, String emailUsuario) {
        validaPermissao(emailUsuario);

        ELote lote = loteInterface.findByIdAndStatus(id, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Nenhum lote ativo encontrado para o ID: " + id));

        lote.setStatus(EnStatus.I);
        loteInterface.save(lote);

        log.info("Lote {} inativado por {}", lote.getCodigo(), emailUsuario);
        return "Lote " + lote.getCodigo() + " excluído com sucesso.";
    }

    // ── Transferência de animal ───────────────────────────────────────────

    @Transactional
    public String transferirAnimal(String emailUsuario, TransferenciaAnimalDto dto) {
        validaPermissaoTransferencia(emailUsuario);

        EAnimal animal = animalInterface.findById(dto.getAnimalId())
                .orElseThrow(() -> new IllegalArgumentException("Animal não encontrado."));

        if (isStatusBloqueado(animal.getStatusAnimal())) {
            throw new IllegalArgumentException(
                    "O animal " + animal.getCodigoBrinco()
                            + " possui status " + animal.getStatusAnimal().name()
                            + " e não pode ser movimentado.");
        }

        List<ELoteSetor> alocacoesAtuais =
                loteSetorInterface.findByAnimalIdAndLoteAtivo(dto.getAnimalId(), EnStatus.A);
        if (alocacoesAtuais.isEmpty()) {
            throw new IllegalArgumentException(
                    "O animal " + animal.getCodigoBrinco() + " não está alocado em nenhum lote ativo.");
        }
        ELoteSetor loteSetorOrigem = alocacoesAtuais.get(0);

        ELote loteDestino = loteInterface.findByIdAndStatus(dto.getLoteDestinoId(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Lote de destino não encontrado ou inativo."));

        ESetor setorDestino = setorInterface.findByIdAndStatus(dto.getSetorDestinoId(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Setor de destino não encontrado ou inativo."));

        if (loteSetorOrigem.getLote().getId().equals(dto.getLoteDestinoId())
                && loteSetorOrigem.getSetor().getId().equals(dto.getSetorDestinoId())) {
            throw new IllegalArgumentException("O animal já está alocado no lote e setor informados.");
        }

        int jaAlocados = loteSetorInterface.findBySetor_Id(setorDestino.getId())
                .stream()
                .mapToInt(ls -> (int) ls.getAnimais().stream()
                        .filter(a -> !a.getId().equals(dto.getAnimalId()))
                        .count())
                .sum();
        if (setorDestino.getCapacidadeMaxima() > 0 && jaAlocados + 1 > setorDestino.getCapacidadeMaxima()) {
            throw new IllegalArgumentException(
                    "O setor '" + setorDestino.getNome() + "' excede a capacidade máxima ("
                            + setorDestino.getCapacidadeMaxima() + "). Já há " + jaAlocados + " animais.");
        }

        String nomeOrigem = loteSetorOrigem.getLote().getCodigo() + "/" + loteSetorOrigem.getSetor().getNome();

        loteSetorOrigem.getAnimais().removeIf(a -> a.getId().equals(dto.getAnimalId()));
        loteSetorInterface.save(loteSetorOrigem);

        ELoteSetor loteSetorDestino = loteSetorInterface.findByLote_Id(loteDestino.getId())
                .stream()
                .filter(ls -> ls.getSetor().getId().equals(setorDestino.getId()))
                .findFirst()
                .orElseGet(() -> {
                    ELoteSetor novo = new ELoteSetor();
                    novo.setLote(loteDestino);
                    novo.setSetor(setorDestino);
                    return loteSetorInterface.save(novo);
                });

        loteSetorDestino.getAnimais().add(animal);
        loteSetorInterface.save(loteSetorDestino);

        log.info("Animal {} transferido de {} para {}/{} por {}",
                animal.getCodigoBrinco(), nomeOrigem, loteDestino.getCodigo(), setorDestino.getNome(), emailUsuario);

        return "Animal " + animal.getCodigoBrinco()
                + " transferido para o lote " + loteDestino.getCodigo()
                + ", setor " + setorDestino.getNome() + ".";
    }

    // ── Geração de código ─────────────────────────────────────────────────

    String gerarProximoCodigo() {
        String ultimoCodigo = loteInterface.findUltimoCodigoGerado().orElse(PREFIXO_CODIGO + "000");
        String parteNumerica = ultimoCodigo.substring(PREFIXO_CODIGO.length());

        int proximoNumero;
        try {
            proximoNumero = Integer.parseInt(parteNumerica) + 1;
        } catch (NumberFormatException e) {
            proximoNumero = 1;
        }

        String formatStr = "%0" + LARGURA_NUMERO + "d";
        return PREFIXO_CODIGO + String.format(formatStr, proximoNumero);
    }

    // ── Lógica de alocação ────────────────────────────────────────────────

    private void aplicarAlocacoes(ELote lote, List<LoteSetorCadastroDto> alocacoesDto, Set<Long> animaisJaNesteLote) {
        Set<Long> animaisJaProcessadosNestaChamada = new java.util.HashSet<>();

        for (LoteSetorCadastroDto alocDto : alocacoesDto) {
            ESetor setor = setorInterface.findByIdAndStatus(alocDto.getSetorId(), EnStatus.A)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Setor não encontrado ou inativo: ID " + alocDto.getSetorId()));

            ELoteSetor loteSetor = new ELoteSetor();
            loteSetor.setLote(lote);
            loteSetor.setSetor(setor);

            if (alocDto.getAnimaisIds() != null && !alocDto.getAnimaisIds().isEmpty()) {
                List<EAnimal> animais = animalInterface.findAllById(alocDto.getAnimaisIds());

                if (animais.size() != alocDto.getAnimaisIds().size()) {
                    throw new IllegalArgumentException(
                            "Um ou mais animais informados para o setor " + setor.getNome() + " não foram encontrados.");
                }

                for (EAnimal animal : animais) {
                    List<ELoteSetor> conflitos =
                            loteSetorInterface.findConflitosAtivos(animal.getId(), lote.getId(), EnStatus.A);
                    if (!conflitos.isEmpty()) {
                        String loteConflito = conflitos.get(0).getLote().getCodigo();
                        throw new IllegalArgumentException(
                                "O animal " + animal.getCodigoBrinco() + " já está alocado ao lote " + loteConflito
                                        + ". Desvincule-o antes de adicioná-lo a outro lote.");
                    }

                    if (!animaisJaProcessadosNestaChamada.add(animal.getId())) {
                        throw new IllegalArgumentException(
                                "O animal " + animal.getCodigoBrinco()
                                        + " foi informado em mais de um setor deste lote na mesma operação."
                                        + " Um animal só pode estar em um setor do lote por vez.");
                    }

                    if (!animaisJaNesteLote.contains(animal.getId()) && isStatusBloqueado(animal.getStatusAnimal())) {
                        throw new IllegalArgumentException(
                                "O animal " + animal.getCodigoBrinco()
                                        + " possui status " + animal.getStatusAnimal().name()
                                        + " e não pode ser movimentado entre lotes."
                                        + " Restaure o status para ATIVO ou OBSERVACAO antes de realizar esta operação.");
                    }
                }

                int jaAlocados = loteSetorInterface.findBySetor_Id(setor.getId())
                        .stream()
                        .mapToInt(ls -> ls.getAnimais().size())
                        .sum();
                int totalDepois = jaAlocados + animais.size();

                if (setor.getCapacidadeMaxima() > 0 && totalDepois > setor.getCapacidadeMaxima()) {
                    throw new IllegalArgumentException(
                            "O setor '" + setor.getNome() + "' excede a capacidade máxima (" + setor.getCapacidadeMaxima()
                                    + "). Já há " + jaAlocados + " animais; tentativa de adicionar " + animais.size() + ".");
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

    // ── Custo de ração acumulado (informativo, ver CustoRacaoLoteDto) ─────

    public CustoRacaoLoteDto calcularCustoRacaoAcumulado(Long loteId) {
        ELote lote = loteInterface.findByIdAndStatus(loteId, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Nenhum lote ativo encontrado para o ID: " + loteId));

        List<ELoteSetor> alocacoes = loteSetorInterface.findByLote_Id(lote.getId());
        BigDecimal custoTotal = BigDecimal.ZERO;

        for (ELoteSetor alocacao : alocacoes) {
            int animaisDoLoteNoSetor = alocacao.getAnimais().size();
            if (animaisDoLoteNoSetor == 0) continue;

            List<EConsumoInsumo> consumos = consumoInsumoInterface
                    .findBySetor_IdAndStatusOrderByDataConsumoDesc(alocacao.getSetor().getId(), EnStatus.A);

            for (EConsumoInsumo consumo : consumos) {
                if (consumo.getConsumoPorAnimal() == null) continue;
                Double precoMedio = consumo.getInsumo().getPrecoCompraMedio();
                if (precoMedio == null) continue;

                double quantidadeDoLote = consumo.getConsumoPorAnimal() * animaisDoLoteNoSetor;
                custoTotal = custoTotal.add(BigDecimal.valueOf(quantidadeDoLote * precoMedio));
            }
        }

        int totalAnimaisLote = alocacoes.stream().mapToInt(a -> a.getAnimais().size()).sum();
        custoTotal = custoTotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal custoPorAnimal = totalAnimaisLote > 0
                ? custoTotal.divide(BigDecimal.valueOf(totalAnimaisLote), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new CustoRacaoLoteDto(loteId, custoTotal, custoPorAnimal);
    }

    // ── Mapeamento para resposta ──────────────────────────────────────────

    private LoteDto toDto(ELote lote) {
        LoteDto dto = new LoteDto();
        dto.setId(lote.getId());
        dto.setStatus(lote.getStatus());
        dto.setCodigo(lote.getCodigo());
        dto.setDescricao(lote.getDescricao());
        dto.setRacaPredominante(lote.getRacaPredominante());
        dto.setCorBrinco(lote.getCorBrinco());
        dto.setDataCriacao(lote.getDataCriacao());

        if (lote.getCriadoPor() != null) {
            dto.setCriadoPorNome(lote.getCriadoPor().getNome());
            dto.setCriadoPorEmail(lote.getCriadoPor().getEmail());
        }
        if (lote.getAlteradoPor() != null) {
            dto.setAlteradoPorNome(lote.getAlteradoPor().getNome());
            dto.setAlteradoPorEmail(lote.getAlteradoPor().getEmail());
        }

        List<ELoteSetor> alocacoes = loteSetorInterface.findByLote_Id(lote.getId());

        List<LoteSetorRespostaDto> alocacoesDto = new ArrayList<>();
        for (ELoteSetor ls : alocacoes) {
            LoteSetorRespostaDto lsDto = new LoteSetorRespostaDto();
            lsDto.setLoteSectorId(ls.getId());
            lsDto.setSetorId(ls.getSetor().getId());
            lsDto.setSetorNome(ls.getSetor().getNome());
            lsDto.setCapacidadeMaxima(ls.getSetor().getCapacidadeMaxima());

            List<LoteSetorRespostaDto.AnimalResumoDto> animaisDto = ls.getAnimais().stream().map(a -> {
                LoteSetorRespostaDto.AnimalResumoDto ar = new LoteSetorRespostaDto.AnimalResumoDto();
                ar.setId(a.getId());
                ar.setCodigoBrinco(a.getCodigoBrinco());
                if (a.getRaca() != null) {
                    ar.setRacaNome(a.getRaca().getNome());
                }
                return ar;
            }).collect(Collectors.toList());

            lsDto.setAnimais(animaisDto);
            alocacoesDto.add(lsDto);
        }

        dto.setAlocacoes(alocacoesDto);
        dto.setTotalAnimais(alocacoesDto.stream().mapToInt(a -> a.getAnimais().size()).sum());

        return dto;
    }
}
