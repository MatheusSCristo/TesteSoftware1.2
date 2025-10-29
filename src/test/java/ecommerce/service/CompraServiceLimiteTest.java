package ecommerce.service;

import ecommerce.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CompraServiceLimiteTest {

    private CompraService compraService;
    private Regiao regiaoPadrao;
    private TipoCliente clientePadrao;

    @BeforeEach
    void setUp() {
        compraService = new CompraService(null, null, null, null);
        regiaoPadrao = Regiao.SUDESTE;
        clientePadrao = TipoCliente.BRONZE;
    }

    @Test
    @DisplayName("AVL1: Qtd Tipo = 2 (Limite P1.1) - Sem desconto")
    void calcularCustoTotal_limite_qtdTipo_2() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "100.00", 2L, TipoProduto.LIVRO, "1.0", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, regiaoPadrao, clientePadrao);
        assertThat(total).as("Subtotal 200 (sem desc tipo)").isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("AVL1: Qtd Tipo = 3 (Limite P1.2) - Desconto 5%")
    void calcularCustoTotal_limite_qtdTipo_3() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "100.00", 3L, TipoProduto.LIVRO, "1.0", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, regiaoPadrao, clientePadrao);
        assertThat(total).as("Subtotal 300 * 0.95 = 285").isEqualByComparingTo("285.00");
    }

    @Test
    @DisplayName("AVL1: Qtd Tipo = 4 (Limite P1.2) - Desconto 5%")
    void calcularCustoTotal_limite_qtdTipo_4() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "100.00", 4L, TipoProduto.LIVRO, "1.0", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, regiaoPadrao, clientePadrao);
        assertThat(total).as("Subtotal 400 * 0.95 = 380").isEqualByComparingTo("380.00");
    }

    @Test
    @DisplayName("AVL1: Qtd Tipo = 5 (Limite P1.3) - Desconto 10%")
    void calcularCustoTotal_limite_qtdTipo_5() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "100.00", 5L, TipoProduto.LIVRO, "1.0", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, regiaoPadrao, clientePadrao);
        assertThat(total).as("Subtotal 500 * 0.90 = 450").isEqualByComparingTo("450.00");
    }

    @Test
    @DisplayName("AVL1: Qtd Tipo = 7 (Limite P1.3) - Desconto 10%")
    void calcularCustoTotal_limite_qtdTipo_7() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "100.00", 7L, TipoProduto.LIVRO, "1.0", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, regiaoPadrao, clientePadrao);
        assertThat(total).as("Subtotal (700 * 0.90) = 630. Desc Valor (630 * 0.90) = 567 + Frete(26,00)").isEqualByComparingTo("593.00");
    }

    @Test
    @DisplayName("AVL1: Qtd Tipo = 8 (Limite P1.4) - Desconto 15%")
    void calcularCustoTotal_limite_qtdTipo_8() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "100.00", 8L, TipoProduto.LIVRO, "1.0", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, regiaoPadrao, clientePadrao);
        assertThat(total).as("Subtotal (800 * 0.85) = 680. Desc Valor (680 * 0.90) = 612 + Frete(28,00)").isEqualByComparingTo("640.00");
    }

    @Test
@DisplayName("AVL2: Subtotal = 500.00 (Limite P2.1) - Sem desconto valor")
    void calcularCustoTotal_limite_subtotal_500_00() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "500.00", 1L, TipoProduto.ELETRONICO, "1.0", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, regiaoPadrao, clientePadrao);
        assertThat(total).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("AVL2: Subtotal = 500.01 (Limite P2.2) - Desconto 10%")
    void calcularCustoTotal_limite_subtotal_500_01() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "500.01", 1L, TipoProduto.ELETRONICO, "1.0", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, regiaoPadrao, clientePadrao);
        assertThat(total).as("500.01 * 0.90 = 450.009 -> 450.01").isEqualByComparingTo("450.01");
    }

    @Test
    @DisplayName("AVL2: Subtotal = 1000.00 (Limite P2.2) - Desconto 10%")
    void calcularCustoTotal_limite_subtotal_1000_00() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "1000.00", 1L, TipoProduto.ELETRONICO, "1.0", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, regiaoPadrao, clientePadrao);
        assertThat(total).as("1000.00 * 0.90 = 900.00").isEqualByComparingTo("900.00");
    }

    @Test
    @DisplayName("AVL2: Subtotal = 1000.01 (Limite P2.3) - Desconto 20%")
    void calcularCustoTotal_limite_subtotal_1000_01() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "1000.01", 1L, TipoProduto.ELETRONICO, "1.0", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, regiaoPadrao, clientePadrao);
        assertThat(total).as("1000.01 * 0.80 = 800.008 -> 800.01").isEqualByComparingTo("800.01");
    }

    @Test
    @DisplayName("AVL3: Peso = 5.00 kg (Limite P3.1) - Isento")
    void calcularCustoTotal_limite_peso_5_00() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "100.00", 1L, TipoProduto.MOVEL, "5.00", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, regiaoPadrao, clientePadrao);
        assertThat(total).as("Subtotal 100, Frete 0 (Isento)").isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("AVL3: Peso = 5.01 kg (Limite P3.2) - Faixa B")
    void calcularCustoTotal_limite_peso_5_01() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "100.00", 1L, TipoProduto.MOVEL, "5.01", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, regiaoPadrao, clientePadrao);
        assertThat(total).isEqualByComparingTo("122.02");
    }

    @Test
    @DisplayName("AVL3: Peso = 10.00 kg (Limite P3.2) - Faixa B")
    void calcularCustoTotal_limite_peso_10_00() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "100.00", 1L, TipoProduto.MOVEL, "10.00", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, regiaoPadrao, clientePadrao);
        assertThat(total).isEqualByComparingTo("132.00");
    }

    @Test
    @DisplayName("AVL3: Peso = 10.01 kg (Limite P3.3) - Faixa C")
    void calcularCustoTotal_limite_peso_10_01() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "100.00", 1L, TipoProduto.MOVEL, "10.01", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, regiaoPadrao, clientePadrao);
        assertThat(total).isEqualByComparingTo("152.04");
    }

    @Test
    @DisplayName("AVL3: Peso = 50.00 kg (Limite P3.3) - Faixa C")
    void calcularCustoTotal_limite_peso_50_00() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "100.00", 1L, TipoProduto.MOVEL, "50.00", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, regiaoPadrao, clientePadrao);
        assertThat(total).isEqualByComparingTo("312.00");
    }

    @Test
    @DisplayName("AVL3: Peso = 50.01 kg (Limite P3.4) - Faixa D")
    void calcularCustoTotal_limite_peso_50_01() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "100.00", 1L, TipoProduto.MOVEL, "50.01", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, regiaoPadrao, clientePadrao);
        assertThat(total).isEqualByComparingTo("462.07");
    }

    private ItemCompra criarItem(String nome, String preco, long qtd, TipoProduto tipo, String pesoKg, boolean fragil) {
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