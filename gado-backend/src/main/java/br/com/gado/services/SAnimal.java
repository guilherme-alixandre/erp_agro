package br.com.gado.services;

import br.com.gado.dto.AnimalDto;
import br.com.gado.entities.EAnimal;
import br.com.gado.entities.ERaca;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.IAnimal;
import br.com.gado.repositories.IRaca;
import br.com.gado.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SAnimal {

    /** Tentativas de regerar o código do brinco em caso de corrida entre requisições concorrentes. */
    private static final int TENTATIVAS_MAXIMAS_CODIGO = 5;

    @Autowired
    private IAnimal animalInterface;

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private IRaca racaInterface;

    @Autowired
    private ModelMapper modelMapper;

    public AnimalDto buscarPorBrinco(String brinco) {
        EAnimal animal = animalInterface.findByCodigoBrincoAndStatus(brinco, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("animal não encontrado"));
        return toDto(animal);
    }

    public ArrayList<AnimalDto> buscarTodosAnimais(String termo) {
        String termoLimpo = termo == null ? "" : termo.trim();

        ArrayList<EAnimal> animais = termoLimpo.isBlank()
                ? animalInterface.findAllByStatus(EnStatus.A).orElse(new ArrayList<>())
                : new ArrayList<>(animalInterface.buscarPorTermo(EnStatus.A, termoLimpo));

        return animais.stream()
                .map(this::toDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Transactional
    public AnimalDto cadastraAnimal(String email, AnimalDto animalDto) throws Exception {
        if (animalDto.getRacaId() == null) {
            throw new IllegalArgumentException("A raça do animal é obrigatória.");
        }
        ERaca raca = racaInterface.findById(animalDto.getRacaId())
                .filter(r -> r.getStatus() == EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Raça não encontrada ou inativa."));

        EUsuario usuario = usuarioInterface.findByEmailAndStatus(email, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        EAnimal novoAnimal = modelMapper.map(animalDto, EAnimal.class);
        novoAnimal.setRaca(raca);
        novoAnimal.setUsuario(usuario);
        // codigoBrinco é sempre gerado pelo backend — ignora qualquer valor vindo do cliente.
        EAnimal animalSalvo = salvarComCodigoSequencial(novoAnimal, raca);

        return toDto(animalSalvo);
    }

    @Transactional
    public String deletaAnimal(String brinco) {
        Optional<EAnimal> animalOptional = animalInterface.findByCodigoBrincoAndStatus(brinco, EnStatus.A);

        if(animalOptional.isEmpty())
            return "animal não encontrado";

        EAnimal animal = animalOptional.get();
        animal.setStatus(EnStatus.I);
        return "Animal deletado com sucesso";
    }

    @Transactional
    public AnimalDto alteraAnimal(String brinco, AnimalDto dto) {
        EAnimal animal = animalInterface.findByCodigoBrincoAndStatus(brinco, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("animal não encontrado"));

        Long racaId = dto.getRacaId();
        // codigoBrinco nunca é alterado por aqui (brinco físico é imutável); zera no dto antes do
        // mapeamento genérico pra não correr risco de o modelMapper sobrescrever.
        dto.setCodigoBrinco(null);

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.map(dto, animal);

        if (racaId != null) {
            ERaca raca = racaInterface.findById(racaId)
                    .filter(r -> r.getStatus() == EnStatus.A)
                    .orElseThrow(() -> new IllegalArgumentException("Raça não encontrada ou inativa."));
            animal.setRaca(raca);
        }

        EAnimal animalAtualizado = animalInterface.save(animal);
        return toDto(animalAtualizado);
    }

    // ── Geração do código do brinco ─────────────────────────────────────

    private String gerarProximoCodigoBrinco(ERaca raca) {
        String sigla = raca.getSigla();
        int proximoSequencial = animalInterface
                .findFirstByCodigoBrincoStartingWithOrderByCodigoBrincoDesc(sigla)
                .map(ultimo -> Integer.parseInt(ultimo.getCodigoBrinco().substring(sigla.length())) + 1)
                .orElse(1);
        return sigla + String.format("%04d", proximoSequencial);
    }

    /**
     * Gera o código do brinco e grava o animal. Como EAbstract usa GenerationType.IDENTITY, o
     * INSERT (e a violação da constraint única de codigo_brinco, se houver) só acontece no
     * saveAndFlush(), permitindo recalcular o próximo sequencial e tentar de novo em caso de
     * corrida entre duas requisições concorrentes cadastrando animais da mesma raça.
     */
    private EAnimal salvarComCodigoSequencial(EAnimal animal, ERaca raca) {
        for (int tentativa = 1; tentativa <= TENTATIVAS_MAXIMAS_CODIGO; tentativa++) {
            animal.setCodigoBrinco(gerarProximoCodigoBrinco(raca));
            try {
                return animalInterface.saveAndFlush(animal);
            } catch (DataIntegrityViolationException e) {
                if (tentativa == TENTATIVAS_MAXIMAS_CODIGO) {
                    throw new IllegalStateException(
                            "Não foi possível gerar um código de brinco único após "
                                    + TENTATIVAS_MAXIMAS_CODIGO + " tentativas.", e);
                }
            }
        }
        throw new IllegalStateException("Falha inesperada ao gerar o código do brinco.");
    }

    private AnimalDto toDto(EAnimal animal) {
        AnimalDto dto = modelMapper.map(animal, AnimalDto.class);
        if (animal.getRaca() != null) {
            dto.setRacaId(animal.getRaca().getId());
            dto.setRacaNome(animal.getRaca().getNome());
            dto.setRacaSigla(animal.getRaca().getSigla());
        }
        return dto;
    }

}
