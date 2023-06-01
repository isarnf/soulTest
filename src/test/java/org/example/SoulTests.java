package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Random;

import static org.openqa.selenium.By.*;

public class SoulTests {

    private WebDriver driver;

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);
    }
    @AfterEach
    void tearDown(){
        driver.quit();
    }

    @Nested
    @DisplayName("When editing a soul")
    class EditSoul{
        @Test
        @DisplayName("Should edit the first soul of the table")
        void editar() throws InterruptedException {
            driver.get("http://localhost:3000/");

            final String changeNameTo = "2";        Thread.sleep(50);
            final String changeOwnerTo = "2";
            final String changeLocalicationTo = "Céu";

            final String elementOneName = driver.findElement(xpath("/html/body/div/div/table/tbody/tr[1]/td[2]")).getText();
            final String elementOneOwner = driver.findElement(xpath("/html/body/div/div/table/tbody/tr[1]/td[3]")).getText();
            final String elementOneLocalication = driver.findElement(xpath("/html/body/div/div/table/tbody/tr[1]/td[4]")).getText();

            System.out.println(elementOneLocalication);

            driver.findElement(xpath("/html/body/div/div/table/tbody/tr[1]/td[5]/button[1]")).click();

            Thread.sleep(500);

            final WebElement inputName = driver.findElement(xpath("//*[@id=\"soul-name\"]"));
            final WebElement inputOwner = driver.findElement(xpath("//*[@id=\"soul-owner\"]"));

            inputName.clear();
            inputOwner.clear();

            inputName.sendKeys(changeNameTo);
            inputOwner.sendKeys(changeOwnerTo);

            final WebElement buttonSave = driver.findElement(xpath("//*[@id=\"save-btn\"]"));
            buttonSave.click();

            final String elementOneNameAfter = driver.findElement(xpath("/html/body/div/div/table/tbody/tr[1]/td[2]")).getText();
            final String elementOneOwnerAfter = driver.findElement(xpath("/html/body/div/div/table/tbody/tr[1]/td[3]")).getText();
            final String elementOneLocalicationAfter = driver.findElement(xpath("/html/body/div/div/table/tbody/tr[1]/td[4]")).getText();

            final var softly = new SoftAssertions();
            softly.assertThat(elementOneNameAfter).isEqualTo(changeNameTo);
            softly.assertThat(elementOneOwnerAfter).isEqualTo(changeOwnerTo);
            //softly.assertThat(elementOneOwnerAfter).isEqualTo("Deus");
            softly.assertAll();
        }

    }
}
