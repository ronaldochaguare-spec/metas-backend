package com.metas.service;

import com.metas.dto.DiaHistorial;
import com.metas.dto.GananciaRequest;
import com.metas.dto.ResumenDia;
import com.metas.dto.ResumenMes;
import com.metas.model.Ganancia;
import com.metas.repo.GananciaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GananciaService {

    public static final ZoneId LIMA = ZoneId.of("America/Lima");

    private final GananciaRepository repo;
    private final BigDecimal metaDiaria;

    public GananciaService(GananciaRepository repo, @Value("${app.meta-diaria}") BigDecimal metaDiaria) {
        this.repo = repo;
        this.metaDiaria = metaDiaria;
    }

    public ResumenDia registrar(GananciaRequest req) {
        Ganancia g = new Ganancia();
        g.setMonto(req.monto());
        g.setFuente(req.fuente());
        g.setNota(req.nota());
        g.setFecha(LocalDate.now(LIMA));
        g.setCreadoEn(LocalDateTime.now(LIMA));
        repo.save(g);
        return resumenHoy();
    }

    public ResumenDia eliminarUltima() {
        LocalDate hoy = LocalDate.now(LIMA);
        repo.findFirstByFechaOrderByCreadoEnDesc(hoy).ifPresent(repo::delete);
        return resumenHoy();
    }

    public ResumenDia resumenHoy() {
        LocalDate hoy = LocalDate.now(LIMA);
        BigDecimal ganadoHoy = repo.totalPorFecha(hoy);
        BigDecimal arrastre = excedenteAcumulado(hoy);
        BigDecimal total = arrastre.add(ganadoHoy);
        BigDecimal faltante = metaDiaria.subtract(total).max(BigDecimal.ZERO);
        return new ResumenDia(hoy, total, arrastre, ganadoHoy, metaDiaria, faltante,
                total.compareTo(metaDiaria) >= 0, repo.findByFechaOrderByCreadoEnDesc(hoy));
    }

    public ResumenMes resumenMes(YearMonth ym) {
        LocalDate hoy = LocalDate.now(LIMA);
        List<DiaHistorial> todos = diasHasta(hoy);
        LocalDate primero = todos.isEmpty() ? hoy : todos.get(0).fecha();

        // La meta del mes cuenta desde el primer registro (si el mes es el primero de uso).
        LocalDate inicioMes = primero.isAfter(ym.atDay(1)) ? primero : ym.atDay(1);
        LocalDate finMes = ym.atEndOfMonth();
        int diasMeta = inicioMes.isAfter(finMes) ? 0 : (int) ChronoUnit.DAYS.between(inicioMes, finMes) + 1;

        List<DiaHistorial> delMes = todos.stream()
                .filter(d -> YearMonth.from(d.fecha()).equals(ym))
                .toList();

        BigDecimal metaMes = metaDiaria.multiply(BigDecimal.valueOf(diasMeta));
        BigDecimal ganadoMes = delMes.stream().map(DiaHistorial::ganado).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal faltante = metaMes.subtract(ganadoMes).max(BigDecimal.ZERO);
        int diasCumplidos = (int) delMes.stream().filter(DiaHistorial::cumplida).count();

        // Racha: días seguidos cumplidos hasta hoy. Si hoy aún no cumple, cuenta desde ayer.
        int racha = 0;
        int i = todos.size() - 1;
        if (i >= 0 && !todos.get(i).cumplida()) i--;
        for (; i >= 0 && todos.get(i).cumplida(); i--) racha++;

        return new ResumenMes(ym.toString(), hoy, primero, ym.lengthOfMonth(), diasMeta, metaMes, ganadoMes,
                faltante, diasMeta > 0 && ganadoMes.compareTo(metaMes) >= 0, diasCumplidos, racha, delMes);
    }

    /** Excedente que llega a 'hoy' desde los días anteriores. */
    private BigDecimal excedenteAcumulado(LocalDate hoy) {
        List<DiaHistorial> dias = diasHasta(hoy.minusDays(1));
        if (dias.isEmpty()) return BigDecimal.ZERO;
        return dias.get(dias.size() - 1).total().subtract(metaDiaria).max(BigDecimal.ZERO);
    }

    /**
     * Recorre día por día desde el primer registro hasta 'hasta' (inclusive).
     * Cada día: total = arrastre + ganado; el arrastre del siguiente = max(0, total - meta).
     * Lo que falta para la meta NO se arrastra. Los días sin registros cuentan como 0 ganado.
     */
    private List<DiaHistorial> diasHasta(LocalDate hasta) {
        Map<LocalDate, BigDecimal> porDia = new HashMap<>();
        for (Object[] fila : repo.totalesPorDiaAntes(hasta.plusDays(1))) {
            porDia.put((LocalDate) fila[0], (BigDecimal) fila[1]);
        }
        List<DiaHistorial> dias = new ArrayList<>();
        if (porDia.isEmpty()) return dias;

        BigDecimal arrastre = BigDecimal.ZERO;
        for (LocalDate d = Collections.min(porDia.keySet()); !d.isAfter(hasta); d = d.plusDays(1)) {
            BigDecimal ganado = porDia.getOrDefault(d, BigDecimal.ZERO);
            BigDecimal total = arrastre.add(ganado);
            dias.add(new DiaHistorial(d, ganado, arrastre, total, total.compareTo(metaDiaria) >= 0));
            arrastre = total.subtract(metaDiaria).max(BigDecimal.ZERO);
        }
        return dias;
    }
}
