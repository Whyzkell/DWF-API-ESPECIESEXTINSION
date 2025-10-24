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
import sv.edu.udb.api_especieextionsion.mapping.DistribucionMapper;
import sv.edu.udb.api_especieextionsion.repository.DistribucionRepository;
import sv.edu.udb.api_especieextionsion.repository.EspecieRepository;
import sv.edu.udb.api_especieextionsion.repository.domain.DistribucionGeografica;
import sv.edu.udb.api_especieextionsion.repository.domain.Especie;
import sv.edu.udb.api_especieextionsion.service.impl.DistribucionServiceImpl;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({DistribucionServiceImpl.class, DistribucionMapper.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class DistribucionServiceImplDataJpaTest {

    @Autowired EspecieRepository especieRepo;
    @Autowired DistribucionRepository distRepo;

    @Autowired DistribucionService service;

    // ===== helpers =====
    private Especie especie(String nc, String nombre, String tipo, String estado,
                            boolean endemica, LocalDate fecha) {
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

    private DistribucionRequest req(String region, String eco, double lat, double lon,
                                    Integer precision, LocalDate fecha) {
        return DistribucionRequest.builder()
                .region(region)
                .ecosistema(eco)
                .latitud(lat)
                .longitud(lon)
                .precisionMetros(precision)
                .fechaObservacion(fecha)
                .build();
    }

    private DistribucionGeografica dist(Especie e, String region, String eco, double lat, double lon,
                                        Integer prec, LocalDate fecha) {
        return DistribucionGeografica.builder()
                .especie(e)
                .region(region)
                .ecosistema(eco)
                .latitud(lat)
                .longitud(lon)
                .precisionMetros(prec)
                .fechaObservacion(fecha)
                .build();
    }

    // ===== tests =====

    @Test
    @DisplayName("crear: persiste y devuelve DistribucionResponse mapeado")
    void crear_ok() {
        Especie sp = especieRepo.save(
                especie("Panthera onca", "Jaguar", "FAUNA", "VU", false, LocalDate.now())
        );

        DistribucionResponse res = service.crear(
                sp.getId(),
                req("Centroamérica", "Bosque tropical", 13.6929, -89.2182, 50, LocalDate.now())
        );

        assertThat(res.getId()).isNotNull();
        assertThat(res.getRegion()).isEqualTo("Centroamérica");
        assertThat(res.getEcosistema()).isEqualTo("Bosque tropical");
        assertThat(res.getLatitud()).isEqualTo(13.6929);
        assertThat(res.getLongitud()).isEqualTo(-89.2182);

        // comprobamos en DB
        assertThat(distRepo.count()).isEqualTo(1);
        var inDb = distRepo.findAll().get(0);
        assertThat(inDb.getEspecie().getId()).isEqualTo(sp.getId());
    }

    @Test
    @DisplayName("crear: 404 si la especie no existe")
    void crear_notFound() {
        assertThatThrownBy(() ->
                service.crear(999L, req("CA", "Bosque", 10, -80, 10, LocalDate.now()))
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Especie no encontrada");
        assertThat(distRepo.count()).isZero();
    }

    @Test
    @DisplayName("listarPorEspecie: devuelve todas las distribuciones mapeadas")
    void listar_ok() {
        Especie sp = especieRepo.save(
                especie("Ara macao", "Guacamaya", "FAUNA", "LC", false, LocalDate.now())
        );

        distRepo.saveAll(List.of(
                dist(sp, "Belice", "Selva", 17.0, -88.0, 30, LocalDate.now()),
                dist(sp, "Guatemala", "Bosque nuboso", 15.5, -90.2, 40, LocalDate.now())
        ));

        List<DistribucionResponse> list = service.listarPorEspecie(sp.getId());

        assertThat(list).hasSize(2);
        assertThat(list).extracting(DistribucionResponse::getRegion)
                .containsExactlyInAnyOrder("Belice", "Guatemala");
        assertThat(list).extracting(DistribucionResponse::getEcosistema)
                .containsExactlyInAnyOrder("Selva", "Bosque nuboso");
    }

    @Test
    @DisplayName("listarPorEspecie: 404 cuando la especie no existe")
    void listar_notFound() {
        assertThatThrownBy(() -> service.listarPorEspecie(12345L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Especie no encontrada");
    }
}

