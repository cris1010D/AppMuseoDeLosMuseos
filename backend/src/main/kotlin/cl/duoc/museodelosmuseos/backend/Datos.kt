package cl.duoc.museodelosmuseos.backend

// Datos en memoria (sin base de datos) para mantener el backend simple
object Datos {
    val productos = listOf(
        Producto("L001", "Arte precolombino de Iberoamérica", "Libro", 35.0, 12),
        Producto("L002", "Guía de museos virtuales", "Libro", 22.5, 30),
        Producto("L003", "Catálogo de pintura colonial", "Libro", 48.0, 5),
        Producto("M001", "Polera Museo de los Museos", "Merch", 18.0, 40),
        Producto("M002", "Taza ilustrada", "Merch", 12.0, 25),
        Producto("M003", "Set de postales", "Merch", 8.5, 60),
        Producto("C001", "Curso de curaduría digital", "Curso", 60.0, 999),
        Producto("C002", "Curso de historia del arte latinoamericano", "Curso", 45.0, 999)
    )

    val museos = listOf(
        MuseoVirtual(
            "MU01", "Museo Nacional de Bellas Artes", "Chile", -33.4344, -70.6438,
            "Principal museo de artes visuales de Chile.", "https://www.mnba.gob.cl"
        ),
        MuseoVirtual(
            "MU02", "Museo del Prado", "España", 40.4138, -3.6921,
            "Una de las pinacotecas más importantes del mundo.", "https://www.museodelprado.es"
        ),
        MuseoVirtual(
            "MU03", "MALBA", "Argentina", -34.5770, -58.4033,
            "Museo de Arte Latinoamericano de Buenos Aires.", "https://www.malba.org.ar"
        ),
        MuseoVirtual(
            "MU04", "Museo Nacional de Antropología", "México", 19.4260, -99.1863,
            "Gran colección de arte precolombino mesoamericano.", "https://www.mna.inah.gob.mx"
        ),
        MuseoVirtual(
            "MU05", "MASP", "Brasil", -23.5614, -46.6558,
            "Museo de Arte de São Paulo.", "https://masp.org.br"
        ),
        MuseoVirtual(
            "MU06", "Museo Larco", "Perú", -12.0727, -77.0710,
            "Arte precolombino peruano en Lima.", "https://www.museolarco.org"
        ),
        MuseoVirtual(
            "MU07", "Museo del Oro", "Colombia", 4.6019, -74.0720,
            "La mayor colección de orfebrería prehispánica del mundo.",
            "https://www.banrepcultural.org/bogota/museo-del-oro"
        )
    )
}
