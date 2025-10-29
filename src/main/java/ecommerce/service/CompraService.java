package ecommerce.service;

import java.math.BigDecimal;
import java.math.RoundingMode; // Importado para o arredondamento final
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ecommerce.dto.CompraDTO;
import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.dto.PagamentoDTO;
import ecommerce.entity.*;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Imports não utilizados no método de cálculo foram removidos para clareza
// import ecommerce.dto.CompraDTO;
// ...
// import jakarta.transaction.Transactional;

@Service
public class CompraService {

    private static final BigDecimal DESCONTO_TIPO_5_PERCENT = new BigDecimal("0.05");
    private static final BigDecimal DESCONTO_TIPO_10_PERCENT = new BigDecimal("0.10");
    private static final BigDecimal DESCONTO_TIPO_15_PERCENT = new BigDecimal("0.15");

    private static final BigDecimal VALOR_MIN_DESCONTO_10 = new BigDecimal("500.00");
    private static final BigDecimal VALOR_MIN_DESCONTO_20 = new BigDecimal("1000.00");
    private static final BigDecimal DESCONTO_VALOR_10_PERCENT = new BigDecimal("0.10");
    private static final BigDecimal DESCONTO_VALOR_20_PERCENT = new BigDecimal("0.20");

    private static final BigDecimal LIMITE_PESO_FAIXA_A = new BigDecimal("5.00");
    private static final BigDecimal LIMITE_PESO_FAIXA_B = new BigDecimal("10.00");
    private static final BigDecimal LIMITE_PESO_FAIXA_C = new BigDecimal("50.00");

    private static final BigDecimal VALOR_KG_FAIXA_A = BigDecimal.ZERO;
    private static final BigDecimal VALOR_KG_FAIXA_B = new BigDecimal("2.00");
    private static final BigDecimal VALOR_KG_FAIXA_C = new BigDecimal("4.00");
    private static final BigDecimal VALOR_KG_FAIXA_D = new BigDecimal("7.00");

    private static final BigDecimal TAXA_MINIMA_FRETE = new BigDecimal("12.00");
    private static final BigDecimal TAXA_ITEM_FRAGIL = new BigDecimal("5.00");

    private static final BigDecimal DESCONTO_CLIENTE_PRATA = new BigDecimal("0.50");

    private static final Map<Regiao, BigDecimal> FATOR_REGIAO_MAP = new HashMap<>();

    static {
        FATOR_REGIAO_MAP.put(Regiao.SUDESTE, new BigDecimal("1.00"));
        FATOR_REGIAO_MAP.put(Regiao.SUL, new BigDecimal("1.05"));
        FATOR_REGIAO_MAP.put(Regiao.NORDESTE, new BigDecimal("1.10"));
        FATOR_REGIAO_MAP.put(Regiao.CENTRO_OESTE, new BigDecimal("1.20"));
        FATOR_REGIAO_MAP.put(Regiao.NORTE, new BigDecimal("1.30"));
    }


    private final CarrinhoDeComprasService carrinhoService;
    private final ClienteService clienteService;
    private final IEstoqueExternal estoqueExternal;
    private final IPagamentoExternal pagamentoExternal;

    @Autowired
    public CompraService(CarrinhoDeComprasService carrinhoService, ClienteService clienteService,
                         IEstoqueExternal estoqueExternal, IPagamentoExternal pagamentoExternal) {
        this.carrinhoService = carrinhoService;
        this.clienteService = clienteService;
        this.estoqueExternal = estoqueExternal;
        this.pagamentoExternal = pagamentoExternal;
    }

    @Transactional
    public CompraDTO finalizarCompra(Long carrinhoId, Long clienteId) {
        Cliente cliente = clienteService.buscarPorId(clienteId);
        CarrinhoDeCompras carrinho = carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);

        List<Long> produtosIds = carrinho.getItens().stream().map(i -> i.getProduto().getId())
                .collect(Collectors.toList());
        List<Long> produtosQtds = carrinho.getItens().stream().map(i -> i.getQuantidade()).collect(Collectors.toList());

        DisponibilidadeDTO disponibilidade = estoqueExternal.verificarDisponibilidade(produtosIds, produtosQtds);

        if (!disponibilidade.disponivel()) {
            throw new IllegalStateException("Itens fora de estoque.");
        }

        BigDecimal custoTotal = calcularCustoTotal(carrinho, cliente.getRegiao(), cliente.getTipo());

        PagamentoDTO pagamento = pagamentoExternal.autorizarPagamento(cliente.getId(), custoTotal.doubleValue());

        if (!pagamento.autorizado()) {
            throw new IllegalStateException("Pagamento não autorizado.");
        }

        EstoqueBaixaDTO baixaDTO = estoqueExternal.darBaixa(produtosIds, produtosQtds);

        if (!baixaDTO.sucesso()) {
            pagamentoExternal.cancelarPagamento(cliente.getId(), pagamento.transacaoId());
            throw new IllegalStateException("Erro ao dar baixa no estoque.");
        }

        CompraDTO compraDTO = new CompraDTO(true, pagamento.transacaoId(), "Compra finalizada com sucesso.");

        return compraDTO;
    }


    public BigDecimal calcularCustoTotal(CarrinhoDeCompras carrinho, Regiao regiao, TipoCliente tipoCliente) {

        if (carrinho == null || carrinho.getItens() == null) {
            throw new IllegalArgumentException("Carrinho ou lista de itens não pode ser nulo.");
        }
        if (regiao == null || tipoCliente == null) {
            throw new IllegalArgumentException("Região e Tipo de Cliente não podem ser nulos.");
        }
        if (carrinho.getItens().isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        List<ItemCompra> itensCarrinho = carrinho.getItens();

        BigDecimal subTotalItensComDescontoTipo = calcularSubTotalItens(itensCarrinho);
        BigDecimal subTotalItensDescontoFinal = calcularDescontoTotal(subTotalItensComDescontoTipo);
        BigDecimal frete = calcularFrete(itensCarrinho, tipoCliente, regiao);

        return subTotalItensDescontoFinal.add(frete).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularSubTotalItens(List<ItemCompra> itens) {
        BigDecimal totalFinal = BigDecimal.ZERO;

        Map<TipoProduto, SubTotalPorCategoria> subTotalPorCategoriaMap = itens.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getProduto().getTipo(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                SubTotalPorCategoria::calcularSubTotal
                        )
                ));

        for (SubTotalPorCategoria subTotalPorCategoria : subTotalPorCategoriaMap.values()) {
            int totalQuantidade = subTotalPorCategoria.getTotalQuantidade();
            BigDecimal subTotalCategoria = subTotalPorCategoria.getSubTotal();
            BigDecimal desconto = BigDecimal.ZERO;

            if (totalQuantidade >= 8) {
                desconto = DESCONTO_TIPO_15_PERCENT;
            } else if (totalQuantidade >= 5) {
                desconto = DESCONTO_TIPO_10_PERCENT;
            } else if (totalQuantidade >= 3) {
                desconto = DESCONTO_TIPO_5_PERCENT;
            }

            totalFinal = totalFinal.add(subTotalCategoria.multiply(BigDecimal.ONE.subtract(desconto)));
        }
        return totalFinal;
    }

    private BigDecimal calcularDescontoTotal(BigDecimal total) {

        if (total.compareTo(VALOR_MIN_DESCONTO_20) > 0) {
            return total.multiply(BigDecimal.ONE.subtract(DESCONTO_VALOR_20_PERCENT));

        } else if (total.compareTo(VALOR_MIN_DESCONTO_10) > 0) {
            return total.multiply(BigDecimal.ONE.subtract(DESCONTO_VALOR_10_PERCENT));
        }

        return total;
    }

    private BigDecimal calcularFrete(List<ItemCompra> itens, TipoCliente tipoCliente, Regiao regiao) {

        BigDecimal subTotalFrete = BigDecimal.ZERO;
        BigDecimal totalPeso = BigDecimal.ZERO;
        BigDecimal faixaSelecionada = BigDecimal.ZERO;

        for (ItemCompra itemCompra : itens) {
            Produto produto = itemCompra.getProduto();
            BigDecimal pesoTributavel = produto.calcularPesoTributavel();

            totalPeso = totalPeso.add(
                    pesoTributavel.multiply(BigDecimal.valueOf(itemCompra.getQuantidade()))
            );

            if (produto.isFragil()) {
                BigDecimal taxaFragil = TAXA_ITEM_FRAGIL.multiply(BigDecimal.valueOf(itemCompra.getQuantidade()));
                subTotalFrete = subTotalFrete.add(taxaFragil);
            }
        }

        boolean isentoTaxaMinima = false;

        if (totalPeso.compareTo(LIMITE_PESO_FAIXA_A) <= 0) {
            faixaSelecionada = VALOR_KG_FAIXA_A;
            isentoTaxaMinima = true;
        }
        else if (totalPeso.compareTo(LIMITE_PESO_FAIXA_B) <= 0) {
            faixaSelecionada = VALOR_KG_FAIXA_B;
        }
        else if (totalPeso.compareTo(LIMITE_PESO_FAIXA_C) <= 0) {
            faixaSelecionada = VALOR_KG_FAIXA_C;
        }
        else {
            faixaSelecionada = VALOR_KG_FAIXA_D;
        }

        if (!isentoTaxaMinima) {
            subTotalFrete = subTotalFrete.add(TAXA_MINIMA_FRETE);
        }

        BigDecimal valorPeso = totalPeso.multiply(faixaSelecionada);

        BigDecimal totalFrete = subTotalFrete.add(valorPeso);

        BigDecimal totalFreteFatorRegiao = totalFrete.multiply(FATOR_REGIAO_MAP.get(regiao));

        BigDecimal totalFreteClienteNivel = totalFreteFatorRegiao;

        if (tipoCliente == TipoCliente.OURO) {
            totalFreteClienteNivel = BigDecimal.ZERO;
        } else if (tipoCliente == TipoCliente.PRATA) {
            totalFreteClienteNivel = totalFreteFatorRegiao.multiply(DESCONTO_CLIENTE_PRATA);
        }

        return totalFreteClienteNivel;
    }
}