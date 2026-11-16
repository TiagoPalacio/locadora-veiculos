package br.com.locadora.tarifa;

public class TarifaSemanal implements TarifaStrategy {

    @Override
    public double calcular(double valorDiaria, int dias) {
        int diasPagos = dias - (dias / 7);
        return valorDiaria * diasPagos;
    }

    @Override
    public String nome() {
        return "Pacote semanal (1 dia gratis a cada 7)";
    }
}
