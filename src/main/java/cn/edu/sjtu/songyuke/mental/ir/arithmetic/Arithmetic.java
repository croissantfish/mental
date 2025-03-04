package cn.edu.sjtu.songyuke.mental.ir.arithmetic;

import cn.edu.sjtu.songyuke.mental.ir.Instruction;
import cn.edu.sjtu.songyuke.mental.ir.data.DataValue;

/**
 * Created by Songyu on 16/4/25.
 */
public abstract class Arithmetic extends Instruction {
    public DataValue res;

    public Arithmetic() {
        this.res = null;
    }
}
