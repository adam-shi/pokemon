package pkmn;

import java.util.*;

public class Pokemon {
	String species;
	Set<String> moves;
	
	public Pokemon (String name) {
		species = name;
		moves = new HashSet<String>();
	}
	
}
