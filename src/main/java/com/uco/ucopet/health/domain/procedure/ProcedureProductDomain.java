package com.uco.ucopet.health.domain.procedure;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public class ProcedureProductDomain {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private Integer quantity;

    public ProcedureProductDomain() {
    }

    public ProcedureProductDomain(final UUID productId, final Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public UUID getProductId() { return productId; }
    public void setProductId(final UUID productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(final Integer quantity) { this.quantity = quantity; }
}