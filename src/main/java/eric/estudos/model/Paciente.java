package eric.estudos.model;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paciente {
    private String nome;
    private String cpf;
    private LocalDate dataNascimento;
    private String telefone;
}
