# PROMPTS.md · Fase 2 (rama `mejora-ia`)

## Prompts Completos

1. **Prompt 1**

Reglas fijas para este y todos los prompts:
- Arquitectura MVVM + Repository. El ViewModel NO recibe Context, NO usa android.util.Log y solo conoce las interfaces de los Repository. Los Composables NO calculan distancias, NO filtran datos de negocio y NO conocen los Repository.
- Solo Jetpack Compose Material 3. Sin XML nuevo, sin imágenes (png/svg/drawable) y SIN dependencias nuevas. Usa solo íconos de Icons.Default del set básico (Place, Info, MoreVert, PlayArrow, Refresh, Settings); cualquier otro ícono dibújalo con Canvas.
- El mapa es osmdroid (no Google Maps). No pongas claves de API.
- Entrégame los archivos COMPLETOS con todos sus imports, y al final una lista de qué cambió en cada archivo y por qué. No cambies lógica de ViewModel ni Repository en este prompt. Evita casts como (estado as UbicacionUi.Error): usa when o smart cast.

TAREA: rediseñar toda la UI con la identidad visual de las imágenes que adjunto (barra verde azulado oscuro con ondas turquesa y naranja, fondo crema). Mantén todo el comportamiento actual (permiso, errores, selección, más cercano, distancias).

1) TEMA (ui/theme/Color.kt y Theme.kt)
   Colores (ajusta ligeramente si las imágenes se ven distintas):
- VerdeOscuro #0F4C5C, Turquesa #1FA2A6, Naranja #F28C4F, Crema #F7F2E9, Blanco #FFFFFF
- TurquesaClaro #DDEFEF, TextoSecundario #5F7A80, BordeSuave #E1EAEA
- ErrorFondo #FADAD6, ErrorBorde #E57373, ErrorTexto #8B1A1A
- VerdeCercano #34A853, AzulLugar #1E88E5 (los marcadores y puntos de lista siguen usando estos dos)
  ColorScheme claro: primary=VerdeOscuro, onPrimary=Blanco, secondary=Turquesa, tertiary=Naranja, background=Crema, surface=Blanco, onSurface=VerdeOscuro, onSurfaceVariant=TextoSecundario, primaryContainer=TurquesaClaro, onPrimaryContainer=VerdeOscuro, errorContainer=ErrorFondo, onErrorContainer=ErrorTexto. Desactiva dynamicColor y usa solo el esquema claro. Deja constantes públicas para usar los colores fuera del tema.

2) COMPONENTES DECORATIVOS (nuevo archivo ui/Decoracion.kt, todo con Canvas)
   a) OndaSuperior(modifier): Canvas de ancho completo y 40.dp de alto. Se dibuja pegada justo debajo de la barra y es su continuación. De abajo hacia arriba en orden de dibujo: primero una banda NARANJA cuya curva inferior solo se nota en la mitad derecha (llega a ~100% del alto), luego una banda TURQUESA con curva inferior ondulada a ~70% del alto, y encima una banda VERDEOSCURO con curva inferior ondulada a ~45% del alto. Las curvas con Path y cubicTo (por ejemplo moveTo(0,0) -> lineTo(0, y0) -> cubicTo(w*0.25, y0+8dp, w*0.6, y0-8dp, w, y0+4dp) -> lineTo(w,0) -> close). Arriba es el borde de la barra, sin hueco.
   b) OndaInferior(modifier): la misma onda pero invertida verticalmente (usa Modifier.graphicsLayer(scaleY = -1f)), alto 64.dp.
   c) IconoNavegacion(tamaño, color): flecha de navegación dibujada con un Path de 4 puntos: (0.5w,0) -> (w,h) -> (0.5w,0.75h) -> (0,h) -> cerrar.
   d) IconoMira(tamaño, color): círculo con trazo, punto central lleno y 4 marcas cortas arriba/abajo/izquierda/derecha.
   e) PinNaranja(tamaño): pin de ubicación con círculo naranja, punta hacia abajo y círculo blanco interior.
   f) Ilustracion(modifier): Canvas de 260.dp x 190.dp, con estas piezas (posiciones como fracción del ancho w y alto h):
    - Suelo: elipse #DCEEEC centrada en (0.5w, 0.80h), ancho 0.9w, alto 0.20h.
    - Dos nubes #CFE8E8 formadas por 3 círculos cada una: una en (0.18w, 0.40h) y otra en (0.82w, 0.22h).
    - Edificio: cuerpo central rectángulo #8FB3B8 de x 0.28w a 0.78w y y 0.40h a 0.80h; torre central #7FA6AC de x 0.46w a 0.60w y y 0.28h a 0.80h con techo pequeño oscuro; reloj: círculo blanco radio 0.035w en (0.53w, 0.36h) con dos manecillas oscuras; ventanas: rectángulos #E3F1F2 en 2 filas x 3 columnas a cada lado de la torre; puerta VerdeOscuro en la base de la torre.
    - Árboles: uno en x=0.22w y otro en x=0.88w; tronco rectángulo VerdeOscuro y copa de dos elipses Turquesa apiladas.
    - Camino punteado: Path con cubicTo desde (0.12w, 0.76h) hasta (0.80w, 0.82h) pasando por debajo (puntos de control a y=0.97h), trazo Turquesa de 3.dp con PathEffect.dashPathEffect(floatArrayOf(14f, 12f)).
    - Pin grande naranja: círculo radio 0.12w en (0.42w, 0.22h), punta hasta (0.42w, 0.50h), círculo blanco interior radio 0.045w; tres rayitos naranja cortos arriba a la izquierda del pin.
      No hace falta que sea idéntico; que se reconozca.

3) BARRA SUPERIOR (composable BarraCampus en ui/Decoracion.kt o archivo propio)
   TopAppBar de Material 3 con containerColor=VerdeOscuro; title = Row con Icons.Default.Place blanco de 24.dp, espacio de 8.dp y texto "CampusGPS" en blanco, 20.sp, negrita; actions = IconButton con Icons.Default.MoreVert blanco (sin acción). Justo debajo va OndaSuperior. La app usa enableEdgeToEdge: TopAppBar ya respeta la barra de estado, no agregues padding extra arriba.

4) PANTALLA DE INICIO (nuevo archivo ui/PantallaInicio.kt)
   Se muestra cuando el estado es Inicial, Error, o Buscando (si antes ya estaba en inicio). Parámetros: estado: UbicacionUi, onObtenerUbicacion: () -> Unit, onReintentar: () -> Unit.
- Box a pantalla completa con fondo Crema. Column: BarraCampus + OndaSuperior arriba; zona central con weight(1f), centrada, con verticalScroll por si la pantalla es pequeña; OndaInferior alineada abajo con navigationBarsPadding().
- Zona central: Ilustracion (260x190), Spacer 24.dp, el botón principal, Spacer 16.dp y, si el estado es Error, el composable MensajeError con padding horizontal de 24.dp.
- Botón principal: Button con altura 56.dp, ancho completo con padding horizontal de 32.dp, shape CircleShape, containerColor=VerdeOscuro, contentColor=Blanco, elevation de 6.dp. Contenido: IconoNavegacion de 20.dp + 8.dp de espacio + texto "Obtener mi ubicación" de 18.sp peso Medium. Mientras el estado es Buscando: reemplaza el ícono por CircularProgressIndicator de 20.dp (strokeWidth 2.dp, color blanco), el texto pasa a "Buscando..." y enabled=false (containerColor deshabilitado #5F7F86 y texto blanco al 70%). En cualquier otro estado, incluido Error, está habilitado.

5) MENSAJE DE ERROR (ui/MensajeError.kt, mismo contrato y misma lógica)
   Surface con color ErrorFondo, borde de 1.dp ErrorBorde, shape RoundedCornerShape(16.dp), padding interno 16.dp. Dentro, Column centrada: Icons.Default.Info de 22.dp en ErrorTexto, Spacer 6.dp, mensaje en 17.sp peso Medium color ErrorTexto centrado, Spacer 10.dp, y debajo OutlinedButton con shape CircleShape, borde de 1.5.dp ErrorTexto y texto ErrorTexto ("Abrir ajustes" para SIN_PERMISO, "Reintentar" para GPS_APAGADO y OTRO, ninguno para SIN_PLAY_SERVICES). La lógica de Intent a ajustes se mantiene igual.

6) PANTALLA DEL MAPA (en ui/PantallaMapa.kt)
   Se muestra cuando el estado es Lista. Estructura en Column: BarraCampus + OndaSuperior; un Box con weight(1f) que contiene el MapaUbicacion a pantalla completa; y debajo el panel inferior.
- Controles sobre el mapa: dentro del Box, alineado abajo a la derecha con padding de 16.dp, un botón circular "centrar en mi ubicación": Surface de 48.dp, CircleShape, color Blanco, shadowElevation 6.dp, con IconoMira de 24.dp en VerdeOscuro. Deja libres la franja superior del mapa (alto ~56.dp) y la columna derecha sobre ese botón: ahí pondré controles después.
- Panel inferior: Surface de ancho completo, color Blanco, esquinas superiores RoundedCornerShape(28.dp), shadowElevation 12.dp, con navigationBarsPadding(). Columna con padding horizontal 16.dp, arriba 8.dp, abajo 12.dp, así:
    1. Barra de arrastre decorativa: Box de 40.dp x 4.dp, color #C9D6D8, redondeada, centrada.
    2. Spacer 10.dp.
    3. Row con spacedBy(8.dp): Button "Obtener mi ubicación" (weight 1.2f, altura 44.dp, 13.sp, una línea, CircleShape, colores como el botón principal, ícono IconoNavegacion de 16.dp; deshabilitado y con CircularProgressIndicator mientras es Buscando) y OutlinedButton "Ir al más cercano" (weight 1f, altura 44.dp, 13.sp, una línea, CircleShape, borde VerdeOscuro de 1.5.dp, texto VerdeOscuro; deshabilitado si masCercano es null).
    4. Si hay masCercano con distancia: Text "Más cercano: nombre (distancia)" con formatearDistancia, 14.sp negrita VerdeOscuro, centrado, con 8.dp de margen arriba.
    5. Spacer 8.dp y ListaLugares con altura máxima de 200.dp.
- Durante el estado Buscando mantén la pantalla que ya se estaba mostrando para que no parpadee: usa una variable rememberSaveable que guarde si la última pantalla fue el mapa, y actualízala con LaunchedEffect(estado) (Lista -> mapa; Inicial y Error -> inicio; Buscando -> no cambia).
- PantallaMapa sigue siendo el composable raíz: crea el ViewModel con la Factory como ahora, mantiene el launcher de permisos y la función alPulsar, y decide cuál pantalla mostrar con when sobre el estado. Pasa lambdas a las pantallas hijas.

7) LISTA (ui/ListaLugares.kt, mismos parámetros que ahora)
   Tarjetas con shape RoundedCornerShape(14.dp), separadas 8.dp. No seleccionada: fondo Blanco y borde de 1.dp BordeSuave. Seleccionada: fondo TurquesaClaro y borde de 2.dp Turquesa. Fila con padding 12.dp horizontal y 10.dp vertical: punto de 12.dp (VerdeCercano si es el más cercano, AzulLugar si no), 12.dp de espacio, columna con el nombre (16.sp negrita VerdeOscuro) y la categoría (12.sp TextoSecundario), y a la derecha la distancia (16.sp negrita VerdeOscuro) o "Sin calcular".

8) MAPA (ui/MapaUbicacion.kt)
- Agrega el parámetro centrarToken: Int. Reemplaza la animación de cámara actual por un LaunchedEffect cuyas claves son centrarToken y un booleano "estado es Lista" (no el estado completo): anima a la posición actual con zoom 16 durante 1000 ms. Lee el estado actual con rememberUpdatedState para no capturar un valor viejo. Así la cámara solo se mueve cuando pulso "Obtener mi ubicación", cuando pulso el botón de centrar, o al llegar a Lista por primera vez, y no en cada actualización de posición.
- El contador centrarToken vive en la pantalla (rememberSaveable) y se incrementa al pulsar "Obtener mi ubicación" y el botón de centrar.
- Marcador propio: título "Estás aquí" y snippet con las coordenadas ("Lat -12.0464 · Lon -77.0428"). Crea en util/Formato.kt una función pura formatearCoordenadas(latitud, longitud) que devuelva ese texto con 4 decimales y Locale.US.
- NO agregues lugares como clave del LaunchedEffect(seleccionado): es intencional que lea la lista actual al ejecutarse.

2. **Prompt 2**

Reglas fijas: MVVM + Repository; el ViewModel no recibe Context; solo Jetpack Compose Material 3, sin XML nuevo, sin imágenes y sin dependencias nuevas; íconos básicos o Canvas. Entrégame los archivos COMPLETOS con imports y una lista de qué cambió. No toques ViewModel ni Repository. No pongas claves de API.

TAREA: agregar un splash al iniciar la app, en Compose, reutilizando los componentes de ui/Decoracion.kt (OndaSuperior, OndaInferior, PinNaranja) y los colores del tema.

1) Crea ui/PantallaSplash.kt:
- Box a pantalla completa con fondo Crema. OndaSuperior arriba (llega hasta arriba de la pantalla, detrás de la barra de estado) y OndaInferior abajo con navigationBarsPadding().
- Centro: Column centrada con PinNaranja de 96.dp, Spacer 16.dp, texto "CampusGPS" en 32.sp negrita VerdeOscuro, Spacer 4.dp y el texto "Encuentra tu lugar en el campus" en 14.sp color TextoSecundario.
- Animación de entrada: el pin y los textos pasan de alpha 0 a 1 y de escala 0.85 a 1 en 600 ms (Animatable o animateFloatAsState con tween y FastOutSlowInEasing).

2) Crea ui/AppCampusGPS.kt con un composable AppCampusGPS():
- var mostrarSplash by rememberSaveable { mutableStateOf(true) }.
- LaunchedEffect(Unit) { delay(1600); mostrarSplash = false }.
- Crossfade(targetState = mostrarSplash, animationSpec = tween(400)) que muestra PantallaSplash o PantallaMapa().
- Con rememberSaveable el splash no se repite al rotar la pantalla.

3) MainActivity: setContent { CampusGPSTheme { AppCampusGPS() } } y nada más (sin Greeting ni Preview de plantilla).

4) Android 12 o superior muestra además su splash del sistema. No lo modifiques ni agregues la librería core-splashscreen; solo dime cómo se comporta con el mío.

3. **Prompt 3**

Reglas fijas: MVVM + Repository; el ViewModel no recibe Context ni usa android.util.Log y solo conoce interfaces; los Composables no filtran ni calculan datos de negocio, solo dibujan lo que el ViewModel expone. Solo Compose Material 3, sin dependencias nuevas, íconos básicos o Canvas. Entrégame los archivos COMPLETOS con imports y una lista de qué cambió y por qué. No pongas claves de API. Mantén el diseño actual (colores VerdeOscuro, Turquesa, Crema, etc.).

TAREA: filtro por categoría con chips que muestre solo esos lugares en el mapa y en la lista.

ViewModel (MapaViewModel):
- _categoria = MutableStateFlow<String?>(null) con su versión pública de solo lectura (categoriaSeleccionada). null significa Todas.
- val categorias: List<String> sacada de lugaresRepo.obtenerLugares() con distinct() conservando el orden (Estudio, Comida, Eventos, Servicios). No las escribas fijas en la UI.
- val lugaresVisibles: StateFlow<List<LugarConDistancia>> = combine(_lugares, _categoria) { ... }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), valor inicial coherente). Conserva el orden por distancia.
- masCercano debe derivarse de lugaresVisibles (el primero con metros no nulo), no de _lugares.
- fun filtrarPorCategoria(categoria: String?): actualiza _categoria y, si el lugar seleccionado deja de estar en los visibles, pone seleccionado en null.

View:
- Fila de chips sobre el mapa en la pantalla del mapa: LazyRow con espacio de 8.dp, alineada arriba a la izquierda dentro del Box del mapa, con padding de 12.dp. Primer chip "Todas", luego uno por cada categoría. Usa FilterChip de Material 3: seleccionado con containerColor VerdeOscuro y texto blanco; no seleccionado con fondo blanco al 95% de opacidad, borde BordeSuave y texto VerdeOscuro; elevación 2.dp.
- La pantalla del mapa usa lugaresVisibles para MapaUbicacion y ListaLugares, y para el texto "Más cercano". Los Composables solo reciben listas y callbacks; no hay ningún filter {} en la UI.
- Los chips no deben tapar el botón de centrar ni la columna derecha.

Explícame cómo quedó el flujo de datos: de _lugares y _categoria hasta lo que se dibuja.

4. **Prompt 4**

Reglas fijas: MVVM + Repository; el ViewModel no recibe Context ni usa android.util.Log; los Composables NO calculan distancias ni deciden qué lugar está dentro del radio. El mapa es osmdroid, no Google Maps (no existe Circle: usa Polygon). Sin dependencias nuevas, íconos básicos o Canvas. Entrégame los archivos COMPLETOS con imports y una lista de qué cambió y por qué. No pongas claves de API.

TAREA: selector de radio de 100, 200 o 500 m, un círculo en el mapa y marcar en la lista los lugares dentro del radio.

ViewModel:
- _radio = MutableStateFlow<Int?>(null) (en metros; null = sin radio) con versión pública de solo lectura.
- fun elegirRadio(metros: Int?): si se elige el mismo radio que ya estaba, se quita (null).
- val idsEnRadio: StateFlow<Set<Int>> = combine(lugaresVisibles, _radio) { ... } con los ids de los lugares cuyo metros no es nulo y es <= radio; vacío si no hay radio. Usa la distancia que ya tiene LugarConDistancia; NO la recalcules con Location.

View:
- Selector en el panel inferior, encima de la lista: SingleChoiceSegmentedButtonRow de Material 3 (con @OptIn(ExperimentalMaterial3Api::class) si hace falta) con tres opciones "100 m", "200 m", "500 m", altura 36.dp. Opción seleccionada con fondo TurquesaClaro y texto VerdeOscuro. Al tocar la opción ya seleccionada, se quita el radio. Para hacer sitio, baja la altura máxima de la lista a 160.dp.
- ListaLugares recibe idsEnRadio: Set<Int>. Los lugares dentro del radio muestran, debajo de la categoría, una etiqueta pequeña "En radio" (Surface en forma de píldora, fondo TurquesaClaro, texto VerdeOscuro de 10.sp negrita).
- MapaUbicacion recibe radio: Int? y dibuja el círculo SOLO si hay radio y el estado es Lista: un Polygon de osmdroid con Polygon.pointsAsCircle(GeoPoint(latitud, longitud), radio.toDouble()), relleno Turquesa con alfa ~40/255 y borde Turquesa de 4f de grosor. Agrégalo ANTES que los marcadores para que quede debajo. Como el bloque update limpia los overlays en cada actualización, asegúrate de que el círculo no se duplique ni quede de un radio anterior.

Explícame el flujo de datos del radio de punta a punta.

5. **Prompt 5**

Reglas fijas: MVVM + Repository con interfaces. El ViewModel NO recibe Context, NO usa android.util.Log y NO usa FusedLocationProviderClient: este vive solo en UbicacionRepositoryGoogle. Los Composables no tocan el Repository ni calculan distancias. El mapa es osmdroid. Sin dependencias nuevas, íconos básicos o Canvas. Entrégame los archivos COMPLETOS con imports y una lista de qué cambió y por qué. No pongas claves de API.

TAREA: seguimiento de ubicación en vivo con un botón Iniciar/Detener.

Repository:
- En la interface UbicacionRepository agrega fun ubicacionEnVivo(): Flow<Posicion>.
- En UbicacionRepositoryGoogle impleméntala con callbackFlow: LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L) con setMinUpdateIntervalMillis(1000L); un LocationCallback cuyo onLocationResult haga trySend(Posicion(lat, lon)) con la última ubicación; requestLocationUpdates(request, callback, Looper.getMainLooper()); y awaitClose { cliente.removeLocationUpdates(callback) }. Marca con @SuppressLint("MissingPermission") y, si se lanza SecurityException, cierra el flow con esa excepción (close(e)).

ViewModel:
- _siguiendo = MutableStateFlow(false) con versión pública de solo lectura, y un private var jobSeguimiento: Job?.
- fun iniciarSeguimiento(): si no hay Play Services, publica el mismo error que buscar(); cancela cualquier job anterior; lanza en viewModelScope un collect de ubicacionEnVivo() con .catch { _estado.value = errorDe(it); _siguiendo.value = false }; cada posición hace _estado.value = UbicacionUi.Lista(it) y actualizarDistancias(it). Pon _siguiendo en true.
- fun detenerSeguimiento(): cancela el job y pone _siguiendo en false.
- fun alternarSeguimiento(): si está siguiendo, detiene; si no, inicia.
- buscar() debe detener primero el seguimiento si estaba activo, para que no choquen.

View:
- Botón de seguimiento en la columna derecha del mapa, encima del botón de centrar, con 12.dp de separación: Surface circular de 48.dp con sombra de 6.dp. Detenido: fondo VerdeOscuro e Icons.Default.PlayArrow blanco. Siguiendo: fondo Naranja y un cuadrado blanco de "detener" dibujado con Canvas. contentDescription "Iniciar seguimiento" o "Detener seguimiento".
- Mientras sigue, una etiqueta en píldora "Siguiendo en vivo" (fondo Naranja, texto blanco de 12.sp) centrada arriba del mapa, debajo de los chips.
- Permiso: reutiliza el flujo de permiso que ya existe. Como el launcher actual solo llama a buscar(), agrega una variable rememberSaveable que recuerde si lo pendiente al conceder el permiso es buscar o iniciar seguimiento. Si no hay permiso, se pide; si lo niega, se llama a permisoDenegado().
- Batería: detén el seguimiento cuando la app deja de estar visible. En PantallaMapa observa el ciclo de vida con DisposableEffect y un LifecycleEventObserver: en ON_STOP llama a vm.detenerSeguimiento(). El ViewModel no sabe nada del ciclo de vida.

MapaUbicacion (ajustes obligatorios, porque el seguimiento actualiza la posición cada ~2 s):
- La cámara NO debe moverse sola en cada actualización. Confirma que el LaunchedEffect de cámara solo se dispara por centrarToken y por la transición a Lista, como se definió en el rediseño.
- El bloque update de AndroidView hoy limpia y recrea todos los overlays y llama a showInfoWindow() en cada recomposición. Con actualizaciones cada 2 s eso cerraría y reabriría la ventana de información todo el tiempo. Cámbialo para que la ventana de información solo se abra cuando cambia el lugar seleccionado o cuando se llega a Lista por primera vez; no en cada nueva posición. Puedes conservar las instancias de los marcadores con remember y actualizar solo su posición e ícono. Verifica que el círculo del radio y los marcadores no se dupliquen.

Explícame el flujo de datos del seguimiento (Repository -> ViewModel -> View) y qué pasa al rotar la pantalla y al ir a segundo plano.

6. **Prompt 6 - fix**

Reglas fijas: MVVM + Repository; el ViewModel no recibe Context ni usa android.util.Log; los Composables no calculan datos de negocio; solo Compose Material 3, sin dependencias nuevas; el mapa es osmdroid; sin claves de API. Entrégame los archivos COMPLETOS con imports y una lista de qué cambió, y dime cuál era la causa de cada problema. No cambies ViewModel ni Repository.

Tengo 3 problemas visibles en la pantalla del mapa:

PROBLEMA 1: la barra superior (TopAppBar + OndaSuperior) se tapa, desaparece o se mueve cuando arrastro o hago zoom en el mapa. Revisa en este orden y corrige lo que encuentres:
a) El MapView de osmdroid dibuja fuera de sus límites: el Box que contiene MapaUbicacion debe tener Modifier.clipToBounds(), y el AndroidView debe ocupar solo ese Box (fillMaxSize).
b) La barra y la onda deben ser hijos directos del Column raíz y estar ANTES del Box del mapa. Nunca dentro de un Box superpuesto al mapa, ni con offset negativo ni con zIndex menor que el mapa. La onda debe ocupar su propio alto dentro del Column (sin superponerse con el mapa).
c) La barra y la onda deben tener fondo sólido VerdeOscuro y Modifier.zIndex(1f) para quedar siempre por encima.
d) Ningún contenedor de la pantalla del mapa debe tener verticalScroll ni scroll que se mueva con los gestos del mapa.

PROBLEMA 2: el botón "Ir al más cercano" aparece cortado como "Ir al más", y "Obtener mi ubicación" como "Obtener mi". Corrige los dos botones de la fila del panel inferior:
- Quita cualquier altura fija; usa heightIn(min = 44.dp).
- Ambos con weight(1f) para que midan lo mismo.
- contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp).
- Texto de 12.sp, lineHeight 14.sp, textAlign Center, maxLines = 2 y SIN recortar: si no cabe en una línea, pasa a dos. Los textos deben ser exactamente "Obtener mi ubicación" y "Ir al más cercano".
- Ícono de 16.dp con 6.dp de espacio. Mantén el indicador de progreso mientras busca.
- Verifica que se lean completos en una pantalla de 320.dp de ancho.

PROBLEMA 3: los marcadores de los lugares se ven ROJOS y deberían ser azules (#1E88E5), con el más cercano en verde (#34A853) y solo "Estás aquí" en rojo. Probable causa: todos los marcadores comparten el mismo Drawable del ícono por defecto y el tinte de uno afecta a los demás. Soluciona dándole a cada Marker su propia copia del ícono antes de teñirlo (por ejemplo icon = iconoBase.constantState?.newDrawable()?.mutate() y luego setTint). Hazlo igual para el marcador propio.

Al final dime qué verificar en el emulador para confirmar que los 3 problemas quedaron resueltos.

7. **Prompt 7 - fix**

Reglas fijas: MVVM + Repository; el ViewModel no recibe Context ni usa android.util.Log; los Composables no calculan datos de negocio; solo Compose Material 3, sin dependencias nuevas; el mapa es osmdroid; sin claves de API. Entrégame los archivos COMPLETOS con imports y una lista de qué cambió. No cambies ViewModel ni Repository.

PROBLEMA: en la pantalla del mapa, la etiqueta "Siguiendo en vivo" se dibuja encima de la fila de chips de categorías y tapa los chips "Estudio" y "Comida".

CAUSA PROBABLE: los chips y la etiqueta son dos hijos del Box del mapa, ambos alineados arriba, y se superponen.

SOLUCIÓN: dentro del Box del mapa, agrupa los controles superiores en UN solo Column alineado arriba a la izquierda (Alignment.TopStart), con padding de 12.dp y Arrangement.spacedBy(8.dp):
1. La fila de chips (LazyRow) a ancho completo del Column.
2. Debajo, SOLO si se está siguiendo, la etiqueta "Siguiendo en vivo" (píldora Naranja con texto blanco de 12.sp), alineada al inicio.
   Así la etiqueta nunca puede tapar los chips, aparezca o no.

Además comprueba que los botones circulares de la derecha (seguimiento y centrar) no queden debajo de este Column en pantallas pequeñas: el Column de arriba debe ocupar el ancho menos 72.dp a la derecha (Modifier.padding(end = 60.dp) o equivalente) para no pisarse con ellos.

Dime qué verificar en pantalla.

8. **Prompt 8 - fix**

Reglas fijas: MVVM + Repository; el ViewModel no recibe Context, no usa android.util.Log y no usa FusedLocationProviderClient (este vive solo en UbicacionRepositoryGoogle); los Composables no calculan datos de negocio. Sin dependencias nuevas, sin claves de API. Entrégame los archivos COMPLETOS con imports y una lista de qué cambió y por qué.

PROBLEMA: con el seguimiento en vivo, al caminar pocos pasos dentro de una casa la app marca que me moví varios metros y hasta otra calle. Es ruido del GPS: en interiores la precisión empeora (20 a 50 m) y llegan posiciones poco fiables.

SOLUCIÓN (solo en UbicacionRepositoryGoogle.ubicacionEnVivo(), dentro de la capa Repository):
1. En el LocationRequest agrega setMinUpdateDistanceMeters(5f) para que no emita actualizaciones por movimientos menores a 5 m.
2. En el LocationCallback, ignora las posiciones imprecisas: si location.hasAccuracy() y location.accuracy > 25f, NO hagas trySend (descártala). Si no tiene precisión (hasAccuracy() es false) también descártala.
3. Descarta además saltos imposibles: si la distancia entre la última posición aceptada y la nueva es mayor a lo que se podría recorrer a 10 m/s en el tiempo transcurrido entre ambas, ignora la nueva. Guarda la última posición aceptada y su tiempo en variables locales del callbackFlow (no en el ViewModel).
4. No cambies el contrato de la interface ni el modelo Posicion.

Explícame: (a) qué pasa si estoy en interiores y todas las posiciones son imprecisas (la app no debe fallar, solo no actualizar), (b) cómo probarlo y (c) qué sacrifico al filtrar (un poco de retraso al empezar).

## Reflexión

- **Algo que la IA hizo mal y corregí:** _un caso concreto, por ejemplo el de la fila 3 o la 8._
- **Qué aprendí:** _la IA propone, yo decido; si compila pero rompe MVVM, lo corrijo._