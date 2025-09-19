package sv.edu.udb.api_especieextionsion.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "fuente",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_fuente_nombre", columnNames = "nombre")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Fuente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String nombre;           // Título o referencia corta

    @Column(length = 500)
    private String descripcion;

    @Column(length = 30)
    private String tipo;             // ARTICULO | WEB | LIBRO | REPORTE, etc.

    @Column(length = 500)
    private String enlace;           // URL

    private LocalDate fechaPublicacion;
}
