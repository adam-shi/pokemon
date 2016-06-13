/* 
 * Takes a username as input and outputs all teams used into an output 
 * file, separating by metagame.
 * 
 * Output can be used to generate counterteams with program by Stathakis
 * (hopefully).
 * 
 * Written by Adam Shi (uragg).
 *  */

package pkmn;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;

import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.*;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

public class EastScouterHTML {

	public static final String psURL = "http://replay.pokemonshowdown.com";
	
	public enum Tier {
		ORAS, BW, DPP, none
	}
	
	static Tier tier = Tier.none;
	
	static Team parseLog(String log, String username) {
		StringTokenizer st = new StringTokenizer(log, "\n");

		String line;
		String player = "uninitialized";
		Team team = new Team();
		
		while (st.hasMoreTokens()) {
			line = st.nextToken();
			
			if (line.startsWith("|player")) {
				if (line.length() > 11 + username.length() && line.substring(11, 11 + username.length()).equalsIgnoreCase(username)) {
					player = line.substring(8, 10);
				}
			} else if (line.startsWith("|poke|" + player)) {
				StringTokenizer lineST = new StringTokenizer(line, ",|");
				lineST.nextToken();
				lineST.nextToken();
				team.addName(lineST.nextToken());
			} else if (line.startsWith("|switch|" + player) || line.startsWith("|drag|" + player)) {
				StringTokenizer lineST = new StringTokenizer(line, ",|:");
				lineST.nextToken();
				lineST.nextToken();
				
				team.addPokemon(lineST.nextToken().trim(), lineST.nextToken());
			} else if (line.startsWith("|move|" + player)) {
				StringTokenizer lineST = new StringTokenizer(line, ",|:");
				lineST.nextToken();
				lineST.nextToken();
				
				team.addMove(lineST.nextToken().trim(), lineST.nextToken());
			} else if (line.startsWith("|detailschange|" + player)) {
				StringTokenizer lineST = new StringTokenizer(line, ",|:");
				lineST.nextToken();
				lineST.nextToken();
				
				team.megaEvolve(lineST.nextToken().trim(), lineST.nextToken());
			}
		}
		
		return team;
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		
		
		Charset cs = Charset.forName("UTF-8");
		
		Path inputPath = Paths.get("input");
		List<String> usernameList = Files.readAllLines(inputPath, cs);
		
		RemoteWebDriver driver = new FirefoxDriver();
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		
		// launch Firefox and go to replay site.
		driver.get(psURL);
		driver.manage().window().maximize();
		
		for (String username : usernameList) {
			
			String outputFile = username + "-teams-moves.txt";
			Path movesetFile = Paths.get(outputFile);
			Path teamOnlyFile = Paths.get(username + "-teams.txt");
			Path teamVisualFile = Paths.get(username + "-zamrock.txt");
			
			Files.deleteIfExists(movesetFile);
			Files.createFile(movesetFile);
			Files.deleteIfExists(teamOnlyFile);
			Files.createFile(teamOnlyFile);
			Files.deleteIfExists(teamVisualFile);
			Files.createFile(teamVisualFile);
			
			// inputs username input into search box.
			WebElement usernameElement = driver.findElementByName("user");
			usernameElement.sendKeys(username);
			
			// clicks "Search" button to search for replays by user.
			WebElement buttonElement = driver.findElement(By.xpath("//button[@type='submit']"));
			buttonElement.click();
		
			Thread.sleep(1000);
			
			// clicks "More" repeatedly to get all replays to show up.
			jse.executeScript("scroll(0, 400)");
			
			while (true) {
				try {
					WebElement moreButton = driver.findElement(By.xpath("//button[@name='moreResults']"));
					moreButton.click();
					Thread.sleep(500);
					jse.executeScript("scroll(0, 600)");
				} catch (NoSuchElementException e) {
					break;
				}
			}
				
			// find OU games to look at teams.
			// battles uploaded via smogtours.
			List<WebElement> battles = driver.findElements(By.cssSelector("a[href*='-ou-'],a[href^='/ou-']"));
			// battles not from smogtours but uploaded.
			List<WebElement> otherBattles = driver.findElements(By.cssSelector("a[href^='/ou-']"));
				
			Element date;
			int numDate;
			
			String battleLink;
			Document doc;
			String battleText;
			
			List<Team> teams = new ArrayList<Team>();
			Team tempTeam;
			
			for (WebElement battle : battles) {
				battleLink = battle.getAttribute("href");
				doc = Jsoup.connect(battleLink).userAgent("Mozilla").get();
				
				date = doc.select("small[class='uploaddate']").first();
				numDate = Integer.parseInt(date.attr("data-timestamp"));
				
				// only look at logs after jan 1.
				if (numDate < 1456774105) {
					break;
				}
				
				battleText = doc.select("script[class='log']").first().html();
				
				tempTeam = parseLog(battleText, username);
				
				if (teams.contains(tempTeam)) {
					int index = teams.lastIndexOf(tempTeam);
					Team oldTeam = teams.remove(index);
					
					tempTeam = tempTeam.coalesce(oldTeam);
				} 
				teams.add(tempTeam);
				
			}
			
			for (Team t : teams) {
				if (t.pokemon.contains("Hoopa-Unbound")) {
					continue;
				}

				
				List<String> movesetList = t.writeMovesets();
				Files.write(movesetFile, movesetList, cs, StandardOpenOption.APPEND);
				
				List<String> teamOnlyList = t.writeTeam();
				Files.write(teamOnlyFile, teamOnlyList, cs, StandardOpenOption.APPEND);
				
				List<String> zamrockList = t.writeVisual();
				Files.write(teamVisualFile, zamrockList, cs, StandardOpenOption.APPEND);
			}
			
			driver.navigate().to(psURL);
		}
		
		driver.quit();
	}

}
