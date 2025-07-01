package com.ff.products_service;

import com.ff.products_service.controller.ProductController;
import com.ff.products_service.dto.ProductWithImagesRequest;
import com.ff.products_service.dto.UpdateProductWithImagesRequest;
import com.ff.products_service.entity.Image;
import com.ff.products_service.entity.Product;
import com.ff.products_service.service.ImageService;
import com.ff.products_service.service.ProductService;
import com.ff.products_service.utils.ApiRes;
import com.ff.products_service.utils.ImageValidationUtils;
import com.ff.products_service.utils.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;
@SpringBootTest
class ProductsServiceApplicationTests {

	@InjectMocks
	private ProductController productController;

	@Mock
	private ProductService productService;
	@Mock
	private ImageService imageService;
	@Mock
	private ProductMapper productMapper;
	private Product product;
	private Image img1;
	private Image img2;

	@BeforeEach
	void setUp() {
		// Given
		 img1 = Image.builder()
				.id(1L)
				.url("http://img1.jpg")
				.title("image1")
				.main(true)
				.build();
		 img2 = Image.builder()
				.id(2L)
				.url("http://img2.jpg")
				.title("image2")
				.main(false)
				.build();
		product = Product.builder()
				.id(1L).name("Product 1")
				.description("Product 1")
				.stock(5)
				.images(List.of(img1, img2))
				.build();
	}
	@Test
	void contextLoads() {
	}

	@Test
	void getProductById_shouldReturnProduct() {
		when(productService.findById(1L)).thenReturn(product);

		ResponseEntity<ApiRes<Product>> response = productController.getProductById(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(product.getId(), response.getBody().getData().getId());
		verify(productService, times(1)).findById(1L);
	}

	@Test
	void getAllProducts_shouldReturnListOfProducts() {
		when(productService.findAll()).thenReturn(List.of(product));
		ResponseEntity<ApiRes<List<Product>>> response = productController.getAllProducts();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(1, response.getBody().getData().size());
		verify(productService, times(1)).findAll();

	}

	@Test
	void getProductStock_shouldReturnStock() {
		when(productService.findById(1L)).thenReturn(product);
		ResponseEntity<ApiRes<Product>> response = productController.getProductById(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(5, response.getBody().getData().getStock());
	}

	@Test
	void deleteProduct_shouldRemoveImagesAndProduct(){
		when(productService.findById(1L)).thenReturn(product);
		when(imageService.getImagesByProductId(1L)).thenReturn(List.of(img1, img2));

		var response = productController.deleteProduct(1L);
		verify(imageService).deleteImageAllByProductId(1L);
		verify(productService).delete(product.getId());
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	void getProductStock_shouldReturnEmptyListWhenProductNotFound() {
		when(productService.findById(1L)).thenReturn(product);
		ResponseEntity<ApiRes<Product>> response = productController.getProductById(1L);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(product.getId(), response.getBody().getData().getId());
		verify(productService, times(1)).findById(1L);

	}

	@Test
	void updateProduct_shouldUpdateData() {
		// Préparation des données de la requête
		UpdateProductWithImagesRequest.ImageRequest imageRequest = new UpdateProductWithImagesRequest.ImageRequest();
		imageRequest.setId(1L);
		imageRequest.setUrl("http://img-updated.jpg");
		imageRequest.setTitle("Updated image");
		imageRequest.setMain(true);
		imageRequest.setToDelete(false);

		UpdateProductWithImagesRequest request = new UpdateProductWithImagesRequest();
		request.setName("Updated name");
		request.setDescription("Updated desc");
		request.setPrice(BigDecimal.valueOf(49.99));
		request.setStock(9);
		request.setImages(List.of(imageRequest));

		// Mock comportement
		when(productService.findById(1L)).thenReturn(product);
		when(imageService.findImageById(1L)).thenReturn(img1);
		when(productService.create(any())).thenReturn(product);

		// Exécution
		var response = productController.updateProduct(1L, request);

		// Vérifications
		assertEquals(HttpStatus.OK, response.getStatusCode());
		verify(productService).create(any(Product.class));
		verify(imageService).createImage(any(Image.class));
	}

	@Test
	void createProductWithImageUrls_shouldThrow_whenNoMainImage() {
		ProductWithImagesRequest request = new ProductWithImagesRequest();
		request.setName("Test");
		request.setDescription("Desc");
		request.setPrice(BigDecimal.valueOf(100));
		request.setStock(2);

		ProductWithImagesRequest.ImageRequest img = new ProductWithImagesRequest.ImageRequest();
		img.setUrl("http://img.jpg");
		img.setTitle("No main");
		img.setMain(false);
		request.setImages(List.of(img));

		try (MockedStatic<ImageValidationUtils> mockedStatic = mockStatic(ImageValidationUtils.class)) {
			mockedStatic
					.when(() -> ImageValidationUtils.validateSingleMainImage(request.getImages()))
					.thenThrow(new IllegalArgumentException("Exactly one main image is required"));

			Exception ex = assertThrows(IllegalArgumentException.class, () ->
					productController.createProductWithImageUrls(request));

			assertEquals("Exactly one main image is required", ex.getMessage());
		}
	}


	@Test
	void updateProduct_shouldThrowIfNoMainImage() {
		UpdateProductWithImagesRequest.ImageRequest img = new UpdateProductWithImagesRequest.ImageRequest();
		img.setId(1L);
		img.setMain(false);
		img.setToDelete(false);
		img.setUrl("http://image.jpg");
		img.setTitle("img");

		UpdateProductWithImagesRequest request = new UpdateProductWithImagesRequest();
		request.setImages(List.of(img));
		request.setName("Product");
		request.setPrice(BigDecimal.TEN);
		request.setStock(2);

		when(productService.findById(1L)).thenReturn(product);

		Exception ex = assertThrows(IllegalArgumentException.class,
				() -> productController.updateProduct(1L, request));

		assertEquals("Il doit y avoir une image principale.", ex.getMessage());
	}

	@Test
	void updateProduct_shouldUpdateProductAndImages() {
		UpdateProductWithImagesRequest request = new UpdateProductWithImagesRequest();
		request.setName("Updated Product");
		request.setDescription("Updated Desc");
		request.setPrice(BigDecimal.valueOf(200.00));
		request.setStock(3);

		UpdateProductWithImagesRequest.ImageRequest imageToAdd = new UpdateProductWithImagesRequest.ImageRequest();
		imageToAdd.setId(null); // new image
		imageToAdd.setUrl("http://example.com/new.jpg");
		imageToAdd.setTitle("New Image");
		imageToAdd.setMain(false);
		imageToAdd.setToDelete(false);

		UpdateProductWithImagesRequest.ImageRequest imageToUpdate = new UpdateProductWithImagesRequest.ImageRequest();
		imageToUpdate.setId(20L);
		imageToUpdate.setUrl("http://example.com/existing.jpg");
		imageToUpdate.setTitle("Updated title");
		imageToUpdate.setMain(true);
		imageToUpdate.setToDelete(false);

		request.setImages(List.of(imageToAdd, imageToUpdate));

		when(productService.findById(1L)).thenReturn(product);
		when(imageService.findImageById(20L)).thenReturn(new Image());
		when(productService.create(any(Product.class))).thenReturn(product);

		ResponseEntity<ApiRes<Product>> response = productController.updateProduct(1L, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Product has been successfully modified", response.getBody().getMessage());
		verify(productService).create(any(Product.class));
		verify(imageService, times(2)).createImage(any(Image.class));

	}

	@Test
	void updateProduct_shouldThrowIfMultipleMainImages() {
		UpdateProductWithImagesRequest.ImageRequest img1 = new UpdateProductWithImagesRequest.ImageRequest();
		img1.setMain(true);
		img1.setToDelete(false);
		UpdateProductWithImagesRequest.ImageRequest img2 = new UpdateProductWithImagesRequest.ImageRequest();
		img2.setMain(true);
		img2.setToDelete(false);

		UpdateProductWithImagesRequest request = new UpdateProductWithImagesRequest();
		request.setImages(List.of(img1, img2));
		request.setName("Product");
		request.setPrice(BigDecimal.TEN);
		request.setStock(2);

		when(productService.findById(1L)).thenReturn(product);

		Exception ex = assertThrows(IllegalArgumentException.class,
				() -> productController.updateProduct(1L, request));

		assertEquals("Une seule image peut être marquée comme principale.", ex.getMessage());
	}

	@Test
	void updateProduct_shouldThrowIfDeletingMainImage() {
		UpdateProductWithImagesRequest.ImageRequest imgToDelete = new UpdateProductWithImagesRequest.ImageRequest();
		imgToDelete.setMain(true);
		imgToDelete.setToDelete(true);

		UpdateProductWithImagesRequest.ImageRequest imgOther = new UpdateProductWithImagesRequest.ImageRequest();
		imgOther.setMain(true);
		imgOther.setToDelete(false);

		UpdateProductWithImagesRequest request = new UpdateProductWithImagesRequest();
		request.setImages(List.of(imgToDelete, imgOther));
		request.setName("Product");
		request.setPrice(BigDecimal.TEN);
		request.setStock(2);

		when(productService.findById(1L)).thenReturn(product);

		Exception ex = assertThrows(IllegalArgumentException.class,
				() -> productController.updateProduct(1L, request));

		assertEquals("Une image principale ne peut pas être supprimée directement. Veuillez d’abord en définir une autre comme principale.", ex.getMessage());
	}

	@Test
	void updateProduct_shouldPassWithValidMainImage() {
		UpdateProductWithImagesRequest.ImageRequest img = new UpdateProductWithImagesRequest.ImageRequest();
		img.setId(1L);
		img.setMain(true);
		img.setToDelete(false);
		img.setTitle("main");
		img.setUrl("http://img.jpg");

		UpdateProductWithImagesRequest request = new UpdateProductWithImagesRequest();
		request.setImages(List.of(img));
		request.setName("Product");
		request.setPrice(BigDecimal.TEN);
		request.setStock(2);

		when(productService.findById(1L)).thenReturn(product);
		when(imageService.findImageById(1L)).thenReturn(img1);
		when(productService.create(any())).thenReturn(product);

		ResponseEntity<ApiRes<Product>> response = productController.updateProduct(1L, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		verify(productService).create(any(Product.class));
	}

}
