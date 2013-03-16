package de.simiil.jeeh;

public class Timestamp implements Comparable<Timestamp>{
	private long time;
	
	public Timestamp(long time) {
		this.time = time;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Timestamp){
			return this.time <= ((Timestamp)obj).getTime() + 5 && this.time >= ((Timestamp)obj).getTime() - 5;
		}
		return false;
	}

	@Override
	public int compareTo(Timestamp o) {
		if(this.equals(o)){
			return 0;
		}
		return (int) (this.time - o.time);
	}
}
