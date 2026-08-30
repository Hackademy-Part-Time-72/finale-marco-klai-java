package it.aulab.progetto_finale.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "articles")
public class Article {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @NotEmpty
    @Size(max = 100)
    private String title;

    @Column(nullable = false, length = 100)
    @NotEmpty
    @Size(max = 100)
    private String subtitle;

    @Column(nullable = false, length = 1000, columnDefinition = "TEXT")
    @NotEmpty
    @Size(max = 1000)
    private String body;

    @Column(nullable = true, length = 8)
    @NotNull
    private LocalDate publishDate;

    // Campo per la gestione dello stato di accettazione dell'articolo
    @Column(nullable = true)
    private Boolean isAccepted;

    // Collegamento all'autore dell'articolo
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties("articles")
    private User user;

    // Collegamento alla categoria dell'articolo
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties("articles")
    private Category category;

    // Collegamento all'immagine dell'articolo
    @OneToOne(mappedBy = "article")
    @JsonIgnoreProperties("article")
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

    public Image getImage() { return image; }
    public void setImage(Image image) { this.image = image; }

    @Override
    public boolean equals(Object obj) {
        
        Article article = (Article) obj;
        
        if(title.equals(article.getTitle()) &&
           subtitle.equals(article.getSubtitle()) &&
           body.equals(article.getBody()) &&
           publishDate.equals(article.getPublishDate()) &&
           category.getName().equals(article.getCategory().getName()) &&
           image.getPath().equals(article.getImage().getPath())){
            return true;
        }
        
        return false;
    }
}