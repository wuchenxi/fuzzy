import java.io.*;
import java.util.*;

public class Fuzzy
{
    public static BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
    public static void main(String[] args){
	try{
	    if(args[0].equals("-h")){
		Table file=get_file(args[1]);
		file.print_header();
	    }
	    else if(args[0].equals("-c")){
		int idx_left=Integer.parseInt(args[1])-1;
		int idx_right=Integer.parseInt(args[2])-1;
		Table file_left=get_file(args[3]);
		Table file_right=get_file(args[4]);
		ArrayList<SortedSet <String>> leftcol=tokenize(file_left.get_col(idx_left));
		ArrayList<SortedSet <String>> rightcol=tokenize(file_right.get_col(idx_right));
		Map<String, Integer> word_freq=get_freq(rightcol);
		double [] matching_score=new double[rightcol.size()];
		int [] matching_result=new int[leftcol.size()];
		for(int i=0; i<leftcol.size(); i++){
		    for(int j=0; j<rightcol.size(); j++){
			matching_score[j]=get_score(leftcol.get(i), rightcol.get(j), word_freq);
		    }
		    matching_result[i]=interact(i, matching_score, file_left, file_right);
		}
		BufferedWriter out=new BufferedWriter(new FileWriter(args[5]));
		String out_str=""+matching_result[0];
		for(int i=1; i<leftcol.size(); i++){
		    out_str+=","+matching_result[i];
		}
		out.write(out_str);
	    }
	}catch(IOException e){
	    e.printStackTrace();
	}	    
    }

    private static int interact(int i, double [] score, Table left, Table right) throws IOException {
	int r=-1;
	while(true){
	    int max_id=0;
	    double max_val=0;
	    for(int j=0; j<score.length; j++){
		if(score[j]>max_val){
		    max_id=j;
		    max_val=score[j];
		}
	    }	    
	    System.out.println("Row on Left Table:");
	    left.print_row(i);
	    System.out.println("Row on Right Table:");
	    right.print_row(max_id);
	    String in=input("y: yes, n: no, d: done");
	    char c=in.charAt(0);
	    if(c=='y'){
		 return max_id;
	    }
	    else if(c=='d'){
	        return -1;
	    }
	    else{
	        score[max_id]=-1;
	    }	    
	}
    }

    

    private static double get_score(SortedSet<String> set1, SortedSet<String> set2, Map<String, Integer> freq){
	SortedSet<String> intersection=new TreeSet<String>(set1);
	intersection.retainAll(set2);
	return (2.0*weighted_count(intersection, freq))/(weighted_count(set1, freq)+weighted_count(set2, freq));
    }

    private static ArrayList<SortedSet <String>> tokenize(ArrayList<String> ls){
	ArrayList<SortedSet <String>> r=new ArrayList<SortedSet <String>> ();
	for(int i=0; i<ls.size(); i++){
	    String s=ls.get(i);
	    String tmp="";
	    SortedSet<String>cur= new TreeSet<String>();
	    for(int j=0; j<s.length(); j++){
		char c=s.charAt(j);
		if(c>='0' && c<='9')
		    tmp+=c;
		else if(c>='a' && c<='z')
		    tmp+=(char)(c+('A'-'a'));
		else if(c>='A' && c<='Z')
		    tmp+=c;
		else if(tmp.length()>0){
		    cur.add(tmp);
		    tmp="";
		}		    
	    }
	    if(tmp.length()>0){
		cur.add(tmp);
	    }
	    r.add(cur);
	}
	return r;
    }
    
    private static Table get_file(String fn) throws IOException {
	ArrayList<ArrayList <String>> c=new ArrayList<ArrayList <String>>();
	BufferedReader br=new BufferedReader(new FileReader(new File(fn)));
	ArrayList <String> h=read_row(br);
	while(true){
	    ArrayList<String> row= read_row(br);
	    if(row.size()>0)
		c.add(row);
	    else
		break;
	}
	br.close();
	return new Table(h, c);
    }

    private static String input(String prompt) throws IOException {
	System.out.println(prompt);
	String r=stdin.readLine();
	return r;
    }
	
    private static ArrayList<String> read_row(BufferedReader file) throws IOException {
	ArrayList<String> out=new ArrayList<String> ();
	int t;
	String tmp="";
	boolean f=true;
	while((t=file.read())!=-1){
	    char c=(char)t;
	    switch(c){
	    case ',':
		out.add(tmp);
		tmp="";
		break;
	    case '\n':
		f=false;
		out.add(tmp);
		break;
	    case '"':
		int t1;
		tmp="";
		while((t1=file.read())!=(int)'"'){
		    tmp+=(char)t1;
		}
		break;
	    default:
		tmp+=(char)t;
		break;
	    }
	    if(f==false)
		break;
	}
	return out;
    }


    private static double weighted_count(SortedSet <String> p, Map<String, Integer> freq){
	double r=0;
	Iterator iter=p.iterator();
	while(iter.hasNext()){
	    String s=(String)iter.next();
	    if(freq.containsKey(s)){
		r+=1.0/freq.get(s);
	    }
	    else{
		r+=1.0;
	    }
	}
	return r;
    }

    private static Map<String, Integer> get_freq(ArrayList<SortedSet <String>> col){
	Map<String, Integer> r=new HashMap<String, Integer>();
	for(int i=0; i<col.size(); i++){
	    Iterator iter=col.get(i).iterator();
	    while(iter.hasNext()){
		String word=(String) iter.next();
		if(r.containsKey(word)){
		    r.replace(word, r.get(word)+1);
		}
		else{
		    r.put(word, 1);
		}
	    }
	}
	return r;
    }
}

class Table{
    public ArrayList <String> header;
    public ArrayList<ArrayList <String>> content;

    Table(ArrayList<String> h, ArrayList<ArrayList <String>>c){
	header=h;
	content=c;
    }

    public void print_header(){
	for(int i=0; i<header.size(); i++){
	    System.out.println((i+1)+":"+header.get(i));
	}
    }

    public void print_row(int c){
	ArrayList <String> row=content.get(c);
	String out=row.get(0);
	for(int i=1;i<row.size();i++){
	    out+="; "+row.get(i);
	}
	System.out.println(out);
    }
    
    public ArrayList<String> get_col(int c){
	ArrayList<String> out=new ArrayList<String> ();
	for(int i=0; i<content.size(); i++){
	    out.add(content.get(i).get(c));
	}
	return out;
    }

    public String header_to_str(){
	String out="\""+header.get(0)+"\"";
	for(int i=1; i<header.size(); i++){
	    out+=",\""+header.get(i)+"\"";
	}
	return out;
    }

    public String row_to_str(int c){
	ArrayList <String> row=content.get(c);
	String out="\""+row.get(0)+"\"";
	for(int i=1; i<row.size(); i++){
	    out+=",\""+row.get(i)+"\"";
	}
	return out;
    }
}
