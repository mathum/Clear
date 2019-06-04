package com.clearcrane.logic;

public class PrisonOrganism extends Organism {

	protected Organism workOrgan;
	
	public Organism getWorkOrgan(){
		return workOrgan;
	}
	
	
	/*
	 * is ready to play or is called to play
	 */
	public boolean isAwake(){
		return false;
	}
}
