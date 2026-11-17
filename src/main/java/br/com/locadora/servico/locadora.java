package br.com.locadora.servico;

import br.com.locadora.modelo.Aluguel;
import br.com.locadora.modelo.Cliente;
import br.com.locadora.modelo.Veiculo;
import br.com.locadora.tarifa.TarifaStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Guarda frota, clientes e alugueis em memoria e coordena
 * a abertura de alugueis e a devolucao de veiculos.
 */
public class Locadora {

    private final List<Veiculo> veiculos = new ArrayList<>();
    private final List<Cliente> clientes = new ArrayList<>();
    private final List<Aluguel> alugueisAtivos = new ArrayList<>();

    /** Cria a locadora ja com alguns veiculos e clientes de exemplo. */
    public Locadora() {
        veiculos.add(new Veiculo("ABC1D23", "Fiat Mobi", 120.0));
        veiculos.add(new Veiculo("XYZ9K87", "Toyota Corolla", 250.0));
        veiculos.add(new Veiculo("QWE4R56", "Jeep Compass", 320.0));

        clientes.add(new Cliente("Ana Souza", "111.111.111-11"));
        clientes.add(new Cliente("Carlos Lima", "222.222.222-22"));
    }

    public List<Veiculo> getVeiculos() {
        return Collections.unmodifiableList(veiculos);
    }

    public List<Cliente> getClientes() {
        return Collections.unmodifiableList(clientes);
    }

    public List<Aluguel> getAlugueisAtivos() {
        return Collections.unmodifiableList(alugueisAtivos);
    }

    public Cliente cadastrarCliente(String nome, String cpf) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome nao pode ser vazio.");
        }
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF nao pode ser vazio.");
        }
        String cpfLimpo = cpf.trim();
        for (Cliente c : clientes) {
            if (c.getCpf().equals(cpfLimpo)) {
                throw new IllegalArgumentException("Ja existe cliente com o CPF " + cpfLimpo + ".");
            }
        }
        Cliente novo = new Cliente(nome.trim(), cpfLimpo);
        clientes.add(novo);
        return novo;
    }

    /**
     * Abre um aluguel e marca o veiculo como indisponivel.
     *
     * @throws IllegalStateException    se o veiculo ja estiver alugado
     * @throws IllegalArgumentException se os dados forem invalidos
     */
    public Aluguel alugar(Cliente cliente, Veiculo veiculo, int dias, TarifaStrategy tarifa) {
        if (cliente == null || veiculo == null || tarifa == null) {
            throw new IllegalArgumentException("Cliente, veiculo e tarifa sao obrigatorios.");
        }
        if (dias <= 0) {
            throw new IllegalArgumentException("A quantidade de dias deve ser maior que zero.");
        }
        if (!veiculo.isDisponivel()) {
            throw new IllegalStateException("Veiculo indisponivel: " + veiculo.getModelo());
        }
        Aluguel aluguel = new Aluguel(cliente, veiculo, dias, tarifa);
        veiculo.setDisponivel(false);
        alugueisAtivos.add(aluguel);
        return aluguel;
    }

    /** Encerra o aluguel e devolve o veiculo a frota. */
    public void devolver(Aluguel aluguel) {
        if (aluguel == null || !alugueisAtivos.remove(aluguel)) {
            throw new IllegalStateException("Aluguel nao esta ativo.");
        }
        aluguel.getVeiculo().setDisponivel(true);
    }
}
