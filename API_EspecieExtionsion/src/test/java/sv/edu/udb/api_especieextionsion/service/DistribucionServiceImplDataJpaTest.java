package sv.edu.udb.api_especieextionsion.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import sv.edu.udb.api_especieextionsion.controller.dto.DistribucionRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.DistribucionResponse;
import sv.edu.udb.api_especieextionsion.domain.DistribucionGeografica;
import sv.edu.udb.api_especieextionsion.domain.Especie;
import sv.edu.udb.api_especieextionsion.repository.DistribucionRepository;
import sv.edu.udb.api_especieextionsion.repository.EspecieRepository;
import sv.edu.udb.api_especieextionsion.service.impl.DistribucionServiceImpl;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(DistribucionServiceImpl.class) // inyecta el service real dentro del slice JPA
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // H2 embebida
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class DistribucionServiceImplDataJpaTest {

    @Autowired EspecieRepository especieRepo;
    @Autowired DistribucionRepository distRepo;
    @Autowired DistribucionService service;

    // ---------- helpers ----------
    private Especie especie(String nc, String nombreComun) {
        return Especie.builder()
                .nombreCientifico(nc)
                .nombreComun(nombreComun)
                .tipo("FAUNA")
                .estadoConservacion("VU")
                .descripcion("desc")
                .esEndemica(false)
                .fechaRegistro(LocalDate.now())
                .build();
    }

    private DistribucionRequest req(String region, String eco, double lat, double lng,
                                    Integer precision, LocalDate fecha) {
        var r = new DistribucionRequest();
        r.setRegion(region);
        r.setEcosistema(eco);
        r.setLatitud(lat);
        r.setLongitud(lng);
        r.setPrecisionMetros(precision);
        r.setFechaObservacion(fecha);
        return r;
    }

    // ---------- tests ----------

    @Test
    @DisplayName("crear: inserta distribución ligada a la especie y devuelve DTO")
    void crear_ok() {
        Especie sp = especieRepo.save(especie("NC-1", "Comun 1"));

        DistribucionResponse out = service.crear(
                sp.getId(),
                req("Centro", "Bosque", 13.70, -89.20, 50, LocalDate.of(2024, 5, 12))
        );

        assertThat(out.getId()).isNotNull();
        assertThat(out.getRegion()).isEqualTo("Centro");
        assertThat(out.getEcosistema()).isEqualTo("Bosque");
        assertThat(out.getLatitud()).isEqualTo(13.70);
        assertThat(out.getLongitud()).isEqualTo(-89.20);
        assertThat(out.getPrecisionMetros()).isEqualTo(50);
        assertThat(out.getFechaObservacion()).isEqualTo(LocalDate.of(2024, 5, 12));

        // verificación en BD
        var rows = distRepo.findByEspecieId(sp.getId());
        assertThat(rows).hasSize(1);
        DistribucionGeografica d = rows.get(0);
        assertThat(d.getRegion()).isEqualTo("Centro");
    }

    @Test
    @DisplayName("crear: 404 si la especie no existe")
    void crear_especieNoExiste() {
        assertThatThrownBy(() ->
                service.crear(999L, req("Occidente", "Matorral", 10.1, -88.2, 100, LocalDate.now()))
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Especie no encontrada");
    }

    @Test
    @DisplayName("listarPorEspecie: devuelve solo las distribuciones de esa especie")
    void listar_ok() {
        Especie s1 = especieRepo.save(especie("NC-2", "Comun 2"));
        Especie s2 = especieRepo.save(especie("NC-3", "Comun 3"));

        distRepo.saveAll(List.of(
                DistribucionGeografica.builder()
                        .especie(s1).region("Norte").ecosistema("Bosque")
                        .latitud(1.1).longitud(2.2).precisionMetros(10)
                        .fechaObservacion(LocalDate.of(2023,1,1))
                        .build(),
                DistribucionGeografica.builder()
                        .especie(s1).region("Sur").ecosistema("Sabana")
                        .latitud(3.3).longitud(4.4).precisionMetros(20)
                        .fechaObservacion(LocalDate.of(2023,2,2))
                        .build(),
                // de otra especie (no debe salir)
                DistribucionGeografica.builder()
                        .especie(s2).region("Este").ecosistema("Humedal")
                        .latitud(5.5).longitud(6.6).precisionMetros(30)
                        .fechaObservacion(LocalDate.of(2023,3,3))
                        .build()
        ));

        List<DistribucionResponse> list = service.listarPorEspecie(s1.getId());

        assertThat(list).hasSize(2);
        assertThat(list).extracting(DistribucionResponse::getRegion)
                .containsExactlyInAnyOrder("Norte", "Sur");
        assertThat(list).allSatisfy(r ->
                assertThat(r.getEcosistema()).isIn("Bosque", "Sabana")
        );
    }

    @Test
    @DisplayName("listarPorEspecie: 404 si la especie no existe")
    void listar_especieNoExiste() {
        assertThatThrownBy(() -> service.listarPorEspecie(12345L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Especie no encontrada");
    }
}

