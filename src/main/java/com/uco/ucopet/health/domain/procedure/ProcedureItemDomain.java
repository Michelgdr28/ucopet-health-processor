package com.uco.ucopet.health.domain.procedure;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public class ProcedureItemDomain {

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(nullable = false)
    private Integer quantity;

    public ProcedureItemDomain() {
    }

    public ProcedureItemDomain(final UUID itemId, final Integer quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public UUID getItemId() { return itemId; }
    public void setItemId(final UUID itemId) { this.itemId = itemId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(final Integer quantity) { this.quantity = quantity; }
}