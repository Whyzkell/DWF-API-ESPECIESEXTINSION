package sv.edu.udb.api_especieextionsion.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import sv.edu.udb.api_especieextionsion.controller.dto.EspecieAmenazaLinkRequest;
import sv.edu.udb.api_especieextionsion.controller.dto.EspecieAmenazaResponse;
import sv.edu.udb.api_especieextionsion.domain.Amenaza;
import sv.edu.udb.api_especieextionsion.domain.Especie;
import sv.edu.udb.api_especieextionsion.domain.EspecieAmenaza;
import sv.edu.udb.api_especieextionsion.repository.AmenazaRepository;
import sv.edu.udb.api_especieextionsion.repository.EspecieAmenazaRepository;
import sv.edu.udb.api_especieextionsion.repository.EspecieRepository;
import sv.edu.udb.api_especieextionsion.service.impl.EspecieAmenazaServiceImpl;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(EspecieAmenazaServiceImpl.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class EspecieAmenazaServiceImplDataJpaTest {

    @Autowired EspecieRepository especieRepo;
    @Autowired AmenazaRepository amenazaRepo;
    @Autowired EspecieAmenazaRepository linkRepo;

    @Autowired EspecieAmenazaService service;

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

    private Amenaza amenaza(String codigo, String tipo) {
        return Amenaza.builder()
                .codigo(codigo)
                .tipo(tipo)
                .descripcion("desc")
                .build();
    }

    private EspecieAmenazaLinkRequest linkReq(Long amenazaId, String severidad) {
        var r = new EspecieAmenazaLinkRequest();
        r.setAmenazaId(amenazaId);
        r.setSeveridad(severidad);
        return r;
    }

    // ---------- tests ----------

    @Test
    @DisplayName("asociar: crea vínculo y devuelve datos de amenaza + severidad")
    void asociar_ok() {
        Especie sp = especieRepo.save(especie("NC-1", "Común 1"));
        Amenaza am = amenazaRepo.save(amenaza("A-1", "CAZA"));

        EspecieAmenazaResponse res =
                service.asociar(sp.getId(), linkReq(am.getId(), "MEDIA"));

        assertThat(res.getIdVinculo()).isNotNull();
        assertThat(res.getAmenazaId()).isEqualTo(am.getId());
        assertThat(res.getCodigo()).isEqualTo("A-1");
        assertThat(res.getTipo()).isEqualTo("CAZA");
        assertThat(res.getSeveridad()).isEqualTo("MEDIA");

        var enBd = linkRepo.findByEspecieId(sp.getId());
        assertThat(enBd).hasSize(1);
    }

    @Test
    @DisplayName("asociar: 404 si especie no existe")
    void asociar_especieNoExiste() {
        Amenaza am = amenazaRepo.save(amenaza("A-2", "PERDIDA"));
        assertThatThrownBy(() -> service.asociar(999L, linkReq(am.getId(), "BAJA")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Especie no encontrada");
    }

    @Test
    @DisplayName("asociar: 404 si amenaza no existe")
    void asociar_amenazaNoExiste() {
        Especie sp = especieRepo.save(especie("NC-2", "Común 2"));
        assertThatThrownBy(() -> service.asociar(sp.getId(), linkReq(999L, "ALTA")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Amenaza no encontrada");
    }

    @Test
    @DisplayName("asociar: conflicto si ya existe el vínculo especie-amenaza")
    void asociar_duplicado() {
        Especie sp = especieRepo.save(especie("NC-3", "Común 3"));
        Amenaza am = amenazaRepo.save(amenaza("A-3", "OTRA"));
        service.asociar(sp.getId(), linkReq(am.getId(), "BAJA"));

        assertThatThrownBy(() -> service.asociar(sp.getId(), linkReq(am.getId(), "MEDIA")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya tiene asociada esta amenaza");

        assertThat(linkRepo.findByEspecieId(sp.getId())).hasSize(1);
    }

    @Test
    @DisplayName("listarPorEspecie: devuelve solo los vínculos de esa especie")
    void listar_ok() {
        Especie s1 = especieRepo.save(especie("NC-4", "E1"));
        Especie s2 = especieRepo.save(especie("NC-5", "E2"));
        Amenaza a1 = amenazaRepo.save(amenaza("C-1", "T1"));
        Amenaza a2 = amenazaRepo.save(amenaza("C-2", "T2"));

        // crea dos vínculos para s1
        service.asociar(s1.getId(), linkReq(a1.getId(), "BAJA"));
        service.asociar(s1.getId(), linkReq(a2.getId(), "MEDIA"));
        // vínculo para otra especie (no debe salir)
        service.asociar(s2.getId(), linkReq(a1.getId(), "ALTA"));

        List<EspecieAmenazaResponse> list = service.listarPorEspecie(s1.getId());

        assertThat(list).hasSize(2);
        assertThat(list).extracting(EspecieAmenazaResponse::getCodigo)
                .containsExactlyInAnyOrder("C-1", "C-2");
    }

    @Test
    @DisplayName("listarPorEspecie: 404 si la especie no existe")
    void listar_especieNoExiste() {
        assertThatThrownBy(() -> service.listarPorEspecie(12345L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Especie no encontrada");
    }

    @Test
    @DisplayName("actualizarSeveridad: cambia la severidad del vínculo")
    void actualizarSeveridad_ok() {
        Especie sp = especieRepo.save(especie("NC-6", "E6"));
        Amenaza am = amenazaRepo.save(amenaza("D-1", "T"));
        service.asociar(sp.getId(), linkReq(am.getId(), "BAJA"));

        EspecieAmenazaResponse res =
                service.actualizarSeveridad(sp.getId(), am.getId(), "ALTA");

        assertThat(res.getSeveridad()).isEqualTo("ALTA");

        EspecieAmenaza enBd = linkRepo.findByEspecieIdAndAmenazaId(sp.getId(), am.getId())
                .orElseThrow();
        assertThat(enBd.getSeveridad()).isEqualTo("ALTA");
    }

    @Test
    @DisplayName("actualizarSeveridad: 404 si el vínculo no existe")
    void actualizarSeveridad_noExiste() {
        Especie sp = especieRepo.save(especie("NC-7", "E7"));
        Amenaza am = amenazaRepo.save(amenaza("D-2", "T2"));

        assertThatThrownBy(() -> service.actualizarSeveridad(sp.getId(), am.getId(), "MEDIA"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("no tiene asociada esa amenaza");
    }

    @Test
    @DisplayName("actualizarSeveridad: valida severidad (BAJA|MEDIA|ALTA)")
    void actualizarSeveridad_invalida() {
        Especie sp = especieRepo.save(especie("NC-8", "E8"));
        Amenaza am = amenazaRepo.save(amenaza("D-3", "T3"));
        service.asociar(sp.getId(), linkReq(am.getId(), "MEDIA"));

        assertThatThrownBy(() -> service.actualizarSeveridad(sp.getId(), am.getId(), "MEDIO"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Severidad inválida");
    }

    @Test
    @DisplayName("desasociar: elimina el vínculo existente")
    void desasociar_ok() {
        Especie sp = especieRepo.save(especie("NC-9", "E9"));
        Amenaza am = amenazaRepo.save(amenaza("D-4", "T4"));
        service.asociar(sp.getId(), linkReq(am.getId(), "BAJA"));

        service.desasociar(sp.getId(), am.getId());

        assertThat(linkRepo.findByEspecieIdAndAmenazaId(sp.getId(), am.getId())).isEmpty();
    }

    @Test
    @DisplayName("desasociar: 404 si el vínculo no existe")
    void desasociar_noExiste() {
        Especie sp = especieRepo.save(especie("NC-10", "E10"));
        Amenaza am = amenazaRepo.save(amenaza("D-5", "T5"));

        assertThatThrownBy(() -> service.desasociar(sp.getId(), am.getId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("no tiene asociada esa amenaza");
    }
}

