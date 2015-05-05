package de.uni_mannheim.informatik.dws.web_mining.ffse.crawler;

import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JSoupCrawler {

	final static String crawlStorageFolder = "data2/";
	public final static Pattern MAINFTABLE = Pattern.compile(".*<!-- MAIN CONTENT TABLE -->(.*)<!-- END MAIN CONTENT TABLE -->.*", Pattern.DOTALL);
	public final static Pattern SUBMITTERINFO = Pattern.compile("Submitted by: (.*) \\((\\d{2}/\\d{2}/\\d{4}).*", Pattern.DOTALL);

	public static void main(String[] args) {
		JSoupCrawler crawler = new JSoupCrawler();
		crawler.crawl();

	}

	public void crawl() {
		for (int idIterator = 1; idIterator < 12000; ++idIterator) {
			try {
				int offset = 0;
				Restaurant restaurant = null;
				do {
					System.out.print("Crawl restaurant id: " + idIterator +  " : ");
					Response response = Jsoup.connect("http://www.we8there.com/rest_detail.php?busid=" + idIterator + "&cursor=" + offset).followRedirects(false).execute();
					if (response.statusCode() < 300) {
						String strDocument = extractRelevantPagePart(response.body());
						if(strDocument.equals("")) {
							break;
						}
						if (restaurant == null) {
							restaurant = parsePage(idIterator, strDocument);
						} else {
							restaurant.reviews.addAll(extractReviews(strDocument));
						}
						offset += 5;
						Thread.sleep(100);
					}
					Thread.sleep(100);
				} while (restaurant != null && offset < restaurant.numOfReviews);
				if(restaurant!=null) {
					restaurant.save(crawlStorageFolder);
				}
				Thread.sleep(new Random().nextInt(1000));
				if((idIterator+1)%500==0) {
					Thread.sleep(new Random().nextInt(60*1000));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	public String extractRelevantPagePart(String strDocument) {
		Matcher maintableMatcher = MAINFTABLE.matcher(strDocument);
		if (maintableMatcher.find()) {
			System.out.println("Yes.");
			return maintableMatcher.group(1);
		}
			
		System.out.println("No.");
		return "";
	}

	public Restaurant parsePage(int id, String strDocument) {
		try {
			Restaurant restaurant = new Restaurant();
			restaurant.id = String.valueOf(id);

			Document doc = Jsoup.parse(strDocument);
			Element table = doc.select("table table").get(0);
			restaurant.name = table.select("span[class=h1]").get(0).ownText();
			Elements mixed = table.select("tr:nth-child(3)").get(0).select("td");
			restaurant.addressAndContact = mixed.get(0).ownText();
			restaurant.category = mixed.get(1).ownText().replace("Category: ", "");

			mixed = table.select("tr:nth-child(4) td b");
			String[] strs = mixed.get(0).text().split(":");
			restaurant.numOfReviews = Integer.parseInt(strs[1].trim());
			if (restaurant.numOfReviews > 1) {
				mixed = table.select("tr:nth-child(4) td:nth-child(2) table table");
				restaurant.food = mixed.get(0).select("tr:nth-child(2) td:nth-child(2) img").size();
				restaurant.service = mixed.get(0).select("tr:nth-child(3) td:nth-child(2) img").size();
				restaurant.price_value = mixed.get(0).select("tr:nth-child(4) td:nth-child(2) img").size();
				restaurant.atmosphere = mixed.get(0).select("tr:nth-child(5) td:nth-child(2) img").size();
				restaurant.overall = mixed.get(0).select("tr:nth-child(5) td:nth-child(2) img").size();
			}
			restaurant.reviews.addAll(extractReviews(doc));

			return restaurant;
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
			e.printStackTrace();
			return null;
		}
	}

	public ArrayList<Review> extractReviews(String strDocument) {
		Document document = Jsoup.parse(strDocument);
		return extractReviews(document);
		
	}
	
	public ArrayList<Review> extractReviews(Document document) {
		ArrayList<Review> reviews = new ArrayList<Review>();
		Element table = document.select("table table").get(0);
		Elements reviewTables = table.select("td[colspan=2] table[align=right]");

		for (Element reviewTable : reviewTables) {
			Review review = new Review();
			Element reviewComplete = reviewTable.parent();
			review.text = reviewComplete.ownText();
			String info = reviewComplete.select("p").get(0).ownText();
			Matcher matcher = SUBMITTERINFO.matcher(info);
			if (matcher.find()) {
				review.username = matcher.group(1);
				review.date = matcher.group(2);
			}
			review.firstReview = info.contains("First review.");
			Element mixed = reviewComplete.select("table table").get(0);
			
			int rowIterator = 1;
			String rowBuffer = checkAndExtractRow(rowIterator, mixed, "Food");
			if(rowBuffer!=null) {
				review.food = Integer.parseInt(rowBuffer);
				rowIterator++;
			}
			rowBuffer = checkAndExtractRow(rowIterator, mixed, "Service");
			if(rowBuffer!=null) {
				review.service = Integer.parseInt(rowBuffer);
				rowIterator++;
			}
			rowBuffer = checkAndExtractRow(rowIterator, mixed, "Price");
			if(rowBuffer!=null) {
				review.price_value = Integer.parseInt(rowBuffer);
				rowIterator++;
			}
			rowBuffer = checkAndExtractRow(rowIterator, mixed, "Atmosphere");
			if(rowBuffer!=null) {
				review.atmosphere = Integer.parseInt(rowBuffer);
				rowIterator++;
			}
			rowBuffer = checkAndExtractRow(rowIterator, mixed, "Overall");
			if(rowBuffer!=null) {
				review.overall = Integer.parseInt(rowBuffer);
				rowIterator++;
			}
			rowBuffer = checkAndExtractRow(rowIterator, mixed, "experience");
			if(rowBuffer!=null) {
				review.pleasant = rowBuffer.equals("Yes") ? 1 : rowBuffer.equals("No") ? 0 : 2;
				rowIterator++;
			}
			rowBuffer = checkAndExtractRow(rowIterator, mixed, "seated");
			if(rowBuffer!=null) {
				review.persons = Integer.parseInt(rowBuffer);
				rowIterator++;
			}
			rowBuffer = checkAndExtractRow(rowIterator, mixed, "reservations");
			if(rowBuffer!=null) {
				review.reservations = rowBuffer.equals("Yes");
				rowIterator++;
			}
			rowBuffer = checkAndExtractRow(rowIterator, mixed, "return");
			if(rowBuffer!=null) {
				review.wouldReturn = rowBuffer.equals("Yes") ? 1 : rowBuffer.equals("No") ? 0 : 2;
				rowIterator++;
			}
			rowBuffer = checkAndExtractRow(rowIterator, mixed, "wine");
			if(rowBuffer!=null) {
				review.wine = rowBuffer;
				rowIterator++;
			}
			rowBuffer = checkAndExtractRow(rowIterator, mixed, "Credit");
			if(rowBuffer!=null) {
				review.creditCard = rowBuffer.equals("Yes");
				rowIterator++;
			}

			reviews.add(review);
		}

		return reviews;
	}
	
	private String checkAndExtractRow(int rowIndex, Element table, String keyword) {
		Element row = table.select("tr:nth-child(" + rowIndex + ")").get(0);
		if(row.select("td:nth-child(1)").get(0).text().contains(keyword)) {
			if(row.select("td:nth-child(2) img").size()>0) {
				return String.valueOf(row.select("td:nth-child(2) img").size());
			}
			return row.select("td:nth-child(2)").get(0).ownText();
		}
		return null;
	}

}
