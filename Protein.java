import java.io.*;

public class Protein extends Object {
/*
Classe : Protein
But    : d�crit une prot�ine de base (access number, entry name, s�quence)

Auteur : Julien Kronegg (jkronegg@bigfoot.com)
Date   : 2 juillet 2000
*/
  String serveur = null; // serveur sur lequel on prend les prot�ines

  String file_name  = null; // fichier de provenance de la prot�ine (permet de d�termniner o� stocker les r�sultats de DGPI)
  String access_number = null;
  String entry_name    = null;
  String sequence      = null;
  String access_number_or_entry_name_or_sequence = null; // param�tre fourni par l'utilisateur avant qu'on sache de quoi il s'agit
  String fasta_id      = null; //add gff output for multifasta on Dec 7th 2016 by Eduardo Alves


  public Protein() {}
  
  
  
  

  public Protein(String access_number, String sequence) {
    this.sequence = sequence;
    this.access_number = access_number;
  }//end constructeur




  public String access_number() {
    // retourne l'access number de la prot�ine
    if (access_number==null) return "unknow";
    return access_number;
  }//end access_number



  public String name() {
    // retourne l'access number ou l'entry name de la prot�ine de la prot�ine.
    // si les deux noms sont pr�sents, retourne l'access number
    if (access_number==null) return entry_name();
    if ((access_number==null)&&(entry_name==null)) return fasta_id; //add gff output for multifasta on Dec 7th 2016 by Eduardo Alves
    return access_number;
  }//end name



  public String access_number_HTML() {
    // retourne l'access number de la prot�ine sous forme HTML
    if (access_number==null) return "unknow";
    return "<a href="+serveur+"?"+access_number+">"+access_number+"</a>";
  }//end access_number_HTML




  public String entry_name_HTML() {
    // retourne l'entry name de la prot�ine sous forme HTML
    if (entry_name==null) return "unknow";
    return "<a href="+serveur+"?"+entry_name+">"+entry_name+"</a>";
  }//end entry_name_HTML


  public String entry_name() {
    // retourne l'entry name de la prot�ine sous forme HTML
    if (entry_name==null) return "unknow";
    return entry_name;
  }//end entry_name



  public String name_HTML() {
    // retourne l'access number ou l'entry name de la prot�ine de la prot�ine sous forme HTML.
    // si les deux noms sont pr�sents, retourne l'access number
    if (access_number==null) return entry_name_HTML();
    return "<a href="+serveur+"?"+access_number+">"+access_number+"</a>";
  }//end name_HTML





  public void set(String access_number_or_entry_name_or_sequence) {
    String sequence = "";
    
    if (is_access_number(access_number_or_entry_name_or_sequence)) {
      // c'est un access number
      this.access_number = access_number_or_entry_name_or_sequence;
    } else if (is_entry_name(access_number_or_entry_name_or_sequence)) {
      // c'est un entry name
      this.entry_name = access_number_or_entry_name_or_sequence;
    } else {
      // �a doit �tre une s�quence
      for (int i=0; i<access_number_or_entry_name_or_sequence.length(); i++) {
        char chr = ' ';
        try {
          chr = access_number_or_entry_name_or_sequence.charAt(i);
          if ((chr>='A')&&(chr<='Z')) sequence += chr;
          if ((chr>='a')&&(chr<='z')) sequence += Character.toUpperCase(chr);
        }
        catch (Exception e) {
          System.out.println("Exception lors de la lecture de la s�quence : index="+i+"  char="+chr+" sequence="+sequence);
        }
      }

      this.sequence = sequence;
    }//end if
    this.access_number_or_entry_name_or_sequence = access_number_or_entry_name_or_sequence;
  }//end set






  public String toString() {
    // extends Object.toString()
    // retourne la prot�ine sous forme de String
    return access_number+" ("+sequence+")";
  }//end toString





  public void read_from_file(String file_name) {
    // lit une prot�ine depuis un fichier. Le fichier peut contenir une prot�ine sous diff�rents formats :
    //   o  access number
    //   o  entry name
    //   o  s�quence
    //   o  format FASTA
    //
    //  Note : le format FASTA n'est pas support� enti�rement. Liste des features non support�es :
    //           o  plusieurs prot�ines dans un fichier
    //           o  acides amin�s non conventionnels (support�s : les 20 de base, X, B, U et Z)
    //           o  l'access number de la prot�ine doit �tre dans la premi�re ligne des caract�res 4 � 10
    String result = "";
    BufferedReader in = null;
    
    try {
      // ouvrir le fichier
      in = new BufferedReader(new FileReader(file_name));

      // lire la premi�re ligne
      String ligne = in.readLine();
      
      if (ligne.charAt(0)=='>') {
        // fichier FASTA => on extrait les donn�es de l'ent�te
        if (ligne.length()>10) access_number = ligne.substring(4,10); // lire l'access number

        fasta_id=ligne;  //add gff output for multifasta on Dec 7th 2016 by Eduardo Alves      
        // lire la s�quence
        ligne = in.readLine();
        String sequence = "";
        while ((ligne!=null) && ligne.charAt(0)!='>') {
          if (ligne.charAt(0)==';') ligne = in.readLine(); // sauter la ligne si c'est un commentaire
          sequence += ligne;
          ligne = in.readLine();
        }//end while
        this.sequence = sequence;
        
        
        
      } else {
      	// c'est un fichier d'un autre format (access number, entry name ou s�quence)
        while (ligne!=null) {
          result += ligne;
          ligne = in.readLine();
        }//end while
        // analyse du fichier
        this.set(result);
      }//end if
    }
    catch (IOException e) {
      //e.printStackTrace();
      System.out.println("DGPI error : cannot read file "+file_name);
      System.exit(-1);
    }
  
  }//end read_file







  private boolean is_access_number(String arg) {
    /* d�termine si l'argument arg est un acces_number ou une s�quence  Retourne true si arg repr�sente une s�quence et false si access_number*/
    /* un access_number est caract�ris� par le format suivant : [O,P,Q]  [0-9]  [A-Z, 0-9]  [A-Z, 0-9]   [A-Z, 0-9]   [0-9] */

    if (arg.length()>=6) {
      char ch0 = arg.charAt(0);
      char ch1 = arg.charAt(1);
      char ch2 = arg.charAt(2);
      char ch3 = arg.charAt(3);
      char ch4 = arg.charAt(4);
      char ch5 = arg.charAt(5);

      /* un access_number est caract�ris� par le format suivant : [O,P,Q]  [0-9]  [A-Z, 0-9]  [A-Z, 0-9]   [A-Z, 0-9]   [0-9] */
      if (!(( (ch0>='O') && (ch0<='Q') ) || ((ch0>='o')&&(ch0<='q')))) return false;
      if (!((ch1>='0') && (ch1<='9'))) return false;
      if (!(( (ch2>='A') && (ch2<='Z') ) || ((ch2>='a')&&(ch2<='z')) || ((ch2>='0')&&(ch2<='9')))) return false;
      if (!(( (ch3>='A') && (ch3<='Z') ) || ((ch3>='a')&&(ch3<='z')) || ((ch3>='0')&&(ch3<='9')))) return false;
      if (!(( (ch4>='A') && (ch4<='Z') ) || ((ch4>='a')&&(ch4<='z')) || ((ch4>='0')&&(ch4<='9')))) return false;
      if (!((ch5>='0') && (ch5<='9'))) return false;

      return true;
    } else {
      return false;
    }

  }//end is_access_number







  private boolean is_entry_name(String arg) {
    /* d�termine si l'argument arg est un acces_number ou une s�quence  Retourne true si arg repr�sente une s�quence et false si access_number*/
    // Syntaxe d'un entry name : [A-Z,0-9]*4_[A-Z,0-9]*5    (p.ex : MSLN_HUMAN)
    int max_length = 10;
    if (arg.length()<max_length) max_length = arg.length();

    // si il y a un _ c'est un entry name
    if (arg.substring(0,max_length).indexOf('_')==-1) {
      return false;
    } else {
      return true;
    }

  }//end is_sequence

}//end class
