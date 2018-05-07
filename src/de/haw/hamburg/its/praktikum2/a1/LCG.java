package de.haw.hamburg.its.praktikum2.a1;


public class LCG {
	
	private final long a = 129;
	private final long b = 907633385;
	private final long m = (long) Math.pow(2.0, 32);
	private long x0;
	
	public LCG(long x0) {
		
		if ((x0 >= m) || (x0 < 1)) {
			throw new IllegalArgumentException();
		}
				
		this.x0 = x0;
	}
	
	public int nextInt() {
		x0 = ((a*x0 + b) % m);
		return (int) x0;
	}
	
}
