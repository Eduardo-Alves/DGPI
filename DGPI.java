import java.io.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.net.*;
import java.util.*;

class DGPI extends Frame implements WindowListener, HyperlinkListener, ActionListener {
// note : \u03C9 = caractˆre unicode pour omega
// note : pas d'affichage possibli dans
  String STR_OMEGA = "\u03C9"; // pour le mode graphique
  //String STR_OMEGA = "<font face=Symbol>w</font>"; // pour le mode texte HTML

  public static final int OUTPUT_TYPE_HTML = 0;
  public static final int OUTPUT_TYPE_TEXT_ONLY = 1;
  
  boolean quiet_mode = false;

  Label lb_invite1;
  Label lb_invite2;
  Label lb_invite3;
  Label lb_invite4;
  
  JLabel lb_invite;
  JEditorPane p;
  JButton bt_submit;
  JTextArea ta_protein;
  JScrollPane sp;
  JScrollPane sp2;

  Panel p2;
  Panel p0;
  Panel p1;
  History histo;

  String dgpi_result, dgpi_result_text_only; // résultats de l'analyse par DGPI, sous forme HTML et text only






  Hashtable ht_parametres = new Hashtable(); // table contenant les paramètres de la ligne de commande. Chaque paramètre est sur la ligne de commande comme -<nom_param>=<valeur>
                                             // <nom_param> est la clé de la hashtable et <valeur> est la valeur qui correspond à la clé. Si un paramètre n'a pas de valeur, il est mis dans la table avec la valeur "" (chaine vide).
                                             // un paramètre n'est mis dans la table que si il commence par '-'




  // variables DGPI
  // g‰n‰ration de la page HTML qui sera donn‰e € l'utilisateur. il est possible d'utiliser ce soft en local en entrant :
  //         java DGPI <access_number> > result.html
  // et de lire la page HTML result.html (seule diff‰rence : le header DGPI avec le logo)
  public static Hydrophobicite hy =null;
  public static AncreGPI ancre = null;
  //public static Signal si = null;

//  public static String sequence = null;
//  public static String access_number = null;

  public static int position_w_sp = 0;
  public static boolean keyword_GPI = false;
  public static int nbr_Sequences;
  private String serveur = "";
  private String resultat_get_sequence;

  public String database_name = "-";  
  private boolean is_eukariote = true; //par d‰faut, on considˆre la prot‰ine comme eukariote (dans le cas o· on a que la s‰quence)

  INIFile init_file = null;



  
  public DGPI(String[] argv) {

    // choix du serveur depuis le fichier .ini
    Rectangle r = null;
    try {
      // on essaie de lire le fichier .ini s'il existe
      init_file = new INIFile("dgpi.ini");

      // serveur
      serveur = init_file.getValue("server", "server_url");

      // position de la fenŠtre
      int x = Integer.parseInt(init_file.getValue("window", "x"));
      int y = Integer.parseInt(init_file.getValue("window", "y"));
      int width = Integer.parseInt(init_file.getValue("window", "width"));
      int height = Integer.parseInt(init_file.getValue("window", "height"));
      r = new Rectangle(x,y,width,height);
    }
    catch (IOException e) {
      // le fichier n'existe pas => on cr‰e un nouveau fichier
      init_file = new INIFile("dgpi.ini", true);
      r = new Rectangle (10,10,800,600);
      serveur = "http://www.expasy.ch/cgi-bin/sprot-search-ac";
    }


    // extraction des paramètres
    for (int i=0; i<argv.length; i++) {
      if (argv[i].charAt(0)=='-') {
        // le paramètre commence par '-' => on le met dans la table
        int index_egal = argv[i].indexOf('=',1);
        String nom_param; // le nom du paramètre (clé de la table)
        String valeur; // valeur associée au nom du paramètre

        if (index_egal>0) {
          // il y a un égal dans le paramètre => on calcule le nom du paramètre
          nom_param = argv[i].substring(1, index_egal);
          valeur = argv[i].substring(index_egal+1);
        } else {
          // pas de égal => pas de valeur
          nom_param = argv[i].substring(1);
          valeur = "";
        }//end if
        
        // ajout du paramètre dans la table
        //System.out.println(nom_param+"   "+valeur);
        ht_parametres.put(nom_param, valeur);
      }//end if
    }//end for
    
    // traitement des paramètres
    String valeur_param_quiet=(String)ht_parametres.get("q");
    if (valeur_param_quiet!=null) {
      // pas de paramètre -q => pas de quiet mode quiet mode
      quiet_mode = true;
    }//end if

    String valeur_param_help=(String)ht_parametres.get("h");
    if (valeur_param_help!=null) {
      // paramètre -h => display help
      System.out.println("DGPI v2.04 - Prediction of GPI-anchor from a protein sequence");
      System.out.println("Syntax : ");
      System.out.println("     dgpi [-file=<file_name> [-output=HTML|text]] [-h] [-q]\n");
      System.out.println("     -file=<file_name>    : tell DGPI which file contain the protein to analyze");
      System.out.println("                            <file_name> contains :");
      System.out.println("                                        an access unmber  OR");
      System.out.println("                                        an entry name     OR");
      System.out.println("                                        a sequence        OR");
      System.out.println("                                        a protein in fasta format");
      System.out.println("     -output=HTML|text    : tell DGPI on which format need to be writed");
      System.out.println("                            Default output type is HTML");
      System.out.println("     -h                   : display this help");
      System.out.println("     -q                   : quiet mode (does not display any messages)");
      System.out.println("\n     DGPI start in graphical if no file is specified from the command line");
      System.out.println("\n     DGPI results are stored in HTML format in file <file_name>.html");
      System.out.println("     or in text format in file <file_name>.txt");
      System.out.println("\n     web : dgpi.pathbot.com       email : dgpi@bigfoot.com");
      System.exit(0);
    } else {
      // pas d'aide demandée => on la propose
      if (!quiet_mode) System.out.println("DGPI v2.04 -      (use flag -h for help)");
    }//end if


    String copyright = "<br><br><small><small><hr>Generated by <a href=http://dgpi.pathbot.com/>DGPI</a> v.2.04   (free version for educational purpose only)<br>"+(new Date()).toString()+"</small></small>";
    
    String valeur_param_file=(String)ht_parametres.get("file");
    if (valeur_param_file!=null) {
      // paramètre -file => lire une protéine dans un fichier

      // lecture de la protéine
      Protein prot = null;      
      prot = new Protein();
      prot.read_from_file(valeur_param_file);
      //System.out.println(prot);
      
      STR_OMEGA = "<font face=Symbol>w</font>"; // pour le mode texte HTML sinon la page HTML de résultat n'afficha pas les omégas correctement.
      prot.serveur = this.serveur;
      String reponse = "DGPI error : unable to find output type";
      String file_name_extension = "html";
      
      String valeur_param_output = (String)ht_parametres.get("output");
      if (valeur_param_output!=null) {
        if (valeur_param_output.equals("HTML")) {
          if (!quiet_mode) System.out.println("  output type = HTML");
          reponse = getResponse(prot, OUTPUT_TYPE_HTML);
        } else if (valeur_param_output.equals("text")) {
          if (!quiet_mode) System.out.println("  output type = text");
          file_name_extension = "txt";
          copyright = "";
          reponse = getResponse(prot, OUTPUT_TYPE_TEXT_ONLY);
        } else {
          // type de fichier inconnu => erreur
          System.out.println("DGPI error : output parameter is wrong : "+valeur_param_output+" (valid option are : HTML or text)");
          System.exit(-1);
        }//end if
      } else {
        // pas de paramètre de sortie => format par défaut
        reponse = getResponse(prot);
      }//end if


      // sauver dans un fichier HTML
      String out_file_name = valeur_param_file+"."+file_name_extension;
      try {
        PrintWriter pw = new PrintWriter(new FileWriter(out_file_name));
        pw.println(reponse);
        pw.println(copyright);
        pw.close();
        System.exit(0);
      }
      catch (IOException e) {
        //e.printStackTrace();
        System.out.println("DGPI error : cannot write to file "+out_file_name);
        System.exit(-1);
      }
      if (!quiet_mode) System.out.println("Done! (result saved in "+out_file_name+")");

} else {
	// pas de fichier => mode graphique
  lb_invite1 = new Label("Please enter :");
  lb_invite2 = new Label("   a protein sequence OR");
  lb_invite3 = new Label("   an entry name OR     ");
  lb_invite4 = new Label("   an Access Name       ");
  
  lb_invite = new JLabel("<html>Please enter :<blockquote><li>a protein sequence OR<li>an entry name OR<li>an Access number.</blockquote>");
  //file://c:/kronegg/dgpi/GPI-anchor/dgpi/java/cgi/testHTML/gpi-anchor_ico.jpg
  p = new JEditorPane("text/html", "<h1>Welcome to DGPI v. 2.04</h1>DGPI web site : <a href=http://dgpi.pathbot.com/>http://dgpi.pathbot.com/</a>");
  bt_submit = new JButton("Submit", new ImageIcon("ancre_ico.bak.gif"));
  ta_protein = new JTextArea(5,30);
  sp = new JScrollPane(p);
  sp2 = new JScrollPane(ta_protein);

  p2 = new Panel(new GridLayout(4,1));
  p0 = new Panel(new BorderLayout());
  p1 = new Panel(new FlowLayout());




    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()/*"com.sun.java.swing.plaf.windows.WindowsLookAndFeel"*/);
    }
    catch (Exception e) {
      e.printStackTrace();
    }


    this.setBounds(r);
    this.setTitle("DGPI v. 2.04");
    this.setBackground(Color.lightGray);
    Toolkit tk = Toolkit.getDefaultToolkit();
    this.setIconImage(tk.getImage("gpi-anchor_ico.jpg"));

    p.setEditable(false);
     
    sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    p.addHyperlinkListener(this);
    //sp.setToolTipText("This panel show you the results found by DGPI");
    //p.setToolTipText("This panel show you the results found by DGPI");

    //bt_submit.setToolTipText("Click this button to submit the opposite protein to DGPI.");
    bt_submit.addActionListener(this);

    
    //sp2.setToolTipText("Enter your protein (sequence, access number or entry name) in this box.");
    //ta_protein.setToolTipText("Enter your protein (sequence, access number or entry name) in this box.");

    histo = new History(p);
    p2.add(lb_invite1);
    p2.add(lb_invite2);
    p2.add(lb_invite3);
    p2.add(lb_invite4);
    //p0.add(lb_invite, BorderLayout.NORTH);
    p0.add(p2,BorderLayout.NORTH);
    p0.add(histo, BorderLayout.SOUTH);
    
    p1.add(p0);
    p1.add(sp2);
    p1.add(bt_submit);

    this.setLayout(new BorderLayout());
    this.add(p1, BorderLayout.NORTH);
    this.add(sp,BorderLayout.CENTER);

    this.addWindowListener(this);
    this.show();
    
    }//end if (fichier présent sur la ligne de commande)
    
    
  }//end constructeur











  // WindowListener interface methods
  public void windowClosing(WindowEvent e) {
    // sauver les dimensions de la fenŠtre dans le fichier .ini
    Rectangle d = this.getBounds();
    init_file.addSection("window");
    init_file.addSection("server");
    init_file.addValue("server","server_url", serveur);
    init_file.addValue("window","x", String.valueOf(d.x));
    init_file.addValue("window","y", String.valueOf(d.y));
    init_file.addValue("window","width", String.valueOf(d.width));
    init_file.addValue("window","height", String.valueOf(d.height));
    try {init_file.save();} catch (Exception e2) {System.out.println("DGPI error : unable to save the configuration file (dgpi.ini)");}
    
    System.exit(0);
  }
  public void windowOpened(WindowEvent e) {}
  public void windowClosed(WindowEvent e) {}
  public void windowIconified(WindowEvent e) {}
  public void windowDeiconified(WindowEvent e) {}
  public void windowDeactivated(WindowEvent e) {}
  public void windowActivated(WindowEvent e) {}
  

  // Hyperlink interface methods
  public void hyperlinkUpdate(HyperlinkEvent e) {
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      //System.out.println("link cliqu‰");
      JEditorPane pane = (JEditorPane) e.getSource();
      try {
      	URL url = pane.getPage();
      	if (url == null) {
      	  // la page n'a pas d'url, c'est donc un contenu texte => ajouter le contenu € l'history
      	  histo.add(pane.getText(), HistoryElement.TYPE_TEXT);
        } else {
          // la page est une URL => ajouter le lien € l'history
          histo.add(url.toString(), HistoryElement.TYPE_URL);
      	}//end if
      	if (!quiet_mode) System.out.println(e.getURL().toString());
        pane.setPage(e.getURL());
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }//end if
  }//end hyperlinkUpdate





  // ActionListener interface methods
  public void actionPerformed(ActionEvent e) {
    String file_name = "DGPI_result.html";
    Object source = e.getSource();
    
    if (source == bt_submit) {
      p.setText("<body>Please wait...</body>");
      p.repaint();
      try{
      Protein prot = new Protein();
      prot.set(ta_protein.getText());
      String reponse = getResponse(prot);      

      // sauver dasn un fichier HTML
      PrintWriter pw = new PrintWriter(new FileWriter(file_name));
      pw.println(reponse);
      pw.close();

      // ne marche pas parce que le protocole "file" n'est pas interpr‰t‰ correctement
     // p.setPage("file:///c:/kronegg/dgpi/GPI-anchor/dgpi/java/cgi/testHTML/DGPI_result.html");
     
      // affichage de la page (attention, les URL des images ne peuvent Štre qu'en absolu)
      p.setText(reponse);
      
      }
      catch (Exception ex) {
	//ex.printStackTrace();
	System.out.println("DGPI error : cannot write to file "+file_name);
      }
    }//end if
  }//end actionPerformed
  





  public String getResponse(Protein prot) {
    // par défaut, on retourne une page HTML
    return getResponse(prot, OUTPUT_TYPE_HTML);
  }//end getResponse



  public String getResponse(Protein prot, int output_type) {
    String result = "";
    
    result += "<table width=100%><tr><td align=left><!--<img src=gpi-anchor_ico.jpg></td><td align=left>--><b><BIG><big>DGPI Result</BIG></big></b></td><td align=right><!--Web site : <a href=http://dgpi.pathbot.com/>http://dgpi.pathbot.com/</a>--></td></tr></table>";
    result += "<table width=90% border=1><tr><td>Protein</td><td>"+prot.name_HTML()+"</td></tr>";

    if (prot.sequence==null) {
      // la séquence est inconnue => il faut aller la chercher sur le serveur
      if (!quiet_mode) System.out.println("Retrieving protein from server...");
      prot.sequence = getSequence(prot.name());
      if (!quiet_mode) System.out.println("Scanning protein...");
      if (prot.sequence.length()==0) {
      	result += "<big>ERROR : </big>the protein "+prot.name_HTML()+" does not exist!";
        return result;
      }//end if
      result += resultat_get_sequence;
    }//end if

    // traitement de la protéine par DGPI
    result += "<tr><td>Sequence</td><td><pre><!--font face=courier size=-1-->";
    for (int i=0; i<prot.sequence.length(); i+=100) {
      int b_sup = i+100;

      if (b_sup>=prot.sequence.length()) b_sup=prot.sequence.length();
      result += prot.sequence.substring(i,b_sup)+"<br>";
    }
    result += "</font></pre></td></tr>";
    result += "<tr><td>Length</td><td>"+prot.sequence.length()+"</td></tr>";

    traite(prot);
    
    result += dgpi_result;
    result += "</table><br><br>";
    
    switch (output_type) {
      case OUTPUT_TYPE_HTML: return result;
      case OUTPUT_TYPE_TEXT_ONLY: return dgpi_result_text_only;
      default :
        System.out.println("DGPI Error : unknow output type");
        System.exit(-1);
    }//end switch
    return null;
  }// end getResponse









  public String getSequence(String access_number) {
    resultat_get_sequence = "";
    String str_PSORT = "";
    String val_sequence = "";

   
        // connexion au serveur web expasy et r‰cup‰ration de la prot‰ine
        BufferedReader donneesHTML = null;
        try {
          URL u;
          u = new URL(serveur+"?"+access_number);
          donneesHTML = new BufferedReader(new InputStreamReader(u.openStream()));
        }
        catch (Exception e){
          System.out.println("Error while connecting to server '"+serveur+"'");
        }

        try {
	  String ligneCourante = null;
	  while ((ligneCourante = donneesHTML.readLine()) != null) {
            str_PSORT += ligneCourante;
	  }
	}
	catch (Exception e) {
	  System.out.println("DGPI error : cannot read data from server "+serveur);
	  //e.printStackTrace();
	  System.exit(-1);
	}



	//try {

        // recherche du nom de la base de donn‰e d'o· provient la prot‰ine
        // le format HTML est :   <h1>NiceProt View of TrEMBL: 
        String database_name_str_debut = "<h1>NiceProt View of ";
        if (str_PSORT.indexOf(database_name_str_debut)==-1) {
          return "";// s‰quence vide
        } else {
          int index_database_name_debut = str_PSORT.indexOf(database_name_str_debut) + database_name_str_debut.length();
          int index_database_name_fin = str_PSORT.indexOf(':', index_database_name_debut);
          database_name = str_PSORT.substring(index_database_name_debut, index_database_name_fin);
          resultat_get_sequence += "<tr><td>Data from "+database_name+"</td><td width=70%>";
 
          String str_access_number = "Primary accession number</td><td bgcolor=#FFFF66><B>";
          int index_AC_debut = str_PSORT.indexOf(str_access_number, index_database_name_fin)+str_access_number.length();
          int index_AC_fin   = str_PSORT.indexOf("</B>", index_AC_debut);
          access_number      = str_PSORT.substring(index_AC_debut, index_AC_fin);

          //recherche de la feature PROPEP (partie entre le site w et le C-terminal)
          String str_PROPEP = "<br>PROPEP";
          int index_PROPEP_debut = str_PSORT.indexOf(str_PROPEP);
          if (index_PROPEP_debut!=-1) resultat_get_sequence += "Found the feature PROPEP in "+database_name+" entry<br>";
          index_PROPEP_debut += str_PROPEP.length();
          int index_PROPEP_fin   = str_PSORT.indexOf("<br>", index_PROPEP_debut);
          String str_PROPEP_GPI  = str_PSORT.substring(index_PROPEP_debut, index_PROPEP_fin);

          //extraction de la feature LIPID (ancre GPI)
          String str_LIPID     = "<br>LIPID";
          String str_LIPID_GPI = "";
          String position_w    = "0";
          int index_LIPID_debut = str_PSORT.indexOf(str_LIPID);
          if (index_LIPID_debut!=-1) {
            // LIPID trouv‰e
            resultat_get_sequence += "Found the feature LIPID in "+database_name+" entry<br>";
            index_LIPID_debut +=str_LIPID.length();
            int index_LIPID_fin   = str_PSORT.indexOf("<br>", index_LIPID_debut);
            str_LIPID_GPI         = str_PSORT.substring(index_LIPID_debut, index_LIPID_fin);

            //recherche de la position de l'ancre
            String str_position_GPI = "\">";
            int index_position_GPI_debut = str_LIPID_GPI.indexOf(str_position_GPI)+str_position_GPI.length();
            int index_position_GPI_fin   = str_LIPID_GPI.indexOf("  ", index_position_GPI_debut);
            position_w                   = str_LIPID_GPI.substring(index_position_GPI_debut, index_position_GPI_fin);
            position_w_sp = new Integer(position_w).intValue();
            if (position_w_sp!=0) resultat_get_sequence += "cleavage site at "+position_w_sp+".<br>";
          }//end if

          //extraction de la taxonomie
          String str_taxonomy = "<td>Taxonomy</td><td >";
          int index_taxonomy_debut = str_PSORT.indexOf(str_taxonomy)+str_taxonomy.length();
          //int index_taxonomy_fin   = str_PSORT.indexOf("</td>", index_taxonomy_debut);
          int index_taxonomy_fin   = str_PSORT.indexOf("; ", index_taxonomy_debut);
          String str_taxonomy_2      = str_PSORT.substring(index_taxonomy_debut, index_taxonomy_fin);
          int index_taxonomy_point   = str_taxonomy_2.indexOf(". ");
          if (index_taxonomy_point!=-1) str_taxonomy_2 = str_taxonomy_2.substring(0, index_taxonomy_point);
          //taxonomie = str_taxonomy_2;*/
          if (!str_taxonomy_2.equals("Eukaryota")) {
            resultat_get_sequence += "WARNING : this protein is not from an Eukaryota.<br>";
            is_eukariote = false;
          } else {
            is_eukariote = true;
          }//end if


          //extraction des mots-cl‰s
          String str_Keyword = "<b>Keywords</b></td>";
          int index_Keyword_debut = str_PSORT.indexOf(str_Keyword)+str_Keyword.length();
          int index_Keyword_fin   = str_PSORT.indexOf("</td>", index_Keyword_debut);
          String str_Keyword_GPI  = str_PSORT.substring(index_Keyword_debut, index_Keyword_fin);

          //r‰cup‰ration de la s‰quence
          String str_sequence = "<td><PRE>";
          int index_sequence_debut = str_PSORT.indexOf(str_sequence)+str_sequence.length();
          int index_sequence_fin   = str_PSORT.indexOf("</PRE>", index_sequence_debut);
          for (int j=index_sequence_debut; j<index_sequence_fin; j++) {
            char chr = str_PSORT.charAt(j);
            if ((chr>='A') && (chr<='Z')) val_sequence += chr;
          }//end for
          


          // calcul des propri‰t‰s selon swiss-prot.
/*          String val_keyword_gpi = "0";
          String val_propep      = "0";
          String val_propep_p    = "0";
          String val_propep_bs   = "0";
          String val_lipid       = "0";
          String val_lipid_p     = "0";
          String val_lipid_bs    = "0";*/
          if (str_Keyword_GPI.indexOf("GPI-anchor"            )!=-1) {keyword_GPI = true; resultat_get_sequence += "Found the keyword 'gpi-anchor' in "+database_name+" entry<br>";}
          if (str_PROPEP_GPI.indexOf ("REMOVED IN MATURE FORM")!=-1) resultat_get_sequence += "the PROPEP is removed in mature form<br>";
          if (str_PROPEP_GPI.indexOf ("POTENTIAL"             )!=-1) resultat_get_sequence += "the PROPEP is potential<br>";
          if (str_PROPEP_GPI.indexOf ("BY SIMILARITY"         )!=-1) resultat_get_sequence += "the PROPEP is by similarity<br>";
          if (str_LIPID_GPI.indexOf  ("GPI-ANCHOR"            )!=-1) resultat_get_sequence += "the LIPID is a GPI-anchor<br>";
          if (str_LIPID_GPI.indexOf  ("POTENTIAL"             )!=-1) resultat_get_sequence += "the LIPID is potential<br>";
          if (str_LIPID_GPI.indexOf  ("BY SIMILARITY"         )!=-1) resultat_get_sequence += "the LIPID is by similarity<br>";

          resultat_get_sequence += "&nbsp;</td></tr>";

          }//end if (protein found)

//        }//end if (error)
/*      }//end try
      catch (Exception e) {
         resultat_get_sequence += "impossible de faire la requete!!! (proteine "+access_number+")";
         resultat_get_sequence += e.getMessage();
         resultat_get_sequence += str_PSORT;
      }*/
      return val_sequence;

  }//end getSequence










	public void traite(Protein prot) {
	  String result ="";
	  String result_text_only = "";

          boolean critere1, critere2, critere3;
	  char Caract;

          String is_GPI_anchored = null;
          String comment = null;
          String position_GPI_anchor = null;

          int nbr_gpi_anchor_ok = 0;
          int nbr_ancres_sp = 0;
          int nbr_proteines_gpi_ok = 0;
          int nbr_proteines_gpi_ok_phobe_phile = 0;
          int nbr_proteine_signal = 0;

          result += "<tr><td>Data from DGPI</td><td>";
          result_text_only += "";

          float longueur_hydrophobe = 0;
          float longueur_hydrophile = 0;
          float position_GPI_probable = 0;
          position_GPI_anchor = "0";

//                     if (prot.sequence[i].length()>=47) {

          // SIGNAL
          result += "<b>N-term Signal :</b><br>";
          result_text_only += "N-term Signal :\n";
          sigs si2 = new sigs(prot.sequence, is_eukariote);
          if (si2.has_Nterm_signal) {
            result += "&nbsp;&nbsp;there is a N-term signal   (1.."+si2.pos_site_clivage+" in violet)<br>";
            result_text_only += "  there is a N-term signal   (1.."+si2.pos_site_clivage+")\n";
          } else {
            result += "&nbsp;&nbsp;there ISN'T a N-term signal<br>";
            result_text_only += "  there ISN'T a N-term signal\n";
          }
          result += "&nbsp;&nbsp;maximal score="+si2.score_max+"<br><br>";
          result_text_only += "  maximal score="+si2.score_max+"\n\n";




 
          // HYDROPHOBICITE
          result += "<b>C-term Hydrophobicity profile :</b><br>";
          result_text_only += "C-term Hydrophobicity profile :\n";
          try {
            hy = new Hydrophobicite(prot.sequence, "median passe-bas");
          }
          catch (Exception e) {
            result += "Exception while computing C-term Hydrophobicity profile";
          }
	  result += "&nbsp;&nbsp;hydrophobe length (low-pass filter)="+hy.hydrophobe_length+"<br>";
	  result += "&nbsp;&nbsp;hydrophile length (low-pass filter)="+hy.hydrophile_length+"<br>";
	  result += "&nbsp;&nbsp;hydrophobe length (median filter)="+hy.hydrophobe_median_length+"<br>";
	  result += "&nbsp;&nbsp;hydrophile length (median filter)="+hy.hydrophile_median_length+"<br>";
	  result_text_only += "  hydrophobe length (low-pass filter)="+hy.hydrophobe_length+"\n";
	  result_text_only += "  hydrophile length (low-pass filter)="+hy.hydrophile_length+"\n";
	  result_text_only += "  hydrophobe length (median filter)="+hy.hydrophobe_median_length+"\n";
	  result_text_only += "  hydrophile length (median filter)="+hy.hydrophile_median_length+"\n";

          float POIDS_MEDIAN_PHOBE = (float)0.5;//meilleur = 0.7 (en tous cas pour les 172 mais pas pour les 23)
          float POIDS_MEDIAN_PHILE = (float)0.5;//meilleur = 0.7
          longueur_hydrophobe = POIDS_MEDIAN_PHOBE * hy.hydrophobe_median_length + (1-POIDS_MEDIAN_PHOBE) * hy.hydrophobe_length /*+ hy.hydrophobe_eisenberg_length*/;
          longueur_hydrophile = POIDS_MEDIAN_PHILE * hy.hydrophile_median_length + (1-POIDS_MEDIAN_PHILE) * hy.hydrophile_length /*+ hy.hydrophile_eisenberg_length*/;
          result += "&nbsp;&nbsp;average hydrophobe length = "+longueur_hydrophobe+" (in blue)<br>";
          result += "&nbsp;&nbsp;average hydrophile length = "+longueur_hydrophile+"<br><br>";
          result_text_only += "  average hydrophobe length = "+longueur_hydrophobe+"\n";
          result_text_only += "  average hydrophile length = "+longueur_hydrophile+"\n\n";
          position_GPI_probable = longueur_hydrophobe + 7;


			
//          System.out.println("  position_GPI_probable="+position_GPI_probable);

          result += "<b>Cleavage site ("+STR_OMEGA+") :</b><br>";
          result_text_only += "Cleavage site (w) :\n";
          try {
            ancre = new AncreGPI(prot.sequence, 13);
          }
          catch (Exception e) {
            result += "Exception AncreGPI : proteine = "+prot.access_number;
            result += "  seq="+prot.sequence;
            result_text_only += "Exception AncreGPI : proteine = "+prot.access_number;
            result_text_only += "  seq="+prot.sequence;
          }

                        
/*                        try {
                          si = new Signal(prot.sequence[i], "");
                        }
                        catch (Exception e) {
                          System.out.println("Exception Signal : proteine = "+access_number[i]);
                          System.out.println("  seq="+sequence[i]);
                        }
*/
/*                        System.out.println("  phile1="+si.hydrophile1_length);
                        System.out.println("  phobe ="+si.hydrophobe_length);
                        System.out.println("  phile2="+si.hydrophile2_length);
                        System.out.println("  median_phile1="+si.hydrophile1_median_length);
                        System.out.println("  median_phobe ="+si.hydrophobe_median_length);
                        System.out.println("  median_phile2="+si.hydrophile2_median_length);
*/

                        float valeur_max=1000;
                        int index_max=-1;
                        int num_ancre;
//System.out.println(ancre.nbr_ancres+ " ancres trouves");
                        for (num_ancre=0; num_ancre<ancre.nbr_ancres; num_ancre++) {
                          if ((ancre.position[num_ancre]-position_GPI_probable)<valeur_max) {
                            valeur_max = Math.abs(ancre.position[num_ancre]-position_GPI_probable);
                            index_max = num_ancre;
                          }
                        }

                        int index_max_pourcent=index_max;

                        if (index_max==-1) {
                          result += "&nbsp;&nbsp;no cleavage site detected ("+STR_OMEGA+", "+STR_OMEGA+"+2 rule not respected).<br>"; 
                          result_text_only += "  no cleavage site detected (w, w+2 rule not respected).\n"; 
                          position_GPI_anchor = "0";
                        } else {
                          result += "&nbsp;&nbsp;There's a GPI-anchor near "+(prot.sequence.length()-ancre.position[index_max])+"  (7 aa after hydrophobic tail)";
                          result_text_only += "  There's a GPI-anchor near "+(prot.sequence.length()-ancre.position[index_max])+"  (7 aa after hydrophobic tail)";
                          if (position_w_sp!=0) {
                            result += " ("+position_w_sp+" say "+database_name+", "+prot.sequence.substring(position_w_sp-1, position_w_sp+2)+")";
                            result_text_only += " ("+position_w_sp+" say "+database_name+", "+prot.sequence.substring(position_w_sp-1, position_w_sp+2)+")";
                          }//end if
                          result += "<br>";
                          result_text_only += "\n";

                          int NBR_GAUCHE = 1;
                          int NBR_DROITE = 1;
                          float valeur_max_pourcent=ancre.position_pourcent[index_max];
 //System.out.println("pos_max="+index_max_pourcent + "   pourcent="+ancre.position_pourcent[index_max]);
                          for (num_ancre=index_max-NBR_GAUCHE; num_ancre<=index_max+NBR_DROITE; num_ancre++) {// modif <=
                            if ((num_ancre>=0) &&
                                (num_ancre<ancre.nbr_ancres) && 
                                (ancre.position_pourcent[num_ancre]>valeur_max_pourcent)) {
                              valeur_max_pourcent = ancre.position_pourcent[num_ancre];
                              index_max_pourcent = num_ancre;
                            }
                          }

                          for (num_ancre=0; num_ancre<ancre.nbr_ancres; num_ancre++) {
                            result += "&nbsp;&nbsp;There's a potential cleavage site at "+(prot.sequence.length()-ancre.position[num_ancre])+" (score="+ancre.position_pourcent[num_ancre]+") detected by "+STR_OMEGA+","+STR_OMEGA+"+2 rule.<br>";
                            result_text_only += "  There's a potential cleavage site at "+(prot.sequence.length()-ancre.position[num_ancre])+" (score="+ancre.position_pourcent[num_ancre]+") detected by w, w+2 rule.\n";
                          }

                          result += "&nbsp;&nbsp;The best cleavage site is "+(prot.sequence.length()-ancre.position[index_max_pourcent])+"<br>";
                          result_text_only += "  The best cleavage site is "+(prot.sequence.length()-ancre.position[index_max_pourcent])+"\n";
                          position_GPI_anchor = new Integer(prot.sequence.length()-ancre.position[index_max_pourcent]).toString();
                        
                          //ancre.position[index_max_pourcent] = Math.round((float)0.8 * ancre.position[index_max_pourcent] + (float)0.2 * ancre.position[index_max]);
  
                          if ((prot.sequence.length()-ancre.position[index_max_pourcent]==position_w_sp) && (keyword_GPI==true) ) {
                            //mŠme r‰sultat que swissprot
                            nbr_gpi_anchor_ok++;
                          }

                          }//end if (ancre d‰tect‰e)

                          if (position_w_sp!=0) {
                            if (nbr_gpi_anchor_ok>0) {
                              result += "&nbsp;&nbsp;"+database_name+" indicate the same cleavage site.<br>";
                              result_text_only += "  "+database_name+" indicate the same cleavage site.\n";
                            } else {
                              result += "&nbsp;&nbsp;"+database_name+" indicate a different cleavage site.<br>";
                              result_text_only += "  "+database_name+" indicate a different cleavage site.\n";
                            }
                          }
                          result += "<br>";
                          result_text_only += "\n";


                        // CONCLUSION
                        result += "<b>Conclusion :</b><br>";
                        result_text_only += "Conclusion :\n";
                        // calcul du nombre de prot‰ines gpi selon nous (349 selon swiss-prot)
                        result += "&nbsp;&nbsp;This protein ";
                        result_text_only += "  This protein ";
                        is_GPI_anchored = "0";
                        if (/*si.score_max>=0*/ si2.has_Nterm_signal) {//signal
                          nbr_proteine_signal++;
                          if (longueur_hydrophobe>12) {//partie hydrophobe   <<<< meilleurs resultats avec >15 >>>>
                            nbr_proteines_gpi_ok++;
                            if (longueur_hydrophile>2) {//partie hydrophile <<<< meilleurs resultats avec >2 >>>>
                              nbr_proteines_gpi_ok_phobe_phile++;
                              is_GPI_anchored = "1";
                              result += "is GPI-anchored (signal, hydrophobic & hydrophilic tail present).<br>";
                              result_text_only += "is GPI-anchored (signal, hydrophobic & hydrophilic tail present).\n";
                              //comment = "is GPI-anchored.";
                            } else {
                              result += "is not GPI-anchored (C-terminal hydrophobic area but C-terminal hydrophilic area is too short).<br>";
                              result_text_only += "is not GPI-anchored (C-terminal hydrophobic area but C-terminal hydrophilic area is too short).\n";
                              //comment = "is not GPI-anchored (C-terminal hydrophobic area but C-terminal hydrophilic area is too short).";
                            }
                          }else {
                            result += "is not GPI-anchored (C-terminal hydrophobic area is too short).<br>";
                            result_text_only += "is not GPI-anchored (C-terminal hydrophobic area is too short).\n";
                            //comment = "is not GPI-anchored (C-terminal hydrophobic area is too short).";
                          }
                        } else {
                          result += "is not GPI-anchored (no signal).<br>";
                          result_text_only += "is not GPI-anchored (no signal).\n";
                          //comment = "is not GPI-anchored (no signal).";
                        }

                        if (index_max==-1) {
                          result += "&nbsp;&nbsp;No cleavage site detected.<br>"; 
                          result_text_only += "  No cleavage site detected.\n"; 
                        } else {
                          result += "&nbsp;&nbsp;There is a potential cleavage site at "+(prot.sequence.length()-ancre.position[index_max_pourcent])+" ("+STR_OMEGA+", "+STR_OMEGA+"+1, "+STR_OMEGA+"+2 in red)<br>";
                          result_text_only += "  There is a potential cleavage site at "+(prot.sequence.length()-ancre.position[index_max_pourcent])+" (w, w+1, w+2)\n";
                        }

                        // calcul du nombre d'ancres GPI selon swiss-prot
                        if ((position_w_sp!=0) && (keyword_GPI==true)) {
                          nbr_ancres_sp++;
                        }


                result += "<br></td></tr>";
                result_text_only += "";
//font face=courier size=-1
          result += "<tr><td>Sequence annotated by DGPI</td><td><pre><font color=#FF66FF>";
          String str_sequence = "";
          for (int i=0; i<prot.sequence.length(); i++) {
            if (i+1==si2.pos_site_clivage) str_sequence += "</font>";
            str_sequence += prot.sequence.charAt(i);
            if ((i+1) % 100 == 0) str_sequence +="\n";//"</pre><pre>";
            if ((i+1)==prot.sequence.length()-(new Float(longueur_hydrophobe).intValue())) str_sequence += "<font color=#0099FF>";
            if (index_max!=-1) {
              if ((i+1)==(prot.sequence.length()-ancre.position[index_max_pourcent]-1)) str_sequence += "<font color=red>";
              if ((i+1)==(prot.sequence.length()-ancre.position[index_max_pourcent]-1+3)) str_sequence += "</font>";
            }
          }
          str_sequence += "</font>";
          result += str_sequence;

          result += "</pre></td></tr>";

/*
                int NBR_VARSPLIC = 5;
                int NBR_FRAGMENT = 3;
                if (nbr_ancres_sp==0) {
                  System.out.println(nbr_gpi_anchor_ok+" ancres trouvees sur 0 (- %)");
                } else {
                  System.out.println(nbr_gpi_anchor_ok+" ancres trouvees sur "+nbr_ancres_sp+" ("+nbr_gpi_anchor_ok*100/nbr_ancres_sp+"%)");
                }
                System.out.println(nbr_proteine_signal+" proteines trouvees sur "+(nbr_Sequences-NBR_VARSPLIC-NBR_FRAGMENT)+" ("+nbr_proteine_signal*100/(nbr_Sequences-NBR_VARSPLIC-NBR_FRAGMENT)+"%) avec signal");
                System.out.println(nbr_proteines_gpi_ok+" proteines trouvees sur "+(nbr_Sequences-NBR_VARSPLIC-NBR_FRAGMENT)+" ("+nbr_proteines_gpi_ok*100/(nbr_Sequences-NBR_VARSPLIC-NBR_FRAGMENT)+"%) avec signal+phobe");
                System.out.println(nbr_proteines_gpi_ok_phobe_phile+" proteines trouvees sur "+(nbr_Sequences-NBR_VARSPLIC-NBR_FRAGMENT)+" ("+nbr_proteines_gpi_ok_phobe_phile*100/(nbr_Sequences-NBR_VARSPLIC-NBR_FRAGMENT)+"%) avec signal+phobe+phile");*/
          dgpi_result = result;
          dgpi_result_text_only = result_text_only;
	}//end traite

/*  public static void main(String[] argv) {
    if (argv.length==0) {
      System.out.println("Erreur : il faut donner l'access number ou la s‰quence de la prot‰ine");
    } else {
      DGPI prot = new DGPI(argv[0]);
    }//end if	
  }//end main
*/


  
  public static void main(String[] argv) {
    new DGPI(argv);
  }//end main
}//end 