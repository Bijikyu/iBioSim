package lpn.parser;

import java.util.ArrayList;

public class Place {

	private String name;

	private Boolean marked;

	private ArrayList<Transition> preset;

	private ArrayList<Transition> postset;

	public Place(String name) {
		this.name = name;
		marked = false;
		preset = new ArrayList<Transition>();
		postset = new ArrayList<Transition>();
	}

	public Place(String name, boolean marking) {
		this.name = name;
		marked = marking;
		preset = new ArrayList<Transition>();
		postset = new ArrayList<Transition>();
	}

	public void addPreset(Transition transition) {
		preset.add(transition);
	}

	public void addPostset(Transition transition) {
		postset.add(transition);
	}
	
	public void removePreset(Transition transition) {
		preset.remove(transition);
	}
	
	public void removePostset(Transition transition) {
		postset.remove(transition);
	}

	public void setMarking(Boolean marking) {
		marked = marking;
	}
	
	public boolean isMarked() {
		return marked;
	}
	
	public boolean isConnected() {
		return (preset.size() > 0 || postset.size() > 0);
	}
	
	public boolean containsPreset(String name) {
		return preset.contains(name);
	}
	
	public boolean containsPostset(String name) {
		return postset.contains(name);
	}
	
	public void setName(String newName) {
		this.name = newName;
	}	
	
	public String getName() {
		return name;
	}
	
	public Transition[] getPreset() {
		Transition[] array = new Transition[preset.size()];
		int i = 0;
		for (Transition t : preset) {
			array[i++] = t;
		}
		return array;
	}
	
	public Transition[] getPostset() {
		Transition[] array = new Transition[postset.size()];
		int i = 0;
		for (Transition t : postset) {
			array[i++] = t;
		}
		return array;
	}
	
	@Override
	public String toString() {
		return name;
	}

	public void changeName(String newName){
		name = newName;
	}

}
