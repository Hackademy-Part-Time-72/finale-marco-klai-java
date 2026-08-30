package it.aulab.progetto_finale.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import it.aulab.progetto_finale.models.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}