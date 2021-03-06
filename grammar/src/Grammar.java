
import java.util.*;

/**
 * 文法
 */
public class Grammar {

    private ArrayList<Rule> rules; // 文法规则
    private HashSet<String> terminals; //终结符
    private HashSet<String> variables;
    private String startVariable; // 文法开始符号
    private HashMap<String, HashSet<String>> firstSets; // 所有的首符集
    private HashMap<String, HashSet<String>> followSets; // 所有的follow

    public Grammar(String s) {

        rules = new ArrayList<>();
        terminals = new HashSet<>(); // 终结符
        variables = new HashSet<>(); // 非终结符

        int line = 0;
        for(String st : s.split("\n")) {
            System.out.println(st);
            String[] sides = st.split("->");
            String leftSide = sides[0].trim();
            // 左边必定是非终结符
            variables.add(leftSide);
            String[] rulesRightSide = sides[1].trim().split(" ");
            for (String word : rulesRightSide) {
                if (isTerminal(word)) {
                    terminals.add(word);
                }
            }

            if (line == 0) {
                startVariable = leftSide;
                rules.add(new Rule("S'", new String[]{"program"}));
            }
            rules.add(new Rule(leftSide, rulesRightSide));
            line++;

        }
        System.out.println("Rules: ");
        for (int i = 0;i < rules.size();i++) {
            System.out.println(i + " : " + rules.get(i));
        }


        computeFirstSets();
        outputFirstsets();
        computeFollowSet();
        outputFollowsets();
    }
    // 判断是否是终结符
    public boolean isTerminal(String word) {
        if (word.equals("program") || word.equals("S") || word.equals("decls") || word.equals("decl") || word.equals("type") ||
            word.equals("stmts") || word.equals("stmt") || word.equals("loc") || word.equals("bool") || word.equals("join") ||
            word.equals("equality") || word.equals("rel") || word.equals("expr") || word.equals("term") || word.equals("unary") ||
            word.equals("factor") || word.equals("epsilon")) {
            return false;
        }
        return true;
    }
    /**
     * 输出全部的first集合
     */
    public void outputFirstsets() {
        Set<String> strings = firstSets.keySet();
        for (String string : strings) {
            System.out.print("first集合为:" + string + ":");
            HashSet<String> strings1 = firstSets.get(string);
            System.out.println(strings1);
        }
    }

    /**
     * 输出全部的follow集合
     */
    public void outputFollowsets() {
        Set<String> stringSet = followSets.keySet();
        for (String string : stringSet) {
            System.out.println("follow集合为:" + string + ":");
            HashSet<String> strings1 = followSets.get(string);
            System.out.println(strings1);
        }
    }
    public ArrayList<Rule> getRules() {
        return rules;
    }

    /**
     * 找到规则的下标
     * @param rule
     * @return
     */
    public int findRuleIndex(Rule rule){
        for(int i=0 ; i<rules.size();i++){
            if(rules.get(i).equals(rule)){
                return i;
            }
        }
        return -1;
    }


    public HashSet<String> getVariables() {
        return variables;
    }

    public String getStartVariable() {
        return startVariable;
    }

    // 计算所有的first集合
    private void computeFirstSets() {
        firstSets = new HashMap<>();

        for (String s : variables) {
            HashSet<String> temp = new HashSet<>();
            firstSets.put(s, temp);
        }
        while (true) {
            // 判断首付集合是否变化
            boolean isChanged = false;
            // 对于所有的非终结符
            for (String variable : variables) {
                HashSet<String> firstSet = new HashSet<>();
                for (Rule rule : rules) {
                    if (rule.getLeftSide().equals(variable)) {
                        HashSet<String> addAll = computeFirst(rule.getRightSide(), 0);
                        firstSet.addAll(addAll);
                    }
                }
                if (!firstSets.get(variable).containsAll(firstSet)) {
                    isChanged = true;
                    firstSets.get(variable).addAll(firstSet);
                }

            }
            if (!isChanged) {
                break;
            }
        }

        firstSets.put("S", firstSets.get(startVariable));
    }

    // 计算所有的follow集合
    private void computeFollowSet() {
        followSets = new HashMap<>();
        for (String s : variables) {
            HashSet<String> temp = new HashSet<>();
            followSets.put(s, temp);
        }
        HashSet<String> start = new HashSet<>();
        start.add("$");
        followSets.put("S'", start);

        while (true) {
            boolean isChange = false;
            for (String variable : variables) {
                for (Rule rule : rules) {
                    for (int i = 0; i < rule.getRightSide().length; i++) {
                        // 如果第i个为非终结符
                        if (rule.getRightSide()[i].equals(variable)) {
                            HashSet<String> first;
                            if (i == rule.getRightSide().length - 1) {
                                first = followSets.get(rule.leftSide);
                            }
                            else {
                                first = computeFirst(rule.getRightSide(), i + 1);
                                if (first.contains("epsilon")) {
                                    first.remove("epsilon");
                                    first.addAll(followSets.get(rule.leftSide));
                                }
                            }
                            if (!followSets.get(variable).containsAll(first)) {
                                isChange = true;
                                followSets.get(variable).addAll(first);
                            }
                        }
                    }
                }
            }
            if (!isChange) {
                break;
            }
        }
    }

    // 计算给定右部的first集合
    public HashSet<String> computeFirst(String[] string, int index) {
        HashSet<String> first = new HashSet<>();
        if (index == string.length) {
            return first;
        }
        if (terminals.contains(string[index]) || string[index].equals("epsilon")) {
            first.add(string[index]);
            return first;
        }

        if (variables.contains(string[index])) {
            for (String str : firstSets.get(string[index])) {
                first.add(str);
            }
        }

        if (first.contains("epsilon")) {
            if (index != string.length - 1) {
                first.remove("epsilon");
                first.addAll(computeFirst(string, index + 1));
            }
        }
        return first;
    }

    // 根据非终结符找到对应的规则
    public HashSet<Rule> getRuledByLeftVariable(String variable) {
        HashSet<Rule> variableRules = new HashSet<>();
        for (Rule rule : rules) {
            if (rule.getLeftSide().equals(variable)) {
                variableRules.add(rule);
            }
        }
        return variableRules;
    }

    // 输出终结符集合
    public void outputTerminals() {
        for (String terminal : terminals) {
            System.out.print(terminal + " ");
        }
        System.out.println();
    }

    // 输出非终结符
    public void outputVariables() {
        for (String variabe : variables) {
            System.out.print(variabe + " ");
        }
        System.out.println();
    }
    // 判断是否是非终结符
    public boolean isVariable(String s) {
        return variables.contains(s);
    }

    public HashMap<String, HashSet<String>> getFirstSets() {
        return firstSets;
    }

    public HashMap<String, HashSet<String>> getfollowSets() {
        return followSets;
    }

    public HashSet<String> getTerminals() {
        return terminals;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.rules);
        hash = 37 * hash + Objects.hashCode(this.terminals);
        hash = 37 * hash + Objects.hashCode(this.variables);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Grammar other = (Grammar) obj;
        if (!Objects.equals(this.rules, other.rules)) {
            return false;
        }
        if (!Objects.equals(this.terminals, other.terminals)) {
            return false;
        }
        if (!Objects.equals(this.variables, other.variables)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String str = "";
        for(Rule rule: rules){
            str += rule + "\n";
        }
        return str;
    }
}
