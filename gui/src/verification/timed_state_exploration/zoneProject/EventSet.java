package verification.timed_state_exploration.zoneProject;

import java.util.ArrayList;
import java.util.Iterator;

import lpn.parser.Transition;

/**
 * An EventSet represents a transition to fire or a set of inequalities that must
 * fire together. 
 * 
 * @author Andrew N. Fisher
 *
 */
public class EventSet extends Transition implements Iterable<Event>{

	/*
	 * Abstraction Function : 
	 */
	
	
	/*
	 * Representation Invariant :
	 * Exactly one of the fields '_transition' or '_inequalities' should be non-null.
	 * Testing for null is how this class determines whether it represents a 
	 * Transition or a set of inequalities.
	 */
	
	// A variable indicating whether we are a transition or a set of inequalities
	// may not be need since we could test for null or non-null.
	// Indicates whether this EventSet is a transitions or a set of inequalities.
//	boolean _isTransition;
	
	
	// The transition to fire.
	Transition _transition;
	
	// The set of inequalities.
	ArrayList<InequalityVariable> _inequalities;
	
	/**
	 * Determines whether this EventSet represents a Transition.
	 * @return
	 * 		True if this EventSet represents a Transition; false otherwise.
	 */
	public boolean isTransition(){
		return _transition != null;
	}
	
	/**
	 * Determines whether the EventSet represents a rate event.
	 * @return
	 * 		True if this EventSet represents a rate event; flase otherwise.
	 */
	public boolean isRate(){
		return false;
	}
	
	public void insert(Event e){
		
	}
	
	public Iterator<Event> iterator(){
		return null;
	}
	
	public EventSet clone(){
		return null;
	}
	
	public void remove(Event e){
		
	}
	
	public void add(Event e){
		
	}
	
	public int size(){
		return 0;
	}
	
	public boolean isEmpty(){
		return false;
	}
}
