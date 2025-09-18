package sv.edu.udb.api_especieextionsion.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "especie_amenaza",
        uniqueConstraints = @UniqueConstraint(name = "uk_especie_amenaza", columnNames = {"especie_id","amenaza_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EspecieAmenaza {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "especie_id", nullable = false)
    private Especie especie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "amenaza_id", nullable = false)
    private Amenaza amenaza;

    @Column(nullable = false, length = 10)
    private String severidad; // BAJA | MEDIA | ALTA
}
