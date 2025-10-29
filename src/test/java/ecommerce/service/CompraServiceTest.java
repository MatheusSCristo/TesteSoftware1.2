package ecommerce.service;

import ecommerce.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;


public class CompraServiceTest {

	private CompraService compraService;

	@BeforeEach
	void setUp() {
		compraService = new CompraService(null, null, null, null);
	}


	@ParameterizedTest(name = "[{index}] {4}")
	@MethodSource("cenariosDeCalculo")
	@DisplayName("Deve calcular o custo total corretamente para diversos cenários")
	void calcularCustoTotal_cenariosCompletos(
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

	static Stream<Arguments> cenariosDeCalculo() {
		return Stream.of(
				arguments(
						List.of(criarItem("Item A", "100.00", 1L, TipoProduto.LIVRO, "5.00", false)), // Limite Faixa A
						Regiao.SUDESTE, TipoCliente.BRONZE,
						"100.00",
						"Limite Frete: 5.00kg (Faixa A, Isento)"
				),

				arguments(
						List.of(criarItem("Item B", "100.00", 1L, TipoProduto.MOVEL, "5.01", false)), // Limite Faixa B
						Regiao.SUDESTE, TipoCliente.BRONZE,
						"122.02",
						"Limite Frete: 5.01kg (Faixa B, com taxa mínima)"
				),

				arguments(
						List.of(criarItem("Item C", "100.00", 1L, TipoProduto.MOVEL, "10.01", false)),
						Regiao.SUL, TipoCliente.BRONZE,
						"154.64",
						"Limite Frete: 10.01kg (Faixa C) + Região Sul (1.05)"
				),

				arguments(
						List.of(criarItem("Item D", "100.00", 1L, TipoProduto.MOVEL, "50.01", false)),
						Regiao.NORDESTE, TipoCliente.BRONZE,
						"498.28",
						"Limite Frete: 50.01kg (Faixa D) + Região Nordeste (1.10)"
				),

				arguments(
						List.of(criarItem("Item E", "100.00", 3L, TipoProduto.ROUPA, "1.0", false)),
						Regiao.SUDESTE, TipoCliente.BRONZE,
						"285.00",
						"Limite Desconto Tipo: 3 itens (5%)"
				),

				arguments(
						List.of(criarItem("Item F", "100.00", 5L, TipoProduto.ROUPA, "1.0", false)),
						Regiao.SUDESTE, TipoCliente.BRONZE,
						"450.00",
						"Limite Desconto Tipo: 5 itens (10%)"
				),

				arguments(
						List.of(criarItem("Item G", "100.00", 8L, TipoProduto.ROUPA, "1.0", false)),
						Regiao.SUDESTE, TipoCliente.BRONZE,
						"640.00",
						"Limite Desconto Tipo (8 itens) + Desconto Valor (10%)"
				),

				arguments(
						List.of(criarItem("Item H", "500.01", 1L, TipoProduto.ELETRONICO, "1.0", false)),
						Regiao.SUDESTE, TipoCliente.BRONZE,
						"450.01",
						"Limite Desconto Valor: 500.01 (10%)"
				),

				arguments(
						List.of(criarItem("Item I", "1000.01", 1L, TipoProduto.ELETRONICO, "1.0", false)),
						Regiao.SUDESTE, TipoCliente.BRONZE,
						"800.01",
						"Limite Desconto Valor: 1000.01 (20%)"
				),

				arguments(
						List.of(criarItem("Item Frágil", "50.00", 2L, TipoProduto.ELETRONICO, "1.0", true)),
						Regiao.NORTE, TipoCliente.BRONZE,
						"113.00",
						"Frete: Item Frágil (Faixa A) + Região Norte (1.30)"
				),

				arguments(
						List.of(criarItem("Item Frágil B", "50.00", 2L, TipoProduto.ELETRONICO, "3.0", true)),
						Regiao.CENTRO_OESTE, TipoCliente.BRONZE,
						"140.80",
						"Frete: Item Frágil (Faixa B) + Região Centro-Oeste (1.20)"
				),

				arguments(
						List.of(criarItem("Item Frágil B", "50.00", 2L, TipoProduto.ELETRONICO, "3.0", true)),
						Regiao.CENTRO_OESTE, TipoCliente.PRATA,
						"120.40",
						"Frete: Nível Cliente PRATA (50% desc frete)"
				),


				arguments(
						List.of(criarItem("Item Frágil B", "50.00", 2L, TipoProduto.ELETRONICO, "3.0", true)),
						Regiao.CENTRO_OESTE, TipoCliente.OURO, // Cliente Ouro
						"100.00",
						"Frete: Nível Cliente OURO (100% desc frete)"
				),

				arguments(
						List.of(criarItemPesoCubico("Item Volumoso", "300.00", 1L, TipoProduto.MOVEL,
								"1.0",
								"40", "40", "40", false)),
						Regiao.SUDESTE, TipoCliente.BRONZE,
						"354.67",
						"Frete: Peso Cúbico (10.67kg) > Peso Físico (1kg)"
				),
				arguments(
						List.of(criarItem("Item Peso Negativo", "100.00", 1L, TipoProduto.LIVRO, "-1.0", false)),
						Regiao.SUDESTE, TipoCliente.BRONZE,
						"100.00",
						"Robustez: Peso total negativo (Cobre branch 'peso < 0')"
				)
		);
	}

	private static ItemCompra criarItem(String nome, String preco, long qtd, TipoProduto tipo, String pesoKg, boolean fragil) {
		Produto p = new Produto();
		p.setNome(nome);
		p.setPreco(new BigDecimal(preco));
		p.setTipo(tipo);
		p.setFragil(fragil); // Usa Boolean
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