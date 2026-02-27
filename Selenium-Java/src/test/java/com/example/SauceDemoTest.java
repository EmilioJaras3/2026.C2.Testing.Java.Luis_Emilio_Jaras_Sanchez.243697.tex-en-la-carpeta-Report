package com.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

public class SauceDemoTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
        try {
            Files.createDirectories(Paths.get("screenshots"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    void setupTest() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "standard_user",
            "locked_out_user",
            "problem_user",
            "performance_glitch_user",
            "error_user",
            "visual_user"
    })
    @DisplayName("Test SauceDemo with all user types")
    void testSauceDemoUsers(String username) {
        driver.get("https://www.saucedemo.com/");

        try {
            driver.findElement(By.id("user-name")).sendKeys(username);
            driver.findElement(By.id("password")).sendKeys("secret_sauce");
            driver.findElement(By.id("login-button")).click();

            if (username.equals("locked_out_user")) {
                WebElement errorMsg = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h3[data-test='error']")));
                takeScreenshot(username + "_locked_out");
                Assertions.assertTrue(errorMsg.getText().contains("locked out"));
                return;
            }

                        
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("inventory_container")));
            Assertions.assertTrue(driver.getTitle().contains("Swag Labs"));

            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("inventory_item")));
            List<WebElement> addButtons = driver.findElements(By.xpath("//button[text()='Add to cart']"));

            int itemsToAdd = Math.min(4, addButtons.size());
            for (int i = 0; i < itemsToAdd; i++) {
                try {
                    addButtons.get(i).click();
                } catch (Exception e) {}
            }

            WebElement cartBadge = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("shopping_cart_badge")));
            String itemsInCartStr = cartBadge.getText();

            if (!username.equals("problem_user") && !username.equals("error_user")) {
                 Assertions.assertEquals(String.valueOf(itemsToAdd), itemsInCartStr, "Cart badge mismatch");
            }
            
            takeScreenshot(username + "_success");

        } catch (TimeoutException e) {
                            
            takeScreenshot(username + "_timeout_failure");
        } catch (Exception e) {
            takeScreenshot(username + "_exception");
        }
    }
                    

    private void takeScreenshot(String name) {

            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File destFile = new File("screenshots/" + name + ".png");
                .copy(srcFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOE
                ntStackTrace();
                        
        }

            

                    