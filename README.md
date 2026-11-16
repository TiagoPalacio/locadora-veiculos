# Locadora de Veículos — Strategy + Decorator

Projeto final da disciplina de Design Patterns, **Tema 2: Locadora de veículos**.

## Descrição do sistema

Aplicação de console em Java para uma locadora de veículos. Ela permite:

- listar os veículos da frota e ver se estão disponíveis;
- cadastrar clientes;
- abrir um aluguel escolhendo cliente, veículo, quantidade de dias e **regra de tarifa**;
- incluir **adicionais** no aluguel (seguro, GPS, cadeirinha);
- emitir o recibo detalhado e devolver o veículo, que volta a ficar disponível.

Os dados ficam em memória (não há banco de dados). Alguns veículos e clientes já são carregados quando o programa inicia.

### Entidades principais

| Entidade | Classe | Papel |
|---|---|---|
| Veículo | `modelo.Veiculo` | placa, modelo, valor da diária e disponibilidade |
| Cliente | `modelo.Cliente` | nome e CPF |
| Aluguel | `modelo.Aluguel` | liga cliente e veículo, guarda dias, tarifa (Strategy) e cobrança (Decorator) |

A classe `servico.Locadora` guarda as listas em memória e coordena aluguel e devolução.

## Tecnologias e pré-requisitos

- Java **JDK 11 ou superior** (precisa do `javac`, não só do JRE)
- Apache Maven 3.6+ (opcional, dá para compilar só com o `javac`)
- Nenhuma dependência externa

## Como executar

### Com Maven

```bash
mvn clean package
java -jar target/locadora-veiculos-1.0.0.jar
```

### Sem Maven (apenas JDK)

Linux/macOS/Git Bash:

```bash
javac -d out $(find src/main/java -name "*.java")
java -cp out br.com.locadora.Main
```

Windows PowerShell:

```powershell
javac -d out (Get-ChildItem -Recurse src/main/java -Filter *.java).FullName
java -cp out br.com.locadora.Main
```

### Modo demonstração

Para rodar o fluxo completo sem digitar nada, passe `--demo`:

```bash
java -jar target/locadora-veiculos-1.0.0.jar --demo
```

## Exemplos de uso

### Saída do `--demo`

Toyota Corolla (R$ 250/dia), 8 dias, pacote semanal, com seguro e GPS:

```
----- RECIBO -----
Cliente: Ana Souza (CPF 111.111.111-11)
Toyota Corolla por 8 dia(s) [Pacote semanal (1 dia gratis a cada 7)]: R$ 1750,00
  + Seguro total (R$ 35/dia): R$ 280,00
  + GPS (R$ 10/dia): R$ 80,00
TOTAL: R$ 2110,00
------------------
Veiculo devolvido: Toyota Corolla (XYZ9K87) - R$ 250,00/dia - disponivel
```

### Fluxo interativo

```
=== LOCADORA DE VEICULOS ===
1 - Listar veiculos
2 - Cadastrar cliente
3 - Novo aluguel
4 - Devolver veiculo
0 - Sair
Opcao: 3
1 - Ana Souza (CPF 111.111.111-11)
2 - Carlos Lima (CPF 222.222.222-22)
Cliente (numero): 2
1 - Fiat Mobi (ABC1D23) - R$ 120,00/dia - disponivel
2 - Toyota Corolla (XYZ9K87) - R$ 250,00/dia - disponivel
3 - Jeep Compass (QWE4R56) - R$ 320,00/dia - disponivel
Veiculo (numero): 1
Quantidade de dias: 15
Tarifa: 1 - Diaria padrao | 2 - Pacote semanal | 3 - Longo prazo
Escolha: 3
Adicionar seguro total? (s/n): n
Adicionar GPS? (s/n): n
Adicionar cadeirinha infantil? (s/n): s

----- RECIBO -----
Cliente: Carlos Lima (CPF 222.222.222-22)
Fiat Mobi por 15 dia(s) [Longo prazo (20% off a partir de 15 dias)]: R$ 1440,00
  + Cadeirinha infantil (taxa unica R$ 50): R$ 50,00
TOTAL: R$ 1490,00
------------------
```

(O separador decimal segue a localidade da máquina: vírgula em pt-BR, ponto em en-US.)

## Patterns utilizados

### 1. Strategy — regras de tarifa

**Problema:** a locadora tem várias formas de calcular o valor base do aluguel (diária comum, pacote semanal, desconto de longo prazo). Com `if/else` dentro de `Aluguel`, cada regra nova obrigaria a mexer na classe e ela cresceria sem parar.

**Solução:** cada regra vira uma classe que implementa a mesma interface. O `Aluguel` recebe a estratégia pronta e só chama `calcular(...)`, sem saber qual regra está usando.

| Papel | Classe |
|---|---|
| Strategy (interface) | `tarifa.TarifaStrategy` |
| Estratégias concretas | `tarifa.TarifaDiaria`, `tarifa.TarifaSemanal`, `tarifa.TarifaLongoPrazo` |
| Contexto | `modelo.Aluguel` (usado por `adicional.CobrancaBase`) |

**Como estender:** criar uma classe nova que implementa `TarifaStrategy` e incluí-la na opção do menu em `Main.novoAluguel()`.

### 2. Decorator — adicionais do aluguel

**Problema:** o cliente pode escolher qualquer combinação de adicionais (seguro, GPS, cadeirinha). Fazer uma subclasse para cada combinação geraria uma explosão de classes, e campos booleanos em `Aluguel` acoplariam preço e descrição de todos os adicionais numa classe só.

**Solução:** a cobrança é um objeto `Cobranca`. Cada adicional "embrulha" a cobrança anterior, soma o seu valor e acrescenta sua linha na descrição. Os adicionais podem ser empilhados em qualquer ordem e quantidade, em tempo de execução.

| Papel | Classe |
|---|---|
| Componente (interface) | `adicional.Cobranca` |
| Componente concreto | `adicional.CobrancaBase` (valor vindo da Strategy) |
| Decorator abstrato | `adicional.AdicionalDecorator` |
| Decorators concretos | `adicional.SeguroDecorator`, `adicional.GpsDecorator`, `adicional.CadeirinhaDecorator` |

```java
Cobranca c = aluguel.getCobranca();   // CobrancaBase
c = new SeguroDecorator(c, dias);     // base + seguro
c = new GpsDecorator(c, dias);        // base + seguro + GPS
aluguel.setCobranca(c);
```

**Como estender:** criar uma subclasse de `AdicionalDecorator` implementando `valorAdicional()` e `nomeAdicional()`, e oferecê-la em `Main.novoAluguel()`.

### Como os dois se conectam

`CobrancaBase` é o "miolo" do Decorator e usa a Strategy do aluguel para calcular o valor base. Os decorators somam os adicionais por cima. Assim, trocar a tarifa não afeta os adicionais, e vice-versa.

## Estrutura

```
src/main/java/br/com/locadora/
├── Main.java                 # menu de console e modo --demo
├── modelo/                   # Veiculo, Cliente, Aluguel
├── tarifa/                   # Strategy
├── adicional/                # Decorator
└── servico/Locadora.java     # armazenamento em memória
```

## Declaração de uso de IA

Usamos IA generativa na **Parte 1**, como o roteiro permite.

- **Modelo:** Claude Opus 5.5 (Anthropic), pelo Claude Code e Gemini Argon 4 (Google) pelo Antigravity IDE.
- **Onde foi usada:** geração da estrutura inicial do projeto, das classes Java (entidades, Strategy, Decorator e menu de console), do `pom.xml`, do `.gitignore` e do rascunho deste README.
- **O que a equipe fez:** revisão do código, testes de execução, ajustes, organização dos commits e publicação no GitHub. Todos os integrantes estudaram a implementação para explicá-la na arguição.

## Contribuição dos integrantes (equipe dona)

| Integrante | GitHub | Contribuição |
|---|---|---|
| Tiago Palácio | [TiagoPalacio](https://github.com/TiagoPalacio) | Criação do repositório, `pom.xml`, `.gitignore` e README |
| Bruno Martins | [brunotesckemartins](https://github.com/brunotesckemartins) | Entidades `Veiculo`, `Cliente` e `Aluguel` (`modelo/`) |
| Gabriel Tomé | [GabrielTME](https://github.com/GabrielTME) | Pattern Strategy: regras de tarifa (`tarifa/`) |
| Elisson Miller | [elison4](https://github.com/elison4) | Pattern Decorator: adicionais (`adicional/`) |
| Guilherme Cesarino | [GuilhermeCesarino](https://github.com/GuilhermeCesarino) | Armazenamento em memória (`servico/Locadora`) e menu de console (`Main`) |

---

## Parte 2 — Extensão (equipe extensora)

_Seção a ser preenchida pela equipe extensora no PR: nova regra de tarifa (Strategy), novo adicional (Decorator), decisões e contribuições._
