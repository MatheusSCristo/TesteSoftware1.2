package ecommerce.service;

import ecommerce.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CompraServiceParticaoTest {

    private CompraService compraService;

    @BeforeEach
    void setUp() {
        compraService = new CompraService(null, null, null, null);
    }

    @Test
    @DisplayName("P-Inv1: Deve lançar exceção para carrinho nulo")
    void calcularCustoTotal_particaoInvalida_carrinhoNulo() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            compraService.calcularCustoTotal(null, Regiao.SUDESTE, TipoCliente.BRONZE);
        });
        assertThat(ex.getMessage()).isEqualTo("Carrinho ou lista de itens não pode ser nulo.");
    }

    @Test
    @DisplayName("P-Inv2: Deve lançar exceção para lista de itens nula")
    void calcularCustoTotal_particaoInvalida_itensNulos() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        });
        assertThat(ex.getMessage()).isEqualTo("Carrinho ou lista de itens não pode ser nulo.");
    }

    @Test
    @DisplayName("P-Inv3: Deve lançar exceção para região nula")
    void calcularCustoTotal_particaoInvalida_regiaoNula() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(criarItem("Item", "10", 1L, TipoProduto.LIVRO, "1", false)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            compraService.calcularCustoTotal(carrinho, null, TipoCliente.BRONZE);
        });
        assertThat(ex.getMessage()).isEqualTo("Região e Tipo de Cliente não podem ser nulos.");
    }

    @Test
    @DisplayName("P-Inv4: Deve lançar exceção para tipo de cliente nulo")
    void calcularCustoTotal_particaoInvalida_tipoClienteNulo() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(criarItem("Item", "10", 1L, TipoProduto.LIVRO, "1", false)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, null);
        });
        assertThat(ex.getMessage()).isEqualTo("Região e Tipo de Cliente não podem ser nulos.");
    }

    @Test
    @DisplayName("P-Inv5: Deve retornar R$ 0.00 para carrinho vazio")
    void calcularCustoTotal_particaoValida_carrinhoVazio() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(Collections.emptyList());
        BigDecimal total = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("P1.1: Qtd Tipo [0-2] - Sem desconto de tipo")
    void calcularCustoTotal_particao_qtdTipo_2_semDesconto() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "100.00", 2L, TipoProduto.LIVRO, "1.0", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).as("Subtotal 200 (sem desc tipo), Frete 0").isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("P1.2: Qtd Tipo [3-4] - Desconto 5%")
    void calcularCustoTotal_particao_qtdTipo_3_desconto5pct() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "100.00", 3L, TipoProduto.LIVRO, "1.0", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).as("Subtotal 300 * 0.95 = 285, Frete 0").isEqualByComparingTo("285.00");
    }

    @Test
    @DisplayName("P1.3: Qtd Tipo [5-7] - Desconto 10%")
    void calcularCustoTotal_particao_qtdTipo_5_desconto10pct() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "100.00", 5L, TipoProduto.LIVRO, "1.0", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).as("Subtotal 500 * 0.90 = 450, Frete 0").isEqualByComparingTo("450.00");
    }

    @Test
    @DisplayName("P1.4: Qtd Tipo [8+] - Desconto 15%")
    void calcularCustoTotal_particao_qtdTipo_8_desconto15pct() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "100.00", 8L, TipoProduto.LIVRO, "1.0", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).as("Subtotal (800 * 0.85) = 680. Desc Valor (680 * 0.90) = 612. Frete 28").isEqualByComparingTo("640.00");
    }

    @Test
    @DisplayName("P2.2: Subtotal (500-1000] - Desconto 10%")
    void calcularCustoTotal_particao_subtotal_600_desconto10pct() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "600.00", 1L, TipoProduto.ELETRONICO, "1.0", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).as("Subtotal 600 * 0.90 = 540, Frete 0").isEqualByComparingTo("540.00");
    }

    @Test
    @DisplayName("P2.3: Subtotal (> 1000) - Desconto 20%")
    void calcularCustoTotal_particao_subtotal_1100_desconto20pct() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "1100.00", 1L, TipoProduto.ELETRONICO, "1.0", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).as("Subtotal 1100 * 0.80 = 880, Frete 0").isEqualByComparingTo("880.00");
    }

    @Test
    @DisplayName("P3.2: Peso Faixa B (5-10kg)")
    void calcularCustoTotal_particao_peso_6kg_faixaB() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "100.00", 1L, TipoProduto.MOVEL, "6.0", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("124.00");
    }

    @Test
    @DisplayName("P3.3: Peso Faixa C (10-50kg)")
    void calcularCustoTotal_particao_peso_11kg_faixaC() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "100.00", 1L, TipoProduto.MOVEL, "11.0", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("156.00");
    }

    @Test
    @DisplayName("P3.4: Peso Faixa D (> 50kg)")
    void calcularCustoTotal_particao_peso_51kg_faixaD() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "100.00", 1L, TipoProduto.MOVEL, "51.0", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("469.00");
    }

    @Test
    @DisplayName("P4.2: Item Frágil")
    void calcularCustoTotal_particao_fragil() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "100.00", 1L, TipoProduto.ELETRONICO, "6.0", true)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("129.00");
    }

    @Test
    @DisplayName("P5.2: Peso Cúbico > Peso Físico")
    void calcularCustoTotal_particao_pesoCubico() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItemPesoCubico("Item Volumoso", "100.00", 1L, TipoProduto.MOVEL,
                        "1.0", "40", "40", "40", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("154.67");
    }


    @Test
    @DisplayName("P6.3: Região Nordeste")
    void calcularCustoTotal_particao_regiaoNordeste() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "100.00", 1L, TipoProduto.MOVEL, "6.0", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, Regiao.NORDESTE, TipoCliente.BRONZE);
        assertThat(total).isEqualByComparingTo("126.40");
    }

    @Test
    @DisplayName("P7.2: Cliente Prata")
    void calcularCustoTotal_particao_clientePrata() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "100.00", 1L, TipoProduto.MOVEL, "6.0", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.PRATA);
        assertThat(total).isEqualByComparingTo("112.00");
    }

    @Test
    @DisplayName("P7.3: Cliente Ouro")
    void calcularCustoTotal_particao_clienteOuro() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item A", "100.00", 1L, TipoProduto.MOVEL, "6.0", false)
        ));
        BigDecimal total = compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.OURO);
        assertThat(total).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("P-Inv6: Deve lançar exceção para item com quantidade zero")
    void calcularCustoTotal_particaoInvalida_quantidadeZero() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item Qtd Zero", "100.00", 0L, TipoProduto.LIVRO, "1.0", false)
        ));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        });
        assertThat(ex.getMessage()).contains("Quantidade do item não pode ser zero ou negativa");
    }

    @Test
    @DisplayName("P-Inv6: Deve lançar exceção para item com quantidade negativa")
    void calcularCustoTotal_particaoInvalida_quantidadeNegativa() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item Qtd Negativa", "100.00", -1L, TipoProduto.LIVRO, "1.0", false)
        ));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        });
        assertThat(ex.getMessage()).contains("Quantidade do item não pode ser zero ou negativa");
    }

    @Test
    @DisplayName("P-Inv7: Deve lançar exceção para item com preço negativo")
    void calcularCustoTotal_particaoInvalida_precoNegativo() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(
                criarItem("Item Preco Negativo", "-100.00", 1L, TipoProduto.LIVRO, "1.0", false)
        ));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            compraService.calcularCustoTotal(carrinho, Regiao.SUDESTE, TipoCliente.BRONZE);
        });
        assertThat(ex.getMessage()).contains("Preço do item não pode ser negativo");
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

    private static ItemCompra criarItemPesoCubico(String nome, String preco, long qtd, TipoProduto tipo,
                                                  String pesoKg, String c, String l, String a, boolean fragil) {
        Produto p = new Produto();
        p.setNome(nome);
        p.setPreco(new BigDecimal(preco));
        p.setTipo(tipo);
        p.setFragil(fragil);
        p.setPesoFisico(new BigDecimal(pesoKg));
        p.setComprimento(new BigDecimal(c));
        p.setLargura(new BigDecimal(l));
        p.setAltura(new BigDecimal(a));
        return new ItemCompra(null, p, qtd);
    }
}