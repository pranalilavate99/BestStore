package in.demo.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Files.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import in.demo.modal.Product;
import in.demo.modal.ProductDto;
import in.demo.repository.ProductRepository;


@Controller
@RequestMapping("/products")
public class ProductsController {
	@Autowired
	private ProductRepository repo;
	
	@GetMapping({"", "/"})
	public String showProductList(Model model) {
	    List<Product> products = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
	    model.addAttribute("products", products);
	    return "products/index";
	}
	
	@GetMapping({"/create"})
	public String showCreatePage(Model model) {
	    ProductDto productDto = new ProductDto();
	    model.addAttribute("productDto",productDto);
	    return "products/createProduct";
	}
	
	@PostMapping("/create")
	public String saveProduct(
	    @Valid @ModelAttribute("productDto") ProductDto productDto,
	    BindingResult result,
	    Model model
	) {
	    if (productDto.getImageFile() == null || productDto.getImageFile().isEmpty()) {
	        result.addError(new FieldError("productDto", "imageFile", "The image file is required"));
	    }

	    if (result.hasErrors()) {
	        return "products/createProduct"; // Return to form page with errors
	    }
	    
	    
	    //save image file
	    
	    MultipartFile image = productDto.getImageFile();

	    if (image == null || image.isEmpty()) {
	        // Handle the case where the image is not uploaded
	        System.out.println("No image uploaded.");
	        return "redirect:/createProduct";
	    }

	    Date createdAt = new Date(0);
	    String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

	    try {
	        String uploadDir = "public/images/";
	        Path uploadPath = Paths.get(uploadDir);

	        if (!Files.exists(uploadPath)) {
	            Files.createDirectories(uploadPath);
	        }

	        try (InputStream inputStream = image.getInputStream()) {
	            Files.copy(inputStream, uploadPath.resolve(storageFileName), StandardCopyOption.REPLACE_EXISTING);
	        }

	    } catch (IOException ex) {
	        System.out.println("Exception: " + ex.getMessage());
	        // Optionally return an error view
	        return "redirect:/createProduct?error=true";
	    }
	    
	    Product product = new Product();
	    product.setName(productDto.getName());
	    product.setBrand(productDto.getBrand());
	    product.setCategory(productDto.getCategory());
	    product.setPrice(productDto.getPrice());
	    product.setDiscription(productDto.getDescription());
	    product.setCreatedAt(createdAt);
	    product.setImageFileName(storageFileName);
	   
	    repo.save(product);

	    return "redirect:/products";
	}
	
	@GetMapping("/edit")
	public String showEditPage(Model model,@RequestParam int id) {
		
		try {
			
			Product product = repo.findById(id).get();
			model.addAttribute("product", product);

			ProductDto productDto = new ProductDto();
			productDto.setName(product.getName());
			productDto.setBrand(product.getBrand());
			productDto.setCategory(product.getCategory());
			productDto.setPrice(product.getPrice());
			productDto.setDescription(product.getDiscription());
			

			model.addAttribute("productDto", productDto);

			
			
		} catch (Exception ex) {
			// TODO: handle exception
			System.out.println("Exception :" + ex.getMessage());
		}
		
		return "products/EditProduct";
		
	}
	
	  @PostMapping("/edit")
	  public String updateProduct(Model model,@RequestParam int id,@Valid @ModelAttribute ProductDto productDto,BindingResult result) 
	  {
		  try {
			
			  Product product = repo.findById(id).get();
			  model.addAttribute("product",product);
			  
			  if (result.hasErrors()) {
				
				  return "products:/EditProduct";
			}
			  
			  //here image file save code
			  
			  if (productDto.getImageFile().isEmpty()) {
				  //delete old image
				  String uploadDir ="public/images/";
				  Path oldImagePath = Paths.get(uploadDir +product.getImageFileName());
				  
				  try {
					
					  Files.delete(oldImagePath);
				} catch (Exception ex) {
					// TODO: handle exception
					System.out.println("Exception:" + ex.getMessage());
				}
				  //save new image files
				  
				  MultipartFile image =productDto.getImageFile();
				  Date createdAt = new Date(0);
				    String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();
				    
				    try (InputStream inputStream = image.getInputStream()) {
			            Files.copy(inputStream, Paths.get(uploadDir +storageFileName), StandardCopyOption.REPLACE_EXISTING);
			        }
				    
				    
				    product.setImageFileName(storageFileName);
			  }  
			   
			  
			  product.setName(productDto.getName());
			  product.setBrand(productDto.getBrand());
			  product.setCategory(productDto.getCategory());
			  product.setPrice(productDto.getPrice());
			  product.setDiscription(productDto.getDescription());
			  
			  repo.save(product);
		  }
			  
				    catch (Exception ex) {
						// TODO: handle exception
				    	System.out.println("Exception"+ex.getMessage());
					}
				  
				
			  return "redirect:/products";
	  }
	  @GetMapping("/delete")
	  public String deleteProduct(@RequestParam int id) {
		  try {
			  
			  Product product =repo.findById(id).get();
			  
			  //delete product image
			  
			  Path imagPath = Paths.get("public/images" +product.getImageFileName());
			  
			  try {
				  Files.delete(imagPath);
				
			
		} catch (Exception ex) {
			// TODO: handle exception
			
			System.out.println("Exception" + ex.getMessage());
		}
			  
			  //delete the product
			  repo.delete(product);
		    }
		  
		  catch (Exception ex) {
			// TODO: handle exception
			  
			  System.out.println("Exception" + ex.getMessage());
		}
		  return "redirect:/products";
		  
	  }
}
	
	

