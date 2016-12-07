import java.util.Hashtable;
import java.util.Date;

// Diverses methodes sont disponibles pour executer divers traitements
// sur les sequences
public class Hydrophobicite {
	String sequence;
	boolean critere1, critere2, critere3;
        public static int NBR_AA_ANALYSES = 40;
        public static int NBR_DEBUT = 7;//nombre de premiers aa sur lesquels faire une moyenne (car les 2-3 aa C-terminaux peuvent être hydrophile, mais c'est normal)

        public int hydrophobe_median_length = 0;
        public int hydrophobe_length        = 0;

        public int hydrophile_median_length = 0;
        public int hydrophile_length        = 0;

        public int hydrophobe_eisenberg_length = 0;
        public int hydrophile_eisenberg_length = 0;


        public float[] profile_hydro = new float[NBR_AA_ANALYSES];
        public float[] fonction_hydro = new float[NBR_AA_ANALYSES];
        public float[] fonction_hydro_median = new float[NBR_AA_ANALYSES];



	public Hydrophobicite (String la_sequence, String methode) throws Exception {
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

          //clivage des derniers (NBR_AA_ANALYSES) aa de la séquence (ces aa servent à calculer le profil d'hydrophobicité)
          if (la_sequence.length()-NBR_AA_ANALYSES>0) {
            sequence = la_sequence.substring(la_sequence.length()-NBR_AA_ANALYSES,la_sequence.length());

            //inverse la fin de la séquence
            sequence = inversion_sequ(sequence);

 	    // echelle d'hydrophobicite de Kyte et Doolittle
            //(http://www.expasy.ch/cgi-bin/protscale.pl)
	    Hashtable TabHydro = new Hashtable();
	    TabHydro.put("A", "1.8");
	    TabHydro.put("R", "-4.5");
	    TabHydro.put("N", "-3.5");
	    TabHydro.put("D", "-3.5");
	    TabHydro.put("C", "2.5"); 
	    TabHydro.put("Q", "-3.5");
	    TabHydro.put("E", "-3.5");
	    TabHydro.put("G", "-0.4");
	    TabHydro.put("H", "-3.2");
	    TabHydro.put("I", "4.5"); 
	    TabHydro.put("L", "3.8"); 
	    TabHydro.put("K", "-3.9");
	    TabHydro.put("M", "1.9"); 
	    TabHydro.put("F", "2.8"); 
	    TabHydro.put("P", "-1.6");
	    TabHydro.put("S", "-0.8");
	    TabHydro.put("T", "-0.7");
	    TabHydro.put("W", "-0.9");
	    TabHydro.put("Y", "-1.3");
	    TabHydro.put("V", "4.2"); 
  	    TabHydro.put("X", "0.0"); // aa non défini
	    TabHydro.put("B", "-3.5"); // B = N or D    B=Asx
	    TabHydro.put("Z", "-3.5"); // Z = Q or E    Z=Glx


            //calcul de l'hydrophobicité des aa de la séquence
	    for (i=0; i<sequence.length(); i++) {
              try {
  	        val_hydro = String.valueOf(TabHydro.get(sequence.substring(i, i+1)));
                profile_hydro[i] = Float.valueOf(val_hydro).floatValue();
              }
              catch (Exception e) {
                System.out.println("Exception (Hydrophobicite.java/acces Hashtable) : \n"+
                                    "  car. pas attendu : "+sequence.substring(i,i+1));
                profile_hydro[i]=0;
                throw new Exception();

              }
            }

            if (methode.indexOf("median")!=-1) {
              //calcul du profil d'hydrophobicité avec un filtre passe-bas (sliding-window, diminue l'influence des hautes fréquences) 
              calcMedian(15);
            }
            if (methode.indexOf("passe-bas")!=-1) {
              //calcul du profil d'hydrophobicité avec un filtre médian (élimine les hautes fréquences et augmente les flancs)
              calcPasseBas(15);
            }

            //Amino acid scale: Normalized consensus hydrophobicity scale. 
            //Author(s): Eisenberg D., Schwarz E., Komarony M., Wall R. 
            //Reference: J. Mol. Biol. 179:125-142(1984).  (http://www.expasy.ch/tools/pscale/Hphob.Eisenberg.html)
	    TabHydro.put("A", "0.620"); //  Ala:  0.620  
	    TabHydro.put("R", "-2.53"); // Arg: -2.530  
	    TabHydro.put("N", "-0.78"); // Asn: -0.780  
	    TabHydro.put("D", "-0.9");  //Asp: -0.900  
	    TabHydro.put("C", "0.290"); //  Cys:  0.290  
	    TabHydro.put("Q", "-0.850");//  Gln: -0.850  
	    TabHydro.put("E", "-0.740");//  Glu: -0.740  
	    TabHydro.put("G", "-0.48"); //Gly:  0.480  
	    TabHydro.put("H", "-0.4");  //His: -0.400  
	    TabHydro.put("I", "1.38");  // Ile:  1.380  
	    TabHydro.put("L", "1.06");  // Leu:  1.060  
	    TabHydro.put("K", "-1.5");  //Lys: -1.500  
	    TabHydro.put("M", "0.64");  //  Met:  0.640  
	    TabHydro.put("F", "1.19");  // Phe:  1.190  
	    TabHydro.put("P", "0.12");  //Pro:  0.120  
	    TabHydro.put("S", "-0.180");//  Ser: -0.180  
	    TabHydro.put("T", "-0.05"); // Thr: -0.050  
	    TabHydro.put("W", "0.810"); // Trp:  0.810  
	    TabHydro.put("Y", "-1.3");  //Tyr:  0.260  
  	    TabHydro.put("V", "4.2");   //Val:  1.080 
  	    TabHydro.put("X", "0.0"); // aa non défini
	    TabHydro.put("B", "-3.5"); // B = N or D    B=Asx
	    TabHydro.put("Z", "-3.5"); // Z = Q or E    Z=Glx

/*            //calcul de l'hydrophobicité des aa de la séquence
	    for (i=0; i<sequence.length(); i++) {
	      val_hydro = String.valueOf(TabHydro.get(sequence.substring(i, i+1)));
              profile_hydro[i] = Float.valueOf(val_hydro).floatValue();
            }

            if (methode.indexOf("eisenberg")!=-1) {
              //calcul du profil d'hydrophobicité avec un filtre médian (élimine les hautes fréquences et augmente les flancs)
              calcPasseBasEisenberg(15);
            }
*/

/*            for (i=0; i<NBR_AA_ANALYSES; i++) {
              System.out.println(i+"  "+profile_hydro[i]+"  "+fonction_hydro[i]+"  "+fonction_hydro_median[i]);
            }*/
          }//end if
	}//end Hydrophobicite (constructeur)




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
	

        public float[] sort(float[] tableau) {
          /*-------------------------------------------------------------------*/
          //trie un tableau de float (tri à bulle)
          // peut être optimisé en triant la moitié du tableau +1
          for (int i=0; i<tableau.length; i++) {
            for (int j=i+1; j<tableau.length; j++) {
              if (tableau[i]>tableau[j]) {
                float tmp = tableau[i];
                tableau[i] = tableau[j];
                tableau[j] = tmp;
              }//end if
            }//end for (j)
          }//end for (i)
          return tableau;
        }//end method sort




        public void calcPasseBas(int largeur_fenetre) {
          /*-------------------------------------------------------------------*/
          int i;
          int partie_gche = largeur_fenetre / 2;
          int partie_drte = largeur_fenetre - partie_gche - 1;
          int nbr_elem;
          float somme;

          // filtre passe bas (sliding window)
	  for (i=0; i<sequence.length(); i++) {
            nbr_elem = 0;
            somme = 0;
	    for (int j=i-partie_gche; j<=i+partie_drte; j++) {
              if ((j>=0) && (j<NBR_AA_ANALYSES)) {
                somme += profile_hydro[j];
                nbr_elem++;
              }//end if
            }//end for
            fonction_hydro[i] = somme / nbr_elem;
          }//end for


          //calcul de la longueur de la partie hydrophobe (filtre passe-bas)
          somme = 0;
          for (i=0; i< NBR_DEBUT; i++) {
            somme += fonction_hydro[i];
          }
          somme = somme / NBR_DEBUT;
          for (i=0; i<NBR_DEBUT; i++) {
            fonction_hydro[i] = somme;
          }

          //calcul de la longueur de la partie hydrophobe (filtre passe-bas)
          while ((hydrophobe_length<NBR_AA_ANALYSES) &&
                 (fonction_hydro[hydrophobe_length]>=0) ) hydrophobe_length++;

          //calcul de la longueur de la partie hydrophile (filtre passe-bas)
          while ((hydrophile_length+hydrophobe_length<NBR_AA_ANALYSES) &&
                 (fonction_hydro[hydrophile_length+hydrophobe_length]<=0)        ) hydrophile_length++;

//           System.out.println("hydrophobe_length="+hydrophobe_length);
//           System.out.println("hydrophile_length="+hydrophile_length);

        }//end calcPasseBas


        public void calcPasseBasEisenberg(int largeur_fenetre) {
          /*-------------------------------------------------------------------*/
          int i;
          int partie_gche = largeur_fenetre / 2;
          int partie_drte = largeur_fenetre - partie_gche - 1;
          int nbr_elem;
          float somme;

          // filtre passe bas (sliding window)
	  for (i=0; i<sequence.length(); i++) {
            nbr_elem = 0;
            somme = 0;
	    for (int j=i-partie_gche; j<=i+partie_drte; j++) {
              if ((j>=0) && (j<NBR_AA_ANALYSES)) {
                somme += profile_hydro[j];
                nbr_elem++;
              }//end if
            }//end for
            fonction_hydro[i] = somme / nbr_elem;
          }//end for


          //calcul de la longueur de la partie hydrophobe (filtre passe-bas)
          somme = 0;
          for (i=0; i< NBR_DEBUT; i++) {
            somme += fonction_hydro[i];
          }
          somme = somme / NBR_DEBUT;
          for (i=0; i<NBR_DEBUT; i++) {
            fonction_hydro[i] = somme;
          }

          //calcul de la longueur de la partie hydrophobe (filtre passe-bas)
          while ((hydrophobe_eisenberg_length<NBR_AA_ANALYSES) &&
                 (fonction_hydro[hydrophobe_eisenberg_length]>=0) ) hydrophobe_eisenberg_length++;

          //calcul de la longueur de la partie hydrophile (filtre passe-bas)
          while ((hydrophile_eisenberg_length+hydrophobe_eisenberg_length<NBR_AA_ANALYSES) &&
                 (fonction_hydro[hydrophile_eisenberg_length+hydrophobe_eisenberg_length]<=0)        ) hydrophile_eisenberg_length++;

//           System.out.println("hydrophobe_length="+hydrophobe_length);
//           System.out.println("hydrophile_length="+hydrophile_length);

        }//end calcPasseBasEisenberg




	// calcul de l'hydrophobicite du peptide
	public void calcMedian(int largeur_fenetre) {
          /*-------------------------------------------------------------------*/
          int i;
          int partie_gche = largeur_fenetre / 2;
          int partie_drte = largeur_fenetre - partie_gche - 1;
          float[] tmp_median = new float[largeur_fenetre];
          float somme;

          // filtre médian
          for (i=0; i<NBR_AA_ANALYSES; i++) {
            somme = 0;
            for (int j=i-partie_gche; j<=i+partie_drte; j++) {
	      if ((j>=0) && (j<NBR_AA_ANALYSES)) {
                tmp_median[j-(i-partie_gche)] = profile_hydro[j];
              } else {
                tmp_median[j-(i-partie_gche)] = 999;
              }//end if
	    }//end for
            tmp_median = sort(tmp_median);

            fonction_hydro_median[i] = tmp_median[partie_gche];
	  }//end for


          //moyenne des NBR_DEBUT premiers aa C-terminaux pour modérer la partie hydrophile
          somme = 0;
          for (i=0; i< NBR_DEBUT; i++) {
            somme += fonction_hydro_median[i];
          }
          somme = somme / NBR_DEBUT;
          for (i=0; i<NBR_DEBUT; i++) {
            fonction_hydro_median[i] = somme;
          }

          //calcul de la longueur de la partie hydrophobe (filtre médian)
          while ((hydrophobe_median_length<NBR_AA_ANALYSES) &&
                 (fonction_hydro_median[hydrophobe_median_length]>=0)  ) hydrophobe_median_length++;

          //calcul de la longueur de la partie hydrophile (filtre médian)
          while ((hydrophile_median_length+hydrophobe_median_length<NBR_AA_ANALYSES) &&
                 (fonction_hydro_median[hydrophile_median_length+hydrophobe_median_length]<=0) ) hydrophile_median_length++;

//          System.out.println("hydrophobe_median_length="+hydrophobe_median_length);
//          System.out.println("hydrophile_median_length="+hydrophile_median_length);

	}//end calcMedian

}//end class Hydrophobicite