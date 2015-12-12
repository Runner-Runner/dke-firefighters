package main;

/**
 * Own tupel implementation, necessary because batch runs would not work with the official Java Tupel class.
 * 
 * @author Daniel
 *
 * @param <FIRST>
 * @param <SECOND>
 */
public class Pair<FIRST, SECOND>
{
	private final FIRST first;
	private final SECOND second;

	public Pair(FIRST first, SECOND second)
	{
		this.first = first;
		this.second = second;
	}

	public FIRST getFirst()
	{
		return first;
	}

	public SECOND getSecond()
	{
		return second;
	}

}