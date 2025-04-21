//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class CommandArgs<T> {
    private Map<String, Parse> commandargs;
    private Map<String, String> s2lpar;
    private String title;
    private String syn;
    private String des;
    private HashSet<String> required;

    public CommandArgs() {
        this.title = "";
        this.syn = "";
        this.des = "";
        this.commandargs = new LinkedHashMap();
        this.s2lpar = new LinkedHashMap();
        this.required = new HashSet();
    }

    public CommandArgs(String title, String syn, String des) {
        this.title = title;
        this.syn = syn;
        this.des = des;
        this.commandargs = new LinkedHashMap();
        this.s2lpar = new LinkedHashMap();
        this.required = new HashSet();
    }

    public void addOption(String par1, String par2, String type, T dft, boolean isrequired, String info) {
        String tmp1 = par1.trim();
        String tmp2 = par2.trim();
        String tmp3 = type.trim();
        String tmp5 = info.trim();
        if (this.commandargs.containsKey(tmp1)) {
            this.error(String.format("%s parameter appears many times", tmp1));
        }

        if (this.commandargs.containsKey(tmp2)) {
            this.error(String.format("%s parameter appears many times", tmp2));
        } else {
            this.commandargs.put(tmp2, new Parse(tmp1, tmp2, tmp3, dft, isrequired, tmp5));
            if (isrequired) {
                this.required.add(tmp2);
            }
        }

        if (this.s2lpar.containsKey(tmp1)) {
            this.error(String.format("%s parameter appears many times", tmp1));
        } else {
            this.s2lpar.put(tmp1, tmp2);
        }

    }

    public void addOption(String par1, String par2, String type, T dft, boolean isrequired, String info, T val) {
        String tmp1 = par1.trim();
        String tmp2 = par2.trim();
        String tmp3 = type.trim();
        String tmp5 = info.trim();
        if (this.commandargs.containsKey(tmp1)) {
            this.error(String.format("%s parameter appears many times", tmp1));
        }

        if (this.commandargs.containsKey(tmp2)) {
            this.error(String.format("%s parameter appears many times", tmp2));
        } else {
            this.commandargs.put(tmp2, new Parse(tmp1, tmp2, tmp3, dft, isrequired, tmp5, val));
            if (isrequired) {
                this.required.add(tmp2);
            }
        }

        if (this.s2lpar.containsKey(tmp1)) {
            this.error(String.format("%s parameter appears many times", tmp1));
        } else {
            this.s2lpar.put(tmp1, tmp2);
        }

    }

    public void parse(String[] args) {
        if (args.length == 0) {
            this.usageMsg();
        }

        for(int i = 0; i < args.length; ++i) {
            if (!args[i].equals("--help") && !args[i].equals("-h")) {
                if (!args[i].equals("--version") && !args[i].equals("-v")) {
                    if (!args[i].startsWith("-")) {
                        this.error(String.format("Error: %s not start with -, please check the parameter.", args[i]));
                    } else {
                        Parse var10000;
                        if (this.commandargs.containsKey(args[i])) {
                            if (i + 1 == args.length) {
                                this.error(String.format("Error: %s requires an argument.", args[i]));
                            }

                            this.required.remove(args[i]);
                            if (((Parse)this.commandargs.get(args[i])).getType().equals("int")) {
                                try {
                                    var10000 = (Parse)this.commandargs.get(args[i]);
                                    ++i;
                                    var10000.setVal(Integer.parseInt(args[i]));
                                } catch (NumberFormatException var7) {
                                    var7.printStackTrace();
                                    this.error(String.format("Error: %s'%s must be int.", args[i - 1], args[i]));
                                }
                            } else if (((Parse)this.commandargs.get(args[i])).getType().equals("float")) {
                                try {
                                    var10000 = (Parse)this.commandargs.get(args[i]);
                                    ++i;
                                    var10000.setVal(Float.parseFloat(args[i]));
                                } catch (NumberFormatException var6) {
                                    var6.printStackTrace();
                                    this.error(String.format("Error: %s'%s must be float.", args[i - 1], args[i]));
                                }
                            } else {
                                var10000 = (Parse)this.commandargs.get(args[i]);
                                ++i;
                                var10000.setVal(args[i]);
                            }
                        } else if (this.s2lpar.containsKey(args[i])) {
                            if (this.commandargs.containsKey(this.s2lpar.get(args[i]))) {
                                if (i + 1 == args.length) {
                                    this.error(String.format("Error: %s requires an argument.", args[i]));
                                }

                                this.required.remove(this.s2lpar.get(args[i]));
                                if (((Parse)this.commandargs.get(this.s2lpar.get(args[i]))).getType().equals("int")) {
                                    try {
                                        var10000 = (Parse)this.commandargs.get(this.s2lpar.get(args[i]));
                                        ++i;
                                        var10000.setVal(Integer.parseInt(args[i]));
                                    } catch (NumberFormatException var5) {
                                        var5.printStackTrace();
                                        this.error(String.format("Error: %s'%s must be int.", args[i - 1], args[i]));
                                    }
                                } else if (((Parse)this.commandargs.get(this.s2lpar.get(args[i]))).getType().equals("float")) {
                                    try {
                                        var10000 = (Parse)this.commandargs.get(this.s2lpar.get(args[i]));
                                        ++i;
                                        var10000.setVal(Float.parseFloat(args[i]));
                                    } catch (NumberFormatException var4) {
                                        var4.printStackTrace();
                                        this.error(String.format("Error: %s'%s must be float.", args[i - 1], args[i]));
                                    }
                                } else {
                                    var10000 = (Parse)this.commandargs.get(this.s2lpar.get(args[i]));
                                    ++i;
                                    var10000.setVal(args[i]);
                                }
                            } else {
                                this.error(String.format("Error: argument not recognized: %s", args[i]));
                            }
                        } else {
                            this.error(String.format("Error: argument not recognized: %s", args[i]));
                        }
                    }
                } else {
                    this.versionMsg();
                }
            } else {
                this.usageMsg();
            }
        }

        if (this.required.size() > 0) {
            this.error(String.format("Error: %s argument must be recognized: ", this.required));
        }

    }

    public T getOption(String str) {
        String tmp = "--" + str;
        if (!this.commandargs.containsKey(tmp)) {
            throw new IllegalArgumentException(String.format("%s does not exist", tmp));
        } else {
            return ((Parse<T>)this.commandargs.get(tmp)).getVal() != null ? ((Parse<T>)this.commandargs.get(tmp)).getVal() : ((Parse<T>)this.commandargs.get(tmp)).getDft();
        }
    }

    public String toString() {
        StringBuffer sbuff = new StringBuffer();
        Iterator var2 = this.commandargs.keySet().iterator();

        while(var2.hasNext()) {
            String x = (String)var2.next();
            sbuff.append(String.format("%s: %s\n", x, ((Parse)this.commandargs.get(x)).toString()));
        }

        return sbuff.toString();
    }

    public void error(String errmsg) {
        System.err.println(errmsg);
        System.exit(1);
    }

    public void usageMsg() {
        System.out.println(String.format("\t\t\t\t%s\t\t", this.title));
        System.out.println("SYNOPSIS");
        System.out.println(String.format("\t\t\t%s\t", this.syn));
        System.out.println("DESCRIPTION");
        System.out.println(String.format("\t\t%s\t", this.des));
        System.out.println(String.format("The options for the program as follows:"));
        Iterator var1 = this.commandargs.keySet().iterator();

        while(var1.hasNext()) {
            String x = (String)var1.next();
            if (x.equals("--help")) {
                System.out.println(String.format("\t%s  %s\t\t %s ", ((Parse)this.commandargs.get(x)).getShortpar(), ((Parse)this.commandargs.get(x)).getLongpar(), ((Parse)this.commandargs.get(x)).getInfo()));
            } else if (x.equals("--version")) {
                System.out.println(String.format("\t%s  %s\t\t %s ", ((Parse)this.commandargs.get(x)).getShortpar(), ((Parse)this.commandargs.get(x)).getLongpar(), ((Parse)this.commandargs.get(x)).getInfo()));
            } else if (!((Parse)this.commandargs.get(x)).getIsrequired()) {
                System.out.println(String.format("\t%s  %s\t\t %s [%s][default: %s]", ((Parse)this.commandargs.get(x)).getShortpar(), ((Parse)this.commandargs.get(x)).getLongpar(), ((Parse)this.commandargs.get(x)).getInfo(), ((Parse)this.commandargs.get(x)).getType(), ((Parse)this.commandargs.get(x)).getDft()));
            } else {
                System.out.println(String.format("\t%s  %s\t\t %s [%s]", ((Parse)this.commandargs.get(x)).getShortpar(), ((Parse)this.commandargs.get(x)).getLongpar(), ((Parse)this.commandargs.get(x)).getInfo(), ((Parse)this.commandargs.get(x)).getType()));
            }
        }

        System.exit(0);
    }

    public void versionMsg() {
        System.out.println(((Parse)this.commandargs.get("--version")).getVal());
        System.exit(0);
    }

    public String getSyn() {
        return this.syn;
    }

    public void setSyn(String syn) {
        this.syn = syn;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDes() {
        return this.des;
    }

    public void setDes(String des) {
        this.des = des;
    }
}
