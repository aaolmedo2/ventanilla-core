# 📦 Flujo del Módulo de Cuadre de Caja con Integración CORE

Este flujo simula la jornada completa de un cajero en una ventanilla con **validación automática en el sistema CORE bancario**.

## 1. ☀️ Apertura del Turno (8:00 AM)

El cajero inicia sesión. Lo primero que hace es contar su **"fondo de caja"** (el dinero base que recibe para operar). El sistema debe registrar esta apertura.

Se realiza una petición (un `POST`) para crear un nuevo `turnos_cajero` y, al mismo tiempo, registrar sus `turno_denominaciones` de `tipo_conteo = 'INICIO'`.

**Petición:** `POST /api/turnos/abrir`

Este es el JSON que tu frontend debería enviar. Fíjate cómo el `monto_inicial` debe coincidir con la suma de las denominaciones.

```json
{
  "codigo_caja": "VENT-01",
  "codigo_cajero": "jperez",
  "codigo_turno": "VENT-01-jperez-20251116",
  "monto_inicial": 500.00,
  "denominaciones_iniciales": [
    { "billete": 20, "cantidad": 10 },
    { "billete": 10, "cantidad": 20 },
    { "billete": 5, "cantidad": 10 },
    { "billete": 1, "cantidad": 50 }
  ]
}
```

### Acciones en la Base de Datos:

1. `INSERT INTO turnos_cajero` (con `estado = 'ABIERTO'`, `monto_inicial = 500.00`, etc.).
2. Obtiene el `id` (SERIAL) del turno recién creado (ej: 123).
3. `INSERT INTO turno_denominaciones` (múltiples filas, todas con `turno_id = 123` y `tipo_conteo = 'INICIO'`).

---

## 2. 💸 Realizar un Depósito (Durante el día)

Llega un cliente y entrega dinero. El cajero lo recibe, lo cuenta e informa al sistema.

### 🔗 Integración CORE Automática
El sistema **automáticamente** realizará:

1. **Registro Local**: Guarda la transacción con estado `PENDIENTE`
2. **Validación CORE**: Envía automáticamente al `http://localhost:85/api/core/transacciones`
3. **Respuesta CORE**: Recibe confirmación (COMPLETADO/ERROR)
4. **Actualización**: Actualiza `estadoCore` y `mensajeCore` en la base de datos

> **Importante:** La transacción se registra **SIEMPRE** localmente, independiente del resultado del CORE. El estado CORE se almacena para auditoría y seguimiento.

Se realiza una petición para registrar una `transacciones_turno` de `tipo_transaccion = 'DEPOSITO'`.

**Petición:** `POST /api/transacciones`

El JSON debe incluir el `codigo_turno` (para que el backend sepa a qué turno abierto asignarlo) y el desglose del dinero recibido.

```json
{
  "codigo_turno_activo": "VENT-01-jperez-20251116",
  "tipo_transaccion": "DEPOSITO",
  "monto_total": 75.00,
  "denominaciones": [
    { "billete": 20, "cantidad": 3 },
    { "billete": 10, "cantidad": 1 },
    { "billete": 5, "cantidad": 1 }
  ]
}
```

### Acciones en la Base de Datos:

1. El backend busca el `id` de `turnos_cajero` donde `codigo_turno = 'VENT-01-jperez-20251116'` y `estado = 'ABIERTO'`. (Supongamos que es el `id = 123`).
2. `INSERT INTO transacciones_turno` (con `turno_id = 123`, `tipo_transaccion = 'DEPOSITO'`, `monto_total = 75.00`, **`estado_core = 'PENDIENTE'`**).
3. Obtiene el `id` (SERIAL) de la transacción recién creada (ej: 987).
4. **Envío automático al CORE**: El sistema envía la transacción al sistema bancario.
5. **Actualización CORE**: Se actualiza `estado_core = 'COMPLETADO'` y `mensaje_core = 'Transacción procesada exitosamente'` (o ERROR en caso de rechazo).
6. `INSERT INTO transaccion_denominaciones` (múltiples filas, todas con `transaccion_id = 987`).

---

## 3. 💵 Realizar un Retiro (Durante el día)

Llega un cliente a sacar dinero.

### 🔗 Integración CORE Automática (Retiros)
Para retiros, el sistema CORE validará:

1. **Saldo disponible**: Verificar que la cuenta tenga fondos suficientes
2. **Límites diarios**: Validar que no exceda límites de retiro
3. **Estado de cuenta**: Confirmar que la cuenta esté activa
4. **Debito automático**: El CORE debita el monto de la cuenta cliente

> **Flujo crítico**: Si el CORE rechaza el retiro (`estadoCore = 'ERROR'`), el cajero **NO debe entregar dinero** al cliente. La transacción queda registrada para auditoría pero sin efectivo entregado.

Se crea una `transacciones_turno` de `tipo_transaccion = 'RETIRO'`.

**Petición:** `POST /api/transacciones`

Este JSON es muy similar al depósito, pero el `tipo_transaccion` cambia. El desglose de denominaciones es opcional para los retiros, pero es una excelente práctica registrarlo para auditoría (saber qué billetes entregó el cajero).

```json
{
  "codigo_turno_activo": "VENT-01-jperez-20251116",
  "tipo_transaccion": "RETIRO",
  "monto_total": 40.00,
  "denominaciones": [
    { "billete": 20, "cantidad": 2 }
  ]
}
```

### Acciones en la Base de Datos:

1. El backend busca el `id` del turno abierto (`id = 123`).
2. `INSERT INTO transacciones_turno` (con `turno_id = 123`, `tipo_transaccion = 'RETIRO'`, `monto_total = 40.00`, **`estado_core = 'PENDIENTE'`**).
3. Obtiene el `id` de la transacción (ej: 988).
4. **Validación CORE**: Automáticamente verifica saldo y debita cuenta del cliente.
5. **Resultado CORE**: Actualiza `estado_core` y `mensaje_core` según respuesta del sistema bancario.
6. `INSERT INTO transaccion_denominaciones` (una fila, con `transaccion_id = 988`).

---

## 4. 🌙 Cierre del Turno (5:00 PM)

El cajero termina su jornada. Debe contar todo el dinero que tiene en su cajón. El sistema usará este conteo físico para **"cuadrar la caja"**.

Se realiza una petición para actualizar (`PUT` o `PATCH`) el turno y cerrarlo.

**Petición:** `PUT /api/turnos/cerrar`

El cajero envía su conteo físico total (`monto_final`) y el desglose de ese conteo (`denominaciones_finales`).

```json
{
  "codigo_turno_a_cerrar": "VENT-01-jperez-20251116",
  "monto_final": 535.00,
  "denominaciones_finales": [
    { "billete": 20, "cantidad": 11 },
    { "billete": 10, "cantidad": 21 },
    { "billete": 5, "cantidad": 9 },
    { "billete": 1, "cantidad": 60 }
  ]
}
```

### Acciones en la Base de Datos (Este es el paso más importante):

1. El backend busca el `id` del turno (`id = 123`).

2. **Cálculo del Sistema (Cuadre):**
   - Obtiene `monto_inicial` de `turnos_cajero` → **Monto: 500.00**
   - Suma todos los `monto_total` de `transacciones_turno` donde `tipo_transaccion = 'DEPOSITO'` y `turno_id = 123` → **Monto: 75.00**
   - Resta todos los `monto_total` de `transacciones_turno` donde `tipo_transaccion = 'RETIRO'` y `turno_id = 123` → **Monto: 40.00**
   - **`monto_calculado_sistema = 500.00 + 75.00 - 40.00 = 535.00`**

3. **Comparación:**
   - `monto_final` (del JSON): **535.00**
   - `monto_calculado_sistema`: **535.00**
   - `diferencia_cierre = 535.00 - 535.00 = 0.00`
   - `alerta_cierre = false` **¡Cuadre perfecto!**

4. **`UPDATE turnos_cajero`:**
   - `SET estado = 'CERRADO'`
   - `SET fin_turno = NOW()`
   - `SET monto_final = 535.00` (el del JSON)
   - `SET diferencia_cierre = 0.00`
   - `SET alerta_cierre = false`
   - `WHERE id = 123`

5. `INSERT INTO turno_denominaciones` (múltiples filas, todas con `turno_id = 123` y `tipo_conteo = 'FIN'`).

> *(Si el cajero hubiera contado 530.00, la `diferencia_cierre` sería -5.00 y `alerta_cierre` sería `true`)*

---

## 📡 Resumen de Integración CORE

### Estados de Transacción CORE
- **PENDIENTE**: Estado inicial antes del envío al CORE
- **COMPLETADO**: CORE procesó exitosamente la transacción
- **ERROR**: CORE rechazó o falló la comunicación

### Campos Agregados en Base de Datos
```sql
-- Agregados a la tabla transacciones_turno
estado_core VARCHAR(15) DEFAULT 'PENDIENTE' CHECK (estado_core IN ('PENDIENTE', 'COMPLETADO', 'ERROR')),
mensaje_core VARCHAR(500) -- Mensaje descriptivo del CORE
```

### Configuración Técnica
- **URL CORE**: `http://localhost:85/api/core/transacciones`
- **Timeout**: 10 segundos
- **Formato**: JSON con campos requeridos por CORE
- **Manejo de errores**: Transacción se registra independiente del resultado CORE

### Ventajas del Diseño
✅ **Auditoría completa**: Todas las transacciones se registran localmente  
✅ **Trazabilidad CORE**: Estado y mensaje del sistema bancario  
✅ **Operación continua**: No se bloquea si CORE está offline  
✅ **Recuperación**: Pueden reprocesarse transacciones pendientes