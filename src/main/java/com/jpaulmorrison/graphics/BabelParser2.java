package com.jpaulmorrison.graphics;

/**
 * This class is based on a (language) parsing technique called Babel, that I
 * learned in England many years ago! This class definitely has an internal
 * state, so all users must create an instance of it. This class is almost the
 * same as the original JavaFBP BabelParser class, except that it simply returns
 * false when requested to get more data...
 **/

public class BabelParser2 {

	static final String copyright = "Copyright 1999,... 2020, J. Paul Morrison.  At your option, you may copy, "
			+ "distribute, or make derivative works under the terms of the Clarified Artistic License, "
			+ "based on the Everything Development Company's Artistic License.  A document describing "
			+ "this License may be found at http://www.jpaulmorrison.com/fbp/artistic2.htm. "
			+ "THERE IS NO WARRANTY; USE THIS PRODUCT AT YOUR OWN RISK.";

	private char input[];
	private char output[];
	private int inputIndex;
	private int outputIndex;
	private int endOfInput;
	Integer errNo;

	/**
	 * BabelParser constructor.
	 */
	public BabelParser2(String inStr, Integer errNo) {
		super();
		inputIndex = 0;
		outputIndex = 0;
		output = new char[65536];
		input = inStr.toCharArray();
		endOfInput = inStr.length();
		this.errNo = errNo;

	}
	/**
	 * Erase output stream
	 */
	public void eraseOutput() {

		outputIndex = 0;

	}
	public boolean finished() {
		return inputIndex >= endOfInput;
	}
	/**
	 * Get another input Packet - return false if no more
	 */
	boolean getMoreInput() {
		return false;
	}
	/**
	 * Take output generated by syntax scan, and return as String Output index
	 * is then set back to zero.
	 * 
	 * @return java.lang.String
	 */
	public String getOutStr() {
		String sym = "";
		if (outputIndex > 0) {
			try {
				//System.out.println("\"" + (new String(output)).substring(0, 60) + "\"");
				//System.out.println(outputIndex); 
				sym = new String(output, 0, outputIndex);
			} catch (NullPointerException r) {
				System.out.println("RuntimeException:" + r);

			} catch (IndexOutOfBoundsException e) {
				System.out.println("RuntimeException:" + e);

			}
			outputIndex = 0;
		}
		return sym;
	}
	/**
	 * This macro compares against a given character. Scanning is continuous
	 * from the end of one incoming packet to the start of the next one. End of
	 * input results in a false result - i.e. end of input is considered to not
	 * match _any_ test character.
	 */
	public boolean tc(char x) {
		while (inputIndex >= endOfInput) {
			if (!getMoreInput())
				return false;
		}
		if (input[inputIndex] != x)
			return false;
		if (outputIndex >= output.length) {
			errNo = Integer.valueOf(1);
			return false;
		}
		output[outputIndex] = input[inputIndex];
		outputIndex++;
		inputIndex++;
		return true;
	}
	/**
	 * Same as tc(char), but with modification (must be 'i', 'n' or 'o') ('n' is
	 * equivalent to old Babel 'IO' - I- AND O-modification)
	 * 
	 * @param x
	 *            char
	 * @param mod
	 *            char
	 */

	public boolean tc(char x, char mod) {

		while (inputIndex >= endOfInput) {
			if (!getMoreInput())
				return false;
		}
		if (input[inputIndex] != x)
			return false;
		if (mod != 'o' && mod != 'n') {
			output[outputIndex] = input[inputIndex];
			outputIndex++;
		}
		if (mod != 'i' && mod != 'n')
			inputIndex++;

		return true;
	}
	
	/**
	 * This macro compares against a given string. Scanning is continuous
	 * from the end of one incoming packet to the start of the next one. End of
	 * input results in a false result - i.e. end of input is considered to not
	 * match _any_ test character.
	 * 
	 * @param s
	 *            String	
	 */
	public boolean tcl(String s) {
		for (int i = 0; i < s.length(); i++) {
			while (inputIndex >= endOfInput) {
				if (!getMoreInput())
					return false;
			}
			if (input[inputIndex] != s.charAt(i))
				return false;
			if (outputIndex >= output.length) {
				errNo = Integer.valueOf(1);
				return false;
			}
			output[outputIndex] = input[inputIndex];
			outputIndex++;
			inputIndex++;
		}
		return true;
	}
	/**
	 * Same as tcl(char), but with modification (must be 'i', 'n' or 'o') ('n' is
	 * equivalent to old Babel 'IO' - I- AND O-modification)
	 * 
	 * @param s
	 *            String
	 * @param mod
	 *            char
	 */

	public boolean tcl(String s, char mod) {
		for (int i = 0; i < s.length(); i++) {
			while (inputIndex >= endOfInput) {
				if (!getMoreInput())
					return false;
			}
			
			if (input[inputIndex] != s.charAt(i))
				return false;
			if (mod != 'o' && mod != 'n') {
				output[outputIndex] = input[inputIndex];
				outputIndex++;
			}
			if (mod != 'i' && mod != 'n')
				inputIndex++;
		}
		return true;
	}
	/**
	 * Test for blank, CR or tab, but with modification (must be 'i', 'n' or 'o')
	 * ('n' is equivalent to old Babel 'IO' - I- and O-modification)
	 * (the only blank tests in DiagramBuilder logic are all mod'd)
	 * 	 
	 * @param mod
	 *            char
	 */

	public boolean tb(char mod) {

		while (inputIndex >= endOfInput) {
			if (!getMoreInput())
				return false;
		}
		if (input[inputIndex] != ' ' &&
				input[inputIndex] != '\t' &&
				input[inputIndex] != '\r' &&
				input[inputIndex] != '\n')
			return false;
		
		if (mod != 'o' && mod != 'n') {
			output[outputIndex] = input[inputIndex];
			outputIndex++;
		}
		if (mod != 'i' && mod != 'n')
			inputIndex++;
		return true;
	}
	/**
	 * This macro compares against a number (figure). Scanning is continuous
	 * from the end of one incoming packet to the start of the next one. End of
	 * input results in a false result.
	 */
	public boolean tf() {
		while (inputIndex >= endOfInput) {
			if (!getMoreInput())
				return false;
		}
		if (!Character.isDigit(input[inputIndex]))
			return false;
		if (outputIndex >= output.length) {
			errNo = Integer.valueOf(1);
			return false;
		}
		output[outputIndex] = input[inputIndex];
		outputIndex++;
		inputIndex++;
		return true;
	}
	/**
	 * Same as tf(), but with modification (must be 'i', 'o' or 'n') ('n' is
	 * equivalent to old Babel 'IO' - I- and O-modification)
	 * 
	 * @param mod
	 *            char
	 */

	public boolean tf(char mod) {

		while (inputIndex >= endOfInput) {
			if (!getMoreInput())
				return false;
		}
		if (!Character.isDigit(input[inputIndex]))
			return false;
		if (mod != 'o' && mod != 'n') {
			output[outputIndex] = input[inputIndex];
			outputIndex++;
		}
		if (mod != 'i' && mod != 'n')
			inputIndex++;
		return true;
	}
	/**
	 * This macro compares against a letter. Scanning is continuous from the end
	 * of one incoming packet to the start of the next one. End of input results
	 * in a false result.
	 */
	public boolean tl() {
		while (inputIndex >= endOfInput) {
			if (!getMoreInput())
				return false;
		}
		if (!Character.isLetter(input[inputIndex]))
			return false;
		if (outputIndex >= output.length) {
			errNo = Integer.valueOf(1);
			return false;
		}
		output[outputIndex] = input[inputIndex];
		outputIndex++;
		inputIndex++;
		return true;
	}
	/**
	 * Same as tl(), but with modification (must be 'i', 'o' or 'n') ('n' is
	 * equivalent to old Babel 'IO' - I- and O-modification)
	 * 
	 * @param mod
	 *            char
	 */

	public boolean tl(char mod) {

		while (inputIndex >= endOfInput) {
			if (!getMoreInput())
				return false;
		}
		if (!Character.isLetter(input[inputIndex]))
			return false;
		if (mod != 'o' && mod != 'n') {
			output[outputIndex] = input[inputIndex];
			outputIndex++;
		}
		if (mod != 'i' && mod != 'n')
			inputIndex++;
		return true;
	}
	/**
	 * Babel 'universal comparator' - it is always true, unless we are at end of
	 * file
	 */
	public boolean tu() {
		while (inputIndex >= endOfInput) {
			if (!getMoreInput())
				return false;
		}

		if (outputIndex >= output.length) {
			errNo = Integer.valueOf(1);
			return false;
		}
		output[outputIndex] = input[inputIndex];
		outputIndex++;
		inputIndex++;
		return true;

	}
	/**
	 * Same as tu(), but with modification (must be 'i', 'o' or 'n') ('n' is
	 * equivalent to old Babel 'IO' - I- and O-modification)
	 * 
	 * @param mod
	 *            char
	 */
	public boolean tu(char mod) {
		while (inputIndex >= endOfInput) {
			if (!getMoreInput())
				return false;
		}
		if (mod != 'o' && mod != 'n') {
			output[outputIndex] = input[inputIndex];
			outputIndex++;
		}
		if (mod != 'i' && mod != 'n')
			inputIndex++;
		return true;
	}
	/**
	 * Write one char out to output stream
	 * 
	 * @param x
	 *            char
	 */
	public boolean w(char x) {
		if (outputIndex >= output.length) {
			errNo = Integer.valueOf(1);
			return false;
		}
		output[outputIndex] = x;
		outputIndex++;
		return true;
	}
	
	/**
	 * Back up one char in input stream
	 *  
	 */
	public boolean bsp() {
		
		inputIndex--;
		return true;
	}
}
