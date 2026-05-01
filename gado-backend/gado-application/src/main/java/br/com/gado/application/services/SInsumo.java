package br.com.gado.application.services;

import br.com.gado.domain.entities.EInsumo;
import br.com.gado.domain.entities.EParceiro;
import br.com.gado.domain.enums.EnTipoInsumo;
import br.com.gado.application.dto.InsumoDto;
import br.com.gado.application.dto.InsumoRespostaDto;
import br.com.gado.infrastructure.persistence.repositories.IInsumo;
import br.com.gado.infrastructure.persistence.repositories.IParceiro;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SInsumo {

    @Autowired
    private IInsumo insumoInterface;

    @Autowired
    private IParceiro parceiroInterface;

    @Autowired
    private ModelMapper modelMapper;


    public Map<String, Object> buscaPorId(Long id){
        Map<String, Object> response = new HashMap<>();
        Optional<EInsumo> insumo = insumoInterface.findById(id);

        if(insumo.isEmpty()){
            response.put("Erro", "Insumo não encontrado");
        }
        else {
            response.put("insumo", insumo.get());
        }

        return response;
    }

    @Transactional
    public String cadastraInsumo(InsumoDto dto){
        EInsumo insumo = new EInsumo();

        insumo.setNome(dto.getNome());
        insumo.setEstoqueMinimo(dto.getEstoqueMinimo());
        insumo.setSaldoAtual(dto.getSaldoAtual());

        Optional<EParceiro> parceiroOptional = parceiroInterface.findById(dto.getParceiro_id());
        if(parceiroOptional.isEmpty()){
            return "Id do forcenedor não encontrado";
        }
        insumo.setParceiro(parceiroOptional.get());
        insumo.setTipo(dto.getTipo());

        insumoInterface.save(insumo);
        return "insumo cadastrado";
    }

    @Transactional
    public String deletaInsumo(Long id){
        Optional<EInsumo> insumoOptional = insumoInterface.findById(id);

        if(insumoOptional.isEmpty()){
            return "nenhum insumo com esse id foi encontrado";
        }

        insumoInterface.deleteById(id);
        return "insumo deletado com sucesso";
    }

    @Transactional
    public String alteraInsumo(Long id, InsumoDto dto){
        Optional<EInsumo> insumoOptional = insumoInterface.findById(id);

        if(insumoOptional.isEmpty()){
            return "nenhum insumo encontrado com esse id";
        }

        EInsumo insumo = insumoOptional.get();
        modelMapper.map(dto, insumo);

        insumoInterface.save(insumo);
        return "insumo alterado com sucesso";
    }

    public List<InsumoRespostaDto> listarVacinas(String busca) {
        List<EInsumo> vacinas;
        if (busca == null || busca.trim().isEmpty()) {
            vacinas = insumoInterface.findByTipoOrderByNomeAsc(EnTipoInsumo.VACINA);
        } else {
            vacinas = insumoInterface
                    .findByTipoAndNomeContainingIgnoreCaseOrderByNomeAsc(
                            EnTipoInsumo.VACINA, busca.trim());
        }
        return vacinas.stream().map(this::toRespostaDto).toList();
    }

    @Transactional
    public InsumoRespostaDto cadastraVacina(InsumoDto dto) {
        if (dto.getNome() == null || dto.getNome().isBlank()) {
            throw new IllegalArgumentException("Informe o nome da vacina.");
        }
        String nome = dto.getNome().trim();

        Optional<EInsumo> existente = insumoInterface.findFirstByNomeIgnoreCase(nome);
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Já existe uma vacina cadastrada com esse nome.");
        }

        EInsumo vacina = new EInsumo();
        vacina.setNome(nome);
        vacina.setTipo(EnTipoInsumo.VACINA);
        vacina.setPendente(Boolean.TRUE.equals(dto.getPendente()));
        EInsumo salva = insumoInterface.save(vacina);
        return toRespostaDto(salva);
    }

    @Transactional
    public InsumoRespostaDto alteraVacina(Long id, InsumoDto dto) {
        EInsumo vacina = insumoInterface.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vacina não encontrada."));

        if (dto.getNome() != null && !dto.getNome().isBlank()) {
            vacina.setNome(dto.getNome().trim());
        }
        if (dto.getPendente() != null) {
            vacina.setPendente(dto.getPendente());
        }
        vacina.setTipo(EnTipoInsumo.VACINA);

        EInsumo salva = insumoInterface.save(vacina);
        return toRespostaDto(salva);
    }

    @Transactional
    public String deletaVacina(Long id) {
        if (insumoInterface.findById(id).isEmpty()) {
            return "Vacina não encontrada.";
        }
        insumoInterface.deleteById(id);
        return "vacina deletada";
    }

    private InsumoRespostaDto toRespostaDto(EInsumo insumo) {
        InsumoRespostaDto dto = new InsumoRespostaDto();
        dto.setId(insumo.getId());
        dto.setNome(insumo.getNome());
        dto.setTipo(insumo.getTipo());
        dto.setPendente(Boolean.TRUE.equals(insumo.getPendente()));
        return dto;
    }

}
