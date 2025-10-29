package com.litethinking.platform.orders.domain;

import com.litethinking.platform.catalog.domain.Empresa;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "ordenes")
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private Instant fecha;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_nit")
    private Empresa empresa;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrdenItem> items = new LinkedHashSet<>();

    protected Orden() {
    }

    public Orden(Cliente cliente, Empresa empresa) {
        this.cliente = cliente;
        this.empresa = empresa;
    }

    @PrePersist
    void onCreate() {
        if (fecha == null) {
            fecha = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public Instant getFecha() {
        return fecha;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public Set<OrdenItem> getItems() {
        return items;
    }

    public void replaceItems(Set<OrdenItem> nuevos) {
        this.items.clear();
        this.items.addAll(nuevos);
        this.items.forEach(item -> item.setOrden(this));
    }
}
