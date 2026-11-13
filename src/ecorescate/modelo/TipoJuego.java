package ecorescate.modelo;

/**
 * Los cuatro minijuegos de la suite. Cada uno guarda sus textos, su contenido educativo
 * (concepto, datos y preguntas) y los parámetros del módulo de Impacto Social (RN-02).
 */
public enum TipoJuego {

    LIMPIEZA("Limpieza Monstruosa", "LA PLAZA", 0x23786A, 0x23786A,
            "Recolectar y clasificar residuos",
            "Usar el tacho correcto",
            "Vidas",
            "Cada residuo tiene su tacho. Si separamos el plástico, el papel y el metal, "
                    + "se pueden reciclar y no ensucian la ciudad.",
            new String[]{
                "Camina con las flechas (o haz clic donde quieras ir) y atrapa a los monstruos de basura.",
                "Llévalos a su tacho y quédate un momento frente a él. ¡Mira de qué material es cada monstruo!",
                "Esquiva el lodo tóxico y no dejes que la plaza se llene de basura."
            },
            "Separa tu basura antes de botarla: el plástico, el papel y el metal van en tachos distintos. "
                    + "Fíjate en el letrero de cada tacho.",
            new String[]{
                "Una botella de plástico puede tardar más de 400 años en desaparecer.",
                "El papel y el cartón se pueden reciclar varias veces.",
                "Un vaso descartable es plástico: va al tacho de plástico.",
                "Las latas de metal se reciclan muchas veces sin perder calidad.",
                "Si mezclamos la basura, ya no se puede reciclar fácilmente.",
                "Reciclar papel ayuda a que se corten menos árboles.",
                "Un periódico es papel: va con las cajas y las hojas.",
                "Una lata de atún también es metal, igual que una lata de gaseosa.",
                "Enjuaga los envases antes de reciclarlos para que no ensucien lo demás.",
                "Con 20 botellas de plástico recicladas se puede hacer el relleno de una casaca.",
                "Una lata reciclada puede volver a la tienda como lata nueva en unos 2 meses.",
                "Cada peruano produce en promedio casi 1 kilo de basura al día.",
                "Con papel reciclado se fabrican cuadernos, cajas y hasta papel higiénico.",
                "Las cáscaras y restos de comida pueden convertirse en abono para las plantas."
            },
            new Pregunta[]{
                new Pregunta("¿En qué tacho va una botella de plástico?",
                        new String[]{"En el de plástico", "En el de papel", "En el de metal"}, 0,
                        "Las botellas, los vasos y los envases de plástico van al tacho de plástico."),
                new Pregunta("¿Dónde va una caja de cartón?",
                        new String[]{"En el de metal", "En el de papel y cartón", "En el de plástico"}, 1,
                        "El cartón es un tipo de papel: va en el tacho de papel y cartón."),
                new Pregunta("¿Por qué separamos la basura?",
                        new String[]{"Para que se pueda reciclar", "Para que el tacho pese menos", "Porque es más divertido"}, 0,
                        "Separada, la basura se puede reciclar y no termina en los ríos ni en el mar."),
                new Pregunta("¿En qué tacho va una lata de atún?",
                        new String[]{"En el de papel", "En el de metal", "En el de plástico"}, 1,
                        "Las latas de comida y de gaseosa son de metal."),
                new Pregunta("¿Dónde va un vaso descartable?",
                        new String[]{"En el de papel", "En el de metal", "En el de plástico"}, 2,
                        "Los vasos descartables son de plástico."),
                new Pregunta("¿Qué hacemos antes de reciclar una botella?",
                        new String[]{"La enjuagamos", "La rompemos", "La pintamos"}, 0,
                        "Enjuagar los envases evita que ensucien lo demás y se puedan reciclar.")
            },
            "kg de residuos reciclados", 0.05, 0.25),

    AGUA("Guardián del Agua", "LOS LAVADEROS", 0x2E80A8, 0x2E80A8,
            "Cerrar caños abiertos",
            "No desperdiciar agua",
            "Tanque",
            "El agua es limitada, sobre todo en Ica. Solo abre el caño cuando de verdad la usas: "
                    + "ciérralo mientras te cepillas o te enjabonas.",
            new String[]{
                "Observa qué hace cada niño en el lavadero.",
                "Si se cepilla o se enjabona con el caño abierto, o si el caño quedó abierto o gotea: ¡haz clic para cerrarlo!",
                "Si se está enjuagando, espera: ahí sí necesita el agua."
            },
            "Cierra el caño mientras te cepillas o te enjabonas las manos. "
                    + "Un caño abierto bota unos 6 litros de agua por minuto.",
            new String[]{
                "Un caño abierto bota unos 6 litros de agua por minuto.",
                "Si cierras el caño al cepillarte, ahorras hasta 12 litros cada vez.",
                "Un caño que gotea puede perder más de 30 litros en un día.",
                "Ica está en el desierto: aquí el agua es todavía más valiosa.",
                "Enjabónate con el caño cerrado y ábrelo solo para enjuagarte.",
                "Si ves un caño que gotea, avisa a un adulto para que lo arreglen.",
                "Usa un vaso para enjuagarte la boca después de cepillarte.",
                "El agua que ahorras hoy la pueden usar otras familias mañana.",
                "Cerrar bien el caño toma solo un segundo y ahorra mucha agua.",
                "Solo una parte muy pequeña del agua del planeta es dulce y se puede beber.",
                "Una ducha de 5 minutos gasta mucha menos agua que llenar una tina.",
                "El agua de lavar verduras puede reutilizarse para regar las plantas.",
                "Nuestro cuerpo está hecho en su mayor parte de agua.",
                "Los ríos de Ica nacen en la sierra: el agua viaja muy lejos para llegar a tu caño."
            },
            new Pregunta[]{
                new Pregunta("¿Qué haces mientras te cepillas los dientes?",
                        new String[]{"Dejo el caño abierto", "Cierro el caño", "Abro más el caño"}, 1,
                        "Cerrar el caño mientras te cepillas ahorra hasta 12 litros cada vez."),
                new Pregunta("Un caño que gotea…",
                        new String[]{"No importa, son gotitas", "Desperdicia muchos litros al día", "Ayuda a limpiar el lavadero"}, 1,
                        "Gota a gota, un caño puede perder más de 30 litros en un día."),
                new Pregunta("¿Cuándo sí necesitas el caño abierto?",
                        new String[]{"Al enjuagarme las manos", "Mientras me enjabono", "Cuando me voy del baño"}, 0,
                        "Abre el caño solo cuando de verdad usas el agua, como al enjuagarte."),
                new Pregunta("¿Qué haces si ves un caño que gotea en el colegio?",
                        new String[]{"Nada", "Aviso a un adulto", "Lo abro más"}, 1,
                        "Avisar a un profesor o al personal ayuda a que lo reparen y no se pierda agua."),
                new Pregunta("¿Cuánta agua bota un caño abierto en un minuto?",
                        new String[]{"Una gotita", "Unos 6 litros", "Nada"}, 1,
                        "Un caño abierto bota unos 6 litros por minuto: ¡es mucha agua!")
            },
            "litros de agua ahorrados", 6.0, 10.0),

    ENERGIA("Apaga y Ahorra", "EL COLEGIO", 0xDFA855, 0xCF953A,
            "Apagar luces y equipos",
            "Ahorrar energía",
            "Energía",
            "La energía se gasta aunque nadie la use. Apaga luces, equipos y cargadores en los "
                    + "ambientes vacíos, pero no donde alguien los necesita.",
            new String[]{
                "Mira qué ambientes están vacíos y cuáles tienen alumnos.",
                "Haz clic en las luces, equipos y cargadores prendidos de los ambientes vacíos.",
                "No apagues lo que los alumnos están usando."
            },
            "Al salir de un ambiente apaga la luz, el televisor y la computadora. "
                    + "Desenchufa los cargadores que no estés usando.",
            new String[]{
                "Una luz prendida en un aula vacía gasta energía sin ayudar a nadie.",
                "Los focos LED gastan mucho menos energía que los focos antiguos.",
                "Aprovecha la luz del sol: abre las cortinas antes de prender la luz.",
                "Un cargador enchufado sin celular sigue gastando un poquito de energía.",
                "Un televisor en espera también gasta energía.",
                "Ahorrar energía también ayuda a ahorrar dinero en tu casa.",
                "Al salir del aula, el último en salir apaga la luz.",
                "Para producir electricidad muchas veces se queman combustibles que contaminan.",
                "Apaga la computadora al terminar de usarla, no la dejes encendida toda la noche.",
                "En Perú, mucha electricidad viene de la fuerza del agua de los ríos.",
                "Los paneles solares convierten la luz del sol en electricidad.",
                "Ica tiene sol casi todo el año: ¡es un buen lugar para la energía solar!",
                "Abrir la refrigeradora muchas veces la hace gastar más energía.",
                "Un ventilador gasta mucho menos energía que un aire acondicionado."
            },
            new Pregunta[]{
                new Pregunta("Sales del aula y no queda nadie. ¿Qué haces?",
                        new String[]{"Dejo la luz prendida", "Apago la luz y los equipos", "Prendo el ventilador"}, 1,
                        "Si nadie usa el ambiente, todo lo que queda prendido es energía perdida."),
                new Pregunta("¿Puedes apagar la luz de un aula con alumnos estudiando?",
                        new String[]{"Sí, siempre", "No, ellos la están usando", "Solo si estoy aburrido"}, 1,
                        "Ahorrar es apagar lo que nadie usa, no lo que alguien necesita."),
                new Pregunta("Un cargador enchufado sin celular…",
                        new String[]{"No gasta nada", "Sigue gastando energía", "Se apaga solo"}, 1,
                        "Los cargadores enchufados siguen gastando un poco de energía: desenchúfalos."),
                new Pregunta("¿Qué haces antes de prender la luz de día?",
                        new String[]{"Abro las cortinas", "Cierro las ventanas", "Prendo todas las luces"}, 0,
                        "Si entra luz del sol, muchas veces no hace falta prender la luz."),
                new Pregunta("¿Qué foco gasta menos energía?",
                        new String[]{"El foco LED", "El foco antiguo", "Gastan igual"}, 0,
                        "Los focos LED alumbran igual y gastan mucha menos energía.")
            },
            "kWh de energía ahorrados", 0.05, 0.2),

    VERDE("Sembradores del Verde", "EL BIOHUERTO", 0x4E9A45, 0x4E9A45,
            "Sembrar y cuidar plantas",
            "Proteger áreas verdes",
            "Biohuerto",
            "Las plantas son seres vivos: necesitan agua cuando la tierra está seca y un lugar "
                    + "limpio para crecer. Regar de más también desperdicia agua.",
            new String[]{
                "Elige una herramienta abajo (o usa las teclas 1, 2 y 3).",
                "Siembra en la tierra libre y riega solo cuando la planta tenga sed (gota roja).",
                "Recoge la basura y las plantas secas para cuidar el biohuerto."
            },
            "Cuida las plantas del colegio y de tu casa: riégalas cuando la tierra esté seca, "
                    + "no las pises y nunca botes basura en los jardines.",
            new String[]{
                "Las plantas dan sombra y bajan el calor del colegio.",
                "Regar de más también hace daño: las raíces se pueden pudrir.",
                "La basura en los jardines ensucia la tierra y daña las plantas.",
                "Riega temprano o al atardecer para que el agua no se evapore con el sol.",
                "Las áreas verdes son el hogar de aves, mariposas y otros insectos.",
                "Las plantas limpian el aire que respiramos.",
                "Un biohuerto puede darnos verduras sanas para comer.",
                "Toca la tierra: si está seca, la planta necesita agua.",
                "Una planta seca se retira para dejar espacio a una nueva.",
                "El huarango es un árbol de Ica que resiste la sequía y puede vivir cientos de años.",
                "Las abejas ayudan a que las flores se conviertan en frutos.",
                "Un árbol puede bajar varios grados la temperatura bajo su sombra.",
                "Las raíces de las plantas sujetan la tierra y evitan que se la lleve el viento.",
                "Las semillas necesitan agua, aire y una temperatura adecuada para germinar."
            },
            new Pregunta[]{
                new Pregunta("¿Cuándo debes regar una planta?",
                        new String[]{"Cuando la tierra está seca", "Cada minuto", "Nunca"}, 0,
                        "Riega cuando la tierra está seca: mucha agua también daña la planta y se desperdicia."),
                new Pregunta("¿Qué le pasa a un jardín con basura?",
                        new String[]{"Crece mejor", "Se ensucia y las plantas se enferman", "No le pasa nada"}, 1,
                        "La basura ensucia la tierra y no deja crecer a las plantas."),
                new Pregunta("¿Para qué sirven las áreas verdes?",
                        new String[]{"Dan sombra y aire limpio", "Solo sirven de adorno", "Para botar basura"}, 0,
                        "Las plantas limpian el aire, dan sombra y son el hogar de muchos animales."),
                new Pregunta("¿Qué pasa si riegas una planta que ya tiene la tierra húmeda?",
                        new String[]{"Crece el doble", "Se desperdicia agua y se pueden pudrir sus raíces", "Nada"}, 1,
                        "Las raíces también necesitan aire: el exceso de agua las daña."),
                new Pregunta("¿Qué animalitos ayudan a las flores?",
                        new String[]{"Las abejas y mariposas", "Las moscas de la basura", "Ninguno"}, 0,
                        "Abejas y mariposas llevan el polen de flor en flor y así nacen los frutos.")
            },
            "m² de área verde cuidada", 0.5, 1.0);

    private final String nombre;
    private final String lugar;
    private final int color;
    private final int colorTexto;
    private final String enElJuego;
    private final String enLaVidaReal;
    private final String nombreMedidor;
    private final String concepto;
    private final String[] instrucciones;
    private final String consejo;
    private final String[] datos;
    private final Pregunta[] preguntas;
    private final String unidadImpacto;
    private final double impactoPorAcierto;
    private final double impactoPorNivel;

    TipoJuego(String nombre, String lugar, int color, int colorTexto, String enElJuego, String enLaVidaReal,
              String nombreMedidor, String concepto, String[] instrucciones, String consejo, String[] datos,
              Pregunta[] preguntas, String unidadImpacto, double impactoPorAcierto, double impactoPorNivel) {
        this.nombre = nombre;
        this.lugar = lugar;
        this.color = color;
        this.colorTexto = colorTexto;
        this.enElJuego = enElJuego;
        this.enLaVidaReal = enLaVidaReal;
        this.nombreMedidor = nombreMedidor;
        this.concepto = concepto;
        this.instrucciones = instrucciones;
        this.consejo = consejo;
        this.datos = datos;
        this.preguntas = preguntas;
        this.unidadImpacto = unidadImpacto;
        this.impactoPorAcierto = impactoPorAcierto;
        this.impactoPorNivel = impactoPorNivel;
    }

    public String getNombre() { return nombre; }
    /** Lugar de la misión, por ejemplo "LA PLAZA". */
    public String getLugar() { return lugar; }
    public int getColor() { return color; }
    public int getColorTexto() { return colorTexto; }
    public String getEnElJuego() { return enElJuego; }
    public String getEnLaVidaReal() { return enLaVidaReal; }
    public String getNombreMedidor() { return nombreMedidor; }
    /** Idea principal que el niño debe aprender. */
    public String getConcepto() { return concepto; }
    public String[] getInstrucciones() { return instrucciones.clone(); }
    public String getConsejo() { return consejo; }
    /** Datos "¿Sabías que…?" que aparecen al subir de nivel. */
    public String[] getDatos() { return datos.clone(); }
    public Pregunta[] getPreguntas() { return preguntas.clone(); }
    public String getUnidadImpacto() { return unidadImpacto; }
    public double getImpactoPorAcierto() { return impactoPorAcierto; }
    public double getImpactoPorNivel() { return impactoPorNivel; }
}
