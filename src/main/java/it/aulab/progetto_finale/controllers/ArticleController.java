package it.aulab.progetto_finale.controllers;

import it.aulab.progetto_finale.dtos.ArticleDto;
import it.aulab.progetto_finale.models.Article;
import it.aulab.progetto_finale.models.Category;
import it.aulab.progetto_finale.models.User;
import it.aulab.progetto_finale.repositories.ArticleRepository;
import it.aulab.progetto_finale.repositories.CategoryRepository;
import it.aulab.progetto_finale.repositories.UserRepository;
import it.aulab.progetto_finale.services.ArticleService;
import it.aulab.progetto_finale.services.CrudService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    @Qualifier("categoryService")
    private CrudService<it.aulab.progetto_finale.dtos.CategoryDto, Category, Long> categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/create")
    public String createArticleForm(Model model) {
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("article", new ArticleDto());
        return "article/create";
    }

    @PostMapping("/save")
    public String saveArticle(@Valid @ModelAttribute("article") ArticleDto articleDto,
                              BindingResult result,
                              @RequestParam("file") MultipartFile file,
                              Principal principal,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            List<Category> categories = categoryRepository.findAll();
            model.addAttribute("categories", categories);
            return "article/create";
        }

        Article article = modelMapper.map(articleDto, Article.class);
        if (articleDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(articleDto.getCategoryId()).orElse(null);
            article.setCategory(category);
        }

        articleService.create(article, principal, file);
        
        redirectAttributes.addFlashAttribute("successMessage", "Articolo creato con successo!");
        return "redirect:/";
    }

    @GetMapping("/index")
    public String articlesIndex(Model viewModel) {
        viewModel.addAttribute("title", "Tutti gli articoli");

        List<ArticleDto> articles = new ArrayList<>();
        for (Article article : articleRepository.findByIsAcceptedTrue()) {
            articles.add(modelMapper.map(article, ArticleDto.class));
        }

        Collections.sort(articles, Comparator.comparing(ArticleDto::getPublishDate).reversed());
        viewModel.addAttribute("articles", articles);

        return "article/index";
    }

    @GetMapping("/category/{id}")
    public String articlesByCategory(@PathVariable("id") Long id, Model model) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category != null) {
            model.addAttribute("articles", articleService.findByCategory(category));
            model.addAttribute("categoryName", category.getName());
        }
        return "article/index";
    }

    @GetMapping("/user/{id}")
    public String articlesByUser(@PathVariable("id") Long id, Model model) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            model.addAttribute("articles", articleService.findByUser(user));
            model.addAttribute("userName", user.getUsername());
        }
        return "article/index";
    }

    @GetMapping("/detail/{id}")
    public String articleDetail(@PathVariable("id") Long id, Model model) {
        Article article = articleService.findById(id);
        model.addAttribute("article", article);
        return "article/detail";
    }

    //Rotta di modifica di un articolo
    @GetMapping("/edit/{id}")
    public String editArticle(@PathVariable("id") Long id, Model viewModel, Principal principal) {
        ArticleDto article = articleService.read(id);
        
        // Controllo di sicurezza: l'utente loggato è l'autore dell'articolo?
        if (!article.getUser().getEmail().equals(principal.getName())) {
            return "redirect:/writer/dashboard";
        }
        
        viewModel.addAttribute("title", "Article update");
        viewModel.addAttribute("article", article);
        viewModel.addAttribute("categories", categoryService.readAll());
        return "article/edit";
    }

    //Rotta di memorizzazione modifica di un articolo
    @PostMapping("/update/{id}")
    public String articleUpdate(@PathVariable("id") Long id,
                                @Valid @ModelAttribute("article") Article article,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Principal principal,
                                MultipartFile file,
                                Model viewModel) {
        
        ArticleDto originalArticle = articleService.read(id);
        
        // Controllo di sicurezza: l'utente loggato è l'autore dell'articolo?
        if (!originalArticle.getUser().getEmail().equals(principal.getName())) {
            return "redirect:/writer/dashboard";
        }
        
        //Controllo degli errori con validazioni
        if (result.hasErrors()) {
            viewModel.addAttribute("title", "Article update");
            article.setImage(articleService.read(id).getImage());
            viewModel.addAttribute("article", article);
            viewModel.addAttribute("categories", categoryService.readAll());
            return "article/edit";
        }
        
        articleService.update(id, article, file);
        redirectAttributes.addFlashAttribute("successMessage", "Articolo modificato con successo!");
        
        return "redirect:/writer/dashboard";
    }

    // Rotta dettaglio di un articolo per il revisore
    @GetMapping("/revisor/detail/{id}")
    public String revisorDetailArticle(@PathVariable("id") Long id, Model viewModel) {
        viewModel.addAttribute("title", "Article detail");
        viewModel.addAttribute("article", articleService.findById(id));
        return "revisor/detail";
    }

    // Rotta dedicata all'azione del revisore
    @PostMapping("/accept")
    public String articleSetAccepted(@RequestParam("action") String action, @RequestParam("articleId") Long articleId, RedirectAttributes redirectAttributes) {
        if(action.equals("accept")){
            articleService.setIsAccepted(true, articleId);
            redirectAttributes.addFlashAttribute("resultMessage", "Articolo accettato!");
        }else if(action.equals("reject")){
            articleService.setIsAccepted(false, articleId);
            redirectAttributes.addFlashAttribute("resultMessage", "Articolo rifiutato!");
        }else{
            redirectAttributes.addFlashAttribute("resultMessage", "Azione non corretta!");
        }

        return "redirect:/revisor/dashboard";
    }

    //Rotta per la cancellazione di un articolo
    @GetMapping("/delete/{id}")
    public String articleDelete(@PathVariable("id") Long id, Principal principal, RedirectAttributes redirectAttributes) {
        
        Article article = articleService.findById(id);
        
        // Controllo di sicurezza: l'utente loggato è l'autore dell'articolo?
        if (article.getUser().getEmail().equals(principal.getName())) {
            articleService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Articolo cancellato con successo!");
        }
        
        return "redirect:/writer/dashboard";
    }
    
   // Rotta di ricerca di un articolo
    @GetMapping("/search")
    public String articleSearch(@RequestParam(name = "keyword", required = false) String keyword, Model viewModel) {
        viewModel.addAttribute("title", "Tutti gli articoli trovati");
        
        // Evita errori se l'utente clicca "Search" senza scrivere nulla
        if (keyword == null) {
            keyword = "";
        }

        List<ArticleDto> articles = articleService.search(keyword);
        
        // Filtro per visualizzare nella ricerca solo gli articoli accettati
        List<ArticleDto> acceptedArticles = articles.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsAccepted()))
                .collect(Collectors.toList());

        viewModel.addAttribute("articles", acceptedArticles);
 
        return "article/index";
    }
}