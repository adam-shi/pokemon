<html>
<body>

<?php
ini_set('display_errors',1); 
error_reporting(E_ALL);
set_time_limit(120);

define("url_base", "replay.pokemonshowdown.com/search/?output=html&user=");
define("replay_base", "replay.pokemonshowdown.com");

class Pokemon
{
	public $species;
	public $moves = array();
}

class Team 
{
	public $pokemon = array();
	public $movesets = array();

	public function add_pokemon($p) 
	{
		$this->pokemon[$p] = 1; 
	}

	public function add_moveset($name, $species) 
	{
		if (!array_key_exists($name, $this->movesets)) {
			$this->movesets[$name] = new Pokemon;
			$this->movesets[$name]->species = $species; 
		}
	}

	public function add_move($name, $move)
	{
		($this->movesets[$name]->moves)[$move] = 1;
	}

	public function mega_evolve($name, $evolution)
	{
		unset($this->pokemon[$this->movesets[$name]->species]);
		$this->movesets[$name]->species = $evolution;
		$this->pokemon[$evolution] = 1;
	}

	public function print_team($team_preview) 
	{
		$count = 0;
		if ($team_preview) {
			foreach (array_keys($this->pokemon) as $poke) {
				echo $poke;
				if ($count < count($this->pokemon) - 1) {
					echo " / ";
				}
				$count++;
			}
		} else {
			foreach ($this->movesets as $poke) {
				echo $poke->species;
				if ($count < count($this->movesets) - 1) {
					echo " / ";
				}
				$count++;
			}
		}
		

		echo "<br>";

		foreach ($this->movesets as $poke) {
			$move_count = 0;

			echo $poke->species . ": [";
			foreach (array_keys($poke->moves) as $move) {
				echo $move;
				if ($move_count < count($poke->moves) - 1) {
					echo ", ";
				}
				$move_count++;
			}
			echo "]<br>";
		}
	}
}

function load_url($url)
{
	$options = array(
		CURLOPT_RETURNTRANSFER => true,
		CURLOPT_HEADER => false,
		CURLOPT_USERAGENT => "Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:45.0) Gecko/20100101 Firefox/45.0",
		CURLOPT_CONNECTTIMEOUT => 120,
		CURLOPT_TIMEOUT => 120,

	);

	$ch = curl_init($url);
	curl_setopt_array($ch, $options);
	$content = curl_exec($ch);
	curl_close($ch);

	return $content;
}

function find_battles($content) 
{
	$dom = new DOMDocument;
	$dom->loadHTML($content);

	foreach ($dom->getElementsByTagName('a') as $link) {
		if ($link->hasAttribute('href')) {
			$battle_link = $link->getAttribute('href');
			if (strpos($battle_link, ("/smogtours-" . $_POST["tier"])) === 0
				|| strpos($battle_link, ("/" . $_POST["tier"])) === 0) {
				$GLOBALS['battles'][] = $battle_link;
			}
		} 
	}
}

function get_results($username) 
{
	$pagenum = 1;
	$url_params = $username . "&page=" . $pagenum;
	$content = load_url(url_base . $url_params);
	while (strpos($content, "No results found") === false) {
		find_battles($content);

		$pagenum++;
		$url_params = $username . "&page=" . $pagenum;
		$content = load_url(url_base . $url_params);
	}
}


function parse_battle($battle, $team_preview) 
{
	echo replay_base . $battle;
	echo "<br>";
	$battle_site = load_url(replay_base . $battle);
	$dom = new DOMDocument;
	$dom->loadHTML($battle_site);

	// Retrieve the battle log from the HTML document.
	$battle_log;
	foreach ($dom->getElementsByTagName('script') as $elem) {
		if ($elem->hasAttribute('class') && $elem->getAttribute('class') === 'log') {
			$battle_log = $elem->textContent;
			break;
		} 
	}

	// Break the log apart into lines.
	$battle_lines = explode("\n", $battle_log);
	
	$player = "p1";
	$team = new Team;

	foreach($battle_lines as $line) {
		if (strpos(strtolower($line), "|player|p2|" . strtolower($_POST["username"]) . "|") !== false) {
			$player = "p2";
		} else if (strpos($line, "|poke|" . $player) !== false) {
			strtok($line, ",|");
			strtok(",|");

			$team->add_pokemon(strtok(",|"));
		}  else if (strpos($line, "|switch|" . $player) !== false ||
					strpos($line, "|drag|" . $player) !== false) {
			strtok($line, ",|:");
			strtok(",|:");

			$team->add_moveset(trim(strtok(",|:")), strtok(",|:"));
		}  else if (strpos($line, "|move|" . $player) !== false) {
			strtok($line, ",|:");
			strtok(",|:");

			$team->add_move(trim(strtok(",|:")), strtok(",|:"));
		} else if (strpos($line, "|detailschange|" . $player) !== false) {
			strtok($line, ",|:");
			strtok(",|:");

			$team->mega_evolve(trim(strtok(",|:")), strtok(",|:"));
		}
	}

	$team->print_team($team_preview);
}


$battles = array();

get_results($_POST["username"]);

$team_preview = false;

if ($_POST["tier"] === "ou" || $_POST["tier"] === "uu" ||
	$_POST["tier"] === "ru" || $_POST["tier"] === "nu") {
	$team_preview = true;
}

foreach ($battles as $battle) {
	parse_battle($battle, $team_preview);

	echo "<br>";
} 

?>
</body>
</html>