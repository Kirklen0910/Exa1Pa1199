#!/usr/bin/env python3
"""Genera el informe PDF de la tarea 2.5 a partir del Markdown del repositorio.

Uso:
    python3 docs/generar_informe.py

Requiere python-docx y LibreOffice (soffice) instalados. Las capturas se toman
de docs/capturas/fernando/ y se agregan como anexo al final.
"""

from __future__ import annotations

import re
from pathlib import Path

from docx import Document
from docx.enum.section import WD_SECTION
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_BREAK
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Pt, RGBColor
from PIL import Image

RAIZ = Path(__file__).resolve().parent.parent
DOCS = RAIZ / "docs"
MARKDOWN = DOCS / "comparativa-fernando-orellana.md"
CAPTURAS = DOCS / "capturas" / "fernando"
SALIDA_DOCX = DOCS / "Informe_Tarea_2.5_Fernando_Orellana.docx"
SALIDA_PDF = RAIZ / "Informe_Tarea_2.5_Fernando_Orellana.pdf"

AZUL = RGBColor(0x1F, 0x3A, 0x5F)
GRIS = RGBColor(0x55, 0x55, 0x55)
VERDE = RGBColor(0x1B, 0x5E, 0x20)


def configurar_documento(doc: Document) -> None:
    seccion = doc.sections[0]
    seccion.page_width = Cm(21.0)
    seccion.page_height = Cm(29.7)
    seccion.top_margin = Cm(2.0)
    seccion.bottom_margin = Cm(2.0)
    seccion.left_margin = Cm(2.2)
    seccion.right_margin = Cm(2.2)

    estilos = doc.styles
    normal = estilos["Normal"]
    normal.font.name = "Calibri"
    normal.font.size = Pt(10.5)
    normal.paragraph_format.space_after = Pt(6)
    normal.paragraph_format.line_spacing = 1.08

    for nombre, tamano, color in [
        ("Title", 26, AZUL),
        ("Heading 1", 16, AZUL),
        ("Heading 2", 13, AZUL),
        ("Heading 3", 11.5, GRIS),
    ]:
        estilo = estilos[nombre]
        estilo.font.name = "Calibri"
        estilo.font.size = Pt(tamano)
        estilo.font.color.rgb = color
        estilo.font.bold = True
        estilo.paragraph_format.space_before = Pt(12)
        estilo.paragraph_format.space_after = Pt(6)


def agregar_pie(doc: Document) -> None:
    seccion = doc.sections[0]
    pie = seccion.footer.paragraphs[0]
    pie.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = pie.add_run("José Fernando Orellana Reyes · Tarea 2.5 · ")
    run.font.size = Pt(8)
    run.font.color.rgb = GRIS

    fld = OxmlElement("w:fldSimple")
    fld.set(qn("w:instr"), "PAGE")
    run_pagina = OxmlElement("w:r")
    texto = OxmlElement("w:t")
    texto.text = "1"
    run_pagina.append(texto)
    fld.append(run_pagina)
    pie._p.append(fld)


def agregar_portada(doc: Document) -> None:
    for _ in range(4):
        doc.add_paragraph()

    titulo = doc.add_paragraph(style="Title")
    titulo.alignment = WD_ALIGN_PARAGRAPH.CENTER
    titulo.add_run("Comparativa de consumo de API\ncon Volley, OkHttp y Retrofit")

    subtitulo = doc.add_paragraph()
    subtitulo.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = subtitulo.add_run("Tarea 2.5 · Trabajo individual")
    run.font.size = Pt(14)
    run.font.color.rgb = GRIS
    run.bold = True

    doc.add_paragraph()

    datos = [
        ("Estudiante", "José Fernando Orellana Reyes"),
        ("Número de cuenta", "202210010352"),
        ("Repositorio", "Kirklen0910/Exa1Pa1199"),
        ("Rama", "feature/comparativa-fernando-orellana"),
        ("Pantalla", "ComparativaFernandoActivity"),
        ("Endpoint principal", "GET /api/registros"),
    ]
    tabla = doc.add_table(rows=0, cols=2)
    tabla.alignment = WD_TABLE_ALIGNMENT.CENTER
    tabla.style = "Table Grid"
    for etiqueta, valor in datos:
        fila = tabla.add_row().cells
        fila[0].text = etiqueta
        fila[1].text = valor
        for parrafo in fila[0].paragraphs:
            for run in parrafo.runs:
                run.bold = True
        fila[0].width = Cm(4.5)
        fila[1].width = Cm(10.5)

    doc.add_paragraph()
    nota = doc.add_paragraph()
    nota.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = nota.add_run(
        "Informe generado a partir de la implementación y de las mediciones\n"
        "realizadas en el emulador Pixel 8 (Android 15)."
    )
    run.font.size = Pt(9)
    run.font.color.rgb = GRIS
    run.italic = True

    doc.add_page_break()


def agregar_runs(parrafo, texto: str) -> None:
    """Agrega texto con **negrita** y `código` inline."""
    patron = re.compile(r"(\*\*.+?\*\*|`[^`]+`|\[[^\]]+\]\([^)]+\))")
    posicion = 0
    for coincidencia in patron.finditer(texto):
        if coincidencia.start() > posicion:
            parrafo.add_run(texto[posicion:coincidencia.start()])
        fragmento = coincidencia.group(0)
        if fragmento.startswith("**"):
            run = parrafo.add_run(fragmento[2:-2])
            run.bold = True
        elif fragmento.startswith("`"):
            run = parrafo.add_run(fragmento[1:-1])
            run.font.name = "Consolas"
            run.font.size = Pt(9)
        else:
            m = re.match(r"\[([^\]]+)\]\(([^)]+)\)", fragmento)
            if m:
                parrafo.add_run(m.group(1))
                parrafo.add_run(f" ({m.group(2)})").font.size = Pt(8)
        posicion = coincidencia.end()
    if posicion < len(texto):
        parrafo.add_run(texto[posicion:])


def agregar_tabla(doc: Document, filas: list[list[str]]) -> None:
    if len(filas) < 2:
        return
    encabezado = filas[0]
    datos = filas[1:]
    tabla = doc.add_table(rows=1, cols=len(encabezado))
    tabla.style = "Light Grid Accent 1"
    tabla.alignment = WD_TABLE_ALIGNMENT.CENTER
    tabla.autofit = True

    for i, texto in enumerate(encabezado):
        celda = tabla.rows[0].cells[i]
        celda.text = ""
        parrafo = celda.paragraphs[0]
        agregar_runs(parrafo, texto)
        for run in parrafo.runs:
            run.bold = True
            run.font.size = Pt(8.5)

    for fila in datos:
        celdas = tabla.add_row().cells
        for i, texto in enumerate(fila[: len(encabezado)]):
            celdas[i].text = ""
            parrafo = celdas[i].paragraphs[0]
            agregar_runs(parrafo, texto)
            for run in parrafo.runs:
                run.font.size = Pt(8)

    # Evita que una fila se parta entre dos páginas y repite el encabezado.
    for indice, fila in enumerate(tabla.rows):
        trPr = fila._tr.get_or_add_trPr()
        trPr.append(OxmlElement("w:cantSplit"))
        if indice == 0:
            trPr.append(OxmlElement("w:tblHeader"))

    doc.add_paragraph()


def agregar_codigo(doc: Document, lineas: list[str]) -> None:
    tabla = doc.add_table(rows=1, cols=1)
    tabla.alignment = WD_TABLE_ALIGNMENT.CENTER
    tabla.style = "Table Grid"
    celda = tabla.rows[0].cells[0]
    celda.text = ""
    sombreado = OxmlElement("w:shd")
    sombreado.set(qn("w:fill"), "F3F4F6")
    celda._tc.get_or_add_tcPr().append(sombreado)

    for i, linea in enumerate(lineas):
        parrafo = celda.paragraphs[0] if i == 0 else celda.add_paragraph()
        parrafo.paragraph_format.space_after = Pt(0)
        parrafo.paragraph_format.line_spacing = 1.0
        run = parrafo.add_run(linea if linea else " ")
        run.font.name = "Consolas"
        run.font.size = Pt(8)
        run.font.color.rgb = RGBColor(0x1F, 0x1F, 0x1F)
    doc.add_paragraph()


def agregar_imagen(doc: Document, ruta: Path, ancho_max_cm: float = 16.0) -> None:
    try:
        with Image.open(ruta) as imagen:
            ancho_px, alto_px = imagen.size
    except Exception:
        return
    ancho_cm = min(ancho_max_cm, 16.0)
    alto_cm = ancho_cm * alto_px / ancho_px
    if alto_cm > 22.0:
        alto_cm = 22.0
        ancho_cm = alto_cm * ancho_px / alto_px
    doc.add_picture(str(ruta), width=Cm(ancho_cm))
    doc.paragraphs[-1].alignment = WD_ALIGN_PARAGRAPH.CENTER
    pie = doc.add_paragraph()
    pie.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = pie.add_run(ruta.stem.replace("_", " ").replace("-", " "))
    run.font.size = Pt(8)
    run.font.color.rgb = GRIS
    run.italic = True


def procesar_markdown(doc: Document, texto: str) -> None:
    # La portada ya incluye el título y la ficha del estudiante. Se empieza en el
    # primer apartado numerado para no duplicar esos datos en la página 2.
    if "\n## " in texto:
        texto = "## " + texto.split("\n## ", 1)[1]

    lineas = texto.splitlines()
    i = 0
    primer_titulo = True

    while i < len(lineas):
        linea = lineas[i]
        desnuda = linea.strip()

        if not desnuda:
            i += 1
            continue

        if desnuda == "---":
            parrafo = doc.add_paragraph()
            parrafo.paragraph_format.space_before = Pt(2)
            parrafo.paragraph_format.space_after = Pt(8)
            pPr = parrafo._p.get_or_add_pPr()
            borde = OxmlElement("w:pBdr")
            inferior = OxmlElement("w:bottom")
            inferior.set(qn("w:val"), "single")
            inferior.set(qn("w:sz"), "6")
            inferior.set(qn("w:color"), "B0B7C3")
            borde.append(inferior)
            pPr.append(borde)
            i += 1
            continue

        if desnuda.startswith("```"):
            i += 1
            codigo = []
            while i < len(lineas) and not lineas[i].strip().startswith("```"):
                codigo.append(lineas[i])
                i += 1
            i += 1
            agregar_codigo(doc, codigo)
            continue

        if desnuda.startswith("|"):
            filas = []
            while i < len(lineas) and lineas[i].strip().startswith("|"):
                fila = [c.strip() for c in lineas[i].strip().strip("|").split("|")]
                if not all(re.fullmatch(r":?-{2,}:?", c) for c in fila):
                    filas.append(fila)
                i += 1
            agregar_tabla(doc, filas)
            continue

        if desnuda.startswith("#"):
            nivel = len(desnuda) - len(desnuda.lstrip("#"))
            titulo = desnuda[nivel:].strip()
            if nivel == 1 and primer_titulo:
                primer_titulo = False
                i += 1
                continue
            if nivel == 1:
                doc.add_page_break()
                doc.add_heading(titulo, level=1)
            elif nivel == 2:
                doc.add_heading(titulo, level=1)
            elif nivel == 3:
                doc.add_heading(titulo, level=2)
            else:
                doc.add_heading(titulo, level=3)
            i += 1
            continue

        if desnuda.startswith(">"):
            texto_cita = []
            while i < len(lineas) and lineas[i].strip().startswith(">"):
                texto_cita.append(lineas[i].strip().lstrip(">").strip())
                i += 1
            parrafo = doc.add_paragraph()
            parrafo.paragraph_format.left_indent = Cm(0.8)
            parrafo.paragraph_format.right_indent = Cm(0.8)
            run = parrafo.add_run(" ".join(texto_cita))
            run.italic = True
            run.font.size = Pt(9)
            run.font.color.rgb = GRIS
            continue

        if desnuda.startswith("- "):
            while i < len(lineas) and lineas[i].strip().startswith("- "):
                texto_item = lineas[i].strip()[2:]
                i += 1
                while i < len(lineas):
                    siguiente = lineas[i].strip()
                    if (
                        not siguiente
                        or siguiente == "---"
                        or siguiente.startswith(("#", "```", "|", ">", "- "))
                        or re.match(r"^\d+\.\s", siguiente)
                    ):
                        break
                    texto_item += " " + siguiente
                    i += 1
                parrafo = doc.add_paragraph(style="List Bullet")
                agregar_runs(parrafo, texto_item)
            continue

        if re.match(r"^\d+\.\s", desnuda):
            while i < len(lineas) and re.match(r"^\d+\.\s", lineas[i].strip()):
                texto_item = re.sub(r"^\d+\.\s+", "", lineas[i].strip())
                i += 1
                while i < len(lineas):
                    siguiente = lineas[i].strip()
                    if (
                        not siguiente
                        or siguiente == "---"
                        or siguiente.startswith(("#", "```", "|", ">", "- "))
                        or re.match(r"^\d+\.\s", siguiente)
                    ):
                        break
                    texto_item += " " + siguiente
                    i += 1
                parrafo = doc.add_paragraph(style="List Number")
                agregar_runs(parrafo, texto_item)
            continue

        parrafos = [desnuda]
        i += 1
        while i < len(lineas):
            siguiente = lineas[i].strip()
            if (
                not siguiente
                or siguiente == "---"
                or siguiente.startswith(("#", "```", "|", ">", "- "))
                or re.match(r"^\d+\.\s", siguiente)
            ):
                break
            parrafos.append(siguiente)
            i += 1
        parrafo = doc.add_paragraph()
        agregar_runs(parrafo, " ".join(parrafos))


def agregar_anexo_capturas(doc: Document) -> None:
    imagenes = sorted(
        p for p in CAPTURAS.glob("*")
        if p.suffix.lower() in {".png", ".jpg", ".jpeg", ".webp"}
    )
    if not imagenes:
        return
    doc.add_page_break()
    doc.add_heading("Anexo: capturas del emulador", level=1)
    intro = doc.add_paragraph(
        "Capturas tomadas durante la ejecución de la pantalla Comparativa Fernando. "
        "Cada imagen muestra la cantidad de registros, el tiempo de respuesta o el "
        "detalle técnico del escenario correspondiente."
    )
    intro.runs[0].font.size = Pt(9)
    for ruta in imagenes:
        doc.add_paragraph()
        agregar_imagen(doc, ruta)


def main() -> None:
    texto = MARKDOWN.read_text(encoding="utf-8")
    doc = Document()
    configurar_documento(doc)
    agregar_pie(doc)
    agregar_portada(doc)
    procesar_markdown(doc, texto)
    agregar_anexo_capturas(doc)
    doc.save(SALIDA_DOCX)
    print(f"DOCX generado: {SALIDA_DOCX}")

    import subprocess
    subprocess.run(
        [
            "soffice",
            "--headless",
            "--convert-to",
            "pdf",
            "--outdir",
            str(SALIDA_PDF.parent),
            str(SALIDA_DOCX),
        ],
        check=True,
    )
    print(f"PDF generado: {SALIDA_PDF}")


if __name__ == "__main__":
    main()
