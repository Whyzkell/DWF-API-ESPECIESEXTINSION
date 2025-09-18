package sv.edu.udb.api_especieextionsion.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "especie",
        uniqueConstraints = @UniqueConstraint(name = "uk_especie_nombre_cientifico",
                columnNames = "nombre_cientifico"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder


public class Especie {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_cientifico", nullable = false, length = 120, unique = true)
    private String nombreCientifico;

    @Column(name = "nombre_comun", nullable = false, length = 120)
    private String nombreComun;

    @Column(name = "tipo", nullable = false, length = 10) // FLORA/FAUNA
    private String tipo;

    @Column(name = "estado_conservacion", nullable = false, length = 10) // EX, CR, EN, VU...
    private String estadoConservacion;

    @Column(name = "descripcion", length = 2000)
    private String descripcion;

    @Column(name = "es_endemica", nullable = false)
    private Boolean esEndemica;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;
}
