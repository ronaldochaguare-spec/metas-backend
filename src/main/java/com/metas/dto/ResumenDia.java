package com.metas.dto;

import com.metas.model.Ganancia;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * total = arrastre + ganadoHoy (es lo que se compara contra la meta).
 * arrastre = excedente que viene de días anteriores.
 */
public record ResumenDia(
        LocalDate fecha,
        BigDecimal total,
        BigDecimal arrastre,
        BigDecimal ganadoHoy,
        BigDecimal meta,
        BigDecimal faltante,
        boolean cumplida,
        List<Ganancia> registros) {
}
