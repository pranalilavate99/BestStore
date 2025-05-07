package in.demo.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import in.demo.modal.Product;

public interface ProductRepository extends CrudRepository<Product, Integer>{

	List<Product> findAll(Sort by);

	

	

}
