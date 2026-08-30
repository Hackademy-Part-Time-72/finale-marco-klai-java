package it.aulab.progetto_finale.controllers;

import it.aulab.progetto_finale.dtos.ArticleDto;
import it.aulab.progetto_finale.dtos.CategoryDto;
import it.aulab.progetto_finale.models.Category;
import it.aulab.progetto_finale.services.ArticleService;
import it.aulab.progetto_finale.services.CategoryService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    public String categoriesIndex(Model model) {
        model.addAttribute("categories", categoryService.readAll());
        model.addAttribute("category", new CategoryDto());
        return "category/index";
    }

    // Rotta per la memorizzazione di una categoria
    @PostMapping("/save")
    public String categoryStore(@Valid @ModelAttribute("category") Category category,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Model viewModel) {
        
        // Controllo degli errori con validazioni
        if (result.hasErrors()) {
            viewModel.addAttribute("title", "Crea una categoria");
            viewModel.addAttribute("category", category);
            return "category/create";
        }
        
        // Chiamata aggiornata per combaciare con la nuova firma del Service (3 parametri)
        categoryService.create(category, null, null);
        redirectAttributes.addFlashAttribute("successMessage", "Categoria aggiunta con successo!");
        
        // Reindirizzamento alla dashboard admin
        return "redirect:/admin/dashboard";
    }

    // Rotta per la creazione di una categoria
    @GetMapping("/create")
    public String categoryCreate(Model viewModel) {
        viewModel.addAttribute("title", "Crea una categoria");
        viewModel.addAttribute("category", new Category());
        return "category/create";
    }

    // Rotta per la modifica di una categoria
    @GetMapping("/edit/{id}")
    public String categoryEdit(@PathVariable("id") Long id, Model viewModel) {
        viewModel.addAttribute("title", "Modifica categoria");
        viewModel.addAttribute("category", categoryService.read(id));
        return "category/update";
    }

    // Rotta per la memorizzazione delle modifiche di una categoria
    @PostMapping("/update/{id}")
    public String categoryUpdate(@PathVariable("id") Long id,
                                   @Valid @ModelAttribute("category") Category category,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes,
                                   Model viewModel) {
        
        // Controllo degli errori con validazioni
        if (result.hasErrors()) {
            viewModel.addAttribute("title", "Modifica categoria");
            viewModel.addAttribute("category", category);
            return "category/update";
        }
        
        categoryService.update(id, category, null);
        redirectAttributes.addFlashAttribute("successMessage", "Categoria modificata con successo!");
        
        return "redirect:/admin/dashboard";
    }

    // Rotta per la cancellazione di una categoria
    @GetMapping("delete/{id}")
    public String categoryDelete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        
        categoryService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Categoria cancellata con successo!");
        
        return "redirect:/admin/dashboard";
    }

    // Rotta per la ricerca dell'articolo in base alla categoria
    @GetMapping("/search/{id}")
    public String categorySearch(@PathVariable("id") Long id, Model viewModel) {
        CategoryDto category = categoryService.read(id);
        viewModel.addAttribute("title", "Tutti gli articoli trovati per categoria " + category.getName());

        List<ArticleDto> articles = articleService.searchByCategory(modelMapper.map(category, Category.class));
        List<ArticleDto> acceptedArticles = articles.stream().filter(article -> Boolean.TRUE.equals(article.getIsAccepted())).collect(Collectors.toList());
        viewModel.addAttribute("articles", acceptedArticles);

        return "article/index";
    }
}