import java.util.Vector;
import javax.swing.JEditorPane;
import java.awt.*;
import java.awt.event.*;

class History extends Panel implements ActionListener {


  Vector v = new Vector();
  int position =0;
  JEditorPane p;  
  Button bt_back    = new Button("Back");
  Button bt_forward = new Button("Forward");

  public History(JEditorPane p) {
    this.p = p;
    this.setLayout(new FlowLayout());
    this.add(bt_back);
    this.add(bt_forward);
    bt_back.addActionListener(this);
    bt_forward.addActionListener(this);
  }// end constructeur
  

  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    
    if (source == bt_back) {
      back();
    } else if (source == bt_forward) {
      forward();
    } else {
      // source inconnue => erreur
      System.out.println("History : erreur actionPerformed");
    }//end if
  }//end actionPerformed

  
  
  public void back() {
    // change le contenu du JEditorPane avec la page précédente si il y en a une
    //System.out.println("back:");
    
    if (position!=0) {
      // effectuer le back seulement si on n'est pas au début de l'history buffer
      position--;
      //System.out.println(position);
      charger_page();
      p.repaint();
    }//end if
  }//end back
  
  public void forward() {
    // change le contenu de la page avec le contenu suivant si il y en a
    //System.out.println("forward:");
    if (position<v.size()-1) {
      // il y a une page suivante => on la charge
      position++;
      //System.out.println(position);
      charger_page();
    }//end if
  }//end forward


  private void charger_page() {
    // charge la page de l'historique pointée par l'attribut 'position'
    HistoryElement h = (HistoryElement)v.elementAt(position);
      
    if (h.type == HistoryElement.TYPE_URL) {
      // la page est d'une source URL => changer l'URL du JEditorPane
      try{p.setPage(h.content);}catch (Exception e) {/*e.printStackTrace(); 	System.out.println("History : page not found");*/}
    } else {
      // la page est d'une source code HTML => changer le contenu de la page
      try{p.setText(h.content);}catch (Exception e) {}
    }// end if
  }//end charger_page  


  
  public void add(String content, int type) {
    // ajoute une nouvelle page à l'history
    // type doit être soit HistoryElement.TYPE_UTL soit HistoryElement.TYPE_TEXT
    if ( (type == HistoryElement.TYPE_URL) || (type == HistoryElement.TYPE_TEXT)) {
      // type valide => ajouter la page 
      v.setSize(position); // vider le reste l'history à partir de la position courante...
      v.add(new HistoryElement(type, content)); // ... et ajouter la page à la fin du vecteur
      position++;
    } else {
      // type non valide => message d'erreur
      System.out.println("HistoryElement : erreur, type de contenu non valide");
    }//end if
  }//end add
  


  
  public void removeAll() {
    // vider l'history
    v.clear();
  }// removeAll
  
  
  
}//end class