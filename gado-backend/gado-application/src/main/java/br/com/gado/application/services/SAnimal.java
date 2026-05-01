package br.com.gado.application.services;

import br.com.gado.domain.entities.EAnimal;
import br.com.gado.domain.entities.EInsumo;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.domain.entities.EVacinacao;
import br.com.gado.application.dto.AnimalDto;
import br.com.gado.application.dto.AnimalRespostaDto;
import br.com.gado.application.dto.VacinacaoDTO;
import br.com.gado.infrastructure.persistence.repositories.IAnimal;
import br.com.gado.infrastructure.persistence.repositories.IInsumo;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
import br.com.gado.infrastructure.persistence.repositories.IVacinacao;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SAnimal {

    @Autowired
    private IAnimal animalInterface;

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private IVacinacao vacinacaoInterface;

    @Autowired
    private IInsumo insumoInterface;

    @Autowired
    private ModelMapper modelMapper;

    public List<AnimalRespostaDto> buscarPorTermo(String termo){
        List<EAnimal> animais;
        if (termo == null || termo.trim().isEmpty()) {
            animais = animalInterface.findAll();
        } else {
            String t = termo.trim();
            animais = animalInterface
                    .findByNomeContainingIgnoreCaseOrCodigoBrincoContainingIgnoreCase(t, t);
        }
        return animais.stream()
                .map(this::toRespostaDto)
                .toList();
    }

    private AnimalRespostaDto toRespostaDto(EAnimal animal) {
        AnimalRespostaDto dto = modelMapper.map(animal, AnimalRespostaDto.class);
        List<VacinacaoDTO> vacinas = vacinacaoInterface
                .findByAnimalRelacionado_Id(animal.getId())
                .stream()
                .map(v -> {
                    VacinacaoDTO vDto = new VacinacaoDTO();
                    vDto.setId(v.getId());
                    vDto.setDataOcorrencia(v.getDataOcorrencia());
                    if (v.getInsumoRelacionado() != null) {
                        EInsumo resumo = new EInsumo();
                        resumo.setId(v.getInsumoRelacionado().getId());
                        resumo.setNome(v.getInsumoRelacionado().getNome());
                        vDto.setInsumoRelacionado(resumo);
                    }
                    return vDto;
                })
                .toList();
        dto.setVacinas(vacinas);
        return dto;
    }

    public Map<String, Object> buscarPorBrinco(String brinco){
        Map<String, Object> response = new HashMap<>();
        Optional<EAnimal> animal = animalInterface.findByCodigoBrinco(brinco);

        if(animal.isPresent()) {
            response.put("mensagem", animal.get());
        } else {
            response.put("mensagem", "animal não encontrado");
        }

        return response;
    }

    @Transactional
    public String cadastraAnimal(String email, AnimalDto animal){
        try{
            EAnimal novoAnimal = new EAnimal();
            novoAnimal.setCodigoBrinco(animal.getCodigoBrinco());
            novoAnimal.setNome(animal.getNome());
            novoAnimal.setDataNascimento(animal.getDataNascimento());
            novoAnimal.setPesoAtual(animal.getPesoAtual());
            novoAnimal.setRaca(animal.getRaca());
            novoAnimal.setCor(animal.getCor());
            novoAnimal.setAlturaCernelha(animal.getAlturaCernelha());
            novoAnimal.setPerimetroToracico(animal.getPerimetroToracico());
            novoAnimal.setComprimentoCorporal(animal.getComprimentoCorporal());
            novoAnimal.setSexo(animal.getSexo());
            novoAnimal.setStatusAnimal(animal.getStatusAnimal());

            EUsuario usuario = usuarioInterface.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            novoAnimal.setUsuario(usuario);

            EAnimal animalSalvo = animalInterface.save(novoAnimal);

            if (animal.getVacinas() != null) {
                for (VacinacaoDTO vDto : animal.getVacinas()) {
                    if (vDto == null || vDto.getInsumoRelacionado() == null) {
                        continue;
                    }
                    String nomeInsumo = vDto.getInsumoRelacionado().getNome();
                    if (nomeInsumo == null || nomeInsumo.isBlank()) {
                        continue;
                    }
                    EInsumo insumo = obtemOuCriaInsumoVacina(nomeInsumo.trim());

                    EVacinacao vacinacao = new EVacinacao();
                    vacinacao.setDataOcorrencia(vDto.getDataOcorrencia());
                    vacinacao.setAnimalRelacionado(animalSalvo);
                    vacinacao.setUsuarioRelacionado(usuario);
                    vacinacao.setInsumoRelacionado(insumo);
                    vacinacaoInterface.save(vacinacao);
                }
            }

            return "animal cadastrado";

        } catch (Exception e) {
            return "Erro: " + e.getMessage();
        }
    }

    @Transactional
    public String deletaAnimal(String brinco){
        Optional<EAnimal> animal = animalInterface.findByCodigoBrinco(brinco);

        if(animal.isPresent()){
            animalInterface.deleteByCodigoBrinco(brinco);
            return "animal deletado";
        }
        return "animal não encontrado";

    }

    private EInsumo obtemOuCriaInsumoVacina(String nome) {
        return insumoInterface.findFirstByNomeIgnoreCase(nome)
                .orElseGet(() -> {
                    EInsumo novo = new EInsumo();
                    novo.setNome(nome);
                    novo.setTipo(br.com.gado.domain.enums.EnTipoInsumo.VACINA);
                    novo.setPendente(true);
                    return insumoInterface.save(novo);
                });
    }

    @Transactional
    public String alteraAnimal(String brinco, AnimalDto dto){
        Optional<EAnimal> animalOptional = animalInterface.findByCodigoBrinco(brinco);

        if(animalOptional.isEmpty()){
            return "animal não encontrado";
        }

        EAnimal animal = animalOptional.get();
        if (dto.getNome() != null) animal.setNome(dto.getNome());
        if (dto.getDataNascimento() != null) animal.setDataNascimento(dto.getDataNascimento());
        if (dto.getPesoAtual() != null) animal.setPesoAtual(dto.getPesoAtual());
        if (dto.getRaca() != null) animal.setRaca(dto.getRaca());
        if (dto.getCor() != null) animal.setCor(dto.getCor());
        if (dto.getAlturaCernelha() != null) animal.setAlturaCernelha(dto.getAlturaCernelha());
        if (dto.getPerimetroToracico() != null) animal.setPerimetroToracico(dto.getPerimetroToracico());
        if (dto.getComprimentoCorporal() != null) animal.setComprimentoCorporal(dto.getComprimentoCorporal());
        if (dto.getSexo() != null) animal.setSexo(dto.getSexo());
        if (dto.getStatusAnimal() != null) animal.setStatusAnimal(dto.getStatusAnimal());

        animalInterface.save(animal);
        return "animal atualizado";
    }
}
