package br.com.locadora;

import br.com.locadora.adicional.CadeirinhaDecorator;
import br.com.locadora.adicional.Cobranca;
import br.com.locadora.adicional.GpsDecorator;
import br.com.locadora.adicional.SeguroDecorator;
import br.com.locadora.modelo.Aluguel;
import br.com.locadora.modelo.Cliente;
import br.com.locadora.modelo.Veiculo;
import br.com.locadora.servico.Locadora;
import br.com.locadora.tarifa.TarifaDiaria;
import br.com.locadora.tarifa.TarifaLongoPrazo;
import br.com.locadora.tarifa.TarifaSemanal;
import br.com.locadora.tarifa.TarifaStrategy;

import java.util.List;
import java.util.Scanner;

/** Menu de console da locadora e modo demonstracao (--demo). */
public class Main {

    private static final Locadora locadora = new Locadora();
    private static Scanner in;

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--demo")) {
            demo();
            return;
        }
        in = new Scanner(System.in);
        menu();
    }

    // ---------------------------------------------------------------- menu

    private static void menu() {
        while (true) {
            System.out.println();
            System.out.println("=== LOCADORA DE VEICULOS ===");
            System.out.println("1 - Listar veiculos");
            System.out.println("2 - Cadastrar cliente");
            System.out.println("3 - Novo aluguel");
            System.out.println("4 - Devolver veiculo");
            System.out.println("0 - Sair");
            Integer opcao = lerInt("Opcao: ");
            if (opcao == null) {
                return; // fim da entrada
            }
            try {
                switch (opcao) {
                    case 1: listarVeiculos(); break;
                    case 2: cadastrarCliente(); break;
                    case 3: novoAluguel(); break;
                    case 4: devolverVeiculo(); break;
                    case 0: System.out.println("Ate logo!"); return;
                    default: System.out.println("Opcao invalida.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Erro: " + e.getMessage());
            } catch (EntradaEncerrada e) {
                return;
            }
        }
    }

    private static void listarVeiculos() {
        List<Veiculo> veiculos = locadora.getVeiculos();
        for (int i = 0; i < veiculos.size(); i++) {
            System.out.println((i + 1) + " - " + veiculos.get(i));
        }
    }

    private static void cadastrarCliente() {
        String nome = lerLinha("Nome: ");
        String cpf = lerLinha("CPF: ");
        Cliente c = locadora.cadastrarCliente(nome, cpf);
        System.out.println("Cliente cadastrado: " + c);
    }

    private static void novoAluguel() {
        List<Cliente> clientes = locadora.getClientes();
        if (clientes.isEmpty()) {
            System.out.println("Nenhum cliente cadastrado.");
            return;
        }
        for (int i = 0; i < clientes.size(); i++) {
            System.out.println((i + 1) + " - " + clientes.get(i));
        }
        Cliente cliente = escolher(clientes, "Cliente (numero): ");

        listarVeiculos();
        Veiculo veiculo = escolher(locadora.getVeiculos(), "Veiculo (numero): ");
        if (!veiculo.isDisponivel()) {
            System.out.println("Veiculo indisponivel.");
            return;
        }

        int dias = lerInt("Quantidade de dias: ", true);
        if (dias <= 0) {
            System.out.println("A quantidade de dias deve ser maior que zero.");
            return;
        }

        System.out.println("Tarifa: 1 - Diaria padrao | 2 - Pacote semanal | 3 - Longo prazo");
        TarifaStrategy tarifa;
        switch (lerInt("Escolha: ", true)) {
            case 1: tarifa = new TarifaDiaria(); break;
            case 2: tarifa = new TarifaSemanal(); break;
            case 3: tarifa = new TarifaLongoPrazo(); break;
            default:
                System.out.println("Tarifa invalida.");
                return;
        }

        Aluguel aluguel = locadora.alugar(cliente, veiculo, dias, tarifa);

        Cobranca cobranca = aluguel.getCobranca();
        if (confirmar("Adicionar seguro total? (s/n): ")) {
            cobranca = new SeguroDecorator(cobranca, dias);
        }
        if (confirmar("Adicionar GPS? (s/n): ")) {
            cobranca = new GpsDecorator(cobranca, dias);
        }
        if (confirmar("Adicionar cadeirinha infantil? (s/n): ")) {
            cobranca = new CadeirinhaDecorator(cobranca);
        }
        aluguel.setCobranca(cobranca);

        imprimirRecibo(aluguel);
    }

    private static void devolverVeiculo() {
        List<Aluguel> ativos = locadora.getAlugueisAtivos();
        if (ativos.isEmpty()) {
            System.out.println("Nenhum aluguel ativo.");
            return;
        }
        for (int i = 0; i < ativos.size(); i++) {
            Aluguel a = ativos.get(i);
            System.out.println((i + 1) + " - " + a.getCliente().getNome() + " / " + a.getVeiculo().getModelo()
                    + " (" + a.getVeiculo().getPlaca() + ")");
        }
        Aluguel aluguel = escolher(ativos, "Aluguel (numero): ");
        locadora.devolver(aluguel);
        System.out.println("Veiculo devolvido: " + aluguel.getVeiculo());
    }

    // -------------------------------------------------------------- recibo

    private static void imprimirRecibo(Aluguel aluguel) {
        Cobranca c = aluguel.getCobranca();
        System.out.println();
        System.out.println("----- RECIBO -----");
        System.out.println("Cliente: " + aluguel.getCliente());
        System.out.println(c.descricao());
        System.out.println(String.format("TOTAL: R$ %.2f", c.valor()));
        System.out.println("------------------");
    }

    // ---------------------------------------------------------------- demo

    /** Fluxo completo sem digitar nada: Ana + Corolla, 8 dias, semanal, seguro e GPS. */
    private static void demo() {
        Cliente cliente = locadora.getClientes().get(0);
        Veiculo veiculo = locadora.getVeiculos().get(1);
        int dias = 8;

        Aluguel aluguel = locadora.alugar(cliente, veiculo, dias, new TarifaSemanal());
        Cobranca cobranca = aluguel.getCobranca();
        cobranca = new SeguroDecorator(cobranca, dias);
        cobranca = new GpsDecorator(cobranca, dias);
        aluguel.setCobranca(cobranca);

        imprimirRecibo(aluguel);
        locadora.devolver(aluguel);
        System.out.println("Veiculo devolvido: " + veiculo);
    }

    // ------------------------------------------------------------- entrada

    /** Lancada quando a entrada padrao acaba (Ctrl+D / fim de arquivo). */
    private static class EntradaEncerrada extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private static String lerLinha(String prompt) {
        System.out.print(prompt);
        if (!in.hasNextLine()) {
            throw new EntradaEncerrada();
        }
        return in.nextLine().trim();
    }

    /** Le um inteiro; devolve null se a entrada acabar. Repete se nao for numero. */
    private static Integer lerInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!in.hasNextLine()) {
                return null;
            }
            String s = in.nextLine().trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Digite um numero valido.");
            }
        }
    }

    private static int lerInt(String prompt, boolean obrigatorio) {
        Integer v = lerInt(prompt);
        if (v == null) {
            throw new EntradaEncerrada();
        }
        return v;
    }

    private static boolean confirmar(String prompt) {
        while (true) {
            String s = lerLinha(prompt).toLowerCase();
            if (s.equals("s") || s.equals("sim")) return true;
            if (s.equals("n") || s.equals("nao") || s.equals("não")) return false;
            System.out.println("Responda s ou n.");
        }
    }

    private static <T> T escolher(List<T> lista, String prompt) {
        int n = lerInt(prompt, true);
        if (n < 1 || n > lista.size()) {
            throw new IllegalArgumentException("Numero fora da lista.");
        }
        return lista.get(n - 1);
    }
}
