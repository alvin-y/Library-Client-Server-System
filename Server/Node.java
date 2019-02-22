public class Node{
    public String isbn;
    public int year;
    public String title;
    public String pub;
    public String auth;

    public Node() {
    }
    
    public Node(String i, int y, String t, String p, String a) {
        isbn = i;
        year = y;
        title = t;
        pub = p;
        auth = a;
    }
}