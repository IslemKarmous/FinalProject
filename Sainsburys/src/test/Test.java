package test;
import static org.junit.Assert.assertEquals;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;

import source.Main;

public class Test {
	
	Main mainInstance;
	Element firstProductContainer;
	String firstProductUrl;
	
	//Initialization before the test.
	@Before
    public void setUp() {
		mainInstance = new Main();
		Document mainDoc = mainInstance.connectToUrl(Main.MAIN_URL);
		firstProductContainer = mainInstance.retrieveProducts(mainDoc).first();
		firstProductUrl = "http://hiring-tests.s3-website-eu-west-1.amazonaws.com/2015_Developer_Scrape/sainsburys-apricot-ripe---ready-320g.html";
    }
		
	@org.junit.Test
	public void testGetTitle() {

		Element productInfo = firstProductContainer.select("a[href]").first();
		String title = mainInstance.getTitle(productInfo);
		assertEquals("Sainsbury's Apricot Ripe & Ready x5", title);
	}
	
	@org.junit.Test
	public void testGetSize() {
		String size = mainInstance.getSize(firstProductUrl);
		assertEquals(size, "38.27kb");		
	}
	
	@org.junit.Test
	public void testGetUnitPrice() {
		double unitPrice = mainInstance.getUnitPrice(firstProductContainer);
		assertEquals(3.50, unitPrice, 0);
	}
	
	@org.junit.Test
	public void testGetDescription() {
		String description = mainInstance.getDescription(firstProductUrl);
		assertEquals(
				"Buy Sainsbury's Apricot Ripe & Ready x5 online from Sainsbury's, the same great quality, freshness and choice you'd find in store. Choose from 1 hour delivery slots and collect Nectar points.",
				description);
	}

}
