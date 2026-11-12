# Tienda de Tecnologia - API REST

API REST con Spring Boot desplegada en GitHub Codespaces.
Gestiona productos (laptops, memorias RAM, procesadores y PCs) de marcas como Lenovo, HP, Dell, Asus, Intel, AMD.

## Ejecutar
./gradlew bootRun

## Endpoints
| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | /api/productos | Lista productos (filtros: ?categoria=, ?marca=) |
| GET | /api/productos/{id} | Obtiene un producto |
| POST | /api/productos | Registra un producto |

## Ejemplo de body (POST)
{ "nombre": "Victus 15", "categoria": "LAPTOP", "marca": "HP", "precio": 3899.00, "stock": 6, "descripcion": "Gamer" }
