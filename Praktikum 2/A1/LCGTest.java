
public class LCGTest {
	public static void main(String[] args) {
		LCG test = new LCG(5);
		for (int i = 0; i < 256; i++) {
			System.out.println(test.nextInt() & 0x000000FF);
		}
	}
}
