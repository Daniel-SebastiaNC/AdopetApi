package br.com.alura.adopet.api.service;

import br.com.alura.adopet.api.dto.AprovacaoAdocaoDto;
import br.com.alura.adopet.api.dto.ReprovacaoAdocaoDto;
import br.com.alura.adopet.api.dto.SolicitacaoAdocaoDto;
import br.com.alura.adopet.api.excpetion.ValidacaoExcpetion;
import br.com.alura.adopet.api.model.Adocao;
import br.com.alura.adopet.api.model.Pet;
import br.com.alura.adopet.api.model.StatusAdocao;
import br.com.alura.adopet.api.model.Tutor;
import br.com.alura.adopet.api.repository.AdocaoRepository;
import br.com.alura.adopet.api.repository.PetRepository;
import br.com.alura.adopet.api.repository.TutorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AdocaoService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private AdocaoRepository adocaoRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private TutorRepository tutorRepository;


    public void solicitar(SolicitacaoAdocaoDto dto) {
        Pet pet = petRepository.getReferenceById(dto.idPet());
        Tutor tutor = tutorRepository.getReferenceById(dto.idTutor());

        if (pet.getAdotado() == true) {
            throw new ValidacaoExcpetion("Pet já foi adotado!");
        } else {
            List<Adocao> adocoes = adocaoRepository.findAll();
            for (Adocao a : adocoes) {
                if (a.getTutor() == tutor && a.getStatus() == StatusAdocao.AGUARDANDO_AVALIACAO) {
                    throw new ValidacaoExcpetion("Tutor já possui outra adoção aguardando avaliação!");
                }
            }
            for (Adocao a : adocoes) {
                if (a.getPet() == pet && a.getStatus() == StatusAdocao.AGUARDANDO_AVALIACAO) {
                    throw new ValidacaoExcpetion("Pet já está aguardando avaliação para ser adotado!");
                }
            }
            for (Adocao a : adocoes) {
                int contador = 0;
                if (a.getTutor() == tutor && a.getStatus() == StatusAdocao.APROVADO) {
                    contador = contador + 1;
                }
                if (contador == 5) {
                    throw new ValidacaoExcpetion("Tutor chegou ao limite máximo de 5 adoções!");
                }
            }
        }

        Adocao adocao = new Adocao();
        adocao.setData(LocalDateTime.now());
        adocao.setStatus(StatusAdocao.AGUARDANDO_AVALIACAO);
        adocao.setPet(pet);
        adocao.setTutor(tutor);
        adocaoRepository.save(adocao);

        String to = pet.getAbrigo().getEmail();
        String subject = "Solicitação de adoção";
        String text = "Olá " +pet.getAbrigo().getNome() +"!\n\nUma solicitação de adoção foi registrada hoje para o pet: " +pet.getNome() +". \nFavor avaliar para aprovação ou reprovação.";
        this.emailService.enviarEmail(to, subject, text);
    }

    public void aprovar(AprovacaoAdocaoDto dto) {
        Adocao adocao = adocaoRepository.getReferenceById(dto.idAdocao());

        adocao.setStatus(StatusAdocao.APROVADO);
        adocaoRepository.save(adocao);

        String to = adocao.getPet().getAbrigo().getEmail();
        String subject = "Adoção aprovada";
        String text = "Parabéns " +adocao.getTutor().getNome() +"!\n\nSua adoção do pet " +adocao.getPet().getNome() +", solicitada em " +adocao.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) +", foi aprovada.\nFavor entrar em contato com o abrigo " +adocao.getPet().getAbrigo().getNome() +" para agendar a busca do seu pet.";
        this.emailService.enviarEmail(to, subject, text);
    }

    public void renovar(ReprovacaoAdocaoDto dto) {
        Adocao adocao = adocaoRepository.getReferenceById(dto.idAdocao());

        adocao.setStatus(StatusAdocao.REPROVADO);
        adocaoRepository.save(adocao);
        adocao.setJustificativaStatus(dto.justificativa());

        String to = adocao.getPet().getAbrigo().getEmail();
        String subject = "Adoção reprovada";
        String text = "Olá " +adocao.getTutor().getNome() +"!\n\nInfelizmente sua adoção do pet " +adocao.getPet().getNome() +", solicitada em " +adocao.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) +", foi reprovada pelo abrigo " +adocao.getPet().getAbrigo().getNome() +" com a seguinte justificativa: " +adocao.getJustificativaStatus();
        this.emailService.enviarEmail(to, subject, text);
    }
}
