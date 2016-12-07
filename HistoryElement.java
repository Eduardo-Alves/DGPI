class HistoryElement {

  public static final int TYPE_URL = 0;
  public static final int TYPE_TEXT = 1;
  
  String content;
  int type;
  
  public HistoryElement(int type, String content) {
    this.content = content;
    this.type = type;
  }//end constructeur
}//end class