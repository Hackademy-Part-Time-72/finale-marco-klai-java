package it.aulab.progetto_finale.services;

import it.aulab.progetto_finale.dtos.ArticleDto;
import it.aulab.progetto_finale.models.Article;
import it.aulab.progetto_finale.models.Category;
import it.aulab.progetto_finale.models.User;
import it.aulab.progetto_finale.repositories.ArticleRepository;
import it.aulab.progetto_finale.repositories.CategoryRepository;
import it.aulab.progetto_finale.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class ArticleService implements CrudService<ArticleDto, Article, Long> {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageService imageService;

    private ModelMapper modelMapper = new ModelMapper();

    @Override
    public List<ArticleDto> readAll() {
        List<Article> articles = articleRepository.findAllByOrderByIdDesc();
        return articles.stream()
                .map(article -> modelMapper.map(article, ArticleDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public ArticleDto read(Long key) {
        Article article = articleRepository.findById(key).orElse(null);
        if (article != null) {
            return modelMapper.map(article, ArticleDto.class);
        }
        return null;
    }

    @Override
    public ArticleDto create(Article article, Principal principal, MultipartFile file) {
        article.setPublishDate(LocalDate.now());
        
        // Imposta lo stato di accettazione iniziale a null (in attesa di revisione)
        article.setIsAccepted(null);

        String url = "";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getId()).get();
            article.setUser(user);
        }

        if (file != null && !file.isEmpty()) {
            try {
                CompletableFuture<String> futureUrl = imageService.saveImageOnCloud(file);
                url = futureUrl.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ArticleDto dto = modelMapper.map(articleRepository.save(article), ArticleDto.class);

        if (file != null && !file.isEmpty() && url != null && !url.isEmpty()) {
            imageService.saveImageOnDB(url, article);
        }

        return dto;
    }

    @Override
    public ArticleDto update(Long key, Article updatedArticle, MultipartFile file) {
        String url="";

        //Controllo l'esistenza dell'articolo in base al suo id
        if (articleRepository.existsById(key)) {
            //Assegno all'articolo proveniente dal form lo stesso id dell'articolo originale
            updatedArticle.setId(key);
            //Recupero l'articolo originale non modificato
            Article article = articleRepository.findById(key).get();
            //Imposto l'utente dell'articolo del form con l'utente dell'articolo originale
            updatedArticle.setUser(article.getUser());

            //Faccio un controllo sulla presenza o meno del file nell'articolo del form quindi capisco se devo modificare o meno l'immagine
            if(!file.isEmpty()){
                try {
                    //Elimino l'immagine precedente
                    imageService.deleteImage(article.getImage().getPath());
                    try {
                        //Salvo la nuova immagine
                        CompletableFuture<String> futureUrl = imageService.saveImageOnCloud(file);
                        url = futureUrl.get();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //Salvo il nuovo path nel db
                    imageService.saveImageOnDB(url, updatedArticle);

                    //Essendo l'immagine modificata l'articolo torna in revisione
                    updatedArticle.setIsAccepted(null);
                    return modelMapper.map(articleRepository.save(updatedArticle), ArticleDto.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if(article.getImage() == null){//Se l'articolo originale non ha un'immagine e nemmeno quello da modificare allora sicuramente non è stata fatta alcuna modifica
                updatedArticle.setIsAccepted(article.getIsAccepted());
            } else {
                //Se l'immagine non è stata modificata devo fare un check su tutti gli altri campi se diversi l'articolo torna in revisione

                //Se l'immagine non è stata modificata posso impostare sull'articolo modificato la stessa immagine dell'articolo di originale
                updatedArticle.setImage(article.getImage());

                if(updatedArticle.equals(article)==false){
                    updatedArticle.setIsAccepted(null);
                }else{
                    updatedArticle.setIsAccepted(article.getIsAccepted());
                }
            }
            return modelMapper.map(articleRepository.save(updatedArticle), ArticleDto.class);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public void delete(Long key) {
        if (articleRepository.existsById(key)) {
            
            Article article = articleRepository.findById(key).get();
            
            try {
                String path = article.getImage().getPath();
                article.getImage().setArticle(null);
                imageService.deleteImage(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            articleRepository.deleteById(key);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    // Metodi di supporto aggiuntivi
    public List<Article> findAll() {
        return articleRepository.findAllByOrderByIdDesc();
    }

    public List<Article> findByCategory(Category category) {
        return articleRepository.findByCategoryOrderByIdDesc(category);
    }

    public List<Article> findByUser(User user) {
        return articleRepository.findByUserOrderByIdDesc(user);
    }

    public List<Article> findLastArticles(int limit) {
        List<Article> allArticles = findAll();
        if (allArticles.size() > limit) {
            return allArticles.subList(0, limit);
        }
        return allArticles;
    }

    public Article findById(Long id) {
        return articleRepository.findById(id).orElse(null);
    }

    public void setIsAccepted(Boolean result, Long id){
        Article article = articleRepository.findById(id).get();
        article.setIsAccepted(result);
        articleRepository.save(article);
    }

    public List<ArticleDto> searchByCategory(Category category) {
        List<Article> articles = articleRepository.findByCategoryOrderByIdDesc(category);
        return articles.stream()
                .map(article -> modelMapper.map(article, ArticleDto.class))
                .collect(Collectors.toList());
    }

    public List<ArticleDto> searchByAuthor(User user) {
        List<Article> articles = articleRepository.findByUserOrderByIdDesc(user);
        return articles.stream()
                .map(article -> modelMapper.map(article, ArticleDto.class))
                .collect(Collectors.toList());
    }

    public List<ArticleDto> search(String keyword) {
        List<ArticleDto> dtos = new ArrayList<ArticleDto>();
        for(Article article: articleRepository.search(keyword)){
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;
    }
}