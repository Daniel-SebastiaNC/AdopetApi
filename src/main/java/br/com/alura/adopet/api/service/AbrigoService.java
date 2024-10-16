package br.com.alura.adopet.api.service;

import br.com.alura.adopet.api.dto.AbrigoDto;
import br.com.alura.adopet.api.dto.CadastrarAbrigoDto;
import br.com.alura.adopet.api.dto.PetDto;
import br.com.alura.adopet.api.excpetion.ValidacaoExcpetion;
import br.com.alura.adopet.api.model.Abrigo;
import br.com.alura.adopet.api.repository.AbrigoRepository;
import br.com.alura.adopet.api.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AbrigoService {

    @Autowired
    private AbrigoRepository repository;

    @Autowired
    private PetRepository petRepository;

    public List<AbrigoDto> listar(){
        return repository.findAll()
                .stream()
                .map(AbrigoDto::new)
                .toList();
    }

    public void cadastrar(CadastrarAbrigoDto dto) {
        boolean jaCadastrado = repository.existsByNomeOrTelefoneOrEmail(dto.nome(), dto.telefone(), dto.email());

        if (jaCadastrado) {
            throw new ValidacaoExcpetion("Dados já cadastrados para outro abrigo!");
        }

        repository.save(new Abrigo(dto));

    }

    public List<PetDto> listarPets(String idOuNome){
        Abrigo abrigo = carregarAbrigo(idOuNome);
        return petRepository
                .findByAbrigo(abrigo)
                .stream()
                .map(PetDto::new)
                .toList();
    }

    public Abrigo carregarAbrigo(String idOuNome) {
        Optional<Abrigo> optional;
        try {
            Long id = Long.parseLong(idOuNome);
            optional = repository.findById(id);
        }catch (NumberFormatException e){
            optional = repository.findByNome(idOuNome);
        }

        return optional.orElseThrow(() -> new ValidacaoExcpetion("Abrigo não encontrado!"));
    }
}
