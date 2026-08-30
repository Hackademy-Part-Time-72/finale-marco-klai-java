
package it.aulab.progetto_finale.dtos;

import it.aulab.progetto_finale.models.Category;
import it.aulab.progetto_finale.models.Image;
import it.aulab.progetto_finale.models.User;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ArticleDto {
    private Long id;

    @NotEmpty(message = "Il titolo è obbligatorio")
    private String title;

    @NotEmpty(message = "Il sottotitolo è obbligatorio")
    private String subtitle;

    @NotEmpty(message = "Il testo dell'articolo è obbligatorio")
    private String body;

    private LocalDate publishDate;

    private Boolean isAccepted;

    private User user;

    private Category category;

    @NotNull(message = "La categoria è obbligatoria")
    private Long categoryId;

    private Image image;

    // Getter e Setter manuali di supporto (opzionali grazie a Lombok, ma mantenuti per sicurezza)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public LocalDate getPublishDate() { return publishDate; }
    public void setPublishDate(LocalDate publishDate) { this.publishDate = publishDate; }

    public Boolean getIsAccepted() { return isAccepted; }
    public void setIsAccepted(Boolean isAccepted) { this.isAccepted = isAccepted; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Image getImage() { return image; }
    public void setImage(Image image) { this.image = image; }
}