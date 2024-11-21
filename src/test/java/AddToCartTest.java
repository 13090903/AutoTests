import com.google.common.primitives.Ints;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.By;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AddToCartTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver-win64\\chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.of(25, SECONDS));
    }

    @Test
    public void testAddOneProductToCartSuccess() {
        // given
        driver.get("https://4lapy.ru/product/royal-canin-sterilised-vlazhnyj-korm-dlya-sterilizovannyh-koshek-v-souse-85-g-626-1011129/");
        String expectedProductName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("product_title"))).getText();
        Double expectedProductPrice = getRoundedPrice(wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("ProductPrice_price__nh9mE"))).getText());
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("product_add_to_cart_button")));

        // when
        addToCartButton.click();
        WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(By.id("nav_menu_cart")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("NavIcon_tagWrapper__jsM4T"))); // проверяем, что у корзины появляется значок товара (чтобы не перейти раньше времени)
        cartIcon.click();

        // then
        assertTrue(verifyCartItems(driver, new HashSet<>(Collections.singletonList(expectedProductName)), expectedProductPrice));
    }

    @Test
    public void testAddBatchOfProductsToCartSuccess() {
        // given
        driver.get("https://4lapy.ru/product/royal-canin-sterilised-vlazhnyj-korm-dlya-sterilizovannyh-koshek-v-souse-85-g-626-1011129/");
        String expectedProductName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("product_title"))).getText();
        Integer expectedProductAmount = extractAmount(wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='product_packing_PAK']//div[contains(@class, 'ProductPacking_packSizeAmount__mtDbb')]"))).getText()); // там хранится количество товара
        System.out.println(expectedProductAmount);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("product_packing_PAK"))).click(); // это кнопка для выбора пачки товаров
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("product_add_to_cart_button")));
        // when
        addToCartButton.click();
        WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(By.id("nav_menu_cart")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("NavIcon_tagWrapper__jsM4T")));
        cartIcon.click();

        // then
        WebElement cartItem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("id_product_card_0")));
        assertTrue(cartItem.isDisplayed());
        assertEquals(expectedProductName, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cart_product_title_0"))).getText());
        assertEquals(expectedProductAmount, extractAmount(wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("Title_root__J7hHl"))).getText())); // здесь пишется количество
    }

    @Test
    public void testAddOneProductToCartFromCatalogSuccess() {
        // given
        driver.get("https://4lapy.ru/catalog/koshki/korm-koshki/dieticheskiy/");
        String expectedProductName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//article[@data-id='1011129']//div[@class='CardProduct_productNameInner__Jc_on']"))).getText();
        Double expectedProductPrice = getRoundedPrice(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("id_card_price_1011129"))).getText());
        WebElement addToCartButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@data-offerid='1011129']//button[contains(@class, 'ProductCardBtn_btn__3Jxlo') and @aria-label='В корзину']"))); // интересная ситуация, элемент меняется в процессе и просит новое состояние
        closeCookies();

        // when
        addToCartButton.click();
        WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(By.id("nav_menu_cart")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("NavIcon_tagWrapper__jsM4T"))); // проверяем, что у корзины появляется значок товара (чтобы не перейти раньше времени)
        cartIcon.click();

        // then
        assertTrue(verifyCartItems(driver, new HashSet<>(Collections.singletonList(expectedProductName)), expectedProductPrice));
    }

    @Test
    public void testAddMultipleProductToCartFromCatalogSuccess() {
        // given
        driver.get("https://4lapy.ru/catalog/koshki/korm-koshki/dieticheskiy/");
        String expectedProductName1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//article[@data-id='1011129']//div[@class='CardProduct_productNameInner__Jc_on']"))).getText();
        String expectedProductName2 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//article[@data-id='1003547']//div[@class='CardProduct_productNameInner__Jc_on']"))).getText();
        Double expectedProductPrice1 = getRoundedPrice(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("id_card_price_1011129"))).getText());
        Double expectedProductPrice2 = getRoundedPrice(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("id_card_price_1003547"))).getText());
        WebElement addToCartButton1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@data-offerid='1011129']//button[contains(@class, 'ProductCardBtn_btn__3Jxlo') and @aria-label='В корзину']"))); // интересная ситуация, элемент меняется в процессе и просит новое состояние
        WebElement addToCartButton2 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@data-offerid='1003547']//button[contains(@class, 'ProductCardBtn_btn__3Jxlo') and @aria-label='В корзину']"))); // интересная ситуация, элемент меняется в процессе и просит новое состояние
        closeCookies();

        // when
        addToCartButton1.click();
        addToCartButton2.click();
        WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(By.id("nav_menu_cart")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("NavIcon_tagWrapper__jsM4T"))); // проверяем, что у корзины появляется значок товара (чтобы не перейти раньше времени)
        cartIcon.click();

        // then
        assertTrue(verifyCartItems(driver, new HashSet<>(Arrays.asList(expectedProductName1, expectedProductName2)), expectedProductPrice1 + expectedProductPrice2));
    }

    @Test
    public void testAddOneProductToCartFromCatalogWithFiltersSuccess() {
        // given
        driver.get("https://4lapy.ru/catalog/koshki/korm-koshki/dieticheskiy/filter/?brand=hills+purina&price=FilterFrom_99-FilterTo_234");
        String expectedProductName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//article[@data-id='1026745']//div[@class='CardProduct_productNameInner__Jc_on']"))).getText();
        Double expectedProductPrice = getRoundedPrice(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("id_card_price_1026745"))).getText());
        WebElement addToCartButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@data-offerid='1026745']//button[contains(@class, 'ProductCardBtn_btn__3Jxlo') and @aria-label='В корзину']"))); // интересная ситуация, элемент меняется в процессе и просит новое состояние
        closeCookies();

        // when
        addToCartButton.click();
        WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(By.id("nav_menu_cart")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("NavIcon_tagWrapper__jsM4T"))); // проверяем, что у корзины появляется значок товара (чтобы не перейти раньше времени)
        cartIcon.click();

        // then
        assertTrue(verifyCartItems(driver, new HashSet<>(Collections.singletonList(expectedProductName)), expectedProductPrice));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2}) // было и больше, но у них есть рандомные скидки на некоторые количества...
    public void testAddExtraProductToCartFromCatalogSuccess(int extraAmount) throws InterruptedException {
        // given
        driver.get("https://4lapy.ru/catalog/koshki/korm-koshki/dieticheskiy/filter/?brand=hills+purina&price=FilterFrom_99-FilterTo_234");
        String expectedProductName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//article[@data-id='1026745']//div[@class='CardProduct_productNameInner__Jc_on']"))).getText();
        Double expectedProductPrice = getRoundedPrice(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("id_card_price_1026745"))).getText())*(extraAmount + 1);
        WebElement addToCartButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@data-offerid='1026745']//button[contains(@class, 'ProductCardBtn_btn__3Jxlo') and @aria-label='В корзину']"))); // интересная ситуация, элемент меняется в процессе и просит новое состояние
        closeCookies();

        // when
        addToCartButton.click();
        WebElement addExtraProductButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@data-offerid='1026745']//button[contains(@class, 'Counter_controlButton__sTA8n') and @data-counter-action='plus']")));
        for (int i = 0; i < extraAmount; i++) {
            addExtraProductButton.click();
            Thread.sleep(3000);
        }
        Thread.sleep(3000);
        WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(By.id("nav_menu_cart")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("NavIcon_tagWrapper__jsM4T"))); // проверяем, что у корзины появляется значок товара (чтобы не перейти раньше времени)
        cartIcon.click();

        // then
        assertTrue(verifyCartItems(driver, new HashSet<>(Collections.singletonList(expectedProductName)), expectedProductPrice));
        assertEquals(extraAmount + 1, extractAmount(wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("Title_root__J7hHl"))).getText())); // здесь пишется количество
    }

    private Double getPrice(String textPrice) {
        String cleanedPrice = textPrice.replace(" ", "");
        return Double.valueOf(cleanedPrice.substring(0, cleanedPrice.length() - 1));
    }

    private Double getRoundedPrice(String textPrice) {
        return (double) Math.round(getPrice(textPrice));
    }

    private Integer extractAmount(String text) {
        return Integer.valueOf(text.split(" ")[0]);
    }

    private void closeCookies() {
        try {
            WebElement notificationContainer = driver.findElement(By.className("CookiesNotification_root__rmqa4"));
            WebElement closeButton = notificationContainer.findElement(By.cssSelector("button"));
            closeButton.click();
        } catch (NoSuchElementException ignored) {

        }
    }

    private boolean verifyCartItems(WebDriver driver, Set<String> expectedItems, Double expectedPrice) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[id^='cart_product_title_']")));
        List<WebElement> productTitles = driver.findElements(By.cssSelector("[id^='cart_product_title_']"));
        HashSet<String> actualItems = new HashSet<>();
        for (WebElement element : productTitles) {
            actualItems.add(element.getText().trim());
        }
        Double actualPrice = getRoundedPrice(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cart_total_products_price"))).getText());
        return (actualItems.equals(expectedItems) && actualPrice.equals(expectedPrice));
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}