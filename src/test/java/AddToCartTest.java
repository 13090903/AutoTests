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

    private static final String DRIVER_ADDRESS = "C:\\chromedriver-win64\\chromedriver.exe";
    private static final String DRIVER_NAME = "webdriver.chrome.driver";
    private static final String PRODUCT_URL = "https://4lapy.ru/product/royal-canin-sterilised-vlazhnyj-korm-dlya-sterilizovannyh-koshek-v-souse-85-g-626-1011129/";
    private static final String PRODUCT_TITLE_ID = "product_title";
    private static final String PRODUCT_PRICE_CLASS = "ProductPrice_price__nh9mE";
    private static final String ADD_TO_CART_BUTTON_ID = "product_add_to_cart_button";
    private static final String NAV_MENU_CART_BUTTON_ID = "nav_menu_cart";
    private static final String PRODUCT_COUNTER = "NavIcon_tagWrapper__jsM4T";
    private static final String PRODUCT_PACK_BUTTON_ID = "product_packing_PAK";
    private static final String PRODUCT_PACK_AMOUNT_XPATH = "//div[@id='product_packing_PAK']//div[contains(@class, 'ProductPacking_packSizeAmount__mtDbb')]";
    private static final String CART_FIRST_PRODUCT_ID = "id_product_card_0";
    private static final String CART_FIRST_PRODUCT_TITLE = "cart_product_title_0";
    private static final String CART_PRODUCT_AMOUNT = "Title_root__J7hHl";
    private static final String CATALOG_URL = "https://4lapy.ru/catalog/koshki/korm-koshki/dieticheskiy/";
    private static final String CATALOG_FILTERED_URL = "https://4lapy.ru/catalog/koshki/korm-koshki/dieticheskiy/filter/?brand=hills+purina&price=FilterFrom_99-FilterTo_234";
    private static final String CATALOG_PRODUCT_NAME_XPATH = "//article[@data-id='1011129']//div[@class='CardProduct_productNameInner__Jc_on']";
    private static final String CATALOG_PRODUCT_PRICE_ID = "id_card_price_1011129";
    private static final String CATALOG_SECOND_PRODUCT_PRICE_ID = "id_card_price_1003547";
    private static final String CATALOG_FILTERED_PRODUCT_PRICE_ID = "id_card_price_1026745";
    private static final String CATALOG_ADD_TO_CART_BUTTON_XPATH = "//div[@data-offerid='1011129']//button[contains(@class, 'ProductCardBtn_btn__3Jxlo') and @aria-label='В корзину']";
    private static final String CATALOG_FILTERED_ADD_TO_CART_BUTTON_XPATH = "//div[@data-offerid='1026745']//button[contains(@class, 'ProductCardBtn_btn__3Jxlo') and @aria-label='В корзину']";
    private static final String CATALOG_SECOND_ADD_TO_CART_BUTTON_XPATH = "//div[@data-offerid='1003547']//button[contains(@class, 'ProductCardBtn_btn__3Jxlo') and @aria-label='В корзину']";
    private static final String CATALOG_SECOND_PRODUCT_NAME_XPATH = "//article[@data-id='1003547']//div[@class='CardProduct_productNameInner__Jc_on']";
    private static final String CATALOG_FILTERED_PRODUCT_NAME_XPATH = "//article[@data-id='1026745']//div[@class='CardProduct_productNameInner__Jc_on']";
    private static final String ADD_EXTRA_PRODUCT_BUTTON_XPATH = "//div[@data-offerid='1026745']//button[contains(@class, 'Counter_controlButton__sTA8n') and @data-counter-action='plus']";
    private static final String ADD_EXTRA_PRODUCT_FROM_CART_BUTTON_XPATH = "//div[@data-offerid='1011129']//button[contains(@class, 'Counter_controlButton__sTA8n') and @data-counter-action='plus']";
    private static final String DELETE_PRODUCT_FROM_CART_BUTTON_XPATH = "//div[@data-offerid='1011129']//button[contains(@class, 'Counter_controlButton__sTA8n') and @data-counter-action='minus']";
    private static final String COOKIES_CONTAINER_CLASS = "CookiesNotification_root__rmqa4";
    private static final String BUTTON_SELECTOR = "button";
    private static final String PRODUCT_TITLE_ID_SELECTOR = "[id^='cart_product_title_']";
    private static final String CART_TOTAL_PRICE_ID = "cart_total_products_price";
    private static final String EMPTY_CART_CLASS = "EmptyCart_root__pVp3a";

    @BeforeEach
    public void setUp() {
        System.setProperty(DRIVER_NAME, DRIVER_ADDRESS);
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.of(25, SECONDS));
    }

    @Test
    public void testAddOneProductToCartSuccess() {
        // given
        driver.get(PRODUCT_URL);
        String expectedProductName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(PRODUCT_TITLE_ID))).getText();
        Double expectedProductPrice = getRoundedPrice(wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(PRODUCT_PRICE_CLASS))).getText());
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.id(ADD_TO_CART_BUTTON_ID)));

        // when
        addToCartButton.click();
        WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(By.id(NAV_MENU_CART_BUTTON_ID)));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(PRODUCT_COUNTER))); // проверяем, что у корзины появляется значок товара (чтобы не перейти раньше времени)
        cartIcon.click();

        // then
        assertTrue(verifyCartItems(driver, new HashSet<>(Collections.singletonList(expectedProductName)), expectedProductPrice));
    }

    @Test
    public void testAddBatchOfProductsToCartSuccess() {
        // given
        driver.get(PRODUCT_URL);
        String expectedProductName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(PRODUCT_TITLE_ID))).getText();
        Integer expectedProductAmount = extractAmount(wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(PRODUCT_PACK_AMOUNT_XPATH))).getText()); // там хранится количество товара
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(PRODUCT_PACK_BUTTON_ID))).click(); // это кнопка для выбора пачки товаров
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.id(ADD_TO_CART_BUTTON_ID)));
        // when
        addToCartButton.click();
        WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(By.id(NAV_MENU_CART_BUTTON_ID)));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(PRODUCT_COUNTER)));
        cartIcon.click();

        // then
        WebElement cartItem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(CART_FIRST_PRODUCT_ID)));
        assertTrue(cartItem.isDisplayed());
        assertEquals(expectedProductName, wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(CART_FIRST_PRODUCT_TITLE))).getText());
        assertEquals(expectedProductAmount, extractAmount(wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(CART_PRODUCT_AMOUNT))).getText())); // здесь пишется количество
    }

    @Test
    public void testAddOneProductToCartFromCatalogSuccess() {
        // given
        driver.get(CATALOG_URL);
        String expectedProductName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(CATALOG_PRODUCT_NAME_XPATH))).getText();
        Double expectedProductPrice = getRoundedPrice(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(CATALOG_PRODUCT_PRICE_ID))).getText());
        WebElement addToCartButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(CATALOG_ADD_TO_CART_BUTTON_XPATH))); // интересная ситуация, элемент меняется в процессе и просит новое состояние
        closeCookies();

        // when
        addToCartButton.click();
        WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(By.id(NAV_MENU_CART_BUTTON_ID)));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(PRODUCT_COUNTER))); // проверяем, что у корзины появляется значок товара (чтобы не перейти раньше времени)
        cartIcon.click();

        // then
        assertTrue(verifyCartItems(driver, new HashSet<>(Collections.singletonList(expectedProductName)), expectedProductPrice));
    }

    @Test
    public void testAddMultipleProductToCartFromCatalogSuccess() {
        // given
        driver.get(CATALOG_URL);
        String expectedProductName1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(CATALOG_PRODUCT_NAME_XPATH))).getText();
        String expectedProductName2 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(CATALOG_SECOND_PRODUCT_NAME_XPATH))).getText();
        Double expectedProductPrice1 = getRoundedPrice(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(CATALOG_PRODUCT_PRICE_ID))).getText());
        Double expectedProductPrice2 = getRoundedPrice(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(CATALOG_SECOND_PRODUCT_PRICE_ID))).getText());
        WebElement addToCartButton1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(CATALOG_ADD_TO_CART_BUTTON_XPATH))); // интересная ситуация, элемент меняется в процессе и просит новое состояние
        WebElement addToCartButton2 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(CATALOG_SECOND_ADD_TO_CART_BUTTON_XPATH))); // интересная ситуация, элемент меняется в процессе и просит новое состояние
        closeCookies();

        // when
        addToCartButton1.click();
        addToCartButton2.click();
        WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(By.id(NAV_MENU_CART_BUTTON_ID)));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(PRODUCT_COUNTER))); // проверяем, что у корзины появляется значок товара (чтобы не перейти раньше времени)
        cartIcon.click();

        // then
        assertTrue(verifyCartItems(driver, new HashSet<>(Arrays.asList(expectedProductName1, expectedProductName2)), expectedProductPrice1 + expectedProductPrice2));
    }

    @Test
    public void testAddOneProductToCartFromCatalogWithFiltersSuccess() {
        // given
        driver.get(CATALOG_FILTERED_URL);
        String expectedProductName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(CATALOG_FILTERED_PRODUCT_NAME_XPATH))).getText();
        Double expectedProductPrice = getRoundedPrice(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(CATALOG_FILTERED_PRODUCT_PRICE_ID))).getText());
        WebElement addToCartButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(CATALOG_FILTERED_ADD_TO_CART_BUTTON_XPATH))); // интересная ситуация, элемент меняется в процессе и просит новое состояние
        closeCookies();

        // when
        addToCartButton.click();
        WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(By.id(NAV_MENU_CART_BUTTON_ID)));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(PRODUCT_COUNTER))); // проверяем, что у корзины появляется значок товара (чтобы не перейти раньше времени)
        cartIcon.click();

        // then
        assertTrue(verifyCartItems(driver, new HashSet<>(Collections.singletonList(expectedProductName)), expectedProductPrice));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2}) // было и больше, но у них есть рандомные скидки на некоторые количества...
    public void testAddExtraProductToCartFromCatalogSuccess(int extraAmount) throws InterruptedException {
        // given
        driver.get(CATALOG_FILTERED_URL);
        String expectedProductName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(CATALOG_FILTERED_PRODUCT_NAME_XPATH))).getText();
        Double expectedProductPrice = getRoundedPrice(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(CATALOG_FILTERED_PRODUCT_PRICE_ID))).getText())*(extraAmount + 1);
        WebElement addToCartButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(CATALOG_FILTERED_ADD_TO_CART_BUTTON_XPATH))); // интересная ситуация, элемент меняется в процессе и просит новое состояние
        closeCookies();

        // when
        addToCartButton.click();
        WebElement addExtraProductButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(ADD_EXTRA_PRODUCT_BUTTON_XPATH)));
        for (int i = 0; i < extraAmount; i++) {
            addExtraProductButton.click();
            Thread.sleep(3000);
        }
        Thread.sleep(3000);
        WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(By.id(NAV_MENU_CART_BUTTON_ID)));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(PRODUCT_COUNTER))); // проверяем, что у корзины появляется значок товара (чтобы не перейти раньше времени)
        cartIcon.click();

        // then
        assertTrue(verifyCartItems(driver, new HashSet<>(Collections.singletonList(expectedProductName)), expectedProductPrice));
        assertEquals(extraAmount + 1, extractAmount(wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(CART_PRODUCT_AMOUNT))).getText())); // здесь пишется количество
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    public void testAddExtraProductToCartFromCartSuccess(int extraAmount) throws InterruptedException {
        // given
        driver.get(CATALOG_URL);
        String expectedProductName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(CATALOG_PRODUCT_NAME_XPATH))).getText();
        Double expectedProductPrice = getRoundedPrice(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(CATALOG_PRODUCT_PRICE_ID))).getText())*(extraAmount + 1);
        WebElement addToCartButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(CATALOG_ADD_TO_CART_BUTTON_XPATH))); // интересная ситуация, элемент меняется в процессе и просит новое состояние
        closeCookies();

        // when
        addToCartButton.click();
        WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(By.id(NAV_MENU_CART_BUTTON_ID)));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(PRODUCT_COUNTER))); // проверяем, что у корзины появляется значок товара (чтобы не перейти раньше времени)
        cartIcon.click();
        Thread.sleep(3000);
        WebElement addExtraProductButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(ADD_EXTRA_PRODUCT_FROM_CART_BUTTON_XPATH)));
        for (int i = 0; i < extraAmount; i++) {
            addExtraProductButton.click();
            Thread.sleep(3000);
        }
        Thread.sleep(3000);

        // then
        assertTrue(verifyCartItems(driver, new HashSet<>(Collections.singletonList(expectedProductName)), expectedProductPrice));
        assertEquals(extraAmount + 1, extractAmount(wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(CART_PRODUCT_AMOUNT))).getText())); // здесь пишется количество
    }

    @Test
    public void testDeleteProductFromCartSuccess() throws InterruptedException {
        // given
        driver.get(CATALOG_URL);
        String expectedProductName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(CATALOG_PRODUCT_NAME_XPATH))).getText();
        WebElement addToCartButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(CATALOG_ADD_TO_CART_BUTTON_XPATH))); // интересная ситуация, элемент меняется в процессе и просит новое состояние
        closeCookies();

        // when
        addToCartButton.click();
        WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(By.id(NAV_MENU_CART_BUTTON_ID)));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(PRODUCT_COUNTER))); // проверяем, что у корзины появляется значок товара (чтобы не перейти раньше времени)
        cartIcon.click();
        Thread.sleep(3000);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(DELETE_PRODUCT_FROM_CART_BUTTON_XPATH))).click();
        Thread.sleep(3000);


        // then
        assertTrue(verifyCartIsEmpty() || verifyCartItems(driver, new HashSet<>(Collections.singletonList(expectedProductName)), 0d));
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
            WebElement notificationContainer = driver.findElement(By.className(COOKIES_CONTAINER_CLASS));
            WebElement closeButton = notificationContainer.findElement(By.cssSelector(BUTTON_SELECTOR));
            closeButton.click();
        } catch (NoSuchElementException ignored) {

        }
    }

    private boolean verifyCartItems(WebDriver driver, Set<String> expectedItems, Double expectedPrice) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(PRODUCT_TITLE_ID_SELECTOR)));
        List<WebElement> productTitles = driver.findElements(By.cssSelector(PRODUCT_TITLE_ID_SELECTOR));
        HashSet<String> actualItems = new HashSet<>();
        for (WebElement element : productTitles) {
            actualItems.add(element.getText().trim());
        }
        Double actualPrice = getRoundedPrice(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(CART_TOTAL_PRICE_ID))).getText());
        return (actualItems.equals(expectedItems) && actualPrice.equals(expectedPrice));
    }

    private boolean verifyCartIsEmpty() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(EMPTY_CART_CLASS)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}