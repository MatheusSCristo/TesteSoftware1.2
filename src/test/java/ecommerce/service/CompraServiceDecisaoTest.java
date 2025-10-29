package ecommerce.service;

import ecommerce.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class CompraServiceDecisaoTest {

    private CompraService compraService;

    @BeforeEach
    void setUp() {
        compraService = new CompraService(null, null, null, null);
    }

    @ParameterizedTest(name = "[{index}] {4}")
    @MethodSource("cenariosTabelaDecisao")
    @DisplayName("Deve calcular o custo total corretamente para regras de decisão")
    void calcularCustoTotal_regrasDeDecisao(
            List<ItemCompra> itens,
            Regiao regiao,
            TipoCliente tipoCliente,
            String totalEsperado,
            String nomeCenario) {

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(itens);

        BigDecimal totalCalculado = compraService.calcularCustoTotal(carrinho, regiao, tipoCliente);

        assertThat(totalCalculado)
                .as("Cenário: " + nomeCenario)
                .isEqualByComparingTo(totalEsperado);
    }

    static Stream<Arguments> cenariosTabelaDecisao() {
        return Stream.of(
                arguments(
                        List.of(criarItem("Item R1", "100.00", 1L, TipoProduto.LIVRO, "4.00", false)),
                        Regiao.SUDESTE, TipoCliente.BRONZE,
                        "100.00",
                        "TD_R1: Desc 0%, Frete Isento"
                ),

                arguments(
                        List.of(criarItem("Item R2", "600.00", 1L, TipoProduto.LIVRO, "4.00", false)),
                        Regiao.SUDESTE, TipoCliente.BRONZE,
                        "540.00",
                        "TD_R2: Desc 10% (Valor), Frete Isento"
                ),

                arguments(
                        List.of(criarItem("Item R3", "1100.00", 1L, TipoProduto.LIVRO, "4.00", false)),
                        Regiao.SUDESTE, TipoCliente.BRONZE,
                        "880.00",
                        "TD_R3: Desc 20% (Valor), Frete Isento"
                ),

                arguments(
                        List.of(criarItem("Item R5", "150.00", 4L, TipoProduto.ROUPA, "1.0", false)),
                        Regiao.SUDESTE, TipoCliente.BRONZE,
                        "513.00",
                        "TD_R5: Desc 5% (Tipo) + 10% (Valor)"
                ),

                arguments(
                        List.of(criarItem("Item R6", "300.00", 4L, TipoProduto.ROUPA, "1.0", false)),
                        Regiao.SUDESTE, TipoCliente.BRONZE,
                        "912.00",
                        "TD_R6: Desc 5% (Tipo) + 20% (Valor)"
                ),

                arguments(
                        List.of(criarItem("Item R10", "50.00", 2L, TipoProduto.ELETRONICO, "1.0", true)),
                        Regiao.SUDESTE, TipoCliente.BRONZE,
                        "110.00",
                        "TD_R10: Frete Faixa A (Isento taxa min) + Frágil"
                ),

                arguments(
                        List.of(criarItem("Item R11", "100.00", 1L, TipoProduto.MOVEL, "7.0", false)),
                        Regiao.SUDESTE, TipoCliente.BRONZE,
                        "126.00",
                        "TD_R11: Frete Faixa B, Bronze"
                ),

                arguments(
                        List.of(criarItem("Item R12", "100.00", 1L, TipoProduto.MOVEL, "7.0", true)),
                        Regiao.SUL, TipoCliente.PRATA,
                        "116.28",
                        "TD_R12: Frete Faixa B + Fragil + Regiao Sul + Prata"
                ),

                arguments(
                        List.of(criarItem("Item R13", "200.00", 1L, TipoProduto.MOVEL, "15.0", false)),
                        Regiao.NORTE, TipoCliente.OURO,
                        "200.00",
                        "TD_R13: Frete Faixa C + Regiao Norte + Ouro (Frete Zero)"
                ),

                arguments(
                        List.of(criarItem("Item R14", "100.00", 2L, TipoProduto.MOVEL, "30.0", true)), // Peso total 60kg
                        Regiao.CENTRO_OESTE, TipoCliente.BRONZE
                        , "730.40",
                        "TD_R14: Frete Faixa D + Fragil + Regiao CO + Bronze"
                )
        );
    }


    private static ItemCompra criarItem(String nome, String preco, long qtd, TipoProduto tipo, String pesoKg, boolean fragil) {
        Produto p = new Produto();
        p.setNome(nome);
        p.setPreco(new BigDecimal(preco));
        p.setTipo(tipo);
        p.setFragil(fragil);
        p.setPesoFisico(new BigDecimal(pesoKg));
        p.setComprimento(BigDecimal.ZERO);
        p.setLargura(BigDecimal.ZERO);
        p.setAltura(BigDecimal.ZERO);
        return new ItemCompra(null, p, qtd);
    }
}