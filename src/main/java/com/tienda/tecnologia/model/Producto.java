package com.tienda.tecnologia.model;

import java.math.BigDecimal;

/**
 * Producto de la tienda de tecnologia.
 * Un record genera automaticamente constructor, getters
 * (nombre(), precio(), ...), equals, hashCode y toString.
 */
public record Producto(
        Long id,
        String nombre,
        String categoria,
        String marca,
        BigDecimal precio,
        Integer stock,
        String descripcion) {
}