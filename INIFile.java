/*
 * Feel free to do anything at all with this class.  There is, of course, no warranty
 * or guarantee with this class.
 */

// Make more robust (ie. indexOf checking for -1)
// Add create new file
// JK1'2000 : ajouter les get avec valeur par défaut
import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

/**
 * This class allows the manipulation of the data in a standard INI file
 * without having to worry about the details.
 * The INI file format is as follows:
 * <pre>
 *      ; Comment
 *      [Section 1]
 *      key 1=value 1
 *      key 2=value 2
 *
 *      [Section 2]
 *      key 3=value 3
 * </pre>
 * <ul>
 * <li>Commented lines can appear anywhere in the file when reading
 * but INIFile currently does not allow comments to be written out.</li>
 * <li>Everything to the left of the first '=' will be the key</li>
 * <li>Everything to the right of the first '=' will be the value</li>
 * <li>Extra lines are not necessary but are supported</li>
 * <li>The entire file is cached on construction and needs to be saved explicitly.</li>
 * </ul>
 * @author Christopher Kings-Lynne
 * @author chriskl@tartarus.uwa.edu.au
 * @version 1.0 for JDK 1.1.x
 *
 * Please send questions, comments and suggestions to the above email address.
 *
 * This class is freeware.  However, if you wish to distribute this
 * class commercially, in a compilation or bundled, then please let me know.
 */

public class INIFile extends File {

  private Hashtable sections;
  private BufferedReader dis;
  private String line = "", filename = "";

  /**
   * Creates a new reference to an INI file.  Reads in all the INI data.
   * @param file The filename of the INI file.
   * @exception IOException if anything goes wrong during read.
   */
  
  public INIFile(String file, boolean create) {
    super(file);
    filename = file;
    sections = new Hashtable();
  }//end constructeur
  

  
  
  /**
   * Creates a new reference to an INI file.  Reads in all the INI data.
   * @param file The filename of the INI file.
   * @exception IOException if anything goes wrong during read.
   */
  
  public INIFile(String file /*, boolean create_if_file_not_found */) throws IOException {
    super(file);
    filename = file;    
    sections = new Hashtable();
    // try {
      dis = new BufferedReader(new InputStreamReader(new FileInputStream(this))); 
      line = dis.readLine();
      while(line!=null) {
        while(line!=null && !line.startsWith("[")) line = dis.readLine();
        readSection();
      }//end while
    //}
    //catch (FileNotFoundException e) {
    //  // ne rien faire. Le fichier est considéré comme vide => pas d'entrées
    //  if (!create_if_file_not_found) {
    //    throw new FileNotFoundException(e);
    //  }//end if
    //}
  }
 
  /**
   * Creates a new reference to an INI file.  Reads in all the INI data.
   * @param file The filename of the INI file.
   * @param path The path to the INI file.
   * @exception IOException if anything goes wrong during read.
   */
 
//  public INIFile(String path, String name) throws IOException {
//    super
//    if (name == null) throw new NullPointerException();
//
//    if (path != null) {
//      if (path.endsWith(File.separator)) this(path + name);
//      else this(path + separator + name);
//    }
//    else this(name);
//  }



  // ******************* Getting **********************

  /**
   * Allows the reading of a single piece of configuration data from
   * any section within the INI file.
   * @param sec The section within the INI file (without []'s)
   * @param key The key whose value is to be found within that section
   * @return The string containing the value of the key, null if the key does not exist.
   */

  public String getValue(String sec, String key) {
    if (sections.containsKey(sec) && ((Hashtable)sections.get(sec)).containsKey(key))
      return (String)((Hashtable)sections.get(sec)).get(key);
    else return null; // JK2000 : ou return default_value;
  }
  
  /**
   * Allows the reading of an entire section of configuration data.
   * @param sec The section within the INI file (without []'s)
   * @return The Hashtable containing the keys and values as strings in that section, null if
   *         the section does not exist.
   */

  public Hashtable getSection(String sec) {
    return (Hashtable)sections.get(sec);
  }
  
  /**
   * Allows the reading of the entire INI file.
   * @return The Hashtable containing the Hashtables for each section.
   *         Will be empty if no sections exist.
   */

  public Hashtable getAll() {
    return sections;
  }
  
  // ******************* Adding **********************

  /**
   * Assigns a value to a key in a section of the INI file.
   * Any method calls where the section or key are null or zero
   * length will be ignored. <b>Remember to save() when ready.</b>
   * @param sec The section to add to.  If the section does not exist, it will be created.
   * @param key The key to add.  If the key does not exist, it will be created.
   * @param value The value to assign to the key.  This value can be <tt>""</tt>.
   * @return The value of any key that is overwritten in this operation.
   */

  public String addValue(String sec, String key, String value) {
    if (sec!=null && key!=null && sec!="" && key!="") {
      Hashtable temp;
      if (!sections.containsKey(sec))
        sections.put(sec, temp = new Hashtable());
      else temp = (Hashtable)sections.get(sec);
      
      return (String)temp.put(key, value);
    }
    else return null;
  }

  /**
   * Creates a new section in the INI file.  If the section already
   * exists or the section is null or <tt>""</tt> then no action is
   * taken. <b>Remember to save() when ready.</b>
   * @param sec The section (without []'s) to be created.
   */

  public void addSection(String sec) {
    if (sec!=null && sec!="" && !sections.containsKey(sec)) sections.put(sec, new Hashtable());
  }

  // ******************* Removing **********************
  
  /**
   * Removes a key (and it's corresponding value) from the INI file.
   * <b>Remember to save() when ready.</b>
   * @param sec Section that the key and value is in.
   * @param key The key to be removed.
   * @return The value of any value that is removed.  Null if nothing is removed.
   */

  public String removeValue(String sec, String key) {
    if (!sections.containsKey(sec)) return null;
    else return (String)((Hashtable)sections.get(sec)).remove(key);
  }
  
  /**
   * Removes a section (and all it's keys) from the INI file.
   * <b>Remember to save() when ready.</b>
   * @param sec Section to be removed (without []'s)
   * @return Any section removed as a Hashtable.  Null if nothing is removed.
   */

  public Hashtable removeSection(String sec) {
    return (Hashtable)sections.remove(sec);
  }

  /**
   * Clears the entire INI file.
   * <b>Remember to save() when ready.</b>
   * @return The Hashtable containing the Hashtables for each section.
   */

  public Hashtable removeAll() {
    Hashtable temp = sections;
    sections = new Hashtable();
    return temp;
  }

  // ******************* Saving **********************
    
  /**
   * Writes out the current data to a file, overwriting the original file,
   * or the latest filename that it has been saved to successfully.
   * @exception IOException if an error occurs while writing.
   */

  public void save() throws IOException {
    save(filename);
  }

  /**
   * Writes out the current data to a file.
   * @param file The file to save to.
   * @exception IOException if an error occurs while writing.
   */

  public void save(String file) throws IOException {
    Hashtable temp;
    String str, substr;
    PrintWriter pw = new PrintWriter(new FileOutputStream(file));
    for (Enumeration keys = sections.keys(); keys.hasMoreElements(); ) {
      str = (String)keys.nextElement();
      temp = (Hashtable)sections.get(str);
      pw.println("[" + str + "]");
      for (Enumeration subkeys = temp.keys(); subkeys.hasMoreElements(); ) {
        substr = (String)subkeys.nextElement();
        pw.println(substr + "=" + (String)temp.get(substr));
      }
      pw.println();
    }
    if (pw.checkError()) throw new IOException("An I/O exception occurred while saving the file.");
    filename = file;
  }

  // ******************* Private Methods **********************

  /*
   * An internal method that converts a section in the file
   * to a Hashtable
   */

  private void readSection() throws IOException {
    Hashtable hash = new Hashtable();
    String title = line.substring(1, line.lastIndexOf(']'));
    
    line = dis.readLine();
    while(line!=null && !line.startsWith("[")) {
      if (line!=null && line.length()!=0 && !line.startsWith(";"))
        hash.put(line.substring(0,line.indexOf("=")).trim(), line.substring(line.indexOf("=") + 1).trim());
      line = dis.readLine();
    }
    sections.put(title, hash);
  }

  
}
