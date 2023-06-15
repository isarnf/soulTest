package org.example;

import com.github.javafaker.Faker;
import com.sun.jna.Structure;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.setAllowComparingPrivateFields;
import static org.openqa.selenium.By.xpath;

public class SoulTests {
    private WebDriver driver;
    private final Faker faker = new Faker();

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);
        driver.get("http://localhost:3000/");
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

    private List<WebElement> getRows(WebDriver driver) {
        final List<WebElement> rows = new WebDriverWait(driver, Duration.ofSeconds(3))
                .until(driver1 -> driver1.findElements(By.xpath("/html/body/div/div/table/tbody/tr")));
        if (rows == null)
            throw new NoSuchElementException("Table is empty");

        return rows;
    }

    private void fillInputs(WebDriver driver, String name, String owner, String location) {
        final WebElement inputName = driver.findElement(xpath("//*[@id=\"soul-name\"]"));
        final WebElement inputOwner = driver.findElement(xpath("//*[@id=\"soul-owner\"]"));
        final Select selectLocalication = new Select(driver.findElement(xpath("//*[@id=\"soul-location\"]")));

        inputName.sendKeys(name);
        inputOwner.sendKeys(owner);
        selectLocalication.selectByValue(location);
    }

    private void clickTheSoulEditButton(WebElement soul) {
        soul.findElements(By.tagName("td")).get(4).findElements(By.tagName("button")).get(0).click();
    }

    private void clickTheSoulDeleteButton(WebElement soul) {
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(
                        soul.findElements(By.tagName("td")).get(4).findElements(By.tagName("button")).get(1))
                )
                .click();
    }

    private void clickSaveButton(WebDriver driver) {
        driver.findElement(xpath("/html/body/div/form/div[3]/input")).click();
    }

    private WebElement findSoulById(WebDriver driver, String id) {
        final List<WebElement> rows = getRows(driver);

        for (WebElement e : rows)
            if (e.findElements(By.tagName("td")).get(0).getText().equals(id))
                return e;

        return null;
    }

    private String getSoulId(WebElement soul) {
        return soul.findElements(By.tagName("td")).get(0).getText();
    }

    @Nested
    @DisplayName("When loading landing page")
    class LoadingLandingPage {
        @Test
        @DisplayName("Should show emojis")
        void shouldShowEmojis() {
            assertThat(driver.findElement(xpath("/html/body/div/h3")).isDisplayed()).isTrue();
        }
    }

    @Nested
    @DisplayName("When adding a soul")
    class AddNewSoul {

        private String getNameValueFromRow(WebDriver driver, int rowIndex){
            final List<WebElement> tableRows = getRows(driver);
            final String name = tableRows.get(rowIndex).findElements((By.tagName("td"))).get(1).getText();
            return name;
        }

        private String getOwnerValueFromRow(WebDriver driver, int rowIndex){
            final List<WebElement> tableRows = getRows(driver);
            final String owner = tableRows.get(rowIndex).findElements((By.tagName("td"))).get(2).getText();
            return owner;
        }

        private String getLocationValueFromRow(WebDriver driver, int rowIndex){
            final List<WebElement> tableRows = getRows(driver);
            final String location = tableRows.get(rowIndex).findElements((By.tagName("td"))).get(3).getText();
            return location;
        }

        @Test
        @DisplayName("Should show added soul in the table")
        void shouldShowAddedSoulInTheTable() {

            fillInputs(driver, "Pessoa A", "Pessoa B", "sky");

            final List<WebElement> tableRowsBeforeSaveButtonClick = getRows(driver);
            final int numberOfRowsBeforeSaveButtonClick = tableRowsBeforeSaveButtonClick.size();

            clickSaveButton(driver);
            driver.navigate().refresh();

            final List<WebElement> tableRowsAfterSaveButtonClick = getRows(driver);
            final int numberOfRowsAfterSaveButtonClick = tableRowsAfterSaveButtonClick.size();
            assertThat(numberOfRowsAfterSaveButtonClick).isEqualTo(numberOfRowsBeforeSaveButtonClick + 1);

        }

        @Test
        @DisplayName("Should not add soul when fields are empty.")
        void shouldntAddSoulWhenFormIsEmpty() {

            fillInputs(driver, "", "", "");

            final List<WebElement> tableRowsBeforeSaveButtonClick = getRows(driver);
            final int numberOfRowsBeforeSaveButtonClick = tableRowsBeforeSaveButtonClick.size();

            clickSaveButton(driver);
            driver.navigate().refresh();

            final List<WebElement> tableRowsAfterSaveButtonClick = getRows(driver);
            final int numberOfRowsAfterSaveButtonClick = tableRowsAfterSaveButtonClick.size();
            assertThat(numberOfRowsAfterSaveButtonClick).isEqualTo(numberOfRowsBeforeSaveButtonClick);
        }

        @Test
        @DisplayName("Should not add duplicates.")
        void shouldntAddDuplicates() {

            final SoftAssertions softly = new SoftAssertions();

            // Count the number of rows before saving
            final List<WebElement> tableRows = getRows(driver);
            final int numberOfRowsBeforeSaveButtonClick = tableRows.size();
            softly.assertThat(numberOfRowsBeforeSaveButtonClick).isGreaterThan(0);

            // Get the values from first row
            final String nameFirstRow = getNameValueFromRow(driver,0);
            final String ownerFirstRow = getOwnerValueFromRow(driver,0);
            final String locationFirstRow = getLocationValueFromRow(driver,0);

            // Fill form inputs with same values of the first row
            fillInputs(driver, nameFirstRow, ownerFirstRow, locationFirstRow);

            // Click the save button
            clickSaveButton(driver);
            driver.navigate().refresh();

            // Count number of table rows after saving
            final List<WebElement> tableRowsAfterSaveButtonClick = getRows(driver);
            final int numberOfRowsAfterSaveButtonClick = tableRowsAfterSaveButtonClick.size();
            softly.assertThat(numberOfRowsAfterSaveButtonClick).isEqualTo(numberOfRowsBeforeSaveButtonClick);
            softly.assertAll();

        }
    }

    @Nested
    @DisplayName("When editing a soul")
    class EditSoul {

        private String getRandomLocation() {
            final List<String> locations = Arrays.asList("sky", "hell", "purgatory");
            return locations.get(new Random().nextInt(locations.size()));
        }

        private void cleanInputs(WebDriver driver) {
            final WebElement inputName = driver.findElement(xpath("//*[@id=\"soul-name\"]"));
            final WebElement inputOwner = driver.findElement(xpath("//*[@id=\"soul-owner\"]"));
            final Select selectLocalication = new Select(driver.findElement(xpath("//*[@id=\"soul-location\"]")));

            inputName.clear();
            inputOwner.clear();
            selectLocalication.selectByVisibleText("Selecione");
        }

        private WebElement getFirstSoulInTable(WebDriver driver) {
            WebElement soul = driver.findElement(xpath("/html/body/div/div/table/tbody/tr[1]"));
            if (soul == null)
                throw new NoSuchElementException("Soul not found");
            return soul;
        }

        @Test
        @DisplayName("Should edit the first soul of the table")
        void shouldEditTheFirstSoulOfTheTable() {
            final String changeNameTo = faker.name().fullName();
            final String changeOwnerTo = faker.name().fullName();
            final String changeLocationTo = getRandomLocation();

            final WebElement firstSoulInTable = getFirstSoulInTable(driver);
            final String id = getSoulId(firstSoulInTable);
            clickTheSoulEditButton(firstSoulInTable);

            cleanInputs(driver);
            fillInputs(driver, changeNameTo, changeOwnerTo, changeLocationTo);
            clickSaveButton(driver);


            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(driver -> driver.findElement(By.xpath("/html/body/div/div/table/tbody/tr[1]")));

            final WebElement soul = findSoulById(driver, id);
            final String soulName = soul.findElements(By.tagName("td")).get(1).getText();
            final String soulOwner = soul.findElements(By.tagName("td")).get(2).getText();
            final String soulLocation = soul.findElements(By.tagName("td")).get(3).getText();

            final var softly = new SoftAssertions();
            softly.assertThat(soulName).isEqualTo(changeNameTo);
            softly.assertThat(soulOwner).isEqualTo(changeOwnerTo);
            softly.assertThat(soulLocation).isEqualTo(changeLocationTo);
            softly.assertAll();
        }
    }

    @Nested
    @DisplayName("When removing a soul")
    class RemoveSoul {

        @Test
        @DisplayName("Should remove the first soul of the table")
        void shouldRemoveTheFirstSoulOfTheTable() {

            final WebElement soul = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(driver -> driver.findElement
                            (By.xpath("/html/body/div/div/table/tbody/tr[1]"))
                    );

            final String idOfFirstSoulBeforeExclusion = getSoulId(soul);

            clickTheSoulDeleteButton(soul);

            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.alertIsPresent())
                    .accept();

            driver.navigate().refresh();

            boolean elementIsNotPresent = findSoulById(driver, idOfFirstSoulBeforeExclusion) == null;

            assertThat(elementIsNotPresent).isTrue();
        }
    }
}

