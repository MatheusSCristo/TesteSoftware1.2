package ecommerce.entity;

import java.math.BigDecimal;
import java.util.List;


public class SubTotalPorCategoria {
    private final int totalQuantidade;
    private final BigDecimal SubTotal;

    public SubTotalPorCategoria(int totalQuantidade, BigDecimal subTotal) {
        this.totalQuantidade = totalQuantidade;
        SubTotal = subTotal;
    }

    public int getTotalQuantidade() {
        return totalQuantidade;
    }

    public BigDecimal getSubTotal() {
        return SubTotal;
    }

    public static SubTotalPorCategoria calcularSubTotal(List<ItemCompra> itens) {
        int totalQtd = 0;
        BigDecimal subTotalItens = BigDecimal.ZERO;

        for (ItemCompra item : itens) {
            if (item.getQuantidade() == null || item.getQuantidade() <= 0) {
                throw new IllegalArgumentException("Quantidade do item não pode ser zero ou negativa.");
            }
            if (item.getProduto() == null || item.getProduto().getPreco() == null ||
                    item.getProduto().getPreco().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Preço do item não pode ser negativo.");
            }
            int qtd = item.getQuantidade().intValue();
            totalQtd += qtd;

            BigDecimal lineTotal = item.getProduto().getPreco().multiply(
                    new BigDecimal(qtd)
            );

            subTotalItens = subTotalItens.add(lineTotal);
        }

        return new SubTotalPorCategoria(totalQtd, subTotalItens);
    }




}
