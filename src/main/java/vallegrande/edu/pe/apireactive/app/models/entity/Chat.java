package vallegrande.edu.pe.apireactive.app.models.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("chat")
public class Chat {

    @Id
    private Long id;

    @Column("request_content")
    private String requestContent;

    @Column("response_content")
    private String responseContent;

    @Column("model")
    private String model;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("deleted")
    private Boolean deleted = false; // Nuevo campo para borrado lógico
}