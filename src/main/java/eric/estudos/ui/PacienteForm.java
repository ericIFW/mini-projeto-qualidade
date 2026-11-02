package eric.estudos.ui;

import eric.estudos.model.Paciente;
import eric.estudos.repository.PacienteRepository;
import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class PacienteForm extends JFrame {
    private final JTextField nome = new JTextField(24);
    private final JFormattedTextField cpf;
    private final JFormattedTextField nascimento;
    private final JFormattedTextField telefone;
    private final PacienteRepository repository;

    public PacienteForm() {
        super("Cadastro de Pacientes");
        repository = new PacienteRepository();
        cpf = maskedField("###.###.###-##");
        nascimento = maskedField("##/##/####");
        telefone = maskedField("(##) #####-####");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(buildForm());
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private JFormattedTextField maskedField(String mask) {
        try {
            MaskFormatter formatter = new MaskFormatter(mask);
            formatter.setPlaceholderCharacter('_');
            JFormattedTextField field = new JFormattedTextField(formatter);
            field.setColumns(mask.length());
            return field;
        } catch (java.text.ParseException e) { throw new IllegalStateException(e); }
    }

    private JPanel buildForm() {
        JPanel outer = new JPanel(new BorderLayout(12, 12));
        outer.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        JLabel title = new JLabel("Cadastro de paciente");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        outer.add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(7, 4, 7, 10);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; c.gridy = 0;
        addField(fields, c, "Nome completo *", nome);
        addField(fields, c, "CPF *", cpf);
        addField(fields, c, "Data de nascimento * (dd/MM/aaaa)", nascimento);
        addField(fields, c, "Telefone *", telefone);
        outer.add(fields, BorderLayout.CENTER);

        JButton save = new JButton("Cadastrar paciente");
        save.addActionListener(event -> salvar());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.add(save);
        outer.add(actions, BorderLayout.SOUTH);
        return outer;
    }

    private void addField(JPanel panel, GridBagConstraints c, String label, JComponent field) {
        c.gridx = 0; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(label), c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, c);
        c.gridy++;
    }

    private void salvar() {
        String nomeValue = nome.getText().trim();
        String cpfValue = digits(cpf.getText());
        String nascimentoValue = nascimento.getText().trim();
        String telefoneValue = digits(telefone.getText());
        if (nomeValue.isBlank() || cpfValue.length() != 11 || nascimentoValue.contains("_") || telefoneValue.length() < 10) {
            showError("Preencha todos os campos obrigatórios com valores válidos.");
            return;
        }
        if (!validCpf(cpfValue)) {
            showError("Informe um CPF válido.");
            return;
        }
        LocalDate data;
        try {
            data = LocalDate.parse(nascimentoValue, DateTimeFormatter.ofPattern("dd/MM/uuuu"));
            if (data.isAfter(LocalDate.now())) throw new DateTimeParseException("data futura", nascimentoValue, 0);
        } catch (DateTimeParseException e) {
            showError("Informe uma data de nascimento válida e não futura.");
            return;
        }
        try {
            repository.salvar(new Paciente(nomeValue, cpfValue, data, telefoneValue));
            JOptionPane.showMessageDialog(this, "Paciente cadastrado com sucesso.", "Cadastro concluído", JOptionPane.INFORMATION_MESSAGE);
            nome.setText(""); cpf.setValue(null); nascimento.setValue(null); telefone.setValue(null);
            nome.requestFocusInWindow();
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) showError("Já existe um paciente cadastrado com esse CPF.");
            else showError("Não foi possível cadastrar o paciente. Verifique a conexão com o banco de dados.");
        }
    }

    private String digits(String value) { return value.replaceAll("\\D", ""); }

    private boolean validCpf(String value) {
        if (value.length() != 11 || value.chars().distinct().count() == 1) return false;
        int sum = 0;
        for (int i = 0; i < 9; i++) sum += (value.charAt(i) - '0') * (10 - i);
        int digit = (sum * 10) % 11; if (digit == 10) digit = 0;
        if (digit != value.charAt(9) - '0') return false;
        sum = 0;
        for (int i = 0; i < 10; i++) sum += (value.charAt(i) - '0') * (11 - i);
        digit = (sum * 10) % 11; if (digit == 10) digit = 0;
        return digit == value.charAt(10) - '0';
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Dados inválidos", JOptionPane.WARNING_MESSAGE);
    }
}
