package com.metas.scheduler;

import com.metas.dto.ResumenDia;
import com.metas.dto.ResumenMes;
import com.metas.service.EmailService;
import com.metas.service.GananciaService;
import java.time.YearMonth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MetaScheduler {

    private static final Logger log = LoggerFactory.getLogger(MetaScheduler.class);

    private final GananciaService service;
    private final EmailService email;

    public MetaScheduler(GananciaService service, EmailService email) {
        this.service = service;
        this.email = email;
    }

    /** Aviso a media tarde: todavía puedes hacer algo. */
    @Scheduled(cron = "${app.cron.recordatorio}", zone = "America/Lima")
    public void recordatorio() {
        ResumenDia r = service.resumenHoy();
        if (r.cumplida()) return;
        enviarSeguro("Te faltan S/ " + r.faltante() + " para tu meta de hoy",
                "Llevas S/ " + r.total() + " de S/ " + r.meta() + ".\n"
                        + "Aún estás a tiempo de cumplir la meta de hoy.");
    }

    /** Cierre del día: alerta si no se cumplió, felicitación si sí. */
    @Scheduled(cron = "${app.cron.cierre}", zone = "America/Lima")
    public void cierreDelDia() {
        ResumenDia r = service.resumenHoy();
        if (r.cumplida()) {
            enviarSeguro("Meta cumplida hoy: S/ " + r.total(),
                    "Lo lograste. Llevas S/ " + r.total() + " y la meta era S/ " + r.meta() + ".");
        } else {
            enviarSeguro("Meta NO cumplida hoy",
                    "Hoy llevas S/ " + r.total() + " de S/ " + r.meta() + " (faltaron S/ " + r.faltante() + ").\n"
                            + "Mañana empieza de nuevo.");
        }
    }

    /** Último día del mes, 5 minutos después del cierre diario. */
    @Scheduled(cron = "${app.cron.cierre-mes}", zone = "America/Lima")
    public void cierreDelMes() {
        ResumenMes m = service.resumenMes(YearMonth.now(GananciaService.LIMA));
        String detalle = "Ganaste S/ " + m.ganadoMes() + " de S/ " + m.metaMes() + " este mes.\n"
                + "Días cumplidos: " + m.diasCumplidos() + " de " + m.diasMeta() + ".";
        if (m.cumplida()) {
            enviarSeguro("Meta del mes cumplida", detalle);
        } else {
            enviarSeguro("Meta del mes NO cumplida", detalle + "\nFaltaron S/ " + m.faltante() + ".");
        }
    }

    private void enviarSeguro(String asunto, String cuerpo) {
        try {
            email.enviar(asunto, cuerpo);
        } catch (Exception e) {
            log.error("No se pudo enviar el correo: {}", e.getMessage());
        }
    }
}
