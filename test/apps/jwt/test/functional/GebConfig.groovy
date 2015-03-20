import org.openqa.selenium.Dimension
import org.openqa.selenium.phantomjs.PhantomJSDriver
import org.openqa.selenium.remote.DesiredCapabilities

driver = {
    def capabilities = new DesiredCapabilities()
    capabilities.setCapability("phantomjs.page.customHeaders.Accept-Language", "en-UK")
    def d = new PhantomJSDriver(capabilities)
    d.manage().window().setSize(new Dimension(1028, 768))
    return d
}

atCheckWaiting = true
baseNavigatorWaiting = true
waiting {
    timeout = 10
    retryInterval = 0.5
}
