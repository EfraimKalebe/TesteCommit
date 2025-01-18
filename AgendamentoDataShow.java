import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class AgendamentoDataShow {
    private static Connection conexao;

    public static void main(String[] args) {
        // Conectar ao banco de dados
        conectarBanco();

        // Criar janela principal
        JFrame frame = new JFrame("Agendamento de Data Show");
        frame.setSize(600, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Layout principal
        JPanel panel = new JPanel(new BorderLayout());

        // Formulário para agendamento
        JPanel formPanel = new JPanel(new GridLayout(6, 2));
        JTextField txtProfessor = new JTextField();
        JTextField txtData = new JTextField();
        JTextField txtHoraInicio = new JTextField();
        JTextField txtHoraFim = new JTextField();

        JLabel lblProfessor = new JLabel("Professor:");
        JLabel lblData = new JLabel("Data (AAAA-MM-DD):");
        JLabel lblHoraInicio = new JLabel("Horário Início (HH:MM):");
        JLabel lblHoraFim = new JLabel("Horário Fim (HH:MM):");

        JButton btnAgendar = new JButton("Agendar");
        JLabel lblMensagem = new JLabel("");

        // Adicionar ao painel do formulário
        formPanel.add(lblProfessor);
        formPanel.add(txtProfessor);
        formPanel.add(lblData);
        formPanel.add(txtData);
        formPanel.add(lblHoraInicio);
        formPanel.add(txtHoraInicio);
        formPanel.add(lblHoraFim);
        formPanel.add(txtHoraFim);
        formPanel.add(btnAgendar);
        formPanel.add(lblMensagem);

        // Tabela para exibir agendamentos
        String[] colunas = {"ID", "Professor", "Data", "Hora Início", "Hora Fim", "Data Show"};
        JTable tabelaAgendamentos = new JTable(new Object[0][6], colunas);
        JScrollPane scrollPane = new JScrollPane(tabelaAgendamentos);

        // Adicionar componentes à janela principal
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        frame.add(panel);

        frame.setVisible(true);

        // Atualizar tabela com agendamentos existentes
        atualizarTabela(tabelaAgendamentos);

        // Ação do botão Agendar
        btnAgendar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String professor = txtProfessor.getText();
                String data = txtData.getText();
                String horaInicio = txtHoraInicio.getText();
                String horaFim = txtHoraFim.getText();

                if (agendarDataShow(professor, data, horaInicio, horaFim)) {
                    lblMensagem.setText("Agendamento realizado!");
                    atualizarTabela(tabelaAgendamentos);
                } else {
                    lblMensagem.setText("Data Show não disponível!");
                }
            }
        });
    }

    // Conectar ao banco de dados SQLite
    private static void conectarBanco() {
        try {
            // Carregar o driver JDBC explicitamente (se necessário)
            Class.forName("org.sqlite.JDBC");
            
            conexao = DriverManager.getConnection("jdbc:sqlite:agendamentos.db");
            
            if (conexao != null) {
                System.out.println("Conexão estabelecida com sucesso!");
            }
            
            Statement stmt = conexao.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS agendamentos (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "professor TEXT NOT NULL, " +
                    "data TEXT NOT NULL, " +
                    "horario_inicio TEXT NOT NULL, " +
                    "horario_fim TEXT NOT NULL, " +
                    "data_show_id INTEGER NOT NULL)";
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC nao encontrado!");
            e.printStackTrace();
        }
    }
    
    // Função para testar o Banco de Dados SQl 
    public class TesteConexao {
        public static void main(String[] args) {
            String url = "jdbc:sqlite:agendamentos.db";
            try (Connection conexao = DriverManager.getConnection(url)) {
                System.out.println("Conexão bem-sucedida!");
            } catch (SQLException e) {
                System.err.println("Erro ao conectar ao banco de dados: " + e.getMessage());
            }
        }
    }

    // Função para agendar um Data Show
    private static boolean agendarDataShow(String professor, String data, String horaInicio, String horaFim) {
        try {
            // Verificar disponibilidade
            String sqlDisponivel = "SELECT * FROM agendamentos WHERE data = ? AND " +
                    "((? BETWEEN horario_inicio AND horario_fim) OR " +
                    "(? BETWEEN horario_inicio AND horario_fim))";
            PreparedStatement stmt = conexao.prepareStatement(sqlDisponivel);
            stmt.setString(1, data);
            stmt.setString(2, horaInicio);
            stmt.setString(3, horaFim);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return false; // Já está ocupado
            }

            // Inserir novo agendamento
            String sqlInserir = "INSERT INTO agendamentos (professor, data, horario_inicio, horario_fim, data_show_id) " +
                    "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmtInserir = conexao.prepareStatement(sqlInserir);
            stmtInserir.setString(1, professor);
            stmtInserir.setString(2, data);
            stmtInserir.setString(3, horaInicio);
            stmtInserir.setString(4, horaFim);
            stmtInserir.setInt(5, 1); // Data show ID fixo por enquanto (pode ser alternado)

            stmtInserir.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Atualizar a tabela de agendamentos
    private static void atualizarTabela(JTable tabela) {
        try {
            String sql = "SELECT * FROM agendamentos";
            PreparedStatement stmt = conexao.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            // Limpar tabela
            String[] colunas = {"ID", "Professor", "Data", "Hora Início", "Hora Fim", "Data Show"};
            Object[][] dados = new Object[100][6];
            int i = 0;

            while (rs.next()) {
                dados[i][0] = rs.getInt("id");
                dados[i][1] = rs.getString("professor");
                dados[i][2] = rs.getString("data");
                dados[i][3] = rs.getString("horario_inicio");
                dados[i][4] = rs.getString("horario_fim");
                dados[i][5] = rs.getInt("data_show_id");
                i++;
            }

            tabela.setModel(new javax.swing.table.DefaultTableModel(dados, colunas));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
