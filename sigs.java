import java.lang.Math;


public class sigs {

    public static final int MAX_LENGTH = 40;
    public boolean has_Nterm_signal = false;
    public double score_max = 0;
    public int pos_site_clivage = 1;

    double[][][] mat = //new double[2][23][25];
     {

     // Prokaryotic signal sequences matrix.
     { { 1.14, 0.92, 0.92, 1.03, 0.63, 0.78, 0.45, 0.63, 0.78, 0.78, 2.01,-0.47, 2.27, 1.73, 0.22},
       {-0.53,-0.53,-0.53,-0.53,-0.53,-0.53,-0.53,-0.53,-0.53,-0.53,-3.58,-0.53,-3.58,-0.53,-0.53},
       {-0.47,-0.47,-0.47,-0.47,-0.47,-0.47,-0.47,-0.47,-0.47,-0.47,-3.58, 0.63,-3.58,-0.47, 0.92},
       {-0.69,-0.69,-0.69,-0.69,-0.69,-0.69,-0.69,-0.69,-0.69,-0.69,-3.58,-0.69,-3.58, 0.00, 1.39},
       { 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00,-3.58, 0.00,-3.58, 0.00, 0.00},
       {-0.34,-0.34,-0.34,-0.34,-0.34,-0.34,-0.34,-0.34, 0.36, 0.36,-3.58, 0.76,-3.58,-0.34,-0.34},
       {-0.79,-0.79,-0.79,-0.79,-0.79,-0.79,-0.79,-0.79,-0.79,-0.79,-3.58,-0.79,-3.58, 0.60, 1.29},
       { 0.39,-0.30,-0.30,-0.30, 0.11, 0.62,-0.30, 0.39,-0.30,-0.30,-3.58,-0.30,-0.30,-0.99,-0.99},
       { 0.22, 0.22, 0.22, 0.22, 0.22, 0.22, 0.22, 0.22, 0.22, 0.22,-3.58, 2.17,-3.58, 0.22, 0.22},
       { 0.57,-0.53, 1.08,-0.53, 1.08,-0.53,-0.53, 0.57,-0.53,-0.53,-3.58,-0.53,-3.58,-0.53, 0.16},
       { 1.09, 1.40, 1.20, 1.09, 1.20, 1.57,-0.99,-0.99,-0.30,-0.30,-0.99,-0.30,-3.58,-0.99,-0.99},
       {-0.92,-0.92,-0.92,-0.92,-0.92,-0.92,-0.92,-0.92,-0.92,-0.92,-3.58,-0.22,-3.58, 0.18,-0.92},
       { 0.51, 1.20, 0.51, 0.51, 1.61, 1.20, 1.61, 0.51, 0.51, 1.20,-3.58, 1.90,-3.58, 0.51, 0.51},
       { 0.43, 1.12, 0.84, 1.12,-0.26,-0.26, 1.82,-0.26, 1.12,-0.26,-3.58, 1.68,-3.58,-0.26,-0.26},
       {-0.53,-0.53,-0.53,-0.53,-0.53,-0.53, 0.16, 0.57, 1.08, 0.16,-3.58,-0.53,-3.58,-0.53, 1.08},
       {-0.96,-0.96,-0.96, 0.43, 0.43,-0.96, 0.65, 1.75, 0.65, 1.12, 0.65,-0.26,-0.26,-0.96,-0.96},
       {-0.10,-0.79, 0.60,-0.10,-0.10,-0.10,-0.10,-0.10, 0.82,-0.79, 0.31,-0.79,-0.79,-0.79,-0.10},
       { 0.92, 0.92, 0.92, 0.92, 0.92, 0.92, 0.92, 0.92, 0.92, 0.92,-3.58, 0.92,-3.58, 0.92, 0.92},
       {-0.26,-0.26,-0.26,-0.26,-0.26,-0.26,-0.26,-0.26,-0.26, 0.84,-3.58,-0.26,-3.58,-0.26,-0.26},
       { 0.69, 1.03,-0.92, 0.18,-0.92, 0.47, 1.03,-0.92,-0.92, 0.47, 0.18,-0.92,-3.58,-0.22,-0.92},
       {-0.69,-0.69,-0.69,-0.69,-0.69,-0.69,-0.69,-0.69,-0.69,-0.69,-3.58,-0.69,-3.58,-0.47, 0.92},
       {-0.79,-0.79,-0.79,-0.79,-0.79,-0.79,-0.79,-0.79,-0.79,-0.79,-3.58,-0.79,-3.58,-0.34,-0.34},
       { 0.02,-0.02,-0.02, 0.00, 0.02,-0.03, 0.05,-0.04, 0.04, 0.02,-2.58, 0.07,-2.82,-0.14, 0.02}},

    // Eukaryotic signal sequences matrix.

     { { 0.10,-0.11,-0.04, 0.03, 0.32, 0.22, 0.22, 0.16, 0.54, 0.03, 1.18,-0.88, 1.71, 0.22,-0.88},
       {-1.34,-2.03,-2.03,-2.03,-2.03,-2.03,-2.03,-2.03,-0.08,-0.64,-5.08, 0.68,-5.08, 0.46, 0.17},
       {-1.96,-1.96,-1.96,-1.96,-1.96,-1.96,-1.96,-1.96,-0.86,-0.86,-5.08, 0.34,-5.08,-0.57,-0.01},
       {-2.19,-2.19,-2.19,-2.19,-2.19,-2.19,-2.19,-2.19,-0.58,-1.09,-5.08,-0.58,-5.08, 0.12, 0.21},
       {-0.41, 0.29, 0.69, 0.44, 0.69, 1.13, 0.29, 0.58, 0.11, 0.29, 1.44,-0.41, 0.69, 0.58,-0.41},
       {-1.84,-1.84,-1.84,-1.84,-1.84,-0.05,-1.84,-1.84, 0.46, 0.24,-5.08, 1.05,-0.74, 1.10, 0.46},
       {-2.30,-2.30,-2.30,-2.30,-2.30,-2.30,-2.30,-2.30,-1.20,-0.36,-5.08,-0.36,-5.08, 0.26, 0.34},
       {-1.11,-1.11,-1.39,-0.70,-1.39, 0.07,-1.39,-1.80, 0.45, 1.03,-0.88,-0.55, 1.17,-0.19,-0.55},
       {-1.22,-1.22,-1.22,-1.22,-1.22,-1.22,-1.22,-1.22, 0.39,-1.22,-5.08, 0.57,-5.08, 0.16,-0.53},
       { 0.71, 0.71, 0.08,-0.21, 0.40,-0.39,-0.62, 0.08,-0.39,-2.00, 0.30,-0.39,-5.08, 0.08,-0.06},
       { 1.77, 1.73, 1.78, 1.88, 1.86, 1.31, 1.67, 1.40,-0.19, 0.64,-0.41, 0.50,-2.49,-0.41,-1.11},
       {-2.42,-2.42,-2.42,-2.42,-2.42,-2.42,-2.42,-2.42,-2.42,-1.04,-5.08,-1.73,-5.08,-0.03,-0.23},
       {-0.99, 0.11, 0.95, 0.39,-0.99, 0.80,-0.30,-0.30,-0.99,-0.99,-5.08,-0.99,-5.08,-0.99,-0.30},
       { 0.84, 0.47, 0.68, 0.68, 0.07, 0.22, 1.17, 0.84,-0.34,-0.11,-5.08, 0.84,-5.08, 0.07,-0.34},
       {-1.31,-2.00,-1.31,-2.00,-2.00,-0.62,-2.00, 0.08, 0.99, 0.64,-5.08,-2.00,-0.90,-2.00, 1.09},
       {-0.24,-1.34,-0.35,-0.64, 0.13,-0.13, 0.27, 0.34, 0.82,-0.04, 0.70, 0.40, 0.56, 0.27,-0.13},
       {-1.58, 0.03,-0.66,-0.89,-0.66, 0.29,-0.33,-0.33, 0.21,-0.48, 0.56,-0.19,-0.48,-1.17, 0.03},
       { 0.80, 0.51, 0.51,-0.59,-0.59, 0.11, 1.20, 0.51,-0.59, 0.51,-5.08, 1.61,-5.08, 0.11,-0.59},
       {-1.72,-1.72,-0.34,-1.72,-1.72,-1.72,-0.62,-1.72,-1.72,-1.03,-5.08,-0.11,-5.08,-1.72, 0.22},
       { 0.59, 0.81, 0.30, 0.48, 0.16, 0.30,-0.01, 0.89,-2.41, 0.08, 1.06,-1.31,-5.08,-0.33, 0.43},
       {-2.19,-2.19,-2.19,-2.19,-2.19,-2.19,-2.19,-2.19,-0.86,-1.09,-5.08,-0.58,-5.08,-0.57,-0.01},
       {-2.30,-2.30,-2.30,-2.30,-2.30,-2.30,-2.30,-2.30,-1.20,-0.36,-5.08,-0.36,-5.08, 0.26, 0.34},
       {-0.79,-0.78,-0.65,-0.84,-0.88,-0.53,-0.72,-0.66,-0.39,-0.32,-2.85,-0.18,-3.07,-0.20,-0.11}}    };


  public sigs(String sequence, boolean is_eukariote){
    /*------------------------------------------------------------------------
     * Méthode : constructeur
     * 
     * Auteur : Julien Kronegg (jkronegg@bigfoot.com)
     * Source : sigs.bas, release 1.2, 1996, Amos Bairoch
     * Date   : 4 août 1999 (conversion QBasic -> Java)
     *----------------------------------------------------------------------*/

    int BORNE_RES1 = 100;
    double[][] res = new double[BORNE_RES1+1][2];
    int[] highb = {2,   2}; // borne sup de la fenêtre pour les prokariotes et les Eukariotes respectivement
    int[] lowb  = {-13, -13}; // borne inf de la fenêtre pour les Prokariotes et les Eukariotes respectivement
    int[] ranb = {highb[0]-lowb[0], highb[1]-lowb[1]}; // taille de la fenêtre pour les prokariotes et les eukariotes respectivement
    double[] cutof = {6.0, 2.0};
    int[] vmin = {-20, -40};
    int[] vmax = { 23,  24};
    int[] rang = {vmax[0]-vmin[0], vmax[1]-vmin[1]}; // ? pour les prokariotes et les eukariotes respectivement
    int[] value = new int[6000];

    // règle -1 -3 pour la détection du signal
    String rule1 = "ASGCTQ";
    String rule3 = "FHYWDERKNQ";

    //acides aminés pris en compte (20 aa courants + B, Z, X)
    String aacod1 = "ARNDCQEGHILKMFPSTWYVBZX";


/*
    System.out.println("Signal sequence detection for SWISS-PROT annotation.");
    System.out.println("Release 1.2 / December 1996.");
    System.out.println("");*/

    String n5 = "     ";
    String sigstr1 = "FT   SIGNAL        1              POTENTIAL.";
    String sigstr2 = "FT   CHAIN                        ";

    String cods;
    int typs;

    if (is_eukariote) {
      cods = "eukaryotic";
      typs = 1;
    } else {
      cods = "prokaryotic";
      typs = 0;
    }//end if
    //System.out.println(cods+"<br>");

    int i, j, k;
    boolean rule;
    double n;

//    System.out.println("Considered as "+cods+" origin.");

/*	'-------------------------------------------------------------------
    '
    ' Computation.
    '*/

      int lastpos = sequence.length()-ranb[typs]+1; //dernière position possible pour le site de clivage du signal
      if (lastpos>MAX_LENGTH) lastpos = MAX_LENGTH; // limite de la recherche du signal

      for (i=0; i<sequence.length(); i++) {  //optimisable en calculant i<lastpos+rand[typs]
        value[i] = aacod1.indexOf(sequence.charAt(i));
      }
      //
      // Scan curves and computation.
      //

      for (i=0; i<BORNE_RES1; i++) {
        res[i][0]=vmin[typs];
        //System.out.println(res[i][0]); // OK, -40
      }
      double minim=vmin[typs];

        for (i=0; i<lastpos; i++) {

          // calcul du score (somme des éléments de la matrice Prokariotes ou Eukariotes)
          n=0;
	  for (j=i; j<i+ranb[typs]; j++) {
            n += mat[typs] [value[j]] [j-i];
          }//end for (j)

          //tri de la valeur
          if (n>minim) {
            for (k=BORNE_RES1-1; k>=0; k--) {
              if (n>res[k][0]) {
                res[k+1][1]=res[k][1];//position potentielle du site de clivage
                res[k+1][0]=res[k][0];//score
              } else {
                break;
              } // end if
            }//end for (k)
            res[k+1][0]=((double)Math.round(n*100))/100;
            res[k+1][1]=i;
            minim=res[100][0];
          } // end if

	} // end for (i)

        //compte le nombre de sites potentiels dont le score est au dessus du seuil
        int above = 0;
        for (j=0;j<100;j++) {
          if (res[j][0]>=cutof[typs]) {
            above++;
          } else {
            break;
          } // end if
	}

        score_max = res[0][0];
	if (above==0) {
          /* aucun site potentiel pour le site de clivage (les sites potentiels ont un score trop faible)
           *    => un signal N-terminal sur cette proteine est très improbable */
/*          System.out.println("NO part of the scanned sequence gave a score above the cut-off value ("+cutof[typs]+")");
          System.out.println("The higest score obtained was: "+res[0][0]
			+" for residues "+res[0][1]+" to "+(res[0][1]+ranb[typs]-1)+".");*/
	} else {
          /* il y a au moins un site qui possède un score suffisant pour prétendre à être un site de clivage
           *    => il faut encore vérifier la règle -1, -3 pour ce(s) site(s). */
	  // 
	  //  Check for (-3, -1) rule for every residue obtenant un score supérieur au seuil.
	  // 
          for (j=0; j<above; j++) {

            // calcul de la position du site de clivage potentiel
            int n2=(int)Math.round(res[j][1]) + Math.abs(lowb[typs])-1;

            // vérification de la règle -1,-3
            rule=false;
            int ok = 0;
            if (rule1.indexOf(sequence.charAt(n2))!=-1) {
              ok = 1;
              if (rule3.indexOf(sequence.charAt(n2-2))==-1) {
                ok = 2;
                if (sequence.substring(n2-2,n2+1).indexOf('P')==-1) {
                  rule = true;
                }// end if
              } // end if
            } // end if

            // affichage du résultat
//            System.out.print("["+j+"] Score="+res[j][0]+"; Site ");
            if (rule==false) {
//              System.out.print("does NOT conform");
            } else {
//              System.out.print("conforms");
              has_Nterm_signal = true;
              if (pos_site_clivage==1) pos_site_clivage = n2; // on ne change la position que pour le score max
              // un signal N-term est probable
            }// end if
//            System.out.println(" to the (-3,-1) rule.");
//            System.out.println("  Signal from 1 to "+(n2+1));

            // génération de l'entrée SWISS-PROT (feature SIGNAL)
     /*       a$=n5$
            rset a$=str$(n2%)
            mid$(sigstr1$,23,5)=a$
            print #2,sigstr1$
            a$=n5$:
            rset a$=str$(n2%+1)
	    mid$(sigstr2$,16,5)=a$
            a$=n5$:
            rset a$=str$(slong%)
	    mid$(sigstr2$,23,5)=a$
            print #2,sigstr2$*/

          }//end for (j)
        }//end if (above==0)

  }//end constructeur

}// end class