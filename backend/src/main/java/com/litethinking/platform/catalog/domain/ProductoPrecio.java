package com.litethinking.platform.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

@Entity
@Table(name = "producto_precios")
public class ProductoPrecio {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 3)
    private String moneda;

    @Column(nullable = false)
    private BigDecimal valor;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    protected ProductoPrecio() {
        // JPA
    }

    public ProductoPrecio(String moneda, BigDecimal valor) {
        this.moneda = moneda;
        this.valor = valor;
    }

    public UUID getId() {
        return id;
    }

    public String getMoneda() {
        return moneda;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }
}
