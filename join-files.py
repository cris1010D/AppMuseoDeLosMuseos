import os
import sys
from datetime import datetime


def coincide_patron(nombre_archivo, patrones):
    """True si el archivo calza con alguna extensión o nombre de la configuración."""
    for patron in patrones:
        if patron.startswith("."):  # Es una extensión (.kt) o un nombre que empieza con punto (.gitignore)
            if nombre_archivo.endswith(patron):
                return True
        else:  # Es un nombre directo (gradlew) o extensión sin punto (kt)
            if nombre_archivo == patron or nombre_archivo.endswith(f".{patron}"):
                return True
    return False


def leer_texto(ruta):
    """Lee en UTF-8; si falla, prueba latin-1 (archivos antiguos de Windows)."""
    for codificacion in ("utf-8", "latin-1"):
        try:
            with open(ruta, "r", encoding=codificacion) as f:
                return f.read()
        except UnicodeDecodeError:
            continue
    return None


def dibujar_arbol(rutas):
    """Arma el árbol de carpetas a partir de las rutas incluidas."""
    raiz = {}
    for ruta in rutas:
        nodo = raiz
        for parte in ruta.split("/"):
            nodo = nodo.setdefault(parte, {})

    lineas = []

    def recorrer(nodo, prefijo=""):
        # Carpetas primero, luego archivos, en orden alfabético
        claves = sorted(nodo, key=lambda k: (not nodo[k], k.lower()))
        for i, clave in enumerate(claves):
            ultimo = i == len(claves) - 1
            lineas.append(f"{prefijo}{'└── ' if ultimo else '├── '}{clave}")
            if nodo[clave]:
                recorrer(nodo[clave], prefijo + ("    " if ultimo else "│   "))

    recorrer(raiz)
    return "\n".join(lineas)


def consolidar_archivos(config_extensiones, carpeta_proyecto=".", archivo_salida="myfiles.txt",
                        carpetas_ignoradas=(), archivos_ignorados=(), tamano_maximo_kb=300):
    # Limpiamos la entrada: quitamos espacios y asteriscos para comparar mejor
    # Ejemplo: "*.java; .py; config.xml" -> [".java", ".py", "config.xml"]
    patrones = [p.strip().replace("*", "") for p in config_extensiones.split(";") if p.strip()]

    carpeta_proyecto = os.path.abspath(carpeta_proyecto)
    salida_abs = os.path.abspath(archivo_salida)
    este_script = os.path.abspath(__file__)

    # 1) Recolectar primero las rutas, en orden, para poder armar el árbol y el resumen
    seleccionados = []
    omitidos_grandes = []
    for raiz, carpetas, archivos in os.walk(carpeta_proyecto):
        # Podamos carpetas pesadas/generadas para no entrar en ellas (y las ordenamos)
        carpetas[:] = sorted(c for c in carpetas if c not in carpetas_ignoradas)

        for nombre_archivo in sorted(archivos):
            ruta_completa = os.path.join(raiz, nombre_archivo)

            # Evitar leer el archivo de salida o este mismo script
            if os.path.abspath(ruta_completa) in (salida_abs, este_script):
                continue
            if nombre_archivo in archivos_ignorados:
                continue
            if not coincide_patron(nombre_archivo, patrones):
                continue
            if os.path.getsize(ruta_completa) > tamano_maximo_kb * 1024:
                omitidos_grandes.append(ruta_completa)
                continue

            ruta_relativa = os.path.relpath(ruta_completa, carpeta_proyecto).replace(os.sep, "/")
            seleccionados.append((ruta_relativa, ruta_completa))

    if not seleccionados:
        print("No se encontró ningún archivo que coincida con la configuración.")
        return

    # 2) Leer contenidos
    contenidos = []
    for ruta_relativa, ruta_completa in seleccionados:
        texto = leer_texto(ruta_completa)
        if texto is None:
            print(f"Error al leer {ruta_relativa}: no es texto")
            continue
        contenidos.append((ruta_relativa, texto))
        print(f"Procesado: {ruta_relativa}")

    total_lineas = sum(len(t.splitlines()) for _, t in contenidos)
    nombre_proyecto = os.path.basename(carpeta_proyecto)

    # 3) Escribir el archivo final: resumen + árbol + contenido de cada archivo
    with open(archivo_salida, "w", encoding="utf-8", newline="\n") as f_destino:
        f_destino.write(f"PROYECTO: {nombre_proyecto}\n")
        f_destino.write(f"Generado: {datetime.now():%d-%m-%Y %H:%M}\n")
        f_destino.write(f"Archivos: {len(contenidos)} | Líneas: {total_lineas}\n\n")
        f_destino.write("ESTRUCTURA\n")
        f_destino.write(f"{nombre_proyecto}/\n{dibujar_arbol([r for r, _ in contenidos])}\n\n")

        for ruta_relativa, texto in contenidos:
            # Membrete con el nombre del archivo
            f_destino.write(f"/* {'=' * 60} */\n")
            f_destino.write(f"/* ARCHIVO: {ruta_relativa} */\n")
            f_destino.write(f"/* {'=' * 60} */\n\n")
            f_destino.write(texto.rstrip() + "\n\n\n")

    tamano_kb = os.path.getsize(archivo_salida) / 1024
    print(f"\n¡Listo! {len(contenidos)} archivos ({total_lineas} líneas, {tamano_kb:.0f} KB) guardados en '{archivo_salida}'")
    for ruta in omitidos_grandes:
        print(f"  Omitido por tamaño (> {tamano_maximo_kb} KB): {ruta}")


# --- CONFIGURACIÓN ---
# Extensiones o nombres separados por ";"
# Incluye Kotlin/Android (.kt, .kts, .toml) además de Java, web y SQL
extensiones_a_buscar = (
    ".kt; .kts; .java; .xml; .toml; .properties; .gradle; .pro; .gitignore; "
    "application.yml; .yml; .json; .md; .sql; .js; .jsx; .ts; .tsx; .css; .html; .py; .txt"
)

# Carpetas que NO se deben recorrer (dependencias, compilados y configuración personal del IDE)
carpetas_a_ignorar = {
    "node_modules", "dist", "build", "out", ".git", ".vscode", "__pycache__",
    ".gradle", ".idea", ".kotlin", ".cxx", "captures",
}

# Archivos concretos que NO se deben incluir
archivos_a_ignorar = {
    "package-lock.json",   # enorme y sin valor para leer el código
    "local.properties",    # ruta del SDK de tu PC (privado)
    "gradlew.bat",         # script generado, igual al gradlew
}

# Ejecutar el programa
# Opcional: python join-files.py [carpeta_del_proyecto] [nombre_salida.txt]
if __name__ == "__main__":
    carpeta = sys.argv[1] if len(sys.argv) > 1 else "."
    salida = sys.argv[2] if len(sys.argv) > 2 else "myfiles.txt"
    consolidar_archivos(extensiones_a_buscar,
                        carpeta_proyecto=carpeta,
                        archivo_salida=salida,
                        carpetas_ignoradas=carpetas_a_ignorar,
                        archivos_ignorados=archivos_a_ignorar)
