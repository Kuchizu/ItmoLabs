import logging
import random
import time

import pyperclip
from selenium import webdriver
from selenium.webdriver.chrome.options import Options as ChromeOptions
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.firefox.options import Options as FirefoxOptions
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s :: %(message)s",
    datefmt="%H:%M:%S",
)
logger = logging.getLogger(__name__)

class ImgbbTestSuite:
    LOGIN_URL = "https://imgbb.com/login"
    GALLERY_URL = "https://kuchizu.imgbb.com/"
    LOGOUT_URL = "https://imgbb.com/logout"

    def __init__(self, browser_name: str, headless: bool = False):
        if browser_name == "chrome":
            opts = ChromeOptions()
            if headless:
                opts.add_argument("--headless")
            self.driver = webdriver.Chrome(options=opts)
        elif browser_name == "firefox":
            opts = FirefoxOptions()
            if headless:
                opts.add_argument("--headless")
            self.driver = webdriver.Firefox(options=opts)
        else:
            raise ValueError("Unsupported browser")
        self.wait = WebDriverWait(self.driver, 20)
        self.driver.maximize_window()

    def test_homepage(self):
        logger.info("Loading homepage")
        self.driver.get(self.GALLERY_URL)
        self.wait.until(EC.presence_of_element_located((By.XPATH, "//body")))
        logger.info(
            f"Homepage loaded. Title: {self.driver.title}, URL: {self.driver.current_url}"
        )

    def test_login(self, username: str, password: str):
        logger.info("Starting login")
        self.driver.get(self.LOGIN_URL)
        login_input = self.wait.until(
            EC.presence_of_element_located((By.XPATH, "(//input[@type='text'])[1]"))
        )
        login_input.clear()
        login_input.send_keys(username)
        time.sleep(random.uniform(0.3, 0.7))

        pwd_input = self.wait.until(
            EC.presence_of_element_located((By.XPATH, "(//input[@type='password'])[1]"))
        )
        pwd_input.clear()
        pwd_input.send_keys(password)
        time.sleep(random.uniform(0.3, 0.7))

        submit_btn = self.wait.until(
            EC.element_to_be_clickable((By.XPATH, "(//form//button[@type='submit'])[1]"))
        )
        submit_btn.click()

        self.wait.until(
            EC.presence_of_element_located((By.XPATH, "/html/body/header/div/ul[2]/li[1]"))
        )
        logger.info("Login successful")

    def print_all_attributes(self, elem):
        attrs = self.driver.execute_script(
            "let items = {}; for (let attr of arguments[0].attributes) { items[attr.name] = attr.value }; return items;",
            elem,
        )
        for k, v in attrs.items():
            print(f"{k}: {v}")

    def test_upload_image(self, image_path: str):
        logger.info("Uploading image")
        self.driver.get("https://kuchizu.imgbb.com/")
        container = self.wait.until(
            EC.presence_of_element_located((By.XPATH, "//div[@id='anywhere-upload']"))
        )
        file_input = container.find_element(By.XPATH, ".//input[@type='file']")
        self.driver.execute_script("arguments[0].style.display = 'block';", file_input)
        time.sleep(0.5)
        file_input.send_keys(image_path)
        time.sleep(1)
        confirm_btn = self.wait.until(
            EC.element_to_be_clickable((By.XPATH, "//button[contains(text(),'Загрузка')]"))
        )
        confirm_btn.click()

    def test_get_share_link(self):
        logger.info("Retrieving share link")
        link_container = self.wait.until(
            EC.presence_of_element_located(
                (By.XPATH, "/html/body/div[4]/div[1]/div/div[6]/div/div[2]/div[1]")
            )
        )
        self.driver.execute_script(
            """
            const btn = arguments[0].querySelector("button");
            if (btn) {
                btn.style.display = 'block';
                btn.style.visibility = 'visible';
            }
        """,
            link_container,
        )
        copy_button = self.wait.until(
            EC.element_to_be_clickable(
                (By.XPATH, "/html/body/div[4]/div[1]/div/div[6]/div/div[2]/div[1]/button")
            )
        )
        self.driver.execute_script("arguments[0].click();", copy_button)
        time.sleep(0.5)
        clipboard_link = pyperclip.paste()
        logger.info(f"Copied link from clipboard: {clipboard_link}")

    def test_upload_large_file(self, image_path: str):
        logger.info("Uploading large file (negative case)")
        self.driver.get("https://kuchizu.imgbb.com/")
        container = self.wait.until(
            EC.presence_of_element_located((By.XPATH, "//div[@id='anywhere-upload']"))
        )
        file_input = container.find_element(By.XPATH, ".//input[@type='file']")
        self.driver.execute_script("arguments[0].style.display = 'block';", file_input)
        time.sleep(0.5)
        file_input.send_keys(image_path)

        try:
            time.sleep(3)
            confirm_btn = WebDriverWait(self.driver, 5).until(
                EC.element_to_be_clickable((By.XPATH, "//button[contains(text(),'Загрузка')]"))
            )
            self.driver.execute_script("arguments[0].click();", confirm_btn)
            copy_button = WebDriverWait(self.driver, 5).until(
                EC.element_to_be_clickable(
                    (By.XPATH, "/html/body/div[4]/div[1]/div/div[6]/div/div[2]/div[1]/button")
                )
            )
            self.driver.execute_script("arguments[0].click();", copy_button)
            time.sleep(0.5)
            clipboard_link = pyperclip.paste()
            logger.info(f"Large image uploaded (unexpected). Link: {clipboard_link}")

        except Exception:
            logger.info("Expected error occurred or upload was blocked for large file.")

    def test_upload_multiple_images(self, image_paths: list):
        logger.info("Uploading multiple images")
        self.driver.get("https://kuchizu.imgbb.com/")
        container = self.wait.until(
            EC.presence_of_element_located((By.XPATH, "//div[@id='anywhere-upload']"))
        )
        file_input = container.find_element(By.XPATH, ".//input[@type='file']")
        self.driver.execute_script("arguments[0].style.display = 'block';", file_input)
        time.sleep(0.5)
        file_input.send_keys("\n".join(image_paths))
        confirm_btn = self.wait.until(
            EC.element_to_be_clickable((By.XPATH, "//button[contains(text(),'Загрузка')]"))
        )
        confirm_btn.click()
        logger.info("Multiple images uploaded (confirmation clicked)")

        try:
            copy_button = self.wait.until(
                EC.element_to_be_clickable(
                    (By.XPATH, "/html/body/div[4]/div[1]/div/div[6]/div/div[2]/div[1]/button")
                )
            )
            self.driver.execute_script("arguments[0].click();", copy_button)
            time.sleep(0.5)
            clipboard_link = pyperclip.paste()
            logger.info(f"First image link: {clipboard_link}")
        except:
            logger.warning("Link not copied — multiple uploads might need manual confirmation.")

    def test_create_edit_delete_album(self):
        logger.info("Creating album")
        self.driver.get(self.GALLERY_URL)
        create_album_btn = self.wait.until(
            EC.element_to_be_clickable((By.XPATH, "/html/body/div[2]/div/div[1]/div[2]/div[3]/button"))
        )
        create_album_btn.click()
        time.sleep(2)
        import string, random

        rand_string = lambda prefix, n: prefix + "".join(random.choices(string.ascii_letters + string.digits, k=n))
        name, desc = rand_string("Album_", 6), rand_string("Description_", 10)

        name_input = self.wait.until(
            EC.presence_of_element_located((By.XPATH, "/html/body/div[1]/div/form/div[1]/div/div[1]/input"))
        )
        name_input.clear()
        name_input.send_keys(name)

        desc_input = self.driver.find_element(
            By.XPATH, "/html/body/div[1]/div/form/div[1]/div/div[2]/textarea"
        )
        desc_input.clear()
        desc_input.send_keys(desc)
        self.driver.find_element(By.XPATH, "/html/body/div[1]/div/form/div[2]/button").click()
        logger.info(f"Album created: {name}")

        time.sleep(5)  # redirect
        edit_btn = self.wait.until(
            EC.element_to_be_clickable((By.XPATH, "/html/body/div[1]/div/div[1]/div[1]/div/div[3]/a"))
        )
        edit_btn.click()
        time.sleep(2)

        new_name, new_desc = rand_string("Edited_", 6), rand_string("Updated_", 10)
        name_input = self.wait.until(
            EC.presence_of_element_located((By.XPATH, "/html/body/div[1]/div/form/div[1]/div/div[1]/input"))
        )
        name_input.clear()
        name_input.send_keys(new_name)

        desc_input = self.driver.find_element(
            By.XPATH, "/html/body/div[1]/div/form/div[1]/div/div[2]/textarea"
        )
        desc_input.clear()
        desc_input.send_keys(new_desc)
        self.driver.find_element(By.XPATH, "/html/body/div[1]/div/form/div[2]/button").click()
        logger.info("Album updated")

        time.sleep(5)
        delete_btn = self.wait.until(
            EC.element_to_be_clickable((By.XPATH, "/html/body/div[1]/div/div[1]/div[1]/div/div[5]/a/span[2]"))
        )
        delete_btn.click()
        time.sleep(2)
        self.wait.until(EC.element_to_be_clickable((By.XPATH, "/html/body/div[1]/div/form/div[2]/button"))).click()
        logger.info("Album deleted")

    def test_interact_with_random_image(self):
        logger.info("Interacting with a random image")
        self.driver.get(self.GALLERY_URL)
        self.wait.until(EC.presence_of_all_elements_located((By.XPATH, "//img[contains(@src,'ibb.co')]")))
        images = self.driver.find_elements(By.XPATH, "//img[contains(@src,'ibb.co')]")
        if not images:
            logger.warning("No images found")
            return
        random.choice(images).click()
        time.sleep(2)
        body = self.driver.find_element(By.TAG_NAME, "body")
        for key in (Keys.ARROW_LEFT, Keys.ARROW_RIGHT, Keys.ARROW_RIGHT, Keys.ESCAPE):
            body.send_keys(key)
            time.sleep(1)

    def test_logout(self):
        logger.info("Logging out")
        self.driver.get(self.LOGOUT_URL)
        self.wait.until(EC.presence_of_element_located((By.XPATH, "//body")))
        logger.info("Logout successful")

    def quit(self):
        logger.info("Closing browser")
        self.driver.quit()

def run_step(desc: str, fn, *args):
    try:
        logger.info(f"⏩  {desc}")
        fn(*args)
        logger.info(f"✅  {desc} — OK")

    except Exception as exc:
        logger.exception(f"❌  {desc} — ERROR: {exc}")

if __name__ == "__main__":
    USERNAME = "kuchizuheroku@gmail.com"
    PASSWORD = "kuchizu112"
    SMALL_IMAGE = r"C:\Users\Kuchizu\Desktop\Тестирование проргаммного обеспечения\Lab-3\1.jpeg"
    LARGE_IMAGE = r"C:\Users\Kuchizu\Desktop\Тестирование проргаммного обеспечения\Lab-3\large.png"
    MULTIPLE_IMAGES = [
        r"C:\Users\Kuchizu\Desktop\Тестирование проргаммного обеспечения\Lab-3\1.jpeg",
        r"C:\Users\Kuchizu\Desktop\Тестирование проргаммного обеспечения\Lab-3\2.png",
        r"C:\Users\Kuchizu\Desktop\Тестирование проргаммного обеспечения\Lab-3\3.jpg",
    ]

    for browser in ("chrome", "firefox"):
        logger.info(f"\n========== RUNNING TESTS ON {browser.upper()} ==========")
        suite = ImgbbTestSuite(browser_name=browser, headless=False)
        try:
            run_step("Homepage", suite.test_homepage)
            run_step("Random image interaction", suite.test_interact_with_random_image)
            run_step("Login", suite.test_login, USERNAME, PASSWORD)
            run_step("Create/edit/delete album", suite.test_create_edit_delete_album)
            run_step("Upload small image", suite.test_upload_image, SMALL_IMAGE)
            run_step("Share link after small upload", suite.test_get_share_link)
            run_step("Upload multiple images", suite.test_upload_multiple_images, MULTIPLE_IMAGES)
            run_step("Share link after multi-upload", suite.test_get_share_link)
            run_step("Upload large image", suite.test_upload_large_file, LARGE_IMAGE)
            run_step("Share link after large upload attempt", suite.test_get_share_link)
            run_step("Logout", suite.test_logout)

        finally:
            suite.quit()
