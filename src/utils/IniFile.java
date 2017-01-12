package utils;

import java.io.*;
import java.util.*;
import de.uib.utilities.logging;

/**
 * IniFile is a class for reading and writing to the windows
 * type of <code>.ini</code> file.
 * The .ini file is a text file with various parts contained in it:
 * <ul>
 * <li>Sections are denoted with the section name in brackets
 * <li>In each section are keys
 * <li>After each key is an equal sign and then the key's value
 * </ul>
 * <p>Here is an example of a typical <code>.ini</code> file:
 * <p><code>[SectionName]</code>
 * <br><code>key1=value</code>
 * <br><code>key2=value</code>
 * <br><code>key3=value</code>
 * <p>Turning the cache on will cause the entire file to stay
 * in memory. Turning the cache off will cause the file to be
 * read line by line each time a setting is retrieved. Also, if
 * caching is turned on, all changes will not be saved until
 * the <code>flush()</code> method is called. When caching is
 * turned off, all writes are immediate.
 *
 * @author    Jeff L. Williams, 
 *   added methods getCompleteText(), setCompleteText(), getHashMapModel(),
 *   and constructors IniFile (LineNumberReader), IniFile (String, int)
 *   by Rupert Roeder
 * @version   %I%, %G%
 * @since     JDK1.2
 * http://www.jeffguy.com/java/
 */


public class IniFile {

  private String      iniFilename;
  private Vector      iniCache = new Vector();
  private boolean     cached = false;
  private boolean     currentlyCached;
  private boolean     onlyInMemory = false;


  /**
   * Passes the .ini filename to the object on creation
   *
   * @param filename  the name of the <code>.ini</code> file to use
   * @since           JDK1.2
   */
  public IniFile(String filename) {
    logging.debugOut( getClass().getName(), 2, " create " + filename);
    iniFilename = filename;
  }

  /**
   * Passes the .ini filename to the object on creation
   *
   * @param filename  the name of the <code>.ini</code> file to use
   * @param cached    whether the file is cached in memory or not
   * @since           JDK1.2
   */
  public IniFile(String filename, boolean cached) {
    logging.debugOut( getClass().getName(), 2, " create " + filename + "," + cached);
    iniFilename = filename;
    this.cached = cached;
    loadCache();
  }
 
 /**
   * Passes a LineNumberReader and handles the ini file structure completely in memory 
   *
   * @param lnr       a LineNumberReader which produces the lines which are interpreted as an ini file
   * @since           JDK1.2
   */
 public IniFile ( LineNumberReader lnr )
 {
   logging.debugOut( getClass().getName(), logging.LEVEL_DEVELOP, " get lines from reader ");
   cached = true;
   onlyInMemory = true;
   makeCache ( lnr );
 }
 
 /**
   * Passes the lines which shall be interpreted as an ini file structure as one string 
   *
   * @param intext    the string that shall be interpreted as an ini file structure
   * @param inMemory  dummy parameter to get an different profile of the constructor
   * @since           JDK1.2
   */
 public IniFile ( String intext, int inMemory )  
 // constructor with dummy parameter inMemory to get a difference to IniFile (String) and IniFile (String, boolean) where String is a file name  
 {
    this ( new LineNumberReader ( new StringReader (intext) ) );
    logging.debugOut (this, logging.LEVEL_DEVELOP, "IniFile constructor we are cached " + cached);
 }
 

  //----------------------------------------------------------------------------

  /**
   * InString finds an string within an existing string
   * starting from a specified location
   *
   * @param   string          the string to be searched
   * @param   search          what to look for in the string
   * @param   startPosition   where to start searching in the string
   * @return  returns an integer that holds the location
   *          to the first occurance found in the string
   *          from the starting position
   * @author  Jeff L. Williams
   */
  private int inString(String string,
                       String search,
                       int startPosition) {

    //Create a variable that will hold the position of the search string
    int pos = 0;

    //If what we are looking for is longer than the original string,
    //then it's not going to be a match and return 0
    if (search.length() > string.length()) {
      pos = 0;

    } else {

      //We can't search past the end of the string so find a stopping point
      int stopPosition = (string.length() - search.length()) + 1;

      //Loop through the positions in the string looking for the criteria
      for (int i = startPosition - 1; i < stopPosition; i++) {
        String sTemp = string.substring(i, i + search.length());

        //If we found a match, break out and return the results
        if (sTemp.compareTo(search) == 0) {
          pos = i + 1;
          break;
        }
      }
    }
    return pos;
  }

//----------------------------------------------------------------------------

  /**
   * Determines if the .ini file is cached in memory or read each
   * time it is called.
   *
   * @param cached    true if the entire .ini file is kept in memory;
   *                  false if the .ini file is not kept in memory
   * @since           JDK1.2
   */
  public void setCachedState(boolean cached) {
    this.cached = cached;
    //this.iniCache.clear();
    //this.currentlyCached = false;
  }

//----------------------------------------------------------------------------

  /**
   * Returns true if the .ini file is cached in memory or false if it
   * is read each time the object is called.
   *
   * @param cached    true if the entire .ini file is kept in memory;
   *                  false if the .ini file is not kept in memory
   * @since           JDK1.2
   */
  public boolean getCachedState() {
    return this.cached;
  }

//----------------------------------------------------------------------------

  /**
   * Sets the name of the <code>.ini</code> file the object is using.
   *
   * @param filename  the name of the <code>.ini</code> file to use
   * @since           JDK1.2
   */
  public void setFilename(String filename) {
    iniFilename = filename;
  }

//----------------------------------------------------------------------------

  /**
   * Returns a String that contains the name of the <code>.ini</code>
   * file currently being used.
   *
   * @return          a String that contains the name of the .ini file
   * @since           JDK1.2
   */
  public String getFilename() {
    return iniFilename;
  }

//----------------------------------------------------------------------------

  /**
   * Returns an integer that contains the value for a specified key.
   *
   * @param section       the name of the section in the <code>.ini</code>
   *                      file.
   * @param key           the name of the key in the <code>.ini</code> file.
   * @param defaultValue  the default value for the key.
   * @return              an integer that contains the value for the
   *                      specified key
   * @since               JDK1.2
   */
  public int getSettingInteger(String section,
                               String key,
                               int defaultValue) {

    String s = new String(String.valueOf(defaultValue));

    // Get the value
    String value = getSetting(section, key, s);

    // Convert it into an integer
    try {
      int i = Integer.parseInt(value);
      return i;
    }
    catch(NumberFormatException e) {
      return 0;
    }

  }

//----------------------------------------------------------------------------

  /**
   * Returns a boolean that contains the value for a specified key.
   *
   * @param section       the name of the section in the <code>.ini</code>
   *                      file.
   * @param key           the name of the key in the <code>.ini</code> file.
   *                      If the value of the key is <code>true</code>,
   *                      <code>on</code>, <code>yes</code>, or
   *                      <code>normal</code>, then the boolean value returned
   *                      would be <code>true</code>. All other values will
   *                      return a boolean value of <code>false</code>.
   * @param defaultValue  the default value for the key.
   * @return              a boolean that contains the value for the specified
   *                      key
   * @since               JDK1.2
   */
  public boolean getSettingBoolean(String section,
                                   String key,
                                   boolean defaultValue) {

    String s;
    if (defaultValue) {
      s = "TRUE";
    } else {
      s = "FALSE";
    }

    // Get the value
    String sTemp   = getSetting(section, key, s);
    String sValue  = sTemp.toUpperCase();
    boolean b;

    // Convert the string into a boolean
    if (sValue.compareTo("TRUE") == 0) {
      b = true;
    }
    else if (sValue.compareTo("ON") == 0) {
      b = true;
    }
    else if (sValue.compareTo("AN") == 0) {
      b = true;
    }
    else if (sValue.compareTo("YES") == 0) {
      b = true;
    }
    else if (sValue.compareTo("JA") == 0) {
      b = true;
    }
    else {
      b = false;
    }

    return b;
  }

//----------------------------------------------------------------------------

  /**
   * Loads the .ini file into an internal vector to cache it in
   * memory.
   *
   * @since           JDK1.2
   */
  private void loadCache()
  {
    try 
    {
     makeCache ( new LineNumberReader( (new FileReader(this.iniFilename)) ) );
    }
    catch (IOException e)
    {
      logging.debugOut(getClass().getName(), 0,e.getMessage());
    }
 
    
  }

  /**
   * Gets lines to cache it in
   * memory.
   * Other threads are not allowed to work on the objects 
   * while this is taking place
   *
   * @since           JDK1.2
   */
  private synchronized void makeCache( LineNumberReader lnr )
  {
    String thisLine;
    
    if (this.cached) {
      try {
        //Clear the current cache
        this.iniCache.clear();
        
        //Loop through each line and add non-blank
        //lines to the Vector
        while ((thisLine = lnr.readLine()) != null)
        {
         iniCache.add(thisLine.trim());
         //System.out.println(thisLine.trim());
        }

        lnr.close();
      }
      catch (IOException e)
      {
        logging.debugOut(getClass().getName(), 0,e.getMessage());
      }
    }
    
  }

//----------------------------------------------------------------------------

  /**
   * Returns a String that contains the value for a specified key.
   *
   * @param section       the name of the section in the <code>.ini</code>
   *                      file.
   * @param key           the name of the key in the <code>.ini</code> file.
   * @param defaultValue  the default value for the key.
   * @return              a String that contains the value for the specified
   *                      key
   * @since               JDK1.2
   */
  public String getSetting(String section, String key, String defaultValue) {

    String currentLine      = new String("");
    String currentSection   = new String("");
    String currentKey       = new String("");
    String currentValue     = new String("");

    //If it's cached
    if (this.cached) {

      //Loop through the vector
      for (int i = 0; i < this.iniCache.size(); i++) {

        //Get the current line of text
        currentLine = this.iniCache.elementAt(i).toString();

        //If it's an empty line
        if (currentLine.trim().length() > 0) {

          //If it's a section
          if (currentLine.trim().substring(0, 1).compareTo("[") == 0) {

            //Get the setion name
            String s = currentLine.trim();
            currentSection = s.substring(1, s.length() - 1);
            currentKey = "";
            currentValue = "";

          //If it's a key
          } else if (this.inString(currentLine, "=", 1) > 1) {

            //Get the key name
            currentKey = currentLine.substring(0,
              this.inString(currentLine, "=", 1) - 1);

            //Get the value
            currentValue = currentLine.substring(currentKey.length() + 1);
          }
        } else {

          //no values for this line
          currentLine = "";
          currentValue = "";
        }

        //If we have a match, stop the loop
        if ((currentSection.compareToIgnoreCase(section) == 0) &&
            (currentKey.compareToIgnoreCase(key) == 0)) {
          break;
        }

      }//next

    //It's not cached
    } else {

      try {

        //Open the file
        LineNumberReader ini = new LineNumberReader(
                                 new FileReader(this.iniFilename));
        boolean doContinue = true;

        synchronized (ini) {
          while (doContinue) {

            //Get the current line of text
            if ((currentLine = ini.readLine()) == null) {
              currentLine = "";
              currentValue = "";
              doContinue = false;
            } else {

              //If it's an empty line
              if (currentLine.trim().length() > 0) {

                //If it's a section
                if (currentLine.trim().substring(0, 1).compareTo("[") == 0) {

                  //Get the setion name
                  String s = currentLine.trim();
                  currentSection = s.substring(1, s.length() - 1);
                  currentKey = "";
                  currentValue = "";

                //If it's a key
                } else if (this.inString(currentLine, "=", 1) > 1) {

                  //Get the key name
                  currentKey = currentLine.substring(0,
                    this.inString(currentLine, "=", 1) - 1);

                  //Get the value
                  currentValue =
                    currentLine.substring(currentKey.length() + 1);
                }
              } else {

                //no values for this line
                currentLine = "";
                currentValue = "";
              }

              //If we have a match, stop the loop
              if ((currentSection.compareToIgnoreCase(section) == 0) &&
                  (currentKey.compareToIgnoreCase(key) == 0)) {
                doContinue = false;
              }

            }

          }//loop
        }//end sync

      }
      catch (IOException e) {
        currentValue = "";
      }
    }

    //If there was no setting found, use the default instead
    if (currentValue.trim().compareTo("") == 0) {
      currentValue = defaultValue;
    }

    //Send back the value
    return currentValue;
  }

//----------------------------------------------------------------------------

  /**
   * Sets a value for a specified key. This temporarily loads the entire
   * <code>.ini</code> file into memory if it is not already cached. If
   * the file is cached, the change is only in memory until the
   * <code>flush()</code> method is called. If the file is not cached,
   * then writes happen immediately.
   *
   * @param section   the section in the <code>.ini</code> file
   * @param key       the name of the key to change
   * @param value     the String value to change the key to
   * @since           JDK1.2
   */
  public void setSetting(String section, String key, String value) {

    //If the .ini file is cached in memory
    if (this.cached) {
      setSettingCached(section, key, value);

    //The .ini file is not cached in memory
    } else {
      this.cached = true;
      this.loadCache(); 
      this.setSettingCached(section, key, value);
      this.flush();
      this.setCachedState(false);
    }
  }

//----------------------------------------------------------------------------

  /**
   * Saves the cached <code>.ini</code> information back out to the
   * <code>.ini</code> file. This method is only available when caching
   * is turned on. Otherwise, it does nothing. If you do not call this
   * method when caching is turned on, your changes will not be saved.
   *
   * @since           JDK1.2
   */
  public synchronized void flush() {
    logging.debugOut(this, logging.LEVEL_DEVELOP, "flush called, isCached " + this.cached + ", onlyInMemory " + onlyInMemory);       
    if (this.cached && !onlyInMemory ) {
      
      logging.debugOut(this, logging.LEVEL_DEVELOP, "do " + this.iniCache.size() + " lines  flush to " + this.iniFilename);

      try {
        //Do not allow other threads to read from the input
        //or write to the output while this is taking place
        FileWriter ini = new FileWriter(this.iniFilename);
        
        // Loop through the vector
        for (int i = 0; i < this.iniCache.size(); i++) {

          //Write out each line
          ini.write(iniCache.elementAt(i).toString());
          logging.debugOut(this, logging.LEVEL_DEVELOP, iniCache.elementAt(i).toString());

          //If this is not the last line of the file
          if (i < (this.iniCache.size() - 1)) {

            //Append a carriage return and line feed
            ini.write(13);
            ini.write(10);

          }
        }

        //Write out and close
        ini.flush();
        ini.close();
      } catch (IOException e) { 
          logging.debugOut(this, logging.LEVEL_ERROR, "IO Error while writing inifile: " + e.toString());  
      }
    }
  }

//----------------------------------------------------------------------------

  /**
   * Sets a value for a specified key in the cached Vector object
   *
   * @param key       the name of the key to change
   * @param value     the String value to change the key to
   * @since           JDK1.2
   */
  private void setSettingCached(String section, String key, String value) {

    String currentLine      = new String("");
    String currentSection   = new String("");
    String currentKey       = new String("");
    String currentValue     = new String("");
    String lastLine         = new String("");
    String lastSection      = new String("");
    boolean madeUpdate      = false;

    //Loop through the vector
    for (int i = 0; i < this.iniCache.size(); i++) {

      //Get the current line of text
      lastLine = currentLine;
      lastSection = currentSection;
      currentLine = this.iniCache.elementAt(i).toString();

      //If it's an empty line
      if (currentLine.trim().length() > 0) {

        //If it's a section
        if (currentLine.trim().substring(0, 1).compareTo("[") == 0) {

          //Get the setion name
          String s = currentLine.trim();
          currentSection = s.substring(1, s.length() - 1);
          currentKey = "";
          currentValue = "";

        //If it's a key
        } else if (this.inString(currentLine, "=", 1) > 1) {

          //Get the key name
          currentKey = currentLine.substring(0,
            this.inString(currentLine, "=", 1) - 1);

          //Get the value
          currentValue = currentLine.substring(currentKey.length() + 1);
        }
      } else {

        //no values for this line
        currentLine = "";
        currentValue = "";
      }

      //If the last section matches
      if (lastSection.compareToIgnoreCase(section) == 0) {

        //Is this the correct key line?
        if (currentKey.compareToIgnoreCase(key) == 0) {

          //Insert the new line
          iniCache.remove(i);
          iniCache.insertElementAt(new String(currentKey + "=" + value), i);
          madeUpdate = true;
          break;

        //Is the section different now?
        } else if (currentSection.compareToIgnoreCase(section) != 0) {

          //Insert the new line and a blank line
          iniCache.insertElementAt(new String(key + "=" + value), i - 1);
          iniCache.insertElementAt(new String(""), i);
          madeUpdate = true;
          break;

        //Is the current line blank?
        } else if (currentLine.trim().length() == 0) {

          //Insert the new line
          iniCache.insertElementAt(new String(key + "=" + value), i);
          madeUpdate = true;
          break;

        //Is this the last line of the file?
        } else if (i == (this.iniCache.size() - 1)) {

          //Add the new line
          iniCache.add(new String(key + "=" + value));
          madeUpdate = true;
          break;

        }
      }
    }//next

    //If we didn't find the section, we'll make a new one
    if (!madeUpdate) {

      //If there's not a blank line, skip one line
      if (currentLine.trim().length() > 0) {

        //Write a blank line
        iniCache.add(new String(""));

      }

      //Write the section
      iniCache.add(new String("[" + section + "]"));

      //Write the new line
      iniCache.add(new String(key + "=" + value));

    }
  }

  
  private HashMap getHashMapCached(String section)
  {
   String currentLine      = new String("");
   String currentSection   = new String("");
   String currentKey       = new String("");
   String currentValue     = new String("");
   boolean inSection = false;
   HashMap myMap = new HashMap();

   //If it's cached
   if (this.cached)
   {
    //Loop through the vector
    for (int i = 0; i < this.iniCache.size(); i++)
    {
     //Get the current line of text
     currentLine = this.iniCache.elementAt(i).toString();
     //If it's not an empty line
     if (currentLine.trim().length() > 0)
     {
      //If it's a section
      if (currentLine.trim().substring(0, 1).compareTo("[") == 0)
      {
       //Get the setion name
       String s = currentLine.trim();
       currentSection = s.substring(1, s.length() - 1);
       currentKey = "";
       currentValue = "";
       // Is it our section ?
       if (currentSection.compareToIgnoreCase(section) == 0)
       {
        inSection = true;
       }
       else
       {
        inSection = false;
       }
      }
      //If it's a key and we are inSection
      else if ((this.inString(currentLine, "=", 1) > 1) && inSection)
      {
       if (! (currentLine.startsWith(";") || currentLine.startsWith("#"))) //It is no comment
       {
        //Get the key name
        currentKey = currentLine.substring(0,this.inString(currentLine, "=", 1) - 1);
        //Get the value
        currentValue = currentLine.substring(currentKey.length() + 1);
        myMap.put(currentKey.trim(),currentValue.trim());
       }
      }
     }
     else  //no values for this line
     {
      currentLine = "";
      currentValue = "";
     }
    }//next
   }// not cached
   //Send back the value
   return myMap;
  }

  public HashMap getHashMap(String section)
  {
   //If the .ini file is cached in memory
   if (this.cached)
   {
    return getHashMapCached(section);
   }
   else //The .ini file is not cached in memory
   {
    this.cached = true;
    this.loadCache();
    HashMap myMap = new HashMap(this.getHashMapCached(section));
    this.flush();
    this.setCachedState(false);
    return myMap;
   }
  }

  private void setHashMapCached(String section, HashMap myMap)
  {
   ArrayList myList = new ArrayList(myMap.keySet());
   String key;
   for (Iterator i = myList.iterator(); i.hasNext(); )
   {
    key = i.next().toString();
    setSettingCached(section,key,myMap.get(key).toString());
   }
  }

  public void setHashMap(String section, HashMap myMap)
  {
   //If the .ini file is cached in memory
   if (this.cached)
   {
    setHashMapCached(section, myMap);
   }
   else //The .ini file is not cached in memory
   {
    this.cached = true;
    this.loadCache();
    this.setHashMapCached(section, myMap);
    this.flush();
    this.setCachedState(false);
   }
  }
  
  /** getHashMapModelCached()
  *  reproduces the complete inifile as a hashmap 
  *   where the keys are the section names and the values are the section as hashmap
  */
  private HashMap getHashMapModelCached()
  {
   String currentLine          = "";
   String currentSectionName   = "";
   String currentKey           = "";
   String currentValue         = "";
   HashMap allSections = new HashMap();
   HashMap aSection = new HashMap();

   //If it's cached  - otherwise do nothing in this method
   if (this.cached)
   {
    //Loop through the vector
    for (int i = 0; i < this.iniCache.size(); i++)
    {
     //Get the current line of text
     currentLine = this.iniCache.elementAt(i).toString();
     
     //If it's not an empty line
     if (currentLine.trim().length() > 0)
     {
      currentKey = "";
      currentValue = "";
 
      //If it's a section
      if (currentLine.trim().substring(0, 1).compareTo("[") == 0)
      {
       //finish the preceding section
       if (!aSection.isEmpty())
       {
         // ensure that we dont overwrite a previous section
         while (allSections.containsKey (currentSectionName))
         {
           currentSectionName = currentSectionName + "1"; 
         }
         
         allSections.put (currentSectionName, aSection);
         aSection = new HashMap();
       }
       
       //Get the new section name
       String s = currentLine.trim();
       currentSectionName = s.substring(1, s.length() - 1);
       
      }
      //If it's a key and we are inSection
      else if (this.inString(currentLine, "=", 1) > 1)
      {
       if (! (currentLine.startsWith(";") || currentLine.startsWith("#"))) //It is no comment
       {
        //Get the key name
        currentKey = currentLine.substring(0,this.inString(currentLine, "=", 1) - 1);
        //Get the value
        currentValue = currentLine.substring(currentKey.length() + 1);
        aSection.put(currentKey.trim(),currentValue.trim());
       }
      }
     }
    }//next
    
    // dont forget the last section
    {
       if (!aSection.isEmpty())
       {
         // ensure that we dont overwrite a previous section
         while (allSections.containsKey (currentSectionName))
         {
           currentSectionName = currentSectionName + "1"; 
         }
         
         allSections.put (currentSectionName, aSection);
         aSection = new HashMap();
       }
    }
    
   }// not cached
   //Send back the value
   return allSections;
  }
  
  /** getHashMapModel()
  *   reproduces the complete inifile as a hashmap 
  *   where the keys are the section names and the values are the section as hashmap
  */
  public HashMap getHashMapModel()
  {
   HashMap result = new HashMap(); 
   //If the .ini file is cached in memory
   if (this.cached)
   {
    result =  getHashMapModelCached();
   }
   else //The .ini file is not cached in memory
   {
    this.cached = true;
    this.loadCache();
    result = getHashMapModelCached();
    this.flush();
    this.setCachedState(false);
   }
   
   return result;
  }
  
  /** returns the content of vector as a string
  */
  
  public String getCompleteText()
  {
     StringBuffer sb; 
     boolean saveIsCached = this.cached;
     
     logging.debugOut (this, logging.LEVEL_DONT_SHOW_IT, "getCompleteText we are cached " + cached);
     //If the .ini file is not cached in memory, get it temporarily
     if (!saveIsCached)
     {
      this.cached = true;
      this.loadCache();
     }
     sb = new StringBuffer();
     
     for (int i = 0; i < this.iniCache.size(); i++) 
     {
          //Add the current line of text
         sb.append(iniCache.elementAt(i).toString());
         sb.append("\n");
     }
     
     if (!saveIsCached)
     {
      this.flush();
      this.setCachedState(false);
     }
     
     return new String(sb);
  }
  
  
  /** Replaces current content of cache by lines presented in a String s 
  *   and flushes it
  */
  public void setCompleteText( String s)
  {
     boolean saveIsCached = this.cached;
     
     //If the .ini file is not cached in memory, handle it temporarily as cached - otherwise the flush method would not work 
     if (!saveIsCached)
     {
      this.cached = true;
     }
     
     makeCache ( new LineNumberReader( new StringReader (s) ) );
     
     //this.flush(); 
     
     if (!saveIsCached)
     {
      this.setCachedState(false);
     }
  }
  
//----------------------------------------------------------------------------

public static void main (String[] args)
  {
    System.out.println ("Testing IniFile ");
    String testS = "[test1]\n eintrag1=Wert1\n [test2]\n eintrag2=wert2\neintrag3=wert3\n"; 
    IniFile ini;
    
    if (args != null && args.length > 0)
    {
      System.out.println ("Usage IniFile Strings");
      testS = "";
      for (int i = 0; i < args.length; i++)
      {
        testS = testS + "\n" + args[i];
      }
      System.out.println ("Teststring" + testS );
      ini = new IniFile (new LineNumberReader ( new StringReader (  testS )));
    }
    else
    {
      System.out.println ("TestString " + testS );
      ini = new IniFile (new LineNumberReader ( new StringReader (testS) ));
      
    }
    
    System.out.println ("===========================");
    
    System.out.println ("Input configuration as hash map");
    
    System.out.println (ini.getHashMapModel());
    //System.out.println (ini.getCompleteText());
    
    ini.setSetting ("test2", "eintrag3", "wert3a");
    ini.setSetting ("test2a", "eintrag3", "wert3a");
    ini.setSetting ("test2a", "eintrag3a", "wert3a");
    
    
    System.out.println ("After settings ");
    System.out.println ("it should be set: \n" +
    "section \"test2\" variable \"eintrag3\" value \"wert3a\"  \n" +
    "section \"test2a\" variable \"eintrag3\" value \"wert3a\"  \n" +
    "section \"test2a\" variable \"eintrag3a\" value \"wert3a\"  "); 
    
    System.out.println (ini.getHashMapModel());
    
    
   
  }

}
