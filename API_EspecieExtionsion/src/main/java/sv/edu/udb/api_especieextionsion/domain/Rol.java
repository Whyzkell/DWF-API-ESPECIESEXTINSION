package sv.edu.udb.api_especieextionsion.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Rol del usuario en el sistema")
public enum Rol {
    ADMIN, EDITOR, LECTOR
}
