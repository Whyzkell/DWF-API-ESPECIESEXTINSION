package sv.edu.udb.api_especieextionsion.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "amenaza")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Amenaza {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String codigo;       // p.ej. "IUCN-1.1"

    @Column(nullable = false, length = 120)
    private String tipo;         // categoría / nombre corto

    @Column(length = 2000)
    private String descripcion;
}
