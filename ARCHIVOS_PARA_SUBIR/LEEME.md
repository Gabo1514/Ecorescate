# Operación EcoRescate

Suite de 4 minijuegos educativos en **Java (Swing)** para el cuidado del ambiente, dirigida a estudiantes de primaria.

## Cómo abrirlo

- **Jugar:** doble clic en `ejecutar.bat` (o en `EcoRescate.jar`). Necesita Java 8 o superior.
- **Volver a compilar:** doble clic en `compilar.bat` (necesita el JDK).
- **NetBeans:** *File → New Project → Java with Existing Sources*, carpeta de fuentes `src`, clase principal `ecorescate.Main`.
- **VS Code / IntelliJ:** abrir la carpeta `EcoRescate` y ejecutar `src/ecorescate/Main.java`.

## Los 4 minijuegos

| Minijuego | En el juego | En la vida real | Medidor |
|---|---|---|---|
| Limpieza Monstruosa | Caminar por la plaza con las flechas, atrapar monstruos de basura en la mochila (máx. 5) y llevarlos al tacho de su material (plástico, papel, metal). Monstruos nuevos por nivel: Vasito, Periodicón, Atunín. Estrella de poder, lodo tóxico y barra de invasión | Usar el tacho correcto | 3 vidas |
| Guardián del Agua | Cerrar los caños abandonados, los que gotean y los que quedan abiertos mientras un niño se cepilla o se enjabona; dejar abierto el de quien se enjuaga | No desperdiciar agua | Tanque |
| Apaga y Ahorra | Apagar luces y equipos en ambientes **vacíos** (si hay alumnos, no se apaga) y desenchufar cargadores sin celular | Ahorrar energía | Energía |
| Sembradores del Verde | Con 3 herramientas (semilla, regadera, recoger): sembrar, regar solo cuando la tierra está seca (regar de más es error) y recoger basura y plantas secas | Proteger áreas verdes | Biohuerto |

## Niveles, retos y estrellas

- Cada minijuego tiene **10 niveles** (40 en total) en un **mapa de niveles**. El nivel siguiente se abre al superar el anterior.
- Cada nivel tiene **retos** concretos, por ejemplo: *"Recicla 3 Botellines"*, *"Detén 3 caños que gotean"*, *"Desenchufa 2 cargadores"*, *"Haz florecer 6 plantas"*, *"Llega a COMBO ×4"*. El avance se ve arriba durante el juego (por ejemplo, "Botellín 2/3").
- Cada nivel tiene **tiempo límite**. Se pierde si se acaba el tiempo o el medidor (vidas, tanque, energía o biohuerto).
- **Estrellas por nivel:** ★ completar los retos · ★★ con pocos errores · ★★★ casi sin errores y rápido. Lo que pide cada estrella se muestra antes de jugar, y el mapa guarda la mejor marca de cada nivel.
- **La dificultad sube nivel a nivel:** aparecen cosas nuevas (nuevos monstruos, lodo tóxico, goteos, niños que se enjuagan, cargadores, calor de Ica), todo va más rápido, hay menos tiempo y se toleran menos errores para las 3 estrellas. En Apaga y Ahorra, los botones rojos de ayuda desaparecen desde el nivel 5.
- Los niveles se definen en `modelo/CatalogoNiveles.java` (fácil de ajustar).

## Gráficos

- El estilo es caricatura, con contornos y personajes animados.
- **Limpieza Monstruosa:** el cielo cambia con los niveles (mañana, tarde, atardecer y anochecer), con nubes, pájaros y árboles que se mecen.
- **Guardián del Agua:** los niños entran y salen caminando, y hay espejos con brillo, jaboneras, una plantita y un letrero.
- **Apaga y Ahorra:** va anocheciendo nivel a nivel, con estrellas y luna, y se nota qué aulas tienen la luz prendida.
- **Sembradores del Verde:** hay mariposas, abejas en las flores, nubes y, desde el nivel 5, un sol de calor.

## Para que enganche (motivación)

- **Sonidos y música** generados por el programa (sin archivos). Botón de parlante en cada juego para silenciar.
- **Combo** por aciertos seguidos con palabras grandes: ¡BIEN!, ¡GENIAL!, ¡SÚPER!, ¡ECO-HÉROE!
- **Confeti** al superar un nivel; la pantalla **tiembla** un poquito al equivocarse.
- **EcoMonedas** para la **Tienda**: gorra, lentes, audífonos, sombrero, capa y corona, más 6 colores de polo.
- **14 medallas** coleccionables (Explorador, ¡Perfecto!, Combo máximo, Sin errores, Sabelotodo, superar los 10 niveles de cada juego, Coleccionista, Eco-héroe…).
- **Ranking del salón**: ordena a los alumnos del mismo grado y sección por estrellas (y en empate, por puntaje).
- El progreso de cada alumno se guarda en `datos/perfiles/`.

## Parte educativa

- **Nivel 1:** tarjeta "¿Qué vas a aprender?" con el concepto del juego y "Cómo se juega".
- **Cada nivel:** muestra lo nuevo que aparece y un **dato curioso** (14 por juego, sobre Ica y el Perú), y a mitad del nivel aparece otro "¿Sabías que…?".
- **Cada error explica por qué** estuvo mal (por ejemplo: "Vasito es un vaso descartable: va en el tacho de plástico").
- **Niveles 3, 6, 9 y 10:** terminan con un **Desafío del saber** (pregunta de 3 opciones, +30 puntos). La respuesta se guarda en el CSV (`pregunta_correcta`).

## Estructura del código (POO)

```
src/ecorescate/
├── Main.java                      punto de entrada
├── modelo/                        clases de negocio
│   ├── Estudiante.java            alumno registrado (RN-01)
│   ├── SesionJuego.java           una partida (un nivel jugado)
│   ├── PuntajeObtenido.java       puntos, aciertos, errores, precisión (RF-05)
│   ├── NivelAlcanzado.java        nivel del 1 al 10 y si lo superó
│   ├── Nivel.java, Reto.java      definición de un nivel, sus retos y estrellas
│   ├── CatalogoNiveles.java       los 40 niveles
│   ├── ModuloImpactoSocial.java   convierte el juego a litros, kg, kWh y m² (RF-06)
│   ├── TipoJuego.java             textos, datos curiosos, preguntas y factores (RN-02)
│   ├── Perfil.java, Progreso.java estrellas por nivel, monedas y medallas
│   └── RegistroPartidas.java      guarda las partidas en datos/partidas.csv
├── juegos/
│   ├── Minijuego.java             clase abstracta: ciclo de juego, retos, tiempo, estrellas, pantallas
│   ├── LimpiezaMonstruosa.java    RF-01
│   ├── GuardianAgua.java          RF-02
│   ├── ApagaAhorra.java           RF-03
│   ├── SembradoresVerde.java      RF-04
│   └── Personaje.java, Estilo.java, Efectos.java, BotonLienzo.java
└── ui/
    ├── VentanaPrincipal.java      ventana única (CardLayout)
    ├── PanelMenu.java             menú central con las 4 tarjetas (RF-07)
    ├── PanelNiveles.java          mapa de niveles
    ├── PanelTienda.java, PanelMedallas.java, PanelRanking.java, PanelImpacto.java
    └── Tema.java, Iconos.java, BotonEco.java, Sonido.java
```

- **Herencia y polimorfismo:** los 4 juegos extienden `Minijuego` e implementan `reiniciar()`, `actualizar()`, `dibujarEscena()`, etc.
- **Encapsulamiento:** el modelo no depende de la interfaz gráfica.
- **Fábrica:** `Minijuego.crear(tipo, alumno, impacto, nivel)` crea el juego correcto.

## Registro de partidas

Cada nivel jugado se guarda en `datos/partidas.csv` (separado por `;`, se abre en Excel) con: fecha, alumno, grado, sección, minijuego, puntaje, nivel, si lo superó, aciertos, errores, precisión, duración, impacto, pregunta correcta y estrellas. Sirve como *ficha de registro de desempeño* para el análisis en SPSS.

## Factores de Impacto Social (RN-02)

Se cambian en `TipoJuego.java`:

| Minijuego | Por acierto | Por nivel superado |
|---|---|---|
| Limpieza Monstruosa | 0,05 kg | 0,25 kg |
| Guardián del Agua | 6 L | 10 L |
| Apaga y Ahorra | 0,05 kWh | 0,2 kWh |
| Sembradores del Verde | 0,5 m² | 1 m² |
