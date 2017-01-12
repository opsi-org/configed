import java.text.Collator;

public class Collatortest 
{	
	public Collatortest()
	{
	}
	
	public static void main(String[] args)
	{
		Collator myColl = Collator.getInstance();
		
		
		if (args.length < 2)
			System.out.println ( " erwartet zwei Parameter für den Alpha-Vergleich , 3. Parameter strength "  );
		else
		{
			int strength = Collator.PRIMARY;
			
			if (args.length > 2)
			{
				if (args[2].equals("2"))
					strength = Collator.SECONDARY;
				else if (args[2].equals("3")) 
					strength = Collator.TERTIARY;
				else if (args[2].equals("4"))
					strength = Collator.IDENTICAL;
			}
			
			myColl.setStrength(strength);
				
			if (myColl.compare(args[0], args[1]) == 0 )
				System.out.println ( args[0] +  " equal to " + args[1]);
		
			else if (myColl.compare(args[0], args[1]) < 0 )
				
				System.out.println ( args[0] +  " less than " + args[1]);
			
			else
				System.out.println ( args[0] +  " greater than " + args[1]);
		}
		
	}
	
}
