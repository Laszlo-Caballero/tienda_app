package com.laszlo.tienda_app.api

import com.laszlo.tienda_app.model.Imagen
import com.laszlo.tienda_app.model.ProductAnalysis

object ProductRepository {

    // Rich local fallback dataset of products to ensure functional offline state
    private val LOCAL_PRODUCTS = listOf(
        ProductAnalysis(
            productoId = 101,
            nombre = "Laptop Gamer ASUS ROG Zephyrus G14",
            precios = listOf(5499.00, 5299.00),
            vendido_por = "Saga Falabella",
            marca = "ASUS",
            url_venta = "https://www.falabella.com.pe/falabella-pe/category/cat40712/Laptops",
            caracteristicas = listOf(
                "Procesador AMD Ryzen 9",
                "Memoria RAM de 16GB DDR5",
                "Tarjeta gráfica NVIDIA RTX 4060",
                "Pantalla ROG Nebula 120Hz"
            ),
            categoria = "Tecnología",
            sub_categoria = "Laptops",
            especificaciones = listOf(
                "Almacenamiento: 1TB SSD NVMe",
                "Sistema Operativo: Windows 11 Home",
                "Batería: 76WHrs, 4S1P, 4 celdas Li-ion",
                "Peso: 1.65 kg"
            ),
            imagenes = listOf(Imagen(1, "/uploads/laptop.jpg")),
            similitud = 98.0
        ),
        ProductAnalysis(
            productoId = 102,
            nombre = "Smart TV LG OLED C3 55 pulgadas 4K",
            precios = listOf(4299.00, 3999.00),
            vendido_por = "Ripley",
            marca = "LG",
            url_venta = "https://simple.ripley.com.pe/tecnologia/televisores/oled",
            caracteristicas = listOf(
                "Pantalla OLED Evo de 55 pulgadas",
                "Resolución Real 4K",
                "Procesador α9 AI Gen6",
                "Sonido Dolby Atmos y Dolby Vision"
            ),
            categoria = "Tecnología",
            sub_categoria = "Televisores",
            especificaciones = listOf(
                "Puertos: 4 x HDMI 2.1, 3 x USB",
                "Tasa de Refresco: 120 Hz",
                "Inteligencia Artificial: LG ThinQ AI",
                "Garantía: 2 años"
            ),
            imagenes = listOf(Imagen(2, "/uploads/tv.jpg")),
            similitud = 95.0
        ),
        ProductAnalysis(
            productoId = 103,
            nombre = "Celular Apple iPhone 15 Pro Max 256GB",
            precios = listOf(5899.00),
            vendido_por = "Mercado Libre",
            marca = "Apple",
            url_venta = "https://listado.mercadolibre.com.pe/iphone-15-pro-max",
            caracteristicas = listOf(
                "Diseño de titanio de calidad aeroespacial",
                "Chip A17 Pro con GPU de 6 núcleos",
                "Sistema de cámaras pro con zoom óptico de 5x",
                "Botón de Acción personalizable"
            ),
            categoria = "Tecnología",
            sub_categoria = "Celulares",
            especificaciones = listOf(
                "Capacidad: 256 GB",
                "Pantalla: Super Retina XDR de 6.7 pulgadas",
                "Puerto de carga: USB-C",
                "Resistencia al agua: Clasificación IP68"
            ),
            imagenes = listOf(Imagen(3, "/uploads/iphone.jpg")),
            similitud = 97.0
        ),
        ProductAnalysis(
            productoId = 104,
            nombre = "Audífonos Sony WH-1000XM5 Noise Cancelling",
            precios = listOf(1499.00, 1349.00),
            vendido_por = "Saga Falabella",
            marca = "Sony",
            url_venta = "https://www.falabella.com.pe/falabella-pe/product/110294154/Sony-WH-1000XM5",
            caracteristicas = listOf(
                "Cancelación de ruido líder en la industria",
                "Procesador integrado V1",
                "Llamadas nítidas con 4 micrófonos",
                "Hasta 30 horas de duración de batería"
            ),
            categoria = "Tecnología",
            sub_categoria = "Audífonos",
            especificaciones = listOf(
                "Bluetooth: Versión 5.2",
                "Carga rápida: 3 minutos para 3 horas",
                "Peso: 250 g",
                "Asistente de voz: Compatible con Alexa y Google Assistant"
            ),
            imagenes = listOf(Imagen(4, "/uploads/sony.jpg")),
            similitud = 99.0
        ),
        ProductAnalysis(
            productoId = 105,
            nombre = "Zapatillas Running Nike Air Max Dn",
            precios = listOf(629.00),
            vendido_por = "Tottus",
            marca = "Nike",
            url_venta = "https://tottus.falabella.com.pe/tottus-pe",
            caracteristicas = listOf(
                "Sistema de amortiguación Dynamic Air",
                "Diseño futurista y de alta comodidad",
                "Malla transpirable multicapa en la parte superior",
                "Suela de goma para tracción duradera"
            ),
            categoria = "Moda",
            sub_categoria = "Calzado",
            especificaciones = listOf(
                "Amortiguación: Cápsulas de aire de doble presión",
                "Material exterior: Sintético y textil",
                "Uso recomendado: Diario / Running urbano",
                "Color: Negro/Antracita"
            ),
            imagenes = listOf(Imagen(5, "/uploads/nike.jpg")),
            similitud = 92.0
        )
    )

    fun searchLocal(query: String): List<ProductAnalysis> {
        if (query.isBlank()) return LOCAL_PRODUCTS
        return LOCAL_PRODUCTS.filter { product ->
            product.nombre.contains(query, ignoreCase = true) ||
            product.marca.contains(query, ignoreCase = true) ||
            product.categoria.contains(query, ignoreCase = true) ||
            product.sub_categoria.contains(query, ignoreCase = true) ||
            product.vendido_por.contains(query, ignoreCase = true) ||
            product.productoId.toString() == query
        }
    }

    suspend fun getProducts(query: String): List<ProductAnalysis> {
        return try {
            val historyItems = ApiController.api.getHistory()
            val mapped = historyItems.map { history ->
                ProductAnalysis(
                    productoId = history.id,
                    nombre = history.title,
                    precios = listOf(0.0), // default mock price
                    vendido_por = "Desconocido",
                    marca = "Genérico",
                    url_venta = "",
                    caracteristicas = listOf(history.description),
                    categoria = history.category,
                    sub_categoria = history.category,
                    especificaciones = emptyList(),
                    imagenes = listOf(Imagen(history.id, history.image)),
                    similitud = 100.0
                )
            }
            if (query.isBlank()) {
                mapped.ifEmpty { LOCAL_PRODUCTS }
            } else {
                mapped.filter {
                    it.nombre.contains(query, ignoreCase = true) ||
                    it.categoria.contains(query, ignoreCase = true)
                }.ifEmpty { searchLocal(query) }
            }
        } catch (e: Exception) {
            searchLocal(query)
        }
    }

    suspend fun voiceSearch(query: String): List<ProductAnalysis> {
        return try {
            val response = ApiController.api.voiceSearch(query)
            response.data
        } catch (e: Exception) {
            searchLocal(query)
        }
    }
}

