package com.focus.solicitudes;

import com.focus.common.EstadoSolicitud;
import com.focus.common.TipoSolicitud;
import com.focus.common.UnidadProductiva;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    List<Solicitud> findByEstado(EstadoSolicitud estado);

    @Query("""
        SELECT s FROM Solicitud s
        WHERE (:desde IS NULL OR s.fechaCreacion >= :desde)
          AND (:hasta IS NULL OR s.fechaCreacion <= :hasta)
          AND (:unidad IS NULL OR s.unidad.nombre = :unidad)
          AND (:tipo   IS NULL OR s.tipo = :tipo)
        ORDER BY s.fechaCreacion DESC
        """)
    List<Solicitud> buscar(@Param("desde") Instant desde,
                           @Param("hasta") Instant hasta,
                           @Param("unidad") UnidadProductiva unidad,
                           @Param("tipo")   TipoSolicitud tipo);
}
