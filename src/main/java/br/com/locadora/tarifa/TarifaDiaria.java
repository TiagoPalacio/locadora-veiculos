package br.com.locadora.tarifa;

public class TarifaDiaria implements TarifaStrategy {

    @Override
    public double calcular(double valorDiaria, int dias) {
        return valorDiaria * dias;
    }

    @Override
    public String nome() {
        return "Diaria padrao";
    }
}
