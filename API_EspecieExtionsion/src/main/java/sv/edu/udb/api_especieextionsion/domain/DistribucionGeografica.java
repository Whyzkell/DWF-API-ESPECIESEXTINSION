package sv.edu.udb.api_especieextionsion.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "distribucion_geografica")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DistribucionGeografica {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "especie_id", nullable = false)
    private Especie especie;

    @Column(nullable = false, length = 120)
    private String region;

    @Column(nullable = false, length = 120)
    private String ecosistema;

    @Column(nullable = false)
    private Double latitud;   // en dev usamos H2 con doubles

    @Column(nullable = false)
    private Double longitud;  // (más adelante puedes migrar a PostGIS)

    private Integer precisionMetros;

    private LocalDate fechaObservacion;
}
