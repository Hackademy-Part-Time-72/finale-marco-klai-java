
package it.aulab.progetto_finale.services;

import it.aulab.progetto_finale.dtos.CategoryDto;
import it.aulab.progetto_finale.models.Article;
import it.aulab.progetto_finale.models.Category;
import it.aulab.progetto_finale.repositories.CategoryRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService implements CrudService<CategoryDto, Category, Long> {

    @Autowired
    private CategoryRepository categoryRepository;

    // Inizializziamo ModelMapper direttamente per evitare errori di configurazione
    private ModelMapper modelMapper = new ModelMapper();

    @Override
    public List<CategoryDto> readAll() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(category -> modelMapper.map(category, CategoryDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto read(Long key) {
        Category category = categoryRepository.findById(key)
                .orElseThrow(() -> new RuntimeException("Categoria non trovata"));
        return modelMapper.map(category, CategoryDto.class);
    }

    @Override
    public CategoryDto create(Category model, Principal principal, MultipartFile file) {
        return modelMapper.map(categoryRepository.save(model), CategoryDto.class);
    }

    @Override
    public CategoryDto update(Long key, Category model, MultipartFile file) {
        if (categoryRepository.existsById(key)) {
            model.setId(key);
            return modelMapper.map(categoryRepository.save(model), CategoryDto.class);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (categoryRepository.existsById(id)) {
            Category category = categoryRepository.findById(id).get();
            
            if (category.getArticles() != null) {
                Iterable<Article> articles = category.getArticles();
                for (Article article : articles) {
                    article.setCategory(null);
                }
            }
            
            categoryRepository.deleteById(id);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}