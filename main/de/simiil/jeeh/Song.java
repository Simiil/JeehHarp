package de.simiil.jeeh;

import java.util.LinkedList;
import java.util.TreeMap;

import org.jfugue.Note;

public class Song {
	private int bpm;
	TreeMap<Timestamp, LinkedList<Note>> notes;
	private String interpret;
	private String title;
	
	public Song(TreeMap<Timestamp, LinkedList<Note>> notes, int bpm) {
		this.bpm = bpm;
		this.notes = notes;
	}
	
	public String getInterpret() {
		return interpret;
	}

	public void setInterpret(String interpret) {
		this.interpret = interpret;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getBpm() {
		return bpm;
	}

	public void setBpm(int bpm) {
		this.bpm = bpm;
	}

	public TreeMap<Timestamp, LinkedList<Note>> getNotes() {
		return notes;
	}

	public void setNotes(TreeMap<Timestamp, LinkedList<Note>> notes) {
		this.notes = notes;
	}
	
	
	
	
}
