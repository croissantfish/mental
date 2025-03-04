package cn.edu.sjtu.songyuke.mental.type;

/**
 * Created by Songyu on 16/3/29.
 */
public class MxString extends TypeBase {
    @Override
    public String toString() {
        return "string";
    }

    @Override
    public boolean equals(Object other) {
        if (other != null) {
            return other instanceof MxString;
        }
        return false;
    }
}
