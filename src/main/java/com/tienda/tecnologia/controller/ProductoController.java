package com.tienda.tecnologia.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tienda.tecnologia.model.Producto;

@RestController
public class ProductoController {

    private static final Set<String> CATEGORIAS = Set.of("LAPTOP", "RAM", "CPU", "PC");

    // "Base de datos" en memoria: id -> producto
    private final Map<Long, Producto> productos = new ConcurrentHashMap<>();
    // Generador de ids: 1, 2, 3, ...
    private final AtomicLong secuencia = new AtomicLong(0);

    // Datos iniciales
    public ProductoController() {
        // Laptops
        guardar(new Producto(null, "ThinkPad T16 Gen 3", "LAPTOP", "Lenovo",
                new BigDecimal("5899.00"), 10, "Intel Core Ultra 7, 32GB RAM, 1TB SSD, 16 pulgadas"));
        guardar(new Producto(null, "ThinkPad P1 Gen 7", "LAPTOP", "Lenovo",
                new BigDecimal("9499.00"), 5, "Workstation, Core Ultra 9, RTX 4070, 64GB RAM"));
        guardar(new Producto(null, "Yoga Slim 7i", "LAPTOP", "Lenovo",
                new BigDecimal("4299.00"), 8, "OLED 14 pulgadas, Core Ultra 7, 16GB RAM"));
        guardar(new Producto(null, "EliteBook 840 G11", "LAPTOP", "HP",
                new BigDecimal("5499.00"), 7, "Core Ultra 5, 16GB RAM, 512GB SSD, 14 pulgadas"));
        guardar(new Producto(null, "XPS 14", "LAPTOP", "Dell",
                new BigDecimal("7999.00"), 4, "Core Ultra 7, 32GB RAM, 1TB SSD, OLED 3.2K"));
        guardar(new Producto(null, "Latitude 5450", "LAPTOP", "Dell",
                new BigDecimal("4599.00"), 9, "Core Ultra 5, 16GB RAM, 512GB SSD"));
        guardar(new Producto(null, "Zenbook 14 OLED", "LAPTOP", "Asus",
                new BigDecimal("4199.00"), 6, "Ryzen 7, 16GB RAM, 1TB SSD"));
        // Memorias RAM
        guardar(new Producto(null, "Fury Beast 32GB DDR5", "RAM", "Kingston",
                new BigDecimal("459.00"), 25, "Kit 2x16GB, 5600MHz"));
        guardar(new Producto(null, "Vengeance 32GB DDR5", "RAM", "Corsair",
                new BigDecimal("499.00"), 18, "Kit 2x16GB, 6000MHz"));
        // Procesadores
        guardar(new Producto(null, "Core i7-14700K", "CPU", "Intel",
                new BigDecimal("1699.00"), 12, "20 nucleos, hasta 5.6GHz"));
        guardar(new Producto(null, "Ryzen 7 9700X", "CPU", "AMD",
                new BigDecimal("1499.00"), 15, "8 nucleos, hasta 5.5GHz"));
        // PCs
        guardar(new Producto(null, "ThinkCentre M90q Gen 5", "PC", "Lenovo",
                new BigDecimal("3799.00"), 6, "Mini PC, Core i7, 16GB RAM, 512GB SSD"));
        guardar(new Producto(null, "Elite Mini 800 G9", "PC", "HP",
                new BigDecimal("3599.00"), 5, "Mini PC, Core i5, 16GB RAM, 512GB SSD"));
        guardar(new Producto(null, "OptiPlex Micro 7020", "PC", "Dell",
                new BigDecimal("3299.00"), 8, "Core i5, 16GB RAM, 256GB SSD"));
    }

    // GET /  -> informacion de la API
    @GetMapping("/")
    public Map<String, Object> inicio() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("aplicacion", "Tienda de Tecnologia - API REST");
        info.put("totalProductos", productos.size());
        info.put("categorias", CATEGORIAS);
        info.put("endpoints", List.of(
                "GET  /api/productos",
                "GET  /api/productos?categoria=LAPTOP",
                "GET  /api/productos?marca=HP",
                "GET  /api/productos/{id}",
                "POST /api/productos"));
        return info;
    }

    // GET /api/productos  (filtros opcionales: categoria, marca)
    @GetMapping("/api/productos")
    public List<Producto> listar(
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "marca", required = false) String marca) {
        return productos.values().stream()
                .filter(p -> categoria == null || p.categoria().equalsIgnoreCase(categoria))
                .filter(p -> marca == null || p.marca().equalsIgnoreCase(marca))
                .sorted(Comparator.comparing(Producto::id))
                .toList();
    }

    // GET /api/productos/3
    @GetMapping("/api/productos/{id}")
    public ResponseEntity<?> obtener(@PathVariable(name = "id") Long id) {
        Producto p = productos.get(id);
        if (p == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "No existe el producto con id " + id));
        }
        return ResponseEntity.ok(p);
    }

    // POST /api/productos  (body JSON)
    @PostMapping("/api/productos")
    public ResponseEntity<?> crear(@RequestBody Producto producto) {
        List<String> errores = validar(producto);
        if (!errores.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("errores", errores));
        }
        Producto creado = guardar(producto);
        return ResponseEntity.created(URI.create("/api/productos/" + creado.id())).body(creado);
    }

    // Reglas de validacion
    private List<String> validar(Producto p) {
        List<String> errores = new ArrayList<>();
        if (p.nombre() == null || p.nombre().isBlank()) {
            errores.add("nombre es obligatorio");
        }
        if (p.categoria() == null || !CATEGORIAS.contains(p.categoria().toUpperCase())) {
            errores.add("categoria debe ser una de: LAPTOP, RAM, CPU, PC");
        }
        if (p.marca() == null || p.marca().isBlank()) {
            errores.add("marca es obligatoria");
        }
        if (p.precio() == null || p.precio().compareTo(BigDecimal.ZERO) <= 0) {
            errores.add("precio debe ser mayor a 0");
        }
        if (p.stock() == null || p.stock() < 0) {
            errores.add("stock debe ser 0 o mayor");
        }
        return errores;
    }

    // Asigna id y guarda en memoria
    private Producto guardar(Producto p) {
        long id = secuencia.incrementAndGet();
        Producto nuevo = new Producto(id, p.nombre(), p.categoria().toUpperCase(),
                p.marca(), p.precio(), p.stock(), p.descripcion());
        productos.put(id, nuevo);
        return nuevo;
    }
}