//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package util;

class Parse<T> {
    private String shortpar;
    private String longpar;
    private String type;
    private T dft;
    private String info;
    private T val;
    private boolean isrequired;

    Parse(String sp, String lp, String type, T dft, boolean isrequired, String info) {
        this.shortpar = sp;
        this.longpar = lp;
        this.type = type;
        this.dft = dft;
        this.isrequired = isrequired;
        this.info = info;
    }

    Parse(String sp, String lp, String type, T dft, boolean isrequired, String info, T val) {
        this.shortpar = sp;
        this.longpar = lp;
        this.type = type;
        this.dft = dft;
        this.isrequired = isrequired;
        this.info = info;
        this.val = val;
    }

    public String toString() {
        return String.format("%s %s %s %s", this.shortpar, this.longpar, this.type, this.info);
    }

    public String getLongpar() {
        return this.longpar;
    }

    public void setLongpar(String longpar) {
        this.longpar = longpar;
    }

    public String getShortpar() {
        return this.shortpar;
    }

    public void setShortpar(String shortpar) {
        this.shortpar = shortpar;
    }

    public String getInfo() {
        return this.info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getDft() {
        return this.dft;
    }

    public void setDft(T dft) {
        this.dft = dft;
    }

    public T getVal() {
        return this.val;
    }

    public void setVal(T val) {
        this.val = val;
    }

    public void setIsdft(boolean isdft) {
        this.isrequired = this.isrequired;
    }

    public boolean getIsrequired() {
        return this.isrequired;
    }
}
