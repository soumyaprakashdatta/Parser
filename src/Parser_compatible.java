import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Scanner;


public class Parser_compatible {
    private static Scanner in = new Scanner(System.in);

    // data structures used //

    private static HashMap<String,formula> rules=new HashMap<>();
    private static HashMap<String,String> first_list=new HashMap<>();
    private static HashMap<String,String> follow_list=new HashMap<>();
    public static String parse_table[][]=null;
    private static String first_rule=null;

    public static void main(String[] args) {
        createParseTable();
   }

    private static boolean isTerminal(String token){
        if(rules.get(token)==null)
        {
           // System.out.println(token + " is a terminal.");
            return true;
        }
        //System.out.println(token+" is a non-terminal.");
        return false;
    }

    private static void takeFormulas(){
        System.out.print("Enter number of formulas : ");
        int num=in.nextInt();
        in.nextLine();

        // storing formulas in an array-list //

        for(int i=0;i<num;i++){
            System.out.print("Formulae : ");
            String arguments[]=in.nextLine().split("->");
            formula f=new formula(arguments[0],arguments[1]);
            rules.put(f.left,f);
            if(i==0)
                first_rule=new String(f.left);
        }
    }

    private static void createFirstList(){
        for(String key:rules.keySet()){
            createFirst(key);
        }
    }

    private static void createFirst(String A){
        if(first_list.get(A)==null)
        {
            formula f=rules.get(A);
            String params[]=f.right.split("\\|");
                    for(String param:params){
                        int epsilon_flag=0;  // explained later
                        param=param.trim();
                        String tokens[]=param.split(" ");
                        //for(String token:tokens)
                        //System.out.println(token);

                        int i=0;  // needed to be defined outside the loop , explained later
                        for(String token:tokens) {
                        //for(i=0;i<param.length();i++){

                            // first(terminal)=terminal
                            if(isTerminal(token)){
                                if(first_list.get(A)!=null)
                                    first_list.put(A,first_list.get(A)+" "+token);
                                else
                                    first_list.put(A,token);
                                break;
                            }

                            // else first(Non-terminal) is added
                            else{
                                if(tokens[i].equals(""))
                                    continue;
                                // First check whether the non-terminals first has already been calculated or not
                                // if not then calculate it
                                if(first_list.get(token)==null)
                                    createFirst(token);

                                String nt_first=first_list.get(token);
                                if(nt_first==null)
                                    nt_first="";  // just a precaution
                                String to_add="";  // this will contain first(Non-terminal)-epsilon
                                /*for(int k=0;k<nt_first.length();k++){
                                    // If we find an epsilon in the first
                                    // set the epsilon flag
                                    // it will ensure that we will look for the first for next character
                                    if(nt_first.charAt(k)=='!')
                                        epsilon_flag=1;
                                    else
                                        to_add+=""+nt_first.charAt(k);
                                }
                                */
                                String nt_tokens[]=nt_first.split(" ");
                                for(String ntoken:nt_tokens){
                                    if(ntoken.equals("empty"))
                                        epsilon_flag=1;
                                    else
                                        to_add+=" "+ntoken;
                                }
                                to_add=to_add.trim();

                                if(first_list.get(A)!=null)
                                    first_list.put(A,first_list.get(A)+" "+to_add);
                                else
                                    first_list.put(A,to_add);
                                //System.out.println("toadd : "+to_add);

                                // if epsilon flag is not set then we don't have to look for first for other characters
                                if(epsilon_flag==0)
                                    break;
                            }
                            i++;
                        }

                        // if we have finished all the characters in the param and for that reason came out of loop
                        // then that means that for every character in param first(character) contains epsilon
                        // so we have to add it to the list
                        //System.out.println("i : "+i+"\ttokens length : "+tokens.length);
                        if(i==tokens.length)
                            first_list.put(A,first_list.get(A)+"empty");
                    }
                }


    }

    private static void createFollowList(){
        for(String key:rules.keySet()){
            createFollow(key);
        }
    }

    private static void createFollow(String A){
        if(follow_list.get(A)==null){
            // add $ to the start symbol
            if(A.equals(first_rule))
                follow_list.put(A,"$");
            else
                follow_list.put(A,"");
// In the modified system rules are stored in a hash map
// we have to go through all the formulas in the hash map and search for the required token in the right hand side of all the formulas
            for(String rule_key:rules.keySet()){
                formula f=rules.get(rule_key);
                String params[]=f.right.split("\\|");
                for(String param:params){
                    param=param.trim();
                    String tokens[]=param.split(" ");
                    for(int index=0;index<tokens.length;index++){
                    if(tokens[index].equals(A)){
                        int epsilon_flag=0;
                        int i;
                        for(i=index+1;i<tokens.length;i++) {
                            //if(isTerminal(tokens[i]) && follow_list.get(A).indexOf(tokens[i])==-1){
                                if(isTerminal(tokens[i])){   // fail-safe
                                follow_list.put(A,follow_list.get(A)+" "+tokens[i]);
                                break;
                            }
                            else {
                                if(tokens[i].equals(" ") || tokens[i].equals(""))
                                    continue;
                                String first_next=first_list.get(tokens[i]);
                                if(first_next==null)
                                    first_next="";  // just a precaution
                                String to_add="";
                                String fn_tokens[]=first_next.split(" ");
                                    for(String fntoken:fn_tokens){
                                        if(fntoken.equals("empty"))
                                            epsilon_flag=1;
                                        else if(follow_list.get(A).indexOf(fntoken)==-1)
                                            to_add+=" "+fntoken;
                                    }
                                    to_add=to_add.trim();

                                follow_list.put(A,follow_list.get(A)+" "+to_add);

                                if(epsilon_flag==0)
                                    break;
                            }
                        }
                        if(i==tokens.length){
                            if(follow_list.get(f.left)==null)
                                createFollow(f.left);
                            String follow_left=follow_list.get(f.left);
                            if(follow_left==null)
                                follow_left="";  // just a precaution
                            String to_add="";
                            String fl_tokens[]=follow_left.split(" ");
                            for(String fltoken:fl_tokens){
                                if(follow_list.get(A).indexOf(fltoken)==-1)
                                    to_add+=" "+fltoken;
                            }
                            to_add=to_add.trim();
                            follow_list.put(A,follow_list.get(A)+" "+to_add);
                        }
                    }
                    }
                }
            }
        }
    }

    public static void createParseTable(){
        //takeFormulas();
        useLR();
        createFirstList();
        createFollowList();
        print_first_list();
        print_follow_list();
    }

    static void useLR(){
        lrRemove lr=new lrRemove();
        lr.rl_func();
        first_rule=lr.start_symbol;
        makeItCompatible(lr.parseRules);
    }

    static void makeItCompatible(LinkedHashMap<String,ParseRule> source){
        for(String key:source.keySet()){
            formula f=new formula(key,source.get(key).toString().trim());
            rules.put(key,f);
        }
    }

    static void print_rules(){
        for(String key:rules.keySet()){
            System.out.println("Key : "+key+"\tF-left : " + rules.get(key).left + "\tF-right : " + rules.get(key).right);
        }
    }

    static void print_first_list(){
        int entry_num=first_list.size();
        System.out.println("\n......................................");
        System.out.println("\t\tFirst set");
        System.out.println("......................................\n");
        System.out.println("First list has "+entry_num+" entries .\n");
        if(entry_num>0){
            System.out.println("Name\t\t\t\tEntry");
            System.out.println(".....................................................");
            for(String key:first_list.keySet()){
                System.out.print(key+"\t\t\t\t\t");
                String first=first_list.get(key);
                String tokens[]=first.split(" ");
                for(int i=0;i<tokens.length;i++){
                    System.out.print(tokens[i]);
                    if(i<tokens.length-1)
                        System.out.print(",");
                }
                System.out.println();
            }
        }
    }

    static void print_follow_list(){
        int entry_num=follow_list.size();
        System.out.println("\n......................................");
        System.out.println("\t\tFollow set");
        System.out.println("......................................\n");
        System.out.println("Follow list has "+entry_num+" entries .\n");
        if(entry_num>0){
            System.out.println("Name\t\t\t\tEntry");
            System.out.println("........................................................");
            for(String key:follow_list.keySet()){
                System.out.print(key+"\t\t\t\t\t");
                String follow=follow_list.get(key);
                String tokens[]=follow.split(" ");
                for(int i=0;i<tokens.length;i++){
                    if(tokens[i].equals(""))
                        continue;
                    System.out.print(tokens[i]);
                    if(i<tokens.length-1)
                        System.out.print(",");
                }
                System.out.println();
            }
        }
    }
}
