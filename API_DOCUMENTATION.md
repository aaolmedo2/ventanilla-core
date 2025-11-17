# 📦 API Ventanilla - Módulo de Cuadre de Caja

Este módulo implementa el flujo completo de cuadre de caja para ventanillas bancarias, siguiendo el patrón de apertura, transacciones y cierre de turno con **integración automática al sistema CORE**.

## 🔄 INTEGRACIÓN CON SISTEMA CORE

### 📋 Flujo Automático de Validación
Cada transacción se envía automáticamente al CORE para validación:

1. **Frontend → Ventanilla**: Envía transacción 
2. **Ventanilla → CORE**: Validación automática en `http://localhost:85/api/transacciones-core`
3. **CORE → Ventanilla**: Respuesta (COMPLETADO/ERROR)
4. **Ventanilla → Frontend**: Estado final con resultado del CORE

### 🎯 Estados de Transacción con CORE
- **PENDIENTE**: Estado inicial antes de enviar al CORE
- **COMPLETADO**: CORE aprobó y procesó la transacción
- **ERROR**: CORE rechazó o error de comunicación

## 🚀 Endpoints Implementados

### 📋 Flujo de Apertura de Turno

El cajero ingresa en el front-end:
- **codigoCaja**: Código de la ventanilla (ej: "VENT-01")
- **codigoCajero**: Código del usuario cajero (ej: "jperez") 
- **contrasenia**: Contraseña del cajero para autenticación
- **Conteo de billetes**: Denominaciones del fondo inicial

El sistema automáticamente genera el `codigoTurno` con el formato: `{codigoCaja}-{codigoCajero}-{yyyyMMdd}`

### 1. ☀️ Apertura de Turno
**POST** `/api/turnos/abrir`

Inicia un nuevo turno para un cajero y registra su fondo inicial.

**Request Body:**
```json
{
  "codigoCaja": "VENT-01",
  "codigoCajero": "jperez",
  "contrasenia": "password123",
  "montoInicial": 500.00,
  "denominacionesIniciales": [
    { "billete": 20, "cantidad": 10 },
    { "billete": 10, "cantidad": 20 },
    { "billete": 5, "cantidad": 10 },
    { "billete": 1, "cantidad": 50 }
  ]
}
```

> **Nota**: El `codigoTurno` se genera automáticamente con el formato: `{codigoCaja}-{codigoCajero}-{yyyyMMdd}`

**Response:** `201 Created`
```json
{
  "id": 123,
  "codigoCaja": "VENT-01",
  "codigoCajero": "jperez",
  "codigoTurno": "VENT-01-jperez-20251116",
  "inicioTurno": "2025-11-16T08:00:00-05:00",
  "montoInicial": 500.00,
  "estado": "ABIERTO"
}
```

### 2. 💸 Registrar Transacciones
**POST** `/api/transacciones`

Registra depósitos y retiros durante el turno.

**Request Body (Depósito):**
```json
{
  "codigoTurnoActivo": "VENT-01-jperez-20251116",
  "tipoTransaccion": "DEPOSITO",
  "montoTotal": 75.00,
  "cuentaReferencia": "123456789012",
  "denominaciones": [
    { "billete": 20, "cantidad": 3 },
    { "billete": 10, "cantidad": 1 },
    { "billete": 5, "cantidad": 1 }
  ]
}
```

**Request Body (Retiro):**
```json
{
  "codigoTurnoActivo": "VENT-01-jperez-20251116",
  "tipoTransaccion": "RETIRO",
  "montoTotal": 40.00,
  "cuentaReferencia": "987654321098",
  "denominaciones": [
    { "billete": 20, "cantidad": 2 }
  ]
}
```

**Response:** `201 Created`
```json
{
  "id": 987,
  "turnoId": 123,
  "tipoTransaccion": "DEPOSITO",
  "montoTotal": 75.00,
  "fechaTransaccion": "2025-11-16T10:30:00-05:00",
  "cuentaReferencia": "123456789012",
  "estado": "ACTIVO",
  "estadoCore": "COMPLETADO",
  "mensajeCore": "Transacción procesada exitosamente"
}
```

> **🔄 Integración CORE**: La transacción se envía automáticamente al CORE para validación. El `estadoCore` y `mensajeCore` reflejan la respuesta del sistema CORE.

### 3. 🌙 Cierre de Turno
**PUT** `/api/turnos/cerrar`

Cierra el turno y realiza el cuadre automático de caja.

**Request Body:**
```json
{
  "codigoTurnoACerrar": "VENT-01-jperez-20251116",
  "montoFinal": 535.00,
  "denominacionesFinales": [
    { "billete": 20, "cantidad": 11 },
    { "billete": 10, "cantidad": 21 },
    { "billete": 5, "cantidad": 9 },
    { "billete": 1, "cantidad": 60 }
  ]
}
```

**Response:** `200 OK`
```json
{
  "id": 123,
  "codigoCaja": "VENT-01",
  "codigoCajero": "jperez",
  "codigoTurno": "VENT-01-jperez-20251116",
  "inicioTurno": "2025-11-16T08:00:00-05:00",
  "finTurno": "2025-11-16T17:00:00-05:00",
  "montoInicial": 500.00,
  "montoFinal": 535.00,
  "estado": "CERRADO"
}
```

### 📡 Flujo de Integración con CORE

#### Proceso Automático de Validación
1. **Registro Local**: Transacción se guarda con estado `PENDIENTE`
2. **Envío al CORE**: Se envía automáticamente al sistema bancario
3. **Validación Externa**: CORE valida la transacción
4. **Actualización Estado**: Se actualiza a `COMPLETADO` o `ERROR`
5. **Respuesta**: Se incluye mensaje del CORE en la respuesta

#### Estados de Transacción CORE
- `PENDIENTE`: Esperando validación del CORE
- `COMPLETADO`: Validada exitosamente por CORE
- `ERROR`: Rechazada por CORE o error de comunicación

#### Configuración de Comunicación
- **URL Base**: http://localhost:85
- **Endpoint**: /api/core/transacciones
- **Timeout**: 10 segundos
- **Reintentos**: Sin reintentos automáticos
- **Formato**: JSON con validación completa

#### Ejemplo de Request al CORE
```json
{
  "tipoTransaccion": "DEPOSITO",
  "montoTotal": 75.00,
  "cuentaReferencia": "123456789012", 
  "canal": "VENT-01-jperez-20251116",
  "descripcion": "Depósito ventanilla",
  "fechaTransaccion": "2025-11-16T22:00:00-05:00"
}
```

#### Ejemplo de Response del CORE
```json
{
  "exitoso": true,
  "mensaje": "Transacción procesada exitosamente",
  "codigoError": null,
  "transaccionId": "TXN-12345"
}
```

#### Manejo de Errores CORE
- **Error de comunicación**: Timeout o CORE offline → `estadoCore: "ERROR"`
- **Error de negocio**: CORE rechaza transacción → `estadoCore: "ERROR"` con mensaje específico
- **Transacción exitosa**: CORE procesa → `estadoCore: "COMPLETADO"`

---

## 🎯 Características Implementadas

### ✅ Integración con CORE
- **Validación automática**: Cada transacción se valida con el sistema CORE
- **Estados de seguimiento**: PENDIENTE → COMPLETADO/ERROR
- **Manejo de errores**: Comunicación robusta con timeouts
- **Trazabilidad completa**: Mensaje de respuesta del CORE

### ✅ Validaciones de Negocio
- **Cuadre automático**: El sistema calcula automáticamente el monto esperado y lo compara con el conteo físico
- **Validación de denominaciones**: Verifica que el monto total coincida con la suma de las denominaciones
- **Control de turnos**: No permite múltiples turnos abiertos para el mismo cajero
- **Transacciones asociadas**: Solo permite transacciones en turnos abiertos

### ✅ Manejo de Errores
- **Excepciones personalizadas**: 4 tipos de excepciones específicas del dominio
- **Logs detallados**: Registro completo de operaciones y errores
- **Respuestas consistentes**: Handler global para respuestas de error estandarizadas

### ✅ Arquitectura
- **DTOs separados**: Objetos de transferencia específicos para cada endpoint
- **Servicios transaccionales**: Operaciones atómicas con rollback automático
- **Repositorios especializados**: Consultas optimizadas con Spring Data JPA
- **Enums tipados**: Estados y tipos de transacción controlados

## 📊 Cálculo del Cuadre de Caja

El sistema realiza automáticamente el cálculo del cuadre:

```
Monto Esperado = Monto Inicial + Total Depósitos - Total Retiros
Diferencia = Monto Final (físico) - Monto Esperado (sistema)

Si Diferencia = 0 → ✅ Cuadre Perfecto
Si Diferencia ≠ 0 → ⚠️ Diferencia en el cuadre
```

## 🔧 Estructura de Base de Datos

### Tablas Principales:
- `TurnosCajero`: Turnos de trabajo
- `TransaccionesTurno`: Log de operaciones
- `TurnoDenominaciones`: Conteo inicial/final
- `TransaccionDenominaciones`: Desglose por transacción

### Enums:
- `TipoTransaccion`: DEPOSITO, RETIRO
- `TipoConteo`: INICIO, FIN
- `EstadoGeneral`: ACTIVO, INACTIVO
- `EstadoTurno`: ABIERTO, CERRADO

## 🌟 Próximos Pasos Sugeridos

1. **Integración con CORE**: Conectar con el sistema bancario principal
2. **Auditoría avanzada**: Reportes de cuadre y diferencias
3. **Autenticación**: Sistema de login para cajeros
4. **Dashboard**: Interfaz web para operaciones
5. **Notificaciones**: Alertas para diferencias en cuadre

---

**Estado del Proyecto**: ✅ **LISTO PARA DESARROLLO** - La lógica de negocio está implementada y probada.

> **📝 Nota**: La documentación de integración CORE ha sido consolidada en este archivo. El archivo `INTEGRACION_CORE.md` contiene información complementaria pero puede considerarse como referencia histórica ya que toda la funcionalidad está documentada aquí.