package br.com.gado.application.services;

import br.com.gado.application.dto.InsumoDto;
import br.com.gado.application.dto.insumoDto.VacinaCadastroDto;
import br.com.gado.application.dto.insumoDto.VacinaPutDto;
import br.com.gado.domain.entities.EInsumo;
import br.com.gado.domain.entities.EParceiro;
import br.com.gado.domain.enums.EnTipoInsumo;
import br.com.gado.infrastructure.persistence.repositories.IInsumo;
import br.com.gado.infrastructure.persistence.repositories.IParceiro;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SInsumo {

    @Autowired
    private IInsumo insumoInterface;

    @Autowired
    private IParceiro parceiroInterface;

    @Autowired
    private ModelMapper modelMapper;

    public List<InsumoDto> listarVacinas(String busca) {
        String termo = busca == null ? "" : busca.trim();
        List<EInsumo> vacinas = termo.isBlank()
                ? insumoInterface.findByTipoOrderByNomeAsc(EnTipoInsumo.VACINA)
                : insumoInterface.findByTipoAndNomeContainingIgnoreCaseOrderByNomeAsc(EnTipoInsumo.VACINA, termo);

        return vacinas.stream()
                .map((vacina) -> modelMapper.map(vacina, InsumoDto.class))
                .toList();
    }

    @Transactional
    public InsumoDto criarVacina(VacinaCadastroDto dto) {
        if (dto == null || dto.getNome() == null || dto.getNome().isBlank()) {
            throw new IllegalArgumentException("Informe o nome da vacina.");
        }

        String nome = dto.getNome().trim();
        if (insumoInterface.findFirstByTipoAndNomeIgnoreCase(EnTipoInsumo.VACINA, nome).isPresent()) {
            throw new IllegalArgumentException("Vacina já cadastrada.");
        }

        EInsumo vacina = new EInsumo();
        vacina.setNome(nome);
        vacina.setTipo(EnTipoInsumo.VACINA);
        vacina.setPendente(dto.getPendente() != null ? dto.getPendente() : Boolean.FALSE);

        EInsumo salva = insumoInterface.save(vacina);
        return modelMapper.map(salva, InsumoDto.class);
    }

    @Transactional
    public InsumoDto atualizarVacina(Long id, VacinaPutDto dto) {
        EInsumo vacina = insumoInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vacina não encontrada."));

        if (vacina.getTipo() != EnTipoInsumo.VACINA) {
            throw new IllegalArgumentException("Insumo informado não é uma vacina.");
        }

        if (dto == null) {
            throw new IllegalArgumentException("Dados de atualização ausentes.");
        }

        if (dto.getNome() != null) {
            String nome = dto.getNome().trim();
            if (nome.isBlank()) {
                throw new IllegalArgumentException("Informe o nome da vacina.");
            }
            vacina.setNome(nome);
        }

        if (dto.getPendente() != null) {
            vacina.setPendente(dto.getPendente());
        }

        EInsumo salva = insumoInterface.save(vacina);
        return modelMapper.map(salva, InsumoDto.class);
    }

    @Transactional
    public String deletarVacina(Long id) {
        EInsumo vacina = insumoInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vacina não encontrada."));

        if (vacina.getTipo() != EnTipoInsumo.VACINA) {
            throw new IllegalArgumentException("Insumo informado não é uma vacina.");
        }

        insumoInterface.deleteById(id);
        return "Vacina deletada com sucesso";
    }

    public InsumoDto buscaPorId(Long id) {
        EInsumo insumo = insumoInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Insumo não encontrado."));
        return modelMapper.map(insumo, InsumoDto.class);
    }

    @Transactional
    public InsumoDto cadastraInsumo(InsumoDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Dados de insumo ausentes.");
        }
        if (dto.getParceiro_id() == null) {
            throw new IllegalArgumentException("Informe o fornecedor (parceiro_id).");
        }

        EInsumo insumo = modelMapper.map(dto, EInsumo.class);

        EParceiro parceiro = parceiroInterface.findById(dto.getParceiro_id())
                .orElseThrow(() -> new EntityNotFoundException("Id do fornecedor não encontrado."));

        insumo.setParceiro(parceiro);

        EInsumo insumoSalvo = insumoInterface.save(insumo);
        return modelMapper.map(insumoSalvo, InsumoDto.class);
    }

    @Transactional
    public String deletaInsumo(Long id) {
        if (insumoInterface.findById(id).isEmpty()) {
            return "Nenhum insumo com esse id foi encontrado";
        }

        insumoInterface.deleteById(id);
        return "Insumo deletado com sucesso";
    }

    @Transactional
    public InsumoDto alteraInsumo(Long id, InsumoDto dto) {
        EInsumo insumo = insumoInterface.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nenhum insumo encontrado com esse id"));

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.map(dto, insumo);
        EInsumo insumoAtualizado = insumoInterface.save(insumo);
        return modelMapper.map(insumoAtualizado, InsumoDto.class);
    }
}

