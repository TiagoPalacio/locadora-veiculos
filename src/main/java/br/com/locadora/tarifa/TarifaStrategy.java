package br.com.locadora.tarifa;

public interface TarifaStrategy {
    double calcular(double valorDiaria, int dias);
    String nome();
}
