package br.com.locadora.tarifa;

public class TarifaLongoPrazo implements TarifaStrategy {

    @Override
    public double calcular(double valorDiaria, int dias) {
        double total = valorDiaria * dias;
        if (dias >= 15) {
            return total * 0.8;
        }
        return total;
    }

    @Override
    public String nome() {
        return "Longo prazo (20% off a partir de 15 dias)";
    }
}
