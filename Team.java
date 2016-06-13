package pkmn;

import java.util.*;

public class Team {
	
	Set<String> pokemon;
	Map<String, Pokemon> movesets;
	
	void addName(String name) {
		pokemon.add(name);
	}
	
	void addPokemon(String nick, String pokemon) {
		if (movesets.containsKey(nick)) {
			return;
		}
		movesets.put(nick, new Pokemon(pokemon));
	}
	
	void addMove(String nick, String move) {
		Pokemon poke = movesets.get(nick);
		poke.moves.add(move);
	}
	
	void megaEvolve(String nick, String evo) {
		Pokemon poke = movesets.get(nick);
		pokemon.remove(poke.species);
		poke.species = evo;
		pokemon.add(evo);
	}
	
	Team coalesce(Team t) {
		Pokemon poke1;
		Pokemon poke2;
		for (String s1 : movesets.keySet()) {
			poke1 = movesets.get(s1);
			
			for (String s2 : t.movesets.keySet()) {
				poke2 = t.movesets.get(s2);
				if (poke1.species.equals(poke2.species)) {
					for (String move : poke2.moves) {
						poke1.moves.add(move);
					}
				}
			}
		}
		
		return this;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Team) {
			Team t = (Team) o;
			return t.pokemon.equals(this.pokemon);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return pokemon.hashCode();
	}
	
	List<String> writeMovesets() {
		List<String> l = new ArrayList<String>();
		
		l.add("Pokemon:");
		int count = 0;
		StringBuilder sb = new StringBuilder();
		for (String s : pokemon) {
			sb.append(s);
			if (count < 5) {
				sb.append(" / ");
			}
			count++;
		}
		l.add(sb.toString());
		l.add("Sets:");
		
		StringBuilder sb2;
		for (String nick : movesets.keySet()) {
			sb2 = new StringBuilder();
			
			sb2.append(movesets.get(nick).species);
			sb2.append(": ");
			sb2.append(movesets.get(nick).moves);
			l.add(sb2.toString());
		}
		l.add("");
		l.add("");
		return l;
	}
	
	List<String> writeTeam() {
		List<String> l = new ArrayList<String>();
		int count = 0;
		StringBuilder sb = new StringBuilder();
		
		for (String s : pokemon) {
			sb.append(s);
			if (count < 5) {
				sb.append(" / ");
			}
			count++;
		}
		l.add(sb.toString());
		
		return l;
	}
	
	List<String> writeVisual() {
		List<String> l = new ArrayList<String>();
		l.add("=== - ===");
		
		for (String s : pokemon) {
			l.add("");
			l.add(s);
			l.add("-");
		}
		
		l.add("");
		l.add("");
		
		
		return l;
	}
	
	public Team() {
		pokemon = new HashSet<String>();
		movesets = new HashMap<String, Pokemon>();
	}
	
}
