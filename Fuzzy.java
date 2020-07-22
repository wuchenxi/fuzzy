import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;

public class Fuzzy
{
    public static BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
    private static int parse_args(int curarg, String [] args, ArrayList <Integer> idx_left, ArrayList <Integer> idx_right, ArrayList <Double> idx_weight, ArrayList <Integer> col_left, ArrayList <Integer> col_right){
	while(isint(args[curarg])){
	    idx_left.add(Integer.parseInt(args[curarg])-1);
	    idx_right.add(Integer.parseInt(args[curarg+1])-1);
	    idx_weight.add(Double.parseDouble(args[curarg+2]));
	    curarg+=3;
	}
	if(args[curarg].equals("-cl")){
	    curarg+=1;
	    while(isint(args[curarg])){
		col_left.add(Integer.parseInt(args[curarg])-1);
		curarg+=1;
	    }
	}
	if(args[curarg].equals("-cr")){
	    curarg+=1;
	    while(isint(args[curarg])){
	        col_right.add(Integer.parseInt(args[curarg])-1);
		curarg+=1;
	    }
	}
	return curarg;
    }
    
    public static void main(String[] args){
	try{
	    if(args[0].equals("-h")){
		Table file=get_file(args[1]);
		file.print_header();
	    }
	    else if(args[0].equals("-c")){
		ArrayList <Integer> idx_left=new ArrayList <Integer> ();
		ArrayList <Integer> idx_right=new ArrayList <Integer> ();
		ArrayList <Double> idx_weight=new ArrayList <Double> ();
		ArrayList <Integer> col_left=new ArrayList <Integer> ();
		ArrayList <Integer> col_right=new ArrayList <Integer> ();
		int curarg=1;
		curarg=parse_args(curarg, args, idx_left, idx_right, idx_weight, col_left, col_right);
		Table file_left=get_file(args[curarg]);
		Table file_right=get_file(args[curarg+1]);
		ArrayList <ArrayList<SortedSet <String>>> leftcols=new ArrayList <ArrayList<SortedSet <String>>>();
		ArrayList <ArrayList<SortedSet <String>>> rightcols=new ArrayList<ArrayList<SortedSet <String>>>();
		ArrayList <Map<String, Integer>> word_freqs=new ArrayList <Map<String, Integer>>();
		for(int i=0; i<idx_left.size(); i++){
		    leftcols.add(tokenize(file_left.get_col(idx_left.get(i))));
		    rightcols.add(tokenize(file_right.get_col(idx_right.get(i))));
		    word_freqs.add(get_freq(rightcols.get(i)));
		}
		double [] matching_score=new double[rightcols.get(0).size()];
		int [] matching_result=new int[leftcols.get(0).size()];
		for(int i=0; i<leftcols.get(0).size(); i++){
		    for(int j=0; j<rightcols.get(0).size(); j++){
			matching_score[j]=0;
			for(int k=0; k<idx_left.size(); k++){
			    matching_score[j]+=get_score(leftcols.get(k).get(i), rightcols.get(k).get(j), word_freqs.get(k))*idx_weight.get(k);
			}
		    }
		    matching_result[i]=interact(i, matching_score, file_left, file_right, col_left, col_right);
		}
		BufferedWriter out=new BufferedWriter(new FileWriter(args[curarg+2]));
		String out_str=""+matching_result[0];
		for(int i=1; i<leftcols.get(0).size(); i++){
		    out_str+=","+matching_result[i];
		}
		out.write(out_str);
	    }
	}catch(IOException e){
	    e.printStackTrace();
	}	    
    }

    private static boolean isint(String str){
	return str.matches("\\d+");
    }

    private static int interact(int i, double [] score, Table left, Table right, ArrayList <Integer> col_left, ArrayList <Integer> col_right) throws IOException {
	int r=-1;
	while(true){
	    int [] max_id=new int [10];
	    double [] max_val=new double [10];
	    for(int k=0; k<10; k++){
		max_id[k]=0;
	        max_val[k]=-1;
	        for(int j=0; j<score.length; j++){
		    if(score[j]>max_val[k]){
			boolean found=false;
			for(int l=0; l<k; l++){
			    if(max_id[l]==j){
				found=true;
				break;
			    }
			}
			if(found==false){
			    max_id[k]=j;
			    max_val[k]=score[j];
			}
		    }
		}
	    }
	    String txt="Left Table:\n"+left.row_to_str_sel(i, col_left)+"\nRight Table:";
	    for(int k=0; k<10; k++){
		txt+="\n"+k+":"+right.row_to_str_sel(max_id[k], col_right);
	    }
	    String [] options={"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "done", "next"};
	    int option=JOptionPane.showOptionDialog(null,  txt, "", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	    if(option>=0 && option<=9){
		 return max_id[option];
	    }
	    else if(option==10){
	        return -1;
	    }
	    else{
		for(int j=0; j<10; j++){
	            score[max_id[j]]=-1;
		}
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

    public String row_to_str_sel(int c, ArrayList <Integer> idx){
	ArrayList <String> row=content.get(c);
	String out=row.get(idx.get(0));
	for(int i=1; i<idx.size(); i++){
	    out+="; "+row.get(idx.get(i));
	}
	return out;
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
