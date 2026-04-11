# TODO — Currency Converter (DPS TP1)

Referencia: [docs/Trabajo Práctico #1.pdf](docs/Trabajo%20Práctico%20%231.pdf).  
Prioridad: **código limpio** y separación negocio / infraestructura, como pide la consigna.

---

## Estado frente a las historias de usuario

| #   | Historia                                        | Estado                                                                       | Acción si falta algo                                        |
| --- | ----------------------------------------------- | ---------------------------------------------------------------------------- | ----------------------------------------------------------- |
| 1   | Listar monedas soportadas                       | Hecho                                                                        | —                                                           |
| 2   | Timestamp en la respuesta de conversión         | Hecho (`Instant.now(clock)` en cotizaciones latest; fecha+hora en histórico) | —                                                           |
| 3   | Solo cotización entre dos monedas (sin monto)   | Hecho (`getCurrencyRate` → `CurrencyRateQuote`)                              | —                                                           |
| 4   | Errores de conexión/API claros (404, 500, etc.) | Hecho (`CurrencyRateRemoteException`, `CurrencyRateTransportException`, tests) | —                                                           |
| 5   | Un monto a varias monedas                       | Hecho                                                                        | —                                                           |
| 6   | Histórico por fecha y varias monedas            | Hecho                                                                        | —                                                           |
| 7   | Cotización usada por moneda en la respuesta     | Hecho (`rate` en cada ítem)                                                  | —                                                           |

---

## HU 4 — Errores (conexión y API)

**Problema actual:** `FreeCurrencyApiProvider` lanza `CurrencyRateNotAvailable` sin código HTTP ni mensaje útil; `UnirestHttpClient` atrapa excepciones y devuelve un cuerpo JSON falso con status 500, ocultando fallos de red.

- [x] Propagar fallos de red / timeout de forma explícita (no simular 200/JSON con error genérico).
- [x] Para respuestas HTTP no exitosas, exponer **código de estado** (p. ej. 404, 500) y, si aplica, mensaje o cuerpo seguro de la API.
- [x] Definir excepciones o tipos de error de **dominio** que el usuario de la biblioteca pueda interpretar, sin acoplar nombres HTTP al negocio si se prefiere una capa intermedia.
- [x] Añadir tests unitarios (y/o integración) que cubran 404, 500 y fallo de conexión simulado.

---

## Cobertura de tests unitarios al 100%

**Problema actual:** no hay JaCoCo (ni umbral) en el build Maven; la cobertura unitaria no está verificada de forma automática.

- [ ] Configurar **JaCoCo** en el `pom` (padre o módulo `CurrencyConverter`) y generar reporte (`mvn verify` o goal dedicado).
- [ ] Fijar umbral **100%** (líneas/branches según lo que exija la cátedra) o documentar exclusiones acordadas (p. ej. `Main`, records puros sin lógica — solo si está permitido).
- [ ] Completar tests unitarios faltantes: infraestructura (`UnirestHttpClient`, ramas de `FreeCurrencyApiProvider`), DTOs con lógica (`HistoricalExchangeRateResponse`, `AvailableCurrenciesResponse`), constructores y métodos no cubiertos.

**Ya cumplido:** al menos un test de integración — `FreeCurrencyApiProviderIT` (WireMock).

---

## Código limpio y seguridad

- [ ] Eliminar o resolver comentarios `TODO` / `FIX` en `FreeCurrencyApiProvider` y equivalentes.
- [ ] Sustituir `System.err` en el cliente HTTP por **logging** (el proyecto ya declara Log4j).
- [ ] Mensajes de error y logs: informativos para el usuario/desarrollador sin filtrar secretos.

---

## Opcional / refinamiento

- [x] Timestamp en cotización latest desde reloj inyectado (`CurrencyConverter` + `Clock`); la API no expone hora de cotización en la respuesta típica de `latest`.
- [x] Ampliar `Main` para demostrar todas las historias (listado, multi-moneda, histórico, solo tasa, etc.).

---

## Entrega y presentación (consigna)

- [ ] Generar **ZIP** del proyecto antes de la clase.
- [ ] Ensayar presentación (**8 min** + **5 min** Q&A).

---

## Checklist rápido antes de entregar

- [ ] `mvn test` (y verificación de cobertura si JaCoCo ya está configurado).
- [ ] Sin API keys en el repositorio.
- [x] HU 4 cubierta en comportamiento y tests.
