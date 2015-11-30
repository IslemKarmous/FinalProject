package source;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedHashMap;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {
	
	public static final String MAIN_URL = "http://hiring-tests.s3-website-eu-west-1.amazonaws.com/2015_Developer_Scrape/5_products.html";

	private static DecimalFormat df;
	
	static {
		//initialize decimal format
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
		otherSymbols.setDecimalSeparator('.');
		df = new DecimalFormat("#.00", otherSymbols);
	}
	
	public static void main(String[] args) {
		
		Main instance = new Main();

		// Connect to the main URL
		Document mainDoc = instance.connectToUrl(MAIN_URL);
		if (mainDoc != null) {
			
			// retrieve all products from the main document
			Elements inputProducts = instance.retrieveProducts(mainDoc);

			// format products as Json and print them
			JSONObject outputProducts;
			try {
				outputProducts = instance.getProductsAsJson(inputProducts);
				System.out.println(outputProducts);
			} catch (JSONException e) {
				e.printStackTrace();
				System.out.println("Unable to format products as json array");
			}
			
		}
	}

	public JSONObject getProductsAsJson(Elements inputProducts) throws JSONException {
		
		JSONArray outputProducts = new JSONArray();
		double totalPrice = 0.0;
		
		//loop over all input products to make the json array
		for (Element productContainer : inputProducts) {
			
			//retrieve needed information: title, size of the product page, UnitPrice and description
			double unitPrice = getUnitPrice(productContainer);
			totalPrice += unitPrice;
			
			Element productInfo = productContainer.select("a[href]").first();
			
			String title = getTitle(productInfo);

			String productUrl = productInfo.attr("href");
			String size = getSize(productUrl);
			String description = getDescription(productUrl);

			// make a json product with retreived information
			LinkedHashMap<String, Object> orderedInfoProduct = new LinkedHashMap<String, Object>();
			orderedInfoProduct.put("title", title);
			orderedInfoProduct.put("size", size);
			orderedInfoProduct.put("unit_price", df.format(unitPrice));
			orderedInfoProduct.put("description", description);
			
			JSONObject outputProduct = new JSONObject(orderedInfoProduct);
			
			// add the json product to the json array
			outputProducts.put(outputProduct);

		}

		// make the final result
		LinkedHashMap<String, Object> ordredProducts = new LinkedHashMap<String, Object>();
		ordredProducts.put("results", outputProducts);
		ordredProducts.put("total", df.format(totalPrice));
		
		JSONObject productsAsJson = new JSONObject(ordredProducts);
		
		return productsAsJson;

	}
	
	public Document connectToUrl(String url) {
		Document doc = null;
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Unable to connect to the URL : " + url);
		}
		return doc;
	}
	
	
	public Elements retrieveProducts(Document doc) {
		Element productsContainer = doc.select("ul.listView").first();
		Elements products = productsContainer.select("div.product");
		return products;
	}

	public String getTitle(Element productInfo) {
		return productInfo.text();
	}

	public String getSize(String productUrl) {
		HttpURLConnection content;
		double size = 0;
		try {
			content = (HttpURLConnection) new URL(productUrl).openConnection();
			size = content.getContentLengthLong() / 1024.0;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to open connection linking to the URL : " + productUrl);
		}
		return df.format(size) + "kb";

	}
	
	public double getUnitPrice(Element productContainer) {
		String unitPrice = productContainer.select("p.pricePerUnit").first().text();
		String unitPriceValue = unitPrice.substring(unitPrice.indexOf("&pound") + 6, unitPrice.indexOf("/unit"));
		return Double.parseDouble(unitPriceValue);
	}

	public String getDescription(String productUrl) {
		Document productDoc = connectToUrl(productUrl);
		
		if(productDoc == null)
			return null;
		
		String	description = productDoc.select("meta").first().attr("content");
		return description;
	}

}
