# 🔄 ACTUALIZACIÓN: Formato CORE Correcto

## ✅ **CAMBIOS REALIZADOS**

### 📝 **Nuevos Campos CORE**
He actualizado la integración CORE para usar el formato correcto que espera el sistema:

```json
{
  "cuentaOrigenId": 1,      // ← Solo para RETIROS
  "cuentaDestinoId": 2,     // ← Solo para DEPOSITOS  
  "monto": 100.00,
  "canal": "VENT-01-jperez-20251116",      // ← codigoTurno
  "descripcion": "VENT-01-jperez-20251116", // ← codigoTurno
  "tipoTransaccion": "DEPOSITO"
}
```

---

## 🔧 **ARCHIVOS MODIFICADOS**

### 1. **TransaccionCoreDTO.java** ✅
```java
// ANTES (Campos incorrectos)
private BigDecimal montoTotal;
private String cuentaReferencia;
private OffsetDateTime fechaTransaccion;

// AHORA (Campos correctos)
private Long cuentaOrigenId;     // Solo RETIROS
private Long cuentaDestinoId;    // Solo DEPOSITOS
private BigDecimal monto;
private String canal;            // codigoTurno
private String descripcion;      // codigoTurno
private TipoTransaccion tipoTransaccion;
```

### 2. **CoreClient.java** ✅
```java
// ANTES
@Value("${core.transacciones.endpoint:/api/transacciones-core}")

// AHORA  
@Value("${core.transacciones.endpoint:/api/core/transacciones}")
```

### 3. **TransaccionService.java** ✅
```java
// LÓGICA NUEVA: Solo un campo según tipo de transacción
String numeroCuenta = transaccionDTO.getCuentaReferencia(); // VARCHAR(12)

if (tipoTransaccion == DEPOSITO) {
    transaccionCore.setCuentaDestinoId(numeroCuenta);  // Solo destino
    transaccionCore.setCuentaOrigenId(null);           // Origen null
} else if (tipoTransaccion == RETIRO) {
    transaccionCore.setCuentaOrigenId(numeroCuenta);   // Solo origen  
    transaccionCore.setCuentaDestinoId(null);          // Destino null
}
```

---

## 🎯 **LÓGICA DE ASIGNACIÓN**

### 💸 **Para DEPOSITOS**
```json
{
  "cuentaDestinoId": "123456789012",  // ← Número de cuenta VARCHAR(12) donde entra dinero
  "cuentaOrigenId": null,             // ← Explícitamente null
  "monto": 150.00,
  "canal": "VENT-01-jperez-20251116",
  "descripcion": "VENT-01-jperez-20251116", 
  "tipoTransaccion": "DEPOSITO"
}
```

### 💰 **Para RETIROS**
```json
{
  "cuentaOrigenId": "987654321098",   // ← Número de cuenta VARCHAR(12) de donde sale dinero
  "cuentaDestinoId": null,            // ← Explícitamente null
  "monto": 40.00,
  "canal": "VENT-01-jperez-20251116",
  "descripcion": "VENT-01-jperez-20251116",
  "tipoTransaccion": "RETIRO"
}
```

---

## 📡 **URL y ENDPOINT ACTUALIZADOS**

### **Configuración**
```properties
# application.properties
core.base.url=http://localhost:85
core.transacciones.endpoint=/api/core/transacciones
```

### **URL Completa**
```
POST http://localhost:85/api/core/transacciones
```

---

## 🔍 **EJEMPLO COMPLETO DE FLUJO**

### 1. **Request Frontend → Ventanilla**
```json
{
  "codigoTurnoActivo": "VENT-01-jperez-20251116",
  "tipoTransaccion": "DEPOSITO",
  "montoTotal": 150.00,
  "cuentaReferencia": "123456789012",
  "denominaciones": [...]
}
```

### 2. **Request Ventanilla → CORE** (Automático)
```json
{
  "cuentaDestinoId": "123456789012",  // VARCHAR(12) - Solo para depósitos
  "cuentaOrigenId": null,             // null para depósitos
  "monto": 150.00,
  "canal": "VENT-01-jperez-20251116",
  "descripcion": "VENT-01-jperez-20251116",
  "tipoTransaccion": "DEPOSITO"
}
```

### 3. **Response CORE → Ventanilla**
```json
{
  "exitoso": true,
  "mensaje": "Transacción procesada exitosamente",
  "codigoError": null,
  "transaccionId": "TXN-12345"
}
```

### 4. **Response Final Ventanilla → Frontend**
```json
{
  "id": 2,
  "tipoTransaccion": "DEPOSITO", 
  "montoTotal": 150.00,
  "fechaTransaccion": "2025-11-16T22:40:00-05:00",
  "cuentaReferencia": "123456789012",
  "estadoCore": "COMPLETADO",
  "mensajeCore": "Transacción procesada exitosamente"
}
```

---

## ⚠️ **ACLARACIONES IMPORTANTES**

### 1. **📝 Campos Mutuamente Excluyentes**
- **DEPOSITO**: Solo `cuentaDestinoId` tiene valor, `cuentaOrigenId` = null
- **RETIRO**: Solo `cuentaOrigenId` tiene valor, `cuentaDestinoId` = null

### 2. **🔤 Tipo de Dato de Cuentas**
- **NO es Long/Integer**: Los números de cuenta son `String` (VARCHAR(12))
- **Ejemplo**: `"123456789012"` no `123456789012`

### 3. **🎯 Lógica de Asignación**
```
DEPOSITO: dinero ENTRA a la cuenta → cuentaDestinoId
RETIRO:   dinero SALE de la cuenta → cuentaOrigenId
```

---

## ✅ **VERIFICACIONES**

✅ **Compilación exitosa** - Todas las clases compilan sin errores  
✅ **Campos correctos** - CORE recibe cuentaOrigenId/cuentaDestinoId según corresponde  
✅ **URL actualizada** - Endpoint corregido a `/api/core/transacciones`  
✅ **Lógica condicional** - Asigna cuenta origen/destino según tipo transacción  
✅ **Logs actualizados** - Mensajes de log reflejan nuevos campos  

---

**Estado**: 🎯 **LISTO PARA PRUEBAS** - Integración CORE actualizada con formato correcto.