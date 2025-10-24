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
import sv.edu.udb.api_especieextionsion.mapping.FakeDistribucionMapper;
import sv.edu.udb.api_especieextionsion.repository.DistribucionRepository;
import sv.edu.udb.api_especieextionsion.repository.EspecieRepository;
import sv.edu.udb.api_especieextionsion.repository.domain.DistribucionGeografica;
import sv.edu.udb.api_especieextionsion.repository.domain.Especie;
import sv.edu.udb.api_especieextionsion.service.impl.DistribucionServiceImpl;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({DistribucionServiceImpl.class, FakeDistribucionMapper.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class DistribucionServiceImplDataJpaTest {

    @Autowired EspecieRepository especieRepo;
    @Autowired DistribucionRepository distRepo;
    @Autowired DistribucionService service;

    // ===== helpers =====
    private Especie especie(String nc, String nombre, String tipo, String estado, boolean endemica, LocalDate fecha) {
        return Especie.builder()
                .nombreCientifico(nc)
                .nombreComun(nombre)
                .tipo(tipo)
                .estadoConservacion(estado)
                .descripcion("desc")
                .esEndemica(endemica)
                .fechaRegistro(fecha)
                .build();
    }

    private DistribucionRequest req(
            String region, String ecosistema,
            double lat, double lng, Integer precision, LocalDate fechaObs
    ) {
        return DistribucionRequest.builder()
                .region(region)
                .ecosistema(ecosistema)
                .latitud(lat)
                .longitud(lng)
                .precisionMetros(precision)
                .fechaObservacion(fechaObs)
                .build();
    }

    // ===== tests =====

    @Test
    @DisplayName("crear: persiste la distribución asociada a una especie y mapea response")
    void crear_ok() {
        Especie e = especieRepo.save(
                especie("Panthera onca", "Jaguar", "FAUNA", "VU", false, LocalDate.now())
        );

        DistribucionResponse res = service.crear(
                e.getId(),
                req("Centroamérica", "Bosque tropical", 13.6929, -89.2182, 50, LocalDate.of(2024, 9, 18))
        );

        assertThat(res.getId()).isNotNull();
        assertThat(res.getRegion()).isEqualTo("Centroamérica");
        assertThat(res.getEcosistema()).isEqualTo("Bosque tropical");
        assertThat(res.getLatitud()).isEqualTo(13.6929);
        assertThat(res.getLongitud()).isEqualTo(-89.2182);

        // verificación en DB
        assertThat(distRepo.count()).isEqualTo(1);
        DistribucionGeografica inDb = distRepo.findAll().get(0);
        assertThat(inDb.getEspecie().getId()).isEqualTo(e.getId());
    }

    @Test
    @DisplayName("crear: 404 si la especie no existe")
    void crear_notFound() {
        assertThatThrownBy(() ->
                service.crear(999L, req("Reg", "Eco", 1.0, 2.0, 10, LocalDate.now()))
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Especie no encontrada");

        assertThat(distRepo.count()).isZero();
    }

    @Test
    @DisplayName("listarPorEspecie: devuelve todas las distribuciones mapeadas de esa especie")
    void listarPorEspecie_ok() {
        Especie e = especieRepo.save(
                especie("Ara macao", "Guacamaya roja", "FAUNA", "LC", false, LocalDate.now())
        );

        // persistimos algunas distribuciones
        distRepo.saveAll(List.of(
                DistribucionGeografica.builder()
                        .especie(e)
                        .region("R1").ecosistema("Eco1")
                        .latitud(10.0).longitud(-80.0)
                        .precisionMetros(30).fechaObservacion(LocalDate.of(2024, 1, 1))
                        .build(),
                DistribucionGeografica.builder()
                        .especie(e)
                        .region("R2").ecosistema("Eco2")
                        .latitud(11.0).longitud(-81.0)
                        .precisionMetros(40).fechaObservacion(LocalDate.of(2024, 2, 2))
                        .build()
        ));

        List<DistribucionResponse> list = service.listarPorEspecie(e.getId());

        assertThat(list).hasSize(2);
        assertThat(list).extracting(DistribucionResponse::getRegion)
                .containsExactlyInAnyOrder("R1", "R2");
    }

    @Test
    @DisplayName("listarPorEspecie: 404 si la especie no existe")
    void listarPorEspecie_notFound() {
        assertThatThrownBy(() -> service.listarPorEspecie(123456L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Especie no encontrada");
    }
}
