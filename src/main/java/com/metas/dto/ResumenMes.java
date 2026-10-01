package com.metas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ResumenMes(
        String mes,               // "2026-09"
        LocalDate hoy,
        LocalDate primerRegistro,
        int diasDelMes,           // días reales del mes (para el calendario)
        int diasMeta,             // días que cuentan para la meta (desde el primer registro)
        BigDecimal metaMes,       // meta diaria x diasMeta
        BigDecimal ganadoMes,     // suma de lo realmente ganado en el mes
        BigDecimal faltante,
        boolean cumplida,
        int diasCumplidos,
        int racha,                // días seguidos cumpliendo hasta hoy (hoy cuenta si ya cumplió)
        List<DiaHistorial> dias) {
}
