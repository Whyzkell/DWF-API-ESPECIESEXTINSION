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
import sv.edu.udb.api_especieextionsion.mapping.FakeEspecieAmenazaMapper;
import sv.edu.udb.api_especieextionsion.repository.AmenazaRepository;
import sv.edu.udb.api_especieextionsion.repository.EspecieAmenazaRepository;
import sv.edu.udb.api_especieextionsion.repository.EspecieRepository;
import sv.edu.udb.api_especieextionsion.repository.domain.Amenaza;
import sv.edu.udb.api_especieextionsion.repository.domain.Especie;
import sv.edu.udb.api_especieextionsion.repository.domain.EspecieAmenaza;
import sv.edu.udb.api_especieextionsion.service.impl.EspecieAmenazaServiceImpl;
import sv.edu.udb.api_especieextionsion.shared.DuplicateResourceException;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({EspecieAmenazaServiceImpl.class, FakeEspecieAmenazaMapper.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class EspecieAmenazaServiceImplDataJpaTest {

    @Autowired EspecieRepository especieRepo;
    @Autowired AmenazaRepository amenazaRepo;
    @Autowired EspecieAmenazaRepository linkRepo;
    @Autowired EspecieAmenazaService service;

    // ===== helpers =====
    private Especie especie(String nc, String nombre, String tipo) {
        return Especie.builder()
                .nombreCientifico(nc)
                .nombreComun(nombre)
                .tipo(tipo)
                .estadoConservacion("VU")
                .descripcion("desc")
                .esEndemica(false)
                .fechaRegistro(java.time.LocalDate.now())
                .build();
    }

    private Amenaza amenaza(String codigo, String tipo, String desc) {
        return Amenaza.builder()
                .codigo(codigo)
                .tipo(tipo)
                .descripcion(desc)
                .build();
    }

    private EspecieAmenazaLinkRequest linkReq(Long amenazaId, String severidad) {
        return EspecieAmenazaLinkRequest.builder()
                .amenazaId(amenazaId)
                .severidad(severidad)
                .build();
    }

    // ===== tests =====

    @Test
    @DisplayName("asociar: crea vínculo cuando especie y amenaza existen y no hay duplicado")
    void asociar_ok() {
        Especie e = especieRepo.save(especie("Panthera onca", "Jaguar", "FAUNA"));
        var a = amenazaRepo.save(amenaza("INCENDIO", "HUMANA", "Incendio forestal"));

        EspecieAmenazaResponse res = service.asociar(e.getId(), linkReq(a.getId(), "ALTA"));

        assertThat(res.getIdVinculo()).isNotNull();
        assertThat(res.getAmenazaId()).isEqualTo(a.getId());
        assertThat(res.getCodigo()).isEqualTo("INCENDIO");
        assertThat(res.getSeveridad()).isEqualTo("ALTA");

        assertThat(linkRepo.existsByEspecieIdAndAmenazaId(e.getId(), a.getId())).isTrue();
    }

    @Test
    @DisplayName("asociar: 404 si la especie no existe")
    void asociar_especieNotFound() {
        var a = amenazaRepo.save(amenaza("DEFOREST", "HUMANA", "Deforestación"));
        assertThatThrownBy(() -> service.asociar(999L, linkReq(a.getId(), "MEDIA")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Especie no encontrada");
    }

    @Test
    @DisplayName("asociar: 404 si la amenaza no existe")
    void asociar_amenazaNotFound() {
        Especie e = especieRepo.save(especie("Ara macao", "Guacamaya", "FAUNA"));
        assertThatThrownBy(() -> service.asociar(e.getId(), linkReq(777L, "BAJA")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Amenaza no encontrada");
    }

    @Test
    @DisplayName("asociar: 409 si ya existe ese vínculo especie-amenaza")
    void asociar_duplicate() {
        Especie e = especieRepo.save(especie("Quercus robur", "Roble", "FLORA"));
        var a = amenazaRepo.save(amenaza("PLAGA", "BIOLOGICA", "Plaga X"));

        // vínculo existente
        linkRepo.save(EspecieAmenaza.builder()
                .especie(e).amenaza(a).severidad("MEDIA").build());

        assertThatThrownBy(() -> service.asociar(e.getId(), linkReq(a.getId(), "ALTA")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ya tiene asociada");
    }

    @Test
    @DisplayName("listarPorEspecie: devuelve vínculos mapeados de una especie")
    void listarPorEspecie_ok() {
        Especie e = especieRepo.save(especie("Test sp", "Prueba", "FAUNA"));
        var a1 = amenazaRepo.save(amenaza("A1", "T1", "d1"));
        var a2 = amenazaRepo.save(amenaza("A2", "T2", "d2"));

        linkRepo.saveAll(List.of(
                EspecieAmenaza.builder().especie(e).amenaza(a1).severidad("BAJA").build(),
                EspecieAmenaza.builder().especie(e).amenaza(a2).severidad("ALTA").build()
        ));

        var list = service.listarPorEspecie(e.getId());

        assertThat(list).hasSize(2);
        assertThat(list).extracting(EspecieAmenazaResponse::getCodigo)
                .containsExactlyInAnyOrder("A1", "A2");
    }

    @Test
    @DisplayName("listarPorEspecie: 404 si la especie no existe")
    void listarPorEspecie_notFound() {
        assertThatThrownBy(() -> service.listarPorEspecie(12345L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Especie no encontrada");
    }

    @Test
    @DisplayName("actualizarSeveridad: cambia severidad cuando vínculo existe y severidad válida")
    void actualizarSeveridad_ok() {
        Especie e = especieRepo.save(especie("Bos taurus", "Vaca", "FAUNA"));
        var a = amenazaRepo.save(amenaza("SEQUIA", "CLIMATICA", "Sequía"));

        var link = linkRepo.save(EspecieAmenaza.builder()
                .especie(e).amenaza(a).severidad("BAJA").build());

        var res = service.actualizarSeveridad(e.getId(), a.getId(), "ALTA");

        assertThat(res.getIdVinculo()).isEqualTo(link.getId());
        assertThat(res.getSeveridad()).isEqualTo("ALTA");

        // DB
        assertThat(linkRepo.findById(link.getId()).orElseThrow().getSeveridad()).isEqualTo("ALTA");
    }

    @Test
    @DisplayName("actualizarSeveridad: 400 si severidad inválida")
    void actualizarSeveridad_invalida() {
        Especie e = especieRepo.save(especie("Canis lupus", "Lobo", "FAUNA"));
        var a = amenazaRepo.save(amenaza("CAZA", "HUMANA", "Caza ilegal"));

        linkRepo.save(EspecieAmenaza.builder()
                .especie(e).amenaza(a).severidad("MEDIA").build());

        assertThatThrownBy(() -> service.actualizarSeveridad(e.getId(), a.getId(), "EXTREMA"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Severidad inválida");
    }

    @Test
    @DisplayName("actualizarSeveridad: 404 si el vínculo no existe")
    void actualizarSeveridad_linkNotFound() {
        Especie e = especieRepo.save(especie("Felis catus", "Gato", "FAUNA"));
        var a = amenazaRepo.save(amenaza("URB", "HUMANA", "Urbanización"));

        assertThatThrownBy(() -> service.actualizarSeveridad(e.getId(), a.getId(), "BAJA"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("no tiene asociada esa amenaza");
    }

    @Test
    @DisplayName("desasociar: elimina vínculo existente")
    void desasociar_ok() {
        Especie e = especieRepo.save(especie("Test2", "Nombre", "FAUNA"));
        var a = amenazaRepo.save(amenaza("A-DEL", "T-DEL", "del"));

        var link = linkRepo.save(EspecieAmenaza.builder()
                .especie(e).amenaza(a).severidad("MEDIA").build());

        service.desasociar(e.getId(), a.getId());

        assertThat(linkRepo.existsById(link.getId())).isFalse();
    }

    @Test
    @DisplayName("desasociar: 404 si el vínculo no existe")
    void desasociar_notFound() {
        Especie e = especieRepo.save(especie("Test3", "Nombre", "FAUNA"));
        var a = amenazaRepo.save(amenaza("A-NF", "T-NF", "nf"));

        assertThatThrownBy(() -> service.desasociar(e.getId(), a.getId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("no tiene asociada esa amenaza");
    }
}
