drop table TransaccionDenominaciones;

drop table TransaccionesTurno;

drop table TurnoDenominaciones;

drop table TurnosCajero;

/*==============================================================*/
/* User: PUBLIC                                                 */
/*==============================================================*/
/*==============================================================*/
/* Table: TransaccionDenominaciones                             */
/*==============================================================*/
create table TransaccionDenominaciones (
   id                   SERIAL not null,
   transaccionId        INTEGER              not null,
   billete              INTEGER              not null
      constraint CKC_BILLETE_TRANSACC check (billete > '0'),
   cantidad             INTEGER              not null
      constraint CKC_CANTIDAD_TRANSACC check (cantidad >= '0'),
   monto                NUMERIC(12, 2)       not null,
   estado               VARCHAR(15)          not null default 'ACTIVO'
      constraint CKC_ESTADO_TRANSACC check (estado in ('ACTIVO','INACTIVO')),
   constraint PK_TRANSACCIONDENOMINACIONES primary key (id)
);

comment on table TransaccionDenominaciones is
'Desglose de billetes para cada transacciÃ³n individual.';

comment on column TransaccionDenominaciones.transaccionId is
'Referencia a la llave primaria de la tabla TransaccionesTurno.';

comment on column TransaccionDenominaciones.billete is
'Valor del billete (ej: 20 para $20).';

comment on column TransaccionDenominaciones.cantidad is
'Cantidad de esos billetes';

comment on column TransaccionDenominaciones.monto is
'Columna generada automÃ¡ticamente (billete * cantidad).';

comment on column TransaccionDenominaciones.estado is
'Estado del registro (ACTIVO, INACTIVO) (Req. 3).';

/*==============================================================*/
/* Table: TransaccionesTurno                                    */
/*==============================================================*/
create table TransaccionesTurno (
   id                   SERIAL not null,
   turnoId              INTEGER              not null,
   tipoTransaccion      VARCHAR(10)          not null
      constraint CKC_TIPOTRANSACCION_TRANSACC check (tipoTransaccion in ('DEPOSITO','RETIRO')),
   montoTotal           NUMERIC(12, 2)       not null,
   fechaTransaccion     TIMESTAMPTZ          not null default NOW(),
   cuentaReferencia     VARCHAR(12)          not null,
   estado               VARCHAR(10)          not null
      constraint CKC_ESTADO_TRANSACC check (estado in ('ACTIVO','INACTIVO')),
   estadoCore           VARCHAR(15)          null
      constraint CKC_ESTADOCORE_TRANSACC check (estadoCore in ('COMPLETADO','PENDIENTE','ERROR')),
   mensajeCore          VARCHAR(500)         null,
   constraint PK_TRANSACCIONESTURNO primary key (id)
);

comment on table TransaccionesTurno is
'Log de cada movimiento (depÃ³sito/retiro) asociado a un turno.';

comment on column TransaccionesTurno.turnoId is
'Referencia a la llave primaria de la tabla TurnosCajero.';

comment on column TransaccionesTurno.tipoTransaccion is
'Tipo de movimiento: DEPOSITO, RETIRO, etc.';

comment on column TransaccionesTurno.montoTotal is
'Monto total de esta transacciÃ³n especÃ­fica';

comment on column TransaccionesTurno.fechaTransaccion is
'Fecha y hora exactas en que se registrÃ³ la transacciÃ³n';

comment on column TransaccionesTurno.cuentaReferencia is
'Cuenta del cliente (CORE) asociada a la transacciÃ³n (Req. 4).';

comment on column TransaccionesTurno.estadoCore is
'Estado de la transacción en el sistema CORE (COMPLETADO, PENDIENTE, ERROR).';

comment on column TransaccionesTurno.mensajeCore is
'Mensaje de respuesta del sistema CORE para la transacción.';

/*==============================================================*/
/* Table: TurnoDenominaciones                                   */
/*==============================================================*/
create table TurnoDenominaciones (
   id                   SERIAL not null,
   turnoId              INTEGER              not null,
   tipoConteo           VARCHAR(10)          not null
      constraint CKC_TIPOCONTEO_TURNODEN check (tipoConteo in ('INICIO','FIN')),
   billete              INTEGER              not null
      constraint CKC_BILLETE_TURNODEN check (billete > '0'),
   cantidad             INTEGER              not null
      constraint CKC_CANTIDAD_TURNODEN check (cantidad >= '0'),
   monto                NUMERIC(12, 2)       not null,
   estado               VARCHAR(15)          not null default 'ACTIVO'
      constraint CKC_ESTADO_TURNODEN check (estado in ('ACTIVO','INACTIVO')),
   constraint PK_TURNODENOMINACIONES primary key (id)
);

comment on table TurnoDenominaciones is
'Desglose de billetes (conteo) al INICIO y FIN de cada turno.';

comment on column TurnoDenominaciones.turnoId is
'Referencia a la llave primaria de la tabla TurnosCajero.';

comment on column TurnoDenominaciones.tipoConteo is
'Indica si es el desglose de INICIO o de FIN.';

comment on column TurnoDenominaciones.billete is
'Valor del billete (ej: 20 para $20).';

comment on column TurnoDenominaciones.cantidad is
'Cantidad de esos billetes';

comment on column TurnoDenominaciones.monto is
'Columna generada automÃ¡ticamente (billete * cantidad).';

comment on column TurnoDenominaciones.estado is
'Estado del registro (ACTIVO, INACTIVO) (Req. 3).';

/*==============================================================*/
/* Table: TurnosCajero                                          */
/*==============================================================*/
create table TurnosCajero (
   id                   SERIAL not null,
   codigoCaja           VARCHAR(50)          not null,
   codigoCajero         VARCHAR(50)          not null,
   contrasenia          VARCHAR(100)         not null,
   codigoTurno          VARCHAR(100)         not null,
   inicioTurno          TIMESTAMPTZ          not null default NOW(),
   montoInicial         NUMERIC(12, 2)       not null,
   finTurno             TIMESTAMPTZ          null,
   montoFinal           NUMERIC(12, 2)       null,
   estado               VARCHAR(15)          not null default 'ABIERTO'
      constraint CKC_ESTADO_TURNOSCA check (estado in ('ACTIVO','INACTIVO')),
   constraint PK_TURNOSCAJERO primary key (id)
);

comment on table TurnosCajero is
'Almacena la sesiÃ³n de trabajo (turno) de un cajero en una caja especÃ­fica.';

comment on column TurnosCajero.id is
'Llave primaria (SERIAL autoincremental)';

comment on column TurnosCajero.codigoCaja is
'CÃ³digo de la caja fÃ­sica (ej: "Ventanilla-03")';

comment on column TurnosCajero.codigoCajero is
'CÃ³digo del usuario cajero (ej: "jPerez")';

comment on column TurnosCajero.codigoTurno is
'Llave de negocio legible para humanos (no Ãºnica).';

comment on column TurnosCajero.inicioTurno is
'Fecha y hora exactas de inicio del turno';

comment on column TurnosCajero.montoInicial is
'Monto total de apertura de caja (fondo).';

comment on column TurnosCajero.finTurno is
'Fecha y hora exactas de finalizaciÃ³n del turno';

comment on column TurnosCajero.montoFinal is
'Monto total de cierre de caja (conteo fÃ­sico).';

comment on column TurnosCajero.estado is
'Estado actual del turno (ABIERTO, CERRADO)';

alter table TransaccionDenominaciones
   add constraint FK_DENOMINA_TRANSAC foreign key (transaccionId)
      references TransaccionesTurno (id)
      on delete restrict on update restrict;

alter table TransaccionesTurno
   add constraint FK_TRANSAC_TURNO foreign key (turnoId)
      references TurnosCajero (id)
      on delete restrict on update restrict;

alter table TurnoDenominaciones
   add constraint FK_DENOMINA_TURNO foreign key (turnoId)
      references TurnosCajero (id)
      on delete restrict on update restrict;
