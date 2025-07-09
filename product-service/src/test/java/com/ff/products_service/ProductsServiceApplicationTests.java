package com.ff.products_service;

import com.ff.products_service.controller.ProductAdminController;
import com.ff.products_service.controller.ProductController;
import com.ff.products_service.dto.ProductWithImagesRequest;
import com.ff.products_service.dto.UpdateProductWithImagesRequest;
import com.ff.products_service.entity.Image;
import com.ff.products_service.entity.Product;
import com.ff.products_service.security.JwtService; // Importez JwtService
import com.ff.products_service.service.ImageService;
import com.ff.products_service.service.ProductService;
import com.ff.products_service.utils.ApiRes;
import com.ff.products_service.utils.ImageValidationUtils;
import com.ff.products_service.utils.ProductMapper; // Inutile pour les tests si non utilisé dans le controller pour le retour
import com.ff.products_service.utils.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections; // Pour List.of() sur des collections vides ou uniques
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;

@SpringBootTest
class ProductsServiceApplicationTests {

	@InjectMocks
	private ProductController productController; // Assurez-vous que c'est bien votre contrôleur public
	@InjectMocks
	private ProductAdminController productAdminController;

	@Mock
	private ProductService productService;
	@Mock
	private ImageService imageService;
	@Mock
	private ProductMapper productMapper; // Peut être supprimé si le contrôleur Admin ne l'utilise pas pour les retours
	@Mock
	private JwtService jwtService; // Mock du JwtService

	private Product product;
	private Image img1;
	private Image img2;
	private HttpServletRequest mockHttpServletRequest; // Variable pour le mock de la requête

	private final Long TEST_ADMIN_ID = 100L; // ID admin fictif pour les tests

	@BeforeEach
	void setUp() {
		// Given
		img1 = Image.builder()
				.id(1L)
				.url("http://img1.jpg")
				.title("image1")
				.main(true)
				.product(product) // Assurez-vous que l'image a un produit associé si nécessaire
				.build();
		img2 = Image.builder()
				.id(2L)
				.url("http://img2.jpg")
				.title("image2")
				.main(false)
				.product(product)
				.build();
		product = Product.builder()
				.id(1L)
				.name("Product 1")
				.description("Product 1")
				.stock(5)
				.adminId(TEST_ADMIN_ID) // Important pour les tests admin
				.images(List.of(img1, img2))
				.build();

		// Initialisation du mock HttpServletRequest
		mockHttpServletRequest = mock(HttpServletRequest.class);
		when(mockHttpServletRequest.getHeader("Authorization")).thenReturn("Bearer test_token");
		when(jwtService.extractUserId("test_token")).thenReturn(TEST_ADMIN_ID); // Mock l'extraction de l'ID admin
	}

	@Test
	void contextLoads() {
	}

	// --- Tests pour ProductController (GET methods) ---
	// Note: Ces tests ne nécessitent pas de HttpServletRequest ni de JwtService
	// car votre contrôleur public ne semble pas les utiliser.

	@Test
	void getProductById_shouldReturnProduct() {
		when(productService.findById(1L)).thenReturn(product);

		ResponseEntity<ApiRes<Product>> response = productController.getProductById(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
		assertEquals(product.getId(), response.getBody().getData().getId());
		verify(productService, times(1)).findById(1L);
	}

	@Test
	void getAllProducts_shouldReturnListOfProducts() {
		when(productService.findAll()).thenReturn(List.of(product));
		ResponseEntity<ApiRes<List<Product>>> response = productController.getAllProducts();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		 assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
		assertEquals(1, response.getBody().getData().size());
		verify(productService, times(1)).findAll();
	}

	@Test
	void getProductStock_shouldReturnStock() {
		// Ce test est un doublon du premier getProductById_shouldReturnProduct pour la vérification du stock
		// Je le laisse mais il est redondant avec le premier test d'obtention de produit
		when(productService.findById(1L)).thenReturn(product);
		ResponseEntity<ApiRes<Product>> response = productController.getProductById(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		 assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
		assertEquals(5, response.getBody().getData().getStock());
	}

	// Ce test ne renvoie pas une liste vide mais lance une ResourceNotFoundException.
	// Le nom du test est trompeur. Je le renomme et le corrige.
	@Test
	void getProductById_shouldThrowResourceNotFoundWhenProductNotFound() {
		when(productService.findById(anyLong())).thenReturn(null); // Simulate product not found

		// Assurez-vous que votre ProductController gère bien le cas null par une exception
		// ou un ResponseEntity.notFound().
		// D'après votre code, il semble que productService.findById retourne null et
		// le contrôleur doit gérer cela.
		// Si ProductController ne jette pas d'exception mais retourne un notFound,
		// ce test doit être adapté.
		// J'assume ici qu'il jette une ResourceNotFoundException comme ProductAdminController.
		assertThrows(ResourceNotFoundException.class, () ->
				productController.getProductById(99L)); // Use an ID that won't be found

		verify(productService, times(1)).findById(99L);
	}


	// --- Tests pour ProductAdminController (méthodes ADMIN) ---

	@Test
	void createProductWithImageUrls_shouldCreateProductSuccessfully() {
		ProductWithImagesRequest.ImageRequest imageRequest = new ProductWithImagesRequest.ImageRequest();
		imageRequest.setUrl("http://new-image.jpg");
		imageRequest.setTitle("New Product Image");
		imageRequest.setMain(true);

		ProductWithImagesRequest request = new ProductWithImagesRequest();
		request.setName("New Product");
		request.setDescription("Description for new product");
		request.setPrice(BigDecimal.valueOf(150.00));
		request.setStock(10);
		request.setImages(List.of(imageRequest));

		// Mocking static method ImageValidationUtils.validateSingleMainImage
		try (MockedStatic<ImageValidationUtils> mockedStatic = mockStatic(ImageValidationUtils.class)) {
			mockedStatic.when(() -> ImageValidationUtils.validateSingleMainImage(anyList())).thenAnswer(invocation -> null); // Simule un appel sans exception

			// Mock productService.create pour retourner le produit avec l'ID
			// Simuler l'attribution d'un ID par la DB
			Product savedProduct = Product.builder()
					.id(2L)
					.adminId(TEST_ADMIN_ID)
					.name(request.getName())
					.description(request.getDescription())
					.price(request.getPrice())
					.stock(request.getStock())
					.createdAt(LocalDateTime.now())
					.build();
			when(productService.create(any(Product.class))).thenReturn(savedProduct);

			// Mock imageService.createImage (void method)
			doNothing().when(imageService).createImage(any(Image.class));
			when(imageService.getImagesByProductId(anyLong())).thenReturn(Collections.singletonList(img1)); // Return a list of images

			// Exécution
			ResponseEntity<ApiRes<Product>> response = productAdminController.createProductWithImageUrls(request, mockHttpServletRequest);

			// Vérifications
			assertEquals(HttpStatus.CREATED, response.getStatusCode());
			assertNotNull(response.getBody());
			 assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
			assertEquals("Product has been successfully saved", response.getBody().getMessage());
			assertNotNull(response.getBody().getData());
			assertEquals(2L, response.getBody().getData().getId()); // Check the ID from the mocked savedProduct
			assertEquals(TEST_ADMIN_ID, response.getBody().getData().getAdminId()); // Vérifie l'adminId

			verify(productService, times(1)).create(any(Product.class));
			verify(imageService, times(1)).createImage(any(Image.class));
			verify(imageService, times(1)).getImagesByProductId(2L); // Vérifie l'appel avec le bon ID
			mockedStatic.verify(() -> ImageValidationUtils.validateSingleMainImage(anyList()), times(1));
		}
	}


	@Test
	void deleteProduct_shouldRemoveImagesAndProduct() {
		// Mock l'adminId du produit pour correspondre à celui du token
		product.setAdminId(TEST_ADMIN_ID); // S'assurer que le produit mocké a le bon adminId
		when(productService.findById(1L)).thenReturn(product);
		when(imageService.getImagesByProductId(1L)).thenReturn(List.of(img1, img2));

		doNothing().when(imageService).deleteImageAllByProductId(1L);
		doNothing().when(productService).delete(1L);

		// Appel de la méthode
		ResponseEntity<ApiRes<String>> response = productAdminController.deleteProduct(1L); // Pas besoin de request ici

		// Vérifications
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		 assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
		assertEquals("Product has been successfully deleted", response.getBody().getMessage());
		assertNull(response.getBody().getData()); // Pour le cas où `data` est null dans la réponse de succès

		verify(productService, times(1)).findById(1L);
		verify(imageService, times(1)).getImagesByProductId(1L);
		verify(imageService, times(1)).deleteImageAllByProductId(1L);
		verify(productService, times(1)).delete(product.getId());
	}

	@Test
	void deleteProduct_shouldThrowResourceNotFoundWhenProductNotFound() {
		when(productService.findById(anyLong())).thenReturn(null);

		assertThrows(ResourceNotFoundException.class, () ->
				productAdminController.deleteProduct(99L));

		verify(productService, times(1)).findById(99L);
		verify(imageService, never()).getImagesByProductId(anyLong()); // S'assurer que ces méthodes ne sont pas appelées
		verify(imageService, never()).deleteImageAllByProductId(anyLong());
		verify(productService, never()).delete(anyLong());
	}

	@Test
	void deleteProduct_shouldThrowResourceNotFoundWhenNoImagesFoundForProduct() {
		// Scenario where product is found but has no images or imageService.getImagesByProductId returns empty
		when(productService.findById(1L)).thenReturn(product);
		when(imageService.getImagesByProductId(1L)).thenReturn(Collections.emptyList()); // No images found

		// L'exception ResourceNotFoundException est levée si images est vide ou null
		Exception exception = assertThrows(ResourceNotFoundException.class, () ->
				productAdminController.deleteProduct(1L));

		assertEquals("Image not found with id 1", exception.getMessage()); // Match the exact message

		verify(productService, times(1)).findById(1L);
		verify(imageService, times(1)).getImagesByProductId(1L);
		verify(imageService, never()).deleteImageAllByProductId(anyLong());
		verify(productService, never()).delete(anyLong());
	}

	@Test
	void updateProduct_shouldUpdateData() {
		// Préparation des données de la requête
		UpdateProductWithImagesRequest.ImageRequest imageRequest = new UpdateProductWithImagesRequest.ImageRequest();
		imageRequest.setId(1L); // ID d'une image existante
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
		// Simulez que le produit est trouvé et a l'adminId correspondant au token
		when(productService.findById(1L)).thenReturn(product);
		when(imageService.findImageById(1L)).thenReturn(img1); // L'image existante à mettre à jour
		when(productService.create(any(Product.class))).thenReturn(product); // Sauvegarde le produit mis à jour
		doNothing().when(imageService).createImage(any(Image.class)); // Pour les images nouvelles ou mises à jour

		// Mocking static methods for ImageValidationUtils
		try (MockedStatic<ImageValidationUtils> mockedStatic = mockStatic(ImageValidationUtils.class)) {
			mockedStatic.when(() -> ImageValidationUtils.validateSingleMainImage(anyList())).thenAnswer(invocation -> null);
			mockedStatic.when(() -> ImageValidationUtils.validateNoMainImageBeingDeleted(anyList())).thenAnswer(invocation -> null);

			// Exécution
			ResponseEntity<ApiRes<Product>> response = productAdminController.updateProduct(1L, request, mockHttpServletRequest);

			// Vérifications
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			 assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
			assertEquals("Product has been successfully modified", response.getBody().getMessage());
			assertNotNull(response.getBody().getData());

			verify(productService, times(2)).findById(1L); // Une fois au début, une fois à la fin
			verify(productService, times(1)).create(any(Product.class)); // Pour la mise à jour du produit
			verify(imageService, times(1)).findImageById(1L); // Pour trouver l'image existante
			verify(imageService, times(1)).createImage(any(Image.class)); // Pour la mise à jour de l'image (createImage est réutilisé pour save/update)

			// Vérification des appels statiques
			mockedStatic.verify(() -> ImageValidationUtils.validateSingleMainImage(anyList()), times(1));
			mockedStatic.verify(() -> ImageValidationUtils.validateNoMainImageBeingDeleted(anyList()), times(1));
		}
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
					productAdminController.createProductWithImageUrls(request, mockHttpServletRequest)); // Utilisation du mockHttpServletRequest

			assertEquals("Exactly one main image is required", ex.getMessage());
			mockedStatic.verify(() -> ImageValidationUtils.validateSingleMainImage(request.getImages()), times(1));
		}
	}


	@Test
	void updateProduct_shouldThrowIfNoMainImage() {
		UpdateProductWithImagesRequest.ImageRequest imgReq = new UpdateProductWithImagesRequest.ImageRequest();
		imgReq.setId(1L);
		imgReq.setUrl("http://image.jpg");
		imgReq.setTitle("img");
		imgReq.setMain(false); // <-- Pas d'image principale ici
		imgReq.setToDelete(false);

		UpdateProductWithImagesRequest request = new UpdateProductWithImagesRequest();
		request.setImages(List.of(imgReq));
		request.setName("Product");
		request.setPrice(BigDecimal.TEN);
		request.setStock(2);

		when(productService.findById(1L)).thenReturn(product);

		try (MockedStatic<ImageValidationUtils> mockedStatic = mockStatic(ImageValidationUtils.class)) {
			// Stuber validateSingleMainImage pour qu'elle lance une exception car il n'y a pas d'image principale
			mockedStatic.when(() -> ImageValidationUtils.validateSingleMainImage(anyList()))
					.thenThrow(new IllegalArgumentException("Au moins une image doit être marquée comme principale.")); // Ou le message d'erreur que votre méthode lance

			// PAS besoin de stuber validateNoMainImageBeingDeleted ici
			// car elle ne sera PAS appelée si validateSingleMainImage lance une exception.
			// mockedStatic.when(() -> ImageValidationUtils.validateNoMainImageBeingDeleted(anyList())).thenAnswer(invocation -> null); // <-- Supprimez cette ligne si elle était présente

			Exception ex = assertThrows(IllegalArgumentException.class,
					() -> productAdminController.updateProduct(1L, request, mockHttpServletRequest));

			assertEquals("Au moins une image doit être marquée comme principale.", ex.getMessage());

			// Vérifier que validateSingleMainImage a bien été appelée
			mockedStatic.verify(() -> ImageValidationUtils.validateSingleMainImage(anyList()), times(1));

			// Vérifier explicitement que validateNoMainImageBeingDeleted n'a PAS été appelée
			mockedStatic.verify(() -> ImageValidationUtils.validateNoMainImageBeingDeleted(anyList()), never());
		}
	}

	@Test
	void updateProduct_shouldUpdateProductAndImages() {
		UpdateProductWithImagesRequest request = new UpdateProductWithImagesRequest();
		request.setName("Updated Product");
		request.setDescription("Updated Desc");
		request.setPrice(BigDecimal.valueOf(200.00));
		request.setStock(3);

		UpdateProductWithImagesRequest.ImageRequest imageToAdd = new UpdateProductWithImagesRequest.ImageRequest();
		imageToAdd.setId(null); // nouvelle image
		imageToAdd.setUrl("http://example.com/new.jpg");
		imageToAdd.setTitle("New Image");
		imageToAdd.setMain(false);
		imageToAdd.setToDelete(false);

		UpdateProductWithImagesRequest.ImageRequest imageToUpdate = new UpdateProductWithImagesRequest.ImageRequest();
		imageToUpdate.setId(2L); // Utilisez l'ID de img2 pour simuler une mise à jour d'image existante
		imageToUpdate.setUrl("http://example.com/existing.jpg");
		imageToUpdate.setTitle("Updated title");
		imageToUpdate.setMain(true); // Celle-ci devient la principale
		imageToUpdate.setToDelete(false);

		request.setImages(List.of(imageToAdd, imageToUpdate));

		when(productService.findById(1L)).thenReturn(product); // Le produit existant
		when(imageService.findImageById(2L)).thenReturn(img2); // L'image existante à mettre à jour (img2)
		when(productService.create(any(Product.class))).thenReturn(product); // Retourne le produit après update
		doNothing().when(imageService).createImage(any(Image.class)); // Pour les créations/mises à jour d'images

		try (MockedStatic<ImageValidationUtils> mockedStatic = mockStatic(ImageValidationUtils.class)) {
			mockedStatic.when(() -> ImageValidationUtils.validateSingleMainImage(anyList())).thenAnswer(invocation -> null);
			mockedStatic.when(() -> ImageValidationUtils.validateNoMainImageBeingDeleted(anyList())).thenAnswer(invocation -> null);

			ResponseEntity<ApiRes<Product>> response = productAdminController.updateProduct(1L, request, mockHttpServletRequest);

			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals("Product has been successfully modified", response.getBody().getMessage());
			 assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
			assertNotNull(response.getBody().getData());

			verify(productService, times(2)).findById(1L); // Une fois au début, une fois à la fin
			verify(productService, times(1)).create(any(Product.class)); // Pour la mise à jour du produit
			verify(imageService, times(1)).findImageById(2L); // Pour trouver l'image à mettre à jour
			verify(imageService, times(2)).createImage(any(Image.class)); // Une fois pour la nouvelle image, une fois pour la mise à jour

			mockedStatic.verify(() -> ImageValidationUtils.validateSingleMainImage(anyList()), times(1));
			mockedStatic.verify(() -> ImageValidationUtils.validateNoMainImageBeingDeleted(anyList()), times(1));
		}
	}

	@Test
	void updateProduct_shouldThrowIfMultipleMainImages() {
		UpdateProductWithImagesRequest.ImageRequest img1Req = new UpdateProductWithImagesRequest.ImageRequest();
		img1Req.setId(1L); img1Req.setMain(true); img1Req.setToDelete(false);
		UpdateProductWithImagesRequest.ImageRequest img2Req = new UpdateProductWithImagesRequest.ImageRequest();
		img2Req.setId(2L); img2Req.setMain(true); img2Req.setToDelete(false);

		UpdateProductWithImagesRequest request = new UpdateProductWithImagesRequest();
		request.setImages(List.of(img1Req, img2Req));
		request.setName("Product");
		request.setPrice(BigDecimal.TEN);
		request.setStock(2);

		when(productService.findById(1L)).thenReturn(product);

		try (MockedStatic<ImageValidationUtils> mockedStatic = mockStatic(ImageValidationUtils.class)) {
			mockedStatic.when(() -> ImageValidationUtils.validateSingleMainImage(anyList()))
					.thenThrow(new IllegalArgumentException("Une seule image peut être marquée comme principale."));
			// Vous n'avez pas besoin de stuber validateNoMainImageBeingDeleted ici car elle ne sera pas appelée
			// mockedStatic.when(() -> ImageValidationUtils.validateNoMainImageBeingDeleted(anyList())).thenAnswer(invocation -> null); // <-- Supprimez cette ligne si elle était là pour le stubbing

			Exception ex = assertThrows(IllegalArgumentException.class,
					() -> productAdminController.updateProduct(1L, request, mockHttpServletRequest));

			assertEquals("Une seule image peut être marquée comme principale.", ex.getMessage());
			mockedStatic.verify(() -> ImageValidationUtils.validateSingleMainImage(anyList()), times(1));
			// NE VÉRIFIEZ PAS validateNoMainImageBeingDeleted ici car elle ne devrait pas être appelée.
			// Si vous voulez être explicite qu'elle NE DOIT PAS être appelée :
			mockedStatic.verify(() -> ImageValidationUtils.validateNoMainImageBeingDeleted(anyList()), never());
		}
	}

	@Test
	void updateProduct_shouldThrowIfDeletingMainImage() {
		UpdateProductWithImagesRequest request = buildRequestWithDeletingMainImage();

		when(productService.findById(1L)).thenReturn(product);

		try (MockedStatic<ImageValidationUtils> mockedStatic = mockStatic(ImageValidationUtils.class)) {
			// Ici, validateSingleMainImage devrait passer si une autre est principale
			mockedStatic.when(() -> ImageValidationUtils.validateSingleMainImage(anyList())).thenAnswer(invocation -> null);
			mockedStatic.when(() -> ImageValidationUtils.validateNoMainImageBeingDeleted(anyList()))
					.thenThrow(new IllegalArgumentException("Une image principale ne peut pas être supprimée directement. Veuillez d’abord en définir une autre comme principale."));

			Exception ex = assertThrows(IllegalArgumentException.class,
					() -> productAdminController.updateProduct(1L, request, mockHttpServletRequest));

			assertEquals("Une image principale ne peut pas être supprimée directement. Veuillez d’abord en définir une autre comme principale.", ex.getMessage());
			mockedStatic.verify(() -> ImageValidationUtils.validateSingleMainImage(anyList()), times(1));
			mockedStatic.verify(() -> ImageValidationUtils.validateNoMainImageBeingDeleted(anyList()), times(1));
		}
	}

	private UpdateProductWithImagesRequest buildRequestWithDeletingMainImage() {
		// Simule un scénario où l'image principale (id=1) est marquée pour suppression,
		// et il n'y a pas d'autre image principale non marquée pour suppression.
		UpdateProductWithImagesRequest.ImageRequest imgToDelete = new UpdateProductWithImagesRequest.ImageRequest();
		imgToDelete.setId(1L); // Assume que c'est l'image principale initiale
		imgToDelete.setMain(true);
		imgToDelete.setToDelete(true);

		// Une autre image qui n'est pas principale (ou qui devient principale, mais ici, c'est pour le test de suppression)
		UpdateProductWithImagesRequest.ImageRequest imgOther = new UpdateProductWithImagesRequest.ImageRequest();
		imgOther.setId(2L);
		imgOther.setMain(false); // Pas la principale
		imgOther.setToDelete(false);

		UpdateProductWithImagesRequest request = new UpdateProductWithImagesRequest();
		request.setImages(List.of(imgToDelete, imgOther)); // imgToDelete est la seule "main" et elle est supprimée
		request.setName("Product");
		request.setPrice(BigDecimal.TEN);
		request.setStock(2);

		return request;
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
		when(imageService.findImageById(1L)).thenReturn(img1); // L'image existante à mettre à jour
		when(productService.create(any())).thenReturn(product); // Le produit mis à jour

		try (MockedStatic<ImageValidationUtils> mockedStatic = mockStatic(ImageValidationUtils.class)) {
			mockedStatic.when(() -> ImageValidationUtils.validateSingleMainImage(anyList())).thenAnswer(invocation -> null);
			mockedStatic.when(() -> ImageValidationUtils.validateNoMainImageBeingDeleted(anyList())).thenAnswer(invocation -> null);

			ResponseEntity<ApiRes<Product>> response = productAdminController.updateProduct(1L, request, mockHttpServletRequest); // Utilisation du mockHttpServletRequest

			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			 assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
			verify(productService, times(2)).findById(1L); // findById au début et à la fin
			verify(productService).create(any(Product.class));
			verify(imageService).findImageById(1L);
			verify(imageService).createImage(any(Image.class)); // Pour l'update de l'image
			mockedStatic.verify(() -> ImageValidationUtils.validateSingleMainImage(anyList()), times(1));
			mockedStatic.verify(() -> ImageValidationUtils.validateNoMainImageBeingDeleted(anyList()), times(1));
		}
	}
}