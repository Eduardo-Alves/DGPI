import java.util.Hashtable;
import java.util.Date;

// Diverses methodes sont disponibles pour executer divers traitements
// sur les sequences
public class AncreGPI {
	String sequence;
	boolean critere1, critere2, critere3;
        public static int NBR_AA_ANALYSES = 40;
        public static int NBR_DEBUT = 5;//nombre de premiers aa sur lesquels faire une moyenne (car les 2-3 aa C-terminaux peuvent être hydrophile, mais c'est normal)

        public int nbr_ancres = 0;
        public static int[] position= new int[100];
        public static float[] position_pourcent= new float[100];


        public static int NBR_AA = 20;

	int longueur;
	Hashtable[] aa_permis = new Hashtable[3];

//        static String enumere_w2 = "GASTV";
//	static String enumere_w="GASCDNVL";

        static String enumere_w2 = "GASTVRNBQZLKMY";
	static String enumere_w="GASCDNVLRH";

	public int getPosition(int i) {
          return position[i];
        }

	public boolean in_w2(char caractere) {
		boolean contient;
	
		contient = false;
		for (int i=0; i<enumere_w2.length(); i++) {
			if (caractere == enumere_w2.charAt(i)) {
				contient = true;
			}
		}	
return true;
		//return contient;

	}
	
	public boolean in_w(char caractere) {
		boolean contient;

		contient = false;	
		for (int i=0; i<enumere_w.length(); i++) {
			if (caractere == enumere_w.charAt(i)) {
				contient = true;
			}			
		}				
return true;
		//return contient;
	}


	public AncreGPI (String la_sequence, int debut) throws Exception {
          /*------------------------------------------------------------------
           * Methode : Hydrophobicite (constructeur)
           * But     : calcule la taille des parties hydrophobe et hydrophile
           *           C-terminales selon deux méthodes :
           *               "median"    : filtre médian (élimine les hautes
           *                             fréquences et augmente la pente des
           *                             flancs
           *               "passe-bas" : filtre passe-bas (diminue les hautes
           *                             fréquences)
           *
           *           mettre le paramètre 'methode' à "median" pour le filtre
           *           médian, "passe-bas" pour le filtre passe-bas et
           *           "median passe-bas" pour les deux filtres.
           */
          String val_hydro;
          int i;

/*          aa_permis[0] = new Hashtable();
	  aa_permis[0].put("A", "0.4");
	  aa_permis[0].put("R", "0.0");//ND
	  aa_permis[0].put("N", "0.8");
	  aa_permis[0].put("D", "0.4");
	  aa_permis[0].put("C", "0.2");
	  aa_permis[0].put("Q", "0.1");//0 selon Kodukula
	  aa_permis[0].put("E", "0.0");
	  aa_permis[0].put("G", "0.4");
	  aa_permis[0].put("H", "0.0");//ND
	  aa_permis[0].put("I", "0.0");//absent
	  aa_permis[0].put("L", "0.1");
	  aa_permis[0].put("K", "0.0");
	  aa_permis[0].put("M", "0.0");
	  aa_permis[0].put("F", "0.0");//absent
	  aa_permis[0].put("P", "0.0");
	  aa_permis[0].put("S", "1.0");
	  aa_permis[0].put("T", "0.0");
	  aa_permis[0].put("W", "0.0");
	  aa_permis[0].put("Y", "0.0");
	  aa_permis[0].put("V", "0.1");
	  aa_permis[0].put("X", "0.0"); // aa non défini
	  aa_permis[0].put("B", "0.6");  // B = N or D    B=Asx =(0.8+0.4)/2
	  aa_permis[0].put("Z", "0.05"); // Z = Q or E    Z=Glx =(0.1+0.0)/2

          aa_permis[1] = new Hashtable();
	  aa_permis[1].put("A", "1.0");
	  aa_permis[1].put("R", "0.5");//ND
	  aa_permis[1].put("N", "0.1");//+
	  aa_permis[1].put("D", "0.4");
	  aa_permis[1].put("C", "0.3");
	  aa_permis[1].put("Q", "0.0");//ND
	  aa_permis[1].put("E", "0.0");//+
	  aa_permis[1].put("G", "0.0");//+
	  aa_permis[1].put("H", "0.0");//ND
	  aa_permis[1].put("I", "0.0");//absent
	  aa_permis[1].put("L", "0.0");//ND
	  aa_permis[1].put("K", "0.0");//ND
	  aa_permis[1].put("M", "0.3");
	  aa_permis[1].put("F", "0.0");//absent
	  aa_permis[1].put("P", "0.0");
	  aa_permis[1].put("S", "0.6");
	  aa_permis[1].put("T", "0.3");
	  aa_permis[1].put("W", "0.1");
	  aa_permis[1].put("Y", "0.0");//ND
	  aa_permis[1].put("V", "0.0");//ND
	  aa_permis[1].put("X", "0.0"); // aa non défini
	  aa_permis[1].put("B", "0.25"); // B = N or D    B=Asx =(0.1+0.4)/2
	  aa_permis[1].put("Z", "0.0");  // Z = Q or E    Z=Glx =(0.0+0.0)/2

          aa_permis[2] = new Hashtable();
	  aa_permis[2].put("A", "1.0");
	  aa_permis[2].put("R", "0.0");//ND
	  aa_permis[2].put("N", "0.0");//ND
	  aa_permis[2].put("D", "0.1");
	  aa_permis[2].put("C", "0.0");
	  aa_permis[2].put("Q", "0.0");//ND
	  aa_permis[2].put("E", "0.0");
	  aa_permis[2].put("G", "0.7");
	  aa_permis[2].put("H", "0.0");
	  aa_permis[2].put("I", "0.0");//absent
	  aa_permis[2].put("L", "0.1");//ND selon Kodukula
	  aa_permis[2].put("K", "0.0");//ND
	  aa_permis[2].put("M", "0.0");//ND
	  aa_permis[2].put("F", "0.0");//absent
	  aa_permis[2].put("P", "0.0");
	  aa_permis[2].put("S", "0.3");
	  aa_permis[2].put("T", "0.1");
	  aa_permis[2].put("W", "0.0");
	  aa_permis[2].put("Y", "0.0");//ND
	  aa_permis[2].put("V", "0.1");
	  aa_permis[2].put("X", "0.0"); // aa non défini
	  aa_permis[2].put("B", "0.05"); // B = N or D    B=Asx =(0.1+0.0)/2
	  aa_permis[2].put("Z", "0.0"); // Z = Q or E    Z=Glx =(0.0+0.0)/2

*/
          aa_permis[0] = new Hashtable();
	  aa_permis[0].put("A", "0.005");
	  aa_permis[0].put("R", "0.01");//ND
	  aa_permis[0].put("N", "0.19");
	  aa_permis[0].put("D", "0.13");
	  aa_permis[0].put("C", "0.04");
	  aa_permis[0].put("Q", "0.01");//0 selon Kodukula
	  aa_permis[0].put("E", "0.0");
	  aa_permis[0].put("G", "0.12");
	  aa_permis[0].put("H", "0.0");//ND
	  aa_permis[0].put("I", "0.005");//absent
	  aa_permis[0].put("L", "0.0");
	  aa_permis[0].put("K", "0.01");
	  aa_permis[0].put("M", "0.00");
	  aa_permis[0].put("F", "0.000");//absent
	  aa_permis[0].put("P", "0.000");
	  aa_permis[0].put("S", "0.430");
	  aa_permis[0].put("T", "0.010");
	  aa_permis[0].put("W", "0.000");
	  aa_permis[0].put("Y", "0.000");
	  aa_permis[0].put("V", "0.005");
	  aa_permis[0].put("X", "0.000"); // aa non défini
	  aa_permis[0].put("B", "0.320");  // B = N or D    B=Asx =(0.8+0.4)/2
	  aa_permis[0].put("Z", "0.010"); // Z = Q or E    Z=Glx =(0.1+0.0)/2
	  aa_permis[0].put("U", "0.04");  // U = sélénocystéine (très rare : 74 dans tout swissprot. Associé à la Cystéine =C)

          aa_permis[1] = new Hashtable();
	  aa_permis[1].put("A", "0.180");
	  aa_permis[1].put("R", "0.02");//ND
	  aa_permis[1].put("N", "0.04");//+
	  aa_permis[1].put("D", "0.02");
	  aa_permis[1].put("C", "0.01");
	  aa_permis[1].put("Q", "0.01");//ND
	  aa_permis[1].put("E", "0.01");//+
	  aa_permis[1].put("G", "0.20");//+
	  aa_permis[1].put("H", "0.02");//ND
	  aa_permis[1].put("I", "0.01");//absent
	  aa_permis[1].put("L", "0.01");//ND
	  aa_permis[1].put("K", "0.00");//ND
	  aa_permis[1].put("M", "0.0");
	  aa_permis[1].put("F", "0.01");//absent
	  aa_permis[1].put("P", "0.01");
	  aa_permis[1].put("S", "0.29");
	  aa_permis[1].put("T", "0.02");
	  aa_permis[1].put("W", "0.005");
	  aa_permis[1].put("Y", "0.0");//ND
	  aa_permis[1].put("V", "0.04");//ND
	  aa_permis[1].put("X", "0.0"); // aa non défini
	  aa_permis[1].put("B", "0.06"); // B = N or D    B=Asx =(0.1+0.4)/2
	  aa_permis[1].put("Z", "0.02");  // Z = Q or E    Z=Glx =(0.0+0.0)/2
	  aa_permis[1].put("U", "0.01");  // U = sélénocystéine (très rare : 74 dans tout swissprot. Associé à la Cystéine =C)

          aa_permis[2] = new Hashtable();
	  aa_permis[2].put("A", "0.3");
	  aa_permis[2].put("R", "0.02");//ND
	  aa_permis[2].put("N", "0.0");//ND
	  aa_permis[2].put("D", "0.01");
	  aa_permis[2].put("C", "0.0");
	  aa_permis[2].put("Q", "0.005");//ND
	  aa_permis[2].put("E", "0.0");
	  aa_permis[2].put("G", "0.17");
	  aa_permis[2].put("H", "0.005");
	  aa_permis[2].put("I", "0.02");//absent
	  aa_permis[2].put("L", "0.06");//ND selon Kodukula
	  aa_permis[2].put("K", "0.005");//ND
	  aa_permis[2].put("M", "0.10");//ND
	  aa_permis[2].put("F", "0.01");//absent
	  aa_permis[2].put("P", "0.0");
	  aa_permis[2].put("S", "0.20");
	  aa_permis[2].put("T", "0.04");
	  aa_permis[2].put("W", "0.0");
	  aa_permis[2].put("Y", "0.0");//ND
	  aa_permis[2].put("V", "0.01");
	  aa_permis[2].put("X", "0.0"); // aa non défini
	  aa_permis[2].put("B", "0.01"); // B = N or D    B=Asx =(0.1+0.0)/2
	  aa_permis[2].put("Z", "0.005"); // Z = Q or E    Z=Glx =(0.0+0.0)/2
	  aa_permis[2].put("U", "0.0");  // U = sélénocystéine (très rare : 74 dans tout swissprot. Associé à la Cystéine =C)


          //clivage des derniers (NBR_AA_ANALYSES) aa de la séquence (ces aa servent à calculer le profil d'hydrophobicité)
          if (la_sequence.length()-NBR_AA_ANALYSES<0) {
            sequence = "";
          } else {
            sequence = la_sequence.substring(la_sequence.length()-NBR_AA_ANALYSES,la_sequence.length());
          }//end if

          //inverse la fin de la séquence
          sequence = inversion_sequ(sequence);

          nbr_ancres=0;
          // calcul des ancres possibles dans l'intervalle C-terminal -> 40 après C-terminal
          for (i=debut; i<sequence.length()-2; i++) {
            if ( in_w2(sequence.charAt(i)) && in_w(sequence.charAt(i+2)) ) {
//              System.out.print("\n  GPI-anchor potential at "+(la_sequence.length()-(i+2))+"  ("+(i+2)+" du C-term)");
              position[nbr_ancres]=i+2;

            float val_w  = 0;
            float val_w1 = 0;
            float val_w2 = 0;
            try {
   	      val_hydro = String.valueOf(aa_permis[0].get(sequence.substring(i+2, i+1+2)));
              val_w = Float.valueOf(val_hydro).floatValue();
            }
            catch (Exception e) {
              System.out.println("car pas attendu : "+aa_permis[0].get(sequence.substring(i+2, i+1+2)));
              throw new Exception();
            }
            try {
	      val_hydro = String.valueOf(aa_permis[1].get(sequence.substring(i+1, i+1+1)));
              val_w1 = Float.valueOf(val_hydro).floatValue();
            }
            catch (Exception e) {
              System.out.println("car pas attendu : "+aa_permis[1].get(sequence.substring(i+1, i+1+1)));
              throw new Exception();
            }
            try {
	      val_hydro = String.valueOf(aa_permis[2].get(sequence.substring(i, i+1)));
              val_w2 = Float.valueOf(val_hydro).floatValue();
            }
            catch (Exception e) {
              System.out.println("car pas attendu : "+aa_permis[2].get(sequence.substring(i, i+1)));
              throw new Exception();
            }

            float chances;
            //chances = (val_w + val_w2 + val_w1)/3;
            chances = val_w * val_w2 *val_w1;
            //System.out.print ("  "+chances+"  ");
            position_pourcent[nbr_ancres]= chances*100;


              
//	    if (sequence.charAt(i+1) != 'P' && sequence.charAt(i+1) != 'W') {
	      if (chances>0.001 ) {
//                System.out.println("  GPI-anchor potential at "+(la_sequence.length()-(i+2))+"  ("+(i+2)+" du C-term), "+chances*100+"%");
                position[nbr_ancres]=i+2;
                nbr_ancres++;
 	      }
//            }

            }//end if
          }//end for
//System.out.println("");


	}//end AncreGPI (constructeur)




	public String inversion_sequ(String sequence) {
          /*-------------------------------------------------------------------*/
	// inversion de la sequence
		String caractere;
		String sequ_inversee;
		
		sequ_inversee = " ";
		for (int i=sequence.length(); i>0; i--) {
			sequ_inversee = sequ_inversee + sequence.substring(i-1, i);			
		}

		sequ_inversee = sequ_inversee.substring(1, sequ_inversee.length());
		return (sequ_inversee);	
	}
	





}//end class Hydrophobicite