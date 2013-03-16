package de.simiil.jeeh;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;

import org.jfugue.ChannelPressure;
import org.jfugue.Controller;
import org.jfugue.Instrument;
import org.jfugue.KeySignature;
import org.jfugue.Layer;
import org.jfugue.Measure;
import org.jfugue.MidiParser;
import org.jfugue.Note;
import org.jfugue.ParserListener;
import org.jfugue.Pattern;
import org.jfugue.PitchBend;
import org.jfugue.Player;
import org.jfugue.PolyphonicPressure;
import org.jfugue.Tempo;
import org.jfugue.Time;
import org.jfugue.Voice;
import org.jfugue.extras.Midi2JFugue;
import org.junit.Test;



public class MidiSeqParser implements ParserListener {

	HashMap<String, Boolean> voicesToImport = new HashMap<String, Boolean>();
	HashMap<String, Instrument> voicesInstrument = new HashMap<String, Instrument>();
	
	Player player;
	String noteString = "";
	
//	HashMap<Long, LinkedList<Note>> notes = new HashMap<>();
	TreeMap<Timestamp, LinkedList<Note>> notes = new TreeMap<>();
	LinkedList<Note> currentTime = new LinkedList<>();
	private int bpm;
	private boolean currentVoiceImport = false;
	private Voice currentVoice = null;
	
	public Song parse(String fileToMidi) throws InvalidMidiDataException, IOException{
		// Midi2JFugue.printUsage();
		player = new Player();
		MidiParser parse = new MidiParser();
		parse.addParserListener(this);
		File mid = new File(fileToMidi);
		Sequence seq = MidiSystem.getSequence(mid);
		parse.parse(seq);
//		player.playMidiDirectly(new File("resources/moonv2.mid"));
		
		Iterator<Timestamp> iter = notes.keySet().iterator();
		while(iter.hasNext()){
			Timestamp key = iter.next();
			if(notes.get(key).size()==0){
				iter.remove();
				notes.remove(key);
			}
		}
		
//		for(Entry<Timestamp, LinkedList<Note>> e : this.notes.entrySet()){
//			System.out.println(e.getKey());
//			for(Note n : e.getValue()){
//				System.out.println("\t"+n.getMusicString());
//			}
//		}
//		
//		player.playMidiDirectly(mid);
//		player.play(noteString);
		return new Song(notes, bpm);
		
	}

	@Override
	public void voiceEvent(Voice voice) {
		if(!voicesToImport.containsKey(voice.getMusicString())){
			
			System.out.println("Import Channel "+voice.getMusicString()+"("+voicesInstrument.get(voice.getMusicString())+")?");
			Scanner s = new Scanner(System.in);
			voicesToImport.put(voice.getMusicString(), s.nextLine().equals("y"));
		}
		this.currentVoice = voice;
		this.currentVoiceImport = voicesToImport.get(voice.getMusicString());
//		System.out.println(voice.getVoice());
		
	}

	@Override
	public void tempoEvent(Tempo tempo) {
		System.out.println(tempo.getTempo());
		this.bpm = tempo.getTempo();
		
	}

	@Override
	public void instrumentEvent(Instrument instrument) {
		this.voicesInstrument.put(currentVoice.getMusicString(), instrument);
		System.out.println(this.currentVoice.getMusicString()+" is "+instrument.getInstrumentName());
		
	}

	@Override
	public void layerEvent(Layer layer) {
		System.out.println("layer");
		
	}

	@Override
	public void measureEvent(Measure measure) {
		System.out.println("measure");
	}

	@Override
	public void timeEvent(Time time) {
//		if(time.getTime()!=0){
			if(this.notes.get(new Timestamp(time.getTime()))==null){
				this.notes.put(new Timestamp(time.getTime()), new LinkedList<Note>());
			}
			this.currentTime = this.notes.get(new Timestamp(time.getTime()));
//		}
	}

	@Override
	public void keySignatureEvent(KeySignature keySig) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controllerEvent(Controller controller) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void channelPressureEvent(ChannelPressure channelPressure) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void polyphonicPressureEvent(PolyphonicPressure polyphonicPressure) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pitchBendEvent(PitchBend pitchBend) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void noteEvent(Note note) {
		if(note.getDecimalDuration()!=0 && this.currentVoiceImport){
			this.currentTime.add(note);
			noteString += note.getMusicString()+" ";
		}
	}

	@Override
	public void sequentialNoteEvent(Note note) {
		System.out.println("seq");
	}

	@Override
	public void parallelNoteEvent(Note note) {
		System.out.println("par");
		
	}

}
